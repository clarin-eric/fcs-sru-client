/**
 * This software is copyright (c) 2011 by
 *  - Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.client;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A multithreaded asynchronous SRU client.
 */
public class SRUAsyncClient {
    /**
     * An interface for signaling SRUClient errors.
     */
    public interface ErrorHandler {
        public void handleError(SRUClientException e);
    }

    /** default version the client will use, if not otherwise specified */
    public static final SRUVersion DEFAULT_SRU_VERSION = SRUVersion.VERSION_1_2;
    private static final ErrorHandler NOOP_ERROR_HANDLER = new ErrorHandler() {
        @Override
        public void handleError(SRUClientException e) {
        }
    };
    private static final int INITIAL_QUEUE_SIZE = 128;
    private static final Logger logger =
            LoggerFactory.getLogger(SRUAsyncClient.class);
    private ConcurrentMap<String, SRURecordDataParser> parsers =
            new ConcurrentHashMap<String, SRURecordDataParser>();
    private final SRUVersion defaultVersion;
    private final boolean strictMode;
    private final int workerCount;
    private volatile int runState = RUNSTATE_RUNNING;
    private volatile ErrorHandler errorHandler = NOOP_ERROR_HANDLER;
    private Set<Worker> workers = new HashSet<Worker>();
    private ReentrantLock queueLock = new ReentrantLock(true);
    private Condition queueCondition = queueLock.newCondition();
    private Queue<Request> queue = new ArrayDeque<Request>(INITIAL_QUEUE_SIZE);
    private int idleCount;

    /**
     * Constructor. This constructor will create a <em>strict</em> client and
     * use the default SRU version.
     *
     * @see #SRUParallelClient(SRUVersion, boolean)
     * @see #DEFAULT_SRU_VERSION
     */
    public SRUAsyncClient() {
        this(DEFAULT_SRU_VERSION, true,
             Runtime.getRuntime().availableProcessors() * 2);
    }


    /**
     * Constructor. This constructor will create a <em>strict</em> client and
     * use the default SRU version.
     *
     * @param workerCount
     *            number of worker threads
     * @see #SRUParallelClient(SRUVersion, boolean)
     * @see #DEFAULT_SRU_VERSION
     */
    public SRUAsyncClient(int workerCount) {
        this(DEFAULT_SRU_VERSION, true, workerCount);
    }


    /**
     * Constructor. This constructor will create a <em>strict</em> client.
     *
     * @param defaultVersion
     *            the default version to use for SRU requests; may be overridden
     *            by individual requests
     * @param workerCount
     *            number of worker threads
     * @see #SRUParallelClient(SRUVersion, boolean)
     */
    public SRUAsyncClient(SRUVersion defaultVersion, int workerCount) {
        this(defaultVersion, true, workerCount);
    }


    /**
     * Constructor.
     *
     * @param defaultVersion
     *            the default version to use for SRU requests; may be overridden
     *            by individual requests
     * @param strictMode
     *            if <code>true</code> the client will strictly adhere to the
     *            SRU standard and raise fatal errors on violations, if
     *            <code>false</code> it will act more forgiving and ignore
     *            certain violations
     * @param workerCount
     *            number of worker threads
     */
    public SRUAsyncClient(SRUVersion defaultVersion, boolean strictMode,
            int workerCount) {
        if (defaultVersion == null) {
            throw new NullPointerException("version == null");
        }
        this.defaultVersion = defaultVersion;
        this.strictMode     = strictMode;
        this.workerCount    = workerCount;
        startup();
    }


    /**
     * Register a record data parser.
     *
     * <p>
     * NB: The record data parsers will be shared amongst worker threads and
     * therefore implementations need to be thread-safe
     * </p>
     *
     * @param parser
     *            a parser instance
     * @throws SRUClientException
     *             if a parser handing the same record schema is already
     *             registered
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @throws IllegalArgumentException
     *             if the supplied parser is invalid
     */
    public void registerRecordParser(SRURecordDataParser parser)
            throws SRUClientException {
        if (parser == null) {
            throw new NullPointerException("parser == null");
        }
        final String recordSchema = parser.getRecordSchema();
        if (recordSchema == null) {
            throw new NullPointerException("parser.getRecordSchema() == null");
        }
        if (recordSchema.isEmpty()) {
            throw new IllegalArgumentException(
                    "parser.getRecordSchema() returns empty string");
        }

        if (parsers.putIfAbsent(recordSchema, parser) != null) {
            throw new SRUClientException(
                    "record data parser already registered: " + recordSchema);

        }
    }


    /**
     * Perform a <em>explain</em> operation.
     *
     * @param request
     *            an instance of a {@link SRUExplainRequest} object
     * @param handler
     *            an instance of {@link SRUExplainHandler} to receive callbacks
     *            when processing the result of this request
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @see SRUExplainRequest
     * @see SRUExplainHandler
     */
    public void explain(final SRUExplainRequest request,
            final SRUExplainHandler handler) throws SRUClientException {
        if (request == null) {
            throw new NullPointerException("request == null");
        }
        if (handler == null) {
            throw new NullPointerException("handler == null");
        }
        if (runState != RUNSTATE_RUNNING) {
            throw new SRUClientException("client is shutting down");
        }
        submitRequest(new Request() {
            @Override
            String getEndpointURI() {
                return request.getEndpointURI();
            }

            @Override
            void perform(SRUClient client) throws SRUClientException {
                client.explain(request, handler);
            }
        });
    }


    /**
     * Perform a <em>scan</em> operation.
     *
     * @param request
     *            an instance of a {@link SRUScanRequest} object
     * @param handler
     *            an instance of {@link SRUScanHandler} to receive callbacks
     *            when processing the result of this request
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @see SRUScanRequest
     * @see SRUScanHandler
     */
    public void scan(final SRUScanRequest request,
            final SRUScanHandler handler) throws SRUClientException {
        if (request == null) {
            throw new NullPointerException("request == null");
        }
        if (handler == null) {
            throw new NullPointerException("handler == null");
        }
        if (runState != RUNSTATE_RUNNING) {
            throw new SRUClientException("client is shutting down");
        }
        submitRequest(new Request() {
            @Override
            String getEndpointURI() {
                return request.getEndpointURI();
            }

            @Override
            void perform(SRUClient client) throws SRUClientException {
                client.scan(request, handler);
            }
        });
    }



    /**
     * Perform a <em>searchRetreive</em> operation.
     *
     * @param request
     *            an instance of a {@link SRUSearchRetrieveRequest} object
     * @param handler
     *            an instance of {@link SRUSearchRetrieveHandler} to receive
     *            callbacks when processing the result of this request
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @see SRUSearchRetrieveRequest
     * @see SRUSearchRetrieveHandler
     */
    public void searchRetrieve(final SRUSearchRetrieveRequest request,
            final SRUSearchRetrieveHandler handler) throws SRUClientException {
        if (request == null) {
            throw new NullPointerException("request == null");
        }
        if (handler == null) {
            throw new NullPointerException("handler == null");
        }
        if (runState != RUNSTATE_RUNNING) {
            throw new SRUClientException("client is shutting down");
        }
        submitRequest(new Request() {
            @Override
            String getEndpointURI() {
                return request.getEndpointURI();
            }

            @Override
            void perform(SRUClient client) throws SRUClientException {
                client.searchRetrieve(request, handler);
            }
        });
    }


    /**
     * Get the currently registered error handler.
     *
     * @return the current {@link ErrorHandler} in use
     */
    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }


    /**
     * Set a error handler.
     *
     * @param errorHandler
     *            the new {@link ErrorHandler} to be used
     * @throws NullPointerException
     *             if errorHandler is <code>null</code>
     */
    public void setErrorHandler(ErrorHandler errorHandler) {
        if (errorHandler == null) {
            throw new NullPointerException("errorHandler == null");
        }
        this.errorHandler = errorHandler;
    }


    /**
     * Get the total number of worker threads, either idle or busy.
     *
     * @return the total number of worker threads
     */
    public int getTotalWorkerCount() {
        return workerCount;
    }


    /**
     * Get the number of idle worker threads.
     *
     * @return number of idle worker threads
     */
    public int getIdleWorkersCount() {
        queueLock.lock();
        try {
            return idleCount;
        } finally {
            queueLock.unlock();
        }
    }


    /**
     * Invokes <code>shutdown</code> when this no longer referenced
     */
    @Override
    protected void finalize() throws Throwable {
        shutdownNow();
    }


    /**
     * Terminate the client but drain queued requests.
     */
    public void shutdown() {
        doShutdown(true);
    }


    /**
     * Terminate the client and skip queued requests.
     */
    public void shutdownNow() {
        doShutdown(false);
    }


    private void startup() {
        logger.debug("launching workers");
        synchronized (workers) {
            for (int i = 0; i < workerCount; i++) {
                SRUClient client = new SRUClient(defaultVersion, strictMode,
                        Collections.unmodifiableMap(parsers));
                Worker worker = new Worker(client);
                Thread thread = new Thread(worker);
                worker.thread = thread;
                thread.start();
                workers.add(worker);
            }
        } // synchronized
    }


    private void doShutdown(boolean drainQueue) {
        logger.debug("shutting down workers");
        if (drainQueue) {
            if (runState != RUNSTATE_STOP) {
                runState = RUNSTATE_SHUTDOWN;
            }
        } else {
            runState = RUNSTATE_STOP;
        }

        synchronized (workers) {
            do {
                // kill workers, if forced shutdown
                if (runState == RUNSTATE_STOP) {
                    for (Worker worker : workers) {
                        worker.thread.interrupt();
                    }
                }

                // wait for all workers to shutdown
                try {
                    workers.wait();
                } catch (InterruptedException ignore) {
                    /* IGNORE */
                }
            } while (!workers.isEmpty());
        } // synchronized
        logger.debug("all workers shut down");
        runState = RUNSTATE_TERMINATED;
    }


    private void workerAfterShutdown(Worker worker) {
        synchronized (workers) {
            workers.remove(worker);

            if ((runState == RUNSTATE_STOP) && !workers.isEmpty()) {
                for (Worker w : workers) {
                    if (!w.busy) {
                        w.thread.interrupt();
                    }
                }
            }

            if (workers.isEmpty()) {
                workers.notify();
            }
        } // synchronized
    }


    private void submitRequest(Request request) {
        queueLock.lock();
        try {
            // submit to queue
            queue.offer(request);

            // wake up a sleeping thread
            queueCondition.signal();
        } finally {
            queueLock.unlock();
        }
    }


    private Request pollRequest() {
        queueLock.lock();
        try {
            idleCount++;
            for (;;) {
                Request request = queue.poll();

                if ((runState == RUNSTATE_SHUTDOWN) && queue.isEmpty()) {
                    runState = RUNSTATE_STOP;
                }

                if (request == null) {
                    if (runState == RUNSTATE_STOP) {
                        return null;
                    }
                    // no work, sleep
                    queueCondition.await();
                } else {
                    return request;
                }
            } // for
        } catch (InterruptedException e) {
            return null;
        } finally {
            idleCount++;
            queueLock.unlock();
        }
    }


    private static final int RUNSTATE_RUNNING      = 1;
    private static final int RUNSTATE_SHUTDOWN     = 2;
    private static final int RUNSTATE_STOP         = 3;
    private static final int RUNSTATE_TERMINATED = 4;

    private abstract class Request {
        abstract String getEndpointURI();
        abstract void perform(SRUClient client) throws SRUClientException;
    }


    private class Worker implements Runnable {
        private Thread thread;
        private final SRUClient client;
        private volatile boolean busy;


        Worker(SRUClient client) {
            this.client = client;
        }


        @Override
        public void run() {
            try {
                logger.debug("worker started");

                // main loop
                while (runState < RUNSTATE_STOP) {
                    logger.trace("polling queue");
                    busy = false;
                    Request request = pollRequest();
                    busy = true;
                    if (request == null) {
                        continue;
                    }

                    try {
                        logger.debug("performing request to endpoint {}",
                                request.getEndpointURI());
                        request.perform(client);
                    } catch (SRUClientException e) {
                        logger.debug("caught client exception: {}",
                                e.getMessage());
                        try {
                            errorHandler.handleError(e);
                        } catch (Throwable ignore) {
                            /* IGNORE */
                        }
                    }
                } // while
            } finally {
                workerAfterShutdown(this);
                logger.debug("worker terminated");
            }
        }
    } // inner class Worker

} // class SRUAsyncClient

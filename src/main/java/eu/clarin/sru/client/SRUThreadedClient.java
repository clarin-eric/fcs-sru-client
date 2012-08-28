/**
 * This software is copyright (c) 2011-2012 by
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

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A client to perform SRU operations in parallel. The response of a SRU request
 * is wrapped in a SRU response.
 * <p>
 * This client is reusable and thread-safe: the application may reuse a
 * client object and may shared it between multiple threads. <br />
 * NB: The registered {@link SRURecordDataParser} need to be thread-safe
 * </p> 
 */
public class SRUThreadedClient {
    private static final Logger logger =
            LoggerFactory.getLogger(SRUThreadedClient.class);
    private ConcurrentMap<String, SRURecordDataParser> parsers =
            new ConcurrentHashMap<String, SRURecordDataParser>();
    private final ThreadLocal<SRUClient> client;
    private final ExecutorService executor;


    /**
     * Constructor. This constructor will create a <em>strict</em> client and
     * use the default SRU version.
     *
     * @see #SRUThreadedClient(SRUVersion, boolean)
     * @see SRUSimpleClient#DEFAULT_SRU_VERSION
     */
    public SRUThreadedClient() {
        this(SRUSimpleClient.DEFAULT_SRU_VERSION, true);
    }


    /**
     * Constructor. This constructor will create a <em>strict</em> client.
     *
     * @param defaultVersion
     *            the default version to use for SRU requests; may be overridden
     *            by individual requests
     * @see #SRUThreadedClient(SRUVersion, boolean)
     */
    public SRUThreadedClient(SRUVersion defaultVersion) {
        this(defaultVersion, true);
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
     */
    public SRUThreadedClient(final SRUVersion defaultVersion,
            final boolean strictMode) {
        client = new ThreadLocal<SRUClient>() {
            @Override
            protected SRUClient initialValue() {
                logger.debug("instantiated new sru client");
                return new SRUClient(defaultVersion, strictMode, parsers);
            }
        };
        // TODO: make worker count configurable
        int workerCount = Runtime.getRuntime().availableProcessors() * 2;
        logger.debug("using {} workers", workerCount);
        executor = Executors.newFixedThreadPool(workerCount, new Factory());
    }


    /**
     * Register a record data parser.
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
     * @return a {@link SRUExplainResponse} object
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     */
    public Future<SRUExplainResponse> explain(SRUExplainRequest request)
            throws SRUClientException {
        if (request == null) {
            throw new NullPointerException("request == null");
        }
        if (executor.isShutdown()) {
            throw new SRUClientException("client is shutting down");
        }
        return executor.submit(new Request<SRUExplainRequest, SRUExplainResponse>(request) {
            @Override
            public SRUExplainResponse doRequest(SRUClient client)
                    throws SRUClientException {
                return client.explain(request);
            }
        });
    }


    /**
     * Perform a <em>scan</em> operation.
     *
     * @param request
     *            an instance of a {@link SRUScanRequest} object
     * @return a {@link SRUScanResponse} object
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     */
    public Future<SRUScanResponse> scan(SRUScanRequest request)
            throws SRUClientException {
        if (request == null) {
            throw new NullPointerException("request == null");
        }
        if (executor.isShutdown()) {
            throw new SRUClientException("client is shutting down");
        }
        return executor.submit(new Request<SRUScanRequest, SRUScanResponse>(
                request) {
            @Override
            public SRUScanResponse doRequest(SRUClient client)
                    throws SRUClientException {
                return client.scan(request);
            }
        });
    }


    /**
     * Perform a <em>searchRetrieve</em> operation.
     *
     * @param request
     *            an instance of a {@link SRUSearchRetrieveRequest} object
     * @return a {@link SRUSearchRetrieveRequest} object
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     */
    public Future<SRUSearchRetrieveResponse> searchRetrieve(
            SRUSearchRetrieveRequest request) throws SRUClientException {
        if (request == null) {
            throw new NullPointerException("request == null");
        }
        if (executor.isShutdown()) {
            throw new SRUClientException("client is shutting down");
        }
        return executor.submit(new Request<SRUSearchRetrieveRequest, SRUSearchRetrieveResponse>(request) {
            @Override
            protected SRUSearchRetrieveResponse doRequest(SRUClient client)
                    throws SRUClientException {
                return client.searchRetrieve(request);
            }
        });
    }


    /**
     * Invokes <code>shutdown</code> when this no longer referenced
     */
    @Override
    protected void finalize() throws Throwable {
        shutdown();
    }


    /**
     * Initiates an orderly shutdown in which previously submitted requests are
     * executed, but no new requests will be accepted.
     */
    public void shutdown() {
        executor.shutdown();
    }


    /**
     * Terminate the client but drain queued requests.
     */
    public void shutdownNow() {
        executor.shutdownNow();
    }


    private class Factory implements ThreadFactory {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r) {
                @Override
                public void run() {
                    try {
                        logger.debug("launched new worker");

                        // pre-initialize client
                        client.get();

                        // do work
                        super.run();
                    } finally {
                        // do not leak resources and clean ThreadLocal ...
                        client.remove();
                        logger.debug("cleared sru client");
                    }
                }
            };
        }
    }


    private abstract class Request<V, S> implements Callable<S> {
        protected final V request;


        Request(V request) {
            this.request = request;
        }


        @Override
        public final S call() throws Exception {
            return doRequest(client.get());
        }


        protected abstract S doRequest(SRUClient client)
                throws SRUClientException;
    }

} // class SRUThreadedClient

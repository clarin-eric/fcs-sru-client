/**
 * This software is copyright (c) 2012-2014 by
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.xml.parsers.DocumentBuilderFactory;

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
    private final DocumentBuilderFactory documentBuilderFactory =
            DocumentBuilderFactory.newInstance();
    private final ThreadLocal<SRUClient> client;
    private final ExecutorService executor;


    /**
     * Constructor.
     *
     * @param config
     *            the configuration to be used for this client.
     * @throws NullPointerException
     *             if argument <code>config</code> is <node>null</code>
     * @throws IllegalArgumentException
     *             if an error occurred while registering record data parsers
     * @see SRUClientConfig
     */
   public SRUThreadedClient(final SRUClientConfig config) {
        client = new ThreadLocal<SRUClient>() {
            @Override
            protected SRUClient initialValue() {
                logger.debug("instantiated new sru client");
                return new SRUClient(config, documentBuilderFactory);
            }
        };

        // launch workers ...
        final int threadCount = config.getThreadCount();
        logger.debug("using {} workers", threadCount);
        executor = Executors.newFixedThreadPool(threadCount, new Factory());
    }


    /**
     * Perform a <em>explain</em> operation.
     *
     * @param request
     *            an instance of a {@link SRUExplainRequest} object
     * @return a {@link Future} object that wraps a {@link SRUExplainResponse}
     *         object
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @deprecated Use asynchronous callback interface
     *             {@link #explain(SRUExplainRequest, SRUCallback)}. This method
     *             will be removed in the future.
     */
    @Deprecated
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
     * Perform a <em>explain</em> operation and invoke a user supplied callback
     * after the request has been completed.
     *
     * @param request
     *            an instance of a {@link SRUExplainRequest} object
     * @param callback
     *            the callback to be invoked
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @see SRUCallback
     */
    public void explain(final SRUExplainRequest request,
            final SRUCallback<SRUExplainRequest, SRUExplainResponse> callback)
            throws SRUClientException {
        if (request == null) {
            throw new NullPointerException("request == null");
        }
        if (callback == null) {
            throw new NullPointerException("callback == null");
        }
        if (executor.isShutdown()) {
            throw new SRUClientException("client is shutting down");
        }
        executor.submit(new AsyncRequest<SRUExplainRequest, SRUExplainResponse>(
                request, callback) {
            @Override
            protected SRUExplainResponse doRequest(SRUClient client)
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
     * @return a {@link Future} object that wraps a {@link SRUScanResponse}
     *         object
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @deprecated Use asynchronous callback interface
     *             {@link #scan(SRUScanRequest, SRUCallback)}. This method
     *             will be removed in the future.
     */
    @Deprecated
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
     * Perform a <em>scan</em> operation and invoke a user supplied callback
     * after the request has been completed.
     *
     * @param request
     *            an instance of a {@link SRUScanRequest} object
     * @param callback
     *            the callback to be invoked
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @see SRUCallback
     */
    public void scan(final SRUScanRequest request,
            final SRUCallback<SRUScanRequest, SRUScanResponse> callback)
            throws SRUClientException {
        if (request == null) {
            throw new NullPointerException("request == null");
        }
        if (callback == null) {
            throw new NullPointerException("callback == null");
        }
        if (executor.isShutdown()) {
            throw new SRUClientException("client is shutting down");
        }
        executor.submit(new AsyncRequest<SRUScanRequest, SRUScanResponse>(
                request, callback) {
            @Override
            protected SRUScanResponse doRequest(SRUClient client)
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
     * @return a {@link Future} object that wraps a {@link SRUExplainResponse}
     *         object
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @deprecated Use asynchronous callback interface
     *             {@link #searchRetrieve(SRUSearchRetrieveRequest, SRUCallback)}
     *             . This method will be removed in the future.
     */
    @Deprecated
    public Future<SRUSearchRetrieveResponse> searchRetrieve(
            SRUSearchRetrieveRequest request) throws SRUClientException {
        if (request == null) {
            throw new NullPointerException("request == null");
        }
        if (executor.isShutdown()) {
            throw new SRUClientException("client is shutting down");
        }
        return executor.submit(new Request<SRUSearchRetrieveRequest,
                SRUSearchRetrieveResponse>(request) {
            @Override
            protected SRUSearchRetrieveResponse doRequest(SRUClient client)
                    throws SRUClientException {
                return client.searchRetrieve(request);
            }
        });
    }


    /**
     * Perform a <em>searchRetrieve</em> operation and invoke a user supplied
     * callback after the request has been completed.
     *
     * @param request
     *            an instance of a {@link SRUSearchRetrieveRequest} object
     * @param callback
     *            the callback to be invoked
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @see SRUCallback
     */
    public void searchRetrieve(final SRUSearchRetrieveRequest request,
            final SRUCallback<SRUSearchRetrieveRequest, SRUSearchRetrieveResponse> callback)
            throws SRUClientException {
        if (request == null) {
            throw new NullPointerException("request == null");
        }
        if (callback == null) {
            throw new NullPointerException("callback == null");
        }
        if (executor.isShutdown()) {
            throw new SRUClientException("client is shutting down");
        }
        executor.submit(new AsyncRequest<SRUSearchRetrieveRequest,
                SRUSearchRetrieveResponse>(request, callback) {
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


    private abstract class Request<V extends SRUAbstractRequest, S extends SRUAbstractResponse<V>>
            implements Callable<S> {
        protected final V request;
        private long now = System.nanoTime();


        Request(V request) {
            this.request = request;
        }


        @Override
        public final S call() throws Exception {
            final SRUClient c = client.get();
            c.setTimeQueued(System.nanoTime() - now);
            return doRequest(c);
        }


        protected abstract S doRequest(SRUClient client)
                throws SRUClientException;
    }


    private abstract class AsyncRequest<V extends SRUAbstractRequest,
                                        S extends SRUAbstractResponse<V>>
            implements Runnable {
        protected final V request;
        private long now = System.nanoTime();
        private final SRUCallback<V, S> callback;


        public AsyncRequest(V request, SRUCallback<V, S> callback) {
            this.callback = callback;
            this.request = request;
        }


        @Override
        public void run() {
            try {
                try {
                    final SRUClient c = client.get();
                    c.setTimeQueued(System.nanoTime() - now);
                    final S response = doRequest(c);
                    callback.onSuccess(response);
                } catch (SRUClientException e) {
                    callback.onError(request, e);
                }
            } catch (Throwable t) {
                callback.onError(request, new SRUClientException(
                        "unexpected error while processing the request", t));
            }
        }


        protected abstract S doRequest(SRUClient client)
                throws SRUClientException;
    }

} // class SRUThreadedClient

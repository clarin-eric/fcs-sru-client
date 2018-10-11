/**
 * This software is copyright (c) 2012-2016 by
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * A class for encapsulating the configuration of an SRU client.
 */
public class SRUClientConfig {
    /** default version the client will use, if not otherwise specified */
    public static final SRUVersion DEFAULT_SRU_VERSION = SRUVersion.VERSION_1_2;
    /** default connect timeout to be used, if not otherwise specified */
    public static final int DEFAULT_CONNECT_TIMEOUT = -1;
    /** default socket timeout to be used, if not otherwise specified */
    public static final int DEFAULT_SOCKET_TIMEOUT  = -1;
    private final SRUVersion defaultVersion;
    private final int connectTimeout;
    private final int socketTimeout;
    private final CloseableHttpClient httpClient;
    private final HttpClientContext httpContext;
    private final int threadCount;
    private final List<SRURecordDataParser> recordParsers;
    private final List<SRUExtraResponseDataParser> extraDataParsers;


    /**
     * Get default SRU version to be used.
     *
     * @return the default SRU version to be used.
     */
    public SRUVersion getDefaultVersion() {
        return defaultVersion;
    }


    /**
     * Get the connect timeout. This value is ignored if a customized HTTP
     * client is provided.
     *
     * @return the connect timeout
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }


    /**
     * Get the socket timeout. This value is ignored if a customized HTTP client
     * is provided.
     *
     * @return the connect timeout
     */
    public int getSocketTimeout() {
        return socketTimeout;
    }


    /**
     * Get the customized HTTP client which is to be used.
     * 
     * @return a configured HTTP client instance or <code>null</code>
     */
    public CloseableHttpClient getCustomizedHttpClient() {
        return httpClient;
    }


    /**
     * Get the HTTP client context which is to be used. Only relevant, if a
     * customized HTTP client is set, see {{@link #getCustomizedHttpClient()}.
     * 
     * @return a HTTP client context instance or <code>null</code>
     */
    public HttpClientContext getHttpClientContext() {
        return httpContext;
    }


    /**
     * Get the number of worker threads. This value is only relevant for the
     * {@link SRUThreadedClient}.
     *
     * @return the number of worker threads
     */
    public int getThreadCount() {
        return threadCount;
    }


    /**
     * Get the list of record data parsers to be used.
     *
     * @return the list of record data parsers.
     */
    public List<SRURecordDataParser> getRecordDataParsers() {
        return recordParsers;
    }


    /**
     * Get the list of extra response data parsers to be used.
     *
     * @return the list of extra response data parsers.
     */
    public List<SRUExtraResponseDataParser> getExtraResponseDataParsers() {
        return extraDataParsers;
    }


    private SRUClientConfig(Builder builder) {
        if (builder == null) {
            throw new NullPointerException("builder == null");
        }
        this.defaultVersion   = builder.defaultVersion;
        this.connectTimeout   = builder.connectTimeout;
        this.socketTimeout    = builder.socketTimeout;
        if (builder.httpClient != null) {
            this.httpClient  = builder.httpClient;
            this.httpContext = builder.httpContext;
        } else {
            this.httpClient  = null;
            this.httpContext = null;
        }
        this.threadCount      = builder.threadCount;
        if (builder.recordParsers != null) {
            this.recordParsers =
                    Collections.unmodifiableList(builder.recordParsers);
        } else {
            this.recordParsers = null;
        }
        if (builder.extraDataParsers != null) {
            this.extraDataParsers =
                    Collections.unmodifiableList(builder.extraDataParsers);
        } else {
            this.extraDataParsers = null;
        }
    }


    /**
     * Get a new builder for creating a new {@link SRUClientConfig} instance.
     * 
     * @return a builder instance
     */
    public static Builder builder() {
        return new Builder();
    }


    /**
     * A class that implements the builder pattern to create
     * {@link SRUClientConfig} instances.
     *
     * @see SRUClientConfig
     */
    public static class Builder {
        private SRUVersion defaultVersion = DEFAULT_SRU_VERSION;
        private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
        private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;
        private CloseableHttpClient httpClient = null;
        private HttpClientContext httpContext = null;
        private int threadCount =
                Runtime.getRuntime().availableProcessors() * 2;
        private List<SRURecordDataParser> recordParsers =
                new ArrayList<SRURecordDataParser>();
        private List<SRUExtraResponseDataParser> extraDataParsers = null;


        /**
         * Constructor.
         *
         */
        public Builder() {
        }


        /**
         * Set the default SRU version to be used.
         *
         * @param defaultVersion
         *            the default SRU version to be used
         * @return this {@link Builder} instance
         * @throws NullPointerException
         *             if a mandatory argument is <code>null</code>
         */
        public Builder setDefaultVersion(SRUVersion defaultVersion) {
            if (defaultVersion == null) {
                throw new NullPointerException("defaultVersion == null");
            }
            this.defaultVersion = defaultVersion;
            return this;
        }


        /**
         * Set the timeout in milliseconds until a connection is established.
         * <p>
         * A timeout value of <code>0</code> is interpreted as an infinite
         * timeout; <code>-1</code> is interpreted as system default.
         * </p>
         *
         * @param connectTimeout
         *            the timeout in milliseconds
         * @return this {@link Builder} instance
         */
        public Builder setConnectTimeout(int connectTimeout) {
            if (connectTimeout < -1) {
                throw new IllegalArgumentException("connectTimeout < -1");
            }
            this.connectTimeout = connectTimeout;
            return this;
        }


        /**
         * Set the socket timeout (<code>SO_TIMEOUT</code>) in milliseconds,
         * which is the timeout for waiting for data.
         * <p>
         * A timeout value of <code>0</code> is interpreted as an infinite
         * timeout; <code>-1</code> is interpreted as system default.
         * </p>
         *
         * @param socketTimeout
         *            the socket timeout in milliseconds
         * @return this {@link Builder} instance
         */
        public Builder setSocketTimeout(int socketTimeout) {
            if (socketTimeout < -1) {
                throw new IllegalArgumentException("socketTimeout < -1");
            }
            this.socketTimeout = socketTimeout;
            return this;
        }


        /**
         * Set a customized HTTP client which is to be used.
         * 
         * @param httpClient
         *            a configured HTTP client instance
         * @return this {@link Builder} instance
         */
        public Builder setCustomizedHttpClient(CloseableHttpClient httpClient) {
            this.httpClient = httpClient;
            return this;
        }


        /**
         * Optionally set the HTTP context which is to be used by the customized
         * HTTP client.
         * 
         * @param httpContext
         *            a HTTP context instance
         * @return this {@link Builder} instance
         */
        public Builder setHttpContext(HttpClientContext httpContext) {
            this.httpContext = httpContext;
            return this;
        }


        /**
         * Set the number of worker threads. This value is only relevant for the
         * {@link SRUThreadedClient}.
         *
         * @param threadCount
         *            the number of worker threads
         * @return this {@link Builder} instance
         */
        public Builder setThreadCount(int threadCount) {
            if (threadCount < 1) {
                throw new IllegalArgumentException("threadCount < 1");
            }
            this.threadCount = threadCount;
            return this;
        }


        /**
         * Add a record data parser instance to the list of record data parsers
         *
         * @param parser
         *            the record data parser to be added
         * @return this {@link Builder} instance
         * @throws IllegalArgumentException
         *             if registering of the parser fails
         * @see SRURecordDataParser
         */
        public Builder addRecordDataParser(SRURecordDataParser parser) {
            if (parser == null) {
                throw new NullPointerException("parser == null");
            }
            final String recordSchema = parser.getRecordSchema();
            if (recordSchema == null) {
                throw new NullPointerException(
                        "parser.getRecordSchema() == null");
            }
            if (recordSchema.isEmpty()) {
                throw new IllegalArgumentException(
                        "parser.getRecordSchema() returns empty string");
            }

            for (SRURecordDataParser p : recordParsers) {
                if (p.getRecordSchema().equals(recordSchema)) {
                    throw new IllegalArgumentException(
                            "record data parser already registered: " +
                                    recordSchema);
                }
            }
            recordParsers.add(parser);
            return this;
        }


        /**
         * Add an extra response data parser instance to the list of extra
         * response data parsers
         *
         * @param parser
         *            the extra response data parser to be added
         * @return this {@link Builder} instance
         * @throws IllegalArgumentException
         *             if registering of the parser fails
         * @see SRUExtraResponseDataParser
         */
        public Builder addExtraResponseDataParser(SRUExtraResponseDataParser parser) {
            if (parser == null) {
                throw new NullPointerException("parser == null");
            }
            if (extraDataParsers == null) {
                extraDataParsers = new ArrayList<SRUExtraResponseDataParser>();
            }
            extraDataParsers.add(parser);
            return this;
        }
    } // inner class Builder

} // class SRUClientConfig

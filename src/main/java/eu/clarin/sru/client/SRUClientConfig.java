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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private final int threadCount;
    private final List<SRURecordDataParser> parsers;


    /**
     * Get default SRU version to be used.
     *
     * @return the default SRU version to be used.
     */
    public SRUVersion getDefaultVersion() {
        return defaultVersion;
    }


    /**
     * Get the connect timeout.
     *
     * @return the connect timeout
     */
    public int getConnectTimeout() {
        return connectTimeout;
    }


    /**
     * Get the socket timeout.
     *
     * @return the connect timeout
     */
    public int getSocketTimeout() {
        return socketTimeout;
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
        return parsers;
    }


    private SRUClientConfig(Builder builder) {
        if (builder == null) {
            throw new NullPointerException("builder == null");
        }
        this.defaultVersion = builder.getDefaultVersion();
        this.connectTimeout = builder.getConnectTimeout();
        this.socketTimeout  = builder.getSocketTimeout();
        this.parsers        = builder.getRecordDataParsers();
        this.threadCount    = builder.getThreadCount();
    }


    /**
     * A class that implements the builder pattern to create
     * {@link SRUClientConfig} instances.
     *
     * @see SRUClientConfig
     */
    public static class Builder {
        private SRUVersion defaultVersion = DEFAULT_SRU_VERSION;
        private int connectTimeout        = DEFAULT_CONNECT_TIMEOUT;
        private int socketTimeout         = DEFAULT_SOCKET_TIMEOUT;
        private int threadCount           =
                Runtime.getRuntime().availableProcessors() * 2;
        private List<SRURecordDataParser> parsers =
                new ArrayList<SRURecordDataParser>();


        /**
         * Constructor.
         *
         */
        public Builder() {
        }


        /**
         * Get the default SRU version to be used
         *
         * @return the defaultSRU version to be used
         */
        public SRUVersion getDefaultVersion() {
            return defaultVersion;
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
         * Get the connect timeout.
         *
         * @return the connect timeout.
         */
        public int getConnectTimeout() {
            return connectTimeout;
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
         * Get the socket timeout (<code>SO_TIMEOUT</code>) in milliseconds,
         * which is the timeout for waiting for data.
         *
         * @return the socket timeout in milliseconds
         */
        public int getSocketTimeout() {
            return socketTimeout;
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
         * Get the number of worker threads. This value is only relevant for the
         * {@link SRUThreadedClient}.
         *
         * @return the number of worker threads
         */
        public int getThreadCount() {
            return threadCount;
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
         * Get the list of record data parsers.
         *
         * @return the list of record data parsers
         * @see SRURecordDataParser
         */
        public List<SRURecordDataParser> getRecordDataParsers() {
            return Collections.unmodifiableList(parsers);
        }


        /**
         * Add a record data parser instance to the list of record data parsers
         *
         * @param parser
         *            the record data parser to be added
         * @return this {@link Builder} instance
         * @throws IllegalArgumentException
         *             if registering the parser fails
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

            for (SRURecordDataParser p : parsers) {
                if (p.getRecordSchema().equals(recordSchema)) {
                    throw new IllegalArgumentException(
                            "record data parser already registered: " +
                                    recordSchema);
                }
            }
            parsers.add(parser);
            return this;
        }


        /**
         * Create a configuration instance object for configuring SRU clients
         *
         * @return a immutable configuration instance
         */
        public SRUClientConfig build() {
            return new SRUClientConfig(this);
        }
    } // inner class Builder

} // class SRUClientConfig

/**
 * This software is copyright (c) 2011-2013 by
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

import java.util.Collections;
import java.util.Set;

/**
 * A record data implementation for SRU explain record data conforming to the
 * Zeerex schema.
 */
public class SRUExplainRecordData implements SRURecordData {
    public static final String RECORD_SCHEMA = "http://explain.z3950.org/dtd/2.0/";

    public static class ServerInfo {
        private final String host;
        private final int port;
        private final String database;
        private final SRUVersion version;
        private final Set<String> transport;

        public ServerInfo(String host, int port, String database,
                String protocol, SRUVersion version, Set<String> transport) {
            if (host == null) {
                throw new NullPointerException("host == null");
            }
            if (host.isEmpty()) {
                throw new IllegalArgumentException("host is empty");
            }
            if ((port < 0) || (port > 65535)) {
                throw new IllegalArgumentException("port < 0 || port > 65535");
            }
            if (database == null) {
                throw new NullPointerException("database == null");
            }
            if (database.isEmpty()) {
                throw new IllegalArgumentException("database is empty");
            }
            if (version == null) {
                throw new NullPointerException("version == null");
            }
            if (transport == null) {
                throw new NullPointerException("transport == null");
            }
            this.host      = host;
            this.port      = port;
            this.database  = database;
            this.version   = version;
            this.transport = Collections.unmodifiableSet(transport);
        }


        public String getHost() {
            return host;
        }


        public int getPort() {
            return port;
        }


        public String getDatabase() {
            return database;
        }


        public SRUVersion getVersion() {
            return version;
        }


        public Set<String> getTransport() {
            return transport;
        }
    } // class ServerInfo
    private final ServerInfo serverInfo;


    public SRUExplainRecordData(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }


    @Override
    public boolean isTransient() {
        return false;
    }


    @Override
    public String getRecordSchema() {
        return RECORD_SCHEMA;
    }


    public ServerInfo getServerInfo() {
        return serverInfo;
    }

} // class SRUExplainRecordData

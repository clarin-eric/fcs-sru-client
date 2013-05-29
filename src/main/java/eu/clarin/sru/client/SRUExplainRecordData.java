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
import java.util.List;
import java.util.Set;

/**
 * A record data implementation for SRU explain record data conforming to the
 * ZeeRex schema.
 *
 * @see <a href="http://zeerex.z3950.org/dtd/">ZeeRex DTD</a>
 */
public class SRUExplainRecordData implements SRURecordData {
    public static final String RECORD_SCHEMA =
            "http://explain.z3950.org/dtd/2.0/";

    public static class ServerInfo {
        private final String host;
        private final int port;
        private final String database;
        private final SRUVersion version;
        private final Set<String> transport;

        ServerInfo(String host, int port, String database,
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

    public static final class LocalizedString {
        private final boolean primary;
        private final String lang;
        private final String value;


        LocalizedString(String value, String lang, boolean primary) {
            this.value = value;
            this.lang = lang;
            this.primary = primary;
        }


        public boolean isPrimary() {
            return primary;
        }


        public String getLang() {
            return lang;
        }


        public String getValue() {
            return value;
        }
    } // class LocalizedString

    public static final class DatabaseInfo {
        private final List<LocalizedString> title;
        private final List<LocalizedString> description;
        private final List<LocalizedString> author;
        private final List<LocalizedString> contact;
        private final List<LocalizedString> extent;
        private final List<LocalizedString> history;
        private final List<LocalizedString> langUsage;
        private final List<LocalizedString> restrictions;
        private final List<LocalizedString> subjects;
        private final List<LocalizedString> links;
        private final List<LocalizedString> implementation;


        DatabaseInfo(List<LocalizedString> title,
                List<LocalizedString> description,
                List<LocalizedString> author, List<LocalizedString> contact,
                List<LocalizedString> extent, List<LocalizedString> history,
                List<LocalizedString> langUsage,
                List<LocalizedString> restrictions,
                List<LocalizedString> subjects, List<LocalizedString> links,
                List<LocalizedString> implementation) {
            if ((title != null) && !title.isEmpty()) {
                this.title = Collections.unmodifiableList(title);
            } else {
                this.title = null;
            }
            if ((description != null) && !description.isEmpty()) {
                this.description = Collections.unmodifiableList(description);
            } else {
                this.description = null;
            }
            if ((author != null) && !author.isEmpty()) {
                this.author = Collections.unmodifiableList(author);
            } else {
                this.author = null;
            }
            if ((contact != null) && !contact.isEmpty()) {
                this.contact = Collections.unmodifiableList(contact);
            } else {
                this.contact = null;
            }
            if ((extent != null) && !extent.isEmpty()) {
                this.extent = Collections.unmodifiableList(extent);
            } else {
                this.extent = null;
            }
            if ((history != null) && !history.isEmpty()) {
                this.history = Collections.unmodifiableList(history);
            } else {
                this.history = null;
            }
            if ((langUsage != null) && !langUsage.isEmpty()) {
                this.langUsage = Collections.unmodifiableList(langUsage);
            } else {
                this.langUsage = null;
            }
            if ((restrictions != null) && !restrictions.isEmpty()) {
                this.restrictions = Collections.unmodifiableList(restrictions);
            } else {
                this.restrictions = null;
            }
            if ((subjects != null) && !subjects.isEmpty()) {
                this.subjects = Collections.unmodifiableList(subjects);
            } else {
                this.subjects = null;
            }
            if ((links != null) && !links.isEmpty()) {
                this.links = Collections.unmodifiableList(links);
            } else {
                this.links = null;
            }
            if ((implementation != null) && !implementation.isEmpty()) {
                this.implementation = Collections
                        .unmodifiableList(implementation);
            } else {
                this.implementation = null;
            }
        }


        public List<LocalizedString> getTitle() {
            return title;
        }


        public List<LocalizedString> getDescription() {
            return description;
        }


        public List<LocalizedString> getAuthor() {
            return author;
        }


        public List<LocalizedString> getContact() {
            return contact;
        }


        public List<LocalizedString> getExtend() {
            return extent;
        }


        public List<LocalizedString> getHistory() {
            return history;
        }


        public List<LocalizedString> getLangUsage() {
            return langUsage;
        }


        public List<LocalizedString> getRestrictions() {
            return restrictions;
        }


        public List<LocalizedString> getSubjects() {
            return subjects;
        }


        public List<LocalizedString> getLinks() {
            return links;
        }


        public List<LocalizedString> getImplementation() {
            return implementation;
        }
    } // class DatabaseInfo

    private final ServerInfo serverInfo;
    private final DatabaseInfo databaseInfo;


    SRUExplainRecordData(ServerInfo serverInfo, DatabaseInfo databaseInfo) {
        this.serverInfo   = serverInfo;
        this.databaseInfo = databaseInfo;
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


    public DatabaseInfo getDatabaseInfo() {
        return databaseInfo;
    }

} // class SRUExplainRecordData

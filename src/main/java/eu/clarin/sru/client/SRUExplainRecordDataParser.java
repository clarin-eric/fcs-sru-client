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

import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.client.SRUExplainRecordData.ServerInfo;


/**
 * This class implements a (partial) record data parser for SRU explain record
 * data conforming to the ZeeRex schema.
 *
 * @see <a href="http://zeerex.z3950.org/dtd/">The ZeeRex DTD</a>
 */
class SRUExplainRecordDataParser {
    private static final String ZEEREX_NS       =
            SRUExplainRecordData.RECORD_SCHEMA;
    private static final String ZEEREX_NS_QUIRK =
            "http://explain.z3950.org/dtd/2.1/";
    private static final String VERSION_1_1     = "1.1";
    private static final String VERSION_1_2     = "1.2";
    private static final String TRANSPORT_HTTP  = "http";
    private static final String TRANSPORT_HTTPS = "https";
    private static final Logger logger =
            LoggerFactory.getLogger(SRUExplainRecordDataParser.class);


    public SRURecordData parse(XMLStreamReader reader,
            SRUVersion version, String recordSchema) throws XMLStreamException,
            SRUClientException {
        // sanity check, if we are dealing with the expected record schema
        if (!(ZEEREX_NS.equals(recordSchema) ||
                ZEEREX_NS_QUIRK.equals(recordSchema))) {
            throw new SRUClientException("record schema '" + recordSchema +
                    "' not supported in explain response record data");
        }

        logger.debug("parsing explain record data (version={}, schema={})",
                version, recordSchema);

        // explain
        if (XmlStreamReaderUtils.readStart(reader, ZEEREX_NS,
                "explain", false)) {
            return parseExplain(reader, ZEEREX_NS, version);
        } else if (XmlStreamReaderUtils.readStart(reader,
                ZEEREX_NS_QUIRK, "explain", false)) {
            logger.warn("namespace '{}' is not defined by ZeeRex, enabling " +
                    "quirk mode (consider using namespace '{}' which is defined)",
                    ZEEREX_NS_QUIRK, ZEEREX_NS);
            return parseExplain(reader, ZEEREX_NS_QUIRK, version);
        } else {
            throw new XMLStreamException("expected element '{" + ZEEREX_NS +
                    "}explain' at this position", reader.getLocation());
        }
    }


    private static SRURecordData parseExplain(XMLStreamReader reader,
            final String namespace, final SRUVersion version)
            throws XMLStreamException, SRUClientException {

        /*
         * explain (serverInfo, databaseInfo?, metaInfo?, indexInfo?,
         *     (recordInfo|schemaInfo)?, configInfo?)
         */

        // explain/serverInfo
        ServerInfo serverInfo = parseServerInfo(reader, namespace);

        // explain/databaseInfo
        if (XmlStreamReaderUtils.readStart(reader, namespace, "databaseInfo", false)) {
            /*
             * databaseInfo (title*, description*, (author | contact | extent |
             *     history | langUsage | restrictions | subjects | links |
             *     implementation)*)
             */
            logger.debug("databaseInfo");

            while (XmlStreamReaderUtils.readStart(reader,
                    namespace, "title", false, true)) {
                final String lang = XmlStreamReaderUtils.readAttributeValue(reader, null, "lang", false);
                final String primary = XmlStreamReaderUtils.readAttributeValue(reader, null, "primary", false);
                XmlStreamReaderUtils.consumeStart(reader);
                String value = XmlStreamReaderUtils.readString(reader, false);
                XmlStreamReaderUtils.readEnd(reader, namespace, "title");
                logger.debug("-> title = {} (primary={}, lang={})", value, primary, lang);
            } // while

            while (XmlStreamReaderUtils.readStart(reader,
                    namespace, "description", false, true)) {
                XmlStreamReaderUtils.consumeStart(reader);
                String value = XmlStreamReaderUtils.readString(reader, false);
                XmlStreamReaderUtils.readEnd(reader, namespace, "description");
                logger.debug("-> description = {}", value);
            }

            XmlStreamReaderUtils.readEnd(reader, namespace, "databaseInfo", true);
        }

        // explain/metaInfo
        if (XmlStreamReaderUtils.readStart(reader, namespace, "metaInfo", false)) {
            /*
             * metaInfo (dateModified, (aggregatedFrom, dateAggregated)?)
             */
            logger.debug("metaInfo");
            XmlStreamReaderUtils.readEnd(reader, namespace, "metaInfo", true);
        }

        // explain/indexInfo
        while (XmlStreamReaderUtils.readStart(reader, namespace, "indexInfo", false)) {
            /*
             * indexInfo ((set | index | sortKeywords)+)
             */
            logger.debug("indexInfo");
            boolean found = false;
            for (;;) {
                if (XmlStreamReaderUtils.readStart(reader, namespace, "set", false, true)) {
                    logger.debug("-> set");
                    // FIXME: read attributes
                    XmlStreamReaderUtils.consumeStart(reader);

                    XmlStreamReaderUtils.readEnd(reader, namespace, "set", true);
                    found = true;
                } else if (XmlStreamReaderUtils.readStart(reader, namespace, "index", false, true)) {
                    logger.debug("-> index");
                    // FIXME: read attributes
                    XmlStreamReaderUtils.consumeStart(reader);
                    XmlStreamReaderUtils.readEnd(reader, namespace, "index", true);
                    found = true;
                } else if (XmlStreamReaderUtils.readStart(reader, namespace, "sortKeyword", false)) {
                    logger.debug("-> sortKeywords");
                    XmlStreamReaderUtils.readEnd(reader, namespace, "sortKeyword", true);
                    found = true;
                } else {
                    break;
                }
            } // for
            if (!found) {
                // FIXME: error message
                throw new XMLStreamException("expected at least one of <set>. <index> or <sortKeyword> element", reader.getLocation());
            }
            XmlStreamReaderUtils.readEnd(reader, namespace, "indexInfo", true);
        } // while

        // explain/recordInfo or explain/schemaInfo
        if (XmlStreamReaderUtils.peekStart(reader, namespace, "recordInfo") ||
            XmlStreamReaderUtils.peekStart(reader, namespace, "schemaInfo")) {
            if (XmlStreamReaderUtils.readStart(reader,
                    namespace, "recordInfo", false)) {
                logger.debug("recordInfo");
                XmlStreamReaderUtils.readEnd(reader, namespace, "recordInfo", true);
            } else if (XmlStreamReaderUtils.readStart(reader,
                    namespace, "schemaInfo", false)) {
                logger.debug("schemaInfo");
                XmlStreamReaderUtils.readEnd(reader, namespace, "schemaInfo", true);
            } else {
                throw new XMLStreamException("unexpected start element '" +
                        reader.getName() + "'", reader.getLocation());
            }
        }

        // explain/configInfo
        if (XmlStreamReaderUtils.readStart(reader, namespace,
                "configInfo", false)) {
            /*
             * configInfo ((default|setting|supports)*)
             */
            logger.debug("configInfo");
            for (;;) {
                if (XmlStreamReaderUtils.readStart(reader,
                        namespace, "default", false, true)) {
                    final String type =
                            XmlStreamReaderUtils.readAttributeValue(reader,
                                    null, "type", true);
                    XmlStreamReaderUtils.consumeStart(reader);
                    final String value =
                            XmlStreamReaderUtils.readString(reader, true);
                    XmlStreamReaderUtils.readEnd(reader, namespace, "default");
                    logger.debug("-> default: type={}, value={}", type, value);
                } else if (XmlStreamReaderUtils.readStart(reader,
                        namespace, "setting", false, true)) {
                    final String type =
                            XmlStreamReaderUtils.readAttributeValue(reader,
                                    null, "type", true);
                    XmlStreamReaderUtils.consumeStart(reader);
                    final String value =
                            XmlStreamReaderUtils.readString(reader, true);
                    XmlStreamReaderUtils.readEnd(reader, namespace, "setting");
                    logger.debug("-> setting: type={}, value={}", type, value);
                } else if (XmlStreamReaderUtils.readStart(reader,
                        namespace, "supports", false, true)) {
                    final String type =
                            XmlStreamReaderUtils.readAttributeValue(reader,
                                    null, "type", true);
                    XmlStreamReaderUtils.consumeStart(reader);
                    final String value =
                            XmlStreamReaderUtils.readString(reader, true);
                    XmlStreamReaderUtils.readEnd(reader, namespace, "supports");

                    logger.debug("-> supports: type={}, value={}", type, value);
                } else {
                    break;
                }
            }
            XmlStreamReaderUtils.readEnd(reader, namespace, "configInfo", true);
        }

        XmlStreamReaderUtils.readEnd(reader, namespace, "explain", true);
        return new SRUExplainRecordData(serverInfo);
    }


    private static ServerInfo parseServerInfo(final XMLStreamReader reader,
            final String namespace) throws XMLStreamException,
            SRUClientException {
        XmlStreamReaderUtils.readStart(reader, namespace,
                "serverInfo", true, true);
        String protocol = XmlStreamReaderUtils.readAttributeValue(reader,
                null, "protocol");

        SRUVersion version = SRUVersion.VERSION_1_2;
        String s = XmlStreamReaderUtils.readAttributeValue(reader, null,
                "version");
        if (s != null) {
            if (VERSION_1_1.equals(s)) {
                version = SRUVersion.VERSION_1_1;
            } else if (VERSION_1_2.equals(s)) {
                version = SRUVersion.VERSION_1_2;
            } else {
                throw new SRUClientException("invalid or unsupported value '" +
                        s + "'for attribute 'version' on element '" +
                        reader.getName() + "'");
            }
        }

        Set<String> transports = new HashSet<String>();
        s = XmlStreamReaderUtils.readAttributeValue(reader, null, "transport");
        if (s != null) {
            for (String i : s.split("\\s+")) {
                String t = null;
                if (TRANSPORT_HTTP.equalsIgnoreCase(i)) {
                    t = TRANSPORT_HTTP;
                } else if (TRANSPORT_HTTPS.equalsIgnoreCase(i)) {
                    t = TRANSPORT_HTTPS;
                } else {
                    throw new SRUClientException("invalid value '" + i +
                            "' for attribute 'transport' on element '" +
                            reader.getName() +
                            " (use either 'http' of 'https' or both " +
                            "seperated by whitespace");
                }
                if (t != null) {
                    if (!transports.contains(t)) {
                        transports.add(t);
                    } else {
                        logger.warn("value '{}' already listed in " +
                                "'transport' attribute of element '{}'",
                                t, reader.getName());
                    }
                }

            } // for
        } else {
            transports.add(TRANSPORT_HTTP);
        }
        XmlStreamReaderUtils.consumeStart(reader);

        String host = XmlStreamReaderUtils.readContent(reader, namespace,
                "host", true);
        int port = XmlStreamReaderUtils.readContent(reader, namespace,
                "port", true, -1);
        if ((port < 0) || (port > 65535)) {
            // FIXME: error message
            throw new SRUClientException("invalid port number (" + port + ")");
        }
        String database = XmlStreamReaderUtils.readContent(reader, namespace,
                "database", true);
        if (XmlStreamReaderUtils.readStart(reader, namespace,
                "authentication", false)) {
            XmlStreamReaderUtils.readEnd(reader, namespace,
                    "authentication", true);
        }
        XmlStreamReaderUtils.readEnd(reader, namespace, "serverInfo", true);
        logger.debug("serverInfo: host={}, port={}, database={}, version={}, " +
                "protocol={}, transport={}", host, port, database, version,
                protocol, transports);
        return new ServerInfo(host, port, database, protocol, version,
                transports);
    }

} // class SRUExplainRecordDataParser

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
 * data conforming to the ZEEREX schema
 */
public class SRUExplainRecordDataParser implements SRURecordDataParser {
    private static final String ZEEREX_NS = SRUExplainRecordData.RECORD_SCHEMA;
    private static final String VERSION_1_1     = "1.1";
    private static final String VERSION_1_2     = "1.2";
    private static final String TRANSPORT_HTTP  = "http";
    private static final String TRANSPORT_HTTPS = "https";
    private static final Logger logger =
            LoggerFactory.getLogger(SRUExplainRecordDataParser.class);

    @Override
    public String getRecordSchema() {
        return SRUExplainRecordData.RECORD_SCHEMA;
    }


    @Override
    public SRURecordData parse(XMLStreamReader reader, SRUVersion version)
            throws XMLStreamException, SRUClientException {
        logger.debug("parsing explain record data for version {}", version);

        // explain
        XmlStreamReaderUtils.readStart(reader, ZEEREX_NS, "explain", true);

        // explain/serverInfo
        ServerInfo serverInfo = parseServerInfo(reader);

        // explain/databaseInfo
        if (XmlStreamReaderUtils.readStart(reader, ZEEREX_NS, "databaseInfo", false)) {
            logger.debug("databaseInfo");
            XmlStreamReaderUtils.readEnd(reader, ZEEREX_NS, "databaseInfo", true);
        }

        // explain/metaInfo
        if (XmlStreamReaderUtils.readStart(reader, ZEEREX_NS, "metaInfo", false)) {
            logger.debug("metaInfo");
            XmlStreamReaderUtils.readEnd(reader, ZEEREX_NS, "metaInfo", true);
        }

        // explain/indexInfo
        while (XmlStreamReaderUtils.readStart(reader, ZEEREX_NS, "indexInfo", false)) {
            logger.debug("indexInfo");
            for (;;) {
                /*
                 * FIXME: SRU 2.0 has *either* <set> or <index>
                 * check with SRU 1.2 ...
                 */
                if (XmlStreamReaderUtils.readStart(reader, ZEEREX_NS, "set", false, true)) {
                    logger.debug("set");
                    // FIXME: read attributes
                    XmlStreamReaderUtils.consumeStart(reader);

                    XmlStreamReaderUtils.readEnd(reader, ZEEREX_NS, "set", true);
                } else if (XmlStreamReaderUtils.readStart(reader, ZEEREX_NS, "index", false, true)) {
                    logger.debug("index");
                    // FIXME: read attributes
                    XmlStreamReaderUtils.consumeStart(reader);
                    XmlStreamReaderUtils.readEnd(reader, ZEEREX_NS, "index", true);
                } else {
                    break;
                }
            }
            XmlStreamReaderUtils.readEnd(reader, ZEEREX_NS, "indexInfo", true);
        } // while

        XmlStreamReaderUtils.readEnd(reader, ZEEREX_NS, "explain", true);
        return new SRUExplainRecordData(serverInfo);
    }


    private static ServerInfo parseServerInfo(XMLStreamReader reader)
            throws XMLStreamException, SRUClientException {
        XmlStreamReaderUtils.readStart(reader, ZEEREX_NS,
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
                throw new SRUClientException(
                        "invalid or unsupported version: " + s);
            }
        }

        Set<String> transports = new HashSet<String>();
        s = XmlStreamReaderUtils.readAttributeValue(reader,
                null, "transport");
        if (s != null) {
            for (String i : s.split("\\s+")) {
                String t = null;
                if (TRANSPORT_HTTP.equalsIgnoreCase(i)) {
                    t = TRANSPORT_HTTP;
                } else if (TRANSPORT_HTTPS.equalsIgnoreCase(i)) {
                    t = TRANSPORT_HTTPS;
                } else {
                    throw new SRUClientException("invalid transport: " + i);
                }
                if (t != null) {
                    if (!transports.contains(t)) {
                        transports.add(t);
                    } else {
                        // FIXME: error message
                        logger.warn("transport  {} was already listed", s);
                    }
                }

            } // for
        } else {
            transports.add(TRANSPORT_HTTP);
        }
        XmlStreamReaderUtils.consumeStart(reader);

        String host = XmlStreamReaderUtils.readContent(reader, ZEEREX_NS,
                "host", true);
        int port = XmlStreamReaderUtils.readContent(reader, ZEEREX_NS,
                "port", true, -1);
        if ((port < 0) || (port > 65535)) {
            // FIXME: error message
            throw new SRUClientException("invalid port number (" + port + ")");
        }
        String database = XmlStreamReaderUtils.readContent(reader, ZEEREX_NS,
                "database", true);
        if (XmlStreamReaderUtils.readStart(reader, ZEEREX_NS,
                "authentication", false)) {
            XmlStreamReaderUtils.readEnd(reader, ZEEREX_NS,
                    "authentication", true);
        }
        XmlStreamReaderUtils.readEnd(reader, ZEEREX_NS, "serverInfo", true);
        logger.debug("serverInfo: host={}, port={}, database={}, version={}, protocol={}, transport={}",
                host, port, database, version, protocol, transports);
        return new ServerInfo(host, port, database, protocol, version,
                transports);
    }

} // class SRUExplainRecordDataParser

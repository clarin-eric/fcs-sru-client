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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.client.SRUExplainRecordData.DatabaseInfo;
import eu.clarin.sru.client.SRUExplainRecordData.LocalizedString;
import eu.clarin.sru.client.SRUExplainRecordData.ServerInfo;


/**
 * This class implements a (partial) record data parser for SRU explain record
 * data conforming to the ZeeRex schema.
 *
 * @see <a href="http://zeerex.z3950.org/dtd/">ZeeRex DTD</a>
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
    private static final String PRIMARY_TRUE    = "true";
    private static final String PRIMARY_FALSE   = "false";
    private static final Logger logger =
            LoggerFactory.getLogger(SRUExplainRecordDataParser.class);


    public SRURecordData parse(XMLStreamReader reader, SRUVersion version,
            boolean strict, String recordSchema) throws XMLStreamException,
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
        if (XmlStreamReaderUtils.peekStart(reader, ZEEREX_NS, "explain")) {
            if (strict) {
                logger.warn("namespace '{}' is not defined by ZeeRex, " +
                        "enabling quirks mode (consider using namespace '{}'" +
                        " which is defined)", ZEEREX_NS_QUIRK, ZEEREX_NS);
            } else {
                logger.info("namespace '{}' is not defined by ZeeRex, " +
                        "enabling quirks mode (consider using namespace '{}'" +
                        " which is defined)", ZEEREX_NS_QUIRK, ZEEREX_NS);
            }
            return parseExplain(reader, version, strict, ZEEREX_NS);
        } else if (XmlStreamReaderUtils.peekStart(reader,
                ZEEREX_NS_QUIRK, "explain")) {
            if (strict) {
                logger.warn("namespace '{}' is not defined by ZeeRex, " +
                        "enabling quirks mode (consider using namespace '{}'" +
                        " which is defined)", ZEEREX_NS_QUIRK, ZEEREX_NS);
            } else {
                logger.info("namespace '{}' is not defined by ZeeRex, " +
                        "enabling quirks mode (consider using namespace '{}'" +
                        " which is defined)", ZEEREX_NS_QUIRK, ZEEREX_NS);
            }
            return parseExplain(reader, version, strict, ZEEREX_NS_QUIRK);
        } else {
            throw new XMLStreamException("expected element '" +
                    new QName(ZEEREX_NS, "explain") + " at this position",
                    reader.getLocation());
        }
    }


    private static SRURecordData parseExplain(XMLStreamReader reader,
            SRUVersion version, boolean strict, String ns)
            throws XMLStreamException, SRUClientException {

        // explain
        XmlStreamReaderUtils.readStart(reader, ns, "explain", true);

        /*
         * explain (serverInfo, databaseInfo?, metaInfo?, indexInfo?,
         *     (recordInfo|schemaInfo)?, configInfo?)
         */

        // explain/serverInfo (mandatory)
        ServerInfo serverInfo = parseServerInfo(reader, ns);

        // explain/databaseInfo (optional)
        DatabaseInfo databaseInfo = null;
        if (XmlStreamReaderUtils.readStart(reader, ns, "databaseInfo", false)) {
            databaseInfo = parseDatabaseInfo(reader, ns, strict);
            XmlStreamReaderUtils.readEnd(reader, ns, "databaseInfo");
        }

        // explain/metaInfo (optional)
        if (XmlStreamReaderUtils.readStart(reader, ns, "metaInfo", false)) {
            /*
             * metaInfo (dateModified, (aggregatedFrom, dateAggregated)?)
             */
            logger.debug("metaInfo");
            XmlStreamReaderUtils.readEnd(reader, ns, "metaInfo", true);
        }

        // explain/indexInfo (optional)
        while (XmlStreamReaderUtils.readStart(reader, ns, "indexInfo", false)) {
            parseIndexInfo(reader, strict, ns);
            XmlStreamReaderUtils.readEnd(reader, ns, "indexInfo");
        } // while

        // explain/recordInfo or explain/schemaInfo
        if (XmlStreamReaderUtils.peekStart(reader, ns, "recordInfo") ||
            XmlStreamReaderUtils.peekStart(reader, ns, "schemaInfo")) {
            if (XmlStreamReaderUtils.readStart(reader,
                    ns, "recordInfo", false)) {
                logger.debug("recordInfo");
                XmlStreamReaderUtils.readEnd(reader, ns, "recordInfo", true);
            } else if (XmlStreamReaderUtils.readStart(reader,
                    ns, "schemaInfo", false)) {
                logger.debug("schemaInfo");
                XmlStreamReaderUtils.readEnd(reader, ns, "schemaInfo", true);
            } else {
                throw new XMLStreamException("unexpected start element '" +
                        reader.getName() + "'", reader.getLocation());
            }
        }

        // explain/configInfo (optional)
        if (XmlStreamReaderUtils.readStart(reader, ns, "configInfo", false)) {
            parseConfigInfo(reader, strict, ns);
            XmlStreamReaderUtils.readEnd(reader, ns, "configInfo");
        }

        XmlStreamReaderUtils.readEnd(reader, ns, "explain", true);
        return new SRUExplainRecordData(serverInfo, databaseInfo);
    }


    private static ServerInfo parseServerInfo(XMLStreamReader reader,
            String namespace) throws XMLStreamException, SRUClientException {
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


    private static DatabaseInfo parseDatabaseInfo(XMLStreamReader reader,
            String ns, boolean strict) throws XMLStreamException,
            SRUClientException {
        /*
         * databaseInfo (title*, description*, (author | contact | extent |
         *     history | langUsage | restrictions | subjects | links |
         *     implementation)*)
         */

        // make sure to remove any whitespace
        XmlStreamReaderUtils.consumeWhitespace(reader);

        List<LocalizedString> title = null;
        List<LocalizedString> description = null;
        List<LocalizedString> author = null;
        List<LocalizedString> contact = null;
        List<LocalizedString> extent = null;
        List<LocalizedString> history = null;
        List<LocalizedString> langUsage = null;
        List<LocalizedString> restrictions = null;
        List<LocalizedString> subjects = null;
        List<LocalizedString> links = null;
        List<LocalizedString> implementation = null;

        byte mode = 0; /* 1 = title, 2 = description, 3 = others */
        while (reader.isStartElement()) {
            if (XmlStreamReaderUtils.peekStart(reader, ns, "title")) {
                if ((mode != 0) && (mode != 1)) {
                    if (strict) {
                        throw new XMLStreamException(
                                "element '" + new QName(ns, "title") +
                                        "' not allowed here [required content" +
                                        " model is: title*, description*, " +
                                        "(author | contact | extent | history" +
                                        " | langUsage | restrictions | " +
                                        "subjects | links | implementation)*]",
                                        reader.getLocation());
                    } else {
                        logger.warn("element '{}' not allowed here [required " +
                                "content model is: title*, description*, " +
                                "(author | contact | extent | history | " +
                                "langUsage | restrictions | subjects | links " +
                                "| implementation)*] at position " +
                                "[row, col]: [{}, {}]",
                                new QName(ns, "title"),
                                reader.getLocation().getLineNumber(),
                                reader.getLocation().getColumnNumber());
                    }
                }
                LocalizedString s = parseStringI18N(reader, strict, ns, "title");
                if (title == null) {
                    title = new ArrayList<LocalizedString>();
                }
                title.add(s);
                mode = 1;
            } else if (XmlStreamReaderUtils.peekStart(reader, ns, "description")) {
                if ((mode != 0) && (mode != 1) && (mode != 2)) {
                    if (strict) {
                        throw new XMLStreamException(
                                "element '" + new QName(ns, "description") +
                                        "' not allowed here [required content" +
                                        " model is: title*, description*, " +
                                        "(author | contact | extent | history" +
                                        " | langUsage | restrictions | " +
                                        "subjects | links | implementation)*]",
                                        reader.getLocation());
                    } else {
                        logger.warn("element '{}' not allowed here [required " +
                                "content model is: title*, description*, " +
                                "(author | contact | extent | history | " +
                                "langUsage | restrictions | subjects | links " +
                                "| implementation)*] at position " +
                                "[row, col]: [{}, {}]",
                                new QName(ns, "description"),
                                reader.getLocation().getLineNumber(),
                                reader.getLocation().getColumnNumber());
                    }
                }
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "description");
                if (description == null) {
                    description = new ArrayList<LocalizedString>();
                }
                description.add(s);
                mode = 2;
            } else if (XmlStreamReaderUtils.peekStart(reader, ns, "author")) {
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "author");
                if (author == null) {
                    author = new ArrayList<LocalizedString>();
                }
                author.add(s);
                mode = 3;
            } else if (XmlStreamReaderUtils.peekStart(reader, ns, "contact")) {
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "contact");
                if (contact == null) {
                    contact = new ArrayList<LocalizedString>();
                }
                contact.add(s);
                mode = 3;
            } else if (XmlStreamReaderUtils.peekStart(reader, ns, "extent")) {
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "extent");
                if (extent == null) {
                    extent = new ArrayList<LocalizedString>();
                }
                extent.add(s);
                mode = 3;
            } else if (XmlStreamReaderUtils.peekStart(reader, ns, "history")) {
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "history");
                if (history == null) {
                    history = new ArrayList<LocalizedString>();
                }
                history.add(s);
                mode = 3;
            } else if (XmlStreamReaderUtils.peekStart(reader,
                    ns, "restrictions")) {
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "restrictions");
                if (restrictions == null) {
                    restrictions = new ArrayList<LocalizedString>();
                }
                restrictions.add(s);
                mode = 3;
            } else if (XmlStreamReaderUtils.peekStart(reader, ns, "subjects")) {
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "subjects");
                if (subjects == null) {
                    subjects = new ArrayList<LocalizedString>();
                }
                subjects.add(s);
                mode = 3;
            } else if (XmlStreamReaderUtils.peekStart(reader, ns, "links")) {
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "links");
                if (links == null) {
                    links = new ArrayList<LocalizedString>();
                }
                links.add(s);
                mode = 3;
            } else if (XmlStreamReaderUtils.peekStart(reader,
                    ns, "implementation")) {
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "implementation");
                if (implementation == null) {
                    implementation = new ArrayList<LocalizedString>();
                }
                implementation.add(s);
                mode = 3;
            } else if (XmlStreamReaderUtils.peekStart(reader, ns, "langUsage")) {
                // FIXME: implement!
                XmlStreamReaderUtils.readContent(reader, ns, "langUsage", false);
                mode = 3;
            } else {
                break;
            }

            // make sure to remove any whitespace
            XmlStreamReaderUtils.consumeWhitespace(reader);
        } // while

        return new DatabaseInfo(title, description, author, contact,
                extent, history, langUsage, restrictions, subjects,
                links, implementation);
    }


    private static Object parseIndexInfo(XMLStreamReader reader,
            boolean strict, String ns) throws XMLStreamException,
            SRUClientException {
        /*
         * indexInfo ((set | index | sortKeywords)+)
         */
        logger.debug("indexInfo");
        boolean found = false;
        for (;;) {
            if (XmlStreamReaderUtils.readStart(reader, ns, "set", false, true)) {
                logger.debug("-> set");
                // FIXME: read attributes
                XmlStreamReaderUtils.consumeStart(reader);

                XmlStreamReaderUtils.readEnd(reader, ns, "set", true);
                found = true;
            } else if (XmlStreamReaderUtils.readStart(reader, ns, "index", false, true)) {
                logger.debug("-> index");
                // FIXME: read attributes
                XmlStreamReaderUtils.consumeStart(reader);
                XmlStreamReaderUtils.readEnd(reader, ns, "index", true);
                found = true;
            } else if (XmlStreamReaderUtils.readStart(reader, ns, "sortKeyword", false)) {
                logger.debug("-> sortKeywords");
                XmlStreamReaderUtils.readEnd(reader, ns, "sortKeyword", true);
                found = true;
            } else {
                break;
            }
        } // for
        if (!found) {
            throw new XMLStreamException("expected at least one '" +
                    new QName(ns, "set") + "', '" +
                    new QName(ns, "index") + "' or '" +
                    new QName(ns, "sortKeyword") +
                    "' element within element '" +
                    new QName(ns, "indexInfo") + "'",
                    reader.getLocation());
        }
        return null;
    }


    private static Object parseConfigInfo(XMLStreamReader reader,
            boolean strict, String ns) throws XMLStreamException,
            SRUClientException {
        /*
         * configInfo ((default|setting|supports)*)
         */
        logger.debug("configInfo");
        for (;;) {
            if (XmlStreamReaderUtils.readStart(reader,
                    ns, "default", false, true)) {
                final String type =
                        XmlStreamReaderUtils.readAttributeValue(reader,
                                null, "type", true);
                XmlStreamReaderUtils.consumeStart(reader);
                final String value =
                        XmlStreamReaderUtils.readString(reader, true);
                XmlStreamReaderUtils.readEnd(reader, ns, "default");
                logger.debug("-> default: type={}, value={}", type, value);
            } else if (XmlStreamReaderUtils.readStart(reader,
                    ns, "setting", false, true)) {
                final String type =
                        XmlStreamReaderUtils.readAttributeValue(reader,
                                null, "type", true);
                XmlStreamReaderUtils.consumeStart(reader);
                final String value =
                        XmlStreamReaderUtils.readString(reader, true);
                XmlStreamReaderUtils.readEnd(reader, ns, "setting");
                logger.debug("-> setting: type={}, value={}", type, value);
            } else if (XmlStreamReaderUtils.readStart(reader,
                    ns, "supports", false, true)) {
                final String type =
                        XmlStreamReaderUtils.readAttributeValue(reader,
                                null, "type", true);
                XmlStreamReaderUtils.consumeStart(reader);
                final String value =
                        XmlStreamReaderUtils.readString(reader, true);
                XmlStreamReaderUtils.readEnd(reader, ns, "supports");

                logger.debug("-> supports: type={}, value={}", type, value);
            } else {
                break;
            }
        }

        return null;
    }


    private static LocalizedString parseStringI18N(XMLStreamReader reader,
            boolean strict, String ns, String localName)
            throws XMLStreamException {
        XmlStreamReaderUtils.readStart(reader, ns, localName, true, true);
        final String lang = XmlStreamReaderUtils.readAttributeValue(reader,
                null, "lang", false);
        if (lang != null) {
            if (lang.length() != 2) {
                // FIXME: message
                logger.warn("ZeeRex sugguests to use 2-letter codes");
            }
        }
        final String s = XmlStreamReaderUtils.readAttributeValue(reader,
                null, "primary", false);
        boolean primary = false;
        if (s != null) {
            if (PRIMARY_TRUE.equalsIgnoreCase(s)) {
                if (!PRIMARY_TRUE.equals(s)) {
                    if (strict) {
                        throw new XMLStreamException("capitalization");
                    } else {
                        // FIXME: message
                        logger.warn("capitalizaton");
                    }
                }
                primary = true;
            } else if (PRIMARY_FALSE.equalsIgnoreCase(s)) {
                if (!PRIMARY_FALSE.equals(s)) {
                    if (strict) {
                        throw new XMLStreamException("capitalization");
                    } else {
                        // FIXME: message
                        logger.warn("capitalizaton");
                    }
                }
                primary = false;
            } else {
                // FIXME: message
                throw new XMLStreamException("value of @primary invalid");
            }
        }
        XmlStreamReaderUtils.consumeStart(reader);
        String value = XmlStreamReaderUtils.readString(reader, false);
        XmlStreamReaderUtils.readEnd(reader, ns, localName);
        logger.debug("databaseInfo/{}='{}' (primary={}, lang={})", localName,
                value, primary, lang);
        return new LocalizedString(value, lang, primary);
    }

} // class SRUExplainRecordDataParser

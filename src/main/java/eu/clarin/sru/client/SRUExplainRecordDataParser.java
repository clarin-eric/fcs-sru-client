/**
 * This software is copyright (c) 2012-2022 by
 *  - Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.client.SRUExplainRecordData.ConfigInfo;
import eu.clarin.sru.client.SRUExplainRecordData.DatabaseInfo;
import eu.clarin.sru.client.SRUExplainRecordData.IndexInfo;
import eu.clarin.sru.client.SRUExplainRecordData.LocalizedString;
import eu.clarin.sru.client.SRUExplainRecordData.Schema;
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
    private static final String VERSION_2       = "2";
    private static final String VERSION_2_0     = "2.0";
    private static final String TRANSPORT_HTTP  = "http";
    private static final String TRANSPORT_HTTPS = "https";
    private static final String STRING_TRUE     = "true";
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
            throw new XMLStreamException("expected start tag for element '" +
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
        IndexInfo indexInfo = null;
        if (XmlStreamReaderUtils.readStart(reader, ns, "indexInfo", false)) {
            indexInfo = parseIndexInfo(reader, strict, ns);
            XmlStreamReaderUtils.readEnd(reader, ns, "indexInfo");
        } // if

        // explain/recordInfo or explain/schemaInfo
        List<Schema> schemaInfo = null;
        if (XmlStreamReaderUtils.peekStart(reader, ns, "recordInfo") ||
            XmlStreamReaderUtils.peekStart(reader, ns, "schemaInfo")) {
            if (XmlStreamReaderUtils.readStart(reader,
                    ns, "recordInfo", false)) {
                logger.debug("skipping 'recordInfo'");
                XmlStreamReaderUtils.readEnd(reader, ns, "recordInfo", true);
            } else if (XmlStreamReaderUtils.readStart(reader,
                    ns, "schemaInfo", false)) {
                schemaInfo = parseSchemaInfo(reader, strict, ns);
                XmlStreamReaderUtils.readEnd(reader, ns, "schemaInfo");
            } else {
                throw new XMLStreamException("unexpected start element '" +
                        reader.getName() + "'", reader.getLocation());
            }
        }

        // explain/configInfo (optional)
        ConfigInfo configInfo = null;
        if (XmlStreamReaderUtils.readStart(reader, ns, "configInfo", false)) {
            configInfo = parseConfigInfo(reader, strict, ns);
            XmlStreamReaderUtils.readEnd(reader, ns, "configInfo");
        }

        XmlStreamReaderUtils.readEnd(reader, ns, "explain", true);
        return new SRUExplainRecordData(serverInfo, databaseInfo, indexInfo,
                schemaInfo, configInfo);
    }


    private static ServerInfo parseServerInfo(XMLStreamReader reader,
            String ns) throws XMLStreamException, SRUClientException {
        XmlStreamReaderUtils.readStart(reader, ns,
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
            } else if (VERSION_2.equals(s) || VERSION_2_0.equals(s)) {
                version = SRUVersion.VERSION_2_0;
            } else {
                throw new SRUClientException("invalid or unsupported value '" +
                        s + " 'for attribute 'version' on element '" +
                        reader.getName() + "'");
            }
        }

        Set<String> transports = new HashSet<>();
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

        final String host = XmlStreamReaderUtils.readContent(reader,
                ns, "host", true);
        final int port = XmlStreamReaderUtils.readContent(reader,
                ns, "port", true, -1);
        if ((port < 0) || (port > 65535)) {
            // FIXME: error message
            throw new SRUClientException("invalid port number (" + port + ")");
        }
        String database = XmlStreamReaderUtils.readContent(reader, ns,
                "database", true);
        if (XmlStreamReaderUtils.readStart(reader, ns,
                "authentication", false)) {
            XmlStreamReaderUtils.readEnd(reader, ns,
                    "authentication", true);
        }
        XmlStreamReaderUtils.readEnd(reader, ns, "serverInfo", true);
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
        for (;;) {
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
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "title", true);
                if (title == null) {
                    title = new ArrayList<>();
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
                        parseStringI18N(reader, strict, ns, "description", true);
                if (description == null) {
                    description = new ArrayList<>();
                }
                description.add(s);
                mode = 2;
            } else if (XmlStreamReaderUtils.peekStart(reader, ns, "author")) {
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "author", true);
                if (author == null) {
                    author = new ArrayList<>();
                }
                author.add(s);
                mode = 3;
            } else if (XmlStreamReaderUtils.peekStart(reader, ns, "contact")) {
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "contact", true);
                if (contact == null) {
                    contact = new ArrayList<>();
                }
                contact.add(s);
                mode = 3;
            } else if (XmlStreamReaderUtils.peekStart(reader, ns, "extent")) {
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "extent", true);
                if (extent == null) {
                    extent = new ArrayList<>();
                }
                extent.add(s);
                mode = 3;
            } else if (XmlStreamReaderUtils.peekStart(reader, ns, "history")) {
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "history", true);
                if (history == null) {
                    history = new ArrayList<>();
                }
                history.add(s);
                mode = 3;
            } else if (XmlStreamReaderUtils.peekStart(reader,
                    ns, "restrictions")) {
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "restrictions", true);
                if (restrictions == null) {
                    restrictions = new ArrayList<>();
                }
                restrictions.add(s);
                mode = 3;
            } else if (XmlStreamReaderUtils.peekStart(reader, ns, "subjects")) {
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "subjects", true);
                if (subjects == null) {
                    subjects = new ArrayList<>();
                }
                subjects.add(s);
                mode = 3;
            } else if (XmlStreamReaderUtils.peekStart(reader, ns, "links")) {
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "links", true);
                if (links == null) {
                    links = new ArrayList<>();
                }
                links.add(s);
                mode = 3;
            } else if (XmlStreamReaderUtils.peekStart(reader,
                    ns, "implementation")) {
                LocalizedString s =
                        parseStringI18N(reader, strict, ns, "implementation", true);
                if (implementation == null) {
                    implementation = new ArrayList<>();
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
        } // for

        return new DatabaseInfo(title, description, author, contact,
                extent, history, langUsage, restrictions, subjects,
                links, implementation);
    }


    private static IndexInfo parseIndexInfo(XMLStreamReader reader,
            boolean strict, String ns) throws XMLStreamException,
            SRUClientException {
        /*
         * indexInfo ((set | index | sortKeywords)+)
         */
        boolean found = false;
        List<IndexInfo.Set> sets = null;
        List<IndexInfo.Index> indexes = null;
        for (;;) {
            if (XmlStreamReaderUtils.readStart(reader, ns, "set", false, true)) {
                final String identifier =
                        XmlStreamReaderUtils.readAttributeValue(reader,
                                null, "identifier");
                final String name =
                        XmlStreamReaderUtils.readAttributeValue(reader,
                                null, "name");
                XmlStreamReaderUtils.consumeStart(reader);

                // indexInfo/set/title
                List<LocalizedString> titles = null;
                for (;;) {
                    LocalizedString title =
                            parseStringI18N(reader, strict, ns, "title", false);
                    if (title == null) {
                        break;
                    }
                    if (titles == null) {
                        titles = new ArrayList<>();
                    }
                    titles.add(title);
                } // for
                XmlStreamReaderUtils.readEnd(reader, ns, "set");

                if (sets == null) {
                    sets = new ArrayList<>();
                }
                sets.add(new IndexInfo.Set(identifier, name, titles));
                found = true;
            } else if (XmlStreamReaderUtils.readStart(reader, ns,
                    "index", false, true)) {
                final String id =
                        XmlStreamReaderUtils.readAttributeValue(reader, null, "id");
                final boolean can_search = parseBooleanAttribute(reader,
                        strict, null, "search", false);
                final boolean can_scan = parseBooleanAttribute(reader,
                        strict, null, "scan", false);
                final boolean can_sort = parseBooleanAttribute(reader,
                        strict, null, "sort", false);
                XmlStreamReaderUtils.consumeStart(reader);

                // indexInfo/index/title
                List<LocalizedString> titles = null;
                for (;;) {
                    LocalizedString title =
                            parseStringI18N(reader, strict, ns, "title", false);
                    if (title == null) {
                        break;
                    }
                    if (titles == null) {
                        titles = new ArrayList<>();
                    }
                    titles.add(title);
                } // for

                // indexInfo/index/map ((attr+)|name)
                List<IndexInfo.Index.Map> maps = null;
                boolean first_map = true;
                while (XmlStreamReaderUtils.readStart(reader, ns, "map",
                        first_map, true)) {
                    final boolean primary = parseBooleanAttribute(reader,
                            strict, null, "primary", false);
                    XmlStreamReaderUtils.consumeStart(reader);

                    if (XmlStreamReaderUtils.peekStart(reader, ns, "attr")) {
                        /*
                         * skip "attr" elements, because they are not supported
                         */
                        while (XmlStreamReaderUtils.readStart(reader,
                                ns, "attr", false)) {
                            logger.debug("skipping 'attr'");
                            XmlStreamReaderUtils.readEnd(reader, ns, "attr");
                        } // while (attr)
                    } else if (XmlStreamReaderUtils.peekStart(reader,
                            ns, "name")) {
                        XmlStreamReaderUtils.readStart(reader, ns, "name", true, true);
                        final String set =
                                XmlStreamReaderUtils.readAttributeValue(reader,
                                        null, "set");
                        // FIXME: check 'set'
                        XmlStreamReaderUtils.consumeStart(reader);
                        final String name =
                                XmlStreamReaderUtils.readString(reader, false);
                        // FIXME: check 'name'
                        XmlStreamReaderUtils.readEnd(reader, ns, "name");
                        if ((set != null) && (name != null)) {
                            if (maps == null) {
                                maps = new ArrayList<>();
                            }
                            maps.add(new IndexInfo.Index.Map(primary, set, name));
                        }
                    } else {
                        // FIXME: error message
                        throw new XMLStreamException(
                                "expected 'attr' or 'name'",
                                reader.getLocation());
                    }

                    XmlStreamReaderUtils.readEnd(reader, ns, "map");
                    first_map = false;
                } // while (map)

                // indexInfo/index/configInfo (optional)
                if (XmlStreamReaderUtils.readStart(reader, ns, "configInfo", false)) {
                    logger.debug("skipping 'configInfo' within 'indexInfo/index'");
                    XmlStreamReaderUtils.readEnd(reader, ns, "configInfo", true);
                }

                XmlStreamReaderUtils.readEnd(reader, ns, "index");
                if (indexes == null) {
                    indexes = new ArrayList<>();
                }
                indexes.add(new IndexInfo.Index(id, titles, can_search,
                        can_scan, can_sort, maps));
                found = true;
            } else if (XmlStreamReaderUtils.readStart(reader, ns, "sortKeyword", false)) {
                logger.debug("skipping 'sortKeywords'");
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
        return ((sets != null) || (indexes != null))
                ? new IndexInfo(sets, indexes)
                : null;
    }


    private static ConfigInfo parseConfigInfo(XMLStreamReader reader,
            boolean strict, String ns) throws XMLStreamException,
            SRUClientException {
        /*
         * configInfo ((default|setting|supports)*)
         */
        Map<String, String> defaults = null;
        Map<String, String> settings = null;
        Map<String, String> supports = null;
        for (;;) {
            if (XmlStreamReaderUtils.peekStart(reader, ns, "default")) {
                defaults = parseConfigInfoItem(reader, strict, ns,
                        "default", defaults);
            } else if (XmlStreamReaderUtils.peekStart(reader, ns, "setting")) {
                settings = parseConfigInfoItem(reader, strict, ns,
                        "setting", settings);
            } else if (XmlStreamReaderUtils.peekStart(reader, ns, "supports")) {
                supports = parseConfigInfoItem(reader, strict, ns,
                        "supports", supports);
            } else {
                break;
            }
        } // for

        return ((defaults != null) || (settings != null) || (supports != null))
                ? new ConfigInfo(defaults, settings, supports)
                : null;
    }


    private static LocalizedString parseStringI18N(XMLStreamReader reader,
            boolean strict, String ns, String localName, boolean required)
            throws XMLStreamException {
        if (!XmlStreamReaderUtils.readStart(reader, ns, localName, required,
                true)) {
            return null;
        }
        final String lang = XmlStreamReaderUtils.readAttributeValue(reader,
                null, "lang", false);
        if (lang != null) {
            if (lang.length() != 2) {
                // FIXME: message
                logger.warn("ZeeRex sugguests to use 2-letter codes");
            }
        }
        final boolean primary =
                parseBooleanAttribute(reader, strict, null, "primary", false);
        XmlStreamReaderUtils.consumeStart(reader);
        String value = XmlStreamReaderUtils.readString(reader, false);
        XmlStreamReaderUtils.readEnd(reader, ns, localName);
        return new LocalizedString(value, lang, primary);
    }


    private static boolean parseBooleanAttribute(XMLStreamReader reader,
            boolean strict, String ns, String localName, boolean defaultValue)
            throws XMLStreamException {
        boolean result = defaultValue;
        final String s = XmlStreamReaderUtils.readAttributeValue(reader,
                ns, localName, false);
        if (s != null) {
            if (STRING_TRUE.equalsIgnoreCase(s)) {
                if (!STRING_TRUE.equals(s)) {
                    if (strict) {
                        throw new XMLStreamException("capitalization");
                    } else {
                        // FIXME: message
                        logger.warn("capitalizaton");
                    }
                }
                result = true;
            } else if (PRIMARY_FALSE.equalsIgnoreCase(s)) {
                if (!PRIMARY_FALSE.equals(s)) {
                    if (strict) {
                        throw new XMLStreamException("capitalization");
                    } else {
                        // FIXME: message
                        logger.warn("capitalizaton");
                    }
                }
                result = false;
            } else {
                // FIXME: message
                throw new XMLStreamException("value of @" + localName + " invalid");
            }
        }
        return result;
    }


    private static List<Schema> parseSchemaInfo(XMLStreamReader reader,
            boolean strict, String ns) throws XMLStreamException {
        /*
         * schemaInfo (schema+)
         * schema (title*)
         */
        List<Schema> schemaInfo = null;
        boolean first_schema = true;
        while (XmlStreamReaderUtils.readStart(reader, ns, "schema",
                first_schema, true)) {
            final String identifier =
                    XmlStreamReaderUtils.readAttributeValue(reader,
                            null, "identifier", true);
            final String name = XmlStreamReaderUtils.readAttributeValue(reader,
                    null, "name", true);
            final String location =
                    XmlStreamReaderUtils.readAttributeValue(reader,
                            null, "location");
            final boolean sort = parseBooleanAttribute(reader, strict,
                    null, "sort", false);
            final boolean retrieve = parseBooleanAttribute(reader, strict,
                    null, "retrieve", true);
            XmlStreamReaderUtils.consumeStart(reader);

            List<LocalizedString> titles = null;
            for (;;) {
                LocalizedString title =
                        parseStringI18N(reader, strict, ns, "title", false);
                if (title == null) {
                    break;
                }
                if (titles == null) {
                    titles = new ArrayList<>();
                }
                titles.add(title);
            } // for
            XmlStreamReaderUtils.readEnd(reader, ns, "schema");

            if (schemaInfo == null) {
                schemaInfo = new ArrayList<>();
            }
            schemaInfo.add(new Schema(identifier, name, location, sort,
                    retrieve, titles));
            first_schema = false;
        }
        return schemaInfo;
    }


    private static Map<String, String> parseConfigInfoItem(
            XMLStreamReader reader, boolean strict, String ns,
            String localName, Map<String, String> map)
            throws XMLStreamException {
        XmlStreamReaderUtils.readStart(reader, ns, localName, true, true);
        final String type = XmlStreamReaderUtils.readAttributeValue(reader,
                null, "type", true);
        XmlStreamReaderUtils.consumeStart(reader);
        final String value = XmlStreamReaderUtils.readString(reader, true);
        XmlStreamReaderUtils.readEnd(reader, ns, localName);

        if ((type != null) && (value != null)) {
            if (map == null) {
                map = new HashMap<>();
            }
            if (map.containsKey(type)) {
                logger.warn(
                        "{} with type '{}' is already present, skipping duplicate entry",
                        localName, type);
            } else {
                map.put(type, value);
            }
        }
        return map;
    }

} // class SRUExplainRecordDataParser

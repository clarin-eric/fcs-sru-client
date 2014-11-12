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
package eu.clarin.sru.client.fcs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRUExtraResponseData;
import eu.clarin.sru.client.SRUExtraResponseDataParser;
import eu.clarin.sru.client.XmlStreamReaderUtils;
import eu.clarin.sru.client.fcs.ClarinFCSEndpointDescription.DataView;
import eu.clarin.sru.client.fcs.ClarinFCSEndpointDescription.DataView.DeliveryPolicy;
import eu.clarin.sru.client.fcs.ClarinFCSEndpointDescription.ResourceInfo;


/**
 * An extra response data parser for parsing CLARIN-FCS endpoint descriptions.
 */
public class ClarinFCSEndpointDescriptionParser implements
        SRUExtraResponseDataParser {
    public static final int INFINITE_MAX_DEPTH = -1;
    public static final int DEFAULT_MAX_DEPTH  = INFINITE_MAX_DEPTH;
    private static final Logger logger =
            LoggerFactory.getLogger(ClarinFCSClientBuilder.class);
    private static final String ED_NS_URI =
            "http://clarin.eu/fcs/endpoint-description";
    private static final QName ED_ROOT_ELEMENT =
            new QName(ED_NS_URI, "EndpointDescription");
    private static final int EXPECTED_VERSION = 1;
    private static final String CAPABILITY_PREFIX =
            "http://clarin.eu/fcs/capability/";
    private static final URI CAPABILITY_BASIC_SEARCH =
            URI.create("http://clarin.eu/fcs/capability/basic-search");
    private static final String MIMETYPE_HITS_DATAVIEW =
            "application/x-clarin-fcs-hits+xml";
    private final int maxDepth;


    public ClarinFCSEndpointDescriptionParser() {
        this(DEFAULT_MAX_DEPTH);
    }


    public ClarinFCSEndpointDescriptionParser(int maxDepth) {
        if (maxDepth < -1) {
            throw new IllegalArgumentException("maxDepth < -1");
        }
        this.maxDepth = maxDepth;
    }


    @Override
    public boolean supports(QName name) {
        return ED_ROOT_ELEMENT.equals(name);
    }


    @Override
    public SRUExtraResponseData parse(XMLStreamReader reader)
            throws XMLStreamException, SRUClientException {
        final int version = parseVersion(reader);
        if (version != EXPECTED_VERSION) {
            throw new SRUClientException("Attribute 'version' of " +
                    "element '<EndpointDescription>' must be of value '1'");
        }
        logger.error("VERSION: {}", version);
        reader.next(); // consume start tag

        // Capabilities
        List<URI> capabilities = null;
        XmlStreamReaderUtils.readStart(reader, ED_NS_URI, "Capabilities", true);
        while (XmlStreamReaderUtils.readStart(reader, ED_NS_URI,
                "Capability", (capabilities == null))) {
            final String s = XmlStreamReaderUtils.readString(reader, true);
            try {
                if (!s.startsWith(CAPABILITY_PREFIX)) {
                    throw new XMLStreamException("Capabilites must start " +
                            "with prefix '" + CAPABILITY_PREFIX +
                            "' (offending value = '" + s +"')",
                            reader.getLocation());
                }
                final URI uri = new URI(s);
                if (capabilities == null) {
                    capabilities = new ArrayList<URI>();
                }
                capabilities.add(uri);
            } catch (URISyntaxException e) {
                throw new XMLStreamException("Capabilities must be encoded " +
                        "as URIs (offending value = '" + s + "')",
                        reader.getLocation(), e);
            }
            XmlStreamReaderUtils.readEnd(reader, ED_NS_URI, "Capability");
        } // while
        XmlStreamReaderUtils.readEnd(reader, ED_NS_URI, "Capabilities");
        if ((capabilities == null) ||
                (capabilities.indexOf(CAPABILITY_BASIC_SEARCH) == -1)) {
            throw new SRUClientException("Endpoint must support " +
                    "'basic-search' (" + CAPABILITY_BASIC_SEARCH +
                    ") to conform to CLARIN-FCS specification");
        }

        // SupportedDataViews
        List<DataView> supportedDataViews = null;
        XmlStreamReaderUtils.readStart(reader, ED_NS_URI,
                "SupportedDataViews", true);
        while (XmlStreamReaderUtils.readStart(reader, ED_NS_URI,
                "SupportedDataView", (supportedDataViews == null), true)) {
            final String id = XmlStreamReaderUtils.readAttributeValue(
                    reader, null, "id", true);
            if ((id.indexOf(' ') != -1) || (id.indexOf(',') != -1) ||
                    (id.indexOf(';') != -1)) {
                throw new XMLStreamException("Value of attribute 'id' on " +
                        "element '<SupportedDataView>' may not contain the " +
                        "characters ',' (comma) or ';' (semicolon) " +
                        "or ' ' (space)", reader.getLocation());
            }
            final DeliveryPolicy policy = parsePolicy(reader);
            reader.next(); // consume start tag

            final String type = XmlStreamReaderUtils.readString(reader, true);
            // do some sanity checks ...
            if (supportedDataViews != null) {
                for (DataView dataView : supportedDataViews) {
                    if (dataView.getIdentifier().equals(id)) {
                        throw new XMLStreamException("Supported data view " +
                                "with identifier '" + id +
                                "' was already declared", reader.getLocation());
                    }
                    if (dataView.getMimeType().equals(type)) {
                        throw new XMLStreamException("Supported data view " +
                                "with MIME type '" + type +
                                "' was already declared", reader.getLocation());
                    }
                }
            } else {
                supportedDataViews = new ArrayList<DataView>();
            }
            supportedDataViews.add(new DataView(id, type, policy));
            XmlStreamReaderUtils.readEnd(reader,
                    ED_NS_URI, "SupportedDataView");
        } // while
        XmlStreamReaderUtils.readEnd(reader, ED_NS_URI,
                "SupportedDataViews", true);
        boolean found = false;
        if (supportedDataViews != null) {
            for (DataView dataView : supportedDataViews) {
                if (MIMETYPE_HITS_DATAVIEW.equals(dataView.getMimeType())) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            throw new SRUClientException("Endpoint must support " +
                    "generic hits dataview (expected MIME type '" +
                    MIMETYPE_HITS_DATAVIEW +
                    "') to conform to CLARIN-FCS specification");
        }

        // Resources
        final List<ResourceInfo> resources =
                parseResources(reader, 0, supportedDataViews);

        // skip over extensions
        while (!XmlStreamReaderUtils.peekEnd(reader,
                ED_NS_URI, "EndpointDescription")) {
            if (reader.isStartElement()) {
                final String namespaceURI = reader.getNamespaceURI();
                final String localName    = reader.getLocalName();
                logger.debug("skipping over extension with element {{}}{}",
                        namespaceURI, localName);
                XmlStreamReaderUtils.skipTag(reader, namespaceURI, localName);
            }
        }

        XmlStreamReaderUtils.readEnd(reader, ED_NS_URI, "EndpointDescription");

        return new ClarinFCSEndpointDescription(version, capabilities,
                supportedDataViews, resources);
    }


    private List<ResourceInfo> parseResources(XMLStreamReader reader, int depth,
            List<DataView> supportedDataviews) throws XMLStreamException {
        List<ResourceInfo> resources = null;

        XmlStreamReaderUtils.readStart(reader, ED_NS_URI, "Resources", true);
        while (XmlStreamReaderUtils.readStart(reader, ED_NS_URI,
                "Resource", (resources == null), true)) {
            final String pid = XmlStreamReaderUtils.readAttributeValue(reader,
                    null, "pid", true);
            reader.next(); // consume start tag

            logger.debug("pid = {}", pid);

            final Map<String, String> title =
                    parseI18String(reader, "Title", true);
            logger.debug("title: {}", title);

            final Map<String, String> description =
                    parseI18String(reader, "Description", false);
            logger.debug("description: {}", description);

            final String landingPageURI =
                    XmlStreamReaderUtils.readContent(reader, ED_NS_URI,
                            "LandingPageURI", false);
            logger.debug("landingPageURI: {}", landingPageURI);

            List<String> languages = null;
            XmlStreamReaderUtils.readStart(reader,
                    ED_NS_URI, "Languages", true);
            while (XmlStreamReaderUtils.readStart(reader, ED_NS_URI,
                    "Language", (languages == null))) {
                final String language =
                        XmlStreamReaderUtils.readString(reader, true);
                XmlStreamReaderUtils.readEnd(reader, ED_NS_URI, "Language");
                if (languages == null) {
                    languages = new ArrayList<String>();
                } else {
                    for (String l : languages) {
                        if (l.equals(language)) {
                            throw new XMLStreamException("language '" +
                                    language + "' was already defined " +
                                    "in '<Language>'", reader.getLocation());
                        }
                    } // for
                }
                languages.add(language);
            } // while
            XmlStreamReaderUtils.readEnd(reader, ED_NS_URI, "Languages", true);
            logger.debug("languages: {}", languages);

            XmlStreamReaderUtils.readStart(reader, ED_NS_URI, "AvailableDataViews", true, true);
            final String dvs = XmlStreamReaderUtils.readAttributeValue(reader, null, "ref", true);
            reader.next(); // consume start tag
            XmlStreamReaderUtils.readEnd(reader, ED_NS_URI, "AvailableDataViews");

            List<DataView> dataviews = null;
            for (String dv : dvs.split("\\s+")) {
                boolean found = false;
                for (DataView dataview : supportedDataviews) {
                    if (dataview.getIdentifier().equals(dv)) {
                        found = true;
                        if (dataviews == null) {
                            dataviews = new ArrayList<DataView>();
                        }
                        dataviews.add(dataview);
                        break;
                    }
                } // for
                if (!found) {
                    throw new XMLStreamException("DataView with id '" + dv +
                            "' was not declared in <SupportedDataViews>",
                            reader.getLocation());
                }
            } // for
            logger.debug("DataViews: {}", dataviews);

            List<ResourceInfo> subResources = null;
            if (XmlStreamReaderUtils.peekStart(reader, ED_NS_URI, "Resources")) {
                final int nextDepth = depth + 1;
                if ((maxDepth == INFINITE_MAX_DEPTH) || (nextDepth < maxDepth)) {
                    subResources = parseResources(reader, nextDepth,
                            supportedDataviews);
                } else {
                    XmlStreamReaderUtils.skipTag(reader, ED_NS_URI,
                            "Resources", true);
                }
            }

            while (!XmlStreamReaderUtils.peekEnd(reader,
                    ED_NS_URI, "Resource")) {
                if (reader.isStartElement()) {
                    final String namespaceURI = reader.getNamespaceURI();
                    final String localName    = reader.getLocalName();
                    logger.debug("skipping over extension with element " +
                            "{{}}{} (resource)", namespaceURI, localName);
                    XmlStreamReaderUtils.skipTag(reader,
                            namespaceURI, localName);
                }
            } // while

            XmlStreamReaderUtils.readEnd(reader, ED_NS_URI, "Resource");

            if (resources == null) {
                resources = new ArrayList<ResourceInfo>();
            }
            resources.add(new ResourceInfo(pid, title, description,
                    landingPageURI, languages, dataviews, subResources));
        } // while
        XmlStreamReaderUtils.readEnd(reader, ED_NS_URI, "Resources");

        return resources;
    }


    private static Map<String, String> parseI18String(XMLStreamReader reader,
            String localName, boolean required) throws XMLStreamException {
        Map<String, String> result = null;
        while (XmlStreamReaderUtils.readStart(reader, ED_NS_URI, localName,
                ((result == null) && required), true)) {
            final String lang = XmlStreamReaderUtils.readAttributeValue(reader,
                    XMLConstants.XML_NS_URI, "lang", true);
            reader.next(); // skip start tag
            final String content = XmlStreamReaderUtils.readString(reader, true);
            if (result == null) {
                result = new HashMap<String, String>();
            }
            if (result.containsKey(lang)) {
                throw new XMLStreamException("language '" + lang +
                        "' already defined for element '<" + localName + ">'",
                        reader.getLocation());
            } else {
                result.put(lang, content);
            }
            XmlStreamReaderUtils.readEnd(reader, ED_NS_URI, localName);
        } // while
        return result;
    }


    private static int parseVersion(XMLStreamReader reader)
            throws XMLStreamException {
        try {
            final String s = XmlStreamReaderUtils.readAttributeValue(
                    reader, null, "version", true);
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new XMLStreamException("Attribute 'version' is not a number",
                    reader.getLocation(), e);
        }
    }


    private static DeliveryPolicy parsePolicy(XMLStreamReader reader)
            throws XMLStreamException {
        final String s = XmlStreamReaderUtils.readAttributeValue(reader,
                null, "delivery-policy", true);
        if ("send-by-default".equals(s)) {
            return DeliveryPolicy.SEND_BY_DEFAULT;
        } else if ("need-to-request".equals(s)) {
            return DeliveryPolicy.NEED_TO_REQUEST;
        } else {
            throw new XMLStreamException("Unexpected value '" + s +
                    "' for attribute 'delivery-policy' on " +
                    "element '<SupportedDataView>'", reader.getLocation());
        }
    }

} // class ClarinFCSEndpointDescriptionParser

/**
 * This software is copyright (c) 2011 by
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
package eu.clarin.sru.fcs;

import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.NodeList;

import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRURecordData;
import eu.clarin.sru.client.SRURecordDataParser;
import eu.clarin.sru.client.XmlStreamReaderUtils;


/**
 * A record parse to parse records conforming to CLARIN FCS specification. The
 * parser currently supports the KWIC view.
 */
public class ClarinFCSRecordParser implements SRURecordDataParser {
    private static class TransformHelper {
        private final DocumentBuilder builder;
        private final Transformer transformer;
        private Document document;


        private TransformHelper(DocumentBuilder builder,
                Transformer transformer) {
            if (builder == null) {
                throw new NullPointerException("builder == null");
            }
            this.builder = builder;
            if (transformer == null) {
                throw new NullPointerException("transformer == null");
            }
            this.transformer = transformer;
        }


        private DocumentFragment transform(XMLStreamReader reader)
                throws XMLStreamException, TransformerException {
            if (document == null) {
                document = builder.newDocument();
            }

            // parse STAX to DOM fragment
            DocumentFragment fragment = document.createDocumentFragment();
            DOMResult result = new DOMResult(fragment);
            transformer.transform(new StAXSource(reader), result);
            return fragment;
        }


        private void reset() {
            builder.reset();
            transformer.reset();
            document = null;
        }
    } // private class TransformHelper
    private static final Logger logger =
            LoggerFactory.getLogger(ClarinFCSRecordParser.class);
    private static final String FCS_NS =
            ClarinFCSRecordData.RECORD_SCHEMA;
    private static final String FCS_KWIC_NS = "http://clarin.eu/fcs/1.0/kwic";
    private static final String DATAVIEW_KWIC_LEGACY_TYPE = "kwic";
    private final ThreadLocal<TransformHelper> transformHelper;


    public ClarinFCSRecordParser() {
        this(DocumentBuilderFactory.newInstance(),
                TransformerFactory.newInstance());
    }


    public ClarinFCSRecordParser(final DocumentBuilderFactory builderFactory,
            final TransformerFactory transformerFactory) {
        if (builderFactory == null) {
            throw new NullPointerException("builderFactory == null");
        }
        if (transformerFactory == null) {
            throw new NullPointerException("transformerFactory == null");
        }
        this.transformHelper = new ThreadLocal<TransformHelper>() {
            @Override
            protected TransformHelper initialValue() {
                try {
                    return new TransformHelper(builderFactory.newDocumentBuilder(),
                                   transformerFactory.newTransformer());
                } catch (TransformerConfigurationException e) {
                    throw new InternalError("unexpected error creating new transformer");
                } catch (ParserConfigurationException e) {
                    throw new InternalError("unexpected error creating new document builder");
                }
            }
        };
    }


    @Override
    public String getRecordSchema() {
        return ClarinFCSRecordData.RECORD_SCHEMA;
    }


    @Override
    public SRURecordData parse(XMLStreamReader reader)
            throws XMLStreamException, SRUClientException {
        logger.debug("parsing CLARIN-FCS record");

        final TransformHelper helper = transformHelper.get();
        try {
            // Resource
            XmlStreamReaderUtils.readStart(reader, FCS_NS, "Resource", true, true);
            String pid = XmlStreamReaderUtils.readAttributeValue(reader, null, "pid");
            String ref = XmlStreamReaderUtils.readAttributeValue(reader, null, "ref");
            XmlStreamReaderUtils.consumeStart(reader);

            // Resource/Resource (optional)
            if (XmlStreamReaderUtils.readStart(reader, FCS_NS, "Resource", false)) {
                logger.info("skipping nested <Resource> element");
                XmlStreamReaderUtils.readEnd(reader, FCS_NS, "Resource", true);
            }

            // Resource/DataView
            final List<DataView> dataviews = parseDataViews(reader, helper);

            // Resource/ResourceFragment
            final List<Resource.ResourceFragment> resourceFragments =
                    parseResourceFragments(reader, helper);

            XmlStreamReaderUtils.readEnd(reader, FCS_NS, "Resource", true);

            return new ClarinFCSRecordData(pid, ref, dataviews,
                    resourceFragments);
        } finally {
            // make sure, we reset the helper
            helper.reset();
        }
    }


    private static List<DataView> parseDataViews(XMLStreamReader reader,
            TransformHelper foo) throws XMLStreamException, SRUClientException {
        List<DataView> dataviews = null;

        while (XmlStreamReaderUtils.readStart(reader, FCS_NS, "DataView", false, true)) {
            String pid = XmlStreamReaderUtils.readAttributeValue(reader, null, "pid");
            String ref = XmlStreamReaderUtils.readAttributeValue(reader, null, "ref");
            String type = XmlStreamReaderUtils.readAttributeValue(reader, null, "mime-type");
            if ((type == null) || type.isEmpty()) {
                logger.debug("element <DataView> does not carry attribute " +
                        "'mime-type'; trying attribute 'type' instead");
                type = XmlStreamReaderUtils.readAttributeValue(reader, null, "type");
                if (type != null) {
                    logger.warn("attribute 'type' is deprecated for element " +
                            "<DataView>; please use 'mime-type' attribute");
                }
            }
            if ((type == null) || type.isEmpty()) {
                throw new SRUClientException("element <DataView> needs a "
                        + "non-empty 'mime-type' (or 'type') attribute");
            }

            // consume start element and get rid of any whitespace
            XmlStreamReaderUtils.consumeStart(reader);
            XmlStreamReaderUtils.consumeWhitespace(reader);

            logger.debug("found DataView of type = {}", type);
            DataView dataview = null;
            if (KWICDataView.MIMETYPE.equals(type) ||
                    DATAVIEW_KWIC_LEGACY_TYPE.equals(type)) {
                logger.debug("parsing dataview using FCS-KWIC parser");
                dataview = parseDataViewKWIC(reader, pid, ref);
            } else {
                logger.debug("parsing dataview using generic parser");
                dataview = parseDataViewGeneric(reader, foo, type, pid, ref);
            }

            XmlStreamReaderUtils.readEnd(reader, FCS_NS, "DataView", true);

            if (dataview != null) {
                if (dataviews == null) {
                    dataviews = new LinkedList<DataView>();
                }
                dataviews.add(dataview);
            } else {
                logger.info("DataView of type = {} skipped", type);
            }
        } // while
        return dataviews;
    }


    private static List<Resource.ResourceFragment> parseResourceFragments(
            XMLStreamReader reader, TransformHelper foo)
            throws XMLStreamException, SRUClientException {
        List<Resource.ResourceFragment> resourceFragments = null;
        while (XmlStreamReaderUtils.readStart(reader, FCS_NS, "ResourceFragment", false, true)) {
            logger.debug("found ResourceFragment");
            String pid = XmlStreamReaderUtils.readAttributeValue(reader, null, "pid");
            String ref = XmlStreamReaderUtils.readAttributeValue(reader, null, "ref");
            XmlStreamReaderUtils.consumeStart(reader);
            final List<DataView> dataviews = parseDataViews(reader, foo);
            XmlStreamReaderUtils.readEnd(reader, FCS_NS, "ResourceFragment", true);

            if (resourceFragments == null) {
                resourceFragments = new LinkedList<Resource.ResourceFragment>();
            }
            resourceFragments.add(new Resource.ResourceFragment(pid, ref, dataviews));
        } // while
        return resourceFragments;
    }


    private static DataView parseDataViewGeneric(XMLStreamReader reader,
            TransformHelper helper, String type, String pid, String ref)
            throws XMLStreamException, SRUClientException {
        try {
            final DocumentFragment fragment = helper.transform(reader);
            final NodeList children = fragment.getChildNodes();
            if ((children != null) && (children.getLength() > 0)) {
                return new GenericDataView(type, pid, ref, fragment);
            } else {
                throw new SRUClientException("element <DataView> does not " +
                        "contain any nested elements");
            }
        } catch (TransformerException e) {
            throw new SRUClientException("error while parsing dataview", e);
        }
    }


    private static DataView parseDataViewKWIC(XMLStreamReader reader,
            String pid, String ref) throws XMLStreamException,
            SRUClientException {
        String left = null;
        String keyword = null;
        String right = null;

        XmlStreamReaderUtils.readStart(reader, FCS_KWIC_NS, "kwic", true);
        if (XmlStreamReaderUtils.readStart(reader, FCS_KWIC_NS, "c", false)) {
            left = XmlStreamReaderUtils.readString(reader, false);
            XmlStreamReaderUtils.readEnd(reader, FCS_KWIC_NS, "c");
        }
        keyword = XmlStreamReaderUtils.readContent(reader, FCS_KWIC_NS, "kw", true);
        if (XmlStreamReaderUtils.readStart(reader, FCS_KWIC_NS, "c", false)) {
            right = XmlStreamReaderUtils.readString(reader, false);
            XmlStreamReaderUtils.readEnd(reader, FCS_KWIC_NS, "c");
        }
        XmlStreamReaderUtils.readEnd(reader, FCS_KWIC_NS, "kwic");

        logger.debug("left='{}' keyword='{}', right='{}'", new Object[] {
                left, keyword, right }
        );
        return new KWICDataView(pid, ref, left, keyword, right);
    }

} // class ClarinFederatedContentSearchRecordParser

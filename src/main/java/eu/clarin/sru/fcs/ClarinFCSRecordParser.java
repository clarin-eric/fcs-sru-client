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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRURecordData;
import eu.clarin.sru.client.SRURecordDataParser;
import eu.clarin.sru.client.XmlStreamReaderUtils;


/**
 * A record parse to parse records conforming to CLARIN FCS specification. The
 * parser currently supports the KWIC view.
 */
public class ClarinFCSRecordParser implements
        SRURecordDataParser {
    private static final Logger logger =
            LoggerFactory.getLogger(ClarinFCSRecordParser.class);
    private static final String FCS_NS =
            ClarinFCSRecordData.RECORD_SCHEMA;
    private static final String FCS_KWIC_NS = "http://clarin.eu/fcs/1.0/kwic";
    private static final String DATAVIEW_KWIC_LEGACY_TYPE = "kwic";


    @Override
    public String getRecordSchema() {
        return ClarinFCSRecordData.RECORD_SCHEMA;
    }


    @Override
    public SRURecordData parse(XMLStreamReader reader)
            throws XMLStreamException, SRUClientException {
        logger.debug("parsing CLARIN-FCS record");
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
        final List<DataView> dataviews = parseDataViews(reader);

        // Resource/ResourceFragment
        final List<Resource.ResourceFragment> resourceFragments =
                parseResourceFragments(reader);

        XmlStreamReaderUtils.readEnd(reader, FCS_NS, "Resource", true);

        return new ClarinFCSRecordData(pid, ref, dataviews,
                resourceFragments);
    }


    private static List<DataView> parseDataViews(XMLStreamReader reader)
            throws XMLStreamException, SRUClientException {
        List<DataView> dataviews = null;

        while (XmlStreamReaderUtils.readStart(reader, FCS_NS, "DataView", false, true)) {
            String pid = XmlStreamReaderUtils.readAttributeValue(reader, null, "pid");
            String ref = XmlStreamReaderUtils.readAttributeValue(reader, null, "ref");
            String type = XmlStreamReaderUtils.readAttributeValue(reader, null, "mime-type");
            if ((type == null) || type.isEmpty()) {
                logger.debug("element <DataView> does not carry attribute " +
                        "'mime-type'; trying attribute 'type' instead");
                type = XmlStreamReaderUtils.readAttributeValue(reader, null, "type");
            }
            XmlStreamReaderUtils.consumeStart(reader);
            if ((type == null) || type.isEmpty()) {
                throw new SRUClientException("element <DataView> need as "
                        + "non-empty 'mime-type' (or 'type') attribute");
            }
            logger.debug("found DataView of type = {}", type);
            DataView dataview = null;
            if (KWICDataView.MIMETYPE.equals(type) ||
                    DATAVIEW_KWIC_LEGACY_TYPE.equals(type)) {
                dataview = parseDataViewKWIC(reader, pid, ref);
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
            XMLStreamReader reader) throws XMLStreamException,
            SRUClientException {
        List<Resource.ResourceFragment> resourceFragments = null;
        while (XmlStreamReaderUtils.readStart(reader, FCS_NS, "ResourceFragment", false, true)) {
            logger.debug("found ResourceFragment");
            String pid = XmlStreamReaderUtils.readAttributeValue(reader, null, "pid");
            String ref = XmlStreamReaderUtils.readAttributeValue(reader, null, "ref");
            XmlStreamReaderUtils.consumeStart(reader);
            List<DataView> dataviews = parseDataViews(reader);
            XmlStreamReaderUtils.readEnd(reader, FCS_NS, "ResourceFragment", true);

            if (resourceFragments == null) {
                resourceFragments = new LinkedList<Resource.ResourceFragment>();
            }
            resourceFragments.add(new Resource.ResourceFragment(pid, ref, dataviews));
        } // while
        return resourceFragments;
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
        keyword = XmlStreamReaderUtils.readContent(reader, FCS_KWIC_NS, "kw",
                true);
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

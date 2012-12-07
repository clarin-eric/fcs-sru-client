/**
 * This software is copyright (c) 2011-2012 by
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
 * A record parse to parse records conforming to CLARIN FCS specification.
 */
public class ClarinFCSRecordParser implements SRURecordDataParser {
    private static final Logger logger =
            LoggerFactory.getLogger(ClarinFCSRecordParser.class);
    private static final String FCS_NS =
            ClarinFCSRecordData.RECORD_SCHEMA;
    // TODO: make this configurable
    private final DataViewParser[] parsers = new DataViewParser[] {
            new DataViewParserGenericDOM(),
            new DataViewParserKWIC()
    };


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

        return new ClarinFCSRecordData(pid, ref, dataviews, resourceFragments);
    }


    private List<DataView> parseDataViews(XMLStreamReader reader)
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

            logger.debug("processing <DataView> of type = {}", type);

            DataViewParser parser = null;
            for (int i = 0; i < parsers.length; i++) {
                if (parsers[i].acceptType(type) &&
                        ((parser == null) ||
                         (parser.getPriority() < parsers[i].getPriority()))) {
                    parser = parsers[i];
                }
            }

            DataView dataview = null;
            if (parser != null) {
                dataview = parser.parse(reader, type, pid, ref);
            } else {
                logger.warn("no parser found for <DataView> of type = {}", type);
            }

            XmlStreamReaderUtils.readEnd(reader, FCS_NS, "DataView", true);

            if (dataview != null) {
                if (dataviews == null) {
                    dataviews = new LinkedList<DataView>();
                }
                dataviews.add(dataview);
            } else {
                logger.warn("skipped <DataView> of type = {}", type);
            }
        } // while
        return dataviews;
    }


    private List<Resource.ResourceFragment> parseResourceFragments(
            XMLStreamReader reader) throws XMLStreamException,
            SRUClientException {
        List<Resource.ResourceFragment> resourceFragments = null;
        while (XmlStreamReaderUtils.readStart(reader, FCS_NS, "ResourceFragment", false, true)) {
            logger.debug("found ResourceFragment");
            String pid = XmlStreamReaderUtils.readAttributeValue(reader, null, "pid");
            String ref = XmlStreamReaderUtils.readAttributeValue(reader, null, "ref");
            XmlStreamReaderUtils.consumeStart(reader);
            final List<DataView> dataviews = parseDataViews(reader);
            XmlStreamReaderUtils.readEnd(reader, FCS_NS, "ResourceFragment", true);

            if (resourceFragments == null) {
                resourceFragments = new LinkedList<Resource.ResourceFragment>();
            }
            resourceFragments.add(new Resource.ResourceFragment(pid, ref, dataviews));
        } // while
        return resourceFragments;
    }

} // class ClarinFCSRecordParser

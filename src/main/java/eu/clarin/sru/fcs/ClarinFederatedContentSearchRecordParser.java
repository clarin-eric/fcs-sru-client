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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRURecordData;
import eu.clarin.sru.client.SRURecordDataParser;
import eu.clarin.sru.client.XmlStreamReaderUtils;


/**
 * A record for CLARIN FCS.
 */
public class ClarinFederatedContentSearchRecordParser implements
        SRURecordDataParser {
    private static final Logger logger =
            LoggerFactory.getLogger(ClarinFederatedContentSearchRecordParser.class);
    public static final String FCS_NS =
            ClarinFederatedContentSearchRecordData.RECORD_SCHEMA;
    private static final String FCS_KWIC_NS = "http://clarin.eu/fcs/1.0/kwic";
    private static final String DATAVIEW_KWIC = "kwic";


    @Override
    public String getRecordSchema() {
        return ClarinFederatedContentSearchRecordData.RECORD_SCHEMA;
    }


    @Override
    public SRURecordData parse(XMLStreamReader reader)
            throws XMLStreamException, SRUClientException {
        XmlStreamReaderUtils.readStart(reader, FCS_NS, "Resource", true, true);
        String pid = XmlStreamReaderUtils.readAttributeValue(reader, null,
                "pid");
        XmlStreamReaderUtils.consumeStart(reader);

        String left = null;
        String keyword = null;
        String right = null;

        boolean first = true;
        boolean kwic = false;

        while (XmlStreamReaderUtils.readStart(reader, FCS_NS, "DataView",
                first, true)) {
            first = false;
            String type = XmlStreamReaderUtils.readAttributeValue(reader, null,
                    "type");
            XmlStreamReaderUtils.consumeStart(reader);
            if ((type == null) || type.isEmpty()) {
                throw new SRUClientException(
                        "DataView element need a non-empty 'type' attribute");
            }
            logger.debug("found DataView @type = {}", type);
            if (DATAVIEW_KWIC.equals(type)) {
                if (kwic) {
                    throw new SRUClientException(
                            "only one KWIC dataview is allowed");
                }
                XmlStreamReaderUtils.readStart(reader, FCS_KWIC_NS, "kwic",
                        true);
                if (XmlStreamReaderUtils.readStart(reader, FCS_KWIC_NS, "c",
                        false)) {
                    left = XmlStreamReaderUtils.readString(reader, false);
                    XmlStreamReaderUtils.readEnd(reader, FCS_KWIC_NS, "c");
                }
                keyword = XmlStreamReaderUtils.readContent(reader, FCS_KWIC_NS,
                        "kw", true);
                if (XmlStreamReaderUtils.readStart(reader, FCS_KWIC_NS, "c",
                        false)) {
                    right = XmlStreamReaderUtils.readString(reader, false);
                    XmlStreamReaderUtils.readEnd(reader, FCS_KWIC_NS, "c");
                }
                XmlStreamReaderUtils.readEnd(reader, FCS_KWIC_NS, "kwic");
                kwic = true;
            } else {
                logger.warn("skipping dataview of type '{}'", type);
            }
            XmlStreamReaderUtils.readEnd(reader, FCS_NS, "DataView", true);
        } // while

        XmlStreamReaderUtils.readEnd(reader, FCS_NS, "Resource");

        if (kwic) {
            return new ClarinFederatedContentSearchRecordData(pid, left,
                    keyword, right);
        } else {
            throw new SRUClientException("no mandatroy kwic dataview found");
        }
    }

} // class ClarinFederatedContentSearchRecordParser

package eu.clarin.sru.fcs;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRURecordData;
import eu.clarin.sru.client.SRURecordDataParser;
import eu.clarin.sru.client.XmlStreamReaderUtils;

public class ClarinFederatedContentSearchRecordParser implements
        SRURecordDataParser {
    private static final Logger logger =
            LoggerFactory.getLogger(ClarinFederatedContentSearchRecordParser.class);
    public static final String FCS_NS = "http://clarin.eu/fcs/1.0";
    public static final String FCS_RECORD_SCHEMA = FCS_NS;
    public static final String FCS_KWIC_NS = "http://clarin.eu/fcs/1.0/kwic";
    private static final String DATAVIEW_KWIC = "kwic";

    
    @Override
    public String getRecordSchema() {
        return FCS_RECORD_SCHEMA;
    }

    
    @Override
    public SRURecordData parse(XMLStreamReader reader)
            throws XMLStreamException, SRUClientException {
        XmlStreamReaderUtils.readStart(reader, FCS_NS, "Resource", true, true);
        String pid = XmlStreamReaderUtils.readAttributeValue(reader, null, "pid");
        XmlStreamReaderUtils.consumeStart(reader);
        
        String left    = null;
        String keyword = null;
        String right   = null;

        boolean first = true;
        boolean kwic = false;
        
        while (XmlStreamReaderUtils.readStart(reader, FCS_NS, "DataView", first, true)) {
            first = false;
            String type = XmlStreamReaderUtils.readAttributeValue(reader, null, "type");
            XmlStreamReaderUtils.consumeStart(reader);
            if ((type == null) || type.isEmpty()) {
                throw new SRUClientException(
                        "DataView element need a non-empty 'type' attribute");
            }
            logger.debug("found DataView @type = {}", type);
            if (DATAVIEW_KWIC.equals(type)) {
                if (kwic) {
                    throw new SRUClientException("only one KWIC dataview is allowed");
                }
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

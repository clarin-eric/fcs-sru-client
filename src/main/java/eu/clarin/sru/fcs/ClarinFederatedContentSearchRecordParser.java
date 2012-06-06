package eu.clarin.sru.fcs;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import eu.clarin.sru.client.SRURecordData;
import eu.clarin.sru.client.SRURecordDataParser;
import eu.clarin.sru.client.XmlStreamReaderUtils;

public class ClarinFederatedContentSearchRecordParser implements
        SRURecordDataParser {
    public static final String FCS_NS = "http://clarin.eu/fcs/1.0";
    public static final String FCS_RECORD_SCHEMA = FCS_NS;
    public static final String FCS_KWIC_NS = "http://clarin.eu/fcs/1.0/kwic";

    
    @Override
    public String getRecordSchema() {
        return FCS_NS;
    }

    
    @Override
    public SRURecordData parse(XMLStreamReader reader)
            throws XMLStreamException {
        XmlStreamReaderUtils.readStart(reader, FCS_NS, "Resource", true, true);
        String pid = XmlStreamReaderUtils.readAttributeValue(reader, null, "pid");
        XmlStreamReaderUtils.consumeStart(reader);
        
        XmlStreamReaderUtils.readStart(reader, FCS_NS, "DataView", true, true);
        String type = XmlStreamReaderUtils.readAttributeValue(reader, null, "type");
        XmlStreamReaderUtils.consumeStart(reader);
        String left    = null;
        String keyword = null;
        String right   = null;
        if ((type != null) && "kwic".equals(type)) {
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
        }

        XmlStreamReaderUtils.readEnd(reader, FCS_NS, "DataView", true);
        
        XmlStreamReaderUtils.readEnd(reader, FCS_NS, "Resource");
        if (keyword != null) {
            return new ClarinFederatedContentSearchRecordData(pid, left, keyword, right);
        } else {
            return null;
        }
    }

} // class ClarinFederatedContentSearchRecordParser

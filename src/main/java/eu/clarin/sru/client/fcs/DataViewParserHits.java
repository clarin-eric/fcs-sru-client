package eu.clarin.sru.client.fcs;

import java.util.Arrays;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.XmlStreamReaderUtils;


public final class DataViewParserHits implements DataViewParser {
    private static final int OFFSET_CHUNK_SIZE = 8;
    private static final String FCS_HITS_NS =
            "http://clarin.eu/fcs/dataview/hits";
    private static final Logger logger =
            LoggerFactory.getLogger(DataViewParserHits.class);


    @Override
    public boolean acceptType(String type) {
        return DataViewHits.TYPE.equals(type);
    }


    @Override
    public int getPriority() {
        return 1000;
    }


    @Override
    public DataView parse(XMLStreamReader reader, String type, String pid,
            String ref) throws XMLStreamException, SRUClientException {
        int offsets[] = new int[OFFSET_CHUNK_SIZE];
        int offsets_idx = 0;
        StringBuilder buffer = new StringBuilder();
        XmlStreamReaderUtils.readStart(reader, FCS_HITS_NS, "Result", true);

        int idx = 0;
        while (!XmlStreamReaderUtils.peekEnd(reader, FCS_HITS_NS, "Result")) {
            if (buffer.length() > 0) {
                if (!Character.isWhitespace(buffer.charAt(buffer.length() - 1))) {
                    buffer.append(' ');
                }
                idx = buffer.length();
            }

            if (XmlStreamReaderUtils.readStart(reader, FCS_HITS_NS, "Hit", false)) {
                String hit = XmlStreamReaderUtils.readString(reader, false);
                XmlStreamReaderUtils.readEnd(reader, FCS_HITS_NS, "Hit");
                if (hit.length() > 0) {
                    buffer.append(hit);
                    if (offsets_idx == offsets.length) {
                        offsets = Arrays.copyOf(offsets, offsets.length + 8);
                    }
                    /*
                     * add pair of offsets and simultaneously increase index
                     */
                    offsets[offsets_idx++] = idx;
                    offsets[offsets_idx++] = idx + hit.length();
                } else {
                    logger.warn("skipping empty <Hit> element within <Result> element");
                }
            } else {
                buffer.append(XmlStreamReaderUtils.readString(reader, false));
            }
        } // while
        XmlStreamReaderUtils.readEnd(reader, FCS_HITS_NS, "Result");

        final String text = buffer.toString();
        return new DataViewHits(pid, ref, text, offsets, offsets_idx);
    }

} // class DataViewParserHits

/**
 * This software is copyright (c) 2012-2013 by
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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.XmlStreamReaderUtils;

public class DataViewParserKWIC implements DataViewParser {
    private static final String FCS_KWIC_NS = "http://clarin.eu/fcs/1.0/kwic";
    private static final String KWIC_LEGACY_TYPE = "kwic";
    private static final String KWIC_TYPE =
            "application/x-clarin-fcs-kwic+xml";
    private static final Logger logger =
            LoggerFactory.getLogger(DataViewParserKWIC.class);

    @Override
    public boolean acceptType(String type) {
        return KWIC_TYPE.equals(type) || KWIC_LEGACY_TYPE.equals(type);
    }

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    public DataView parse(XMLStreamReader reader, String type, String pid,
            String ref) throws XMLStreamException, SRUClientException {
        if (KWIC_LEGACY_TYPE.equals(type)) {
            logger.warn("type '" + KWIC_LEGACY_TYPE + "' is deprecated " +
                    "for a KWIC <DataView>, please use '" +
                    KWIC_TYPE + "' instead");
        }
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

        logger.debug("left='{}' keyword='{}', right='{}'",
                left, keyword, right);

        logger.warn("Upgraded deprecated KWIC dataview to HITS dataview. " +
                "Please upgrade to the new CLARIN-FCS specification " +
                "as soon as possible.");
        final int[] offsets    = new int[3];
        final StringBuilder sb = new StringBuilder();
        if (left != null) {
            sb.append(left);
            if (!Character.isWhitespace(sb.charAt(sb.length() - 1))) {
                sb.append(" ");
            }
        }
        offsets[0] = sb.length();
        sb.append(keyword);
        offsets[1] = sb.length();
        if (right != null) {
            if (!Character.isWhitespace(sb.charAt(sb.length() - 1))) {
                sb.append(" ");
            }
            sb.append(right);
        }
        return new DataViewHits(pid, ref, sb.toString(), offsets, 3);
    }

} // class DataViewParserKWIC

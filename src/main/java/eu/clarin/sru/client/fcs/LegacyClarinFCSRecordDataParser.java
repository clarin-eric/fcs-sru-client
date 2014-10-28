package eu.clarin.sru.client.fcs;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import eu.clarin.sru.client.SRUClientException;
import eu.clarin.sru.client.SRURecordData;


/**
 * A record data parse to parse legacy records.
 *
 * @deprecated Use only to talk to legacy clients. Endpoints should upgrade to
 *             recent CLARIN-FCS specification.
 */
@Deprecated
public class LegacyClarinFCSRecordDataParser extends
        AbstractClarinFCSRecordDataParser {

    
    /**
     * Constructor.
     *
     * @param parsers
     *            the list of data view parsers to be used by this record data
     *            parser. This list should contain one
     *            {@link DataViewParserGenericDOM} or
     *            {@link DataViewParserGenericString} instance.
     * @throws NullPointerException
     *             if parsers is <code>null</code>
     * @throws IllegalArgumentException
     *             if parsers is empty or contains duplicate entries
     */
    public LegacyClarinFCSRecordDataParser(List<DataViewParser> parsers) {
        super(parsers);
    }


    @Override
    public String getRecordSchema() {
        return ClarinFCSRecordData.LEGACY_RECORD_SCHEMA;
    }


    @Override
    public SRURecordData parse(XMLStreamReader reader)
            throws XMLStreamException, SRUClientException {
        logger.warn("The endpoint supplied data in the deprecated CLARIN-FCS " +
            "record data format. Please upgrade to the new CLARIN-FCS " +
                "specification as soon as possible.");
        return parse(reader, ClarinFCSRecordData.LEGACY_RECORD_SCHEMA);
    }

} // class LegacyClarinFCSRecordDataParser

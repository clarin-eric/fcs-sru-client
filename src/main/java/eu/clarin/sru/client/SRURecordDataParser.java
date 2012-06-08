package eu.clarin.sru.client;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface SRURecordDataParser {

    public String getRecordSchema();
    
    public SRURecordData parse(XMLStreamReader reader)
            throws XMLStreamException, SRUClientException;

} // interface SRURecordDataParser

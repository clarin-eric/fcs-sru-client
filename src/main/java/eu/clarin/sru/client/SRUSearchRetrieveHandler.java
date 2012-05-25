package eu.clarin.sru.client;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface SRUSearchRetrieveHandler {

    public void onStartRecords() throws SRUClientException;
    
    public void onFinishRecords(int nextRecordPosition) throws SRUClientException;

    public void onRecord(String schema, String identifier, int position,
            XMLStreamReader reader) throws XMLStreamException,
            SRUClientException;

} // interface SRUSearchRetrieveHandler

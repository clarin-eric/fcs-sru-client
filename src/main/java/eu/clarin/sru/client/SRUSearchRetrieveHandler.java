package eu.clarin.sru.client;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface SRUSearchRetrieveHandler {

    public void onFatalError(List<SRUDiagnostic> diagnistics)
            throws SRUClientException;

    public void onStartRecords() throws SRUClientException;
    
    public void onFinishRecords(int nextRecordPosition) throws SRUClientException;

    public void onRecord(String schema, String identifier, int position,
            XMLStreamReader reader) throws XMLStreamException,
            SRUClientException;

    public void onRecord(String identifier, int position, SRURecordData data)
            throws SRUClientException;

} // interface SRUSearchRetrieveHandler

package eu.clarin.sru.client;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface SRUSearchRetrieveHandler {

    public void onDiagnostics(List<SRUDiagnostic> diagnostics)
            throws SRUClientException;

    public void onRequestStatistics(int bytes, long millisTotal,
            long millisNetwork, long millisParsing);

    public void onExtraResponseData(XMLStreamReader reader)
            throws XMLStreamException, SRUClientException;

    public void onStartRecords(int numberOfRecords, int resultSetId,
            int resultSetIdleTime) throws SRUClientException;

    public void onFinishRecords(int nextRecordPosition)
            throws SRUClientException;

    public void onRecord(String schema, String identifier, int position,
            XMLStreamReader reader) throws XMLStreamException,
            SRUClientException;

    public void onRecord(String identifier, int position, SRURecordData data)
            throws SRUClientException;

    public void onSurrogateRecord(String identifier, int position,
            SRUDiagnostic data) throws SRUClientException;

    public void onExtraRecordData(String identifier, int position,
            XMLStreamReader reader) throws XMLStreamException,
            SRUClientException;

} // interface SRUSearchRetrieveHandler

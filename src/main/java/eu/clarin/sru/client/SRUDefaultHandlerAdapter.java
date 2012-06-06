package eu.clarin.sru.client;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class SRUDefaultHandlerAdapter implements SRUDefaultHandler {

    @Override
    public void onFatalError(List<SRUDiagnostic> diagnistics)
            throws SRUClientException {
    }

    
    @Override
    public void onStartTerms() throws SRUClientException {
    }


    @Override
    public void onFinishTerms() throws SRUClientException {
    }


    @Override
    public void onTerm(String value, int numberOfRecords, String displayTerm,
            WhereInList whereInList) throws SRUClientException {
    }


    @Override
    public void onExtraTermData(String value, XMLStreamReader reader)
            throws XMLStreamException, SRUClientException {
    }


    @Override
    public void onStartRecords() throws SRUClientException {
    }


    @Override
    public void onFinishRecords(int nextRecordPosition) throws SRUClientException {
    }


    @Override
    public void onRecord(String schema, String identifier, int position,
            XMLStreamReader reader) throws XMLStreamException,
            SRUClientException {
    }


    @Override
    public void onRecord(String identifier, int position, SRURecordData data)
            throws SRUClientException {
    }


    @Override
    public void onSurrogateRecord(String identifier, int position,
            SRUDiagnostic data) throws SRUClientException {
    }


    @Override
    public void onExtraRecordData(String identifier, int position,
            XMLStreamReader reader) throws XMLStreamException,
            SRUClientException {
    }

} // class SRUDefaultHandler

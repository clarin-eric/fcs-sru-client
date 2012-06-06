package eu.clarin.sru.client;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface SRUScanHandler {
    public enum WhereInList {
        FIRST, LAST, ONLY, INNER; 
    }
    
    public void onFatalError(List<SRUDiagnostic> diagnistics)
            throws SRUClientException;

    public void onStartTerms() throws SRUClientException;

    public void onFinishTerms() throws SRUClientException;

    public void onTerm(String value, int numberOfRecords, String displayTerm,
            WhereInList whereInList) throws SRUClientException;
    
    /* XXX: not yet implemented in client */
    public void onExtraTermData(String value, XMLStreamReader reader)
            throws XMLStreamException, SRUClientException;

} // interface SRUScanHandler

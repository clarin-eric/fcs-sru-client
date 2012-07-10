package eu.clarin.sru.client;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface SRUScanHandler {
    public enum WhereInList {
        FIRST, LAST, ONLY, INNER;
    }

    public void onDiagnostics(List<SRUDiagnostic> diagnostics)
            throws SRUClientException;

    public void onRequestStatistics(int bytes, long millisTotal,
            long millisNetwork, long millisParsing);

    public void onExtraResponseData(XMLStreamReader reader)
            throws XMLStreamException, SRUClientException;

    public void onStartTerms() throws SRUClientException;

    public void onFinishTerms() throws SRUClientException;

    public void onTerm(String value, int numberOfRecords, String displayTerm,
            WhereInList whereInList) throws SRUClientException;

    public void onExtraTermData(String value, XMLStreamReader reader)
            throws XMLStreamException, SRUClientException;

} // interface SRUScanHandler

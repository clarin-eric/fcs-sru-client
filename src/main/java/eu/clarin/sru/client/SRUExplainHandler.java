package eu.clarin.sru.client;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface SRUExplainHandler {

    public void onDiagnostics(List<SRUDiagnostic> diagnostics)
            throws SRUClientException;

    public void onRequestStatistics(int bytes, long millisTotal,
            long millisNetwork, long millisParsing);

    public void onExtraResponseData(XMLStreamReader reader)
            throws XMLStreamException, SRUClientException;

} // interface SRUExplainHandler

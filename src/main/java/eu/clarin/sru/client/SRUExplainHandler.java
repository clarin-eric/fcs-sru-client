package eu.clarin.sru.client;

import java.util.List;

public interface SRUExplainHandler {

    public void onFatalError(List<SRUDiagnostic> diagnistics)
            throws SRUClientException;

} // interface SRUExplainHandler

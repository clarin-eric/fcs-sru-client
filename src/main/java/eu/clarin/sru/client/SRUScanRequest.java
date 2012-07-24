package eu.clarin.sru.client;

public final class SRUScanRequest extends SRUAbstractRequest {
    private String scanClause;
    private int responsePosition = -1;
    private int maximumTerms = -1;


    protected SRUScanRequest(String baseURI) {
        super(baseURI);
    }


    public String getScanClause() {
        return scanClause;
    }


    public void setScanClause(String scanClause) {
        if (scanClause == null) {
            throw new NullPointerException("scanClause == null");
        }
        if (scanClause.isEmpty()) {
            throw new IllegalArgumentException("scanClause is an empty string");
        }
        this.scanClause = scanClause;
    }


    public int getResponsePosition() {
        return responsePosition;
    }


    public void setResponsePosition(int responsePosition) {
        if (responsePosition < 0) {
            throw new IllegalArgumentException("responsePosition < 0");
        }
        this.responsePosition = responsePosition;
    }


    public int getMaximumTerms() {
        return maximumTerms;
    }


    public void setMaximumTerms(int maximumTerms) {
        if (maximumTerms < 0) {
            throw new IllegalArgumentException("maximumTerms < 0");
        }
        this.maximumTerms = maximumTerms;
    }


    @Override
    protected SRUOperation getOperation() {
        return SRUOperation.SCAN;
    }


    @Override
    protected void addParametersToURI(URIBuilder uriBuilder)
            throws SRUClientException {
        // scanClause
        if ((scanClause == null) || scanClause.isEmpty()) {
            throw new SRUClientException(
                    "mandatory argument 'scanClause' not set or empty");
        }
        uriBuilder.append(PARAM_SCAN_CLAUSE, scanClause);

        // responsePosition
        if (responsePosition > -1) {
            uriBuilder.append(PARAM_RESPONSE_POSITION, responsePosition);
        }

        // maximumTerms
        if (maximumTerms > -1) {
            uriBuilder.append(PARAM_MAXIMUM_TERMS, maximumTerms);
        }
    }

} // class SRUScanRequest

package eu.clarin.sru.client;

public final class SRUExplainRequest extends SRUAbstractRequest {
    private SRURecordPacking recordPacking;


    protected SRUExplainRequest(String baseURI) {
        super(baseURI);
    }


    public void setRecordPacking(SRURecordPacking recordPacking) {
        if (recordPacking == null) {
            throw new NullPointerException("recordPacking == null");
        }
        this.recordPacking = recordPacking;
    }


    public SRURecordPacking getRecordPacking() {
        return recordPacking;
    }


    @Override
    protected SRUOperation getOperation() {
        return SRUOperation.EXPLAIN;
    }


    @Override
    protected void addParametersToURI(StringBuilder uri)
            throws SRUClientException {
        // recordPacking
        if (recordPacking != null) {
            uri.append('&').append(PARAM_RECORD_PACKING).append('=');
            switch (recordPacking) {
            case XML:
                uri.append(RECORD_PACKING_XML);
                break;
            case STRING:
                uri.append(RECORD_PACKING_STRING);
                break;
            default:
                throw new SRUClientException("unsupported record packing: " +
                        recordPacking);
            } // switch
        }
    }

} // class SRUExplainRequest

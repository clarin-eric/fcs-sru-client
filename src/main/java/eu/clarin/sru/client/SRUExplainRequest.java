package eu.clarin.sru.client;

public class SRUExplainRequest extends SRUAbstractRequest {
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

} // class SRUExplainRequest


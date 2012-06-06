package eu.clarin.sru.client;

public class SRUSearchRetrieveRequest extends SRUAbstractRequest {
    private String query;
    private int startRecord = -1;
    private int maximumRecords = -1;
    private SRURecordPacking recordPacking;
    private String recordSchema;
    private int resultSetTTL = -1;


    protected SRUSearchRetrieveRequest(String baseURI) {
        super(baseURI);
    }


    public String getQuery() {
        return query;
    }


    public void setQuery(String query) {
        if (query == null) {
            throw new NullPointerException("query == null");
        }
        if (query.isEmpty()) {
            throw new IllegalArgumentException("query is an empty string");
        }
        this.query = query;
    }


    public int getStartRecord() {
        return startRecord;
    }


    public void setStartRecord(int startRecord) {
        if (startRecord < 1) {
            throw new IllegalArgumentException("startRecord < 1");
        }
        this.startRecord = startRecord;
    }


    public int getMaximumRecords() {
        return maximumRecords;
    }


    public void setMaximumRecords(int maximumRecords) {
        if (maximumRecords < 0) {
            throw new IllegalArgumentException("maximumRecords < 0");
        }
        this.maximumRecords = maximumRecords;
    }


    public String getRecordSchema() {
        return recordSchema;
    }


    public void setRecordSchema(String recordSchema) {
        this.recordSchema = recordSchema;
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


    public int getResultSetTTL() {
        return resultSetTTL;
    }


    public void setResultSetTTL(int resultSetTTL) {
        this.resultSetTTL = resultSetTTL;
    }

} // class SRUSearchRetrieveRequest

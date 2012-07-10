package eu.clarin.sru.client;

public final class SRUSearchRetrieveRequest extends SRUAbstractRequest {
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


    @Override
    protected SRUOperation getOperation() {
        return SRUOperation.SEARCH_RETRIEVE;
    }


    @Override
    protected void addParametersToURI(URIBuilder uriBuilder)
            throws SRUClientException {
        // query
        uriBuilder.append(PARAM_QUERY, query);

        // startRecord
        if (startRecord > 0) {
            uriBuilder.append(PARAM_START_RECORD, startRecord);
        }

        // maximumRecords
        if (maximumRecords > -1) {
            uriBuilder.append(PARAM_MAXIMUM_RECORDS, maximumRecords);
        }

        // recordPacking
        if (recordPacking != null) {
            switch (recordPacking) {
            case XML:
                uriBuilder.append(PARAM_RECORD_PACKING, RECORD_PACKING_XML);
                break;
            case STRING:
                uriBuilder.append(PARAM_RECORD_PACKING, RECORD_PACKING_STRING);
                break;
            default:
                throw new SRUClientException("unsupported record packing: " +
                        recordPacking);
            } // switch
        }

        // recordSchema
        if (recordSchema != null) {
            uriBuilder.append(PARAM_RECORD_SCHEMA, recordSchema);
        }

        // resultSetTTL
        if (resultSetTTL > -1) {
            uriBuilder.append(PARAM_RESULT_SET_TTL, resultSetTTL);
        }
    }

} // class SRUSearchRetrieveRequest

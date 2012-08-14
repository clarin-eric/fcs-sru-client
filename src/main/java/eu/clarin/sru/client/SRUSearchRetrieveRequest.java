/**
 * This software is copyright (c) 2011 by
 *  - Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.client;

/**
 * An object for performing a <em>explain</em> operation.
 * <p>
 * The following argument arguments are mandatory:
 * </p>
 * <ul>
 * <li><em>query</em></li>
 * </ul>
 * 
 * @see SRUSearchRetrieveHandler
 * @see <a href="http://www.loc.gov/standards/sru/specs/search-retrieve.html">
 *      SRU SearchRetrieve Operation</a>
 */
public final class SRUSearchRetrieveRequest extends SRUAbstractRequest {
    private String query;
    private int startRecord = -1;
    private int maximumRecords = -1;
    private SRURecordPacking recordPacking;
    private String recordSchema;
    private int resultSetTTL = -1;


    /**
     * Constructor.
     * 
     * @param baseURI
     *            the baseURI of the endpoint
     */
    public SRUSearchRetrieveRequest(String baseURI) {
        super(baseURI);
    }


    /**
     * Get the value of the <em>query</em> argument for this request.
     * 
     * @return the value for the <em>query</em> argument or <code>null</code> of
     *         none was set
     */
    public String getQuery() {
        return query;
    }


    /**
     * Set the value of the <em>query</em> argument for this request.
     * 
     * @param query
     *            the value for the <em>query</em> argument
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @throws IllegalArgumentException
     *             if any argument is invalid
     */
    public void setQuery(String query) {
        if (query == null) {
            throw new NullPointerException("query == null");
        }
        if (query.isEmpty()) {
            throw new IllegalArgumentException("query is an empty string");
        }
        this.query = query;
    }


    /**
     * Get the value of the <em>startRecord</em> argument for this request.
     * 
     * @return the value for the <em>startRecord</em> argument or
     *         <code>-1</code> of none was set
     */
    public int getStartRecord() {
        return startRecord;
    }


    /**
     * Set the value of the <em>startRecord</em> argument for this request.
     * 
     * @param startRecord
     *            the value for the <em>startRecord</em> argument
     * @throws IllegalArgumentException
     *             if any argument is invalid
     */
    public void setStartRecord(int startRecord) {
        if (startRecord < 1) {
            throw new IllegalArgumentException("startRecord < 1");
        }
        this.startRecord = startRecord;
    }


    /**
     * Get the value of the <em>maximumRecords</em> argument for this request.
     * 
     * @return the value for the <em>maximumRecords</em> argument or
     *         <code>-1</code> of none was set
     */
    public int getMaximumRecords() {
        return maximumRecords;
    }


    /**
     * Set the value of the <em>maximumRecords</em> argument for this request.
     * 
     * @param maximumRecords
     *            the value for the <em>maximumRecords</em> argument
     * @throws IllegalArgumentException
     *             if any argument is invalid
     */
    public void setMaximumRecords(int maximumRecords) {
        if (maximumRecords < 0) {
            throw new IllegalArgumentException("maximumRecords < 0");
        }
        this.maximumRecords = maximumRecords;
    }


    /**
     * Get the value of the <em>recordSchema</em> argument for this request.
     * 
     * @return the value for the <em>recordSchema</em> argument or
     *         <code>null</code> of none was set
     */
    public String getRecordSchema() {
        return recordSchema;
    }


    /**
     * Set the value of the <em>recordSchema</em> argument for this request.
     * 
     * @param recordSchema
     *            the value for the <em>recordSchema</em> argument
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @throws IllegalArgumentException
     *             if any argument is invalid
     */
    public void setRecordSchema(String recordSchema) {
        this.recordSchema = recordSchema;
    }


    /**
     * Get the value of the <em>recordSchema</em> argument for this request.
     * 
     * @return the value for the <em>recordSchema</em> argument or
     *         <code>null</code> of none was set
     */
    public SRURecordPacking getRecordPacking() {
        return recordPacking;
    }


    /**
     * Set the value of the <em>recordPacking</em> argument for this request.
     * 
     * @param recordPacking
     *            the value for the <em>recordPacking</em> argument
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     */
    public void setRecordPacking(SRURecordPacking recordPacking) {
        if (recordPacking == null) {
            throw new NullPointerException("recordPacking == null");
        }
        this.recordPacking = recordPacking;
    }


    /**
     * Get the value of the <em>resultSetTTL</em> argument for this request.
     * 
     * @return the value for the <em>resultSetTTL</em> argument or
     *         <code>-1</code> of none was set
     */
    public int getResultSetTTL() {
        return resultSetTTL;
    }


    /**
     * Set the value of the <em>resultSetTTL</em> argument for this request.
     * 
     * @param resultSetTTL
     *            the value for the <em>resultSetTTL</em> argument
     * @throws IllegalArgumentException
     *             if any argument is invalid
     */
    public void setResultSetTTL(int resultSetTTL) {
        this.resultSetTTL = resultSetTTL;
    }


    @Override
    SRUOperation getOperation() {
        return SRUOperation.SEARCH_RETRIEVE;
    }


    @Override
    void addParametersToURI(URIBuilder uriBuilder) throws SRUClientException {
        // query
        if ((query == null) || query.isEmpty()) {
            throw new SRUClientException(
                    "mandatory argument 'query' not set or empty");
        }
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

/**
 * This software is copyright (c) 2012-2022 by
 *  - Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.client;

import java.net.URI;

/**
 * An object for performing a <em>searchRetrieve</em> operation.
 * <p>
 * The following argument arguments are mandatory:
 * </p>
 * <ul>
 * <li><em>query</em></li>
 * <li><em>queryType</em>, if using query language other than CQL (only SRU 2.0)</li>
 * </ul>
 *
 * @see SRUSearchRetrieveHandler
 * @see <a href="http://www.loc.gov/standards/sru/specs/search-retrieve.html">
 *      SRU SearchRetrieve Operation</a>
 */
public class SRUSearchRetrieveRequest extends SRUAbstractRequest {
    /** for end-point conformance testing only. never use in production. */
    public static final String X_MALFORMED_QUERY =
            "x-malformed-query";
    /** for end-point conformance testing only. never use in production. */
    public static final String X_MALFORMED_START_RECORD =
            "x-malformed-startRecord";
    /** for end-point conformance testing only. never use in production. */
    public static final String X_MALFORMED_MAXIMUM_RECORDS =
            "x-malformed-maximumRecords";
    /** for end-point conformance testing only. never use in production. */
    public static final String X_MALFORMED_RECORD_XML_ESCAPING =
            "x-malformed-recordPacking";
    private String queryType;
    private String query;
    private int startRecord = -1;
    private int maximumRecords = -1;
    private SRURecordXmlEscaping recordXmlEscaping;
    private SRURecordPacking recordPacking;
    private String recordSchema;
    private int resultSetTTL = -1;


    /**
     * Constructor.
     *
     * @param baseURI
     *            the baseURI of the endpoint
     */
    public SRUSearchRetrieveRequest(URI baseURI) {
        super(baseURI);
    }


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
     * (SRU 2.0) Get the value of the <em>queryType</em> argument for this
     * request.
     *
     * @return the value for the <em>queryType</em> argument or
     *         <code>null</code> of none was set
     */
    public String getQueryType() {
        return queryType;
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
     * Set the value of the <em>queryType</em> (SRU 2.0) and the <em>query</em>
     * argument for this request.
     * <p>
     * For SRU 1.1 and SRU 1.2 requests use the following:</p>
     * <pre>
     * {@code
     * String cql_query = ...
     * SRUSearchRetrieveRequest req =
     *      new SRUSearchRetrieveRequest("http://endpoint.example.org");
     * req.setQuery(SRUClientConstants.QUERY_TYPE_CQL, cql_query);
     * }
     * </pre>
     *
     * @param queryType
     *            the value for the <em>queryType</em> argument
     * @param query
     *            the value for the <em>query</em> argument
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @throws IllegalArgumentException
     *             if any argument is invalid
     * @see SRUClientConstants#QUERY_TYPE_CQL
     */
    public void setQuery(String queryType, String query) {
        if (query == null) {
            throw new NullPointerException("query == null");
        }
        if (query.isEmpty()) {
            throw new IllegalArgumentException("query is an empty string");
        }
        if (queryType == null) {
            throw new NullPointerException("queryType == null");
        }
        if (queryType.isEmpty()) {
            throw new IllegalArgumentException("queryType is an empty string");
        }
        for (int i = 0; i < queryType.length(); i++) {
            final char ch = queryType.charAt(i);
            if (!((ch >= 'a' && ch <= 'z') ||
                    (ch >= 'A' && ch <= 'Z') ||
                    (ch >= '0' && ch <= '9') ||
                    ((i > 0) && ((ch == '-') || ch == '_')))) {
                throw new IllegalArgumentException(
                        "queryType contains illegal characters");
            }
        }

        this.queryType = queryType;
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
     * Get the <em>recordXmlEscpaing</em> (SRU 2.0) or <em>recordPacking</em>
     * (SRU 1.1 and SRU 1.2) parameter of this request.
     *
     * @return the requested record XML escaping
     * @see SRURecordXmlEscaping
     */
    public SRURecordXmlEscaping getRecordXmlEscaping() {
        return recordXmlEscaping;
    }


    /**
     * Set the <em>recordXmlEscpaing</em> (SRU 2.0) or <em>recordPacking</em>
     * (SRU 1.1 and SRU 1.2) parameter of this request.
     *
     * @param recordXmlEscaping
     *            the requested record XML escaping
     * @see SRURecordXmlEscaping
     */
    public void setRecordXmlEscaping(SRURecordXmlEscaping recordXmlEscaping) {
        if (recordXmlEscaping == null) {
            throw new NullPointerException("recordXmlEscaping == null");
        }
        this.recordXmlEscaping = recordXmlEscaping;
    }


    /**
     * Get the <em>recordPacking</em> (SRU 2.0) parameter of this request.
     *
     * @return the requested record packing
     * @see SRURecordPacking
     */
    public SRURecordPacking getRecordPacking() {
        return recordPacking;
    }


    /**
     * Set the <em>recordPacking</em> (SRU 2.0) parameter of this request.
     *
     * @param recordPacking
     *            the requested recordPacking mode
     * @see SRURecordXmlEscaping
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
    public SRUOperation getOperation() {
        return SRUOperation.SEARCH_RETRIEVE;
    }


    @Override
    void addParametersToURI(URIHelper uriHelper, SRUVersion version)
            throws SRUClientException {
        /*
         * append query argument (mandatory)
         *
         * NB: Setting "x-malformed-query" as an extra request parameter makes
         * the client to send invalid requests. This is intended to use for
         * testing endpoints for protocol conformance (i.e. provoke an error)
         * and SHOULD NEVER be used in production!
         */
        final String malformedQuery = getExtraRequestData(X_MALFORMED_QUERY);
        if (malformedQuery == null) {
            if ((query == null) || query.isEmpty()) {
                throw new SRUClientException(
                        "mandatory argument 'query' not set or empty");
            }
            uriHelper.append(PARAM_QUERY, query);
        } else {
            if (!malformedQuery.equalsIgnoreCase(MALFORMED_OMIT)) {
                uriHelper.append(PARAM_QUERY, malformedQuery);
            }
        }

        /*
         * append queryType parameter (SRU 2.0, optional)
         */
        if ((version == SRUVersion.VERSION_2_0) && (queryType != null)) {
            if (!SRUClientConstants.QUERY_TYPE_CQL.equals(queryType)) {
                uriHelper.append(PARAM_QUERY_TYPE, queryType);
            }
        }

        /*
         * append startRecord argument (optional)
         *
         * NB: Setting "x-malformed-startRecord" as an extra request parameter
         * makes the client to send invalid requests. This is intended to use
         * for testing endpoints for protocol conformance (i.e. provoke an
         * error) and SHOULD NEVER be used in production!
         */
        final String malformedStartRecord =
                getExtraRequestData(X_MALFORMED_START_RECORD);
        if (malformedStartRecord == null) {
            if (startRecord > 0) {
                uriHelper.append(PARAM_START_RECORD, startRecord);
            }
        } else {
            if (!malformedStartRecord.equalsIgnoreCase(MALFORMED_OMIT)) {
                uriHelper.append(PARAM_START_RECORD, malformedStartRecord);
            }
        }

        /*
         * append maximumRecords argument (optional)
         *
         * NB: Setting "x-malformed-maximumRecords" as an extra request
         * parameter makes the client to send invalid requests. This is
         * intended to use for testing endpoints for protocol conformance
         * (i.e. provoke an error) and SHOULD NEVER be used in production!
         */
        final String malformedMaxiumRecords =
                getExtraRequestData(X_MALFORMED_MAXIMUM_RECORDS);
        if (malformedMaxiumRecords == null) {
            if (maximumRecords > -1) {
                uriHelper.append(PARAM_MAXIMUM_RECORDS, maximumRecords);
            }
        } else {
            if (!malformedMaxiumRecords.equalsIgnoreCase(MALFORMED_OMIT)) {
                uriHelper.append(PARAM_MAXIMUM_RECORDS,
                        malformedMaxiumRecords);
            }
        }

        /*
         * append record XML escaping argument (optional)
         *
         * NB: Setting "x-malformed-recordPacking" as an extra request
         * parameter makes the client to send invalid requests. This is
         * intended to use for testing endpoints for protocol conformance
         * (i.e. provoke an error) and SHOULD NEVER be used in production!
         */
        final String malformedRecordXmlEscaping =
                getExtraRequestData(X_MALFORMED_RECORD_XML_ESCAPING);
        if (malformedRecordXmlEscaping == null) {
            if (recordXmlEscaping != null) {
                switch (recordXmlEscaping) {
                case XML:
                    if (version == SRUVersion.VERSION_2_0) {
                        uriHelper.append(PARAM_RECORD_XML_ESCAPING,
                                RECORD_XML_ESCAPING_XML);
                    } else {
                        uriHelper.append(PARAM_RECORD_PACKING,
                                RECORD_XML_ESCAPING_XML);
                    }
                    break;
                case STRING:
                    if (version == SRUVersion.VERSION_2_0) {
                        uriHelper.append(PARAM_RECORD_XML_ESCAPING,
                                RECORD_XML_ESCPAING_STRING);
                    } else {
                        uriHelper.append(PARAM_RECORD_PACKING,
                                RECORD_XML_ESCPAING_STRING);
                    }
                    break;
                default:
                    throw new SRUClientException("internal error: invalid " +
                            "recordXmlEscaping (" + recordXmlEscaping + ")");
                } // switch
            }
        } else {
            if (!malformedRecordXmlEscaping.equalsIgnoreCase(MALFORMED_OMIT)) {
                if (version == SRUVersion.VERSION_2_0) {
                    uriHelper.append(PARAM_RECORD_XML_ESCAPING,
                            malformedRecordXmlEscaping);
                } else {
                    uriHelper.append(PARAM_RECORD_PACKING,
                            malformedRecordXmlEscaping);
                }
            }
        }

        /*
         * (SRU 2.0) append recordPacking argument (optional)
         */
        if ((version == SRUVersion.VERSION_2_0) && (recordPacking != null)) {
            switch (recordPacking) {
            case PACKED:
                uriHelper.append(PARAM_RECORD_PACKING, RECORD_PACKING_PACKED);
                break;
            case UNPACKED:
                uriHelper.append(PARAM_RECORD_PACKING, RECORD_PACKING_UNPACKED);
                break;
            default:
                throw new SRUClientException("internal error: invalid value " +
                        "for recordPacking (" + recordPacking + ")");
            }
        }

        /*
         * append recordSchema argument (optional)
         */
        if (recordSchema != null) {
            uriHelper.append(PARAM_RECORD_SCHEMA, recordSchema);
        }

        /*
         * append resultSetTTL argument (optional)
         */
        if (resultSetTTL > -1) {
            uriHelper.append(PARAM_RESULT_SET_TTL, resultSetTTL);
        }
    }

} // class SRUSearchRetrieveRequest

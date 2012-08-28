/**
 * This software is copyright (c) 2011-2012 by
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

import java.net.URI;
import java.util.HashMap;
import java.util.Map;



/**
 * Abstract base class for SRU requests.
 * 
 * @see SRUExplainResponse
 * @see SRUScanResponse
 * @see SRUSearchRetrieveResponse
 */
abstract class SRUAbstractRequest {
    static final String PARAM_OPERATION                = "operation";
    static final String PARAM_VERSION                  = "version";
    static final String PARAM_RECORD_PACKING           = "recordPacking";
    static final String PARAM_STYLESHEET               = "stylesheet";
    static final String PARAM_QUERY                    = "query";
    static final String PARAM_START_RECORD             = "startRecord";
    static final String PARAM_MAXIMUM_RECORDS          = "maximumRecords";
    static final String PARAM_RECORD_SCHEMA            = "recordSchema";
    static final String PARAM_RECORD_X_PATH            = "recordXPath";
    static final String PARAM_RESULT_SET_TTL           = "resultSetTTL";
    static final String PARAM_SORT_KEYS                = "sortKeys";
    static final String PARAM_SCAN_CLAUSE              = "scanClause";
    static final String PARAM_RESPONSE_POSITION        = "responsePosition";
    static final String PARAM_MAXIMUM_TERMS            = "maximumTerms";
    static final String RECORD_PACKING_XML             = "xml";
    static final String RECORD_PACKING_STRING          = "string";
    private static final String OP_EXPLAIN             = "explain";
    private static final String OP_SCAN                = "scan";
    private static final String OP_SEARCH_RETRIEVE     = "searchRetrieve";
    private static final String VERSION_1_1            = "1.1";
    private static final String VERSION_1_2            = "1.2";
    private static final String PARAM_EXTENSION_PREFIX = "x-";
    /** for end-point conformance testing only. never use in production. */
    public static final String X_MALFORMED_OPERATION   =
            "x-malformed-operation";
    /** for end-point conformance testing only. never use in production. */
    public static final String X_MALFORMED_VERSION     =
            "x-malformed-version";
    /** for end-point conformance testing only. never use in production. */
    public static final String MALFORMED_OMIT          = "omit";
    private static final String MALFORMED_KEY_PREFIX   = "x-malformed";


    enum SRUOperation {
        EXPLAIN, SCAN, SEARCH_RETRIEVE
    } // enum SRUOperation


    class URIBuilder {
        private final StringBuilder sb;
        private boolean firstParam = true;

        private URIBuilder(String endpointURI) {
            this.sb = new StringBuilder(endpointURI);
        }


        public URIBuilder append(String name, String value) {
            if (name == null) {
                throw new NullPointerException("name == null");
            }
            if (name.isEmpty()) {
                throw new IllegalArgumentException("name is empty");
            }
            if (value == null) {
                throw new NullPointerException("value == null");
            }
            if (value.isEmpty()) {
                throw new IllegalArgumentException("value is empty");
            }

            if (firstParam) {
                sb.append('?');
                firstParam = false;
            } else {
                sb.append('&');
            }
            sb.append(name).append('=').append(value);
            return this;
        }


        public URIBuilder append(String name, int value) {
            return append(name, Integer.toString(value));
        }


        private URI makeURI() {
            return URI.create(sb.toString());
        }
    } // class
    /** The URL of the endpoint. */
    protected final String endpointURI;
    /** The version to be sued  for this request. */
    protected SRUVersion version;
    /** A map of extra request data parameters. */
    protected Map<String, String> extraRequestData;
    private SRUVersion versionPreformed;


    /**
     * Constructor.
     *
     * @param endpointURI
     *            the URI of the endpoint
     * @throws NullPointerException
     *             if any required argument is null
     */
    protected SRUAbstractRequest(String endpointURI) {
        if (endpointURI == null) {
            throw new NullPointerException("endpointURI == null");
        }
        this.endpointURI = endpointURI;
    }


    /**
     * Get the endpoint URI.
     *
     * @return the endpoint URI
     */
    public String getEndpointURI() {
        return endpointURI;
    }


    /**
     * Set the version for this request.
     *
     * @param version a version of <code>null</code> for client default
     */
    public void setVersion(SRUVersion version) {
        this.version = version;
    }


    /**
     * Get the version for this request.
     *
     * @return version for this request or <code>null</code> of client default
     *         is used
     */
    public SRUVersion getVersion() {
        return version;
    }


    /**
     * Set an extra request parameter for this request.
     *
     * @param name
     *            the name for the extra request data parameter
     * @param value
     *            the value for the extra request data parameter
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @throws IllegalArgumentException
     *             if any argument is invalid
     * @see <a href="http://www.loc.gov/standards/sru/specs/extra-data.html">SRU
     *      Extra Data / Extensions</a>
     */
    public void setExtraRequestData(String name, String value) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is an empty string");
        }
        if (!name.startsWith(PARAM_EXTENSION_PREFIX)) {
            throw new IllegalArgumentException("name must start with '" +
                    PARAM_EXTENSION_PREFIX + "'");
        }
        if (value == null) {
            throw new NullPointerException("value == null");
        }
        if (value.isEmpty()) {
            throw new IllegalArgumentException("value is an empty string");
        }
        if (extraRequestData == null) {
            extraRequestData = new HashMap<String, String>();
        }
        extraRequestData.put(name, value);
    }


    /**
     * Set the value of extra request parameter for this request.
     *
     * @param name
     *            the name for the extra request data parameter
     * @return the value for the extra request data parameter or
     *         <code>null</code> if parameter was not set
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @throws IllegalArgumentException
     *             if any argument is invalid
     * @see <a href="http://www.loc.gov/standards/sru/specs/extra-data.html">SRU
     *      Extra Data / Extensions</a>
     */
    public String getExtraRequestData(String name) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is an empty string");
        }
        if (!name.startsWith(PARAM_EXTENSION_PREFIX)) {
            throw new IllegalArgumentException("name must start with '" +
                    PARAM_EXTENSION_PREFIX + "'");
        }
        if (extraRequestData != null) {
            return extraRequestData.get(name);
        }
        return null;
    }


    final SRUVersion getVersionPerformed() {
        return versionPreformed;
    }


    final URI makeURI(SRUVersion defaultVersion)
            throws SRUClientException {
        if (defaultVersion == null) {
            throw new NullPointerException("defaultVersion == null");
        }
        URIBuilder uriBuilder = new URIBuilder(endpointURI);

        /*
         * append operation parameter
         *
         * NB: Setting "x-malformed-operation" as an extra request parameter
         * makes the client to send invalid requests. This is intended to use
         * for testing endpoints for protocol conformance (i.e. provoke an
         * error) and SHOULD NEVER be used in production!
         */
        final String malformedOperation =
                getExtraRequestData(X_MALFORMED_OPERATION);
        if (malformedOperation == null) {
            switch (getOperation()) {
            case EXPLAIN:
                uriBuilder.append(PARAM_OPERATION, OP_EXPLAIN);
                break;
            case SCAN:
                uriBuilder.append(PARAM_OPERATION, OP_SCAN);
                break;
            case SEARCH_RETRIEVE:
                uriBuilder.append(PARAM_OPERATION, OP_SEARCH_RETRIEVE);
                break;
            default:
                throw new SRUClientException(
                        "unsupported operation: " + getOperation());
            } // switch
        } else {
            if (!malformedOperation.equals(MALFORMED_OMIT)) {
                uriBuilder.append(PARAM_OPERATION, malformedOperation);
            }
        }

        /*
         * append version parameter
         *
         * NB: Setting "x-malformed-version" as an extra request parameter makes
         * the client to send invalid requests. This is intended to use for
         * testing endpoints for protocol conformance (i.e. provoke an error)
         * and SHOULD NEVER be used in production!
         */
        final String malformedVersion =
                getExtraRequestData(X_MALFORMED_VERSION);
        if (malformedVersion == null) {
            versionPreformed = (version != null) ? version : defaultVersion;
            switch (versionPreformed) {
            case VERSION_1_1:
                uriBuilder.append(PARAM_VERSION, VERSION_1_1);
                break;
            case VERSION_1_2:
                uriBuilder.append(PARAM_VERSION, VERSION_1_2);
                break;
            default:
                throw new SRUClientException("unsupported version: " +
                        versionPreformed);
            } // switch
        } else {
            if (!malformedVersion.equalsIgnoreCase(MALFORMED_OMIT)) {
                uriBuilder.append(PARAM_VERSION, malformedVersion);
            }
        }

        // request specific parameters
        addParametersToURI(uriBuilder);

        // extraRequestData
        if ((extraRequestData != null) && !extraRequestData.isEmpty()) {
            for (Map.Entry<String, String> entry :
                extraRequestData.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(MALFORMED_KEY_PREFIX)) {
                    continue;
                }
                uriBuilder.append(key, entry.getValue());
            }
        }
        return uriBuilder.makeURI();
    }


    /**
     * <em>Note: this method is not a part of public API.</em>
     * @return a constant for this
     */
    abstract SRUOperation getOperation();


    abstract void addParametersToURI(URIBuilder uri) throws SRUClientException;

} // class AbstractSRURequest

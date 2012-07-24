package eu.clarin.sru.client;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;


public abstract class SRUAbstractRequest {
    protected static final String PARAM_OPERATION         = "operation";
    protected static final String PARAM_VERSION           = "version";
    protected static final String PARAM_RECORD_PACKING    = "recordPacking";
    protected static final String PARAM_STYLESHEET        = "stylesheet";
    protected static final String PARAM_QUERY             = "query";
    protected static final String PARAM_START_RECORD      = "startRecord";
    protected static final String PARAM_MAXIMUM_RECORDS   = "maximumRecords";
    protected static final String PARAM_RECORD_SCHEMA     = "recordSchema";
    protected static final String PARAM_RECORD_X_PATH     = "recordXPath";
    protected static final String PARAM_RESULT_SET_TTL    = "resultSetTTL";
    protected static final String PARAM_SORT_KEYS         = "sortKeys";
    protected static final String PARAM_SCAN_CLAUSE       = "scanClause";
    protected static final String PARAM_RESPONSE_POSITION = "responsePosition";
    protected static final String PARAM_MAXIMUM_TERMS     = "maximumTerms";
    protected static final String RECORD_PACKING_XML      = "xml";
    protected static final String RECORD_PACKING_STRING   = "string";
    private static final String OP_EXPLAIN                = "explain";
    private static final String OP_SCAN                   = "scan";
    private static final String OP_SEARCH_RETRIEVE        = "searchRetrieve";
    private static final String VERSION_1_1               = "1.1";
    private static final String VERSION_1_2               = "1.2";
    private static final String PARAM_EXTENSION_PREFIX    = "x-";
    /** for end-point conformance testing only. never use in production */
    public static final String X_MALFORMED_VERSION       =
            "x-malformed-version";
    /** for end-point conformance testing only. never use in production */
    public static final String X_MALFORMED_OPERATION     =
            "x-maformed-operation";
    /** for end-point conformance testing only. never use in production */
    public static final String MALFORMED_OMIT            = "omit";

    protected enum SRUOperation {
        EXPLAIN, SCAN, SEARCH_RETRIEVE
    } // enum SRUOperation

    protected class URIBuilder {
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
    protected final String endpointURI;
    protected SRUVersion version;
    protected Map<String, String> extraRequestData;
    private SRUVersion versionPreformed;


    protected SRUAbstractRequest(String endpointURI) {
        if (endpointURI == null) {
            throw new NullPointerException("endpointURI == null");
        }
        this.endpointURI = endpointURI;
    }


    public String getEndpointURI() {
        return endpointURI;
    }


    public void setVersion(SRUVersion version) {
        this.version = version;
    }


    public SRUVersion getVersion() {
        return version;
    }


    public SRUVersion getVersionPerformed() {
        return versionPreformed;
    }


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


    public String getExtraRequestData(String name) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is an empty string");
        }
        if (!name.startsWith("x-")) {
            throw new IllegalArgumentException("name must start with 'x-'");
        }
        if (extraRequestData != null) {
            return extraRequestData.get(name);
        }
        return null;
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
         * NB: Setting "x-operation-version" as an extra request parameter makes
         * the client to send invalid requests. This is intended to use for
         * testing endpoints for protocol conformance (i.e. provoke an error)
         * and SHOULD NEVER be used in production!
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
                if (key.equals(X_MALFORMED_OPERATION) ||
                        key.equals(X_MALFORMED_VERSION)) {
                    continue;
                }
                uriBuilder.append(key, entry.getValue());
            }
        }
        return uriBuilder.makeURI();
    }


    protected abstract SRUOperation getOperation();


    protected abstract void addParametersToURI(URIBuilder uri)
            throws SRUClientException;

} // class AbstractSRURequest

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
    protected enum SRUOperation {
        EXPLAIN, SCAN, SEARCH_RETRIEVE
    }
    protected final String endpointURI;
    protected SRUVersion version;
    protected Map<String, String> extraRequestData;


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
        StringBuilder uri = new StringBuilder(endpointURI);

        // operation
        uri.append('?').append(PARAM_OPERATION).append('=');
        switch (getOperation()) {
        case EXPLAIN:
            uri.append(OP_EXPLAIN);
            break;
        case SCAN:
            uri.append(OP_SCAN);
            break;
        case SEARCH_RETRIEVE:
            uri.append(OP_SEARCH_RETRIEVE);
            break;
        default:
            throw new SRUClientException(
                    "unsupported operation: " + getOperation());
        } // switch

        // version
        SRUVersion v = (version != null) ? version : defaultVersion;
        uri.append('&').append(PARAM_VERSION).append('=');
        switch (v) {
        case VERSION_1_1:
            uri.append(VERSION_1_1);
            break;
        case VERSION_1_2:
            uri.append(VERSION_1_2);
            break;
        default:
            throw new SRUClientException("unsupported version: " + v);
        } // switch

        // request specific parameters
        addParametersToURI(uri);

        // extraRequestData
        if ((extraRequestData != null) && !extraRequestData.isEmpty()) {
            for (Map.Entry<String, String> entry :
                extraRequestData.entrySet()) {
                uri.append('&').append(entry.getKey()).append('=')
                        .append(entry.getValue());
            }
        }
        return URI.create(uri.toString());
    }


    protected abstract SRUOperation getOperation();


    protected abstract void addParametersToURI(StringBuilder uri)
            throws SRUClientException;

} // class AbstractSRURequest

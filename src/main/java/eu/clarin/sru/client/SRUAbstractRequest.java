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
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.utils.URIBuilder;



/**
 * Abstract base class for SRU requests.
 *
 * @see SRUExplainResponse
 * @see SRUScanResponse
 * @see SRUSearchRetrieveResponse
 */
abstract class SRUAbstractRequest {
    /* general / explain related parameter names */
    protected static final String PARAM_OPERATION            = "operation";
    protected static final String PARAM_VERSION              = "version";
    protected static final String PARAM_STYLESHEET           = "stylesheet";
    protected static final String PARAM_RENDER_BY            = "renderedBy";
    protected static final String PARAM_HTTP_ACCEPT          = "httpAccept";
    protected static final String PARAM_RESPONSE_TYPE        = "responseType";
    /* searchRetrieve related parameter names */
    protected static final String PARAM_QUERY                = "query";
    protected static final String PARAM_QUERY_TYPE           = "queryType";
    protected static final String PARAM_START_RECORD         = "startRecord";
    protected static final String PARAM_MAXIMUM_RECORDS      = "maximumRecords";
    protected static final String PARAM_RECORD_XML_ESCAPING  = "recordXMLEscaping";
    protected static final String PARAM_RECORD_PACKING       = "recordPacking";
    protected static final String PARAM_RECORD_SCHEMA        = "recordSchema";
    protected static final String PARAM_RECORD_X_PATH        = "recordXPath";
    protected static final String PARAM_RESULT_SET_TTL       = "resultSetTTL";
    protected static final String PARAM_SORT_KEYS            = "sortKeys";
    /* scan related parameter names */
    protected static final String PARAM_SCAN_CLAUSE          = "scanClause";
    protected static final String PARAM_RESPONSE_POSITION    = "responsePosition";
    protected static final String PARAM_MAXIMUM_TERMS        = "maximumTerms";
    /* operations */
    protected static final String OP_EXPLAIN                 = "explain";
    protected static final String OP_SCAN                    = "scan";
    protected static final String OP_SEARCH_RETRIEVE         = "searchRetrieve";
    protected static final String VERSION_1_1                = "1.1";
    protected static final String VERSION_1_2                = "1.2";
    /* various parameter values */
    protected static final String RECORD_XML_ESCAPING_XML    = "xml";
    protected static final String RECORD_XML_ESCPAING_STRING = "string";
    protected static final String RECORD_PACKING_PACKED      = "packed";
    protected static final String RECORD_PACKING_UNPACKED    = "unpacked";
    protected static final String RENDER_BY_CLIENT           = "client";
    protected static final String RENDER_BY_SERVER           = "server";
    protected static final String PARAM_EXTENSION_PREFIX = "x-";
    /** for end-point conformance testing only. never use in production. */
    public static final String X_MALFORMED_OPERATION   =
            "x-malformed-operation";
    /** for end-point conformance testing only. never use in production. */
    public static final String X_MALFORMED_VERSION     =
            "x-malformed-version";
    /** for end-point conformance testing only. never use in production. */
    public static final String MALFORMED_OMIT          = "omit";
    private static final String MALFORMED_KEY_PREFIX   = "x-malformed";


    protected static class URIHelper {
        private final URIBuilder uriBuilder;


        private URIHelper(URIBuilder builder) {
            this.uriBuilder = builder;
        }


        protected URIHelper append(String name, String value) {
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

            uriBuilder.addParameter(name, value);
            return this;
        }


        protected URIHelper append(String name, int value) {
            return append(name, Integer.toString(value));
        }


        private URI makeURI() throws URISyntaxException {
            return uriBuilder.build();
        }
    } // class URIHelper


    /** The baseURI of the SRU server. */
    protected final URI baseURI;
    /**
     * The request should be processed in strict or non-strict SRU protocol
     * conformance mode. Default value is <code>true</code>.
     */
    private boolean strictMode = true;
    /** The version to be used for this request. */
    protected SRUVersion version;
    /** A map of extra request data parameters. */
    protected Map<String, String> extraRequestData;
    /** Whether a authentication header should be sent. */
    private boolean sendAuthentication = false;
    /** A map of extra authentication context data. */
    protected Map<String, String> authenticationContext;
    /*
     * The version that was used to perform the request.
     * It is set as a side-effect of makeURI().
     */
    private SRUVersion versionRequested;
    /*
     * The URI that was used to perform the request.
     * It is set a a side-effect of makeURI().
     */
    private URI uriRequested;


    /**
     * Constructor.
     *
     * @param baseURI
     *            the baseURI of the SRU server
     * @throws NullPointerException
     *             if any required argument is null
     */
    protected SRUAbstractRequest(URI baseURI) {
        if (baseURI == null) {
            throw new NullPointerException("baseURI == null");
        }
        this.baseURI = baseURI;
    }


    /**
     * Constructor.
     *
     * @param baseURI
     *            the baseURI of the SRU server
     * @throws NullPointerException
     *             if any required argument is null
     * @throw IllegalArgumentException if the URI is invalid
     */
    protected SRUAbstractRequest(String baseURI) {
        if (baseURI == null) {
            throw new NullPointerException("baseURI == null");
        }
        if (baseURI.isEmpty()) {
            throw new IllegalArgumentException("baseURI is empty");
        }
        try {
            this.baseURI = new URI(baseURI);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("invalid URI", e);
        }
    }


    /**
     * Get the baseURI of the SRU server.
     *
     * @return the baseURI of the SRU server
     */
    public URI getBaseURI() {
        return baseURI;
    }


    /**
     * Get the SRU protocol conformance mode for this request
     *
     * @return <code>true</code> if the request will be performed in strict
     *         mode, <code>false</code> if the request will be performed in a
     *         more tolerant mode
     */
    public boolean isStrictMode() {
        return strictMode;
    }


    /**
     * Set the SRU protocol conformance mode for this request
     *
     * @param strictMode
     *            <code>true</code> if the request should be performed in strict
     *            mode, <code>false</code> if the request should be performed
     *            client should in a more tolerant mode
     */
    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
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
     * @return version for this request or <code>null</code> if client default
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
            extraRequestData = new HashMap<>();
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


    /**
     * Get the URI that was used to perform the request. This method may only be
     * called <em>after</em> the request was carried out, otherwise it will
     * throw an {@link IllegalStateException}.
     *
     * @return the URI that was used to carry out this request
     * @throws IllegalStateException
     *             if the request was not yet carried out
     */
    public final URI getRequestedURI() {
        if (uriRequested == null) {
            throw new IllegalStateException(
                    "The request was not yet carried out");
        }
        return uriRequested;
    }


    /**
     * Get the version that was used to carry out this request. This method may
     * only be called <em>after</em> the request was carried out, otherwise it
     * will throw an {@link IllegalStateException}.
     *
     * @return the version that was used to carry out this request
     * @throws IllegalStateException
     *             if the request was not yet carried out
     */
    public final SRUVersion getRequestedVersion() {
        if (versionRequested == null) {
            throw new IllegalStateException(
                    "The request was not yet carried out");
        }
        return versionRequested;
    }


    /**
     * Get the authentication mode for this request
     *
     * @return <code>true</code> if the request will try to send authentication
     *         data (header), <code>false</code> if the request will not send
     *         send authentication information
     */
    public boolean isSendAuthentication() {
        return sendAuthentication;
    }


    /**
     * Set whether authentication information should be sent for this request
     *
     * @param sendAuthentication
     *            <code>true</code> if the request should try to send
     *            authentication information, <code>false</code> if sending
     *            should be suppressed
     */
    public void setSendAuthentication(boolean sendAuthentication) {
        this.sendAuthentication = sendAuthentication;
    }


    /**
     * Returns a nullable read-only map of authentication context data.
     * 
     * @return the authentication context map or <code>null</code> if no
     *             entries were created
     */
    public Map<String, String> getAuthenticationContext() {
        if (authenticationContext != null) {
            return Collections.unmodifiableMap(authenticationContext);
        }
        return null;
    }


    /**
     * Set an authentication context entry for this request.
     *
     * @param name
     *            the name for the context entry parameter
     * @param value
     *            the value for the context entry parameter
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @throws IllegalArgumentException
     *             if any argument is invalid
     */
    public void setAuthenticationContext(String name, String value) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is an empty string");
        }
        // if (value == null) {
        //     throw new NullPointerException("value == null");
        // }
        // if (value.isEmpty()) {
        //     throw new IllegalArgumentException("value is an empty string");
        // }
        if (authenticationContext == null) {
            authenticationContext = new HashMap<>();
        }
        authenticationContext.put(name, value);
    }


    /*
     * This is not public API.
     */
    protected final URI makeURI(SRUVersion defaultVersion)
            throws SRUClientException {
        if (defaultVersion == null) {
            throw new NullPointerException("defaultVersion == null");
        }

        try {
            final URIHelper uriBuilder =
                    new URIHelper(new URIBuilder(baseURI));

            /* store the version, we use for this request */
            versionRequested = (version != null) ? version : defaultVersion;

            switch (versionRequested) {
            case VERSION_1_1:
                /* $FALL-THROUGH$ */
            case VERSION_1_2:
                /*
                 * append operation parameter
                 *
                 * NB: Setting "x-malformed-operation" as an extra request parameter
                 * makes the client send invalid requests. This is intended to
                 * use for testing SRU servers for protocol conformance (i.e.
                 * provoke an error) and SHOULD NEVER be used in production!
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
                    } // switch
                } else {
                    if (!malformedOperation.equals(MALFORMED_OMIT)) {
                        uriBuilder.append(PARAM_OPERATION, malformedOperation);
                    }
                }

                /*
                 * append version parameter
                 *
                 * NB: Setting "x-malformed-version" as an extra request parameter
                 * makes the client send invalid requests. This is intended to
                 * use for testing SRU servers for protocol conformance (i.e.
                 * provoke an error) and SHOULD NEVER be used in production!
                 */
                final String malformedVersion =
                        getExtraRequestData(X_MALFORMED_VERSION);
                if (malformedVersion == null) {
                    versionRequested = (version != null) ? version : defaultVersion;
                    switch (versionRequested) {
                    case VERSION_1_1:
                        uriBuilder.append(PARAM_VERSION, VERSION_1_1);
                        break;
                    case VERSION_1_2:
                        uriBuilder.append(PARAM_VERSION, VERSION_1_2);
                        break;
                    default:
                        throw new SRUClientException("internal error: " +
                                "unsupported value for version (" +
                                versionRequested + ")");
                    } // switch
                } else {
                    if (!malformedVersion.equalsIgnoreCase(MALFORMED_OMIT)) {
                        uriBuilder.append(PARAM_VERSION, malformedVersion);
                    }
                }
                break;
            case VERSION_2_0:
                if (getExtraRequestData(X_MALFORMED_OPERATION) != null) {
                    throw new SRUClientException("parameter '" +
                            X_MALFORMED_OPERATION +
                            "' is not supported when using version 2.0");
                }
                if (getExtraRequestData(X_MALFORMED_VERSION) != null) {
                    throw new SRUClientException("parameter '" +
                            X_MALFORMED_VERSION +
                            "' is not supported when using version 2.0");
                }
                break;
            default:
                throw new SRUClientException("internal error: " +
                        "unsupported value for version (" +
                        versionRequested + ")");
            }

            // request specific parameters
            addParametersToURI(uriBuilder, versionRequested);

            // extraRequestData
            if ((extraRequestData != null) && !extraRequestData.isEmpty()) {
                for (Map.Entry<String, String> entry :
                    extraRequestData.entrySet()) {
                    final String key = entry.getKey();

                    /*
                     * make sure, we skip the client-internal parameters
                     * used to generate invalid requests ...
                     */
                    if (!key.startsWith(MALFORMED_KEY_PREFIX)) {
                        uriBuilder.append(key, entry.getValue());
                    }
                }
            }

            final URI uri = uriBuilder.makeURI();
            uriRequested = uri;
            return uri;
        } catch (URISyntaxException e) {
            throw new SRUClientException("error while building request URI", e);
        }
    }


    /**
     * <em>Note: this method is not a part of public API.</em>
     * @return a operation constant for this request
     */
    public abstract SRUOperation getOperation();


    abstract void addParametersToURI(URIHelper uriBuilder, SRUVersion version)
            throws SRUClientException;

} // class SRUAbstractRequest

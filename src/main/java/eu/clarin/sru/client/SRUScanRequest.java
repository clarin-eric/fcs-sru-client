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
 * An object for performing a <em>explain</em> operation.
 * <p>The following argument arguments are mandatory:</p>
 * <ul>
 *   <li><em>scanClause</em></li>
 * </ul>
 *
 * @see SRUScanHandler
 * @see <a href="http://www.loc.gov/standards/sru/specs/scan.html">SRU Scan
 *      Operation</a>
 */
public class SRUScanRequest extends SRUAbstractRequest {
    /** for end-point conformance testing only. never use in production. */
    public static final String X_MALFORMED_SCAN_CLAUSE =
            "x-malformed-scanClause";
    /** for end-point conformance testing only. never use in production. */
    public static final String X_MALFORMED_RESPONSE_POSITION =
            "x-malformed-responsePosition";
    /** for end-point conformance testing only. never use in production. */
    public static final String X_MALFORMED_MAXIMUM_TERMS =
            "x-malformed-maximumTerms";
    private String scanClause;
    private int responsePosition = -1;
    private int maximumTerms = -1;


    /**
     * Constructor.
     *
     * @param baseURI
     *            the baseURI of the endpoint
     */
    public SRUScanRequest(URI baseURI) {
        super(baseURI);
    }


    /**
     * Constructor.
     *
     * @param baseURI
     *            the baseURI of the endpoint
     */
    public SRUScanRequest(String baseURI) {
        super(baseURI);
    }


    /**
     * Get the value of the <em>scanClause</em> argument for this request.
     *
     * @return the value for the <em>scanClause</em> argument or
     *         <code>null</code> of none was set
     */
    public String getScanClause() {
        return scanClause;
    }


    /**
     * Set the value of the <em>scanClause</em> argument for this request.
     *
     * @param scanClause
     *            the value for the <em>scanClause</em> argument
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @throws IllegalArgumentException
     *             if any argument is invalid
     */
    public void setScanClause(String scanClause) {
        if (scanClause == null) {
            throw new NullPointerException("scanClause == null");
        }
        if (scanClause.isEmpty()) {
            throw new IllegalArgumentException("scanClause is an empty string");
        }
        this.scanClause = scanClause;
    }


    /**
     * Get the value of the <em>responsePosition</em> argument for this request.
     *
     * @return the value for the <em>responsePosition</em> argument
     */
    public int getResponsePosition() {
        return responsePosition;
    }


    /**
     * Set the value of the <em>responsePosition</em> argument for this request.
     *
     * @param responsePosition
     *            the value for the <em>responsePosition</em> argument
     * @throws IllegalArgumentException
     *             if any argument is invalid
     */
    public void setResponsePosition(int responsePosition) {
        if (responsePosition < 0) {
            throw new IllegalArgumentException("responsePosition < 0");
        }
        this.responsePosition = responsePosition;
    }


    /**
     * Get the value of the <em>maximumTerms</em> argument for this request.
     *
     * @return the value for the <em>maximumTerms</em> argument
     */
    public int getMaximumTerms() {
        return maximumTerms;
    }


    /**
     * Set the value of the <em>maximumTerms</em> argument for this request.
     *
     * @param maximumTerms
     *            the value for the <em>maximumTerms</em> argument
     * @throws IllegalArgumentException
     *             if any argument is invalid
     */
    public void setMaximumTerms(int maximumTerms) {
        if (maximumTerms < 0) {
            throw new IllegalArgumentException("maximumTerms < 0");
        }
        this.maximumTerms = maximumTerms;
    }


    @Override
    public SRUOperation getOperation() {
        return SRUOperation.SCAN;
    }


    @Override
    void addParametersToURI(URIHelper uriHelper, SRUVersion version)
            throws SRUClientException {
        // scanClause
        final String malformedScan =
                getExtraRequestData(X_MALFORMED_SCAN_CLAUSE);
        if (malformedScan == null) {
            if ((scanClause == null) || scanClause.isEmpty()) {
                throw new SRUClientException(
                        "mandatory argument 'scanClause' not set or empty");
            }
            uriHelper.append(PARAM_SCAN_CLAUSE, scanClause);
        } else {
            if (!malformedScan.equalsIgnoreCase(MALFORMED_OMIT)) {
                uriHelper.append(PARAM_VERSION, malformedScan);
            }
        }

        // responsePosition
        final String malformedResponsePosition =
                getExtraRequestData(X_MALFORMED_RESPONSE_POSITION);
        if (malformedResponsePosition == null) {
            if (responsePosition > -1) {
                uriHelper.append(PARAM_RESPONSE_POSITION, responsePosition);
            }
        } else {
            if (!malformedResponsePosition.equalsIgnoreCase(MALFORMED_OMIT)) {
                uriHelper.append(PARAM_RESPONSE_POSITION,
                        malformedResponsePosition);
            }
        }

        // maximumTerms
        final String malformedMaximumTerms =
                getExtraRequestData(X_MALFORMED_MAXIMUM_TERMS);
        if (malformedMaximumTerms == null) {
            if (maximumTerms > -1) {
                uriHelper.append(PARAM_MAXIMUM_TERMS, maximumTerms);
            }
        } else {
            if (!malformedMaximumTerms.equalsIgnoreCase(MALFORMED_OMIT)) {
                uriHelper.append(PARAM_MAXIMUM_TERMS, malformedMaximumTerms);
            }
        }
    }

} // class SRUScanRequest

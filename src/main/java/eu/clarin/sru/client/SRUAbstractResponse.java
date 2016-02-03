/**
 * This software is copyright (c) 2012-2016 by
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

import java.util.Collections;
import java.util.List;



/**
 * Abstract base class for SRU responses.
 *
 * @see SRUExplainResponse
 * @see SRUScanResponse
 * @see SRUSearchRetrieveResponse
 */
class SRUAbstractResponse<T extends SRUAbstractRequest> {
    private final T request;
    private final List<SRUDiagnostic> diagnostics;
    private final List<SRUExtraResponseData> extraResponseData;
    private final int totalBytesTransferred;
    private final long timeTotal;
    private final long timeQueued;
    private final long timeNetwork;
    private final long timeProcessing;


    /**
     * Constructor.
     *
     * @param diagnostics
     *            a list of diagnostics associated to this result or
     *            <code>null</code> if none.
     * @param extraResponseData
     *            extra response data for this result or <code>null</code> if
     *            none.
     * @param totalBytesTransferred
     *            the total number of bytes transferred for this request
     * @param timeTotal
     *            the total number of milliseconds elapsed while performing this
     *            request
     * @param timeQueued
     *            the total number of milliseconds elapsed while this request
     *            was queued
     * @param timeNetwork
     *            the total number of milliseconds elapsed while this request
     *            waited for network operations to finish
     * @param timeProcessing
     *            the total number of milliseconds elapsed while the client
     *            processed the response from the endpoint
     */
    protected SRUAbstractResponse(T request,
            List<SRUDiagnostic> diagnostics,
            List<SRUExtraResponseData> extraResponseData,
            int totalBytesTransferred,
            long timeTotal,
            long timeQueued,
            long timeNetwork,
            long timeProcessing) {
        this.request = request;
        this.diagnostics = ((diagnostics != null) && !diagnostics.isEmpty())
                ? Collections.unmodifiableList(diagnostics)
                : null;
        this.extraResponseData = ((extraResponseData != null) &&
                                                !extraResponseData.isEmpty())
                ? Collections.unmodifiableList(extraResponseData)
                : null;
        this.totalBytesTransferred = totalBytesTransferred;
        this.timeTotal             = timeTotal;
        this.timeQueued            = timeQueued;
        this.timeNetwork           = timeNetwork;
        this.timeProcessing        = timeProcessing;
    }


    /**
     * Get the request that produced this response.
     *
     * @return the request
     */
    public T getRequest() {
        return request;
    }


    /**
     * Get the diagnostics for this response.
     *
     * @return diagnostics for this response or <code>null</code> if none
     */
    public List<SRUDiagnostic> getDiagnostics() {
        return diagnostics;
    }


    /**
     * Check, if the response contains any diagnostics.
     *
     * <p>
     * NB: Surrogate diagnostics are not covered by this.
     * </p>
     *
     * @return <code>true</code> if response contains any diagnostic,
     *         <code>false</code> otherwise
     */
    public boolean hasDiagnostics() {
        return diagnostics != null;
    }


    /**
     * Get the extra response data for this result.
     *
     * @return get a list of {@link SRUExtraResponseData} instances for the
     *         extra response data from the SRU response or <code>null</code> if
     *         none are available
     */
    public List<SRUExtraResponseData> getExtraResponseData() {
        return extraResponseData;
    }


    /**
     * Check, if this response has any extra response data attached to it.
     *
     * @return <code>true</code> if extra response is attached,
     *         <code>false</code> otherwise
     */
    public boolean hasExtraResponseData() {
        return extraResponseData != null;
    }


    /**
     * Get the total number of bytes transferred for this request.
     *
     * @return the total number of bytes or <code>-1</code> if not available
     */
    public long getTotalBytesTransferred() {
        return totalBytesTransferred;
    }


    /**
     * Get the total number of milliseconds elapsed for this request.
     *
     * @return the total number of milliseconds or <code>-1</code> if not
     *         available
     */
    public long getTimeTotal() {
        return timeTotal;
    }


    /**
     * Get the number of milliseconds this request has been queued before it was
     * processed by the client.
     *
     * @return the number of milliseconds queued or <code>-1</code> if not
     *         available
     */
    public long getTimeWait() {
        return timeQueued;
    }


    /**
     * Get the number of milliseconds this request spend waiting for network
     * operations to finish.
     *
     * @return the number of milliseconds spend in waiting on network or
     *         <code>-1</code> if not available
     */
    public long getTimeNetwork() {
        return timeNetwork;
    }


    /**
     * Get the number of milliseconds the client was busy processing the results
     * sent from the endpoint.
     *
     * @return the number of milliseconds spend in processing or <code>-1</code>
     *         if not available
     */
    public long getTimeProcessing() {
        return timeProcessing;
    }

} // class SRUAbstractResponse

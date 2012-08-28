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

import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;



/**
 * Abstract base class for SRU responses.
 *
 * @see SRUExplainResponse
 * @see SRUScanResponse
 * @see SRUSearchRetrieveResponse
 */
class SRUAbstractResponse<T> {
    private final T request;
    private final List<SRUDiagnostic> diagnostics;
    private final Document extraResponseData;


    /**
     * Constructor.
     *
     * @param diagnostics
     *            a list of diagnostics associated to this result or
     *            <code>null</code> if none.
     * @param extraResponseData
     *            extra response data for this result or <code>null</code> if
     *            none.
     */
    protected SRUAbstractResponse(T request, List<SRUDiagnostic> diagnostics,
            Document extraResponseData) {
        this.request = request;
        this.diagnostics = ((diagnostics != null) && !diagnostics.isEmpty())
                ? Collections.unmodifiableList(diagnostics) : null;
        this.extraResponseData = extraResponseData;
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
     * @return a {@link Document} node for the extra response data or
     *         <code>null</code> if none
     */
    public Document getExtraResponseData() {
        return extraResponseData;
    }

} // class SRUAbstractResponse

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

import java.util.Collections;
import java.util.List;

import org.w3c.dom.DocumentFragment;



/**
 * A response to a <em>scan</em> request.
 */
public final class SRUScanResponse extends SRUAbstractResponse<SRUScanRequest> {
    private final List<SRUTerm> terms;


    SRUScanResponse(SRUScanRequest request,
            List<SRUDiagnostic> diagnostics,
            DocumentFragment extraResponseData,
            int totalBytesTransferred,
            long timeTotal,
            long timeQueued,
            long timeNetwork,
            long timeProcessing,
            List<SRUTerm> terms) {
        super(request, diagnostics, extraResponseData, totalBytesTransferred,
                timeTotal, timeQueued, timeNetwork, timeProcessing);
        this.terms = ((terms != null) && !terms.isEmpty())
                ? Collections.unmodifiableList(terms)
                : null;
    }


    /**
     * Get list of terms matched by the request.
     *
     * @return a list of terms or <code>null</code> if no terms matched the
     *         request.
     */
    public List<SRUTerm> getTerms() {
        return terms;
    }


    /**
     * Check, if response contains any terms.
     *
     * @return <code>true</code> of response contains terms, <code>false</code>
     *         otherwise
     */
    public boolean hasTerms() {
        return terms != null;
    }

} // class SRUScanResponse

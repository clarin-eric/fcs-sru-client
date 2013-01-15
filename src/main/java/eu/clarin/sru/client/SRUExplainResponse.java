/**
 * This software is copyright (c) 2011-2013 by
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

import java.util.List;

import org.w3c.dom.DocumentFragment;



/**
 * A response to a <em>explain</em> request.
 */
public final class SRUExplainResponse extends
        SRUAbstractResponse<SRUExplainRequest> {
    private final SRURecord record;


    SRUExplainResponse(SRUExplainRequest request,
            List<SRUDiagnostic> diagnostics,
            DocumentFragment extraResponseData,
            int totalBytesTransferred,
            long timeTotal,
            long timeQueued,
            long timeNetwork,
            long timeProcessing,
            SRURecord record) {
        super(request, diagnostics, extraResponseData, totalBytesTransferred,
                timeTotal, timeQueued, timeNetwork, timeProcessing);
        this.record = record;
    }


    /**
     * Get the explain record.
     *
     * @return the explain record or <code>null</code>
     */
    public SRURecord getRecord() {
        return record;
    }


    /**
     * Check, if response contains a record.
     *
     * @return <code>true</code> of response contains a record,
     *         <code>false</code> otherwise
     */
    public boolean hasRecord() {
        return record != null;
    }

} // class SRUExplainResponse

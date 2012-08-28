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

import org.w3c.dom.Document;



/**
 * A response to a <em>searchRetrieve</em> request.
 */
public final class SRUSearchRetrieveResponse extends
        SRUAbstractResponse<SRUSearchRetrieveRequest> {
    private final int numberOfRecords;
    private final String resultSetId;
    private final int resultSetIdleTime;
    private final List<SRURecord> records;
    private final int nextRecordPosition;


    SRUSearchRetrieveResponse(SRUSearchRetrieveRequest request,
            List<SRUDiagnostic> diagnostics, Document extraResponseData,
            int numberOfRecords, String resultSetId, int resultSetIdleTime,
            List<SRURecord> records, int nextRecordPosition) {
        super(request, diagnostics, extraResponseData);
        this.numberOfRecords = numberOfRecords;
        this.resultSetId = resultSetId;
        this.resultSetIdleTime = resultSetIdleTime;
        this.records = (records != null && !records.isEmpty())
                ? Collections.unmodifiableList(records) : null;
        this.nextRecordPosition = nextRecordPosition;
    }


    /**
     * Get the number of records.
     *
     * @return the number of records or <code>-1</code> if not available
     */
    public int getNumberOfRecords() {
        return numberOfRecords;
    }


    /**
     * Get the result set id.
     *
     * @return the result set id or <code>-1</code> if not available
     */
    public String getResultSetId() {
        return resultSetId;
    }


    /**
     * Get the result set idle time.
     *
     * @return the result set idle time or <code>-1</code> if not available
     */
    public int getResultSetIdleTime() {
        return resultSetIdleTime;
    }


    /**
     * Get the list of records.
     *
     * @return the list of records or <code>null</code> if none
     */
    public List<SRURecord> getRecords() {
        return records;
    }


    /**
     * Check, if response contains any records.
     *
     * @return <code>true</code> of response contains records,
     *         <code>false</code> otherwise
     */
    public boolean hasRecords() {
        return records != null;
    }


    /**
     * Get the next record position.
     *
     * @return the next record position or <code>-1</code> if not available
     */
    public int getNextRecordPosition() {
        return nextRecordPosition;
    }

} // class SRUSearchRetrieveResponse

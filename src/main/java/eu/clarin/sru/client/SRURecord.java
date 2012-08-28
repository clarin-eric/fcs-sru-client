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

import org.w3c.dom.Document;


/**
 * Class for holding a single record from a result set.
 * 
 * @see SRUSearchRetrieveResponse
 */
public final class SRURecord {
    private final SRURecordData recordData;
    private final String recordIdentifier;
    private final int recordPosition;
    private final Document extraRecordData;


    SRURecord(SRURecordData recordData, String recordIdentifier,
            int recordPosition, Document extraRecordData) {
        if (recordData == null) {
            throw new NullPointerException("recordData == null");
        }
        this.recordData = recordData;
        this.recordIdentifier = recordIdentifier;
        this.recordPosition = recordPosition;
        this.extraRecordData = extraRecordData;
    }


    /**
     * The record schema for this record.
     *
     * @return the record schema for this record
     */
    public String getRecordSchema() {
        return recordData.getRecordSchema();
    }


    /**
     * Check if this record is in a certain record schema.
     * 
     * @param recordSchema
     *            the record schema to test against
     * @return <code>true</code> if the record is in the supplied record schema,
     *         <code>false</code> otherwise
     * @throws NullPointerException
     *             if any required arguments are not supplied
     */
    public boolean isRecordSchema(String recordSchema) {
        if (recordSchema == null) {
            throw new NullPointerException("recordSchema == null");
        }
        return recordData.getRecordSchema().equals(recordSchema);
    }


    /**
     * Get the record.
     * 
     * @return the record
     */
    public SRURecordData getRecordData() {
        return recordData;
    }


    /**
     * Get the record identifier (only SRU version 1.2).
     *
     * @return the record identifier or <code>null</code> if not available
     */
    public String getRecordIdentifier() {
        return recordIdentifier;
    }


    /**
     * Get the record position in the result set.
     *
     * @return position of the record in the result set or <code>-1</code> if
     *         not available
     */
    public int getRecordPosition() {
        return recordPosition;
    }


    /**
     * Get extra record data attached to this record.
     *
     * @return get the extra record data or <code>null</code> if not available
     */
    public Document getExtraRecordData() {
        return extraRecordData;
    }

} // class SRURecord

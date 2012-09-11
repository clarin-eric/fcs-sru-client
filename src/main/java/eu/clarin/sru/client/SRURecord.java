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

import org.w3c.dom.DocumentFragment;


/**
 * Class for holding a single record from a result set.
 *
 * @see SRUSearchRetrieveResponse
 */
public final class SRURecord {
    private final SRURecordData recordData;
    private final String recordIdentifier;
    private final int recordPosition;
    private DocumentFragment extraRecordData = null;


    SRURecord(SRURecordData recordData, String recordIdentifier,
            int recordPosition) {
        if (recordData == null) {
            throw new NullPointerException("recordData == null");
        }
        this.recordData = recordData;
        this.recordIdentifier = recordIdentifier;
        this.recordPosition = recordPosition;
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
     * @return get an instance of {@link DocumentFragment} containing the XML
     *         fragment for the extra record data from the SRU response or
     *         <code>null</code> if none are available
     */
    public DocumentFragment getExtraRecordData() {
        return extraRecordData;
    }


    /**
     * Check, if this record has extra record data attached to it.
     *
     * @return <code>true</code> if extra record data is attached,
     *         <code>false</code> otherwise
     */
    public boolean hasExtraRecordData() {
        return extraRecordData != null;
    }


    void setExtraRecordData(DocumentFragment extraRecordData) {
        this.extraRecordData = extraRecordData;
    }

} // class SRURecord

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

import org.w3c.dom.DocumentFragment;


/**
 * Class to hold a single entry from a scan response.
 *
 * @see SRUScanResponse
 */
public final class SRUTerm {
    private final String value;
    private final int numberOfRecords;
    private final String displayTerm;
    private final SRUWhereInList whereInList;
    private DocumentFragment extraTermData = null;


    /**
     * Constructor.
     *
     * @param value
     *            value of the term
     * @param numberOfRecords
     *            number of record or <code>-1</code>
     * @param displayTerm
     *            a display string or <code>null</code>
     * @param whereInList
     *            flag or <code>null</code>
     */
    SRUTerm(String value, int numberOfRecords, String displayTerm,
            SRUWhereInList whereInList) {
        if (value == null) {
            throw new NullPointerException("value == null");
        }
        this.value = value;
        this.numberOfRecords = numberOfRecords;
        this.displayTerm = displayTerm;
        this.whereInList = whereInList;
    }


    /**
     * Get the term as it appeared in the index.
     *
     * @return the value of the term
     */
    public String getValue() {
        return value;
    }


    /**
     * Get the number of records which would me matched by the term.
     *
     * @return the number of record or <code>-1</code> if unknown
     */
    public int getNumberOfRecords() {
        return numberOfRecords;
    }


    /**
     * Get A string to display to the end user in place of the term itself.
     *
     * @return a display string or <code>null</code> if unknown
     */
    public String getDisplayTerm() {
        return displayTerm;
    }


    /**
     * Get the flag to indicate the position of the term within the complete
     * term list.
     *
     * @return the flag or <code>null</code> if unknown
     */
    public SRUWhereInList getWhereInList() {
        return whereInList;
    }


    /**
     * Get extra term data for this term.
     *
     * @return get an instance of {@link DocumentFragment} containing the XML
     *         fragment for the extra term data from the SRU response or
     *         <code>null</code> if none are available
     */
    public DocumentFragment getExtraTermData() {
        return extraTermData;
    }


    /**
     * Check, if this term has extra term data attached to it.
     *
     * @return <code>true</code> if extra term data is attached,
     *         <code>false</code> otherwise
     */
    public boolean hasExtraTermData() {
        return extraTermData != null;
    }

    void setExtraTermData(DocumentFragment extraTermData) {
        this.extraTermData = extraTermData;
    }

} // class SRUTerm

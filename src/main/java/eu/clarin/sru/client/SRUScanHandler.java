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

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


/**
 * Receive notifications to the response of a <em>scan</em> request.
 * 
 * @see SRUScanRequest
 * @see <a href="http://www.loc.gov/standards/sru/specs/scan.html">SRU
 *      Scan Operation</a>
 */
public interface SRUScanHandler {
    /**
     * A flag to indicate the position of the term within the complete term
     * list.
     */
    public enum WhereInList {
        /**
         * The first term (<em>first</em>)
         */
        FIRST,

        /**
         * The last term (<em>last</em>)
         */
        LAST,

        /**
         * The only term (<em>only</em>)
         */
        ONLY,

        /**
         * Any other term (<em>inner</em>)
         */
        INNER;
    }


    /**
     * Receive notification of diagnostics.
     * 
     * @param diagnostics
     *            a list of {@link SRUDiagnostic}
     * @throws SRUClientException
     *             any SRU exception, possibly wrapping another exception
     * @see SRUDiagnostic
     */
    public void onDiagnostics(List<SRUDiagnostic> diagnostics)
            throws SRUClientException;


    /**
     * Receive notification of request statistics.
     * 
     * @param bytes
     *            the size of the response in bytes
     * @param millisTotal
     *            the total time spend processing the request
     * @param millisNetwork
     *            the time spend performing network operations
     * @param millisParsing
     *            the time spend parsing the response
     */
    public void onRequestStatistics(int bytes, long millisTotal,
            long millisNetwork, long millisParsing);


    /**
     * Receive notification of extra response data.
     * 
     * @param reader
     *            a {@link XMLStreamReader} to parse the extra response data
     * @throws XMLStreamException
     *             an error occurred while parsing the response
     * @throws SRUClientException
     *             any SRU exception, possibly wrapping another exception
     * @see <a href="http://www.loc.gov/standards/sru/specs/extra-data.html">SRU
     *      Extra Data / Extensions</a>
     */
    public void onExtraResponseData(XMLStreamReader reader)
            throws XMLStreamException, SRUClientException;


    /**
     * Receive notifications of the start of the enumeration of terms in the
     * response.
     * 
     * @throws SRUClientException
     *             any SRU exception, possibly wrapping another exception
     */
    public void onStartTerms() throws SRUClientException;


    /**
     * Receive notifications of the end of the enumeration of terms in the
     * response.
     * 
     * @throws SRUClientException
     *             any SRU exception, possibly wrapping another exception
     */
    public void onFinishTerms() throws SRUClientException;


    /**
     * Receive notification of a term.
     * 
     * @param value
     *            the term (exactly) as it appears in the index
     * @param numberOfRecords
     *            the number of records for the current term which would be
     *            matched
     * @param displayTerm
     *            a string for the current term to display to the end user in
     *            place of the term itself or <code>null</code> if not available
     * @param whereInList
     *            a flag to indicate the position of the term within the
     *            complete term list or <code>null</code> of not available
     * @throws SRUClientException
     *             any SRU exception, possibly wrapping another exception
     */
    public void onTerm(String value, int numberOfRecords, String displayTerm,
            WhereInList whereInList) throws SRUClientException;


    /**
     * Receive notification of extra term data.
     * 
     * @param value
     *            the term (exactly) as it appears in the index
     * @param reader
     *            a {@link XMLStreamReader} to parse the extra term data
     * @throws XMLStreamException
     *             an error occurred while parsing the response
     * @throws SRUClientException
     *             any SRU exception, possibly wrapping another exception
     */
    public void onExtraTermData(String value, XMLStreamReader reader)
            throws XMLStreamException, SRUClientException;

} // interface SRUScanHandler

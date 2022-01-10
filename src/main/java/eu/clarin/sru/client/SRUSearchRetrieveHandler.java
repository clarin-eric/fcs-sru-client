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

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


/**
 * Receive notifications to the response of a <em>searchRetrieve</em> request.
 *
 * @see SRUSearchRetrieveRequest
 * @see <a href="http://www.loc.gov/standards/sru/specs/search-retrieve.html">
 *      SRU SearchRetrieve Operation</a>
 */
public interface SRUSearchRetrieveHandler {

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
     * @param totalBytesTransferred
     *            the total number of bytes transferred while receiving the
     *            response
     * @param millisTotal
     *            the total time spend processing the request
     * @param millisNetwork
     *            the time spend performing network operations
     * @param millisProcessing
     *            the time spend processing the response
     */
    public void onRequestStatistics(int totalBytesTransferred,
            long millisTotal, long millisNetwork, long millisProcessing);


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
     * Receive notifications of the start of the enumeration of records in the
     * response.
     *
     * @param numberOfRecords
     *            the number of records or <code>-1</code> if not available
     * @param resultSetId
     *            the result set id or <code>null</code> if not available
     * @param resultSetIdleTime
     *            the result set idle time or <code>-1</code> if not available
     * @throws SRUClientException
     *             any SRU exception, possibly wrapping another exception
     */
    public void onStartRecords(int numberOfRecords, String resultSetId,
            int resultSetIdleTime) throws SRUClientException;


    /**
     * Receive notifications of the end of the enumeration of records in the
     * response.
     *
     * @param nextRecordPosition
     *            the next record position or <code>-1</code> if not available
     * @throws SRUClientException
     *             any SRU exception, possibly wrapping another exception
     */
    public void onFinishRecords(int nextRecordPosition)
            throws SRUClientException;


    /**
     * Receive notification of a record in the result set.
     *
     * @param identifier
     *            identifier of the record or <code>null</code> if not available
     * @param position
     *            position of the record in the result set or <code>-1</code> if
     *            not available
     * @param data
     *            the parsed record data
     * @throws SRUClientException
     *             any SRU exception, possibly wrapping another exception
     * @see SRURecordData
     * @see SRURecordDataParser
     */
    public void onRecord(String identifier, int position, SRURecordData data)
            throws SRUClientException;


    /**
     * Receive notification of a surrogate record in the result set.
     *
     * @param identifier
     *            identifier of the record or <code>null</code> if not available
     * @param position
     *            position of the record in the result set or <code>-1</code> if
     *            not available
     * @param data
     *            the surrogate record data, i.e. a diagnostic
     * @throws SRUClientException
     *             any SRU exception, possibly wrapping another exception
     * @see SRUDiagnostic
     */
    public void onSurrogateRecord(String identifier, int position,
            SRUDiagnostic data) throws SRUClientException;


    /**
     * Receive notification of extra record data.
     *
     * @param identifier
     *            identifier of the record or <code>null</code> if not available
     * @param position
     *            position of the record in the result set or <code>-1</code> if
     *            not available
     * @param reader
     *            a {@link XMLStreamReader} to parse the extra term data
     * @throws XMLStreamException
     *             an error occurred while parsing the response
     * @throws SRUClientException
     *             any SRU exception, possibly wrapping another exception
     */
    public void onExtraRecordData(String identifier, int position,
            XMLStreamReader reader) throws XMLStreamException,
            SRUClientException;

} // interface SRUSearchRetrieveHandler

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
 * Receive notifications to the response of a <em>explain</em> request.
 *
 * @see SRUExplainRequest
 * @see <a href="http://www.loc.gov/standards/sru/specs/explain.html">SRU Explain Operation</a>
 */
public interface SRUExplainHandler {

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

} // interface SRUExplainHandler

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

/**
 * A SRU client.
 */
public interface SRUClient {

    /**
     * Register a record data parser.
     * 
     * @param parser
     *            a parser instance
     * @throws SRUClientException
     *             if a parser handing the same record schema is already
     *             registered
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @throws IllegalArgumentException
     *             if the supplied parser is invalid
     */
    public abstract void registerRecordParser(SRURecordDataParser parser)
            throws SRUClientException;


    /**
     * Perform a <em>explain</em> operation.
     * 
     * @param request
     *            an instance of a {@link SRUExplainRequest} object
     * @param handler
     *            an instance of {@link SRUExplainHandler} to receive callbacks
     *            when processing the result of this request
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @see SRUExplainRequest
     * @see SRUExplainHandler
     */
    public abstract void explain(SRUExplainRequest request,
            SRUExplainHandler handler) throws SRUClientException;


    /**
     * Perform a <em>scan</em> operation.
     * 
     * @param request
     *            an instance of a {@link SRUScanRequest} object
     * @param handler
     *            an instance of {@link SRUScanHandler} to receive callbacks
     *            when processing the result of this request
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @see SRUScanRequest
     * @see SRUScanHandler
     */
    public abstract void scan(SRUScanRequest request, SRUScanHandler handler)
            throws SRUClientException;


    /**
     * Perform a <em>searchRetreive</em> operation.
     * 
     * @param request
     *            an instance of a {@link SRUSearchRetrieveRequest} object
     * @param handler
     *            an instance of {@link SRUSearchRetrieveHandler} to receive
     *            callbacks when processing the result of this request
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @see SRUSearchRetrieveRequest
     * @see SRUSearchRetrieveHandler
     */
    public abstract void searchRetrieve(SRUSearchRetrieveRequest request,
            SRUSearchRetrieveHandler handler) throws SRUClientException;

}
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

/**
 * A interface for creating asynchronous callbacks for use with the
 * {@link SRUThreadedClient}.
 *
 * <p>
 * NB: the callbacks will be executed by any of worker threads of the client.
 * </p>
 *
 * @param <T>
 *            a response type
 *
 * @see SRUThreadedClient#explain(SRUExplainRequest, SRUCallback)
 * @see SRUThreadedClient#scan(SRUScanRequest, SRUCallback)
 * @see SRUThreadedClient#searchRetrieve(SRUSearchRetrieveRequest, SRUCallback)
 */
public interface SRUCallback<T extends SRUAbstractResponse<?>> {

    /**
     * Invoked when the request has been completed successfully.
     *
     * @param response
     *            the response to the rquest
     */
    public void done(T response);

} // interface SRUCallback

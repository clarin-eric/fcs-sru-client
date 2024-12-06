/**
 * This software is copyright (c) 2011-2021 by
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

import java.util.Map;

public interface SRURequestAuthenticator {

    /**
     * Make an HTTP Authentication header value for a SRU request. This method
     * must return a valid HTTP Authentication header value or <code>null</code>
     * if no authentication for this endpoint and/or operation is required.
     *
     * @param operation
     *            the SRU operation for the request
     * @param endpointURI
     *            the endpoint base URI
     * @param context
     *            additional authentication context data to provide additional
     *            information per SRU requests, may be <code>null</code>
     * @return or authentication header value or <code>null</code>
     */
    public String createAuthenticationHeaderValue(SRUOperation operation, String endpointURI, Map<String, String> context);

} // interface SRURequestAuthenticator

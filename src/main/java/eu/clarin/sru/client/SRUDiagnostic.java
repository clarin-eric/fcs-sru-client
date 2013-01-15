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

/**
 * Class to hold a SRU diagnostic.
 * 
 * @see <a href="http://www.loc.gov/standards/sru/specs/diagnostics.html">SRU
 *      Diagnostics</a>
 * @see <a
 *      href="http://www.loc.gov/standards/sru/resources/diagnostics-list.html">SRU
 *      Diagnostics List</a>
 */
public final class SRUDiagnostic {
    private final String uri;
    private final String details;
    private final String message;


    /**
     * Constructor.
     * 
     * @param uri
     *            the URI identifying the diagnostic
     * @param details
     *            supplementary information available, often in a format
     *            specified by the diagnostic or <code>null</code>
     * @param message
     *            human readable message to display to the end user or
     *            <code>null</code>
     */
    public SRUDiagnostic(String uri, String details, String message) {
        this.uri = uri;
        this.details = details;
        this.message = message;
    }


    /**
     * Get diagnostic's identifying URI.
     * 
     * @return diagnostic code
     */
    public String getURI() {
        return uri;
    }


    /**
     * Get supplementary information for this diagnostic. The format for this
     * value is often specified by the diagnostic code.
     * 
     * @return supplementary information
     */
    public String getDetails() {
        return details;
    }


    /**
     * Get human readable message.
     * 
     * @return human readable message
     */
    public String getMessage() {
        return message;
    }

} // class SRUDiagnostic

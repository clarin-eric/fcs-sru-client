/**
 * This software is copyright (c) 2025- by
 *  - Sächsische Akademie der Wissenschaften zu Leipzig (https://www.saw-leipzipg.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Sächsische Akademie der Wissenschaften zu Leipzig (https://www.saw-leipzipg.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */

package eu.clarin.sru.client;

/**
 * SRU client exception raised when SRU responses show versions not
 * corresponding the SRU requests.
 */
@SuppressWarnings("serial")
public class SRUInvalidVersionException extends SRUClientException {

    private final SRUVersion requestedVersion;
    private final SRUVersion detectedVersion;

    /**
     * Constructor
     *
     * @param message          an error message
     * @param requestedVersion the SRU version used in the request
     * @param detectedVersion  the SRU version found in the response
     */
    public SRUInvalidVersionException(String message, SRUVersion requestedVersion, SRUVersion detectedVersion) {
        super(message);
        this.requestedVersion = requestedVersion;
        this.detectedVersion = detectedVersion;
    }

    /**
     * The SRU Version of the SRU Request.
     * 
     * @return the SRU version used in the SRU request or <code>null</code> if
     *         unknown
     */
    public SRUVersion getRequestedVersion() {
        return requestedVersion;
    }

    /**
     * The SRU Version of the SRU Response.
     * 
     * @return the SRU version found in the SRU response or <code>null</code> if
     *         unable to parse/detect or otherwise
     */
    public SRUVersion getDetectedVersion() {
        return detectedVersion;
    }

}

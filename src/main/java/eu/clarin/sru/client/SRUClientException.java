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
 * SRU client exception. 
 */
@SuppressWarnings("serial")
public class SRUClientException extends Exception {

    /**
     * Constructor
     * 
     * @param message an error message
     */
    public SRUClientException(String message) {
        super(message);
    }


    /**
     * Constructor
     *  
     * @param message an error message
     * @param cause the cause of the error
     */
    public SRUClientException(String message, Throwable cause) {
        super(message, cause);
    }

} // class SRUClientException

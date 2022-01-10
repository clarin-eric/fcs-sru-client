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

/**
 * Diagnostics defined by SRU client.
 */
public final class SRUClientDiagnostics {
    private static final String URI_PREFIX = "info:clarin/sru/diagnostic/";
    /**
     * The record parser erroneously returned a null pointer instead of a record
     * data object.
     */
    public static final String DIAG_RECORD_PARSER_NULL = URI_PREFIX + "1";
    /**
     * No record parser was found for this record schema. The name of the record
     * schema is supplied in the diagnostic's details.
     */
    public static final String DIAG_NO_RECORD_PARSER = URI_PREFIX + "2";


    /*
     * hide constructor
     */
    private SRUClientDiagnostics() {
    }

} // class SRUClientDiagnostics

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
 * Some constants for the SRUClient.
 */
public final class SRUClientConstants {

    /** constant record data schema parser to match any schema */
    public static final String RECORD_DATA_PARSER_SCHEMA_ANY = "*";

    /** constant for CQL query type */
    public static final String QUERY_TYPE_CQL = "cql";

    /**
     * constant for extra request parameter "x-unlimited-resultset" (NB: only
     * applicable for SRUServer implementation)
     */
    public static final String X_UNLIMITED_RESULTSET =
            "x-unlimited-resultset";

    /**
     * constant for extra request parameter "x-unlimited-termlist" (NB: only
     * applicable for SRUServer implementation)
     */
    public static final String X_UNLIMITED_TERMLIST =
            "x-unlimited-termlist";

    /**
     * constant for extra request parameter "x-indent-response" (NB: only
     * applicable for SRUServer implementation)
     */
    public static final String X_INDENT_RESPONSE =
            "x-indent-response";


    /* hide constructor */
    private SRUClientConstants() {
    }

} // class SRUClientConstants

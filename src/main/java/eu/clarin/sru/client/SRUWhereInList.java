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
 * A flag to indicate the position of the term within the complete term
 * list.
 */
public enum SRUWhereInList {
    /**
     * The first term (<em>first</em>)
     */
    FIRST,

    /**
     * The last term (<em>last</em>)
     */
    LAST,

    /**
     * The only term (<em>only</em>)
     */
    ONLY,

    /**
     * Any other term (<em>inner</em>)
     */
    INNER

} // enum SRUWhereInList
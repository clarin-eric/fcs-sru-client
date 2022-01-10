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
 * SRU 2.0 record packing.
 */
public enum SRURecordPacking {
    /**
     * The client requests that the server should supply records strictly
     * according to the requested schema.
     */
    PACKED,

    /**
     * The server is free to allow the location of application data to vary
     * within the record.
     */
    UNPACKED
} // enum SRURecordPacking

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
 * Interface for parsed record data.
 * 
 */
public interface SRURecordData {

    /**
     * This record is transient. If you want to store the data to process it
     * later, you need to create a copy of the data.
     * 
     * @return <code>true</code>, if record is transient,
     *         <code>false<code> otherwis
     */
    public boolean isTransient();


    /**
     * The record schema for this record.
     * 
     * @return the record schema
     */
    public String getRecordSchema();

} // interface SRURecordData

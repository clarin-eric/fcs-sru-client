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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


/**
 * A parser to parse record data and create appropriate Java objects.
 *
 */
public interface SRURecordDataParser {
    /**
     * The record schema this parser is able to process.
     *
     * @return the record schema this parser is able to process
     */
    public String getRecordSchema();


    /**
     * Parse a record data into a Java object.
     *
     * @param reader
     *            a {@link XMLStreamReader} to parse the record data
     * @return the parsed record
     * @throws XMLStreamException
     *             an error occurred while parsing the response
     * @throws SRUClientException
     *             any SRU exception, possibly wrapping another exception
     * @see SRURecordData
     */
    public SRURecordData parse(XMLStreamReader reader)
            throws XMLStreamException, SRUClientException;

} // interface SRURecordDataParser

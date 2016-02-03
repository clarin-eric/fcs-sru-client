/**
 * This software is copyright (c) 2012-2016 by
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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


/**
 * A parser to parse extra response data in a SRU response create appropriate
 * Java objects.
 */
public interface SRUExtraResponseDataParser {

    /**
     * Check, if the current element can be handled by this parser.
     *
     * @param name
     *            the name of the element to be examined
     *
     * @return <code>true</code> if the element can be handled by this parser;
     *         <code>false</code> otherwise
     */
    public boolean supports(QName name);


    /**
     * Parse the extra response data into a Java object. After parsing, the
     * supplied reader should be positioned after the end element to the
     * corresponding start element.
     *
     * @param reader
     *            a {@link XMLStreamReader} to parse the extra response data
     * @return the parsed extra response data
     * @throws XMLStreamException
     *             an error occurred while parsing
     * @throws SRUClientException
     *             any SRU exception, possibly wrapping another exception
     * @see SRUExtraResponseData
     */
    public SRUExtraResponseData parse(XMLStreamReader reader)
            throws XMLStreamException, SRUClientException;

} // interface SRUExtraResponseDataParser

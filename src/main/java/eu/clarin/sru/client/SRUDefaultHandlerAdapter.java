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

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


/**
 * Default base class for SRU client response handlers.
 * <p>
 * This class is available as a convenience base class for SRU client
 * applications: it provides default implementations for all of the callbacks in
 * the three request handler handler classes:
 * </p>
 * <ul>
 *  <li>{@link SRUExplainHandler}</li>
 *  <li>{@link SRUScanHandler}</li>
 *  <li>{@link SRUSearchRetrieveHandler}</li>
 * </ul>
 * <p>
 * Application writers can extend this class when they need to implement only
 * part of an interface; parser writers can instantiate this class to provide
 * default handlers when the application has not supplied its own.
 * </p>
 * 
 * @see SRUExplainHandler
 * @see SRUScanHandler
 * @see SRUSearchRetrieveHandler
 */
public class SRUDefaultHandlerAdapter implements SRUDefaultHandler {
    
    @Override
    public void onDiagnostics(List<SRUDiagnostic> diagnostics)
            throws SRUClientException {
    }

    @Override
    public void onRequestStatistics(int bytes, long millisTotal,
            long millisNetwork, long millisParsing) {
    }


    @Override
    public void onExtraResponseData(XMLStreamReader reader)
            throws XMLStreamException, SRUClientException {
    }


    @Override
    public void onStartTerms() throws SRUClientException {
    }


    @Override
    public void onFinishTerms() throws SRUClientException {
    }


    @Override
    public void onTerm(String value, int numberOfRecords, String displayTerm,
            SRUWhereInList whereInList) throws SRUClientException {
    }


    @Override
    public void onExtraTermData(String value, XMLStreamReader reader)
            throws XMLStreamException, SRUClientException {
    }


    @Override
    public void onStartRecords(int numberOfRecords, String resultSetId,
            int resultSetIdleTime) throws SRUClientException {
    }


    @Override
    public void onFinishRecords(int nextRecordPosition)
            throws SRUClientException {
    }


    @Override
    public void onRecord(String identifier, int position, SRURecordData data)
            throws SRUClientException {
    }


    @Override
    public void onSurrogateRecord(String identifier, int position,
            SRUDiagnostic data) throws SRUClientException {
    }


    @Override
    public void onExtraRecordData(String identifier, int position,
            XMLStreamReader reader) throws XMLStreamException,
            SRUClientException {
    }

} // class SRUDefaultHandler

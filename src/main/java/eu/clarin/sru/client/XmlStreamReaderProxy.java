/**
 * This software is copyright (c) 2012-2014 by
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

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

class XmlStreamReaderProxy implements XMLStreamReader {
    private static enum State {
        START_DOCUMENT, PROCESSING, END_DOCUMENT, PAST_END_DOCUMENT
    }

    private XMLStreamReader reader;
    private State state;
    private int depth;

    XmlStreamReaderProxy() {
        super();
    }

    void reset(XMLStreamReader reader) throws XMLStreamException {
        this.reader = reader;
        this.state = State.START_DOCUMENT;
        this.depth = 0;
    }

    @Override
    public void close() throws XMLStreamException {
    }

    @Override
    public int getAttributeCount() {
        if (!checkEventTypes(XMLStreamConstants.START_ELEMENT,
                XMLStreamConstants.ATTRIBUTE)) {
            throw new IllegalStateException(
                    "Current event is not START_ELEMENT or ATTRIBUTE");
        }
        return reader.getAttributeCount();
    }

    @Override
    public String getAttributeLocalName(int index) {
        if (!checkEventTypes(XMLStreamConstants.START_ELEMENT,
                XMLStreamConstants.ATTRIBUTE)) {
            throw new IllegalStateException(
                    "Current event is not START_ELEMENT or ATTRIBUTE");
        }
        return reader.getAttributeLocalName(index);
    }

    @Override
    public QName getAttributeName(int index) {
        if (!checkEventTypes(XMLStreamConstants.START_ELEMENT,
                XMLStreamConstants.ATTRIBUTE)) {
            throw new IllegalStateException(
                    "Current event is not START_ELEMENT or ATTRIBUTE");
        }
        return reader.getAttributeName(index);
    }

    @Override
    public String getAttributeNamespace(int index) {
        if (!checkEventTypes(XMLStreamConstants.START_ELEMENT,
                XMLStreamConstants.ATTRIBUTE)) {
            throw new IllegalStateException(
                    "Current event is not START_ELEMENT or ATTRIBUTE");
        }
        return reader.getAttributeNamespace(index);
    }

    @Override
    public String getAttributePrefix(int index) {
        if (!checkEventTypes(XMLStreamConstants.START_ELEMENT,
                XMLStreamConstants.ATTRIBUTE)) {
            throw new IllegalStateException(
                    "Current event is not START_ELEMENT or ATTRIBUTE");
        }
        return reader.getAttributePrefix(index);
    }

    @Override
    public String getAttributeType(int index) {
        if (!checkEventTypes(XMLStreamConstants.START_ELEMENT,
                XMLStreamConstants.ATTRIBUTE)) {
            throw new IllegalStateException(
                    "Current event is not START_ELEMENT or ATTRIBUTE");
        }
        return reader.getAttributeType(index);
    }

    @Override
    public String getAttributeValue(int index) {
        if (!checkEventTypes(XMLStreamConstants.START_ELEMENT,
                XMLStreamConstants.ATTRIBUTE)) {
            throw new IllegalStateException(
                    "Current event is not START_ELEMENT or ATTRIBUTE");
        }
        return reader.getAttributeValue(index);
    }

    @Override
    public String getAttributeValue(String namespaceURI, String localName) {
        if (!checkEventTypes(XMLStreamConstants.START_ELEMENT,
                XMLStreamConstants.ATTRIBUTE)) {
            throw new IllegalStateException(
                    "Current event is not START_ELEMENT or ATTRIBUTE");
        }
        return reader.getAttributeValue(namespaceURI, localName);
    }

    @Override
    public String getCharacterEncodingScheme() {
        return reader.getCharacterEncodingScheme();
    }

    @Override
    public String getElementText() throws XMLStreamException {
        if (!checkEventType(XMLStreamConstants.START_ELEMENT)) {
            throw new IllegalStateException(
                    "Current event is not START_ELEMENT");
        }
        return reader.getElementText();
    }

    @Override
    public String getEncoding() {
        return reader.getEncoding();
    }

    @Override
    public int getEventType() {
        switch (state) {
        case START_DOCUMENT:
            return XMLStreamConstants.START_DOCUMENT;
        case PROCESSING:
            return reader.getEventType();
        case END_DOCUMENT:
            return XMLStreamConstants.END_DOCUMENT;
        case PAST_END_DOCUMENT:
            throw new IllegalStateException("past end of document");
        default:
            throw new RuntimeException("invalid internel state: " + state);
        }
    }

    @Override
    public String getLocalName() {
        if (!checkEventTypes(XMLStreamConstants.START_ELEMENT,
                XMLStreamConstants.END_ELEMENT,
                XMLStreamConstants.ENTITY_REFERENCE)) {
            throw new IllegalStateException("Current event is not "
                    + "START_ELEMENT, END_ELEMENT or ENTITY_REFERENCE");
        }
        return reader.getLocalName();
    }

    @Override
    public Location getLocation() {
        return reader.getLocation();
    }

    @Override
    public QName getName() {
        if (!checkEventTypes(XMLStreamConstants.START_ELEMENT,
                XMLStreamConstants.END_ELEMENT)) {
            throw new IllegalStateException("Current event is not "
                    + "START_ELEMENT or END_ELEMENT");
        }
        return reader.getName();
    }

    @Override
    public NamespaceContext getNamespaceContext() {
        if (!checkEventTypes(XMLStreamConstants.START_ELEMENT,
                XMLStreamConstants.END_ELEMENT, XMLStreamConstants.NAMESPACE)) {
            throw new IllegalStateException("Current event is not "
                    + "START_ELEMENT, END_ELEMENT or NAMESPACE");
        }
        return reader.getNamespaceContext();
    }

    @Override
    public int getNamespaceCount() {
        if (!checkEventTypes(XMLStreamConstants.START_ELEMENT,
                XMLStreamConstants.END_ELEMENT, XMLStreamConstants.NAMESPACE)) {
            throw new IllegalStateException("Current event is not "
                    + "START_ELEMENT, END_ELEMENT or NAMESPACE");
        }
        return reader.getNamespaceCount();
    }

    @Override
    public String getNamespacePrefix(int index) {
        if (!checkEventTypes(XMLStreamConstants.START_ELEMENT,
                XMLStreamConstants.END_ELEMENT, XMLStreamConstants.NAMESPACE)) {
            throw new IllegalStateException("Current event is not "
                    + "START_ELEMENT, END_ELEMENT or NAMESPACE");
        }
        return reader.getNamespacePrefix(index);
    }

    @Override
    public String getNamespaceURI() {
        if (!checkEventTypes(XMLStreamConstants.START_ELEMENT,
                XMLStreamConstants.END_ELEMENT, XMLStreamConstants.NAMESPACE)) {
            throw new IllegalStateException("Current event is not "
                    + "START_ELEMENT, END_ELEMENT or NAMESPACE");
        }
        return reader.getNamespaceURI();
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return reader.getNamespaceURI(prefix);
    }

    @Override
    public String getNamespaceURI(int index) {
        if (!checkEventTypes(XMLStreamConstants.START_ELEMENT,
                XMLStreamConstants.END_ELEMENT, XMLStreamConstants.NAMESPACE)) {
            throw new IllegalStateException("Current event is not "
                    + "START_ELEMENT, END_ELEMENT or NAMESPACE");
        }
        return reader.getNamespaceURI(index);
    }

    @Override
    public String getPIData() {
        if (!checkEventType(XMLStreamConstants.PROCESSING_INSTRUCTION)) {
            throw new IllegalStateException(
                    "Current event is not PROCESSING_INSTRUCTION");
        }
        return reader.getPIData();
    }

    @Override
    public String getPITarget() {
        if (!checkEventType(XMLStreamConstants.PROCESSING_INSTRUCTION)) {
            throw new IllegalStateException(
                    "Current event is not PROCESSING_INSTRUCTION");
        }
        return reader.getPITarget();
    }

    @Override
    public String getPrefix() {
        return reader.getPrefix();
    }

    @Override
    public Object getProperty(String name) throws IllegalArgumentException {
        return reader.getProperty(name);
    }

    @Override
    public String getText() {
        if (!checkEventTypes(XMLStreamConstants.CHARACTERS,
                XMLStreamConstants.CDATA, XMLStreamConstants.COMMENT,
                XMLStreamConstants.SPACE, XMLStreamConstants.ENTITY_REFERENCE)) {
            throw new IllegalStateException("Current event is a text event");
        }
        return reader.getText();
    }

    @Override
    public char[] getTextCharacters() {
        if (!checkEventTypes(XMLStreamConstants.CHARACTERS,
                XMLStreamConstants.CDATA, XMLStreamConstants.COMMENT,
                XMLStreamConstants.SPACE)) {
            throw new IllegalStateException("Current event is a text event");
        }
        return reader.getTextCharacters();
    }

    @Override
    public int getTextCharacters(int sourceStart, char[] target,
            int targetStart, int length) throws XMLStreamException {
        if (!checkEventTypes(XMLStreamConstants.CHARACTERS,
                XMLStreamConstants.CDATA, XMLStreamConstants.COMMENT,
                XMLStreamConstants.SPACE)) {
            throw new UnsupportedOperationException(
                    "Current event is a text event");
        }
        return reader.getTextCharacters(sourceStart, target, targetStart,
                length);
    }

    @Override
    public int getTextLength() {
        if (!checkEventTypes(XMLStreamConstants.CHARACTERS,
                XMLStreamConstants.CDATA, XMLStreamConstants.COMMENT,
                XMLStreamConstants.SPACE)) {
            throw new IllegalStateException("invalid state");
        }
        return reader.getTextLength();
    }

    @Override
    public int getTextStart() {
        if (!checkEventTypes(XMLStreamConstants.CHARACTERS,
                XMLStreamConstants.CDATA, XMLStreamConstants.COMMENT,
                XMLStreamConstants.SPACE)) {
            throw new IllegalStateException("invalid state");
        }
        return reader.getTextStart();
    }

    @Override
    public String getVersion() {
        return reader.getVersion();
    }

    @Override
    public boolean hasName() {
        switch (state) {
        case START_DOCUMENT:
            /* FALL-THOUGH */
        case END_DOCUMENT:
            /* FALL-THOUGH */
        case PAST_END_DOCUMENT:
            return false;
        default:
            return reader.hasName();
        }
    }

    @Override
    public boolean hasNext() throws XMLStreamException {
        if (state == State.END_DOCUMENT) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hasText() {
        switch (state) {
        case START_DOCUMENT:
            /* FALL-THOUGH */
        case END_DOCUMENT:
            /* FALL-THOUGH */
        case PAST_END_DOCUMENT:
            return false;
        default:
            return reader.hasText();
        } // switch
    }

    @Override
    public boolean isAttributeSpecified(int index) {
        if (!checkEventTypes(XMLStreamConstants.START_ELEMENT,
                XMLStreamConstants.ATTRIBUTE)) {
            throw new IllegalStateException(
                    "Current event is not START_ELEMENT or ATTRIBUTE");
        }
        return reader.isAttributeSpecified(index);
    }

    @Override
    public boolean isCharacters() {
        switch (state) {
        case START_DOCUMENT:
            /* FALL-THOUGH */
        case END_DOCUMENT:
            /* FALL-THOUGH */
        case PAST_END_DOCUMENT:
            return false;
        default:
            return reader.isCharacters();
        }
    }

    @Override
    public boolean isEndElement() {
        switch (state) {
        case START_DOCUMENT:
            /* FALL-THOUGH */
        case END_DOCUMENT:
            /* FALL-THOUGH */
        case PAST_END_DOCUMENT:
            return false;
        default:
            return reader.isEndElement();
        }
    }

    @Override
    public boolean isStandalone() {
        return reader.isStandalone();
    }

    @Override
    public boolean isStartElement() {
        switch (state) {
        case START_DOCUMENT:
            /* FALL-THOUGH */
        case END_DOCUMENT:
            /* FALL-THOUGH */
        case PAST_END_DOCUMENT:
            return false;
        default:
            return reader.isStartElement();
        }
    }

    @Override
    public boolean isWhiteSpace() {
        switch (state) {
        case START_DOCUMENT:
            /* FALL-THOUGH */
        case END_DOCUMENT:
            return true;
        case PAST_END_DOCUMENT:
            return false;
        default:
            return reader.isWhiteSpace();
        }
    }

    @Override
    public int next() throws XMLStreamException {
        int type = -1;
        switch (state) {
        case START_DOCUMENT:
            state = State.PROCESSING;
            type = reader.getEventType();
            if (type == XMLStreamConstants.START_ELEMENT) {
                depth++;
            }
            break;
        case PROCESSING:
            type = reader.next();
            switch (type) {
            case XMLStreamConstants.START_ELEMENT:
                depth++;
                break;
            case XMLStreamConstants.END_ELEMENT:
                depth--;
                break;
            default:
                break;
            } // switch (t)
            if (depth < 0) {
                state = State.END_DOCUMENT;
                type = XMLStreamConstants.END_DOCUMENT;
            }
            break;
        case END_DOCUMENT:
            state = State.PAST_END_DOCUMENT;
            type = XMLStreamConstants.END_DOCUMENT;
            break;
        default:
            throw new IllegalStateException("past end of input");
        } // switch (state)
        return type;
    }

    @Override
    public int nextTag() throws XMLStreamException {
        boolean skip = true;
        int type = getEventType();
        do {
            switch (type) {
            case XMLStreamConstants.CHARACTERS:
                /* FALL_TROUGH */
            case XMLStreamConstants.CDATA:
                if (!isWhiteSpace()) {
                    skip = false;
                    break;
                }
            case XMLStreamConstants.START_DOCUMENT:
                /* FALL_TROUGH */
            case XMLStreamConstants.SPACE:
                /* FALL_TROUGH */
            case XMLStreamConstants.PROCESSING_INSTRUCTION:
                /* FALL_TROUGH */
            case XMLStreamConstants.COMMENT:
                type = next();
                break;
            default:
                skip = false;
            } // switch
        } while (skip);
        if ((type != XMLStreamConstants.START_ELEMENT) &&
                (type != XMLStreamConstants.END_ELEMENT)) {
            throw new XMLStreamException("expected start or end tag",
                    getLocation());
        }
        return type;
    }

    @Override
    public void require(int type, String namespaceURI, String localName)
            throws XMLStreamException {
        reader.require(type, namespaceURI, localName);
    }

    @Override
    public boolean standaloneSet() {
        return reader.standaloneSet();
    }

    private boolean checkEventType(int type) {
        switch (state) {
        case START_DOCUMENT:
            return XMLStreamConstants.START_DOCUMENT == type;
        case END_DOCUMENT:
            return XMLStreamConstants.END_DOCUMENT == type;
        case PAST_END_DOCUMENT:
            return false;
        default:
            return reader.getEventType() == type;
        } // switch
    }

    private boolean checkEventTypes(int... types) {
        if (state == State.PROCESSING) {
            for (int type : types) {
                if (type == reader.getEventType()) {
                    return true;
                }
            }
        }
        return false;
    }

} // class XmlStreamReaderWrapper

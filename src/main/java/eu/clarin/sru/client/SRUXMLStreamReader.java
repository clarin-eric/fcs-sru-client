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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import com.ctc.wstx.api.WstxInputProperties;


class SRUXMLStreamReader implements XMLStreamReader {
    private final class CountingInputStream extends FilterInputStream {
        protected long count = 0;

        private CountingInputStream(InputStream stream) {
            super(stream);
        }

        @Override
        public int read() throws IOException {
            final int value = super.read();
            if (value != -1) {
                count++;
            }
            return value;
        }

        @Override
        public int read(byte[] buffer, int offset, int length)
                throws IOException {
            final int result = super.read(buffer, offset, length);
            if (result >= 0) {
                count += result;
            }
            return result;
        }

        @Override
        public int read(byte[] buffer) throws IOException {
            return this.read(buffer, 0, buffer.length);
        }

        @Override
        public long skip(long n) throws IOException {
            final long result = super.skip(n);
            if (result > 0) {
                count += result;
            }
            return result;
        }
    } // class CountingInputStream
    private static final XMLInputFactory2 factory;
    private final InputStream stream;
    private final XMLStreamReader2 reader;

    SRUXMLStreamReader(InputStream in, boolean wrap) throws XMLStreamException {
        this.stream = wrap ? new CountingInputStream(in) : in;
        this.reader =
                (XMLStreamReader2) factory.createXMLStreamReader(stream);
    }


    @Override
    public void close() throws XMLStreamException {
        reader.close();
    }


    @Override
    public int getAttributeCount() {
        return reader.getAttributeCount();
    }


    @Override
    public String getAttributeLocalName(int index) {
        return reader.getAttributeLocalName(index);
    }


    @Override
    public QName getAttributeName(int index) {
        return reader.getAttributeName(index);
    }


    @Override
    public String getAttributeNamespace(int index) {
        return reader.getAttributeNamespace(index);
    }


    @Override
    public String getAttributePrefix(int index) {
        return reader.getAttributePrefix(index);
    }


    @Override
    public String getAttributeType(int index) {
        return reader.getAttributeType(index);
    }


    @Override
    public String getAttributeValue(int index) {
        return reader.getAttributeValue(index);
    }


    @Override
    public String getAttributeValue(String namespaceURI, String localName) {
        return reader.getAttributeValue(namespaceURI, localName);
    }


    @Override
    public String getCharacterEncodingScheme() {
        return reader.getCharacterEncodingScheme();
    }


    @Override
    public String getElementText() throws XMLStreamException {
        return reader.getElementText();
    }


    @Override
    public String getEncoding() {
        return reader.getEncoding();
    }


    @Override
    public int getEventType() {
        return reader.getEventType();
    }


    @Override
    public String getLocalName() {
        return reader.getLocalName();
    }


    @Override
    public Location getLocation() {
        return reader.getLocation();
    }


    @Override
    public QName getName() {
        return reader.getName();
    }


    @Override
    public NamespaceContext getNamespaceContext() {
        return reader.getNamespaceContext();
    }


    @Override
    public int getNamespaceCount() {
        return reader.getNamespaceCount();
    }


    @Override
    public String getNamespacePrefix(int index) {
        return reader.getNamespacePrefix(index);
    }


    @Override
    public String getNamespaceURI() {
        return reader.getNamespaceURI();
    }


    @Override
    public String getNamespaceURI(String prefix) {
        return reader.getNamespaceURI(prefix);
    }


    @Override
    public String getNamespaceURI(int index) {
        return reader.getNamespaceURI(index);
    }


    @Override
    public String getPIData() {
        return reader.getPIData();
    }


    @Override
    public String getPITarget() {
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
        return reader.getText();
    }


    @Override
    public char[] getTextCharacters() {
        return reader.getTextCharacters();
    }


    @Override
    public int getTextCharacters(int sourceStart, char[] target,
            int targetStart, int length) throws XMLStreamException {
        return reader.getTextCharacters(sourceStart, target, targetStart, length);
    }


    @Override
    public int getTextLength() {
        return reader.getTextLength();
    }


    @Override
    public int getTextStart() {
        return reader.getTextStart();
    }


    @Override
    public String getVersion() {
        return reader.getVersion();
    }


    @Override
    public boolean hasName() {
        return reader.hasName();
    }


    @Override
    public boolean hasNext() throws XMLStreamException {
        return reader.hasNext();
    }


    @Override
    public boolean hasText() {
        return reader.hasText();
    }


    @Override
    public boolean isAttributeSpecified(int index) {
        return reader.isAttributeSpecified(index);
    }


    @Override
    public boolean isCharacters() {
        return reader.isCharacters();
    }


    @Override
    public boolean isEndElement() {
        return reader.isEndElement();
    }


    @Override
    public boolean isStandalone() {
        return reader.isStandalone();
    }


    @Override
    public boolean isStartElement() {
        return reader.isStartElement();
    }


    @Override
    public boolean isWhiteSpace() {
        return reader.isWhiteSpace();
    }


    @Override
    public int next() throws XMLStreamException {
        return reader.next();
    }


    @Override
    public int nextTag() throws XMLStreamException {
        return reader.nextTag();
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


    void closeCompletly() {
        try {
            reader.close();
        } catch (XMLStreamException e) {
            /* IGNORE */
        }
        try {
            stream.close();
        } catch (IOException e) {
            /* IGNORE */
        }
    }


    long getByteCount() {
        if (stream instanceof CountingInputStream) {
            return ((CountingInputStream) stream).count;
        } else {
            return -1;
        }
    }


    boolean readStart(String namespaceURI, String localName, boolean required)
            throws XMLStreamException {
        return readStart(namespaceURI, localName, required, false);
    }


    boolean readStart(String namespaceURI, String localName, boolean required,
            boolean attributes) throws XMLStreamException {
        // System.err.println("readStart (" + localName + ", required = " +
        // required + ") @ " + toReadable(reader));
        if (!reader.isEndElement()) {
            while (reader.hasNext()) {
                // System.err.println("  LOOP: " + dumpState());
                if (reader.isWhiteSpace()) {
                    reader.next();
                    continue;
                }
                if (reader.isStartElement()) {
                    if (namespaceURI.equals(reader.getNamespaceURI()) &&
                            localName.equals(reader.getLocalName())) {
                        // System.err.print("--> found ");
                        if (!attributes) {
                            // System.err.print("and consumed ");
                            reader.next(); // skip to next event
                        }
                        // System.err.println("@ " + toReadable(reader));
                        return true;
                    }
                    break;
                }
                if (reader.isCharacters() || reader.isEndElement()) {
                    break;
                }
                reader.next();
            } // while
        }
        if (required) {
            // System.err.println("--> error, not found @ " +
            // toReadable(reader));
            throw new XMLStreamException("expected element '" +
                    new QName(namespaceURI, localName) + "', but found '" +
                    reader.getName() + "'", reader.getLocation());
        }
        // System.err.println("--> not found @ " + toReadable(reader));
        return false;
    }


    void readEnd(String namespaceURI, String localName)
            throws XMLStreamException {
        readEnd(namespaceURI, localName, false);
    }


    void readEnd(String namespaceURI, String localName, boolean skipContent)
            throws XMLStreamException {
        // System.err.println("readEnd (" + localName + ") @ " + dumpState() +
        // ", skipContent = " + skipContent);
        int level = 1;
        while (reader.hasNext()) {
            // System.err.println("  LOOP " + dumpState() + " [" +
            // level + "]");
            if (reader.isWhiteSpace()) {
                reader.next();
                continue;
            }
            if (skipContent) {
                if (reader.isCharacters()) {
                    reader.next();
                    continue;
                }
                if (reader.isStartElement()) {
                    if (!(namespaceURI.equals(reader.getNamespaceURI()) && localName
                            .equals(reader.getLocalName()))) {
                        level++;
                    }
                    reader.next();
                    continue;
                }
            }
            if (reader.isEndElement()) {
                level--;
                // System.err.println("   @END-TAG: " + dumpState() + " [" +
                // level + "]");
                if (level == 0) {
                    if (namespaceURI.equals(reader.getNamespaceURI()) &&
                            localName.equals(reader.getLocalName())) {
                        reader.next(); // consume tag
                        break;
                    } else {
                        throw new XMLStreamException("expected end tag for '" +
                                new QName(namespaceURI, localName) +
                                "', but found '" + reader.getName() + "'",
                                reader.getLocation());
                    }
                }
            }
            reader.next();
        }
        // System.err.println("--> ok @ " + dumpState());
    }


    boolean peekStart(String namespaceURI, String localName)
            throws XMLStreamException {
        if (!reader.isEndElement()) {
            while (reader.hasNext()) {
                // System.err.println("  LOOP: " + dumpState());
                if (reader.isWhiteSpace()) {
                    reader.next();
                    continue;
                }
                if (reader.isStartElement()) {
                    if (namespaceURI.equals(reader.getNamespaceURI()) &&
                            localName.equals(reader.getLocalName())) {
                        return true;
                    } else {
                        return false;
                    }
                }
                if (reader.isCharacters() || reader.isEndElement()) {
                    break;
                }
                reader.next();
            } // while
        }
        return false;
    }


    String readContent(String namespaceURI, String localName, boolean required)
            throws XMLStreamException {
        String result = null;
        if (readStart(namespaceURI, localName, required)) {
            try {
                result = readString(true);
            } catch (XMLStreamException e) {
                StringBuilder sb = new StringBuilder();
                sb.append("element '");
                if (namespaceURI != null) {
                    sb.append('{').append(namespaceURI).append('}');
                }
                sb.append(localName).append("' may not be empty");
                throw new XMLStreamException(sb.toString(), e.getLocation());
            }
            readEnd(namespaceURI, localName);
        }
        return result;
    }


    int readContent(String namespaceURI, String localName, boolean required,
            int defaultValue) throws XMLStreamException {
        if (readStart(namespaceURI, localName, required)) {
            String s = readString(true);
            try {
                readEnd(namespaceURI, localName);
            } catch (XMLStreamException e) {
                StringBuilder sb = new StringBuilder();
                sb.append("element '");
                if (namespaceURI != null) {
                    sb.append('{').append(namespaceURI).append('}');
                }
                sb.append(localName).append("' may not be empty");
                throw new XMLStreamException(sb.toString(), e.getLocation());
            }
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                StringBuilder sb = new StringBuilder();
                sb.append("element '");
                if (namespaceURI != null) {
                    sb.append('{').append(namespaceURI).append('}');
                }
                sb.append(" was expected to be of type xs:integer; ");
                sb.append("incompatible value was: ").append(s);
                throw new XMLStreamException(sb.toString(),
                        reader.getLocation(), e);
            }
        }
        return defaultValue;
    }


    String readString(boolean required) throws XMLStreamException {
        // System.err.println("readString @ " + toReadable(reader));
        StringBuilder sb = new StringBuilder();
        while (reader.isCharacters()) {
            String s = reader.getText();
            if (s != null) {
                sb.append(s);
            }
            reader.next();
        } // while
        String s = null;
        if (sb.length() > 0) {
            s = sb.toString().trim();
        }
        if (required && ((s == null) || s.isEmpty())) {
            throw new XMLStreamException("expected character content "
                    + "at position ", reader.getLocation());
        }
        // System.err.println("--> ok @ " + toReadable(reader));
        return s;
    }


    String readAttributeValue(String namespaceURI, String localName)
            throws XMLStreamException {
        if (!reader.isStartElement()) {
            throw new XMLStreamException("not at a start elment event",
                    reader.getLocation());
        }
        String attr = reader.getAttributeValue(namespaceURI, localName);
        if (attr != null) {
            attr = attr.trim().intern();
        }
        return attr;
    }


    String readNamespaceURI() throws XMLStreamException {
        if (!reader.isStartElement()) {
            throw new XMLStreamException("not at a start elment event",
                    reader.getLocation());
        }
        return reader.getNamespaceURI();
    }


    String peekElementLocalName() throws XMLStreamException {
        if (!reader.isStartElement()) {
            throw new XMLStreamException("not at a start elment event",
                    reader.getLocation());
        }
        return reader.getLocalName();
    }


    void consumeStart() throws XMLStreamException {
        if (!reader.isStartElement()) {
            throw new XMLStreamException("not at a start elment event",
                    reader.getLocation());
        }
        reader.next();
    }


    void consumeWhitespace() throws XMLStreamException {
        while (reader.isWhiteSpace() && reader.hasNext()) {
            reader.next();
            continue;
        }
    }


    void copyTo(XMLStreamWriter writer) throws XMLStreamException {
        final int depth = reader.getDepth();
        do {
            copyEvent(reader, writer);
            reader.next();
        } while (reader.getDepth() >= depth);
    }


    String dumpState() {
        StringBuilder sb = new StringBuilder();
        switch (reader.getEventType()) {
        case XMLStreamConstants.START_DOCUMENT:
            return "START_DOC";
        case XMLStreamConstants.END_DOCUMENT:
            return "END_DOC";
        case XMLStreamConstants.START_ELEMENT:
            sb.append("START[");
            sb.append(reader.getNamespaceURI());
            sb.append(",");
            sb.append(reader.getLocalName());
            sb.append("]");
            break;
        case XMLStreamConstants.END_ELEMENT:
            sb.append("END[");
            sb.append(reader.getNamespaceURI());
            sb.append(",");
            sb.append(reader.getLocalName());
            sb.append("]");
            break;
        case XMLStreamConstants.CHARACTERS:
            sb.append("CHARACTERS[\"");
            sb.append(reader.getText().replace("\n", "\\n")
                    .replace("\r", "\\r").replace("\t", "\\t"));
            sb.append("\", isWhitespace = ");
            sb.append(reader.isWhiteSpace());
            sb.append("]");
            break;
        case XMLStreamConstants.CDATA:
            sb.append("CDATA[\"");
            sb.append(reader.getText().replace("\n", "\\n")
                    .replace("\r", "\\r").replace("\t", "\\t"));
            sb.append("\", isWhitespace = ");
            sb.append(reader.isWhiteSpace());
            sb.append("]");
            break;
        default:
            sb.append(Integer.toString(reader.getEventType()));
        }
        return sb.toString();
    }


    private static void copyEvent(XMLStreamReader from, XMLStreamWriter to)
            throws XMLStreamException {
        switch (from.getEventType()) {
        case XMLStreamConstants.START_DOCUMENT:
            {
                String version = from.getVersion();
                if (version == null || version.length() == 0) {
                    to.writeStartDocument();
                } else {
                    to.writeStartDocument(from.getCharacterEncodingScheme(),
                            from.getVersion());
                }
                to.writeCharacters("\n");
            }
            return;

        case XMLStreamConstants.END_DOCUMENT:
            to.writeCharacters("\n");
            to.writeEndDocument();
            return;

        case XMLStreamConstants.START_ELEMENT:
            copyStartElement(from, to);
            return;

        case XMLStreamConstants.END_ELEMENT:
            to.writeEndElement();
            return;

        case XMLStreamConstants.SPACE:
            to.writeCharacters(from.getTextCharacters(), from.getTextStart(),
                    from.getTextLength());
            return;

        case XMLStreamConstants.CDATA:
            to.writeCData(from.getText());
            return;

        case XMLStreamConstants.CHARACTERS:
            to.writeCharacters(from.getTextCharacters(), from.getTextStart(),
                    from.getTextLength());
            return;

        case XMLStreamConstants.COMMENT:
            to.writeComment(from.getText());
            return;

        case XMLStreamConstants.PROCESSING_INSTRUCTION:
            to.writeProcessingInstruction(from.getPITarget(), from.getPIData());
            return;

        case XMLStreamConstants.DTD:
        case XMLStreamConstants.ENTITY_REFERENCE:
        case XMLStreamConstants.ATTRIBUTE:
        case XMLStreamConstants.NAMESPACE:
        case XMLStreamConstants.ENTITY_DECLARATION:
        case XMLStreamConstants.NOTATION_DECLARATION:
            /* FALL_TROUGH */
        }
        throw new XMLStreamException("unsupported event type: " +
                from.getEventType());
    }


    private static void copyStartElement(XMLStreamReader from,
            XMLStreamWriter to) throws XMLStreamException {
        final int nsCount = from.getNamespaceCount();
        if (nsCount > 0) { // yup, got some...
            for (int i = 0; i < nsCount; ++i) {
                String pfx = from.getNamespacePrefix(i);
                String uri = from.getNamespaceURI(i);
                if ((pfx == null) || pfx.isEmpty()) { // default NS
                    to.setDefaultNamespace(uri);
                } else {
                    to.setPrefix(pfx, uri);
                }
            }
        }

        final String prefix             = from.getPrefix();
        final NamespaceContext from_ctx = from.getNamespaceContext();
        final NamespaceContext to_ctx   = to.getNamespaceContext();
        boolean repair_prefix_namespace = false;
        if ((prefix != null) && (to_ctx.getNamespaceURI(prefix) == null)) {
            repair_prefix_namespace = true;
            to.setPrefix(prefix, from_ctx.getNamespaceURI(prefix));
        }

        to.writeStartElement(prefix, from.getLocalName(),
                from.getNamespaceURI());

        if (nsCount > 0) {
            // write namespace declarations
            for (int i = 0; i < nsCount; ++i) {
                String pfx = from.getNamespacePrefix(i);
                String uri    = from.getNamespaceURI(i);

                if ((pfx == null) || pfx.isEmpty()) { // default NS
                    to.writeDefaultNamespace(uri);
                } else {
                    to.writeNamespace(pfx, uri);
                }
            }
        }
        if (repair_prefix_namespace) {
            to.writeNamespace(prefix, from_ctx.getNamespaceURI(prefix));
        }

        int attrCount = from.getAttributeCount();
        if (attrCount > 0) {
            for (int i = 0; i < attrCount; ++i) {
                to.writeAttribute(from.getAttributePrefix(i),
                        from.getAttributeNamespace(i),
                        from.getAttributeLocalName(i),
                        from.getAttributeValue(i));
            }
        }
    }


    static {
        factory = (XMLInputFactory2) XMLInputFactory.newInstance();
        // Stax settings
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);

        // Stax2 settings
        factory.setProperty(XMLInputFactory2.P_INTERN_NS_URIS, Boolean.TRUE);
        factory.setProperty(XMLInputFactory2.P_LAZY_PARSING, Boolean.FALSE);

        // Woodstox settings
        factory.setProperty(WstxInputProperties.P_NORMALIZE_LFS, Boolean.TRUE);
    }

} // SRUXMLStreamReader

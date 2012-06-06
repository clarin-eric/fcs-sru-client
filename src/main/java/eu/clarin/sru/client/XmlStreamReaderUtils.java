package eu.clarin.sru.client;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public final class XmlStreamReaderUtils {
    private XmlStreamReaderUtils() {
    }

    public static boolean readStart(XMLStreamReader reader, String namespaceURI, String localName, boolean required)
            throws XMLStreamException {
        return readStart(reader, namespaceURI, localName, required, false);
    }


    public static boolean readStart(XMLStreamReader reader, String namespaceURI, String localName, boolean required,
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


    public static void readEnd(XMLStreamReader reader, String namespaceURI, String localName)
            throws XMLStreamException {
        readEnd(reader, namespaceURI, localName, false);
    }


    public static void readEnd(XMLStreamReader reader, String namespaceURI, String localName, boolean skipContent)
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


    public static String readContent(XMLStreamReader reader, String namespaceURI, String localName, boolean required)
            throws XMLStreamException {
        String result = null;
        if (readStart(reader, namespaceURI, localName, required)) {
            result = readString(reader, true);
            readEnd(reader, namespaceURI, localName);
        }
        return result;
    }


    public static int readContent(XMLStreamReader reader, String namespaceURI, String localName, boolean required,
            int defaultValue) throws XMLStreamException {
        if (readStart(reader, namespaceURI, localName, required)) {
            String s = readString(reader, true);
            readEnd(reader, namespaceURI, localName);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                throw new XMLStreamException(
                        "expected a xs:integer value: s", e);
            }
        }
        return defaultValue;
    }


    public static String readString(XMLStreamReader reader, boolean required) throws XMLStreamException {
        // System.err.println("readString @ " + toReadable(reader));
        String s = null;
        if (reader.isCharacters()) {
            s = reader.getText();
            if (s != null) {
                s = s.trim();
            }
            reader.next();
        }
        if (required && ((s == null) || s.isEmpty())) {
            throw new XMLStreamException("expected character content "
                    + "at position", reader.getLocation());
        }
        // System.err.println("--> ok @ " + toReadable(reader));
        return s;
    }


    public static String readAttributeValue(XMLStreamReader reader, String namespaceURI, String localName)
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


    public static String readNamespaceURI(XMLStreamReader reader) throws XMLStreamException {
        if (!reader.isStartElement()) {
            throw new XMLStreamException("not at a start elment event",
                    reader.getLocation());
        }
        return reader.getNamespaceURI();
    }


    public static String peekElementLocalName(XMLStreamReader reader) throws XMLStreamException {
        if (!reader.isStartElement()) {
            throw new XMLStreamException("not at a start elment event",
                    reader.getLocation());
        }
        return reader.getLocalName();
    }


    public static void consumeStart(XMLStreamReader reader) throws XMLStreamException {
        if (!reader.isStartElement()) {
            throw new XMLStreamException("not at a start elment event",
                    reader.getLocation());
        }
        reader.next();
    }


    public static void consumeWhitespace(XMLStreamReader reader) throws XMLStreamException {
        while (reader.isWhiteSpace() && reader.hasNext()) {
            reader.next();
            continue;
        }
    }

} // class XmlStreamReaderUtils

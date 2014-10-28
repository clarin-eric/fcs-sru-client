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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;


/**
 * Helper class for dealing with {@link XMLStreamReader}.
 * <p>
 * <em>Note: this class is semi-public API and may be change or removed in the future.</em>
 * </p>
 */
public final class XmlStreamReaderUtils {
    private XmlStreamReaderUtils() {
    }


    public static boolean readStart(XMLStreamReader reader,
            String namespaceURI, String localName, boolean required)
            throws XMLStreamException {
        return readStart(reader, namespaceURI, localName, required, false);
    }


    public static boolean readStart(XMLStreamReader reader,
            String namespaceURI, String localName, boolean required,
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


    public static void readEnd(XMLStreamReader reader, String namespaceURI,
            String localName) throws XMLStreamException {
        readEnd(reader, namespaceURI, localName, false);
    }


    public static void readEnd(XMLStreamReader reader, String namespaceURI,
            String localName, boolean skipContent) throws XMLStreamException {
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
                    if (!(namespaceURI.equals(reader.getNamespaceURI()) &&
                            localName.equals(reader.getLocalName()))) {
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


    public static String readContent(XMLStreamReader reader,
            String namespaceURI, String localName, boolean required)
            throws XMLStreamException {
        String result = null;
        if (readStart(reader, namespaceURI, localName, required)) {
            result = readString(reader, true);
            readEnd(reader, namespaceURI, localName);
        }
        return result;
    }


    public static int readContent(XMLStreamReader reader, String namespaceURI,
            String localName, boolean required, int defaultValue)
            throws XMLStreamException {
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


    public static String readString(XMLStreamReader reader, boolean required)
            throws XMLStreamException {
        // System.err.println("readString @ " + toReadable(reader));
        String s = null;
        StringBuilder sb = null;
        while (reader.isCharacters()) {
            if (sb == null) {
                sb = new StringBuilder();
            }
            String tmp = reader.getText();
            if (!tmp.isEmpty()) {
                sb.append(tmp);
            }
            reader.next();
        } // while
        if ((sb != null) && (sb.length() > 0)) {
            s = sb.toString().trim();
        }
        if (required && ((s == null) || s.isEmpty())) {
            throw new XMLStreamException("expected character content "
                    + "at position", reader.getLocation());
        }
        // System.err.println("--> ok @ " + toReadable(reader));
        return s;
    }


    public static String readAttributeValue(XMLStreamReader reader,
            String namespaceURI, String localName, boolean required)
            throws XMLStreamException {
        if (!reader.isStartElement()) {
            throw new XMLStreamException("not at a start elment event",
                    reader.getLocation());
        }
        String attr = reader.getAttributeValue(namespaceURI, localName);
        if (attr != null) {
            attr = attr.trim();
            if (attr.isEmpty()) {
                attr = null;
            }
        }
        if ((attr == null) && required) {
            throw new XMLStreamException("expected non-empty attribute '" +
                    new QName(namespaceURI, localName) + "' on element '" +
                    reader.getName() + "'", reader.getLocation());
        }
        return attr;
    }


    public static String readAttributeValue(XMLStreamReader reader,
            String namespaceURI, String localName) throws XMLStreamException {
        return readAttributeValue(reader, namespaceURI, localName, false);
    }


    public static String readNamespaceURI(XMLStreamReader reader)
            throws XMLStreamException {
        if (!reader.isStartElement()) {
            throw new XMLStreamException("not at a start elment event",
                    reader.getLocation());
        }
        return reader.getNamespaceURI();
    }


    public static boolean peekStart(XMLStreamReader reader,
            String namespaceURI, String localName)
            throws XMLStreamException {
        if (reader.isWhiteSpace()) {
            consumeWhitespace(reader);
        }
        if (!reader.isStartElement()) {
            return false;
        }
        return namespaceURI.equals(reader.getNamespaceURI()) &&
                localName.equals(reader.getLocalName());
    }


    public static boolean peekEnd(XMLStreamReader reader,
            String namespaceURI, String localName)
            throws XMLStreamException {
        if (reader.isWhiteSpace()) {
            consumeWhitespace(reader);
        }
        if (!reader.isEndElement()) {
            return false;
        }
        return namespaceURI.equals(reader.getNamespaceURI()) &&
                localName.equals(reader.getLocalName());
    }


    public static void consumeStart(XMLStreamReader reader)
            throws XMLStreamException {
        if (!reader.isStartElement()) {
            throw new XMLStreamException("not at a start element event",
                    reader.getLocation());
        }
        reader.next();
    }


    public static void consumeWhitespace(XMLStreamReader reader)
            throws XMLStreamException {
        while (reader.isWhiteSpace() && reader.hasNext()) {
            reader.next();
            continue;
        }
    }

} // class XmlStreamReaderUtils

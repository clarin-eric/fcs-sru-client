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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * A client to perform SRU operations. The response of a SRU request is wrapped
 * in a SRU response.
 * <p>
 * This client is reusable but not thread-safe: the application may reuse a
 * client object, but it may not be concurrently shared between multiple
 * threads.
 * </p>
 */
public class SRUClient {
    private static final Logger logger = LoggerFactory.getLogger(SRUClient.class);
    private final SRUSimpleClient client;
    private final Handler handler;
    /* common */
    private List<SRUDiagnostic> diagnostics;
    private List<SRUExtraResponseData> extraResponseData;
    /* scan */
    private List<SRUTerm> terms;
    /* searchRetrieve */
    private int numberOfRecords;
    private String resultSetId;
    private int resultSetIdleTime;
    private int nextRecordPosition;
    /* explain/searchRetrieve */
    private List<SRURecord> records;
    /* statistics */
    private int totalBytesTransferred;
    private long timeTotal;
    private long timeQueued;
    private long timeNetwork;
    private long timeParsing;
    /* other fields */
    private final DocumentBuilder documentBuilder;
    private final Deque<Node> stack = new ArrayDeque<Node>();


    /**
     * Constructor.
     *
     * @param config
     *            the configuration to be used for this client.
     * @throws NullPointerException
     *             if argument <code>config</code> is <node>null</code>
     * @throws IllegalArgumentException
     *             if an error occurred while registering record data parsers
     * @see SRUClientConfig
     */
    public SRUClient(final SRUClientConfig config) {
        this(config, DocumentBuilderFactory.newInstance());
    }


    /**
     * Constructor.
     *
     * <p>
     * For internal use only.
     * </p>
     *
     * @param defaultVersion
     *            the default version to use for SRU requests; may be overridden
     *            by individual requests
     * @param parsers
     *            a <code>Map</code> to store record schema to record data
     *            parser mappings
     * @param documentBuilderFactory
     *            the Document Builder factory to be used to create Document
     *            Builders
     */
    SRUClient(final SRUClientConfig config,
            final DocumentBuilderFactory documentBuilderFactory) {
        if (config == null) {
            throw new NullPointerException("config == null");
        }
        if (documentBuilderFactory == null) {
            throw new NullPointerException("documentBuilderFactory == null");
        }
        this.client = new SRUSimpleClient(config);
        this.handler = new Handler(config);
        try {
            synchronized (documentBuilderFactory) {
                documentBuilderFactory.setNamespaceAware(true);
                documentBuilderFactory.setCoalescing(true);
                this.documentBuilder =
                        documentBuilderFactory.newDocumentBuilder();
            } // documentBuilderFactory (documentBuilderFactory)
        } catch (ParserConfigurationException e) {
            throw new Error("error initialzing document builder factory", e);
        }
        reset();
    }


    /**
     * Perform a <em>explain</em> operation.
     *
     * @param request
     *            an instance of a {@link SRUExplainRequest} object
     * @return a {@link SRUExplainResponse} object
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     */
    public SRUExplainResponse explain(SRUExplainRequest request)
            throws SRUClientException {
        if (request == null) {
            throw new NullPointerException("request == null");
        }
        try {
            client.explain(request, handler);
            SRURecord record = null;
            if ((records != null) && !records.isEmpty()) {
                record = records.get(0);
            }
            return new SRUExplainResponse(request,
                    diagnostics,
                    extraResponseData,
                    totalBytesTransferred,
                    timeTotal,
                    timeQueued,
                    timeNetwork,
                    timeParsing,
                    record);
        } finally {
            reset();
        }
    }


    /**
     * Perform a <em>scan</em> operation.
     *
     * @param request
     *            an instance of a {@link SRUScanRequest} object
     * @return a {@link SRUScanResponse} object
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     */
    public SRUScanResponse scan(SRUScanRequest request)
            throws SRUClientException {
        if (request == null) {
            throw new NullPointerException("request == null");
        }
        try {
            client.scan(request, handler);
            return new SRUScanResponse(request,
                    diagnostics,
                    extraResponseData,
                    totalBytesTransferred,
                    timeTotal,
                    timeQueued,
                    timeNetwork,
                    timeParsing,
                    terms);
        } finally {
            reset();
        }

    }


    /**
     * Perform a <em>searchRetrieve</em> operation.
     *
     * @param request
     *            an instance of a {@link SRUSearchRetrieveRequest} object
     * @return a {@link SRUSearchRetrieveRequest} object
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     */
    public SRUSearchRetrieveResponse searchRetrieve(
            SRUSearchRetrieveRequest request) throws SRUClientException {
        if (request == null) {
            throw new NullPointerException("request == null");
        }
        try {
            client.searchRetrieve(request, handler);
            return new SRUSearchRetrieveResponse(request,
                    diagnostics,
                    extraResponseData,
                    totalBytesTransferred,
                    timeTotal,
                    timeQueued,
                    timeNetwork,
                    timeParsing,
                    numberOfRecords,
                    resultSetId,
                    resultSetIdleTime,
                    records,
                    nextRecordPosition);
        } finally {
            reset();
        }
    }


    void setTimeQueued(long timeQueued) {
        this.timeQueued = TimeUnit.NANOSECONDS.toMillis(timeQueued);
    }


    private void addTerm(SRUTerm term) {
        if (terms == null) {
            terms = new LinkedList<SRUTerm>();
        }
        terms.add(term);
    }


    private void addRecord(SRURecord record) {
        if (records == null) {
            records = new LinkedList<SRURecord>();
        }
        records.add(record);
    }


    private void reset() {
        /* common */
        diagnostics           = null;
        extraResponseData     = null;
        /* scan */
        terms                 = null;
        /* searchRetrieve */
        numberOfRecords       = -1;
        resultSetId           = null;
        resultSetIdleTime     = -1;
        nextRecordPosition    = -1;
        /* explain/searchRetrieve */
        records               = null;
        /* statistics */
        totalBytesTransferred = -1;
        timeQueued            = -1;
        timeTotal             = -1;
        timeNetwork           = -1;
        timeParsing           = -1;
    }



    private class Handler extends SRUDefaultHandlerAdapter {
        private final List<SRUExtraResponseDataParser> parsers;


        private Handler(SRUClientConfig config) {
            this.parsers = config.getExtraResponseDataParsers();
        }


        @Override
        public void onDiagnostics(List<SRUDiagnostic> diagnostics)
                throws SRUClientException {
            SRUClient.this.diagnostics = diagnostics;
        }


        @Override
        public void onExtraResponseData(XMLStreamReader reader)
                throws XMLStreamException, SRUClientException {
            if (reader.getEventType() == XMLStreamConstants.START_DOCUMENT) {
                reader.next();
            }

            while (reader.hasNext()) {
                SRUExtraResponseData data = null;

                XmlStreamReaderUtils.consumeWhitespace(reader);
                switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    final QName root = reader.getName();
                    logger.debug("@start: {}", root);

                    /*
                     * The content model of "extraResponseData" is a sequence
                     * of elements. Parse each child element into a separate
                     * entity, i.e. if a parse is available for handling the
                     * element use it, otherwise parse into a document fragment.
                     */
                    if ((parsers != null) && !parsers.isEmpty()) {
                        for (SRUExtraResponseDataParser parser: parsers) {
                            if (parser.supports(root)) {
                                logger.debug("parsing extra response data with parser '{}'",
                                        parser.getClass().getName());
                                data = parser.parse(reader);
                                break;
                            }
                        }
                    }
                    if (data == null) {
                        logger.debug("parsing of extra response data (generic)");
                        data = parseExtraResponseAsDocumentFragment(
                                root, documentBuilder, stack, reader);
                    }
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    break;
                default:
                  throw new SRUClientException("expected a start element at " +
                          "this location (event code = " +
                          reader.getEventType() + ")");
                }

                if (data != null) {
                    if (SRUClient.this.extraResponseData == null) {
                        SRUClient.this.extraResponseData =
                                new ArrayList<SRUExtraResponseData>();
                    }
                    SRUClient.this.extraResponseData.add(data);
                }
            } // while

        }


        @Override
        public void onTerm(String value, int numberOfRecords,
                String displayTerm, SRUWhereInList whereInList)
                throws SRUClientException {
            SRUClient.this.addTerm(new SRUTerm(value, numberOfRecords,
                    displayTerm, whereInList));
        }


        @Override
        public void onExtraTermData(String value, XMLStreamReader reader)
                throws XMLStreamException, SRUClientException {
            final List<SRUTerm> terms = SRUClient.this.terms;
            if ((terms != null) && !terms.isEmpty()) {
                SRUTerm term = terms.get(terms.size() - 1);
                term.setExtraTermData(copyStaxToDocumentFragment(
                        documentBuilder, stack, reader));
            } else {
                /*
                 * should never happen ...
                 */
                throw new SRUClientException(
                        "internal error; 'terms' is null or empty");
            }
        }


        @Override
        public void onStartRecords(int numberOfRecords, String resultSetId,
                int resultSetIdleTime) throws SRUClientException {
            SRUClient.this.resultSetId = resultSetId;
            SRUClient.this.resultSetIdleTime = resultSetIdleTime;
        }


        @Override
        public void onFinishRecords(int nextRecordPosition)
                throws SRUClientException {
            SRUClient.this.nextRecordPosition = nextRecordPosition;
        }


        @Override
        public void onRecord(String identifier, int position,
                SRURecordData data) throws SRUClientException {
            SRUClient.this.addRecord(
                    new SRURecord(data, identifier, position));
        }


        @Override
        public void onSurrogateRecord(String identifier, int position,
                SRUDiagnostic data) throws SRUClientException {
            SRUClient.this.addRecord(new SRURecord(
                    new SRUSurrogateRecordData(data), identifier, position));
        }


        @Override
        public void onExtraRecordData(String identifier, int position,
                XMLStreamReader reader) throws XMLStreamException,
                SRUClientException {
            final List<SRURecord> records = SRUClient.this.records;
            if ((records != null) && !records.isEmpty()) {
                final SRURecord record = records.get(records.size() - 1);
                record.setExtraRecordData(copyStaxToDocumentFragment(
                        documentBuilder, stack, reader));
            } else {
                /*
                 * should never happen ...
                 */
                throw new SRUClientException(
                        "internal error; 'records' are null or empty");
            }
        }


        @Override
        public void onRequestStatistics(int totalBytesTransferred,
                long millisTotal, long millisNetwork, long millisProcessing) {
            SRUClient.this.totalBytesTransferred = totalBytesTransferred;
            if (SRUClient.this.timeQueued > 0) {
                SRUClient.this.timeTotal = timeQueued  + millisTotal;
            } else {
                SRUClient.this.timeTotal = millisTotal;
            }
            SRUClient.this.timeNetwork = millisNetwork;
            SRUClient.this.timeParsing = millisProcessing;
        }

    } // inner class Handler


    private static SRUExtraResponseData parseExtraResponseAsDocumentFragment(
            QName name, DocumentBuilder builder, Deque<Node> stack, XMLStreamReader reader)
            throws XMLStreamException {
        return new SRUGenericExtraResponseData(name,
                copyStaxToDocumentFragment(builder, stack, reader));
    }


    private static DocumentFragment copyStaxToDocumentFragment(
            DocumentBuilder builder, Deque<Node> stack, XMLStreamReader reader)
            throws XMLStreamException {
        try {
            final Document doc = builder.newDocument();
            stack.push(doc.createDocumentFragment());

            boolean stop = false;
            while (!stop && reader.hasNext()) {
                final Node parent = stack.peek();
                switch (reader.getEventType()) {
                case XMLStreamConstants.START_ELEMENT:
                    stack.push(createElementNode(parent, doc, reader));
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    stack.pop();
                    if (stack.size() == 1) {
                        stop = true;
                    }
                    break;
                case XMLStreamConstants.CHARACTERS:
                    parent.appendChild(doc.createTextNode(reader.getText()));
                    break;
                case XMLStreamConstants.COMMENT:
                    parent.appendChild(doc.createComment(reader.getText()));
                    break;
                case XMLStreamConstants.CDATA:
                    parent.appendChild(doc.createCDATASection(reader.getText()));
                    break;
                default:
                    break;
                }
                reader.next();
            } // while
            if (stack.size() != 1) {
                throw new XMLStreamException(
                        "internal error; stack should hold only one element");
            }
            return (DocumentFragment) stack.pop();
        } catch (DOMException e) {
            throw new XMLStreamException(
                    "error creating document fragment", e);
        }
    }


    private static Element createElementNode(Node parent, Document doc,
            XMLStreamReader reader) throws XMLStreamException, DOMException {
        Element element = doc.createElementNS(reader.getNamespaceURI(),
                reader.getLocalName());

        if ((reader.getPrefix() != null) && !reader.getPrefix().isEmpty()) {
            element.setPrefix(reader.getPrefix());
        }

        parent.appendChild(element);

        // add namespace declarations
        for (int i = 0; i < reader.getNamespaceCount(); i++) {
            final String uri    = reader.getNamespaceURI(i);
            final String prefix = reader.getNamespacePrefix(i);

            if ((prefix != null) && !prefix.isEmpty()) {
                element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                        XMLConstants.XMLNS_ATTRIBUTE + ":" + prefix,
                        uri);
            } else {
                if (uri != null) {
                    element.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                            XMLConstants.XMLNS_ATTRIBUTE,
                            uri);
                }
            }
        }

        // add other attributes
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name   = reader.getAttributeLocalName(i);
            String prefix = reader.getAttributePrefix(i);
            if (prefix != null && prefix.length() > 0) {
                name = prefix + ":" + name;
            }

            Attr attr = doc.createAttributeNS(
                    reader.getAttributeNamespace(i), name);
            attr.setValue(reader.getAttributeValue(i));
            element.setAttributeNode(attr);
        }
        return element;
    }

} // class SRUClient

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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A simple client to perform SRU operations using callbacks. The application
 * must provide the appropriate callbacks to receive the results of the
 * operations.
 * <p>
 * This client is reusable but not thread-safe: the application may reuse a
 * client object, but it may not be concurrently shared between multiple
 * threads.
 * </p>
 * <p>
 * This class is modeled after Java's SAX-API.
 * </p>
 *
 * @see SRUExplainHandler
 * @see SRUScanHandler
 * @see SRUSearchRetrieveHandler
 * @see SRUDefaultHandlerAdapter
 */
public class SRUSimpleClient {
    private static final String USER_AGENT = "SRU-Client/1.0.0";
    /** default version the client will use, if not otherwise specified */
//    private static final String SRU_NS =
//            "http://www.loc.gov/zing/srw/";
//    private static final String SRU_DIAGNOSIC_NS =
//            "http://www.loc.gov/zing/srw/diagnostic/";
    private static final String SRU_DIAGNOSTIC_RECORD_SCHEMA =
            "info:srw/schema/1/diagnostics-v1.1";
    private static final String VERSION_1_1 = "1.1";
    private static final String VERSION_1_2 = "1.2";
    private static final String VERSION_2_0 = "2.0";
    private static final String RECORD_PACKING_PACKED = "packed";
    private static final String RECORD_PACKING_UNPACKED = "unpacked";
    private static final String RECORD_ESCAPING_XML = "xml";
    private static final String RECORD_ESCAPING_STRING = "string";
    private static final Logger logger =
            LoggerFactory.getLogger(SRUSimpleClient.class);
    private final SRUVersion defaultVersion;
    private final Map<String, SRURecordDataParser> parsers;
    private final CloseableHttpClient httpClient;
    private final XmlStreamReaderProxy proxy = new XmlStreamReaderProxy();
    private final SRUExplainRecordDataParser explainRecordParser =
            new SRUExplainRecordDataParser();


    /**
     * Constructor.
     *
     * @param config
     *            the configuration to be used for this client.
     * @throws NullPointerException
     *             if argument <code>config</code> is <code>null</code>
     * @throws IllegalArgumentException
     *             if an error occurred while registering record data parsers
     * @see SRUClientConfig
     */
    public SRUSimpleClient(final SRUClientConfig config) {
        if (config == null) {
            throw new NullPointerException("config == null");
        }
        this.defaultVersion = config.getDefaultVersion();

        // Initialize parsers lookup table ...
        final List<SRURecordDataParser> list = config.getRecordDataParsers();
        if ((list == null) || list.isEmpty()) {
            throw new IllegalArgumentException(
                    "no record data parsers registered");
        }
        this.parsers = new HashMap<String, SRURecordDataParser>();
        for (SRURecordDataParser parser : list) {
            final String recordSchema = parser.getRecordSchema();
            if (!parsers.containsKey(recordSchema)) {
                parsers.put(recordSchema, parser);
            } else {
                throw new IllegalArgumentException(
                        "record data parser already registered: " +
                                recordSchema);
            }
        }

        // create HTTP client
        httpClient = createHttpClient(config.getConnectTimeout(),
                                      config.getSocketTimeout());
    }


    /**
     * Perform a <em>explain</em> operation.
     *
     * @param request
     *            an instance of a {@link SRUExplainRequest} object
     * @param handler
     *            an instance of {@link SRUExplainHandler} to receive callbacks
     *            when processing the result of this request
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @see SRUExplainRequest
     * @see SRUExplainHandler
     */
    public void explain(SRUExplainRequest request, SRUExplainHandler handler)
            throws SRUClientException {
        if (request == null) {
            throw new NullPointerException("request == null");
        }
        if (handler == null) {
            throw new NullPointerException("handler == null");
        }
        logger.debug("performing explain request");

        final long ts_start = System.nanoTime();

        // create URI and perform request
        final URI uri = request.makeURI(defaultVersion);
        CloseableHttpResponse response = executeRequest(uri);
        InputStream stream             = null;
        SRUXMLStreamReader reader      = null;
        try {
            final HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new SRUClientException("cannot get entity");
            }

            stream = entity.getContent();

            final long ts_parsing = System.nanoTime();
            reader = createReader(stream, true);
            parseExplainResponse(reader, request, handler);
            final long ts_end = System.nanoTime();

            final long millisTotal =
                    TimeUnit.NANOSECONDS.toMillis(ts_end - ts_start);
            final long millisNetwork =
                    TimeUnit.NANOSECONDS.toMillis(ts_parsing - ts_start);
            final long millisProcessing =
                    TimeUnit.NANOSECONDS.toMillis(ts_end - ts_parsing);
            logger.debug("{} byte(s) in {} milli(s) " +
                    "({} milli(s) network / {} milli(s) processing)",
                    reader.getByteCount(), millisTotal, millisNetwork,
                    millisProcessing);
            handler.onRequestStatistics((int) reader.getByteCount(),
                    millisTotal, millisNetwork, millisProcessing);
        } catch (IllegalStateException e) {
            throw new SRUClientException("error reading response", e);
        } catch (IOException e) {
            throw new SRUClientException("error reading response", e);
        } catch (XMLStreamException e) {
            throw new SRUClientException("error reading response", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    /* IGNORE */
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    /* IGNORE */
                }
            }

            /* make sure to release allocated resources */
            try {
                response.close();
            } catch (IOException e) {
                /* IGNORE */
            }
        }
    }


    /**
     * Perform a <em>scan</em> operation.
     *
     * @param request
     *            an instance of a {@link SRUScanRequest} object
     * @param handler
     *            an instance of {@link SRUScanHandler} to receive callbacks
     *            when processing the result of this request
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @see SRUScanRequest
     * @see SRUScanHandler
     */
    public void scan(SRUScanRequest request, SRUScanHandler handler)
            throws SRUClientException {
        if (request == null) {
            throw new NullPointerException("request == null");
        }
        if (handler == null) {
            throw new NullPointerException("handler == null");
        }
        logger.debug("performing scan request: scanClause = {}",
                request.getScanClause());

        final long ts_start = System.nanoTime();

        // create URI and perform request
        final URI uri = request.makeURI(defaultVersion);
        CloseableHttpResponse response = executeRequest(uri);
        InputStream stream             = null;
        SRUXMLStreamReader reader      = null;
        try {
            final HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new SRUClientException("cannot get entity");
            }
            stream = entity.getContent();

            final long ts_parsing = System.nanoTime();
            reader = createReader(stream, true);
            parseScanResponse(reader, request, handler);
            final long ts_end = System.nanoTime();

            final long millisTotal =
                    TimeUnit.NANOSECONDS.toMillis(ts_end - ts_start);
            final long millisNetwork =
                    TimeUnit.NANOSECONDS.toMillis(ts_parsing - ts_start);
            final long millisProcessing =
                    TimeUnit.NANOSECONDS.toMillis(ts_end - ts_parsing);
            logger.debug("{} byte(s) in {} milli(s) " +
                    "({} milli(s) network / {} milli(s) processing)",
                    reader.getByteCount(), millisTotal, millisNetwork,
                    millisProcessing);
            handler.onRequestStatistics((int) reader.getByteCount(),
                    millisTotal, millisNetwork, millisProcessing);
        } catch (IllegalStateException e) {
            throw new SRUClientException("error reading response", e);
        } catch (IOException e) {
            throw new SRUClientException("error reading response", e);
        } catch (XMLStreamException e) {
            throw new SRUClientException("error reading response", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    /* IGNORE */
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    /* IGNORE */
                }
            }

            /* make sure to release allocated resources */
            try {
                response.close();
            } catch (IOException e) {
                /* IGNORE */
            }
        }
    }


    /**
     * Perform a <em>searchRetrieve</em> operation.
     *
     * @param request
     *            an instance of a {@link SRUSearchRetrieveRequest} object
     * @param handler
     *            an instance of {@link SRUSearchRetrieveHandler} to receive
     *            callbacks when processing the result of this request
     * @throws SRUClientException
     *             if an unrecoverable error occurred
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @see SRUSearchRetrieveRequest
     * @see SRUSearchRetrieveHandler
     */
    public void searchRetrieve(SRUSearchRetrieveRequest request,
            SRUSearchRetrieveHandler handler) throws SRUClientException {
        if (request == null) {
            throw new NullPointerException("request == null");
        }
        if (handler == null) {
            throw new NullPointerException("handler == null");
        }
        logger.debug("performing searchRetrieve request: query = {}",
                request.getQuery());

        final long ts_start = System.nanoTime();

        // create URI and perform request
        final URI uri = request.makeURI(defaultVersion);
        CloseableHttpResponse response = executeRequest(uri);
        InputStream stream             = null;
        SRUXMLStreamReader reader      = null;
        try {
            final HttpEntity entity = response.getEntity();
            if (entity == null) {
                throw new SRUClientException("cannot get entity");
            }

            stream = entity.getContent();

            final long ts_parsing = System.nanoTime();
            reader = createReader(stream, true);
            parseSearchRetrieveResponse(reader, request, handler);
            final long ts_end = System.nanoTime();

            final long millisTotal =
                    TimeUnit.NANOSECONDS.toMillis(ts_end - ts_start);
            final long millisNetwork =
                    TimeUnit.NANOSECONDS.toMillis(ts_parsing - ts_start);
            final long millisProcessing =
                    TimeUnit.NANOSECONDS.toMillis(ts_end - ts_parsing);
            logger.debug("{} byte(s) in {} milli(s) " +
                    "({} milli(s) network / {} milli(s) processing)",
                    reader.getByteCount(), millisTotal, millisNetwork,
                    millisProcessing);
            handler.onRequestStatistics((int) reader.getByteCount(),
                    millisTotal, millisNetwork, millisProcessing);
        } catch (IllegalStateException e) {
            throw new SRUClientException("error reading response", e);
        } catch (IOException e) {
            throw new SRUClientException("error reading response", e);
        } catch (XMLStreamException e) {
            throw new SRUClientException("error reading response", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (XMLStreamException e) {
                    /* IGNORE */
                }
            }
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    /* IGNORE */
                }
            }

            /* make sure to release allocated resources */
            try {
                response.close();
            } catch (IOException e) {
                /* IGNORE */
            }
        }
    }


    private CloseableHttpResponse executeRequest(URI uri)
            throws SRUClientException {
        CloseableHttpResponse response = null;
        boolean forceClose             = true;
        try {
            logger.debug("submitting HTTP request: {}", uri.toString());
            try {
                HttpGet request = new HttpGet(uri);
                response        = httpClient.execute(request);
                StatusLine status = response.getStatusLine();
                if (status.getStatusCode() != HttpStatus.SC_OK) {
                    if (status.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                        throw new SRUClientException("not found: " + uri);
                    } else {
                        throw new SRUClientException("unexpected status: " +
                                status.getStatusCode());
                    }
                }
                forceClose = false;
                return response;
            } catch (ClientProtocolException e) {
                throw new SRUClientException("client protocol exception", e);
            } catch (UnknownHostException e) {
                throw new SRUClientException(
                        "unknown host: " + uri.getHost(), e);
            } catch (IOException e) {
                String msg = null;
                if ((e.getMessage() != null) && !e.getMessage().isEmpty()) {
                    msg = e.getMessage();
                }
                throw new SRUClientException(msg != null
                        ? msg
                        : "input/output error", e);
            }
        } catch (SRUClientException e) {
            /*
             * if an error occurred, make sure we are freeing up the resources
             * we've used
             */
            if (forceClose && (response != null)) {
                try {
                    response.close();
                } catch (IOException ex) {
                    /* IGNORE */
                }
            }
            throw e;
        }
    }


    private void parseExplainResponse(final SRUXMLStreamReader reader,
            final SRUExplainRequest request, final SRUExplainHandler handler)
            throws SRUClientException {
        logger.debug("parsing 'explain' response (mode = {})",
                (request.isStrictMode() ? "strict" : "non-strict"));

        /* detect response namespaces */
        final SRUNamespaces ns =
                detectNamespace(reader, request.getRequestedVersion());

        /*
         * Eventually, SRUClient should always parse explain record data.
         * However, for now, make caller explicitly ask for it.
         */
        final boolean parse = request.isParseRecordDataEnabled();
        if (!parse) {
            logger.debug("parsing of explain record data skipped");
        }
        doParseExplainResponse(reader, ns, request, handler, parse);
    }


    private void doParseExplainResponse(SRUXMLStreamReader reader,
            SRUNamespaces ns, SRUAbstractRequest request,
            SRUExplainHandler handler, boolean parseRecordData)
                    throws SRUClientException {
        try {
            final boolean strictMode = request.isStrictMode();

            // explainResponse
            reader.readStart(ns.sruNS(), "explainResponse", true);

            // explainResponse/version
            final SRUVersion version = parseVersion(reader, ns.sruNS());
            logger.debug("version = {}, requested = {}", version,
                    request.getRequestedVersion());

            // explainResponse/record
            reader.readStart(ns.sruNS(), "record", true);
            if (parseRecordData) {
                handler.onStartRecords(-1, null, -1);

                /*
                 * common error: recordEscaping / recordPacking (SRU 1.2) in
                 * wrong order
                 */
                SRURecordXmlEscaping recordXmlEscaping = null;
                if (!strictMode && reader.peekStart(ns.sruNS(), "recordPacking")) {
                    recordXmlEscaping = parseRecordXmlEscaping(reader, ns,
                            request.getRequestedVersion(), false);
                    if (recordXmlEscaping != null) {
                        logger.error("element <recordPacking> must apperear " +
                                "after element <recordSchema> within " +
                                "element <record>");
                    }
                }

                String schema =
                        reader.readContent(ns.sruNS(), "recordSchema", true);

                // (SRU 2.0) recordPacking (optional)
                // XXX: what to do with it?
                SRURecordPacking recordPacking = null;
                if (version == SRUVersion.VERSION_2_0) {
                    recordPacking = parseRecordPacking(reader,
                            ns.sruNS(), strictMode);
                }

                if (recordXmlEscaping == null) {
                    recordXmlEscaping = parseRecordXmlEscaping(reader, ns,
                            request.getRequestedVersion(), strictMode);
                }
                logger.debug("schema = {}, escaping = {}, packing = {}",
                        schema, recordXmlEscaping, recordPacking);

                // explainResponse/record/recordData
                reader.readStart(ns.sruNS(), "recordData", true);
                reader.consumeWhitespace();

                SRURecordData recordData = null;
                SRUXMLStreamReader recordReader = null;

                if (recordXmlEscaping == SRURecordXmlEscaping.STRING) {
                    /*
                     * read content into temporary buffer and then use a new XML
                     * reader to parse record data
                     */
                    final String data = reader.readString(true);
                    InputStream in = new ByteArrayInputStream(data.getBytes());
                    // FIXME: namespace context?
                    recordReader = createReader(in, false);
                } else {
                    recordReader = reader;
                }

                try {
                    proxy.reset(recordReader);
                    /*
                     * Try to parse explain record data with explain record
                     * parser. It will throw an exception, if it cannot handle
                     * the data.
                     */
                    recordData = explainRecordParser.parse(proxy,
                            version, strictMode, schema);
                } catch (XMLStreamException e) {
                    throw new SRUClientException(
                            "error parsing explain record", e);
                } finally {
                    /*
                     * make sure, we're deallocate the record reader in case of
                     * string record packing
                     */
                    if (recordXmlEscaping == SRURecordXmlEscaping.STRING) {
                        recordReader.closeCompletly();
                    }
                }
                if (recordData == null) {
                    throw new SRUClientException("no record data could " +
                            "be extracted from the response");
                }
                reader.consumeWhitespace();
                reader.readEnd(ns.sruNS(), "recordData", true);

                if (version == SRUVersion.VERSION_1_2) {
                    reader.readContent(ns.sruNS(), "recordIdentifier", false);
                }
                reader.readContent(ns.sruNS(), "recordPosition", false, -1);

                // notify handler
                handler.onRecord(null, -1, recordData);

                if (reader.readStart(ns.sruNS(), "extraRecordData", false)) {
                    reader.consumeWhitespace();
                    proxy.reset(reader);
                    try {
                        logger.debug("parsing extra response data");
                        handler.onExtraRecordData(null, -1, proxy);
                    } catch (XMLStreamException e) {
                        throw new SRUClientException("handler "
                                + "triggered error while parsing "
                                + "'extraRecordData'", e);
                    }
                    reader.consumeWhitespace();
                    reader.readEnd(ns.sruNS(), "extraRecordData", true);
                }

                handler.onFinishRecords(-1);

                reader.readEnd(ns.sruNS(), "record");
            } else {
                /*
                 * do not really parse record and skip everything
                 * until <record> end tag
                 */
                reader.readEnd(ns.sruNS(), "record", true);
            }

            // explainResponse/echoedExplainRequest
            if (reader.readStart(ns.sruNS(), "echoedExplainRequest", false)) {
                reader.readEnd(ns.sruNS(), "echoedExplainRequest", true);
            }

            /*
             * common error: echoedExplainRequest in default namespace
             */
            if (reader.readStart("", "echoedExplainRequest", false)) {
                logger.error("Element 'echoedExplainRequest' must be in SRU " +
                        "namespace, but endpoint put it into default namespace");
                if (strictMode) {
                    throw new SRUClientException("Element " +
                            "'echoedExplainRequest' must be in SRU namespace,"+
                            " but endpoint put it into default namespace");
                }
                reader.readEnd("", "echoedExplainRequest", true);
            }

            // explainResponse/diagnostics
            final List<SRUDiagnostic> diagnostics =
                    parseDiagnostics(reader, ns, ns.scanNS(), strictMode);
            if (diagnostics != null) {
                handler.onDiagnostics(diagnostics);
            }

            // explainResponse/extraResponseData
            if (reader.readStart(ns.sruNS(), "extraResponseData", false)) {
                reader.consumeWhitespace();
                proxy.reset(reader);
                try {
                    logger.debug("parsing extra response data");
                    handler.onExtraResponseData(proxy);
                } catch (XMLStreamException e) {
                    throw new SRUClientException("handler triggered "
                            + "error while parsing 'extraResponseData'", e);
                }
                reader.consumeWhitespace();
                reader.readEnd(ns.sruNS(), "extraResponseData", true);
            }

            reader.readEnd(ns.sruNS(), "explainResponse");
        } catch (XMLStreamException e) {
            throw new SRUClientException(e.getMessage(), e);
        }
    }


    private void parseScanResponse(final SRUXMLStreamReader reader,
            final SRUScanRequest request, final SRUScanHandler handler)
            throws SRUClientException {
        try {
            /* detect response namespaces */
            final SRUNamespaces ns =
                    detectNamespace(reader, request.getRequestedVersion());

            /*
             * if the endpoint cannot determine the operation, it should create
             * a explain response.
             */
            if (reader.peekStart(ns.sruNS(), "explainResponse")) {
                doParseExplainResponse(reader, ns, request, new SRUExplainHandler() {
                    @Override
                    public void onRequestStatistics(int bytes, long millisTotal,
                            long millisNetwork, long millisParsing) {
                    }


                    @Override
                    public void onExtraResponseData(XMLStreamReader reader)
                            throws XMLStreamException, SRUClientException {
                    }


                    @Override
                    public void onDiagnostics(List<SRUDiagnostic> diagnostics)
                            throws SRUClientException {
                        handler.onDiagnostics(diagnostics);
                    }


                    @Override
                    public void onStartRecords(int numberOfRecords,
                            String resultSetId, int resultSetIdleTime)
                            throws SRUClientException {
                    }


                    @Override
                    public void onFinishRecords(int nextRecordPosition)
                            throws SRUClientException {
                    }


                    @Override
                    public void onRecord(String identifier, int position,
                            SRURecordData data) throws SRUClientException {
                    }


                    @Override
                    public void onExtraRecordData(String identifier,
                            int position, XMLStreamReader reader)
                            throws XMLStreamException, SRUClientException {
                    }
                }, false);
            } else {
                final boolean strictMode = request.isStrictMode();

                logger.debug("parsing 'scan' response (mode = {})",
                        (strictMode ? "strict" : "non-strict"));

                // scanResponse
                reader.readStart(ns.scanNS(), "scanResponse", true);

                // scanResponse/version
                final SRUVersion version = parseVersion(reader, ns.scanNS());
                logger.debug("version = {}, requested = {}", version,
                        request.getRequestedVersion());

                // scanResponse/terms
                if (reader.readStart(ns.scanNS(), "terms", false)) {
                    boolean first = true;
                    while (reader.readStart(ns.scanNS(), "term", first)) {
                        if (first) {
                            first = false;
                            handler.onStartTerms();
                        }

                        // scanResponse/terms/value
                        String value =
                                reader.readContent(ns.scanNS(), "value", true);

                        // scanResponse/terms/numberOfRecords
                        int numberOfRecords = reader.readContent(ns.scanNS(),
                                "numberOfRecords", false, -1);

                        // scanResponse/terms/displayTerm
                        String displayTerm = reader.readContent(ns.scanNS(),
                                "displayTerm", false);

                        // scanResponse/terms/whereInList
                        String s = reader.readContent(ns.scanNS(),
                                "whereInList", false);
                        SRUWhereInList whereInList = null;
                        if (s != null) {
                            if ("first".equals(s)) {
                                whereInList = SRUWhereInList.FIRST;
                            } else if ("last".equals(s)) {
                                whereInList = SRUWhereInList.LAST;
                            } else if ("only".equals(s)) {
                                whereInList = SRUWhereInList.ONLY;
                            } else if ("inner".equals(s)) {
                                whereInList = SRUWhereInList.INNER;
                            } else {
                                throw new SRUClientException(
                                        "invalid value for 'whereInList': " + s);
                            }
                        }
                        logger.debug("value = {}, numberOfRecords = {}, " +
                                "displayTerm = {}, whereInList = {}", value,
                                numberOfRecords, displayTerm, whereInList);
                        handler.onTerm(value, numberOfRecords, displayTerm,
                                whereInList);

                        // scanResponse/terms/extraTermData
                        if (reader.readStart(ns.scanNS(), "extraTermData", first)) {
                            reader.consumeWhitespace();
                            proxy.reset(reader);
                            try {
                                handler.onExtraTermData(value, proxy);
                            } catch (XMLStreamException e) {
                                throw new SRUClientException("handler "
                                        + "triggered error while parsing "
                                        + "'extraTermData'", e);
                            }
                            reader.consumeWhitespace();
                            reader.readEnd(ns.scanNS(), "extraTermData", true);
                        }
                        reader.readEnd(ns.scanNS(), "term", true);

                    } // while
                    reader.readEnd(ns.scanNS(), "terms");
                    handler.onFinishTerms();
                }

                // scanResponse/echoedScanRequest
                if (reader.readStart(ns.scanNS(), "echoedScanRequest", false)) {
                    reader.readEnd(ns.scanNS(), "echoedScanRequest", true);
                }

                /*
                 * common error: echoedScanRequest in default namespace
                 */
                if (reader.readStart("", "echoedScanRequest", false)) {
                    logger.error("Element 'echoedScanRequest' must be in SRU namespace, but endpoint put it into default namespace");
                    if (strictMode) {
                        throw new SRUClientException("Element 'echoedScanRequest' must be in SRU namespace, but endpoint put it into default namespace");
                    }
                    reader.readEnd("", "echoedScanRequest", true);
                }

                // scanResponse/diagnostics
                final List<SRUDiagnostic> diagnostics =
                        parseDiagnostics(reader, ns, ns.scanNS(), strictMode);
                if (diagnostics != null) {
                    handler.onDiagnostics(diagnostics);
                }

                // scanResponse/extraResponseData
                if (reader.readStart(ns.scanNS(), "extraResponseData", false)) {
                    reader.consumeWhitespace();
                    proxy.reset(reader);
                    try {
                        logger.debug("parsing extra response data");
                        handler.onExtraResponseData(proxy);
                    } catch (XMLStreamException e) {
                        throw new SRUClientException("handler triggered "
                                + "error while parsing 'extraResponseData'", e);
                    }
                    reader.consumeWhitespace();
                    reader.readEnd(ns.scanNS(), "extraResponseData", true);
                }

                reader.readEnd(ns.scanNS(), "scanResponse");
            }
        } catch (XMLStreamException e) {
            throw new SRUClientException(e.getMessage(), e);
        }
    }


    private void parseSearchRetrieveResponse(final SRUXMLStreamReader reader,
            final SRUSearchRetrieveRequest request,
            final SRUSearchRetrieveHandler handler) throws SRUClientException {
        try {
            /* detect response namespaces */
            final SRUNamespaces ns =
                    detectNamespace(reader, request.getRequestedVersion());

            /*
             * if the endpoint cannot determine the operation, it should create
             * a explain response.
             */
            if (reader.peekStart(ns.sruNS(), "explainResponse")) {
                doParseExplainResponse(reader, ns, request, new SRUExplainHandler() {
                    @Override
                    public void onRequestStatistics(int bytes, long millisTotal,
                            long millisNetwork, long millisParsing) {
                    }


                    @Override
                    public void onExtraResponseData(XMLStreamReader reader)
                            throws XMLStreamException, SRUClientException {
                    }


                    @Override
                    public void onDiagnostics(List<SRUDiagnostic> diagnostics)
                            throws SRUClientException {
                        handler.onDiagnostics(diagnostics);
                    }


                    @Override
                    public void onStartRecords(int numberOfRecords,
                            String resultSetId, int resultSetIdleTime)
                            throws SRUClientException {
                    }


                    @Override
                    public void onFinishRecords(int nextRecordPosition)
                            throws SRUClientException {
                    }


                    @Override
                    public void onRecord(String identifier, int position,
                            SRURecordData data) throws SRUClientException {
                    }


                    @Override
                    public void onExtraRecordData(String identifier,
                            int position, XMLStreamReader reader)
                            throws XMLStreamException, SRUClientException {
                    }
                }, false);
            } else {
                final boolean strictMode = request.isStrictMode();

                logger.debug("parsing 'searchRetrieve' response (mode = {})",
                        (strictMode ? "strict" : "non-strict"));

                // searchRetrieveResponse
                reader.readStart(ns.sruNS(), "searchRetrieveResponse", true);

                // searchRetrieveResponse/version
                final SRUVersion version = parseVersion(reader, ns.sruNS());
                logger.debug("version = {}, requested = {}", version,
                        request.getRequestedVersion());

                // searchRetrieveResponse/numberOfRecords
                int numberOfRecords = reader.readContent(ns.sruNS(),
                        "numberOfRecords", true, -1);

                // searchRetrieveResponse/resultSetId
                String resultSetId = reader.readContent(ns.sruNS(),
                        "resultSetId", false);

                // searchRetrieveResponse/resultSetIdleTime
                int resultSetIdleTime = reader.readContent(ns.sruNS(),
                        "resultSetIdleTime", false, -1);

                logger.debug("numberOfRecords = {}, resultSetId = {}, " +
                        "resultSetIdleTime = {}", numberOfRecords,
                        resultSetId, resultSetIdleTime);

                // searchRetrieveResponse/results
                if (numberOfRecords > 0) {
                    /*
                     * some endpoints set numberOfRecords but do not serialize
                     * any records, e.g if requested record schema is not
                     * supported
                     */
                    int recordCount = 0;
                    if (reader.readStart(ns.sruNS(), "records", false)) {
                        // searchRetrieveResponse/records/record
                        boolean first = true;
                        while (reader.readStart(ns.sruNS(), "record", first)) {
                            if (first) {
                                first = false;
                                handler.onStartRecords(numberOfRecords,
                                        resultSetId, resultSetIdleTime);
                            }

                            /*
                             * common error: recordEscaping / recordPacking
                             * (SRU 1.2) in wrong order
                             */
                            SRURecordXmlEscaping recordXmlEscaping = null;
                            if (!strictMode &&
                                    reader.peekStart(ns.sruNS(), "recordPacking")) {
                                recordXmlEscaping =
                                        parseRecordXmlEscaping(reader, ns,
                                                version, false);
                                if (recordXmlEscaping != null) {
                                    logger.error("element <recordPacking> " +
                                            "must appear after element " +
                                            "<recordSchema> within " +
                                            "element <record>");
                                }
                            }

                            String schema = reader.readContent(ns.sruNS(),
                                    "recordSchema", true);

                            // (SRU 2.0) recordPacking (optional)
                            // XXX: what to do with it?
                            SRURecordPacking recordPacking = null;
                            if (version == SRUVersion.VERSION_2_0) {
                                recordPacking = parseRecordPacking(reader,
                                        ns.sruNS(), strictMode);
                            }

                            if (recordXmlEscaping == null) {
                                recordXmlEscaping =
                                        parseRecordXmlEscaping(reader, ns,
                                                version, strictMode);
                            }

                            logger.debug("schema = {}, escpaing = {}, " +
                                    "packing = {}, requested escaping = {}, " +
                                    "requested packing = {}",
                                    schema, recordXmlEscaping, recordPacking,
                                    request.getRecordXmlEscaping(),
                                    request.getRecordPacking());

                            if ((request.getRecordXmlEscaping() != null) &&
                                    (recordXmlEscaping != request.getRecordXmlEscaping())) {
                                final SRURecordXmlEscaping p =
                                        request.getRecordXmlEscaping();
                                logger.error("requested '{}' record XML escaping, " +
                                        "but server responded with '{}' " +
                                        "record XML escaping",
                                        p.getStringValue(),
                                        recordXmlEscaping.getStringValue());
                                if (strictMode) {
                                    throw new SRUClientException("requested '" +
                                            p.getStringValue() +
                                            "' record packing, but server " +
                                            "responded with '" +
                                            recordXmlEscaping.getStringValue() +
                                            "' record packing");
                                }
                            }

                            // searchRetrieveResponse/record/recordData
                            reader.readStart(ns.sruNS(), "recordData", true);
                            reader.consumeWhitespace();

                            SRURecordData recordData = null;
                            SRUDiagnostic surrogate = null;
                            SRUXMLStreamReader recordReader = null;

                            if (recordXmlEscaping == SRURecordXmlEscaping.STRING) {
                                /*
                                 * read content into temporary buffer and then
                                 * use a new XML reader to parse record data
                                 */
                                final String data = reader.readString(true);
                                InputStream in = new ByteArrayInputStream(
                                        data.getBytes());
                                // FIXME: namespace context?
                                recordReader = createReader(in, false);
                            } else {
                                recordReader = reader;
                            }

                            if (SRU_DIAGNOSTIC_RECORD_SCHEMA.equals(schema)) {
                                surrogate = parseDiagnostic(recordReader, ns,
                                        true, strictMode);
                            } else {
                                SRURecordDataParser parser = findParser(schema);
                                if (parser != null) {
                                    try {
                                        proxy.reset(recordReader);
                                        recordData = parser.parse(proxy);
                                    } catch (XMLStreamException e) {
                                        throw new SRUClientException(
                                                "error parsing record", e);
                                    } finally {
                                        /*
                                         * make sure, we deallocate the record
                                         * reader in case of string record
                                         * packing
                                         */
                                        if (recordXmlEscaping == SRURecordXmlEscaping.STRING) {
                                            recordReader.closeCompletly();
                                        }
                                    }
                                    if (recordData == null) {
                                        logger.debug("record parser did not parse "
                                                + "record correctly and returned "
                                                + "null; injecting client side "
                                                + "surrogate diagnostic");
                                        surrogate = new SRUDiagnostic(
                                                SRUClientDiagnostics.DIAG_RECORD_PARSER_NULL,
                                                null,
                                                "Record parser for schema '" +
                                                        schema +
                                                        "' did not " +
                                                        "parse record correctly " +
                                                        "and errornously " +
                                                        "returned null.");
                                    }
                                } else {
                                    /*
                                     * no record parser found, inject a
                                     * surrogate diagnostic
                                     */
                                    logger.debug(
                                            "no record data parser found "
                                                    + "for schema '{}'; injecting client "
                                                    + "side surrogate diagnostic",
                                            schema);
                                    surrogate = new SRUDiagnostic(
                                            SRUClientDiagnostics.DIAG_NO_RECORD_PARSER,
                                            schema,
                                            "No record data parser for schema '" +
                                                    schema + "' found.");
                                }
                            }

                            reader.consumeWhitespace();
                            reader.readEnd(ns.sruNS(), "recordData", true);

                            String identifier = null;
                            if (version == SRUVersion.VERSION_1_2) {
                                identifier = reader.readContent(ns.sruNS(),
                                        "recordIdentifier", false);
                            }

                            int position = reader.readContent(ns.sruNS(),
                                    "recordPosition", false, -1);

                            logger.debug("recordIdentifier = {}, " +
                                    "recordPosition = {}",
                                    identifier, position);

                            // notify handler
                            if (surrogate != null) {
                                handler.onSurrogateRecord(identifier,
                                        position, surrogate);
                            } else {
                                if (recordData != null) {
                                    handler.onRecord(identifier,
                                            position, recordData);
                                }
                            }

                            if (reader.readStart(ns.sruNS(),
                                    "extraRecordData", false)) {
                                reader.consumeWhitespace();
                                proxy.reset(reader);
                                try {
                                    handler.onExtraRecordData(identifier,
                                            position, proxy);
                                } catch (XMLStreamException e) {
                                    throw new SRUClientException("handler " +
                                            "triggered error while parsing " +
                                            "'extraRecordData'", e);
                                }
                                reader.consumeWhitespace();
                                reader.readEnd(ns.sruNS(), "extraRecordData", true);
                            }

                            reader.readEnd(ns.sruNS(), "record");
                            recordCount++;
                        } // while
                        reader.readEnd(ns.sruNS(), "records");
                    }
                    if (recordCount == 0) {
                        logger.error("endpoint declared {} results, but response contained no <record> elements (behavior may violate SRU specification)", numberOfRecords);
                    } else if ((request.getMaximumRecords() != -1) && (recordCount > request.getMaximumRecords())) {
                        logger.error("endpoint did not honour 'maximumRecords' request parameter and responded with {} records instead of a maximum of {}", recordCount, request.getMaximumRecords());
                    }
                } else {
                    /*
                     * provide a better error format, if endpoints responds with
                     * an empty <records> element
                     */
                    if (reader.readStart(ns.sruNS(), "records", false)) {
                        int bad = 0;
                        while (reader.readStart(ns.sruNS(), "record", false)) {
                            bad++;
                            reader.readEnd(ns.sruNS(), "record", true);
                        }
                        reader.readEnd(ns.sruNS(), "records", true);
                        if (bad == 0) {
                            logger.error("endpoint declared 0 results, but " +
                                    "response contained an empty 'records' " +
                                    "element");
                            if (strictMode) {
                                throw new SRUClientException(
                                        "endpoint declared 0 results, but " +
                                        "response contained an empty " +
                                        "'records' element (behavior " +
                                        "violates SRU specification)");
                            }
                        } else {
                            logger.error("endpoint declared 0 results, but " +
                                    "response contained " + bad +
                                    " record(s)");
                            if (strictMode) {
                                throw new SRUClientException(
                                            "endpoint declared 0 results, " +
                                            "but response containted " + bad +
                                            " records (behavior may violate " +
                                            "SRU specification)");
                            }
                        }
                    }
                }

                int nextRecordPosition = reader.readContent(ns.sruNS(),
                        "nextRecordPosition", false, -1);
                logger.debug("nextRecordPosition = {}", nextRecordPosition);
                handler.onFinishRecords(nextRecordPosition);

                // searchRetrieveResponse/echoedSearchRetrieveResponse
                if (reader.readStart(ns.sruNS(),
                        "echoedSearchRetrieveRequest", false)) {
                    reader.readEnd(ns.sruNS(), "echoedSearchRetrieveRequest", true);
                }

                /*
                 * common error: echoedSearchRetrieveRequest in
                 * default namespace
                 */
                if (reader.readStart("", "echoedSearchRetrieveRequest", false)) {
                    logger.error("Element 'echoedSearchRetrieveRequest' " +
                            "must be in SRU namespace, but endpoint put it " +
                            "into default namespace");
                    if (strictMode) {
                        throw new SRUClientException(
                                "Element 'echoedSearchRetrieveRequest' must " +
                                "be in SRU namespace, but endpoint put it " +
                                "into default namespace");
                    }
                    reader.readEnd("", "echoedSearchRetrieveRequest", true);
                }

                // searchRetrieveResponse/diagnostics
                final List<SRUDiagnostic> diagnostics =
                        parseDiagnostics(reader, ns, ns.sruNS(), strictMode);
                if (diagnostics != null) {
                    handler.onDiagnostics(diagnostics);
                }

                // explainResponse/extraResponseData
                if (reader.readStart(ns.sruNS(), "extraResponseData", false)) {
                    reader.consumeWhitespace();
                    proxy.reset(reader);
                    try {
                        handler.onExtraResponseData(proxy);
                    } catch (XMLStreamException e) {
                        throw new SRUClientException("handler triggered "
                                + "error while parsing 'extraResponseData'", e);
                    }
                    reader.consumeWhitespace();
                    reader.readEnd(ns.sruNS(), "extraResponseData", true);
                }

                if (version == SRUVersion.VERSION_2_0) {
                    // SRU (2.0) arbitrary stuff
                    // SRU (2.0) resultSetTTL (replaces resultSetIdleTime)
                    // SRU (2.0) resultCountPrecision
                    if (reader.readStart(ns.sruNS(), "resultCountPrecision", false)) {
                        reader.readEnd(ns.sruNS(), "resultCountPrecision", true);
                    }
                    // SRU (2.0) facetedResults
                    // SRU (2.0) searchResultAnalysis
                }
                reader.readEnd(ns.sruNS(), "searchRetrieveResponse");
            }
        } catch (XMLStreamException e) {
            throw new SRUClientException(e.getMessage(), e);
        }
    }


    private static SRUVersion parseVersion(SRUXMLStreamReader reader,
            String envelopNs) throws XMLStreamException, SRUClientException {
        final String v = reader.readContent(envelopNs, "version", true);
        if (VERSION_1_1.equals(v)) {
            return SRUVersion.VERSION_1_1;
        } else if (VERSION_1_2.equals(v)) {
            return SRUVersion.VERSION_1_2;
        } else if (VERSION_2_0.equals(v)) {
            return SRUVersion.VERSION_2_0;
        } else {
            throw new SRUClientException("invalid value '" + v +
                    "' for version (valid values are: '" + VERSION_1_1 +
                    "' and '" + VERSION_1_2 + "')");
        }
    }


    private static List<SRUDiagnostic> parseDiagnostics(
            SRUXMLStreamReader reader, SRUNamespaces ns, String responseNs, boolean strictMode)
            throws XMLStreamException, SRUClientException {
        if (reader.readStart(responseNs, "diagnostics", false)) {
            List<SRUDiagnostic> diagnostics = null;

            SRUDiagnostic diagnostic = null;
            while ((diagnostic = parseDiagnostic(reader, ns,
                    (diagnostics == null), strictMode)) != null) {
                if (diagnostics == null) {
                    diagnostics = new ArrayList<SRUDiagnostic>();
                }
                diagnostics.add(diagnostic);
            } // while
            reader.readEnd(responseNs, "diagnostics");
            return diagnostics;
        } else {
            return null;
        }
    }


    private static SRUDiagnostic parseDiagnostic(SRUXMLStreamReader reader,
            SRUNamespaces ns, boolean required, boolean strictMode) throws XMLStreamException,
            SRUClientException {
        if (reader.readStart(ns.diagnosticNS(), "diagnostic", required)) {

            // diagnostic/uri
            String uri = reader.readContent(ns.diagnosticNS(), "uri", true);

            String details = null;
            String message = null;
            if (strictMode) {
                // diagnostic/details
                details = reader.readContent(ns.diagnosticNS(), "details",
                        false, true);

                // diagnostic/message
                message = reader.readContent(ns.diagnosticNS(), "message",
                        false, true);
            } else {
                /*
                 * common error: diagnostic/details and diagnostic/message may
                 * appear in any order
                 */
                if (reader.peekStart(ns.diagnosticNS(), "details")) {
                    details = reader.readContent(ns.diagnosticNS(), "details",
                            false, false);
                    message = reader.readContent(ns.diagnosticNS(), "message",
                            false, false);
                } else {
                    message = reader.readContent(ns.diagnosticNS(), "message",
                            false, false);
                    details = reader.readContent(ns.diagnosticNS(), "details",
                            false, false);
                    if ((message != null) && (details != null)) {
                        logger.error("element <message> and element " +
                                "<details> within element <diagnostic> " +
                                "appeared in wrong order");
                    }
                }
            }

            if ((details != null) && details.isEmpty()) {
                details = null;
                logger.debug("omitting empty element <details> " +
                        "within element <diagnostic>");
            }
            if ((message != null) && message.isEmpty()) {
                message = null;
                logger.debug("omitting empty element <message> " +
                        "within element <diagnostic>");
            }

            reader.readEnd(ns.diagnosticNS(), "diagnostic");

            logger.debug("diagnostic: uri={}, detail={}, message={}",
                    uri, details, message);
            return new SRUDiagnostic(uri, details, message);
        } else {
            return null;
        }
    }


    private static SRURecordPacking parseRecordPacking(
            SRUXMLStreamReader reader, String envelopNs, boolean strictMode)
                    throws XMLStreamException, SRUClientException {
        final String s = reader.readContent(envelopNs, "recordPacking", false);
        if (s != null) {
            if (RECORD_PACKING_PACKED.equals(s)) {
                return SRURecordPacking.PACKED;
            } else if (RECORD_PACKING_UNPACKED.equals(s)) {
                return SRURecordPacking.UNPACKED;
            } else if (!strictMode && RECORD_PACKING_PACKED.equalsIgnoreCase(s)) {
                logger.error("invalid value '{}' for '<recordPacking>', should be '{}'",
                             s, RECORD_PACKING_PACKED);
                return SRURecordPacking.PACKED;
            } else if (!strictMode && RECORD_PACKING_UNPACKED.equalsIgnoreCase(s)) {
                logger.error("invalid value '{}' for '<recordPacking>', should be '{}'",
                             s, RECORD_PACKING_UNPACKED);
                return SRURecordPacking.UNPACKED;
            } else {
                throw new SRUClientException("invalid value '" + s +
                        "' for '<recordPacking>' (valid values are: '" +
                        RECORD_PACKING_PACKED + "' and '" + RECORD_PACKING_UNPACKED +
                        "')");
            }
        }
        return null;
    }


    private static SRURecordXmlEscaping parseRecordXmlEscaping(
            SRUXMLStreamReader reader, SRUNamespaces ns, SRUVersion version, boolean strictMode)
            throws XMLStreamException, SRUClientException {

        final String name = (version == SRUVersion.VERSION_2_0)
                          ? "recordXMLEscaping" : "recordPacking";
        final String s = reader.readContent(ns.sruNS(), name, true);

        if (RECORD_ESCAPING_XML.equals(s)) {
            return SRURecordXmlEscaping.XML;
        } else if (RECORD_ESCAPING_STRING.equals(s)) {
            return SRURecordXmlEscaping.STRING;
        } else if (!strictMode && RECORD_ESCAPING_XML.equalsIgnoreCase(s)) {
            logger.error("invalid value '{}' for '<{}>', should be '{}'",
                         s, name, RECORD_ESCAPING_XML);
            return SRURecordXmlEscaping.XML;
        } else if (!strictMode && RECORD_ESCAPING_STRING.equalsIgnoreCase(s)) {
            logger.error("invalid value '{}' for '<{}>', should be '{}'",
                         s, name, RECORD_ESCAPING_STRING);
            return SRURecordXmlEscaping.STRING;
        } else {
            throw new SRUClientException("invalid value '" + s +
                    "' for '<" + name + ">' (valid values are: '" +
                    RECORD_ESCAPING_XML + "' and '" + RECORD_ESCAPING_STRING +
                    "')");
        }
    }


    private SRURecordDataParser findParser(String schema) {
        SRURecordDataParser parser = parsers.get(schema);
        if (parser == null) {
            parser = parsers.get(SRUClientConstants.RECORD_DATA_PARSER_SCHEMA_ANY);
        }
        return parser;
    }


    private static SRUXMLStreamReader createReader(InputStream in, boolean wrap)
            throws XMLStreamException {
        return new SRUXMLStreamReader(in, wrap);
    }


    private static CloseableHttpClient createHttpClient(int connectTimeout,
            int socketTimeout) {
        final PoolingHttpClientConnectionManager manager =
                new PoolingHttpClientConnectionManager();
        manager.setDefaultMaxPerRoute(8);
        manager.setMaxTotal(128);

        final SocketConfig socketConfig = SocketConfig.custom()
                .setSoReuseAddress(true)
                .setSoLinger(0)
                .build();

        final RequestConfig requestConfig = RequestConfig.custom()
                .setAuthenticationEnabled(false)
                .setRedirectsEnabled(true)
                .setMaxRedirects(4)
                .setCircularRedirectsAllowed(false)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES)
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout)
                .setConnectionRequestTimeout(0) /* infinite */
                .setStaleConnectionCheckEnabled(false)
                .build();

        return HttpClients.custom()
                .setUserAgent(USER_AGENT)
                .setConnectionManager(manager)
                .setDefaultSocketConfig(socketConfig)
                .setDefaultRequestConfig(requestConfig)
                .setConnectionReuseStrategy(new NoConnectionReuseStrategy())
                .build();
    }


    private static SRUNamespaces detectNamespace(XMLStreamReader reader,
            SRUVersion requestedVersion)
                    throws SRUClientException {
        try {
            // skip to first start tag
            reader.nextTag();

            final String namespaceURI = reader.getNamespaceURI();
            logger.debug("found namespace URI '{}', requested version = {}",
                    namespaceURI, requestedVersion);

            if (NAMESPACES_LEGACY_LOC.foo(namespaceURI)) {
                return NAMESPACES_LEGACY_LOC;
            } else if (NAMESPACES_OASIS.foo(namespaceURI)) {
                return NAMESPACES_OASIS;
            } else {
                throw new SRUClientException(
                        "invalid namespace '" + reader.getNamespaceURI() + "'");
            }
        } catch (XMLStreamException e) {
            throw new SRUClientException("error detecting namespace", e);
        }
    }


    private interface SRUNamespaces {
        public String sruNS();

        public String scanNS();

        public String diagnosticNS();

        public boolean foo(String namespaceURI);

    } // interface SRUNamespace


    private static final SRUNamespaces NAMESPACES_LEGACY_LOC = new SRUNamespaces() {
        private static final String SRU_NS =
                "http://www.loc.gov/zing/srw/";
        private static final String SRU_DIAGNOSIC_NS =
                "http://www.loc.gov/zing/srw/diagnostic/";


        @Override
        public String sruNS() {
            return SRU_NS;
        }


        @Override
        public String scanNS() {
            return SRU_NS;
        }


        @Override
        public String diagnosticNS() {
            return SRU_DIAGNOSIC_NS;
        }


        @Override
        public boolean foo(String namespaceURI) {
            return SRU_NS.equals(namespaceURI);
        }
    };


    private static final SRUNamespaces NAMESPACES_OASIS = new SRUNamespaces() {
        private static final String SRU_NS =
                "http://docs.oasis-open.org/ns/search-ws/sruResponse";
        private static final String SRU_SCAN_NS =
                "http://docs.oasis-open.org/ns/search-ws/scan";
        private static final String SRU_DIAGNOSIC_NS =
                "http://docs.oasis-open.org/ns/search-ws/diagnostic";


        @Override
        public String sruNS() {
            return SRU_NS;
        }


        @Override
        public String scanNS() {
            return SRU_SCAN_NS;

        }


        @Override
        public String diagnosticNS() {
            return SRU_DIAGNOSIC_NS;
        }


        @Override
        public boolean foo(String namespaceURI) {
            return SRU_NS.equals(namespaceURI) || SRU_SCAN_NS.equals(namespaceURI);
        }
    };

} // class SRUSimpleClient

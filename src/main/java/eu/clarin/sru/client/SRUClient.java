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

import org.apache.commons.lang.NullArgumentException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.client.SRUScanHandler.WhereInList;

/**
 * A class to perform SRU operations.
 * <p><em>This class is not thread-safe!</em></p>
 */
public class SRUClient {
    /** constant record data schema parser to match any schema */
    public static final String RECORD_DATA_PARSER_SCHEMA_ANY = "*";
    private static final String SRU_NS =
            "http://www.loc.gov/zing/srw/";
    private static final String SRU_DIAGNOSIC_NS =
            "http://www.loc.gov/zing/srw/diagnostic/";
    private static final String SRU_DIAGNOSTIC_RECORD_SCHEMA =
            "info:srw/schema/1/diagnostics-v1.1";
    private static final String VERSION_1_1 = "1.1";
    private static final String VERSION_1_2 = "1.2";
    private static final String RECORD_PACKING_XML = "xml";
    private static final String RECORD_PACKING_STRING = "string";
    private static final Logger logger =
            LoggerFactory.getLogger(SRUClient.class);
    private final SRUVersion defaultVersion;
    private final HttpClient httpClient;
    private final Map<String, SRURecordDataParser> parsers =
            new HashMap<String, SRURecordDataParser>();
    private final XmlStreamReaderProxy proxy = new XmlStreamReaderProxy();


    /**
     * Constructor.
     *
     * @param defaultVersion
     *            the default version to use for SRU requests; may be overridden
     *            by individual requests
     */
    public SRUClient(SRUVersion defaultVersion) {
        if (defaultVersion == null) {
            throw new NullPointerException("version == null");
        }
        this.defaultVersion = defaultVersion;
        this.httpClient = new DefaultHttpClient();
        this.httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
                    "eu.clarin.sru.client/0.0.1");
    }


    /**
     * Register a record data parser.
     *
     * @param parser
     *            a parser instance
     * @throws SRUClientException
     *             if a parser handing the same record schema is already
     *             registered
     * @throws NullPointerException
     *             if any required argument is <code>null</code>
     * @throws IllegalArgumentException
     *             if the supplied parser is invalid
     */
    public void registerRecordParser(SRURecordDataParser parser)
            throws SRUClientException {
        if (parser == null) {
            throw new NullPointerException("parser == null");
        }
        final String recordSchema = parser.getRecordSchema();
        if (recordSchema == null) {
            throw new NullPointerException("parser.getRecordSchema() == null");
        }
        if (recordSchema.isEmpty()) {
            throw new IllegalArgumentException(
                    "parser.getRecordSchema() returns empty string");
        }

        if (!parsers.containsKey(recordSchema)) {
            parsers.put(recordSchema, parser);
        } else {
            throw new SRUClientException(
                    "record data parser already registered: " + recordSchema);
        }
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
            throw new NullArgumentException("handler == null");
        }
        logger.debug("explain");

        final long ts_start = System.nanoTime();

        // create URI and perform request
        final URI uri = request.makeURI(defaultVersion);
        HttpResponse response = executeRequest(uri);
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new SRUClientException("cannot get entity");
        }

        InputStream stream = null;
        SRUXMLStreamReader reader = null;
        try {
            stream = entity.getContent();

            final long ts_parsing = System.nanoTime();
            reader = createReader(stream, true);
            parseExplainResponse(reader, request, handler);
            final long ts_end = System.nanoTime();

            final long millisTotal =
                    TimeUnit.NANOSECONDS.toMillis(ts_end - ts_start);
            final long millisNetwork =
                    TimeUnit.NANOSECONDS.toMillis(ts_parsing - ts_start);
            final long millisParsing =
                    TimeUnit.NANOSECONDS.toMillis(ts_end - ts_parsing);
            logger.debug("{} byte(s) in {} milli(s) ({} milli(s) network / {} milli(s) parsing)",
                    new Object[] { reader.getByteCount(),
                            millisTotal, millisNetwork, millisParsing });
            handler.onRequestStatistics((int) reader.getByteCount(),
                    millisTotal, millisNetwork, millisParsing);
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
            throw new NullArgumentException("handler == null");
        }
        logger.debug("searchRetrieve: scanClause = {}", request.getScanClause());

        final long ts_start = System.nanoTime();

        // create URI and perform request
        final URI uri = request.makeURI(defaultVersion);
        HttpResponse response = executeRequest(uri);
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new SRUClientException("cannot get entity");
        }

        InputStream stream = null;
        SRUXMLStreamReader reader = null;
        try {
            stream = entity.getContent();

            final long ts_parsing = System.nanoTime();
            reader = createReader(stream, true);
            parseScanResponse(reader, request, handler);
            final long ts_end = System.nanoTime();

            final long millisTotal =
                    TimeUnit.NANOSECONDS.toMillis(ts_end - ts_start);
            final long millisNetwork =
                    TimeUnit.NANOSECONDS.toMillis(ts_parsing - ts_start);
            final long millisParsing =
                    TimeUnit.NANOSECONDS.toMillis(ts_end - ts_parsing);
            logger.debug("{} byte(s) in {} milli(s) ({} milli(s) network / {} milli(s) parsing)",
                    new Object[] { reader.getByteCount(),
                            millisTotal, millisNetwork, millisParsing });
            handler.onRequestStatistics((int) reader.getByteCount(),
                    millisTotal, millisNetwork, millisParsing);
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
        }
    }


    /**
     * Perform a <em>searchRetreive</em> operation.
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
            throw new NullArgumentException("handler == null");
        }
        logger.debug("searchRetrieve: query = {}", request.getQuery());

        final long ts_start = System.nanoTime();

        // create URI and perform request
        final URI uri = request.makeURI(defaultVersion);
        HttpResponse response = executeRequest(uri);
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new SRUClientException("cannot get entity");
        }

        InputStream stream = null;
        SRUXMLStreamReader reader = null;
        try {
            stream = entity.getContent();

            final long ts_parsing = System.nanoTime();
            reader = createReader(stream, true);
            parseSearchRetrieveResponse(reader, request, handler);
            final long ts_end = System.nanoTime();

            final long millisTotal =
                    TimeUnit.NANOSECONDS.toMillis(ts_end - ts_start);
            final long millisNetwork =
                    TimeUnit.NANOSECONDS.toMillis(ts_parsing - ts_start);
            final long millisParsing =
                    TimeUnit.NANOSECONDS.toMillis(ts_end - ts_parsing);
            logger.debug("{} byte(s) in {} milli(s) ({} milli(s) network / {} milli(s) parsing)",
                    new Object[] { reader.getByteCount(),
                            millisTotal, millisNetwork, millisParsing });
            handler.onRequestStatistics((int) reader.getByteCount(),
                    millisTotal, millisNetwork, millisParsing);
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
        }
    }


    private HttpResponse executeRequest(URI uri) throws SRUClientException {
        HttpGet request = null;
        HttpResponse response = null;
        try {
            logger.debug("executing HTTP request: {}", uri.toString());
            try {
                request = new HttpGet(uri);
                response = httpClient.execute(request);
                StatusLine status = response.getStatusLine();
                if (status.getStatusCode() != HttpStatus.SC_OK) {
                    if (status.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                        throw new SRUClientException("not found: " + uri);
                    } else {
                        throw new SRUClientException("unexpected status: " +
                                status.getStatusCode());
                    }
                }
                return response;
            } catch (ClientProtocolException e) {
                throw new SRUClientException("client protocol exception", e);
            } catch (UnknownHostException e) {
                throw new SRUClientException("unknown host: " + uri.getHost(),
                        e);
            } catch (IOException e) {
                throw new SRUClientException("input/output error", e);
            }
        } catch (SRUClientException e) {
            /*
             * if an error occurred, make sure we are freeing up the resources
             * we've used
             */
            if (response != null) {
                try {
                    EntityUtils.consume(response.getEntity());
                } catch (IOException ex) {
                    /* IGNORE */
                }
            }
            if (request != null) {
                request.abort();
            }
            throw e;
        }
    }


    private void parseExplainResponse(final SRUXMLStreamReader reader,
            final SRUAbstractRequest request, final SRUExplainHandler handler)
            throws SRUClientException {
        logger.debug("parsing 'explain' response");
        try {
            // explainResponse
            reader.readStart(SRU_NS, "explainResponse", true);

            // explainResponse/version
            SRUVersion version = parseVersion(reader);
            logger.debug("version = {}, requested = {}",
                    version, request.getVersionPerformed());

            // explainResponse/record
            reader.readStart(SRU_NS, "record", true);

            String schema = reader.readContent(SRU_NS, "recordSchema", true);

            SRURecordPacking packing = parseRecordPacking(reader);

            logger.debug("schema = {}, packing = {}", schema, packing);

            // explainResponse/record/recordData
            reader.readStart(SRU_NS, "recordData", true);
            reader.readEnd(SRU_NS, "recordData", true);

            // explainResponse/record/recordPosition
            if (reader.readStart(SRU_NS, "recordPosition", false)) {
                reader.readEnd(SRU_NS, "recordPosition", true);
            }

            // explainResponse/record/extraRecordData
            if (reader.readStart(SRU_NS, "extraRecordData", false)) {
                reader.readEnd(SRU_NS, "extraRecordData", true);
            }

            reader.readEnd(SRU_NS, "record");

            // explainResponse/echoedExplainRequest
            if (reader.readStart(SRU_NS, "echoedExplainRequest", false)) {
                reader.readEnd(SRU_NS, "echoedExplainRequest", true);
            }

            // explainResponse/diagnostics
            final List<SRUDiagnostic> diagnostics = parseDiagnostics(reader);
            if (diagnostics != null) {
                handler.onDiagnostics(diagnostics);
            }

            // explainResponse/extraResponseData
            if (reader.readStart(SRU_NS, "extraResponseData", false)) {
                reader.consumeWhitespace();
                proxy.reset(reader);
                try {
                    handler.onExtraResponseData(proxy);
                } catch (XMLStreamException e) {
                    throw new SRUClientException("handler triggered "
                            + "error while parsing 'extraResponseData'", e);
                }
                reader.consumeWhitespace();
                reader.readEnd(SRU_NS, "extraResponseData", true);
            }

            reader.readEnd(SRU_NS, "explainResponse");
        } catch (XMLStreamException e) {
            throw new SRUClientException(e.getMessage(), e);
        }
    }


    private void parseScanResponse(final SRUXMLStreamReader reader,
            final SRUScanRequest request, final SRUScanHandler handler)
            throws SRUClientException {
        try {
            /*
             * if the endpoint cannot determine the operation, it should create
             * a explain response.
             */
            if (reader.peekStart(SRU_NS, "explainResponse")) {
                parseExplainResponse(reader, request, new SRUExplainHandler() {
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
                });
            } else {
                logger.debug("parsing 'scanResponse' response");

                // scanResponse
                reader.readStart(SRU_NS, "scanResponse", true);

                // scanResponse/version
                SRUVersion version = parseVersion(reader);
                logger.debug("version = {}, requested = {}", version,
                        request.getVersionPerformed());

                // scanResponse/terms
                if (reader.readStart(SRU_NS, "terms", false)) {
                    boolean first = true;
                    while (reader.readStart(SRU_NS, "term", first)) {
                        if (first) {
                            first = false;
                            handler.onStartTerms();
                        }

                        // scanResponse/terms/value
                        String value = reader
                                .readContent(SRU_NS, "value", true);

                        // scanResponse/terms/numberOfRecords
                        int numberOfRecords = reader.readContent(SRU_NS,
                                "numberOfRecords", false, -1);

                        // scanResponse/terms/displayTerm
                        String displayTerm = reader.readContent(SRU_NS,
                                "displayTerm", false);

                        // scanResponse/terms/whereInList
                        String s = reader.readContent(SRU_NS,
                                "whereInList", false);
                        WhereInList whereInList = null;
                        if (s != null) {
                            if ("first".equals(s)) {
                                whereInList = WhereInList.FIRST;
                            } else if ("last".equals(s)) {
                                whereInList = WhereInList.LAST;
                            } else if ("only".equals(s)) {
                                whereInList = WhereInList.ONLY;
                            } else if ("inner".equals(s)) {
                                whereInList = WhereInList.INNER;
                            } else {
                                throw new SRUClientException(
                                        "invalid value for 'whereInList': " + s);
                            }
                        }
                        logger.debug("value = {}, numberOfRecords = {}, "
                                + "displayTerm = {}, whereInList = {}",
                                new Object[] { value, numberOfRecords,
                                        displayTerm, whereInList });
                        handler.onTerm(value, numberOfRecords, displayTerm,
                                whereInList);

                        // scanResponse/terms/extraTermData
                        if (reader.readStart(SRU_NS, "extraTermData", first)) {
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
                            reader.readEnd(SRU_NS, "extraTermData", true);
                        }
                        reader.readEnd(SRU_NS, "term", true);

                    } // while
                    reader.readEnd(SRU_NS, "terms");
                    handler.onFinishTerms();
                }

                // scanResponse/echoedScanRequest
                if (reader.readStart(SRU_NS, "echoedScanRequest", false)) {
                    reader.readEnd(SRU_NS, "echoedScanRequest", true);
                }

                // scanResponse/diagnostics
                final List<SRUDiagnostic> diagnostics = parseDiagnostics(reader);
                if (diagnostics != null) {
                    handler.onDiagnostics(diagnostics);
                }

                // scanResponse/extraResponseData
                if (reader.readStart(SRU_NS, "extraResponseData", false)) {
                    reader.consumeWhitespace();
                    proxy.reset(reader);
                    try {
                        handler.onExtraResponseData(proxy);
                    } catch (XMLStreamException e) {
                        throw new SRUClientException("handler triggered "
                                + "error while parsing 'extraResponseData'", e);
                    }
                    reader.consumeWhitespace();
                    reader.readEnd(SRU_NS, "extraResponseData", true);
                }

                reader.readEnd(SRU_NS, "scanResponse");
            }
        } catch (XMLStreamException e) {
            throw new SRUClientException(e.getMessage(), e);
        }
    }


    private void parseSearchRetrieveResponse(final SRUXMLStreamReader reader,
            final SRUSearchRetrieveRequest request,
            final SRUSearchRetrieveHandler handler) throws SRUClientException {
        try {
            /*
             * if the endpoint cannot determine the operation, it should create
             * a explain response.
             */
            if (reader.peekStart(SRU_NS, "explainResponse")) {
                parseExplainResponse(reader, request, new SRUExplainHandler() {
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
                });
            } else {
                logger.debug("parsing 'serarchRetrieve' response");

                // searchRetrieveResponse
                reader.readStart(SRU_NS, "searchRetrieveResponse", true);

                // searchRetrieveResponse/version
                SRUVersion version = parseVersion(reader);
                logger.debug("version = {}, requested = {}", version,
                        request.getVersionPerformed());

                // searchRetrieveResponse/numberOfRecords
                int numberOfRecords = reader.readContent(SRU_NS,
                        "numberOfRecords", true, -1);

                // searchRetrieveResponse/resultSetId
                int resultSetId = reader.readContent(SRU_NS,
                        "resultSetId", false, -1);

                // searchRetrieveResponse/resultSetIdleTime
                int resultSetIdleTime = reader.readContent(SRU_NS,
                        "resultSetIdleTime", false, -1);

                logger.debug("numberOfRecords = {}, resultSetId = {}, "
                        + "resultSetIdleTime = {}", new Object[] {
                        numberOfRecords, resultSetId, resultSetIdleTime });

                // searchRetrieveResponse/results
                if (numberOfRecords > 0) {
                    reader.readStart(SRU_NS, "records", true);

                    // searchRetrieveResponse/records/record
                    boolean first = true;
                    while (reader.readStart(SRU_NS, "record", first)) {
                        if (first) {
                            first = false;
                            handler.onStartRecords(numberOfRecords,
                                    resultSetId, resultSetIdleTime);
                        }

                        String schema = reader.readContent(SRU_NS,
                                "recordSchema", true);

                        SRURecordPacking packing = parseRecordPacking(reader);

                        logger.debug("schema = {}, packing = {}, " +
                                "requested packing = {}",
                                new Object[] { schema, packing,
                                        request.getRecordPacking() });

                        if ((request.getRecordPacking() != null) &&
                                (packing != request.getRecordPacking())) {
                            final SRURecordPacking p =
                                    request.getRecordPacking();
                            logger.warn("requested '{}' record packing, but " +
                                "server responded with '{}' record packing",
                                    p.getStringValue(),
                                    packing.getStringValue());
                            // XXX: only throw if client is pedantic?
                            throw new SRUClientException("requested '" +
                                            p.getStringValue() +
                                            "' record packing, but server " +
                                            "responded with '" +
                                            packing.getStringValue() +
                                            "' record packing");
                        }

                        // searchRetrieveResponse/record/recordData
                        reader.readStart(SRU_NS, "recordData", true);
                        reader.consumeWhitespace();

                        SRURecordData recordData = null;
                        SRUDiagnostic surrogate = null;
                        SRUXMLStreamReader recordReader = null;

                        if (packing == SRURecordPacking.STRING) {
                            /*
                             * read content into temporary buffer and then use
                             * a new XML reader to parse record data
                             */
                            final String data = reader.readString(true);
                            InputStream in =
                                    new ByteArrayInputStream(data.getBytes());
                            // FIXME: namespace context?
                            recordReader = createReader(in, false);
                        } else {
                            recordReader = reader;
                        }

                        if (SRU_DIAGNOSTIC_RECORD_SCHEMA.equals(schema)) {
                            surrogate = parseDiagnostic(recordReader, true);
                        } else {
                            SRURecordDataParser parser = findParser(schema);
                            if (parser != null) {
                                try {
                                    proxy.reset(recordReader);
                                    recordData = parser.parse(proxy);
                                } catch (XMLStreamException e) {
                                    throw new SRUClientException(
                                            "error parsing record", e);
                                }
                                if (recordData == null) {
                                    // FIXME: handle this better? maybe throw?
                                    logger.warn("parse did not correctly "
                                            + "parse the record, will skip "
                                            + "handler callback.");
                                }
                            } else {
                                // FIXME: handle this better?
                                logger.debug("no record parser found for schema '{}'",
                                        schema);
                            }
                        }

                        if (packing == SRURecordPacking.STRING) {
                            recordReader.closeCompletly();
                        }

                        reader.consumeWhitespace();
                        reader.readEnd(SRU_NS, "recordData", true);

                        String identifier = null;
                        if (version == SRUVersion.VERSION_1_2) {
                            identifier = reader.readContent(SRU_NS,
                                    "recordIdentifier", false);
                        }

                        int position = reader.readContent(SRU_NS,
                                "recordPosition", false, -1);

                        logger.debug("recordIdentifier = {}, recordPosition = {}",
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

                        if (reader.readStart(SRU_NS, "extraRecordData", false)) {
                            reader.consumeWhitespace();
                            proxy.reset(reader);
                            try {
                                handler.onExtraRecordData(identifier,
                                        position, proxy);
                            } catch (XMLStreamException e) {
                                throw new SRUClientException("handler "
                                        + "triggered error while parsing "
                                        + "'extraRecordData'", e);
                            }
                            reader.consumeWhitespace();
                            reader.readEnd(SRU_NS, "extraRecordData", true);
                        }

                        reader.readEnd(SRU_NS, "record");
                    } // while
                    reader.readEnd(SRU_NS, "records");
                }

                int nextRecordPosition = reader.readContent(SRU_NS,
                        "nextRecordPosition", false, -1);
                logger.debug("nextRecordPosition = {}", nextRecordPosition);
                handler.onFinishRecords(nextRecordPosition);

                // searchRetrieveResponse/echoedSearchRetrieveResponse
                if (reader.readStart(SRU_NS,
                        "echoedSearchRetrieveRequest", false)) {
                    reader.readEnd(SRU_NS, "echoedSearchRetrieveRequest", true);
                }

                // searchRetrieveResponse/diagnostics
                final List<SRUDiagnostic> diagnostics = parseDiagnostics(reader);
                if (diagnostics != null) {
                    handler.onDiagnostics(diagnostics);
                }

                // explainResponse/extraResponseData
                if (reader.readStart(SRU_NS, "extraResponseData", false)) {
                    reader.consumeWhitespace();
                    proxy.reset(reader);
                    try {
                        handler.onExtraResponseData(proxy);
                    } catch (XMLStreamException e) {
                        throw new SRUClientException("handler triggered "
                                + "error while parsing 'extraResponseData'", e);
                    }
                    reader.consumeWhitespace();
                    reader.readEnd(SRU_NS, "extraResponseData", true);
                }

                reader.readEnd(SRU_NS, "searchRetrieveResponse");
            }
        } catch (XMLStreamException e) {
            throw new SRUClientException(e.getMessage(), e);
        }
    }


    private static SRUVersion parseVersion(SRUXMLStreamReader reader)
        throws XMLStreamException, SRUClientException {
        final String v = reader.readContent(SRU_NS, "version", true);
        if (VERSION_1_1.equals(v)) {
            return SRUVersion.VERSION_1_1;
        } else if (VERSION_1_2.equals(v)) {
            return SRUVersion.VERSION_1_2;
        } else {
            throw new SRUClientException("invalid value '" + v +
                    "' for version (valid values are: '" + VERSION_1_1 +
                    "' and '" + VERSION_1_2 + "')");
        }
    }


    private static List<SRUDiagnostic> parseDiagnostics(
            SRUXMLStreamReader reader) throws XMLStreamException,
            SRUClientException {
        if (reader.readStart(SRU_NS, "diagnostics", false)) {
            List<SRUDiagnostic> diagnostics = null;

            SRUDiagnostic diagnostic = null;
            while ((diagnostic = parseDiagnostic(reader,
                    (diagnostics == null))) != null) {
                if (diagnostics == null) {
                    diagnostics = new ArrayList<SRUDiagnostic>();
                }
                diagnostics.add(diagnostic);
            } // while
            reader.readEnd(SRU_NS, "diagnostics");
            return diagnostics;
        } else {
            return null;
        }
    }


    private static SRUDiagnostic parseDiagnostic(SRUXMLStreamReader reader,
            boolean required) throws XMLStreamException, SRUClientException {
        if (reader.readStart(SRU_DIAGNOSIC_NS, "diagnostic", required)) {

            // diagnostic/uri
            String uri = reader.readContent(SRU_DIAGNOSIC_NS, "uri", true);

            // diagnostic/details
            String details =
                    reader.readContent(SRU_DIAGNOSIC_NS, "details", false);

            // diagnostic/message
            String message =
                    reader.readContent(SRU_DIAGNOSIC_NS, "message", false);

            reader.readEnd(SRU_DIAGNOSIC_NS, "diagnostic");

            logger.debug("diagostic: uri={}, detail={}, message={}",
                    new Object[] { uri, details, message });
            return new SRUDiagnostic(uri, details, message);
        } else {
            return null;
        }
    }


    private static SRURecordPacking parseRecordPacking(SRUXMLStreamReader reader)
            throws XMLStreamException, SRUClientException {
        final String v = reader.readContent(SRU_NS, "recordPacking", true);

        if (RECORD_PACKING_XML.equals(v)) {
            return SRURecordPacking.XML;
        } else if (RECORD_PACKING_STRING.equals(v)) {
            return SRURecordPacking.STRING;
        } else {
            throw new SRUClientException("invalid value '" + v +
                    "' for record packing (valid values are: '" +
                    RECORD_PACKING_XML + "' and '" + RECORD_PACKING_STRING +
                    "')");
        }
    }


    private SRURecordDataParser findParser(String schema) {
        SRURecordDataParser parser = parsers.get(schema);
        if (parser == null) {
            parser = parsers.get(RECORD_DATA_PARSER_SCHEMA_ANY);
        }
        return parser;
    }


    private SRUXMLStreamReader createReader(InputStream in, boolean wrap)
            throws XMLStreamException {
        return new SRUXMLStreamReader(in, wrap);
    }

} // class SRUClient

package eu.clarin.sru.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLStreamException;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.client.SRUScanHandler.WhereInList;


public class SRUClient {
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
    private final ConcurrentMap<String, SRURecordDataParser> parsers =
            new ConcurrentHashMap<String, SRURecordDataParser>();
    private final XmlStreamReaderProxy proxy = new XmlStreamReaderProxy();


    public SRUClient(SRUVersion defaultVersion) {
        if (defaultVersion == null) {
            throw new NullPointerException("version == null");
        }
        this.defaultVersion = defaultVersion;
        this.httpClient = new DefaultHttpClient();
        this.httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
                    "eu.clarin.sru.client/0.0.1");
    }


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

        SRURecordDataParser old = parsers.putIfAbsent(recordSchema, parser);
        if (old != null) {
            throw new SRUClientException(
                    "record data parser already registered: " + recordSchema);
        }
    }


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
            reader = createReader(stream);
            parseExplainResponse(reader, handler);
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
            reader = createReader(stream);
            parseScanResponse(reader, handler);
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
            reader = createReader(stream);
            parseSearchRetrieveResponse(reader, handler);
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
        try {
            logger.debug("executing HTTP request: {}", uri.toString());
            HttpGet request = new HttpGet(uri);
            HttpResponse response = httpClient.execute(request);
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
            throw new SRUClientException("unknown host: " + uri.getHost(), e);
        } catch (IOException e) {
            throw new SRUClientException("input/output error", e);
        }
    }


    private void parseExplainResponse(SRUXMLStreamReader reader,
            SRUExplainHandler handler) throws SRUClientException {
        logger.debug("parsing 'explain' response");
        try {
            // explainResponse
            reader.readStart(SRU_NS, "explainResponse", true);

            // explainResponse/version
            SRUVersion version = parseVersion(reader);
            logger.debug("version = {}, requested = {}",
                    version, this.defaultVersion);

            // check if a fatal error occurred
            final List<SRUDiagnostic> diagnostics = parseDiagnostics(reader);
            if (diagnostics != null) {
                handler.onFatalError(diagnostics);
            } else {
                // explainResponse/record
                reader.readStart(SRU_NS, "record", true);

                String schema =
                        reader.readContent(SRU_NS, "recordSchema", true);

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
                parseDiagnostics(reader);

                // explainResponse/extraResponseData
                if (reader.readStart(SRU_NS, "extraResponseData", false)) {
                    reader.consumeWhitespace();
                    proxy.reset(reader);
                    try {
                        handler.onExtraResponseData(proxy);
                    } catch (XMLStreamException e) {
                        throw new SRUClientException("handler triggered " +
                                "error while parsing 'extraResponseData'", e);
                    }
                    reader.consumeWhitespace();
                    reader.readEnd(SRU_NS, "extraResponseData", true);
                }

                reader.readEnd(SRU_NS, "explainResponse");
            }
        } catch (XMLStreamException e) {
            throw new SRUClientException("error parsing response", e);
        }
    }


    private void parseScanResponse(SRUXMLStreamReader reader,
            SRUScanHandler handler) throws SRUClientException {
        logger.debug("parsing 'scanResponse' response");
        try {
            // scanResponse
            reader.readStart(SRU_NS, "scanResponse", true);

            // scanResponse/version
            SRUVersion version = parseVersion(reader);
            logger.debug("version = {}, requested = {}",
                    version, this.defaultVersion);

            // check if a fatal error occurred
            final List<SRUDiagnostic> diagnostics = parseDiagnostics(reader);
            if (diagnostics != null) {
                handler.onFatalError(diagnostics);
            } else {
                // scanResponse/terms
                if (reader.readStart(SRU_NS, "terms", false)) {
                    boolean first = true;
                    while (reader.readStart(SRU_NS, "term", first)) {
                        if (first) {
                            first = false;
                            handler.onStartTerms();
                        }

                        // scanResponse/terms/value
                        String value = reader.readContent(SRU_NS,
                                "value", true);

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
                        logger.debug("value = {}, numberOfRecords = {}, " +
                                "displayTerm = {}, whereInList = {}",
                                new Object[]  { value, numberOfRecords,
                                    displayTerm, whereInList });
                        handler.onTerm(value, numberOfRecords, displayTerm, whereInList);

                        // scanResponse/terms/extraTermData
                        if (reader.readStart(SRU_NS, "extraTermData", first)) {
                            reader.consumeWhitespace();
                            proxy.reset(reader);
                            try {
                                handler.onExtraTermData(value, proxy);
                            } catch (XMLStreamException e) {
                                throw new SRUClientException("handler " +
                                        "triggered error while parsing " +
                                        "'extraTermData'", e);
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
                parseDiagnostics(reader);

                // scanResponse/extraResponseData
                if (reader.readStart(SRU_NS, "extraResponseData", false)) {
                    reader.consumeWhitespace();
                    proxy.reset(reader);
                    try {
                        handler.onExtraResponseData(proxy);
                    } catch (XMLStreamException e) {
                        throw new SRUClientException("handler triggered " +
                                "error while parsing 'extraResponseData'", e);
                    }
                    reader.consumeWhitespace();
                    reader.readEnd(SRU_NS, "extraResponseData", true);
                }

                reader.readEnd(SRU_NS, "scanResponse");
            }
        } catch (XMLStreamException e) {
            throw new SRUClientException("error parsing response", e);
        }
    }


    private void parseSearchRetrieveResponse(SRUXMLStreamReader reader,
            SRUSearchRetrieveHandler handler) throws SRUClientException {
        logger.debug("parsing 'serarchRetrieve' response");
        try {
            // searchRetrieveResponse
            reader.readStart(SRU_NS, "searchRetrieveResponse", true);

            // searchRetrieveResponse/version
            SRUVersion version = parseVersion(reader);
            logger.debug("version = {}, requested = {}",
                    version, this.defaultVersion);

            // check if a fatal error occurred
            final List<SRUDiagnostic> diagnostics = parseDiagnostics(reader);
            if (diagnostics != null) {
                handler.onFatalError(diagnostics);
            } else {

                // searchRetrieveResponse/numberOfRecords
                int numberOfRecords = reader.readContent(SRU_NS,
                        "numberOfRecords", true, -1);

                // searchRetrieveResponse/resultSetId
                int resultSetId = reader.readContent(SRU_NS, "resultSetId",
                        false, -1);

                // searchRetrieveResponse/resultSetIdleTime
                int resultSetIdleTime = reader.readContent(SRU_NS,
                        "resultSetIdleTime", false, -1);

                logger.debug("numberOfRecords = {}, resultSetId = {}, " +
                        "resultSetIdleTime = {}",
                        new Object[] { numberOfRecords, resultSetId,
                                resultSetIdleTime });

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

                        logger.debug("schema = {}, packing = {}",
                                schema, packing);

                        /* FIXME: bail on non-XML record packing */
                        if (packing != SRURecordPacking.XML) {
                            throw new SRUClientException("record packing '" +
                                    packing + "' is not supported");
                        }

                        // searchRetrieveResponse/record/recordData
                        reader.readStart(SRU_NS, "recordData", true);
                        reader.consumeWhitespace();

                        SRURecordData recordData = null;
                        SRUDiagnostic surrogate = null;

                        if (SRU_DIAGNOSTIC_RECORD_SCHEMA.equals(schema)) {
                            surrogate = parseDiagnostic(reader, true);
                        } else {
                            SRURecordDataParser parser = parsers.get(schema);
                            if (parser != null) {
                                try {
                                    proxy.reset(reader);
                                    recordData = parser.parse(proxy);
                                } catch (XMLStreamException e) {
                                    throw new SRUClientException(
                                            "error parsing record", e);
                                }
                            } else {
                                // FIXME: handle this better?
                                logger.debug(
                                        "no record parser found for schema '{}'",
                                        schema);
                            }
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
                                throw new SRUClientException("handler " +
                                        "triggered error while parsing " +
                                        "'extraRecordData'", e);
                            }
                            reader.consumeWhitespace();
                            reader.readEnd(SRU_NS, "extraRecordData", true);
                        }

                        reader.readEnd(SRU_NS, "record");
                    } // while

                    reader.readEnd(SRU_NS, "records");
                    int nextRecordPosition = reader.readContent(SRU_NS,
                            "nextRecordPosition", false, -1);
                    logger.debug("nextRecordPosition = {}", nextRecordPosition);
                    handler.onFinishRecords(nextRecordPosition);
                } else {
                    handler.onStartRecords(numberOfRecords,
                            resultSetId, resultSetIdleTime);
                    handler.onFinishRecords(-1);
                }

                // searchRetrieveResponse/echoedSearchRetrieveResponse
                if (reader.readStart(SRU_NS, "echoedSearchRetrieveRequest", false)) {
                    reader.readEnd(SRU_NS, "echoedSearchRetrieveRequest", true);
                }

                // explainResponse/diagnostics
                parseDiagnostics(reader);

                // explainResponse/extraResponseData
                if (reader.readStart(SRU_NS, "extraResponseData", false)) {
                    reader.consumeWhitespace();
                    proxy.reset(reader);
                    try {
                        handler.onExtraResponseData(proxy);
                    } catch (XMLStreamException e) {
                        throw new SRUClientException("handler triggered " +
                                "error while parsing 'extraResponseData'", e);
                    }
                    reader.consumeWhitespace();
                    reader.readEnd(SRU_NS, "extraResponseData", true);
                }

                reader.readEnd(SRU_NS, "searchRetrieveResponse");
            }
        } catch (XMLStreamException e) {
            throw new SRUClientException("error parsing response", e);
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
            List<SRUDiagnostic> diagostics = null;

            SRUDiagnostic diagnostic = null;
            while ((diagnostic = parseDiagnostic(reader,
                    (diagostics == null))) != null) {
                if (diagostics == null) {
                    diagostics = new ArrayList<SRUDiagnostic>();
                }
                diagostics.add(diagnostic);
            } // while
            reader.readEnd(SRU_NS, "diagnostics");
            return diagostics;
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


    private SRUXMLStreamReader createReader(InputStream in)
            throws XMLStreamException {
        return new SRUXMLStreamReader(in);
    }

} // class SRUClient

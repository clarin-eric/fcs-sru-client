package eu.clarin.sru.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

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
    private static final String PARAM_QUERY = "query";
    private static final String PARAM_SCAN_CLAUSE = "scanClause";
    private enum Operation {
        OP_EXPLAIN, OP_SCAN, OP_SEARCH_RETRIEVE
    }

    private static final Logger logger =
            LoggerFactory.getLogger(SRUClient.class);
    private final String endpointURI;
    private SRUVersion version;
    private final HttpClient httpClient;


    public SRUClient(String endpointURI, SRUVersion version) {
        if (endpointURI == null) {
            throw new NullPointerException("endpointURI == null");
        }
        this.endpointURI = endpointURI;
        if (version == null) {
            throw new NullPointerException("version == null");
        }
        this.version = version;
        this.httpClient = new DefaultHttpClient();
        this.httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
                    "eu.clarin.sru.client/0.0.1");
    }


    /*
     * TODO: Make Request Object and pass to methods
     *  AbstractRequest,
     *  ExplainRequest,
     *  SearchRetrieveRequest,
     *  ScanRequest
     *
     *  setVersion();
     *  setExtrarequestParams(), etc
     */
    public void explain(SRUExplainHandler handler) throws SRUClientException {
        if (handler == null) {
            throw new NullArgumentException("handler == null");
        }
        try {
            URI uri = makeURI(Operation.OP_EXPLAIN, null);
            HttpResponse response = executeRequest(uri);

            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            SRUXMLStreamReader reader = null;
            try {
                final long now = System.nanoTime();
                reader = createReader(stream);
                parseExplainResponse(reader);
                final long delta = System.nanoTime() - now;
                logger.debug("processed {} byte(s) in {} milli(s)",
                        reader.getByteCount(),
                        TimeUnit.NANOSECONDS.toMillis(delta));
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (XMLStreamException e) {
                        /* IGNORE */
                    }
                }
                try {
                    stream.close();
                } catch (IOException e) {
                    /* IGNORE */
                }
            }
        } catch (IllegalStateException e) {
            throw new SRUClientException("error reading response", e);
        } catch (IOException e) {
            throw new SRUClientException("error reading response", e);
        } catch (XMLStreamException e) {
            throw new SRUClientException("error reading response", e);
        }
    }


    public void scan(String scanClause, SRUScanHandler handler)
            throws SRUClientException {
        if (scanClause == null) {
            throw new NullPointerException("scanClause == null");
        }
        if (handler == null) {
            throw new NullArgumentException("handler == null");
        }
        logger.debug("searchRetrieve: scanClause = {}", scanClause);

        // prepare arguments
        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put(PARAM_SCAN_CLAUSE, scanClause);
        // create URI and perform request
        URI uri = makeURI(Operation.OP_SCAN, arguments);
        HttpResponse response = executeRequest(uri);
        HttpEntity entity = response.getEntity();

        InputStream stream;
        try {
            stream = entity.getContent();
            SRUXMLStreamReader reader = null;
            try {
                final long now = System.nanoTime();
                reader = createReader(stream);
                parseScanResponse(reader, handler);
                final long delta = System.nanoTime() - now;
                logger.debug("processed {} byte(s) in {} milli(s)",
                        reader.getByteCount(),
                        TimeUnit.NANOSECONDS.toMillis(delta));
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (XMLStreamException e) {
                        /* IGNORE */
                    }
                }
                try {
                    stream.close();
                } catch (IOException e) {
                    /* IGNORE */
                }
            }
        } catch (IllegalStateException e) {
            throw new SRUClientException("error reading response", e);
        } catch (IOException e) {
            throw new SRUClientException("error reading response", e);
        } catch (XMLStreamException e) {
            throw new SRUClientException("error reading response", e);
        }
    }


    public void searchRetrieve(String query, SRUSearchRetrieveHandler handler)
            throws SRUClientException {
        if (query == null) {
            throw new NullPointerException("query == null");
        }
        if (handler == null) {
            throw new NullArgumentException("handler == null");
        }
        logger.debug("searchRetrieve: query = {}", query);

        // prepare arguments
        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put(PARAM_QUERY, query);
        arguments.put("x-indent-response", Integer.toString(4));

        // create URI and perform request
        URI uri = makeURI(Operation.OP_SEARCH_RETRIEVE, arguments);
        HttpResponse response = executeRequest(uri);
        HttpEntity entity = response.getEntity();

        InputStream stream;
        try {
            stream = entity.getContent();
            SRUXMLStreamReader reader = null;
            try {
                final long now = System.nanoTime();
                reader = createReader(stream);
                parseSearchRetrieveResponse(reader, handler);
                final long delta = System.nanoTime() - now;
                logger.debug("processed {} byte(s) in {} milli(s)",
                        reader.getByteCount(),
                        TimeUnit.NANOSECONDS.toMillis(delta));
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (XMLStreamException e) {
                        /* IGNORE */
                    }
                }
                try {
                    stream.close();
                } catch (IOException e) {
                    /* IGNORE */
                }
            }
        } catch (IllegalStateException e) {
            throw new SRUClientException("error reading response", e);
        } catch (IOException e) {
            throw new SRUClientException("error reading response", e);
        } catch (XMLStreamException e) {
            throw new SRUClientException("error reading response", e);
        }
    }


    private HttpResponse executeRequest(URI uri) throws SRUClientException {
        try {
            logger.debug("executing HTTP request: {}", uri.toString());
            HttpGet request = new HttpGet(uri);
            long now = System.nanoTime();
            HttpResponse response = httpClient.execute(request);
            StatusLine status = response.getStatusLine();
            long delta = System.nanoTime() - now;
            logger.debug("status = {}, time = {} milli(s)",
                    status.getStatusCode(),
                    TimeUnit.NANOSECONDS.toMillis(delta));
            if (status.getStatusCode() != HttpStatus.SC_OK) {
                // FIXME: maybe handle different and don't throw
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


    private void parseExplainResponse(SRUXMLStreamReader reader)
            throws SRUClientException {
        logger.debug("parsing 'explain' response");
        try {
            // explainResponse
            reader.readStart(SRU_NS, "explainResponse", true);

            // explainResponse/version
            String v = reader.readContent(SRU_NS, "version", true);
            logger.debug("version = {}, requested = {}", v, this.version);

            // check if a fatal error occurred
            if (parseDiagnostics(reader)) {
                // FIXME: do something useful ...
            } else {
                // explainResponse/record
                parseRecordExplain(reader);

                // explainResponse/echoedExplainRequest
                if (reader.readStart(SRU_NS, "echoedExplainRequest", false)) {
                    reader.readEnd(SRU_NS, "echoedExplainRequest", true);
                }

                // explainResponse/diagnostics
                parseDiagnostics(reader);

                // explainResponse/extraResponseData
                if (reader.readStart(SRU_NS, "extraResponseData", false)) {
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
            String v = reader.readContent(SRU_NS, "version", true);
            logger.debug("version = {}, requested = {}", v, this.version);

            // check if a fatal error occurred
            if (parseDiagnostics(reader)) {
                // FIXME: do something useful ...
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
            String v = reader.readContent(SRU_NS, "version", true);
            logger.debug("version = {}, requested = {}", v, this.version);

            // check if a fatal error occurred
            if (parseDiagnostics(reader)) {
                // FIXME: do something useful ...
            } else {

                // searchRetrieveResponse/numberOfRecords
                String numberOfRecords =
                        reader.readContent(SRU_NS, "numberOfRecords", true);

                // searchRetrieveResponse/resultSetId
                String resultSetId =
                        reader.readContent(SRU_NS, "resultSetId", false);

                // searchRetrieveResponse/resultSetIdleTime
                String resultSetIdleTime =
                        reader.readContent(SRU_NS, "resultSetIdleTime", false);

                logger.debug("numberOfRecords = {}, resultSetId = {}, " +
                        "resultSetIdleTime = {}",
                        new Object[] { numberOfRecords, resultSetId,
                                resultSetIdleTime });

                // searchRetrieveResponse/results
                reader.readStart(SRU_NS, "records", true);

                // searchRetrieveResponse/records/record
                boolean first = true;
                
                while (reader.readStart(SRU_NS, "record", first)) {
                    if (first) {
                        first = false;
                        handler.onStartRecords();
                    }
                    String schema = reader.readContent(SRU_NS,
                            "recordSchema", true);
                    String packing = reader.readContent(SRU_NS,
                            "recordPacking", false);

                    logger.debug("schema = {}, packing = {}", schema, packing);

                    // explainResponse/record/recordData
                    reader.readStart(SRU_NS, "recordData", true);
                    reader.consumeWhitespace();

                    /*
                     * FIXME: (temporary code) optimize buffer and try to reuse objects
                     * the following stuff is more or less a hack ;)
                     */
                    ByteArrayOutputStream out =
                            new ByteArrayOutputStream(1024);
                    XMLOutputFactory factory = XMLOutputFactory.newFactory();
                    XMLStreamWriter writer = factory.createXMLStreamWriter(out);
                    reader.copyTo(writer);
                    writer.close();
                    logger.debug("buffered {} bytes of record data", out.size());

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
                    
                    /*
                     * FIXME: (temporary code) research, if readers and
                     * other objects could be reused
                     */
                    XMLStreamReader r2 = null; 
                    try {
                        XMLInputFactory f2 = XMLInputFactory.newFactory();
                        r2 = f2.createXMLStreamReader(new ByteArrayInputStream(out.toByteArray())); 
                        handler.onRecord(schema, identifier, position, r2);
                    } catch (SRUClientException e) {
                        throw e;
                    } catch (XMLStreamException e) {
                        throw new SRUClientException("handler failed reading xml", e);
                    } catch (Throwable e) {
                        throw new SRUClientException("handler provoked an error", e);
                    } finally {
                        if (r2 != null) {
                            try {
                                r2.close();
                            } catch (XMLStreamException e) {
                                /* IGNORE */
                            }
                        }
                    }

                    if (reader.readStart(SRU_NS, "extraRecordData", false)) {
                        reader.readEnd(SRU_NS, "extraRecordData", true);
                    }

                    reader.readEnd(SRU_NS, "record");
                } // while
                    
                reader.readEnd(SRU_NS, "records");

                int nextRecordPosition = reader.readContent(SRU_NS,
                        "nextRecordPosition", false, -1);
                logger.debug("nextRecordPosition = {}", nextRecordPosition);
                handler.onFinishRecords(nextRecordPosition);

                
                // searchRetrieveResponse/echoedSearchRetrieveResponse
                if (reader.readStart(SRU_NS, "echoedSearchRetrieveRequest", false)) {
                    reader.readEnd(SRU_NS, "echoedSearchRetrieveRequest", true);
                }

                // explainResponse/diagnostics
                parseDiagnostics(reader);

                // explainResponse/extraResponseData
                if (reader.readStart(SRU_NS, "extraResponseData", false)) {
                    reader.readEnd(SRU_NS, "extraResponseData", true);
                }

                reader.readEnd(SRU_NS, "searchRetrieveResponse");
            }
        } catch (XMLStreamException e) {
            throw new SRUClientException("error parsing response", e);
        }
    }


    private static boolean parseDiagnostics(SRUXMLStreamReader reader)
            throws XMLStreamException, SRUClientException {
        if (reader.readStart(SRU_NS, "diagnostics", false)) {
            boolean first = true;
            while (reader.readStart(SRU_DIAGNOSIC_NS, "diagnostic", first)) {
                first = false;
                // diagnostic/uri
                String uri = reader.readContent(SRU_DIAGNOSIC_NS, "uri", true);

                // diagnostic/details
                String details = reader.readContent(SRU_DIAGNOSIC_NS,
                        "details", false);

                // diagnostic/message
                String message = reader.readContent(SRU_DIAGNOSIC_NS,
                        "message", false);

                reader.readEnd(SRU_DIAGNOSIC_NS, "diagnostic");

                logger.info("diagostic: uri={}, detail={}, message={}",
                        new Object[] { uri, details, message });
            }
            reader.readEnd(SRU_NS, "diagnostics");
            return true;
        } else {
            return false;
        }
    }

    
    private static void parseRecordExplain(SRUXMLStreamReader reader)
            throws SRUClientException {
        try {
            if (reader.readStart(SRU_NS, "record", true)) {
                String schema =
                        reader.readContent(SRU_NS, "recordSchema", true);

                String packing =
                        reader.readContent(SRU_NS, "recordPacking", false);

                logger.debug("schema = {}, packing = {}", schema, packing);

                // explainResponse/record/recordData
                reader.readStart(SRU_NS, "recordData", true);
                reader.readEnd(SRU_NS, "recordData", true);

                String recordPosition =
                        reader.readContent(SRU_NS, "recordPosition", false);
                logger.debug("recordPosition = {}", recordPosition);

                if (reader.readStart(SRU_NS, "extraRecordData", false)) {
                    reader.readEnd(SRU_NS, "extraRecordData", true);
                }

                reader.readEnd(SRU_NS, "record");
            }
        } catch (XMLStreamException e) {
            throw new SRUClientException("error parsing record", e);
        }
    }

    
    private final URI makeURI(Operation operation,
            Map<String, String> arguments) {
        StringBuilder uri = new StringBuilder(endpointURI);
        uri.append("?operation=");
        switch (operation) {
        case OP_EXPLAIN:
            uri.append("explain");
            break;
        case OP_SCAN:
            uri.append("scan");
            break;
        case OP_SEARCH_RETRIEVE:
            uri.append("searchRetrieve");
            break;
        }

        uri.append("&version=");
        switch (version) {
        case VERSION_1_1:
            uri.append("1.1");
            break;
        case VERSION_1_2:
            uri.append("1.2");
            break;
        }

        if ((arguments != null) && !arguments.isEmpty()) {
            for (Entry<String, String> entry : arguments.entrySet()) {
                uri.append('&')
                    .append(entry.getKey())
                    .append('=')
                    .append(entry.getValue());
            }
        }
        return URI.create(uri.toString());
    }


    private SRUXMLStreamReader createReader(InputStream in)
            throws XMLStreamException {
        return new SRUXMLStreamReader(in);
    }

} // class SRUClient

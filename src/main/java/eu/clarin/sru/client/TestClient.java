package eu.clarin.sru.client;

import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.fcs.ClarinFederatedContentSearchRecordData;
import eu.clarin.sru.fcs.ClarinFederatedContentSearchRecordParser;


public class TestClient {
    private static final Logger logger =
            LoggerFactory.getLogger(TestClient.class);

    public static void main(String[] args) {
        if (args.length > 0) {
            logger.info("initializing client ...");
            SRUClient client = new SRUClient(SRUVersion.VERSION_1_2);
            try {
                client.registerRecordParser(new ClarinFederatedContentSearchRecordParser());
            } catch (SRUClientException e) {
                logger.error("error adding record parser", e);
                System.exit(1);
            }

            SRUDefaultHandlerAdapter handler = new SRUDefaultHandlerAdapter() {
                @Override
                public void onDiagnostics(List<SRUDiagnostic> diagnostics)
                        throws SRUClientException {
                    for (SRUDiagnostic diagnostic : diagnostics) {
                        logger.info("onDiagnostics: uri={}, detail={}, message={}",
                                new Object[] { diagnostic.getURI(),
                                        diagnostic.getDetails(),
                                        diagnostic.getMessage() });
                    }
                }

                @Override
                public void onRequestStatistics(int bytes, long millisTotal,
                        long millisNetwork, long millisParsing) {
                    logger.info("onRequestStatistics(): {} bytes in {} millis",
                            bytes, millisTotal);
                }

                @Override
                public void onStartTerms() throws SRUClientException {
                    logger.info("onStartTerms()");
                }

                @Override
                public void onFinishTerms() throws SRUClientException {
                    logger.info("onFinishTerms()");
                }

                @Override
                public void onTerm(String value, int numberOfRecords,
                        String displayTerm, WhereInList whereInList)
                        throws SRUClientException {
                    logger.info("onTerm(): value = {}, numberOfRecords = {}, displayTerm = {}, whereInList = {}",
                            new Object[] { value, numberOfRecords, displayTerm,
                                    whereInList });
                }

                @Override
                public void onStartRecords(int numberOfRecords,
                        int resultSetId, int resultSetIdleTime)
                        throws SRUClientException {
                    logger.info("onStartRecords(): numberOfRecords = {}",
                            numberOfRecords);
                }

                @Override
                public void onFinishRecords(int nextRecordPosition)
                        throws SRUClientException {
                    logger.info("onFinishRecords(): nextRecordPosition = {}",
                            nextRecordPosition);
                }

                @Override
                public void onRecord(String schema, String identifier,
                        int position, XMLStreamReader reader)
                        throws XMLStreamException, SRUClientException {
                    logger.info("onRecord(): schema = {}, identifier = {}, position = {}",
                            new Object[] { schema, identifier, position });
                    /* just try to read ... */
                    while (reader.hasNext()) {
                        reader.next();
                    }
                }

                @Override
                public void onRecord(String identifier, int position,
                        SRURecordData data) throws SRUClientException {
                    logger.info("onRecord(): identifier = {}, position = {}, schema = {}",
                            new Object[] { identifier, position,
                                    data.getRecordSchema() });
                    if (ClarinFederatedContentSearchRecordParser.FCS_NS
                            .equals(data.getRecordSchema())) {
                        ClarinFederatedContentSearchRecordData record = (ClarinFederatedContentSearchRecordData) data;
                        logger.info("CLARIN-FCS: \"{}\"/\"{}\"/\"{}\"",
                                new Object[] { record.getLeft(),
                                        record.getKeyword(), record.getRight() });
                    }
                }

                @Override
                public void onSurrogateRecord(String identifier, int position,
                        SRUDiagnostic data) throws SRUClientException {
                    logger.info("onSurrogateRecord: identifier = {}, position = {}, uri={}, detail={}, message={}",
                            new Object[] { identifier, position, data.getURI(),
                                    data.getDetails(), data.getMessage() });
                }
            };

            try {
                logger.info("performing 'explain' request ...");
                SRUExplainRequest request = new SRUExplainRequest(args[0]);
                client.explain(request, handler);
            } catch (SRUClientException e) {
                logger.error("a fatal error occured while performing 'explain' request", e);
            }

            try {
                logger.info("performing 'scan' request ...");
                SRUScanRequest request = new SRUScanRequest(args[0]);
                request.setScanClause("cmd.collections");
                request.setMaximumTerms(2);
//                request.setExtraRequestData(
//                        SRUScanRequest.X_MALFORMED_OPERATION,
//                        SRUScanRequest.MALFORMED_OMIT);
//                request.setExtraRequestData(
//                        SRUAbstractRequest.X_MALFORMED_VERSION,
//                        SRUAbstractRequest.MALFORMED_OMIT);
                client.scan(request, handler);
            } catch (SRUClientException e) {
                logger.error("a fatal error occured while performing 'scan' request", e);
            }

            try {
                logger.info("performing 'searchRetrieve' request ...");
                SRUSearchRetrieveRequest request =
                        new SRUSearchRetrieveRequest(args[0]);
                request.setQuery("Faustus");
                request.setRecordSchema(ClarinFederatedContentSearchRecordParser.FCS_RECORD_SCHEMA);
                request.setMaximumRecords(5);
                request.setRecordPacking(SRURecordPacking.XML);
                request.setExtraRequestData("x-indent-response", "4");
//                request.setExtraRequestData(
//                        SRUScanRequest.X_MALFORMED_OPERATION,
//                        "invalid");
//                request.setExtraRequestData(
//                        SRUScanRequest.X_MALFORMED_VERSION,
//                        SRUScanRequest.MALFORMED_OMIT);
                client.searchRetrieve(request, handler);
            } catch (SRUClientException e) {
                logger.error("a fatal error occured while performing 'searchRetrieve' request", e);
            }
        } else {
            System.err.println("missing args");
            System.exit(64);
        }
    }

    static {
        org.apache.log4j.BasicConfigurator.configure(
                new org.apache.log4j.ConsoleAppender(
                        new org.apache.log4j.PatternLayout("%-5p [%t] %m%n"),
                        org.apache.log4j.ConsoleAppender.SYSTEM_ERR));
        org.apache.log4j.Logger logger =
                org.apache.log4j.Logger.getRootLogger();
        logger.setLevel(org.apache.log4j.Level.INFO);
        logger.getLoggerRepository()
            .getLogger("eu.clarin").setLevel(org.apache.log4j.Level.DEBUG);
    }
} // class TestClient

package eu.clarin.sru.client;

import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.fcs.ClarinFederatedContentSearchRecordData;
import eu.clarin.sru.fcs.ClarinFederatedContentSearchRecordParser;

public class TestThreadedClient {
    private static final Logger logger =
            LoggerFactory.getLogger(TestThreadedClient.class);

    public static void main(String[] args) {
        if (args.length > 0) {
            logger.info("initializing client ...");
            SRUThreadedClient client = new SRUThreadedClient();

            try {
                client.registerRecordParser(new ClarinFederatedContentSearchRecordParser());
            } catch (SRUClientException e) {
                logger.error("error adding record parser", e);
                System.exit(1);
            }

            try {
                /*
                 * the following requests will be run asynchronously and
                 * concurrently
                 */
                logger.info("submitting 'explain' request ...");
                SRUExplainRequest request1 = new SRUExplainRequest(args[0]);
                Future<SRUExplainResponse> result1 = client.explain(request1);

                logger.info("submitting 'scan' request ...");
                SRUScanRequest request2 = new SRUScanRequest(args[0]);
                request2.setScanClause("fcs.resource");
                Future<SRUScanResponse> result2 = client.scan(request2);

                logger.info("submitting 'searchRetrieve' request ...");
                SRUSearchRetrieveRequest request3 =
                        new SRUSearchRetrieveRequest(args[0]);
                request3.setQuery("Faustus");
                request3.setRecordSchema(ClarinFederatedContentSearchRecordData.RECORD_SCHEMA);
                request3.setMaximumRecords(5);
                Future<SRUSearchRetrieveResponse> result3 =
                        client.searchRetrieve(request3);

                /*
                 * HACK: the following code is quick and dirty. Don't every busy
                 * wait on responses like this in a real-world application!
                 */
                while (!(result1.isDone() && result2.isDone() &&
                        result3.isDone())) {
                    logger.debug("waiting for results ...");
                    try {
                        Thread.sleep(125);
                    } catch (InterruptedException e) {
                        /* IGNORE */
                    }
                }

                printExplainResponse(result1.get());
                printScanResponse(result2.get());
                printSearchResponse(result3.get());
            } catch (Exception e) {
                logger.error("a fatal error occured while performing request", e);
            }

            client.shutdown();
            logger.info("done");
        } else {
            System.err.println("missing args");
            System.exit(64);
        }
    }


    private static void printExplainResponse(SRUExplainResponse response) {
        logger.info("displaying results of 'explain' request ...");
        if (response.hasDiagnostics()) {
            for (SRUDiagnostic diagnostic : response.getDiagnostics()) {
                logger.info("uri={}, message={}, detail={}",
                        new Object[] { diagnostic.getURI(),
                                diagnostic.getMessage(),
                                diagnostic.getDetails() });
            }
        }
        if (response.hasRecord()) {
            SRURecord record = response.getRecord();
            logger.info("schema = {}", record.getRecordSchema());
        }
    }


    private static void printScanResponse(SRUScanResponse response) {
        logger.info("displaying results of 'scan' request ...");
        if (response.hasDiagnostics()) {
            for (SRUDiagnostic diagnostic : response.getDiagnostics()) {
                logger.info("uri={}, message={}, detail={}",
                        new Object[] {
                            diagnostic.getURI(),
                            diagnostic.getMessage(),
                            diagnostic.getDetails()
                            });
            }
        }
        if (response.hasTerms()) {
            for (SRUTerm term : response.getTerms()) {
                logger.info(
                        "value={}, numberOfRecords={}, displayTerm={}",
                        new Object[] { term.getValue(),
                                term.getNumberOfRecords(),
                                term.getDisplayTerm() });
            }
        } else {
            logger.info("no terms");
        }
    }


    private static void printSearchResponse(SRUSearchRetrieveResponse response) {
        logger.info("displaying results of 'searchRetrieve' request ...");
        logger.info("numberOfRecords = {}, nextResultPosition = {}",
                new Object[] { response.getNumberOfRecords(),
                        response.getNextRecordPosition() });
        if (response.hasDiagnostics()) {
            for (SRUDiagnostic diagnostic : response.getDiagnostics()) {
                logger.info("uri={}, message={}, detail={}",
                        new Object[] { diagnostic.getURI(),
                                diagnostic.getMessage(),
                                diagnostic.getDetails() });
            }
        }
        if (response.hasRecords()) {
            for (SRURecord record : response.getRecords()) {
                logger.info("schema = {}, identifier = {}, position = {}",
                        new Object[] { record.getRecordSchema(),
                                record.getRecordIdentifier(),
                                record.getRecordPosition() });
                if (record.isRecordSchema(ClarinFederatedContentSearchRecordData.RECORD_SCHEMA)) {
                    ClarinFederatedContentSearchRecordData r =
                            (ClarinFederatedContentSearchRecordData) record.getRecordData();
                    logger.info("CLARIN-FCS: \"{}\"/\"{}\"/\"{}\"",
                            new Object[] { r.getLeft(), r.getKeyword(), r.getRight() });
                } else if (record.isRecordSchema(SRUSurrogateRecordData.RECORD_SCHEMA)) {
                    SRUSurrogateRecordData r =
                            (SRUSurrogateRecordData) record.getRecordData();
                    logger.info("SURROGATE DIAGNOSTIC: uri={}, message={}, detail={}",
                            new Object[] { r.getURI(), r.getMessage(),
                                    r.getDetails() });
                } else {
                    logger.info("UNKNOWN RECORD SCHEMA");
                }
            }
        } else {
            logger.info("no results");
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
        logger.getLoggerRepository().getLogger("eu.clarin").setLevel(
                org.apache.log4j.Level.DEBUG);
    }

}

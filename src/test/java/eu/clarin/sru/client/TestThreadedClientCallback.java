package eu.clarin.sru.client;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.fcs.ClarinFederatedContentSearchRecordData;
import eu.clarin.sru.fcs.ClarinFederatedContentSearchRecordParser;

public class TestThreadedClientCallback {
    private static final Logger logger =
            LoggerFactory.getLogger(TestThreadedClientCallback.class);

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
                final CountDownLatch latch = new CountDownLatch(3);

                /*
                 * the following requests will be run asynchronously and
                 * concurrently
                 * Invoke requests and supply a callback, that
                 *  a) prints the results
                 *  b) downs a latch (which is used to make the main-thread wait
                 *     on the requests to be completed)
                 */
                logger.info("submitting 'explain' request ...");
                SRUExplainRequest request1 = new SRUExplainRequest(args[0]);
                client.explain(request1, new SRUCallback<SRUExplainResponse>() {
                    @Override
                    public void done(SRUExplainResponse response) {
                        printExplainResponse(response);
                        latch.countDown();
                    }
                });

                logger.info("submitting 'scan' request ...");
                SRUScanRequest request2 = new SRUScanRequest(args[0]);
                request2.setScanClause("fcs.resource");
                client.scan(request2, new SRUCallback<SRUScanResponse>() {
                    @Override
                    public void done(SRUScanResponse response) {
                        printScanResponse(response);
                        latch.countDown();
                    }
                });

                logger.info("submitting 'searchRetrieve' request ...");
                SRUSearchRetrieveRequest request3 =
                        new SRUSearchRetrieveRequest(args[0]);
                request3.setQuery("Faustus");
                request3.setRecordSchema(ClarinFederatedContentSearchRecordData.RECORD_SCHEMA);
                request3.setMaximumRecords(5);
                client.searchRetrieve(request3, new SRUCallback<SRUSearchRetrieveResponse>() {
                    @Override
                    public void done(SRUSearchRetrieveResponse response) {
                        printSearchResponse(response);
                        latch.countDown();
                    }
                });

                latch.await();
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

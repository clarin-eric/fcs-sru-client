package eu.clarin.sru.client;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.fcs.ClarinFederatedContentSearchRecordData;
import eu.clarin.sru.fcs.ClarinFederatedContentSearchRecordParser;
import eu.clarin.sru.fcs.DataView;
import eu.clarin.sru.fcs.KWICDataView;
import eu.clarin.sru.fcs.Resource;


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
                client.explain(request1, new SRUCallback<SRUExplainRequest, SRUExplainResponse>() {
                    @Override
                    public void onSuccess(SRUExplainResponse response) {
                        printExplainResponse(response);
                        latch.countDown();
                    }

                    @Override
                    public void onError(SRUExplainRequest request,
                            SRUClientException error) {
                        logger.error("error while performing request", error);
                        latch.countDown();
                    }
                });

                logger.info("submitting 'scan' request ...");
                SRUScanRequest request2 = new SRUScanRequest(args[0]);
                request2.setScanClause("fcs.resource");
                request2.setExtraRequestData("x-clarin-resource-info", "true");
                client.scan(request2, new SRUCallback<SRUScanRequest, SRUScanResponse>() {
                    @Override
                    public void onSuccess(SRUScanResponse response) {
                        printScanResponse(response);
                        latch.countDown();
                    }

                    @Override
                    public void onError(SRUScanRequest request,
                            SRUClientException error) {
                        logger.error("error while performing request", error);
                        latch.countDown();
                    }
                });

                logger.info("submitting 'searchRetrieve' request ...");
                SRUSearchRetrieveRequest request3 =
                        new SRUSearchRetrieveRequest(args[0]);
                request3.setQuery("Faustus");
                request3.setRecordSchema(ClarinFederatedContentSearchRecordData.RECORD_SCHEMA);
                request3.setMaximumRecords(5);
                client.searchRetrieve(request3, new SRUCallback<SRUSearchRetrieveRequest, SRUSearchRetrieveResponse>() {
                    @Override
                    public void onSuccess(SRUSearchRetrieveResponse response) {
                        printSearchResponse(response);
                        latch.countDown();
                    }

                    @Override
                    public void onError(SRUSearchRetrieveRequest request,
                            SRUClientException error) {
                        logger.error("error while performing request", error);
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
        logger.info("request time (in millis): {} total, {} queued, {} " +
                "network, {} processing; {} bytes transferred",
            new Object[] { response.getTimeTotal(),
                    response.getTimeWait(),
                    response.getTimeNetwork(),
                    response.getTimeProcessing(),
                    response.getTotalBytesTransferred() });
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
                if (term.hasExtraTermData()) {
                    logger.debug("extra term data is attached");
                }
            }
        } else {
            logger.info("no terms");
        }
        logger.info("request time (in millis): {} total, {} queued, {} " +
                    "network, {} processing; {} bytes transferred",
                new Object[] { response.getTimeTotal(),
                        response.getTimeWait(),
                        response.getTimeNetwork(),
                        response.getTimeProcessing(),
                        response.getTotalBytesTransferred() });
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
                    ClarinFederatedContentSearchRecordData rd =
                            (ClarinFederatedContentSearchRecordData) record.getRecordData();
                    dumpResource(rd.getResource());
                } else if (record.isRecordSchema(SRUSurrogateRecordData.RECORD_SCHEMA)) {
                    SRUSurrogateRecordData r =
                            (SRUSurrogateRecordData) record.getRecordData();
                    logger.info("SURROGATE DIAGNOSTIC: uri={}, message={}, detail={}",
                            new Object[] { r.getURI(), r.getMessage(),
                                    r.getDetails() });
                } else {
                    logger.info("UNSUPPORTED SCHEMA: {}",
                            record.getRecordSchema());
                }
            }
        } else {
            logger.info("no results");
        }
        logger.info("request time (in millis): {} total, {} queued, {} " +
                "network, {} processing; {} bytes transferred",
            new Object[] { response.getTimeTotal(),
                    response.getTimeWait(),
                    response.getTimeNetwork(),
                    response.getTimeProcessing(),
                    response.getTotalBytesTransferred() });
    }


    private static void dumpResource(Resource resource) {
        logger.info("CLARIN-FCS: pid={}, ref={}",
                resource.getPid(), resource.getRef());
        if (resource.hasDataViews()) {
            dumpDataView("CLARIN-FCS: ", resource.getDataViews());
        }
        if (resource.hasResourceFragments()) {
            for (Resource.ResourceFragment fragment : resource.getResourceFragments()) {
                logger.debug("CLARIN-FCS: ResourceFragment: pid={}, ref={}",
                        fragment.getPid(), fragment.getRef());
                if (fragment.hasDataViews()) {
                    dumpDataView("CLARIN-FCS: ResourceFragment/", fragment.getDataViews());
                }
            }
        }
    }


    private static void dumpDataView(String s, List<DataView> dataviews) {
        for (DataView dataview : dataviews) {
            logger.info("{}DataView: type={}, pid={}, ref={}",
                    new Object[] {
                        s,
                        dataview.getMimeType(),
                        dataview.getPid(),
                        dataview.getRef()
                    });
            if (dataview.isMimeType(KWICDataView.MIMETYPE)) {
                final KWICDataView kw = (KWICDataView) dataview;
                logger.info("{}DataView: {} / {} / {}",
                        new Object[] {
                            s,
                            kw.getLeft(),
                            kw.getKeyword(),
                            kw.getRight() });
            }
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

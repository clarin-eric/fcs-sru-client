package eu.clarin.sru.client;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.fcs.ClarinFederatedContentSearchRecordData;
import eu.clarin.sru.fcs.ClarinFederatedContentSearchRecordParser;
import eu.clarin.sru.fcs.DataView;
import eu.clarin.sru.fcs.KWICDataView;
import eu.clarin.sru.fcs.Resource;


public class TestClient {
    private static final Logger logger =
            LoggerFactory.getLogger(TestClient.class);


    public static void main(String[] args) {
        if (args.length > 0) {
            logger.info("initializing client ...");
            SRUClient client = new SRUClient();

            try {
                client.registerRecordParser(new ClarinFederatedContentSearchRecordParser());
            } catch (SRUClientException e) {
                logger.error("error adding record parser", e);
                System.exit(1);
            }

            // explain
            try {
                logger.info("performing 'explain' request ...");
                SRUExplainRequest request = new SRUExplainRequest(args[0]);
                SRUExplainResponse result = client.explain(request);
                logger.info("displaying results of 'explain' request ...");
                if (result.hasDiagnostics()) {
                    for (SRUDiagnostic diagnostic : result.getDiagnostics()) {
                        logger.info("uri={}, message={}, detail={}",
                                new Object[] { diagnostic.getURI(),
                                        diagnostic.getMessage(),
                                        diagnostic.getDetails() });
                    }
                }
                if (result.hasRecord()) {
                    SRURecord record = result.getRecord();
                    logger.info("schema = {}", record.getRecordSchema());
                }
            } catch (SRUClientException e) {
                logger.error("a fatal error occured while performing 'explain' request", e);
            }

            // scan
            try {
                logger.info("performing 'scan' request ...");
                SRUScanRequest request = new SRUScanRequest(args[0]);
                request.setScanClause("fcs.resource");
                SRUScanResponse result = client.scan(request);
                logger.info("displaying results of 'scan' request ...");
                if (result.hasDiagnostics()) {
                    for (SRUDiagnostic diagnostic : result.getDiagnostics()) {
                        logger.info("uri={}, message={}, detail={}",
                                new Object[] { diagnostic.getURI(),
                                        diagnostic.getMessage(),
                                        diagnostic.getDetails() });
                    }
                }
                if (result.hasTerms()) {
                    for (SRUTerm term : result.getTerms()) {
                        logger.info(
                                "value={}, numberOfRecords={}, displayTerm={}",
                                new Object[] { term.getValue(),
                                        term.getNumberOfRecords(),
                                        term.getDisplayTerm() });
                    }
                } else {
                    logger.info("no terms");
                }
            } catch (SRUClientException e) {
                logger.error("a fatal error occured while performing 'scan' request", e);
            }

            // searchRetrieve
            try {
                logger.info("performing 'searchRetrieve' request ...");
                SRUSearchRetrieveRequest request =
                        new SRUSearchRetrieveRequest(args[0]);
                request.setQuery("Faustus");
                request.setRecordSchema(ClarinFederatedContentSearchRecordData.RECORD_SCHEMA);
                request.setMaximumRecords(5);
                request.setRecordPacking(SRURecordPacking.XML);
                request.setExtraRequestData("x-indent-response", "4");
                SRUSearchRetrieveResponse result = client.searchRetrieve(request);
                logger.info("displaying results of 'searchRetrieve' request ...");
                logger.info("numberOfRecords = {}, nextResultPosition = {}",
                        new Object[] { result.getNumberOfRecords(),
                                result.getNextRecordPosition() });
                if (result.hasDiagnostics()) {
                    for (SRUDiagnostic diagnostic : result.getDiagnostics()) {
                        logger.info("uri={}, message={}, detail={}",
                                new Object[] { diagnostic.getURI(),
                                        diagnostic.getMessage(),
                                        diagnostic.getDetails() });
                    }
                }
                if (result.hasRecords()) {
                    for (SRURecord record : result.getRecords()) {
                        logger.info("schema = {}, identifier = {}, position = {}",
                                new Object[] { record.getRecordSchema(),
                                        record.getRecordIdentifier(),
                                        record.getRecordPosition() });
                        if (record.isRecordSchema(ClarinFederatedContentSearchRecordData.RECORD_SCHEMA)) {
                            final ClarinFederatedContentSearchRecordData cd =
                                    (ClarinFederatedContentSearchRecordData) record.getRecordData();
                            dumpResource(cd.getResource());
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

                logger.info("done");
            } catch (SRUClientException e) {
                logger.error("a fatal error occured while performing 'searchRetrieve' request", e);
            }
        } else {
            System.err.println("missing args");
            System.exit(64);
        }
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
        org.apache.log4j.BasicConfigurator
                .configure(new org.apache.log4j.ConsoleAppender(
                        new org.apache.log4j.PatternLayout("%-5p [%t] %m%n"),
                        org.apache.log4j.ConsoleAppender.SYSTEM_ERR));
        org.apache.log4j.Logger logger = org.apache.log4j.Logger
                .getRootLogger();
        logger.setLevel(org.apache.log4j.Level.INFO);
        logger.getLoggerRepository().getLogger("eu.clarin")
                .setLevel(org.apache.log4j.Level.DEBUG);
    }

}

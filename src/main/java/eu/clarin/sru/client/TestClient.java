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
            try {
                logger.info("initializing client ...");
                SRUClient client = new SRUClient(SRUVersion.VERSION_1_2);
                client.registerRecordParser(new ClarinFederatedContentSearchRecordParser());

                SRUDefaultHandlerAdapter handler = new SRUDefaultHandlerAdapter() {
                    @Override
                    public void onFatalError(List<SRUDiagnostic> diagnostics)
                            throws SRUClientException {
                        for (SRUDiagnostic diagnostic : diagnostics) {
                            logger.info(
                                    "onFatalError: uri={}, detail={}, message={}",
                                    new Object[] { diagnostic.getURI(),
                                            diagnostic.getDetails(),
                                            diagnostic.getMessage() });
                        }
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
                        logger.info("onTerm() value = {}, numberOfRecords = {}, displayTerm = {}, whereInList = {}",
                                new Object[] { value, numberOfRecords,
                                        displayTerm, whereInList });
                    }


                    @Override
                    public void onStartRecords() throws SRUClientException {
                        logger.info("onStartRecords()");
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
                            logger.info("CLARIN-FCS: \"{}\"/\"{}\"/\"{}\"", new Object[] { record.getLeft(), record.getKeyword(), record.getRight() });
                        }
                    }
                };

                logger.info("performing 'explain' request ...");
                SRUExplainRequest r1 = new SRUExplainRequest(args[0]);
                client.explain(r1, handler);

                logger.info("performing 'scan' request ...");
                SRUScanRequest r2 = new SRUScanRequest(args[0]);
                r2.setScanClause("cmd.collections");
                client.scan(r2, handler);

                logger.info("performing 'scan' request ...");
                SRUSearchRetrieveRequest r3 =
                        new SRUSearchRetrieveRequest(args[0]);
                r3.setQuery("Gott");
                client.searchRetrieve(r3, handler);
            } catch (SRUClientException e) {
                logger.error("some error occured", e);
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

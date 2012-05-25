package eu.clarin.sru.client;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestClient {
    private static final Logger logger =
            LoggerFactory.getLogger(TestClient.class);

    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                logger.info("initializing client ...");
                SRUClient client =
                        new SRUClient(args[0], SRUVersion.VERSION_1_2);

                SRUDefaultHandlerAdapter handler = new SRUDefaultHandlerAdapter() {
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
                };

//                logger.info("performing 'explain' request ...");
//                client.explain(handler);

                logger.info("performing 'scan' request ...");
                client.scan("cmd.collections", handler);

                logger.info("performing 'scan' request ...");
                client.searchRetrieve("Gott", handler);
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

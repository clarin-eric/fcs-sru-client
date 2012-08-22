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

import java.util.List;

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
            SRUSimpleClient client = new SRUSimpleClient(SRUVersion.VERSION_1_2);
            try {
                client.registerRecordParser(new ClarinFederatedContentSearchRecordParser());
            } catch (SRUClientException e) {
                logger.error("error adding record parser", e);
                System.exit(1);
            }

            /*
             * just use one dump handler for each request.
             * A real application should be smarter here and use the
             * appropriate handler 
             */
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
//                        SRUAbstractRequest.X_MALFORMED_OPERATION,
//                        SRUAbstractRequest.MALFORMED_OMIT);
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
//                        SRUAbstractRequest.X_MALFORMED_OPERATION,
//                        "invalid");
//                request.setExtraRequestData(
//                        SRUAbstractRequest.X_MALFORMED_VERSION,
//                        SRUAbstractRequest.MALFORMED_OMIT);
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

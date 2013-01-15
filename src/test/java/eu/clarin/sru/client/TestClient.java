/**
 * This software is copyright (c) 2011-2013 by
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.sru.fcs.ClarinFCSRecordParser;


public class TestClient {
    private static final Logger logger =
            LoggerFactory.getLogger(TestClient.class);


    public static void main(String[] args) {
        if (args.length > 0) {
            logger.info("initializing client ...");
            SRUClient client = new SRUClient();

            try {
                client.registerRecordParser(new ClarinFCSRecordParser());
            } catch (SRUClientException e) {
                logger.error("error adding record parser", e);
                System.exit(1);
            }

            // explain
            try {
                logger.info("performing 'explain' request ...");
                SRUExplainRequest request = TestUtils.makeExplainRequest(args[0]);
                SRUExplainResponse response = client.explain(request);
                TestUtils.printExplainResponse(response);
            } catch (SRUClientException e) {
                logger.error("a fatal error occured while performing 'explain' request", e);
            }

            // scan
            try {
                logger.info("performing 'scan' request ...");
                SRUScanRequest request = TestUtils.makeScanRequest(args[0]);
                SRUScanResponse response = client.scan(request);
                TestUtils.printScanResponse(response);
            } catch (SRUClientException e) {
                logger.error("a fatal error occured while performing 'scan' request", e);
            }

            // searchRetrieve
            try {
                logger.info("performing 'searchRetrieve' request ...");
                SRUSearchRetrieveRequest request = TestUtils.makeSearchRequest(args[0], null);
                SRUSearchRetrieveResponse response = client.searchRetrieve(request);
                TestUtils.printSearchResponse(response);
            } catch (SRUClientException e) {
                logger.error("a fatal error occured while performing 'searchRetrieve' request", e);
            }

            logger.info("done");
        } else {
            System.err.println("missing args");
            System.exit(64);
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

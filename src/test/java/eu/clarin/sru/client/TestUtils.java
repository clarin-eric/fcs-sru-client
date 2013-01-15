/**
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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import eu.clarin.sru.fcs.ClarinFCSRecordData;
import eu.clarin.sru.fcs.DataView;
import eu.clarin.sru.fcs.DataViewGenericDOM;
import eu.clarin.sru.fcs.DataViewGenericString;
import eu.clarin.sru.fcs.DataViewKWIC;
import eu.clarin.sru.fcs.Resource;

class TestUtils {
    private static final Logger logger =
            LoggerFactory.getLogger(TestUtils.class);

    public static SRUExplainRequest makeExplainRequest(String baseURI) {
        return new SRUExplainRequest(baseURI);
    }


    public static SRUScanRequest makeScanRequest(String baseURI) {
        SRUScanRequest request = new SRUScanRequest(baseURI);
        request.setScanClause("fcs.resource");
        request.setExtraRequestData("x-clarin-resource-info", "true");
        return request;
    }


    public static SRUSearchRetrieveRequest makeSearchRequest(String baseURI, String query) {
        if ((query == null) || query.isEmpty()) {
            query = "Faustus";
        }
        SRUSearchRetrieveRequest request = new SRUSearchRetrieveRequest(baseURI);
        request.setQuery(query);
        request.setRecordSchema(ClarinFCSRecordData.RECORD_SCHEMA);
        request.setMaximumRecords(5);
        request.setRecordPacking(SRURecordPacking.XML);
//        request.setExtraRequestData("x-indent-response", "4");
        return request;
    }


    public static void printExplainResponse(SRUExplainResponse response) {
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


    public static void printScanResponse(SRUScanResponse response) {
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


    public static void printSearchResponse(SRUSearchRetrieveResponse response) {
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
                if (record.isRecordSchema(ClarinFCSRecordData.RECORD_SCHEMA)) {
                    ClarinFCSRecordData rd =
                            (ClarinFCSRecordData) record.getRecordData();
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
    }


    public static void dumpResource(Resource resource) {
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
            if (dataview instanceof DataViewGenericDOM) {
                final DataViewGenericDOM view = (DataViewGenericDOM) dataview;
                final Node root = view.getDocument().getFirstChild();
                logger.info("{}DataView: root element <{}> / {}",
                        new Object[] {
                            s,
                            root.getNodeName(),
                            root.getOwnerDocument().hashCode() });
            } else if (dataview instanceof DataViewGenericString) {
                final DataViewGenericString view = (DataViewGenericString) dataview;
                logger.info("{}DataView: data = {}",
                        new Object[] {
                            s,
                            view.getContent() });
            } else if (dataview.isMimeType(DataViewKWIC.TYPE)) {
                final DataViewKWIC kw = (DataViewKWIC) dataview;
                logger.info("{}DataView: {} / {} / {}",
                        new Object[] {
                            s,
                            kw.getLeft(),
                            kw.getKeyword(),
                            kw.getRight() });
            }
        }
    }

} // class TestUtils

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

import eu.clarin.sru.client.SRUExplainRecordData.ConfigInfo;
import eu.clarin.sru.client.SRUExplainRecordData.Schema;
import eu.clarin.sru.client.fcs.ClarinFCSRecordData;
import eu.clarin.sru.client.fcs.DataView;
import eu.clarin.sru.client.fcs.DataViewGenericDOM;
import eu.clarin.sru.client.fcs.DataViewGenericString;
import eu.clarin.sru.client.fcs.DataViewKWIC;
import eu.clarin.sru.client.fcs.Resource;

class TestUtils {
    private static final Logger logger =
            LoggerFactory.getLogger(TestUtils.class);

    public static SRUExplainRequest makeExplainRequest(String baseURI) {
        SRUExplainRequest request = new SRUExplainRequest(baseURI);
        request.setExtraRequestData("x-indent-response", "4");
        request.setParseRecordDataEnabled(true);
        return request;
    }


    public static SRUScanRequest makeScanRequest(String baseURI) {
        SRUScanRequest request = new SRUScanRequest(baseURI);
        request.setScanClause("fcs.resource = root");
        request.setExtraRequestData("x-clarin-resource-info", "true");
        request.setExtraRequestData("x-indent-response", "4");
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
        request.setExtraRequestData("x-indent-response", "4");
        return request;
    }


    public static void printExplainResponse(SRUExplainResponse response) {
        logger.info("displaying results of 'explain' request ...");
        if (response.hasDiagnostics()) {
            for (SRUDiagnostic diagnostic : response.getDiagnostics()) {
                logger.info("uri={}, message={}, detail={}",
                        diagnostic.getURI(),
                        diagnostic.getMessage(),
                        diagnostic.getDetails());
            }
        }
        if (response.hasRecord()) {
            SRURecord record = response.getRecord();
            logger.info("schema = {}", record.getRecordSchema());
            if (record.isRecordSchema(SRUExplainRecordData.RECORD_SCHEMA)) {
                dumpExplainRecordData(record.getRecordData());
            }
        }
    }


    public static void printScanResponse(SRUScanResponse response) {
        logger.info("displaying results of 'scan' request ...");
        if (response.hasDiagnostics()) {
            for (SRUDiagnostic diagnostic : response.getDiagnostics()) {
                logger.info("uri={}, message={}, detail={}",
                        diagnostic.getURI(),
                        diagnostic.getMessage(),
                        diagnostic.getDetails());
            }
        }
        if (response.hasTerms()) {
            for (SRUTerm term : response.getTerms()) {
                logger.info("value={}, numberOfRecords={}, displayTerm={}",
                            term.getValue(),
                            term.getNumberOfRecords(),
                            term.getDisplayTerm());
            }
        } else {
            logger.info("no terms");
        }
    }


    public static void printSearchResponse(SRUSearchRetrieveResponse response) {
        logger.info("displaying results of 'searchRetrieve' request ...");
        logger.info("numberOfRecords = {}, nextResultPosition = {}",
                response.getNumberOfRecords(),
                response.getNextRecordPosition());
        if (response.hasDiagnostics()) {
            for (SRUDiagnostic diagnostic : response.getDiagnostics()) {
                logger.info("uri={}, message={}, detail={}",
                        diagnostic.getURI(),
                        diagnostic.getMessage(),
                        diagnostic.getDetails());
            }
        }
        if (response.hasRecords()) {
            for (SRURecord record : response.getRecords()) {
                logger.info("schema = {}, identifier = {}, position = {}",
                        record.getRecordSchema(),
                        record.getRecordIdentifier(),
                        record.getRecordPosition());
                if (record.isRecordSchema(ClarinFCSRecordData.RECORD_SCHEMA)) {
                    ClarinFCSRecordData rd =
                            (ClarinFCSRecordData) record.getRecordData();
                    dumpResource(rd.getResource());
                } else if (record.isRecordSchema(SRUSurrogateRecordData.RECORD_SCHEMA)) {
                    SRUSurrogateRecordData r =
                            (SRUSurrogateRecordData) record.getRecordData();
                    logger.info("SURROGATE DIAGNOSTIC: uri={}, message={}, detail={}",
                                r.getURI(), r.getMessage(), r.getDetails());
                } else {
                    logger.info("UNSUPPORTED SCHEMA: {}",
                            record.getRecordSchema());
                }
            }
        } else {
            logger.info("no results");
        }
    }


    public static void dumpExplainRecordData(SRURecordData recordData) {
        if (SRUExplainRecordData.RECORD_SCHEMA.equals(recordData.getRecordSchema())) {
            SRUExplainRecordData data = (SRUExplainRecordData) recordData;
            logger.info("host={}, port={}, database={}",
                    data.getServerInfo().getHost(),
                    data.getServerInfo().getPort(),
                    data.getServerInfo().getDatabase());
            List<Schema> schemaInfo = data.getSchemaInfo();
            if (schemaInfo != null) {
                for (Schema schema : schemaInfo) {
                    logger.debug("schema: identifier={}, name={}, " +
                            "location={}, sort={}, retrieve={}",
                            schema.getIdentifier(),
                            schema.getName(),
                            schema.getLocation(),
                            schema.getSort(),
                            schema.getRetrieve());
                }
            }
            ConfigInfo configInfo = data.getConfigInfo();
            if (configInfo != null) {
                if (configInfo.getDefaults() != null) {
                    logger.debug("configInfo/default = {}",
                            configInfo.getDefaults());
                }
                if (configInfo.getSettings() != null) {
                    logger.debug("configInfo/setting = {}",
                            configInfo.getSettings());
                }
                if (configInfo.getSupports() != null) {
                    logger.debug("configInfo/supports = {}",
                            configInfo.getSupports());
                }
            }
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
                    s, dataview.getMimeType(), dataview.getPid(),
                    dataview.getRef());
            if (dataview instanceof DataViewGenericDOM) {
                final DataViewGenericDOM view = (DataViewGenericDOM) dataview;
                final Node root = view.getDocument().getFirstChild();
                logger.info("{}DataView: root element <{}> / {}",
                        s, root.getNodeName(),
                        root.getOwnerDocument().hashCode());
            } else if (dataview instanceof DataViewGenericString) {
                final DataViewGenericString view = (DataViewGenericString) dataview;
                logger.info("{}DataView: data = {}", s, view.getContent());
            } else if (dataview.isMimeType(DataViewKWIC.TYPE)) {
                final DataViewKWIC kw = (DataViewKWIC) dataview;
                logger.info("{}DataView: {} / {} / {}",
                        s, kw.getLeft(), kw.getKeyword(), kw.getRight());
            }
        }
    }

} // class TestUtils

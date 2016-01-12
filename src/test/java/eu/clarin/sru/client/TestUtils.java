/**
 * This software is copyright (c) 2012-2014 by
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

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import eu.clarin.sru.client.SRUExplainRecordData.ConfigInfo;
import eu.clarin.sru.client.SRUExplainRecordData.Schema;
import eu.clarin.sru.client.fcs.ClarinFCSEndpointDescription;
import eu.clarin.sru.client.fcs.ClarinFCSEndpointDescription.ResourceInfo;
import eu.clarin.sru.client.fcs.ClarinFCSRecordData;
import eu.clarin.sru.client.fcs.DataView;
import eu.clarin.sru.client.fcs.DataViewGenericDOM;
import eu.clarin.sru.client.fcs.DataViewGenericString;
import eu.clarin.sru.client.fcs.DataViewHits;
import eu.clarin.sru.client.fcs.Resource;

class TestUtils {
    private static final Logger logger =
            LoggerFactory.getLogger(TestUtils.class);

    public static SRUExplainRequest makeExplainRequest(String baseURI) {
        SRUExplainRequest request = new SRUExplainRequest(baseURI);
        request.setExtraRequestData("x-indent-response", "4");
        request.setExtraRequestData("x-fcs-endpoint-description", "true");
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
//        request.setRecordSchema(ClarinFCSRecordData.LEGACY_RECORD_SCHEMA);
        request.setMaximumRecords(5);
        request.setRecordXmlEscaping(SRURecordXmlEscaping.XML);
        request.setRecordPacking(SRURecordPacking.PACKED);
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
            if (record.hasExtraRecordData()) {
                logger.info("extraRecordInfo = {}",
                        record.getExtraRecordData());
            }
        }
        if (response.hasExtraResponseData()) {
            for (SRUExtraResponseData data : response.getExtraResponseData()) {
                if (data instanceof ClarinFCSEndpointDescription) {
                    dumpEndpointDescription(
                            (ClarinFCSEndpointDescription) data);
                } else {
                    logger.info("extraResponseData = {} (class={})",
                            data.getRootElement(), data.getClass().getName());
                }

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


    private static void dumpEndpointDescription(ClarinFCSEndpointDescription ed) {
        logger.info("dumping <EndpointDescription> (version={})",
                ed.getVersion());
        for (URI capability : ed.getCapabilities()) {
            logger.info("  capability: {}", capability);
        } // for
        for (ClarinFCSEndpointDescription.DataView dataView :
            ed.getSupportedDataViews()) {
            logger.info("  supportedDataView: id={}, type={}, policy={}",
                    dataView.getIdentifier(),
                    dataView.getMimeType(),
                    dataView.getDeliveryPolicy());
        } // for
        dumpResourceInfo(ed.getResources(), 1, "  ");
    }


    private static void dumpResourceInfo(List<ResourceInfo> ris, int depth,
            String indent) {
        for (ResourceInfo ri : ris) {
            logger.info("{}[depth={}] <ResourceInfo>", indent, depth);
            logger.info("{}    pid={}", indent, ri.getPid());
            logger.info("{}    title: {}", indent, ri.getTitle());
            if (ri.getDescription() != null) {
                logger.info("{}    description: {}",
                        indent, ri.getDescription());
            }
            if (ri.getLandingPageURI() != null) {
                logger.info("{}    landingPageURI: {}",
                        indent, ri.getLandingPageURI());
            }
            for (ClarinFCSEndpointDescription.DataView dv :
                ri.getAvailableDataViews()) {
                logger.info("{}    available dataviews: type={}, policy={}",
                        indent, dv.getMimeType(), dv.getDeliveryPolicy());
            }
            if (ri.hasSubResources()) {
                dumpResourceInfo(ri.getSubResources(),
                        depth + 1, indent + "  ");
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
                logger.info("{}DataView (generic dom): root element <{}> / {}",
                        s, root.getNodeName(),
                        root.getOwnerDocument().hashCode());
            } else if (dataview instanceof DataViewGenericString) {
                final DataViewGenericString view =
                        (DataViewGenericString) dataview;
                logger.info("{}DataView (generic string): data = {}",
                        s, view.getContent());
            } else if (dataview instanceof DataViewHits) {
                final DataViewHits hits = (DataViewHits) dataview;
                logger.info("{}DataView: {}",
                        s, addHitHighlights(hits));
            } else {
                logger.info("{}DataView: cannot display " +
                        "contents of unexpected class '{}'",
                        s, dataview.getClass().getName());
            }
        }
    }


    private static String addHitHighlights(DataViewHits hits) {
        StringBuilder sb = new StringBuilder(hits.getText());
        int corr = 0;
        for (int i = 0; i < hits.getHitCount(); i++) {
            int[] offsets = hits.getHitOffsets(i);
            sb.insert(offsets[0] + corr, "[");
            corr += 1;
            sb.insert(offsets[1] + corr, "]");
            corr += 1;
        }
        return sb.toString();
    }

} // class TestUtils

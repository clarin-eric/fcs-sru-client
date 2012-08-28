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
package eu.clarin.sru.fcs;

import eu.clarin.sru.client.SRURecordData;


/**
 * A record parse to parse records conforming to CLARIN FCS specification. The
 * parser currently supports the KWIC view.
 */
public final class ClarinFederatedContentSearchRecordData implements
        SRURecordData {
    /**
     * The record schema for CLARIN FCS records.
     */
    public static final String RECORD_SCHEMA = "http://clarin.eu/fcs/1.0";
    private String pid;
    private String left;
    private String keyword;
    private String right;


    ClarinFederatedContentSearchRecordData(String pid, String left,
            String keyword, String right) {
        this.pid = pid;
        this.left = left;
        this.keyword = keyword;
        this.right = right;
    }


    @Override
    public boolean isTransient() {
        return false;
    }


    @Override
    public String getRecordSchema() {
        return ClarinFederatedContentSearchRecordParser.FCS_NS;
    }


    public String getPid() {
        return pid;
    }


    public String getLeft() {
        return left;
    }


    public String getKeyword() {
        return keyword;
    }


    public String getRight() {
        return right;
    }

} // class ClarinFederatedContentSearchRecordData

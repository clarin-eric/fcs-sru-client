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

import java.util.List;

import eu.clarin.sru.client.SRURecordData;


/**
 * A record data implementation for CLARIN FCS.
 */
public final class ClarinFederatedContentSearchRecordData implements
        SRURecordData {
    /**
     * The record schema for CLARIN FCS records.
     */
    public static final String RECORD_SCHEMA = "http://clarin.eu/fcs/1.0";
    private final Resource resource;


    ClarinFederatedContentSearchRecordData(String pid, String ref,
            List<DataView> dataviews,
            List<Resource.ResourceFragment> resourceFragments) {
        this.resource = new Resource(pid, ref, dataviews, resourceFragments);
    }


    @Override
    public boolean isTransient() {
        return false;
    }


    @Override
    public String getRecordSchema() {
        return RECORD_SCHEMA;
    }

    /**
     * Get the CLARIN FCS record resource.
     * 
     * @return a {@link Resource} object
     */
    public Resource getResource() {
        return resource;
    }

} // class ClarinFederatedContentSearchRecordData

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

/**
 * An object for performing a <em>explain</em> operation.
 *
 * @see SRUExplainHandler
 * @see <a href="http://www.loc.gov/standards/sru/specs/explain.html">SRU Explain Operation</a>
 */
public class SRUExplainRequest extends SRUAbstractRequest {
    private SRURecordPacking recordPacking;
    private boolean parseRecordDataEnabled = false;

    /**
     * Constructor.
     *
     * @param baseURI
     *            the baseURI of the endpoint
     */
    public SRUExplainRequest(URI baseURI) {
        super(baseURI);
    }


    /**
     * Constructor.
     *
     * @param baseURI
     *            the baseURI of the endpoint
     */
    public SRUExplainRequest(String baseURI) {
        super(baseURI);
    }


    /**
     * Set the requested record packing.
     *
     * @param recordPacking
     *            the requested record packing
     * @see SRURecordPacking
     */
    public void setRecordPacking(SRURecordPacking recordPacking) {
        if (recordPacking == null) {
            throw new NullPointerException("recordPacking == null");
        }
        this.recordPacking = recordPacking;
    }


    /**
     * Get the requested record packing.
     *
     * @return the requested record packing
     * @see SRURecordPacking
     */
    public SRURecordPacking getRecordPacking() {
        return recordPacking;
    }


    /**
     * Enable or disable parsing of explain record data (ZeeRex record) of the
     * explain response.
     *
     * @param enabled
     *            <code>true</code> enabled parsing, <code>false</code> disables
     *            parsing
     */
    public void setParseRecordDataEnabled(boolean enabled) {
        this.parseRecordDataEnabled = enabled;
    }


    /**
     * Check, whether the record data of a explain response (ZeeRex record)
     * shall be parsed or not.
     *
     * @return <code>true</code> if parsing is enabled, <code>false</code>
     *         otherwise
     */
    public boolean isParseRecordDataEnabled() {
        return parseRecordDataEnabled;
    }


    @Override
    SRUOperation getOperation() {
        return SRUOperation.EXPLAIN;
    }


    @Override
    void addParametersToURI(URIHelper uriHelper) throws SRUClientException {
        // recordPacking
        if (recordPacking != null) {
            switch (recordPacking) {
            case XML:
                uriHelper.append(PARAM_RECORD_PACKING, RECORD_PACKING_XML);
                break;
            case STRING:
                uriHelper.append(PARAM_RECORD_PACKING, RECORD_PACKING_STRING);
                break;
            default:
                throw new SRUClientException("unsupported record packing: " +
                        recordPacking);
            } // switch
        }
    }

} // class SRUExplainRequest

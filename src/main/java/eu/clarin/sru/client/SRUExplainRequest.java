/**
 * This software is copyright (c) 2012-2016 by
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
    private SRURecordXmlEscaping recordXmlEscaping;
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
     * Get the <em>recordXmlEscpaing</em> (SRU 2.0) or <em>recordPacking</em>
     * (SRU 1.1 and SRU 1.2) parameter of this request.
     *
     * @return the requested record packing
     * @see SRURecordXmlEscaping
     */
    public SRURecordXmlEscaping getRecordXmlEscaping() {
        return recordXmlEscaping;
    }


    /**
     * Set the <em>recordXmlEscpaing</em> (SRU 2.0) or <em>recordPacking</em>
     * (SRU 1.1 and SRU 1.2) parameter of this request.
     *
     * @param getRecordXmlEscaping
     *            the requested record XML escaping
     * @see SRURecordXmlEscaping
     */
    public void setRecordXmlEscaping(SRURecordXmlEscaping getRecordXmlEscaping) {
        if (getRecordXmlEscaping == null) {
            throw new NullPointerException("getRecordXmlEscaping == null");
        }
        this.recordXmlEscaping = getRecordXmlEscaping;
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


    @Override
    public SRUOperation getOperation() {
        return SRUOperation.EXPLAIN;
    }


    @Override
    void addParametersToURI(URIHelper uriHelper, SRUVersion version)
            throws SRUClientException {
        // recordXMLEscaping / recordPacking
        if (recordXmlEscaping != null) {
            switch (version) {
            case VERSION_1_1:
                /* $FALL-THROUGH$ */
            case VERSION_1_2:
                switch (recordXmlEscaping) {
                case XML:
                    uriHelper.append(PARAM_RECORD_PACKING,
                            RECORD_XML_ESCAPING_XML);
                    break;
                case STRING:
                    uriHelper.append(PARAM_RECORD_PACKING,
                            RECORD_XML_ESCPAING_STRING);
                    break;
                default:
                    throw new SRUClientException("unsupported record packing: " +
                            recordXmlEscaping);
                } // switch
                break;
            case VERSION_2_0:
                switch (recordXmlEscaping) {
                case XML:
                    uriHelper.append(PARAM_RECORD_XML_ESCAPING,
                            RECORD_XML_ESCAPING_XML);
                    break;
                case STRING:
                    uriHelper.append(PARAM_RECORD_XML_ESCAPING,
                            RECORD_XML_ESCPAING_STRING);
                    break;
                default:
                    throw new SRUClientException(
                            "unsupported record packing: " +
                                    recordXmlEscaping);
                }
                break;
            default:
                /* cannot happen */
                break;
            }
        }
    }

} // class SRUExplainRequest

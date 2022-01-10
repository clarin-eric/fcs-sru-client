/**
 * This software is copyright (c) 2012-2022 by
 *  - Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 * This is free software. You can redistribute it
 * and/or modify it under the terms described in
 * the GNU General Public License v3 of which you
 * should have received a copy. Otherwise you can download
 * it from
 *
 *   http://www.gnu.org/licenses/gpl-3.0.txt
 *
 * @copyright Leibniz-Institut fuer Deutsche Sprache (http://www.ids-mannheim.de)
 *
 * @license http://www.gnu.org/licenses/gpl-3.0.txt
 *  GNU General Public License v3
 */
package eu.clarin.sru.client;



/**
 * A record data implementation to hold surrogate diagnostics.
 */
public final class SRUSurrogateRecordData implements SRURecordData {
    /**
     * The record schema for SRU surrogate records.
     */
    public static final String RECORD_SCHEMA =
            "info:srw/schema/1/diagnostic-v1.1";
    private final SRUDiagnostic diagnostic;


    SRUSurrogateRecordData(SRUDiagnostic diagnostic) {
        this.diagnostic = diagnostic;
    }


    @Override
    public String getRecordSchema() {
        return RECORD_SCHEMA;
    }


    @Override
    public boolean isTransient() {
        return false;
    }


    /**
     * Get the surrogate diagnostic.
     *
     * @return the surrogate diagnostic
     */
    public SRUDiagnostic getDiagnostic() {
        return diagnostic;
    }


    /**
     * Convenience method to get diagnostic's identifying URI.
     *
     * @return diagnostic code
     */
    public String getURI() {
        return diagnostic.getURI();
    }


    /**
     * Convenience method to get supplementary information for this diagnostic.
     * The format for this value is often specified by the diagnostic code.
     *
     * @return supplementary information
     */
    public String getDetails() {
        return diagnostic.getDetails();
    }


    /**
     * Convenience method to get human readable message.
     *
     * @return human readable message
     */
    public String getMessage() {
        return diagnostic.getMessage();
    }

} // class SRUSurrogateRecordData

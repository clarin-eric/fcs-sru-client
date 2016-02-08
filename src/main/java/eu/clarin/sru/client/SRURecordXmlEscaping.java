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

/**
 * SRU record packing
 */
public enum SRURecordXmlEscaping {
    /**
     * XML record packing
     */
    XML {
        @Override
        String getStringValue() {
            return "xml";
        }
    },

    /**
     * String record packing
     */
    STRING {
        @Override
        String getStringValue() {
            return "string";
        }
    };

    abstract String getStringValue();

} // enum SRURecordPacking

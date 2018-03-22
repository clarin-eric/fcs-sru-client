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
 * SRU version
 */
public enum SRUVersion {
    /**
     * SRU/CQL version 1.1
     */
    VERSION_1_1 {
        @Override
        public int getVersionNumber() {
            return ((1 << 16) | 1);
        }
    },

    /**
     * SRU/CQL version 1.2
     */
    VERSION_1_2 {
        @Override
        public int getVersionNumber() {
            return ((1 << 16) | 2);
        }
    },

    /**
     * SRU/CQL version 2.0
     */
    VERSION_2_0 {
        @Override
        public int getVersionNumber() {
            return ((2 << 16) | 0);
        }
    };

    
    /**
     * Get a numerical representation of the version.
     * 
     * @return numerical representation of the version
     */
    public abstract int getVersionNumber();
    

    public boolean isVersion(SRUVersion version) {
        if (version == null) {
            throw new NullPointerException("version == null");
        }
        return (this == version);
    }


    public boolean isVersion(SRUVersion min, SRUVersion max) {
        if (min == null) {
            throw new NullPointerException("min == null");
        }
        if (max == null) {
            throw new NullPointerException("max == null");
        }
        return ((this.getVersionNumber() >= min.getVersionNumber()) &&
                (this.getVersionNumber() <= max.getVersionNumber()));
    }

} // enum SRUVersion

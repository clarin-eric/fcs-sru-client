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

import javax.xml.namespace.QName;

import org.w3c.dom.DocumentFragment;


/**
 * A class that provides a generic implementation for
 * {@link SRUExtraResponseData}. The parsed extra response data made available
 * by converting it into a DocumentFragment.
 */
public class SRUGenericExtraResponseData implements SRUExtraResponseData {
    private final QName name;
    private final DocumentFragment fragment;


    SRUGenericExtraResponseData(QName name, DocumentFragment fragment) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        this.name     = name;
        if (fragment == null) {
            throw new NullPointerException("fragment == null");
        }
        this.fragment = fragment;
    }


    @Override
    public QName getRootElement() {
        return name;
    }


    /**
     * Get the parsed extra response data as DocumentFragment
     *
     * @return the parsed extra response data as DocumentFragment
     */
    public DocumentFragment getDocumentFragment() {
        return fragment;
    }

} // class SRUGenericExtraResponseData

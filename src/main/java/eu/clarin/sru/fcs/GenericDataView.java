package eu.clarin.sru.fcs;

import org.w3c.dom.DocumentFragment;


public class GenericDataView extends DataView {
    private final DocumentFragment fragment;


    protected GenericDataView(String mimetype, String pid, String ref,
            DocumentFragment fragment) {
        super(mimetype, pid, ref);
        this.fragment = fragment;
    }


    public DocumentFragment getDocumentFragment() {
        return fragment;
    }

} // class GenericDataView

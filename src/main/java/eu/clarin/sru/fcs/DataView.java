package eu.clarin.sru.fcs;

import org.apache.commons.lang.NullArgumentException;

/**
 * Base class for DataView implementations according to the CLARIN FCS record
 * schema.
 */
public abstract class DataView {
    private final String mimetype;
    private final String pid;
    private final String ref;


    /**
     * Constructor.
     * 
     * @param mimetype
     *            the MIME type of this dataview
     * @param pid
     *            a persistent identifier or <code>null</code>
     * @param ref
     *            a reference URI or <code>null</code>
     * @throws NullArgumentException
     *             if a mandatory argument was not supplied
     * 
     */
    protected DataView(String mimetype, String pid, String ref) {
        if (mimetype == null) {
            throw new NullArgumentException("mimetype == null");
        }
        this.mimetype = mimetype;
        this.pid = ((pid != null) && !pid.isEmpty()) ? pid : null;
        this.ref = ((ref != null) && !ref.isEmpty()) ? ref : null;
    }


    /**
     * Get the MIME type of this DataView.
     * 
     * @return the MIME type of this DataView
     */
    public String getMimeType() {
        return mimetype;
    }


    /**
     * Convenience method to check if this DataView is of a certain MIME type.
     * 
     * @param mimetype
     *            the MIME type to test against
     * @return <code>true</code> if the DataView is in the supplied MIME type,
     *         <code>false</code> otherwise
     * @throws NullPointerException
     *             if any required arguments are not supplied
     */
    public boolean isMimeType(String mimetype) {
        if (mimetype == null) {
            throw new NullArgumentException("mimetype == null");
        }
        return (this.mimetype.equals(mimetype));
    }

    /**
     * Get the persistent identifier for this DataView.
     * 
     * @return a persistent identifier or <code>null</code> of this DataView has
     *         none
     */
    public String getPid() {
        return pid;
    }


    /**
     * Get the reference URI for this DataView.
     * 
     * @return a reference URI or <code>null</code> of this DataView has
     *         none
     */
    public String getRef() {
        return ref;
    }

} // abstract class DataView

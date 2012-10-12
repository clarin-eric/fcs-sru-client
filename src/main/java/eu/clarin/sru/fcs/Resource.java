package eu.clarin.sru.fcs;

import java.util.Collections;
import java.util.List;

/**
 * A CLARIN FCS resource
 */
public class Resource {
    /**
     * A CLARIN FCS resource fragment
     */
    public final static class ResourceFragment {
        private final String pid;
        private final String ref;
        private final List<DataView> dataviews;


        ResourceFragment(String pid, String ref, List<DataView> dataviews) {
            this.pid = ((pid != null) && !pid.isEmpty()) ? pid : null;
            this.ref = ((ref != null) && !ref.isEmpty()) ? ref : null;
            if ((dataviews != null) && !dataviews.isEmpty()) {
                this.dataviews = Collections.unmodifiableList(dataviews);
            } else {
                this.dataviews = null;
            }
        }


        /**
         * Get the persistent identifier for this resource fragment.
         * 
         * @return a persistent identifier or <code>null</code> of this resource
         *         fragment has none
         */
        public String getPid() {
            return pid;
        }


        /**
         * Get the reference URI for this resource fragment.
         * 
         * @return a reference URI or <code>null</code> of this resource
         *         fragment has none
         */
        public String getRef() {
            return ref;
        }


        /**
         * Convenience method to check if this resource fragment has any
         * dataviews.
         * 
         * @return <code>true</code> if this resource fragment has dataviews,
         *         <code>false</code> otherwise
         */
        public boolean hasDataViews() {
            return (dataviews != null);
        }


        /**
         * Get the list of dataview objects for this this resource fragment.
         * 
         * @return a list of {@link DataView} objects or <code>null</code>,
         *         or <code>null</code> if this resource fragment has
         *         none
         */
        public List<DataView> getDataViews() {
            return dataviews;
        }
    } // inner class ResourceFragment
    private final String pid;
    private final String ref;
    private final List<DataView> dataviews;
    private final List<ResourceFragment> resourceFragments;


    Resource(String pid, String ref, List<DataView> dataviews,
            List<ResourceFragment> resourceFragments) {
        this.pid = ((pid != null) && !pid.isEmpty()) ? pid : null;
        this.ref = ((ref != null) && !ref.isEmpty()) ? ref : null;
        if ((dataviews != null) && !dataviews.isEmpty()) {
            this.dataviews = Collections.unmodifiableList(dataviews);
        } else {
            this.dataviews = null;
        }
        if ((resourceFragments != null) && !resourceFragments.isEmpty()) {
            this.resourceFragments =
                    Collections.unmodifiableList(resourceFragments);
        } else {
            this.resourceFragments = null;
        }
    }


    /**
     * Get the persistent identifier for this resource.
     * 
     * @return a persistent identifier or <code>null</code> of this resource has
     *         none
     */
    public String getPid() {
        return pid;
    }


    /**
     * Get the reference URI for this resource.
     * 
     * @return a reference URI or <code>null</code> of this resource has
     *         none
     */
    public String getRef() {
        return ref;
    }


    /**
     * Convenience method to check if this resource has any dataviews.
     * 
     * @return <code>true</code> if this resource has dataviews,
     *         <code>false</code> otherwise
     */
    public boolean hasDataViews() {
        return (dataviews != null);
    }


    /**
     * Get the list of dataview objects for this this resource.
     * 
     * @return a list of {@link DataView} objects or <code>null</code>, or
     *         <code>null</code> if this resource has none
     */
    public List<DataView> getDataViews() {
        return dataviews;
    }


    /**
     * Convenience method to check if this resource has any resource fragments.
     * 
     * @return <code>true</code> if this resource has resource fragments,
     *         <code>false</code> otherwise
     */
    public boolean hasResourceFragments() {
        return (resourceFragments != null);
    }


    /**
     * Get the list of resource fragment objects for this this resource.
     * 
     * @return a list of {@link ResourceFragment} objects or <code>null</code>,
     *         or <code>null</code> if this resource has none
     */
    public List<ResourceFragment> getResourceFragments() {
        return resourceFragments;
    }

} // class Resource

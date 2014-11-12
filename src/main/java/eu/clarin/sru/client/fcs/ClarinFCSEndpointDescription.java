package eu.clarin.sru.client.fcs;

import java.io.Serializable;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import eu.clarin.sru.client.SRUExtraResponseData;


public class ClarinFCSEndpointDescription implements Serializable,
        SRUExtraResponseData {
    private static final long serialVersionUID = 9061228238942949036L;
    private static final QName ROOT_ELEMENT =
            new QName("http://clarin.eu/fcs/endpoint-description",
                      "EndpointDescription");
    private final int version;
    private final List<URI> capabilites;
    private final List<DataView> supportedDataViews;
    private final List<ResourceInfo> resources;


    ClarinFCSEndpointDescription(int version, List<URI> capabilites,
            List<DataView> supportedDataViews, List<ResourceInfo> resources) {
        this.version = version;
        if ((capabilites != null) && !capabilites.isEmpty()) {
            this.capabilites = Collections.unmodifiableList(capabilites);
        } else {
            this.capabilites = Collections.emptyList();
        }
        if ((supportedDataViews != null) && !supportedDataViews.isEmpty()) {
            this.supportedDataViews =
                    Collections.unmodifiableList(supportedDataViews);
        } else {
            this.supportedDataViews = Collections.emptyList();
        }
        if ((resources != null) && !resources.isEmpty()) {
            this.resources = Collections.unmodifiableList(resources);
        } else {
            this.resources = Collections.emptyList();
        }
    }


    @Override
    public QName getRootElement() {
        return ROOT_ELEMENT;
    }


    public int getVersion() {
        return version;
    }


    public List<URI> getCapabilities() {
        return capabilites;
    }


    public List<DataView> getSupportedDataViews() {
        return supportedDataViews;
    }


    public List<ResourceInfo> getResources() {
        return resources;
    }


    public static class DataView implements Serializable {
        private static final long serialVersionUID = -5628565233032672627L;


        /**
         * Enumeration to indicate the delivery policy of a data view.
         */
        public enum DeliveryPolicy {
            /**
             * The data view is sent automatically  by the endpoint.
             */
            SEND_BY_DEFAULT,
            /**
             * A client must explicitly request the endpoint.
             */
            NEED_TO_REQUEST;
        } // enum PayloadDelivery
        private final String identifier;
        private final String mimeType;
        private final DeliveryPolicy deliveryPolicy;


        DataView(String identifier, String mimeType,
                DeliveryPolicy deliveryPolicy) {
            if (identifier == null) {
                throw new NullPointerException("identifier == null");
            }
            if (identifier.isEmpty()) {
                throw new IllegalArgumentException("identifier is empty");
            }
            this.identifier = identifier;

            if (mimeType == null) {
                throw new NullPointerException("mimeType == null");
            }
            if (mimeType.isEmpty()) {
                throw new IllegalArgumentException("mimeType is empty");
            }
            this.mimeType = mimeType;

            if (deliveryPolicy == null) {
                throw new NullPointerException("deliveryPolicy == null");
            }
            this.deliveryPolicy = deliveryPolicy;
        }


        /**
         * Get the identifier of this data view.
         *
         * @return the identifier of the data view
         */
        public String getIdentifier() {
            return identifier;
        }


        /**
         * Get the MIME type of this data view.
         *
         * @return the MIME type of this data view
         */
        public String getMimeType() {
            return mimeType;
        }


        /**
         * Get the delivery policy for this data view.
         *
         * @return the delivery policy of this data view
         * @see DeliveryPolicy
         */
        public DeliveryPolicy getDeliveryPolicy() {
            return deliveryPolicy;
        }


        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(getClass().getSimpleName());
            sb.append("[");
            sb.append("identifier=").append(identifier);
            sb.append(", mimeType=").append(mimeType);
            sb.append("]");
            return sb.toString();
        }

    } // class DataView


    public static class ResourceInfo implements Serializable {
        private static final long serialVersionUID = 1046130188435071544L;
        private final String pid;
        private final Map<String, String> title;
        private final Map<String, String> description;
        private final String landingPageURI;
        private final List<String> languages;
        private final List<DataView> availableDataViews;
        private final List<ResourceInfo> subResources;


        ResourceInfo(String pid, Map<String, String> title,
                Map<String, String> description, String landingPageURI,
                List<String> languages, List<DataView> availableDataViews,
                List<ResourceInfo> subResources) {
            if (pid == null) {
                throw new NullPointerException("pid == null");
            }
            this.pid = pid;

            if (title == null) {
                throw new NullPointerException("title == null");
            }
            if (title.isEmpty()) {
                throw new IllegalArgumentException("title is empty");
            }
            this.title = Collections.unmodifiableMap(title);
            if ((description != null) && !description.isEmpty()) {
                this.description = Collections.unmodifiableMap(description);
            } else {
                this.description = null;
            }

            this.landingPageURI = landingPageURI;
            if (languages == null) {
                throw new NullPointerException("languages == null");
            }
            if (languages.isEmpty()) {
                throw new IllegalArgumentException("languages is empty");
            }
            this.languages = languages;

            if (availableDataViews == null) {
                throw new IllegalArgumentException("availableDataViews == null");
            }
            this.availableDataViews =
                    Collections.unmodifiableList(availableDataViews);

            if ((subResources != null) && !subResources.isEmpty()) {
                this.subResources = Collections.unmodifiableList(subResources);
            } else {
                this.subResources = null;
            }
        }


        /**
         * Get the persistent identifier of this resource.
         *
         * @return a string representing the persistent identifier of this resource
         */
        public String getPid() {
            return pid;
        }


        /**
         * Determine, if this resource has sub-resources.
         *
         * @return <code>true</code> if the resource has sub-resources,
         *         <code>false</code> otherwise
         */
        public boolean hasSubResources() {
            return subResources != null;
        }


        /**
         * Get the title of this resource.
         *
         * @return a Map of titles keyed by language code
         */
        public Map<String, String> getTitle() {
            return title;
        }


        /**
         * Get the title of the resource for a specific language code.
         *
         * @param language
         *            the language code
         * @return the title for the language code or <code>null</code> if no title
         *         for this language code exists
         */
        public String getTitle(String language) {
            return title.get(language);
        }


        /**
         * Get the description of this resource.
         *
         * @return a Map of descriptions keyed by language code
         */
        public Map<String, String> getDescription() {
            return description;
        }


        /**
         * Get the description of the resource for a specific language code.
         *
         * @param language
         *            the language code
         * @return the description for the language code or <code>null</code> if no
         *         title for this language code exists
         */
        public String getDescription(String language) {
            return (description != null) ? description.get(language) : null;
        }


        /**
         * Get the landing page of this resource.
         *
         * @return the landing page of this resource or <code>null</code> if not
         *         applicable
         */
        public String getLandingPageURI() {
            return landingPageURI;
        }


        /**
         * Get the list of languages in this resource represented as ISO-632-3 three
         * letter language code.
         *
         * @return the list of languages in this resource as a list of ISO-632-3
         *         three letter language codes.
         */
        public List<String> getLanguages() {
            return languages;
        }


        /**
         * Get the direct sub-ordinate resources of this resource.
         *
         * @return a list of resources or <code>null</code> if this resource has no
         *         sub-ordinate resources
         */
        public List<ResourceInfo> getSubResources() {
            return subResources;
        }


        public List<DataView> getAvailableDataViews() {
            return availableDataViews;
        }

    } // class ResourceInfo

} // class ClarinFCSEndpointDescription

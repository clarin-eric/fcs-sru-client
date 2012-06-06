package eu.clarin.sru.client;

import java.util.HashMap;
import java.util.Map;


public abstract class SRUAbstractRequest {
    protected final String baseURI;
    protected SRUVersion version;
    protected Map<String, String> extraRequestData;


    protected SRUAbstractRequest(String baseURI) {
        if (baseURI == null) {
            throw new NullPointerException("baseURI == null");
        }
        this.baseURI = baseURI;
    }


    public String getBaseURI() {
        return baseURI;
    }


    public void setVersion(SRUVersion version) {
        this.version = version;
    }


    public SRUVersion getVersion() {
        return version;
    }


    public void setExtraRequestData(String name, String value) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is an empty string");
        }
        if (!name.startsWith("x-")) {
            throw new IllegalArgumentException("name must start with 'x-'");
        }
        if (value == null) {
            throw new NullPointerException("value == null");
        }
        if (value.isEmpty()) {
            throw new IllegalArgumentException("value is an empty string");
        }
        if (extraRequestData == null) {
            extraRequestData = new HashMap<String, String>();
        }
        extraRequestData.put(name, value);
    }


    public String getExtraRequestData(String name) {
        if (name == null) {
            throw new NullPointerException("name == null");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name is an empty string");
        }
        if (!name.startsWith("x-")) {
            throw new IllegalArgumentException("name must start with 'x-'");
        }
        if (extraRequestData != null) {
            return extraRequestData.get(name);
        }
        return null;
    }

} // class AbstractSRURequest

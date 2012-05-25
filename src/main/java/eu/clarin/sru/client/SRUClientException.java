package eu.clarin.sru.client;

@SuppressWarnings("serial")
public class SRUClientException extends Exception {

    public SRUClientException(String message) {
        super(message);
    }


    public SRUClientException(String message, Throwable t) {
        super(message, t);
    }
} // class SRUClientException

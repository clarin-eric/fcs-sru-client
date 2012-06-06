package eu.clarin.sru.client;

public final class SRUDiagnostic {
    private final String uri;
    private final String details;
    private final String message;


    public SRUDiagnostic(String uri, String details, String message) {
        this.uri     = uri;
        this.details = details;
        this.message = message;
    }


    public String getURI() {
        return uri;
    }


    public String getDetails() {
        return details;
    }


    public String getMessage() {
        return message;
    }

} // class SRUDiagnostic

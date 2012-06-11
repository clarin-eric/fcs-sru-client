package eu.clarin.sru.client;

public enum SRURecordPacking {
    XML, STRING;

    public String toProtocolString() {
        switch (this) {
        case XML:
            return "xml";
        case STRING:
            return "string";
        default:
            return null;
        }
    }

} // enum SRURecordPacking

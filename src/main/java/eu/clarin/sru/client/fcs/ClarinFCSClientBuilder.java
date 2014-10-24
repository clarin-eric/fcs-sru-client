package eu.clarin.sru.client.fcs;

import java.util.ArrayList;
import java.util.List;

import eu.clarin.sru.client.SRUClient;
import eu.clarin.sru.client.SRUSimpleClient;
import eu.clarin.sru.client.SRUThreadedClient;
import eu.clarin.sru.client.SRUVersion;


public class ClarinFCSClientBuilder {
    private static final boolean DEFAULT_UNKNOWN_AS_DOM =
            false;
    private static final SRUVersion DEFAULT_SRU_VERSION =
            SRUVersion.VERSION_1_2;
    private List<DataViewParser> parsers = new ArrayList<DataViewParser>();
    private SRUVersion defaultSruVersion = DEFAULT_SRU_VERSION;
    private boolean unknownAsDom;
    private boolean legacySupport = false;


    /**
     * Constructor.
     */
    public ClarinFCSClientBuilder(boolean unknownAsDom) {
        this.unknownAsDom = unknownAsDom;
    }


    /**
     * Constructor.
     */
    public ClarinFCSClientBuilder() {
        this(DEFAULT_UNKNOWN_AS_DOM);
    }


    public ClarinFCSClientBuilder defaults() {
        doRegisterDataViewParser(parsers, new DataViewParserHits());
        return this;
    }


    public ClarinFCSClientBuilder unkownDataViewAsDOM() {
        unknownAsDom = true;
        return this;
    }


    public ClarinFCSClientBuilder unkownDataViewAsString() {
        unknownAsDom = false;
        return this;
    }


    public ClarinFCSClientBuilder registerDataViewParser(DataViewParser parser) {
        if (parser == null) {
            throw new NullPointerException("parser == null");
        }
        if ((parser instanceof DataViewParserGenericDOM) ||
                (parser instanceof DataViewParserGenericString)) {
            throw new IllegalArgumentException("parsers of type '" +
                    parser.getClass().getName() +
                    "' should not be added manually");
        }

        if (!doRegisterDataViewParser(parsers, parser)) {
            throw new IllegalArgumentException("parser of type '" +
                    parser.getClass().getName() + "' was already registered");
        }
        return this;
    }


    public ClarinFCSClientBuilder enableLegacySupport() {
        legacySupport = true;
        return this;
    }


    public ClarinFCSClientBuilder disableLegacySupport() {
        legacySupport = false;
        return this;
    }


    @SuppressWarnings("deprecation")
    public SRUSimpleClient buildSimpleClient() {
        final List<DataViewParser> p = finalizeDataViewParsers();

        SRUSimpleClient client = new SRUSimpleClient(defaultSruVersion);
        client.registerRecordParser(new ClarinFCSRecordDataParser(p));
        if (legacySupport) {
            client.registerRecordParser(new LegacyClarinFCSRecordDataParser(p));
        }
        return client;
    }


    @SuppressWarnings("deprecation")
    public SRUClient buildClient() {
        final List<DataViewParser> p = finalizeDataViewParsers();

        SRUClient client = new SRUClient(defaultSruVersion);
        client.registerRecordParser(new ClarinFCSRecordDataParser(p));
        if (legacySupport) {
            client.registerRecordParser(new LegacyClarinFCSRecordDataParser(p));
        }
        return client;
    }


    @SuppressWarnings("deprecation")
    public SRUThreadedClient buildThreadedClient() {
        final List<DataViewParser> p = finalizeDataViewParsers();

        SRUThreadedClient client = new SRUThreadedClient(defaultSruVersion);
        client.registerRecordParser(new ClarinFCSRecordDataParser(p));
        if (legacySupport) {
            client.registerRecordParser(new LegacyClarinFCSRecordDataParser(p));
        }
        return client;
    }


    private List<DataViewParser> finalizeDataViewParsers() {
        final List<DataViewParser> result =
                new ArrayList<DataViewParser>(parsers.size() +
                        (legacySupport ? 2 : 1));
        result.addAll(parsers);
        if (unknownAsDom) {
            result.add(new DataViewParserGenericDOM());
        } else {
            result.add(new DataViewParserGenericString());
        }
        if (legacySupport) {
            result.add(new DataViewParserKWIC());
        }

        return result;
    }


    public static ClarinFCSClientBuilder create() {
        return new ClarinFCSClientBuilder();
    }


    public static ClarinFCSClientBuilder create(boolean unknownAsDom) {
        return new ClarinFCSClientBuilder(unknownAsDom);
    }


    private static boolean doRegisterDataViewParser(
            List<DataViewParser> parsers, DataViewParser parser) {
        if (parsers.contains(parser)) {
            return false;
        } else {
            parsers.add(parser);
            return true;
        }
    }

} // class ClarinFCSClientBuilder

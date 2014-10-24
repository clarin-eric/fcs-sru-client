package eu.clarin.sru.client.fcs;

public class DataViewHits extends DataView {
    /**
     * The MIME type for CLARIN-FCS KWIC data views.
     */
    public static final String TYPE = "application/x-clarin-fcs-hits+xml";
    private final String text;
    private final int[] offsets;
    private final int max_offset;


    /**
     * Constructor.
     *
     * @param pid
     *            a persistent identifier or <code>null</code>
     * @param ref
     *            a reference URI or <code>null</code>
     * @param text
     *            the textual content of the hits
     * @param offsets
     *            an array of (start, end) offset pairs that indicate the part
     *            of the text, that is considered a hit
     * @param offsets_idx
     *            the largest index (= hit_count * 2) within the offsets array
     *            plus one
     * @throws NullPointerException
     *             if any mandatory argument is <code>null</code>
     * @throws IllegalArgumentException
     *             if any argument is illegal
     */
    protected DataViewHits(String pid, String ref, String text,
            int[] offsets, int offsets_idx) {
        super(TYPE, pid, ref);
        if (text == null) {
            throw new NullPointerException("text == null");
        }
        if (text.isEmpty()) {
            throw new IllegalArgumentException("text is empty");
        }
        this.text = text;
        if (offsets == null) {
            throw new NullPointerException("offsets == null");
        }
        this.offsets = offsets;
        if (offsets_idx < 0) {
            throw new IllegalArgumentException("offset_idx < 0");
        }
        if (offsets_idx > offsets.length) {
            throw new IllegalArgumentException("offset_idx > offsets.length");
        }
        this.max_offset = (offsets_idx / 2);
    }


    public int getHitCount() {
        return max_offset;
    }


    public String getText() {
        return text;
    }


    public int[] getHitOffsets(int idx) {
        if (idx < 0) {
            throw new IllegalArgumentException("idx < 0");
        }
        if (idx < max_offset) {
            int[] result = new int[2];
            result[0] = offsets[(2 *idx)];
            result[1] = offsets[(2 * idx) + 1];
            return result;
        } else {
            throw new ArrayIndexOutOfBoundsException("idx > " +
                    (max_offset - 1));
        }
    }

} // class DataViewHits

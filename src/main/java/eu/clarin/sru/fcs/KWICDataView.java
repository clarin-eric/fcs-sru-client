package eu.clarin.sru.fcs;

/**
 * A CLARIN FCS KWIC DataView.
 */
public final class KWICDataView extends DataView {
    /**
     * The MIME type for CLARIN FCS KWIC dataviews.
     */
    public static final String MIMETYPE = "application/x-clarin-fcs-kwic+xml";
    private final String left;
    private final String keyword;
    private final String right;


    /**
     * Constructor.
     * 
     * @param pid
     *            a persistent identifier or <code>null</code>
     * @param ref
     *            a reference URI or <code>null</code>
     * @param left
     *            the left KWIC context
     * @param keyword
     *            the matched KWIC context
     * @param right
     *            the right KWIC context
     */
    KWICDataView(String pid, String ref, String left, String keyword,
            String right) {
        super(MIMETYPE, pid, ref);
        this.left    = (left    != null) ? left    : "";
        this.keyword = (keyword != null) ? keyword : "";
        this.right   = (right   != null) ? right   : "";
    }


    /**
     * Get the left KWIC context.
     *
     * @return the left KWIC context
     */
    public String getLeft() {
        return left;
    }


    /**
     * Get the matched KWIC context.
     *
     * @return the matched KWIC context
     */
    public String getKeyword() {
        return keyword;
    }


    /**
     * Get the right KWIC context.
     *
     * @return the right KWIC context
     */
    public String getRight() {
        return right;
    }

} // class DataViewKWIC

package eu.clarin.sru.client;

/**
 * A flag to indicate the position of the term within the complete term
 * list.
 */
public enum SRUWhereInList {
    /**
     * The first term (<em>first</em>)
     */
    FIRST,

    /**
     * The last term (<em>last</em>)
     */
    LAST,

    /**
     * The only term (<em>only</em>)
     */
    ONLY,

    /**
     * Any other term (<em>inner</em>)
     */
    INNER

} // enum SRUWhereInList
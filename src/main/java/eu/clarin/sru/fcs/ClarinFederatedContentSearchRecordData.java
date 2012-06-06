package eu.clarin.sru.fcs;

import eu.clarin.sru.client.SRURecordData;

public final class ClarinFederatedContentSearchRecordData implements
        SRURecordData {
    private String pid;
    private String left;
    private String keyword;
    private String right;

    ClarinFederatedContentSearchRecordData(String pid, String left,
            String keyword, String right) {
        this.pid   = pid;
        this.left  = left;
        this.keyword   = keyword;
        this.right = right;
    }

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public String getRecordSchema() {
        return ClarinFederatedContentSearchRecordParser.FCS_NS;
    }
    
    public String getPid() {
        return pid;
    }

    public String getLeft() {
        return left;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public String getRight() {
        return right;
    }
    
} // class ClarinFederatedContentSearchRecordData

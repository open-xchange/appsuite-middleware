package com.openexchange.admin.reseller.rmi.extensions;

import java.util.HashSet;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;


public class OXContextExtension extends OXCommonExtension {

    /**
     * For serialization
     */
    private static final long serialVersionUID = 8443761921961452860L;

    private String errortext;
    
    private ResellerAdmin owner;
    
    private String customid;
    
    private HashSet<Restriction> restriction;
    
    private boolean restrictionset;
    
    private boolean ownerset;
    
    private int sid;
    
    private boolean sidset;
    
    private boolean customidset;
    
    /**
     * Initializes a new {@link OXContextExtension}.
     * @param sid
     */
    public OXContextExtension(final int sid) {
        super();
        setSid(sid);
    }
    
    /**
     * Initializes a new {@link OXContextExtension}.
     * @param owner
     */
    public OXContextExtension(final ResellerAdmin owner) {
        super();
        setOwner(owner);
    }

    /**
     * Initializes a new {@link OXContextExtension}.
     * @param restriction
     */
    public OXContextExtension(final HashSet<Restriction> restriction) {
        super();
        setRestriction(restriction);
    }

    /**
     * Initializes a new {@link OXContextExtension}.
     * @param owner
     * @param restriction
     */
    public OXContextExtension(final ResellerAdmin owner, final HashSet<Restriction> restriction) {
        super();
        setOwner(owner);
        setRestriction(restriction);
    }

    /**
     * Initializes a new {@link OXContextExtension}.
     * @param restriction
     */
    public OXContextExtension(final String customid) {
        super();
        setCustomid(customid);
    }
    /**
     * Returns the owner of this context
     * 
     * @return
     */
    public final ResellerAdmin getOwner() {
        return owner;
    }

    public final int getSid() {
        return sid;
    }

    public void setExtensionError(final String errortext) {
        this.errortext = errortext;
    }

    public String getExtensionError() {
        return this.errortext;
    }
    
    /**
     * Sets the owner of this context
     * 
     * @param owner
     */
    public final void setOwner(final ResellerAdmin owner) {
        this.ownerset = true;
        this.owner = owner;
    }

    
    public final void setSid(int sid) {
        this.sidset = true;
        this.sid = sid;
    }

    
    public final boolean isOwnerset() {
        return ownerset;
    }

    
    public final boolean isSidset() {
        return sidset;
    }

    
    public final HashSet<Restriction> getRestriction() {
        return restriction;
    }

    
    public final void setRestriction(HashSet<Restriction> restriction) {
        this.restrictionset = true;
        this.restriction = restriction;
    }

    
    public final boolean isRestrictionset() {
        return restrictionset;
    }

    public final boolean isCustomidset() {
        return customidset;
    }
    
    /**
     * @return the customid
     */
    public final String getCustomid() {
        return customid;
    }

    
    /**
     * @param customid the customid to set
     */
    public final void setCustomid(final String customid) {
        this.customidset = true;
        this.customid = customid;
    }
    
}

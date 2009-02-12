package com.openexchange.admin.reseller.rmi.extensions;

import java.util.ArrayList;
import java.util.HashSet;
import com.openexchange.admin.reseller.rmi.dataobjects.ResellerAdmin;
import com.openexchange.admin.reseller.rmi.dataobjects.Restriction;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;


public class OXContextExtension extends OXCommonExtension {

    private static final ArrayList<String> columnnames = new ArrayList<String>();

    private static final ArrayList<String> columnnamesCSV = new ArrayList<String>();
    
    /**
     * For serialization
     */
    private static final long serialVersionUID = 8443761921961452860L;

    static {
        columnnames.add("Owner");
        columnnamesCSV.add("Owner");
        columnnamesCSV.add("Restrictions");
    }
    
    private String errortext;
    
    private ResellerAdmin owner;
    
    private HashSet<Restriction> restriction;
    
    private boolean restrictionset;
    
    private boolean ownerset;
    
    private int sid;
    
    private boolean sidset;
    
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

    @Override
    public ArrayList<String> getColumnNamesCSV() {
        return columnnamesCSV;
    }

    @Override
    public ArrayList<String> getColumnNamesNormal() {
        return columnnames;
    }

    @Override
    public ArrayList<String> getCSVData() {
        final ArrayList<String> retval = new ArrayList<String>();
        final ResellerAdmin owner2 = getOwner();
        if (isOwnerset() && null != owner2) {
            retval.add(owner2.getName());
        } else {
            retval.add(null);
        }
        if (isRestrictionset() && null != restriction) {
            retval.add(getObjectsAsString(restriction));
        } else {
            retval.add(null);
        }
        return retval;
    }

    public String getExtensionError() {
        return this.errortext;
    }

    @Override
    public ArrayList<String> getNormalData() {
        final ArrayList<String> retval = new ArrayList<String>();
        final ResellerAdmin owner2 = getOwner();
        if (isOwnerset() && null != owner2) {
            retval.add(owner2.getName());
        } else {
            retval.add(null);
        }
        return retval;
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
    
    /**
     * This method takes an array of objects and format them in one comma-separated string
     * 
     * @param objects
     * @return
     */
    public static String getObjectsAsString(final HashSet<Restriction> objects) {
        final StringBuilder sb = new StringBuilder();
        if (null != objects && objects.size() > 0) {
            for (final Restriction id : objects) {
                sb.append(id.getName());
                sb.append("=");
                sb.append(id.getValue());
                sb.append(',');
            }
            sb.deleteCharAt(sb.length() - 1);

            return sb.toString();
        } else {
            return "";
        }
    }

}

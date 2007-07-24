/**
 * 
 */
package com.openexchange.admin.rmi.dataobjects;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;

import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;

/**
 * @author choeger
 * 
 */
public abstract class EnforceableDataObject implements Serializable, Cloneable {

    private ArrayList<String> unset_members = null;

    /**
     * This method must be implemented and it must return a String array
     * containing all names of mandatory members of the corresponding class
     * required to CREATE data.
     * 
     * @return String array containing names of mandatory members or null if
     *         unwanted
     */
    protected abstract String[] getMandatoryMembersCreate();

    /**
     * This method must be implemented and it must return a String array
     * containing all names of mandatory members of the corresponding class
     * required to CHANGE data.
     * 
     * @return String array containing names of mandatory members or null if
     *         unwanted
     */
    protected abstract String[] getMandatoryMembersChange();

    /**
     * @return
     * @throws EnforceableDataObjectException
     */
    public boolean mandatoryCreateMembersSet() throws EnforceableDataObjectException {
        return mandatoryMembersSet(getMandatoryMembersCreate());
    }

    /**
     * @return
     * @throws EnforceableDataObjectException
     */
    public boolean mandatoryChangeMembersSet() throws EnforceableDataObjectException {
        return mandatoryMembersSet(getMandatoryMembersChange());
    }

    private boolean mandatoryMembersSet(final String[] members) throws EnforceableDataObjectException {
        this.unset_members.clear();

        try {
            if (members != null && members.length > 0) {
                for (final String m : members) {
                    Field f = this.getClass().getDeclaredField(m);
                    f.setAccessible(true);
                    Object val = f.get(this);
                    if (val == null || (val instanceof String && ((String) val).equals(""))) {
                        this.unset_members.add(m);
                    }
                }
                if (this.unset_members.size() > 0) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return true;
            }
        } catch (SecurityException e) {
            throw new EnforceableDataObjectException(e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new EnforceableDataObjectException("No such member: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new EnforceableDataObjectException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new EnforceableDataObjectException(e.getMessage());
        }
    }

    public ArrayList<String> getUnsetMembers() {
        return this.unset_members;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        final EnforceableDataObject object = (EnforceableDataObject) super.clone();
        object.unset_members = new ArrayList<String>(this.unset_members);
        return object;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder ret = new StringBuilder(super.toString());
        ret.append("  ");
        ret.append(" Mandatory members:\n");
        ret.append("  Create:");
        if (getMandatoryMembersCreate() != null && getMandatoryMembersCreate().length > 0) {
            for (final String m : getMandatoryMembersCreate()) {
                ret.append(" ");
                ret.append(m);
                ret.append("\n");
            }
        } else {
            ret.append(" NONE\n");
        }
        ret.append("  Change:");
        if (getMandatoryMembersChange() != null && getMandatoryMembersChange().length > 0) {
            for (final String m : getMandatoryMembersChange()) {
                ret.append(" ");
                ret.append(m);
                ret.append("\n");
            }
        } else {
            ret.append(" NONE\n");
        }

        return ret.toString();
    }

    /**
     * 
     */
    public EnforceableDataObject() {
        this.unset_members = new ArrayList<String>();
    }

}

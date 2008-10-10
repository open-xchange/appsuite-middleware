/**
 * 
 */
package com.openexchange.admin.reseller.rmi.dataobjects;

import java.lang.reflect.Field;

import com.openexchange.admin.rmi.dataobjects.ExtendableDataObject;

/**
 * @author choeger
 *
 */
public class Restriction extends ExtendableDataObject implements Cloneable {

    /**
     * 
     */
    private static final long serialVersionUID = -3767091906243210327L;

    public static final String MAX_CONTEXT = "com.openexchange.Context";
    
    public static final String MAX_CONTEXT_QUOTA = "com.openexchange.Context.Quota";
    
    public static final String MAX_USER = "com.openexchange.User";
    
    private String value;
    
    private Integer id;
    
    private String name;
    
    /**
     * 
     */
    public Restriction() {
        super();
        init();
    }

    /**
     * @param name
     * @param value
     */
    public Restriction(final String name, final String value) {
        super();
        init();
        this.name = name;
        this.value = value;
    }
    
    private void init() {
        this.value = null;
        this.id = null;
        this.name = null;
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.EnforceableDataObject#getMandatoryMembersChange()
     */
    @Override
    public String[] getMandatoryMembersChange() {
        return new String[]{ "name" };
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.EnforceableDataObject#getMandatoryMembersCreate()
     */
    @Override
    public String[] getMandatoryMembersCreate() {
        return new String[]{ "name" };
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.EnforceableDataObject#getMandatoryMembersDelete()
     */
    @Override
    public String[] getMandatoryMembersDelete() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.EnforceableDataObject#getMandatoryMembersRegister()
     */
    @Override
    public String[] getMandatoryMembersRegister() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return the value
     */
    public final String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public final void setValue(final String value) {
        this.value = value;
    }

    /**
     * @return the id
     */
    public final Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public final void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public final void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Restriction other = (Restriction) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    public final String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("[ \n");
        for (final Field f : this.getClass().getDeclaredFields()) {
            try {
                Object ob = f.get(this);
                String tname = f.getName();
                if (ob != null && !tname.equals("serialVersionUID")) {
                    ret.append("  ");
                    ret.append(tname);
                    ret.append(": ");
                    ret.append(ob);
                    ret.append("\n");
                }
            } catch (IllegalArgumentException e) {
                ret.append("IllegalArgument\n");
            } catch (IllegalAccessException e) {
                ret.append("IllegalAccessException\n");
            }
        }
        ret.append("]");
        return ret.toString();
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.ExtendableDataObject#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}

/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.admin.reseller.rmi.dataobjects;

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

    public static final String MAX_CONTEXT_PER_SUBADMIN = "Subadmin.MaxContext";
    
    public static final String MAX_OVERALL_CONTEXT_QUOTA_PER_SUBADMIN = "Subadmin.MaxOverallContextQuota";
    
    public static final String MAX_USER_PER_CONTEXT = "Context.MaxUser";
    
    public static final String MAX_OVERALL_USER_PER_SUBADMIN = "Subadmin.MaxOverallUser";
    
    public static final String MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX = "Subadmin.MaxOverallUserByModuleaccess_";
    
    public static final String MAX_OVERALL_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX = "Context.MaxUserByModuleaccess_";

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
        ret.append(this.getName());
        ret.append("=");
        ret.append(this.getValue());
//        ret.append("[ \n");
//        for (final Field f : this.getClass().getDeclaredFields()) {
//            try {
//                Object ob = f.get(this);
//                String tname = f.getName();
//                if (ob != null && !tname.equals("serialVersionUID") &&
//                    !tname.startsWith("MAX")) {
//                    ret.append("  ");
//                    ret.append(tname);
//                    ret.append(": ");
//                    ret.append(ob);
//                    ret.append("\n");
//                }
//            } catch (IllegalArgumentException e) {
//                ret.append("IllegalArgument\n");
//            } catch (IllegalAccessException e) {
//                ret.append("IllegalAccessException\n");
//            }
//        }
//        ret.append("]");
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

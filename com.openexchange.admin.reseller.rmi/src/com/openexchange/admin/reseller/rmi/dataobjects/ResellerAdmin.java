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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

import java.lang.reflect.Field;
import java.util.Arrays;
import com.openexchange.admin.rmi.dataobjects.EnforceableDataObject;
import com.openexchange.admin.rmi.dataobjects.PasswordMechObject;

/**
 * @author choeger
 *
 */
public class ResellerAdmin extends EnforceableDataObject implements PasswordMechObject,Cloneable {

    /**
     *
     */
    private static final long serialVersionUID = 7212339205350666355L;

    private Integer id;

    private boolean idset = false;

    private Integer parentId;

    private boolean parentIdset = false;

    private String name;

    private boolean nameset = false;

    private String password;

    private boolean passwordset = false;

    private String passwordMech;

    private boolean passwordMechset = false;

    private String displayname;

    private boolean displaynameset = false;

    private Restriction[] restrictions;

    private boolean restrictionsset = false;

    public ResellerAdmin() {
        super();
        init();
    }

    /**
     * @param id
     */
    public ResellerAdmin(final int id) {
        super();
        init();
        setId(id);
    }

    /**
     * @param name
     */
    public ResellerAdmin(final String name) {
        super();
        init();
        setName(name);
    }


    /**
     * Initializes a new {@link ResellerAdmin}.
     * @param id
     * @param name
     */
    public ResellerAdmin(Integer id, String name) {
        super();
        setId(id);
        setName(name);
    }

    /**
     * @param name
     * @param password
     */
    public ResellerAdmin(final String name, final String password) {
        super();
        init();
        setName(name);
        setPassword(password);
    }

    /**
     * @return the display_name
     */
    public String getDisplayname() {
        return displayname;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.PasswordMechObject#getPassword()
     */
    @Override
    public String getPassword() {
        return password;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.PasswordMechObject#getPasswordMech()
     */
    @Override
    public String getPasswordMech() {
        return passwordMech;
    }

    /**
     * @return the pid
     */
    public Integer getParentId() {
        return parentId;
    }

    private void init() {
        this.id = null;
        this.parentId = null;
        this.name = null;
        this.password = null;
        this.displayname = null;
        this.passwordMech = null;
        this.restrictions = null;
    }

    /**
     * @return the display_nameset
     */
    public boolean isDisplaynameset() {
        return displaynameset;
    }

    /**
     * @return the idset
     */
    public boolean isIdset() {
        return idset;
    }

    /**
     * @return the nameset
     */
    public boolean isNameset() {
        return nameset;
    }

    /**
     * @return the passwordMechset
     */
    public boolean isPasswordMechset() {
        return passwordMechset;
    }

    /**
     * @return the passwordset
     */
    public boolean isPasswordset() {
        return passwordset;
    }

    /**
     * @return the pidset
     */
    public boolean isParentIdset() {
        return parentIdset;
    }

    /**
     * @param display_name the display_name to set
     */
    public void setDisplayname(final String displayname) {
        this.displaynameset = true;
        this.displayname = displayname;
    }

    /**
     * @param id the id to set
     */
    public void setId(final Integer id) {
        this.idset = true;
        this.id = id;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.nameset = true;
        this.name = name;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(final String password) {
        this.passwordset = true;
        this.password = password;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.PasswordMechObject#setPasswordMech(java.lang.String)
     */
    @Override
    public void setPasswordMech(final String passwordMech) {
        this.passwordMechset = true;
        this.passwordMech = passwordMech;
    }

    /**
     * This parameter is currently not used
     *
     * @param pid the pid to set
     */
    public void setParentId(final Integer pid) {
        this.parentIdset = true;
        this.parentId = pid;
    }

    /**
     * @return the restrictions
     */
    public final Restriction[] getRestrictions() {
        return restrictions;
    }

    /**
     * @param restrictions the restrictions to set
     */
    public final void setRestrictions(final Restriction[] restrictions) {
        this.restrictionsset = true;
        this.restrictions = restrictions;
    }

    public final boolean isRestrictionsset() {
        return restrictionsset;
    }

    @Override
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

    @Override
    public String[] getMandatoryMembersChange() {
        return null;
    }

    @Override
    public String[] getMandatoryMembersCreate() {
        return new String[]{ "displayname", "name" , "password"};
    }

    @Override
    public String[] getMandatoryMembersDelete() {
        return null;
    }

    @Override
    public String[] getMandatoryMembersRegister() {
        return null;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((displayname == null) ? 0 : displayname.hashCode());
        result = prime * result + (displaynameset ? 1231 : 1237);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (idset ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nameset ? 1231 : 1237);
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result + (parentIdset ? 1231 : 1237);
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        result = prime * result + ((passwordMech == null) ? 0 : passwordMech.hashCode());
        result = prime * result + (passwordMechset ? 1231 : 1237);
        result = prime * result + (passwordset ? 1231 : 1237);
        result = prime * result + ((restrictions == null) ? 0 : Arrays.hashCode(restrictions));
        result = prime * result + (restrictionsset ? 1231 : 1237);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ResellerAdmin other = (ResellerAdmin) obj;
        if (displayname == null) {
            if (other.displayname != null) {
                return false;
            }
        } else if (!displayname.equals(other.displayname)) {
            return false;
        }
        if (displaynameset != other.displaynameset) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (idset != other.idset) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (nameset != other.nameset) {
            return false;
        }
        if (parentId == null) {
            if (other.parentId != null) {
                return false;
            }
        } else if (!parentId.equals(other.parentId)) {
            return false;
        }
        if (parentIdset != other.parentIdset) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        if (passwordMech == null) {
            if (other.passwordMech != null) {
                return false;
            }
        } else if (!passwordMech.equals(other.passwordMech)) {
            return false;
        }
        if (passwordMechset != other.passwordMechset) {
            return false;
        }
        if (passwordset != other.passwordset) {
            return false;
        }
        if (restrictions == null) {
            if (other.restrictions != null) {
                return false;
            }
        } else if (!Arrays.equals(restrictions, other.restrictions)) {
            return false;
        }
        if (restrictionsset != other.restrictionsset) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.ExtendableDataObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}

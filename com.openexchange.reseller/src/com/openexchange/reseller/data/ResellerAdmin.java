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

package com.openexchange.reseller.data;

import java.util.List;
import com.google.common.collect.ImmutableList;

/**
 *
 * {@link ResellerAdmin} - A reseller administrator.
 * <pre>
 *   ResellerAdmin ra = ResellerAdmin.builder()
 *                                   .id(123)
 *                                   .name("admin")
 *                                    ...
 *                                   .build();
 * </pre>
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class ResellerAdmin implements PasswordMechObject, Cloneable {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static ResellerAdminBuilder builder() {
        return new ResellerAdminBuilder();
    }

    /** The builder for an instance of <code>ResellerAdmin</code> */
    public static class ResellerAdminBuilder {

        private Integer id;
        private Integer parentId;
        private String name;
        private String password;
        private String passwordMech;
        private String displayname;
        private Restriction[] restrictions;

        ResellerAdminBuilder() {
            super();
        }

        public ResellerAdminBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        public ResellerAdminBuilder parentId(Integer id) {
            this.parentId = id;
            return this;
        }

        public ResellerAdminBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ResellerAdminBuilder password(String password) {
            this.password = password;
            return this;
        }

        /**
         * Sets the password encryption mechanism, value is a password mechanism.
         * <p>
         * Currently supported mechanisms are <code>"{CRYPT}"</code> and <code>"{SHA}"</code>.
         *
         * @param passwordMech The passwordMech to set
         */
        public ResellerAdminBuilder passwordMech(String passwordMech) {
            this.passwordMech = passwordMech;
            return this;
        }

        public ResellerAdminBuilder displayname(String displayname) {
            this.displayname = displayname;
            return this;
        }

        public ResellerAdminBuilder restrictions(Restriction[] restrictions) {
            this.restrictions = restrictions;
            return this;
        }

        /**
         * Creates the {@code ResellerAdmin} instance from this builder's attributes.
         *
         * @return The {@code ResellerAdmin} instance
         */
        public ResellerAdmin build(){
            return new ResellerAdmin(id, parentId, name, password, passwordMech, displayname, restrictions);
        }
    }

    // -------------------------------------------------------------------------------------------------------

    private final Integer id;
    private final boolean idset;

    private final Integer parentId;
    private final boolean parentIdset;

    private final String name;
    private final boolean nameset;

    private final String password;
    private final boolean passwordset;

    private final String passwordMech;
    private final boolean passwordMechset;

    private final String displayname;
    private final boolean displaynameset;

    private final List<Restriction> restrictions;
    private final boolean restrictionsset;

    private volatile Integer hash; // For lazy hash-code computation

    /**
     * Initializes a new {@link ResellerAdmin}.
     */
    ResellerAdmin(Integer id, Integer parentId, String name, String password, String passwordMech, String displayname, Restriction[] restrictions) {
        super();

        if (id != null) {
            this.id = id;
            idset = true;
        } else {
            this.id = null;
            idset = false;
        }

        if (parentId != null) {
            this.parentId = parentId;
            parentIdset = true;
        } else {
            this.parentId = null;
            parentIdset = false;
        }

        if (name != null) {
            this.name = name;
            nameset = true;
        } else {
            this.name = null;
            nameset = false;
        }

        if (password != null) {
            this.password = password;
            passwordset = true;
        } else {
            this.password = null;
            passwordset = false;
        }

        if (passwordMech != null) {
            this.passwordMech = passwordMech;
            passwordMechset = true;
        } else {
            this.passwordMech = null;
            passwordMechset = false;
        }

        if (displayname != null) {
            this.displayname = displayname;
            displaynameset = true;
        } else {
            this.displayname = null;
            displaynameset = false;
        }

        if (restrictions != null) {
            this.restrictions = ImmutableList.copyOf(restrictions);
            restrictionsset = true;
        } else {
            this.restrictions = null;
            restrictionsset = false;
        }
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

    @Override
    public String getPassword() {
        return password;
    }

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
     * @return the restrictions
     */
    public final List<Restriction> getRestrictions() {
        return restrictions;
    }

    public final boolean isRestrictionsset() {
        return restrictionsset;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        builder.append("{");
        if (id != null) {
            builder.append("id=").append(id).append(", ");
        }
        if (parentId != null) {
            builder.append("parentId=").append(parentId).append(", ");
        }
        if (name != null) {
            builder.append("name=").append(name).append(", ");
        }
        if (password != null) {
            builder.append("password=").append(password).append(", ");
        }
        if (passwordMech != null) {
            builder.append("passwordMech=").append(passwordMech).append(", ");
        }
        if (displayname != null) {
            builder.append("displayname=").append(displayname).append(", ");
        }
        if (restrictions != null) {
            builder.append("restrictions=").append(restrictions);
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        Integer tmp = hash;
        if (null == tmp) {
            // No problem if concurrent hash-code calculation takes pace...
            int prime = 31;
            int result = 1;
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
            result = prime * result + ((restrictions == null) ? 0 : restrictions.hashCode());
            result = prime * result + (restrictionsset ? 1231 : 1237);
            tmp = Integer.valueOf(result);
            hash = tmp;
        }
        return tmp.intValue();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ResellerAdmin)) {
            return false;
        }
        ResellerAdmin other = (ResellerAdmin) obj;

        // Easy ones first
        if (nameset != other.nameset) {
            return false;
        }
        if (idset != other.idset) {
            return false;
        }
        if (parentIdset != other.parentIdset) {
            return false;
        }
        if (displaynameset != other.displaynameset) {
            return false;
        }
        if (passwordMechset != other.passwordMechset) {
            return false;
        }
        if (passwordset != other.passwordset) {
            return false;
        }
        if (restrictionsset != other.restrictionsset) {
            return false;
        }

        // Compare other values
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (parentId == null) {
            if (other.parentId != null) {
                return false;
            }
        } else if (!parentId.equals(other.parentId)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (displayname == null) {
            if (other.displayname != null) {
                return false;
            }
        } else if (!displayname.equals(other.displayname)) {
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
        if (restrictions == null) {
            if (other.restrictions != null) {
                return false;
            }
        } else if (!restrictions.equals(other.restrictions)) {
            return false;
        }

        return true;
    }

    @Override
    public ResellerAdmin clone() {
        try {
            return (ResellerAdmin) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError("CloneNotSupportedException although Cloneable is implemented.");
        }
    }

}

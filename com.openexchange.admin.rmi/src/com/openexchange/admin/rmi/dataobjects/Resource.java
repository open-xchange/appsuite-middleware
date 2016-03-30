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

package com.openexchange.admin.rmi.dataobjects;

import java.lang.reflect.Field;
import java.util.ArrayList;
import com.openexchange.admin.rmi.extensions.OXCommonExtension;
import com.openexchange.admin.rmi.extensions.OXResourceExtensionInterface;

/**
 * This dataobject stores all the data which is related to a resource
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 *
 */
public class Resource extends ExtendableDataObject implements NameAndIdObject {
    /**
     * For serialization
     */
    private static final long serialVersionUID = 6549687169790363728L;

    private Integer id;

    private String name;

    private boolean nameset;

    private String displayname;

    private boolean displaynameset;

    private String description;

    private boolean descriptionset;

    private String email;

    private boolean emailset;

    private Boolean available;

    private boolean availableset;

    /**
     * Instantiates a new {@link Resource} object
     */
    public Resource() {
        super();
        init();
    }

    /**
     * Instantiates a new {@link Resource} object with the given id
     *
     * @param id An {@link Integer} object containing the id
     */
    public Resource(final Integer id) {
        super();
        init();
        this.id = id;
    }

    private void init() {
        initExtendable();
        this.id = null;
        this.name = null;
        this.displayname = null;
        this.description = null;
        this.email = null;
        this.available = null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.NameAndIdObject#getId()
     */
    @Override
    public Integer getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.NameAndIdObject#setId(java.lang.Integer)
     */
    @Override
    public void setId(final Integer id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.NameAndIdObject#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.NameAndIdObject#setName(java.lang.String)
     */
    @Override
    public void setName(final String name) {
        nameset = true;
        this.name = name;
    }

    /**
     * Returns the displayname of this resource
     *
     * @return A {@link String} containing the displayname
     */
    public String getDisplayname() {
        return displayname;
    }

    /**
     * Sets the displayname for this resource
     *
     * @param displayname A {@link String} containing the displayname
     */
    public void setDisplayname(final String displayname) {
        displaynameset = true;
        this.displayname = displayname;
    }

    /**
     * Returns the E-Mail of this resource
     *
     * @return A {@link String} object containing the E-Mail address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address for this resource
     *
     * @param email A {@link String} object containing the E-Mail address
     */
    public void setEmail(final String email) {
        emailset = true;
        this.email = email;
    }

    /**
     * This attribute is not used
     */
    public Boolean getAvailable() {
        return available;
    }

    /**
     * This attribute is not used
     */
    public void setAvailable(Boolean available) {
        availableset = true;
        this.available = available;
    }

    /**
     * Returns the description of this resource
     *
     * @return A {@link String} object containing the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description for this resource
     *
     * @param description A {@link String} object containing the description
     */
    public void setDescription(final String description) {
        descriptionset = true;
        this.description = description;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.ExtendableDataObject#toString()
     */
    @Override
    public String toString() {
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

    /**
     * @param extension
     * @deprecated
     */
    @Deprecated
    public void addExtension(final OXResourceExtensionInterface extension) {
        getAllExtensionsAsHash().put(extension.getClass().getName(), (OXCommonExtension) extension);
    }

    /**
     * @return
     * @deprecated
     */
    @Deprecated
    public ArrayList<OXResourceExtensionInterface> getExtensions() {
        final ArrayList<OXResourceExtensionInterface> retval = new ArrayList<OXResourceExtensionInterface>();
        for (final OXCommonExtension commoninterface : getAllExtensionsAsHash().values()) {
            retval.add((OXResourceExtensionInterface) commoninterface);
        }
        return retval;
    }

    /**
     * @param o
     * @return
     * @deprecated
     */
    @Deprecated
    public boolean removeExtension(final OXResourceExtensionInterface o) {
        if (null == getAllExtensionsAsHash().remove(o.getClass().getName())) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * This method is used to get an extensions through the name of this
     * extension. This first occurence will be returned, or null if no fitting
     * extension was found.
     *
     * @param extname a String for the extension
     * @return the {@link OXResourceExtensionInterface} with extname
     * @deprecated
     */
    @Deprecated
    public OXResourceExtensionInterface getExtensionbyName(final String extname) {
        for (final OXCommonExtension ext : getAllExtensionsAsHash().values()) {
            if (extname.equals(ext.getClass().getName())) {
                return (OXResourceExtensionInterface) ext;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.ExtendableDataObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * At the moment no fields are defined here
     */
    @Override
    public String[] getMandatoryMembersChange() {
        return null;
    }

    /**
     * At the moment {@link #setName}, {@link #setDisplayname} and {@link #setEmail} are defined here
     */
    @Override
    public String[] getMandatoryMembersCreate() {
        return new String[]{ "name", "displayname", "email"};
    }

    /**
     * At the moment no fields are defined here
     */
    @Override
    public String[] getMandatoryMembersDelete() {
        return null;
    }

    /**
     * At the moment no fields are defined here
     */
    @Override
    public String[] getMandatoryMembersRegister() {
        return null;
    }

    /**
     * Used to check if the member of this object has been changed
     *
     * @return true if set; false if not
     */
    public boolean isNameset() {
        return nameset;
    }

    /**
     * Used to check if the displayname of this object has been changed
     *
     * @return true if set; false if not
     */
    public boolean isDisplaynameset() {
        return displaynameset;
    }

    /**
     * Used to check if the description of this object has been changed
     *
     * @return true if set; false if not
     */
    public boolean isDescriptionset() {
        return descriptionset;
    }

    /**
     * Used to check if the E-Mail of this object has been changed
     *
     * @return true if set; false if not
     */
    public boolean isEmailset() {
        return emailset;
    }

    /**
     * This attribute is not used
     */
    public boolean isAvailableset() {
        return availableset;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((available == null) ? 0 : available.hashCode());
        result = prime * result + (availableset ? 1231 : 1237);
        result = prime * result + ((description == null) ? 0 : description.hashCode());
        result = prime * result + (descriptionset ? 1231 : 1237);
        result = prime * result + ((displayname == null) ? 0 : displayname.hashCode());
        result = prime * result + (displaynameset ? 1231 : 1237);
        result = prime * result + ((email == null) ? 0 : email.hashCode());
        result = prime * result + (emailset ? 1231 : 1237);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nameset ? 1231 : 1237);
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
        if (!(obj instanceof Resource)) {
            return false;
        }
        final Resource other = (Resource) obj;
        if (available == null) {
            if (other.available != null) {
                return false;
            }
        } else if (!available.equals(other.available)) {
            return false;
        }
        if (availableset != other.availableset) {
            return false;
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else if (!description.equals(other.description)) {
            return false;
        }
        if (descriptionset != other.descriptionset) {
            return false;
        }
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
        if (email == null) {
            if (other.email != null) {
                return false;
            }
        } else if (!email.equals(other.email)) {
            return false;
        }
        if (emailset != other.emailset) {
            return false;
        }
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
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
        return true;
    }
}

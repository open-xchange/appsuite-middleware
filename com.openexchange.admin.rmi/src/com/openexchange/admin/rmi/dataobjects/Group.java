/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.admin.rmi.dataobjects;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import com.openexchange.admin.rmi.extensions.OXCommonExtensionInterface;
import com.openexchange.admin.rmi.extensions.OXGroupExtensionInterface;

/**
 *
 * This class represents a group.
 *
 * @author <a href="mailto:manuel.kraft@open-xchange.com">Manuel Kraft</a>
 * @author <a href="mailto:carsten.hoeger@open-xchange.com">Carsten Hoeger</a>
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 */
@SuppressWarnings("deprecation")
public class Group extends ExtendableDataObject implements NameAndIdObject {
    /**
     * For serialization
     */
    private static final long serialVersionUID = -916912847701699619L;

    private Integer id;

    private String name;

    private boolean nameset;

    private String displayname;

    private boolean displaynameset;

    private Integer[] members;

    private boolean membersset;

    /**
     * Initiates an empty group object
     */
    public Group() {
        super();
        init();
    }


    /**
     * Initiates a group object with the given id set
     *
     * @param id An {@link Integer} containing the id
     */
    public Group(final Integer id) {
        super();
        init();
        this.id = id;
    }

    /**
     * Initiates a group object with the given id, name and display name set
     *
     * @param id An {@link Integer} containing the id
     * @param name A {@link String} containing the name
     * @param displayname A {@link String} containing the display name
     */
    public Group(final Integer id, final String name, final String displayname) {
        super();
        init();
        this.id = id;
        this.name = name;
        this.displayname = displayname;
    }

    private void init(){
        initExtendable();
        this.id = null;
        this.name = null;
        this.displayname = null;
        this.members = null;
    }

    /**
     * Used to check if the display name of this object has been changed
     *
     * @return true if set; false if not
     */
    public final boolean isDisplaynameset() {
        return displaynameset;
    }


    /**
     * Used to check if the members of this object have been changed
     *
     * @return true if set; false if not
     */
    public final boolean isMembersset() {
        return membersset;
    }


    /**
     * Used to check if the name of this object has been changed
     *
     * @return true if set; false if not
     */
    public final boolean isNameset() {
        return nameset;
    }

    @Override
    public final Integer getId() {
        return id;
    }

    @Override
    public final void setId(final Integer val) {
        this.id = val;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final void setName(final String val) {
        nameset = true;
        this.name = val;
    }

    /**
     * Returns the displayname of this group
     *
     * @return A String containing the displayname
     */
    public final String getDisplayname() {
        return displayname;
    }

    /**
     * Sets the displayname for this group
     *
     * @param displayname The displayname as string
     */
    public final void setDisplayname(final String displayname) {
        displaynameset = true;
        this.displayname = displayname;
    }

    /**
     * Returns the members of this group
     *
     * @return An {@link Integer} array containing the member ids
     */
    public final Integer[] getMembers() {
        return members;
    }

    /**
     * Sets the the members for this group
     *
     * @param members An {@link Integer} array containing the member ids
     */
    public final void setMembers(final Integer[] members) {
        membersset = true;
        this.members = members;
    }

    @SuppressWarnings("unused")
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
            } catch ( IllegalArgumentException e) {
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
    public final void addExtension(final OXGroupExtensionInterface extension) {
        getAllExtensionsAsHash().put(extension.getClass().getName(), extension);
    }

    /**
     * @return
     * @deprecated
     */
    @Deprecated
    public final ArrayList<OXGroupExtensionInterface> getExtensions() {
        final ArrayList<OXGroupExtensionInterface> retval = new ArrayList<OXGroupExtensionInterface>();
        for (final OXCommonExtensionInterface commoninterface : getAllExtensionsAsHash().values()) {
            retval.add((OXGroupExtensionInterface) commoninterface);
        }
        return retval;
    }

    /**
     * @param o
     * @return
     * @deprecated
     */
    @Deprecated
    public final boolean removeExtension(final OXGroupExtensionInterface o) {
        return (null != getAllExtensionsAsHash().remove(o.getClass().getName()));
    }

    /**
     * This method is used to get an extensions through the name of this
     * extension. This first occurence will be returned, or null if no fitting
     * extension was found.
     *
     * @param extname a String for the extension
     * @return the {@link OXGroupExtensionInterface} with extname
     * @deprecated
     */
    @Deprecated
    public final OXGroupExtensionInterface getExtensionbyName(final String extname) {
        for (final OXCommonExtensionInterface ext : getAllExtensionsAsHash().values()) {
            if (extname.equals(ext.getClass().getName())) {
                return (OXGroupExtensionInterface) ext;
            }
        }
        return null;
    }

    /**
     * At the moment {@link #setDisplayname} and {@link #setName} are defined here
     */
    @Override
    public final String[] getMandatoryMembersCreate() {
        return new String[]{ "displayname", "name" };
    }

    /**
     * At the moment no fields are defined here
     */
    @Override
    public final String[] getMandatoryMembersChange() {
        return null;
    }


    /**
     * At the moment no fields are defined here
     */
    @Override
    public final String[] getMandatoryMembersDelete() {
        return null;
    }


    /**
     * At the moment no fields are defined here
     */
    @Override
    public final String[] getMandatoryMembersRegister() {
        return null;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((displayname == null) ? 0 : displayname.hashCode());
        result = prime * result + (displaynameset ? 1231 : 1237);
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + Arrays.hashCode(members);
        result = prime * result + (membersset ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + (nameset ? 1231 : 1237);
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof Group)) {
            return false;
        }
        final Group other = (Group) obj;
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
        if (!Arrays.equals(members, other.members)) {
            return false;
        }
        if (membersset != other.membersset) {
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

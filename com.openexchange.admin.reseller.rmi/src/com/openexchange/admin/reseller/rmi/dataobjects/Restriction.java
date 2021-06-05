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

package com.openexchange.admin.reseller.rmi.dataobjects;

import com.openexchange.admin.rmi.dataobjects.EnforceableDataObject;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;

/**
 * @author choeger
 *
 */
public class Restriction extends EnforceableDataObject implements Cloneable {

    /**
     * The maximum number of contexts a subadmin may create
     */
    public static final String MAX_CONTEXT_PER_SUBADMIN = "Subadmin.MaxContext";

    /**
     * The overall quota of all contexts belonging to subadmin
     */
    public static final String MAX_OVERALL_CONTEXT_QUOTA_PER_SUBADMIN = "Subadmin.MaxOverallContextQuota";

    /**
     * The maximum number of users a subadmin may create distibuted over all contexts
     * Important: The oxadminuser is also counted as a user which means that when setting
     *            MaxOverallUser to 10, only 9 additional users can be created
     */
    public static final String MAX_OVERALL_USER_PER_SUBADMIN = "Subadmin.MaxOverallUser";

    /**
     * Per default a subadmin can only create contexts. Use this setting to enable a subadmin
     * to be able to create further subadmins.
     * Possible values are: true/false
     */
    public static final String SUBADMIN_CAN_CREATE_SUBADMINS = "Subadmin.CanCreateSubadmin";

    /**
     * If {@link Restriction.SUBADMIN_CAN_CREATE_SUBADMINS} is enabled, limit the amount of subadmins
     * to be created by a subadmin to this value. If not set, there's no limit.
     */
    public static final String MAX_SUBADMIN_PER_SUBADMIN = "Subadmin.MaxSubadmin";

    /**
     * The maximum number of users with a specific {@link UserModuleAccess} a subadmin may create distibuted over all contexts
     * Important: The oxadminuser is also counted as a user which means that when setting
     *            MaxOverallUser to 10, only 9 additional users can be created
     */
    public static final String MAX_OVERALL_USER_PER_SUBADMIN_BY_MODULEACCESS_PREFIX = "Subadmin.MaxOverallUserByModuleaccess_";

    /**
     * The maximum number of users that can be created in this context
     * Important: The oxadminuser is also counted as a user which means that when setting
     *            MaxOverallUser to 10, only 9 additional users can be created
     */
    public static final String MAX_USER_PER_CONTEXT = "Context.MaxUser";

    /**
     * The maximum number of users with a specific {@link UserModuleAccess} that can be created in this context
     * Important: The oxadminuser is also counted as a user which means that when setting
     *            MaxOverallUser to 10, only 9 additional users can be created
     */
    public static final String MAX_USER_PER_CONTEXT_BY_MODULEACCESS_PREFIX = "Context.MaxUserByModuleaccess_";

    /**
     * All currently existing restrictions <b>except</b> the BY_MODULEACCESS restrictions
     */
    public static final String[] ALL_RESTRICTIONS = new String[] {
        MAX_CONTEXT_PER_SUBADMIN,
        MAX_OVERALL_CONTEXT_QUOTA_PER_SUBADMIN,
        MAX_OVERALL_USER_PER_SUBADMIN,
        MAX_USER_PER_CONTEXT,
        SUBADMIN_CAN_CREATE_SUBADMINS,
        MAX_SUBADMIN_PER_SUBADMIN
        };
    /**
     *
     */
    private static final long serialVersionUID = -3767091906243210327L;

    private Integer id;

    private String name;

    private String value;

    /**
     *
     */
    public Restriction() {
        super();
        init();
    }

    /**
     * Initializes a new {@link Restriction}.
     * @param id
     * @param name
     */
    public Restriction(final Integer id, final String name) {
        super();
        init();
        this.id = id;
        this.name = name;
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

    /**
     * Initializes a new {@link Restriction}.
     * @param id
     * @param name
     * @param value
     */
    public Restriction(final Integer id, final String name, final String value) {
        super();
        init();
        this.id = id;
        this.name = name;
        this.value = value;
    }

    /**
     * Note that this method only cares about the name attribute inside this class, all other attributes are not included in the check
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Restriction other = (Restriction) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    /**
     * @return the id
     */
    public final Integer getId() {
        return id;
    }

    @Override
    public String[] getMandatoryMembersChange() {
        return new String[]{ "name" };
    }

    @Override
    public String[] getMandatoryMembersCreate() {
        return new String[]{ "name" };
    }

    @Override
    public String[] getMandatoryMembersDelete() {
        // Nothing to do
        return null;
    }

    @Override
    public String[] getMandatoryMembersRegister() {
        // Nothing to do
        return null;
    }

    /**
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * @return the value
     */
    public final String getValue() {
        return value;
    }

    /**
     * Note that this method only cares about the name attribute inside this class, all other attributes are not included in the check
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /**
     * @param id the id to set
     */
    public final void setId(final Integer id) {
        this.id = id;
    }

    /**
     * @param name the name to set
     */
    public final void setName(final String name) {
        this.name = name;
    }


    /**
     * @param value the value to set
     */
    public final void setValue(final String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Restriction [id=" + id + ", name=" + name + ", value=" + value + "]";
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    private void init() {
        value = null;
        id = null;
        name = null;
    }

}

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

package com.openexchange.reseller.data;

import java.io.Serializable;

/**
 * {@link Restriction} - A restriction associated with a reseller administrator.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class Restriction implements Cloneable, Serializable {

    private static final long serialVersionUID = -5606359645775067497L;

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
    public static final String[] getAllRestrictions() {
        return new String[] {
            MAX_CONTEXT_PER_SUBADMIN,
            MAX_OVERALL_CONTEXT_QUOTA_PER_SUBADMIN,
            MAX_OVERALL_USER_PER_SUBADMIN,
            MAX_USER_PER_CONTEXT,
            SUBADMIN_CAN_CREATE_SUBADMINS,
            MAX_SUBADMIN_PER_SUBADMIN
        };
    }

    // -----------------------------------------------------------------------------------------

    private final Integer id;
    private final String name;
    private final String value;

    /**
     * Initializes a new {@link Restriction}.
     *
     * @param id The identifier
     * @param name The name
     * @param value The value
     */
    public Restriction(Integer id, String name, String value) {
        super();
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
        if (!(obj instanceof Restriction)) {
            return false;
        }
        Restriction other = (Restriction) obj;
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
        int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Restriction [id=" + id + ", name=" + name + ", value=" + value + "]";
    }

    @Override
    public Restriction clone() {
        try {
            return (Restriction) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError("CloneNotSupportedException although Cloneable is implemented", e);
        }
    }

}

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

package com.openexchange.resource;

/**
 * {@link ResourceGroup} - This class implements the data container for the attributes of a resource group.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ResourceGroup {

    /**
     * The unique identifier of the group.
     */
    private int id = -1;

    /**
     * The identifier of the group.
     */
    private String identifier;

    /**
     * The display name of the group. Currently the identifier is used as display name so this attribute will also be filled with the
     * identifier. But the identifier is limited in the characters that can be used and a display name won't be limited.
     */
    private String displayName;

    /**
     * If a resource group is not available, all its resources can't be booked.
     */
    private boolean available;

    /**
     * Resources that are member of this resource group.
     */
    private int[] member;

    /**
     * Description of this group.
     */
    private String description;

    /**
     * Default constructor.
     */
    public ResourceGroup() {
        super();
    }

    /**
     * Getter for id.
     *
     * @return @returns the unique identifier.
     */
    public int getId() {
        return id;
    }

    /**
     * Getter for identifier.
     *
     * @return @returns the identifier.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Getter for the displayName.
     *
     * @return display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns if the resource group is available. The resources of the group can only be booked if the group is available.
     *
     * @return <code>true</code> if the resource is available.
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Setter for member.
     *
     * @param member Identifier of resources that are member.
     */
    public void setMember(final int[] member) {
        this.member = member;
    }

    /**
     * Getter for member.
     *
     * @return Identifier of resources that are member.
     */
    public int[] getMember() {
        final int[] retval = new int[member.length];
        System.arraycopy(member, 0, retval, 0, member.length);
        return retval;
    }

    /**
     * Setter for id.
     *
     * @param id Unique identifier.
     */
    public void setId(final int id) {
        this.id = id;
    }

    /**
     * Setter for identifier.
     *
     * @param identifier identifier.
     */
    public void setIdentifier(final String identifier) {
        this.identifier = identifier;
    }

    /**
     * Setter for available.
     *
     * @param available <code>true</code> if the resource group is available.
     */
    public void setAvailable(final boolean available) {
        this.available = available;
    }

    /**
     * Setter for displayName.
     *
     * @param displayName Display name.
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Setter for description.
     *
     * @param description Description.
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Getter for description.
     *
     * @return Description.
     */
    public String getDescription() {
        return description;
    }
}

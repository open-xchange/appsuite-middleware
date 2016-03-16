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

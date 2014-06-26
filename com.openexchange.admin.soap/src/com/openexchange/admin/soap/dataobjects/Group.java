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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.admin.soap.dataobjects;


/**
 *
 * This class represents a group.
 */
public class Group {

    private Integer id;

    private String name;

    private String displayname;

    private Integer[] members;

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

    public Group(final com.openexchange.admin.rmi.dataobjects.Group grp) {
        super();
        this.id = grp.getId();
        this.name = grp.getName();
        this.displayname = grp.getDisplayname();
        this.members = grp.getMembers();
    }

    private void init(){
        this.id = null;
        this.name = null;
        this.displayname = null;
        this.members = null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.NameAndIdObject#getId()
     */
    public final Integer getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.NameAndIdObject#setId(java.lang.Integer)
     */
    public final void setId(final Integer val) {
        this.id = val;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.NameAndIdObject#getName()
     */
    public final String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.openexchange.admin.rmi.dataobjects.NameAndIdObject#setName(java.lang.String)
     */
    public final void setName(final String val) {
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
        this.members = members;
    }

}

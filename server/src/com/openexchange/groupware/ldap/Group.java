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

package com.openexchange.groupware.ldap;

import java.util.Date;

/**
 * This is the data container class for group.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class Group implements Cloneable {

    /**
     * Unique nummeric identifier of this group.
     */
    private int identifier;

    /**
     * This name of the group has some restrictions for the character that can
     * be used.
     */
    private String simpleName;

    /**
     * Member of this group.
     * TODO should be int[]
     */
    private int[] member;

    /**
     * Display name of this group.
     */
    private String displayName;

    /**
     * Timestamp of the last modification of the group.
     */
    private Date lastModified;

    /**
     * Default constructor.
     */
    public Group() {
        super();
    }

    /**
     * Setter for displayName.
     * @param displayName Display name.
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    /**
     * Getter for displayName.
     * @return Display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Setter for unique identifier.
     * @param identifier unique identifier.
     */
    public void setIdentifier(final int identifier) {
        this.identifier = identifier;
    }

    /**
     * Getter for unique identifier.
     * @return unique identifier.
     */
    public int getIdentifier() {
        return identifier;
    }

    /**
     * Setter for member.
     * @param member Member.
     */
    public void setMember(final int[] member) {
        this.member = member;
    }

    /**
     * Getter for member.
     * @return the members of the group. If the group doesn't have any members
     * an empty array will be returned.
     */
    public int[] getMember() {
        return member;
    }

    /**
     * @return Returns the lastModified.
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * @param lastModified The lastModified to set.
     */
    public void setLastModified(final Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        final Group retval = (Group) super.clone();
        if (null != lastModified) {
            retval.lastModified = (Date) lastModified.clone();
        }
        if (null != member) {
            retval.member = new int[member.length];
            System.arraycopy(member, 0, retval.member, 0, member.length);
        }
        return retval;
    }
}

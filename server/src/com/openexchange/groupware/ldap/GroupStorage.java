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

import com.openexchange.groupware.contexts.Context;

/**
 * This is the central interface class to the store of groups.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class GroupStorage {

    /**
     * Attribute name of the group displayName.
     */
    public static final String DISPLAYNAME;

    /**
     * Attribute name of the group identifier.
     */
    public static final String IDENTIFIER;

    /**
     * Attribute name of the group identifier.
     */
    public static final String COMMONNAME;

    /**
     * Attribute name of the group member attribute.
     */
    public static final String MEMBER;

    /**
     * Attribute name containing the last modification timestamp.
     */
    public static final String LAST_MODIFIED;

    /**
     * Proxy attribute for the class implementing this interface.
     */
    private static Class<? extends GroupStorage> implementingClass;

    /**
     * Private constructor to prevent instantiation.
     */
    protected GroupStorage() {
        super();
    }

    /**
     * Reads a group from the persistent storage.
     * @param gid Unique identifier of the group.
     * @return The group data object.
     * @throws LdapException if an error occurs.
     */
    public abstract Group getGroup(final int gid) throws LdapException;

    /**
     * This method implements a universal search for groups. You have to define
     * additionally to the search pattern the attributes that should be searched
     * in. You can also name the attributes that values should be returned.
     * Please insure that returned attributes are strings and not any other data
     * types. You will get a Set with string arrays. The string arrays contain
     * the values auf the requested attributes in the same order.
     * @param pattern this pattern will be searched in the displayName of the
     * group.
     * @return an array of groups that match the search pattern.
     * @throws LdapException if an error occurs while searching for groups.
     */
    public abstract Group[] searchGroups(String pattern) throws LdapException;

    /**
     * This method returns groups that have been modified since the given
     * timestamp.
     * @param modifiedSince timestamp after that the groups have been modified.
     * @return an array of groups.
     * @throws LdapException if an error occurs.
     */
    public abstract Group[] listModifiedGroups(Date modifiedSince)
        throws LdapException;

    /**
     * Returns the data objects of all groups.
     * @return all groups.
     * @throws LdapException if an error occurs.
     */
    public abstract Group[] getGroups() throws LdapException;

    /**
     * Creates a new instance implementing the group storage interface.
     * @param context Context.
     * @return an instance implementing the group storage interface.
     * @throws LdapException if the instance can't be created.
     */
    public static GroupStorage getInstance(final Context context)
        throws LdapException {
        return getInstance(context, false);
    }

    /**
     * Creates a new instance implementing the group storage interface. The
     * returned instance can also handle the group with identifier 0.
     * @param context Context.
     * @param group0 <code>true</code> if group with identifier 0 should be
     * handled.
     * @return an instance implementing the group storage interface.
     * @throws LdapException if the instance can't be created.
     */
    public static GroupStorage getInstance(final Context context,
        final boolean group0) throws LdapException {
        synchronized (GroupStorage.class) {
            if (null == implementingClass) {
                final String className = LdapUtility.findProperty(Names.
                    GROUPSTORAGE_IMPL);
                implementingClass = LdapUtility.getImplementation(className,
                    GroupStorage.class);
            }
        }
        final GroupStorage impl = LdapUtility.getInstance(implementingClass,
            context);
        final GroupStorage retval;
        if (group0) {
            retval = new GroupsWithGroupZero(context, impl);
        } else {
            retval = impl;
        }
        return retval;
    }

    static {
        try {
            DISPLAYNAME = LdapUtility.findProperty(Names
                .GROUP_ATTRIBUTE_DISPLAYNAME);
            IDENTIFIER = LdapUtility.findProperty(Names.
                GROUP_ATTRIBUTE_IDENTIFIER);
            COMMONNAME = LdapUtility.findProperty(Names
                .GROUP_ATTRIBUTE_COMMONNAME);
            MEMBER = LdapUtility.findProperty(Names
                .GROUP_ATTRIBUTE_MEMBER);
            LAST_MODIFIED = LdapUtility.findProperty(Names
                .GROUP_ATTRIBUTE_LASTMODIFIED);
        } catch (LdapException e) {
            throw new RuntimeException(e);
        }
    }
}

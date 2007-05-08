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
 * This interface provides methods to read resources and their groups from the
 * directory service. This class is implemented according the DAO design
 * pattern.
 * @author <a href="mailto:marcus@open-xchange.de">Marcus Klein </a>
 */
public abstract class ResourceStorage {

    /**
     * Proxy attribute for the class implementing this interface.
     */
    private static Class<? extends ResourceStorage> implementingClass;

    /**
     * Default constructor.
     */
    protected ResourceStorage() {
        super();
    }

    /**
     * Creates a new instance implementing the resources interface.
     * @param context Context.
     * @return an instance implementing the resources interface.
     * @throws LdapException if the instance can't be created.
     */
    public static ResourceStorage getInstance(final Context context)
        throws LdapException {
        synchronized (ResourceStorage.class) {
            if (null == implementingClass) {
                final String className = LdapUtility.findProperty(
                    Names.RESOURCESTORAGE_IMPL);
                implementingClass = LdapUtility.getImplementation(className,
                    ResourceStorage.class);
            }
        }
        return LdapUtility.getInstance(implementingClass, context);
    }

    /**
     * Reads the data of resource group from the underlying persistent data
     * storage.
     * @param groupId Identifier of the resource group.
     * @return a resource group object.
     * @throws LdapException if an error occurs while reading from the
     * persistent storage or the resource group doesn't exist.
     */
    public abstract ResourceGroup getGroup(int groupId) throws LdapException;

    public abstract ResourceGroup[] getGroups() throws LdapException;

    /**
     * Reads a resource from the underlying persistant storage and returns it in
     * a data object.
     * @param resourceId The unique identifier of the resource to return.
     * @return The data object of the resource.
     * @throws LdapException if the resource can't be found or an exception
     * appears while reading it.
     */
    public abstract Resource getResource(int resourceId)
        throws LdapException;

    /**
     * Searches all groups whose identifier matches the given pattern.
     * @param pattern The identifier of all returned groups will match this
     * pattern.
     * @return a string array with resource group identifiers. If no identifiers
     * match an empty array will be returned.
     * @throws LdapException if an exception occurs while reading from the
     * underlying persistent storage.
     */
    public abstract ResourceGroup[] searchGroups(String pattern)
        throws LdapException;

    /**
     * Searches all resources that identifier matches the given pattern.
     * @param pattern The identifier of all returned resources will match this
     * pattern.
     * @return a string array with the resource identifiers. If no identifiers
     * match, an empty array will be returned.
     * @throws LdapException if an exception occurs while reading from the
     * underlying persistent storage.
     */
    public abstract Resource[] searchResources(String pattern)
        throws LdapException;

    /**
     * This method returns resources that have been modified since the given
     * timestamp.
     * @param modifiedSince timestamp after that the resources have been
     * modified.
     * @return an array of resources.
     * @throws LdapException if an error occurs.
     */
    public abstract Resource[] listModified(Date modifiedSince)
        throws LdapException;
}

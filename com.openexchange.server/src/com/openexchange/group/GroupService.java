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

package com.openexchange.group;

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * This service defines the API to the groups component.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
@SingletonService
public interface GroupService {

    /**
     * Creates a group.
     *
     * @param ctx Context.
     * @param user User for permission checks.
     * @param group group to create.
     * @param checkI18nNames Whether to check i18n names
     * @throws OXException if some problem occurs.
     */
    void create(Context ctx, User user, Group group, boolean checkI18nNames) throws OXException;

    /**
     * Updates a group.
     *
     * @param ctx Context.
     * @param user User for permission checks.
     * @param group group to update.
     * @param lastRead timestamp when the group to update has last been read.
     * @param checkI18nNames Whether to check i18n names
     * @throws OXException if some problem occurs.
     */
    void update(Context ctx, User user, Group group, Date lastRead, boolean checkI18nNames) throws OXException;

    /**
     * Deletes a group.
     *
     * @param ctx Context.
     * @param user User for permission checks.
     * @param groupId unique identifier of the group to delete.
     * @param lastModified timestamp when the group to delete has last been read.
     * @throws OXException if some problem occurs.
     */
    void delete(Context ctx, User user, int groupId, Date lastModified) throws OXException;

    /**
     * Returns the denoted group.
     *
     * @param ctx The context
     * @param groupId The group identifier
     * @return The group
     * @throws OXException If group cannot be returned
     */
    Group getGroup(Context ctx, int groupId) throws OXException;

    /**
     * Searches for groups by their display name.
     *
     * @param ctx The context
     * @param pattern The pattern to search for
     * @param loadMembers <code>true</code> to load the members of found groups, <code>false</code>, otherwise
     * @return An array of groups that match the search pattern
     * @throws OXException if searching has some storage related problem.
     */
    Group[] search(Context ctx, String pattern, boolean loadMembers) throws OXException;

}

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

package com.openexchange.group.internal;

import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class GroupServiceImpl implements GroupService {

    public GroupServiceImpl() {
        super();
    }

    @Override
    public void create(Context context, User user, Group group, boolean checkI18nNames) throws OXException {
        Create create = new Create(context, user, group, checkI18nNames);
        create.perform();
    }

    @Override
    public void delete(Context context, User user, int groupId, Date lastRead) throws OXException {
        Delete delete = new Delete(context, user, groupId, lastRead);
        delete.perform();
    }

    @Override
    public Group getGroup(Context context, int groupId) throws OXException {
        return GroupStorage.getInstance().getGroup(groupId, context);
    }

    @Override
    public Group[] getGroup(Context context, int[] groupIds) throws OXException {
        return GroupStorage.getInstance().getGroup(groupIds, context);
    }

    @Override
    public Group[] search(Context context, String pattern, boolean loadMembers) throws OXException {
        return GroupStorage.getInstance().searchGroups(pattern, loadMembers, context);
    }

    @Override
    public Group[] listAllGroups(Context context, boolean loadMembers) throws OXException {
        return GroupStorage.getInstance().getGroups(loadMembers, context);
    }

    @Override
    public Group[] listModifiedGroups(Context context, Date modifiedSince) throws OXException {
        return GroupStorage.getInstance().listModifiedGroups(modifiedSince, context);
    }

    @Override
    public Group[] listDeletedGroups(Context context, Date modifiedSince) throws OXException {
        return GroupStorage.getInstance().listDeletedGroups(modifiedSince, context);
    }

    @Override
    public void update(Context context, User user, Group group, Date lastRead, boolean checkI18nNames) throws OXException {
        Update update = new Update(context, user, group, lastRead, checkI18nNames);
        update.perform();
    }
}

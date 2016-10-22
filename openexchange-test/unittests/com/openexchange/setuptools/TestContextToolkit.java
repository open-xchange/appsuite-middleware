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

package com.openexchange.setuptools;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.resource.storage.ResourceStorage;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class TestContextToolkit {

    public int resolveUser(final String username) throws OXException {
        return resolveUser(username, getDefaultContext());
    }

    public int resolveUser(final String username, final Context ctx) {
        UserStorage uStorage = null;
        try {
            uStorage = UserStorage.getInstance();
            final int pos = username.indexOf('@');
            return uStorage.getUserId((pos == -1 ? username : username.substring(0, pos)), ctx);
        } catch (final OXException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int resolveResource(final String resource) throws OXException {
        return resolveResource(resource, getDefaultContext());
    }

    public int resolveResource(final String resource, final Context ctx) {
        ResourceStorage rStorage = null;
        try {
            rStorage = ResourceStorage.getInstance();
            return rStorage.searchResources(resource, ctx)[0].getIdentifier();
        } catch (final OXException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int resolveGroup(final String group) throws OXException {
        return resolveGroup(group, getDefaultContext());
    }

    public int resolveGroup(final String group, final Context ctx) {
        GroupStorage gStorage = null;
        try {
            gStorage = GroupStorage.getInstance();
            return gStorage.searchGroups(group, true, ctx)[0].getIdentifier();
        } catch (final OXException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public Context getDefaultContext() throws OXException {
        final TestConfig config = new TestConfig();

        return getContextByName(config.getContextName());
    }

    public Context getContextByName(final String name) {
        try {
            final ContextStorage storage = ContextStorage.getInstance();
            return storage.getContext(storage.getContextId(name));
        } catch (final OXException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int[] getGroups(final int user, final Context ctx) {
        try {
            return UserConfigurationStorage.getInstance().getUserConfiguration(user, ctx).getGroups();
        } catch (final OXException e) {
            e.printStackTrace();
            return new int[0];
        }
    }

    public Session getSessionForUser(final String user, final Context ctx) {
        final int userId = resolveUser(user, ctx);
        return SessionObjectWrapper.createSessionObject(userId, ctx, "session for " + user);
    }

    public Group loadGroup(final int id, final Context ctx) {
        GroupStorage gStorage = null;
        try {
            gStorage = GroupStorage.getInstance();
            return gStorage.getGroup(id, ctx);
        } catch (final OXException e) {
            e.printStackTrace();
            return null;
        }
    }

    public User loadUser(final int userId, final Context ctx) {
        UserStorage uStorage = null;
        try {
            uStorage = UserStorage.getInstance();
            return uStorage.getUser(userId, ctx);
        } catch (final OXException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<UserParticipant> users(final Context ctx, final String... users) {
        final List<UserParticipant> participants = new ArrayList<UserParticipant>(users.length);
        final TestContextToolkit tools = new TestContextToolkit();
        for (final String user : users) {
            final int id = tools.resolveUser(user, ctx);
            final UserParticipant participant = new UserParticipant(id);
            participants.add(participant);
        }
        return participants;
    }

    public List<ResourceParticipant> resources(final Context ctx, final String... resources) {
        final List<ResourceParticipant> participants = new ArrayList<ResourceParticipant>(resources.length);
        final TestContextToolkit tools = new TestContextToolkit();
        for (final String resource : resources) {
            final int id = tools.resolveResource(resource, ctx);
            final ResourceParticipant participant = new ResourceParticipant(id);
            participants.add(participant);
        }
        return participants;
    }

    public List<GroupParticipant> groups(final Context ctx, final String... groups) {
        final List<GroupParticipant> participants = new ArrayList<GroupParticipant>(groups.length);
        final TestContextToolkit tools = new TestContextToolkit();
        for (final String group : groups) {
            final int id = tools.resolveGroup(group, ctx);
            final GroupParticipant participant = new GroupParticipant(id);
            participants.add(participant);
        }
        return participants;
    }

}

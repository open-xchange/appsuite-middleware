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
package com.openexchange.groupware.calendar.tools;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.ldap.*;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.ResourceParticipant;
import com.openexchange.groupware.container.GroupParticipant;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.SessionObjectWrapper;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class CalendarContextToolkit {

    public int resolveUser(String username) {
        return resolveUser(username, getDefaultContext());
    }

    public int resolveUser(String username, Context ctx) {
        UserStorage uStorage = null;
        try {
            uStorage = UserStorage.getInstance();
            return uStorage.getUserId(username, ctx);
        } catch (LdapException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int resolveResource(String resource) {
        return resolveResource(resource, getDefaultContext());
    }

    public int resolveResource(String resource, Context ctx) {
        ResourceStorage rStorage = null;
        try {
            rStorage = ResourceStorage.getInstance();
            return rStorage.searchResources(resource, ctx)[0].getIdentifier();
        } catch (LdapException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public int resolveGroup(String group) {
        return resolveGroup(group, getDefaultContext());
    }

    public int resolveGroup(String group, Context ctx) {
       GroupStorage gStorage = null;
        try {
            gStorage = GroupStorage.getInstance();
            return gStorage.searchGroups(group, ctx)[0].getIdentifier();
        } catch (LdapException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public Context getDefaultContext() {
        try {
            return ContextStorage.getInstance().getContext(ContextStorage.getInstance().getContextId("defaultcontext"));
        } catch (ContextException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int[] getGroups(int user, Context ctx) {
        try {
            return UserConfigurationStorage.getInstance().getUserConfiguration(user,ctx).getGroups();
        } catch (UserConfigurationException e) {
            e.printStackTrace();
            return new int[0];
        }
    }

    public Session getSessionForUser(String user, Context ctx) {
        int userId = resolveUser(user, ctx);
        return SessionObjectWrapper.createSessionObject(userId,ctx,"session for "+user);    
    }

    public Group loadGroup(int id, Context ctx) {
        GroupStorage gStorage = null;
        try {
            gStorage = GroupStorage.getInstance();
            return gStorage.getGroup(id, ctx);
        } catch (LdapException e) {
            e.printStackTrace();
            return null;
        }
    }

    public User loadUser(int userId, Context ctx) {
        UserStorage uStorage = null;
        try {
            uStorage = UserStorage.getInstance();
            return uStorage.getUser(userId, ctx);
        } catch (LdapException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<UserParticipant> users(Context ctx, String...users) {
        List<UserParticipant> participants = new ArrayList<UserParticipant>(users.length);
        CalendarContextToolkit tools = new CalendarContextToolkit();
        for(String user : users) {
            int id = tools.resolveUser(user, ctx);
            UserParticipant participant = new UserParticipant(id);
            participants.add( participant );
        }
        return participants;
    }

    public List<ResourceParticipant> resources(Context ctx, String...resources) {
        List<ResourceParticipant> participants = new ArrayList<ResourceParticipant>(resources.length);
        CalendarContextToolkit tools = new CalendarContextToolkit();
        for(String resource : resources) {
            int id = tools.resolveResource(resource, ctx);
            ResourceParticipant participant = new ResourceParticipant(id);
            participants.add( participant );
        }
        return participants;
    }

    public List<GroupParticipant> groups(Context ctx, String...groups) {
        List<GroupParticipant> participants = new ArrayList<GroupParticipant>(groups.length);
        CalendarContextToolkit tools = new CalendarContextToolkit();
        for(String group : groups) {
            int id = tools.resolveGroup(group, ctx);
            GroupParticipant participant = new GroupParticipant(id);
            participants.add( participant );
        }
        return participants;
    }

}

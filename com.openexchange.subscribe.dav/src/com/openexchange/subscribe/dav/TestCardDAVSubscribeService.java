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

package com.openexchange.subscribe.dav;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import com.google.common.collect.ImmutableMap;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.subscribe.Subscription;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link TestCardDAVSubscribeService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class TestCardDAVSubscribeService extends AbstractCardDAVSubscribeService {

    public static void main(String[] args) throws OXException {
        Subscription subscription = new Subscription();
        subscription.setConfiguration(ImmutableMap.of("login", "dimitribronkowitsch@web.de", "password", "Wh79Q49#e6o}#$,"));
        subscription.setSession(new ServerSession() {

            @Override
            public void setParameter(String name, Object value) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setLocalIp(String ip) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setHash(String hash) {
                // TODO Auto-generated method stub

            }

            @Override
            public void setClient(String client) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean isTransient() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public String getUserlogin() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getUserId() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public String getSessionID() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getSecret() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getRandomToken() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getPassword() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Set<String> getParameterNames() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Object getParameter(String name) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getLoginName() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getLogin() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getLocalIp() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getHash() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public int getContextId() {
                // TODO Auto-generated method stub
                return 0;
            }

            @Override
            public String getClient() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getAuthId() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean containsParameter(String name) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public boolean isAnonymous() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public UserSettingMail getUserSettingMail() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public UserPermissionBits getUserPermissionBits() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public UserConfiguration getUserConfiguration() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public User getUser() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Context getContext() {
                // TODO Auto-generated method stub
                return null;
            }
        });
        TestCardDAVSubscribeService test = new TestCardDAVSubscribeService();
        test.getContent(subscription);
    }

    /**
     * Initializes a new {@link TestCardDAVSubscribeService}.
     */
    public TestCardDAVSubscribeService() {
        super(null);
    }

    @Override
    protected URI getBaseUrl(ServerSession session) {
        try {
            return new URI("https://carddav.web.de/CardDavProxy/carddav");
        } catch (URISyntaxException e) {
            // Ignore
        }
        return null;
    }
    @Override
    protected URI getUserPrincipal(ServerSession session) {
        try {
            return new URI("https://carddav.web.de/CardDavProxy/carddav/current-user-principal-uri");
        } catch (URISyntaxException e) {
            // Ignore
        }
        return null;
    }

    @Override
    protected int getChunkSize(ServerSession session) {
        return 25;
    }

    @Override
    protected String getDisplayName() {
        return "Peter Silie";
    }

    @Override
    protected String getId() {
        return "com.openexchange.subscribe.dav.test";
    }



}

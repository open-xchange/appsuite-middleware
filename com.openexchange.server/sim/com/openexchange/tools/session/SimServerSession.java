/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.tools.session;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.ldap.MockUser;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.session.Origin;
import com.openexchange.user.User;

/**
 * {@link SimServerSession}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SimServerSession implements ServerSession {

    private Context context;
    private User user;
    private UserConfiguration userConfig;
    private String login;
    private UserPermissionBits userPermissionBits;
    private final Map<String, Object> parameters;

    public SimServerSession(final Context context, final User user, final UserConfiguration userConfig) {
        super();
        this.context = context;
        this.user = user;
        this.userConfig = userConfig;
        this.parameters = new HashMap<String, Object>();
    }

    public SimServerSession(final int ctxId, final int uid) {
        this(new SimContext(ctxId), null, null);
        this.user = new MockUser(uid);
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public UserConfiguration getUserConfiguration() {
        return userConfig;
    }

    /**
     * Sets the user permission bits
     *
     * @param userPermissionBits The user permission bits to set
     */
    public void setUserPermissionBits(UserPermissionBits userPermissionBits) {
        this.userPermissionBits = userPermissionBits;
    }

    @Override
    public UserPermissionBits getUserPermissionBits() {
        return userPermissionBits;
    }

    @Override
    public UserSettingMail getUserSettingMail() {
        return null;
    }

    @Override
    public boolean containsParameter(final String name) {
        return parameters.containsKey(name);
    }

    @Override
    public int getContextId() {
        return context.getContextId();
    }

    @Override
    public String getLocalIp() {
        return null;
    }

    @Override
    public String getLogin() {
        return login;
    }

    @Override
    public String getLoginName() {
        return null;
    }

    @Override
    public Object getParameter(final String name) {
        return parameters.get(name);
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getRandomToken() {
        return null;
    }

    @Override
    public String getSecret() {
        return null;
    }

    @Override
    public String getSessionID() {
        return null;
    }

    @Override
    public int getUserId() {
        return null == user ? 0 : user.getId();
    }

    @Override
    public String getUserlogin() {
        return null;
    }

    @Override
    public void setParameter(final String name, final Object value) {
        parameters.put(name, value);
    }

    @Override
    public String getAuthId() {
        return null;
    }

    @Override
    public String getHash() {
        // Nothing to do
        return null;
    }

    @Override
    public void setLocalIp(final String ip) {
        // Nothing to do

    }

    @Override
    public void setHash(final String hash) {
        // Nothing to do
    }

    public void setContext(final Context context) {
        this.context = context;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public void setUserConfig(final UserConfiguration userConfig) {
        this.userConfig = userConfig;
    }

    public void setLogin(final String login) {
        this.login = login;
    }

    @Override
    public String getClient() {
        // Nothing to do
        return null;
    }

    @Override
    public void setClient(final String client) {
        // Nothing to do
    }

	@Override
	public boolean isAnonymous() {
		// Nothing to do
		return false;
	}

    @Override
    public boolean isTransient() {
        return false;
    }

    @Override
    public boolean isStaySignedIn() {
        return false;
    }

    @Override
    public Set<String> getParameterNames() {
        return Collections.emptySet();
    }

    @Override
    public Origin getOrigin() {
        // TODO Auto-generated method stub
        return null;
    }

}

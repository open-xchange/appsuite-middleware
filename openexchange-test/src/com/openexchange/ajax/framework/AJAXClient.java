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

package com.openexchange.ajax.framework;

import java.io.IOException;
import org.json.JSONException;
import com.openexchange.ajax.session.LoginTools;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.ajax.session.actions.LogoutRequest;
import com.openexchange.exception.OXException;
import com.openexchange.test.common.configuration.AJAXConfig;
import com.openexchange.test.common.test.pool.TestUser;

/**
 * This class implements the temporary memory of an AJAX client and provides some convenience methods to determine user specific values for
 * running some tests more easily.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class AJAXClient {

    public static final String VERSION = "7.4.2";

    private final AJAXSession session;

    private final UserValues values = new UserValues(this);

    private boolean mustLogout;

    private String hostname = null;

    private String protocol = null;

    public AJAXClient(final AJAXSession session, final boolean logout) {
        super();
        this.session = session;
        this.mustLogout = logout;
    }

    public AJAXClient(TestUser user) throws OXException, IOException, JSONException {
        this(user, AJAXClient.class.getName());
    }

    public AJAXClient(TestUser user, String client) throws OXException, IOException, JSONException {
        super();

        if (hostname == null) {
            this.hostname = AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);
        }

        if (protocol == null) {
            this.protocol = AJAXConfig.getProperty(AJAXConfig.Property.PROTOCOL);
            if (this.protocol == null) {
                this.protocol = "http";
            }
        }
        session = new AJAXSession();
        session.setId(execute(new LoginRequest(user.getLogin(), user.getPassword(), LoginTools.generateAuthId(), null == client ? AJAXClient.class.getName() : client, VERSION)).getSessionId());
    }

    /**
     * @return the session
     */
    public AJAXSession getSession() {
        return session;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mustLogout) {
                logout();
            }
        } finally {
            super.finalize();
        }
    }

    public void logout() throws OXException, IOException, JSONException {
        if (null != session.getId()) {
            execute(new LogoutRequest(true));
            session.setId(null);
        }
        session.getConversation().clearContents();
        session.getHttpClient().getConnectionManager().shutdown();
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setMustLogout(boolean mustLogout) {
        this.mustLogout = mustLogout;
    }

    /**
     * @return the values
     */
    public UserValues getValues() {
        return values;
    }

    public <T extends AbstractAJAXResponse> T execute(final AJAXRequest<T> request, final int sleep) throws OXException, IOException, JSONException {
        if (hostname != null && protocol != null) {
            // TODO: Maybe assume http as default protocol
            if (sleep != -1) {
                return Executor.execute(getSession(), request, getProtocol(), getHostname(), sleep);
            }
            return Executor.execute(getSession(), request, getProtocol(), getHostname());
        }

        return Executor.execute(this, request);
    }

    public <T extends AbstractAJAXResponse> T execute(final AJAXRequest<T> request) throws OXException, IOException, JSONException {
        return execute(request, -1);
    }

    /**
     * Executes given request, swallowing all possible exceptions.
     *
     * @param request The request to execute
     * @return The response or <code>null</code> in case an error occurred
     */
    public <T extends AbstractAJAXResponse> T executeSafe(final AJAXRequest<T> request) {
        try {
            return execute(request, -1);
        } catch (Exception e) {
            // ignore
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unused")
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Protocol: " + this.protocol + ", ");
        builder.append("Hostname: " + this.hostname + ", ");
        try {
            builder.append("ContextId: " + this.getValues().getContextId() + ", ");
            builder.append("UserId: " + this.getValues().getUserId());
        } catch (OXException e) {
        } catch (IOException e) {
        } catch (JSONException e) {
        }

        return builder.toString();
    }
}

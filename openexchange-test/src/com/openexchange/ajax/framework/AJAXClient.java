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

package com.openexchange.ajax.framework;

import java.io.IOException;
import org.json.JSONException;
import com.openexchange.ajax.session.LoginTools;
import com.openexchange.ajax.session.actions.LoginRequest;
import com.openexchange.ajax.session.actions.LogoutRequest;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.exception.OXException;
import com.openexchange.test.pool.TestUser;

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

    public AJAXClient(TestUser user) throws OXException, OXException, IOException, JSONException {
        this(user, AJAXClient.class.getName());
    }

    public AJAXClient(TestUser user, String client) throws OXException, OXException, IOException, JSONException {
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
            } else {
                return Executor.execute(getSession(), request, getProtocol(), getHostname());
            }
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
        } catch (final Exception e) {
            // ignore
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
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

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

package com.openexchange.mailfilter.json.ajax.actions;

import javax.security.auth.Subject;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.mailfilter.Credentials;
import com.openexchange.mailfilter.json.ajax.Action;
import com.openexchange.mailfilter.json.ajax.Parameter;
import com.openexchange.mailfilter.json.osgi.Services;
import com.openexchange.mailfilter.properties.CredentialSource;
import com.openexchange.mailfilter.properties.MailFilterProperty;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractRequest {

    private static final String KERBEROS_SESSION_SUBJECT = "kerberosSubject";

    private Session session;
    private Parameters parameters;

    /**
     * The body of a PUT request.
     */
    private String body;

    /**
     * Default constructor.
     */
    protected AbstractRequest() {
        super();
    }

    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * @return the parameters
     */
    public Parameters getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }

    /**
     * @return the session
     */
    public Session getSession() {
        return session;
    }

    /**
     * @param session the session to set
     */
    public void setSession(Session session) {
        this.session = session;
    }

    /**
     * Returns the {@link Credentials} for the user
     * 
     * @return the {@link Credentials} for the user
     */
    public Credentials getCredentials() {
        LeanConfigurationService config = Services.getService(LeanConfigurationService.class);

        int userId = session.getUserId();
        int contextId = session.getContextId();

        String credentialSource = config.getProperty(userId, contextId, MailFilterProperty.credentialSource);
        String loginName = CredentialSource.SESSION_FULL_LOGIN.name.equals(credentialSource) ? session.getLogin() : session.getLoginName();
        String password = session.getPassword();

        Subject subject = (Subject) session.getParameter(KERBEROS_SESSION_SUBJECT);
        String oauthToken = (String) session.getParameter(Session.PARAM_OAUTH_ACCESS_TOKEN);

        try {
            String username = getUsername();
            return new Credentials(loginName, password, userId, contextId, username, subject, oauthToken);
        } catch (@SuppressWarnings("unused") OXException e) {
            return new Credentials(loginName, password, userId, contextId, null, subject, oauthToken);
        }
    }

    /**
     * @return the action
     * @throws OXException
     */
    public Action getAction() throws OXException {
        Parameter action = Parameter.ACTION;
        if (null == parameters) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(action.getName());
        }
        String value = parameters.getParameter(Parameter.ACTION);
        if (null == value) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(action.getName());
        }
        Action retval = Action.byName(value);
        if (null == retval) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create(value);
        }
        return retval;
    }

    private String getUsername() throws OXException {
        Parameter pUsername = Parameter.USERNAME;
        String username = parameters.getParameter(pUsername);
        if (username == null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(pUsername);
        }

        return username;
    }

    public interface Parameters {

        String getParameter(Parameter param) throws OXException;
    }
}

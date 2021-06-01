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

package com.openexchange.gdpr.dataexport.provider.mail.generator;

import static com.openexchange.mail.api.MailConfig.getConfiguredAuthType;
import java.util.LinkedHashMap;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.gdpr.dataexport.GeneratedSession;
import com.openexchange.gdpr.dataexport.provider.mail.MailDataExportPropertyNames;
import com.openexchange.mail.api.AuthType;
import com.openexchange.mail.api.MailConfig.LoginSource;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link AbstractSessionGenerator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public abstract class AbstractSessionGenerator implements SessionGenerator {

    /** The OSGi service look-up */
    protected final ServiceLookup services;

    private final String id;

    /**
     * Initializes a new {@link AbstractSessionGenerator}.
     *
     * @param id The identifier for this generator
     * @param services The OSGi service look-up
     */
    protected AbstractSessionGenerator(String id, ServiceLookup services) {
        super();
        this.id = id;
        this.services = services;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean isApplicable(Session session) throws OXException {
        AuthType configuredAuthType = getConfiguredAuthType(true, session);
        return isApplicable(AuthType.isOAuthType(configuredAuthType), session);
    }

    /**
     * Checks if this generator is applicable for given session.
     *
     * @param isOAuthEnabled Whether configured authentication type is one of known OAuth-based types; either <code>XOAUTH2</code> or <code>OAUTHBEARER</code>
     * @param session The session in use while submitting data export task
     * @return <code>true</code> if applicable; otherwise <code>false</code>
     * @throws OXException If applicability cannot be checked
     */
    protected abstract boolean isApplicable(boolean isOAuthEnabled, Session session) throws OXException;

    @Override
    public Map<String, Object> craftExtendedProperties(Session session) throws OXException {
        Map<String, Object> properties = new LinkedHashMap<>(6);

        // Pass login name to properties if needed
        LoginSource loginSource = MailProperties.getInstance().getLoginSource(session.getUserId(), session.getContextId());
        if (LoginSource.USER_NAME == loginSource || MailProperties.getInstance().getAuthProxyDelimiter() != null) {
            properties.put(MailDataExportPropertyNames.PROP_LOGIN_NAME, session.getLoginName());
        }

        enhanceExtendedProperties(properties, session);

        return properties;
    }

    /**
     * Enhances given extended properties by generator-specific values.
     *
     * @param extendedProperties The extended properties to enhance
     * @param session The session in use while submitting data export task
     * @throws OXException If extended properties cannot be returned
     */
    protected abstract void enhanceExtendedProperties(Map<String, Object> extendedProperties, Session session) throws OXException;

    @Override
    public GeneratedSession generateSession(int userId, int contextId, Map<String, Object> moduleProperties) throws OXException {
        GeneratedSession session = new GeneratedSession(userId, contextId);
        String loginName = (String) moduleProperties.get(MailDataExportPropertyNames.PROP_LOGIN_NAME);
        if (loginName != null) {
            session.setLoginName(loginName);
        }

        enhanceGeneratedSession(session, moduleProperties);

        return session;
    }

    /**
     * Enhances given generated session to use for accessing mail system.
     *
     * @param session The generated session to enhance
     * @param moduleProperties The module properties (with extended properties previously generated through {@link #craftExtendedProperties(Session)})
     * @return The session
     * @throws OXException If session cannot be generated
     */
    protected abstract void enhanceGeneratedSession(GeneratedSession session, Map<String, Object> moduleProperties) throws OXException;

    @Override
    public FailedAuthenticationResult onFailedAuthentication(OXException e, GeneratedSession session, Map<String, Object> moduleProperties) throws OXException {
        throw e;
    }

}

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

package com.openexchange.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.oauth.services.Services;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

/**
 * {@link AbstractOAuthServiceMetaData} - The default {@link OAuthServiceMetaData} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractOAuthServiceMetaData implements OAuthServiceMetaData {

    protected enum OAuthPropertyID {
        apiKey, apiSecret, consumerKey, consumerSecret, redirectUrl, productName;
    };

    private final Map<OAuthPropertyID, OAuthConfigurationProperty> properties;

    protected String id;
    protected String displayName;
    protected boolean needsRequestToken = true;
    protected boolean registerTokenBasedDeferrer = false;

    /**
     * Initializes a new {@link AbstractOAuthServiceMetaData}.
     */
    protected AbstractOAuthServiceMetaData() {
        super();
        properties = new ConcurrentHashMap<OAuthPropertyID, OAuthConfigurationProperty>(OAuthPropertyID.values().length, 0.9f, 1);
    }

    /**
     * Add an OAuthProperty
     *
     * @param prop The property's name
     * @param value The property's value
     */
    protected void addOAuthProperty(OAuthPropertyID prop, OAuthConfigurationProperty value) {
        properties.put(prop, value);
    }

    /**
     * Get the specified OAuthProperty
     *
     * @param prop The property's name
     * @return The property's value or null
     */
    protected OAuthConfigurationProperty getOAuthProperty(OAuthPropertyID prop) {
        return properties.get(prop);
    }

    /**
     * Get the configuration properties' names
     *
     * @return The configuration properties' names
     */
    protected String[] getConfigurationPropertyNames() {
        String[] propNames = new String[properties.size()];
        int i = 0;
        for (OAuthConfigurationProperty prop : properties.values()) {
            propNames[i++] = prop.getName();
        }
        return propNames;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getAPIKey() {
        return getOAuthProperty(OAuthPropertyID.apiKey).getValue();
    }

    @Override
    public boolean isEnabled(final int userId, final int contextId) throws OXException {
        /*
         * disable for guests
         */
        User user = Services.getService(UserService.class).getUser(userId, contextId);
        if (user.isGuest()) {
            return false;
        }
        /*
         * check config cascade for specific "enabled" property
         */
        final ConfigView view = Services.getService(ConfigViewFactory.class).getView(userId, contextId);
        final ComposedConfigProperty<Boolean> property = view.property(getEnabledProperty(), Boolean.class);
        if (!property.isDefined()) {
            return true;
        }

        return property.get().booleanValue();
    }

    /**
     * The name of the property that indicates if this service is enabled.
     * Defaults to {@link #getId()} and must be overwritten if the properties name differs
     * from the services id.
     */
    protected String getEnabledProperty() {
        return getId();
    }

    /**
     * Get the OAuthProperty from the ConfigViewFactory
     *
     * @param session The session
     * @param propertyId The property identifier
     * @return The property's value
     * @throws OXException
     */
    private String getFromConfigViewFactory(final Session session, OAuthPropertyID propertyId) throws OXException {
        OAuthConfigurationProperty oauthProperty = getOAuthProperty(propertyId);
        if (session == null) {
            return oauthProperty.getValue();
        }
        int context = 0, user = 0;
        context = session.getContextId();
        user = session.getUserId();
        return Services.getService(ConfigViewFactory.class).getView(user, context).get(oauthProperty.getName(), String.class);
    }

    @Override
    public String getAPIKey(final Session session) throws OXException {
        return getFromConfigViewFactory(session, OAuthPropertyID.apiKey);
    }

    @Override
    public String getAPISecret() {
        return getOAuthProperty(OAuthPropertyID.apiSecret).getValue();
    }

    @Override
    public String getAPISecret(final Session session) throws OXException {
        return getFromConfigViewFactory(session, OAuthPropertyID.apiSecret);
    }

    @Override
    public String getConsumerKey() {
        return getOAuthProperty(OAuthPropertyID.consumerKey).getValue();
    }

    @Override
    public String getConsumerSecret() {
        return getOAuthProperty(OAuthPropertyID.consumerKey).getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.OAuthServiceMetaData#getProductName()
     */
    @Override
    public String getProductName() {
        return getOAuthProperty(OAuthPropertyID.productName).getValue();
    }

    /**
     * Sets the identifier
     *
     * @param id The identifier to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Sets the display name
     *
     * @param displayName The display name to set
     */
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    @Override
    public void processArguments(final Map<String, Object> arguments, final Map<String, String> parameter, final Map<String, Object> state) throws OXException {
        // no-op
    }

    @Override
    public OAuthToken getOAuthToken(final Map<String, Object> arguments) throws OXException {
        return null;
    }

    @Override
    public OAuthInteraction initOAuth(final String callbackUrl, final Session session) throws OXException {
        return null;
    }

    @Override
    public boolean needsRequestToken() {
        return needsRequestToken;
    }

    @Override
    public String getScope() {
        return null;
    }

    @Override
    public String processAuthorizationURL(final String authUrl) {
        return authUrl;
    }

    @Override
    public String processAuthorizationURLCallbackAware(final String authUrl, final String callback) {
        return authUrl;
    }

    @Override
    public String modifyCallbackURL(final String callbackUrl, String currentHost, final Session session) {
        return callbackUrl;
    }

    @Override
    public boolean registerTokenBasedDeferrer() {
        return registerTokenBasedDeferrer;
    }

    @Override
    public String getRegisterToken(String authUrl) {
        return null;
    }

    /**
     * URL-encodes specified string.
     */
    protected static String urlEncode(final String s) {
        try {
            return URLEncoder.encode(s, "ISO-8859-1");
        } catch (final UnsupportedEncodingException e) {
            return s;
        }
    }
}

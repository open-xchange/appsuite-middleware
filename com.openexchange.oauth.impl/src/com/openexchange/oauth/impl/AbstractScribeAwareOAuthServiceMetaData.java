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

package com.openexchange.oauth.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.ws.handler.MessageContext.Scope;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.java.Strings;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthConfigurationProperty;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AbstractScribeAwareOAuthServiceMetaData}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractScribeAwareOAuthServiceMetaData extends AbstractOAuthServiceMetaData implements ScribeAware, Reloadable {

    protected final ServiceLookup services;

    private final List<OAuthPropertyID> propertyNames;

    private static final String PROP_PREFIX = "com.openexchange.oauth";

    private API api;

    /**
     * Initialises a new {@link AbstractScribeAwareOAuthServiceMetaData}.
     *
     * @param services the service lookup instance
     * @param api The {@link API}
     * @param scopes The {@link Scope}s
     */
    public AbstractScribeAwareOAuthServiceMetaData(final ServiceLookup services, API api, OAuthScope... scopes) {
        super(scopes);
        this.services = services;
        this.api = api;

        setId(api.getFullName());
        setDisplayName(api.getShortName());

        // Common properties for all OAuthServiceMetaData implementations.
        propertyNames = new ArrayList<>();
        propertyNames.add(OAuthPropertyID.apiKey);
        propertyNames.add(OAuthPropertyID.apiSecret);

        // Add the extra properties (if any)
        propertyNames.addAll(getExtraPropertyNames());

        // Load configuration
        loadConfiguration();
    }

    /**
     * Load the configuration.
     */
    protected void loadConfiguration() {
        ConfigurationService configService = services.getService(ConfigurationService.class);
        if (null == configService) {
            throw new IllegalStateException("Missing configuration service");
        }
        reloadConfiguration(configService);
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        StringBuilder builder = new StringBuilder();
        for (OAuthPropertyID prop : propertyNames) {
            String propName = builder.append(PROP_PREFIX).append(".").append(getPropertyId()).append(".").append(prop).toString();
            String propValue = configService.getProperty(propName);
            if (Strings.isEmpty(propValue)) {
                throw new IllegalStateException("Missing following property in configuration: " + propName);
            }
            addOAuthProperty(prop, new OAuthConfigurationProperty(propName, propValue));
            builder.setLength(0);
        }

        // Basic URL encoding
        OAuthConfigurationProperty redirectUrl = getOAuthProperty(OAuthPropertyID.redirectUrl);
        if (redirectUrl != null) {
            String r = redirectUrl.getValue().replaceAll(":", "%3A").replaceAll("/", "%2F");
            addOAuthProperty(OAuthPropertyID.redirectUrl, new OAuthConfigurationProperty(redirectUrl.getName(), r));
        }
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(getConfigurationPropertyNames());
    }

    @Override
    public API getAPI() {
        return api;
    }

    /**
     * Get the property identifier
     *
     * @return the property identifier
     */
    protected abstract String getPropertyId();

    /**
     * Get the extra property names
     *
     * @return A collection with extra property names
     */
    protected abstract Collection<OAuthPropertyID> getExtraPropertyNames();
}

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

package com.openexchange.spamhandler.spamexperts.management;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.spamhandler.spamexperts.exceptions.SpamExpertsExceptionCode;
import com.openexchange.tools.net.URIDefaults;
import com.openexchange.tools.net.URIParser;

/**
 * {@link SpamExpertsConfig}
 */
public class SpamExpertsConfig {

    private final ServiceLookup services;
    private final Cache<String, URI> uriCache;

    /**
     * Initializes a new {@link SpamExpertsConfig}.
     */
    public SpamExpertsConfig(ServiceLookup services) {
        super();
        this.services = services;
        uriCache = CacheBuilder.newBuilder().expireAfterAccess(2, TimeUnit.HOURS).build();
    }

    /**
     * Gets the user-sensitive property.
     *
     * @param session The session providing user data
     * @param propertyName The name of the property to look-up
     * @param defaultValue The default value to return
     * @param clazz The type of the property's value
     * @return The property value of given default value if absent
     * @throws OXException If property value cannot be returned
     */
    public <V> V getPropertyFor(Session session, String propertyName, V defaultValue, Class<V> clazz) throws OXException {
        ConfigViewFactory factory = services.getService(ConfigViewFactory.class);
        ConfigView view = factory.getView(session.getUserId(), session.getContextId());

        ComposedConfigProperty<V> property = view.property(propertyName, clazz);
        return (null != property && property.isDefined()) ? property.get() : defaultValue;
    }

    /**
     * Requires specified property
     *
     * @param session The session providing user data
     * @param propertyName The name of the required property
     * @return The value
     * @throws OXException If property value cannot be returned
     */
    public String requireProperty(Session session, String propertyName) throws OXException {
        String value = getPropertyFor(session, propertyName, "", String.class).trim();
        if (Strings.isEmpty(value)) {
            throw SpamExpertsExceptionCode.MISSING_CONFIG_OPTION.create(propertyName);
        }
        return value;
    }

    /**
     * Gets the denoted URI property
     *
     * @param session The session providing user data
     * @param propertyName The name of the required property
     * @param defaultValue The default value
     * @return The URI property
     * @throws OXException If URI property cannot be returned
     */
    public URI getUriProperty(Session session, String propertyName, String defaultValue) throws OXException {
        String sUri = getPropertyFor(session, propertyName, defaultValue, String.class).trim();
        return getUriFor(sUri);
    }

    /**
     * Requires the denoted URI property
     *
     * @param session The session providing user data
     * @param propertyName The name of the required property
     * @return The URI property
     * @throws OXException If URI property cannot be returned
     */
    public URI requireUriProperty(Session session, String propertyName) throws OXException {
        String sUri = requireProperty(session, propertyName);
        return getUriFor(sUri);
    }

    private URI getUriFor(String sUri) throws OXException {
        URI uri = uriCache.getIfPresent(sUri);
        if (null == uri) {
            try {
                uri = new URI(sUri);
                if (Strings.isEmpty(uri.getHost())) {
                    throw SpamExpertsExceptionCode.INVALID_URL.create(sUri);
                }
                uriCache.put(sUri, uri);
            } catch (URISyntaxException e) {
                throw SpamExpertsExceptionCode.INVALID_URL.create(e, sUri);
            }
        }
        return uri;
    }

    /**
     * Gets the configured IMAP URI
     *
     * @param session The session providing user data
     * @return The IMAP URI
     * @throws OXException If IMAP URI cannot be returned
     */
    public URI getImapURL(Session session) throws OXException {
        String iurl = requireProperty(session, "com.openexchange.custom.spamexperts.imapurl");

        URI uri = uriCache.getIfPresent(iurl);
        if (null == uri) {
            try {
                uri = URIParser.parse(iurl, URIDefaults.IMAP);
                uriCache.put(iurl, uri);
            } catch (URISyntaxException e) {
                throw SpamExpertsExceptionCode.INVALID_URL.create(e, iurl);
            }
        }
        return uri;
    }

}

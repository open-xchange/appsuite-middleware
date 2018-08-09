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

package com.openexchange.mail.config;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.net.HostList;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.UserAndContext;

/**
 * {@link MailProxyConfig}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class MailProxyConfig implements Reloadable {

    private static MailProxyConfig INSTANCE = new MailProxyConfig();

    public static MailProxyConfig getInstance() {
        return INSTANCE;
    }

    private MailProxyConfig() {
        // hide constructor
    }

    /**
     * A comma separated list of hosts that should be accessed without going through the proxy.
     */
    private static Property MAIL_NON_PROXY_HOSTS = DefaultProperty.valueOf("com.openexchange.mail.proxy.nonProxyHosts", null);

    private final Cache<UserAndContext, HostList> CACHE_NON_PROXY_HOSTS = CacheBuilder.newBuilder().maximumSize(65536).expireAfterAccess(30, TimeUnit.MINUTES).build();

    /**
     * Gets the whitelisted hosts
     *
     * @param contextId The context id
     * @param userId The user id
     * @return A list of {@link IPRange}
     * @throws OXException
     */
    public HostList getNonProxyHostList(int contextId, int userId) throws OXException {
        UserAndContext key = UserAndContext.newInstance(userId, contextId);
        HostList result = CACHE_NON_PROXY_HOSTS.getIfPresent(key);
        if (null != result) {
            return result;
        }

        Callable<HostList> loader = new Callable<HostList>() {

            @Override
            public HostList call() throws Exception {
                return doGetNonProxyHosts(userId, contextId);
            }
        };

        try {
            return CACHE_NON_PROXY_HOSTS.get(key, loader);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw cause instanceof OXException ? (OXException) cause : new OXException(cause);
        }
    }

    /**
     * Loads the non proxy hosts
     *
     * @param userId The user id
     * @param contextId The context id
     * @return The list of non proxy hosts
     * @throws OXException
     */
    protected HostList doGetNonProxyHosts(int userId, int contextId) throws OXException {
        LeanConfigurationService leanConfigService = ServerServiceRegistry.getInstance().getService(LeanConfigurationService.class);
        if (null == leanConfigService) {
            throw ServiceExceptionCode.absentService(LeanConfigurationService.class);
        }

        String property = leanConfigService.getProperty(userId, contextId, MAIL_NON_PROXY_HOSTS);
        if (Strings.isEmpty(property)) {
            return HostList.EMPTY;
        }
        return HostList.valueOf(property);
    }

    /**
     * Clears the cache.
     */
    private void invalidateCache() {
        CACHE_NON_PROXY_HOSTS.invalidateAll();
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        invalidateCache();
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().propertiesOfInterest(MAIL_NON_PROXY_HOSTS.getFQPropertyName()).build();
    }

}

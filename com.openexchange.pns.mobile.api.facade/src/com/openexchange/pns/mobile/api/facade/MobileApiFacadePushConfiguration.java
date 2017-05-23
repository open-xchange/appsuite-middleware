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

package com.openexchange.pns.mobile.api.facade;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.UserAndContext;

public class MobileApiFacadePushConfiguration {

    public static final String CONFIG_CLIENT_IDS_YAML = "pns-mobile-api-facade-clients.yml";
    public static final String CONFIG_APN_BADGE_ENABLED = "com.openexchange.pns.mobile.api.facade.apn.badge.enabled";
    public static final String CONFIG_APN_SOUND_ENABLED = "com.openexchange.pns.mobile.api.facade.apn.sound.enabled";
    public static final String CONFIG_APN_SOUND_FILENAME = "com.openexchange.pns.mobile.api.facade.apn.sound.filename";

    private static final Cache<UserAndContext, MobileApiFacadePushConfiguration> CACHE_CONFIGS = CacheBuilder.newBuilder().maximumSize(65536).expireAfterAccess(30, TimeUnit.MINUTES).build();

    /**
     * Clears the cache.
     */
    public static void invalidateCache() {
        CACHE_CONFIGS.invalidateAll();
    }

    /**
     * Gets the Mobile API Facade configuration for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param viewFactory The view factory to use
     * @return The Mobile API Facade configuration for specified user
     * @throws OXException If the Mobile API Facade configuration cannot be returned for specified user
     */
    public static MobileApiFacadePushConfiguration getConfigFor(final int userId, final int contextId, final ConfigViewFactory viewFactory) throws OXException {
        UserAndContext key = UserAndContext.newInstance(userId, contextId);
        MobileApiFacadePushConfiguration config = CACHE_CONFIGS.getIfPresent(key);
        if (null != config) {
            return config;
        }

        Callable<MobileApiFacadePushConfiguration> loader = new Callable<MobileApiFacadePushConfiguration>() {

            @Override
            public MobileApiFacadePushConfiguration call() throws Exception {
                return doGetConfigFor(userId, contextId, viewFactory);
            }
        };

        try {
            return CACHE_CONFIGS.get(key, loader);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw cause instanceof OXException ? (OXException) cause : new OXException(cause);
        }
    }

    static MobileApiFacadePushConfiguration doGetConfigFor(int userId, int contextId, ConfigViewFactory viewFactory) throws OXException {
        if (null == viewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = viewFactory.getView(userId, contextId);

        MobileApiFacadePushConfiguration.Builder builder = MobileApiFacadePushConfiguration.builder();
        builder.apnBadgeEnabled(ConfigViews.getDefinedBoolPropertyFrom(MobileApiFacadePushConfiguration.CONFIG_APN_BADGE_ENABLED, true, view));
        builder.apnSoundEnabled(ConfigViews.getDefinedBoolPropertyFrom(MobileApiFacadePushConfiguration.CONFIG_APN_SOUND_ENABLED, true, view));
        builder.apnSoundFile(ConfigViews.getDefinedStringPropertyFrom(MobileApiFacadePushConfiguration.CONFIG_APN_SOUND_FILENAME, "default", view));
        return builder.build();
    }

    // ---------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new builder.
     *
     * @return A new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** A builder for an instance of <code>MobileApiFacadePushConfiguration</code> */
    public static class Builder {

        private boolean apnBadgeEnabled;
        private boolean apnSoundEnabled;
        private String apnSoundFile;

        Builder() {
            super();
        }

        /**
         * Sets whether APN badge is enabled
         *
         * @param apnBadgeEnabled <code>true</code> if enabled; otherwise <code>false</code>
         * @return This builder
         */
        public Builder apnBadgeEnabled(boolean apnBadgeEnabled) {
            this.apnBadgeEnabled = apnBadgeEnabled;
            return this;
        }

        /**
         * Sets whether APN sound is enabled
         *
         * @param apnSoundEnabled <code>true</code> if enabled; otherwise <code>false</code>
         * @return This builder
         */
        public Builder apnSoundEnabled(boolean apnSoundEnabled) {
            this.apnSoundEnabled = apnSoundEnabled;
            return this;
        }

        /**
         * Sets the APN sound file
         *
         * @param apnSoundFile The APN sound file to set
         * @return This builder
         */
        public Builder apnSoundFile(String apnSoundFile) {
            this.apnSoundFile = apnSoundFile;
            return this;
        }

        /**
         * Builds the <code>MobileApiFacadePushConfiguration</code> instance from this builder's arguments
         *
         * @return The <code>MobileApiFacadePushConfiguration</code> instance
         */
        public MobileApiFacadePushConfiguration build() {
            return new MobileApiFacadePushConfiguration(apnBadgeEnabled, apnSoundEnabled, apnSoundFile);
        }
    }

    // ---------------------------------------------------------------------------------------------------------------

    private final boolean apnBadgeEnabled;
    private final boolean apnSoundEnabled;
    private final String apnSoundFile;

    MobileApiFacadePushConfiguration(boolean apnBadgeEnabled, boolean apnSoundEnabled, String apnSoundFile) {
        super();
        this.apnBadgeEnabled = apnBadgeEnabled;
        this.apnSoundEnabled = apnSoundEnabled;
        this.apnSoundFile = apnSoundFile;
    }

    public boolean isApnBadgeEnabled() {
        return apnBadgeEnabled;
    }

    public boolean isApnSoundEnabled() {
        return apnSoundEnabled;
    }

    public String getApnSoundFile() {
        return apnSoundFile;
    }

}

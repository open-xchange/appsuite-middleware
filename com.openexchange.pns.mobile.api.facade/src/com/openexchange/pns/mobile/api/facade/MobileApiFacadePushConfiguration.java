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

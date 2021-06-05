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

package com.openexchange.multifactor.provider.sms.demo.impl;

import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.multifactor.MultifactorProperties;
import com.openexchange.multifactor.TokenCreationStrategy;

/**
 * {@link DemoAwareTokenCreationStrategy} provides a handling for dealing with secret token-creation in demo mode.
 * <p>
 * It uses {@link DemoTokenCreationStrategy} if the demo mode (c.o.multifactor.demo) is enabled, otherwise
 * the token creation is done using the given delegate.
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class DemoAwareTokenCreationStrategy implements TokenCreationStrategy, Reloadable {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DemoAwareTokenCreationStrategy.class);
    }

    private final TokenCreationStrategy delegate;
    private final TokenCreationStrategy demoStrategy;
    private volatile boolean demoMode;

    /**
     * {@link DemoTokenCreationStrategy} is a {@link TokenCreationStrategy} for TESTING PURPOSE ONLY.
     *
     * It will always produce the same token and IS NOT SUITABLE FOR USAGE IN A PRODUCTIVE ENVIRONMENT.
     *
     * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
     * @since v7.10.2
     */
    private class DemoTokenCreationStrategy implements TokenCreationStrategy {

        public static final String DEMO_TOKEN = "0815";

        public DemoTokenCreationStrategy() {}

        @Override
        public String createToken(int length) {
            return DEMO_TOKEN;
        }
    }

    /**
     * Initializes a new {@link DemoAwareTokenCreationStrategy}.
     *
     * @param configurationService The configuration service
     * @param delegate The delegate to use for token creation if the demo mode is disabled.
     */
    public DemoAwareTokenCreationStrategy(LeanConfigurationService configurationService, TokenCreationStrategy delegate) {
        this.demoMode = configurationService.getBooleanProperty(MultifactorProperties.demo);
        this.delegate = delegate;
        this.demoStrategy = new DemoTokenCreationStrategy();
    }

    private TokenCreationStrategy getStrategy() {
        if (demoMode) {
            LoggerHolder.LOG.warn(
                "SECURITY WARNING: Current {} is {}, which will produce demo tokens for multifactor authentication! Please disable {} if this is not intended.",
                TokenCreationStrategy.class.getSimpleName(),
                DemoTokenCreationStrategy.class.getSimpleName(),
                MultifactorProperties.demo.getFQPropertyName());
            return demoStrategy;
        }
        return delegate;
    }

    @Override
    public String createToken(int length) throws OXException {
        return getStrategy().createToken(length);
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        demoMode = configService.getBoolProperty(MultifactorProperties.demo.getFQPropertyName(), false);
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().propertiesOfInterest(MultifactorProperties.demo.getFQPropertyName()).build();
    }
}

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

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(DemoAwareTokenCreationStrategy.class);

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
            logger.warn(
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

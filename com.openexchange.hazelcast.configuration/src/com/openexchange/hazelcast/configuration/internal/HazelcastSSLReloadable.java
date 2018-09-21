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

package com.openexchange.hazelcast.configuration.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.hazelcast.config.Config;
import com.hazelcast.instance.BuildInfoProvider;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadables;

/**
 * {@link HazelcastSSLReloadable}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.1
 */
public class HazelcastSSLReloadable implements ForcedReloadable {

    private final static Logger LOGGER = LoggerFactory.getLogger(HazelcastSSLReloadable.class);

    private final HazelcastConfigurationServiceImpl hzConfiguration;

    /**
     * Initializes a new {@link HazelcastSSLReloadable}.
     *
     */
    public HazelcastSSLReloadable(HazelcastConfigurationServiceImpl hzConfiguration) {
        super();
        this.hzConfiguration = hzConfiguration;
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        if (false == BuildInfoProvider.getBuildInfo().isEnterprise()) {
            return;
        }
        Config config = hzConfiguration.getConfigDirect();
        if (null == config) {
            // Hazelcast has not yet been initialized
            return;
        }

        Object factory = config.getNetworkConfig().getSSLConfig().getFactoryImplementation();
        if (null != factory && HazelcastSSLFactory.class.isAssignableFrom(factory.getClass())) {
            HazelcastSSLFactory hazelcastSSLFactory = (HazelcastSSLFactory) factory;
            try {
                hazelcastSSLFactory.init(HazelcastSSLFactory.getPropertiesFromService(configService));
            } catch (Exception e) {
                LOGGER.error("Unable to reload {}.", HazelcastSSLFactory.class.getSimpleName(), e);
            }
        }
    }

    @Override
    public Interests getInterests() {
        // Force reloadable for key stores
        return Reloadables.getInterestsForAll();
    }
}

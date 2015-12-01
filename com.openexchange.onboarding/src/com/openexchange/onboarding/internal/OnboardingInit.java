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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.onboarding.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.onboarding.DefaultIcon;
import com.openexchange.onboarding.Icon;
import com.openexchange.onboarding.OnboardingExceptionCodes;
import com.openexchange.onboarding.OnboardingType;

/**
 * {@link OnboardingInit} - Initialization class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingInit {

    private static final String CONFIGFILE_SCENARIOS = "scenarios.yml";

    /**
     * Initializes a new {@link OnboardingInit}.
     */
    public OnboardingInit() {
        super();
    }

    /**
     * Initializes the configured on-boarding scenarios.
     *
     * @param configService The configuration service to use
     * @return The configured scenarios
     * @throws OXException If initializing scenarios fails
     */
    public static Map<String, ConfiguredScenario> initScenarios(ConfigurationService configService) throws OXException {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OnboardingInit.class);

        Map<String, ConfiguredScenario> scenarios = null;

        Object yaml = configService.getYaml(CONFIGFILE_SCENARIOS);
        if (null != yaml && Map.class.isInstance(yaml)) {
            Map<String, Object> map = (Map<String, Object>) yaml;
            if (0 < map.size()) {
                scenarios = parseScenarios(map);
            }
        }

        if (null == scenarios || scenarios.isEmpty()) {
            logger.warn("No on-boarding scenarios configured in \"{}\".", CONFIGFILE_SCENARIOS);
            return Collections.emptyMap();
        }
        return scenarios;
    }

    private static Map<String, ConfiguredScenario> parseScenarios(Map<String, Object> yaml) throws OXException {
        Map<String, ConfiguredScenario> scenarios = new LinkedHashMap<String, ConfiguredScenario>(yaml.size());
        for (Map.Entry<String, Object> entry : yaml.entrySet()) {
            String id = entry.getKey();

            // Check for duplicate
            if (scenarios.containsKey(id)) {
                throw OnboardingExceptionCodes.DUPLICATE_SCENARIO_CONFIGURATION.create(id);
            }

            // Check value
            if (false == Map.class.isInstance(entry.getValue())) {
                throw OnboardingExceptionCodes.INVALID_SCENARIO_CONFIGURATION.create(id);
            }

            // Parse value
            Map<String, Object> values = (Map<String, Object>) entry.getValue();
            Boolean enabled = (Boolean) values.get("enabled");
            OnboardingType type = OnboardingType.typeFor((String) values.get("type"));
            List<String> providerIds;
            {
                Object providersValue = values.get("providers");
                if (null == providersValue || false == List.class.isInstance(providersValue)) {
                    throw OnboardingExceptionCodes.INVALID_SCENARIO_CONFIGURATION.create(id);
                }
                providerIds = (List<String>) providersValue;
            }
            List<String> alternativeIds;
            {
                Object alternativesValue = values.get("alternatives");
                if (null == alternativesValue || false == List.class.isInstance(alternativesValue)) {
                    throw OnboardingExceptionCodes.INVALID_SCENARIO_CONFIGURATION.create(id);
                }
                alternativeIds = (List<String>) alternativesValue;
            }
            Icon icon = new DefaultIcon(Base64.decodeBase64((String) values.get("icon")), "image/jpg");
            String displayName = (String) values.get("displayName");
            String description = (String) values.get("description");

            ConfiguredScenario scenario = new ConfiguredScenario(id, enabled.booleanValue(), type, providerIds, alternativeIds, displayName, icon, description);
            scenarios.put(scenario.getId(), scenario);
        }
        return scenarios;
    }

}

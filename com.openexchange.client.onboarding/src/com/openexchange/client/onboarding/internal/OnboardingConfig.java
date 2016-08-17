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

package com.openexchange.client.onboarding.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.codec.binary.Base64;
import com.openexchange.client.onboarding.DefaultIcon;
import com.openexchange.client.onboarding.FontAwesomeIcon;
import com.openexchange.client.onboarding.Icon;
import com.openexchange.client.onboarding.LinkType;
import com.openexchange.client.onboarding.OnboardingExceptionCodes;
import com.openexchange.client.onboarding.OnboardingType;
import com.openexchange.client.onboarding.TemplateIcon;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.ImageTypeDetector;
import com.openexchange.java.Strings;

/**
 * {@link OnboardingConfig} - Initialization class.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class OnboardingConfig {

    private static final String CONFIGFILE_SCENARIOS = "client-onboarding-scenarios.yml";

    /**
     * Gets the name of the YAML configuration file for client on-boarding scenarios
     *
     * @return The file name
     */
    public static String getScenariosConfigFileName() {
        return CONFIGFILE_SCENARIOS;
    }

    // ---------------------------------------------------------------------------------------------------------------------------- //

    /**
     * Initializes a new {@link OnboardingConfig}.
     */
    public OnboardingConfig() {
        super();
    }

    /**
     * Initializes the configured on-boarding scenarios.
     *
     * @param configService The configuration service to use
     * @return The configured scenarios
     * @throws OXException If initializing scenarios fails
     */
    public static Map<String, ConfiguredScenario> parseScenarios(ConfigurationService configService) throws OXException {
        org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OnboardingConfig.class);

        Map<String, ConfiguredScenario> scenarios = null;

        Object yaml = configService.getYaml(CONFIGFILE_SCENARIOS);
        if (null != yaml && Map.class.isInstance(yaml)) {
            Map<String, Object> map = (Map<String, Object>) yaml;
            if (!map.isEmpty()) {
                scenarios = parseScenarios(map);
            }
        }

        if (null == scenarios || scenarios.isEmpty()) {
            logger.warn("No on-boarding scenarios configured in \"{}\".", CONFIGFILE_SCENARIOS);
            return Collections.emptyMap();
        }
        return scenarios;
    }

    private static final Pattern PATTERN_FILENAME = Pattern.compile("^[\\w,\\s-]+\\.[A-Za-z]{2,4}$");

    private static Map<String, ConfiguredScenario> parseScenarios(Map<String, Object> yaml) throws OXException {
        Map<String, ConfiguredScenario> scenarios = new LinkedHashMap<String, ConfiguredScenario>(yaml.size());
        for (Map.Entry<String, Object> entry : yaml.entrySet()) {
            String id = entry.getKey();

            // Check for duplicate
            if (scenarios.containsKey(id)) {
                throw OnboardingExceptionCodes.DUPLICATE_SCENARIO_CONFIGURATION.create(id);
            }

            // Check values map
            if (false == Map.class.isInstance(entry.getValue())) {
                throw OnboardingExceptionCodes.INVALID_SCENARIO_CONFIGURATION.create(id);
            }

            // Parse values map
            Map<String, Object> values = (Map<String, Object>) entry.getValue();

            // Enabled flag
            Boolean enabled = (Boolean) values.get("enabled");

            // The scenario's type
            OnboardingType type;
            {
                String sType = (String) values.get("type");
                type = OnboardingType.typeFor(sType);
                if (null == type) {
                    throw OnboardingExceptionCodes.NO_SUCH_TYPE.create(sType, id);
                }
            }

            // Check for special "link" scenario
            ConfiguredLink link = null;
            if (OnboardingType.LINK == type) {
                // The link
                Object linkObject = values.get("link");
                if (null != linkObject) {
                    if (false == Map.class.isInstance(linkObject)) {
                        throw OnboardingExceptionCodes.INVALID_SCENARIO_CONFIGURATION.create(id);
                    }
                    Map<String, Object> linkValues = (Map<String, Object>) linkObject;
                    LinkType linkType = LinkType.COMMON;
                    {
                        String sType = (String) linkValues.get("type");
                        if (!Strings.isEmpty(sType)) {
                            linkType = LinkType.typeFor(sType);
                            if (null == linkType) {
                                throw OnboardingExceptionCodes.INVALID_LINK_TYPE_IN_SCENARIO_CONFIGURATION.create(sType, id);
                            }
                        }
                    }
                    String sUrl = (String) linkValues.get("url");
                    if (!Strings.isEmpty(sUrl)) {
                        try {
                            URI uri = new URI(sUrl);
                            if ("property".equalsIgnoreCase(uri.getScheme())) {
                                // E.g. "property://com.openexchange.client.onboarding.app.link"
                                link = new ConfiguredLink(uri.getAuthority(), true, linkType);
                            } else {
                                // E.g. "https://itunes.apple.com/us/app/keynote/id361285480?mt=8"
                                link = new ConfiguredLink(sUrl, false, linkType);
                            }
                        } catch (URISyntaxException e) {
                            throw OnboardingExceptionCodes.INVALID_SCENARIO_CONFIGURATION.create(e, id);
                        }
                    }
                }
            }

            // Associated static capabilities
            List<String> capabilities = Collections.emptyList();
            if (OnboardingType.LINK == type) {
                String sCapabilities = (String) values.get("capabilities");
                if (!Strings.isEmpty(sCapabilities) && !"null".equalsIgnoreCase(sCapabilities)) {
                    String[] saCpabilities = Strings.splitByComma(sCapabilities);
                    if (null != saCpabilities && saCpabilities.length > 0) {
                        capabilities = Arrays.asList(saCpabilities);
                    }
                }
            }

            // Associated providers
            List<String> providerIds;
            {
                Object providersValue = values.get("providers");
                if (null == providersValue || false == List.class.isInstance(providersValue)) {
                    throw OnboardingExceptionCodes.INVALID_SCENARIO_CONFIGURATION.create(id);
                }
                providerIds = (List<String>) providersValue;
            }

            // Optional alternative scenarios
            List<String> alternativeIds;
            {
                Object alternativesValue = values.get("alternatives");
                if (null == alternativesValue || false == List.class.isInstance(alternativesValue)) {
                    throw OnboardingExceptionCodes.INVALID_SCENARIO_CONFIGURATION.create(id);
                }
                alternativeIds = (List<String>) alternativesValue;
            }

            // Read icon name
            Icon icon;
            {
                String iconValue = (String) values.get("icon");
                if (Strings.isEmpty(iconValue) || "null".equalsIgnoreCase(iconValue)) {
                    icon = null;
                } else {
                    iconValue = iconValue.trim();
                    if (Strings.asciiLowerCase(iconValue).startsWith("fa-")) {
                        // Assume Font-Awesome names
                        String[] names = Strings.splitByComma(iconValue);
                        icon = new FontAwesomeIcon(names);
                    } else if (PATTERN_FILENAME.matcher(iconValue).matches()) {
                        // Assume a file name
                        icon = new TemplateIcon(iconValue);
                    } else {
                        // Assume base64-encoded image
                        byte[] bytes = Base64.decodeBase64(iconValue);
                        String mimeType = ImageTypeDetector.getMimeType(bytes, null);
                        icon = new DefaultIcon(bytes, null == mimeType ? "image/jpg" : mimeType);
                    }
                }
            }

            // Read i18n strings
            String displayName = (String) values.get("displayName_t10e");
            String description = (String) values.get("description_t10e");

            scenarios.put(id, new ConfiguredScenario(id, enabled.booleanValue(), type, link, providerIds, alternativeIds, displayName, icon, description, capabilities));
        }
        return scenarios;
    }

}

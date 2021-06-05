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

package com.openexchange.config.mbean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.management.MBeanException;
import javax.management.NotCompliantMBeanException;
import javax.management.StandardMBean;
import org.slf4j.Logger;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.internal.ConfigurationImpl;

/**
 * {@link ConfigReloadMBeanImpl}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since 7.6.0
 */
public class ConfigReloadMBeanImpl extends StandardMBean implements ConfigReloadMBean {

    private final ConfigurationImpl configService;

    /**
     * Initializes a new {@link ConfigReloadMBeanImpl}.
     *
     * @param mbeanInterface
     * @throws NotCompliantMBeanException
     */
    public ConfigReloadMBeanImpl(Class<? extends ConfigReloadMBean> mbeanInterface, ConfigurationImpl configService) throws NotCompliantMBeanException {
        super(mbeanInterface);
        this.configService = configService;
    }

    @Override
    public void reloadConfiguration() throws MBeanException {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigReloadMBeanImpl.class);
        try {
            final ConfigurationImpl configService = this.configService;
            if (null != configService) {
                configService.reloadConfiguration();
            }
        } catch (Exception e) {
            logger.error("", e);
            final String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }

    @Override
    public Map<String, List<String>> listReloadables() throws MBeanException {
        final Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigReloadMBeanImpl.class);
        try {
            final ConfigurationImpl configService = this.configService;
            if (null == configService) {
                return Collections.emptyMap();
            }

            Map<String, List<String>> files = new TreeMap<String, List<String>>();
            Set<String> properties = null;

            for (Iterator<Reloadable> i = configService.getReloadables().iterator(); i.hasNext();) {
                Interests interests = i.next().getInterests();

                if (null != interests) {
                    String[] configFileNames = interests.getConfigFileNames();
                    if (null != configFileNames && configFileNames.length > 0) {
                        for (String configFileName : configFileNames) {
                            if (false == files.containsKey(configFileName)) {
                                files.put(configFileName, Arrays.asList("All properties in that file"));
                            }
                        }
                    }

                    String[] propertiesOfInterest = interests.getPropertiesOfInterest();
                    if (null != propertiesOfInterest && propertiesOfInterest.length > 0) {
                        if (null == properties) {
                            properties = new TreeSet<>();
                        }
                        properties.addAll(Arrays.asList(propertiesOfInterest));
                    }
                }
            }

            Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
            map.putAll(files);

            if (null != properties) {
                properties.remove("*");
                map.put("properties", new ArrayList<>(properties));
            }

            return map;
        } catch (Exception e) {
            logger.error("", e);
            final String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }
}

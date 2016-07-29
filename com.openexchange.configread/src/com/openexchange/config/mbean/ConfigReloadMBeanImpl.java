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
        } catch (final Exception e) {
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
        } catch (final Exception e) {
            logger.error("", e);
            final String message = e.getMessage();
            throw new MBeanException(new Exception(message), message);
        }
    }
}

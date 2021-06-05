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

package com.openexchange.serverconfig.impl.values;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import com.google.common.collect.ImmutableList;
import com.openexchange.config.ConfigurationService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ComputedServerConfigValueService;
import com.openexchange.session.Session;

/**
 * {@link Languages}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class Languages implements ComputedServerConfigValueService {

    private final List<SimpleEntry<String, String>> languages;

    /**
     * Initializes a new {@link Languages}.
     *
     * @param services The service look-up
     */
    public Languages(ServiceLookup services) {
        super();

        ConfigurationService config = services.getService(ConfigurationService.class);
        Properties properties = config.getPropertiesInFolder("languages" + File.separatorChar + "appsuite");

        List<SimpleEntry<String, String>> languages = new ArrayList<SimpleEntry<String,String>>(properties.size());
        for (Object key : properties.keySet()) {
            String propName = (String) key;
            String languageName = properties.getProperty(propName);

            int index = propName.lastIndexOf('/');
            if (index > 0) {
                propName = propName.substring(index + 1);
            }
            languages.add(new SimpleEntry<String, String>(propName, languageName));
        }

        if (languages.isEmpty()) {
            // Assume american english
            languages.add(new SimpleEntry<String, String>("en_US", "English"));
        }

        // Sort it alphabetically
        Collections.sort(languages, new Comparator<SimpleEntry<String, String>>() {

            @Override
            public int compare(SimpleEntry<String, String> arg0, SimpleEntry<String, String> arg1) {
                String language1 = arg0.getValue();
                if (null == language1) {
                    return null == arg1.getValue() ? 0 : 1;
                }

                String language2 = arg1.getValue();
                if (null == language2) {
                    return -1;
                }
                return language1.compareToIgnoreCase(language2);
            }
        });

        this.languages = ImmutableList.copyOf(languages);
    }

    @Override
    public void addValue(Map<String, Object> serverConfig, String hostName, int userId, int contextId, Session optSession) {

        Object existingLanguages = serverConfig.get("languages");
        if (existingLanguages == null || existingLanguages.equals("all")) {
            serverConfig.put("languages", languages);
        }
    }

}

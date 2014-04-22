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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.apps.manifests.json.values;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.apps.manifests.ComputedServerConfigValueService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Languages}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Languages implements ComputedServerConfigValueService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Languages.class);

    private final JSONArray allLanguages;

    /**
     * Initializes a new {@link Languages}.
     *
     * @param services The service look-up
     */
    public Languages(ServiceLookup services) {
        super();

        ConfigurationService config = services.getService(ConfigurationService.class);
        Properties properties = config.getPropertiesInFolder("languages/appsuite");

        List<SimpleEntry<String, String>> languages = new ArrayList<SimpleEntry<String,String>>();
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
                String language2 = arg1.getValue();
                if (null == language1) {
                    return null == language2 ? 0 : 1;
                }
                if (null == language2) {
                    return -1;
                }
                return language1.compareToIgnoreCase(language2);
            }
        });

        final JSONArray allLanguages = new JSONArray(languages.size());
        for (SimpleEntry<String, String> language : languages) {
            allLanguages.put(new JSONArray(2).put(language.getKey()).put(language.getValue()));
        }
        this.allLanguages = allLanguages;
    }

    @Override
    public void addValue(JSONObject serverConfig, AJAXRequestData request, ServerSession session) throws OXException, JSONException {

        Object languages = serverConfig.opt("languages");
        if (languages == null || languages.equals("all")) {
            serverConfig.put("languages", allLanguages);
        }
    }

}

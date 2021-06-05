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

package com.openexchange.groupware.settings.tree.modules.mail;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.html.HtmlService;
import com.openexchange.html.whitelist.Attribute;
import com.openexchange.html.whitelist.Element;
import com.openexchange.html.whitelist.Tag;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link Whitelist}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class Whitelist implements PreferencesItemService, ConfigTreeEquivalent {

    /**
     * Initializes a new {@link Whitelist}.
     */
    public Whitelist() {
        super();
    }

    @Override
    public String[] getPath() {
        return new String[] { "modules", "mail", "whitelist" };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {

            @Override
            public boolean isAvailable(UserConfiguration userConfig) {
                return true;
            }

            @Override
            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
                HtmlService htmlService = ServerServiceRegistry.getInstance().getService(HtmlService.class);
                if (null == htmlService) {
                    setting.setSingleValue(null);
                    return;
                }

                try {
                    JSONObject jWhitelist;

                    boolean exact = false;
                    if (exact) {
                        com.openexchange.html.whitelist.Whitelist whitelist = htmlService.getWhitelist(true);
                        jWhitelist = new JSONObject(4);

                        {
                            Map<Tag, Map<Attribute, Set<String>>> htmlWhitelist = whitelist.getHtmlWhitelist();
                            JSONObject jHtmlWhitelist = new JSONObject(htmlWhitelist.size());
                            for (Map.Entry<Tag, Map<Attribute, Set<String>>> e : htmlWhitelist.entrySet()) {
                                Map<Attribute, Set<String>> attributes = e.getValue();
                                if (null == attributes) {
                                    jHtmlWhitelist.put(e.getKey().getName(), JSONObject.NULL);
                                } else {
                                    JSONObject jAttributes = new JSONObject(attributes.size());
                                    for (Map.Entry<Attribute, Set<String>> a : attributes.entrySet()) {
                                        Set<String> values = a.getValue();
                                        if (null == values) {
                                            jAttributes.put(a.getKey().getName(), JSONObject.NULL);
                                        } else {
                                            jAttributes.put(a.getKey().getName(), new JSONArray(values));
                                        }
                                    }
                                    jHtmlWhitelist.put(e.getKey().getName(), jAttributes);
                                }
                            }
                            jWhitelist.put("html", jHtmlWhitelist);
                        }

                        {
                            Map<Element, Set<String>> styleWhitelist = whitelist.getStyleWhitelist();
                            JSONObject jStyleWhitelist = new JSONObject(styleWhitelist.size());
                            for (Map.Entry<Element, Set<String>> e : styleWhitelist.entrySet()) {
                                Set<String> values = e.getValue();
                                if (null == values) {
                                    jStyleWhitelist.put(e.getKey().getName(), JSONObject.NULL);
                                } else {
                                    jStyleWhitelist.put(e.getKey().getName(), new JSONArray(values));
                                }
                            }
                            jWhitelist.put("css", jStyleWhitelist);
                        }
                    } else {
                        com.openexchange.html.whitelist.Whitelist whitelist = htmlService.getWhitelist(true);
                        jWhitelist = new JSONObject(4);

                        Set<String> sortedTags = new TreeSet<>();
                        Set<String> sortedAttributes = new TreeSet<>();

                        Map<Tag, Map<Attribute, Set<String>>> htmlWhitelist = whitelist.getHtmlWhitelist();
                        for (Map.Entry<Tag, Map<Attribute, Set<String>>> e : htmlWhitelist.entrySet()) {
                            sortedTags.add(e.getKey().getName());
                            Map<Attribute, Set<String>> attributes = e.getValue();
                            if (null != attributes) {
                                for (Attribute a : attributes.keySet()) {
                                    sortedAttributes.add(a.getName());
                                }
                            }
                        }

                        jWhitelist.put("allowedTags", new JSONArray(sortedTags));
                        jWhitelist.put("allowedAttributes", new JSONArray(sortedAttributes));
                    }

                    setting.setSingleValue(jWhitelist);
                } catch (Exception e) {
                    // White-list cannot be populated
                    org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Whitelist.class);
                    logger.warn("HTML/CSS white-list cannot be populated", e);
                    setting.setSingleValue(null);
                }
            }
        };
    }

    @Override
    public String getConfigTreePath() {
        return "modules/mail/whitelist";
    }

    @Override
    public String getJslobPath() {
        return "io.ox/mail//whitelist";
    }

}

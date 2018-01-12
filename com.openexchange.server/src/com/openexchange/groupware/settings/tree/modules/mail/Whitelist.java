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

package com.openexchange.groupware.settings.tree.modules.mail;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
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

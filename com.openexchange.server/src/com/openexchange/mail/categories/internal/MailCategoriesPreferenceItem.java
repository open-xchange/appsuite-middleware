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

package com.openexchange.mail.categories.internal;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.mail.categories.MailCategoriesConfigService;
import com.openexchange.mail.categories.MailCategoryConfig;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link MailCategoriesPreferenceItem}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.2
 */
public class MailCategoriesPreferenceItem implements PreferencesItemService {

    private final ServiceLookup lookupService;

    /**
     * Initializes a new {@link MailCategoriesPreferenceItem}.
     */
    public MailCategoriesPreferenceItem(ServiceLookup lookupService) {
        super();
        this.lookupService = lookupService;
    }

    @Override
    public String[] getPath() {
        return new String[] { "modules", "mail", "categories" };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {

            @Override
            public boolean isAvailable(UserConfiguration userConfig) {
                return userConfig.hasWebMail();
            }

            @Override
            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
                JSONObject item = new JSONObject(3);
                try {
                    MailCategoriesConfigService service = lookupService.getOptionalService(MailCategoriesConfigService.class);
                    if (service != null) {
                        boolean mailCategoriesEnabled = service.isEnabled(session);
                        item.put("tabbed_inbox", mailCategoriesEnabled);
                        if (mailCategoriesEnabled) {
                            boolean mailUserCategoriesEnabled = service.isAllowedToCreateUserCategories(session);
                            item.put("user_can_create_categories", mailUserCategoriesEnabled);
                            List<MailCategoryConfig> configs = service.getAllCategories(session, false);
                            JSONArray categories = new JSONArray();
                            for (MailCategoryConfig config : configs) {
                                JSONObject categoryJSON = new JSONObject(3);
                                categoryJSON.put("category", config.getCategory());
                                String name = config.getNames().containsKey(user.getLocale()) ? config.getNames().get(user.getLocale()) : config.getName();
                                categoryJSON.put("name", name);
                                categoryJSON.put("active", config.isActive());
                                categories.put(categoryJSON);
                            }
                            item.put("inbox_tabs", categories);
                        }
                    } else {
                        item.put("tabbed_inbox", false);
                    }
                    setting.setSingleValue(item);
                } catch (JSONException e) {
                    throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create();
                }
            }
        };
    }

}

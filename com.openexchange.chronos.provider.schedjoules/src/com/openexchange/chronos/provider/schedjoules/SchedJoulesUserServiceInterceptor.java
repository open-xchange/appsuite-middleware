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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.provider.schedjoules;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.account.AdministrativeCalendarAccountService;
import com.openexchange.chronos.provider.caching.CachingCalendarUtils;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.AbstractUserServiceInterceptor;

/**
 * {@link SchedJoulesUserServiceInterceptor}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class SchedJoulesUserServiceInterceptor extends AbstractUserServiceInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedJoulesUserServiceInterceptor.class);

    // Disabled for now
    private static final boolean ENABLED = false;

    /**
     * The URL parameter name that defines the language of the SchedJoules calendar
     */
    private static final String LOCALE_PARAMETER = "l";

    private ServiceLookup services;

    /**
     * Initialises a new {@link SchedJoulesUserServiceInterceptor}.
     *
     * @param services The {@link ServiceLookup} instance
     */
    public SchedJoulesUserServiceInterceptor(ServiceLookup services) {
        super();
        this.services = services;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.user.AbstractUserServiceInterceptor#afterUpdate(com.openexchange.groupware.contexts.Context, com.openexchange.groupware.ldap.User, com.openexchange.groupware.container.Contact, java.util.Map)
     */
    @Override
    public void afterUpdate(Context context, User user, Contact contactData, Map<String, Object> properties) throws OXException {
        if (null == user || null == user.getLocale()) {
            return;
        }
        if (!ENABLED) {
            return;
        }
        String language = user.getLocale().getLanguage();
        AdministrativeCalendarAccountService service = services.getService(AdministrativeCalendarAccountService.class);
        List<CalendarAccount> accounts = service.getAccounts(context.getContextId(), new int[] { user.getId() }, BasicSchedJoulesCalendarProvider.PROVIDER_ID);
        for (CalendarAccount account : accounts) {
            JSONObject internalConfig = account.getInternalConfiguration();
            JSONArray folders = internalConfig.optJSONArray(SchedJoulesFields.FOLDERS);
            if (folders == null) {
                continue;
            }
            for (int index = 0; index < folders.length(); index++) {
                try {
                    JSONObject folder = folders.getJSONObject(index);
                    String localeStr = folder.optString(SchedJoulesFields.LOCALE, "");
                    if (language.equals(new Locale(localeStr).getLanguage())) {
                        continue;
                    }
                    String url = folder.getString(SchedJoulesFields.URL);
                    folder.put(SchedJoulesFields.URL, replaceLocale(url, language));
                    folder.put(SchedJoulesFields.LOCALE, language);

                    CachingCalendarUtils.invalidateCache(account, 0);
                } catch (JSONException e) {
                    LOGGER.warn("Invalid/Malformed configuration detected in SchedJoules account '{}' for user '{}' in context '{}'", account.getAccountId(), user.getId(), context.getContextId(), e);
                }
            }
            service.updateAccount(context.getContextId(), user.getId(), account.getAccountId(), account.getInternalConfiguration(), account.getUserConfiguration(), account.getLastModified().getTime());
        }
        super.afterUpdate(context, user, contactData, properties);
    }

    /**
     * Replaces the locale in the specified URL string
     *
     * @param url The URL string
     * @param languageLocale The language locale to set
     * @return The new URL
     */
    private String replaceLocale(String url, String languageLocale) {
        StringBuilder urlBuilder = new StringBuilder();

        int indexOf = url.indexOf(LOCALE_PARAMETER + "=");
        if (indexOf < 0) {
            // The SchedJoules calendar URL always has at least one parameter, i.e. the 'x', thus the '?' is implied.
            return urlBuilder.append(url).append("&").append(LOCALE_PARAMETER).append("=").append(languageLocale).toString();
        }

        String preUrl = url.substring(0, indexOf);
        String postUrl = url.substring(indexOf + 4);
        return urlBuilder.append(preUrl).append(LOCALE_PARAMETER).append("=").append(languageLocale).append(postUrl).toString();
    }
}

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

package com.openexchange.chronos.provider.schedjoules;

import static com.openexchange.java.Autoboxing.I;
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
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.User;
import com.openexchange.user.interceptor.AbstractUserServiceInterceptor;

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

    private final ServiceLookup services;

    /**
     * Initialises a new {@link SchedJoulesUserServiceInterceptor}.
     *
     * @param services The {@link ServiceLookup} instance
     */
    public SchedJoulesUserServiceInterceptor(ServiceLookup services) {
        super();
        this.services = services;
    }

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
                    LOGGER.warn("Invalid/Malformed configuration detected in SchedJoules account '{}' for user '{}' in context '{}'", I(account.getAccountId()), I(user.getId()), I(context.getContextId()), e);
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

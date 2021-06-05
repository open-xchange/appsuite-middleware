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

package com.openexchange.groupware.settings.tree;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nServiceRegistry;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.user.User;

/**
 * {@link AvailableTimezones}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class AvailableTimeZones implements PreferencesItemService {

    /** The logger */
    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AvailableTimeZones.class);

    private static final String NAME = "availableTimeZones";

    public AvailableTimeZones() {
        super();
    }

    @Override
    public String[] getPath() {
        return new String[] { NAME };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {

            private final Set<String> SPECIAL = Collections.<String> unmodifiableSet(new HashSet<String>(Arrays.asList("UTC", "GMT")));

            @Override
            public boolean isAvailable(final UserConfiguration userConfig) {
                return true;
            }

            @Override
            public void getValue(final Session session, final Context ctx, final User user, final UserConfiguration userConfig, final Setting setting) throws OXException {
                try {
                    final JSONObject json = new JSONObject();
                    final I18nService i18nService = ServerServiceRegistry.getServize(I18nServiceRegistry.class, true).getI18nService(user.getLocale());
                    Object[][] timezones = new Object[TimeZone.getAvailableIDs().length][3];

                    int i = 0;
                    long now = System.currentTimeMillis();
                    for (final String timeZoneID : TimeZone.getAvailableIDs()) {
                        final int len = timeZoneID.length();
                        if (len >= 4 || SPECIAL.contains(Strings.toUpperCase(timeZoneID))) {
                            if (timeZoneID.startsWith("Etc")) {
                                /*
                                 * The special area of "Etc" is used for some administrative zones, particularly for "Etc/UTC" which
                                 * represents Coordinated Universal Time. In order to conform with the POSIX style, those zone names
                                 * beginning with "Etc/GMT" have their sign reversed from what most people expect. Therefore discard them to
                                 * avoid further confusion
                                 */
                                LOGGER.debug("Ignoring time zone {}", timeZoneID);
                            } else {
                                int offset = TimeZone.getTimeZone(timeZoneID).getOffset(now);
                                timezones[i][0] = Integer.valueOf(offset);
                                timezones[i][1] = timeZoneID;
                                timezones[i][2] = prefix(offset, i18nService.getLocalized(timeZoneID.replace('_', ' ')));
                            }
                        }
                        i++;
                    }

                    for(i = 0; i < timezones.length; i++) {
                        Object[] entry = timezones[i];
                        if (entry == null || entry[1] == null || entry[2] == null) {
                            continue;
                        }
                        json.put((String) entry[1], (String) entry[2]);
                    }

                    setting.setSingleValue(json);

                } catch (JSONException e) {
                    throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
                }
            }

            private String prefix(int offset, String translate) {
                int seconds = offset / 1000;

                int hours = seconds / 3600;
                int extraMinutes = (seconds % 3600) / 60;
                if (offset > 0) {
                    return String.format("(GMT+%02d:%02d) %s", Integer.valueOf(hours), Integer.valueOf(extraMinutes), translate);
                }
                return String.format("(GMT-%02d:%02d) %s", Integer.valueOf(Math.abs(hours)), Integer.valueOf(Math.abs(extraMinutes)), translate);
            }
        }; // End of ReadOnlyValue implementation
    }

}

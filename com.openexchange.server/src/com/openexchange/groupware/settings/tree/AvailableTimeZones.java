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
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Strings;
import com.openexchange.server.services.I18nServices;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

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
                    final I18nServices i18nServices = I18nServices.getInstance();
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
                                timezones[i][2] = prefix(offset, i18nServices.translate(user.getLocale(), timeZoneID.replace('_', ' '), false));
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

                } catch (final JSONException e) {
                    throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
                }
            }

            private String prefix(int offset, String translate) {
                int seconds = offset / 1000;

                int hours = seconds / 3600;
                int extraMinutes = (seconds % 3600) / 60;
                if (offset > 0) {
                    return String.format("(GMT+%02d:%02d) %s", hours, extraMinutes, translate);
                } else {
                    return String.format("(GMT-%02d:%02d) %s", Math.abs(hours), Math.abs(extraMinutes), translate);
                }
            }
        }; // End of ReadOnlyValue implementation
    }

}

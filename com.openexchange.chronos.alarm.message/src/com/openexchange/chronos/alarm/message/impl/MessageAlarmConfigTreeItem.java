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

package com.openexchange.chronos.alarm.message.impl;

import java.util.Map.Entry;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.alarm.message.AlarmNotificationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.PreferencesItemService;
import com.openexchange.groupware.settings.ReadOnlyValue;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.jslob.ConfigTreeEquivalent;
import com.openexchange.session.Session;
import com.openexchange.user.User;

/**
 * {@link MessageAlarmConfigTreeItem}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class MessageAlarmConfigTreeItem implements PreferencesItemService, ConfigTreeEquivalent {

    final AlarmNotificationServiceRegistry registry;

    /**
     * Initializes a new {@link MessageAlarmConfigTreeItem}.
     */
    public MessageAlarmConfigTreeItem(AlarmNotificationServiceRegistry registry) {
        super();
        this.registry = registry;

    }

    @Override
    public String[] getPath() {
        return new String[] { "modules", "calendar", "availableAlarmTypes" };
    }

    @Override
    public IValueHandler getSharedValue() {
        return new ReadOnlyValue() {

            @Override
            public boolean isAvailable(UserConfiguration userConfig) {
                return userConfig.hasCalendar();
            }

            @Override
            public void getValue(Session session, Context ctx, User user, UserConfiguration userConfig, Setting setting) throws OXException {
                setting.addMultiValue(AlarmAction.DISPLAY.getValue());
                setting.addMultiValue(AlarmAction.AUDIO.getValue());
                for (Entry<AlarmAction, AlarmNotificationService> entry : registry.services.entrySet()) {
                    if (entry.getValue().isEnabled(user.getId(), ctx.getContextId())) {
                        setting.addMultiValue(entry.getKey().getValue());
                    }
                }
            }
        };
    }

    @Override
    public String getConfigTreePath() {
        return "modules/calendar/availableAlarmTypes";
    }

    @Override
    public String getJslobPath() {
        return "io.ox/calendar//availableAlarmTypes";
    }

}

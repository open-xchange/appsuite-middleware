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

package com.openexchange.groupware.reminder.json;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.reminder.json.actions.AbstractReminderAction;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthModule;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ReminderActionFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@OAuthModule
public class ReminderActionFactory implements AJAXActionServiceFactory {

    @Deprecated
    public static final String OAUTH_READ_SCOPE = "read_reminders";

    @Deprecated
    public static final String OAUTH_WRITE_SCOPE = "write_reminders";

    private final Map<String, AbstractReminderAction> actions;

    /**
     * Initializes a new {@link ReminderActionFactory}.
     *
     * @param services The service look-up
     */
    public ReminderActionFactory(final ServiceLookup services) {
        super();
        actions = new ConcurrentHashMap<String, AbstractReminderAction>(4, 0.9f, 1);
        actions.put("delete", new com.openexchange.groupware.reminder.json.actions.DeleteAction(services));
        actions.put("updates", new com.openexchange.groupware.reminder.json.actions.UpdatesAction(services));
        actions.put("range", new com.openexchange.groupware.reminder.json.actions.RangeAction(services));
        actions.put("remindAgain", new com.openexchange.groupware.reminder.json.actions.RemindAgainAction(services));
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        return actions.get(action);
    }

}

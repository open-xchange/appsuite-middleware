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

package com.openexchange.groupware.reminder.json.actions;

import static com.openexchange.tools.TimeZoneUtils.getTimeZone;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.ReminderParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.ReminderWriter;
import com.openexchange.api2.ReminderService;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.reminder.ReminderExceptionCode;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.json.ReminderAJAXRequest;
import com.openexchange.groupware.reminder.json.ReminderActionFactory;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link RemindAgainAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "remindAgain", description = "Remind again (since v6.18.1).", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "id", description = "The ID of the reminder whose date shall be changed.")
}, requestBody = "The JSON representation of the reminder; mainly containing the field \u201calarm\u201d which provides the new reminder date. E.g. { \"alarm\": 1283418027381 }",
responseDescription = "The JSON representation of the updated reminder.")
@OAuthAction(ReminderActionFactory.OAUTH_WRITE_SCOPE)
public final class RemindAgainAction extends AbstractReminderAction {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(RemindAgainAction.class);

    /**
     * Initializes a new {@link RemindAgainAction}.
     * @param services
     */
    public RemindAgainAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ReminderAJAXRequest req) throws OXException, JSONException {
        // timestamp = DataParser.checkDate(jsonObject, AJAXServlet.PARAMETER_TIMESTAMP);
        final int reminderId = req.checkInt(AJAXServlet.PARAMETER_ID);
        final TimeZone tz = req.getTimeZone();
        final TimeZone timeZone;
        {
            final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? tz : getTimeZone(timeZoneId);
        }
        /*
         * Parse reminder from JSON
         */
        final JSONObject jreminder = req.getData();
        final ReminderObject reminder = new ReminderObject();
        new ReminderParser(tz).parse(reminder, jreminder);
        if (null == reminder.getDate()) {
            throw ReminderExceptionCode.MANDATORY_FIELD_ALARM.create();
        }
        reminder.setObjectId(reminderId);
        /*
         * Load storage version and check permission
         */
        final ServerSession session = req.getSession();
        final ReminderService reminderSql = new ReminderHandler(session.getContext());
        {
            final ReminderObject storageReminder = reminderSql.loadReminder(reminder.getObjectId());
            /*
             * Check module permission
             */
            if (!hasModulePermission(storageReminder, session)) {
                throw ReminderExceptionCode.UNEXPECTED_ERROR.create("No module permission.");
            }
            /*
             * Set other fields
             */
            reminder.setModule(storageReminder.getModule());
            reminder.setDescription(storageReminder.getDescription());
            reminder.setFolder(storageReminder.getFolder());
            reminder.setTargetId(storageReminder.getTargetId());
            reminder.setUser(storageReminder.getUser());
        }
        /*
         * Trigger action
         */
        reminderSql.remindAgain(reminder, session, session.getContext());
        final Date timestamp = reminder.getLastModified();
        /*
         * Write updated reminder
         */
        final ReminderWriter reminderWriter = new ReminderWriter(timeZone);
        final JSONObject jsonReminderObj = new JSONObject();
        reminderWriter.writeObject(reminder, jsonReminderObj);
        return new AJAXRequestResult(jsonReminderObj, timestamp, "json");
    }

}

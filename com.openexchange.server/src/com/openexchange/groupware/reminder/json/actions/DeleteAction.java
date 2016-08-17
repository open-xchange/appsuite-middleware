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

import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
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


/**
 * {@link DeleteAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "delete", description = "Delete Reminders", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module.")
}, requestBody = "An object with the field \"id\" or an array of objects with the field \"id\".",
responseDescription = "An JSON array with the ids that was not deleted.")
@OAuthAction(ReminderActionFactory.OAUTH_WRITE_SCOPE)
public final class DeleteAction extends AbstractReminderAction {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(DeleteAction.class);

    /**
     * Initializes a new {@link DeleteAction}.
     * @param services
     */
    public DeleteAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ReminderAJAXRequest req) throws OXException, JSONException {
        final JSONArray response = new JSONArray();
        if (req.getData() instanceof JSONObject) {
            final JSONObject jData = req.getData();
            final int id = DataParser.checkInt(jData, AJAXServlet.PARAMETER_ID);
            final TimeZone tz = req.getTimeZone();
            try {
                final ReminderService reminderSql = new ReminderHandler(req.getSession().getContext());
                final ReminderObject reminder = reminderSql.loadReminder(id);

                if (reminder.isRecurrenceAppointment()) {
                    final ReminderObject nextReminder = getNextRecurringReminder(req.getSession(), tz, reminder);
                    if (nextReminder != null) {
                        reminderSql.updateReminder(nextReminder);
                        response.put(nextReminder.getObjectId());
                    } else {
                        reminderSql.deleteReminder(reminder);
                    }
                } else {
                    reminderSql.deleteReminder(reminder);
                }
            } catch (final OXException oxe) {
                LOG.debug("", oxe);
                if (ReminderExceptionCode.NOT_FOUND.equals(oxe)) {
                    response.put(id);
                    return new AJAXRequestResult(response, "json");
                }
                throw oxe;
            }
        } else {
            JSONArray jsonArray = req.getData();
            for (int i = 0; i < jsonArray.length(); i++) {
                final JSONObject jData = jsonArray.getJSONObject(i);
                final int id = DataParser.checkInt(jData, AJAXServlet.PARAMETER_ID);
                final TimeZone tz = req.getTimeZone();
                try {
                    final ReminderService reminderSql = new ReminderHandler(req.getSession().getContext());
                    final ReminderObject reminder = reminderSql.loadReminder(id);

                    if (reminder.isRecurrenceAppointment()) {
                        final ReminderObject nextReminder = getNextRecurringReminder(req.getSession(), tz, reminder);
                        if (nextReminder != null) {
                            reminderSql.updateReminder(nextReminder);
                            response.put(nextReminder.getObjectId());
                        } else {
                            reminderSql.deleteReminder(reminder);
                        }
                    } else {
                        reminderSql.deleteReminder(reminder);
                    }
                } catch (final OXException oxe) {
                    LOG.debug("", oxe);
                    if (ReminderExceptionCode.NOT_FOUND.equals(oxe)) {
                        response.put(id);
                        return new AJAXRequestResult(response, "json");
                    }
                    throw oxe;
                }
            }
        }
        return new AJAXRequestResult(response, "json");
    }

}

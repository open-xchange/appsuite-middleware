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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.writer.ReminderWriter;
import com.openexchange.api2.ReminderService;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.reminder.ReminderHandler;
import com.openexchange.groupware.reminder.ReminderObject;
import com.openexchange.groupware.reminder.json.ReminderAJAXRequest;
import com.openexchange.groupware.reminder.json.ReminderActionFactory;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link RangeAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "range", description = "Get reminder range.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "end", description = "The End date of the reminder range.")
}, responseDescription = "An Array with all reminders which are scheduled until the specified time. Each reminder is described in Reminder response.")
@OAuthAction(ReminderActionFactory.OAUTH_READ_SCOPE)
public final class RangeAction extends AbstractReminderAction {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(RangeAction.class);

    /**
     * Initializes a new {@link RangeAction}.
     * @param services
     */
    public RangeAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final ReminderAJAXRequest req) throws OXException, JSONException {
        final Date end = req.checkDate(AJAXServlet.PARAMETER_END);
        final TimeZone tz = req.getTimeZone();
        final TimeZone timeZone;
        {
            final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            timeZone = null == timeZoneId ? tz : getTimeZone(timeZoneId);
        }

        final ReminderWriter reminderWriter = new ReminderWriter(timeZone);
        try {
            final ServerSession session = req.getSession();
            final ReminderService reminderSql = new ReminderHandler(session.getContext());
            final User user = session.getUser();
            final SearchIterator<ReminderObject> it = reminderSql.getArisingReminder(session, session.getContext(), user, end);
            final JSONArray jsonResponseArray = new JSONArray();
            try {
                while (it.hasNext()) {
                    final ReminderObject reminder = it.next();
                    if (reminder.isRecurrenceAppointment()) {
                        try {
                            if (!getLatestRecurringReminder(session, tz, end, reminder)) {
                                final ReminderObject nextReminder = getNextRecurringReminder(session, tz, reminder);
                                if (nextReminder != null) {
                                    reminderSql.updateReminder(nextReminder);
                                } else {
                                    reminderSql.deleteReminder(reminder);
                                }
                                continue;
                            }
                        } catch (final OXException e) {
                            if (e.isGeneric(Generic.NOT_FOUND)) {
                                LOG.warn("Cannot load target object of this reminder.", e);
                                deleteReminderSafe(reminder, user.getId(), reminderSql);
                            } else {
                                LOG.error("Can not calculate recurrence of appointment {}{}{}", reminder.getTargetId(), ':', session.getContextId(), e);
                            }
                        }
                    }
                    try {
                        if (hasModulePermission(reminder, session) && stillAccepted(reminder, session)) {
                            final JSONObject jsonReminderObj = new JSONObject(12);
                            reminderWriter.writeObject(reminder, jsonReminderObj);
                            jsonResponseArray.put(jsonReminderObj);
                        }
                    } catch (OXException e) {
                        if (!OXFolderExceptionCode.NOT_EXISTS.equals(e)) {
                            throw e;
                        }
                        LOG.warn("Cannot load target object of this reminder.", e);
                        deleteReminderSafe(reminder, user.getId(), reminderSql);
                    }
                }
            } finally {
                SearchIterators.close(it);
            }
            return new AJAXRequestResult(jsonResponseArray, "json");
        } catch (final OXException e) {
            throw e;
        }
    }

}

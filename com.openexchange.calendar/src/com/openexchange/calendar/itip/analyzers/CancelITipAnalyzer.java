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

package com.openexchange.calendar.itip.analyzers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.openexchange.calendar.itip.ITipAction;
import com.openexchange.calendar.itip.ITipAnalysis;
import com.openexchange.calendar.itip.ITipAnnotation;
import com.openexchange.calendar.itip.ITipChange;
import com.openexchange.calendar.itip.ITipChange.Type;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.calendar.itip.Messages;
import com.openexchange.calendar.itip.generators.TypeWrapper;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link CancelITipAnalyzer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CancelITipAnalyzer extends AbstractITipAnalyzer{

    /**
     * Initializes a new {@link CancelITipAnalyzer}.
     * @param util
     * @param services TODO
     */
    public CancelITipAnalyzer(final ITipIntegrationUtility util, final ServiceLookup services) {
        super(util, services);
    }

    @Override
    public List<ITipMethod> getMethods() {
        return Arrays.asList(ITipMethod.CANCEL);
    }

    @Override
    public ITipAnalysis analyze(final ITipMessage message, final Map<String, String> header, final TypeWrapper wrapper, final Locale locale, final User user, final Context ctx, final Session session) throws OXException {

        final ITipAnalysis analysis = new ITipAnalysis();
        analysis.setMessage(message);

        final ITipChange change = new ITipChange();
        change.setType(Type.DELETE);

        CalendarDataObject appointment = message.getDataObject();
        if (appointment == null && message.exceptions().iterator().hasNext()) {
            appointment = message.exceptions().iterator().next();
        }
        if (appointment == null) {
            analysis.addAnnotation(new ITipAnnotation(Messages.CANCEL_UNKNOWN_APPOINTMENT, locale));
            analysis.recommendAction(ITipAction.IGNORE);
            return analysis;
        }
        analysis.setUid(appointment.getUid());
        Appointment toDelete = getToDelete(session, appointment);
        if (toDelete == null) {
            analysis.addAnnotation(new ITipAnnotation(Messages.CANCEL_UNKNOWN_APPOINTMENT, locale));
            analysis.recommendAction(ITipAction.IGNORE);
            return analysis;
        }
        change.setCurrentAppointment(toDelete);
        if (appointment.containsRecurrenceDatePosition()) {
            final List<Appointment> exceptions = util.getExceptions(toDelete, session);
            toDelete = findAndRemoveMatchingException(appointment, exceptions);
            if (toDelete == null) {
                toDelete = appointment;
                change.setType(Type.CREATE_DELETE_EXCEPTION);
            }
            change.setException(true);
        }
        // Update toDelete with changes in this cancel mail
        for (final int field: Appointment.ALL_COLUMNS) {
        	if (appointment.contains(field)) {
        		toDelete.set(field, appointment.get(field));
        	}
        }
        change.setDeleted(toDelete);

        describeDiff(change, wrapper, session, message);

        analysis.addChange(change);
        analysis.recommendAction(ITipAction.DELETE);

        return analysis;
    }

    private CalendarDataObject getToDelete(final Session session, CalendarDataObject appointment) throws OXException {
        CalendarDataObject toDelete = util.resolveUid(appointment.getUid(), session);
        if (toDelete == null) {
            return null;
        }
        if (appointment.containsRecurrenceDatePosition() && toDelete.containsDeleteExceptions() && toDelete.getDeleteException() != null) {
            for (Date deleteException : toDelete.getDeleteException()) {
                if (deleteException.equals(appointment.getRecurrenceDatePosition())) {
                    return null;
                }
            }
        }
        return toDelete;
    }




}

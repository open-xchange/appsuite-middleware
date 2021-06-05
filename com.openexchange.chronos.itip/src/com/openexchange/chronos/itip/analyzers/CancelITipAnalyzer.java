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

package com.openexchange.chronos.itip.analyzers;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAnnotation;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.ITipChange.Type;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.ITipMessage;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.LegacyAnalyzing;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.TypeWrapper;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.user.User;

/**
 * {@link CancelITipAnalyzer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class CancelITipAnalyzer extends AbstractITipAnalyzer implements LegacyAnalyzing {

    /**
     * Initializes a new {@link CancelITipAnalyzer}.
     *
     * @param util The {@link ITipIntegrationUtility}
     */
    public CancelITipAnalyzer(final ITipIntegrationUtility util) {
        super(util);
    }

    @Override
    public List<ITipMethod> getMethods() {
        return Arrays.asList(ITipMethod.CANCEL);
    }

    @Override
    public ITipAnalysis analyze(final ITipMessage message, final Map<String, String> header, final TypeWrapper wrapper, final Locale locale, final User user, final Context ctx, final CalendarSession session) throws OXException {

        final ITipAnalysis analysis = new ITipAnalysis();
        analysis.setMessage(message);

        final ITipChange change = new ITipChange();
        change.setType(Type.DELETE);

        Event event = message.getEvent();
        if (event == null && message.exceptions().iterator().hasNext()) {
            event = message.exceptions().iterator().next();
        }
        if (event == null) {
            analysis.addAnnotation(new ITipAnnotation(Messages.CANCEL_UNKNOWN_APPOINTMENT, locale));
            analysis.recommendAction(ITipAction.IGNORE);
            return analysis;
        }
        analysis.setUid(event.getUid());
        Event toDelete = getToDelete(session, event);
        session.getUtilities().adjustTimeZones(session.getSession(), session.getUserId(), event, toDelete);
        if (toDelete == null) {
            analysis.addAnnotation(new ITipAnnotation(Messages.CANCEL_UNKNOWN_APPOINTMENT, locale));
            analysis.recommendAction(ITipAction.IGNORE);
            return analysis;
        }
        change.setCurrentEvent(toDelete);
        if (event.containsRecurrenceId()) {
            List<Event> exceptions = util.getExceptions(toDelete, session);
            toDelete = findAndRemoveMatchingException(event, exceptions);
            if (toDelete == null) {
                toDelete = event;
                change.setType(Type.CREATE_DELETE_EXCEPTION);
            }
            change.setException(true);
        }
        // Update toDelete with changes in this cancel mail
        for (EventField field : EventField.values()) {
            if (event.isSet(field)) {
                EventMapper.getInstance().copy(event, toDelete, field);
            }
        }
        change.setDeleted(toDelete);

        describeDiff(change, wrapper, session, message);

        analysis.addChange(change);
        analysis.recommendAction(ITipAction.DELETE);

        return analysis;
    }

    private Event getToDelete(CalendarSession session, Event event) throws OXException {
        Event toDelete = util.resolveUid(event.getUid(), session);
        if (toDelete == null) {
            return null;
        }
        if (event.containsRecurrenceId() && toDelete.containsDeleteExceptionDates() && toDelete.getDeleteExceptionDates() != null) {
            for (RecurrenceId deleteException : toDelete.getDeleteExceptionDates()) {
                if (deleteException.equals(event.getRecurrenceId())) {
                    return null;
                }
            }
        }
        return toDelete;
    }

}

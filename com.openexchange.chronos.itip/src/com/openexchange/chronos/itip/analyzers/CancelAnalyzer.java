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

import static com.openexchange.chronos.common.CalendarUtils.matches;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAnnotation;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.ITipChange.Type;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.ITipMessage;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.TypeWrapper;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.user.User;

/**
 * {@link CancelAnalyzer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.6
 */
public class CancelAnalyzer extends AbstractITipAnalyzer {

    /**
     * Initializes a new {@link CancelAnalyzer}.
     *
     * @param util The {@link ITipIntegrationUtility}
     */
    public CancelAnalyzer(ITipIntegrationUtility util) {
        super(util);
    }

    @Override
    public List<ITipMethod> getMethods() {
        return Arrays.asList(ITipMethod.CANCEL);
    }

    @Override
    public ITipAnalysis analyze(ITipMessage message, Map<String, String> headers, TypeWrapper wrapper, Locale locale, User user, Context ctx, CalendarSession session) throws OXException {
        return analyze(session, session.getUserId(), message, wrapper, locale);
    }

    private ITipAnalysis analyze(CalendarSession session, int calendarUserId, ITipMessage message, TypeWrapper wrapper, Locale locale) throws OXException {
        ITipAnalysis analysis = new ITipAnalysis();
        analysis.setMessage(message);
        CalendarObjectResource resource;
        try {
            resource = getResource(session, message, true);
        } catch (OXException e) {
            analysis.addAnnotation(new ITipAnnotation(e.getDisplayMessage(locale), locale));
            analysis.recommendAction(ITipAction.IGNORE);
            return analysis;
        }
        analysis.setUid(resource.getUid());
        /*
         * get currently stored resource & determine changes
         */
        CalendarObjectResource originalResource = getOriginalResource(session, resource.getUid(), calendarUserId);
        for (ITipChange change : determineChanges(session, calendarUserId, originalResource, message, wrapper)) {
            analysis.addChange(change);
        }
        /*
         * set recommendation & return resulting analysis
         */
        if (null != originalResource && false == matches(originalResource.getOrganizer(), resource.getOrganizer())) {
            analysis.addAnnotation(new ITipAnnotation(Messages.UNALLOWED_ORGANIZER_CHANGE, locale));
            analysis.recommendAction(ITipAction.IGNORE);
        } else if (analysis.getChanges().isEmpty()) {
            analysis.addAnnotation(new ITipAnnotation(Messages.CANCEL_UNKNOWN_APPOINTMENT, locale));
            analysis.recommendAction(ITipAction.IGNORE);
        } else {
            analysis.recommendAction(ITipAction.DELETE);
        }
        return analysis;
    }

    private List<ITipChange> determineChanges(CalendarSession session, int calendarUserId, CalendarObjectResource originalResource, ITipMessage message, TypeWrapper wrapper) throws OXException {
        if (null == originalResource) {
            return Collections.emptyList(); // no changes 
        }
        CalendarObjectResource resource = getResource(session, message, true);
        Event patchedSeriesMaster = null != resource.getSeriesMaster() ? patchEvent(session, resource.getSeriesMaster(), null, calendarUserId) : null;
        List<ITipChange> changes = new ArrayList<ITipChange>();
        for (Event event : resource.getEvents()) {
            /*
             * patch event & derive change based on currently stored resource
             */
            Event patchedEvent = patchEvent(session, event, originalResource.getFirstEvent(), calendarUserId);
            ITipChange change = optChange(session, originalResource, patchedEvent, patchedSeriesMaster);
            if (null != change) {
                describeDiff(change, wrapper, session, message);
                changes.add(change);
            }
        }
        return changes;
    }

    private ITipChange optChange(CalendarSession session, CalendarObjectResource originalResource, Event event, Event seriesMaster) {
        if (null != event.getRecurrenceId()) {
            Event originalChangeException = originalResource.getChangeException(event.getRecurrenceId());
            if (null != originalChangeException) {
                /*
                 * cancel of existing change exception, add change if different
                 */
                return getChange(Type.DELETE, originalChangeException, event, true);
            }
            /*
             * creation of new delete exception
             */
            Event originalOccurrence = optEventOccurrence(session, seriesMaster, event.getRecurrenceId());
            return getChange(Type.CREATE_DELETE_EXCEPTION, originalOccurrence, event, true);
        }
        if (null != originalResource.getFirstEvent() && null == originalResource.getFirstEvent().getRecurrenceId()) {
            /*
             * cancel of series master or non-recurring
             */
            return getChange(Type.DELETE, originalResource.getFirstEvent(), event, false);
        }
        return null; // nothing to delete
    }

    private static ITipChange getChange(Type type, Event currentEvent, Event deletedEvent, boolean isException) {
        ITipChange change = new ITipChange();
        change.setType(type);
        change.setCurrentEvent(currentEvent);
        change.setDeleted(deletedEvent);
        change.setException(isException);
        return change;
    }

}

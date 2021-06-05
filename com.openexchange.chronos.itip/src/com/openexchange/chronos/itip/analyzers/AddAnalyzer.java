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
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
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
 * {@link AddAnalyzer} - Analyzer for the iTIP method <code>ADD</code>
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.6
 */
public class AddAnalyzer extends AbstractITipAnalyzer {

    /**
     * Initializes a new {@link AddAnalyzer}.
     * 
     * @param util The utilities
     */
    public AddAnalyzer(ITipIntegrationUtility util) {
        super(util);
    }

    @Override
    public List<ITipMethod> getMethods() {
        return Arrays.asList(ITipMethod.ADD);
    }

    @Override
    public ITipAnalysis analyze(ITipMessage message, Map<String, String> header, TypeWrapper wrapper, Locale locale, User user, Context ctx, CalendarSession session) throws OXException {
        ITipMethod method = message.getMethod();
        if (false == method.equals(ITipMethod.ADD)) {
            throw new IllegalStateException("Wrong mehtod to analyze");
        }
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
        CalendarObjectResource originalResource = getOriginalResource(session, resource.getUid(), calendarUserId);
        /*
         * Check if we known about a series with the same UID
         */
        if (null == originalResource || null == originalResource.getSeriesMaster()) {
            analysis.addAnnotation(new ITipAnnotation(Messages.ADD_TO_UNKNOWN, locale));
            analysis.recommendAction(ITipAction.REFRESH);
            return analysis;
        }

        /*
         * Analyze each event
         */
        for (Event changeException : resource.getChangeExceptions()) {
            Event originalException = originalResource.getChangeException(changeException.getRecurrenceId());
            if (null != originalException) {
                /*
                 * Known exception instance. RFC recommends to send a REFRESH, see https://tools.ietf.org/html/rfc5546#section-3.2.4
                 */
                analysis.addAnnotation(new ITipAnnotation(Messages.ADD_WOULD_OVERWRITE_EXISTING_EXCEPTION, locale));
                analysis.recommendActions(ITipAction.REFRESH, ITipAction.IGNORE);
                return analysis;
            }
            /*
             * Announce new change exception
             */
            ITipChange change = new ITipChange();
            change.setException(true);
            change.setType(Type.CREATE);
            changeException = session.getUtilities().copyEvent(changeException, (EventField[]) null);
            ensureParticipant(originalResource.getFirstEvent(), changeException, session, calendarUserId);
            change.setConflicts(util.getConflicts(changeException, session));
            change.setNewEvent(changeException);
            change.setMaster(originalResource.getSeriesMaster());
            describeDiff(change, wrapper, session, message);
            analysis.addChange(change);
        }
        if (analysis.getChanges().isEmpty()) {
            return analysis;
        }
        /*
         * Add recommended actions, advice to ignore if we have any conflicts
         */
        analysis.recommendActions(ITipAction.UPDATE, ITipAction.ACCEPT, ITipAction.TENTATIVE, ITipAction.DECLINE);
        if (hasConflicts(analysis)) {
            analysis.recommendAction(ITipAction.ACCEPT_AND_IGNORE_CONFLICTS);
        }
        return analysis;
    }

}

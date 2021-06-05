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
 * {@link AddITipAnalyzer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AddITipAnalyzer extends AbstractITipAnalyzer implements LegacyAnalyzing {

    public AddITipAnalyzer(ITipIntegrationUtility util) {
        super(util);
    }

    @Override
    public List<ITipMethod> getMethods() {
        return Arrays.asList(ITipMethod.ADD);
    }

    @Override
    public ITipAnalysis analyze(ITipMessage message, Map<String, String> header, TypeWrapper wrapper, Locale locale, User user, Context ctx, CalendarSession session) throws OXException {

        ITipAnalysis analysis = new ITipAnalysis();
        analysis.setMessage(message);

        Event master = null;
        List<Event> exceptions = null;
        boolean findActions = true;
        for (Event exception : message.exceptions()) {
            exception = session.getUtilities().copyEvent(exception, (EventField[]) null);
            int owner = session.getUserId();
            if (message.getOwner() > 0 && message.getOwner() != session.getUserId()) {
                owner = message.getOwner();
            }
            ensureParticipant(master, exception, session, owner);
            ITipChange change = new ITipChange();
            change.setType(Type.CREATE);
            change.setException(true);
            analysis.setUid(exception.getUid());
            master = util.resolveUid(exception.getUid(), session);
            if (master == null) {
                analysis.addAnnotation(new ITipAnnotation(Messages.ADD_TO_UNKNOWN, locale));
                analysis.recommendAction(ITipAction.REFRESH);
                return analysis;
            }
            exceptions = util.getExceptions(master, session);
            Event existingException = findAndRemoveMatchingException(exception, exceptions);
            if (existingException != null) {
                change.setCurrentEvent(existingException);
                analysis.recommendActions(ITipAction.IGNORE, ITipAction.ACCEPT_AND_REPLACE);
                findActions = false;
                session.getUtilities().adjustTimeZones(session.getSession(), owner, exception, existingException);
            } else {
                session.getUtilities().adjustTimeZones(session.getSession(), owner, exception, master);

            }
            change.setConflicts(util.getConflicts(exception, session));
            change.setNewEvent(exception);
            change.setMaster(master);

            describeDiff(change, wrapper, session, message);
            analysis.addChange(change);

        }

        if (findActions && !analysis.getChanges().isEmpty()) {
            if (rescheduling(analysis)) {
                analysis.recommendActions(ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);
                if (hasConflicts(analysis)) {
                    analysis.recommendAction(ITipAction.ACCEPT_AND_IGNORE_CONFLICTS);
                } else {
                    analysis.recommendAction(ITipAction.ACCEPT);
                }
            } else {
                if (isCreate(analysis)) {
                    analysis.recommendActions(ITipAction.ACCEPT, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);
                } else {
                    analysis.recommendActions(ITipAction.UPDATE, ITipAction.ACCEPT, ITipAction.DECLINE, ITipAction.TENTATIVE, ITipAction.DELEGATE, ITipAction.COUNTER);
                }
            }
        }

        return analysis;
    }

}

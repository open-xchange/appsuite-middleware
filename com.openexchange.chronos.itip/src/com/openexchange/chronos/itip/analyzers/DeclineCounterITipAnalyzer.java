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
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAnnotation;
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
 * {@link DeclineCounterITipAnalyzer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DeclineCounterITipAnalyzer extends AbstractITipAnalyzer implements LegacyAnalyzing {

    public DeclineCounterITipAnalyzer(ITipIntegrationUtility util) {
        super(util);
    }

    @Override
    public List<ITipMethod> getMethods() {
        return Arrays.asList(ITipMethod.DECLINECOUNTER);
    }

    @Override
    public ITipAnalysis analyze(ITipMessage message, Map<String, String> header, TypeWrapper wrapper, Locale locale, User user, Context ctx, CalendarSession session) throws OXException {
        ITipAnalysis analysis = new ITipAnalysis();
        analysis.setMessage(message);

        Event event = message.getEvent();
        boolean isException = false;
        if (event == null) {
            for (Event exception : message.exceptions()) {
                event = exception;
                isException = true;
                break;
            }
        }
        if (null == event) {
            analysis.addAnnotation(new ITipAnnotation(Messages.DECLINED_FOR_UNKNOWN, locale));
            analysis.recommendActions(ITipAction.IGNORE, ITipAction.REFRESH);
            return analysis;
        }
        analysis.setUid(event.getUid());

        Event declinedFor = util.resolveUid(event.getUid(), session);
        if (declinedFor == null) {
            analysis.addAnnotation(new ITipAnnotation(Messages.DECLINED_FOR_UNKNOWN, locale));
            analysis.recommendActions(ITipAction.IGNORE, ITipAction.REFRESH);
            return analysis;
        }

        if (declinedFor.containsSequence() && event.containsSequence() && declinedFor.getSequence() > event.getSequence()) {
            analysis.addAnnotation(new ITipAnnotation(Messages.OLD_UPDATE, locale));
            analysis.recommendAction(ITipAction.IGNORE);
            return analysis;
        }

        if (isException) {
            List<Event> exceptions = util.getExceptions(declinedFor, session);
            declinedFor = findAndRemoveMatchingException(event, exceptions);
            if (declinedFor == null) {
                analysis.addAnnotation(new ITipAnnotation(Messages.DECLINED_FOR_UNKNOWN, locale));
                analysis.recommendActions(ITipAction.IGNORE, ITipAction.REFRESH);
                return analysis;
            }
        }

        ITipAnnotation annotation = new ITipAnnotation(Messages.DECLINED_COUNTER_PROPOSAL, locale);
        annotation.setEvent(declinedFor);
        analysis.addAnnotation(annotation);

        analysis.recommendActions(ITipAction.DECLINE, ITipAction.REFRESH);

        return analysis;
    }

}

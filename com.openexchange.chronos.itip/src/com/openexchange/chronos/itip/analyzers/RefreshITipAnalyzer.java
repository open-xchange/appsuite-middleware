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
 * {@link RefreshITipAnalyzer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RefreshITipAnalyzer extends AbstractITipAnalyzer implements LegacyAnalyzing {

    public RefreshITipAnalyzer(ITipIntegrationUtility util) {
        super(util);
    }

    @Override
    public List<ITipMethod> getMethods() {
        return Arrays.asList(ITipMethod.REFRESH);
    }

    @Override
    public ITipAnalysis analyze(ITipMessage message, Map<String, String> header, TypeWrapper wrapper, Locale locale, User user, Context ctx, CalendarSession session) throws OXException {
        ITipAnalysis analysis = new ITipAnalysis();
        analysis.setMessage(message);

        Event event = message.getEvent();

        Event refreshed = null;
        boolean isException = false;
        if (event == null) {
            isException = true;
            for (Event exception : message.exceptions()) {
                event = exception;
            }
        }

        if (null == event) {
            analysis.addAnnotation(new ITipAnnotation(Messages.REFRESH_FOR_UNKNOWN, locale));
            analysis.recommendAction(ITipAction.IGNORE);
            return analysis;
        }
        analysis.setUid(event.getUid());

        refreshed = util.resolveUid(event.getUid(), session);

        if (isException && refreshed != null) {
            refreshed = findAndRemoveMatchingException(event, util.getExceptions(refreshed, session));
        }

        if (refreshed == null) {
            analysis.addAnnotation(new ITipAnnotation(Messages.REFRESH_FOR_UNKNOWN, locale));
            analysis.recommendAction(ITipAction.IGNORE);
            return analysis;
        }

        ITipAnnotation annotation = new ITipAnnotation(Messages.REQUESTED_A_REFRESHER, locale);
        annotation.setEvent(refreshed);
        analysis.addAnnotation(annotation);
        analysis.recommendAction(ITipAction.SEND_APPOINTMENT);

        return analysis;
    }

}

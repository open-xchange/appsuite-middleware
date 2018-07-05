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
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.generators.TypeWrapper;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;

/**
 * {@link DeclineCounterITipAnalyzer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DeclineCounterITipAnalyzer extends AbstractITipAnalyzer {

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
            declinedFor = findAndRemoveMatchingException(null, event, exceptions);
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

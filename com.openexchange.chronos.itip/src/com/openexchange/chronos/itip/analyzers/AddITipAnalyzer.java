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
import com.openexchange.groupware.ldap.User;

/**
 * {@link AddITipAnalyzer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AddITipAnalyzer extends AbstractITipAnalyzer {

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
                session.getUtilities().adjustTimeZones(owner, exception, existingException);
            } else {
                session.getUtilities().adjustTimeZones(owner, exception, master);

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

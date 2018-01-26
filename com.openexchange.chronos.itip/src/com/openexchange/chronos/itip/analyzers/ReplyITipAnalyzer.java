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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.itip.ITipAction;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAnnotation;
import com.openexchange.chronos.itip.ITipChange;
import com.openexchange.chronos.itip.ITipChange.Type;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.ITipMessage;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.Messages;
import com.openexchange.chronos.itip.ParticipantChange;
import com.openexchange.chronos.itip.generators.TypeWrapper;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;

/**
 * {@link ReplyITipAnalyzer} - 'Reply' is a action only performed as answer on a 'request'
 * For details see <a href="https://tools.ietf.org/html/rfc2446#section-3.2.3">RFC 2446</a>
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 */
public class ReplyITipAnalyzer extends AbstractITipAnalyzer {

    private final static Logger LOGGER = LoggerFactory.getLogger(ReplyITipAnalyzer.class);

    public ReplyITipAnalyzer(final ITipIntegrationUtility util) {
        super(util);
    }

    @Override
    public List<ITipMethod> getMethods() {
        return Arrays.asList(ITipMethod.REPLY);
    }

    @Override
    public ITipAnalysis analyze(final ITipMessage message, final Map<String, String> header, final TypeWrapper wrapper, final Locale locale, final User user, final Context ctx, final CalendarSession session) throws OXException {
        final ITipAnalysis analysis = new ITipAnalysis();
        analysis.setMessage(message);

        ITipMethod method = message.getMethod();
        if (false == method.equals(ITipMethod.REPLY)) {
            LOGGER.error("Wrong analyzer for provided iTip method {}", method);
            analysis.addAnnotation(new ITipAnnotation(Messages.NONE, locale));
            return analysis;
        }

        final Event update = message.getEvent();

        String uid = null;
        if (update != null) {
            uid = update.getUid();
        } else {
            for (final Event event : message.exceptions()) {
                uid = event.getUid();
                if (null != uid) {
                    break;
                }
            }
        }

        analysis.setUid(uid);

        final Event original = util.resolveUid(uid, session);
        if (original == null) {
            analysis.addAnnotation(new ITipAnnotation(Messages.CHANGE_PARTICIPANT_STATE_IN_UNKNOWN_APPOINTMENT, locale));
            return analysis;
        }

        if (update != null) {
            final ParticipantChange participantChange = applyParticipantChange(update, original, message);
            if (participantChange != null) {
                participantChange.setComment(message.getComment());
            }
            if (participantChange != null || method == ITipMethod.COUNTER) {
                final ITipChange change = new ITipChange();
                change.setNewEvent(update);
                change.setCurrentEvent(original);

                change.setType(Type.UPDATE);
                change.setParticipantChange(participantChange);
                describeDiff(change, wrapper, session, message);
                analysis.addChange(change);
            }
        }

        final List<Event> exceptions = util.getExceptions(original, session.getSession());
        for (final Event exception : message.exceptions()) {
            final Event matchingException = findAndRemoveMatchingException(exception, exceptions);
            ITipChange change = new ITipChange();
            change.setException(true);
            change.setMaster(original);
            if (matchingException != null) {
                ParticipantChange participantChange = applyParticipantChange(exception, matchingException, message);

                change = new ITipChange();
                change.setException(true);
                change.setNewEvent(exception);
                change.setCurrentEvent(matchingException);

                change.setType(Type.UPDATE);
                if (participantChange != null) {
                    participantChange.setComment(message.getComment());
                    change.setParticipantChange(participantChange);
                }
                describeDiff(change, wrapper, session, message);

                analysis.addChange(change);
            } else {
                ParticipantChange participantChange = applyParticipantChange(exception, original, message);
                change.setCurrentEvent(original);
                change.setNewEvent(exception);
                change.setType(Type.CREATE);
                if (participantChange != null) {
                    change.setParticipantChange(participantChange);
                }
                describeDiff(change, wrapper, session, message);
                analysis.addChange(change);

                //analysis.addAnnotation(new ITipAnnotation(Messages.CHANGE_PARTICIPANT_STATE_IN_UNKNOWN_APPOINTMENT, locale));
            }
        }
        if (containsPartyCrasher(analysis)) {
            analysis.recommendAction(ITipAction.ACCEPT_PARTY_CRASHER);
        } else {
            if (containsChangesForUpdate(analysis)) {
                analysis.recommendAction(ITipAction.UPDATE);
            }
        }
        return analysis;
    }

    private boolean containsChangesForUpdate(ITipAnalysis analysis) throws OXException {
        if (analysis.getChanges() == null || analysis.getChanges().size() == 0) {
            return false;
        }

        for (ITipChange change : analysis.getChanges()) {
            if (change.getDiff() == null) {
                continue;
            }

            if (change.getDiff().getUpdatedFields() == null) {
                continue;
            }

            if (change.getDiff().getUpdatedFields().size() != 0) {
                return true;
            }
        }
        return false;
    }

    private boolean containsPartyCrasher(final ITipAnalysis analysis) {
        for (final ITipChange change : analysis.getChanges()) {
            if (change.getParticipantChange() != null && change.getParticipantChange().isPartyCrasher()) {
                return true;
            }
        }
        return false;
    }

    private ParticipantChange applyParticipantChange(final Event update, final Event original, final ITipMessage message) throws OXException {
        final ParticipantChange pChange = new ParticipantChange();

        List<Attendee> originalAttendees = original.getAttendees();
        List<Attendee> updatedAttendees = update.getAttendees();

        if (updatedAttendees.size() != 1) {
            // Not RFC conform
            throw new OXException();
        }
        Attendee reply = updatedAttendees.get(0);

        // Party crasher?
        boolean partyCrasher = true;
        for (Attendee attendee : originalAttendees) {
            if (compareAttendees(attendee, reply, new AttendeeField[] { AttendeeField.URI })) {
                // Nope, we already know the replaying attendee
                partyCrasher = false;
                // Did something change?
                if (false == attendee.getComment().equals(reply.getComment()) || false == attendee.getPartStat().equals(reply.getPartStat())) {
                    return null;
                }
                break;
            }
        }
        pChange.setComment(reply.getComment());
        pChange.setConfirmStatusUpdate(reply.getPartStat());
        pChange.setPartyCrasher(partyCrasher);

        return pChange;
    }

    private boolean compareAttendees(Attendee a, Attendee b, AttendeeField... fields) throws OXException {
        AttendeeMapper mapper = AttendeeMapper.getInstance();
        AttendeeField[] differences;
        if (null == fields || 1 >= fields.length) {
            differences = mapper.getDifferentFields(a, b);
        } else {
            List<AttendeeField> values = Arrays.asList(AttendeeField.values());
            for (AttendeeField field : fields) {
                values.remove(field);
            }
            differences = mapper.getDifferentFields(a, b, true, values.toArray(new AttendeeField[] {})).toArray(new AttendeeField[] {});
        }
        if (0 == differences.length) {
            return true;
        }
        return false;
    }
}

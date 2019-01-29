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

package com.openexchange.chronos.itip.sender;

import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.itip.EventNotificationPoolService;
import com.openexchange.chronos.itip.generators.NotificationMail;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.State.Type;
import com.openexchange.session.Session;

public class PoolingMailSenderService implements MailSenderService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PoolingMailSenderService.class);

    private final EventNotificationPoolService pool;

    private final MailSenderService delegate;

    public PoolingMailSenderService(EventNotificationPoolService pool, MailSenderService delegate) {
        this.pool = pool;
        this.delegate = delegate;
    }

    @Override
    public void sendMail(NotificationMail mail, CalendarSession session, CalendarUser principal, String comment) throws OXException {
        if (!mail.shouldBeSent(session)) {
            return;
        }
        try {
            // Pool messages if this is a create mail or a modify mail
            // Dump messages if the appointment is deleted
            if (isDeleteMail(mail)) {
                pool.drop(mail.getEvent(), session);
                delegate.sendMail(mail, session, principal, comment);
                return;
            }

            // Direct send reply messages to external organizers.
            if (mail.isAboutActorsStateChangeOnly() && !CalendarUtils.isInternal(mail.getOriginal().getOrganizer(), CalendarUserType.INDIVIDUAL)) {
                poolAwareDirectSend(mail, session, principal, comment);
                return;
            }

            if (shouldEnqueue(mail)) {
                int sharedFolderOwner = -1;
                if (mail.getSharedCalendarOwner() != null) {
                    sharedFolderOwner = mail.getSharedCalendarOwner().getIdentifier();
                }
                pool.enqueue(mail.getOriginal(), mail.getEvent(), session, sharedFolderOwner, principal, comment);
                return;
            }

            // Fasttrack messages prior to creating a change or delete exception
            if (needsFastTrack(mail)) {
                Event app = mail.getOriginal();
                if (app == null) {
                    app = mail.getEvent();
                }
                pool.fasttrack(app, session);
                delegate.sendMail(mail, session, principal, comment);
                return;
            }
            poolAwareDirectSend(mail, session, principal, comment);
            //delegate.sendMail(mail, session);

        } catch (OXException x) {
            LOG.error("", x);
        }
    }

    /**
     * Sends the mail directly, but makes the aware of this to avoid duplicate Mails.
     *
     * @param mail The {@link NotificationMail} to send
     * @param session The {@link Session}
     * @throws OXException In case sending fails
     */
    private void poolAwareDirectSend(NotificationMail mail, CalendarSession session, CalendarUser principal, String comment) throws OXException {
        pool.aware(mail.getEvent(), mail.getRecipient(), session);
        delegate.sendMail(mail, session, principal, comment);
    }

    private boolean isStateChange(NotificationMail mail) {
        switch (mail.getStateType()) {
            case ACCEPTED:
            case DECLINED:
            case TENTATIVELY_ACCEPTED:
            case NONE_ACCEPTED:
            case MODIFIED:
                return true;
            default:
                return false;
        }
    }

    private boolean shouldEnqueue(NotificationMail mail) throws OXException {
        if (!mail.getActor().equals(mail.getOnBehalfOf())) {
            return false;
        }
        if (isStateChange(mail)) {
            return !needsFastTrack(mail);
        }
        return false;
    }

    private boolean needsFastTrack(NotificationMail mail) {
        if (mail.getOriginal() == null || mail.getEvent() == null) {
            return true;
        }
        return isChangeExceptionsMail(mail);
    }

    private boolean isChangeExceptionsMail(NotificationMail mail) {
        if (mail.getOriginal() != null && CalendarUtils.isSeriesMaster(mail.getOriginal()) && ((mail.getEvent().containsRecurrenceId() && mail.getEvent().getRecurrenceId() != null))) {
            return true;
        }

        return false;
    }

    private boolean isDeleteMail(NotificationMail mail) {
        return mail.getStateType() == Type.DELETED && !isChangeExceptionsMail(mail);
    }
}

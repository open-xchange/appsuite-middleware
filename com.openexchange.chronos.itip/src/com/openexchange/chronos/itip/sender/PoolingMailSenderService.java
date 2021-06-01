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
        if (null == mail || null == mail.getStateType()) {
            return false;
        }
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

    private boolean shouldEnqueue(NotificationMail mail) {
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

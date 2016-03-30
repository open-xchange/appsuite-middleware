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

package com.openexchange.calendar.itip.sender;

import com.openexchange.calendar.itip.AppointmentNotificationPoolService;
import com.openexchange.calendar.itip.generators.NotificationMail;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.notify.State.Type;
import com.openexchange.session.Session;

public class PoolingMailSenderService implements MailSenderService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PoolingMailSenderService.class);

	private final AppointmentNotificationPoolService pool;
	private final MailSenderService delegate;

	public PoolingMailSenderService(AppointmentNotificationPoolService pool, MailSenderService delegate) {
		this.pool = pool;
		this.delegate = delegate;
	}

	@Override
    public void sendMail(NotificationMail mail, Session session) {
		if (!mail.shouldBeSent()) {
			return;
		}
		try {

			// Pool messages if this is a create mail or a modify mail
			// Dump messages if the appointment is deleted
			if (isDeleteMail(mail)) {
				pool.drop(mail.getAppointment(), session);
				delegate.sendMail(mail, session);
				return;
			}

			if (shouldEnqueue(mail)) {
				int sharedFolderOwner = -1;
				if (mail.getSharedCalendarOwner() != null) {
					sharedFolderOwner = mail.getSharedCalendarOwner().getIdentifier();
				}
				pool.enqueue(mail.getOriginal(), mail.getAppointment(), session, sharedFolderOwner);
				return;
			}

			// Fasttrack messages prior to creating a change or delete exception
			if (needsFastTrack(mail)) {
                Appointment app = mail.getOriginal();
                if (app == null) {
                    app = mail.getAppointment();
                }
                pool.fasttrack(app, session);
				delegate.sendMail(mail, session);
				return;
			}
			poolAwareDirectSend(mail, session);
			//delegate.sendMail(mail, session);

		} catch (OXException x) {
			LOG.error("", x);
		}
	}

	/**
	 * Sends the mail directly, but makes the aware of this to avoid duplicate Mails.
	 * @param mail
	 * @param session
	 */
	private void poolAwareDirectSend(NotificationMail mail, Session session) {
	    pool.aware(mail.getAppointment(), mail.getRecipient(), session);
	    delegate.sendMail(mail, session);
	}


	private boolean isStateChange(NotificationMail mail) {
		Type stateType = mail.getStateType();
		if (stateType == Type.ACCEPTED || stateType == Type.DECLINED || stateType == Type.TENTATIVELY_ACCEPTED || stateType == Type.NONE_ACCEPTED) {
			return true;
		}
		return false;
	}

	private boolean shouldEnqueue(NotificationMail mail) {
	    if (!mail.getActor().equals(mail.getOnBehalfOf())) {
	        return false;
	    }
		if (mail.getOriginal() == null || mail.getAppointment() == null) {
			return false;
		}
		Type stateType = mail.getStateType();
		if (stateType == Type.MODIFIED || isStateChange(mail)) {
			return !needsFastTrack(mail);
		}

		return false;
	}

	private boolean needsFastTrack(NotificationMail mail) {
		if (mail.getOriginal() == null || mail.getAppointment() == null) {
			return true;
		}
		return isChangeExceptionsMail(mail);
	}

	private boolean isChangeExceptionsMail(NotificationMail mail) {
		if (mail.getOriginal() != null && mail.getOriginal().isMaster() &&  ((mail.getAppointment().containsRecurrenceDatePosition() && mail.getAppointment().getRecurrenceDatePosition() != null) || (mail.getAppointment().containsRecurrencePosition() && mail.getAppointment().getRecurrencePosition() != 0))) {
			return true;
		}

		return false;
	}

	private boolean isDeleteMail(NotificationMail mail) {
		return mail.getStateType() == Type.DELETED && ! isChangeExceptionsMail(mail);
	}

}

package com.openexchange.calendar.itip.sender;

import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;

import com.openexchange.calendar.itip.AppointmentNotificationPoolService;
import com.openexchange.calendar.itip.generators.NotificationMail;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.notify.State.Type;
import com.openexchange.session.Session;

public class PoolingMailSenderService implements MailSenderService {
	
	private static final Log LOG = LogFactory.getLog(PoolingMailSenderService.class);
	
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
			
			delegate.sendMail(mail, session);
			
		} catch (OXException x) {
			LOG.error(x.getMessage(), x);
		}
	}


	private boolean isStateChange(NotificationMail mail) {
		Type stateType = mail.getStateType();
		if (stateType == Type.ACCEPTED || stateType == Type.DECLINED || stateType == Type.TENTATIVELY_ACCEPTED || stateType == Type.NONE_ACCEPTED) {
			return true;
		}
		return false;
	}
	
	private boolean shouldEnqueue(NotificationMail mail) {
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

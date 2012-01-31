package com.openexchange.calendar.itip.sender;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.calendar.itip.AppointmentNotificationPoolService;
import com.openexchange.calendar.itip.generators.NotificationMail;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.State.Type;
import com.openexchange.session.Session;

public class PoolingMailSenderService implements MailSenderService {
	
	private static final Log LOG = LogFactory.getLog(PoolingMailSenderService.class);
	
	private AppointmentNotificationPoolService pool; 
	private MailSenderService delegate;
	
	public PoolingMailSenderService(AppointmentNotificationPoolService pool, MailSenderService delegate) {
		this.pool = pool;
		this.delegate = delegate;
	}
	
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
				pool.enqueue(mail.getOriginal(), mail.getAppointment(), session);
				return;
			}
			
			// Fasttrack messages prior to creating a change or delete exception
			if (needsFastTrack(mail)) {
				pool.fasttrack(mail.getOriginal(), session);
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
		Type stateType = mail.getStateType();
		if (stateType == Type.MODIFIED || isStateChange(mail)) {
			return !needsFastTrack(mail);
		}
		
		return false;
	}
	
	private boolean needsFastTrack(NotificationMail mail) {
		
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

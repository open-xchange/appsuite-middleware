package com.openexchange.ajax.request;

import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;

import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.UserService;

public class CalendarRequest {
	
    protected ServerSession session;

    protected Date timestamp;

    protected TimeZone timeZone;
    
    protected void convertExternalToInternalUsersIfPossible(CalendarObject appointmentObj, Context ctx, Log log){
		Participant[] participants = appointmentObj.getParticipants();
		if(participants == null)
			return;
		
		UserService us = ServerServiceRegistry.getInstance().getService(UserService.class);
		
		for(int pos = 0; pos < participants.length; pos++){
			Participant part = participants[pos];
			if(part.getType() == Participant.EXTERNAL_USER){
				User foundUser;
				try {
					foundUser = us.searchUser(part.getEmailAddress(), ctx);
					if(foundUser == null)
						continue;
					participants[pos] = new UserParticipant(foundUser.getId());
				} catch (UserException e) {
					log.error(e); //...and continue doing this for the remaining users
				}
			}
		}
		
		appointmentObj.setParticipants(participants);
	}
}

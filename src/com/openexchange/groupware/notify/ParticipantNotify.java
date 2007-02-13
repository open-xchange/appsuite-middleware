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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.notify;

import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.event.AppointmentEvent;
import com.openexchange.event.TaskEvent;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.mail.MailObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.Notifications;
import com.openexchange.groupware.ldap.Group;
import com.openexchange.groupware.ldap.GroupStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.Resource;
import com.openexchange.groupware.ldap.ResourceStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.i18n.StringHelper;
import com.openexchange.i18n.StringTemplate;
import com.openexchange.i18n.Template;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.SessionObject;

import com.openexchange.groupware.notify.NotificationConfig.NotificationProperty;

public class ParticipantNotify implements AppointmentEvent, TaskEvent {
	
	//TODO: Signatur?
	//TODO: Abgesagt / Zugesagt

	private Log LOG = LogFactory.getLog(ParticipantNotify.class);
	
	public ParticipantNotify() {
	}

	protected void sendMessage(String messageTitle, String message, List<String> name, SessionObject session, CalendarObject obj, int folderId, State state) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Sending message to: "+name);
			LOG.debug("=====["+messageTitle+"]====\n\n");
			LOG.debug(message);
			LOG.debug("\n\n============");
		}
		MailObject mail = new MailObject(session, obj.getObjectID(), folderId == -1 ? obj.getParentFolderID() : folderId, state.getModule());
		mail.setFromAddr(session.getUserObject().getMail());
		mail.setToAddrs(name.toArray(new String[name.size()]));
		mail.setText(message);
		mail.setSubject(messageTitle);
		mail.setContentType("text/plain");
		
		
		//System.out.println(folderId);
		try {
			mail.send();
		} catch (OXException e) {
			LOG.error(e);
		}
	}
	
	// Override for testing
	
	protected User[] resolveUsers(Context ctx, int...ids) throws LdapException {
		UserStorage users = UserStorage.getInstance(ctx);
		User[] r = new User[ids.length];
		int i = 0;
		for(int id : ids) {
			r[i++] = users.getUser(id); // FIXME
		}
		return r;
	}
	
	protected Group[] resolveGroups(Context ctx, int...ids) throws LdapException {
		GroupStorage groups = GroupStorage.getInstance(ctx);
		Group[] r = new Group[ids.length];
		int i = 0;
		for(int id : ids) {
			r[i++] = groups.getGroup(id);
		}
		return r;
	}
	
	protected Resource[] resolveResources(Context ctx, int...ids) throws LdapException {
		ResourceStorage resources = ResourceStorage.getInstance(ctx);
		Resource[] r = new Resource[ids.length];
		int i = 0;
		for(int id : ids) {
			r[i++] = resources.getResource(id);
		}
		return r;
	}
	
	protected UserConfiguration getUserConfiguration(int id, int[] groups, Context context) throws SQLException, LdapException, DBPoolingException, OXException {
		return UserConfiguration.loadUserConfiguration(id,groups,context);
	}
	
	public void appointmentCreated(AppointmentObject appointmentObj,
			SessionObject sessionObj) {
		sendNotification(appointmentObj, sessionObj, Notifications.APPOINTMENT_CREATE_MAIL,Notifications.APPOINTMENT_CREATE_TITLE, new AppointmentState(),false);
	}

	public void appointmentModified(AppointmentObject appointmentObj,
			SessionObject sessionObj) {
		sendNotification(appointmentObj, sessionObj, Notifications.APPOINTMENT_UPDATE_MAIL,Notifications.APPOINTMENT_UPDATE_TITLE, new AppointmentState(), false);
	}

	public void appointmentDeleted(AppointmentObject appointmentObj,
			SessionObject sessionObj) {

		sendNotification(appointmentObj, sessionObj, Notifications.APPOINTMENT_DELETE_MAIL,Notifications.APPOINTMENT_DELETE_TITLE, new AppointmentState(), NotificationConfig.getPropertyAsBoolean(NotificationProperty.NOTIFY_ON_DELETE, false));
	}
	
	public void taskCreated(Task taskObj, SessionObject sessionObj) {
		sendNotification(taskObj, sessionObj, Notifications.TASK_CREATE_MAIL,Notifications.TASK_CREATE_TITLE, new TaskState(),false);	
	}

	public void taskModified(Task taskObj, SessionObject sessionObj) {
		sendNotification(taskObj, sessionObj, Notifications.TASK_UPDATE_MAIL,Notifications.TASK_UPDATE_TITLE, new TaskState(), false);
		
	}

	public void taskDeleted(Task taskObj, SessionObject sessionObj) {
		sendNotification(taskObj, sessionObj, Notifications.TASK_DELETE_MAIL,Notifications.TASK_DELETE_TITLE, new TaskState(),NotificationConfig.getPropertyAsBoolean(NotificationProperty.NOTIFY_ON_DELETE, false));
	}
	
	private void sendNotification(CalendarObject obj, SessionObject sessionObj, String msgKey, String titleKey, State state, boolean forceNotifyOthers) {
		if(!obj.getNotification() && obj.getCreatedBy() == sessionObj.getUserObject().getId() && !forceNotifyOthers)
			return;
		if(obj.getParticipants() == null)
			return;
		StringBuffer participantsList = new StringBuffer();
		StringBuffer resourcesList = new StringBuffer();
		
		Map<Locale,List<EmailableParticipant>> receivers = new HashMap<Locale, List<EmailableParticipant>>();
		
		Map<String,EmailableParticipant> all = new HashMap<String,EmailableParticipant>();
		UserParticipant[] users = obj.getUsers();
		if(null == users) {
			sortParticipants(obj.getParticipants(), participantsList, resourcesList, receivers, sessionObj, all);
		} else {
			sortUserParticipants(obj.getUsers(), participantsList, receivers, sessionObj,all);
			sortExternalParticipantsAndResources(obj.getParticipants(),participantsList,resourcesList,receivers, sessionObj,all);
		}
		
		String createdByDisplayName = "UNKNOWN";
		String modifiedByDisplayName = "UNKNOWN";
		try {
			Context ctx = sessionObj.getContext();
			if(0 != obj.getCreatedBy()) 
				createdByDisplayName = resolveUsers(ctx,obj.getCreatedBy())[0].getDisplayName();
			if(0 != obj.getModifiedBy())
				modifiedByDisplayName = resolveUsers(ctx,obj.getModifiedBy())[0].getDisplayName();
		} catch (LdapException e) {
			createdByDisplayName = e.toString();
			modifiedByDisplayName = e.toString();
			LOG.debug(e);
		}
		
		// We need to generate one message per Locale and TimeZone and send it once for each internal user and collected for the external participants
		Map<String, Map<TimeZone,String>> messagesPerTZ = new HashMap<String, Map<TimeZone, String>>();
		List<MailMessage> messages = new ArrayList<MailMessage>();
		for(Locale locale : receivers.keySet()) {
			
			StringHelper strings = new StringHelper(locale);
			Template createTemplate = new StringTemplate(strings.getString(msgKey));
			
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,DateFormat.DEFAULT,locale);
			
			List<EmailableParticipant> participants = receivers.get(locale);
			Map<TimeZone, String> tz2text = messagesPerTZ.get(strings.getString(titleKey));
			if(tz2text == null) {
				tz2text = new HashMap<TimeZone, String>();
				messagesPerTZ.put(strings.getString(titleKey), tz2text);
			}
			
			for(EmailableParticipant p : participants) {
				TimeZone tz = TimeZone.getDefault();
				boolean sendMail = true;
				
				if(p.type != Participant.EXTERNAL_USER) {
					try {
						UserConfiguration userConfig = getUserConfiguration(p.id,p.groups,sessionObj.getContext());
						sendMail = state.sendMail(userConfig) && obj.getModifiedBy() != p.id && (obj.getNotification() || p.id == obj.getCreatedBy() || forceNotifyOthers);
						tz = p.timeZone;
					} catch (Exception e) {
						LOG.debug(e);
					}
				} else {
					sendMail = obj.getNotification() || (obj.getModifiedBy() != p.id && forceNotifyOthers);
				}
				
				if(sendMail) {
					String text = tz2text.get(tz);
					if(text == null) {
						df.setTimeZone(tz);
							
						Map<String,String> m = m(
						"start" 	,	(null == obj.getStartDate()) ? "" : df.format(obj.getStartDate()),
						"end"		,	(null == obj.getEndDate()) ? "" : df.format(obj.getEndDate()),
						"title"		,	(null == obj.getTitle()) ? "" : obj.getTitle(),
						"participants",		participantsList.toString(),
						"resources"	,	(resourcesList.length() > 0) ? resourcesList.toString() : strings.getString(Notifications.NO_RESOURCES),
						"created_by",		createdByDisplayName,
						"changed_by",		modifiedByDisplayName,
						"description",		(null == obj.getNote()) ? "" : obj.getNote()
						);
							
						state.addSpecial(obj,m);
							
						text = createTemplate.render(m);
						tz2text.put(tz,text);
					}
					MailMessage msg = new MailMessage();
					msg.message = text;
					msg.title = strings.getString(titleKey)+": "+obj.getTitle();
					msg.addresses.add(p.email);
					msg.folderId = p.folderId;
					messages.add(msg);
				}
			}
		}
		
		for(MailMessage mmsg : messages) {
			sendMessage(mmsg.title, mmsg.message, mmsg.addresses, sessionObj, obj, mmsg.folderId, state);
		}
		
	}

	private Map<String, String> m(String...args) {
		if(args.length % 2 != 0)
			throw new IllegalArgumentException("Length must be even");
		
		Map<String,String> retval = new HashMap<String, String>();
		
		for(int i = 0; i < args.length; i++) {
			retval.put(args[i], args[++i]);
		}
		return retval;
	}

	private Locale getLocale(String lang) {
		int index = lang.indexOf("_");
		if(index != -1)
			lang = lang.substring(0,index);
		
		return new Locale(lang);
	}
	
	private void sortExternalParticipantsAndResources(Participant[] participants, StringBuffer participantsList, StringBuffer resourcesList, Map<Locale, List<EmailableParticipant>> receivers, SessionObject sessionObj,Map<String,EmailableParticipant> all) {
		if(participants == null) {
			return ;
		}
		Context ctx = sessionObj.getContext();
		for(Participant participant : participants) {					
			switch(participant.getType()) {
			case Participant.USER:
				break;
			case Participant.EXTERNAL_USER :
				EmailableParticipant p = getExternalParticipant(participant);
				if(p != null)
					addSingleParticipant(p, participantsList, sessionObj, receivers,all,false);
				
				break;
			case Participant.RESOURCE :
				p = getResourceParticipant(participant,ctx);
				if(p != null) {
					addSingleParticipant(p, participantsList, sessionObj, receivers, all,true);
				}
				resourcesList.append(p.displayName);
				resourcesList.append("\n");
				break;
			case Participant.GROUP : 
			break;
			default:
				throw new IllegalArgumentException("Unknown Participant Type: "+participant.getType());
			}
		}
	}
	
	private void sortParticipants(Participant[] participants, StringBuffer participantsList, StringBuffer resourcesList, Map<Locale, List<EmailableParticipant>> receivers, SessionObject sessionObj, Map<String,EmailableParticipant> all) {
		if(participants == null) {
			return ;
		}
		Context ctx = sessionObj.getContext();
		for(Participant participant : participants) {					
			switch(participant.getType()) {
			case Participant.USER:
				EmailableParticipant p = getUserParticipant(participant, ctx);
				if(p != null)
					addSingleParticipant(p, participantsList, sessionObj, receivers,all,false);
				break;
			case Participant.EXTERNAL_USER :
				p = getExternalParticipant(participant);
				if(p != null)
					addSingleParticipant(p, participantsList, sessionObj, receivers,all,false);
				
				break;
			case Participant.RESOURCE : 
				p = getResourceParticipant(participant,ctx);
				if(p != null) {
					addSingleParticipant(p, participantsList, sessionObj, receivers, all,true);
				}
				resourcesList.append(p.displayName);
				resourcesList.append("\n");
				break;
			case Participant.GROUP : 
				try {
					//FIXME 101 SELECT problem
					Group group = resolveGroups(ctx, participant.getIdentifier())[0];
					int[] members = group.getMember();
					User[] memberObjects = resolveUsers(ctx , members);
					for(User user : memberObjects) {
							
						String lang = user.getPreferredLanguage();
						int[] groups = user.getGroups();
						TimeZone tz = TimeZone.getTimeZone(user.getTimeZone());
							
						if(user.getMail() != null) {
							p = new EmailableParticipant(
								Participant.USER,
								user.getId(),
								groups,
								user.getMail(),
								user.getDisplayName(),
								getLocale(lang),
								tz,
								10,
								-1
							);
							addSingleParticipant(p,participantsList,sessionObj,receivers,all,false);
						}
						}
					} catch (LdapException e) {
						LOG.debug(e);
					}
				break;
			default:
				throw new IllegalArgumentException("Unknown Participant Type: "+participant.getType());
			}
		}
	}
	
	private EmailableParticipant getExternalParticipant(Participant participant) {
		if(null == participant.getEmailAddress())
			return null;
		return new EmailableParticipant(
				participant.getType(),
				-1,
				new int[0],
				participant.getEmailAddress(),
				participant.getDisplayName(),
				Locale.getDefault(),
				TimeZone.getDefault(),
				0,
				-1
			);
	}

	private EmailableParticipant getUserParticipant(Participant participant, Context ctx) {
		String lang  = null;
		int[] groups = null;
		TimeZone tz = null;
		String mail = null;
		String displayName = null;
		int folderId = -1;
		try {
			User user = resolveUsers(ctx,participant.getIdentifier())[0];
			lang = user.getPreferredLanguage();
			mail = user.getMail();
			if(mail == null) mail = participant.getEmailAddress();
			displayName = user.getDisplayName();
			if(displayName == null) displayName = participant.getDisplayName();
			groups = user.getGroups();
			tz = TimeZone.getTimeZone(user.getTimeZone());
			if (participant instanceof UserParticipant) {
				UserParticipant userParticipant = (UserParticipant) participant;
				folderId = userParticipant.getPersonalFolderId();
				//System.out.println("PERSONAL FOLDER ID FOR PARTICIPANT "+userParticipant.getIdentifier()+": "+folderId);
			}
		} catch (LdapException e) {
			LOG.debug(e);
		}
		
		Locale locale = getLocale(lang);
		
		EmailableParticipant p;
		if(mail != null) {
			p = new EmailableParticipant(
				participant.getType(),
				participant.getIdentifier(),
				groups,
				mail,
				displayName,
				locale,
				tz,
				10,
				folderId
			);
			return p;
		}
		return null;
	}
	
	private EmailableParticipant getResourceParticipant(Participant participant, Context ctx) {
		int[] groups = new int[0];
		String mail = null;
		String displayName = null;
		try {
			Resource resource = resolveResources(ctx,participant.getIdentifier())[0];
			mail = resource.getMail();
			if(mail == null) mail = participant.getEmailAddress();
			displayName = resource.getDisplayName();
			if(displayName == null) displayName = participant.getDisplayName();
		} catch (LdapException e) {
			LOG.debug(e);
		}
				
		EmailableParticipant p;
		if(mail != null) {
			p = new EmailableParticipant(
				participant.getType(),
				participant.getIdentifier(),
				groups,
				mail,
				displayName,
				Locale.getDefault(),
				TimeZone.getDefault(),
				-1,
				MailObject.DONT_SET
			);
			return p;
		}
		return null;
	}

	private void sortUserParticipants(UserParticipant[] participants, StringBuffer participantsList, Map<Locale, List<EmailableParticipant>> receivers, SessionObject sessionObj, Map<String,EmailableParticipant> all) {
		if(participants == null) {
			return ;
		}
		Context ctx = sessionObj.getContext();
		for(Participant participant : participants) {					
			EmailableParticipant p = getUserParticipant(participant, ctx);
			if(p != null)
				addSingleParticipant(p, participantsList, sessionObj, receivers,all,false);
		}
	}

	private void addSingleParticipant(EmailableParticipant participant, StringBuffer participantsList, SessionObject sessionObj, Map<Locale,List<EmailableParticipant>> receivers, Map<String, EmailableParticipant> all, boolean /* HACK */ resource) {
		
		boolean onlyAddToLocaleList = false;
		
		if(all.containsKey(participant.email)){
			EmailableParticipant other = all.get(participant.email);
			if(other.reliability < participant.reliability) {
				if(other.locale.equals(participant.locale)) {
					other.copy(participant);
					return;
				} else {
					List<EmailableParticipant> p = receivers.get(other.locale);
					p.remove(p.indexOf(other));
					onlyAddToLocaleList = true;
				}
			}
			return;
		}
		Locale l = null;
		
		l = participant.locale;
		
		List<EmailableParticipant> p = receivers.get(l);
		if(p == null) {
			p = new ArrayList<EmailableParticipant>();
			receivers.put(l,p);
		}
		
		all.put(participant.email, participant);
		p.add(participant);
		
		if(onlyAddToLocaleList)
			return;
		
		if(resource)
			return;
		participantsList.append(participant.displayName);
		participantsList.append("\n");
		
		
		
	}
	
	private static final class EmailableParticipant {
		public String email;
		public String displayName;
		public Locale locale;
		public int type;
		public int id;
		public int[] groups;
		public TimeZone timeZone;
		public int reliability = 0;
		public int folderId;
		
		public EmailableParticipant(int type, int id, int[] groups, String email, String displayName, Locale locale, TimeZone timeZone, int reliability, int folderId) {
			this.type = type;
			this.email = email;
			this.displayName = displayName;
			this.locale = locale;
			this.id = id;
			this.groups = groups;
			this.timeZone = timeZone;
			this.reliability = reliability;
			this.folderId = folderId;
		}
		
		public void copy(EmailableParticipant participant) {
			this.type = participant.type;
			this.email = participant.email;
			this.displayName = participant.displayName;
			this.locale = participant.locale;
			this.id = participant.id;
			this.groups = participant.groups;
			this.timeZone = participant.timeZone;
			this.reliability = participant.reliability;
		}

		public int hashCode(){
			return email.hashCode();
		}
		
		public boolean equals(Object o) {
			if (o instanceof EmailableParticipant) {
				EmailableParticipant other = (EmailableParticipant) o;
				return other.email.equals(email);
			}
			return false;
		}
		
	}
	
	private static class MailMessage {
		public String message;
		public String title;
		public List<String> addresses = new ArrayList<String>();
		public int folderId;
	}
	
	// Special handling for Appointments or Tasks goes here
	static interface State {
		public boolean sendMail(UserConfiguration userConfig);
		public void addSpecial(CalendarObject obj, Map<String,String> subst);
		public int getModule();
	}
	
	private static class AppointmentState implements State {

		public boolean sendMail(UserConfiguration userConfig) {
			return userConfig.getUserSettingMail().isNotifyAppointments();
		}

		public void addSpecial(CalendarObject obj, Map<String, String> subst) {
			AppointmentObject appointmentObj = (AppointmentObject) obj;
			subst.put("location", appointmentObj.getLocation());
		}
		
		public int getModule(){
			return Types.APPOINTMENT;
		}
	}
	
	private static class TaskState implements State {

		public boolean sendMail(UserConfiguration userConfig) {
			return userConfig.getUserSettingMail().isNotifyTasks();
		}

		public void addSpecial(CalendarObject obj, Map<String, String> subst) {
			//Task task = (Task) obj;
		}
		
		public int getModule(){
			return Types.TASK;
		}
		
	}
}

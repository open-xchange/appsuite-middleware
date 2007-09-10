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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.event.AppointmentEvent;
import com.openexchange.event.TaskEvent;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.RdbUserConfigurationStorage;
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
import com.openexchange.imap.UserSettingMail;
import com.openexchange.imap.UserSettingMailStorage;
import com.openexchange.server.DBPoolingException;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.exceptions.LoggingLogic;

import com.openexchange.groupware.notify.NotificationConfig.NotificationProperty;

public class ParticipantNotify implements AppointmentEvent, TaskEvent {
	
	//TODO: Signatur?
	//TODO: Abgesagt / Zugesagt
	
	//TODO: user getLocale of user instead of detemining locale yourself

	private final static Log LOG = LogFactory.getLog(ParticipantNotify.class);
	private final static LoggingLogic LL = LoggingLogic.getLoggingLogic(ParticipantNotify.class);
	
	public ParticipantNotify() {
	}

	protected void sendMessage(final String messageTitle, final String message, final List<String> name, final SessionObject session, final CalendarObject obj, int folderId, final State state, final boolean suppressOXReminderHeader) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Sending message to: "+name);
			LOG.debug("=====["+messageTitle+"]====\n\n");
			LOG.debug(message);
			LOG.debug("\n\n============");
		}
		if(folderId == -1) {
			folderId = obj.getParentFolderID();
		}
		
		if(suppressOXReminderHeader) {
			folderId = MailObject.DONT_SET;
		}
		
		final MailObject mail = new MailObject(session, obj.getObjectID(), folderId, state.getModule());
		mail.setFromAddr(session.getUserObject().getMail());
		mail.setToAddrs(name.toArray(new String[name.size()]));
		mail.setText(message);
		mail.setSubject(messageTitle);
		mail.setContentType("text/plain; charset=UTF-8");
		
		
		//System.out.println(folderId);
		try {
			mail.send();
		} catch (final OXException e) {
			LL.log(e);
		}
	}
	
	// Override for testing
	
	protected User[] resolveUsers(final Context ctx, final int...ids) throws LdapException {
		final UserStorage users = UserStorage.getInstance(ctx);
		final User[] r = new User[ids.length];
		int i = 0;
		for(final int id : ids) {
			r[i++] = users.getUser(id); // FIXME
		}
		return r;
	}
	
	protected Group[] resolveGroups(final Context ctx, final int...ids) throws LdapException {
		final GroupStorage groups = GroupStorage.getInstance(ctx);
		final Group[] r = new Group[ids.length];
		int i = 0;
		for(final int id : ids) {
			r[i++] = groups.getGroup(id);
		}
		return r;
	}
	
	protected Resource[] resolveResources(final Context ctx, final int...ids) throws LdapException {
		final ResourceStorage resources = ResourceStorage.getInstance(ctx);
		final Resource[] r = new Resource[ids.length];
		int i = 0;
		for(final int id : ids) {
			r[i++] = resources.getResource(id);
		}
		return r;
	}
	
	protected UserConfiguration getUserConfiguration(final int id, final int[] groups, final Context context) throws SQLException, LdapException, DBPoolingException, OXException {
		return RdbUserConfigurationStorage.loadUserConfiguration(id,groups,context);
	}
	
	protected UserSettingMail getUserSettingMail(final int id, final Context context) throws OXException {
		return UserSettingMailStorage.getInstance().loadUserSettingMail(id, context);
	}
	
	public void appointmentCreated(final AppointmentObject appointmentObj,
			final SessionObject sessionObj) {
		sendNotification(appointmentObj, sessionObj, Notifications.APPOINTMENT_CREATE_MAIL,Notifications.APPOINTMENT_CREATE_TITLE, new AppointmentState(),false, false);
	}

	public void appointmentModified(final AppointmentObject appointmentObj,
			final SessionObject sessionObj) {
		sendNotification(appointmentObj, sessionObj, Notifications.APPOINTMENT_UPDATE_MAIL,Notifications.APPOINTMENT_UPDATE_TITLE, new AppointmentState(), false, false);
	}

	public void appointmentDeleted(final AppointmentObject appointmentObj,
			final SessionObject sessionObj) {
		sendNotification(appointmentObj, sessionObj, Notifications.APPOINTMENT_DELETE_MAIL,Notifications.APPOINTMENT_DELETE_TITLE, new AppointmentState(), NotificationConfig.getPropertyAsBoolean(NotificationProperty.NOTIFY_ON_DELETE, false), true);
	}
	
	public void taskCreated(final Task taskObj, final SessionObject sessionObj) {
		sendNotification(taskObj, sessionObj, Notifications.TASK_CREATE_MAIL,Notifications.TASK_CREATE_TITLE, new TaskState(),false, false);	
	}

	public void taskModified(final Task taskObj, final SessionObject sessionObj) {
		sendNotification(taskObj, sessionObj, Notifications.TASK_UPDATE_MAIL,Notifications.TASK_UPDATE_TITLE, new TaskState(), false, false);
		
	}

	public void taskDeleted(final Task taskObj, final SessionObject sessionObj) {
		sendNotification(taskObj, sessionObj, Notifications.TASK_DELETE_MAIL,Notifications.TASK_DELETE_TITLE, new TaskState(),NotificationConfig.getPropertyAsBoolean(NotificationProperty.NOTIFY_ON_DELETE, false), true);
	}
	
	private void sendNotification(final CalendarObject obj, final SessionObject sessionObj, final String msgKey, final String titleKey, final State state, final boolean forceNotifyOthers, final boolean suppressOXReminderHeader) {
		if(!obj.getNotification() && obj.getCreatedBy() == sessionObj.getUserObject().getId() && !forceNotifyOthers) {
			return;
		}
		if(obj.getParticipants() == null) {
			return;
		}
		final SortedSet<String> participantSet = new TreeSet<String>();
		final SortedSet<String> resourceSet = new TreeSet<String>();
		
		final Map<Locale,List<EmailableParticipant>> receivers = new HashMap<Locale, List<EmailableParticipant>>();
		
		final Map<String,EmailableParticipant> all = new HashMap<String,EmailableParticipant>();
		final UserParticipant[] users = obj.getUsers();
		if(null == users) {
			sortParticipants(obj.getParticipants(), participantSet, resourceSet, receivers, sessionObj, all);
		} else {
			sortUserParticipants(obj.getUsers(), participantSet, receivers, sessionObj,all);
			sortExternalParticipantsAndResources(obj.getParticipants(),participantSet,resourceSet,receivers, sessionObj,all);
		}
		
		String createdByDisplayName = "UNKNOWN";
		String modifiedByDisplayName = "UNKNOWN";
		try {
			final Context ctx = sessionObj.getContext();
			if(0 != obj.getCreatedBy()) {
				createdByDisplayName = resolveUsers(ctx,obj.getCreatedBy())[0].getDisplayName();
			}
			if(0 != obj.getModifiedBy()) {
				modifiedByDisplayName = resolveUsers(ctx,obj.getModifiedBy())[0].getDisplayName();
			}
		} catch (final LdapException e) {
			createdByDisplayName = e.toString();
			modifiedByDisplayName = e.toString();
			LL.log(e);
		}
		
		final List<MailMessage> messages = new ArrayList<MailMessage>();
		for(final Locale locale : receivers.keySet()) {
			
			final StringHelper strings = new StringHelper(locale);
			final Template createTemplate = new StringTemplate(strings.getString(msgKey));
			
			DateFormat df = state.getDateFormat(locale);
			final List<EmailableParticipant> participants = receivers.get(locale);
			
			for(final EmailableParticipant p : participants) {
				TimeZone tz = TimeZone.getDefault();
				boolean sendMail = true;
				
				if(p.type != Participant.EXTERNAL_USER) {
					try {
						final UserSettingMail userSettingMail = getUserSettingMail(p.id, sessionObj.getContext());
						sendMail = state.sendMail(userSettingMail) && obj.getModifiedBy() != p.id && (obj.getNotification() || p.id == obj.getCreatedBy() || forceNotifyOthers);
						tz = p.timeZone;
					} catch (final AbstractOXException e) {
						LL.log(e);
					}
				} else {
					sendMail = obj.getNotification() || (obj.getModifiedBy() != p.id && forceNotifyOthers);
				}
				
				if(sendMail) {
					df.setTimeZone(tz);

					final Map<String,String> m = m(
						"start" 	,	(null == obj.getStartDate()) ? "" : df.format(obj.getStartDate()),
						"end"		,	(null == obj.getEndDate()) ? "" : df.format(obj.getEndDate()),
						"title"		,	(null == obj.getTitle()) ? "" : obj.getTitle(),
						"participants",		list(participantSet),
						"resources"	,	(resourceSet.size() > 0) ? list(resourceSet) : strings.getString(Notifications.NO_RESOURCES),
						"created_by",		createdByDisplayName,
						"changed_by",		modifiedByDisplayName,
						"description",		(null == obj.getNote()) ? "" : obj.getNote()
					);
							
					state.addSpecial(obj,m,p);
							
					
					final MailMessage msg = new MailMessage();
					msg.message = createTemplate.render(m);
					msg.title = strings.getString(titleKey)+": "+m.get("title");
					msg.addresses.add(p.email);
					msg.folderId = p.folderId;
					messages.add(msg);
				}
			}
		}
		
		for(final MailMessage mmsg : messages) {
			sendMessage(mmsg.title, mmsg.message, mmsg.addresses, sessionObj, obj, mmsg.folderId, state, suppressOXReminderHeader);
		}
		
	}

	private String list(final SortedSet<String> sSet) {
		final StringBuilder b = new StringBuilder();
		for(final String s : sSet) { b.append(s).append('\n'); }
		return b.toString();
	}

	private Map<String, String> m(final String...args) {
		if(args.length % 2 != 0) {
			throw new IllegalArgumentException("Length must be even");
		}
		
		final Map<String,String> retval = new HashMap<String, String>();
		
		for(int i = 0; i < args.length; i++) {
			retval.put(args[i], args[++i]);
		}
		return retval;
	}

	private Locale getLocale(String lang) {
		final int index = lang.indexOf('_');
		if(index != -1) {
			lang = lang.substring(0,index);
		}
		
		return new Locale(lang);
	}
	
	private void sortExternalParticipantsAndResources(final Participant[] participants, final Set<String> participantSet, final Set<String> resourceSet, final Map<Locale, List<EmailableParticipant>> receivers, final SessionObject sessionObj,final Map<String,EmailableParticipant> all) {
		if(participants == null) {
			return ;
		}
		final Context ctx = sessionObj.getContext();
		for(final Participant participant : participants) {					
			switch(participant.getType()) {
			case Participant.USER:
				break;
			case Participant.EXTERNAL_USER :
				EmailableParticipant p = getExternalParticipant(participant);
				if(p != null) {
					addSingleParticipant(p, participantSet, sessionObj, receivers,all,false);
				}
				
				break;
			case Participant.RESOURCE :
				p = getResourceParticipant(participant,ctx);
				if(p == null) {
					// Might be user added as resource (!)
					p = getUserParticipant(participant, ctx);
				}
				if(p != null) {
					addSingleParticipant(p, participantSet, sessionObj, receivers, all,true);
					resourceSet.add(p.displayName);
				}
				break;
			case Participant.GROUP : 
			break;
			default:
				throw new IllegalArgumentException("Unknown Participant Type: "+participant.getType());
			}
		}
	}
	
	private void sortParticipants(final Participant[] participants, final Set<String> participantSet, final Set<String> resourceSet, final Map<Locale, List<EmailableParticipant>> receivers, final SessionObject sessionObj, final Map<String,EmailableParticipant> all) {
		if(participants == null) {
			return ;
		}
		final Context ctx = sessionObj.getContext();
		for(final Participant participant : participants) {					
			switch(participant.getType()) {
			case Participant.USER:
				EmailableParticipant p = getUserParticipant(participant, ctx);
				if(p != null) {
					addSingleParticipant(p, participantSet, sessionObj, receivers,all,false);
				}
				break;
			case Participant.EXTERNAL_USER :
				p = getExternalParticipant(participant);
				if(p != null) {
					addSingleParticipant(p, participantSet, sessionObj, receivers,all,false);
				}
				
				break;
			case Participant.RESOURCE : 
				p = getResourceParticipant(participant,ctx);
				if(p != null) {
					addSingleParticipant(p, participantSet, sessionObj, receivers, all,true);
					resourceSet.add(p.displayName);
				}
				break;
			case Participant.GROUP : 
				try {
					//FIXME 101 SELECT problem
					final Group group = resolveGroups(ctx, participant.getIdentifier())[0];
					final int[] members = group.getMember();
					final User[] memberObjects = resolveUsers(ctx , members);
					for(final User user : memberObjects) {
							
						final String lang = user.getPreferredLanguage();
						final int[] groups = user.getGroups();
						final TimeZone tz = TimeZone.getTimeZone(user.getTimeZone());
							
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
							addSingleParticipant(p,participantSet,sessionObj,receivers,all,false);
						}
						}
					} catch (final LdapException e) {
						LL.log(e);
					}
				break;
			default:
				throw new IllegalArgumentException("Unknown Participant Type: "+participant.getType());
			}
		}
	}
	
	private EmailableParticipant getExternalParticipant(final Participant participant) {
		if(null == participant.getEmailAddress()) {
			return null;
		}
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

	private EmailableParticipant getUserParticipant(final Participant participant, final Context ctx) {
		String lang  = null;
		int[] groups = null;
		TimeZone tz = null;
		String mail = null;
		String displayName = null;
		int folderId = -1;
		try {
			final User user = resolveUsers(ctx,participant.getIdentifier())[0];
			lang = user.getPreferredLanguage();
			mail = user.getMail();
			if(mail == null) {
				mail = participant.getEmailAddress();
			}
			displayName = user.getDisplayName();
			if(displayName == null) {
				displayName = participant.getDisplayName();
			}
			groups = user.getGroups();
			tz = TimeZone.getTimeZone(user.getTimeZone());
			if (participant instanceof UserParticipant) {
				final UserParticipant userParticipant = (UserParticipant) participant;
				folderId = userParticipant.getPersonalFolderId();
				//System.out.println("PERSONAL FOLDER ID FOR PARTICIPANT "+userParticipant.getIdentifier()+": "+folderId);
			}
		} catch (final LdapException e) {
			LL.log(e);
		}
		
		final Locale locale = getLocale(lang);
		
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
	
	private EmailableParticipant getResourceParticipant(final Participant participant, final Context ctx) {
		final int[] groups = new int[0];
		String mail = null;
		String displayName = null;
		try {
			final Resource resource = resolveResources(ctx,participant.getIdentifier())[0];
			mail = resource.getMail();
			if(mail == null) {
				mail = participant.getEmailAddress();
			}
			displayName = resource.getDisplayName();
			if(displayName == null) {
				displayName = participant.getDisplayName();
			}
		} catch (final LdapException e) {
			LL.log(e);
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

	private void sortUserParticipants(final UserParticipant[] participants, final Set<String> participantSet, final Map<Locale, List<EmailableParticipant>> receivers, final SessionObject sessionObj, final Map<String,EmailableParticipant> all) {
		if(participants == null) {
			return ;
		}
		final Context ctx = sessionObj.getContext();
		for(final Participant participant : participants) {					
			final EmailableParticipant p = getUserParticipant(participant, ctx);
			if(p != null) {
				addSingleParticipant(p, participantSet, sessionObj, receivers,all,false);
			}
		}
	}

	private void addSingleParticipant(final EmailableParticipant participant, final Set<String> participantSet, final SessionObject sessionObj, final Map<Locale,List<EmailableParticipant>> receivers, final Map<String, EmailableParticipant> all, final boolean /* HACK */ resource) {
		
		boolean onlyAddToLocaleList = false;
		
		if(all.containsKey(participant.email)){
			final EmailableParticipant other = all.get(participant.email);
			if(other.reliability < participant.reliability) {
				if(other.locale.equals(participant.locale)) {
					other.copy(participant);
					return;
				}
				final List<EmailableParticipant> p = receivers.get(other.locale);
				p.remove(p.indexOf(other));
				onlyAddToLocaleList = true;
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
		
		if(onlyAddToLocaleList) {
			return;
		}
		
		if(resource) {
			return;
		}
		participantSet.add(participant.displayName);
		
	}
	
	public static final class EmailableParticipant {
		public String email;
		public String displayName;
		public Locale locale;
		public int type;
		public int id;
		public int[] groups;
		public TimeZone timeZone;
		public int reliability;
		public int folderId;
		
		public EmailableParticipant(final int type, final int id, final int[] groups, final String email, final String displayName, final Locale locale, final TimeZone timeZone, final int reliability, final int folderId) {
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
		
		public void copy(final EmailableParticipant participant) {
			this.type = participant.type;
			this.email = participant.email;
			this.displayName = participant.displayName;
			this.locale = participant.locale;
			this.id = participant.id;
			this.groups = participant.groups;
			this.timeZone = participant.timeZone;
			this.reliability = participant.reliability;
		}

		@Override
		public int hashCode(){
			return email.hashCode();
		}
		
		@Override
		public boolean equals(final Object o) {
			if (o instanceof EmailableParticipant) {
				final EmailableParticipant other = (EmailableParticipant) o;
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
		public boolean sendMail(UserSettingMail userSettingMail);
		public DateFormat getDateFormat(Locale locale);
		public void addSpecial(CalendarObject obj, Map<String,String> subst, EmailableParticipant p);
		public int getModule();
	}
	
	public static abstract class LinkableState implements State {
		
		protected static Template object_link_template;
		private static String hostname;
		private static UnknownHostException warnSpam;
		static {
			try {
				hostname = InetAddress.getLocalHost().getCanonicalHostName();
			} catch (UnknownHostException e) {
				hostname = "localhost";
				warnSpam = e;
			}
		}
		
		public void addSpecial(final CalendarObject obj, final Map<String, String> subst, final EmailableParticipant p){
			subst.put("link", generateLink(obj, p));
		}

		public String generateLink(final CalendarObject obj, final EmailableParticipant p) {
			if(object_link_template == null) {
				loadTemplate();
			}
			
			if(warnSpam != null) {
				LOG.error("Can't resolve my own hostname, using 'localhost' instead, which is certainly not what you want.!", warnSpam);
			}
			
			final Map<String, String> subst = new HashMap<String,String>();
			switch(getModule()) {
			case Types.APPOINTMENT : subst.put("module", "calendar"); break;
			case Types.TASK : subst.put("module", "task"); break;
			default : subst.put("module", "unknown"); break;
			}
			
			int folder = p.folderId;
			if(folder == -1)
				folder = obj.getParentFolderID();
			
			subst.put("folder", String.valueOf(folder));
			subst.put("object", String.valueOf(obj.getObjectID()));
			subst.put("hostname", hostname);
			
			return object_link_template.render(subst);
		}
		
		public void loadTemplate(){
			synchronized (LinkableState.class) {
				object_link_template = new StringTemplate(NotificationConfig.getProperty(NotificationProperty.OBJECT_LINK, ""));
			}
		}

	}
	
	public static class AppointmentState extends LinkableState {

		public boolean sendMail(final UserSettingMail userSettingMail) {
			return userSettingMail.isNotifyAppointments();
		}
		
		@Override
		public void addSpecial(final CalendarObject obj, final Map<String, String> subst, final EmailableParticipant p) {
			super.addSpecial(obj, subst, p);
			final AppointmentObject appointmentObj = (AppointmentObject) obj;
			subst.put("location", appointmentObj.getLocation());
		}
		
		public int getModule(){
			return Types.APPOINTMENT;
		}

		public DateFormat getDateFormat(Locale locale) {
			return tryAppendingTimeZone(DateFormat.getDateTimeInstance(DateFormat.DEFAULT,DateFormat.DEFAULT,locale));
		}
		
		private DateFormat tryAppendingTimeZone(final DateFormat df) {
			if (df instanceof SimpleDateFormat) {
				final SimpleDateFormat sdf = (SimpleDateFormat) df;
				final String format = sdf.toPattern();
				return new SimpleDateFormat(format+", z");
			}
			return df;
		}

	}
	
	public static class TaskState extends LinkableState {

		public boolean sendMail(final UserSettingMail userSettingMail) {
			return userSettingMail.isNotifyTasks();
		}
		
		public int getModule(){
			return Types.TASK;
		}

		public DateFormat getDateFormat(Locale locale) {
			return DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
		}
		
	}
}

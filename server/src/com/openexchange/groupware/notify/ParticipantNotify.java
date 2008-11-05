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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
import com.openexchange.event.impl.AppointmentEventInterface2;
import com.openexchange.event.impl.TaskEventInterface2;
import com.openexchange.group.Group;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.Tools;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.container.mail.MailObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.i18n.Notifications;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.notify.NotificationConfig.NotificationProperty;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.userconfiguration.RdbUserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.i18n.tools.RenderMap;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.i18n.tools.StringTemplate;
import com.openexchange.i18n.tools.Template;
import com.openexchange.i18n.tools.TemplateReplacement;
import com.openexchange.i18n.tools.TemplateToken;
import com.openexchange.i18n.tools.replacement.AppointmentActionReplacement;
import com.openexchange.i18n.tools.replacement.ChangeExceptionsReplacement;
import com.openexchange.i18n.tools.replacement.ConfirmationActionReplacement;
import com.openexchange.i18n.tools.replacement.CreationDateReplacement;
import com.openexchange.i18n.tools.replacement.DeleteExceptionsReplacement;
import com.openexchange.i18n.tools.replacement.EndDateReplacement;
import com.openexchange.i18n.tools.replacement.FormatLocalizedStringReplacement;
import com.openexchange.i18n.tools.replacement.ParticipantsReplacement;
import com.openexchange.i18n.tools.replacement.ResourcesReplacement;
import com.openexchange.i18n.tools.replacement.SeriesReplacement;
import com.openexchange.i18n.tools.replacement.StartDateReplacement;
import com.openexchange.i18n.tools.replacement.StringReplacement;
import com.openexchange.i18n.tools.replacement.TaskActionReplacement;
import com.openexchange.i18n.tools.replacement.TaskPriorityReplacement;
import com.openexchange.i18n.tools.replacement.TaskStatusReplacement;
import com.openexchange.mail.MailException;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.resource.Resource;
import com.openexchange.resource.storage.ResourceStorage;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.session.Session;
import com.openexchange.tools.exceptions.LoggingLogic;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

public class ParticipantNotify implements AppointmentEventInterface2, TaskEventInterface2 {

	// TODO: Signature?

	private static final String STR_UNKNOWN = "UNKNOWN";

	private final static Log LOG = LogFactory.getLog(ParticipantNotify.class);

	private final static LoggingLogic LL = LoggingLogic.getLoggingLogic(ParticipantNotify.class);

	public static ParticipantNotify messageSender = new ParticipantNotify();

	/**
	 * Initializes a new {@link ParticipantNotify}
	 */
	public ParticipantNotify() {
		super();
	}

	/**
	 * Sends specified message
	 * 
	 * @param mmsg
	 *            The message
	 * @param session
	 *            The session
	 * @param obj
	 *            The calendar object
	 * @param state
	 *            The state
	 */
	protected static void sendMessage(final MailMessage mmsg, final ServerSession session, final CalendarObject obj,
			final State state) {
		messageSender.sendMessage(mmsg.title, mmsg.message, mmsg.addresses, session, obj, mmsg.folderId, state, false,
				mmsg.internal);
	}

	protected void sendMessage(final String messageTitle, final String message, final List<String> name,
			final ServerSession session, final CalendarObject obj, final int folderId, final State state,
			final boolean suppressOXReminderHeader, final boolean internal) {
		if (LOG.isDebugEnabled()) {
			LOG.debug(new StringBuilder(message.length() + 64).append("Sending message to: ").append(name).append(
					"\n=====[").append(messageTitle).append("]====\n\n").append(message).append("\n\n"));
		}
		int fuid = folderId;
		if (fuid == -1) {
			fuid = obj.getParentFolderID();
		}

		if (suppressOXReminderHeader) {
			fuid = MailObject.DONT_SET;
		}

		final MailObject mail = new MailObject(session, obj.getObjectID(), fuid, state.getModule(), state.getType()
				.toString());
		mail.setFromAddr(UserStorage.getStorageUser(session.getUserId(), session.getContext()).getMail());
		mail.setToAddrs(name.toArray(new String[name.size()]));
		mail.setText(message);
		mail.setSubject(messageTitle);
		mail.setContentType("text/plain; charset=UTF-8");

		if (internal) {
			state.modifyInternal(mail, obj, session);
		} else {
			state.modifyExternal(mail, obj, session);
		}

		// System.out.println(folderId);
		try {
			mail.send();
		} catch (final MailException e) {
			LL.log(e);
		}
	}

	// Override for testing

	protected User[] resolveUsers(final Context ctx, final int... ids) throws LdapException {
		final User[] r = new User[ids.length];
		for (int i = 0; i < ids.length; i++) {
			r[i] = UserStorage.getInstance().getUser(ids[i], ctx);
		}
		return r;
	}

	protected Group[] resolveGroups(final Context ctx, final int... ids) throws LdapException {
		final GroupStorage groups = GroupStorage.getInstance();
		final Group[] r = new Group[ids.length];
		int i = 0;
		for (final int id : ids) {
			r[i++] = groups.getGroup(id, ctx);
		}
		return r;
	}

	protected Resource[] resolveResources(final Context ctx, final int... ids) throws LdapException {
		final ResourceStorage resources = ResourceStorage.getInstance();
		final Resource[] r = new Resource[ids.length];
		int i = 0;
		for (final int id : ids) {
			r[i++] = resources.getResource(id, ctx);
		}
		return r;
	}

	protected UserConfiguration getUserConfiguration(final int id, final int[] groups, final Context context)
			throws SQLException, LdapException, DBPoolingException, OXException {
		return RdbUserConfigurationStorage.loadUserConfiguration(id, groups, context);
	}

	protected UserSettingMail getUserSettingMail(final int id, final Context context) throws OXException {
		return UserSettingMailStorage.getInstance().loadUserSettingMail(id, context);
	}

	public void appointmentCreated(final AppointmentObject appointmentObj, final Session sessionObj) {
		sendNotification(null, appointmentObj, sessionObj, new AppointmentState(new AppointmentActionReplacement(
				AppointmentActionReplacement.ACTION_NEW), Notifications.APPOINTMENT_CREATE_MAIL, State.Type.NEW),
				false, false, false);
	}

	public void appointmentModified(final AppointmentObject appointmentObj, final Session sessionObj) {
		sendNotification(null, appointmentObj, sessionObj, new AppointmentState(new AppointmentActionReplacement(
				AppointmentActionReplacement.ACTION_CHANGED), Notifications.APPOINTMENT_UPDATE_MAIL,
				State.Type.MODIFIED), false, false, true);
	}

	public void appointmentModified(final AppointmentObject oldAppointment, final AppointmentObject newAppointment,
			final Session sessionObj) {
		sendNotification(oldAppointment, newAppointment, sessionObj, new AppointmentState(
				new AppointmentActionReplacement(AppointmentActionReplacement.ACTION_CHANGED),
				Notifications.APPOINTMENT_UPDATE_MAIL, State.Type.MODIFIED), false, false, true);
	}

	public void appointmentAccepted(final AppointmentObject appointmentObj, final Session sessionObj) {
		sendNotification(null, appointmentObj, sessionObj, new AppointmentState(new AppointmentActionReplacement(
				AppointmentActionReplacement.ACTION_ACCEPTED), new ConfirmationActionReplacement(
				ConfirmationActionReplacement.ACTION_ACCEPTED), Notifications.APPOINTMENT_CONFIRMATION_MAIL,
				State.Type.ACCEPTED), false, false, false);
	}

	public void appointmentDeclined(final AppointmentObject appointmentObj, final Session sessionObj) {
		sendNotification(null, appointmentObj, sessionObj, new AppointmentState(new AppointmentActionReplacement(
				AppointmentActionReplacement.ACTION_DECLINED), new ConfirmationActionReplacement(
				ConfirmationActionReplacement.ACTION_DECLINED), Notifications.APPOINTMENT_CONFIRMATION_MAIL,
				State.Type.DECLINED), false, false, false);
	}

	public void appointmentTentativelyAccepted(final AppointmentObject appointmentObj, final Session sessionObj) {
		sendNotification(null, appointmentObj, sessionObj, new AppointmentState(new AppointmentActionReplacement(
				AppointmentActionReplacement.ACTION_TENTATIVE), new ConfirmationActionReplacement(
				ConfirmationActionReplacement.ACTION_TENTATIVELY_ACCEPTED),
				Notifications.APPOINTMENT_CONFIRMATION_MAIL, State.Type.TENTATIVELY_ACCEPTED), false, false, false);
	}

	public void appointmentDeleted(final AppointmentObject appointmentObj, final Session sessionObj) {
		/*
		 * Clear calendar object from notification pool
		 */
		NotificationPool.getInstance().removeByObject(appointmentObj.getObjectID(), sessionObj.getContextId());
		/*
		 * Send delete notification
		 */
		sendNotification(null, appointmentObj, sessionObj,
				new AppointmentState(new AppointmentActionReplacement(AppointmentActionReplacement.ACTION_DELETED),
						Notifications.APPOINTMENT_DELETE_MAIL, State.Type.DELETED), NotificationConfig
						.getPropertyAsBoolean(NotificationProperty.NOTIFY_ON_DELETE, false), true, false);
	}

	public void taskCreated(final Task taskObj, final Session sessionObj) {
		sendNotification(null, taskObj, sessionObj, new TaskState(new TaskActionReplacement(
				TaskActionReplacement.ACTION_NEW), Notifications.TASK_CREATE_MAIL, State.Type.NEW), false, false, false);
	}

	public void taskModified(final Task taskObj, final Session sessionObj) {
		sendNotification(null, taskObj, sessionObj, new TaskState(new TaskActionReplacement(
				TaskActionReplacement.ACTION_CHANGED), Notifications.TASK_UPDATE_MAIL, State.Type.MODIFIED), false,
				false, true);

	}

	public void taskModified(final Task oldTask, final Task newTask, final Session sessionObj) {
		sendNotification(oldTask, newTask, sessionObj, new TaskState(new TaskActionReplacement(
				TaskActionReplacement.ACTION_CHANGED), Notifications.TASK_UPDATE_MAIL, State.Type.MODIFIED), false,
				false, true);
	}

	public void taskAccepted(final Task taskObj, final Session sessionObj) {
		sendNotification(null, taskObj, sessionObj, new TaskState(new TaskActionReplacement(
				TaskActionReplacement.ACTION_ACCEPTED), new ConfirmationActionReplacement(
				ConfirmationActionReplacement.ACTION_ACCEPTED), Notifications.TASK_CONFIRMATION_MAIL,
				State.Type.ACCEPTED), false, false, false);
	}

	public void taskDeclined(final Task taskObj, final Session sessionObj) {
		sendNotification(null, taskObj, sessionObj, new TaskState(new TaskActionReplacement(
				TaskActionReplacement.ACTION_DECLINED), new ConfirmationActionReplacement(
				ConfirmationActionReplacement.ACTION_DECLINED), Notifications.TASK_CONFIRMATION_MAIL,
				State.Type.DECLINED), false, false, false);
	}

	public void taskTentativelyAccepted(final Task taskObj, final Session sessionObj) {
		sendNotification(null, taskObj, sessionObj, new TaskState(new TaskActionReplacement(
				TaskActionReplacement.ACTION_TENTATIVE), new ConfirmationActionReplacement(
				ConfirmationActionReplacement.ACTION_TENTATIVELY_ACCEPTED), Notifications.TASK_CONFIRMATION_MAIL,
				State.Type.TENTATIVELY_ACCEPTED), false, false, false);
	}

	public void taskDeleted(final Task taskObj, final Session sessionObj) {
		/*
		 * Clear calendar object from notification pool
		 */
		NotificationPool.getInstance().removeByObject(taskObj.getObjectID(), sessionObj.getContextId());
		/*
		 * Send delete notification
		 */
		sendNotification(null, taskObj, sessionObj, new TaskState(new TaskActionReplacement(
				TaskActionReplacement.ACTION_DELETED), Notifications.TASK_DELETE_MAIL, State.Type.DELETED),
				NotificationConfig.getPropertyAsBoolean(NotificationProperty.NOTIFY_ON_DELETE, false), true, false);
	}

	private void sendNotification(final CalendarObject oldObj, final CalendarObject newObj, final Session session,
			final State state, final boolean forceNotifyOthers, final boolean suppressOXReminderHeader,
			final boolean isUpdate) {

		final ServerSession sessionObj;
		try {
			sessionObj = new ServerSessionAdapter(session);
		} catch (final ContextException e) {
			LOG.error(e.getLocalizedMessage(), e);
			return;
		}
		/*
		 * Remember object's title
		 */
		final String title = newObj.getTitle() == null ? (oldObj == null ? "" : (oldObj.getTitle() == null ? ""
				: oldObj.getTitle())) : newObj.getTitle();
		/*
		 * Check if notification shall be dropped
		 */
		if (newObj.containsNotification() && !newObj.getNotification()
				&& newObj.getCreatedBy() == sessionObj.getUserId() && !forceNotifyOthers) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(new StringBuilder(256).append("Dropping notification for ").append(
						(state.getModule() == Types.APPOINTMENT ? "appointment " : "task ")).append(title).append(" (")
						.append(newObj.getObjectID()).append(") since it indicates to discard its notification")
						.toString());
			}
			return;
		}
		if (newObj.getParticipants() == null) {
			if (oldObj == null || oldObj.getParticipants() == null) {
				if (LOG.isDebugEnabled()) {
					final StringBuilder builder = new StringBuilder(256).append("Dropping notification for ").append(
							(state.getModule() == Types.APPOINTMENT ? "appointment " : "task ")).append(title).append(
							" (").append(newObj.getObjectID()).append(") since it contains NO participants");
					LOG.debug(builder.toString());
				}
				return;
			}
			/*
			 * Grab participants/users from old object
			 */
			newObj.setParticipants(oldObj.getParticipants());
			newObj.setUsers(oldObj.getUsers());
		}
		/*
		 * Ensure start/end is set
		 */
		if (newObj.getStartDate() == null && oldObj != null && oldObj.getStartDate() != null) {
			newObj.setStartDate(oldObj.getStartDate());
		}
		if (newObj.getEndDate() == null && oldObj != null && oldObj.getEndDate() != null) {
			newObj.setEndDate(oldObj.getEndDate());
		}
		if (!checkStartAndEndDate(newObj, state.getModule())) {
			return;
		}
		/*
		 * Ensure that important fields are set
		 */
		if (!newObj.containsCreatedBy() && oldObj != null && oldObj.containsCreatedBy()) {
			newObj.setCreatedBy(oldObj.getCreatedBy());
		}
		if (!newObj.containsCreationDate() && oldObj != null && oldObj.containsCreationDate()) {
			newObj.setCreationDate(oldObj.getCreationDate());
		}
		/*
		 * A map to remember receivers
		 */
		final Map<Locale, List<EmailableParticipant>> receivers = new HashMap<Locale, List<EmailableParticipant>>();
		/*
		 * Generate a render map filled with object-specific information
		 */
		final RenderMap renderMap = createRenderMap(newObj, oldObj, isUpdate, title, state.getModule(), receivers,
				sessionObj);
		/*
		 * Add confirmation action replacement to render map if non-null
		 */
		{
			final TemplateReplacement confirmActionRepl = state.getConfirmationAction();
			if (confirmActionRepl != null) {
				renderMap.put(confirmActionRepl);
			}
		}

		/*
		 * Create message list
		 */
		final List<MailMessage> messages = createMessageList(oldObj, newObj, state, forceNotifyOthers, isUpdate,
				sessionObj, receivers, title, renderMap);

		/*
		 * Send messages
		 */
		for (final MailMessage mmsg : messages) {
			sendMessage(mmsg.title, mmsg.message, mmsg.addresses, sessionObj, newObj, mmsg.folderId, state,
					suppressOXReminderHeader, mmsg.internal);
		}
	}

	private List<MailMessage> createMessageList(final CalendarObject oldObj, final CalendarObject newObj,
			final State state, final boolean forceNotifyOthers, final boolean isUpdate, final ServerSession sessionObj,
			final Map<Locale, List<EmailableParticipant>> receivers, final String title, final RenderMap renderMap) {
		final OXFolderAccess access = new OXFolderAccess(sessionObj.getContext());
		final StringBuilder b = new StringBuilder(2048);

		final List<MailMessage> messages = new ArrayList<MailMessage>();
		for (final Locale locale : receivers.keySet()) {
			/*
			 * Apply new locale to replacements
			 */
			final TemplateReplacement actionRepl = state.getAction();
			actionRepl.setLocale(locale);
			renderMap.applyLocale(locale);
			/*
			 * Iterate over locale's participants
			 */
			final List<EmailableParticipant> participants = receivers.get(locale);
			for (final EmailableParticipant p : participants) {
				TimeZone tz = TimeZone.getDefault();
				boolean sendMail = true;

				if (isUser(sessionObj.getContext(), p)) {
					try {
						final UserSettingMail userSettingMail = getUserSettingMail(p.id, sessionObj.getContext());
						sendMail = state.sendMail(userSettingMail)
								&& newObj.getModifiedBy() != p.id
								&& ((!newObj.containsNotification() || newObj.getNotification())
										|| p.id == newObj.getCreatedBy() || forceNotifyOthers);
						tz = p.timeZone;
					} catch (final AbstractOXException e) {
						LL.log(e);
					}
				} else {
					sendMail = (!newObj.containsNotification() || newObj.getNotification())
							|| (newObj.getModifiedBy() != p.id && forceNotifyOthers);
					if (p.timeZone != null) {
						tz = p.timeZone;
					}
				}

				if (sendMail) {
					/*
					 * Apply time zone
					 */
					renderMap.applyTimeZone(tz);

					/*
					 * Folder
					 */
					final int folderId = p.folderId > 0 ? p.folderId : newObj.getParentFolderID();
					if (folderId > 0) {
						String folderName = FolderObject.getFolderString(folderId, locale);
						if (folderName == null) {
							try {
								folderName = access.getFolderName(folderId);
							} catch (final OXException e) {
								LOG.error(e.getMessage(), e);
								folderName = "";
							}
						}
						final TemplateReplacement folderRepl = new FormatLocalizedStringReplacement(
								TemplateToken.FOLDER_NAME, Notifications.FORMAT_FOLDER, folderName);
						folderRepl.setLocale(locale);
						if (oldObj != null) {
							if (p.folderId > 0) {
								checkChangedFolder(oldObj, p.email, folderId, folderRepl, sessionObj);
							} else {
								folderRepl.setChanged(newObj.getParentFolderID() != oldObj.getParentFolderID());
							}
						}
						renderMap.put(folderRepl);
					}

					/*
					 * Special information(s)
					 */
					state.addSpecial(newObj, oldObj, renderMap, p);

					if (isUpdate && EmailableParticipant.STATE_NONE == p.state) {
						/*
						 * Add to pool
						 */
						NotificationPool.getInstance().put(
								new PooledNotification(p, title, state, locale, (RenderMap) renderMap.clone(),
										sessionObj, newObj));
						if (LOG.isDebugEnabled()) {
							LOG.debug(new StringBuilder(128).append(
									(Types.APPOINTMENT == state.getModule() ? "Appointment" : "Task")).append(
									" update (id = ").append(newObj.getObjectID()).append(
									") notification added to pool for receiver ").append(p.email).toString());
						}
					} else {
						/*
						 * Compose message
						 */
						messages.add(createParticipantMessage(p, title, actionRepl, state, locale, renderMap, isUpdate,
								b));
						if (LOG.isDebugEnabled()) {
							LOG.debug(new StringBuilder(128).append(
									(Types.APPOINTMENT == state.getModule() ? "Appointment" : "Task"))
									.append(" (id = ").append(newObj.getObjectID()).append(
											") notification message generated for receiver ").append(p.email)
									.toString());
						}
					}
				}
			}
		}
		return messages;
	}

	/**
	 * Creates a message for specified participant
	 * 
	 * @param p
	 *            The participant
	 * @param title
	 *            The object's title
	 * @param actionRepl
	 *            The action replacement to compose the message's title
	 * @param state
	 *            The object's state
	 * @param locale
	 *            The locale
	 * @param renderMap
	 *            The render map
	 * @param isUpdate
	 *            <code>true</code> if an update event triggered the
	 *            notification; otherwise <code>false</code>
	 * @param b
	 *            A string builder
	 * @return The created message
	 */
	protected static MailMessage createParticipantMessage(final EmailableParticipant p, final String title,
			final TemplateReplacement actionRepl, final State state, final Locale locale, final RenderMap renderMap,
			final boolean isUpdate, final StringBuilder b) {
		final MailMessage msg = new MailMessage();
		final Template createTemplate = state.getTemplate();
		final StringHelper strings = new StringHelper(locale);
		b.setLength(0);
		msg.title = b.append(actionRepl.getReplacement()).append(": ").append(title).toString();
		b.setLength(0);
		if (isUpdate) {
			if (EmailableParticipant.STATE_REMOVED == p.state) {
				/*
				 * Current participant is removed by caught update event
				 */
				/*
				 * Get cloned version of render map to apply changed status
				 */
				final RenderMap clone = clonedRenderMap(renderMap);
				if (Types.APPOINTMENT == state.getModule()) {
					msg.title = b.append(
							new AppointmentActionReplacement(AppointmentActionReplacement.ACTION_DELETED, locale)
									.getReplacement()).append(": ").append(title).toString();
					b.setLength(0);
					/*
					 * Render proper message for removed participant
					 */
					msg.message = new StringTemplate(strings.getString(Notifications.APPOINTMENT_DELETE_MAIL))
							.render(clone);
				} else {
					msg.title = b.append(
							new TaskActionReplacement(TaskActionReplacement.ACTION_DELETED, locale).getReplacement())
							.append(": ").append(title).toString();
					b.setLength(0);
					/*
					 * Render proper message for removed participant
					 */
					msg.message = new StringTemplate(strings.getString(Notifications.APPOINTMENT_DELETE_MAIL))
							.render(clone);

				}
				if (Participant.RESOURCE == p.type) {
					/*
					 * Special prefixes for resource participant receivers
					 */
					msg.message = b.append(
							String.format(strings.getString(Notifications.RESOURCE_PREFIX), p.displayName))
							.append(": ").append(msg.message).toString();
					b.setLength(0);
					msg.title = b.append('[').append(strings.getString(Notifications.RESOURCE_TITLE_PREFIX)).append(
							"] ").append(msg.title).toString();
					b.setLength(0);
				}
			} else if (EmailableParticipant.STATE_NEW == p.state) {
				/*
				 * Current participant is added by caught update event
				 */
				/*
				 * Get cloned version of render map to apply changed status
				 */
				final RenderMap clone = clonedRenderMap(renderMap);
				if (Types.APPOINTMENT == state.getModule()) {
					msg.title = b.append(
							new AppointmentActionReplacement(AppointmentActionReplacement.ACTION_NEW, locale)
									.getReplacement()).append(": ").append(title).toString();
					b.setLength(0);
					/*
					 * Render proper message for removed participant
					 */
					final String message = p.type == Participant.EXTERNAL_USER || p.type == Participant.RESOURCE ? Notifications.APPOINTMENT_CREATE_MAIL_EXT
							: Notifications.APPOINTMENT_CREATE_MAIL;
					msg.message = new StringTemplate(strings.getString(message)).render(clone);
				} else {
					msg.title = b.append(
							new TaskActionReplacement(TaskActionReplacement.ACTION_NEW, locale).getReplacement())
							.append(": ").append(title).toString();
					b.setLength(0);
					/*
					 * Render proper message for removed participant
					 */
					final String message = p.type == Participant.EXTERNAL_USER || p.type == Participant.RESOURCE ? Notifications.TASK_CREATE_MAIL_EXT
							: Notifications.TASK_CREATE_MAIL;
					msg.message = new StringTemplate(strings.getString(message)).render(clone);
				}
				if (Participant.RESOURCE == p.type) {
					/*
					 * Special prefixes for resource participant receivers
					 */
					msg.message = b.append(
							String.format(strings.getString(Notifications.RESOURCE_PREFIX), p.displayName))
							.append(": ").append(msg.message).toString();
					b.setLength(0);
					msg.title = b.append('[').append(strings.getString(Notifications.RESOURCE_TITLE_PREFIX)).append(
							"] ").append(msg.title).toString();
					b.setLength(0);
				}
			} else {
				if (p.type == Participant.EXTERNAL_USER || p.type == Participant.RESOURCE) {
					final String template = strings
							.getString(Types.APPOINTMENT == state.getModule() ? Notifications.APPOINTMENT_UPDATE_MAIL_EXT
									: Notifications.TASK_UPDATE_MAIL_EXT);
					msg.message = new StringTemplate(template).render(renderMap);
				} else {
					msg.message = createTemplate.render(renderMap);
				}
			}
		} else {
			if (State.Type.NEW.equals(state.getType())
					&& (p.type == Participant.EXTERNAL_USER || p.type == Participant.RESOURCE)) {
				final String template = strings
						.getString(Types.APPOINTMENT == state.getModule() ? Notifications.APPOINTMENT_CREATE_MAIL_EXT
								: Notifications.TASK_CREATE_MAIL_EXT);
				msg.message = new StringTemplate(template).render(renderMap);
			} else {
				msg.message = createTemplate.render(renderMap);
			}
		}
		if (Participant.RESOURCE == p.type) {
			/*
			 * Special prefixes for resource participant receivers
			 */
			msg.message = b.append(String.format(strings.getString(Notifications.RESOURCE_PREFIX), p.displayName))
					.append(": ").append(msg.message).toString();
			b.setLength(0);
			msg.title = b.append('[').append(strings.getString(Notifications.RESOURCE_TITLE_PREFIX)).append("] ")
					.append(msg.title).toString();
			b.setLength(0);
		}
		msg.addresses.add(p.email);
		msg.folderId = p.folderId;
		msg.internal = p.type != Participant.EXTERNAL_USER;
		return msg;
	}

	private RenderMap createRenderMap(final CalendarObject newObj, final CalendarObject oldObj, final boolean isUpdate,
			final String title, final int module, final Map<Locale, List<EmailableParticipant>> receivers,
			final ServerSession session) {
		/*
		 * Containers for traversed participants
		 */
		final SortedSet<EmailableParticipant> participantSet = new TreeSet<EmailableParticipant>();
		final SortedSet<EmailableParticipant> resourceSet = new TreeSet<EmailableParticipant>();
		final Map<String, EmailableParticipant> all = new HashMap<String, EmailableParticipant>();
		/*
		 * Traverse participants and fill containers
		 */
		final UserParticipant[] users = newObj.getUsers();
		if (null == users) {
			Participant[] oldParticipants = new Participant[0];
			if (oldObj != null) {
				oldParticipants = oldObj.getParticipants();
			}
			sortParticipants(oldParticipants, newObj.getParticipants(), participantSet, resourceSet, receivers,
					session, all);
		} else {
			UserParticipant[] oldUsers = new UserParticipant[0];
			if (oldObj != null) {
				oldUsers = oldObj.getUsers();
			}
			Participant[] oldParticipants = new Participant[0];
			if (oldObj != null) {
				oldParticipants = oldObj.getParticipants();
			}

			sortUserParticipants(oldUsers, newObj.getUsers(), participantSet, isUpdate, receivers, session, all);
			sortExternalParticipantsAndResources(oldParticipants, newObj.getParticipants(), participantSet,
					resourceSet, receivers, session, all);
		}
		/*
		 * Generate a render map
		 */
		final RenderMap renderMap = new RenderMap();
		renderMap
				.put(new FormatLocalizedStringReplacement(TemplateToken.TITLE, Notifications.FORMAT_DESCRIPTION, title)
						.setChanged(isUpdate ? (oldObj == null ? false : !compareObjects(title, oldObj.getTitle()))
								: false));
		renderMap.put(new ParticipantsReplacement(participantSet).setChanged(isUpdate));
		renderMap.put(new ResourcesReplacement(resourceSet).setChanged(isUpdate));
		{
			String createdByDisplayName = STR_UNKNOWN;
			final Context ctx = session.getContext();
			if (0 != newObj.getCreatedBy()) {
				try {
					createdByDisplayName = resolveUsers(ctx, newObj.getCreatedBy())[0].getDisplayName();
				} catch (final LdapException e) {
					createdByDisplayName = STR_UNKNOWN;
					LL.log(e);
				}
			}
			String modifiedByDisplayName = STR_UNKNOWN;
			try {
				modifiedByDisplayName = resolveUsers(ctx, session.getUserId())[0].getDisplayName();
			} catch (final LdapException e) {
				modifiedByDisplayName = STR_UNKNOWN;
				LL.log(e);
			}
			renderMap.put(new StringReplacement(TemplateToken.CREATED_BY, createdByDisplayName));
			renderMap.put(new StringReplacement(TemplateToken.CHANGED_BY, modifiedByDisplayName));
		}
		{
			final String note = null == newObj.getNote() ? "" : newObj.getNote();
			renderMap.put(new FormatLocalizedStringReplacement(TemplateToken.DESCRIPTION,
					Notifications.FORMAT_COMMENTS, note).setChanged(isUpdate ? (oldObj == null ? false
					: !compareStrings(note, oldObj.getNote())) : false));
		}
		/*
		 * Add task-specific replacements
		 */
		if (Types.TASK == module) {
			final Task task = (Task) newObj;
			final Task oldTask = (Task) oldObj;
			{
				final int priority = task.getPriority();
				try {
					renderMap.put(new TaskPriorityReplacement(priority).setChanged(oldTask == null ? false
							: priority != oldTask.getPriority()));
				} catch (final IllegalArgumentException e) {
					renderMap.put(TaskPriorityReplacement.emptyTaskPriorityReplacement());
				}
			}
			{
				final int status = task.getStatus();
				final int percentComplete = task.getPercentComplete();
				boolean changed = false;
				if (status != 0 && oldTask != null) {
					changed |= (status != oldTask.getStatus());
					changed |= (percentComplete != oldTask.getPercentComplete());
				}
				try {
					renderMap.put(new TaskStatusReplacement(status, percentComplete).setChanged(changed));
				} catch (final IllegalArgumentException e) {
					renderMap.put(TaskStatusReplacement.emptyTaskStatusReplacement());
				}
			}
		}
		/*
		 * Generate replacements which got modified by participant data
		 */
		{
			final Date start = newObj.getStartDate();
			renderMap.put(new StartDateReplacement(start).setChanged(isUpdate ? (oldObj == null ? false
					: !compareObjects(start, oldObj.getStartDate())) : false));
			Date end = newObj.getEndDate();
			/*
			 * Determine changed status with original end time
			 */
			final boolean endChanged = isUpdate ? (oldObj == null ? false : !compareObjects(end, oldObj.getEndDate()))
					: false;
			/*
			 * Set end time to first occurrence's end time if necessary
			 */
			if (newObj.containsRecurrenceType()) {
				if (start != null && end != null) {
					end = computeFirstOccurrenceEnd(start.getTime(), end.getTime());
				}
			} else if (oldObj != null && oldObj.containsRecurrenceType()) {
				if (start != null && end != null) {
					end = computeFirstOccurrenceEnd(start.getTime(), end.getTime());
				}
			} else if (newObj.getRecurrenceType() != CalendarObject.NO_RECURRENCE) {
				if (start != null && end != null) {
					end = computeFirstOccurrenceEnd(start.getTime(), end.getTime());
				}
			}
			renderMap.put(new EndDateReplacement(end, Types.TASK == module).setChanged(endChanged));
		}
		renderMap.put(new CreationDateReplacement(newObj.containsCreationDate() ? newObj.getCreationDate()
				: (oldObj == null ? null : oldObj.getCreationDate()), null));
		{
			final SeriesReplacement seriesRepl;
			if (newObj.containsRecurrenceType() || newObj.getRecurrenceType() != CalendarObject.NO_RECURRENCE) {
				seriesRepl = new SeriesReplacement(newObj);
				seriesRepl.setChanged(isUpdate ? (oldObj == null ? false : !compareRecurrenceInformations(newObj,
						oldObj)) : false);
			} else if (oldObj != null && oldObj.containsRecurrenceType()) {
				seriesRepl = new SeriesReplacement(oldObj);
				seriesRepl.setChanged(false);
			} else {
				seriesRepl = new SeriesReplacement(newObj);
				seriesRepl.setChanged(false);
			}
			renderMap.put(seriesRepl);
		}
		{
			final DeleteExceptionsReplacement deleteExceptionsReplacement;
			final Date[] deleteExcs = newObj.getDeleteException();
			if (newObj.containsDeleteExceptions() || deleteExcs != null) {
				deleteExceptionsReplacement = new DeleteExceptionsReplacement(deleteExcs);
				deleteExceptionsReplacement.setChanged(isUpdate ? (oldObj == null ? false : !compareDates(deleteExcs,
						oldObj.getDeleteException())) : false);
			} else if (oldObj != null && oldObj.containsDeleteExceptions()) {
				deleteExceptionsReplacement = new DeleteExceptionsReplacement(oldObj.getDeleteException());
				deleteExceptionsReplacement.setChanged(false);
			} else {
				deleteExceptionsReplacement = new DeleteExceptionsReplacement(deleteExcs);
				deleteExceptionsReplacement.setChanged(false);
			}
			renderMap.put(deleteExceptionsReplacement);
		}
		{
			final ChangeExceptionsReplacement changeExceptionsReplacement;
			final Date[] changeExcs = newObj.getChangeException();
			if (newObj.containsDeleteExceptions() || changeExcs != null) {
				changeExceptionsReplacement = new ChangeExceptionsReplacement(changeExcs);
				changeExceptionsReplacement.setChanged(isUpdate ? (oldObj == null ? false : !compareDates(changeExcs,
						oldObj.getChangeException())) : false);
			} else if (oldObj != null && oldObj.containsDeleteExceptions()) {
				changeExceptionsReplacement = new ChangeExceptionsReplacement(oldObj.getChangeException());
				changeExceptionsReplacement.setChanged(false);
			} else {
				changeExceptionsReplacement = new ChangeExceptionsReplacement(changeExcs);
				changeExceptionsReplacement.setChanged(false);
			}
			renderMap.put(changeExceptionsReplacement);
		}
		return renderMap;
	}

	private static RenderMap clonedRenderMap(final RenderMap renderMap) {
		return ((RenderMap) renderMap.clone()).applyChangedStatus(false);
	}

	private void checkChangedFolder(final CalendarObject oldObj, final String email, final int folderId,
			final TemplateReplacement tr, final ServerSession sessionObj) {
		final Participant[] oldParticipants = oldObj.getParticipants();
		final Context ctx = sessionObj.getContext();
		if (oldParticipants != null) {
			for (int i = 0; i < oldParticipants.length; i++) {
				final Participant participant = oldParticipants[i];
				switch (participant.getType()) {
				case Participant.USER:
					EmailableParticipant p = getUserParticipant(participant, ctx);
					if (p.type == Participant.USER && p.folderId > 0 && p.email.equalsIgnoreCase(email)) {
						tr.setChanged(p.folderId != folderId);
						return;
					}
					break;
				case Participant.EXTERNAL_USER:
					p = getExternalParticipant(participant, sessionObj);
					if (p.type == Participant.USER && p.folderId > 0 && p.email.equalsIgnoreCase(email)) {
						tr.setChanged(p.folderId != folderId);
						return;
					}
					break;
				case Participant.RESOURCE:
					p = getResourceParticipant(participant, ctx);
					if (p.type == Participant.USER && p.folderId > 0 && p.email.equalsIgnoreCase(email)) {
						tr.setChanged(p.folderId != folderId);
						return;
					}
					break;
				case Participant.GROUP:
					try {
						// FIXME 101 SELECT problem
						final Group group = resolveGroups(ctx, participant.getIdentifier())[0];
						final int[] members = group.getMember();
						final User[] memberObjects = resolveUsers(ctx, members);
						for (final User user : memberObjects) {

							final String lang = user.getPreferredLanguage();
							final int[] groups = user.getGroups();
							final TimeZone tz = TimeZone.getTimeZone(user.getTimeZone());

							if (user.getMail() != null) {
								p = new EmailableParticipant(ctx.getContextId(), Participant.USER, user.getId(),
										groups, user.getMail(), user.getDisplayName(), user.getLocale(), tz, 10, -1,
										CalendarObject.NONE, null);
								if (p.type == Participant.USER && p.folderId > 0 && p.email.equalsIgnoreCase(email)) {
									tr.setChanged(p.folderId != folderId);
									return;
								}
							}
						}
					} catch (final LdapException e) {
						LL.log(e);
					}
					break;
				default:
					throw new IllegalArgumentException("Unknown Participant Type: " + participant.getType());
				}
			}
		}
	}

	private boolean isUser(final Context ctx, final EmailableParticipant p) {
		if (p.type == Participant.EXTERNAL_USER) {
			return false;
		}
		try {
			return resolveUsers(ctx, p.id) != null;
		} catch (final LdapException x) {
			if (x.getDetail() == LdapException.Detail.NOT_FOUND) {
				return false;
			}
			LL.log(x);
			return false;
		}
	}

	private void sortExternalParticipantsAndResources(final Participant[] oldParticipants,
			final Participant[] newParticipants, final Set<EmailableParticipant> participantSet,
			final Set<EmailableParticipant> resourceSet, final Map<Locale, List<EmailableParticipant>> receivers,
			final ServerSession sessionObj, final Map<String, EmailableParticipant> all) {
		sortNewExternalParticipantsAndResources(newParticipants, participantSet, resourceSet, receivers, sessionObj,
				all, oldParticipants);
		sortOldExternalParticipantsAndResources(oldParticipants, resourceSet, receivers, all, sessionObj,
				newParticipants);
	}

	private void sortOldExternalParticipantsAndResources(final Participant[] oldParticipants,
			final Set<EmailableParticipant> resourceSet, final Map<Locale, List<EmailableParticipant>> receivers,
			final Map<String, EmailableParticipant> all, final ServerSession sessionObj,
			final Participant[] newParticipants) {
		if (oldParticipants == null) {
			return;
		}
		final Context ctx = sessionObj.getContext();
		for (final Participant participant : oldParticipants) {
			switch (participant.getType()) {
			case Participant.USER:
				break;
			case Participant.EXTERNAL_USER:
				EmailableParticipant p = getExternalParticipant(participant, sessionObj);
				if (p != null) {
					p.state = contains(participant, newParticipants) ? EmailableParticipant.STATE_NONE
							: EmailableParticipant.STATE_REMOVED;
					addSingleParticipant(p, null, resourceSet, receivers, all, true);
				}
				break;
			case Participant.RESOURCE:
				p = getResourceParticipant(participant, ctx);
				if (p == null) {
					// Might be user added as resource (!)
					p = getUserParticipant(participant, ctx);
				}
				if (p != null) {
					p.state = contains(participant, newParticipants) ? EmailableParticipant.STATE_NONE
							: EmailableParticipant.STATE_REMOVED;
					addSingleParticipant(p, null, resourceSet, receivers, all, true);
				}
				break;
			case Participant.GROUP:
				break;
			default:
				throw new IllegalArgumentException("Unknown Participant Type: " + participant.getType());
			}
		}
	}

	private void sortNewExternalParticipantsAndResources(final Participant[] newParticipants,
			final Set<EmailableParticipant> participantSet, final Set<EmailableParticipant> resourceSet,
			final Map<Locale, List<EmailableParticipant>> receivers, final ServerSession sessionObj,
			final Map<String, EmailableParticipant> all, final Participant[] oldParticipants) {
		if (newParticipants == null) {
			return;
		}
		final Context ctx = sessionObj.getContext();
		for (final Participant participant : newParticipants) {
			switch (participant.getType()) {
			case Participant.USER:
				break;
			case Participant.EXTERNAL_USER:
				EmailableParticipant p = getExternalParticipant(participant, sessionObj);
				if (p != null) {
					p.state = contains(participant, oldParticipants) ? EmailableParticipant.STATE_NONE
							: EmailableParticipant.STATE_NEW;
					addSingleParticipant(p, participantSet, resourceSet, receivers, all, false);
				}

				break;
			case Participant.RESOURCE:
				p = getResourceParticipant(participant, ctx);
				if (p == null) {
					// Might be user added as resource (!)
					p = getUserParticipant(participant, ctx);
				}
				if (p != null) {
					p.state = contains(participant, oldParticipants) ? EmailableParticipant.STATE_NONE
							: EmailableParticipant.STATE_NEW;
					addSingleParticipant(p, participantSet, resourceSet, receivers, all, true);
				}
				break;
			case Participant.GROUP:
				break;
			default:
				throw new IllegalArgumentException("Unknown Participant Type: " + participant.getType());
			}
		}
	}

	private void sortParticipants(final Participant[] oldParticipants, final Participant[] newParticipants,
			final Set<EmailableParticipant> participantSet, final Set<EmailableParticipant> resourceSet,
			final Map<Locale, List<EmailableParticipant>> receivers, final ServerSession sessionObj,
			final Map<String, EmailableParticipant> all) {
		sortNewParticipants(newParticipants, participantSet, resourceSet, receivers, sessionObj, all, oldParticipants);
		sortOldParticipants(oldParticipants, participantSet, resourceSet, receivers, all, sessionObj, newParticipants);
	}

	private void sortOldParticipants(final Participant[] oldParticipants,
			final Set<EmailableParticipant> participantSet, final Set<EmailableParticipant> resourceSet,
			final Map<Locale, List<EmailableParticipant>> receivers, final Map<String, EmailableParticipant> all,
			final ServerSession sessionObj, final Participant[] newParticipants) {
		if (oldParticipants == null) {
			return;
		}
		final Context ctx = sessionObj.getContext();
		for (final Participant participant : oldParticipants) {
			switch (participant.getType()) {
			case Participant.USER:
				EmailableParticipant p = getUserParticipant(participant, ctx);
				if (p != null) {
					p.state = contains(participant, newParticipants) ? EmailableParticipant.STATE_NONE
							: EmailableParticipant.STATE_REMOVED;
					addSingleParticipant(p, participantSet, resourceSet, receivers, all, false);
				}
				break;
			case Participant.EXTERNAL_USER:
				p = getExternalParticipant(participant, sessionObj);
				if (p != null) {
					p.state = contains(participant, newParticipants) ? EmailableParticipant.STATE_NONE
							: EmailableParticipant.STATE_REMOVED;
					addSingleParticipant(p, participantSet, resourceSet, receivers, all, false);
				}
				break;
			case Participant.RESOURCE:
				p = getResourceParticipant(participant, ctx);
				if (p != null) {
					p.state = contains(participant, newParticipants) ? EmailableParticipant.STATE_NONE
							: EmailableParticipant.STATE_REMOVED;
					addSingleParticipant(p, participantSet, resourceSet, receivers, all, true);
				}
				break;
			case Participant.GROUP:
				try {
					// FIXME 101 SELECT problem
					final int state = contains(participant, newParticipants) ? EmailableParticipant.STATE_NONE
							: EmailableParticipant.STATE_REMOVED;
					final Group group = resolveGroups(ctx, participant.getIdentifier())[0];
					final int[] members = group.getMember();
					final User[] memberObjects = resolveUsers(ctx, members);
					for (final User user : memberObjects) {
						// final String lang = user.getPreferredLanguage();
						final int[] groups = user.getGroups();
						final TimeZone tz = TimeZone.getTimeZone(user.getTimeZone());

						if (user.getMail() != null) {
							p = new EmailableParticipant(ctx.getContextId(), Participant.USER, user.getId(), groups,
									user.getMail(), user.getDisplayName(), user.getLocale(), tz, 10, -1,
									CalendarObject.NONE, null);
							p.state = state;
							addSingleParticipant(p, participantSet, resourceSet, receivers, all, false);
						}
					}
				} catch (final LdapException e) {
					LL.log(e);
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown Participant Type: " + participant.getType());
			}

		}
	}

	private void sortNewParticipants(final Participant[] newParticipants,
			final Set<EmailableParticipant> participantSet, final Set<EmailableParticipant> resourceSet,
			final Map<Locale, List<EmailableParticipant>> receivers, final ServerSession sessionObj,
			final Map<String, EmailableParticipant> all, final Participant[] oldParticipants) {
		if (newParticipants == null) {
			return;
		}
		final Context ctx = sessionObj.getContext();
		for (final Participant participant : newParticipants) {
			switch (participant.getType()) {
			case Participant.USER:
				EmailableParticipant p = getUserParticipant(participant, ctx);
				if (p != null) {
					p.state = contains(participant, oldParticipants) ? EmailableParticipant.STATE_NONE
							: EmailableParticipant.STATE_NEW;
					addSingleParticipant(p, participantSet, resourceSet, receivers, all, false);
				}
				break;
			case Participant.EXTERNAL_USER:
				p = getExternalParticipant(participant, sessionObj);
				if (p != null) {
					p.state = contains(participant, oldParticipants) ? EmailableParticipant.STATE_NONE
							: EmailableParticipant.STATE_NEW;
					addSingleParticipant(p, participantSet, resourceSet, receivers, all, false);
				}

				break;
			case Participant.RESOURCE:
				p = getResourceParticipant(participant, ctx);
				if (p != null) {
					p.state = contains(participant, oldParticipants) ? EmailableParticipant.STATE_NONE
							: EmailableParticipant.STATE_NEW;
					addSingleParticipant(p, participantSet, resourceSet, receivers, all, true);
				}
				break;
			case Participant.GROUP:
				try {
					// FIXME 101 SELECT problem
					final int state = contains(participant, oldParticipants) ? EmailableParticipant.STATE_NONE
							: EmailableParticipant.STATE_NEW;
					final Group group = resolveGroups(ctx, participant.getIdentifier())[0];
					final int[] members = group.getMember();
					final User[] memberObjects = resolveUsers(ctx, members);
					for (final User user : memberObjects) {
						// final String lang = user.getPreferredLanguage();
						final int[] groups = user.getGroups();
						final TimeZone tz = TimeZone.getTimeZone(user.getTimeZone());

						if (user.getMail() != null) {
							p = new EmailableParticipant(ctx.getContextId(), Participant.USER, user.getId(), groups,
									user.getMail(), user.getDisplayName(), user.getLocale(), tz, 10, -1,
									CalendarObject.NONE, null);
							p.state = state;
							addSingleParticipant(p, participantSet, resourceSet, receivers, all, false);
						}
					}
				} catch (final LdapException e) {
					LL.log(e);
				}
				break;
			default:
				throw new IllegalArgumentException("Unknown Participant Type: " + participant.getType());
			}

		}
	}

	private EmailableParticipant getExternalParticipant(final Participant participant, final ServerSession sessionObj) {
		if (null == participant.getEmailAddress()) {
			return null;
		}
		/*
		 * Store session user's locale and time zone which are used for external
		 * participants
		 */
		final User user = UserStorage.getStorageUser(sessionObj.getUserId(), sessionObj.getContext());
		final Locale l = user.getLocale();
		final TimeZone tz = Tools.getTimeZone(user.getTimeZone());
		return new EmailableParticipant(-1, participant.getType(), -1, new int[0], participant.getEmailAddress(),
				participant.getDisplayName(), l, tz, 0, -1, CalendarObject.NONE, null);
	}

	private EmailableParticipant getUserParticipant(final Participant participant, final Context ctx) {

		int[] groups = null;
		TimeZone tz = null;
		String mail = null;
		String displayName = null;
		int folderId = -1;
		Locale locale = null;

		try {
			final User user = resolveUsers(ctx, participant.getIdentifier())[0];
			locale = user.getLocale();
			mail = user.getMail();
			if (mail == null) {
				mail = participant.getEmailAddress();
			}
			displayName = user.getDisplayName();
			if (displayName == null) {
				displayName = participant.getDisplayName();
			}
			groups = user.getGroups();
			tz = Tools.getTimeZone(user.getTimeZone());
			if (participant instanceof UserParticipant) {
				final UserParticipant userParticipant = (UserParticipant) participant;
				folderId = userParticipant.getPersonalFolderId();
				// System.out.println("PERSONAL FOLDER ID FOR PARTICIPANT "+
				// userParticipant.getIdentifier()+": "+folderId);
			}
		} catch (final LdapException e) {
			LL.log(e);
		}

		if (mail != null) {
			if (participant instanceof UserParticipant) {
				final UserParticipant up = (UserParticipant) participant;
				return new EmailableParticipant(ctx.getContextId(), up.getType(), up.getIdentifier(), groups, mail,
						displayName, locale, tz, 10, folderId, up.getConfirm(), up.getConfirmMessage());
			}
			return new EmailableParticipant(ctx.getContextId(), participant.getType(), participant.getIdentifier(),
					groups, mail, displayName, locale, tz, 10, folderId, CalendarObject.NONE, null);
		}
		return null;
	}

	private EmailableParticipant getResourceParticipant(final Participant participant, final Context ctx) {
		final int[] groups = new int[0];
		String mail = null;
		String displayName = null;
		try {
			final Resource resource = resolveResources(ctx, participant.getIdentifier())[0];
			mail = resource.getMail();
			if (mail == null) {
				mail = participant.getEmailAddress();
			}
			displayName = resource.getDisplayName();
			if (displayName == null) {
				displayName = participant.getDisplayName();
			}
		} catch (final LdapException e) {
			LL.log(e);
		}

		EmailableParticipant p;
		if (mail != null) {
			p = new EmailableParticipant(ctx.getContextId(), participant.getType(), participant.getIdentifier(),
					groups, mail, displayName, Locale.getDefault(), TimeZone.getDefault(), -1, MailObject.DONT_SET,
					CalendarObject.NONE, null);
			return p;
		}
		return null;
	}

	private void sortUserParticipants(final UserParticipant[] oldParticipants, final UserParticipant[] newParticipants,
			final Set<EmailableParticipant> participantSet, final boolean forUpdate,
			final Map<Locale, List<EmailableParticipant>> receivers, final ServerSession sessionObj,
			final Map<String, EmailableParticipant> all) {
		if (newParticipants == null) {
			return;
		}
		final Context ctx = sessionObj.getContext();
		for (final UserParticipant participant : newParticipants) {
			final EmailableParticipant p = getUserParticipant(participant, ctx);
			if (p != null) {
				p.state = containsUser(participant, oldParticipants) ? EmailableParticipant.STATE_NONE
						: EmailableParticipant.STATE_NEW;
				addSingleParticipant(p, participantSet, null, receivers, all, false);
			}
		}

		for (final UserParticipant participant : oldParticipants) {
			final EmailableParticipant p = getUserParticipant(participant, ctx);
			if (p != null) {
				p.state = containsUser(participant, newParticipants) ? EmailableParticipant.STATE_NONE
						: EmailableParticipant.STATE_REMOVED;
				if (forUpdate) {
					addSingleParticipant(p, participantSet, null, receivers, all, false);
				} else {
					addReceiver(p, receivers, all);
				}
			}
		}

	}

	private void addReceiver(final EmailableParticipant participant,
			final Map<Locale, List<EmailableParticipant>> receivers, final Map<String, EmailableParticipant> all) {

		if (all.containsKey(participant.email)) {
			final EmailableParticipant other = all.get(participant.email);
			if (other.reliability < participant.reliability) {
				if (other.locale.equals(participant.locale)) {
					other.copy(participant);
					return;
				}
				final List<EmailableParticipant> p = receivers.get(other.locale);
				p.remove(p.indexOf(other));
			}
			return;
		}
		final Locale l = participant.locale;

		List<EmailableParticipant> p = receivers.get(l);
		if (p == null) {
			p = new ArrayList<EmailableParticipant>();
			receivers.put(l, p);
		}

		all.put(participant.email, participant);
		p.add(participant);

	}

	private void addSingleParticipant(final EmailableParticipant participant,
			final Set<EmailableParticipant> participantSet, final Set<EmailableParticipant> resourceSet,
			final Map<Locale, List<EmailableParticipant>> receivers, final Map<String, EmailableParticipant> all,
			final boolean /* HACK */resource) {
		addReceiver(participant, receivers, all);
		if (resource) {
			resourceSet.add(participant);
			return;
		}
		participantSet.add(participant);
	}

	static final class MailMessage {

		/**
		 * Initializes a new MailMessage
		 */
		public MailMessage() {
			super();
		}

		public String message;

		public String title;

		public List<String> addresses = new ArrayList<String>();

		public int folderId;

		public boolean internal;
	}

	private static final boolean containsUser(final UserParticipant toSearch, final UserParticipant[] userParticipants) {
		if (null == userParticipants) {
			return true;
		}
		for (final UserParticipant userParticipant : userParticipants) {
			if (userParticipant != null && userParticipant.equals(toSearch)) {
				return true;
			}
		}
		return false;
	}

	private static final boolean contains(final Participant toSearch, final Participant[] participants) {
		if (null == participants) {
			return true;
		}
		for (final Participant participant : participants) {
			if (participant != null && participant.equals(toSearch)) {
				return true;
			}
		}
		return false;
	}

	private static final boolean compareRecurrenceInformations(final CalendarObject o1, final CalendarObject o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1.getRecurrenceType() != o2.getRecurrenceType()) {
			return false;
		}
		if (CalendarObject.DAILY == o1.getRecurrenceType()) {
			return o1.getInterval() == o2.getInterval();
		}
		if (CalendarObject.WEEKLY == o1.getRecurrenceType()) {
			if (o1.getInterval() != o2.getInterval()) {
				return false;
			}
			return o1.getDays() == o2.getDays();
		}
		if (CalendarObject.MONTHLY == o1.getRecurrenceType()) {
			if (o1.getInterval() != o2.getInterval()) {
				return false;
			}
			if (o1.getDays() != o2.getDays()) {
				return false;
			}
			return o1.getDayInMonth() == o2.getDayInMonth();
		}
		if (CalendarObject.YEARLY == o1.getRecurrenceType()) {
			if (o1.getMonth() != o2.getMonth()) {
				return false;
			}
			if (o1.getDays() != o2.getDays()) {
				return false;
			}
			return o1.getDayInMonth() == o2.getDayInMonth();
		}
		return true;
	}

	/**
	 * Compares given {@link Object} references.
	 * 
	 * @param o1
	 *            The first object
	 * @param o2
	 *            The second object
	 * @return <code>true</code> if both {@link Object} references are
	 *         considered to be equal; otherwise <code>false</code>
	 */
	static final boolean compareObjects(final Object o1, final Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null) {
			if (o2 == null) {
				return true;
			}
			return false;
		}
		return o1.equals(o2);
	}

	static final boolean compareDates(final Date[] dates1, final Date[] dates2) {
		if (dates1 == dates2) {
			return true;
		}
		if (dates1 == null) {
			if (dates2 == null) {
				return true;
			}
			return dates2.length == 0 ? true : false;
		}
		if (dates2 == null && dates1.length == 0) {
			return true;
		}
		return Arrays.equals(dates1, dates2);
	}

	/**
	 * Compares given {@link String} references.
	 * <p>
	 * Note: A <code>null</code> reference and an empty string are considered to
	 * be equal. Otherwise use {@link #compareObjects(Object, Object)}
	 * 
	 * @param s1
	 *            The first string
	 * @param s2
	 *            The second string
	 * @return <code>true</code> if both {@link String} references are
	 *         considered to be equal; otherwise <code>false</code>
	 */
	static final boolean compareStrings(final String s1, final String s2) {
		if (s1 == s2) {
			return true;
		}
		if (s1 == null) {
			if (s2 == null) {
				return true;
			}
			return s2.length() == 0 ? true : false;
		}
		if (s2 == null && s1.length() == 0) {
			return true;
		}
		return s1.equals(s2);
	}

	private static final long THIRTY_MINUTES = 1800000l;

	/**
	 * Checks the start date of specified calendar object if it is not more than
	 * 30 minutes in the past and its end date is not in the past compared to
	 * {@link System#currentTimeMillis()}.
	 * 
	 * @param calendarObj
	 *            The calendar object whose start and end date is ought to be
	 *            checked
	 * @param module
	 *            The module
	 * @return <code>true</code> if the start date of specified calendar object
	 *         if it is not more than 30 minutes in the past and its end date is
	 *         not in the past compared to {@link System#currentTimeMillis()};
	 *         otherwise <code>false</code>.
	 */
	static final boolean checkStartAndEndDate(final CalendarObject calendarObj, final int module) {
		final long now = System.currentTimeMillis();
		{
			// Do not send notification mails for tasks and appointments in the
			// past. Bug #12063
			final Date endDate = calendarObj.getEndDate();
			if (endDate != null) {
				if (Types.APPOINTMENT == module && endDate.getTime() < now) {
					if (LOG.isDebugEnabled()) {
						LOG.debug(new StringBuilder().append("Ignoring notification(s) for appointment object ")
								.append(calendarObj.getObjectID()).append(" since its end date is in the past")
								.toString());
					}
					return false;
				}
				if (Types.TASK == module && !compare2Date(endDate.getTime(), now)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug(new StringBuilder().append("Ignoring notification(s) for task object ").append(
								calendarObj.getObjectID()).append(" since its end date is in the past").toString());
					}
					return false;
				}
			}
		}
		{
			// Do not send notification mails for tasks and appointments whose
			// start date is more than 30 minutes in the past
			final Date startDate = calendarObj.getStartDate();
			if (startDate != null) {
				if (Types.APPOINTMENT == module && (now - startDate.getTime()) > THIRTY_MINUTES) {
					if (LOG.isDebugEnabled()) {
						LOG.debug(new StringBuilder().append("Ignoring notification(s) for appointment object ")
								.append(calendarObj.getObjectID()).append(
										" since its start date is more than 30 minutes in the past").toString());
					}
					return false;
				}
				if (Types.TASK == module && !compare2Date(startDate.getTime(), now)) {
					if (LOG.isDebugEnabled()) {
						LOG.debug(new StringBuilder().append("Ignoring notification(s) for task object ").append(
								calendarObj.getObjectID()).append(" since its start date is in the past").toString());
					}
					return false;
				}

			}
		}
		return true;
	}

	/**
	 * Compares if given time millis fit into the date denoted by specified date
	 * millis.
	 * 
	 * @param date
	 *            The date millis
	 * @param millis
	 *            The time millis
	 * @return <code>true</code> if given time millis fit into the date denoted
	 *         by specified date millis; otherwise <code>false</code>
	 */
	private static boolean compare2Date(final long date, final long millis) {
		return date >= (millis - (millis % Constants.MILLI_DAY));
	}

	/**
	 * Computes the first occurence's end time.
	 * 
	 * @param startMillis
	 *            The start time in UTC milliseconds
	 * @param endMillis
	 *            The end time in UTC milliseconds
	 * @return The first occurence's end time.
	 */
	private static Date computeFirstOccurrenceEnd(final long startMillis, final long endMillis) {
		final Calendar cal = GregorianCalendar.getInstance(Tools.getTimeZone("UTC"), Locale.ENGLISH);
		cal.setTimeInMillis(endMillis);
		final int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
		final int minutes = cal.get(Calendar.MINUTE);
		cal.setTimeInMillis(startMillis);
		cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
		cal.set(Calendar.MINUTE, minutes);
		return cal.getTime();
	}
}

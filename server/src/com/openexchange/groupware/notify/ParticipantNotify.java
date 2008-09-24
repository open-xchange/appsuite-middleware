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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import com.openexchange.groupware.notify.hostname.HostnameService;
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
import com.openexchange.i18n.tools.replacement.CreationDateReplacement;
import com.openexchange.i18n.tools.replacement.EndDateReplacement;
import com.openexchange.i18n.tools.replacement.FormatLocalizedStringReplacement;
import com.openexchange.i18n.tools.replacement.LocalizedStringReplacement;
import com.openexchange.i18n.tools.replacement.ModuleReplacement;
import com.openexchange.i18n.tools.replacement.ParticipantsReplacement;
import com.openexchange.i18n.tools.replacement.ResourcesReplacement;
import com.openexchange.i18n.tools.replacement.SeriesReplacement;
import com.openexchange.i18n.tools.replacement.StartDateReplacement;
import com.openexchange.i18n.tools.replacement.StringReplacement;
import com.openexchange.i18n.tools.replacement.TaskActionReplacement;
import com.openexchange.mail.MailException;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.resource.Resource;
import com.openexchange.resource.storage.ResourceStorage;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.exceptions.LoggingLogic;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

public class ParticipantNotify implements AppointmentEventInterface2, TaskEventInterface2 {

	// TODO: Signatur?
	// TODO: Abgesagt / Zugesagt

	// TODO: user getLocale of user instead of detemining locale yourself

	private final static Log LOG = LogFactory.getLog(ParticipantNotify.class);

	private final static LoggingLogic LL = LoggingLogic.getLoggingLogic(ParticipantNotify.class);

	/**
	 * Initializes a new {@link ParticipantNotify}
	 */
	public ParticipantNotify() {
		super();
	}

	protected void sendMessage(final String messageTitle, final String message, final List<String> name,
			final ServerSession session, final CalendarObject obj, int folderId, final State state,
			final boolean suppressOXReminderHeader, final boolean internal) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Sending message to: " + name);
			LOG.debug("=====[" + messageTitle + "]====\n\n");
			LOG.debug(message);
			LOG.debug("\n\n============");
		}
		if (folderId == -1) {
			folderId = obj.getParentFolderID();
		}

		if (suppressOXReminderHeader) {
			folderId = MailObject.DONT_SET;
		}

		final MailObject mail = new MailObject(session, obj.getObjectID(), folderId, state.getModule());
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

	protected static User[] resolveUsers(final Context ctx, final int... ids) throws LdapException {
		final User[] r = new User[ids.length];
		int i = 0;
		for (final int id : ids) {
			r[i++] = UserStorage.getInstance().getUser(id, ctx); // FIXME
		}
		return r;
	}

	protected static Group[] resolveGroups(final Context ctx, final int... ids) throws LdapException {
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
				AppointmentActionReplacement.ACTION_NEW), Notifications.APPOINTMENT_CREATE_MAIL), false, false, false);
	}

	public void appointmentModified(final AppointmentObject appointmentObj, final Session sessionObj) {
		sendNotification(null, appointmentObj, sessionObj, new AppointmentState(new AppointmentActionReplacement(
				AppointmentActionReplacement.ACTION_CHANGED), Notifications.APPOINTMENT_UPDATE_MAIL), false, false,
				true);
	}

	public void appointmentModified(final AppointmentObject oldAppointment, final AppointmentObject newAppointment,
			final Session sessionObj) {
		sendNotification(oldAppointment, newAppointment, sessionObj, new AppointmentState(
				new AppointmentActionReplacement(AppointmentActionReplacement.ACTION_CHANGED),
				Notifications.APPOINTMENT_UPDATE_MAIL), false, false, true);
	}

	public void appointmentDeleted(final AppointmentObject appointmentObj, final Session sessionObj) {
		sendNotification(null, appointmentObj, sessionObj, new AppointmentState(new AppointmentActionReplacement(
				AppointmentActionReplacement.ACTION_DELETED), Notifications.APPOINTMENT_DELETE_MAIL),
				NotificationConfig.getPropertyAsBoolean(NotificationProperty.NOTIFY_ON_DELETE, false), true, false);
	}

	public void taskCreated(final Task taskObj, final Session sessionObj) {
		sendNotification(null, taskObj, sessionObj, new TaskState(new TaskActionReplacement(
				TaskActionReplacement.ACTION_NEW), Notifications.TASK_CREATE_MAIL), false, false, false);
	}

	public void taskModified(final Task taskObj, final Session sessionObj) {
		sendNotification(null, taskObj, sessionObj, new TaskState(new TaskActionReplacement(
				TaskActionReplacement.ACTION_CHANGED), Notifications.TASK_UPDATE_MAIL), false, false, true);

	}

	public void taskModified(final Task oldTask, final Task newTask, final Session sessionObj) {
		sendNotification(oldTask, newTask, sessionObj, new TaskState(new TaskActionReplacement(
				TaskActionReplacement.ACTION_CHANGED), Notifications.TASK_UPDATE_MAIL), false, false, true);
	}

	public void taskDeleted(final Task taskObj, final Session sessionObj) {
		sendNotification(null, taskObj, sessionObj, new TaskState(new TaskActionReplacement(
				TaskActionReplacement.ACTION_DELETED), Notifications.TASK_DELETE_MAIL), NotificationConfig
				.getPropertyAsBoolean(NotificationProperty.NOTIFY_ON_DELETE, false), true, false);
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

		if (!newObj.getNotification() && newObj.getCreatedBy() == sessionObj.getUserId() && !forceNotifyOthers) {
			return;
		}
		if (newObj.getParticipants() == null) {
			return;
		}
		// Do not send notification mails for tasks and appointments in the
		// past. Bug #12063
		if (newObj.getEndDate().getTime() < System.currentTimeMillis()) {
			return;
		}

		final SortedSet<EmailableParticipant> participantSet = new TreeSet<EmailableParticipant>();
		final SortedSet<EmailableParticipant> resourceSet = new TreeSet<EmailableParticipant>();

		final Map<Locale, List<EmailableParticipant>> receivers = new HashMap<Locale, List<EmailableParticipant>>();

		final Map<String, EmailableParticipant> all = new HashMap<String, EmailableParticipant>();
		final UserParticipant[] users = newObj.getUsers();
		if (null == users) {
			Participant[] oldParticipants = new Participant[0];
			if (oldObj != null) {
				oldParticipants = oldObj.getParticipants();
			}
			sortParticipants(oldParticipants, newObj.getParticipants(), participantSet, resourceSet, receivers,
					sessionObj, all);
		} else {
			UserParticipant[] oldUsers = new UserParticipant[0];
			if (oldObj != null) {
				oldUsers = oldObj.getUsers();
			}
			Participant[] oldParticipants = new Participant[0];
			if (oldObj != null) {
				oldParticipants = oldObj.getParticipants();
			}

			sortUserParticipants(oldUsers, newObj.getUsers(), participantSet, isUpdate, receivers, sessionObj, all);
			sortExternalParticipantsAndResources(oldParticipants, newObj.getParticipants(), participantSet,
					resourceSet, receivers, sessionObj, all);
		}

		String createdByDisplayName = "UNKNOWN";
		String modifiedByDisplayName = "UNKNOWN";
		try {
			final Context ctx = sessionObj.getContext();
			if (0 != newObj.getCreatedBy()) {
				createdByDisplayName = resolveUsers(ctx, newObj.getCreatedBy())[0].getDisplayName();
			}
			if (0 != newObj.getModifiedBy()) {
				modifiedByDisplayName = resolveUsers(ctx, newObj.getModifiedBy())[0].getDisplayName();
			}
		} catch (final LdapException e) {
			createdByDisplayName = e.toString();
			modifiedByDisplayName = e.toString();
			LL.log(e);
		}

		/*
		 * Generate a render map filled with object-specific information
		 */
		final String title = null == newObj.getTitle() ? "" : newObj.getTitle();
		final RenderMap renderMap = createRenderMap(newObj, oldObj, isUpdate, participantSet, resourceSet,
				createdByDisplayName, modifiedByDisplayName, title);
		/*
		 * The message title
		 */
		final TemplateReplacement messageSubjectRepl = state.getTitle();

		final OXFolderAccess access = new OXFolderAccess(sessionObj.getContext());

		final List<MailMessage> messages = new ArrayList<MailMessage>();
		for (final Locale locale : receivers.keySet()) {

			final StringHelper strings = new StringHelper(locale);
			final Template createTemplate = state.getMessageTemplate();

			final List<EmailableParticipant> participants = receivers.get(locale);

			/*
			 * Apply new locale to replacements
			 */
			messageSubjectRepl.setLocale(locale);
			renderMap.applyLocale(locale);
			/*
			 * Iterate over locale's participants
			 */
			for (final EmailableParticipant p : participants) {
				TimeZone tz = TimeZone.getDefault();
				boolean sendMail = true;

				if (isUser(sessionObj.getContext(), p)) {
					try {
						final UserSettingMail userSettingMail = getUserSettingMail(p.id, sessionObj.getContext());
						sendMail = state.sendMail(userSettingMail) && newObj.getModifiedBy() != p.id
								&& (newObj.getNotification() || p.id == newObj.getCreatedBy() || forceNotifyOthers);
						tz = p.timeZone;
					} catch (final AbstractOXException e) {
						LL.log(e);
					}
				} else {
					sendMail = newObj.getNotification() || (newObj.getModifiedBy() != p.id && forceNotifyOthers);
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
						final TemplateReplacement tr = new FormatLocalizedStringReplacement(TemplateToken.FOLDER_NAME,
								Notifications.FORMAT_FOLDER, folderName);
						tr.setLocale(locale);
						if (oldObj != null) {
							if (p.folderId > 0) {
								checkChangedFolder(oldObj, p.email, folderId, tr, sessionObj);
							} else {
								tr.setChanged(newObj.getParentFolderID() != oldObj.getParentFolderID());
							}
						}
						renderMap.put(tr);
					}

					/*
					 * Special information(s)
					 */
					state.addSpecial(newObj, oldObj, renderMap, p);

					final MailMessage msg = new MailMessage();
					msg.title = new StringBuilder(messageSubjectRepl.getReplacement()).append(": ").append(title)
							.toString();
					if (isUpdate) {
						if (EmailableParticipant.STATE_REMOVED == p.state) {
							/*
							 * Current participant is removed by caught update
							 * event
							 */
							if (Types.APPOINTMENT == state.getModule()) {
								msg.title = new StringBuilder(new AppointmentActionReplacement(
										AppointmentActionReplacement.ACTION_DELETED, locale).getReplacement()).append(
										": ").append(title).toString();
								/*
								 * Get cloned version of render map to apply
								 * changed status
								 */
								final RenderMap clone = clonedRenderMap(renderMap);
								/*
								 * Render proper message for removed participant
								 */
								msg.message = new StringTemplate(strings
										.getString(Notifications.APPOINTMENT_DELETE_MAIL)).render(clone);
							} else {
								// TODO: Change title for task notification here
								msg.message = new StringTemplate(strings
										.getString(Notifications.TASK_REMOVED_PARTICIPANT))
										.render(((RenderMap) renderMap.clone()).applyChangedStatus(false));
							}
						} else if (EmailableParticipant.STATE_NEW == p.state) {
							/*
							 * Current participant is added by caught update
							 * event
							 */
							if (Types.APPOINTMENT == state.getModule()) {
								msg.title = new StringBuilder(new AppointmentActionReplacement(
										AppointmentActionReplacement.ACTION_NEW, locale).getReplacement()).append(": ")
										.append(title).toString();
								final RenderMap clone = clonedRenderMap(renderMap);
								/*
								 * Render proper message for removed participant
								 */
								if (p.type == Participant.EXTERNAL_USER) {
									// Without [link] replacement
									msg.message = new StringTemplate(strings
											.getString(Notifications.APPOINTMENT_CREATE_MAIL_EXT)).render(clone);
								} else {
									msg.message = new StringTemplate(strings
											.getString(Notifications.APPOINTMENT_CREATE_MAIL)).render(clone);
								}
							} else {
								// TODO: Change title for task notification here
								if (p.type == Participant.EXTERNAL_USER) {
									// Without [link] replacement
									msg.message = new StringTemplate(strings
											.getString(Notifications.TASK_ADDED_PARTICIPANT_EXT))
											.render(((RenderMap) renderMap.clone()).applyChangedStatus(false));
								} else {
									msg.message = new StringTemplate(strings
											.getString(Notifications.TASK_ADDED_PARTICIPANT))
											.render(((RenderMap) renderMap.clone()).applyChangedStatus(false));
								}
							}
						} else {
							msg.message = createTemplate.render(renderMap);
						}
					} else {
						msg.message = createTemplate.render(renderMap);
					}
					msg.addresses.add(p.email);
					msg.folderId = p.folderId;
					msg.internal = p.type != Participant.EXTERNAL_USER;
					messages.add(msg);
				}
			}
		}

		for (final MailMessage mmsg : messages) {
			sendMessage(mmsg.title, mmsg.message, mmsg.addresses, sessionObj, newObj, mmsg.folderId, state,
					suppressOXReminderHeader, mmsg.internal);
		}
	}

	private RenderMap createRenderMap(final CalendarObject newObj, final CalendarObject oldObj, final boolean isUpdate,
			final SortedSet<EmailableParticipant> participantSet, final SortedSet<EmailableParticipant> resourceSet,
			final String createdByDisplayName, final String modifiedByDisplayName, final String title) {
		/*
		 * Generate a render map filled with object-specific information
		 */
		final RenderMap renderMap = new RenderMap();
		renderMap
				.put(new FormatLocalizedStringReplacement(TemplateToken.TITLE, Notifications.FORMAT_DESCRIPTION, title)
						.setChanged(isUpdate ? (oldObj == null ? false : !compareObjects(title, oldObj.getTitle()))
								: false));
		renderMap.put(new ParticipantsReplacement(participantSet).setChanged(isUpdate));
		renderMap.put(new ResourcesReplacement(resourceSet).setChanged(isUpdate));
		renderMap.put(new StringReplacement(TemplateToken.CREATED_BY, createdByDisplayName));
		renderMap.put(new StringReplacement(TemplateToken.CHANGED_BY, modifiedByDisplayName));
		{
			final String note = null == newObj.getNote() ? "" : newObj.getNote();
			renderMap.put(new FormatLocalizedStringReplacement(TemplateToken.DESCRIPTION,
					Notifications.FORMAT_COMMENTS, note).setChanged(isUpdate ? (oldObj == null ? false
					: !compareObjects(note, oldObj.getNote())) : false));
		}
		/*
		 * Generate replacements which got modified by participant data
		 */
		{
			final Date start = newObj.getStartDate();
			renderMap.put(new StartDateReplacement(start).setChanged(isUpdate ? (oldObj == null ? false
					: !compareObjects(start, oldObj.getStartDate())) : false));
		}
		{
			final Date end = newObj.getEndDate();
			renderMap.put(new EndDateReplacement(end).setChanged(isUpdate ? (oldObj == null ? false : !compareObjects(
					end, oldObj.getEndDate())) : false));
		}
		renderMap.put(new CreationDateReplacement(newObj.containsCreationDate() ? newObj.getCreationDate()
				: (oldObj == null ? null : oldObj.getCreationDate()), null));
		{
			final SeriesReplacement seriesRepl;
			if (newObj.containsRecurrenceType()) {
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
		return renderMap;
	}

	private RenderMap clonedRenderMap(final RenderMap renderMap) {
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
			tz = TimeZone.getTimeZone(user.getTimeZone());
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

	public static final class EmailableParticipant implements Comparable<EmailableParticipant>, Cloneable {

		/**
		 * Indicating no change compared to object's participants
		 */
		public static final int STATE_NONE = 0;

		/**
		 * Marks a participant as being newly added to object's participants
		 */
		public static final int STATE_NEW = 1;

		/**
		 * Marks a participant as being removed from object's participants
		 */
		public static final int STATE_REMOVED = -1;

		private final int hc;

		public String email;

		public String displayName;

		public Locale locale;

		public int type;

		public int id;

		public int[] groups;

		public TimeZone timeZone;

		public int reliability;

		public int folderId;

		public int cid;

		public int confirm = CalendarObject.NONE;

		public String confirmMessage;

		/**
		 * The current participant's state: {@link #STATE_NONE} ,
		 * {@link #STATE_REMOVED}, or {@link #STATE_NEW}
		 */
		public int state = STATE_NONE;

		public EmailableParticipant(final int cid, final int type, final int id, final int[] groups,
				final String email, final String displayName, final Locale locale, final TimeZone timeZone,
				final int reliability, final int folderId, final int confirm, final String confirmMessage) {
			this.cid = cid;
			this.type = type;
			this.email = email;
			this.displayName = displayName;
			this.locale = locale;
			this.id = id;
			this.groups = groups;
			this.timeZone = timeZone;
			this.reliability = reliability;
			this.folderId = folderId;
			this.hc = getHashCode();
			this.confirm = confirm;
			this.confirmMessage = confirmMessage;
		}

		private int getHashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((email == null) ? 0 : email.toLowerCase(Locale.ENGLISH).hashCode());
			return result;
		}

		public void copy(final EmailableParticipant participant) {
			this.cid = participant.cid;
			this.type = participant.type;
			this.email = participant.email;
			this.displayName = participant.displayName;
			this.locale = participant.locale;
			this.id = participant.id;
			this.groups = participant.groups;
			this.timeZone = participant.timeZone;
			this.reliability = participant.reliability;
			this.state = participant.state;
			this.confirm = participant.confirm;
			this.confirmMessage = participant.confirmMessage;
		}

		@Override
		public int hashCode() {
			return hc;
		}

		@Override
		public boolean equals(final Object o) {
			if (o instanceof EmailableParticipant) {
				final EmailableParticipant other = (EmailableParticipant) o;
				return other.email.equalsIgnoreCase(email);
			}
			return false;
		}

		public int compareTo(final EmailableParticipant other) {
			return this.displayName.compareTo(other.displayName);
		}

		@Override
		public Object clone() throws CloneNotSupportedException {
			final EmailableParticipant clone = (EmailableParticipant) super.clone();
			clone.locale = (Locale) (locale == null ? null : locale.clone());
			clone.timeZone = (TimeZone) (timeZone == null ? null : timeZone.clone());
			return clone;
		}
	}

	private static class MailMessage {

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

	// Special handling for Appointments or Tasks goes here
	public static interface State {
		public boolean sendMail(UserSettingMail userSettingMail);

		public DateFormat getDateFormat(Locale locale);

		public void addSpecial(CalendarObject obj, CalendarObject oldObj, RenderMap renderMap, EmailableParticipant p);

		public int getModule();

		public void modifyInternal(MailObject mail, CalendarObject obj, ServerSession sessObj);

		public void modifyExternal(MailObject mail, CalendarObject obj, ServerSession sessObj);

		public Template getMessageTemplate();

		public LocalizedStringReplacement getTitle();

	}

	public static abstract class LinkableState implements State {

		protected static volatile Template object_link_template;

		private static String hostname;

		private static UnknownHostException warnSpam;
		static {
			try {
				hostname = InetAddress.getLocalHost().getCanonicalHostName();
			} catch (final UnknownHostException e) {
				hostname = "localhost";
				warnSpam = e;
			}
		}

		public void addSpecial(final CalendarObject obj, final CalendarObject oldObj, final RenderMap renderMap,
				final EmailableParticipant p) {
			renderMap.put(new StringReplacement(TemplateToken.LINK, generateLink(obj, p)));
		}

		public String generateLink(final CalendarObject obj, final EmailableParticipant p) {
			if (object_link_template == null) {
				loadTemplate();
			}

			final RenderMap subst = new RenderMap();
			switch (getModule()) {
			case Types.APPOINTMENT:
				subst.put(new ModuleReplacement(ModuleReplacement.MODULE_CALENDAR));
				break;
			case Types.TASK:
				subst.put(new ModuleReplacement(ModuleReplacement.MODULE_TASK));
				break;
			default:
				subst.put(new ModuleReplacement(ModuleReplacement.MODULE_UNKNOWN));
				break;
			}

			int folder = p.folderId;
			if (folder == -1) {
				folder = obj.getParentFolderID();
			}

			subst.put(new StringReplacement(TemplateToken.FOLDER_ID, String.valueOf(folder)));
			subst.put(new StringReplacement(TemplateToken.OBJECT_ID, String.valueOf(obj.getObjectID())));
			final HostnameService hostnameService = ServerServiceRegistry.getInstance().getService(
					HostnameService.class);
			final String hostnameStr;
			if (hostnameService == null || (hostnameStr = hostnameService.getHostname(p.id, p.cid)) == null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("No host name available; using local host name as fallback");
				}
				if (warnSpam != null) {
					LOG
							.error(
									"Can't resolve my own hostname, using 'localhost' instead, which is certainly not what you want.!",
									warnSpam);
				}
				subst.put(new StringReplacement(TemplateToken.HOSTNAME, hostname));
			} else {
				subst.put(new StringReplacement(TemplateToken.HOSTNAME, hostnameStr));
			}

			return object_link_template.render(subst);
		}

		public void loadTemplate() {
			synchronized (LinkableState.class) {
				object_link_template = new StringTemplate(NotificationConfig.getProperty(
						NotificationProperty.OBJECT_LINK, ""));
			}
		}

	}

	public static class AppointmentState extends LinkableState {

		private final LocalizedStringReplacement titleReplacement;

		private final String messageTemplate;

		public AppointmentState(final LocalizedStringReplacement titleReplacement, final String messageTemplate) {
			super();
			this.titleReplacement = titleReplacement;
			this.messageTemplate = messageTemplate;
		}

		public boolean sendMail(final UserSettingMail userSettingMail) {
			return userSettingMail.isNotifyAppointments();
		}

		@Override
		public void addSpecial(final CalendarObject obj, final CalendarObject oldObj, final RenderMap renderMap,
				final EmailableParticipant p) {
			super.addSpecial(obj, oldObj, renderMap, p);
			String location = ((AppointmentObject) obj).getLocation();
			if (location == null) {
				location = "";
			}
			final TemplateReplacement tr = new FormatLocalizedStringReplacement(TemplateToken.LOCATION,
					Notifications.FORMAT_LOCATION, location);
			tr.setLocale(p.locale);
			tr.setChanged(oldObj == null ? false
					: !compareObjects(location, ((AppointmentObject) oldObj).getLocation()));
			renderMap.put(tr);
		}

		public int getModule() {
			return Types.APPOINTMENT;
		}

		public DateFormat getDateFormat(final Locale locale) {
			return tryAppendingTimeZone(DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, locale));
		}

		private DateFormat tryAppendingTimeZone(final DateFormat df) {
			if (df instanceof SimpleDateFormat) {
				final SimpleDateFormat sdf = (SimpleDateFormat) df;
				final String format = sdf.toPattern();
				return new SimpleDateFormat(format + ", z");
			}
			return df;
		}

		public void modifyInternal(final MailObject mail, final CalendarObject obj, final ServerSession sessObj) {

		}

		public void modifyExternal(final MailObject mail, final CalendarObject obj, final ServerSession sessObj) {
			addICALAttachment(mail, (AppointmentObject) obj, sessObj);
		}

		private void addICALAttachment(final MailObject mail, final AppointmentObject obj, final ServerSession sessObj) {
			try {
				final VersitDefinition versitDefinition = Versit.getDefinition("text/calendar");
				final UnsynchronizedByteArrayOutputStream byteArrayOutputStream = new UnsynchronizedByteArrayOutputStream();
				final VersitDefinition.Writer versitWriter = versitDefinition.getWriter(byteArrayOutputStream, "UTF-8");
				final VersitObject versitObjectContainer = OXContainerConverter.newCalendar("2.0");
				versitDefinition.writeProperties(versitWriter, versitObjectContainer);
				final OXContainerConverter oxContainerConverter = new OXContainerConverter(sessObj.getContext(),
						TimeZone.getDefault());
				final VersitDefinition eventDef = versitDefinition.getChildDef("VEVENT");

				final VersitObject versitObject = oxContainerConverter.convertAppointment(obj);
				eventDef.write(versitWriter, versitObject);
				versitDefinition.writeEnd(versitWriter, versitObjectContainer);
				versitWriter.flush();

				final InputStream icalFile = new UnsynchronizedByteArrayInputStream(byteArrayOutputStream.toByteArray());

				final ContentType ct = new ContentType();
				ct.setPrimaryType("text");
				ct.setSubType("calendar");
				ct.setCharsetParameter("utf-8");

				final String filename = "appointment.ics";

				mail.addFileAttachment(ct, filename, icalFile);

			} catch (final IOException e) {
				LOG.error("Can't convert appointment for notification mail.", e);
			} catch (final ConverterException e) {
				LOG.error("Can't convert appointment for notification mail.", e);
			} catch (final MailException e) {
				LOG.error("Can't add attachment", e);
			}
		}

		public Template getMessageTemplate() {
			return new StringTemplate(messageTemplate);
		}

		public LocalizedStringReplacement getTitle() {
			return titleReplacement;
		}

	}

	public static class TaskState extends LinkableState {

		private final LocalizedStringReplacement titleReplacement;

		private final String messageTemplate;

		public TaskState(final LocalizedStringReplacement titleReplacement, final String messageTemplate) {
			super();
			this.titleReplacement = titleReplacement;
			this.messageTemplate = messageTemplate;
		}

		public boolean sendMail(final UserSettingMail userSettingMail) {
			return userSettingMail.isNotifyTasks();
		}

		public int getModule() {
			return Types.TASK;
		}

		public void modifyInternal(final MailObject mail, final CalendarObject obj, final ServerSession sessObj) {

		}

		public void modifyExternal(final MailObject mail, final CalendarObject obj, final ServerSession sessObj) {

		}

		public DateFormat getDateFormat(final Locale locale) {
			return DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
		}

		public Template getMessageTemplate() {
			return new StringTemplate(messageTemplate);
		}

		public LocalizedStringReplacement getTitle() {
			return titleReplacement;
		}

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
}

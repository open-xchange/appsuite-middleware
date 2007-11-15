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

package com.openexchange.mail.versit;

import java.io.IOException;
import java.util.List;

import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLInterfaceImpl;
import com.openexchange.mail.MailException;
import com.openexchange.mail.config.MailConfig;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.versit.Versit;
import com.openexchange.tools.versit.VersitDefinition;
import com.openexchange.tools.versit.VersitObject;
import com.openexchange.tools.versit.converter.ConverterException;
import com.openexchange.tools.versit.converter.OXContainerConverter;

/**
 * {@link VersitUtility}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class VersitUtility {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(VersitUtility.class);

	private static final String PARAM_CHARSET = "charset";

	/**
	 * No instantiation
	 */
	private VersitUtility() {
		super();
	}

	/**
	 * Saves specified <code>VCard</code> mail part into corresponding default
	 * folder. The resulting instance of {@link CommonObject} is added to given
	 * list.
	 * 
	 * @param versitPart
	 *            The <code>VCard</code> mail part
	 * @param retvalList
	 *            The list to which the resulting instance of
	 *            {@link CommonObject} is added
	 * @param session
	 *            The session providing needed user data
	 * @throws MailException
	 *             If mail part's data cannot be read
	 * @throws IOException
	 *             If mail part's data cannot be parsed
	 */
	public static void saveVCard(final MailPart versitPart, final List<CommonObject> retvalList,
			final SessionObject session) throws MailException, IOException {
		/*
		 * Define versit reader
		 */
		final VersitDefinition def = Versit.getDefinition(versitPart.getContentType().getBaseType());
		final VersitDefinition.Reader r = def.getReader(versitPart.getInputStream(), versitPart.getContentType()
				.containsParameter(PARAM_CHARSET) ? versitPart.getContentType().getParameter(PARAM_CHARSET)
				: MailConfig.getDefaultMimeCharset());
		/*
		 * Ok, convert versit object to corresponding data object and save this
		 * object via its interface
		 */
		OXContainerConverter oxc = null;
		try {
			oxc = new OXContainerConverter(session);
			final ContactSQLInterface contactInterface = new RdbContactSQLInterface(session);
			final VersitObject vo = def.parse(r);
			if (vo != null) {
				try {
					final ContactObject contactObj = oxc.convertContact(vo);
					contactObj.setParentFolderID(new OXFolderAccess(session.getContext()).getDefaultFolder(
							session.getUserId(), FolderObject.CONTACT).getObjectID());
					contactObj.setContextId(session.getContext().getContextId());
					contactInterface.insertContactObject(contactObj);
					/*
					 * Add to list
					 */
					retvalList.add(contactObj);
				} catch (final ConverterException e) {
					throw new MailException(MailException.Code.VRESIT_ERROR, e, e.getLocalizedMessage());
				}
			}
		} catch (final OXException e) {
			throw new MailException(e);
		} finally {
			if (oxc != null) {
				oxc.close();
				oxc = null;
			}
		}
	}

	private static final String VERSIT_VTODO = "VTODO";

	private static final String VERSIT_VEVENT = "VEVENT";

	/**
	 * Saves specified <code>ICalendar</code> mail part into corresponding
	 * default folders. The resulting instances of {@link CommonObject} are
	 * added to given list.
	 * 
	 * @param versitPart
	 *            The <code>ICalendar</code> mail part
	 * @param retvalList
	 *            The list to which resulting instances of {@link CommonObject}
	 *            are added
	 * @param session
	 *            The session providing needed user data
	 * @throws MailException
	 *             If mail part's data cannot be read
	 * @throws IOException
	 *             If mail part's data cannot be parsed
	 * @throws OXException
	 *             If mail part cannot be saved
	 */
	public static void saveICal(final MailPart versitPart, final List<CommonObject> retvalList,
			final SessionObject session) throws MailException, IOException {
		/*
		 * Define versit reader
		 */
		final VersitDefinition def = Versit.getDefinition(versitPart.getContentType().getBaseType());
		final VersitDefinition.Reader r = def.getReader(versitPart.getInputStream(), versitPart.getContentType()
				.containsParameter(PARAM_CHARSET) ? versitPart.getContentType().getParameter(PARAM_CHARSET)
				: MailConfig.getDefaultMimeCharset());
		/*
		 * Ok, convert versit object to corresponding data object and save this
		 * object via its interface
		 */
		OXContainerConverter oxc = null;
		AppointmentSQLInterface appointmentInterface = null;
		TasksSQLInterface taskInterface = null;
		try {
			oxc = new OXContainerConverter(session);
			final VersitObject rootVersitObj = def.parseBegin(r);
			VersitObject vo = null;
			int defaultCalendarFolder = -1;
			int defaultTaskFolder = -1;
			final OXFolderAccess access = new OXFolderAccess(session.getContext());
			while ((vo = def.parseChild(r, rootVersitObj)) != null) {
				try {
					if (VERSIT_VEVENT.equals(vo.name)) {
						/*
						 * An appointment
						 */
						final CalendarDataObject appointmentObj = oxc.convertAppointment(vo);
						appointmentObj.setContext(session.getContext());
						if (defaultCalendarFolder == -1) {
							defaultCalendarFolder = access.getDefaultFolder(session.getUserId(),
									FolderObject.CALENDAR).getObjectID();
						}
						appointmentObj.setParentFolderID(defaultCalendarFolder);
						/*
						 * Create interface if not done, yet
						 */
						if (appointmentInterface == null) {
							appointmentInterface = new CalendarSql(session);
						}
						appointmentInterface.insertAppointmentObject(appointmentObj);
						/*
						 * Add to list
						 */
						retvalList.add(appointmentObj);
					} else if (VERSIT_VTODO.equals(vo.name)) {
						/*
						 * A task
						 */
						final Task taskObj = oxc.convertTask(vo);
						if (defaultTaskFolder == -1) {
							defaultTaskFolder = access.getDefaultFolder(session.getUserId(),
									FolderObject.TASK).getObjectID();
						}
						taskObj.setParentFolderID(defaultTaskFolder);
						/*
						 * Create interface if not done, yet
						 */
						if (taskInterface == null) {
							taskInterface = new TasksSQLInterfaceImpl(session);
						}
						taskInterface.insertTaskObject(taskObj);
						/*
						 * Add to list
						 */
						retvalList.add(taskObj);
					} else {
						if (LOG.isWarnEnabled()) {
							LOG.warn("invalid versit object: " + vo.name);
						}
					}
				} catch (final ConverterException e) {
					throw new MailException(MailException.Code.VRESIT_ERROR, e, e.getLocalizedMessage());
				}
			}
		} catch (final OXException e) {
			throw new MailException(e);
		} finally {
			if (oxc != null) {
				oxc.close();
				oxc = null;
			}
		}
	}
}

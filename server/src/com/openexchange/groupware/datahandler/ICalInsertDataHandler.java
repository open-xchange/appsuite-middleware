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

package com.openexchange.groupware.datahandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.CalendarFields;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataException;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataHandler;
import com.openexchange.conversion.DataProperties;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.CalendarSql;
import com.openexchange.groupware.calendar.Tools;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLInterfaceImpl;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link ICalInsertDataHandler} - The data handler to insert appointments and
 * tasks parsed from an ICal input stream.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class ICalInsertDataHandler implements DataHandler {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(ICalInsertDataHandler.class);

	private static final String[] ARGS = { "com.openexchange.groupware.calendar.folder",
			"com.openexchange.groupware.task.folder" };

	private static final Class<?>[] TYPES = { InputStream.class };

	/**
	 * Initializes a new {@link ICalInsertDataHandler}
	 */
	public ICalInsertDataHandler() {
		super();
	}

	public String[] getRequiredArguments() {
		return ARGS;
	}

	public Class<?>[] getTypes() {
		return TYPES;
	}

	public Object processData(final Data<? extends Object> data, final DataArguments dataArguments,
			final Session session) throws DataException {
		final int calendarFolder;
		try {
			calendarFolder = Integer.parseInt(dataArguments.get(ARGS[0]));
		} catch (final NumberFormatException e) {
			throw DataExceptionCodes.INVALID_ARGUMENT.create(ARGS[0], e, dataArguments.get(ARGS[0]));
		}
		final int taskFolder;
		try {
			taskFolder = Integer.parseInt(dataArguments.get(ARGS[1]));
		} catch (final NumberFormatException e) {
			throw DataExceptionCodes.INVALID_ARGUMENT.create(ARGS[1], e, dataArguments.get(ARGS[1]));
		}
		final Context ctx;
		try {
			ctx = ContextStorage.getStorageContext(session);
		} catch (final ContextException e) {
			throw new DataException(e);
		}
		final ICalParser iCalParser = ServerServiceRegistry.getInstance().getService(ICalParser.class);
		if (iCalParser == null) {
			throw new DataException(new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, ICalParser.class
					.getName()));
		}
		final List<CalendarDataObject> appointments;
		final List<Task> tasks;
		final InputStreamCopy inputStreamCopy;
		{
			long size;
			try {
				size = Long.parseLong(data.getDataProperties().get(DataProperties.PROPERTY_SIZE));
			} catch (final NumberFormatException e) {
				size = 0;
			}
			inputStreamCopy = copyStream((InputStream) data.getData(), size);
		}
		try {
			/*
			 * Get user time zone
			 */
			final TimeZone defaultZone = Tools.getTimeZone(UserStorage.getStorageUser(session.getUserId(), ctx)
					.getTimeZone());
			/*
			 * Errors and warnings
			 */
			final List<ConversionError> conversionErrors = new ArrayList<ConversionError>(4);
			final List<ConversionWarning> conversionWarnings = new ArrayList<ConversionWarning>(4);
			{
				/*
				 * Start parsing appointments
				 */
				appointments = parseAppointmentStream(ctx, iCalParser, inputStreamCopy, conversionErrors,
						conversionWarnings, defaultZone);
				// TODO: Handle errors/warnings
				conversionErrors.clear();
				conversionWarnings.clear();
				/*
				 * Start parsing tasks
				 */
				tasks = parseTaskStream(ctx, iCalParser, inputStreamCopy, conversionErrors, conversionWarnings,
						defaultZone);
			}
		} catch (final ConversionError e) {
			throw new DataException(e);
		} catch (final IOException e) {
			throw DataExceptionCodes.ERROR.create(e, e.getMessage());
		} finally {
			inputStreamCopy.close();
		}
		/*
		 * The JSON array to return
		 */
		final JSONArray folderAndIdArray = new JSONArray();
		if (!appointments.isEmpty()) {
			/*
			 * Insert parsed appointments into denoted calendar folder
			 */
			try {
				insertAppointments(session, calendarFolder, ctx, appointments, folderAndIdArray);
			} catch (final OXException e) {
				throw new DataException(e);
			} catch (final JSONException e) {
				throw new DataException(new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]));
			}
		}
		if (!tasks.isEmpty()) {
			/*
			 * Insert parsed tasks into denoted task folder
			 */
			try {
				insertTasks(session, taskFolder, tasks, folderAndIdArray);
			} catch (final OXException e) {
				throw new DataException(e);
			} catch (final JSONException e) {
				throw new DataException(new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]));
			}
		}
		return folderAndIdArray;
	}

	private void insertTasks(final Session session, final int taskFolder, final List<Task> tasks,
			final JSONArray folderAndIdArray) throws OXException, JSONException {
		final TasksSQLInterface taskSql = new TasksSQLInterfaceImpl(session);
		for (final Task task : tasks) {
			task.setParentFolderID(taskFolder);
			taskSql.insertTaskObject(task);
			folderAndIdArray.put(new JSONObject().put(CalendarFields.FOLDER_ID, taskFolder).put(CalendarFields.ID,
					task.getObjectID()));
		}
	}

	private void insertAppointments(final Session session, final int calendarFolder, final Context ctx,
			final List<CalendarDataObject> appointments, final JSONArray folderAndIdArray) throws OXException,
			JSONException {
		final AppointmentSQLInterface appointmentSql = new CalendarSql(session);
		for (final CalendarDataObject appointment : appointments) {
			appointment.setParentFolderID(calendarFolder);
			appointment.setContext(ctx);
			appointment.setIgnoreConflicts(true);
			appointmentSql.insertAppointmentObject(appointment);
			folderAndIdArray.put(new JSONObject().put(CalendarFields.FOLDER_ID, calendarFolder).put(CalendarFields.ID,
					appointment.getObjectID()));
		}
	}

	private List<CalendarDataObject> parseAppointmentStream(final Context ctx, final ICalParser iCalParser,
			final InputStreamCopy inputStreamCopy, final List<ConversionError> conversionErrors,
			final List<ConversionWarning> conversionWarnings, final TimeZone defaultZone) throws IOException,
			ConversionError {
		final InputStream inputStream = inputStreamCopy.getInputStream();
		try {
			return iCalParser.parseAppointments(inputStream, defaultZone, ctx, conversionErrors, conversionWarnings);
		} finally {
			try {
				inputStream.close();
			} catch (final IOException e) {
				LOG.error("Error closing input stream", e);
			}
		}
	}

	private List<Task> parseTaskStream(final Context ctx, final ICalParser iCalParser,
			final InputStreamCopy inputStreamCopy, final List<ConversionError> conversionErrors,
			final List<ConversionWarning> conversionWarnings, final TimeZone defaultZone) throws IOException,
			ConversionError {
		final InputStream inputStream = inputStreamCopy.getInputStream();
		try {
			return iCalParser.parseTasks(inputStream, defaultZone, ctx, conversionErrors, conversionWarnings);
		} finally {
			try {
				inputStream.close();
			} catch (final IOException e) {
				LOG.error("Error closing input stream", e);
			}
		}
	}

	private static final int LIMIT = 1048576;

	private static InputStreamCopy copyStream(final InputStream orig, final long size) throws DataException {
		try {
			return new InputStreamCopy(orig, (size <= 0 || size > LIMIT));
		} catch (final IOException e) {
			throw DataExceptionCodes.ERROR.create(e, e.getMessage());
		}
	}

	private static final class InputStreamCopy {

		private static final int DEFAULT_BUF_SIZE = 0x2000;

		private static final String FILE_PREFIX = "openexchange";

		private byte[] bytes;

		private File file;

		private final long size;

		public InputStreamCopy(final InputStream orig, final boolean createFile) throws IOException {
			super();
			if (createFile) {
				size = copy2File(orig);
			} else {
				size = copy2ByteArr(orig);
			}
		}

		public InputStream getInputStream() throws IOException {
			return bytes == null ? (file == null ? null : (new BufferedInputStream(new FileInputStream(file),
					DEFAULT_BUF_SIZE))) : (new UnsynchronizedByteArrayInputStream(bytes));
		}

		public long getSize() {
			return size;
		}

		public void close() {
			if (file != null) {
				if (file.exists()) {
					file.delete();
				}
				file = null;
			}
			if (bytes != null) {
				bytes = null;
			}
		}

		private int copy2ByteArr(final InputStream in) throws IOException {
			final ByteArrayOutputStream out = new UnsynchronizedByteArrayOutputStream(DEFAULT_BUF_SIZE << 1);
			final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
			int len;
			while ((len = in.read(bbuf)) != -1) {
				out.write(bbuf, 0, len);
			}
			out.flush();
			this.bytes = out.toByteArray();
			return bytes.length;
		}

		private long copy2File(final InputStream in) throws IOException {
			long totalBytes = 0;
			{
				final File tmpFile = File.createTempFile(FILE_PREFIX, null, new File(ServerConfig
						.getProperty(ServerConfig.Property.UploadDirectory)));
				tmpFile.deleteOnExit();
				OutputStream out = null;
				try {
					out = new BufferedOutputStream(new FileOutputStream(tmpFile), DEFAULT_BUF_SIZE);
					final byte[] bbuf = new byte[DEFAULT_BUF_SIZE];
					int len;
					while ((len = in.read(bbuf)) != -1) {
						out.write(bbuf, 0, len);
						totalBytes += len;
					}
					out.flush();
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (final IOException e) {
							LOG.error(e.getMessage(), e);
						}
					}
				}
				file = tmpFile;
			}
			return totalBytes;
		}
	}
}

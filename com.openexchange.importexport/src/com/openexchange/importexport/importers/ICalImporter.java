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

package com.openexchange.importexport.importers;

import static com.openexchange.java.Autoboxing.I;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.ical.ImportedComponent;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.data.conversion.ical.ParseResult;
import com.openexchange.data.conversion.ical.TruncationInfo;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.exception.OXException.ProblematicAttribute;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.Participant;
import com.openexchange.groupware.container.UserParticipant;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.importexport.ImportResult;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.osgi.ImportExportServices;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.exceptions.SimpleTruncatedAttribute;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;

/**
 * Imports ICal files. ICal files can be translated to either tasks or
 * appointments within the OX, so the importer works with both SQL interfaces.
 *
 * @see AppointmentSQLInterface AppointmentSQLInterface - if you have a problem
 *      entering the parsed entry as Appointment
 * @see TasksSQLInterface TasksSQLInterface - if you have trouble entering the
 *      parsed entry as Task
 * @author <a href="mailto:sebastian.kauss@open-xchange.com">Sebastian Kauss</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb'
 *         Prinz</a> (changes to new interface, bugfixes, maintenance)
 */
public class ICalImporter extends AbstractImporter {

    public ICalImporter(ServiceLookup services) {
        super(services);
    }

    private static final int APP = 0;
	private static final int TASK = 1;

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ICalImporter.class);

	@Override
    public boolean canImport(final ServerSession session, final Format format,
			final List<String> folders,
			final Map<String, String[]> optionalParams)
			throws OXException {
		if (!format.equals(Format.ICAL)) {
			return false;
		}
		final OXFolderAccess folderAccess = new OXFolderAccess(
				session.getContext());
		final Iterator<String> iterator = folders.iterator();
		while (iterator.hasNext()) {
			final String folder = iterator.next();

			int folderId = 0;
			try {
				folderId = Integer.parseInt(folder);
			} catch (final NumberFormatException e) {
				throw ImportExportExceptionCodes.NUMBER_FAILED
						.create(e, folder);
			}

			FolderObject fo;
			try {
				fo = folderAccess.getFolderObject(folderId);
			} catch (final OXException e) {
				return false;
			}

			// check format of folder
			final int module = fo.getModule();
			if (module == FolderObject.CALENDAR) {
				if (!UserConfigurationStorage
						.getInstance()
						.getUserConfigurationSafe(session.getUserId(),
								session.getContext()).hasCalendar()) {
					return false;
				}
			} else if (module == FolderObject.TASK) {
				if (!UserConfigurationStorage
						.getInstance()
						.getUserConfigurationSafe(session.getUserId(),
								session.getContext()).hasTask()) {
					return false;
				}
			} else {
				return false;
			}

			// check read access to folder
			EffectivePermission perm;
			try {
				perm = fo.getEffectiveUserPermission(
						session.getUserId(),
						UserConfigurationStorage.getInstance()
								.getUserConfigurationSafe(session.getUserId(),
										session.getContext()));
			} catch (final OXException e) {
				throw ImportExportExceptionCodes.NO_DATABASE_CONNECTION
						.create(e);
			} catch (final RuntimeException e) {
				throw ImportExportExceptionCodes.SQL_PROBLEM.create(e,
						e.getMessage());
			}

			if (!perm.canCreateObjects()) {
				return false;
			}
		}
		return true;
	}

	private int[] determineFolders(final ServerSession session, final List<String> folders,
			final Format format) throws OXException {
		final int[] result = new int[] { -1, -1 };
		final OXFolderAccess folderAccess = new OXFolderAccess(
				session.getContext());
		final Iterator<String> iterator = folders.iterator();
		while (iterator.hasNext()) {
			final int folderId = Integer.parseInt(iterator.next());
			FolderObject fo;
			try {
				fo = folderAccess.getFolderObject(folderId);
			} catch (final OXException e) {
				throw ImportExportExceptionCodes.LOADING_FOLDER_FAILED.create(e, I(folderId));
			}
			if (fo.getModule() == FolderObject.CALENDAR) {
				result[APP] = folderId;
			} else if (fo.getModule() == FolderObject.TASK) {
				result[TASK] = folderId;
			} else {
				throw ImportExportExceptionCodes.CANNOT_IMPORT.create(I(folderId), format);
			}
		}
		return result;
	}

	@Override
    public ImportResults importData(final ServerSession session,
			final Format format, final InputStream is,
			final List<String> folders,
			final Map<String, String[]> optionalParams)
			throws OXException {
		final int[] res = determineFolders(session, folders, format);
		final int appointmentFolderId = res[APP];
		final int taskFolderId = res[TASK];

		final AppointmentSQLInterface appointmentInterface = retrieveAppointmentInterface(
				appointmentFolderId, session);
		final TasksSQLInterface taskInterface = retrieveTaskInterface(
				taskFolderId, session);
		final ICalParser parser = ImportExportServices.getIcalParser();
		final Context ctx = session.getContext();
		final TimeZone defaultTz = TimeZoneUtils.getTimeZone(UserStorage
				.getInstance().getUser(session.getUserId(), ctx).getTimeZone());

		final List<ImportResult> list = new ArrayList<ImportResult>();
		final List<ConversionError> errors = new ArrayList<ConversionError>();
		final List<ConversionWarning> warnings = new ArrayList<ConversionWarning>();
		TruncationInfo truncationInfo = null;

		if (appointmentFolderId != -1) {
            if (ImportExportServices.LOOKUP.get().getService(CalendarService.class).init(session).getConfig().isUseLegacyStack()) {
                truncationInfo = importAppointment(session, is, optionalParams, appointmentFolderId,
                    appointmentInterface, parser, ctx, defaultTz, list, errors,
                    warnings);
		    } else {
                //TODO: import events instead
	            truncationInfo = importAppointment(session, is, optionalParams, appointmentFolderId,
                    appointmentInterface, parser, ctx, defaultTz, list, errors,
                    warnings);
		    }
		}
		if (taskFolderId != -1) {
			truncationInfo = importTask(is, optionalParams, taskFolderId, taskInterface, parser, ctx, defaultTz,
					list, errors, warnings);
		}
		return new DefaultImportResults(list, truncationInfo);
	}

	private TruncationInfo importTask(final InputStream is, final Map<String, String[]> optionalParams, final int taskFolderId,
			final TasksSQLInterface taskInterface, final ICalParser parser,
			final Context ctx, final TimeZone defaultTz,
			final List<ImportResult> list, final List<ConversionError> errors,
			final List<ConversionWarning> warnings)
			throws OXException {
		ParseResult<Task> tasks = parser.parseTasks(is, defaultTz, ctx, errors, warnings);
		TruncationInfo truncationInfo = tasks.getTruncationInfo();
		final TIntObjectMap<ConversionError> errorMap = new TIntObjectHashMap<ConversionError>();

		for (final ConversionError error : errors) {
			errorMap.put(error.getIndex(), error);
		}

		final TIntObjectMap<List<ConversionWarning>> warningMap = new TIntObjectHashMap<List<ConversionWarning>>();

		for (final ConversionWarning warning : warnings) {
			List<ConversionWarning> warningList = warningMap.get(warning
					.getIndex());
			if (warningList == null) {
				warningList = new LinkedList<ConversionWarning>();
				warningMap.put(warning.getIndex(), warningList);
			}
			warningList.add(warning);
		}

        boolean ignoreUIDs = isIgnoreUIDs(optionalParams);
        Map<String, String> uidReplacements = ignoreUIDs ? new HashMap<String, String>() : null;
		int index = 0;
		final Iterator<Task> iter = tasks.getImportedObjects().iterator();
		while (iter.hasNext()) {
			final ImportResult importResult = new ImportResult();
			final ConversionError error = errorMap.get(index);
			if (error != null) {
				errorMap.remove(index);
				importResult.setException(error);
			} else {
				// IGNORE WARNINGS. Protocol doesn't allow for warnings.
				// TODO: Verify This
				final Task task = iter.next();
				task.setParentFolderID(taskFolderId);
                if (ignoreUIDs && task.containsUid()) {
                    // perform fixed UID replacement to keep recurring task relations
                    String originalUID = task.getUid();
                    String replacedUID = uidReplacements.get(originalUID);
                    if (null == replacedUID) {
                        replacedUID = UUID.randomUUID().toString();
                        uidReplacements.put(originalUID, replacedUID);
                    }
                    task.setUid(replacedUID);
                }
				try {
					taskInterface.insertTaskObject(task);
					importResult.setObjectId(String.valueOf(task
							.getObjectID()));
					importResult.setDate(task.getLastModified());
					importResult.setFolder(String.valueOf(taskFolderId));
				} catch (final OXException e) {
					LOG.error("", e);
					importResult.setException(e);
				}

				final List<ConversionWarning> warningList = warningMap
						.get(index);
				if (warningList != null) {
					importResult.addWarnings(warningList);
					importResult
							.setException(ImportExportExceptionCodes.WARNINGS
									.create(I(warningList.size())));
				}
			}
			importResult.setEntryNumber(index);
			list.add(importResult);
			index++;
		}
		if (!errorMap.isEmpty()) {
			errorMap.forEachValue(new TObjectProcedure<ConversionError>() {

				@Override
                public boolean execute(final ConversionError error) {
					final ImportResult importResult = new ImportResult();
					importResult.setEntryNumber(error.getIndex());
					importResult.setException(error);
					list.add(importResult);
					return true;
				}
			});
		}
		return truncationInfo;
	}

    /**
     * Imports events from the iCalendar input stream into specific folder.
     *
     * @param session The session
     * @param inputStream The input stream carrying the iCalendar data
     * @param optionalParameters A map of optional parameters for the import
     * @param folderId The identifier of the target folder for the import
     * @return The import results
     */
    private List<ImportResult> importEvents(ServerSession session, InputStream inputStream, Map<String, String[]> optionalParameters, String folderId) throws OXException {
        /*
         * initialize import
         */
        CalendarService calendarService = ImportExportServices.LOOKUP.get().getService(CalendarService.class);
        CalendarSession calendarSession = calendarService.init(session);
        calendarSession.set(CalendarParameters.PARAMETER_IGNORE_CONFLICTS, Boolean.TRUE);
        if (null != optionalParameters && optionalParameters.containsKey("suppressNotification")) {
            calendarSession.set(CalendarParameters.PARAMETER_NOTIFICATION, Boolean.FALSE);
        }
        ICalService iCalService = ImportExportServices.LOOKUP.get().getService(ICalService.class);
        ICalParameters iCalParameters = iCalService.initParameters();
        iCalParameters.set(ICalParameters.DEFAULT_TIMEZONE, TimeZone.getTimeZone(session.getUser().getTimeZone()));
        iCalParameters.set(ICalParameters.SANITIZE_INPUT, Boolean.TRUE);
        /*
         * perform the import
         */
        ImportedCalendar calendarImport;
        try {
            calendarImport = iCalService.importICal(inputStream, iCalParameters);
        } catch (OXException e) {
            if ("ICAL-0003".equals(e.getErrorCode())) {
                // "No calendar data found", silently ignore, as expected by com.openexchange.ajax.importexport.Bug9209Test.test9209ICal()
                return Collections.emptyList();
            }
            ImportResult result = new ImportResult();
            result.setException(e);
            return Collections.singletonList(result);
        }
        boolean ignoreUIDs = isIgnoreUIDs(optionalParameters);
        List<ImportResult> importResults = new ArrayList<ImportResult>();
        for (Map.Entry<String, List<Event>> entry : getEventsByUID(calendarImport.getEvents(), true).entrySet()) {
            List<Event> events = sortSeriesMasterFirst(entry.getValue());
            /*
             * (re-) assign UID to imported event if required
             */
            String uid = entry.getKey();
            if (null == uid || ignoreUIDs) {
                uid = UUID.randomUUID().toString();
                for (Event event : events) {
                    event.setUid(uid);
                }
            }
            /*
             * create first event (master or non-recurring)
             */
            ImportResult result = createEvent(calendarSession, folderId, events.get(0));
            importResults.add(result);
            /*
             * create further events as change exceptions
             */
            if (1 < events.size() && false == result.hasError()) {
                EventID masterEventID = new EventID(folderId, result.getObjectId());
                for (int i = 1; i < events.size(); i++) {
                    importResults.add(createEventException(calendarSession, masterEventID, events.get(i)));
                }
            }
        }
        return importResults;
    }

	private TruncationInfo importAppointment(final ServerSession session,
			final InputStream is, final Map<String, String[]> optionalParams,
			final int appointmentFolderId,
			final AppointmentSQLInterface appointmentInterface,
			final ICalParser parser, final Context ctx,
			final TimeZone defaultTz, final List<ImportResult> list,
			final List<ConversionError> errors,
			final List<ConversionWarning> warnings)
			throws OXException {
	    ParseResult<CalendarDataObject> parseResult = parser.parseAppointments(is, defaultTz, ctx, errors, warnings);
	    List<CalendarDataObject> appointments = parseResult.getImportedObjects();
	    TruncationInfo truncationInfo = parseResult.getTruncationInfo();
		final TIntObjectMap<ConversionError> errorMap = new TIntObjectHashMap<ConversionError>();

		for (final ConversionError error : errors) {
			errorMap.put(error.getIndex(), error);
		}
		if (null == appointments) {
		    appointments = Collections.<CalendarDataObject> emptyList();
		}

		sortSeriesMastersFirst(appointments);
		final Map<Integer, Integer> pos2Master = handleChangeExceptions(appointments);
		final Map<Integer, Integer> master2id = new HashMap<Integer,Integer>();

		final TIntObjectMap<List<ConversionWarning>> warningMap = new TIntObjectHashMap<List<ConversionWarning>>();

		for (final ConversionWarning warning : warnings) {
			List<ConversionWarning> warningList = warningMap.get(warning
					.getIndex());
			if (warningList == null) {
				warningList = new LinkedList<ConversionWarning>();
				warningMap.put(warning.getIndex(), warningList);
			}
			warningList.add(warning);
		}

		int index = 0;
		final Iterator<CalendarDataObject> iter = appointments.iterator();

		final boolean suppressNotification = (optionalParams != null && optionalParams
				.containsKey("suppressNotification"));
		boolean ignoreUIDs = isIgnoreUIDs(optionalParams);
		Map<String, String> uidReplacements = ignoreUIDs ? new HashMap<String, String>() : null;
		while (iter.hasNext()) {
			final ImportResult importResult = new ImportResult();
			final ConversionError error = errorMap.get(index);
			if (error != null) {
				errorMap.remove(index);
				importResult.setException(error);
			} else {
				final CalendarDataObject appointmentObj = iter.next();
				appointmentObj.setContext(session.getContext());
				appointmentObj.setParentFolderID(appointmentFolderId);
				appointmentObj.setIgnoreConflicts(true);
				appointmentObj.removeLastModified();
				if (ignoreUIDs && appointmentObj.containsUid()) {
				    // perform fixed UID replacement to keep recurring appointment relations
				    String originalUID = appointmentObj.getUid();
				    String replacedUID = uidReplacements.get(originalUID);
				    if (null == replacedUID) {
				        replacedUID = UUID.randomUUID().toString();
				        uidReplacements.put(originalUID, replacedUID);
				    }
				    appointmentObj.setUid(replacedUID);
				}
				OXFolderAccess folderAccess = new OXFolderAccess(session.getContext());
				FolderObject folder = folderAccess.getFolderObject(appointmentFolderId);
				if (folder.getType() == FolderObject.PUBLIC) {
				    if (appointmentObj.getParticipants() != null && appointmentObj.getParticipants().length > 0 && appointmentObj.getPrivateFlag()) {
				        appointmentObj.removeParticipants();
			            warnings.add(new ConversionWarning(index, ConversionWarning.Code.PRIVATE_APPOINTMENTS_HAVE_NO_PARTICIPANTS));
			            appointmentObj.setPrivateFlag(false);
			        }
				}
				if (suppressNotification) {
					appointmentObj.setNotification(false);
				}
				// Check for possible full-time appointment
				check4FullTime(appointmentObj);
				/*
				 * ensure there is at least one internal participant
				 */
				addUserParticipantIfNeeded(session, folder, appointmentObj);
				try {
					final boolean isMaster = appointmentObj.containsUid() && !pos2Master.containsKey(index);
					final boolean isChange = appointmentObj.containsUid() && pos2Master.containsKey(index);
					final Date changeDate = new Date(Long.MAX_VALUE);
					Appointment[] conflicts = null;
					if(isChange){
					    final Integer masterPos = pos2Master.get(index);
	                    final Integer masterID = master2id.get(masterPos.intValue());
					    if (masterID == null) {
	                        /*
	                         * In this case the current appointment is a change exception but the corresponding master
	                         * could not be inserted before. As the result list already contains an error message for the
	                         * master, we simply skip this one without generating an import result.
	                         */
	                        continue;
	                    } else {
	                        appointmentObj.setRecurrenceID(masterID);
	                        appointmentObj.removeUid();
	                        if(appointmentObj.containsRecurrenceDatePosition()) {
	                            appointmentObj.setRecurrenceDatePosition(calculateRecurrenceDatePosition(appointmentObj.getRecurrenceDatePosition()));
	                        } else {
	                            appointmentObj.setRecurrenceDatePosition(calculateRecurrenceDatePosition(appointmentObj.getStartDate()));
	                        }

	                        appointmentObj.setObjectID(masterID);
	                        conflicts = appointmentInterface.updateAppointmentObject(appointmentObj, appointmentFolderId, changeDate);
	                    }
					} else {
					    conflicts = appointmentInterface.insertAppointmentObject(appointmentObj);
					}

					if(isMaster) {
                        master2id.put(index, appointmentObj.getObjectID());
                    }

					if (conflicts == null || conflicts.length == 0) {
						importResult.setObjectId(String
								.valueOf(appointmentObj.getObjectID()));
						importResult.setDate(appointmentObj
								.getLastModified());
						importResult.setFolder(String
								.valueOf(appointmentFolderId));
					} else {
						importResult
								.setException(ImportExportExceptionCodes.RESOURCE_HARD_CONFLICT
										.create());
					}
				} catch (final OXException e) {
					OXException ne = makeMoreInformative(e);
					//LOG.error("", ne); //removed logging, because this would be a user error spamming our log.
					importResult.setException(ne);
				}
				final List<ConversionWarning> warningList = warningMap.get(index);
				if (warningList != null) {
					importResult.addWarnings(warningList);
					importResult
							.setException(ImportExportExceptionCodes.WARNINGS
									.create(I(warningList.size())));
				}
			}
			importResult.setEntryNumber(index);
			list.add(importResult);
			index++;
		}
		if (!errorMap.isEmpty()) {
			errorMap.forEachValue(new TObjectProcedure<ConversionError>() {

				@Override
                public boolean execute(final ConversionError error) {
					final ImportResult importResult = new ImportResult();
					importResult.setEntryNumber(error.getIndex());
					importResult.setException(error);
					list.add(importResult);
					return true;
				}
			});
		}
		return truncationInfo;
	}

    private OXException makeMoreInformative(OXException e) {
		if( e.getCategory() == Category.CATEGORY_TRUNCATED){
			ProblematicAttribute[] problematics = e.getProblematics();
			StringBuilder bob = new StringBuilder();
			for(ProblematicAttribute att: problematics){
				if((att instanceof SimpleTruncatedAttribute) && ((SimpleTruncatedAttribute) att).getValue() != null){
					SimpleTruncatedAttribute temp = (SimpleTruncatedAttribute) att;
					bob.append(temp.getValue());
					bob.append(" (>");
					bob.append(temp.getMaxSize());
					bob.append(")\n");
				}
			}
			OXException exception = ImportExportExceptionCodes.TRUNCATION.create(bob.toString());
			for (ProblematicAttribute problematic : problematics) {
	            exception.addProblematic(problematic);
            }
			return exception;
		}
		return e;
	}

	private Date calculateRecurrenceDatePosition(final Date startDate) {
		final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cal.setTime(startDate);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	private void sortSeriesMastersFirst(final List<CalendarDataObject> appointments) {
		Collections.sort(appointments, new Comparator<CalendarDataObject>(){
			@Override
            public int compare(final CalendarDataObject o1, final CalendarDataObject o2) {
				if( o1.containsRecurrenceType() && !o2.containsRecurrenceType()) {
                    return -1;
                }
				if( !o1.containsRecurrenceType() && o2.containsRecurrenceType()) {
                    return 1;
                }
				return 0;
			}});
	}
	/**
	 * @return mapping for position of a recurrence in the list to the position of the recurrence master
	 */
	private Map<Integer, Integer> handleChangeExceptions(final List<CalendarDataObject> appointments) {
		final Map<String, Integer> uid2master = new HashMap<String,Integer>();
		final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		CalendarDataObject app;

		//find master
		for(int pos = 0, len = appointments.size(); pos < len; pos++){
			app = appointments.get(pos);
			if(! app.containsUid()) {
                continue;
            }

			final String uid = app.getUid();
			if(! uid2master.containsKey(uid)) {
                uid2master.put(uid, pos);
            }
		}

		//references to master
		for(int pos = 0, len = appointments.size(); pos < len; pos++){
			app = appointments.get(pos);
			if(! app.containsUid()) {
                continue;
            }

			final String uid = app.getUid();
			final Integer masterPos = uid2master.get(uid);

			if(pos > masterPos) {
                map.put(pos, uid2master.get(uid));
            }
		}

		return map;
	}


	private AppointmentSQLInterface retrieveAppointmentInterface(
			final int appointmentFolderId, final ServerSession session)
			throws OXException {
		if (appointmentFolderId == -1) {
            return null;
        }

		if (!UserConfigurationStorage
				.getInstance()
				.getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasCalendar()) {
            throw ImportExportExceptionCodes.CALENDAR_DISABLED
					.create().setGeneric(Generic.NO_PERMISSION);
        }

		return ImportExportServices.getAppointmentFactoryService()
				.createAppointmentSql(session);
	}

	private TasksSQLInterface retrieveTaskInterface(final int taskFolderId,
			final ServerSession session) throws OXException {
		if (taskFolderId == -1) {
            return null;
        }
		if (!UserConfigurationStorage
				.getInstance()
				.getUserConfigurationSafe(session.getUserId(),
						session.getContext()).hasTask()) {
            throw ImportExportExceptionCodes.TASKS_DISABLED
					.create().setGeneric(Generic.NO_PERMISSION);
        }

		return new TasksSQLImpl(session);
	}

	/**
	 * Checks if specified appointment lasts exactly one day; if so treat it as
	 * a full-time appointment through setting
	 * {@link CalendarDataObject#setFullTime(boolean)} to <code>true</code>.
	 * <p>
	 * Moreover its start/end date is changed to match the date in UTC time
	 * zone.
	 *
	 * @param appointmentObj
	 *            The appointment to check
	 */
	private void check4FullTime(final Appointment appointmentObj) {
		final long start = appointmentObj.getStartDate().getTime();
		final long end = appointmentObj.getEndDate().getTime();
		if (Constants.MILLI_DAY == (end - start)) {
			// Appointment exactly lasts one day; assume a full-time appointment
			appointmentObj.setFullTime(true);
			// Adjust start/end to UTC date's zero time; e.g.
			// "13. January 2009 00:00:00 UTC"
			final TimeZone tz = ImportExportServices.getCalendarCollectionService()
					.getTimeZone(appointmentObj.getTimezone());
			long offset = tz.getOffset(start);
			appointmentObj.setStartDate(new Date(start + offset));
			offset = tz.getOffset(end);
			appointmentObj.setEndDate(new Date(end + offset));
		}
	}

    /**
     * Adds the user (or the owner of the shared folder) to the list of participants if needed, i.e. the appointment not yet has any
     * internal user participants.
     *
     * @param session The session
     * @param targetFolder The target folder
     * @param appointment The appointment to add the participant if needed
     * @return <code>true</code> if the participant was added, <code>false</code>, otherwise
     */
    private static boolean addUserParticipantIfNeeded(ServerSession session, FolderObject targetFolder, Appointment appointment) {
        Participant[] currentParticipants = appointment.getParticipants();
        boolean containsInternalParticpiant = false;
        if (null != currentParticipants && 0 < currentParticipants.length) {
            for (Participant participant : appointment.getParticipants()) {
                if (Participant.GROUP == participant.getType() || Participant.RESOURCE == participant.getType() ||
                    Participant.USER == participant.getType() || Participant.RESOURCEGROUP == participant.getType()) {
                    containsInternalParticpiant = true;
                    break;
                }
            }
        }
        if (false == containsInternalParticpiant) {
            UserParticipant userParticipant = new UserParticipant(targetFolder.getType() == FolderObject.PUBLIC ? session.getUserId() : targetFolder.getCreatedBy());
            userParticipant.setConfirm(Appointment.ACCEPT);
            Participant[] newParticipants = null == currentParticipants ?
                new Participant[1] : Arrays.copyOf(currentParticipants, 1 + currentParticipants.length);
            newParticipants[newParticipants.length - 1] = userParticipant;
            appointment.setParticipants(newParticipants);
            return true;
        }
        return false;
    }

    /**
     * Gets a value whether the supplied parameters indicate that UIDs should be ignored during import or not.
     *
     * @param optionalParams The optional parameters as passed from the import request, may be <code>null</code>
     * @return <code>true</code> if UIDs should be ignored, <code>false</code>, otherwise
     */
    private static boolean isIgnoreUIDs(Map<String, String[]> optionalParams) {
        if (null != optionalParams) {
            String[] value = optionalParams.get("ignoreUIDs");
            if (null != value && 0 < value.length) {
                return Boolean.valueOf(value[0]).booleanValue();
            }
        }
        return false;
    }

    private static ImportResult prepareResult(Event importedEvent) {
        ImportResult importResult = new ImportResult();
        if (ImportedComponent.class.isInstance(importedEvent)) {
            ImportedComponent component = (ImportedComponent) importedEvent;
            importResult.setEntryNumber(component.getIndex());
            List<OXException> importWarnings = component.getWarnings();
            if (null != importWarnings && 0 < importWarnings.size()) {
                List<ConversionWarning> conversionWarnings = new ArrayList<ConversionWarning>(importWarnings.size());
                for (OXException importWarning : importWarnings) {
                    conversionWarnings.add(new ConversionWarning(component.getIndex(), importWarning));
                }
                importResult.addWarnings(conversionWarnings);
            }
        }
        return importResult;
    }

    private static ImportResult createEvent(CalendarSession session, String folderId, Event importedEvent) {
        final int MAX_RETRIES = 5;
        ImportResult importResult = prepareResult(importedEvent);
        for (int retryCount = 1; retryCount <= MAX_RETRIES; retryCount++) {
            try {
                CalendarResult result = session.getCalendarService().createEvent(session, folderId, importedEvent);
                importResult.setDate(new Date(result.getTimestamp()));
                session.set(CalendarParameters.PARAMETER_TIMESTAMP, Long.valueOf(result.getTimestamp()));
                if (result.getCreations().isEmpty()) {
                    importResult.setException(ImportExportExceptionCodes.COULD_NOT_CREATE.create(importedEvent));
                } else {
                    importResult.setFolder(result.getCreations().get(0).getCreatedEvent().getFolderId());
                    importResult.setObjectId(result.getCreations().get(0).getCreatedEvent().getId());
                }
            } catch (OXException e) {
                if (retryCount < MAX_RETRIES && handle(session, e, importedEvent)) {
                    // try again
                    LOG.debug("{} - trying again ({}/{})", e.getMessage(), retryCount, MAX_RETRIES, e);
                    importResult.addWarnings(Collections.singletonList(
                        new ConversionWarning(importResult.getEntryNumber(), ConversionWarning.Code.TRUNCATION_WARNING, e.getMessage())));
                    continue;
                }
                // "re-throw"
                importResult.setException(e);
            }
            if (false == importResult.hasError() && null != importResult.getWarnings() && 0 < importResult.getWarnings().size()) {
                importResult.setException(ImportExportExceptionCodes.WARNINGS.create(I(importResult.getWarnings().size())));
            }
            return importResult;
        }
        throw new AssertionError(); // should not get here
    }

    private static ImportResult createEventException(CalendarSession session, EventID masterEventID, Event importedException) {
        final int MAX_RETRIES = 5;
        ImportResult importResult = prepareResult(importedException);
        for (int retryCount = 1; retryCount <= MAX_RETRIES; retryCount++) {
            try {
                CalendarResult result = session.getCalendarService().updateEvent(session, masterEventID, importedException);
                importResult.setDate(new Date(result.getTimestamp()));
                session.set(CalendarParameters.PARAMETER_TIMESTAMP, Long.valueOf(result.getTimestamp()));
                if (result.getCreations().isEmpty()) {
                    importResult.setException(ImportExportExceptionCodes.COULD_NOT_CREATE.create(importedException));
                } else {
                    importResult.setFolder(String.valueOf(result.getCreations().get(0).getCreatedEvent().getFolderId()));
                    importResult.setObjectId(String.valueOf(result.getCreations().get(0).getCreatedEvent().getId()));
                }
            } catch (OXException e) {
                if (retryCount < MAX_RETRIES && handle(session, e, importedException)) {
                    // try again
                    LOG.debug("{} - trying again ({}/{})", e.getMessage(), retryCount, MAX_RETRIES, e);
                    importResult.addWarnings(Collections.singletonList(
                        new ConversionWarning(importResult.getEntryNumber(), ConversionWarning.Code.TRUNCATION_WARNING, e.getMessage())));
                    continue;
                }
                // "re-throw"
                importResult.setException(e);
            }
            if (false == importResult.hasError() && null != importResult.getWarnings() && 0 < importResult.getWarnings().size()) {
                importResult.setException(ImportExportExceptionCodes.WARNINGS.create(I(importResult.getWarnings().size())));
            }
            return importResult;
        }
        throw new AssertionError(); // should not get here
    }

    /**
     * Maps a list of events based on their UID property, so that each event series including any change exceptions are grouped separately.
     *
     * @param events The events to map
     * @param assignIfEmpty <code>true</code> to assign a new unique identifier in case it's missing from an event, <code>false</code>, otherwise
     * @return The events, mapped by their unique identifier
     */
    private static Map<String, List<Event>> getEventsByUID(List<Event> events, boolean assignIfEmpty) {
        if (null == events) {
            return Collections.emptyMap();
        }
        Map<String, List<Event>> eventsByUID = new LinkedHashMap<String, List<Event>>();
        for (Event event : events) {
            String uid = event.getUid();
            if (null == uid && assignIfEmpty) {
                uid = UUID.randomUUID().toString();
                event.setUid(uid);
            }
            List<Event> list = eventsByUID.get(uid);
            if (null == list) {
                list = new ArrayList<Event>();
                eventsByUID.put(uid, list);
            }
            list.add(event);
        }
        return eventsByUID;
    }

    /**
     * Sorts a list of events and change exceptions so that the <i>series master</i> event will be the first element in the list, and any
     * change exceptions are sorted afterwards based on their recurrence identifier.
     *
     * @param events The events to sort
     * @return The sorted events
     */
    private static List<Event> sortSeriesMasterFirst(List<Event> events) {
        if (null != events && 1 < events.size()) {
            Collections.sort(events, new Comparator<Event>() {

                @Override
                public int compare(Event event1, Event event2) {
                    RecurrenceId recurrenceId1 = event1.getRecurrenceId();
                    RecurrenceId recurrenceId2 = event2.getRecurrenceId();
                    if (null == recurrenceId1) {
                        return null == recurrenceId2 ? 0 : -1;
                    }
                    if (null == recurrenceId2) {
                        return 1;
                    }
                    return Long.compare(recurrenceId1.getValue().getTimestamp(), recurrenceId2.getValue().getTimestamp());
                }
            });
        }
        return events;
    }

    /**
     * Tries to handle data truncation and incorrect string errors automatically.
     *
     * @param session The calendar session
     * @param e The exception to handle
     * @param event The event being saved
     * @return <code>true</code> if the excpetion could be handled and the operation should be tried again, <code>false</code>, otherwise
     */
    protected static boolean handle(CalendarSession session, OXException e, Event event) {
        try {
            switch (e.getErrorCode()) {
                case "CAL-4227": // Incorrect string [string %1$s, field %2$s, column %3$s]
                    return session.getUtilities().handleIncorrectString(e, event);
                case "CAL-5070": // Data truncation [field %1$s, limit %2$d, current %3$d]
                    return session.getUtilities().handleDataTruncation(e, event);
            }
        } catch (Exception x) {
            LOG.warn("Error during automatic handling of {}", e.getErrorCode(), x);
        }
        return false;
    }

}

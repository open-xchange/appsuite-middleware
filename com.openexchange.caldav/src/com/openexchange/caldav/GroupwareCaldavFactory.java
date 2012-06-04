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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.caldav;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.caldav.mixins.ScheduleOutboxURL;
import com.openexchange.caldav.reports.Syncstatus;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.mail.contentType.TrashContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.search.Order;
import com.openexchange.log.LogFactory;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.SessionHolder;
import com.openexchange.user.UserService;
import com.openexchange.webdav.loader.BulkLoader;
import com.openexchange.webdav.loader.LoadingHints;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.WebdavStatusImpl;
import com.openexchange.webdav.protocol.helpers.AbstractWebdavFactory;

/**
 * The {@link GroupwareCaldavFactory} holds access to all external groupware services and acts as the factory for CaldavResources and
 * CaldavCollections
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class GroupwareCaldavFactory extends AbstractWebdavFactory implements BulkLoader {

    private static final Log LOG = LogFactory.getLog(GroupwareCaldavFactory.class);

    /**
     * The reserved tree identifier for MS Outlook folder tree: <code>"1"</code>.
     * (copied from com.openexchange.folderstorage.outlook)
     */
    private static final String OUTLOOK_TREE_ID = "1"; 

    private static final CaldavProtocol PROTOCOL = new CaldavProtocol();

    public static final WebdavPath ROOT_URL = new WebdavPath();

    private final SessionHolder sessionHolder;

    private final AppointmentSqlFactoryService appointments;

    private final FolderService folders;

    private final ICalEmitter icalEmitter;

    private final ICalParser icalParser;

    private final ThreadLocal<State> stateHolder = new ThreadLocal<State>();

    private final UserService users;

    private final CalendarCollectionService calendarUtils;

    private final ConfigViewFactory configs;
    
    public GroupwareCaldavFactory(final SessionHolder sessionHolder, final AppointmentSqlFactoryService appointments, final FolderService folders, final ICalEmitter icalEmitter, final ICalParser icalParser, final UserService users, final CalendarCollectionService calendarUtils, final ConfigViewFactory configs) {
        this.sessionHolder = sessionHolder;
        this.appointments = appointments;
        this.folders = folders;
        this.icalEmitter = icalEmitter;
        this.icalParser = icalParser;
        this.users = users;
        this.calendarUtils = calendarUtils;
        this.configs = configs;
    }

    @Override
    public void beginRequest() {
        super.beginRequest();
        stateHolder.set(new State(this));
    }

    @Override
    public void endRequest(final int status) {
        stateHolder.set(null);
        super.endRequest(status);
    }

    @Override
	public CaldavProtocol getProtocol() {
        return PROTOCOL;
    }

    @Override
	public WebdavCollection resolveCollection(WebdavPath url) throws WebdavProtocolException {
        url = sanitize(url);
        if (url.size() > 1) {
            throw WebdavProtocolException.generalError(new WebdavPath(), HttpServletResponse.SC_NOT_FOUND);
        }
        if (isRoot(url)) {
            return mixin(new RootCollection(this));
        } else if (ScheduleOutboxURL.SCHEDULE_OUTBOX.equals(url.name())) {
        	return mixin(new ScheduleOutboxCollection(this));
        }
        return resolveChild(url);
    }

    public WebdavCollection resolveChild(final WebdavPath url) throws WebdavProtocolException {
        try {
            final int folderId = Integer.parseInt(url.name());
            final UserizedFolder folder = getFolderService().getFolder(this.getTreeID(), String.valueOf(folderId), getSession(), null);

            return new CaldavCollection(new RootCollection(this), folder, this);
        } catch (final Exception x) {
            throw WebdavProtocolException.generalError(x, new WebdavPath(), HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // TODO: i18n

    public boolean isRoot(final WebdavPath url) {
        return url.size() == 0;
    }

    @Override
	public WebdavResource resolveResource(WebdavPath url) throws WebdavProtocolException {
        url = sanitize(url);
        if (url.size() == 2) {
            return mixin(((CaldavCollection) resolveCollection(url.parent())).getChild(url.name()));
        }
        return resolveCollection(url);
    }

    private WebdavPath sanitize(final WebdavPath url) {
        if (url.startsWith(new WebdavPath("caldav"))) {
            return url.subpath(1);
        }
        
        return url;
    }

    public AppointmentSQLInterface getAppointmentInterface() {
        return appointments.createAppointmentSql(sessionHolder.getSessionObject());
    }

    public FolderService getFolderService() {
        return folders;
    }

    public Session getSession() {
        return sessionHolder.getSessionObject();
    }

    public ICalEmitter getIcalEmitter() {
        return icalEmitter;
    }

    public ICalParser getIcalParser() {
        return icalParser;
    }

    public Context getContext() {
        return sessionHolder.getContext();
    }

    public User getUser() {
        return sessionHolder.getUser();
    }

    public String getLoginName() {
        return sessionHolder.getSessionObject().getLoginName();
    }

    public SessionHolder getSessionHolder() {
        return sessionHolder;
    }

    public CalendarCollectionService getCalendarUtilities() {
        return calendarUtils;
    }

    public String getConfigValue(final String key, final String defaultValue) throws WebdavProtocolException {
        try {
            final ConfigView view = configs.getView(sessionHolder.getUser().getId(), sessionHolder.getContext().getContextId());
            final ComposedConfigProperty<String> property = view.property(key, String.class);
            if (!property.isDefined()) {
                return defaultValue;
            }
            return property.get();
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(e, new WebdavPath(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    public State getState() {
        return stateHolder.get();
    }

    public boolean isInRange(final Appointment appointment) throws WebdavProtocolException {
        final Date start = start();
        final Date end = end();
        for (final Date d : Arrays.asList(appointment.getStartDate(), appointment.getEndDate())) {
            if (!d.after(start) || !d.before(end)) {
                return false;
            }
        }
        return true;
    }

    public Date end() throws WebdavProtocolException {
        final String value = getConfigValue("com.openexchange.caldav.interval.end", "one_year");
        int addYears = 2;
        if (value.equals("two_years")) {
            addYears = 3;
        }
        final Calendar instance = Calendar.getInstance();
        instance.add(Calendar.YEAR, addYears);
        instance.set(Calendar.DAY_OF_YEAR, 0);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);

        return instance.getTime();
    }

    public Date start() throws WebdavProtocolException {
        final String value = getConfigValue("com.openexchange.caldav.interval.start", "one_month");

        if (value.equals("one_year")) {
            final Calendar instance = Calendar.getInstance();
            instance.add(Calendar.YEAR, -1);
            instance.set(Calendar.DAY_OF_YEAR, 0);
            instance.set(Calendar.HOUR_OF_DAY, 0);
            instance.set(Calendar.MINUTE, 0);
            instance.set(Calendar.SECOND, 0);
            instance.set(Calendar.MILLISECOND, 0);

            return instance.getTime();
        }

        final Calendar instance = Calendar.getInstance();

        if (value.equals("six_months")) {
            instance.add(Calendar.MONTH, -6);
        } else {
            instance.add(Calendar.MONTH, -1);
        }
        instance.set(Calendar.DAY_OF_MONTH, 1);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        return instance.getTime();

    }
    
    /**
     * Gets the used folder tree identifier for folder operations.
     */
    private String getTreeID() {
        try {
			return this.getConfigValue("com.openexchange.caldav.tree", FolderStorage.REAL_TREE_ID);
		} catch (final WebdavProtocolException e) {
			LOG.warn("falling back to tree id '" + FolderStorage.REAL_TREE_ID +"'.", e);
			return FolderStorage.REAL_TREE_ID;
		}
    }

    public User resolveUser(final int uid) throws WebdavProtocolException {
        try {
            return users.getUser(uid, getContext());
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            throw WebdavProtocolException.generalError(e, new WebdavPath(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // Efficient loading based on loading hints

    @Override
	public void load(final LoadingHints hint) {
        if (isRoot(hint.getUrl())) {
            return;
        }
        if (isCollection(hint)) {
            if (hint.getDepth() > 0) {
                if (mustLoadAllProperties(hint)) {
                    cacheFolderWithAllProperties(hint);
                } else {
                    cacheFolderWithSlimProperties(hint);
                }
            }
        } else {
            if (mustLoadAllProperties(hint)) {
                cacheAppointmentWithAllProperties(hint);
            } else {
                cacheAppointmentWithSlimProperties(hint);
            }
        }
    }

    private void cacheAppointmentWithSlimProperties(final LoadingHints hint) {
        final String name = hint.getUrl().name();
        getState().cacheAppointmentSlim(Integer.parseInt(hint.getUrl().parent().name()), Integer.parseInt(name.substring(0, name.length()-4)));

    }

    private void cacheAppointmentWithAllProperties(final LoadingHints hint) {
        final String name = hint.getUrl().name();
        try {
            getState().cacheAppointment(Integer.parseInt(hint.getUrl().parent().name()), Integer.parseInt(name.substring(0, name.length()-4)));
        } catch (final NumberFormatException e) { 
            // IGNORE
            // preloading failed, we don't care, let's load this one normally.
        } catch (final WebdavProtocolException e) {
            // IGNORE
        }
    }

    private void cacheFolderWithSlimProperties(final LoadingHints hint) {
        getState().cacheFolderSlim(Integer.parseInt(hint.getUrl().name()));

    }

    private void cacheFolderWithAllProperties(final LoadingHints hint) {
        getState().cacheFolderSlim(Integer.parseInt(hint.getUrl().name()));
    }

    private boolean mustLoadAllProperties(final LoadingHints hint) {
        if (hint.getProps() == LoadingHints.Property.ALL) {
            return true;
        }
        if (hint.mustLoad(CaldavProtocol.CAL_NAMESPACE, "calendar-data") || hint.mustLoad(Protocol.GETCONTENTLENGTH_LITERAL)) {
            return true;
        }

        if (hint.loadOnly(Protocol.GETETAG_LITERAL, Protocol.GETLASTMODIFIED_LITERAL)) {
            return false;
        }

        return true;
    }

    private boolean isCollection(final LoadingHints hint) {
        final int size = hint.getUrl().size();
        return size < 2;
    }

    @Override
	public void load(final List<LoadingHints> hints) {
        for (final LoadingHints loadingHints : hints) {
            load(loadingHints);
        }
    }

    public static final class State {

        private final static int[] FIELDS_FOR_ALL_REQUEST = { DataObject.OBJECT_ID, FolderChildObject.FOLDER_ID, DataObject.LAST_MODIFIED, Appointment.RECURRENCE_ID, Appointment.UID };

        private final static int[] APPOINTMENT_FIELDS = {
            DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY,
            FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, CalendarObject.TITLE, Appointment.LOCATION,
            CalendarObject.START_DATE, CalendarObject.END_DATE, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE,
            CalendarObject.RECURRENCE_CALCULATOR, CalendarObject.RECURRENCE_ID, CalendarObject.PARTICIPANTS, CalendarObject.USERS,
            Appointment.SHOWN_AS, Appointment.FULL_TIME, Appointment.COLOR_LABEL, Appointment.TIMEZONE, Appointment.UID,
            Appointment.SEQUENCE, Appointment.ORGANIZER, Appointment.CONFIRMATIONS, Appointment.ORGANIZER_ID, Appointment.PRINCIPAL, Appointment.PRINCIPAL_ID };

        private final GroupwareCaldavFactory factory;

        public State(final GroupwareCaldavFactory factory) {
            this.factory = factory;
        }

        private final Map<UIDMatch, Appointment> appointmentCache = new HashMap<UIDMatch, Appointment>();

        private final TIntObjectMap<List<Appointment>> changeExceptionCache = new TIntObjectHashMap<List<Appointment>>();

        private final TIntObjectMap<List<Appointment>> folderCache = new TIntObjectHashMap<List<Appointment>>();

        private final Map<String, Integer> uuidMap = new HashMap<String, Integer>();

//        private final Set<Integer> loadedAllFolderGuardian = new HashSet<Integer>();

        private final Set<UIDMatch> loadedAllAppointmentGuardian = new HashSet<UIDMatch>();

        private final TIntSet loadedAllChangeExceptionsGuardian = new TIntHashSet();

        private final Set<String> patchGuard = new HashSet<String>();

        private String defaultFolderTrash = null; 

        public void cacheFolderSlim(final int folderId) {
            if (folderCache.containsKey(folderId)) {
                return;
            }
            final AppointmentSQLInterface calendar = factory.getAppointmentInterface();
            try {
                final SearchIterator<Appointment> iterator = calendar.getAppointmentsBetweenInFolder(
                    folderId,
                    FIELDS_FOR_ALL_REQUEST,
                    factory.start(),
                    factory.end(),
                    -1,
                    null);
                final List<Appointment> children = new LinkedList<Appointment>();
                while (iterator.hasNext()) {
                    final Appointment appointment = iterator.next();
                    if (appointment.containsRecurrenceID() && appointment.getObjectID() != appointment.getRecurrenceID()) {
                    	final int recurrenceID = appointment.getRecurrenceID();
                    	List<Appointment> changeExceptions = changeExceptionCache.get(recurrenceID);
                    	if (null == changeExceptions) {
                    		changeExceptions = new ArrayList<Appointment>(); 
                    		changeExceptionCache.put(recurrenceID, changeExceptions);
                    	}
                    	changeExceptions.add(appointment);
                    } else {
                        appointmentCache.put(new UIDMatch(appointment.getObjectID(), appointment.getParentFolderID()), appointment);
                        children.add(appointment);
                    }
                }
                folderCache.put(folderId, children);
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }

		/**
		 * Gets a list of all visible and subscribed calendar folders in the configured folder tree.
		 * @return
		 * @throws FolderException
		 */
	    public List<UserizedFolder> getVisibleFolders() throws OXException {
	    	final List<UserizedFolder> folders = new ArrayList<UserizedFolder>();
	    	folders.addAll(this.getVisibleFolders(PrivateType.getInstance()));
	    	folders.addAll(this.getVisibleFolders(PublicType.getInstance()));
	    	folders.addAll(this.getVisibleFolders(SharedType.getInstance()));
	    	return folders;
	    }
        
		/**
		 * Gets a list containing all visible folders of the given {@link Type}.
		 * @param type
		 * @return
		 * @throws FolderException 
		 */
	    private List<UserizedFolder> getVisibleFolders(final Type type) throws OXException {
	    	final List<UserizedFolder> folders = new ArrayList<UserizedFolder>();
			final FolderService folderService = this.factory.getFolderService();
			final FolderResponse<UserizedFolder[]> visibleFoldersResponse = folderService.getVisibleFolders(
					this.factory.getTreeID(), CalendarContentType.getInstance(), type, false, this.factory.getSession(), null);
            final UserizedFolder[] response = visibleFoldersResponse.getResponse();
            for (final UserizedFolder folder : response) {
                if (Permission.READ_OWN_OBJECTS < folder.getOwnPermission().getReadPermission() && false == this.isTrashFolder(folder)) {
                	folders.add(folder);
                }
            }
            return folders;
	    }
	    
	    /**
	     * Gets the id of the default trash folder
	     * @return
	     */
        private String getDefaultFolderTrash() {
        	if (null == this.defaultFolderTrash) {
    			final FolderService folderService = this.factory.getFolderService();
    			try {
					this.defaultFolderTrash = folderService.getDefaultFolder(this.factory.getUser(), OUTLOOK_TREE_ID, 
							TrashContentType.getInstance(), this.factory.getSession(), null).getID();
				} catch (final OXException e) {
					LOG.warn("unable to determine default trash folder", e);
				}
        	}
        	return this.defaultFolderTrash;
        }
        
	    /**
	     * Checks whether the supplied folder is a trash folder, i.e. one of 
	     * it's parent folders is the default trash folder.
	     * 
	     * @param folder
	     * @return
	     * @throws WebdavProtocolException
	     * @throws FolderException 
	     */
	    private boolean isTrashFolder(final UserizedFolder folder) throws OXException {
	    	final String trashFolderId = this.getDefaultFolderTrash();
	    	if (null != trashFolderId) {
				final FolderService folderService = this.factory.getFolderService();
				final FolderResponse<UserizedFolder[]> pathResponse = folderService.getPath(
						OUTLOOK_TREE_ID, folder.getID(), this.factory.getSession(), null);
	            final UserizedFolder[] response = pathResponse.getResponse();
	            for (final UserizedFolder parentFolder : response) {
	            	if (trashFolderId.equals(parentFolder.getID())) {
	            		LOG.debug("Detected folder below trash: " + folder);
	            		return true;
	            	}
	            }
	    	} else {
	    		LOG.warn("No config value for trash folder id found");
	    	}
	    	return false;
	    }

	    public void cacheAppointment(final int folder, final int appointment) throws WebdavProtocolException {
            cacheFolderSlim(folder);
            getComplete(appointment, folder);
        }

        public void cacheAppointmentSlim(final int folder, final int appointment) {
            cacheFolderSlim(folder);
        }

        public Appointment getComplete(final int id, final int folderId) throws WebdavProtocolException {
            cacheFolderSlim(folderId);  // Get rid of this
            upgrade(id, folderId);
            return get(id, folderId);
        }

        /**
         * Gets an appointment instance that represents the supplied 
         * appointment without patches applied.
         * 
         * @param appointment
         * @return
         * @throws WebdavProtocolException
         */
        public Appointment getUnpatched(final Appointment appointment) throws WebdavProtocolException {
        	if (false == this.hasBeenPatched(appointment)) {
        		return appointment;
        	}        	
            final AppointmentSQLInterface appointmentInterface = factory.getAppointmentInterface();
            try {
                return appointmentInterface.getObjectById(appointment.getObjectID(), appointment.getParentFolderID());
            } catch (final Exception e) {
            	throw WebdavProtocolException.generalError(e, new WebdavPath(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }

        private void upgrade(final int id, final int folderId) throws WebdavProtocolException {
            final UIDMatch key = new UIDMatch(id, folderId);
            if (loadedAllAppointmentGuardian.contains(key)) {
                return;
            }
            final AppointmentSQLInterface appointmentInterface = factory.getAppointmentInterface();
            try {
                final CalendarDataObject appointment = appointmentInterface.getObjectById(id, folderId);
                appointmentCache.put(key, appointment);
                loadedAllAppointmentGuardian.add(key);
                uuidMap.put(appointment.getUid(), I(appointment.getObjectID()));
            } catch (final Exception e) {
                throw WebdavProtocolException.generalError(e, new WebdavPath(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }

        public Appointment get(final int id, final int folderId) {
            cacheFolderSlim(folderId);
            final Appointment appointment = appointmentCache.get(new UIDMatch(id, folderId));
            if (appointment != null && appointment.getParentFolderID() != folderId) {
                return null; // anyway, because this uid is in another folder.
            }
            return appointment;
        }

        public List<Appointment> getChangeExceptionsComplete(final Integer id, final int folderId) throws WebdavProtocolException {
            cacheFolderSlim(folderId);
            final List<Appointment> exceptions = changeExceptionCache.get(i(id));
            if (exceptions == null) {
                return Collections.emptyList();
            }
            return upgradeExceptions(id, exceptions);
        }

        private List<Appointment> upgradeExceptions(final Integer masterId, final List<Appointment> exceptions) throws WebdavProtocolException {
            if (loadedAllChangeExceptionsGuardian.contains(i(masterId))) {
                return exceptions;
            }
            loadedAllChangeExceptionsGuardian.add(i(masterId));
            final AppointmentSQLInterface appointmentInterface = factory.getAppointmentInterface();
            final int[][] objectIdAndInFolder = new int[exceptions.size()][2];
            int i = 0;
            for (final Appointment appointment : exceptions) {
                objectIdAndInFolder[i][0] = appointment.getObjectID();
                objectIdAndInFolder[i][1] = appointment.getParentFolderID();
                i++;
            }

            try {
                final SearchIterator<Appointment> objectsById = appointmentInterface.getObjectsById(objectIdAndInFolder, APPOINTMENT_FIELDS);
                final List<Appointment> fullyLoadedExceptions = new ArrayList<Appointment>(exceptions.size());
                while(objectsById.hasNext()) {
                    fullyLoadedExceptions.add(objectsById.next());
                }
                changeExceptionCache.put(i(masterId), fullyLoadedExceptions);
                return fullyLoadedExceptions;
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
                throw WebdavProtocolException.generalError(e, new WebdavPath(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }

        public List<Appointment> getChangeExceptions(final Integer id, final int folderId) {
            cacheFolderSlim(folderId);
            final List<Appointment> exceptions = changeExceptionCache.get(i(id));
            if (exceptions == null) {
                return Collections.emptyList();
            }
            return exceptions;
        }

        public List<Appointment> getFolder(final int id) {
            cacheFolderSlim(id);
            final List<Appointment> appointments = folderCache.get(id);
            if (appointments == null) {
                return Collections.emptyList();
            }
            return appointments;
        }

        public boolean hasBeenPatched(final Appointment appointment) {
            return patchGuard.contains(appointment.getObjectID() + ":" + appointment.getParentFolderID());
        }

        public void markAsPatched(final Appointment appointment) {
            patchGuard.add(appointment.getObjectID() + ":" + appointment.getParentFolderID());
        }

        public int resolveUUID(final String uuid, final int folderId) {

            final Integer id = uuidMap.get(uuid);
            if (id != null) {
                return i(id);
            }

            final AppointmentSQLInterface calendar = factory.getAppointmentInterface();
            try {
                final SearchIterator<Appointment> iterator = calendar.getAppointmentsBetweenInFolder(
                    folderId,
                    new int[]{Appointment.OBJECT_ID, Appointment.UID, Appointment.RECURRENCE_ID},
                    factory.start(),
                    factory.end(),
                    -1,
                    null);
                while (iterator.hasNext()) {
                    final Appointment appointment = iterator.next();
                    if (appointment.containsRecurrenceID() && appointment.getObjectID() != appointment.getRecurrenceID()) {
                        // Exception, so ignore
                    } else {
                        if (appointment.getUid() != null) {
                            uuidMap.put(appointment.getUid(), I(appointment.getObjectID()));
                        }
                    }
                }
                if (uuidMap.containsKey(uuid)) {
                    return i(uuidMap.get(uuid));
                }
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
            }
            return -1;
        }

        public List<Appointment> getAppointmentsInFolderAndRange(final int folderId, Date start, Date end) throws WebdavProtocolException {
            start = (start == null) ? factory.start() : start;
            end = (end == null) ? factory.end() : end;

            final AppointmentSQLInterface calendar = factory.getAppointmentInterface();
            try {
                final SearchIterator<Appointment> iterator = calendar.getAppointmentsBetweenInFolder(
                    folderId,
                    FIELDS_FOR_ALL_REQUEST,
                    start,
                    end,
                    -1,
                    null);
                final List<Appointment> children = new LinkedList<Appointment>();
                while (iterator.hasNext()) {
                    final Appointment appointment = iterator.next();
                    if (appointment.containsRecurrenceID() && appointment.getObjectID() != appointment.getRecurrenceID()) {
                    	final int recurrenceID = appointment.getRecurrenceID();
                    	List<Appointment> changeExceptions = changeExceptionCache.get(recurrenceID);
                    	if (null == changeExceptions) {
                    		changeExceptions = new ArrayList<Appointment>(); 
                    		changeExceptionCache.put(recurrenceID, changeExceptions);
                    	}
                    	changeExceptions.add(appointment);
                    } else {
                        appointmentCache.put(new UIDMatch(appointment.getObjectID(), appointment.getParentFolderID()), appointment);
                        children.add(appointment);
                    }
                }
                folderCache.put(folderId, children);
                return children;
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
                throw WebdavProtocolException.generalError(e, new WebdavPath(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }

        /**
         * Gets the date of the last change made to the supplied folder, i.e. the time a change was made to an appointment in the default 
         * timerange or the time of the last appointment deletion. 
         * 
         * @param folderID The id of the calendar folder
         * @return A {@link Date} indicating the last modification time, or {@code new Date(0)} if no change was detected 
         */
        public Date getLastModification(final int folderID) {
        	return this.getLastModification(folderID, null, null);
        }        
        
        /**
         * Gets the date of the last change made to the supplied folder, i.e. the time a change was made to an appointment in the given 
         * timerange or the time of the last appointment deletion. 
         * 
         * @param folderID The id of the calendar folder
         * @param start The start of the timerange for the appointments to check
         * @param end The end of the timerange for the appointments to check
         * @return A {@link Date} indicating the last modification time, or {@code new Date(0)} if no change was detected 
         */
        public Date getLastModification(final int folderID, final Date start, final Date end) {
        	Date lastModification = new Date(0);
            final AppointmentSQLInterface appointmentInterface = this.factory.getAppointmentInterface();
            try {           	
            	// get latest last modified of appointments in timerange
            	final Date timerangeStart = null != start ? start : this.factory.start(); 
            	final Date timerangeEnd = null != end ? end : this.factory.end(); 
                final SearchIterator<Appointment> appointmentsBetweenInFolder = appointmentInterface.getAppointmentsBetweenInFolder(
                		folderID, new int[] { Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.LAST_MODIFIED }, 
                		timerangeStart, timerangeEnd, Appointment.LAST_MODIFIED, Order.DESCENDING);
                if (appointmentsBetweenInFolder.hasNext()) {
                    lastModification = appointmentsBetweenInFolder.next().getLastModified();
                }
                appointmentsBetweenInFolder.close();
                
                // get last modified of appointments deleted after current latest last modified
                final Date deletedSince = lastModification.after(timerangeStart) ? lastModification : timerangeStart;
                final SearchIterator<Appointment> deletedAppointmentsInFolder = appointmentInterface.getDeletedAppointmentsInFolder(
                		folderID, new int[] { Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.LAST_MODIFIED, 
                				Appointment.START_DATE, Appointment.END_DATE }, deletedSince);
                while (deletedAppointmentsInFolder.hasNext()) {
                	final Appointment appointment = deletedAppointmentsInFolder.next();
                	final Date currentStart = appointment.getStartDate();
                	final Date currentEnd = appointment.getEndDate();
                	if (null != currentEnd && currentEnd.after(timerangeStart) || null != currentStart && currentStart.before(timerangeEnd)) {
                		// appointment was in requested timerange, check last modified (which is the deletion time)
                    	final Date currentLastModified = appointment.getLastModified();
                    	if (null != currentLastModified && currentLastModified.after(lastModification)) {
                        	lastModification = currentLastModified;
                    	}
                	}
                }
                deletedAppointmentsInFolder.close();
            } catch (final WebdavProtocolException e) {
                LOG.error(e.getMessage(),e);
            } catch (final OXException e) {
                LOG.error(e.getMessage(),e);
            } catch (final SQLException e) {
                LOG.error(e.getMessage(),e);
            }
            return lastModification;
        }
    }

    public static final class UIDMatch {

        private final int id;

        private final int folder;

        public UIDMatch(final int id, final int folder) {
            this.id = id;
            this.folder = folder;

        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + folder;
            result = prime * result + id;
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final UIDMatch other = (UIDMatch) obj;
            if (folder != other.folder) {
                return false;
            }
            if (id != other.id) {
                return false;
            }
            return true;
        }



    }
    
    private final int[] SYNC_STATUS_FIELDS = { Appointment.OBJECT_ID, Appointment.FOLDER_ID, Appointment.LAST_MODIFIED, Appointment.CREATION_DATE };

    public Syncstatus<WebdavResource> getSyncStatusSince(final WebdavCollection webdavCollection, String token) throws WebdavProtocolException {
        if (token.length() == 0) {
            token = null;
        }
        if (!(webdavCollection instanceof CaldavCollection)) {
            throw WebdavProtocolException.generalError(new WebdavPath(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        final Date lastModified = token != null ? new Date(Long.parseLong(token)) : new Date(0);

        try {
            final CaldavCollection parent = (CaldavCollection)webdavCollection;
            final int folderId = parent.getId();
            
            final Syncstatus<WebdavResource> multistatus = new Syncstatus<WebdavResource>();
            
            final SearchIterator<Appointment> modifiedAppointmentsInFolder = getAppointmentInterface().getModifiedAppointmentsInFolder(folderId, start(), end(), SYNC_STATUS_FIELDS, lastModified);
            
            Date youngest = lastModified;

            
            while(modifiedAppointmentsInFolder.hasNext()) {
                final Appointment appointment = modifiedAppointmentsInFolder.next();
                
                final long time1 = appointment.getLastModified().getTime();
                final long time2 = lastModified.getTime();
                final long diff = time1 - time2;
                if (diff <= 0) {
                    continue;
                }

                if (time1 > youngest.getTime()) {
                    youngest = appointment.getLastModified();
                }

                final CaldavResource resource = new CaldavResource(parent, appointment, this);
                int status = 200;
                if (appointment.getCreationDate().after(lastModified)) {
                    status = 201;
                }
                multistatus.addStatus(new WebdavStatusImpl<WebdavResource>(status, resource.getUrl(), resource));
            }
            
            final SearchIterator<Appointment> deletedAppointmentsInFolder = getAppointmentInterface().getDeletedAppointmentsInFolder(folderId, SYNC_STATUS_FIELDS, lastModified);
            while(deletedAppointmentsInFolder.hasNext()) {
                final Appointment appointment = deletedAppointmentsInFolder.next();
                final CaldavResource resource = new CaldavResource(parent, appointment, this);
                multistatus.addStatus(new WebdavStatusImpl<WebdavResource>(404, resource.getUrl(), resource));
                youngest = new Date();
            }

            multistatus.setToken(Long.toString(youngest.getTime()));

            return multistatus;
        } catch (final Exception x) {
            LOG.error(x.getMessage(), x);
            throw WebdavProtocolException.generalError(x, new WebdavPath(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}

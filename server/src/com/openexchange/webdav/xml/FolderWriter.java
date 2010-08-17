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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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



package com.openexchange.webdav.xml;

import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import com.openexchange.api.OXConflictException;
import com.openexchange.api.OXObjectNotFoundException;
import com.openexchange.api2.FolderSQLInterface;
import com.openexchange.api2.RdbFolderSQLInterface;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.FolderObjectIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderNotFoundException;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * FolderWriter
 *
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class FolderWriter extends FolderChildWriter {
	
	protected static final String PRIVATE_STRING = "private";
	
	protected static final String PUBLIC_STRING = "public";
	
	protected static final String SHARED_STRING = "shared";
	
	protected int counter;
	
	protected int userId = -1;
	
	private static final Log LOG = LogFactory.getLog(FolderWriter.class);
	
	public FolderWriter(final int userId) {
		this.userId = userId;
	}
	
	public FolderWriter(final Session sessionObj, final Context ctx) {
		this.sessionObj = sessionObj;
		this.ctx = ctx;
		userId = sessionObj.getUserId();
	}
	
	public void startWriter(final int objectId,  final OutputStream os) throws Exception {
		final FolderSQLInterface sqlinterface = new RdbFolderSQLInterface(new ServerSessionAdapter(sessionObj));
		
		final Element eProp = new Element("prop", "D", "DAV:");
		final XMLOutputter xo = new XMLOutputter();
		try {
			final FolderObject folderObj = sqlinterface.getFolderById(objectId);
			writeObject(folderObj, eProp, false, xo, os);
		} catch (final OXFolderNotFoundException exc) {
			writeResponseElement(eProp, 0, HttpServletResponse.SC_NOT_FOUND, XmlServlet.OBJECT_NOT_FOUND_EXCEPTION, xo, os);
		} catch (final OXObjectNotFoundException exc) {
			writeResponseElement(eProp, 0, HttpServletResponse.SC_NOT_FOUND, XmlServlet.OBJECT_NOT_FOUND_EXCEPTION, xo, os);
		} catch (final Exception ex) {
			writeResponseElement(eProp, 0, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, XmlServlet.SERVER_ERROR_EXCEPTION, xo, os);
		}
	}
	
	public void startWriter(final boolean modified, final boolean deleted, final boolean bList, Date lastsync, final OutputStream os) throws Exception {
		final ServerSessionAdapter serverSession = new ServerSessionAdapter(sessionObj);
        final FolderSQLInterface sqlinterface = new RdbFolderSQLInterface(serverSession);
		
		final XMLOutputter xo = new XMLOutputter();
		
		if (lastsync == null) {
			lastsync = new Date(0);
		}
		final boolean oldBehavior = false;
		if (oldBehavior) {
		    /*
	         * Fist send all 'deletes', than all 'modified'
	         */
	        if (deleted) {
	            SearchIterator<FolderObject>  it = null;
	            try {
	                it = sqlinterface.getDeletedFolders(lastsync);
	                writeIterator(it, true, xo, os);
	            } finally {
	                if (it != null) {
	                    it.close();
	                }
	            }
	        }
	        
	        if (modified) {
	            SearchIterator<FolderObject>  it = null;
	            try {
	                it = sqlinterface.getModifiedUserFolders(lastsync);
	                writeIterator(it, false, xo, os);
	            } finally {
	                if (it != null) {
	                    it.close();
	                }
	            }
	        }
	        
	        if (bList) {
	            SearchIterator<FolderObject>  it = null;
	            try {
	                it = sqlinterface.getModifiedUserFolders(new Date(0));
	                writeList(it, xo, os);
	            } finally {
	                if (it != null) {
	                    it.close();
	                }
	            }
	        }
        } else {
    		/*
    		 * Calculate updated and "deleted" folders
    		 */
    		if (modified || deleted) {
    		    final UpdatesResult updatesResult = calculateUpdates(sqlinterface, lastsync, !deleted, false, false, serverSession, lastsync);
    	        /*
    	         * Fist send all 'deletes', than all 'modified'
    	         */
    	        if (deleted) {
    	            final Queue<FolderObject> deletedQueue = updatesResult.deletedQueue;
    	            SearchIterator<FolderObject> it = null;
    	            try {
    	                it = new SearchIteratorAdapter<FolderObject>(deletedQueue.iterator(), deletedQueue.size());
    	                writeIterator(it, true, xo, os);
    	            } finally {
    	                if (it != null) {
    	                    it.close();
    	                }
    	            }
    	        }
    	        if (modified) {
    	            SearchIterator<FolderObject> it = null;
    	            try {
    	                final Queue<FolderObject> updatedQueue = updatesResult.updatedQueue;
    	                it = new SearchIteratorAdapter<FolderObject>(updatedQueue.iterator(), updatedQueue.size());
    	                writeIterator(it, false, xo, os);
    	            } finally {
    	                if (it != null) {
    	                    it.close();
    	                }
    	            }
    	        }
    		}
    
    		if (bList) {
    		    SearchIterator<FolderObject>  it = null;
                try {
                    it = sqlinterface.getModifiedUserFolders(new Date(0));
                    writeList(it, xo, os);
                } finally {
                    if (it != null) {
                        it.close();
                    }
                }
    		}
        }
	}
	
	public void writeIterator(final SearchIterator it, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
		while (it.hasNext()) {
			writeObject((FolderObject)it.next(), delete, xo, os);
		}
	}
	
	public void writeObject(final FolderObject folderobject, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
		writeObject(folderobject, new Element("prop", "D", "DAV:"), delete, xo, os);
	}
	
	public void writeObject(final FolderObject folderobject, final Element e_prop, final boolean delete, final XMLOutputter xo, final OutputStream os) throws Exception {
		final int module = folderobject.getModule();
		
		if (module == FolderObject.CALENDAR || module == FolderObject.TASK || module == FolderObject.CONTACT) {
			int status = 200;
			String description = "OK";
			int object_id = 0;
			
			try {
				object_id = folderobject.getObjectID();
				addContent2PropElement(e_prop, folderobject, delete);
			} catch (final Exception exc) {
				LOG.error("writeObject", exc);
				status = 500;
				description = "Server Error: " + exc.getMessage();
				object_id = 0;
			}
			
			writeResponseElement(e_prop, object_id, status, description, xo, os);
		}
	}
	
	public void addContent2PropElement(final Element e_prop, final FolderObject folderobject, final boolean delete) throws Exception {
		counter++;
		if (delete) {
			addElement("object_id", folderobject.getObjectID(), e_prop);
			addElement("object_status", "DELETE", e_prop);
		} else {
			final int type = folderobject.getType();
			final int owner = folderobject.getCreator();
			final int module = folderobject.getModule();
			
			addElement("object_status", "CREATE", e_prop);
			
			String folderName = null;
			if ((folderName = folderobject.getFolderName()) != null && folderName.length() > 0) {
				addElement("title", folderobject.getFolderName(), e_prop);
			} else {
				addElement("title", "no folder name " + counter, e_prop);
			}
			
			addElement("owner", folderobject.getCreator(), e_prop);
			
			switch (module) {
				case FolderObject.CALENDAR:
					addElement("module", "calendar", e_prop);
					break;
				case FolderObject.CONTACT:
					addElement("module", "contact", e_prop);
					break;
				case FolderObject.TASK:
					addElement("module", "task", e_prop);
					break;
				default:
					throw new OXConflictException("invalid module");
			}
			
			if (type == FolderObject.PRIVATE) {
				if (owner == userId) {
					addElement("type", PRIVATE_STRING, e_prop);
				} else {
					addElement("type", SHARED_STRING, e_prop);
					folderobject.setParentFolderID(3);
				}
			} else {
				addElement("type", PUBLIC_STRING, e_prop);
			}
			
			addElement("defaultfolder", folderobject.isDefaultFolder(), e_prop);
			
			writeFolderChildElements(folderobject, e_prop);
			
			addElementPermission(folderobject.getPermissions(), e_prop);
		}
	}
	
	public static void addElementPermission(final List permissions, final Element e_prop) throws Exception {
		final Element e_permissions = new Element("permissions", XmlServlet.PREFIX, XmlServlet.NAMESPACE);
		
		if (permissions != null) {
			for (int a = 0; a < permissions.size(); a++) {
				final OCLPermission oclp = (OCLPermission)permissions.get(a);
				final int entity = oclp.getEntity();
				final int fp = oclp.getFolderPermission();
				final int orp = oclp.getReadPermission();
				final int owp = oclp.getWritePermission();
				final int odp = oclp.getDeletePermission();
				
				if (oclp.isGroupPermission()) {
					addElementGroup(e_permissions, entity, fp, orp, owp, odp, oclp.isFolderAdmin());
				} else {
					addElementUser(e_permissions, entity, fp, orp, owp, odp, oclp.isFolderAdmin());
				}
			}
		}
		
		e_prop.addContent(e_permissions);
	}
	
	protected static void addElementUser(final Element e_permissions, final int entity, final int fp, final int orp, final int owp, final int odp, final boolean adminFlag) throws Exception {
		final Element e = new Element("user", namespace);
		addAttributes(e, fp, orp, owp, odp, adminFlag);
		e.addContent(String.valueOf(entity));
		e_permissions.addContent(e);
	}
	
	protected static void addElementGroup(final Element e_permissions, final int entity, final int fp, final int orp, final int owp, final int odp, final boolean adminFlag) throws Exception {
		final Element e = new Element("group", namespace);
		addAttributes(e, fp, orp, owp, odp, adminFlag);
		e.addContent(String.valueOf(entity));
		e_permissions.addContent(e);
	}
	
	protected static void addAttributes(final Element e, final int fp, final int orp, final int owp, final int odp, final boolean adminFlag) throws Exception {
		e.setAttribute("folderpermission", String.valueOf(fp), namespace);
		e.setAttribute("objectreadpermission", String.valueOf(orp), namespace);
		e.setAttribute("objectwritepermission", String.valueOf(owp), namespace);
		e.setAttribute("objectdeletepermission", String.valueOf(odp), namespace);
		e.setAttribute("admin_flag", String.valueOf(adminFlag), namespace);
	}

	/*
	 * Stuff for calculating deleted/updates folders
	 */
	
	public static final class UpdatesResult {
        
        public final Date lastModified;
        
        public final Queue<FolderObject> updatedQueue;
        
        public final Queue<FolderObject> deletedQueue;

        public UpdatesResult(final Date lastModified, final Queue<FolderObject> updatedQueue, final Queue<FolderObject> deletedQueue) {
            super();
            this.lastModified = lastModified;
            this.updatedQueue = updatedQueue;
            this.deletedQueue = deletedQueue;
        }

    }

    public static UpdatesResult calculateUpdates(final FolderSQLInterface foldersqlinterface, final Date timestamp, final boolean ignoreDeleted, final boolean includeVirtualListFolder, final boolean gatherDisplayNamesOfSharedFolders, final ServerSession session, final Date lastModified) throws AbstractOXException {
        final Context ctx = session.getContext();
        /*
         * Get all updated OX folders
         */
        Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getAllModifiedFolders(timestamp)).asQueue();
        final OXFolderAccess access = new OXFolderAccess(ctx);
        final Queue<FolderObject> updatedQueue = new LinkedList<FolderObject>();
        final Queue<FolderObject> deletedQueue = ignoreDeleted ? null : new LinkedList<FolderObject>();
        final Map<String, Integer> displayNames = gatherDisplayNamesOfSharedFolders ? new HashMap<String, Integer>() : Collections.<String, Integer> emptyMap();
        boolean addSystemSharedFolder = false;
        boolean checkVirtualListFolders = false;
        int size = q.size();
        Iterator<FolderObject> iter = q.iterator();
        try {
            final UserConfiguration userConf = session.getUserConfiguration();
            final UserStorage us = UserStorage.getInstance();
            final StringHelper strHelper = new StringHelper(session.getUser().getLocale());
            final boolean sharedFolderAccess = userConf.hasFullSharedFolderAccess();
            for (int i = 0; i < size; i++) {
                final FolderObject fo = iter.next();
                if (fo.isVisible(session.getUserId(), userConf)) {
                    if (fo.isShared(session.getUserId())) {
                        if (sharedFolderAccess) {
                            /*
                             * Add display name of shared folder owner
                             */
                            if (gatherDisplayNamesOfSharedFolders) {
                                String creatorDisplayName;
                                try {
                                    creatorDisplayName = us.getUser(fo.getCreatedBy(), ctx).getDisplayName();
                                } catch (final LdapException e) {
                                    if (fo.getCreatedBy() != OCLPermission.ALL_GROUPS_AND_USERS) {
                                        throw new AbstractOXException(e);
                                    }
                                    creatorDisplayName = strHelper.getString(Groups.ALL_USERS);
                                }
                                if (!displayNames.containsKey(creatorDisplayName)) {
                                    displayNames.put(creatorDisplayName, Integer.valueOf(fo.getCreatedBy()));
                                }
                            }
                            /*
                             * Remember to include system shared folder
                             */
                            addSystemSharedFolder = true;
                        } else {
                            if (!ignoreDeleted) {
                                deletedQueue.add(fo);
                            }
                        }
                    } else if (FolderObject.PUBLIC == fo.getType()) {
                        if (access.getFolderPermission(fo.getParentFolderID(), session.getUserId(), userConf).isFolderVisible()) {
                            /*
                             * Parent is already visible: Add real parent
                             */
                            updatedQueue.add(access.getFolderObject(fo.getParentFolderID()));
                        } else {
                            /*
                             * Parent is not visible: Update superior system folder to let the newly visible folder appear underneath
                             * virtual "Other XYZ folders"
                             */
                            updatedQueue.add(fo.getModule() == FolderObject.INFOSTORE ? access.getFolderObject(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) : access.getFolderObject(FolderObject.SYSTEM_PUBLIC_FOLDER_ID));
                        }
                    }
                    updatedQueue.add(fo);
                } else {
                    checkVirtualListFolders |= (FolderObject.PUBLIC == fo.getType());
                    if (!ignoreDeleted) {
                        deletedQueue.add(fo);
                    }
                }
            }
            /*
             * Check virtual list folders
             */
            if (includeVirtualListFolder && checkVirtualListFolders && !ignoreDeleted) {
                if (userConf.hasTask() && !foldersqlinterface.getNonTreeVisiblePublicTaskFolders().hasNext()) {
                    final FolderObject virtualTasks = new FolderObject(FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID);
                    virtualTasks.setLastModified(new Date(0));
                    deletedQueue.add(virtualTasks);
                }
                if (userConf.hasCalendar() && !foldersqlinterface.getNonTreeVisiblePublicCalendarFolders().hasNext()) {
                    final FolderObject virtualCalendar = new FolderObject(FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID);
                    virtualCalendar.setLastModified(new Date(0));
                    deletedQueue.add(virtualCalendar);
                }
                if (userConf.hasContact() && !foldersqlinterface.getNonTreeVisiblePublicContactFolders().hasNext()) {
                    final FolderObject virtualContact = new FolderObject(FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID);
                    virtualContact.setLastModified(new Date(0));
                    deletedQueue.add(virtualContact);
                }
                if (userConf.hasInfostore() && !foldersqlinterface.getNonTreeVisiblePublicInfostoreFolders().hasNext()) {
                    final FolderObject virtualInfostore = new FolderObject(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID);
                    virtualInfostore.setLastModified(new Date(0));
                    deletedQueue.add(virtualInfostore);
                }
            }
        } catch (final SQLException e) {
            throw new OXFolderException(OXFolderException.FolderCode.SQL_ERROR, e, e.getMessage());
        }
        /*
         * Check if shared folder must be updated, too
         */
        if (addSystemSharedFolder) {
            final FolderObject sharedFolder = access.getFolderObject(FolderObject.SYSTEM_SHARED_FOLDER_ID);
            sharedFolder.setFolderName(FolderObject.getFolderString(FolderObject.SYSTEM_SHARED_FOLDER_ID, session.getUser().getLocale()));
            updatedQueue.add(sharedFolder);
            if (!displayNames.isEmpty()) {
                for (final Entry<String, Integer> entry : displayNames.entrySet()) {
                    updatedQueue.add(FolderObject.createVirtualSharedFolderObject(entry.getValue().intValue(), entry.getKey()));
                }
            }
        }
        /*
         * Output updated folders
         */
        size = updatedQueue.size();
        iter = updatedQueue.iterator();
        for (int i = 0; i < size; i++) {
            final FolderObject fo = iter.next();
            {
                final Date modified = fo.getLastModified();
                if (null != modified && lastModified.before(modified)) {
                    lastModified.setTime(modified.getTime());
                }
            }
        }
        if (!ignoreDeleted) {
            /*
             * Get deleted OX folders
             */
            q = ((FolderObjectIterator) foldersqlinterface.getDeletedFolders(timestamp)).asQueue();
            /*
             * Add deleted OX folders from above
             */
            q.addAll(deletedQueue);
            /*
             * Return with deleted folders
             */
            return new UpdatesResult(lastModified, updatedQueue, q);
        }
        /*
         * Return without deleted folders
         */
        return new UpdatesResult(lastModified, updatedQueue, null);
    }

}

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

package com.openexchange.custom.audit.impl;

import java.io.IOException;
import java.util.Iterator;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.openexchange.api2.FolderSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbFolderSQLInterface;
import com.openexchange.custom.audit.osgi.AuditActivator;
import com.openexchange.event.CommonEvent;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.RdbContextStorage;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.FolderObjectIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

/**
 * @author Benjamin Otterbach
 */
public class AuditEventHandler implements EventHandler {

	//private static transient final Log LOG = LogFactory.getLog(AuditActivator.class);
	
    private static final Logger auditLogging = Logger.getLogger(AuditEventHandler.class.getName());
    
	private static final AuditEventHandler instance = new AuditEventHandler();
	
    public static AuditEventHandler getInstance() {
        return instance;
    }
	
    /**
     * Initializes a new {@link AuditEventHandler}.
     */
	public AuditEventHandler() {
		super();
		
		try {
			FileHandler handler = new FileHandler("/Users/bartl3by/Desktop/log/open-xchange-audit.log", 2097152, 99, true);
        	handler.setFormatter(new SimpleFormatter());
        	auditLogging.setLevel(Level.INFO);
			auditLogging.addHandler(handler);
		} catch (SecurityException e) {
			//LOG.error(e.getMessage(), e);
		} catch (IOException e) {
			//LOG.error(e.getMessage(), e);
		}
		
		auditLogging.log(Level.INFO, "Title222");
		auditLogging.log(Level.INFO, AuditEventHandler.class.getName());
	}
	
	public void handleEvent(final Event event) {
		
		
		try {
			final CommonEvent commonEvent = (CommonEvent) event.getProperty(CommonEvent.EVENT_KEY);
			
	        ModuleSwitch: switch (commonEvent.getModule()) {
	        default: break ModuleSwitch;
	        case Types.APPOINTMENT:	        	
	        	AppointmentObject appointment = (AppointmentObject)commonEvent.getActionObj();
				if (commonEvent.getAction() == CommonEvent.INSERT) {
					auditLogging.info("Title222: " + appointment.getTitle());
					auditLogging.info("Folder id: " + appointment.getParentFolderID());
					auditLogging.info("Foldername: " + getPathToRoot(appointment.getParentFolderID(), commonEvent.getContextId(), commonEvent.getSession()));					
				}
	        	break ModuleSwitch;
	        case Types.CONTACT:
	        	ContactObject contact = (ContactObject)commonEvent.getActionObj();
	        	auditLogging.info(contact.getGivenName() + ", " + contact.getSurName());
	        	break ModuleSwitch;
	        case Types.TASK:
	        	Task task = (Task)commonEvent.getActionObj();
	        	auditLogging.info(task.getTitle());
	        	break ModuleSwitch;
	        case Types.INFOSTORE:
	        	DocumentMetadata document = (DocumentMetadata)commonEvent.getActionObj();
	        	auditLogging.info(document.getTitle());
	        	break ModuleSwitch;
	        }
		} catch (final Exception e) {
			//LOG.error(e.getMessage(), e);
		}	
	}
	
	/**
	 * This method will return the full folder path as String.
	 * @param folderId
	 * @param contextId
	 * @param sessionObj
	 * @return String fullFolderPath
	 */
	private String getPathToRoot(int folderId, int contextId, Session sessionObj) {
		String retval = "";
		
		try {
			final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(sessionObj, new RdbContextStorage().loadContext(contextId));
			final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getPathToRoot(folderId)).asQueue();
			final int size = q.size();
			final Iterator<FolderObject> iter = q.iterator();
			for (int i = 0; i < size; i++) {
			    retval = iter.next().getFolderName() + "/" + retval;
			}
		} catch (ContextException e) {
			e.printStackTrace();
		} catch (SearchIteratorException e) {
			e.printStackTrace();
		} catch (OXException e) {
			e.printStackTrace();
		}
		
		return retval;
	}

}

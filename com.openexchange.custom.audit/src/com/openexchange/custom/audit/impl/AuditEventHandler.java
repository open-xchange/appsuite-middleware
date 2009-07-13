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
import java.util.logging.Formatter;
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
import com.openexchange.custom.audit.configuration.AuditConfiguration;
import com.openexchange.custom.audit.logging.AuditFileHandler;
import com.openexchange.custom.audit.logging.AuditFilter;
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
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.FolderObjectIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

/**
 * @author Benjamin Otterbach
 */
public class AuditEventHandler implements EventHandler {

	private static final Logger LOG = Logger.getLogger(AuditEventHandler.class.getName());

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
			Logger rootLogger = Logger.getLogger("");
			Handler[] handlers = rootLogger.getHandlers();		
			for (int position = 0; position < handlers.length; position ++) {
				handlers[position].setFilter(new AuditFilter());
			}
			LOG.addHandler(new AuditFileHandler());
		} catch (SecurityException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
		
		LOG.log(Level.INFO, "INFO");
		LOG.log(Level.WARNING, "WARNING");
		LOG.log(Level.SEVERE, "SEVERE");
		
		/*
		try {
			Logger rootLogger = Logger.getLogger("");
			Handler[] handlers = rootLogger.getHandlers();
			for (int position = 0; position < handlers.length; position ++) {
				handlers[position].setFilter(new AuditFilter());
			}
		} catch (SecurityException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
		
		try {
			FileHandler handler = new FileHandler(
					AuditConfiguration.getLogfileLocation(),
					AuditConfiguration.getLogfileLimit(),
					AuditConfiguration.getLogfileCount(),
					AuditConfiguration.getLogfileAppend());
        	try {
				handler.setFormatter(AuditConfiguration.getLogfileFormatter());
			} catch (InstantiationException e) {
				LOG.log(Level.SEVERE, e.getMessage(), e);
			} catch (IllegalAccessException e) {
				LOG.log(Level.SEVERE, e.getMessage(), e);
			} catch (ClassNotFoundException e) {
				LOG.log(Level.SEVERE, e.getMessage(), e);
			} finally {
				LOG.severe(handler.getFormatter().toString());
				if (handler.getFormatter() == null) {
					LOG.log(Level.WARNING, "Setting java.util.logging.SimpleFormatter as default formatter for audit logging.");
					handler.setFormatter(new SimpleFormatter());	
				}
			}
			
        	LOG.setLevel(AuditConfiguration.getLoglevel());
			LOG.addHandler(handler);
		} catch (ServiceException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		} catch (SecurityException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		} catch (IOException e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
		*/
	}
	
	public void handleEvent(final Event event) {
		try {
			final CommonEvent commonEvent = (CommonEvent) event.getProperty(CommonEvent.EVENT_KEY);
			
	        ModuleSwitch: switch (commonEvent.getModule()) {
	        default: break ModuleSwitch;
	        case Types.APPOINTMENT:	        	
	        	AppointmentObject appointment = (AppointmentObject)commonEvent.getActionObj();
				if (commonEvent.getAction() == CommonEvent.INSERT) {
					LOG.info("Title222: " + appointment.getTitle());
					LOG.info("Folder id: " + appointment.getParentFolderID());
					LOG.info("Foldername: " + getPathToRoot(appointment.getParentFolderID(), commonEvent.getContextId(), commonEvent.getSession()));					
				}
	        	break ModuleSwitch;
	        case Types.CONTACT:
	        	ContactObject contact = (ContactObject)commonEvent.getActionObj();
	        	LOG.info(contact.getGivenName() + ", " + contact.getSurName());
	        	break ModuleSwitch;
	        case Types.TASK:
	        	Task task = (Task)commonEvent.getActionObj();
	        	LOG.info(task.getTitle());
	        	break ModuleSwitch;
	        case Types.INFOSTORE:
	        	DocumentMetadata document = (DocumentMetadata)commonEvent.getActionObj();
	        	LOG.info(document.getTitle());
	        	break ModuleSwitch;
	        }
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
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

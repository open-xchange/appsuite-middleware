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

package com.openexchange.groupware.attach;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.api2.OXException;
import com.openexchange.event.AppointmentEvent;
import com.openexchange.event.ContactEvent;
import com.openexchange.event.TaskEvent;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.impl.AttachmentBaseImpl;
import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.exceptions.LoggingLogic;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

public class AttachmentCleaner implements AppointmentEvent, TaskEvent,
		ContactEvent {
	
	private static final AttachmentBase ATTACHMENT_BASE = new AttachmentBaseImpl(new DBPoolProvider()); // No notifications, no permission check.

	private static final LoggingLogic LL = LoggingLogic.getLoggingLogic(AttachmentCleaner.class);
	
	public final void appointmentDeleted(final AppointmentObject appointmentObj,
			final SessionObject sessionObj) {
		deleteAttachments(appointmentObj.getParentFolderID(), appointmentObj.getObjectID(), Types.APPOINTMENT, sessionObj );
	}
	
	public final void taskDeleted(final Task taskObj, final SessionObject sessionObj) {
	
		deleteAttachments(taskObj.getParentFolderID(), taskObj.getObjectID(), Types.TASK, sessionObj);
	}
	
	public final void contactDeleted(final ContactObject contactObj,
			final SessionObject sessionObj) {
		deleteAttachments(contactObj.getParentFolderID(), contactObj.getObjectID(), Types.CONTACT, sessionObj);
		
	}

	public final void appointmentCreated(final AppointmentObject appointmentObj,
			final SessionObject sessionObj) {
		// TODO Auto-generated method stub

	}

	public final void appointmentModified(final AppointmentObject appointmentObj,
			final SessionObject sessionObj) {
		// TODO Auto-generated method stub

	}	

	public final void taskCreated(final Task taskObj, final SessionObject sessionObj) {
		// TODO Auto-generated method stub

	}

	public final void taskModified(final Task taskObj, final SessionObject sessionObj) {
		// TODO Auto-generated method stub

	}

	public final void contactCreated(final ContactObject contactObj,
			final SessionObject sessionObj) {
		// TODO Auto-generated method stub

	}

	public final void contactModified(final ContactObject contactObj,
			final SessionObject sessionObj) {
		// TODO Auto-generated method stub

	}
	
	private final void deleteAttachments(final int parentFolderID, final int objectID, final int type, final SessionObject sessionObj) {
		SearchIterator iter = null;
		try {
			ATTACHMENT_BASE.startTransaction();
			final TimedResult rs = ATTACHMENT_BASE.getAttachments(parentFolderID,objectID,type,new AttachmentField[]{AttachmentField.ID_LITERAL},AttachmentField.ID_LITERAL,AttachmentBase.ASC,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			final List<Integer> ids = new ArrayList<Integer>();
			iter = rs.results();
			if(!iter.hasNext()) {
				return; // Shortcut
			}
			while(iter.hasNext()){
				ids.add(Integer.valueOf(((AttachmentMetadata)iter.next()).getId()));
			}
			int[] idA = new int[ids.size()];
			
			int i = 0;
			for(int id : ids) {
				idA[i++] = id;
			}
			
			ATTACHMENT_BASE.detachFromObject(parentFolderID, objectID, type, idA,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			ATTACHMENT_BASE.commit();
		
		} catch (TransactionException e) {
			rollback(e);
		} catch (OXException e) {
			rollback(e);
		} catch (SearchIteratorException e) {
			rollback(e);
		} finally {
			if(iter != null) {
				try {
					iter.close();
				} catch (SearchIteratorException e) {
					LL.log(e);
				}
			}
			try {
				ATTACHMENT_BASE.finish();
			} catch (TransactionException e) {
				LL.log(e);
			}
		}
	}

	private void rollback(AbstractOXException x) {
		try {
			ATTACHMENT_BASE.rollback();
		} catch (TransactionException e) {
			LL.log(e);
		}
		LL.log(x);
	}

}

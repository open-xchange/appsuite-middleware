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

package com.openexchange.event;

import com.openexchange.groupware.container.AppointmentObject;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.sessiond.SessionObject;

/**
 * EventClient
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */

public class EventClient {
	
	public static final int CREATED = 5;
	public static final int CHANGED = 6;
	public static final int DELETED = 7;
	
	private SessionObject sessionObj;
	
	public EventClient(SessionObject sessionObj) {
		this.sessionObj = sessionObj;
	}

	public void create(final AppointmentObject appointmentObj) throws InvalidStateException {
		final EventObject eventObject = new EventObject(appointmentObj, CREATED, sessionObj);
		EventQueue.add(eventObject);
	}

	public void modify(final AppointmentObject appointmentObj) throws InvalidStateException {
		final EventObject eventObject = new EventObject(appointmentObj, CHANGED, sessionObj);
		EventQueue.add(eventObject);
	}

	public void delete(final AppointmentObject appointmentObj) throws InvalidStateException {
		final EventObject eventObject = new EventObject(appointmentObj, DELETED, sessionObj);
		EventQueue.add(eventObject);
	}

    public void create(final Task taskObj) throws InvalidStateException {
		final EventObject eventObject = new EventObject(taskObj, CREATED, sessionObj);
		EventQueue.add(eventObject);
	}

    public void modify(final Task taskObj) throws InvalidStateException {
		final EventObject eventObject = new EventObject(taskObj, CHANGED, sessionObj);
		EventQueue.add(eventObject);
	}

    public void delete(final Task taskObj) throws InvalidStateException {
		final EventObject eventObject = new EventObject(taskObj, DELETED, sessionObj);
		EventQueue.add(eventObject);
	}

	public void create(final ContactObject contactObj) throws InvalidStateException {
		final EventObject eventObject = new EventObject(contactObj, CREATED, sessionObj);
		EventQueue.add(eventObject);
	}

	public void modify(final ContactObject contactObj) throws InvalidStateException {
		final EventObject eventObject = new EventObject(contactObj, CHANGED, sessionObj);
		EventQueue.add(eventObject);
	}

	public void delete(final ContactObject contactObj) throws InvalidStateException {
		final EventObject eventObject = new EventObject(contactObj, DELETED, sessionObj);
		EventQueue.add(eventObject);
	}

	public void create(final FolderObject folderObj) throws InvalidStateException {
		final EventObject eventObject = new EventObject(folderObj, CREATED, sessionObj);
		EventQueue.add(eventObject);
	}

	public void modify(final FolderObject folderObj) throws InvalidStateException {
		final EventObject eventObject = new EventObject(folderObj, CHANGED, sessionObj);
		EventQueue.add(eventObject);
	}

	public void delete(final FolderObject folderObj) throws InvalidStateException {
		final EventObject eventObject = new EventObject(folderObj, DELETED, sessionObj);
		EventQueue.add(eventObject);
	}

	public void create(final DocumentMetadata document) throws InvalidStateException {
		final EventObject eventObject = new EventObject(document, CREATED, sessionObj);
		EventQueue.add(eventObject);	
	}

	public void modify(final DocumentMetadata document) throws InvalidStateException {
		final EventObject eventObject = new EventObject(document, CHANGED, sessionObj);
		EventQueue.add(eventObject);
	}

	public void delete(final DocumentMetadata document) throws InvalidStateException {
		final EventObject eventObject = new EventObject(document, DELETED, sessionObj);
		EventQueue.add(eventObject);
	}
}

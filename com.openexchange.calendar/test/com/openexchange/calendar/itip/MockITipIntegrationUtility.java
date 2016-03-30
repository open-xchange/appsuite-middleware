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

package com.openexchange.calendar.itip;

import java.util.Date;
import java.util.List;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.ConfirmationChange;
import com.openexchange.session.Session;

/**
 * {@link MockITipIntegrationUtility}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MockITipIntegrationUtility implements ITipIntegrationUtility {

    private List<Appointment> exceptions;

    private Appointment appointment;

    public List<Appointment> getExceptions() {
        return exceptions;
    }

    public void setExceptions(List<Appointment> exceptions) {
        this.exceptions = exceptions;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }

    @Override
    public void changeConfirmationForExternalParticipant(Appointment update, ConfirmationChange change, Session session) throws OXException {

    }

    @Override
    public void createAppointment(CalendarDataObject appointment, Session session) throws OXException {

    }

    @Override
    public void deleteAppointment(Appointment appointment, Session session, Date clientLastModified) throws OXException {

    }

    @Override
    public List<Appointment> getConflicts(CalendarDataObject appointment, Session session) throws OXException {
        return null;
    }

    @Override
    public List<Appointment> getExceptions(Appointment original, Session session) throws OXException {
        return exceptions;
    }

    @Override
    public int getPrivateCalendarFolderId(Session session) throws OXException {
        return 0;
    }

    @Override
    public Appointment reloadAppointment(Appointment address, Session session) throws OXException {
        return this.appointment;
    }

    @Override
    public CalendarDataObject resolveUid(String uid, Session session) throws OXException {
        return null;
    }

    @Override
    public void updateAppointment(CalendarDataObject update, Session session, Date clientLastModified) throws OXException {

    }

	@Override
    public int getFolderIdForUser(int appId, int userId, int contextId)
			throws OXException {
		// Nothing to do
		return 0;
	}


	@Override
	public Appointment loadAppointment(Appointment appointment, Session session)
			throws OXException {
		// Nothing to do
		return null;
	}

}

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

package com.openexchange.groupware.calendar;

import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.groupware.attach.AttachmentAuthorization;
import com.openexchange.groupware.attach.AttachmentEvent;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.session.ServerSession;

/**
 * CalendarAttachments
 * @author <a href="mailto:martin.kauss@open-xchange.org">Martin Kauss</a>
 */

public class CalendarAttachments implements  AttachmentListener, AttachmentAuthorization {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarAttachments.class);

    //private static AppointmentSqlFactoryService appointmentSqlFactory = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class);

    @Override
    public long attached(final AttachmentEvent e) throws Exception {
        final AppointmentSQLInterface csql = getInterface(e.getSession());
        return csql.attachmentAction(e.getFolderId(), e.getAttachedId(), e.getUser().getId(), e.getSession(), e.getContext(), 1);
    }

    @Override
    public long detached(final AttachmentEvent e) throws Exception {
        final AppointmentSQLInterface csql = getInterface(e.getSession());
        return csql.attachmentAction(e.getFolderId(), e.getAttachedId(), e.getUser().getId(), e.getSession(), e.getContext(), -(e.getDetached().length));
    }

    private static AppointmentSQLInterface getInterface(final Session session) {
		return ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class).createAppointmentSql(session);
	}

	@Override
    public void checkMayAttach(ServerSession session, int folderId, int objectId) throws OXException {
        try {
            final CalendarCollectionService collection = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
            if (!collection.getWritePermission(objectId, folderId, session, session.getContext())) {
                throw OXCalendarExceptionCodes.NO_PERMISSIONS_TO_ATTACH_DETACH.create();
            }
        } catch (final OXException e) {
            if (e.isGeneric(Generic.NOT_FOUND)) {
                LOG.error(StringCollection.convertArraytoString(new Object[] {
                    "checkMayAttach failed. The object does not exists (cid:oid) : ", session.getContextId(), ":", objectId } ));
            }
            throw e;
        } catch (final Exception e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, 14);
        }
    }

    @Override
    public void checkMayDetach(ServerSession session, int folderId, int objectId) throws OXException {
        checkMayAttach(session, folderId, objectId);
    }

    @Override
    public void checkMayReadAttachments(ServerSession session, int folderId, int objectId) throws OXException {
        try {
            final CalendarCollectionService collection = ServerServiceRegistry.getInstance().getService(CalendarCollectionService.class);
            if (!collection.getReadPermission(objectId, folderId, session, session.getContext())) {
                throw OXCalendarExceptionCodes.NO_PERMISSIONS_TO_READ.create();
            }
        } catch (final OXException e) {
            if (e.isGeneric(Generic.NOT_FOUND)) {
                LOG.error(StringCollection.convertArraytoString(new Object[] {
                    "checkMayReadAttachments failed. The object does not exists (cid:oid) : ", session.getContextId(), ":", objectId } ));
            }
            throw e;
        } catch(final Exception e) {
            throw OXCalendarExceptionCodes.UNEXPECTED_EXCEPTION.create(e, 15);
        }
    }

}

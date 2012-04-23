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

package com.openexchange.groupware.attach;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.event.impl.AppointmentEventInterface;
import com.openexchange.event.impl.ContactEventInterface;
import com.openexchange.event.impl.TaskEventInterface;
import com.openexchange.exception.Log;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.impl.AttachmentBaseImpl;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

public class AttachmentCleaner implements AppointmentEventInterface, TaskEventInterface, ContactEventInterface {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(com.openexchange.log.LogFactory.getLog(AttachmentCleaner.class));

    private static final AttachmentBase ATTACHMENT_BASE = new AttachmentBaseImpl(new DBPoolProvider()); // No notifications, no permission

    @Override
    public final void appointmentDeleted(final Appointment appointmentObj, final Session sessionObj) {
        deleteAttachments(appointmentObj.getParentFolderID(), appointmentObj.getObjectID(), Types.APPOINTMENT, sessionObj);
    }

    @Override
    public final void taskDeleted(final Task taskObj, final Session sessionObj) {

        deleteAttachments(taskObj.getParentFolderID(), taskObj.getObjectID(), Types.TASK, sessionObj);
    }

    @Override
    public final void contactDeleted(final Contact contactObj, final Session sessionObj) {
        deleteAttachments(contactObj.getParentFolderID(), contactObj.getObjectID(), Types.CONTACT, sessionObj);

    }

    @Override
    public final void appointmentCreated(final Appointment appointmentObj, final Session sessionObj) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void appointmentModified(final Appointment appointment, final Session session) {
        // Nothing to do.

    }

    @Override
    public final void taskCreated(final Task taskObj, final Session sessionObj) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void taskModified(final Task taskObj, final Session sessionObj) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void contactCreated(final Contact contactObj, final Session sessionObj) {
        // TODO Auto-generated method stub

    }

    @Override
    public final void contactModified(final Contact contact, final Session session) {
        // Nothing to do.
    }

    private final void deleteAttachments(final int parentFolderID, final int objectID, final int type, final Session session) {
        SearchIterator iter = null;
        try {
            final ServerSession sessionObj = ServerSessionAdapter.valueOf(session);
            ATTACHMENT_BASE.startTransaction();
            final TimedResult rs =
                ATTACHMENT_BASE.getAttachments(
                    parentFolderID,
                    objectID,
                    type,
                    new AttachmentField[] { AttachmentField.ID_LITERAL },
                    AttachmentField.ID_LITERAL,
                    AttachmentBase.ASC,
                    sessionObj.getContext(),
                    null,
                    null);
            final TIntList ids = new TIntArrayList();
            iter = rs.results();
            if (!iter.hasNext()) {
                return; // Shortcut
            }
            while (iter.hasNext()) {
                ids.add(((AttachmentMetadata) iter.next()).getId());
            }

            ATTACHMENT_BASE.detachFromObject(
                parentFolderID,
                objectID,
                type,
                ids.toArray(),
                sessionObj,
                sessionObj.getContext(),
                null,
                null);
            ATTACHMENT_BASE.commit();

        } catch (final OXException e) {
            rollback(e);
        } finally {
            if (iter != null) {
                try {
                    iter.close();
                } catch (final OXException e) {
                    e.log(LOG);
                }
            }
            try {
                ATTACHMENT_BASE.finish();
            } catch (final OXException e) {
                e.log(LOG);
            }
        }
    }

    private void rollback(final OXException x) {
        try {
            ATTACHMENT_BASE.rollback();
        } catch (final OXException e) {
            e.log(LOG);
        }
        x.log(LOG);
    }

    @Override
    public void appointmentAccepted(final Appointment appointmentObj, final Session sessionObj) {
        // Nothing to do
    }

    @Override
    public void appointmentDeclined(final Appointment appointmentObj, final Session sessionObj) {
        // Nothing to do
    }

    @Override
    public void appointmentTentativelyAccepted(final Appointment appointmentObj, final Session sessionObj) {
        // Nothing to do
    }

    @Override
    public void appointmentWaiting(final Appointment appointmentObj, final Session sessionObj) {
        // Nothing to do
    }

    @Override
    public void taskAccepted(final Task taskObj, final Session sessionObj) {
        // Nothing to do
    }

    @Override
    public void taskDeclined(final Task taskObj, final Session sessionObj) {
        // Nothing to do
    }

    @Override
    public void taskTentativelyAccepted(final Task taskObj, final Session sessionObj) {
        // Nothing to do
    }

}

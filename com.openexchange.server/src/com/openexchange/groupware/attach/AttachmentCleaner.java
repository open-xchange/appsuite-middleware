/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.groupware.attach;

import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.event.impl.ContactEventInterface;
import com.openexchange.event.impl.NoDelayEventInterface;
import com.openexchange.event.impl.TaskEventInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.impl.AttachmentBaseImpl;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class AttachmentCleaner implements TaskEventInterface, ContactEventInterface, NoDelayEventInterface {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttachmentCleaner.class);

    private static final AttachmentBase ATTACHMENT_BASE = new AttachmentBaseImpl(new DBPoolProvider()); // No notifications, no permission

    @Override
    public final void taskDeleted(final Task taskObj, final Session sessionObj) {

        deleteAttachments(taskObj.getParentFolderID(), taskObj.getObjectID(), Types.TASK, sessionObj);
    }

    @Override
    public final void contactDeleted(final Contact contactObj, final Session sessionObj) {
        deleteAttachments(contactObj.getParentFolderID(), contactObj.getObjectID(), Types.CONTACT, sessionObj);

    }

    @Override
    public final void taskCreated(final Task taskObj, final Session sessionObj) {
        // Nothing to do

    }

    @Override
    public final void taskModified(final Task taskObj, final Session sessionObj) {
        // Nothing to do

    }

    @Override
    public final void contactCreated(final Contact contactObj, final Session sessionObj) {
        // Nothing to do

    }

    @Override
    public final void contactModified(final Contact contact, final Session session) {
        // Nothing to do.
    }

    private final void deleteAttachments(final int parentFolderID, final int objectID, final int type, final Session session) {
        SearchIterator<AttachmentMetadata> iter = null;
        try {
            final ServerSession sessionObj = ServerSessionAdapter.valueOf(session);
            ATTACHMENT_BASE.startTransaction();
            final TimedResult<AttachmentMetadata> rs =
                ATTACHMENT_BASE.getAttachments(
                    session, parentFolderID,
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
            do {
                ids.add(iter.next().getId());
            } while (iter.hasNext());

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

        } catch (OXException e) {
            rollback(e);
        } finally {
            SearchIterators.close(iter);
            try {
                ATTACHMENT_BASE.finish();
            } catch (OXException e) {
                log(e);
            }
        }
    }

    private void log(final OXException e) {
        switch (e.getCategories().get(0).getLogLevel()) {
            case TRACE:
                LOG.trace("", e);
                break;
            case DEBUG:
                LOG.debug("", e);
                break;
            case INFO:
                LOG.info("", e);
                break;
            case WARNING:
                LOG.warn("", e);
                break;
            case ERROR:
                LOG.error("", e);
                break;
            default:
                break;
        }
    }

    private void rollback(final OXException x) {
        try {
            ATTACHMENT_BASE.rollback();
        } catch (OXException e) {
            log(e);
        }
        log(x);
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

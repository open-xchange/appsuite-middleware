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
package com.openexchange.groupware.attach;

import java.io.InputStream;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.attach.impl.AttachmentImpl;
import com.openexchange.groupware.calendar.CalendarListener;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.java.Streams;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class CopyAttachmentsForChangeExceptions implements CalendarListener {
    private final AttachmentBase attachmentBase;

    public CopyAttachmentsForChangeExceptions(final AttachmentBase attachmentBase) {
        this.attachmentBase = attachmentBase;
    }

    @Override
    public void createdChangeExceptionInRecurringAppointment(final Appointment master, final Appointment exception,final int inFolder, final ServerSession session) throws OXException {
        try {
            attachmentBase.startTransaction();
            final Context ctx = session.getContext();
            final User userObject = UserStorage.getInstance().getUser(session.getUserId(), ctx);
            final UserConfiguration userConfig = UserConfigurationStorage.getInstance().getUserConfiguration(session.getUserId(), ctx);
            final TimedResult<AttachmentMetadata> result = attachmentBase.getAttachments(session, master.getParentFolderID(), master.getObjectID(), Types.APPOINTMENT, ctx, userObject, userConfig);
            final SearchIterator<AttachmentMetadata> iterator = result.results();
            int folderId = exception.getParentFolderID();
            if(folderId == 0) {
                folderId = inFolder;
            }

            while(iterator.hasNext()) {
                final AttachmentMetadata attachment = iterator.next();
                final AttachmentMetadata copy = new AttachmentImpl(attachment);
                copy.setId(AttachmentBase.NEW);
                copy.setFileId(null);
                copy.setAttachedId(exception.getObjectID());

                copy.setFolderId(folderId);
                copy.setModuleId(Types.APPOINTMENT);

                InputStream is = null;
                try {
                    is = attachmentBase.getAttachedFile(session, folderId, exception.getObjectID(), Types.APPOINTMENT, attachment.getId(), ctx, userObject, userConfig);
                    attachmentBase.attachToObject(copy, is, session, ctx, userObject, userConfig);
                } finally {
                    Streams.close(is);
                }
            }
            attachmentBase.commit();
        } catch (final OXException x) {
            attachmentBase.rollback();
            throw x;
        } finally {
            attachmentBase.finish();
        }

    }
}

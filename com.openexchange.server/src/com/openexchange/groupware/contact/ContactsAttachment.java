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

package com.openexchange.groupware.contact;

import static com.openexchange.java.Autoboxing.I;
import java.util.Date;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentAuthorization;
import com.openexchange.groupware.attach.AttachmentEvent;
import com.openexchange.groupware.attach.AttachmentListener;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserConfiguration;

/**
 * Contacts
 * @author <a href="mailto:ben.pahne@open-xchange.com">Benjamin Frederic Pahne</a>
 */
public class ContactsAttachment implements AttachmentListener, AttachmentAuthorization {

    public ContactsAttachment () {
        super();
    }

    @Override
    public long attached(final AttachmentEvent e) throws OXException {
        final int userId = e.getUser().getId();
        final int[] groups = e.getUser().getGroups();
        final Contact co = Contacts.getContactById(e.getAttachedId(), userId, groups, e.getContext(), e.getUserConfig(), e.getWriteConnection());
        co.setNumberOfAttachments(co.getNumberOfAttachments() + 1);
        Contacts.performContactStorageUpdate(co, co.getParentFolderID(), co.getLastModified(), userId, groups, e.getContext(), e.getUserConfig());
        return co.getLastModified().getTime();
    }

    @Override
    public long detached(final AttachmentEvent event) throws OXException {
        final Contact co = Contacts.getContactById(event.getAttachedId(),event.getUser().getId(),event.getUser().getGroups(),event.getContext(),event.getUserConfig(),event.getWriteConnection());
        if (co.getNumberOfAttachments() < 1) {
            throw ContactExceptionCodes.TOO_FEW_ATTACHMENTS.create();
        }
        final int[] xx = event.getDetached();
        co.setNumberOfAttachments(co.getNumberOfAttachments()-xx.length);
        final Date d = new Date(System.currentTimeMillis());
        Contacts.performContactStorageUpdate(co,co.getParentFolderID(),d,event.getUser().getId(),event.getUser().getGroups(),event.getContext(),event.getUserConfig());
        return co.getLastModified().getTime();
    }

    @Override
    public void checkMayAttach(final int folderId, final int objectId, final User user, final UserConfiguration userConfig, final Context ctx) throws OXException {
        final boolean back = Contacts.performContactWriteCheckByID(folderId, objectId,user.getId(),ctx,userConfig);
        if (!back) {
            throw ContactExceptionCodes.NO_CHANGE_PERMISSION.create(I(objectId), I(ctx.getContextId()));
        }
    }

    @Override
    public void checkMayDetach(final int folderId, final int objectId, final User user, final UserConfiguration userConfig, final Context ctx) throws OXException {
        checkMayAttach(folderId, objectId,user,userConfig,ctx);
    }

    @Override
    public void checkMayReadAttachments(final int folderId, final int objectId, final User user, final UserConfiguration userConfig, final Context ctx) throws OXException {
        final boolean back = Contacts.performContactReadCheckByID(folderId, objectId,user.getId(),ctx,userConfig);
        if (!back) {
            throw ContactExceptionCodes.NO_ACCESS_PERMISSION.create(I(folderId), I(ctx.getContextId()), I(user.getId()));
        }
    }
}

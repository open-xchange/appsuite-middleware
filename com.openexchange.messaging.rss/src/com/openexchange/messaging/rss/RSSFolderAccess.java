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

package com.openexchange.messaging.rss;

import com.openexchange.exception.OXException;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingFolderAccess;
import com.openexchange.messaging.Quota;
import com.openexchange.messaging.Quota.Type;
import com.openexchange.session.Session;

/**
 * {@link RSSFolderAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RSSFolderAccess extends RSSCommon implements MessagingFolderAccess {

    public RSSFolderAccess(final int accountId, final Session session) {
        super(accountId, session);
    }

    @Override
    public void clearFolder(final String folderId) throws OXException {
        checkFolder(folderId);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public void clearFolder(final String folderId, final boolean hardDelete) throws OXException {
        checkFolder(folderId);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public String createFolder(final MessagingFolder toCreate) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public String deleteFolder(final String folderId) throws OXException {
        checkFolder(folderId);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public String deleteFolder(final String folderId, final boolean hardDelete) throws OXException {
        checkFolder(folderId);
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public boolean exists(final String folderId) {
        return EMPTY.equals(folderId);
    }

    @Override
    public String getConfirmedHamFolder() {
        return null;
    }

    @Override
    public String getConfirmedSpamFolder() {
        return null;
    }

    @Override
    public String getDraftsFolder() {
        return null;
    }

    @Override
    public MessagingFolder getFolder(final String folderId) throws OXException {
        checkFolder(folderId);
        return new RSSFolder(session.getUserId());
    }

    private static final Quota.Type[] MESSAGE = { Quota.Type.MESSAGE };

    @Override
    public Quota getMessageQuota(final String folderId) throws OXException {
        checkFolder(folderId);
        return getQuotas(folderId, MESSAGE)[0];
    }

    private static final MessagingFolder[] EMPTY_PATH = new MessagingFolder[0];

    @Override
    public MessagingFolder[] getPath2DefaultFolder(final String folderId) {
        return EMPTY_PATH;
    }

    @Override
    public Quota[] getQuotas(final String folder, final Type[] types) throws OXException {
        checkFolder(folder);
        return Quota.getUnlimitedQuotas(types);
    }

    @Override
    public MessagingFolder getRootFolder() {
        return new RSSFolder(session.getUserId());
    }

    @Override
    public String getSentFolder() {
        return null;
    }

    @Override
    public String getSpamFolder() {
        return null;
    }

    private static final Quota.Type[] STORAGE = { Quota.Type.STORAGE };

    @Override
    public Quota getStorageQuota(final String folderId) throws OXException {
        checkFolder(folderId);
        return getQuotas(folderId, STORAGE)[0];
    }

    @Override
    public MessagingFolder[] getSubfolders(final String parentIdentifier, final boolean all) throws OXException {
        checkFolder(parentIdentifier);
        return new MessagingFolder[0];
    }

    @Override
    public String getTrashFolder() throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public String moveFolder(final String folderId, final String newParentId) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public String renameFolder(final String folderId, final String newName) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

    @Override
    public String updateFolder(final String identifier, final MessagingFolder toUpdate) throws OXException {
        throw MessagingExceptionCodes.OPERATION_NOT_SUPPORTED.create(RSSMessagingService.ID);
    }

}

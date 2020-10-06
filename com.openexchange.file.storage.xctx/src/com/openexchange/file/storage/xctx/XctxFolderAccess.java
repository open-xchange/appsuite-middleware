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

package com.openexchange.file.storage.xctx;

import static com.openexchange.java.Autoboxing.I;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.infostore.folder.AbstractInfostoreFolderAccess;
import com.openexchange.file.storage.infostore.folder.FolderConverter;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.tools.arrays.Collections;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link XctxFolderAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class XctxFolderAccess extends AbstractInfostoreFolderAccess {
    
    static final Set<String> UNSUPPORTED_FOLDER_IDS = Collections.unmodifiableSet(
        AbstractInfostoreFolderAccess.INFOSTORE_FOLDER_ID, AbstractInfostoreFolderAccess.PUBLIC_INFOSTORE_FOLDER_ID);

    private final XctxAccountAccess accountAccess;
    private final XctxFolderConverter folderConverter;
    private final ServerSession localSession;

    /**
     * Initializes a new {@link XctxFolderAccess}.
     *
     * @param accountAccess The parent account access
     * @param localSession The user's <i>local</i> session associated with the file storage account
     * @param guestSession The <i>remote</i> session of the guest user used to access the contents of the foreign context
     */
    public XctxFolderAccess(XctxAccountAccess accountAccess, ServerSession localSession, ServerSession guestSession) {
        super(guestSession);
        this.localSession = localSession;
        this.accountAccess = accountAccess;
        this.folderConverter = new XctxFolderConverter(accountAccess, localSession, guestSession);
    }

    @Override
    protected FolderService getFolderService() throws OXException {
        return accountAccess.getServiceSafe(FolderService.class);
    }

    @Override
    protected InfostoreFacade getInfostore() throws OXException {
        return accountAccess.getServiceSafe(InfostoreFacade.class);
    }

    @Override
    protected FolderConverter getConverter() {
        return folderConverter;
    }

    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        if (UNSUPPORTED_FOLDER_IDS.contains(folderId)) {
            throw FileStorageExceptionCodes.FOLDER_NOT_FOUND.create(
                folderId, accountAccess.getAccountId(), accountAccess.getService().getId(), I(localSession.getUserId()), I(localSession.getContextId()));
        }
        return super.getFolder(folderId);
    }

    @Override
    protected FileStorageFolder getDefaultFolder(com.openexchange.folderstorage.Type type) throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public FileStorageFolder getRootFolder() throws OXException {
        throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
    }

    @Override
    public String toString() {
        return "XctxFolderAccess [accountId=" + accountAccess.getAccountId() + 
            ", localUser=" + localSession.getUserId() + '@' + localSession.getContextId() + 
            ", guestUser=" + super.session.getUserId() + '@' + super.session.getContextId() + ']';
    }

}

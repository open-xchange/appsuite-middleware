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

package com.openexchange.file.storage.onedrive.oauth;

import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.onedrive.OneDriveConstants;
import com.openexchange.file.storage.onedrive.access.OneDriveOAuthAccess;
import com.openexchange.oauth.association.OAuthAccountAssociation;
import com.openexchange.oauth.association.Status;
import com.openexchange.oauth.association.Type;
import com.openexchange.session.Session;


/**
 * {@link OneDriveOAuthAccountAssociation}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class OneDriveOAuthAccountAssociation implements OAuthAccountAssociation {

    private final int oAuthAccountId;
    private final FileStorageAccount fileStorageAccount;
    private final int userId;
    private final int contextId;

    /**
     * Initializes a new {@link OneDriveOAuthAccountAssociation}.
     *
     * @param oAuthAccountId The identifier of the OAuth account
     * @param fileStorageAccount The association Google Drive file storage account
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public OneDriveOAuthAccountAssociation(int oAuthAccountId, FileStorageAccount fileStorageAccount, int userId, int contextId) {
        super();
        this.oAuthAccountId = oAuthAccountId;
        this.fileStorageAccount = fileStorageAccount;
        this.userId = userId;
        this.contextId = contextId;

    }

    @Override
    public int getOAuthAccountId() {
        return oAuthAccountId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public int getContextId() {
        return contextId;
    }

    @Override
    public String getServiceId() {
        return OneDriveConstants.ID;
    }

    @Override
    public String getId() {
        return fileStorageAccount.getId();
    }

    @Override
    public String getDisplayName() {
        return fileStorageAccount.getDisplayName();
    }

    @Override
    public Type getType() {
        return Type.FILE_STORAGE;
    }

    @Override
    public Status getStatus(Session session) throws OXException {
        OneDriveOAuthAccess access = new OneDriveOAuthAccess(fileStorageAccount, session);
        try {
            access.initialize();
        } catch (OXException e) {
            return Status.RECREATION_NEEDED;
        }
        boolean success = access.ping();
        if (success) {
            return Status.OK;
        }
        return Status.INVALID_GRANT;
    }

}

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

package com.openexchange.file.storage.googledrive.oauth;

import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.googledrive.GoogleDriveConstants;
import com.openexchange.file.storage.googledrive.access.GoogleDriveOAuthAccess;
import com.openexchange.file.storage.oauth.AbstractFileStorageOAuthAccountAssociation;
import com.openexchange.oauth.access.AbstractOAuthAccess;
import com.openexchange.oauth.google.GoogleOAuthScope;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.session.Session;

/**
 * {@link GoogleDriveOAuthAccountAssociation}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class GoogleDriveOAuthAccountAssociation extends AbstractFileStorageOAuthAccountAssociation {

    /**
     * Initializes a new {@link GoogleDriveOAuthAccountAssociation}.
     *
     * @param oAuthAccountId The identifier of the OAuth account
     * @param fileStorageAccount The association Google Drive file storage account
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public GoogleDriveOAuthAccountAssociation(int oAuthAccountId, FileStorageAccount fileStorageAccount, int userId, int contextId) {
        super(oAuthAccountId, userId, contextId, fileStorageAccount);
    }

    @Override
    public String getServiceId() {
        return GoogleDriveConstants.ID;
    }

    @Override
    protected AbstractOAuthAccess newAccess(Session session) throws OXException {
        return new GoogleDriveOAuthAccess(getFileStorageAccount(), session);
    }

    @Override
    public List<OAuthScope> getScopes() {
        return Collections.singletonList(GoogleOAuthScope.drive);
    }
}

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

package com.openexchange.file.storage.dropbox;

import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.CompositeFileStorageAccountManagerProvider;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.RootFolderPermissionsAware;
import com.openexchange.file.storage.dropbox.access.DropboxAccountAccess;
import com.openexchange.file.storage.oauth.AbstractOAuthFileStorageService;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.dropbox.DropboxOAuthScope;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link DropboxFileStorageService} - The Dropbox file storage service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DropboxFileStorageService extends AbstractOAuthFileStorageService implements RootFolderPermissionsAware {

    /**
     * Initializes a new {@link DropboxFileStorageService}.
     * 
     * @param services The {@link ServiceLookup}
     */
    public DropboxFileStorageService(ServiceLookup services) {
        super(services, KnownApi.DROPBOX, DropboxConstants.DISPLAY_NAME, DropboxConstants.ID);
    }

    /**
     * Initialises a new {@link DropboxFileStorageService}.
     *
     * @param services The {@link ServiceLookup}
     * @param compositeFileStorageAccountManagerProvider The {@link CompositeFileStorageAccountManagerProvider}
     */
    public DropboxFileStorageService(ServiceLookup services, CompositeFileStorageAccountManagerProvider compositeFileStorageAccountManagerProvider) {
        super(services, KnownApi.DROPBOX, DropboxConstants.DISPLAY_NAME, DropboxConstants.ID, compositeFileStorageAccountManagerProvider);
    }

    @Override
    public FileStorageAccountAccess getAccountAccess(final String accountId, final Session session) throws OXException {
        final FileStorageAccount account = getAccountAccess(session, accountId);
        return new DropboxAccountAccess(this, account, session);
    }

    @Override
    public List<FileStoragePermission> getRootFolderPermissions(String accountId, Session session) throws OXException {
        DefaultFileStoragePermission permission = DefaultFileStoragePermission.newInstance();
        permission.setAdmin(true);
        permission.setFolderPermission(FileStoragePermission.CREATE_SUB_FOLDERS);
        permission.setEntity(session.getUserId());
        return Collections.<FileStoragePermission> singletonList(permission);
    }

    @Override
    protected OAuthScope getScope() {
        return DropboxOAuthScope.drive;
    }
}

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

package com.openexchange.file.storage.infostore.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.Set;
import com.openexchange.context.ContextService;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AdministrativeFileStorageFileAccess;
import com.openexchange.file.storage.AdministrativeFileStorageService;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.infostore.osgi.Services;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.session.Session;

/**
 * {@link InfostoreFileStorageService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreFileStorageService implements AdministrativeFileStorageService {

    private InfostoreFacade infostore;

    private InfostoreSearchEngine search;

    @Override
    public FileStorageAccountAccess getAccountAccess(final String accountId, final Session session) throws OXException {
        if (!accountId.equals(InfostoreDefaultAccountManager.DEFAULT_ID)) {
            throw FileStorageExceptionCodes.ACCOUNT_NOT_FOUND.create(accountId, getId(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
        }
        return new InfostoreAccountAccess(session, this);
    }

    @Override
    public FileStorageAccountManager getAccountManager() {
        return new InfostoreDefaultAccountManager(this);
    }

    @Override
    public String getDisplayName() {
        return "Standard Infostore";
    }

    @Override
    public String getId() {
        return "com.openexchange.infostore";
    }

    @Override
    public Set<String> getSecretProperties() {
        return Collections.emptySet();
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return new DynamicFormDescription();
    }

    // Override this for OSGi Lookup, use the setter in tests
    public InfostoreFacade getInfostore() {
        return infostore;
    }

    public void setInfostore(final InfostoreFacade infostore) {
        this.infostore = infostore;
    }

    // Override this for OSGi Lookup.
    public InfostoreSearchEngine getSearch() {
        return search;
    }

    public void setSearch(final InfostoreSearchEngine search) {
        this.search = search;
    }

    @Override
    public AdministrativeFileStorageFileAccess getAdministrativeFileAccess(String accountId, int contextId) throws OXException {
        if (!accountId.equals(InfostoreDefaultAccountManager.DEFAULT_ID)) {
            throw FileStorageExceptionCodes.ACCOUNT_NOT_FOUND.create(accountId, getId(), "admin", I(contextId));
        }
        return new AdministrativeInfostoreFileAccess(getInfostore(), Services.getService(ContextService.class).getContext(contextId));
    }

}

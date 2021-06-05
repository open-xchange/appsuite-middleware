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

package com.openexchange.file.storage.dropbox.osgi;

import org.osgi.framework.BundleContext;
import com.openexchange.file.storage.CompositeFileStorageAccountManagerProvider;
import com.openexchange.file.storage.dropbox.DropboxConstants;
import com.openexchange.file.storage.dropbox.DropboxFileStorageService;
import com.openexchange.file.storage.dropbox.oauth.DropboxOAuthAccountAssociationProvider;
import com.openexchange.file.storage.oauth.AbstractOAuthFileStorageService;
import com.openexchange.file.storage.oauth.osgi.AbstractCloudStorageServiceRegisterer;
import com.openexchange.oauth.association.spi.OAuthAccountAssociationProvider;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DropboxServiceRegisterer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class DropboxServiceRegisterer extends AbstractCloudStorageServiceRegisterer {

    /**
     * Initializes a new {@link DropboxServiceRegisterer}.
     */
    public DropboxServiceRegisterer(BundleContext context, ServiceLookup services) {
        super(context, services);
    }

    @Override
    protected AbstractOAuthFileStorageService getCloudFileStorageService() {
        return new DropboxFileStorageService(getServiceLookup());
    }

    @Override
    protected AbstractOAuthFileStorageService getCloudFileStorageService(CompositeFileStorageAccountManagerProvider compositeProvider) {
        return new DropboxFileStorageService(getServiceLookup(), compositeProvider);
    }

    @Override
    protected OAuthAccountAssociationProvider getOAuthAccountAssociationProvider(AbstractOAuthFileStorageService storageService) {
        return new DropboxOAuthAccountAssociationProvider((DropboxFileStorageService) storageService);
    }

    @Override
    protected String getProviderId() {
        return DropboxConstants.ID;
    }

}

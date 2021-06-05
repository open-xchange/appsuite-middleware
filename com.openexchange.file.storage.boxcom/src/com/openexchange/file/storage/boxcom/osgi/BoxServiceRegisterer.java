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

package com.openexchange.file.storage.boxcom.osgi;

import org.osgi.framework.BundleContext;
import com.openexchange.file.storage.CompositeFileStorageAccountManagerProvider;
import com.openexchange.file.storage.boxcom.BoxConstants;
import com.openexchange.file.storage.boxcom.BoxFileStorageService;
import com.openexchange.file.storage.boxcom.oauth.BoxOAuthAccountAssociationProvider;
import com.openexchange.file.storage.oauth.AbstractOAuthFileStorageService;
import com.openexchange.file.storage.oauth.osgi.AbstractCloudStorageServiceRegisterer;
import com.openexchange.oauth.association.spi.OAuthAccountAssociationProvider;
import com.openexchange.server.ServiceLookup;

/**
 * {@link BoxServiceRegisterer}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class BoxServiceRegisterer extends AbstractCloudStorageServiceRegisterer {

    /**
     * Initializes a new {@link BoxServiceRegisterer}.
     */
    public BoxServiceRegisterer(BundleContext context, ServiceLookup services) {
        super(context, services);
    }

    @Override
    protected AbstractOAuthFileStorageService getCloudFileStorageService() {
        return new BoxFileStorageService(getServiceLookup());
    }

    @Override
    protected AbstractOAuthFileStorageService getCloudFileStorageService(CompositeFileStorageAccountManagerProvider compositeProvider) {
        return new BoxFileStorageService(getServiceLookup(), compositeProvider);
    }

    @Override
    protected OAuthAccountAssociationProvider getOAuthAccountAssociationProvider(AbstractOAuthFileStorageService storageService) {
        return new BoxOAuthAccountAssociationProvider((BoxFileStorageService) storageService);
    }

    @Override
    protected String getProviderId() {
        return BoxConstants.ID;
    }

}

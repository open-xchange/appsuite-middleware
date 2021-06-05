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

package com.openexchange.oauth.microsoft.graph.osgi;

import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.common.osgi.AbstractOAuthActivator;
import com.openexchange.oauth.microsoft.graph.MicrosoftGraphOAuthScope;
import com.openexchange.oauth.microsoft.graph.MicrosoftGraphOAuthServiceMetaData;
import com.openexchange.oauth.microsoft.graph.groupware.MigrateMSLiveAccountsTask;
import com.openexchange.oauth.scope.OAuthScope;

/**
 * {@link MicrosoftGraphOAuthActivator} - The activator for the Microsoft Graph OAuth service.
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public final class MicrosoftGraphOAuthActivator extends AbstractOAuthActivator {

    /**
     * Initialises a new {@link MicrosoftGraphOAuthActivator}.
     */
    public MicrosoftGraphOAuthActivator() {
        super();
    }

    @Override
    protected void startBundle() throws Exception {
        super.startBundle();
        // Register the update task
        DefaultUpdateTaskProviderService providerService = new DefaultUpdateTaskProviderService(new MigrateMSLiveAccountsTask());
        registerService(UpdateTaskProviderService.class.getName(), providerService);
    }

    @Override
    protected OAuthServiceMetaData getOAuthServiceMetaData() {
        return new MicrosoftGraphOAuthServiceMetaData(this);
    }

    @Override
    protected OAuthScope[] getScopes() {
        return MicrosoftGraphOAuthScope.values();
    }
}

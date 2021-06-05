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

package com.openexchange.oauth.dropbox.osgi;

import com.google.common.collect.ImmutableList;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.common.osgi.AbstractOAuthActivator;
import com.openexchange.oauth.dropbox.DropboxOAuthScope;
import com.openexchange.oauth.dropbox.DropboxOAuthServiceMetaData;
import com.openexchange.oauth.dropbox.internal.groupware.OAuthDropboxDropLongTermTokensTask;
import com.openexchange.oauth.dropbox.internal.groupware.OAuthDropboxDropTokensTask;
import com.openexchange.oauth.scope.OAuthScope;

/**
 * {@link DropboxOAuthActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DropboxOAuthActivator extends AbstractOAuthActivator {

    /**
     * Initializes a new {@link DropboxOAuthActivator}.
     */
    public DropboxOAuthActivator() {
        super();
    }

    @Override
    protected void startBundle() throws Exception {
        DropboxOAuthServices.setServices(this);
        registerService(UpdateTaskProviderService.class, () -> ImmutableList.of(new OAuthDropboxDropTokensTask(), new OAuthDropboxDropLongTermTokensTask()));
        super.startBundle();
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            // Clear service registry
            DropboxOAuthServices.setServices(null);
            super.stopBundle();
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(DropboxOAuthActivator.class).error("", e);
            throw e;
        }
    }

    @Override
    protected OAuthServiceMetaData getOAuthServiceMetaData() {
        return new DropboxOAuthServiceMetaData(this);
    }

    @Override
    protected OAuthScope[] getScopes() {
        return DropboxOAuthScope.values();
    }
}

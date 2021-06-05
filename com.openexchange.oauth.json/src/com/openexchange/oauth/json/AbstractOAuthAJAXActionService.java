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

package com.openexchange.oauth.json;

import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.association.OAuthAccountAssociationService;
import com.openexchange.secret.SecretService;
import com.openexchange.session.Session;

/**
 * {@link AbstractOAuthAJAXActionService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractOAuthAJAXActionService implements AJAXActionService {

    /**
     * The {@link DefaultDeferringURLService} reference.
     */
    public static final java.util.concurrent.atomic.AtomicReference<DispatcherPrefixService> PREFIX = new java.util.concurrent.atomic.AtomicReference<DispatcherPrefixService>();

    private static volatile OAuthService oAuthService;
    private static volatile OAuthAccountAssociationService oAuthAccountAssociationService;
    private static volatile SecretService secretService;

    /**
     * Sets the OAuth service
     *
     * @param oAuthService The OAuth service
     */
    public static void setOAuthService(final OAuthService oAuthService) {
        AbstractOAuthAJAXActionService.oAuthService = oAuthService;
    }

    /**
     * Gets the OAuth service
     *
     * @return The OAuth service
     */
    public static OAuthService getOAuthService() {
        return oAuthService;
    }

    public static void setSecretService(final SecretService secretService) {
        AbstractOAuthAJAXActionService.secretService = secretService;
    }

    public static String secret(final Session session) {
        return secretService.getSecret(session);
    }

    /**
     * Sets the <code>OAuthAccountAssociationService</code> instance
     *
     * @param oAuthAccountAssociationService The instance
     */
    public static void setOAuthAccountAssociationService(final OAuthAccountAssociationService oAuthAccountAssociationService) {
        AbstractOAuthAJAXActionService.oAuthAccountAssociationService = oAuthAccountAssociationService;
    }

    /**
     * Gets the <code>OAuthAccountAssociationService</code> instance
     *
     * @return The instance or <code>null</code>
     */
    public static OAuthAccountAssociationService getOAuthAccountAssociationService() {
        return oAuthAccountAssociationService;
    }

    /**
     * Initializes a new {@link AbstractOAuthAJAXActionService}.
     */
    protected AbstractOAuthAJAXActionService() {
        super();
    }

}

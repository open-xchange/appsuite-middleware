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

package com.openexchange.oauth.association;

import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.scope.OAuthScope;
import com.openexchange.session.Session;

/**
 * {@link OAuthAccountAssociation} - Represents a resource that utilizes an OAuth account to authenticate against a remote end-point/service and using its APIs.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public interface OAuthAccountAssociation {

    /**
     * Gets the identifier of the associated OAuth account.
     *
     * @return The OAuth account identifier
     */
    int getOAuthAccountId();

    /**
     * Gets the identifier of the user that owns this OAuth account association
     *
     * @return The user identifier
     */
    int getUserId();

    /**
     * Gets the identifier of the context in which the user resides that owns this OAuth account association
     *
     * @return The context identifier
     */
    int getContextId();

    // ---------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the identifier of the service that uses the OAuth account; i.e. <code>"googledrive"</code>.
     *
     * @return The service identifier
     */
    String getServiceId();

    /**
     * Gets the identifier for this association.
     * <p>
     * Typically the identifier of the resource/account; e.g. the identifier for the concrete file storage account.
     *
     * @return The identifier
     */
    String getId();

    /**
     * Gets the display name; i.e. <code>"My Google Drive"</code>.
     *
     * @return The display name
     */
    String getDisplayName();

    /**
     * Gets the module which the OAuth account is associated with.
     *
     * @return The module which the OAuth account is associated with
     */
    String getModule();

    /**
     * Gets the optional folder of the association.
     *
     * @return The optional folder or <code>null</code> if no
     *         folder is associated with this {@link OAuthAccountAssociation}
     */
    String getFolder();

    // ---------------------------------------------------------------------------------------------------------------------

    /**
     * Checks the current status of the account association.
     *
     * @param session The session providing user data
     * @return The status for this account association.
     * @throws OXException If status cannot be retrieved
     */
    Status getStatus(Session session) throws OXException;

    /**
     * Gets an unmodifiable {@link List} with all enabled scopes of the association.
     *
     * @return An unmodifiable {@link List} with all enabled scopes of the association
     */
    List<OAuthScope> getScopes();

}

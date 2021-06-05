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

package com.openexchange.ajax.folder.actions;

import com.openexchange.folderstorage.Permissions;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.share.recipient.ShareRecipient;
import com.openexchange.testing.httpclient.invoker.ApiClient;

/**
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class OCLGuestPermission extends OCLPermission {

    private static final long serialVersionUID = -3277662647906767821L;

    private ShareRecipient recipient;

    private ApiClient apiClient;

    /**
     * Initializes an empty {@link OCLGuestPermission}.
     */
    public OCLGuestPermission() {
        super();
    }

    /**
     * Initializes a new {@link OCLGuestPermission}.
     *
     * @param recipient The share recipient
     */
    public OCLGuestPermission(ShareRecipient recipient) {
        super();
        this.recipient = recipient;
    }

    public ShareRecipient getRecipient() {
        return recipient;
    }

    public void setRecipient(ShareRecipient recipient) {
        this.recipient = recipient;
    }

    public int getPermissionBits() {
        return Permissions.createPermissionBits(getFolderPermission(), getReadPermission(), getWritePermission(), getDeletePermission(), isFolderAdmin());
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

}

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

package com.openexchange.share.subscription;

/**
 * {@link ShareSubscriptionInformation}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public final class ShareSubscriptionInformation {

    private final String accountId;

    private final String module;

    private final String folder;

    /**
     * Initializes a new {@link ShareSubscriptionInformation}.
     * 
     * @param accountId The account ID of the subscription
     * @param module The module name as per {@link com.openexchange.share.impl.groupware.ShareModuleMapping}
     * @param folder The folder ID
     */
    public ShareSubscriptionInformation(String accountId, String module, String folder) {
        super();
        this.accountId = accountId;
        this.module = module;
        this.folder = folder;
    }

    /**
     * Gets the accountId
     *
     * @return The accountId
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * Gets the module
     *
     * @return The module
     */
    public String getModule() {
        return module;
    }

    /**
     * Gets the folder
     *
     * @return The folder
     */
    public String getFolder() {
        return folder;
    }

    @Override
    public String toString() {
        return "ShareSubscriptionInformation [accountId=" + accountId + ", module=" + module + ", folder=" + folder + "]";
    }
}

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

package com.openexchange.file.storage.xox;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.Property;

/**
 * {@link XOXFileStorageProperties}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class XOXFileStorageProperties {

    /**
     * The time interval, in seconds, after which the access to an an error afflicted XOX (cross ox) sharing account should be retried.
     */
    public static final Property RETRY_AFTER_ERROR_INTERVAL = DefaultProperty.valueOf("com.openexchange.file.storage.xox.retryAfterErrorInterval", I(3600 /* seconds = 1h */));

    /**
     * Defines the maximum number of allowed accounts within the xox file storage provider. A value of 0 disables the limit.
     */
    public static final Property MAX_ACCOUNTS = DefaultProperty.valueOf("com.openexchange.file.storage.xox.maxAccounts", I(20));

    /**
     * Indicates whether the automatic removal of accounts in the <i>cross-ox</i> file storage provider that refer to a no
     * longer existing guest user in the remote context is enabled or not.
     */
    public static final Property AUTO_REMOVE_UNKNOWN_SHARES = DefaultProperty.valueOf("com.openexchange.file.storage.xox.autoRemoveUnknownShares", Boolean.TRUE);
}

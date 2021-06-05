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

package com.openexchange.pop3.storage;

/**
 * {@link POP3StoragePropertyNames} - Constants for POP3 storage properties.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class POP3StoragePropertyNames {

    /**
     * Initializes a new {@link POP3StoragePropertyNames}.
     */
    private POP3StoragePropertyNames() {
        super();
    }

    /**
     * The abbreviation of the storage to use;<br />
     * e.g. <code>"mailaccount"</code> to use the default mail account as storage.
     *
     * @type <code>String</code>
     */
    public static final String PROPERTY_STORAGE = "pop3.storage";

    /**
     * The path in storage to the folder/resource keeping POP3 messages;<br />
     * e.g. <code>"INBOX/My POP3 Messages"</code> inside an mail account storage.
     *
     * @type <code>String</code>
     */
    public static final String PROPERTY_PATH = "pop3.path";

    /**
     * The refresh rate in minutes when a new connection to actual POP3 account is (possibly) allowed.
     *
     * @type <code>Integer</code>
     */
    public static final String PROPERTY_REFRESH_RATE = "pop3.refreshrate";

    /**
     * The last-accessed time stamp the last access to actual POP3 account took place.
     *
     * @type <code>Long</code>
     */
    public static final String PROPERTY_LAST_ACCESSED = "pop3.lastaccess";

    /**
     * Whether to only retrieve (<code>"false"</code>) or to retrieve-and-delete (<code>"true"</code>) messages from POP3 account.
     *
     * @type <code>Boolean</code>
     */
    public static final String PROPERTY_EXPUNGE = "pop3.expunge";

    /**
     * Whether a delete operation performed on storage also deletes affected message in POP3 account.
     *
     * @type <code>Boolean</code>
     */
    public static final String PROPERTY_DELETE_WRITE_THROUGH = "pop3.deletewt";

}

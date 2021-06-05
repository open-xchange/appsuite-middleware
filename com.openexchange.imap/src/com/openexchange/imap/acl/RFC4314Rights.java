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

package com.openexchange.imap.acl;

import com.sun.mail.imap.Rights.Right;

/**
 * {@link RFC4314Rights} - Provides constants for the additional rights defined in <small><b><a
 * href="http://www.rfc-archive.org/getrfc.php?rfc=4314">RFC 4314</a></b></small>.
 * <p>
 * <ul>
 * <li><b><code>k</code></b> - create mailboxes (CREATE new sub-mailboxes in any implementation-defined hierarchy, parent mailbox for the
 * new mailbox name in RENAME)</li>
 * <li><b><code>x</code></b> - delete mailbox (DELETE mailbox, old mailbox name in RENAME)</li>
 * <li><b><code>t</code></b> - delete messages (set or clear \DELETED flag via STORE, set \DELETED flag during APPEND/COPY)</li>
 * <li><b><code>e</code></b> - perform EXPUNGE and expunge as a part of CLOSE</li>
 * <ul>
 * <br>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RFC4314Rights {

    /**
     * Create mailboxes - CREATE new sub-mailboxes in any implementation-defined hierarchy, parent mailbox for the new mailbox name in
     * RENAME.
     */
    public static final Right CREATE_MAILBOXES = Right.getInstance('k');

    /**
     * Delete mailbox - DELETE mailbox, old mailbox name in RENAME.
     */
    public static final Right DELETE_MAILBOX = Right.getInstance('x');

    /**
     * Delete messages - Set or clear \DELETED flag via STORE, set \DELETED flag during APPEND/COPY.
     */
    public static final Right DELETE_MESSAGES = Right.getInstance('t');

    /**
     * Perform EXPUNGE and expunge as a part of CLOSE.
     */
    public static final Right EXPUNGE = Right.getInstance('e');

    /**
     * Initializes a new {@link RFC4314Rights}.
     */
    private RFC4314Rights() {
        super();
    }

}

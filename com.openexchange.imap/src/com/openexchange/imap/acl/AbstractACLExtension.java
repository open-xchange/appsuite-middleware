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

import com.sun.mail.imap.Rights;

/**
 * {@link AbstractACLExtension} - The abstract ACL extension for common rights in <small><b><a
 * href="http://www.rfc-archive.org/getrfc.php?rfc=2086">RFC 2086</a></b></small> and <small><b><a
 * href="http://www.rfc-archive.org/getrfc.php?rfc=4314">RFC 4314</a></b></small>
 * <p>
 * <ul>
 * <li><b><code>l</code></b> - lookup (mailbox is visible to LIST/LSUB commands, SUBSCRIBE mailbox)</li>
 * <li><b><code>r</code></b> - read (SELECT the mailbox, perform STATUS)</li>
 * <li><b><code>s</code></b> - keep seen/unseen information across sessions (set or clear \SEEN flag via STORE, also set \SEEN during
 * APPEND/COPY/ FETCH BODY[...])</li>
 * <li><b><code>w</code></b> - write (set or clear flags other than \SEEN and \DELETED via STORE, also set them during APPEND/COPY)</li>
 * <li><b><code>i</code></b> - insert (perform APPEND, COPY into mailbox)</li>
 * <li><b><code>p</code></b> - post (send mail to submission address for mailbox, not enforced by IMAP4 itself)</li>
 * <ul>
 * <br>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
abstract class AbstractACLExtension implements ACLExtension {

    /**
     * Initializes a new {@link AbstractACLExtension}.
     */
    protected AbstractACLExtension() {
        super();
    }

    @Override
    public boolean aclSupport() {
        return true;
    }

    @Override
    public boolean canRead(Rights rights) {
        return rights.contains(Rights.Right.READ);
    }

    @Override
    public boolean canLookUp(Rights rights) {
        return rights.contains(Rights.Right.LOOKUP);
    }

    @Override
    public boolean canKeepSeen(Rights rights) {
        return rights.contains(Rights.Right.KEEP_SEEN);
    }

    @Override
    public boolean canWrite(Rights rights) {
        return rights.contains(Rights.Right.WRITE);
    }

    @Override
    public boolean canInsert(Rights rights) {
        return rights.contains(Rights.Right.INSERT);
    }

    @Override
    public boolean canPost(Rights rights) {
        return rights.contains(Rights.Right.POST);
    }

}

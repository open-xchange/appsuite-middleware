/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
    public boolean canRead(final Rights rights) {
        return rights.contains(Rights.Right.READ);
    }

    @Override
    public boolean canLookUp(final Rights rights) {
        return rights.contains(Rights.Right.LOOKUP);
    }

    @Override
    public boolean canKeepSeen(final Rights rights) {
        return rights.contains(Rights.Right.KEEP_SEEN);
    }

    @Override
    public boolean canWrite(final Rights rights) {
        return rights.contains(Rights.Right.WRITE);
    }

    @Override
    public boolean canInsert(final Rights rights) {
        return rights.contains(Rights.Right.INSERT);
    }

    @Override
    public boolean canPost(final Rights rights) {
        return rights.contains(Rights.Right.POST);
    }

}

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

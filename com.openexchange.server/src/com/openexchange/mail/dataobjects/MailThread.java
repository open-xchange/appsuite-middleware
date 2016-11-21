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

package com.openexchange.mail.dataobjects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * {@link MailThread} - Represents the thread references for a certain folder.
 * <p>
 * Example:
 * <p>
 * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in;">
 *   <img src="./thread-example.png" alt="OX Drive Stand-Alone">
 * </div>
 * <p>
 * The first thread consists only of message 2. The second thread consists of the messages 3 (parent) and 6 (child), after which it
 * splits into two sub-threads; the first of which contains messages 4 (child of 6, sibling of 44) and 23 (child of 4), and the second
 * of which contains messages 44 (child of 6, sibling of 4), 7 (child of 44), and 96 (child of 7). Since some later messages are
 * parents of earlier messages, the messages were probably moved from some other mailbox at different times.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailThread {

    private final MailMessage parent;
    private final List<MailThread> children;

    /**
     * Initializes a new {@link MailThread} without a parent message.
     */
    public MailThread() {
        this(null);
    }

    /**
     * Initializes a new {@link MailThread}.
     *
     * @param parent The thread's parent message or <code>null</code>
     */
    public MailThread(MailMessage parent) {
        super();
        this.parent = parent;
        children = new ArrayList<>();
    }

    /**
     * Gets the parent
     *
     * @return The parent
     */
    public MailMessage getParent() {
        return parent;
    }

    /**
     * Gets the children
     *
     * @return The children
     */
    public List<MailThread> getChildren() {
        return children;
    }

    /**
     * Adds specified children to this mail thread.
     *
     * @param children The children to add
     */
    public void addChildren(Collection<MailThread> children) {
        if (null != children) {
            this.children.addAll(children);
        }
    }

}

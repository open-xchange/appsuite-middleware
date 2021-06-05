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

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

package com.openexchange.imap.threadsort;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * {@link ThreadSortNode} - Represents a tree node in a thread-sort string.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ThreadSortNode {

    /**
     * The message information of this tree node's message.
     */
    final MessageInfo msgInfo;

    /**
     * The UID.
     */
    final long uid;

    private final List<ThreadSortNode> childs;

    /**
     * Initializes a new {@link ThreadSortNode}.
     *
     * @param msgInfo The  message information of this tree node's message.
     */
    public ThreadSortNode(final MessageInfo msgInfo, final long uid) {
        super();
        this.msgInfo = msgInfo;
        childs = new ArrayList<ThreadSortNode>();
        this.uid = uid;
    }

    /**
     * Adds a child to this tree node.
     *
     * @param child The child to add.
     */
    public void addChild(final ThreadSortNode child) {
        childs.add(child);
    }

    /**
     * Adds children to this tree node.
     *
     * @param childThreads The children to add.
     */
    public void addChildren(final List<ThreadSortNode> childThreads) {
        childs.addAll(childThreads);
    }

    @Override
    public String toString() {
        return new StringBuilder().append(msgInfo).append(' ').append(childs).toString();
    }

    /**
     * Gets the UID.
     *
     * @return The UID
     */
    public long getUid() {
        return uid;
    }

    /**
     * Gets the message information of this tree node's message.
     *
     * @return The message information of this tree node's message
     */
    public MessageInfo getMsgInfo() {
        return msgInfo;
    }

    /**
     * Gets the number of this tree node's message.
     *
     * @return The number of this tree node's message.
     */
    public int getMsgNum() {
        return msgInfo.getMessageNumber();
    }

    /**
     * Gets this tree node's children.
     *
     * @return This tree node's children.
     */
    public List<ThreadSortNode> getChilds() {
        return childs;
    }

    /**
     * Filters from nodes those sub-trees which solely consist of nodes associated with given full name
     *
     * @param fullName The full name to filter with
     * @param threadList The nodes
     */
    public static void filterFullName(final String sentFullName, final List<ThreadSortNode> threadList) {
        for (final Iterator<ThreadSortNode> iterator = threadList.iterator(); iterator.hasNext();) {
            if (checkFullName(sentFullName, iterator.next())) {
                iterator.remove();
            }
        }
    }

    private static boolean checkFullName(final String fullName, final ThreadSortNode node) {
        if (!fullName.equals(node.msgInfo.getFullName())) {
            return false;
        }
        final List<ThreadSortNode> childs = node.getChilds();
        if (null != childs) {
            for (final ThreadSortNode child : childs) {
                if (!checkFullName(fullName, child)) {
                    return false;
                }
            }
        }
        // Solely consists of threadables associated with given full name
        return true;
    }

    /**
     * Applies specified full name to every node.
     *
     * @param fullName The full name to apply
     * @param threadList The thread list to apply to
     */
    public static void applyFullName(final String fullName, final List<ThreadSortNode> threadList) {
        if (null == threadList) {
            return;
        }
        for (final ThreadSortNode node : threadList) {
            node.msgInfo.setFullName(fullName);
            applyFullName(fullName, node.getChilds());
        }
    }

}

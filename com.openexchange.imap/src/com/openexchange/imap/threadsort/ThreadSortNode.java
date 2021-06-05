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
    public ThreadSortNode(MessageInfo msgInfo, long uid) {
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
    public void addChild(ThreadSortNode child) {
        childs.add(child);
    }

    /**
     * Adds children to this tree node.
     *
     * @param childThreads The children to add.
     */
    public void addChildren(List<ThreadSortNode> childThreads) {
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
    public static void filterFullName(String sentFullName, List<ThreadSortNode> threadList) {
        for (Iterator<ThreadSortNode> iterator = threadList.iterator(); iterator.hasNext();) {
            if (checkFullName(sentFullName, iterator.next())) {
                iterator.remove();
            }
        }
    }

    private static boolean checkFullName(String fullName, ThreadSortNode node) {
        if (!fullName.equals(node.msgInfo.getFullName())) {
            return false;
        }
        final List<ThreadSortNode> childs = node.getChilds();
        if (null != childs) {
            for (ThreadSortNode child : childs) {
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
    public static void applyFullName(String fullName, List<ThreadSortNode> threadList) {
        if (null == threadList) {
            return;
        }
        for (ThreadSortNode node : threadList) {
            node.msgInfo.setFullName(fullName);
            applyFullName(fullName, node.getChilds());
        }
    }

}

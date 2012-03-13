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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.util.List;

/**
 * {@link ThreadSortNode} - Represents a tree node in a thread-sort string.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ThreadSortNode {

    /**
     * The number of this tree node's message.
     */
    final int msgNum;

    private final List<ThreadSortNode> childs;

    /**
     * Initializes a new {@link ThreadSortNode}.
     *
     * @param msgNum The number of this tree node's message.
     */
    ThreadSortNode(final int msgNum) {
        this.msgNum = msgNum;
        childs = new ArrayList<ThreadSortNode>();
    }

    /**
     * Adds a child to this tree node.
     *
     * @param child The child to add.
     */
    void addChild(final ThreadSortNode child) {
        childs.add(child);
    }

    /**
     * Adds children to this tree node.
     *
     * @param childThreads The children to add.
     */
    void addChildren(final List<ThreadSortNode> childThreads) {
        childs.addAll(childThreads);
    }

    @Override
    public String toString() {
        return new StringBuilder().append(msgNum).append(' ').append(childs).toString();
    }

    /**
     * Gets the number of this tree node's message.
     *
     * @return The number of this tree node's message.
     */
    public int getMsgNum() {
        return msgNum;
    }

    /**
     * Gets this tree node's children.
     *
     * @return This tree node's children.
     */
    public List<ThreadSortNode> getChilds() {
        return childs;
    }
}

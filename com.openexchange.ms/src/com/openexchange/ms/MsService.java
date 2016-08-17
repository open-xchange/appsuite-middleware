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

package com.openexchange.ms;

import java.util.Set;
import com.openexchange.exception.OXException;

/**
 * {@link MsService} - The messaging service.
 * <p>
 * To avoid class loading problems, please use <a href="http://en.wikipedia.org/wiki/Plain_Old_Java_Object">POJO</a>s if possible.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MsService {

    /**
     * Gets the distributed queue with the specified name.
     *
     * @param name The name of the distributed queue
     * @return The distributed queue with the specified name
     */
    <E> Queue<E> getQueue(String name);

    /**
     * Returns the distributed topic with the specified name.
     *
     * @param name The name of the distributed topic
     * @return The distributed topic with the specified name
     */
    <E> Topic<E> getTopic(String name);

    /**
     * Gets the (local) message Inbox.
     *
     * @return The message Inbox
     */
    MessageInbox getMessageInbox();

    /**
     * Set of current members of the cluster. Returning set instance is not modifiable. Every member in the cluster has the same member list
     * in the same order. First member is the oldest member.
     *
     * @return The members
     */
    Set<Member> getMembers();

    /**
     * Transports a message to given member only.
     *
     * @param message The message
     * @param member The member to transfer to
     * @throws OXException If transport attempt fails
     */
    void directMessage(final Message<?> message, final Member member) throws OXException;

}

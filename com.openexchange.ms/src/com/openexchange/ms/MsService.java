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

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

/**
 * {@link Topic} - Represents a topic for the publish-subscribe messaging model.
 * <p>
 * A topic follows the Publish/Subscribe Messaging Domain:<br>
 * <ul>
 * <li>Each message may have multiple consumers.</li>
 * <li>Publishers and subscribers have a timing dependency. A client that subscribes to a topic can consume only messages published after
 * the client has created a subscription, and the subscriber must continue to be active in order for it to consume messages.</li>
 * </ul>
 * <img src="http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/images/Fig2.3.gif" alt="pub-sub">
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Topic<E> extends MessageDispatcher<E> {

    /**
     * Publishes specified message to this topic.
     *
     * @param message The message; prefer POJOs
     */
    void publish(E message);

    /**
     * Cancels this topic.
     */
    void cancel();

}

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

import java.util.concurrent.BlockingQueue;

/**
 * {@link Queue} - Represents a queue for the p2p messaging model.
 * <p>
 * A queue follows the Point-to-Point Messaging Domain:<br>
 * <ul>
 * <li>Each message has only one consumer.</li>
 * <li>A sender and a receiver of a message have no timing dependencies. The receiver can fetch the message whether or not it was running
 * when the client sent the message.</li>
 * <li>The receiver acknowledges the successful processing of a message.</li>
 * </ul>
 * <img src="http://docs.oracle.com/javaee/1.3/jms/tutorial/1_3_1-fcs/doc/images/Fig2.2.gif" alt="p2p">
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Queue<E> extends MessageDispatcher<E>, BlockingQueue<E> {

    // Nothing more to add
}

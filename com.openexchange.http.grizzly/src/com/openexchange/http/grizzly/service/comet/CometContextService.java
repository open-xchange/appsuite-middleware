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

package com.openexchange.http.grizzly.service.comet;

import org.glassfish.grizzly.comet.CometContext;
import org.glassfish.grizzly.comet.CometEngine;
import org.glassfish.grizzly.comet.NotificationHandler;

/**
 * {@link CometContextService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface CometContextService {

    /**
     * Registers a context path with the underlying comet engine using a default notification handler based on an comet-internal
     * thread pool.
     *
     * @param topic The topic used to register the context
     * @return CometContext A new configured comet context, or the existing one if it was registered previously.
     * @see CometEngine#register(String)
     */
    <E> CometContext<E> register(String topic);

    /**
     * Registers a context path with the underlying comet engine using a new instance of the supplied notification handler.
     *
     * @param topic The topic used to register the context
     * @param notificationClass The type of the desired notification handler
     * @return CometContext A new configured comet context, or the existing one if it was registered previously.
     * @see CometEngine#register(String, Class)
     */
    <E> CometContext<E> register(String topic, Class<? extends NotificationHandler> notificationClass);

    /**
     * Gets the comet context associated with the topic.
     *
     * @param topic The topic used to create the comet context
     * @see CometEngine#getCometContext(String)
     */
    <E> CometContext<E> getCometContext(String topic);

    /**
     * Unregisters and removes a previously registered comet context.
     *
     * @param topic The topic used to register the context
     * @return The removed comet context
     * @see CometEngine#deregister(String)
     */
    <E> CometContext<E> deregister(String topic);

}

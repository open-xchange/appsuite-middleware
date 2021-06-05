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

package com.openexchange.http.grizzly.service.comet.impl;

import org.glassfish.grizzly.comet.CometContext;
import org.glassfish.grizzly.comet.CometEngine;
import org.glassfish.grizzly.comet.NotificationHandler;
import com.openexchange.http.grizzly.service.comet.CometContextService;



/**
 * {@link CometContextServiceImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CometContextServiceImpl implements CometContextService {

    /**
     * Initializes a new {@link CometContextServiceImpl}.
     */
    public CometContextServiceImpl() {
        super();
    }

    @Override
    public <E> CometContext<E> register(String topic) {
        return CometEngine.getEngine().register(topic);
    }

    @Override
    public <E> CometContext<E> register(String topic, Class<? extends NotificationHandler> notificationClass) {
        return CometEngine.getEngine().register(topic, notificationClass);
    }

    @Override
    public <E> CometContext<E> getCometContext(String topic) {
        return CometEngine.getEngine().getCometContext(topic);
    }

    @Override
    public <E> CometContext<E> deregister(String topic) {
        return CometEngine.getEngine().deregister(topic);
    }

}

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

package com.openexchange.caching.internal.cache2jcs;

import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import com.openexchange.caching.ElementEvent;
import com.openexchange.caching.ElementEventHandler;
import com.openexchange.caching.internal.jcs2cache.JCSElementEventDelegator;

/**
 * {@link ElementEventHandler2JCS}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ElementEventHandler2JCS implements ElementEventHandler {

    private static final long serialVersionUID = -4007284871120270328L;

    private final IElementEventHandler handler;

    /**
     * Initializes a new {@link ElementEventHandler2JCS}
     */
    public ElementEventHandler2JCS(final IElementEventHandler handler) {
        super();
        this.handler = handler;
    }

    @Override
    public void handleElementEvent(final ElementEvent event) {
        handler.handleElementEvent(new JCSElementEventDelegator(event));
    }

    @Override
    public void onExceededIdletimeBackground(final ElementEvent event) {
        handler.handleElementEvent(new JCSElementEventDelegator(event));
    }

    @Override
    public void onExceededIdletimeOnRequest(final ElementEvent event) {
        handler.handleElementEvent(new JCSElementEventDelegator(event));
    }

    @Override
    public void onExceededMaxlifeBackground(final ElementEvent event) {
        handler.handleElementEvent(new JCSElementEventDelegator(event));
    }

    @Override
    public void onExceededMaxlifeOnRequest(final ElementEvent event) {
        handler.handleElementEvent(new JCSElementEventDelegator(event));
    }

    @Override
    public void onSpooledDiskAvailable(final ElementEvent event) {
        handler.handleElementEvent(new JCSElementEventDelegator(event));
    }

    @Override
    public void onSpooledDiskNotAvailable(final ElementEvent event) {
        handler.handleElementEvent(new JCSElementEventDelegator(event));
    }

    @Override
    public void onSpooledNotAllowed(final ElementEvent event) {
        handler.handleElementEvent(new JCSElementEventDelegator(event));
    }

}

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

package com.openexchange.caching.internal.jcs2cache;

import java.io.Serializable;
import org.apache.jcs.engine.control.event.ElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventConstants;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import com.openexchange.caching.ElementEventHandler;
import com.openexchange.caching.internal.cache2jcs.ElementEvent2JCS;

/**
 * {@link JCSElementEventHandlerDelegator} - A JSC element event handler that delegates events to an instance of {@link ElementEventHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JCSElementEventHandlerDelegator implements IElementEventHandler, Serializable {

    private static final long serialVersionUID = 4363921266600402439L;

    private final ElementEventHandler handler;

    /**
     * Initializes a new {@link JCSElementEventHandlerDelegator}
     */
    public JCSElementEventHandlerDelegator(final ElementEventHandler handler) {
        super();
        this.handler = handler;
    }

    @Override
    public void handleElementEvent(final IElementEvent elemEvent) {
        switch (elemEvent.getElementEvent()) {
        case IElementEventConstants.ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND:
            handler.onExceededIdletimeBackground(new ElementEvent2JCS((ElementEvent) elemEvent));
            break;
        case IElementEventConstants.ELEMENT_EVENT_EXCEEDED_IDLETIME_ONREQUEST:
            handler.onExceededIdletimeOnRequest(new ElementEvent2JCS((ElementEvent) elemEvent));
            break;
        case IElementEventConstants.ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND:
            handler.onExceededMaxlifeBackground(new ElementEvent2JCS((ElementEvent) elemEvent));
            break;
        case IElementEventConstants.ELEMENT_EVENT_EXCEEDED_MAXLIFE_ONREQUEST:
            handler.onExceededMaxlifeOnRequest(new ElementEvent2JCS((ElementEvent) elemEvent));
            break;
        case IElementEventConstants.ELEMENT_EVENT_SPOOLED_DISK_AVAILABLE:
            handler.onSpooledDiskAvailable(new ElementEvent2JCS((ElementEvent) elemEvent));
            break;
        case IElementEventConstants.ELEMENT_EVENT_SPOOLED_DISK_NOT_AVAILABLE:
            handler.onSpooledDiskNotAvailable(new ElementEvent2JCS((ElementEvent) elemEvent));
            break;
        case IElementEventConstants.ELEMENT_EVENT_SPOOLED_NOT_ALLOWED:
            handler.onSpooledNotAllowed(new ElementEvent2JCS((ElementEvent) elemEvent));
            break;
        default:
            handler.handleElementEvent(new ElementEvent2JCS((ElementEvent) elemEvent));
        }
    }

}

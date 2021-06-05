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

package com.openexchange.caching;

import java.io.Serializable;

/**
 * {@link ElementEventHandler} - Handles several events triggered by cache
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ElementEventHandler extends Serializable {

    /**
     * Handle events for this element. The events are typed.
     *
     * @param event The event created by the cache.
     */
    public void handleElementEvent(ElementEvent event);

    /**
     * The element exceeded its max life. This was detected in a background cleanup
     *
     * @param event - the element event containing event code and event's source object
     */
    public void onExceededIdletimeBackground(final ElementEvent event);

    /**
     * The element exceeded its max life. This was detected on request
     *
     * @param event - the element event containing event code and event's source object
     */
    public void onExceededIdletimeOnRequest(final ElementEvent event);

    /**
     * The element exceeded its max idle. This was detected in a background cleanup
     *
     * @param event - the element event containing event code and event's source object
     */
    public void onExceededMaxlifeBackground(final ElementEvent event);

    /**
     * The element exceeded its max idle time. This was detected on request
     *
     * @param event - the element event containing event code and event's source object
     */
    public void onExceededMaxlifeOnRequest(final ElementEvent event);

    /**
     * The element was pushed out of the memory store, there is a disk store available for the region, and the element is marked as
     * spoolable
     *
     * @param event - the element event containing event code and event's source object
     */
    public void onSpooledDiskAvailable(final ElementEvent event);

    /**
     * The element was pushed out of the memory store, and there is not a disk store available for the region
     *
     * @param event - the element event containing event code and event's source object
     */
    public void onSpooledDiskNotAvailable(ElementEvent event);

    /**
     * The element was pushed out of the memory store, there is a disk store available for the region, but the element is marked as not
     * spoolable
     *
     * @param event - the element event containing event code and event's source object
     */
    public void onSpooledNotAllowed(ElementEvent event);
}

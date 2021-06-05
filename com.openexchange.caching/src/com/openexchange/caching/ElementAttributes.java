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
import java.util.ArrayList;
import java.util.Queue;

/**
 * {@link ElementAttributes}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ElementAttributes extends Serializable {

    /**
     * Sets the maxLife attribute of the attributes object.
     *
     * @param mls The new MaxLifeSeconds value
     */
    public void setMaxLifeSeconds(long mls);

    /**
     * Sets the maxLife attribute of the attributes object. How many seconds it can live after creation.
     * <p>
     * If this is exceeded the element will not be returned, instead it will be removed. It will be removed on retrieval, or removed
     * actively if the memory shrinker is turned on.
     *
     * @return The MaxLifeSeconds value
     */
    public long getMaxLifeSeconds();

    /**
     * Sets the idleTime attribute of the attributes object. This is the maximum time the item can be idle in the cache, that is not
     * accessed.
     * <p>
     * If this is exceeded the element will not be returned, instead it will be removed. It will be removed on retrieval, or removed
     * actively if the memory shrinker is turned on.
     *
     * @param idle The new idleTime value
     */
    public void setIdleTime(long idle);

    /**
     * Size in bytes. This is not used except in the admin pages. It will be -1 by default.
     *
     * @param size The new size value
     */
    public void setSize(int size);

    /**
     * Gets the size attribute of the attributes object
     *
     * @return The size value
     */
    public int getSize();

    /**
     * Gets the createTime attribute of the attributes object.
     * <p>
     * This should be the current time in milliseconds returned by the system call when the element is put in the cache.
     * <p>
     * Putting an item in the cache overrides any existing items.
     *
     * @return The createTime value
     */
    public long getCreateTime();

    /**
     * Gets the LastAccess attribute of the attributes object.
     *
     * @return The LastAccess value.
     */
    public long getLastAccessTime();

    /**
     * Sets the LastAccessTime as now of the element attributes object
     */
    public void setLastAccessTimeNow();

    /**
     * Gets the idleTime attribute of the attributes object
     *
     * @return The idleTime value
     */
    public long getIdleTime();

    /**
     * Gets the time left to live of the attributes object.
     * <p>
     * This is the (max life + create time) - current time.
     *
     * @return The TimeToLiveSeconds value
     */
    public long getTimeToLiveSeconds();

    /**
     * Returns a copy of the object.
     *
     * @return element attributes
     */
    public ElementAttributes copy();

    /**
     * Can this item be spooled to disk
     * <p>
     * By default this is true.
     *
     * @return The spoolable value
     */
    public boolean getIsSpool();

    /**
     * Sets the isSpool attribute of the element attributes object
     * <p>
     * By default this is true.
     *
     * @param val The new isSpool value
     */
    public void setIsSpool(boolean val);

    /**
     * Is this item laterally distributable. Can it be sent to auxiliaries of type lateral.
     * <p>
     * By default this is true.
     *
     * @return The isLateral value
     */
    public boolean getIsLateral();

    /**
     * Sets the isLateral attribute of the element attributes object
     * <p>
     * By default this is true.
     *
     * @param val The new isLateral value
     */
    public void setIsLateral(boolean val);

    /**
     * Can this item be sent to the remote cache.
     * <p>
     * By default this is true.
     *
     * @return The isRemote value
     */
    public boolean getIsRemote();

    /**
     * Sets the isRemote attribute of the element attributes object.
     * <p>
     * By default this is true.
     *
     * @param val The new isRemote value
     */
    public void setIsRemote(boolean val);

    /**
     * This turns off expiration if it is true.
     *
     * @return The IsEternal value
     */
    public boolean getIsEternal();

    /**
     * Sets the isEternal attribute of the element attributes object
     *
     * @param val The new isEternal value
     */
    public void setIsEternal(boolean val);

    /**
     * Adds a element event handler. Handler's can be registered for multiple events. A registered handler will be called at every
     * recognized event.
     * <p>
     * <b>Note</b> that element event handlers are not transmitted to other caches via lateral or remote auxiliaries, nor are they spooled
     * to disk.
     *
     * @param eventHandler The feature to be added to the element event handler
     */
    public void addElementEventHandler(ElementEventHandler eventHandler);

    /**
     * Gets the element event handlers.
     * <p>
     * Event handlers are transient. The only events defined are in memory events. All handlers are lost if the item goes to disk or to any
     * lateral or remote auxiliary caches.
     *
     * @return The element event handlers value, null if there are none
     */
    public Queue<ElementEventHandler> getElementEventHandlers();

    /**
     * Sets the event handlers of the element attributes object
     *
     * @param eventHandlers value
     */
    public void addElementEventHandlers(ArrayList<ElementEventHandler> eventHandlers);

}

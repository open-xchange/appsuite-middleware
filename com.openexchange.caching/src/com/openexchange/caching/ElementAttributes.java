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

package com.openexchange.caching;

import java.io.Serializable;
import java.util.ArrayList;

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
    public ArrayList<ElementEventHandler> getElementEventHandlers();

    /**
     * Sets the event handlers of the element attributes object
     *
     * @param eventHandlers value
     */
    public void addElementEventHandlers(ArrayList<ElementEventHandler> eventHandlers);

}

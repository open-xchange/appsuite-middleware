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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.caching.hazelcast;

import java.io.Serializable;
import java.util.ArrayList;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEntry;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.caching.ElementEventHandler;

/**
 * {@link HazelcastElementAttributes}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HazelcastElementAttributes implements ElementAttributes {

    private static final long serialVersionUID = 7040302789404305032L;

    private final MapConfig mapConfig;

    private final MapEntry<Serializable, Serializable> mapEntry;

    private final IMap<Serializable, Serializable> map;

    /**
     * Initializes a new {@link HazelcastElementAttributes}.
     * 
     */
    public HazelcastElementAttributes(final MapEntry<Serializable, Serializable> mapEntry, final MapConfig mapConfig, final IMap<Serializable, Serializable> map) {
        super();
        this.mapEntry = mapEntry;
        this.mapConfig = mapConfig;
        this.map = map;
    }

    @Override
    public void setVersion(final long version) {
        // Huh?
    }

    @Override
    public void setMaxLifeSeconds(final long mls) {
        mapConfig.setTimeToLiveSeconds((int) mls);
    }

    @Override
    public long getMaxLifeSeconds() {
        return mapConfig.getTimeToLiveSeconds();
    }

    @Override
    public void setIdleTime(final long idle) {
        mapConfig.setMaxIdleSeconds((int) idle);
    }

    @Override
    public void setSize(final int size) {
        mapConfig.getMaxSizeConfig().setSize(size);
    }

    @Override
    public int getSize() {
        return mapConfig.getMaxSizeConfig().getSize();
    }

    @Override
    public long getCreateTime() {
        return null == mapEntry ? -1L : mapEntry.getCreationTime();
    }

    @Override
    public long getLastAccessTime() {
        return null == mapEntry ? -1L : mapEntry.getLastAccessTime();
    }

    @Override
    public void setLastAccessTimeNow() {
        if (null != mapEntry) {
            map.get(mapEntry.getKey());
        }
    }

    @Override
    public long getVersion() {
        return 0;
    }

    @Override
    public long getIdleTime() {
        return mapConfig.getMaxIdleSeconds();
    }

    @Override
    public long getTimeToLiveSeconds() {
        return mapConfig.getTimeToLiveSeconds();
    }

    @Override
    public ElementAttributes copy() {
        return this;
    }

    @Override
    public boolean getIsSpool() {
        return false;
    }

    @Override
    public void setIsSpool(final boolean val) {
        // Ignore
    }

    @Override
    public boolean getIsLateral() {
        return false;
    }

    @Override
    public void setIsLateral(final boolean val) {
        // Ignore
    }

    @Override
    public boolean getIsRemote() {
        return false;
    }

    @Override
    public void setIsRemote(final boolean val) {
        // Ignore
    }

    @Override
    public boolean getIsEternal() {
        return false;
    }

    @Override
    public void setIsEternal(final boolean val) {
        // Ignore
    }

    @Override
    public void addElementEventHandler(final ElementEventHandler eventHandler) {
        map.addEntryListener(new EntryListenerImpl(eventHandler), true);
    }

    @Override
    public ArrayList<ElementEventHandler> getElementEventHandlers() {
        throw new UnsupportedOperationException("HazelcastElementAttributes.getElementEventHandlers()");
    }

    @Override
    public void addElementEventHandlers(final ArrayList<ElementEventHandler> eventHandlers) {
        if (null == eventHandlers || eventHandlers.isEmpty()) {
            return;
        }
        for (final ElementEventHandler elementEventHandler : eventHandlers) {
            addElementEventHandler(elementEventHandler);
        }
    }

}

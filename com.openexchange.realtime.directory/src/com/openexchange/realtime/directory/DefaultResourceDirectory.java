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

package com.openexchange.realtime.directory;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.util.IDMap;

/**
 * {@link DefaultResourceDirectory}
 *
 * Abstract {@link ResourceDirectory} implementation handling listener notifications.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultResourceDirectory implements ResourceDirectory {

    private final Set<ChangeListener> listeners;
    private final Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();

    /**
     * Initializes a new {@link DefaultResourceDirectory}.
     */
    public DefaultResourceDirectory() {
        super();
        this.listeners = new CopyOnWriteArraySet<ChangeListener>();
    }

    @Override
    public void addListener(ChangeListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeListener(ChangeListener listener) throws OXException {
        this.listeners.remove(listener);
    }

    @Override
    public Resource set(ID id, Resource resource) throws OXException {
        Resource previousResource = doSet(id, resource, true);
        if (null == previousResource) {
            notifyAdded(id, resource);
        } else {
            notifyUpdated(id, resource, previousResource);
        }
        return previousResource;
    }
    
    @Override
    public Resource setIfAbsent(ID id, Resource resource) throws OXException {
        Resource previousResource = doSet(id, resource, false);
        if (null == previousResource) {
            notifyAdded(id, resource);
        }
        return previousResource;
    }

    @Override
    public IDMap<Resource> remove(ID id) throws OXException {
        return notifyRemoved(doRemove(id));
    }
    

    @Override
    public IDMap<Resource> remove(Collection<ID> ids) throws OXException {
        return notifyRemoved(doRemove(ids));
    }

    protected void notifyAdded(ID id, Resource addedResource) {
        for (ChangeListener listener : listeners) {
            listener.added(id, addedResource);
        }
    }

    private void notifyUpdated(ID id, Resource updatedResource, Resource previousResource) {
        for (ChangeListener listener : listeners) {
            listener.updated(id, updatedResource, previousResource);
        }
    }

    /**
     * Notify registered ChangeListeners about the removal of Resources from the ResourceDirectory.
     * 
     * @param removedResources The Resources that were removed
     * @return the IDMap with removed Resources
     */
    private IDMap<Resource> notifyRemoved(IDMap<Resource> removedResources) {
        for (ChangeListener listener : listeners) {
            for (Entry<ID, Resource> entry : removedResources.entrySet()) {
                listener.removed(entry.getKey(), entry.getValue());
            }
        }
        return removedResources;
    }
    
    protected boolean conjure(ID id) throws OXException {
        String protocol = id.getProtocol();
        if (protocol == null) {
            return false;
        }
        Channel channel = channels.get(protocol);
        if (channel == null) {
            return false;
        }
        
        return channel.conjure(id);
    }
    
    public void addChannel(Channel channel) {
        channels.put(channel.getProtocol(), channel);
    }
    
    public void removeChannel(Channel channel) {
        channels.remove(channel.getProtocol());
    }

    protected abstract IDMap<Resource> doRemove(Collection<ID> ids) throws OXException;

    protected abstract IDMap<Resource> doRemove(ID id) throws OXException;

    protected abstract Resource doSet(ID id, Resource data, boolean overwrite) throws OXException;

}

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

package com.openexchange.realtime.hazelcast.impl;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.Channel;
import com.openexchange.realtime.directory.DefaultResource;
import com.openexchange.realtime.directory.Resource;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.ElementPath;


/**
 * {@link ResponseChannel}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ResponseChannel implements Channel{
    
    private ConcurrentHashMap<ID, Stanza> responses = new ConcurrentHashMap<ID, Stanza>();
    private ConcurrentHashMap<ID, Condition> condition = new ConcurrentHashMap<ID, Condition>();
    private ConcurrentHashMap<ID, Lock> locks = new ConcurrentHashMap<ID, Lock>();
    
    private ResourceDirectory directory;
    
    public ResponseChannel(ResourceDirectory directory) {
        this.directory = directory;
    }
    
    public void setUp(String uuid, Stanza stanza) throws OXException {
        ID id = getId(uuid);
        ReentrantLock lock = new ReentrantLock();
        locks.put(id, lock);
        condition.put(id, lock.newCondition());
        Resource res = new DefaultResource();
        directory.set(id, res);
        stanza.setFrom(id);
    }
    
    public Stanza waitFor(String uuid, long timeout, TimeUnit unit) throws OXException {
        ID id = getId(uuid);
        try {
            locks.get(id).lock();
            Stanza stanza = responses.get(id);
            if (stanza != null) {
                return stanza;
            }
            condition.get(id).await(timeout, unit);
            stanza = responses.get(id);
            if (stanza == null) {
                throw new OXException();
            }
            
            return stanza;
        } catch (InterruptedException e) {
            throw new OXException();
        } finally {
            directory.remove(id);
            condition.remove(id);
            responses.remove(id);        
            locks.remove(id).unlock();
        }
        
    }



    private ID getId(String uuid) {
        ID id = new ID(getProtocol(), uuid, "internal", "");
        return id;
    }

    @Override
    public String getProtocol() {
        return "call";
    }

    @Override
    public boolean canHandle(Set<ElementPath> elementPaths, ID recipient) throws OXException {
        return true;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isConnected(ID id) throws OXException {
        return condition.containsKey(id);
    }

    @Override
    public boolean conjure(ID id) throws OXException {
        return id.getProtocol().equals(getProtocol());
    }

    @Override
    public void send(Stanza stanza, ID recipient) throws OXException {
        stanza.trace("Delivering synchronously. ResponseChannel.");
        Lock lock = locks.get(recipient);
        try {
            lock.lock();
            responses.put(recipient, stanza);
            condition.get(recipient).signal();
        } finally {
            lock.unlock();
        }
    }

}

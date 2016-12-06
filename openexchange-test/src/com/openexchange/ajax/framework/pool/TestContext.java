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

package com.openexchange.ajax.framework.pool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link TestContext}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.3
 */
public class TestContext implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8836508664321761890L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TestContext.class);

    private final String name;

    private final int id;

    private volatile List<TestUser> acquiredUsers = Collections.synchronizedList(new ArrayList<TestUser>()); //required for reset

    private volatile BlockingQueue<TestUser> users = new LinkedBlockingQueue<>();

    private volatile List<String> acquiredGroupParticipants = Collections.synchronizedList(new ArrayList<String>()); //required for reset

    private volatile BlockingQueue<String> groupParticipants = new LinkedBlockingQueue<>();

    // the admin is not handled to be acquired only by one party
    private AtomicReference<TestUser> admin = new AtomicReference<>();

    public TestContext(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public void setAdmin(TestUser lAdmin) {
        admin.compareAndSet(null, lAdmin);
    }

    public TestUser getAdmin() {
        return admin.get();
    }

    public void addGroupParticipant(String groupParticipant) {
        groupParticipants.add(groupParticipant);
    }

    public String acquireGroupParticipant() {
        try {
            String participant = groupParticipants.take();
            acquiredGroupParticipants.add(participant);
            return participant;
        } catch (InterruptedException e) {
            LOG.error("", e);
        }
        return null;
    }

    public void backGroupParticipant(String groupParticipant) {
        try {
            groupParticipants.remove(groupParticipant);
            groupParticipants.put(groupParticipant);
        } catch (InterruptedException e) {
            LOG.error("", e);
        }
    }

    public void addUser(TestUser user) {
        users.add(user);
    }

    public TestUser acquireUser() {
        try {
            TestUser user = users.take();
            acquiredUsers.add(user);
            return user;
        } catch (InterruptedException e) {
            LOG.error("", e);
        }
        return null;
    }

    public void backUser(TestUser user) {
        try {
            acquiredUsers.remove(user);
            users.put(user);
        } catch (InterruptedException e) {
            LOG.error("", e);
        }
    }

    /**
     * Resets the context and adds all acquired users back to the pool
     */
    protected void reset() {
        if (!acquiredUsers.isEmpty()) {
            users.addAll(acquiredUsers);
            acquiredUsers.clear();
        }
        if (!acquiredGroupParticipants.isEmpty()) {
            groupParticipants.addAll(acquiredGroupParticipants);
            acquiredGroupParticipants.clear();
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}

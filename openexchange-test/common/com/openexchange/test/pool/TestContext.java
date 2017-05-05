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

package com.openexchange.test.pool;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.java.ConcurrentHashSet;
import com.openexchange.java.Strings;
import edu.emory.mathcs.backport.java.util.Arrays;

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

    private String acquiredBy;

    private volatile ConcurrentHashSet<TestUser> acquiredUsers = new ConcurrentHashSet<TestUser>(); //required for reset

    private volatile BlockingQueue<TestUser> users = new LinkedBlockingQueue<>();

    private volatile ConcurrentHashSet<String> acquiredGroupParticipants = new ConcurrentHashSet<String>(); //required for reset

    private volatile List<String> groupParticipants = new ArrayList<String>();
    private volatile List<String> userParticipants = new ArrayList<String>();
    private volatile List<String> resourceParticipants = new ArrayList<String>();

    // the admin is not handled to be acquired only by one party
    private AtomicReference<TestUser> contextAdmin = new AtomicReference<>();

    private AtomicReference<TestUser> noReplyUser = new AtomicReference<>();

    public TestContext(String name) {
        this.name = name;
    }

    public void setAdmin(TestUser lAdmin) {
        contextAdmin.compareAndSet(null, lAdmin);
    }

    public TestUser getAdmin() {
        return contextAdmin.get();
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
        setAcquiredBy(null);

        if (!acquiredUsers.isEmpty()) {
            users.addAll(acquiredUsers);
            acquiredUsers.clear();
        }
        if (!acquiredGroupParticipants.isEmpty()) {
            groupParticipants.addAll(acquiredGroupParticipants);
            acquiredGroupParticipants.clear();
        }
    }

    public String getName() {
        return name;
    }

    public String getAcquiredBy() {
        return acquiredBy;
    }

    public void setAcquiredBy(String acquiredBy) {
        this.acquiredBy = acquiredBy;
    }

    public List<String> getUserParticipants() {
        return userParticipants;
    }

    public void addUserParticipants(String... userParticipants) {
        this.userParticipants.addAll(Arrays.asList(userParticipants));
    }

    public List<String> getResourceParticipants() {
        return resourceParticipants;
    }

    public void addResourceParticipants(String... resourceParticipants) {
        this.resourceParticipants.addAll(Arrays.asList(resourceParticipants));
    }

    public void addGroupParticipant(String... groupParticipants) {
        this.groupParticipants.addAll(Arrays.asList(groupParticipants));
    }

    public List<String> getGroupParticipants() {
        return this.groupParticipants;
    }

    public List<TestUser> getCopyOfAll() {
        return new ArrayList<>(users);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TestContext other = (TestContext) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equalsIgnoreCase(other.name)) {
            return false;
        }
        return true;
    }

    public TestUser getNoReplyUser() {
        return noReplyUser.get();
    }

    public void setNoReplyUser(TestUser noReplyUser) {
        this.noReplyUser.set(noReplyUser);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        builder.append("TestContext [");
        if (Strings.isNotEmpty(name)) {
            builder.append("name=").append(name).append(", ");
        }
        if (Strings.isNotEmpty(acquiredBy)) {
            builder.append("acquiredBy=").append(acquiredBy).append(", ");
        }
        builder.append("]");
        return builder.toString();
    }

}

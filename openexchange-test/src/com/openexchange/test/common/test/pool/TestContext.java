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

package com.openexchange.test.common.test.pool;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import javax.mail.internet.AddressException;
import org.junit.Assert;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.ajax.framework.CleanableResourceManager;
import com.openexchange.ajax.framework.ConfigurableResource;
import com.openexchange.java.Strings;
import com.openexchange.test.common.test.TestClassConfig;
import com.openexchange.testing.httpclient.invoker.ApiException;

/**
 * {@link TestContext}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.3
 */
public class TestContext implements Serializable, CleanableResourceManager, ConfigurableResource {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -8836508664321761890L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TestContext.class);

    private final String name;
    private final int contextId;

    private final AtomicReference<TestUser> contextAdmin = new AtomicReference<>();
    private final AtomicReference<TestUser> noReplyUser = new AtomicReference<>();

    /** Overall test users by this context */
    private final List<TestUser> users = new LinkedList<>();

    /** Pre-provisioned test users */
    private final LinkedList<TestUser> userPool = new LinkedList<>();

    /**
     * Initializes a new {@link TestContext}.
     *
     * @param contextId The context identifier
     * @param name The name
     * @param admin The admin of the context
     * @param testConfig The test configuration
     */
    public TestContext(int contextId, String name, TestUser admin) {
        this.name = name;
        this.contextId = contextId;
        this.contextAdmin.set(admin);
    }

    /**
     * Get the context admin
     *
     * @return The admin as {@link TestUser}
     */
    public TestUser getAdmin() {
        return contextAdmin.get();
    }

    /**
     * Get the context name
     *
     * @return The context name
     */
    public String getName() {
        return name;
    }

    @Override
    public void configure(TestClassConfig testConfig) throws Exception {
        for (int i = 0; i < testConfig.getNumberOfusersPerContext(); i++) {
            TestUser createUser = createUser();
            if (null != createUser) {
                createUser.configure(testConfig);
                userPool.add(createUser);
                users.add(createUser);
            }
        }
    }

    /**
     * Acquire a unique user from this context
     *
     * @return A unique user
     */
    public TestUser acquireUser() {
        // Get pre-provisioned user
        TestUser user = userPool.poll();
        if (null != user) {
            return user;
        }
        // Create new user
        TestUser createUser = createUser();
        users.add(createUser);
        return createUser;
    }

    private TestUser createUser() {
        try {
            TestUser createdUser = ProvisioningService.getInstance().createUser(contextId, getUserNameFromPool());
            return createdUser;
        } catch (AddressException | RemoteException | StorageException | InvalidCredentialsException | NoSuchContextException | InvalidDataException | DatabaseUpdateException | MalformedURLException | NotBoundException e) {
            LOG.error("Unable to pre provision test user", e);
        }
        return null;
    }

    /**
     *
     * Gets the next unused user name from the user name pool.
     *
     * @return The user name. Returns null, if the user name pool can not be read of if there are more users than names in the pool.
     */
    private String getUserNameFromPool() {
        int usedUserSize = users.size();
        String[] pool = ProvisioningService.userNamesPool;
        if (pool == null) {
            return null;
        }
        int poolSize = pool.length;
        if (usedUserSize < poolSize) {
            try {
                return pool[users.size()];
            } catch (@SuppressWarnings("unused") Exception e) {
                return null;
            }
        }
        return null;
    }

    private final Random rand = new Random(System.currentTimeMillis());

    /**
     * Gets a user from this context. Can be a user that has already
     * been acquired by {@link #acquireUser()}
     *
     * @return A user
     */
    public TestUser getRandomUser() {
        if (userPool.isEmpty()) {
            // Create a new one
            return acquireUser();
        }
        if (1 == userPool.size()) {
            TestUser result = userPool.poll();
            if (result != null) {
                return result;
            }
        }

        int next = rand.nextInt(userPool.size());
        return userPool.remove(next);
    }

    /**
     * Acquire a resource from this context
     *
     * @return The resource identifier
     */
    public Integer acquireResource() {
        try {
            return ProvisioningService.getInstance().createResource(contextId);
        } catch (RemoteException | StorageException | InvalidCredentialsException | NoSuchContextException | InvalidDataException | DatabaseUpdateException | MalformedURLException | NotBoundException e) {
            LOG.error("Unable to acquire resource", e);
            Assert.fail();
        }
        return null;
    }

    /**
     * Acquire a group from this context
     *
     * @param optUsers The optional list of group members
     * @return The group identifier
     */
    public Integer acquireGroup(Optional<List<Integer>> optUsers) {
        try {
            return ProvisioningService.getInstance().createGroup(contextId, optUsers);
        } catch (RemoteException | StorageException | InvalidCredentialsException | NoSuchContextException | InvalidDataException | DatabaseUpdateException | MalformedURLException | NotBoundException | NoSuchUserException e) {
            LOG.error("Unable to acquire group", e);
            Assert.fail();
        }
        return null;
    }

    /**
     * Gets the context id.
     *
     * @return The context id.
     */
    public int getId() {
        return contextId;
    }

    /**
     * Acquire the NoReply user from this context
     *
     * @return The no reply user as {@link TestUser}
     */
    public TestUser acquireNoReplyUser() {
        if (noReplyUser.get() == null) {
            synchronized (this) {
                if (noReplyUser.get() == null) {
                    noReplyUser.set(acquireUser());
                    try {
                        ProvisioningService.getInstance().changeContexConfig(this.contextId, Collections.singletonMap("com.openexchange.noreply.address", noReplyUser.get().getLogin()));
                    } catch (RemoteException | InvalidCredentialsException | NoSuchContextException | StorageException | InvalidDataException | MalformedURLException | NotBoundException e) {
                        LOG.error("Unable to change config for no reply address", e);
                        Assert.fail();
                    }
                }
            }
        }
        return noReplyUser.get();
    }

    @Override
    public void cleanUp() throws ApiException {
        Throwable t = null;
        for (TestUser testUser : users) {
            try {
                testUser.cleanUp();
            } catch (Throwable e) {
                t = e;
                LOG.info("Unable to clean up context", e);
            }
        }
        if (null != t) {
            throw new ApiException(t);
        }
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(64);
        builder.append("TestContext [");
        if (Strings.isNotEmpty(name)) {
            builder.append("name=").append(name).append(", ");
        }
        builder.append("contextId=").append(contextId);
        builder.append("]");
        return builder.toString();
    }

}

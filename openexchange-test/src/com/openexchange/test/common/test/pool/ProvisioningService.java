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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.test.common.test.pool;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import javax.mail.internet.AddressException;
import org.junit.Assert;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXGroupInterface;
import com.openexchange.admin.rmi.OXResourceInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Group;
import com.openexchange.admin.rmi.dataobjects.Resource;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.exceptions.ContextExistsException;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.test.common.configuration.AJAXConfig;

/**
 * {@link ProvisioningService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.5
 */
public class ProvisioningService {

    private static ProvisioningService INSTANCE;

    /**
     * Gets the {@link ProvisioningService}
     *
     * @return The {@link ProvisioningService}
     * @throws MalformedURLException In case provisioning can't be initialized
     * @throws RemoteException In case provisioning can't be initialized
     * @throws NotBoundException In case provisioning can't be initialized
     */
    public static ProvisioningService getInstance() throws MalformedURLException, RemoteException, NotBoundException {
        if (INSTANCE == null) {
            synchronized (ProvisioningService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ProvisioningService(getRMIHostUrl(), TestContextPool.getOxAdminMaster().getUser(), TestContextPool.getOxAdminMaster().getPassword());
                }
            }
        }
        return INSTANCE;
    }

    private static final String CONTEXT_NAME_SUFFIX = AJAXConfig.getProperty(AJAXConfig.Property.CONTEXT_NAME_SUFFIX, "ox.test");
    private static final String CTX_SECRET = AJAXConfig.getProperty(AJAXConfig.Property.CONTEXT_ADMIN_PASSWORD, "secret");
    private static final String CTX_ADMIN = AJAXConfig.getProperty(AJAXConfig.Property.CONTEXT_ADMIN_USER, "admin");
    private static final Credentials CONTEXT_CREDS = new Credentials(CTX_ADMIN, CTX_SECRET);
    private static final Long DEFAULT_MAX_QUOTA = Long.valueOf(500);
    private static final String MAIL_NAME_FORMAT = "%s@context%s." + CONTEXT_NAME_SUFFIX;
    private static final String CONTEXT_NAME_FORMAT = "context%s." + CONTEXT_NAME_SUFFIX;
    private static final String USER_NAMES = AJAXConfig.getProperty(AJAXConfig.Property.USER_NAMES, "Anton,Berta,Caesar,Dora,Emil");

    private final OXContextInterface oxContext;
    private final OXUserInterface oxUser;
    private final OXGroupInterface oxGroup;
    private final OXResourceInterface oxResource;
    private final Credentials masterCreds;

    static final String[] userNamesPool = USER_NAMES.split(",");
    private static final boolean USE_RANDOM_CTX_RANGE = Boolean.valueOf(AJAXConfig.getProperty(AJAXConfig.Property.USE_RANDOM_CID_RANGE, "false")).booleanValue();
    private static final String USER_SECRET = AJAXConfig.getProperty(AJAXConfig.Property.USER_PASSWORD, "secret");
    /*
     * Start with a random 5000er slot above 100
     *
     * This should mitigate caching issues caused by deletion and recreation of the same ctx
     */
    private final AtomicInteger cidCounter = new AtomicInteger(USE_RANDOM_CTX_RANGE ? (new Random(System.currentTimeMillis()).nextInt(100000) + 1 * 5000) + 100 : 100);

    /**
     * Initializes a new {@link ProvisioningService}.
     *
     * @throws NotBoundException
     * @throws RemoteException
     * @throws MalformedURLException
     */
    private ProvisioningService(String rmiEndPointURL, String adminUser, String adminPW) throws MalformedURLException, RemoteException, NotBoundException {
        super();
        oxContext = OXContextInterface.class.cast(Naming.lookup(rmiEndPointURL + OXContextInterface.RMI_NAME));
        oxUser = OXUserInterface.class.cast(Naming.lookup(rmiEndPointURL + OXUserInterface.RMI_NAME));
        oxGroup = OXGroupInterface.class.cast(Naming.lookup(rmiEndPointURL + OXGroupInterface.RMI_NAME));
        oxResource = OXResourceInterface.class.cast(Naming.lookup(rmiEndPointURL + OXResourceInterface.RMI_NAME));
        masterCreds = new Credentials(adminUser, adminPW);
    }

    /**
     * Creates a context
     *
     * @return The context as {@link TestContext}
     * @throws RemoteException If context can't be created
     * @throws StorageException If context can't be created
     * @throws InvalidCredentialsException If context can't be created
     * @throws InvalidDataException If context can't be created
     * @throws ContextExistsException If context can't be created
     * @throws AddressException If context can't be created
     */
    public TestContext createContext() throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, AddressException {
        return createContext(Optional.empty());
    }

    /**
     * Creates a context
     *
     * @param optConfig The optional configuration to pass for context creation
     * @return The context as {@link TestContext}
     * @throws RemoteException If context can't be created
     * @throws StorageException If context can't be created
     * @throws InvalidCredentialsException If context can't be created
     * @throws InvalidDataException If context can't be created
     * @throws ContextExistsException If context can't be created
     * @throws AddressException If context can't be created
     */
    public TestContext createContext(Optional<Map<String, String>> optConfig) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, ContextExistsException, AddressException {
        int cid = cidCounter.getAndIncrement();
        User admin_user = createUser(CTX_ADMIN, CTX_SECRET, CTX_ADMIN, CTX_ADMIN, CTX_ADMIN, getMailAddress(CTX_ADMIN, cid), Optional.empty());
        UserModuleAccess userModuleAccess = new UserModuleAccess();
        userModuleAccess.enableAll();
        try {
            try {
                Context result = oxContext.create(createContext(cid, DEFAULT_MAX_QUOTA, optConfig), admin_user, userModuleAccess, masterCreds);
                if (result.getId().intValue() != cid) {
                    //Creating and setting the context-name not during context-creation but afterwards,
                    //because the server might have ignored the given cid (if "autocontextid" is active)
                    //and we still want that the auto-cid is part of the context-name
                    result.setName(getContextName(i(result.getId())));
                    oxContext.change(result, masterCreds);
                }
                return toTestContext(result.getId().intValue(), userToTestUser(result.getName(), admin_user, I(2), result.getId()));
            } catch (@SuppressWarnings("unused") ContextExistsException e) {
                // retry once
                cid = cidCounter.getAndIncrement();
                admin_user = createUser(CTX_ADMIN, CTX_SECRET, CTX_ADMIN, CTX_ADMIN, CTX_ADMIN, getMailAddress(CTX_ADMIN, cid), Optional.empty());
                Context result = oxContext.create(createContext(cid, DEFAULT_MAX_QUOTA, optConfig), admin_user, userModuleAccess, masterCreds);
                if (result.getId().intValue() != cid) {
                    //Creating and setting the context-name not during context-creation but afterwards,
                    //because the server might have ignored the given cid (if "autocontextid" is active)
                    //and we still want that the auto-cid is part of the context-name
                    result.setName(getContextName(i(result.getId())));
                    oxContext.change(result, masterCreds);
                }
                return toTestContext(result.getId().intValue(), userToTestUser(result.getName(), admin_user, I(2), result.getId()));
            }
        } catch (NoSuchContextException e) {
            // should never happen
            e.printStackTrace();
            Assert.fail();
            return null;
        }
    }

    /**
     * Changes a context
     *
     * @param cid The context identifier
     * @param configs The configuration to pass for context
     * @throws RemoteException If context can't be changed
     * @throws StorageException If context can't be changed
     * @throws InvalidCredentialsException If context can't be changed
     * @throws NoSuchContextException If context can't be found
     * @throws InvalidDataException If context can't be changed
     * @throws ContextExistsException If context can't be changed
     * @throws AddressException If context can't be changed
     */
    public void changeContexConfig(int cid, Map<String, String> configs) throws RemoteException, InvalidCredentialsException, NoSuchContextException, StorageException, InvalidDataException {
        Context ctx = new Context(I(cid));
        ctx.setUserAttributes(Collections.singletonMap("config", configs));
        oxContext.change(ctx, masterCreds);
    }

    /**
     * Delete a context
     *
     * @param cid The context identifier
     * @throws RemoteException If context can't be deleted
     * @throws StorageException If context can't be deleted
     * @throws InvalidCredentialsException If context can't be deleted
     * @throws InvalidDataException If context can't be deleted
     * @throws NoSuchContextException If context can't be deleted
     * @throws DatabaseUpdateException If context can't be deleted
     */
    public void deleteContext(int cid) throws RemoteException, StorageException, InvalidCredentialsException, InvalidDataException, NoSuchContextException, DatabaseUpdateException {
        oxContext.delete(contextForId(cid), masterCreds);
    }

    /**
     * Creates a user in the given context
     *
     * @param cid The context identifier of the context the user shall be created in
     * @return The user as {@link TestUser}
     * @throws RemoteException If user can't be created
     * @throws StorageException If user can't be created
     * @throws InvalidCredentialsException If user can't be created
     * @throws NoSuchContextException If user can't be created
     * @throws InvalidDataException If user can't be created
     * @throws DatabaseUpdateException If user can't be created
     * @throws AddressException If user can't be created
     */
    public TestUser createUser(int cid) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, AddressException {
        return createUser(cid, null);
    }

    /**
     * Creates a user in the given context
     *
     * @param cid The context identifier of the context the user shall be created in
     * @param userLogin The login name of the user.
     * @return The user as {@link TestUser}
     * @throws RemoteException If user can't be created
     * @throws StorageException If user can't be created
     * @throws InvalidCredentialsException If user can't be created
     * @throws NoSuchContextException If user can't be created
     * @throws InvalidDataException If user can't be created
     * @throws DatabaseUpdateException If user can't be created
     * @throws AddressException If user can't be created
     */
    public TestUser createUser(int cid, String userLogin) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, AddressException {
        User userToCreate = createUser(cid, Optional.empty(), userLogin);
        Context context = contextForId(cid);
        User created = oxUser.create(context, userToCreate, CONTEXT_CREDS);
        return userToTestUser(context.getName(), userToCreate, created.getId(), I(cid));
    }

    /**
     * Creates a group in the given context
     *
     * @param cid The context identifier of the context the user shall be created in
     * @param optUserIds The users to add to the group
     * @return The group identifier
     * @throws RemoteException If group can't be created
     * @throws StorageException If group can't be created
     * @throws InvalidCredentialsException If group can't be created
     * @throws NoSuchContextException If group can't be created
     * @throws InvalidDataException If group can't be created
     * @throws DatabaseUpdateException If group can't be created
     * @throws NoSuchUserException If a user doesn't exist
     * @throws AddressException If group can't be created
     */
    public Integer createGroup(int cid, Optional<List<Integer>> optUserIds) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        Group result = oxGroup.create(contextForId(cid), createGroup(optUserIds), CONTEXT_CREDS);
        return result.getId();
    }

    /**
     * Creates a resource in the given context
     *
     * @param cid The context identifier of the context the user shall be created in
     * @return The resource identifier
     * @throws RemoteException If group can't be created
     * @throws StorageException If group can't be created
     * @throws InvalidCredentialsException If group can't be created
     * @throws NoSuchContextException If group can't be created
     * @throws InvalidDataException If group can't be created
     * @throws DatabaseUpdateException If group can't be created
     * @throws NoSuchUserException If a user doesn't exist
     * @throws AddressException If group can't be created
     */
    public Integer createResource(int cid) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        Resource result = oxResource.create(contextForId(cid), createResourceObject(cid), CONTEXT_CREDS);
        return result.getId();
    }

    private TestContext toTestContext(int cid, TestUser admin) {
        return new TestContext(cid, getContextName(cid), admin);
    }

    /**
     * Creates a {@link TestUser} from the given informations
     *
     * @param cid The context id
     * @param user The created user
     * @return
     */
    private TestUser userToTestUser(String contextName, User user, Integer userId, Integer ctxId) {
        return new TestUser(user.getName(), contextName, user.getPassword(), userId, ctxId);
    }

    // -------------  factory methods --------------------

    private static Group createGroup(Optional<List<Integer>> optUserIds) {
        Group group = new Group();
        String rand = UUID.randomUUID().toString();
        String name = "name_" + rand;
        group.setDisplayname(name);
        group.setName(name);
        optUserIds.ifPresent(ids -> group.setMembers(ids.toArray(new Integer[ids.size()])));
        return group;
    }

    private static Resource createResourceObject(int cid) {
        Resource resource = new Resource();
        String rand = UUID.randomUUID().toString();
        String name = "name_" + rand;
        resource.setDisplayname(name);
        resource.setName(name);
        resource.setEmail(getMailAddress(name, cid));
        return resource;
    }

    private Context contextForId(int cid) {
        Context context = new Context(I(cid));
        context.setName(getContextName(cid));
        return context;
    }

    /**
     * Creates a new {@link Context} object with the specified id
     * and max quota
     *
     * @param contextId The context identifier
     * @param maxQuota The maximum quota of the context
     * @param optConfig The optional ctx config
     * @return The new {@link Context} object
     */
    public static Context createContext(int contextId, Long maxQuota, Optional<Map<String, String>> optConfig) {
        Context context = new Context(I(contextId));
        context.setName(getContextName(contextId));
        context.setMaxQuota(maxQuota);
        optConfig.ifPresent((c) -> context.setUserAttributes(Collections.singletonMap("config", c)));
        return context;
    }

    /**
     * Creates a new random {@link User} with only the mandatory fields
     *
     * @param cid The context id
     * @return The {@link User} object
     * @throws AddressException
     */
    public static User createRandomUser(int cid) throws AddressException {
        return createUser(cid, Optional.empty(), null);
    }

    /**
     * Creates a new random {@link User} with only the mandatory fields and a optional config
     *
     * @param cid The context id
     * @param config The optional config
     * @return The {@link User} object
     * @throws AddressException
     */
    public static User createRandomUser(int cid, Optional<Map<String, String>> config) throws AddressException {
        return createUser(cid, config, null);
    }

    /**
     * Creates a new random {@link User} with only the mandatory fields and a optional config
     *
     * @param cid The context id
     * @param config The optional config
     * @param userLogin The login name of the user
     * @return The {@link User} object
     * @throws AddressException
     */
    public static User createUser(int cid, Optional<Map<String, String>> config, String userLogin) throws AddressException {
        String login = userLogin != null ? userLogin : "login_" + UUID.randomUUID();
        String pw = USER_SECRET;
        User user = createUser(login, pw, login, login, login, getMailAddress(login, cid), config);
        return user;
    }

    private static String getContextName(int cid) {
        return String.format(CONTEXT_NAME_FORMAT, String.valueOf(cid));
    }

    public static String getMailAddress(String login, int cid) {
        return String.format(MAIL_NAME_FORMAT, login, String.valueOf(cid));
    }

    /**
     * Creates a {@link User} object from the given data
     *
     * @param name
     * @param passwd
     * @param displayName
     * @param givenName
     * @param surname
     * @param email
     * @param config
     * @return The {@link User} object
     * @throws AddressException
     */
    @SuppressWarnings("unused")
    public static User createUser(String name, String passwd, String displayName, String givenName, String surname, String email, Optional<Map<String, String>> config) throws AddressException {
        Objects.requireNonNull(name);
        Objects.requireNonNull(passwd);
        Objects.requireNonNull(displayName);
        Objects.requireNonNull(givenName);
        Objects.requireNonNull(surname);
        Objects.requireNonNull(email);
        // Check for valid address
        new QuotedInternetAddress(email);

        User user = new User();
        user.setName(name);
        user.setPassword(passwd);
        user.setDisplay_name(displayName);
        user.setGiven_name(givenName);
        user.setSur_name(surname);
        user.setPrimaryEmail(email);
        user.setEmail1(email);
        user.setImapLogin(email);

        if (config.isPresent()) {
            Map<String, Map<String, String>> nsConfig = new HashMap<>(1);
            nsConfig.put("config", config.get());
            user.setUserAttributes(nsConfig);
        }
        return user;
    }

    private static String getRMIHostUrl() {
        String host = getRMIHost();

        if (!host.startsWith("rmi://")) {
            host = "rmi://" + host;
        }
        if (!host.endsWith("/")) {
            host += "/";
        }
        return host;
    }

    private static String getRMIHost() {
        String host = "localhost";

        if (System.getProperty("rmi_test_host") != null) {
            host = System.getProperty("rmi_test_host");
        } else if (AJAXConfig.getProperty(AJAXConfig.Property.RMIHOST) != null) {
            host = AJAXConfig.getProperty(AJAXConfig.Property.RMIHOST);
        }

        return host;
    }

}

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

package com.openexchange.admin.rmi.impl;

import static com.openexchange.java.Autoboxing.i;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.internet.idn.IDNA;
import org.osgi.framework.BundleContext;
import com.damienmiller.BCrypt;
import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.plugins.OXUserPluginInterface;
import com.openexchange.admin.plugins.OXUserPluginInterfaceExtended;
import com.openexchange.admin.plugins.PluginException;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.OXContextInterface;
import com.openexchange.admin.rmi.OXUserInterface;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.dataobjects.UserModuleAccess;
import com.openexchange.admin.rmi.dataobjects.UserProperty;
import com.openexchange.admin.rmi.exceptions.DatabaseUpdateException;
import com.openexchange.admin.rmi.exceptions.EnforceableDataObjectException;
import com.openexchange.admin.rmi.exceptions.InvalidCredentialsException;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.NoSuchContextException;
import com.openexchange.admin.rmi.exceptions.NoSuchFilestoreException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.NoSuchUserException;
import com.openexchange.admin.rmi.exceptions.ProgrammErrorException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.rmi.impl.util.OXUserPropertySorter;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.services.PluginInterfaces;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUserStorageInterface;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.storage.utils.Filestore2UserUtil;
import com.openexchange.admin.taskmanagement.TaskManager;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.GenericChecks;
import com.openexchange.admin.tools.PropertyHandler;
import com.openexchange.admin.tools.SHACrypt;
import com.openexchange.admin.tools.UnixCrypt;
import com.openexchange.admin.tools.filestore.FilestoreDataMover;
import com.openexchange.admin.tools.filestore.PostProcessTask;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheService;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.ConfigurationProperty;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorages;
import com.openexchange.groupware.alias.UserAliasStorage;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;

/**
 * @author d7
 * @author cutmasta
 */
public class OXUser extends OXCommonImpl implements OXUserInterface {

    /** The logger */
    final static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OXUser.class);

    // ------------------------------------------------------------------------------------------------ //

    private final OXUserStorageInterface oxu;
    private final BasicAuthenticator basicauth;
    private final AdminCache cache;
    private final PropertyHandler prop;
    private final BundleContext context;
    private final boolean allowChangingQuotaIfNoFileStorageSet;

    /**
     * Initializes a new {@link OXUser}.
     *
     * @param context The associated bundle context
     * @throws StorageException If initialization fails
     */
    public OXUser(BundleContext context) throws StorageException {
        super();
        this.context = context;
        this.cache = ClientAdminThread.cache;
        this.prop = this.cache.getProperties();
        allowChangingQuotaIfNoFileStorageSet = Boolean.parseBoolean(prop.getUserProp("ALLOW_CHANGING_QUOTA_IF_NO_FILESTORE_SET", "false").trim());
        LOGGER.info("Class loaded: {}", this.getClass().getName());
        basicauth = new BasicAuthenticator(this.context);
        try {
            oxu = OXUserStorageInterface.getInstance();
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    private boolean usernameIsChangeable(){
        return this.cache.getProperties().getUserProp(AdminProperties.User.USERNAME_CHANGEABLE, false);
    }

    @Override
    public Set<String> getCapabilities(final Context ctx, final User user, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        if (null == ctx) {
            throw new InvalidDataException("Missing context.");
        }
        if (null == user) {
            throw new InvalidDataException("Missing user.");
        }

        Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        try {
            basicauth.doAuthentication(auth, ctx);
            checkContextAndSchema(ctx);
            try {
                setIdOrGetIDFromNameAndIdObject(ctx, user);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }
            final int user_id = user.getId().intValue();
            if (!tool.existsUser(ctx, user_id)) {
                throw new NoSuchUserException("No such user " + user_id + " in context " + ctx.getId());
            }
            return oxu.getCapabilities(ctx, user);
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchUserException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public void changeMailAddressPersonal(Context ctx, User user, String personal, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        try {
            basicauth.doAuthentication(auth, ctx);
            checkContextAndSchema(ctx);
            try {
                setIdOrGetIDFromNameAndIdObject(ctx, user);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }
            final int user_id = user.getId().intValue();
            if (!tool.existsUser(ctx, user_id)) {
                throw new NoSuchUserException("No such user " + user_id + " in context " + ctx.getId());
            }

            // Change personal
            oxu.changeMailAddressPersonal(ctx, user, personal);

            // Check for context administrator
            final boolean isContextAdmin = tool.isContextAdmin(ctx, user.getId().intValue());

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXUserPluginInterface oxuser : pluginInterfaces.getUserPlugins().getServiceList()) {
                        if (oxuser.canHandleContextAdmin() || (!oxuser.canHandleContextAdmin() && !isContextAdmin)) {
                            try {
                                LOGGER.debug("Calling changeMailAddressPersonal for plugin: {}", oxuser.getClass().getName());
                                oxuser.changeMailAddressPersonal(ctx, user, personal, auth);
                            } catch (final PluginException e) {
                                LOGGER.error("Error while calling changeMailAddressPersonal for plugin: {}", oxuser.getClass().getName(), e);
                                throw StorageException.wrapForRMI(e);
                            } catch (final RuntimeException e) {
                                LOGGER.error("Error while calling changeMailAddressPersonal for plugin: {}", oxuser.getClass().getName(), e);
                                throw StorageException.wrapForRMI(e);
                            }
                        }
                    }
                }
            }

        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchUserException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public void changeCapabilities(final Context ctx, final User user, final Set<String> capsToAdd, final Set<String> capsToRemove, final Set<String> capsToDrop, final Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        if ((null == capsToAdd || capsToAdd.isEmpty()) && (null == capsToRemove || capsToRemove.isEmpty()) && (null == capsToDrop || capsToDrop.isEmpty())) {
            throw new InvalidDataException("No capabilities specified.");
        }
        if (null == ctx) {
            throw new InvalidDataException("Missing context.");
        }
        if (null == user) {
            throw new InvalidDataException("Missing user.");
        }

        Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        LOGGER.debug("{} - {} - {} | {} - {}", ctx, user, (null == capsToAdd ? "" : capsToAdd.toString()), (null == capsToRemove ? "" : capsToRemove.toString()), auth);

        try {
            basicauth.doAuthentication(auth, ctx);
            checkContextAndSchema(ctx);
            try {
                setIdOrGetIDFromNameAndIdObject(ctx, user);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }
            final int user_id = user.getId().intValue();
            if (!tool.existsUser(ctx, user_id)) {
                throw new NoSuchUserException("No such user " + user_id + " in context " + ctx.getId());
            }

            // Change capabilities
            oxu.changeCapabilities(ctx, user, capsToAdd, capsToRemove, capsToDrop, auth);

            // Check for context administrator
            final boolean isContextAdmin = tool.isContextAdmin(ctx, user.getId().intValue());

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXUserPluginInterface oxuser : pluginInterfaces.getUserPlugins().getServiceList()) {
                        if (oxuser.canHandleContextAdmin() || (!oxuser.canHandleContextAdmin() && !isContextAdmin)) {
                            try {
                                LOGGER.debug("Calling changeCapabilities for plugin: {}", oxuser.getClass().getName());
                                oxuser.changeCapabilities(ctx, user, capsToAdd, capsToRemove, capsToDrop, auth);
                            } catch (final PluginException e) {
                                LOGGER.error("Error while calling change for plugin: {}", oxuser.getClass().getName(), e);
                                throw StorageException.wrapForRMI(e);
                            } catch (final RuntimeException e) {
                                LOGGER.error("Error while calling change for plugin: {}", oxuser.getClass().getName(), e);
                                throw StorageException.wrapForRMI(e);
                            }
                        }
                    }
                }
            }

        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchUserException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public int moveUserFilestore(final Context ctx, User user, Filestore dstFilestore, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, NoSuchFilestoreException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(user);
        } catch (final InvalidDataException e2) {
            final InvalidDataException invalidDataException = new InvalidDataException("One of the given arguments for moving file storage data is null");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        try {
            basicauth.doAuthentication(auth, ctx);
            checkContextAndSchema(ctx);
            try {
                setIdOrGetIDFromNameAndIdObject(ctx, user);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }

            final int user_id = user.getId().intValue();
            if (!tool.existsContext(ctx)) {
                throw new NoSuchContextException();
            } else if (!tool.existsUser(ctx, user_id)) {
                throw new NoSuchUserException("No such user " + user_id + " in context " + ctx.getId());
            } else if (!tool.existsStore(dstFilestore.getId().intValue())) {
                throw new NoSuchFilestoreException();
            }

            final OXUserStorageInterface oxuser = this.oxu;
            User storageUser = oxuser.getData(ctx, new User[] { user })[0];

            // Check equality
            int srcStore_id = storageUser.getFilestoreId().intValue();
            if (srcStore_id <= 0) {
                throw new InvalidDataException("Unable to get filestore " + srcStore_id);
            }
            if (srcStore_id == dstFilestore.getId().intValue()) {
                throw new InvalidDataException("The identifiers for the source and destination storage are equal: " + dstFilestore);
            }

            // Check storage name
            String name = storageUser.getFilestore_name();
            if (name == null) {
                throw new InvalidDataException("Unable to get filestore directory for user " + user_id + " in " + ctx.getIdAsString());
            }

            // Check capacity
            OXUtilStorageInterface oxu = OXUtilStorageInterface.getInstance();
            Filestore destFilestore = oxu.getFilestore(dstFilestore.getId().intValue(), false);
            if (!oxu.hasSpaceForAnotherUser(destFilestore)) {
                throw new StorageException("Destination filestore does not have enough space for another user.");
            }

            // Load it to ensure validity
            String baseUri = destFilestore.getUrl();
            try {
                URI uri = FileStorages.getFullyQualifyingUriForContext(ctx.getId().intValue(), new java.net.URI(baseUri));
                FileStorages.getFileStorageService().getFileStorage(uri);
            } catch (OXException e) {
                throw new StorageException(e.getMessage(), e);
            } catch (URISyntaxException e) {
                throw new StorageException("Invalid file storage URI: " + baseUri, e);
            }

            // Initialize mover instance
            FilestoreDataMover fsdm = FilestoreDataMover.newUserMover(oxu.getFilestore(srcStore_id), dstFilestore, storageUser, ctx);

            // Enable user after processing
            fsdm.addPostProcessTask(new PostProcessTask() {

                @Override
                public void perform(ExecutionException executionError) throws StorageException {
                    if (null == executionError) {
                        oxuser.enableUser(user_id, ctx);
                    } else {
                        LOGGER.warn("An execution error occurred during \"moveuserfilestore\" for user {} in context {}. User will stay disabled.", user_id, ctx.getId(), executionError.getCause());
                    }
                }
            });

            // Schedule task
            oxuser.disableUser(user_id, ctx);
            return TaskManager.getInstance().addJob(fsdm, "moveuserfilestore", "move user " + user_id + " from context " + ctx.getIdAsString() + " to another filestore " + dstFilestore.getId(), ctx.getId());
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchUserException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public int moveFromUserFilestoreToMaster(final Context ctx, User user, User masterUser, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, NoSuchFilestoreException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(user);
        } catch (final InvalidDataException e2) {
            final InvalidDataException invalidDataException = new InvalidDataException("One of the given arguments for moving file storage data is null");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        try {
            basicauth.doAuthentication(auth, ctx);
            checkContextAndSchema(ctx);
            try {
                setIdOrGetIDFromNameAndIdObject(ctx, user);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }
            try {
                setIdOrGetIDFromNameAndIdObject(ctx, masterUser);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }

            final int userId = user.getId().intValue();
            if (!tool.existsUser(ctx, userId)) {
                throw new NoSuchUserException("No such user " + userId + " in context " + ctx.getId());
            }

            final OXUserStorageInterface oxuser = this.oxu;
            if (userId == masterUser.getId().intValue()) {
                throw new StorageException("User and master user identifiers are equal.");
            }

            User[] data = oxuser.getData(ctx, new User[] { user, masterUser });
            User storageUser = data[0];
            User storageMasterUser = data[1];

            if (null == storageMasterUser.getFilestoreId() || storageMasterUser.getFilestoreId().intValue() <= 0) {
                throw new StorageException("Master user " + storageMasterUser.getId() + " has no file storage set.");
            }
            if (null == storageUser.getFilestoreId() || storageUser.getFilestoreId().intValue() <= 0) {
                throw new StorageException("User " + storageUser.getId() + " has no file storage set.");
            }
            if (storageMasterUser.getFilestoreId().intValue() == storageUser.getFilestoreId().intValue()) {
                String masterFsName = storageMasterUser.getFilestore_name();
                if (null == masterFsName) {
                    throw new InvalidDataException("Unable to get filestore directory for master user " + masterUser.getId() + " in " + ctx.getIdAsString());
                }
                String userFsName = storageUser.getFilestore_name();
                if (null == userFsName) {
                    throw new InvalidDataException("Unable to get filestore directory for user " + userId + " in " + ctx.getIdAsString());
                }
                if (masterFsName.equals(userFsName)) {
                    throw new StorageException("User " + storageUser.getId() + " already has a master file storage set.");
                }
            }

            if (!tool.existsStore(storageMasterUser.getFilestoreId().intValue())) {
                throw new NoSuchFilestoreException();
            }
            boolean equal = storageMasterUser.getFilestoreId().intValue() == storageUser.getFilestoreId().intValue();

            if (!equal && !tool.existsStore(storageUser.getFilestoreId().intValue())) {
                throw new NoSuchFilestoreException();
            }

            OXUtilStorageInterface oxu = OXUtilStorageInterface.getInstance();
            Filestore destFilestore = oxu.getFilestore(storageMasterUser.getFilestoreId().intValue(), false);
            Filestore srcFilestore = equal ? destFilestore : oxu.getFilestoreBasic(storageUser.getFilestoreId().intValue());

            // Check equality
            int srcStore_id = storageUser.getFilestoreId().intValue();
            if (srcStore_id <= 0) {
                throw new InvalidDataException("Unable to get filestore " + srcStore_id);
            }

            // Check storage name
            String name = storageUser.getFilestore_name();
            if (name == null) {
                throw new InvalidDataException("Unable to get filestore directory for user " + userId + " in " + ctx.getIdAsString());
            }
            name = storageMasterUser.getFilestore_name();
            if (name == null) {
                throw new InvalidDataException("Unable to get filestore directory for user " + storageMasterUser.getId() + " in " + ctx.getIdAsString());
            }

            // Check capacity
            if (!equal && !oxu.hasSpaceForAnotherUser(destFilestore)) {
                throw new StorageException("Destination filestore does not have enough space for another user.");
            }

            // Initialize mover instance
            FilestoreDataMover fsdm = FilestoreDataMover.newUser2MasterMover(srcFilestore, destFilestore, storageUser, storageMasterUser, ctx);

            // Enable user after processing
            fsdm.addPostProcessTask(new PostProcessTask() {

                @Override
                public void perform(ExecutionException executionError) throws StorageException {
                    if (null == executionError) {
                        oxuser.enableUser(userId, ctx);
                    } else {
                        LOGGER.warn("An execution error occurred during \"movefromuserfilestoretomaster\" for user {} in context {}. User will stay disabled.", userId, ctx.getId(), executionError.getCause());
                    }
                }
            });

            // Schedule task
            oxuser.disableUser(userId, ctx);
            return TaskManager.getInstance().addJob(fsdm, "movefromuserfilestoretomaster", "move user " + userId + " from context " + ctx.getIdAsString() + " from individual to master filestore " + destFilestore.getId(), ctx.getId());
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchUserException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public int moveFromMasterToUserFilestore(final Context ctx, User user, User masterUser, Filestore dstFilestore, long maxQuota, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, NoSuchFilestoreException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(user);
        } catch (final InvalidDataException e2) {
            final InvalidDataException invalidDataException = new InvalidDataException("One of the given arguments for moving file storage data is null");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        try {
            basicauth.doAuthentication(auth, ctx);
            checkContextAndSchema(ctx);
            try {
                setIdOrGetIDFromNameAndIdObject(ctx, user);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }
            try {
                setIdOrGetIDFromNameAndIdObject(ctx, masterUser);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }

            final int userId = user.getId().intValue();
            if (!tool.existsUser(ctx, userId)) {
                throw new NoSuchUserException("No such user " + userId + " in context " + ctx.getId());
            }

            final OXUserStorageInterface oxuser = this.oxu;
            if (userId == masterUser.getId().intValue()) {
                throw new StorageException("User and master user identifiers are equal.");
            }

            User[] data = oxuser.getData(ctx, new User[] { user, masterUser });
            User storageUser = data[0];
            User storageMasterUser = data[1];

            if (null == storageMasterUser.getFilestoreId() || storageMasterUser.getFilestoreId().intValue() <= 0) {
                throw new StorageException("Master user " + storageMasterUser.getId() + " has no file storage set.");
            }
            if (null == storageUser.getFilestoreId() || storageUser.getFilestoreId().intValue() <= 0) {
                throw new StorageException("User " + storageUser.getId() + " has no file storage set.");
            }
            if (storageMasterUser.getFilestoreId().intValue() != storageUser.getFilestoreId().intValue()) {
                throw new StorageException("User " + storageUser.getId() + " has no master file storage set.");
            }
            {
                String masterFsName = storageMasterUser.getFilestore_name();
                if (null == masterFsName) {
                    throw new InvalidDataException("Unable to get filestore directory for master user " + masterUser.getId() + " in " + ctx.getIdAsString());
                }
                String userFsName = storageUser.getFilestore_name();
                if (null == userFsName) {
                    throw new InvalidDataException("Unable to get filestore directory for user " + userId + " in " + ctx.getIdAsString());
                }
                if (!masterFsName.equals(userFsName)) {
                    throw new StorageException("User " + storageUser.getId() + " has no master file storage set.");
                }
            }

            if (null == dstFilestore) {
                throw new InvalidDataException("Missing filestore parameter");
            }

            if (!tool.existsStore(dstFilestore.getId().intValue())) {
                throw new NoSuchFilestoreException();
            }
            boolean equal = dstFilestore.getId().intValue() == storageMasterUser.getFilestoreId().intValue();

            if (!equal && !tool.existsStore(storageMasterUser.getFilestoreId().intValue())) {
                throw new NoSuchFilestoreException();
            }

            OXUtilStorageInterface oxu = OXUtilStorageInterface.getInstance();
            Filestore destFilestore = oxu.getFilestore(dstFilestore.getId().intValue(), false);
            Filestore srcFilestore = equal ? destFilestore : oxu.getFilestoreBasic(storageMasterUser.getFilestoreId().intValue());

            // Check equality
            int srcStore_id = storageMasterUser.getFilestoreId().intValue();
            if (srcStore_id <= 0) {
                throw new InvalidDataException("Unable to get filestore " + srcStore_id);
            }

            // Check storage name
            String name = storageUser.getFilestore_name();
            if (name == null) {
                throw new InvalidDataException("Unable to get filestore directory for user " + userId + " in " + ctx.getIdAsString());
            }
            name = storageMasterUser.getFilestore_name();
            if (name == null) {
                throw new InvalidDataException("Unable to get filestore directory for user " + storageMasterUser.getId() + " in " + ctx.getIdAsString());
            }

            // Check capacity
            if (!equal && !oxu.hasSpaceForAnotherUser(destFilestore)) {
                throw new StorageException("Destination filestore does not have enough space for another user.");
            }

            // Load it to ensure validity
            String baseUri = destFilestore.getUrl();
            try {
                URI uri = FileStorages.getFullyQualifyingUriForContext(ctx.getId().intValue(), new java.net.URI(baseUri));
                FileStorages.getFileStorageService().getFileStorage(uri);
            } catch (OXException e) {
                throw new StorageException(e.getMessage(), e);
            } catch (URISyntaxException e) {
                throw new StorageException("Invalid file storage URI: " + baseUri, e);
            }

            // Initialize mover instance
            FilestoreDataMover fsdm = FilestoreDataMover.newUserFromMasterMover(srcFilestore, destFilestore, maxQuota, storageUser, storageMasterUser, ctx);

            // Enable user after processing
            fsdm.addPostProcessTask(new PostProcessTask() {

                @Override
                public void perform(ExecutionException executionError) throws StorageException {
                    if (null == executionError) {
                        oxuser.enableUser(userId, ctx);
                    } else {
                        LOGGER.warn("An execution error occurred during \"movefrommastertouserfilestore\" for user {} in context {}. User will stay disabled.", userId, ctx.getId(), executionError.getCause());
                    }
                }
            });

            // Schedule task
            oxuser.disableUser(userId, ctx);
            return TaskManager.getInstance().addJob(fsdm, "movefrommastertouserfilestore", "move user " + userId + " from context " + ctx.getIdAsString() + " from master to individual filestore " + destFilestore.getId(), ctx.getId());
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchUserException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public int moveFromContextToUserFilestore(final Context ctx, User user, Filestore dstFilestore, long maxQuota, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, NoSuchFilestoreException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        return moveFromContextToUserFilestore(ctx, user, dstFilestore, maxQuota, credentials, false);
    }

    private int moveFromContextToUserFilestore(final Context ctx, User user, Filestore dstFilestore, long maxQuota, Credentials credentials, boolean inline) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, NoSuchFilestoreException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(user);
        } catch (final InvalidDataException e2) {
            final InvalidDataException invalidDataException = new InvalidDataException("One of the given arguments for moving file storage data is null");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        try {
            if (false == inline) {
                basicauth.doAuthentication(auth, ctx);
                checkContextAndSchema(ctx);
                try {
                    setIdOrGetIDFromNameAndIdObject(ctx, user);
                } catch (NoSuchObjectException e) {
                    throw new NoSuchUserException(e);
                }

                int user_id = user.getId().intValue();
                if (!tool.existsUser(ctx, user_id)) {
                    throw new NoSuchUserException("No such user " + user_id + " in context " + ctx.getId());
                }
            }

            final int user_id = user.getId().intValue();
            final OXUserStorageInterface oxuser = this.oxu;
            final OXContextInterface oxctx = AdminServiceRegistry.getInstance().getService(OXContextInterface.class, true);

            Context storageContext = oxctx.getOwnData(ctx, auth);
            User storageUser = oxuser.getData(ctx, new User[] { user })[0];

            if (null != storageUser.getFilestoreId() && storageUser.getFilestoreId().intValue() > 0) {
                throw new StorageException("User " + storageUser.getId() + " already has a dedicate file storage set.");
            }

            if (null == dstFilestore) {
                throw new InvalidDataException("Missing filestore parameter");
            }

            if (!tool.existsStore(dstFilestore.getId().intValue())) {
                throw new NoSuchFilestoreException();
            }
            boolean equal = dstFilestore.getId().intValue() == storageContext.getFilestoreId().intValue();

            if (!equal && !tool.existsStore(storageContext.getFilestoreId().intValue())) {
                throw new NoSuchFilestoreException();
            }

            OXUtilStorageInterface oxu = OXUtilStorageInterface.getInstance();
            Filestore destFilestore = oxu.getFilestore(dstFilestore.getId().intValue(), false);
            Filestore srcFilestore = equal ? destFilestore : oxu.getFilestoreBasic(storageContext.getFilestoreId().intValue());

            // Check equality
            int srcStore_id = storageContext.getFilestoreId().intValue();
            if (srcStore_id <= 0) {
                throw new InvalidDataException("Unable to get filestore " + srcStore_id);
            }
            ctx.setFilestoreId(Integer.valueOf(srcStore_id));
            if (srcStore_id == destFilestore.getId().intValue()) {
                // Ok when moving from context to user
                //throw new InvalidDataException("The identifiers for the source and destination storage are equal: " + destFilestore);
            }

            // Check storage name
            String name = storageContext.getFilestore_name();
            if (name == null) {
                throw new InvalidDataException("Unable to get filestore directory for context " + ctx.getIdAsString());
            }

            // Check capacity
            if (!equal && !oxu.hasSpaceForAnotherUser(destFilestore)) {
                throw new StorageException("Destination filestore does not have enough space for another user.");
            }

            // Load it to ensure validity
            String baseUri = destFilestore.getUrl();
            try {
                URI uri = FileStorages.getFullyQualifyingUriForContext(ctx.getId().intValue(), new java.net.URI(baseUri));
                FileStorages.getFileStorageService().getFileStorage(uri);
            } catch (OXException e) {
                throw new StorageException(e.getMessage(), e);
            } catch (URISyntaxException e) {
                throw new StorageException("Invalid file storage URI: " + baseUri, e);
            }

            // Initialize mover instance
            FilestoreDataMover fsdm = FilestoreDataMover.newContext2UserMover(srcFilestore, destFilestore, maxQuota, storageUser, storageContext);

            // Enable user after processing
            fsdm.addPostProcessTask(new PostProcessTask() {

                @Override
                public void perform(ExecutionException executionError) throws StorageException {
                    if (null == executionError) {
                        oxuser.enableUser(user_id, ctx);
                    } else {
                        LOGGER.warn("An execution error occurred during \"movefromcontexttouserfilestore\" for user {} in context {}. User will stay disabled.", user_id, ctx.getId(), executionError.getCause());
                    }
                }
            });

            oxuser.disableUser(user_id, ctx);

            if (false == inline) {
                // Schedule task
                return TaskManager.getInstance().addJob(fsdm, "movefromcontexttouserfilestore", "move user " + user_id + " from context " + ctx.getIdAsString() + " from context to individual filestore " + destFilestore.getId(), ctx.getId());
            }

            // Execute with current thread
            fsdm.call();
            return -1;
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchUserException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final RemoteException e) {
            LOGGER.error("", e);
            throw new StorageException(e);
        } catch (final OXException e) {
            LOGGER.error("", e);
            throw new StorageException(e);
        } catch (IOException e) {
            LOGGER.error("", e);
            throw new StorageException(e);
        } catch (InterruptedException e) {
            // Keep interrupted state
            Thread.currentThread().interrupt();
            LOGGER.error("", e);
            throw new StorageException(e);
        } catch (ProgrammErrorException e) {
            LOGGER.error("", e);
            throw new StorageException(e);
        }
    }

    @Override
    public int moveFromUserToContextFilestore(final Context ctx, User user, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, NoSuchFilestoreException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        return moveFromUserToContextFilestore(ctx, user, credentials, false);
    }

    private int moveFromUserToContextFilestore(final Context ctx, User user, Credentials credentials, boolean inline) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, NoSuchFilestoreException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(user);
        } catch (final InvalidDataException e2) {
            final InvalidDataException invalidDataException = new InvalidDataException("One of the given arguments for moving file storage data is null");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        try {
            if (false == inline) {
                basicauth.doAuthentication(auth, ctx);
                checkContextAndSchema(ctx);
                try {
                    setIdOrGetIDFromNameAndIdObject(ctx, user);
                } catch (NoSuchObjectException e) {
                    throw new NoSuchUserException(e);
                }

                int user_id = user.getId().intValue();
                if (!tool.existsUser(ctx, user_id)) {
                    throw new NoSuchUserException("No such user " + user_id + " in context " + ctx.getId());
                }
            }

            final int userId = user.getId().intValue();
            final OXUserStorageInterface oxuser = this.oxu;
            final OXContextInterface oxctx = AdminServiceRegistry.getInstance().getService(OXContextInterface.class, true);

            Context storageContext = oxctx.getOwnData(ctx, auth);
            User storageUser = oxuser.getData(ctx, new User[] { user })[0];

            if (null == storageUser.getFilestoreId() || storageUser.getFilestoreId().intValue() <= 0) {
                throw new StorageException("User " + storageUser.getId() + " has no file storage set.");
            }
            if (storageUser.getFilestoreOwner() != null) {
                int ownerId = storageUser.getFilestoreOwner().intValue();
                if (ownerId > 0 && ownerId != userId) {
                    throw new StorageException("User " + storageUser.getId() + " does not have his own file storage set, but is currently using the file storage from user " + ownerId);
                }
            }
            if (storageContext.getFilestoreId().intValue() == storageUser.getFilestoreId().intValue()) {
                String contextFsName = storageContext.getFilestore_name();
                if (null == contextFsName) {
                    throw new InvalidDataException("Unable to get filestore directory for context " + ctx.getIdAsString());
                }
                String userFsName = storageUser.getFilestore_name();
                if (null == userFsName) {
                    throw new InvalidDataException("Unable to get filestore directory for user " + userId + " in " + ctx.getIdAsString());
                }
                if (contextFsName.equals(userFsName)) {
                    throw new StorageException("User " + storageUser.getId() + " already has a context file storage set.");
                }
            }

            if (!tool.existsStore(storageContext.getFilestoreId().intValue())) {
                throw new NoSuchFilestoreException();
            }
            boolean equal = storageContext.getFilestoreId().intValue() == storageUser.getFilestoreId().intValue();

            if (!equal && !tool.existsStore(storageUser.getFilestoreId().intValue())) {
                throw new NoSuchFilestoreException();
            }

            OXUtilStorageInterface oxu = OXUtilStorageInterface.getInstance();
            Filestore destFilestore = oxu.getFilestore(storageContext.getFilestoreId().intValue(), false);
            Filestore srcFilestore = equal ? destFilestore : oxu.getFilestoreBasic(storageUser.getFilestoreId().intValue());

            // Check equality
            int srcStore_id = storageUser.getFilestoreId().intValue();
            if (srcStore_id <= 0) {
                throw new InvalidDataException("Unable to get filestore " + srcStore_id);
            }
            ctx.setFilestoreId(destFilestore.getId());
            if (srcStore_id == destFilestore.getId().intValue()) {
                // Ok
                // throw new InvalidDataException("The identifiers for the source and destination storage are equal: " + destFilestore);
            }

            // Check storage name
            String name = storageUser.getFilestore_name();
            if (name == null) {
                throw new InvalidDataException("Unable to get filestore directory for user " + userId + " in " + ctx.getIdAsString());
            }
            name = storageContext.getFilestore_name();
            if (name == null) {
                throw new InvalidDataException("Unable to get filestore directory for context " + storageContext.getId());
            }

            // Check capacity
            if (!equal && !oxu.hasSpaceForAnotherUser(destFilestore)) {
                throw new StorageException("Destination filestore does not have enough space for another user.");
            }

            // Initialize mover instance
            FilestoreDataMover fsdm = FilestoreDataMover.newUser2ContextMover(srcFilestore, destFilestore, storageUser, storageContext);

            // Enable user after processing
            fsdm.addPostProcessTask(new PostProcessTask() {

                @Override
                public void perform(ExecutionException executionError) throws StorageException {
                    if (null == executionError) {
                        oxuser.enableUser(userId, ctx);
                    } else {
                        LOGGER.warn("An execution error occurred during \"movefromusertocontextfilestore\" for user {} in context {}. User will stay disabled.", userId, ctx.getId(), executionError.getCause());
                    }
                }
            });

            oxuser.disableUser(userId, ctx);

            if (false == inline) {
                // Schedule task
                return TaskManager.getInstance().addJob(fsdm, "movefromusertocontextfilestore", "move user " + userId + " from context " + ctx.getIdAsString() + " from individual to context filestore " + destFilestore.getId(), ctx.getId());
            }

            // Execute with current thread
            fsdm.call();
            return -1;
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchUserException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final RemoteException e) {
            LOGGER.error("", e);
            throw new StorageException(e);
        } catch (final OXException e) {
            LOGGER.error("", e);
            throw new StorageException(e);
        }  catch (IOException e) {
            LOGGER.error("", e);
            throw new StorageException(e);
        } catch (InterruptedException e) {
            // Keep interrupted state
            Thread.currentThread().interrupt();
            LOGGER.error("", e);
            throw new StorageException(e);
        } catch (ProgrammErrorException e) {
            LOGGER.error("", e);
            throw new StorageException(e);
        }
    }

    @Override
    public void change(final Context ctx, final User usrdata, Credentials credentials) throws StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(usrdata);
        } catch (final InvalidDataException e2) {
            final InvalidDataException invalidDataException = new InvalidDataException("One of the given arguments for change is null");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        // SPECIAL USER AUTH CHECK FOR THIS METHOD!
        // check if credentials are from oxadmin or from an user
        Integer userid = null;

        try {
            contextcheck(ctx);

            checkContextAndSchema(ctx);

            try {
                setIdOrGetIDFromNameAndIdObject(ctx, usrdata);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }
            usrdata.testMandatoryCreateFieldsNull();
            userid = usrdata.getId();

            if (!cache.contextAuthenticationDisabled()) {
                if( basicauth.isMasterOfContext(credentials, ctx) ) {
                    basicauth.doAuthentication(auth, ctx);
                } else {
                    final int auth_user_id = tool.getUserIDByUsername(ctx, auth.getLogin());
                    // check if given user is admin
                    if (tool.isContextAdmin(ctx, auth_user_id)) {
                        basicauth.doAuthentication(auth, ctx);
                    } else {
                        basicauth.doUserAuthentication(auth, ctx);
                        // now check if user which authed has the same id as the user he
                        // wants to change,else fail,
                        // cause then he/she wants to change not his own data!
                        if (userid.intValue() != auth_user_id) {
                            throw new InvalidCredentialsException("Permission denied");
                        }
                    }
                }
            }

                LOGGER.debug("{} - {} - {}", ctx, usrdata, auth);

            if (!tool.existsUser(ctx, userid.intValue())) {
                final NoSuchUserException noSuchUserException = new NoSuchUserException("No such user " + userid + " in context " + ctx.getId());
                LOGGER.error("", noSuchUserException);
                throw noSuchUserException;
            }
            if (tool.getIsGuestByUserID(ctx, userid.intValue())) {
                final InvalidDataException invalidDataException = new InvalidDataException("User to change (user id "+ userid + " , context id " + ctx.getId() + ") is a guest user. Guests cannot be changed via provisioning.");
                LOGGER.error("", invalidDataException);
                throw invalidDataException;
            }

            if (tool.existsDisplayName(ctx, usrdata, i(usrdata.getId()))) {
                throw new InvalidDataException("The displayname is already used");
            }
            final User[] dbuser = oxu.getData(ctx, new User[] { usrdata });

            checkChangeUserData(ctx, usrdata, dbuser[0], this.prop);

        } catch (final InvalidDataException e1) {
            LOGGER.error("", e1);
            throw e1;
        } catch (final StorageException e1) {
            LOGGER.error("", e1);
            throw e1;
        }

        // Check if he wants to change the filestore id
        {
            Integer filestoreId = usrdata.getFilestoreId();
            if (filestoreId != null) {
                if (!tool.existsStore(filestoreId.intValue())) {
                    final InvalidDataException inde = new InvalidDataException("No such filestore with id " + filestoreId.intValue());
                    LOGGER.error("", inde);
                    throw inde;
                }

                Integer fsId = getData(ctx, usrdata, credentials).getFilestoreId();
                if (fsId == null || fsId.intValue() <= 0) {
                    final InvalidDataException inde = new InvalidDataException("Not allowed to change the filestore for user " + userid + " in context " + ctx.getId() + ". Please use appropriate method instead.");
                    LOGGER.error("", inde);
                    throw inde;
                }
                if (fsId.intValue() != filestoreId.intValue()) {
                    final InvalidDataException inde = new InvalidDataException("Not allowed to change the filestore for user " + userid + " in context " + ctx.getId() + ". Please use appropriate method instead.");
                    LOGGER.error("", inde);
                    throw inde;
                }
            }
        }

        final boolean isContextAdmin = tool.isContextAdmin(ctx, userid.intValue());

        // Is a quota specified that implies to assign a file storage?
        Long maxQuota = usrdata.getMaxQuota();
        if (maxQuota != null) {
            long quota_max_temp = maxQuota.longValue();
            if (quota_max_temp != -1) {
                // A valid quota is specified - ensure an appropriate file storage is set
                Integer fsId = getData(ctx, usrdata, credentials).getFilestoreId();
                if (fsId == null || fsId.intValue() <= 0) {
                    if (!allowChangingQuotaIfNoFileStorageSet) {
                        throw new StorageException("Quota cannot be changed for user " + userid + " in context " + ctx.getId() + " since that user has no file storage set. See \"ALLOW_CHANGING_QUOTA_IF_NO_FILESTORE_SET\".");
                    }

                    // Auto-select next suitable file storage
                    OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
                    int fileStorageToPrefer = oxutil.getFilestoreIdFromContext(ctx.getId().intValue());
                    Filestore filestoreForUser = oxutil.findFilestoreForUser(fileStorageToPrefer);

                    // Load it to ensure validity
                    OXUtilStorageInterface oxu = OXUtilStorageInterface.getInstance();
                    try {
                        URI uri = FileStorages.getFullyQualifyingUriForContext(ctx.getId().intValue(), oxu.getFilestoreURI(i(filestoreForUser.getId())));
                        FileStorages.getFileStorageService().getFileStorage(uri);
                    } catch (OXException e) {
                        throw new StorageException(e.getMessage(), e);
                    }

                    // (Synchronous) Move from context to individual user file storage
                    try {
                        moveFromContextToUserFilestore(ctx, usrdata, filestoreForUser, quota_max_temp, credentials, true);
                    } catch (RemoteException e) {
                        throw new StorageException(e);
                    } catch (NoSuchFilestoreException e) {
                        throw new StorageException(e);
                    }
                } else {
                    if (!OXToolStorageInterface.getInstance().existsStore(i(fsId))) {
                        throw new StorageException("Filestore with identifier " + fsId + " does not exist.");
                    }

                    // Load it to ensure validity
                    OXUtilStorageInterface oxu = OXUtilStorageInterface.getInstance();
                    try {
                        URI uri = FileStorages.getFullyQualifyingUriForContext(ctx.getId().intValue(), oxu.getFilestoreURI(i(fsId)));
                        FileStorages.getFileStorageService().getFileStorage(uri);
                    } catch (OXException e) {
                        throw new StorageException(e.getMessage(), e);
                    }
                }
            }
        }

        // Trigger plugin extensions
        {
            final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (final OXUserPluginInterface oxuser : pluginInterfaces.getUserPlugins().getServiceList()) {
                    if ((oxuser instanceof OXUserPluginInterfaceExtended) && (oxuser.canHandleContextAdmin() || (!oxuser.canHandleContextAdmin() && !isContextAdmin))) {
                        OXUserPluginInterfaceExtended oxuserExtended = (OXUserPluginInterfaceExtended) oxuser;
                        try {
                            LOGGER.debug("Calling change for plugin: {}", oxuser.getClass().getName());
                            oxuserExtended.beforeChange(ctx, usrdata, auth);
                        } catch (final PluginException e) {
                            LOGGER.error("Error while calling change for plugin: {}", oxuser.getClass().getName(), e);
                            throw StorageException.wrapForRMI(e);
                        } catch (final RuntimeException e) {
                            LOGGER.error("Error while calling change for plugin: {}", oxuser.getClass().getName(), e);
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }
        }

        oxu.change(ctx, usrdata);

        // Trigger plugin extensions
        {
            final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (final OXUserPluginInterface oxuser : pluginInterfaces.getUserPlugins().getServiceList()) {
                    if (oxuser.canHandleContextAdmin() || (!oxuser.canHandleContextAdmin() && !isContextAdmin)) {
                        try {
                            LOGGER.debug("Calling change for plugin: {}", oxuser.getClass().getName());
                            oxuser.change(ctx, usrdata, auth);
                        } catch (final PluginException e) {
                            LOGGER.error("Error while calling change for plugin: {}", oxuser.getClass().getName(), e);
                            throw StorageException.wrapForRMI(e);
                        } catch (final RuntimeException e) {
                            LOGGER.error("Error while calling change for plugin: {}", oxuser.getClass().getName(), e);
                            throw StorageException.wrapForRMI(e);
                        }
                    }
                }
            }
        }

        // change cached admin credentials if necessary
        if (isContextAdmin && usrdata.getPassword() != null) {
            final Credentials cauth = cache.getAdminCredentials(ctx);
            final String mech = cache.getAdminAuthMech(ctx);
            if ("{CRYPT}".equalsIgnoreCase(mech)) {
                try {
                    cauth.setPassword(UnixCrypt.crypt(usrdata.getPassword()));
                } catch (final UnsupportedEncodingException e) {
                    LOGGER.error("Error encrypting password for credential cache ", e);
                    throw new StorageException(e);
                }
            } else if ("{SHA}".equalsIgnoreCase(mech)) {
                try {
                    cauth.setPassword(SHACrypt.makeSHAPasswd(usrdata.getPassword()));
                } catch (final NoSuchAlgorithmException e) {
                    LOGGER.error("Error encrypting password for credential cache ", e);
                    throw new StorageException(e);
                } catch (final UnsupportedEncodingException e) {
                    LOGGER.error("Error encrypting password for credential cache ", e);
                    throw new StorageException(e);
                }
            } else if ("{BCRYPT}".equalsIgnoreCase(mech)) {
                try {
                    cauth.setPassword(BCrypt.hashpw(usrdata.getPassword(), BCrypt.gensalt()));
                } catch (final RuntimeException e) {
                    LOGGER.error("Error encrypting password for credential cache ", e);
                    throw new StorageException(e);
                }
            }
            cache.setAdminCredentials(ctx,mech,cauth);
        }
    }

    @Override
    public void changeModuleAccess(final Context ctx, final User user, final UserModuleAccess moduleAccess, final Credentials credentials) throws StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(user,moduleAccess);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("User or UserModuleAccess is null");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

            LOGGER.debug("{} - {} - {} - {}", ctx, user, moduleAccess, auth);

        try {
            basicauth.doAuthentication(auth, ctx);
            checkContextAndSchema(ctx);
            try {
                setIdOrGetIDFromNameAndIdObject(ctx, user);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }
            final int user_id = user.getId().intValue();
            if (!tool.existsUser(ctx, user_id)) {
                throw new NoSuchUserException("No such user " + user_id + " in context " + ctx.getId());
            }

            // Change module access
            oxu.changeModuleAccess(ctx, user_id, moduleAccess);

            // Check for context administrator
            final boolean isContextAdmin = tool.isContextAdmin(ctx, user.getId().intValue());

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXUserPluginInterface oxuser : pluginInterfaces.getUserPlugins().getServiceList()) {
                        if (oxuser.canHandleContextAdmin() || (!oxuser.canHandleContextAdmin() && !isContextAdmin)) {
                            try {
                                LOGGER.debug("Calling changeModuleAccess for plugin: {}", oxuser.getClass().getName());
                                oxuser.changeModuleAccess(ctx, user, moduleAccess, auth);
                            } catch (final PluginException e) {
                                LOGGER.error("Error while calling change for plugin: {}", oxuser.getClass().getName(), e);
                                throw StorageException.wrapForRMI(e);
                            } catch (final RuntimeException e) {
                                LOGGER.error("Error while calling change for plugin: {}", oxuser.getClass().getName(), e);
                                throw StorageException.wrapForRMI(e);
                            }
                        }
                    }
                }
            }
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchUserException e) {
            LOGGER.error("", e);
            throw e;
        }

//      JCS
        try {
            UserConfigurationStorage.getInstance().invalidateCache(user.getId().intValue(), new ContextImpl(ctx.getId().intValue()));
        } catch (final OXException e) {
            LOGGER.error("Error removing user {} in context {} from configuration storage", user.getId(), ctx.getId(),e);
        }
        // END OF JCS
    }

    @Override
    public void changeModuleAccess(final Context ctx, final User user,final String access_combination_name, final Credentials credentials)
            throws StorageException,InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchUserException {

        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(user,access_combination_name);
            if (access_combination_name.trim().length() == 0) {
                throw new InvalidDataException("Invalid access combination name");
            }
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("User or UserModuleAccess is null");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        LOGGER.debug("{} - {} - {} - {}", ctx, user, access_combination_name, auth);

        try {
            basicauth.doAuthentication(auth, ctx);
            checkContextAndSchema(ctx);
            try {
                setIdOrGetIDFromNameAndIdObject(ctx, user);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }
            final int user_id = user.getId().intValue();
            if (!tool.existsUser(ctx, user_id)) {
                throw new NoSuchUserException("No such user " + user_id + " in context " + ctx.getId());
            }

            UserModuleAccess access = cache.getNamedAccessCombination(access_combination_name.trim(), tool.getAdminForContext(ctx) == user_id);
            if(access==null){
                // no such access combination name defined in configuration
                // throw error!
                throw new InvalidDataException("No such access combination name \""+access_combination_name.trim()+"\"");
            }
            access = access.clone();

            // Change module access
            oxu.changeModuleAccess(ctx, user_id, access);

            // Check for context administrator
            final boolean isContextAdmin = tool.isContextAdmin(ctx, user.getId().intValue());

            // Trigger plugin extensions
            {
                final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
                if (null != pluginInterfaces) {
                    for (final OXUserPluginInterface oxuser : pluginInterfaces.getUserPlugins().getServiceList()) {
                        if (oxuser.canHandleContextAdmin() || (!oxuser.canHandleContextAdmin() && !isContextAdmin)) {
                            try {
                                LOGGER.debug("Calling changeModuleAccess for plugin: {}", oxuser.getClass().getName());
                                oxuser.changeModuleAccess(ctx, user, access_combination_name, auth);
                            } catch (final PluginException e) {
                                LOGGER.error("Error while calling change for plugin: {}", oxuser.getClass().getName(), e);
                                throw StorageException.wrapForRMI(e);
                            } catch (final RuntimeException e) {
                                LOGGER.error("Error while calling change for plugin: {}", oxuser.getClass().getName(), e);
                                throw StorageException.wrapForRMI(e);
                            }
                        }
                    }
                }
            }
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchUserException e) {
            LOGGER.error("", e);
            throw e;
        }

        // JCS
        try {
            UserConfigurationStorage.getInstance().invalidateCache(user.getId().intValue(), new ContextImpl(ctx.getId().intValue()));
        } catch (final OXException e) {
            LOGGER.error("Error removing user {} in context {} from configuration storage", user.getId(), ctx.getId(),e);
        }
        final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
        if (null != cacheService) {
            try {
                final Cache usercCache = cacheService.getCache("User");
                final Cache upCache = cacheService.getCache("UserPermissionBits");
                final Cache ucCache = cacheService.getCache("UserConfiguration");
                final Cache usmCache = cacheService.getCache("UserSettingMail");
                final Cache capabilitiesCache = cacheService.getCache("Capabilities");
                {
                    final CacheKey key = cacheService.newCacheKey(i(ctx.getId()), user.getId().intValue());
                    usercCache.remove(key);
                    usercCache.remove(cacheService.newCacheKey(i(ctx.getId()), user.getName()));
                    upCache.remove(key);
                    ucCache.remove(key);
                    usmCache.remove(key);
                    capabilitiesCache.removeFromGroup(user.getId(), ctx.getId().toString());
                    try {
                        UserConfigurationStorage.getInstance().invalidateCache(user.getId().intValue(),
                                new ContextImpl(ctx.getId().intValue()));
                    } catch (final OXException e) {
                        LOGGER.error("Error removing user {} in context {} from configuration storage", user.getId(), ctx.getId(), e);
                    }
                }
            } catch (final OXException e) {
                LOGGER.error("", e);
            }
        }
        // END OF JCS
    }

    @Override
    public User create(final Context ctx, final User usr, final UserModuleAccess access, final Credentials credentials) throws StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        // Call common create method directly because we already have out access module
        return createUserCommon(ctx, usr, access, credentials);
    }

    @Override
    public User create(final Context ctx, final User usrdata, final String access_combination_name, final Credentials credentials) throws StorageException,InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException {
        // Resolve the access rights by the specified combination name. If combination name does not exists, throw error as it is described
        // in the spec!
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(usrdata, access_combination_name);
            if (access_combination_name.trim().length() == 0) {
                throw new InvalidDataException("Invalid access combination name");
            }
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for create is null", e3);
            throw e3;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        LOGGER.debug("{} - {} - {} - {}", ctx, usrdata, access_combination_name, auth);

        basicauth.doAuthentication(auth, ctx);


        UserModuleAccess access = cache.getNamedAccessCombination(access_combination_name.trim(), false);
        if(access==null){
            // no such access combination name defined in configuration
            // throw error!
            throw new InvalidDataException("No such access combination name \""+access_combination_name.trim()+"\"");
        }
        access = access.clone();

        // Call main create user method with resolved access rights
        return createUserCommon(ctx, usrdata, access, auth);
    }


    @Override
    public User create(final Context ctx, final User usrdata, final Credentials credentials)    throws StorageException,InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException {

        /*
         * Resolve current access rights from the specified context (admin) as
         * it is described in the spec and then call the main create user method
         * with the access rights!
         */
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;

        try {
            doNullCheck(usrdata);
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for create is null", e3);
            throw e3;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

            LOGGER.debug("{} - {} - {}", ctx, usrdata, auth);

        basicauth.doAuthentication(auth, ctx);

        /*
         * Resolve admin user of specified context via tools and then get his current module access rights
         */

        final int admin_id = tool.getAdminForContext(ctx);
        final UserModuleAccess access = oxu.getModuleAccess(ctx, admin_id);

        if (access.isPublicFolderEditable()) {
            // publicFolderEditable can only be applied to the context administrator.
            access.setPublicFolderEditable(false);
        }

        return createUserCommon(ctx, usrdata, access, auth);
    }

    @Override
    public User getContextAdmin(final Context ctx, final Credentials credentials) throws InvalidCredentialsException, StorageException, InvalidDataException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;

        basicauth.doAuthentication(auth, ctx);
        return (oxu.getData(ctx, new User[]{ new User(tool.getAdminForContext(ctx))} ))[0];
    }

    @Override
    public UserModuleAccess getContextAdminUserModuleAccess(final Context ctx, final Credentials credentials)  throws StorageException,InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        basicauth.doAuthentication(auth, ctx);

        /*
         * Resolve admin user of specified context via tools and then get his current module access rights
         */

        final int admin_id = tool.getAdminForContext(ctx);
        final UserModuleAccess access = oxu.getModuleAccess(ctx, admin_id);
        return access;
    }

    /*
     * Main method to create a user. Which all inner create methods MUST use after resolving the access rights!
     */
    private User createUserCommon(final Context ctx, final User usr, final UserModuleAccess access, final Credentials auth) throws StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {

        try {
            doNullCheck(usr,access);
        } catch (final InvalidDataException e3) {
            LOGGER.error("One of the given arguments for create is null", e3);
            throw e3;
        }

            LOGGER.debug("{} - {} - {} - {}", ctx, usr, access, auth);

        try {
            basicauth.doAuthentication(auth,ctx);

            checkContextAndSchema(ctx);

            tool.checkCreateUserData(ctx, usr);

            if (tool.existsUserName(ctx, usr.getName())) {
                throw new InvalidDataException("User " + usr.getName() + " already exists in this context");
            }

            // validate email adresss
            tool.primaryMailExists(ctx, usr.getPrimaryEmail());
        } catch (final InvalidDataException e2) {
            LOGGER.error("", e2);
            throw e2;
        } catch (final EnforceableDataObjectException e) {
            LOGGER.error("", e);
            throw new InvalidDataException(e.getMessage());
        }

        final int retval = oxu.create(ctx, usr, access);
        usr.setId(Integer.valueOf(retval));
        final ArrayList<OXUserPluginInterface> interfacelist = new ArrayList<OXUserPluginInterface>();

        // homedirectory
        /*-
         *
        final String homedir = this.prop.getUserProp(AdminProperties.User.HOME_DIR_ROOT, "/home") + "/" + usr.getName();
        if (this.prop.getUserProp(AdminProperties.User.CREATE_HOMEDIRECTORY, false) && !tool.isContextAdmin(ctx, usr.getId().intValue())) {
            if (!new File(homedir).mkdir()) {
                log.error("unable to create directory: {}", homedir);
            }
            final String CHOWN = "/bin/chown";
            final Process p;
            try {
                p = Runtime.getRuntime().exec(new String[] { CHOWN, usr.getName() + ":", homedir });
                p.waitFor();
                if (p.exitValue() != 0) {
                    log.error("{} exited abnormally", CHOWN);
                    final BufferedReader prerr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                    String line = null;
                    while ((line = prerr.readLine()) != null) {
                        log.error(line);
                    }
                    log.error("Unable to chown homedirectory: {}", homedir);
                }
            } catch (final IOException e) {
                log.error("Unable to chown homedirectory: {}", homedir, e);
            } catch (final InterruptedException e) {
                log.error("Unable to chown homedirectory: {}", homedir, e);
            }
        }
        */

        // Trigger plugin extensions
        {
            final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (final OXUserPluginInterface oxuser : pluginInterfaces.getUserPlugins().getServiceList()) {
                    final String bundlename = oxuser.getClass().getName();
                    try {
                        final boolean canHandleContextAdmin = oxuser.canHandleContextAdmin();
                        if (canHandleContextAdmin || (!canHandleContextAdmin && !tool.isContextAdmin(ctx, usr.getId().intValue()))) {
                            try {
                                LOGGER.debug("Calling create for plugin: {}", bundlename);
                                oxuser.create(ctx, usr, access, auth);
                                interfacelist.add(oxuser);
                            } catch (final PluginException e) {
                                LOGGER.error("Error while calling create for plugin: {}", bundlename, e);
                                LOGGER.info("Now doing rollback for everything until now...");
                                for (final OXUserPluginInterface oxuserinterface : interfacelist) {
                                    try {
                                        oxuserinterface.delete(ctx, new User[] { usr }, auth);
                                    } catch (final PluginException e1) {
                                        LOGGER.error("Error doing rollback for plugin: {}", bundlename, e1);
                                    } catch (final RuntimeException e1) {
                                        LOGGER.error("Error doing rollback for plugin: {}", bundlename, e1);
                                    }
                                }
                                try {

                                    oxu.delete(ctx, usr, -1);
                                } catch (final StorageException e1) {
                                    LOGGER.error("Error doing rollback for creating user in database", e1);
                                }
                                throw StorageException.wrapForRMI(e);
                            } catch (final RuntimeException e) {
                                LOGGER.error("Error while calling create for plugin: {}", bundlename, e);
                                LOGGER.info("Now doing rollback for everything until now...");
                                for (final OXUserPluginInterface oxuserinterface : interfacelist) {
                                    try {
                                        oxuserinterface.delete(ctx, new User[] { usr }, auth);
                                    } catch (final PluginException e1) {
                                        LOGGER.error("Error doing rollback for plugin: {}", bundlename, e1);
                                    } catch (final RuntimeException e1) {
                                        LOGGER.error("Error doing rollback for plugin: {}", bundlename, e1);
                                    }
                                }
                                try {
                                    oxu.delete(ctx, usr, -1);
                                } catch (final StorageException e1) {
                                    LOGGER.error("Error doing rollback for creating user in database", e1);
                                }
                                throw StorageException.wrapForRMI(e);
                            }
                        }
                    } catch (final RuntimeException e) {
                        LOGGER.error("Error while calling canHandleContextAdmin for plugin: {}", bundlename, e);
                        LOGGER.info("Now doing rollback for everything until now...");
                        for (final OXUserPluginInterface oxuserinterface : interfacelist) {
                            try {
                                oxuserinterface.delete(ctx, new User[] { usr }, auth);
                            } catch (final PluginException e1) {
                                LOGGER.error("Error doing rollback for plugin: {}", bundlename, e1);
                            } catch (final RuntimeException e1) {
                                LOGGER.error("Error doing rollback for plugin: {}", bundlename, e1);
                            }
                        }
                        try {
                            oxu.delete(ctx, usr, -1);
                        } catch (final StorageException e1) {
                            LOGGER.error("Error doing rollback for creating user in database", e1);
                        }
                        throw StorageException.wrapForRMI(e);
                    }
                }
            }
        }

        // The mail account cache caches resolved imap logins or primary addresses. Creating or changing a user needs the invalidation of
        // that cached data.
        final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);;
        if (null != cacheService) {
            try {
                final Cache mailAccountCache = cacheService.getCache("MailAccount");
                mailAccountCache.remove(cacheService.newCacheKey(ctx.getId().intValue(), String.valueOf(0), String.valueOf(usr.getId())));
                mailAccountCache.remove(cacheService.newCacheKey(ctx.getId().intValue(), String.valueOf(usr.getId())));
                mailAccountCache.invalidateGroup(ctx.getId().toString());

                final Cache globalFolderCache = cacheService.getCache("GlobalFolderCache");
                CacheKey cacheKey = cacheService.newCacheKey(1, "0", Integer.toString(FolderObject.SYSTEM_LDAP_FOLDER_ID));
                globalFolderCache.removeFromGroup(cacheKey, ctx.getId().toString());

                final Cache folderCache = cacheService.getCache("OXFolderCache");
                cacheKey = cacheService.newCacheKey(ctx.getId().intValue(), FolderObject.SYSTEM_LDAP_FOLDER_ID);
                folderCache.remove(cacheKey);
            } catch (final OXException e) {
                LOGGER.error("", e);
            }
        }

        // Return created user
        return usr;
    }

    @Override
    public void delete(final Context ctx, final User user, final Integer destUser, final Credentials auth) throws StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        delete(ctx, new User[] { user }, destUser, auth);
    }

    @Override
    public void delete(final Context ctx, final User[] users, Integer destUser, final Credentials credentials) throws StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;

        try {
            doNullCheck((Object[])users);
        } catch (final InvalidDataException e1) {
            LOGGER.error("One of the given arguments for delete is null", e1);
            throw e1;
        }

        if (users.length == 0) {
            final InvalidDataException e = new InvalidDataException("User array is empty");
            LOGGER.error("", e);
            throw e;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        basicauth.doAuthentication(auth,ctx);

            LOGGER.debug("{} - {} - {}", ctx, Arrays.toString(users), auth);
        checkContextAndSchema(ctx);

        try {
            try {
                setUserIdInArrayOfUsers(ctx, users);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }
            // FIXME: Change function from int to user object
            if (!tool.existsUser(ctx, users)) {
                final NoSuchUserException noSuchUserException = new NoSuchUserException("No such user(s) " + getUserIdArrayFromUsersAsString(users) + " in context " + ctx.getId());
                LOGGER.error("No such user(s) {} in context {}", Arrays.toString(users), ctx.getId(), noSuchUserException);
                throw noSuchUserException;
            }

            Set<Integer> dubCheck = new HashSet<Integer>();
            List<User> filestoreOwners = new java.util.LinkedList<User>();
            for (final User user : users) {
                if (destUser != null && user.getId() == destUser.intValue()) {
                    throw new InvalidDataException("It is not allowed to reassign the shared data to the user which should be deleted. Please choose a different reassign user.");
                }
                if (false == dubCheck.add(user.getId())) {
                    throw new InvalidDataException("User " + user.getId() + " is contained multiple times in delete request.");
                }

                if (tool.isContextAdmin(ctx, user.getId().intValue())) {
                    throw new InvalidDataException("Admin delete not supported");
                }

                if (tool.isMasterFilestoreOwner(ctx, user.getId().intValue())) {
                    Map<Integer, List<Integer>> slaveUsers = tool.fetchSlaveUsersOfMasterFilestore(ctx, user.getId().intValue());
                    if (!slaveUsers.isEmpty()) {
                        String affectedUsers = mapToString(slaveUsers);
                        throw new InvalidDataException("The user " + user.getId() + " is the owner of a master filestore which other users are using. "
                            + "Before deleting this user you must move the filestores of the affected users either to the context filestore, "
                            + "to another master filestore or to a user filestore with the appropriate commandline tools. "
                            + "Affected users are: " + affectedUsers);
                    }

                    filestoreOwners.add(user);
                }

            }
            if (destUser == null) { // Move to ctx store
                for (User filestoreOwner : filestoreOwners) {
                    LOGGER.info("User {} has an individual filestore set. Hence, moving user-associated files to context filestore...", filestoreOwner.getId());
                    moveFromUserToContextFilestore(ctx, filestoreOwner, credentials, true);
                    LOGGER.info("Moved all files from user {} to context filestore.", filestoreOwner.getId());
                }
            } else {
                if (destUser.intValue() > 0) { // Move to master store
                    if (!tool.existsUser(ctx, destUser.intValue())) {
                        throw new InvalidDataException(String.format("The reassign user with id %1$s does not exist in context %2$s. Please choose a different reassign user.", destUser, ctx.getId()));
                    }
                    if (!tool.isMasterFilestoreOwner(ctx, destUser.intValue())) {
                        throw new InvalidDataException(String.format("The reassign user with id %1$s is not an owner of a filestore. Please choose a different reassign user.", destUser, ctx.getId()));
                    }
                    User masterUser = new User(destUser.intValue());
                    for (User filestoreOwner : filestoreOwners) {
                        LOGGER.info("User {} has an individual filestore set. Hence, moving user-associated files to filestore of user {}", filestoreOwner.getId(), masterUser.getId());
                        moveFromUserFilestoreToMaster(ctx, filestoreOwner, masterUser, credentials);
                        LOGGER.info("Moved all files from user {} to context filestore.", filestoreOwner.getId());
                    }
                }
            }
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (RemoteException e) {
            LOGGER.error("", e);
            throw new StorageException(e);
        } catch (NoSuchFilestoreException e) {
            LOGGER.error("", e);
            throw new StorageException(e);
        }


        User[] retusers = oxu.getData(ctx, users);

        final ArrayList<OXUserPluginInterface> interfacelist = new ArrayList<OXUserPluginInterface>();

        // Here we define a list which takes all exceptions which occur during plugin-processing
        // By this we are able to throw all exceptions to the client while concurrently processing all plugins
        final ArrayList<Exception> exceptionlist = new ArrayList<Exception>();

        // Trigger plugin extensions
        {
            final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (final OXUserPluginInterface oxuser : pluginInterfaces.getUserPlugins().getServiceList()) {
                    if (!oxuser.canHandleContextAdmin()) {
                        retusers = removeContextAdmin(ctx, retusers);
                        if (retusers.length > 0) {
                            LOGGER.debug("Calling delete for plugin: {}", oxuser.getClass().getName());
                            final Exception exception = callDeleteForPlugin(ctx, auth, retusers, interfacelist, oxuser.getClass().getName(), oxuser);
                            if (null != exception) {
                                exceptionlist.add(exception);
                            }
                        }
                    } else {
                        LOGGER.debug("Calling delete for plugin: {}", oxuser.getClass().getName());
                        final Exception exception = callDeleteForPlugin(ctx, auth, retusers, interfacelist, oxuser.getClass().getName(), oxuser);
                        if (null != exception) {
                            exceptionlist.add(exception);
                        }
                    }

                }
            }
        }

        /*-
         *
        if (this.prop.getUserProp(AdminProperties.User.CREATE_HOMEDIRECTORY, false)) {
            for(final User usr : users) {
                // homedirectory
                String homedir = this.prop.getUserProp(AdminProperties.User.HOME_DIR_ROOT, "/home");
                homedir += "/" + usr.getName();
                // FIXME: if(! tool.isContextAdmin(ctx, usr.getId()) ) {} ??
                try {
                    FileUtils.deleteDirectory(new File(homedir));
                } catch (final IOException e) {
                    log.error("Could not delete homedir for user: {}", usr);
                }
            }
        }
        */

        oxu.delete(ctx, users, destUser);
        for (final User user : users) {
            try {
                Filestore2UserUtil.removeFilestore2UserEntry(ctx.getId().intValue(), user.getId().intValue(), cache);
            } catch (Exception e) {
                LOGGER.error("Failed to remove filestore2User entry for user {} in context {}", ctx.getId(), user.getId(), e);
            }
        }

        // JCS
        final CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);;
        if (null != cacheService) {
            try {
                final Cache usercCache = cacheService.getCache("User");
                final Cache upCache = cacheService.getCache("UserPermissionBits");
                final Cache ucCache = cacheService.getCache("UserConfiguration");
                final Cache usmCache = cacheService.getCache("UserSettingMail");
                final Cache capabilitiesCache = cacheService.getCache("Capabilities");
                for (final User user : users) {
                    final CacheKey key = cacheService.newCacheKey(i(ctx.getId()), user.getId().intValue());
                    usercCache.remove(key);
                    usercCache.remove(cacheService.newCacheKey(i(ctx.getId()), user.getName()));
                    upCache.remove(key);
                    ucCache.remove(key);
                    usmCache.remove(key);
                    capabilitiesCache.removeFromGroup(user.getId(), ctx.getId().toString());
                    try {
                        UserConfigurationStorage.getInstance().invalidateCache(user.getId().intValue(),
                                new ContextImpl(ctx.getId().intValue()));
                    } catch (final OXException e) {
                        LOGGER.error("Error removing user {} in context {} from configuration storage", user.getId(), ctx.getId(), e);
                    }
                }
            } catch (final OXException e) {
                LOGGER.error("", e);
            }
        }
        // END OF JCS

        if (!exceptionlist.isEmpty()) {
            final StringBuilder sb = new StringBuilder("The following exceptions occured in the plugins: ");
            for (final Exception e : exceptionlist) {
                sb.append(e.toString());
                sb.append('\n');
            }
            throw new StorageException(sb.toString());
        }
    }

    private String mapToString(Map<Integer, List<Integer>> map) {
        StringBuilder builder = new StringBuilder();
        for (Entry<Integer, List<Integer>> cidEntry : map.entrySet()) {
            builder.append("\nCID: ").append(cidEntry.getKey()).append(", User IDs: ");
            List<Integer> ids = cidEntry.getValue();
            for (Integer id : ids) {
                builder.append(id).append(",");
            }
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }

    @Override
    public User getData(final Context ctx, final User user, final Credentials auth) throws StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException {
        return getData(ctx, new User[]{user}, auth)[0];
    }

    @Override
    public User[] getData(final Context ctx, final User[] users, final Credentials credentials) throws StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, NoSuchUserException, DatabaseUpdateException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck((Object[]) users);
        } catch (final InvalidDataException e1) {
            LOGGER.error("One of the given arguments for getData is null", e1);
            throw e1;
        }
        try {
            checkContext(ctx);
            if (users.length <= 0) {
                throw new InvalidDataException();
            }
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        LOGGER.debug("{} - {} - {}", ctx, Arrays.toString(users), auth);
        try {
            // enable check who wants to get data if authentication is enabled
            if (!cache.contextAuthenticationDisabled()) {
                // ok here its possible that a user wants to get his own data
                // SPECIAL USER AUTH CHECK FOR THIS METHOD!
                // check if credentials are from oxadmin or from an user
                // check if given user is not admin, if he is admin, the
                final User authuser = new User();
                authuser.setName(auth.getLogin());
                if( basicauth.isMasterOfContext(auth, ctx) ) {
                    basicauth.doAuthentication(auth, ctx);
                } else if (!tool.isContextAdmin(ctx, authuser)) {
                    final InvalidCredentialsException invalidCredentialsException = new InvalidCredentialsException("Permission denied");
                    if (users.length == 1) {
                        final int auth_user_id = authuser.getId().intValue();
                        basicauth.doUserAuthentication(auth, ctx);
                        // its possible that he wants his own data
                        final Integer userid = users[0].getId();
                        if (userid != null) {
                            if (userid.intValue() != auth_user_id) {
                                throw invalidCredentialsException;
                            }
                        } else {
                            // id not set, try to resolv id by username and then check again
                            final String username = users[0].getName();
                            if (username != null) {
                                final int check_user_id = tool.getUserIDByUsername(ctx, username);
                                if (check_user_id != auth_user_id) {
                                    LOGGER.debug("user[0].getId() does not match id from Credentials.getLogin()");
                                    throw invalidCredentialsException;
                                }
                            } else {
                                LOGGER.debug("Cannot resolv user[0]`s internal id because the username is not set!");
                                throw new InvalidDataException("Username and userid missing.");
                            }
                        }
                    } else {
                        LOGGER.error("User sent {} users to get data for. Only context admin is allowed to do that", Integer.valueOf(users.length), invalidCredentialsException);
                        throw invalidCredentialsException;
                        // one user cannot edit more than his own data
                    }
                } else {
                    basicauth.doAuthentication(auth, ctx);
                }
            } else {
                basicauth.doAuthentication(auth, ctx);
            }

            checkContextAndSchema(ctx);

            for (final User usr : users) {
                final String username = usr.getName();
                final Integer userid = usr.getId();
                if (null != userid && !tool.existsUser(ctx, i(userid))) {
                    if (username != null) {
                        throw new NoSuchUserException("No such user " + username + " in context " + ctx.getId());
                    }
                    throw new NoSuchUserException("No such user " + userid + " in context " + ctx.getId());
                }
                if (null != username && !tool.existsUserName(ctx, username)) {
                    throw new NoSuchUserException("No such user " + username + " in context " + ctx.getId());
                }
                if (username == null && userid == null) {
                    throw new InvalidDataException("Username and userid missing.");
                }
                // ok , try to get the username by id or username
                if (username == null && null != userid) {
                    usr.setName(tool.getUsernameByUserID(ctx, userid.intValue()));
                }
                if (userid == null) {
                    usr.setId(new Integer(tool.getUserIDByUsername(ctx, username)));
                }
            }
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw(e);
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw(e);
        }

        User[] retusers = oxu.getData(ctx, users);

        // Trigger plugin interfaces
        {
            final PluginInterfaces pluginInterfaces = PluginInterfaces.getInstance();
            if (null != pluginInterfaces) {
                for (final OXUserPluginInterface oxuserplugin : pluginInterfaces.getUserPlugins().getServiceList()) {
                    LOGGER.debug("Calling getData for plugin: {}", oxuserplugin.getClass().getName());
                    retusers = oxuserplugin.getData(ctx, retusers, auth);
                }
            }
        }

        LOGGER.debug(Arrays.toString(retusers));
        return retusers;
    }

    @Override
    public UserModuleAccess moduleAccessForName(final String accessCombinationName) {
        if (null == accessCombinationName) {
            return null;
        }
        final UserModuleAccess moduleAccess = cache.getAccessCombinationNames().get(accessCombinationName);
        return null == moduleAccess ? null : moduleAccess.clone();
    }

    @Override
    public UserModuleAccess getModuleAccess(final Context ctx, final User user, final Credentials credentials) throws StorageException, InvalidCredentialsException, NoSuchContextException,InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(user);
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("User object is null");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

            LOGGER.debug("{} - {} - {}", ctx, user, auth);
        try {
            basicauth.doAuthentication(auth, ctx);
            checkContextAndSchema(ctx);
            try {
                setIdOrGetIDFromNameAndIdObject(ctx, user);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }
            final int user_id = user.getId().intValue();
            if (!tool.existsUser(ctx, user_id)) {
                throw new NoSuchUserException("No such user " + user_id + " in context " + ctx.getId());
            }
            return oxu.getModuleAccess(ctx, user_id);
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchUserException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public String getAccessCombinationName(final Context ctx, final User user, final Credentials credentials) throws StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;

        try {
            doNullCheck(user);
        } catch (final InvalidDataException e) {
            final InvalidDataException invalidDataException = new InvalidDataException("User object is null");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

            LOGGER.debug("{} - {} - {}", ctx, user, auth);
        try {
            basicauth.doAuthentication(auth, ctx);
            checkContextAndSchema(ctx);
            try {
                setIdOrGetIDFromNameAndIdObject(ctx, user);
            } catch (NoSuchObjectException e) {
                throw new NoSuchUserException(e);
            }
            final int user_id = user.getId().intValue();

            if (!tool.existsUser(ctx, user_id)) {
                throw new NoSuchUserException("No such user " + user_id + " in context " + ctx.getId());
            }

            return cache.getNameForAccessCombination(oxu.getModuleAccess(ctx, user_id));
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchUserException e) {
            LOGGER.error("", e);
            throw e;
        }


    }

    @Override
    public User[] list(final Context ctx, final String search_pattern, final Credentials credentials) throws StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        return list(ctx, search_pattern, credentials, false, false);
    }

    @Override
    public User[] list(final Context ctx, final String search_pattern, final Credentials credentials, final boolean includeGuests, final boolean excludeUsers) throws StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(ctx,search_pattern);
        } catch (final InvalidDataException e1) {
            LOGGER.error("One of the given arguments for list is null", e1);
            throw e1;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

            LOGGER.debug("{} - {}", ctx, auth);

        basicauth.doAuthentication(auth,ctx);

        checkContextAndSchema(ctx);

        final User[] retval =  oxu.list(ctx, search_pattern, includeGuests, excludeUsers);

        return retval;
    }

    @Override
    public User[] listCaseInsensitive(final Context ctx, final String search_pattern, final Credentials credentials) throws StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        return listCaseInsensitive(ctx, search_pattern, credentials, false, false);
    }

    @Override
    public User[] listCaseInsensitive(final Context ctx, final String search_pattern, final Credentials credentials, final boolean includeGuests, final boolean excludeUsers) throws StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(ctx,search_pattern);
        } catch (final InvalidDataException e1) {
            LOGGER.error("One of the given arguments for list is null", e1);
            throw e1;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

            LOGGER.debug("{} - {}", ctx, auth);

        basicauth.doAuthentication(auth,ctx);

        checkContextAndSchema(ctx);

        final User[] retval =  oxu.listCaseInsensitive(ctx, search_pattern, includeGuests, excludeUsers);

        return retval;
    }

    @Override
    public User[] listUsersWithOwnFilestore(final Context context, final Credentials credentials, final Integer filestore_id) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        final Credentials auth = credentials == null ? new Credentials("", "") : credentials;
        try {
            doNullCheck(context);
        } catch (final InvalidDataException e1) {
            LOGGER.error("One of the given arguments for list is null", e1);
            throw e1;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        LOGGER.debug("{} - {}", context, auth);

        basicauth.doAuthentication(auth, context);

        checkContextAndSchema(context);
        if (null == filestore_id || filestore_id.intValue() <= 0) {
            return oxu.listUsersWithOwnFilestore(context, null);
        } else {
            return oxu.listUsersWithOwnFilestore(context, filestore_id);
        }
    }

    @Override
    public User[] listAll(final Context ctx, final Credentials auth) throws StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        return list(ctx, "*", auth);
    }

    @Override
    public User[] listAll(final Context ctx, final Credentials auth, final boolean includeGuests, final boolean excludeUsers) throws StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException {
        return list(ctx, "*", auth, includeGuests, excludeUsers);
    }

    private Exception callDeleteForPlugin(final Context ctx, final Credentials auth, final User[] retusers, final ArrayList<OXUserPluginInterface> interfacelist, final String bundlename, final OXUserPluginInterface oxuser) {
        try {
            LOGGER.debug("Calling delete for plugin: {}", bundlename);
            oxuser.delete(ctx, retusers, auth);
            interfacelist.add(oxuser);
            return null;
        } catch (final PluginException e) {
            LOGGER.error("Error while calling delete for plugin: {}", bundlename, e);
            return e;
        } catch (final RuntimeException e) {
            LOGGER.error("Error while calling delete for plugin: {}", bundlename, e);
            return e;
        }
    }

    /**
     * checking for some requirements when changing existing user data
     *
     * @param ctx
     * @param newuser
     * @param dbuser
     * @param prop
     * @throws StorageException
     * @throws InvalidDataException
     */
    private void checkChangeUserData(final Context ctx, final User newuser, final User dbuser, final PropertyHandler prop) throws StorageException, InvalidDataException {
        if (newuser.getName() != null) {
            if (usernameIsChangeable()) {
                if (prop.getUserProp(AdminProperties.User.CHECK_NOT_ALLOWED_CHARS, true)) {
                    tool.validateUserName(newuser.getName());
                }
                if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
                    newuser.setName(newuser.getName().toLowerCase());
                }
            }
            // must be loaded additionally because the user loading method gets the new user name passed and therefore does not load the
            // current one.
            final String currentName = tool.getUsernameByUserID(ctx, newuser.getId().intValue());
            if (!newuser.getName().equals(currentName)) {
                if (usernameIsChangeable()) {
                    if (tool.existsUserName(ctx, newuser.getName())) {
                        throw new InvalidDataException("User " + newuser.getName() + " already exists in this context");
                    }
                } else {
                    throw new InvalidDataException("Changing username is disabled!");
                }
            }
        }

        {
            String lang = newuser.getLanguage();
            if (lang != null && lang.indexOf('_') < 0) {
                throw new InvalidDataException("Language must contain an underscore, e.g. en_US.");
            }
        }

        String newDefaultSenderAddress = newuser.getDefaultSenderAddress();
        String newPrimaryEmail = newuser.getPrimaryEmail();
        String newEmail1 = newuser.getEmail1();
        boolean mailCheckNeeded = (null != newDefaultSenderAddress) || (null != newPrimaryEmail) || (null != newEmail1) || (null != newuser.getAliases());

        if (prop.getUserProp(AdminProperties.User.PRIMARY_MAIL_UNCHANGEABLE, true)) {
            if (newPrimaryEmail != null && !newPrimaryEmail.equalsIgnoreCase(dbuser.getPrimaryEmail())) {
                throw new InvalidDataException("primary mail must not be changed");
            }
        }

        GenericChecks.checkChangeValidPasswordMech(newuser);

        // if no password mech supplied, use the old one as set in db
        if (newuser.getPasswordMech() == null) {
            newuser.setPasswordMech(dbuser.getPasswordMech());
        }

        if (mailCheckNeeded && !tool.isContextAdmin(ctx, newuser.getId().intValue())) {
            // checks below throw InvalidDataException
            tool.checkValidEmailsInUserObject(newuser);
            Set<String> useraliases = newuser.getAliases();
            if (useraliases == null) {
                useraliases = dbuser.getAliases();
            }
            if (null != useraliases) {
                Set<String> tmp = new HashSet<String>(useraliases.size());
                for (String email : useraliases) {
                    tmp.add(IDNA.toIDN(email));
                }
                useraliases = tmp;
            } else {
                useraliases = new HashSet<String>(1);
            }

            if (newPrimaryEmail != null && newEmail1 != null && !newPrimaryEmail.equalsIgnoreCase(newEmail1)) {
                // primary mail value must be same with email1
                throw new InvalidDataException("email1 not equal with primarymail!");
            }

            String check_primary_mail;
            String check_email1;
            String check_default_sender_address;
            if (newPrimaryEmail != null) {
                check_primary_mail = IDNA.toIDN(newPrimaryEmail);
                if (!newPrimaryEmail.equalsIgnoreCase(dbuser.getPrimaryEmail())) {
                    tool.primaryMailExists(ctx, newPrimaryEmail);
                }
            } else {
                final String email = dbuser.getPrimaryEmail();
                check_primary_mail = email == null ? email : IDNA.toIDN(email);
            }

            if (newEmail1 != null) {
                check_email1 = IDNA.toIDN(newEmail1);
            } else {
                final String s = dbuser.getEmail1();
                check_email1 = s == null ? s : IDNA.toIDN(s);
            }
            if (newDefaultSenderAddress != null) {
                check_default_sender_address = IDNA.toIDN(newDefaultSenderAddress);
            } else {
                final String s = dbuser.getDefaultSenderAddress();
                check_default_sender_address = s == null ? s : IDNA.toIDN(s);
            }

            final boolean found_primary_mail = useraliases.contains(check_primary_mail);
            final boolean found_email1 = useraliases.contains(check_email1);
            final boolean found_default_sender_address = useraliases.contains(check_default_sender_address);

            if (!found_primary_mail || !found_email1 || !found_default_sender_address) {
                throw new InvalidDataException("primaryMail, Email1 and defaultSenderAddress must be present in set of aliases.");
            }
            // added "usrdata.getPrimaryEmail() != null" for this check, else we cannot update user data without mail data
            // which is not very good when just changing the displayname for example
            if (newPrimaryEmail != null && newEmail1 == null) {
                throw new InvalidDataException("email1 not sent but required!");

            }
        }


        // TODO mail checks
    }

    private static void checkContext(final Context ctx) throws InvalidDataException {
        if (null == ctx || null == ctx.getId()) {
            throw new InvalidDataException("Context invalid");
        }
        // Check a context existence is considered as a security flaw
    }

    private String getUserIdArrayFromUsersAsString(final User[] users) throws InvalidDataException {
        if (null == users) {
            return null;
        } else if (users.length == 0) {
            return "";
        }
        final StringBuilder sb = new StringBuilder(users.length * 8);
        {
            final Integer id = users[0].getId();
            if (null == id) {
                throw new InvalidDataException("One user object has no id");
            }
            sb.append(id);
        }
        for (int i = 1; i < users.length; i++) {
            final Integer id = users[i].getId();
            if (null == id) {
                throw new InvalidDataException("One user object has no id");
            }
            sb.append(',');
            sb.append(id);
        }
        return sb.toString();
    }

    private User[] removeContextAdmin(final Context ctx, final User[] retusers) throws StorageException {
        final ArrayList<User> list = new ArrayList<User>(retusers.length);
        for (final User user : retusers) {
            if (!tool.isContextAdmin(ctx, user.getId().intValue())) {
                list.add(user);
            }
        }
        return list.toArray(new User[list.size()]);
    }

    @Override
    public void changeModuleAccessGlobal(final String filter, final UserModuleAccess addAccess, final UserModuleAccess removeAccess, final Credentials credentials) throws RemoteException, InvalidCredentialsException, StorageException, InvalidDataException {
        final Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(addAccess, removeAccess);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("Some parameters are null");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        try {
            checkForGABRestriction(addAccess);
            checkForGABRestriction(removeAccess);
        } catch (final InvalidDataException e1) {
            final InvalidDataException invalidDataException = new InvalidDataException("\"GlobalAddressBookDisabled\" can not be changed with this method.");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        LOGGER.debug("{} - {} - {} - {}", filter, addAccess, removeAccess, auth);

        try {
            basicauth.doAuthentication(auth);
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        }

        int permissionBits = -1;
        if (filter != null) {
            try {
                permissionBits = Integer.parseInt(filter);
            } catch (final NumberFormatException nfe) {
                final UserModuleAccess namedAccessCombination = cache.getNamedAccessCombination(filter);
                if (namedAccessCombination == null) {
                    throw new InvalidDataException("No such access combination name \"" + filter.trim() + "\"");
                }
                permissionBits = getPermissionBits(namedAccessCombination);
            }
        }

        final int addBits = getPermissionBits(addAccess);
        final int removeBits = getPermissionBits(removeAccess);
            LOGGER.debug("Adding {} removing {} to filter {}", addBits, removeBits, filter);

        try {
            tool.changeAccessCombination(permissionBits, addBits, removeBits);
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        }

        // TODO: How to notify via EventSystemService ?
    }

    /**
     * Checks for valid Module Accesses.
     * @param addAccess
     * @throws InvalidDataException
     */
    private void checkForGABRestriction(UserModuleAccess access) throws InvalidDataException {
        if (access.isGlobalAddressBookDisabled()) {
            throw new InvalidDataException("Can not change the value for \"access-global-address-book-disabled\".");
        }
    }

    @Override
    public boolean exists(Context ctx, User user, Credentials credentials) throws RemoteException, InvalidDataException, InvalidCredentialsException, StorageException, DatabaseUpdateException, NoSuchContextException {
        Credentials auth = credentials == null ? new Credentials("","") : credentials;
        try {
            doNullCheck(user);
        } catch (final InvalidDataException e2) {
            final InvalidDataException invalidDataException = new InvalidDataException("One of the given arguments for change is null");
            LOGGER.error("", invalidDataException);
            throw invalidDataException;
        }

        if (prop.getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            auth.setLogin(auth.getLogin().toLowerCase());
        }

        try {
            basicauth.doAuthentication(auth, ctx);
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        }

        try {
            contextcheck(ctx);

            checkContextAndSchema(ctx);

            if (null != user.getId()) {
                return tool.existsUser(ctx, user);
            } else if (null != user.getName()) {
                return tool.existsUserName(ctx, user.getName());
            } else if (null != user.getDisplay_name()) {
                return tool.existsDisplayName(ctx, user.getDisplay_name());
            } else {
                throw new InvalidDataException("Neither identifier, name nor display name set in given user object");
            }
        } catch (InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (DatabaseUpdateException e) {
            LOGGER.error("", e);
            throw e;
        } catch (NoSuchContextException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    public int getPermissionBits(UserModuleAccess namedAccessCombination) {
        int retval = 0;

        if (namedAccessCombination.isActiveSync()) {
            retval |= UserConfiguration.ACTIVE_SYNC;
        }
        if (namedAccessCombination.getCalendar()) {
            retval |= UserConfiguration.CALENDAR;
        }
        if (namedAccessCombination.isCollectEmailAddresses()) {
            retval |= UserConfiguration.COLLECT_EMAIL_ADDRESSES;
        }
        if (namedAccessCombination.getContacts()) {
            retval |= UserConfiguration.CONTACTS;
        }
        if (namedAccessCombination.getDelegateTask()) {
            retval |= UserConfiguration.DELEGATE_TASKS;
        }
        if (namedAccessCombination.getEditGroup()) {
            retval |= UserConfiguration.EDIT_GROUP;
        }
        if (namedAccessCombination.getEditPassword()) {
            retval |= UserConfiguration.EDIT_PASSWORD;
        }
        if (namedAccessCombination.getEditPublicFolders()) {
            retval |= UserConfiguration.EDIT_PUBLIC_FOLDERS;
        }
        if (namedAccessCombination.getEditResource()) {
            retval |= UserConfiguration.EDIT_RESOURCE;
        }
        if (namedAccessCombination.getIcal()) {
            retval |= UserConfiguration.ICAL;
        }
        if (namedAccessCombination.getInfostore()) {
            retval |= UserConfiguration.INFOSTORE;
        }
        if (namedAccessCombination.getSyncml()) {
            retval |= UserConfiguration.MOBILITY;
        }
        if (namedAccessCombination.isMultipleMailAccounts()) {
            retval |= UserConfiguration.MULTIPLE_MAIL_ACCOUNTS;
        }
        if (namedAccessCombination.isOLOX20()) {
            retval |= UserConfiguration.OLOX20;
        }
        if (namedAccessCombination.isPublication()) {
            retval |= UserConfiguration.PUBLICATION;
        }
        if (namedAccessCombination.getReadCreateSharedFolders()) {
            retval |= UserConfiguration.READ_CREATE_SHARED_FOLDERS;
        }
        if (namedAccessCombination.isSubscription()) {
            retval |= UserConfiguration.SUBSCRIPTION;
        }
        if (namedAccessCombination.getTasks()) {
            retval |= UserConfiguration.TASKS;
        }
        if (namedAccessCombination.isUSM()) {
            retval |= UserConfiguration.USM;
        }
        if (namedAccessCombination.getVcard()) {
            retval |= UserConfiguration.VCARD;
        }
        if (namedAccessCombination.getWebdav()) {
            retval |= UserConfiguration.WEBDAV;
        }
        if (namedAccessCombination.getWebdavXml()) {
            retval |= UserConfiguration.WEBDAV_XML;
        }
        if (namedAccessCombination.getWebmail()) {
            retval |= UserConfiguration.WEBMAIL;
        }

        return retval;
    }

    /**
     * Property name black list REGEX. Taken from the oxsysreport
     */
    private static final Pattern PROPERTY_BLACK_LIST = Pattern.compile("[pP]assword[[:blank:]]*|[sS]ecret[[:blank:]]*|[kK]ey[[:blank:]]*|secretSource[[:blank:]]*|secretRandom[[:blank:]]*|[sS]alt[[:blank:]]*|SSLKey(Pass|Name)[[:blank:]]*|[lL]ogin[[:blank:]]*");

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public List<UserProperty> getUserConfigurationSource(final Context ctx, final User user, final String searchPattern, final Credentials credentials) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException, NoSuchUserException {
        if (user == null) {
            throw new InvalidDataException("Invalid user id.");
        }
        if (ctx == null) {
            throw new InvalidDataException("Invalid context id.");
        }

        List<UserProperty> userProperties = new ArrayList<UserProperty>();

        Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        try {
            basicauth.doAuthentication(auth, ctx);
            contextcheck(ctx);
            final int user_id = user.getId().intValue();
            if (!tool.existsUser(ctx, user_id)) {
                throw new NoSuchUserException("No such user " + user_id + " in context " + ctx.getId());
            }

            final CapabilityService capabilityService = AdminServiceRegistry.getInstance().getService(CapabilityService.class);
            if (capabilityService == null) {
                LOGGER.warn("CapabilityService absent. Unable to retrieve user configuration.");
                return userProperties;
            }
            List<ConfigurationProperty> capabilitiesSource = capabilityService.getConfigurationSource(user_id, ctx.getId().intValue(), searchPattern);

            for (ConfigurationProperty property: capabilitiesSource) {
                Matcher m = PROPERTY_BLACK_LIST.matcher(property.getName());
                String value = m.find() ? "<OBFUSCATED>" : property.getValue();
                userProperties.add(new UserProperty(property.getScope(), property.getName(), value));
            }

            Collections.sort(userProperties, new OXUserPropertySorter());

            return userProperties;
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchUserException e) {
            LOGGER.error("", e);
            throw e;
        } catch (OXException e) {
            LOGGER.error("Error retrieving configuration source for user {} in context {}.",user.getId().intValue(), ctx.getId(), e);
        }
        return userProperties;
    }

    /**
     *
     * {@inheritDoc}
     * @throws InvalidDataException
     */
    @Override
    public Map<String, Map<String, Set<String>>> getUserCapabilitiesSource(Context ctx, User user, Credentials credentials) throws RemoteException, InvalidDataException, StorageException, InvalidCredentialsException, NoSuchUserException {
        if (user == null) {
            throw new InvalidDataException("Invalid user id.");
        }
        if (ctx == null) {
            throw new InvalidDataException("Invalid context id.");
        }

        Map<String, Map<String, Set<String>>> capabilitiesSource = new HashMap<String, Map<String, Set<String>>>();

        Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        try {
            basicauth.doAuthentication(auth, ctx);
            contextcheck(ctx);
            final int user_id = user.getId().intValue();
            if (!tool.existsUser(ctx, user_id)) {
                throw new NoSuchUserException("No such user " + user_id + " in context " + ctx.getId());
            }

            final CapabilityService capabilityService = AdminServiceRegistry.getInstance().getService(CapabilityService.class);
            if (capabilityService == null) {
                LOGGER.warn("CapabilityService absent. Unable to retrieve user configuration.");
                return capabilitiesSource;
            }
            capabilitiesSource = capabilityService.getCapabilitiesSource(user_id, ctx.getId());

        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final NoSuchUserException e) {
            LOGGER.error("", e);
            throw e;
        } catch (OXException e) {
            LOGGER.error("Error retrieving configuration source for user {} in context {}.",user.getId().intValue(), ctx.getId(), e);
        }
        return capabilitiesSource;
    }

    @Override
    public User[] listByAliasDomain(Context context, String aliasDomain, Credentials credentials) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException {
        if (aliasDomain == null) {
            throw new InvalidDataException("Invalid alias domain");
        }
        if (context == null) {
            throw new InvalidDataException("Invalid context id.");
        }

        Credentials auth = credentials == null ? new Credentials("", "") : credentials;

        try {
            basicauth.doAuthentication(auth, context);
            contextcheck(context);
            UserAliasStorage uas = AdminServiceRegistry.getInstance().getService(UserAliasStorage.class, true);

            List<Integer> ids = uas.getUserIdsByAliasDomain(context.getId(), aliasDomain);

            ArrayList<User> users = new ArrayList<User>(ids.size());
            for (int id : ids) {
                users.add(new User(id));
            }
            return users.toArray(new User[ids.size()]);
        } catch (OXException e) {
            LOGGER.error("", e);
            throw new StorageException(e);
        } catch (final StorageException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidDataException e) {
            LOGGER.error("", e);
            throw e;
        } catch (final InvalidCredentialsException e) {
            LOGGER.error("", e);
            throw e;
        }
    }

    @Override
    public void delete(Context ctx, User[] users, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        delete(ctx, users, null, auth);
    }

    @Override
    public void delete(Context ctx, User user, Credentials auth) throws RemoteException, StorageException, InvalidCredentialsException, NoSuchContextException, InvalidDataException, DatabaseUpdateException, NoSuchUserException {
        delete(ctx, user, null, auth);
    }
}

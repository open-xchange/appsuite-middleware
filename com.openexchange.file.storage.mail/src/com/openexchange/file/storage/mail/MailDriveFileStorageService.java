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

package com.openexchange.file.storage.mail;

import static com.openexchange.java.Autoboxing.I;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.ReadOnlyDynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.RootFolderPermissionsAware;
import com.openexchange.file.storage.mail.osgi.Services;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailDriveFileStorageService} - The Mail Drive file storage service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public final class MailDriveFileStorageService implements AccountAware, RootFolderPermissionsAware {

    private static final String SERVICE_ID = MailDriveConstants.ID;

    /**
     * Creates a new Mail Drive file storage service.
     *
     * @return A new Mail Drive file storage service
     */
    public static MailDriveFileStorageService newInstance() {
        return new MailDriveFileStorageService();
    }

    private final DynamicFormDescription formDescription;
    private final FileStorageAccountManager accountManager;

    /**
     * Initializes a new {@link MailDriveFileStorageService}.
     */
    private MailDriveFileStorageService() {
        super();
        DynamicFormDescription tmpDescription = new DynamicFormDescription();
        formDescription = new ReadOnlyDynamicFormDescription(tmpDescription);
        accountManager = new MailDriveFileStorageAccountManager(this);
    }

    // --------------------------------------------------------------------------------------------------------------------------------- //

    @Override
    public String getId() {
        return SERVICE_ID;
    }

    @Override
    public String getDisplayName() {
        return "Mail Drive File Storage Service";
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    @Override
    public Set<String> getSecretProperties() {
        return Collections.emptySet();
    }

    @Override
    public List<FileStoragePermission> getRootFolderPermissions(String accountId, Session session) throws OXException {
        if (!MailDriveConstants.ACCOUNT_ID.equals(accountId)) {
            throw FileStorageExceptionCodes.ACCOUNT_NOT_FOUND.create(accountId, MailDriveConstants.ID, I(session.getUserId()), I(session.getContextId()));
        }
        DefaultFileStoragePermission p = DefaultFileStoragePermission.newInstance();
        p.setEntity(session.getUserId());
        p.setAdmin(false);
        p.setAllPermissions(FileStoragePermission.READ_FOLDER, FileStoragePermission.READ_ALL_OBJECTS, FileStoragePermission.NO_PERMISSIONS, FileStoragePermission.NO_PERMISSIONS);
        return Collections.<FileStoragePermission> singletonList(p);
    }

    private UserPermissionBits getUserPermissionBits(Session session, int userId, int contextId) throws OXException {
        if (session instanceof ServerSession) {
            ServerSession serverSession = (ServerSession) session;
            return serverSession.getUserPermissionBits();
        }

        return UserPermissionBitsStorage.getInstance().getUserPermissionBits(userId, contextId);
    }

    /**
     * Checks if session-associated user has Mail Drive access.
     *
     * @param session The session
     * @return <code>true</code> if Mail Drive is accessible; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public boolean hasMailDriveAccess(Session session) throws OXException {
        return getUserPermissionBits(session, session.getUserId(), session.getContextId()).hasWebMail() && isEnabledFor(session);
    }

    /**
     * Checks if session-associated user has Mail Drive access.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if Mail Drive is accessible; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public boolean hasMailDriveAccess(int userId, int contextId) throws OXException {
        return getUserPermissionBits(null, userId, contextId).hasWebMail() && isEnabledFor(userId, contextId);
    }

    /**
     * Gets all service's accounts associated with session user.
     *
     * @param session The session providing needed user data
     * @return All accounts associated with session user.
     * @throws OXException If listing fails
     */
    @Override
    public List<FileStorageAccount> getAccounts(Session session) throws OXException {
        if (!hasMailDriveAccess(session)) {
            return Collections.emptyList();
        }

        return Arrays.<FileStorageAccount> asList(new MailDriveFileStorageAccount(this, session));
    }

    @Override
    public FileStorageAccountManager getAccountManager() throws OXException {
        return accountManager;
    }

    @Override
    public MailDriveAccountAccess getAccountAccess(String accountId, Session session) throws OXException {
        return new MailDriveAccountAccess(getFullNameCollectionFor(session), this, session);
    }

    /**
     * Gets the collection of full names for virtual attachment folders associated with specified session.
     *
     * @param session The session providing user data
     * @return The collection of full names for virtual attachment folders
     * @throws OXException If collection of full names for virtual attachment folders cannot be returned
     */
    public FullNameCollection getFullNameCollectionFor(Session session) throws OXException {
        return getFullNameCollectionFor(session.getUserId(), session.getContextId());
    }

    /**
     * Gets the collection of full names for virtual attachment folders associated with specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The collection of full names for virtual attachment folders
     * @throws OXException If collection of full names for virtual attachment folders cannot be returned
     */
    public FullNameCollection getFullNameCollectionFor(int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = Services.getOptionalService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = viewFactory.getView(userId, contextId);

        boolean requireFullNames = false;
        String fullNameAll;
        {
            String propName = "com.openexchange.file.storage.mail.fullNameAll";
            ComposedConfigProperty<String> propertyAll = view.property(propName, String.class);
            if (!propertyAll.isDefined() || Strings.isEmpty((fullNameAll = propertyAll.get()))) {
                if (requireFullNames) {
                    throw FileStorageExceptionCodes.MISSING_CONFIG.create(propName, MailDriveConstants.ACCOUNT_ID);
                }
                fullNameAll = null;
            }
        }

        String fullNameReceived;
        {
            String propName = "com.openexchange.file.storage.mail.fullNameReceived";
            ComposedConfigProperty<String> propertyReceived = view.property(propName, String.class);
            if (!propertyReceived.isDefined() || Strings.isEmpty((fullNameReceived = propertyReceived.get()))) {
                if (requireFullNames) {
                    throw FileStorageExceptionCodes.MISSING_CONFIG.create(propName, MailDriveConstants.ACCOUNT_ID);
                }
                fullNameReceived = null;
            }
        }

        String fullNameSent;
        {
            String propName = "com.openexchange.file.storage.mail.fullNameSent";
            ComposedConfigProperty<String> propertySent = view.property(propName, String.class);
            if (!propertySent.isDefined() || Strings.isEmpty((fullNameSent = propertySent.get()))) {
                if (requireFullNames) {
                    throw FileStorageExceptionCodes.MISSING_CONFIG.create(propName, MailDriveConstants.ACCOUNT_ID);
                }
                fullNameSent = null;
            }
        }

        return new FullNameCollection(null == fullNameAll ? null : fullNameAll.trim(), null == fullNameReceived ? null : fullNameReceived.trim(), null == fullNameSent ? null : fullNameSent.trim());
    }

    /**
     * Checks if Mail Drive is enabled for session-associated user.
     *
     * @param session The session providing user data
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public boolean isEnabledFor(Session session) throws OXException {
        return isEnabledFor(session.getUserId(), session.getContextId());
    }

    /**
     * Checks if Mail Drive is enabled for specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if enabled; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public boolean isEnabledFor(int userId, int contextId) throws OXException {
        ConfigViewFactory viewFactory = Services.getOptionalService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = viewFactory.getView(userId, contextId);
        String propName = "com.openexchange.file.storage.mail.enabled";
        ComposedConfigProperty<Boolean> property = view.property(propName, Boolean.class);

        return property.isDefined() && property.get().booleanValue();
    }

}

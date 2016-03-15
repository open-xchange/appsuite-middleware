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

package com.openexchange.file.storage.mail;

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
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.mail.osgi.Services;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailDriveFileStorageService} - The Mail Drive file storage service.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailDriveFileStorageService implements AccountAware {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailDriveFileStorageService.class);

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

    private UserPermissionBits getUserPermissionBits(Session session) throws OXException {
        if (session instanceof ServerSession) {
            ServerSession serverSession = (ServerSession) session;
            return serverSession.getUserPermissionBits();
        }

        return UserPermissionBitsStorage.getInstance().getUserPermissionBits(session.getUserId(), session.getContextId());
    }

    /**
     * Checks if session-associated user has Mail Drive access.
     *
     * @param session The session
     * @return <code>true</code> if Mail Drive is accessible; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public boolean hasMailDriveAccess(Session session) throws OXException {
        return getUserPermissionBits(session).hasWebMail() && isEnabledFor(session);
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

        return Arrays.<FileStorageAccount> asList(new MailDriveFileStorageAccount(this));
    }

    @Override
    public FileStorageAccountManager getAccountManager() throws OXException {
        return accountManager;
    }

    @Override
    public FileStorageAccountAccess getAccountAccess(final String accountId, final Session session) throws OXException {
        return new MailDriveAccountAccess(getFullNameCollectionFor(session), this, session);
    }

    private FullNameCollection getFullNameCollectionFor(Session session) throws OXException {
        ConfigViewFactory viewFactory = Services.getOptionalService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        String propName = "com.openexchange.file.storage.mail.fullNameAll";
        ComposedConfigProperty<String> propertyAll = view.property(propName, String.class);
        if (propertyAll.isDefined()) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create(propName, MailDriveConstants.ACCOUNT_DISPLAY_NAME);
        }

        propName = "com.openexchange.file.storage.mail.fullNameReceived";
        ComposedConfigProperty<String> propertyReceived = view.property(propName, String.class);
        if (propertyReceived.isDefined()) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create(propName, MailDriveConstants.ACCOUNT_DISPLAY_NAME);
        }

        propName = "com.openexchange.file.storage.mail.fullNameSent";
        ComposedConfigProperty<String> propertySent = view.property(propName, String.class);
        if (propertySent.isDefined()) {
            throw FileStorageExceptionCodes.MISSING_CONFIG.create(propName, MailDriveConstants.ACCOUNT_DISPLAY_NAME);
        }

        return new FullNameCollection(propertyAll.get(), propertyReceived.get(), propertySent.get());
    }

    private boolean isEnabledFor(Session session) throws OXException {
        ConfigViewFactory viewFactory = Services.getOptionalService(ConfigViewFactory.class);
        if (null == viewFactory) {
            throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
        }

        ConfigView view = viewFactory.getView(session.getUserId(), session.getContextId());
        String propName = "com.openexchange.file.storage.mail.enabled";
        ComposedConfigProperty<Boolean> property = view.property(propName, Boolean.class);

        return property.isDefined() && property.get().booleanValue();
    }

}

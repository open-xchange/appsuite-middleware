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

package com.openexchange.file.storage.xctx;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.annotation.NonNull;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.capabilities.CapabilitySet;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.ReadOnlyDynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.LoginAwareFileStorageServiceExtension;
import com.openexchange.file.storage.SharingFileStorageService;
import com.openexchange.groupware.modules.Module;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;

/**
 * {@link XctxFileStorageService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class XctxFileStorageService implements FileStorageService, AccountAware, SharingFileStorageService, LoginAwareFileStorageServiceExtension {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(XctxFileStorageService.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link XctxFileStorageService}
     * 
     * @param services A service lookup reference
     */
    public XctxFileStorageService(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public List<FileStorageAccount> getAccounts(Session session) throws OXException {
        return getAccountManager().getAccounts(session);
    }

    @NonNull
    @Override
    public String getId() {
        return "xctx" + Module.INFOSTORE.getFolderConstant();
    }

    @Override
    public String getDisplayName() {
        return "Cross-Context";
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return new ReadOnlyDynamicFormDescription(new DynamicFormDescription()
            .add(FormElement.custom("url", "url", "Share Link:", true, ""))
            .add(FormElement.custom("password", "password", "Password:"))
        );
    }

    @Override
    public Set<String> getSecretProperties() {
        return Collections.singleton("password");
    }

    @Override
    public boolean hasCapability(Session session) {
        try {
            CapabilitySet capabilities = services.getServiceSafe(CapabilityService.class).getCapabilities(session);
            return capabilities.contains("filestorage_xctx");
        } catch (OXException e) {
            LOGGER.error("Unable to get capability", e);
        }
        return false;
    }

    @Override
    public FileStorageAccountManager getAccountManager() throws OXException {
        return services.getServiceSafe(FileStorageAccountManagerLookupService.class).getAccountManagerFor(getId());
    }

    @Override
    public FileStorageAccountAccess getAccountAccess(String accountId, Session session) throws OXException {
        assertCapability(session);
        FileStorageAccount account = getAccountManager().getAccount(accountId, session);
        return new XctxAccountAccess(services, account, session);
    }

    @Override
    public void testConnection(FileStorageAccount account, Session session) throws OXException {
        assertCapability(session);
        XctxAccountAccess accountAccess = new XctxAccountAccess(services, account, session);
        accountAccess.connect();
        accountAccess.close();
    }

    /**
     * Checks if the given session has the appropriate capability for using this file storage
     *
     * @param session The session to check
     * @throws OXException in case the session does not have the appropriated capability to use this file storage
     */
    private void assertCapability(Session session) throws OXException {
        if (!hasCapability(session)) {
            throw ShareExceptionCodes.NO_SUBSCRIBE_SHARE_PERMISSION.create();
        }
    }

}

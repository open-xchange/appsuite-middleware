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

package com.openexchange.file.storage.oauth;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.oauth.OAuthUtil;
import com.openexchange.oauth.association.OAuthAccountAssociation;
import com.openexchange.oauth.association.spi.OAuthAccountAssociationProvider;
import com.openexchange.session.Session;

/**
 * {@link AbstractFileStorageOAuthAccountAssociationProvider}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractFileStorageOAuthAccountAssociationProvider implements OAuthAccountAssociationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFileStorageOAuthAccountAssociationProvider.class);

    private final AbstractOAuthFileStorageService storageService;

    /**
     * Initialises a new {@link AbstractFileStorageOAuthAccountAssociationProvider}.
     */
    public AbstractFileStorageOAuthAccountAssociationProvider(AbstractOAuthFileStorageService filestorageService) {
        super();
        this.storageService = filestorageService;
    }

    @Override
    public Collection<OAuthAccountAssociation> getAssociationsFor(int accountId, Session session) throws OXException {
        Collection<OAuthAccountAssociation> associations = null;
        List<FileStorageAccount> accounts = storageService.getAccounts(session);
        for (FileStorageAccount fileStorageAccount : accounts) {
            try {
                if (OAuthUtil.getAccountId(fileStorageAccount.getConfiguration()) != accountId) {
                    continue;
                }
            } catch (IllegalArgumentException e) {
                LOGGER.debug("No association found between file storage account {} and oauth account {} for user {} in context {}.", fileStorageAccount.getId(), I(accountId), I(session.getUserId()), I(session.getContextId()), e);
                continue;
            }
            if (null == associations) {
                associations = new LinkedList<>();
            }
            associations.add(createAssociation(accountId, fileStorageAccount, session.getUserId(), session.getContextId()));
        }

        return null == associations ? Collections.<OAuthAccountAssociation> emptyList() : associations;
    }

    /**
     * Creates a new {@link OAuthAccountAssociation} for the specified {@link FileStorageAccount}
     *
     * @param accountId The OAuthAccount identifier
     * @param account The file storage account
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public abstract OAuthAccountAssociation createAssociation(int accountId, FileStorageAccount account, int userId, int contextId);
}

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

package com.openexchange.file.storage.onedrive.oauth;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.onedrive.OneDriveFileStorageService;
import com.openexchange.oauth.association.OAuthAccountAssociation;
import com.openexchange.oauth.association.spi.OAuthAccountAssociationProvider;
import com.openexchange.session.Session;

/**
 * {@link OneDriveOAuthAccountAssociationProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class OneDriveOAuthAccountAssociationProvider implements OAuthAccountAssociationProvider {

    private final OneDriveFileStorageService storageService;

    /**
     * Initializes a new {@link OneDriveOAuthAccountAssociationProvider}.
     */
    public OneDriveOAuthAccountAssociationProvider(OneDriveFileStorageService storageService) {
        super();
        this.storageService = storageService;
    }

    @Override
    public Collection<OAuthAccountAssociation> getAssociationsFor(int accountId, Session session) throws OXException {
        Collection<OAuthAccountAssociation> associations = null;
        List<FileStorageAccount> accounts = storageService.getAccounts(session);
        for (FileStorageAccount fileStorageAccount : accounts) {
            Map<String, Object> configuration = fileStorageAccount.getConfiguration();
            if (getAccountId(configuration) == accountId) {
                if (null == associations) {
                    associations = new LinkedList<>();
                }
                associations.add(new OneDriveOAuthAccountAssociation(accountId, fileStorageAccount, session.getUserId(), session.getContextId()));
            }
        }

        return null == associations ? Collections.<OAuthAccountAssociation> emptyList() : associations;
    }

    /**
     * Returns the OAuth account identifier from associated account's configuration
     *
     * @param configuration The configuration
     * @return The account identifier or <code>-1</code> if account identifier cannot be determined
     */
    private int getAccountId(Map<String, Object> configuration) {
        if (null == configuration) {
            return -1;
        }

        Object accountId = configuration.get("account");
        if (null == accountId) {return -1;
        }

        if (accountId instanceof Integer) {
            return ((Integer) accountId).intValue();
        }

        try {
            return Integer.parseInt(accountId.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The account identifier '" + accountId.toString() + "' cannot be parsed as an integer.", e);
        }
    }

}

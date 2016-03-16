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

package com.openexchange.file.storage.infostore.internal;

import java.util.Collections;
import java.util.Set;
import com.openexchange.context.ContextService;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AdministrativeFileStorageFileAccess;
import com.openexchange.file.storage.AdministrativeFileStorageService;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.infostore.internal.AdministrativeInfostoreFileAccess;
import com.openexchange.file.storage.infostore.osgi.Services;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.session.Session;

/**
 * {@link InfostoreFileStorageService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreFileStorageService implements AdministrativeFileStorageService {

    private InfostoreFacade infostore;

    private InfostoreSearchEngine search;

    @Override
    public FileStorageAccountAccess getAccountAccess(final String accountId, final Session session) throws OXException {
        if (!accountId.equals(InfostoreDefaultAccountManager.DEFAULT_ID)) {
            throw FileStorageExceptionCodes.ACCOUNT_NOT_FOUND.create(accountId, getId(), Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()));
        }
        return new InfostoreAccountAccess(session, this);
    }

    @Override
    public FileStorageAccountManager getAccountManager() {
        return new InfostoreDefaultAccountManager(this);
    }

    @Override
    public String getDisplayName() {
        return "Standard Infostore";
    }

    @Override
    public String getId() {
        return "com.openexchange.infostore";
    }

    @Override
    public Set<String> getSecretProperties() {
        return Collections.emptySet();
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return new DynamicFormDescription();
    }

    // Override this for OSGi Lookup, use the setter in tests
    public InfostoreFacade getInfostore() {
        return infostore;
    }

    public void setInfostore(final InfostoreFacade infostore) {
        this.infostore = infostore;
    }

    // Override this for OSGi Lookup.
    public InfostoreSearchEngine getSearch() {
        return search;
    }

    public void setSearch(final InfostoreSearchEngine search) {
        this.search = search;
    }

    @Override
    public AdministrativeFileStorageFileAccess getAdministrativeFileAccess(String accountId, int contextId) throws OXException {
        if (!accountId.equals(InfostoreDefaultAccountManager.DEFAULT_ID)) {
            throw FileStorageExceptionCodes.ACCOUNT_NOT_FOUND.create(accountId, getId(), "admin", contextId);
        }
        return new AdministrativeInfostoreFileAccess(getInfostore(), Services.getService(ContextService.class).getContext(contextId));
    }

}

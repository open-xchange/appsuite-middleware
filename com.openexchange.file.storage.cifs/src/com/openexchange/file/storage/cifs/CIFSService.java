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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.file.storage.cifs;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.ReadOnlyDynamicFormDescription;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.session.Session;

/**
 * {@link CIFSService}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CIFSService implements FileStorageService {

    /**
     * Creates a new CIFS/SMB file storage service.
     * 
     * @return A new CIFS/SMB file storage service
     * @throws OXException If creation fails
     */
    public static CIFSService newInstance() throws OXException {
        final CIFSService newInst = new CIFSService();
        newInst.applyAccountManager();
        return newInst;
    }

    private final DynamicFormDescription formDescription;

    private final Set<String> secretProperties;

    private FileStorageAccountManager accountManager;

    /**
     * Initializes a new {@link CIFSService}.
     */
    private CIFSService() {
        super();
        final DynamicFormDescription tmpDescription = new DynamicFormDescription();
        tmpDescription.add(FormElement.input(CIFSConstants.CIFS_LOGIN, FormStrings.FORM_LABEL_LOGIN, true, ""));
        tmpDescription.add(FormElement.password(CIFSConstants.CIFS_PASSWORD, FormStrings.FORM_LABEL_PASSWORD, true, ""));
        formDescription = new ReadOnlyDynamicFormDescription(tmpDescription);
        secretProperties = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(CIFSConstants.CIFS_PASSWORD)));
    }

    private void applyAccountManager() throws OXException {
        accountManager = CIFSServices.getService(FileStorageAccountManagerLookupService.class).getAccountManagerFor(this);
    }

    @Override
    public String getId() {
        return CIFSConstants.ID;
    }

    @Override
    public String getDisplayName() {
        return "CIFS/SMB File Storage Service";
    }

    @Override
    public DynamicFormDescription getFormDescription() {
        return formDescription;
    }

    @Override
    public Set<String> getSecretProperties() {
        return secretProperties;
    }

    @Override
    public FileStorageAccountManager getAccountManager() {
        return accountManager;
    }

    @Override
    public FileStorageAccountAccess getAccountAccess(final String accountId, final Session session) throws OXException {
        return new CIFSAccountAccess(this, accountManager.getAccount(accountId, session), session);
    }

}

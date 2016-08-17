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

package com.openexchange.file.storage.json.actions.accounts;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.CapabilityAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.json.FileStorageAccountConstants;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * Loads a file storage account. Parameters are:
 * <dl>
 * <dt>filestorageService</dt> <dd>The ID of the messaging service. </dd>
 * <dt>id</dt><dd>The id of the messaging service that is to be loaded</dd>
 * </dl>
 * Throws an exception upon an error or returns the loaded FileStorageAccount JSON representation.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class GetAction extends AbstractFileStorageAccountAction {

    public GetAction(final FileStorageServiceRegistry registry) {
        super(registry);
    }

    @Override
    protected AJAXRequestResult doIt(final AJAXRequestData request, final ServerSession session) throws JSONException, OXException {
        List<String> missingParameters = request.getMissingParameters(FileStorageAccountConstants.FILE_STORAGE_SERVICE, FileStorageAccountConstants.ID);
        if (!missingParameters.isEmpty()) {
            throw FileStorageExceptionCodes.MISSING_PARAMETER.create(missingParameters.toString());
        }

        String fsServiceId = request.getParameter(FileStorageAccountConstants.FILE_STORAGE_SERVICE);
        FileStorageService fsService = registry.getFileStorageService(fsServiceId);

        String id = request.getParameter(FileStorageAccountConstants.ID);
        FileStorageAccount account = fsService.getAccountManager().getAccount(id, session);

        FileStorageAccountAccess access = fsService.getAccountAccess(account.getId(), session);
        FileStorageFolder rootFolder = access.getRootFolder();

        // Check file storage capabilities
        Set<String> caps = new HashSet<String>(8, 0.9f);
        if (access instanceof CapabilityAware) {
            CapabilityAware capabilityAware = (CapabilityAware) access;

            Boolean supported = capabilityAware.supports(FileStorageCapability.FILE_VERSIONS);
            if (null != supported && supported.booleanValue()) {
                caps.add(FileStorageCapability.FILE_VERSIONS.name());
            }

            supported = capabilityAware.supports(FileStorageCapability.EXTENDED_METADATA);
            if (null != supported && supported.booleanValue()) {
                caps.add(FileStorageCapability.EXTENDED_METADATA.name());
            }

            supported = capabilityAware.supports(FileStorageCapability.RANDOM_FILE_ACCESS);
            if (null != supported && supported.booleanValue()) {
                caps.add(FileStorageCapability.RANDOM_FILE_ACCESS.name());
            }

            supported = capabilityAware.supports(FileStorageCapability.LOCKS);
            if (null != supported && supported.booleanValue()) {
                caps.add(FileStorageCapability.LOCKS.name());
            }

            supported = capabilityAware.supports(FileStorageCapability.READ_ONLY);
            if (null != supported && supported.booleanValue()) {
                caps.add(FileStorageCapability.READ_ONLY.name());
            }

            supported = capabilityAware.supports(FileStorageCapability.MAIL_ATTACHMENTS);
            if (null != supported && supported.booleanValue()) {
                caps.add(FileStorageCapability.MAIL_ATTACHMENTS.name());
            }
        }
        return new AJAXRequestResult(writer.write(account, rootFolder, caps));
    }

}

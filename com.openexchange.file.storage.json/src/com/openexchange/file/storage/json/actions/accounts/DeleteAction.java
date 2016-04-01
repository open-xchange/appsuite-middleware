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

import static com.openexchange.file.storage.json.FileStorageAccountConstants.FILE_STORAGE_SERVICE;
import static com.openexchange.file.storage.json.FileStorageAccountConstants.ID;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.tools.session.ServerSession;

/**
 * This action deletes a file storage account. Parameters are:
 * <dl>
 *  <dt>filestorageService</dt> <dd>The ID of the messaging service. </dd>
 *  <dt>id</dt><dd>The id of the file storage service that is to be deleted</dd>
 * </dl>
 * Throws an exception upon an error or returns "1" on success.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DeleteAction extends AbstractFileStorageAccountAction {

    public DeleteAction(final FileStorageServiceRegistry registry) {
        super(registry);
    }

    @Override
    protected AJAXRequestResult doIt(AJAXRequestData request, ServerSession session) throws JSONException, OXException {
        List<String> missingParameters = request.getMissingParameters(FILE_STORAGE_SERVICE, ID);
        if (false == missingParameters.isEmpty()) {
            throw FileStorageExceptionCodes.MISSING_PARAMETER.create(missingParameters.toString());
        }
        /*
         * get targeted account
         */
        FileStorageService fileStorageService = registry.getFileStorageService(request.getParameter(FILE_STORAGE_SERVICE));
        FileStorageAccountManager accountManager = fileStorageService.getAccountManager();
        FileStorageAccount account = accountManager.getAccount(request.getParameter(ID), session);
        /*
         * delete the account & return appropriate result on success
         */
        accountManager.deleteAccount(account, session);
        return new AJAXRequestResult(Integer.valueOf(1));
    }

}

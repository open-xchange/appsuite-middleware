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

package com.openexchange.mail.json.actions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.server.ServiceLookup;

/**
 * {@link MoveAllAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class MoveAllAction extends AbstractMailAction {

    /**
     * Initializes a new {@link MoveAllAction}.
     *
     * @param services
     */
    public MoveAllAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException {
        try {
            // Read in parameters
            JSONObject jBody = (JSONObject) req.getRequest().requireData();
            String sourceFolder = jBody.optString("source", null);
            if (null == sourceFolder) {
                sourceFolder = jBody.getString("from");
            }

            String destFolder = jBody.optString("target", null);
            if (null == destFolder) {
                destFolder = jBody.getString("to");
            }

            // Get mail interface
            MailServletInterface mailInterface = getMailInterface(req);
            mailInterface.copyAllMessages(sourceFolder, destFolder, true);

            Map<FullnameArgument, FolderInfo> folders = new LinkedHashMap<FullnameArgument, FolderInfo>(4);
            FullnameArgument first = MailFolderUtility.prepareMailFolderParam(sourceFolder);
            FullnameArgument other = MailFolderUtility.prepareMailFolderParam(destFolder);
            if (first.getAccountId() == other.getAccountId()) {
                folders.put(first, FolderInfo.getFolderInfo(first.getFullName(), mailInterface.getMailAccess().getFolderStorage()));
                if (!folders.containsKey(other)) {
                    folders.put(other, FolderInfo.getFolderInfo(other.getFullName(), mailInterface.getMailAccess().getFolderStorage()));
                }
            } else {
                if (mailInterface.getMailAccess().getAccountId() == first.getAccountId()) {
                    // MailServletInterface is still connected to source account
                    folders.put(first, FolderInfo.getFolderInfo(first.getFullName(), mailInterface.getMailAccess().getFolderStorage()));
                    if (!folders.containsKey(other)) {
                        // Reconnect to previous account
                        mailInterface.openFor(destFolder);
                        folders.put(other, FolderInfo.getFolderInfo(other.getFullName(), mailInterface.getMailAccess().getFolderStorage()));
                    }
                } else {
                    // MailServletInterface is still connected to other account
                    folders.put(other, FolderInfo.getFolderInfo(other.getFullName(), mailInterface.getMailAccess().getFolderStorage()));
                    if (!folders.containsKey(first)) {
                        // Reconnect to previous account
                        mailInterface.openFor(sourceFolder);
                        folders.put(first, FolderInfo.getFolderInfo(first.getFullName(), mailInterface.getMailAccess().getFolderStorage()));
                    }
                }
            }

            // Return folder information
            JSONObject jResponse = new JSONObject(2);
            JSONObject jFolders = new JSONObject(folders.size());
            for (Entry<FullnameArgument, FolderInfo> infoEntry : folders.entrySet()) {
                FullnameArgument fa = infoEntry.getKey();
                String id = MailFolderUtility.prepareFullname(fa.getAccountId(), fa.getFullName());
                FolderInfo folderInfo = infoEntry.getValue();
                jFolders.put(id, new JSONObject(4).put("total", folderInfo.total).put("unread", folderInfo.unread));
            }
            jResponse.put("folders", jFolders);
            return new AJAXRequestResult(jResponse, "json");
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}

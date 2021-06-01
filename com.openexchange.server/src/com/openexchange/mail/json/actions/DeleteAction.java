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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DeleteAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class DeleteAction extends AbstractMailAction {

    /**
     * Initializes a new {@link DeleteAction}.
     *
     * @param services
     */
    public DeleteAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException {
        try {
            // Read in parameters
            boolean hardDelete = AJAXRequestDataTools.parseBoolParameter(req.getParameter(AJAXServlet.PARAMETER_HARDDELETE));
            boolean returnAffectedFolders = AJAXRequestDataTools.parseBoolParameter(req.getParameter("returnAffectedFolders"));
            JSONArray jsonIds = (JSONArray) req.getRequest().requireData();

            // Get mail interface
            MailServletInterface mailInterface = getMailInterface(req);

            JSONObject jResponse = null;
            int length = jsonIds.length();
            if (length > 0) {

                // Collect affected mail paths
                List<MailPath> l = new ArrayList<MailPath>(length);
                Map<FullnameArgument, FolderInfo> folders = returnAffectedFolders ? new LinkedHashMap<FullnameArgument, FolderInfo>(length) : null;
                for (int i = 0; i < length; i++) {
                    JSONObject jId = jsonIds.getJSONObject(i);
                    FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(jId.getString(AJAXServlet.PARAMETER_FOLDERID));
                    l.add(new MailPath(fa.getAccountId(), fa.getFullname(), jId.getString(AJAXServlet.PARAMETER_ID)));
                }

                // Try to batch-delete per folder
                Collections.sort(l, MailPath.COMPARATOR);
                FullnameArgument lastFldArg = l.get(0).getFullnameArgument();
                List<String> arr = new ArrayList<String>(length);
                for (int i = 0; i < length; i++) {
                    MailPath current = l.get(i);
                    FullnameArgument folderArgument = current.getFullnameArgument();

                    // Check if collectable
                    if (!lastFldArg.equals(folderArgument)) {
                        // Delete all collected UIDs until here and reset
                        String[] uids = arr.toArray(new String[arr.size()]);
                        mailInterface.deleteMessages(lastFldArg.getPreparedName(), uids, hardDelete);

                        if (null != folders) {
                            int connectedAccount = mailInterface.getAccountID();

                            // Add folder
                            folders.put(lastFldArg, getFolderInfo(lastFldArg.getFullName(), mailInterface));

                            // Check if trash needs to be added, too
                            if (!hardDelete) {
                                // Add account's trash folder
                                FullnameArgument trash = MailFolderUtility.prepareMailFolderParam(mailInterface.getTrashFolder(connectedAccount));
                                if (!trash.equals(lastFldArg)) {
                                    folders.put(trash, getFolderInfo(trash.getFullName(), mailInterface));
                                }
                            }
                        }
                        arr.clear();
                        lastFldArg = folderArgument;
                    }
                    arr.add(current.getMailID());
                }

                // Delete all collected remaining UIDs
                int size = arr.size();
                if (size > 0) {
                    String[] uids = arr.toArray(new String[size]);
                    mailInterface.deleteMessages(lastFldArg.getPreparedName(), uids, hardDelete);

                    if (null != folders) {
                        int connectedAccount = mailInterface.getAccountID();

                        // Add folder
                        folders.put(lastFldArg, getFolderInfo(lastFldArg.getFullName(), mailInterface));

                        // Check if trash needs to be added, too
                        if (!hardDelete) {
                            // Add account's trash folder
                            FullnameArgument trash = MailFolderUtility.prepareMailFolderParam(mailInterface.getTrashFolder(connectedAccount));
                            if (!trash.equals(lastFldArg)) {
                                folders.put(trash, getFolderInfo(trash.getFullName(), mailInterface));
                            }
                        }
                    }
                }

                if (returnAffectedFolders && null != folders && !folders.isEmpty()) {
                    jResponse = new JSONObject(4);
                    jResponse.put("conflicts", new JSONArray(0));

                    JSONObject jFolders = new JSONObject(folders.size());
                    for (Entry<FullnameArgument, FolderInfo> infoEntry : folders.entrySet()) {
                        FullnameArgument fa = infoEntry.getKey();
                        String id = MailFolderUtility.prepareFullname(fa.getAccountId(), fa.getFullName());
                        FolderInfo folderInfo = infoEntry.getValue();
                        jFolders.put(id, new JSONObject(4).put("total", folderInfo.total).put("unread", folderInfo.unread));
                    }
                    jResponse.put("folders", jFolders);
                }
            }

            return new AJAXRequestResult(null == jResponse ? new JSONArray(0) : jResponse, "json");
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private FolderInfo getFolderInfo(String fullName, MailServletInterface mailInterface) throws OXException {
        return FolderInfo.getFolderInfo(fullName, mailInterface.getMailAccess().getFolderStorage());
    }

}

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
public final class DeleteAction extends AbstractMailAction {

    /**
     * Initializes a new {@link DeleteAction}.
     *
     * @param services
     */
    public DeleteAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
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
                String lastFldArg = l.get(0).getFolderArgument();
                List<String> arr = new ArrayList<String>(length);
                for (int i = 0; i < length; i++) {
                    MailPath current = l.get(i);
                    String folderArgument = current.getFolderArgument();

                    // Check if collectable
                    if (!lastFldArg.equals(folderArgument)) {
                        // Delete all collected UIDs until here and reset
                        String[] uids = arr.toArray(new String[arr.size()]);
                        mailInterface.deleteMessages(lastFldArg, uids, hardDelete);

                        if (null != folders) {
                            int connectedAccount = mailInterface.getAccountID();

                            // Add folder
                            FullnameArgument fa = new FullnameArgument(connectedAccount, current.getFolder());
                            folders.put(fa, getFolderInfo(current.getFolder(), mailInterface));

                            // Check if trash needs to be added, too
                            if (!hardDelete) {
                                // Add account's trash folder
                                FullnameArgument trash = MailFolderUtility.prepareMailFolderParam(mailInterface.getTrashFolder(connectedAccount));
                                if (!trash.equals(fa)) {
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
                    mailInterface.deleteMessages(lastFldArg, uids, hardDelete);

                    if (null != folders) {
                        int connectedAccount = mailInterface.getAccountID();

                        // Add folder
                        FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(lastFldArg);
                        folders.put(fa, getFolderInfo(fa.getFullName(), mailInterface));

                        // Check if trash needs to be added, too
                        if (!hardDelete) {
                            // Add account's trash folder
                            FullnameArgument trash = MailFolderUtility.prepareMailFolderParam(mailInterface.getTrashFolder(connectedAccount));
                            if (!trash.equals(fa)) {
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
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private FolderInfo getFolderInfo(String fullName, MailServletInterface mailInterface) throws OXException {
        return FolderInfo.getFolderInfo(fullName, mailInterface.getMailAccess().getFolderStorage());
    }

}

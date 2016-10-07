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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
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
public final class MoveAllAction extends AbstractMailAction {

    /**
     * Initializes a new {@link MoveAllAction}.
     *
     * @param services
     */
    public MoveAllAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
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
                // MailServletInterface is still connected to other account
                folders.put(other, FolderInfo.getFolderInfo(other.getFullName(), mailInterface.getMailAccess().getFolderStorage()));
                if (!folders.containsKey(first)) {
                    // Reconnect to previous account
                    mailInterface.openFor(sourceFolder);
                    folders.put(first, FolderInfo.getFolderInfo(first.getFullName(), mailInterface.getMailAccess().getFolderStorage()));
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

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.IMailMessageStorageEnhancedDeletion;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.server.ServiceLookup;

/**
 * {@link TrashAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TrashAction extends AbstractMailAction {

    /**
     * Initializes a new {@link TrashAction}.
     *
     * @param services
     */
    public TrashAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        try {
            // Read in parameters
            boolean hardDelete = AJAXRequestDataTools.parseBoolParameter(req.getParameter(AJAXServlet.PARAMETER_HARDDELETE));
            String folderId = req.checkParameter(Mail.PARAMETER_MAILFOLDER);
            JSONArray jsonIds = (JSONArray) req.getRequest().requireData();

            FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(folderId);

            MailServletInterface mailInterface = getMailInterface(req);
            mailInterface.openFor(folderId);
            MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = mailInterface.getMailAccess();

            IMailMessageStorage messageStorage = mailAccess.getMessageStorage();
            if (!IMailMessageStorageEnhancedDeletion.class.isInstance(messageStorage)) {
                throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
            }

            IMailMessageStorageEnhancedDeletion enhancedDeletionMessageStorage = (IMailMessageStorageEnhancedDeletion) messageStorage;
            if (!enhancedDeletionMessageStorage.isEnhancedDeletionSupported()) {
                throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
            }

            String[] mailIds;
            {
                mailIds = new String[jsonIds.length()];
                int i = 0;
                for (Object object : jsonIds) {
                    mailIds[i++] = JSONObject.NULL.equals(object) ? null : object.toString();
                }
            }

            MailPath[] mailPaths = enhancedDeletionMessageStorage.deleteMessagesEnhanced(fa.getFullName(), mailIds, hardDelete);

            JSONArray jPaths = new JSONArray(mailPaths.length);
            for (MailPath mailPath : mailPaths) {
                if (null == mailPath) {
                    jPaths.put(JSONObject.NULL);
                } else {
                    jPaths.put(new JSONObject(2).put(FolderChildFields.FOLDER_ID, MailFolderUtility.prepareFullname(fa.getAccountId(), mailPath.getFolder())).put(DataFields.ID, mailPath.getMailID()));
                }
            }

            return new AJAXRequestResult(jPaths, "json");
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}

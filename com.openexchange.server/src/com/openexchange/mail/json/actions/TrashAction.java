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

import org.json.ImmutableJSONArray;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
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
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class TrashAction extends AbstractMailAction {

    private final JSONArray emptyJsonArray;

    /**
     * Initializes a new {@link TrashAction}.
     *
     * @param services
     */
    public TrashAction(ServiceLookup services) {
        super(services);
        emptyJsonArray = ImmutableJSONArray.immutableFor(new JSONArray(0));
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException {
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

            IMailMessageStorageEnhancedDeletion enhancedDeletionMessageStorage = messageStorage.supports(IMailMessageStorageEnhancedDeletion.class);
            if (null == enhancedDeletionMessageStorage) {
                throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
            }

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
            if (null == mailPaths || mailPaths.length == 0) {
                return new AJAXRequestResult(emptyJsonArray, "json");
            }

            JSONArray jPaths = new JSONArray(mailPaths.length);
            for (MailPath mailPath : mailPaths) {
                if (null != mailPath) {
                    jPaths.put(new JSONObject(2).put(FolderChildFields.FOLDER_ID, MailFolderUtility.prepareFullname(fa.getAccountId(), mailPath.getFolder())).put(DataFields.ID, mailPath.getMailID()));
                } else {
                    jPaths.put(JSONObject.NULL);
                }
            }

            return new AJAXRequestResult(jPaths, "json");
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}

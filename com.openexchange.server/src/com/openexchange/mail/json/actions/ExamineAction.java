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

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailFolderStorageStatusSupport;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.api.MailCapabilities;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.mail.dataobjects.MailFolderStatus;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ExamineAction}
 *
 * @author <a href="mailto:joshua.wirtz@open-xchange.com">Joshua Wirtz</a>
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.READ)
public class ExamineAction extends AbstractMailAction {

    /**
     * Initializes a new {@link ExamineAction}.
     *
     * @param services The service look-up
     */
    public ExamineAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException, JSONException {
        String folder = req.checkParameter("folder");
        FullnameArgument fullnameArgument = MailFolderUtility.prepareMailFolderParam(folder);

        MailServletInterface mailInterface = getMailInterface(req);
        mailInterface.openFor(folder);
        MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = mailInterface.getMailAccess();
        {
            MailConfig mailConfig = mailAccess.getMailConfig();
            MailCapabilities capabilities = mailConfig.getCapabilities();

            if (!capabilities.hasFolderValidity()) {
                throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
            }

            IMailFolderStorage folderStorage = mailAccess.getFolderStorage();

            IMailFolderStorageStatusSupport validityFolderStorage = folderStorage.supports(IMailFolderStorageStatusSupport.class);
            if (null == validityFolderStorage) {
                throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
            }

            if (!validityFolderStorage.isStatusSupported()) {
                throw MailExceptionCode.UNSUPPORTED_OPERATION.create();
            }

            MailFolderStatus status = validityFolderStorage.getFolderStatus(fullnameArgument.getFullname());
            JSONObject jStatus = new JSONObject(6)
                .put("validity", status.getValidity())
                .put("total", status.getTotal())
                .put("unread", status.getUnread())
                .put("next", status.getNextId());

            return new AJAXRequestResult(jStatus, "json");
        }
    }

}

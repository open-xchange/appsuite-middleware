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
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ReceiptAckAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class ReceiptAckAction extends AbstractMailAction {

    /**
     * Initializes a new {@link ReceiptAckAction}.
     *
     * @param services
     */
    public ReceiptAckAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException {
        try {
            /*
             * Read in parameters
             */
            final JSONObject bodyObj = (JSONObject) req.getRequest().requireData();
            final String folderPath = bodyObj.has(AJAXServlet.PARAMETER_FOLDERID) ? bodyObj.getString(AJAXServlet.PARAMETER_FOLDERID) : null;
            if (null == folderPath) {
                throw MailExceptionCode.MISSING_PARAM.create(AJAXServlet.PARAMETER_FOLDERID);
            }
            final String uid = bodyObj.has(AJAXServlet.PARAMETER_ID) ? bodyObj.getString(AJAXServlet.PARAMETER_ID) : null;
            if (null == uid) {
                throw MailExceptionCode.MISSING_PARAM.create(AJAXServlet.PARAMETER_ID);
            }
            final String fromAddr = bodyObj.hasAndNotNull(MailJSONField.FROM.getKey()) ? bodyObj.getString(MailJSONField.FROM.getKey()) : null;
            final MailServletInterface mailInterface = getMailInterface(req);
            mailInterface.sendReceiptAck(folderPath, uid, fromAddr);
            return getJSONNullResult();
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}

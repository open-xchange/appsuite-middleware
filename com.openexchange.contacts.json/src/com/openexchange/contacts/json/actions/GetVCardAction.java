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

package com.openexchange.contacts.json.actions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.openexchange.ajax.container.ModifyableFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.vcard.VCardExport;
import com.openexchange.contact.vcard.storage.VCardStorageService;
import com.openexchange.contacts.json.ContactRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetVCardAction} - Gets session user's VCard.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class GetVCardAction extends ContactAction {

    /**
     * Initializes a new {@link GetVCardAction}.
     *
     * @param serviceLookup The OSGi service look-up
     */
    public GetVCardAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    protected AJAXRequestResult perform(ContactRequest request) throws OXException {
        /*
         * export user's contact as vCard
         */
        AJAXRequestData requestData = request.getRequest();
        ServerSession session = request.getSession();
        VCardExport vCardExport = exportVCard(session);
        if (requestData.setResponseHeader("Content-Type", MimeTypes.MIME_TEXT_VCARD)) {
            /*
             * try to write a "direct" result if possible
             */
            StringBuilder stringBuilder = new StringBuilder(128).append("attachment");
            DownloadUtility.appendFilenameParameter("vcard.vcf", MimeTypes.MIME_TEXT_VCARD, requestData.getUserAgent(), stringBuilder);
            requestData.setResponseHeader("Content-Disposition", stringBuilder.toString());
            OutputStream out = null;
            try {
                out = requestData.optOutputStream();
                if (null != out) {
                    InputStream inputStream = null;
                    byte[] buffer = new byte[0xFFFF];
                    try {
                        inputStream = vCardExport.getClosingStream();
                        for (int len; (len = inputStream.read(buffer, 0, buffer.length)) > 0;) {
                            out.write(buffer, 0, len);
                        }
                    } finally {
                        Streams.close(inputStream);
                    }
                    out.flush();
                    Streams.close(out);
                    out = null;
                    return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
                }
            } catch (IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } finally {
                Streams.close(out);
            }
        }

        /*
         * respond with "file" result as fallback
         */
        requestData.setFormat("file");
        ModifyableFileHolder fileHolder = new ModifyableFileHolder(vCardExport.getVCard());
        fileHolder.setDisposition("attachment").setName("vcard.vcf").setContentType(MimeTypes.MIME_TEXT_VCARD).setDelivery("download");
        return new AJAXRequestResult(fileHolder, "file");
    }

    /**
     * Exports the session user's vCard.
     *
     * @param session The session
     * @return The exported vCard
     */
    private VCardExport exportVCard(ServerSession session) throws OXException {
        Contact contact = getContactService().getUser(session, session.getUserId());
        InputStream originalVCard = null;
        try {
            if (null != contact.getVCardId()) {
                VCardStorageService vCardStorageService = optVCardStorageService(session.getContextId());
                if (null != vCardStorageService) {
                    originalVCard = vCardStorageService.getVCard(contact.getVCardId(), session.getContextId());
                }
            }
            return getVCardService().exportContact(contact, originalVCard, getVCardService().createParameters(session));
        } finally {
            Streams.close(originalVCard);
        }
    }

}

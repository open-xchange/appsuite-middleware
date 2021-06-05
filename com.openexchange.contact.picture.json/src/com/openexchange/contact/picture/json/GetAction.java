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

package com.openexchange.contact.picture.json;

import static com.openexchange.contact.picture.json.PictureRequestParameter.ACCOUNT_ID;
import static com.openexchange.contact.picture.json.PictureRequestParameter.CONTACT;
import static com.openexchange.contact.picture.json.PictureRequestParameter.CONTACT_FOLDER;
import static com.openexchange.contact.picture.json.PictureRequestParameter.MAIL;
import static com.openexchange.contact.picture.json.PictureRequestParameter.USER;
import java.util.Collections;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.requesthandler.ETagAwareAJAXActionService;
import com.openexchange.ajax.requesthandler.LastModifiedAwareAJAXActionService;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.ContactPictureService;
import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetAction}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
@DispatcherNotes(defaultFormat = "file", allowPublicSession = true, publicSessionAuth = true)
@RestrictedAction(module = "contacts", type = RestrictedAction.Type.READ)
public class GetAction implements ETagAwareAJAXActionService, LastModifiedAwareAJAXActionService {

    final ServiceLookup services;

    /**
     * Initializes a new {@link GetAction}.
     *
     * @param services The OSGi service look-up
     */
    GetAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        ContactPicture picture = services.getServiceSafe(ContactPictureService.class).getPicture(session, getData(requestData));
        IFileHolder fileHolder = picture.getFileHolder();
        if (null == fileHolder) {
            // 404 - Not Found
            AJAXRequestResult result = new AJAXRequestResult();
            result.setHttpStatusCode(HttpServletResponse.SC_NOT_FOUND);
            return result;
        }

        AJAXRequestResult result = new AJAXRequestResult(picture.getFileHolder(), "file");
        setETag(picture.getETag(), Tools.getDefaultImageExpiry(), result);
        result.setHeader("Last-Modified", Tools.formatHeaderDate(picture.getLastModified()));
        return result;
    }

    /**
     * Parses the {@link PictureSearchData} from the given {@link AJAXRequestData}
     *
     * @param requestData The {@link AJAXRequestData}
     * @return The {@link PictureSearchData}
     * @throws OXException
     */
    private PictureSearchData getData(AJAXRequestData requestData) throws OXException {
        if (null == requestData) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        String contactId = requestData.getParameter(CONTACT.getParameter(), String.class, true);
        String folderId = requestData.getParameter(CONTACT_FOLDER.getParameter(), String.class, true);
        if (folderId == null && contactId != null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(CONTACT_FOLDER.getParameter());
        }
        Integer userId = requestData.getParameter(USER.getParameter(), Integer.class, true);
        String accountId = requestData.getParameter(ACCOUNT_ID.getParameter());
        String email = requestData.getParameter(MAIL.getParameter());

        return new PictureSearchData(userId, accountId, folderId, contactId, email == null ? null : Collections.singleton(email));
    }

    @Override
    public boolean checkETag(String clientETag, AJAXRequestData request, ServerSession session) throws OXException {
        String eTag = services.getServiceSafe(ContactPictureService.class).getETag(session, getData(request));
        if (eTag == null) {
            return false;
        }
        if (eTag.equals(clientETag)) {
            return true;
        }
        return false;
    }

    @Override
    public void setETag(String eTag, long expires, AJAXRequestResult result) {
        result.setExpires(expires);
        if (eTag != null) {
            result.setHeader("ETag", eTag);
        }
    }

    @Override
    public boolean checkLastModified(long clientLastModified, AJAXRequestData request, ServerSession session) throws OXException {
        Date lastModified = services.getServiceSafe(ContactPictureService.class).getLastModified(session, getData(request));
        return lastModified.getTime() == clientLastModified;
    }
}

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

package com.openexchange.contact.picture.json;

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
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.ContactPictureService;
import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
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
@OAuthAction(ContactActionFactory.OAUTH_READ_SCOPE)
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
        Integer contactId = requestData.getParameter(CONTACT.getParameter(), Integer.class, true);
        Integer folderId = requestData.getParameter(CONTACT_FOLDER.getParameter(), Integer.class, true);
        if (folderId == null && contactId != null) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(CONTACT_FOLDER.getParameter());
        }
        String email = requestData.getParameter(MAIL.getParameter());
        Integer userId = requestData.getParameter(USER.getParameter(), Integer.class, true);

        return new PictureSearchData(userId, folderId, contactId, email == null ? null : Collections.singleton(email));
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

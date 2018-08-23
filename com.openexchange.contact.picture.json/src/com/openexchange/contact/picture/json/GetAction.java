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

import java.rmi.server.UID;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.requesthandler.ETagAwareAJAXActionService;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.contact.picture.ContactPictureRequestData;
import com.openexchange.contact.picture.ContactPictureService;
import com.openexchange.contacts.json.ContactActionFactory;
import com.openexchange.exception.OXException;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
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
public class GetAction implements ETagAwareAJAXActionService {

    private static final String MAIL_PARAM = "mail";
    private static final String USER_PARAM = "userId";
    private static final String CONTACT_PARAM = "contactId";

    static final byte[] TRANSPARENT_GIF = { 71, 73, 70, 56, 57, 97, 1, 0, 1, 0, -128, 0, 0, 0, 0, 0, -1, -1, -1, 33, -7, 4, 1, 0, 0, 0, 0, 44, 0, 0, 0, 0, 1, 0, 1, 0, 0, 2, 1, 68, 0, 59 };

    static final ContactPicture FALLBACK_PICTURE;

    static {
        ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(TRANSPARENT_GIF);
        fileHolder.setContentType("image/gif");
        fileHolder.setName("image.gif");
        MessageDigest digest;
        String etag;
        try {
            digest = MessageDigest.getInstance("MD5");
            etag = digest.digest(TRANSPARENT_GIF).toString();
        } catch (NoSuchAlgorithmException e) {
            // should not occur
            etag = new UID().toString();
        }

        FALLBACK_PICTURE = new ContactPicture(etag.toString(), fileHolder);
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    final ServiceLookup services;

    /**
     * Initializes a new {@link GetPictureAction}.
     *
     * @param services The OSGi service look-up
     */
    GetAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        ContactPicture picture = getPicture(getData(requestData, session, false));
        AJAXRequestResult result = new AJAXRequestResult(picture.containsContactPicture() ? picture.getFileHolder() : FALLBACK_PICTURE.getFileHolder(), "file");
        setETag(picture.getEtag(), Tools.getDefaultImageExpiry(), result);
        return result;
    }

    private ContactPictureRequestData getData(AJAXRequestData requestData, Session session, boolean etagOnly) throws OXException {
        Integer contactId = requestData.getIntParameter(CONTACT_PARAM);
        String email = requestData.getParameter(MAIL_PARAM);
        Integer userId = requestData.getIntParameter(USER_PARAM);
        return new ContactPictureRequestData.ContactPictureDataBuilder()
                                             .setSession(session)
                                             .setContactId(contactId)
                                             .setEmails(email)
                                             .setUser(userId)
                                             .setETag(etagOnly).build();
    }

    @Override
    public boolean checkETag(String clientETag, AJAXRequestData request, ServerSession session) throws OXException {
        ContactPicture contactPicture = getPicture(getData(request, session, true));
        String etag = contactPicture.getEtag();
        if (etag == null) {
            return false;
        }
        if (etag.equals(clientETag)) {
            return true;
        }
        return false;
    }

    @Override
    public void setETag(String eTag, long expires, AJAXRequestResult result) throws OXException {
        result.setExpires(expires);
        if (eTag != null) {
            result.setHeader("ETag", eTag);
        }
    }

    private ContactPicture getPicture(ContactPictureRequestData data) throws OXException {
        return services.getServiceSafe(ContactPictureService.class).getPicture(data);
    }
}

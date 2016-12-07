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

package com.openexchange.halo.pictures;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.requesthandler.ETagAwareAJAXActionService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.halo.ContactHalo;
import com.openexchange.halo.Picture;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link GetPictureAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@DispatcherNotes(allowPublicSession=true, defaultFormat="file")
public class GetPictureAction implements ETagAwareAJAXActionService {

    private static final byte[] TRANSPARENT_GIF = { 71, 73, 70, 56, 57, 97, 1, 0, 1, 0, -128, 0, 0, 0, 0, 0, -1, -1, -1, 33, -7, 4, 1, 0, 0, 0, 0, 44, 0, 0, 0, 0, 1, 0, 1, 0, 0, 2, 1, 68, 0, 59 };

    private static final Picture FALLBACK_PICTURE;

    static {
        ByteArrayFileHolder fileHolder = new ByteArrayFileHolder(TRANSPARENT_GIF);
        fileHolder.setContentType("image/gif");
        fileHolder.setName("image.gif");
        FALLBACK_PICTURE = new Picture(null, fileHolder);
    }

    // -----------------------------------------------------------------------------------------------------------------------------

    private final ServiceLookup services;

    /**
     * Initializes a new {@link GetPictureAction}.
     *
     * @param services The OSGi service look-up
     */
    public GetPictureAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        Picture picture = getPicture(requestData, session);
        if (picture == null) {
            // 404 - Not Found
            AJAXRequestResult result = new AJAXRequestResult();
            result.setHttpStatusCode(HttpServletResponse.SC_NOT_FOUND);
            return result;
        }

        if (FALLBACK_PICTURE == picture) {
            ByteArrayFileHolder fileHolder = (ByteArrayFileHolder) picture.getFileHolder();
            if (requestData.setResponseHeader("Content-Type", fileHolder.getContentType())) {
                // Set HTTP response headers
                {
                    final StringBuilder sb = new StringBuilder(256);
                    sb.append("inline");
                    DownloadUtility.appendFilenameParameter(fileHolder.getName(), fileHolder.getContentType(), requestData.getUserAgent(), sb);
                    requestData.setResponseHeader("Content-Disposition", sb.toString());

                    String eTag = picture.getEtag();
                    long expires = Tools.getDefaultImageExpiry();
                    if (null == eTag) {
                        if (expires > 0) {
                            Tools.setExpires(expires, requestData.optHttpServletResponse());
                        }
                    } else {
                        Tools.setETag(eTag, expires > 0 ? expires : -1L, requestData.optHttpServletResponse());
                    }
                }

                // Write image file
                try {
                    OutputStream out = requestData.optOutputStream();
                    out.write(fileHolder.getBytes());
                    out.flush();
                } catch (IOException e) {
                    throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
                }

                // Signal direct response
                return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
            }
        }

        AJAXRequestResult result = new AJAXRequestResult(picture.getFileHolder(), "file");
        setETag(picture.getEtag(), Tools.getDefaultImageExpiry(), result);
        return result;
    }

    @Override
    public boolean checkETag(String clientETag, AJAXRequestData request, ServerSession session) throws OXException {
        String pictureETag = getPictureETag(request, session);
        if (pictureETag == null) {
            return false;
        }
        if (pictureETag.equals(clientETag)) {
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

    private Picture getPicture(AJAXRequestData req, ServerSession session) throws OXException {
        return getPictureResource(req, session, false);
    }

    private String getPictureETag(AJAXRequestData req, ServerSession session) throws OXException {
        return getPictureResource(req, session, true);
    }

    @SuppressWarnings("unchecked")
    private <V> V getPictureResource(AJAXRequestData req, ServerSession session, boolean eTagOnly) throws OXException {
        final ContactHalo contactHalo = services.getService(ContactHalo.class);
        if (null == contactHalo) {
            throw ServiceExceptionCode.absentService(ContactHalo.class);
        }

        Contact contact = new Contact();
        boolean hadCriterium = false;

        if (req.isSet("internal_userid")) {
            hadCriterium = true;
            contact.setInternalUserId(req.getIntParameter("internal_userid"));
        } else if (req.isSet("userid")) {
            hadCriterium = true;
            contact.setInternalUserId(req.getIntParameter("userid"));
        } else if (req.isSet("user_id")) {
            hadCriterium = true;
            contact.setInternalUserId(req.getIntParameter("user_id"));
        }

        if (req.isSet("id") && !hadCriterium) {
            int id = Strings.parsePositiveInt(req.getParameter("id"));
            if (id > 0) {
                contact.setObjectID(req.getIntParameter("id"));
            }

            if (req.isSet("folder")) {
                hadCriterium = true;
                contact.setParentFolderID(req.getIntParameter("folder"));
            }
        }

        if (req.isSet("email")) {
            hadCriterium = true;
            contact.setEmail1(req.getParameter("email"));
        } else if (req.isSet("email1")) {
            hadCriterium = true;
            contact.setEmail1(req.getParameter("email1"));
        }

        if (req.isSet("email2")) {
            hadCriterium = true;
            contact.setEmail2(req.getParameter("email2"));
        }

        if (req.isSet("email3")) {
            hadCriterium = true;
            contact.setEmail3(req.getParameter("email3"));
        }

        if (!hadCriterium) {
            return (V) (eTagOnly ? null : fallbackPicture());
        }

        try {
            if (eTagOnly) {
                return (V) contactHalo.getPictureETag(contact, session);
            }

            Picture picture = contactHalo.getPicture(contact, session);
            if (picture == null) {
                return (V) fallbackPicture();
            }
            return (V) picture;
        } catch (OXException x) {
            return (V) (eTagOnly ? null : fallbackPicture());
        }
    }

    private Picture fallbackPicture() {
        return FALLBACK_PICTURE;
    }

}

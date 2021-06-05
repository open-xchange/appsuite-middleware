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

package com.openexchange.halo.pictures;

import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ETagAwareAJAXActionService;
import com.openexchange.contact.picture.ContactPicture;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractGetPictureAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
abstract class AbstractGetPictureAction implements ETagAwareAJAXActionService {

    final ServiceLookup services;

    /**
     * Initializes a new {@link GetPictureAction}.
     *
     * @param services The OSGi service look-up
     */
    AbstractGetPictureAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        ContactPicture picture = getPicture(requestData, session);
        if (picture == null) {
            // 404 - Not Found
            AJAXRequestResult result = new AJAXRequestResult();
            result.setHttpStatusCode(HttpServletResponse.SC_NOT_FOUND);
            return result;
        }

        IFileHolder fileHolder = picture.getFileHolder();
        if (fileHolder == null) {
            // 404 - Not Found
            AJAXRequestResult result = new AJAXRequestResult();
            result.setHttpStatusCode(HttpServletResponse.SC_NOT_FOUND);
            return result;
        }

        AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
        setETag(picture.getETag(), Tools.getDefaultImageExpiry(), result);
        return result;
    }

    @Override
    public boolean checkETag(String clientETag, AJAXRequestData request, ServerSession session) throws OXException {
        String pictureETag = getPictureETag(request, session);
        return pictureETag == null ? false : pictureETag.equals(clientETag);
    }

    @Override
    public void setETag(String eTag, long expires, AJAXRequestResult result) {
        result.setExpires(expires);
        if (eTag != null) {
            result.setHeader("ETag", eTag);
        }
    }

    /**
     * Gets the {@link ContactPicture}
     *
     * @param req The {@link AJAXRequestData}
     * @param session The user's {@link ServerSession}
     * @return The {@link ContactPicture}
     * @throws OXException
     */
    private ContactPicture getPicture(AJAXRequestData req, ServerSession session) throws OXException {
        return getPictureResource(req, session, false);
    }

    /**
     * Gets the etag of the {@link ContactPicture}
     *
     * @param req The {@link AJAXRequestData}
     * @param session The user's {@link ServerSession}
     * @return The etag of the {@link ContactPicture}
     * @throws OXException
     */
    private String getPictureETag(AJAXRequestData req, ServerSession session) throws OXException {
        return getPictureResource(req, session, true);
    }

    /**
     * Returns the actual picture resource
     *
     * @param req The {@link AJAXRequestData}
     * @param session The groupware {@link Session}
     * @param eTagOnly whether the eTag should be considered
     * @return The picture resource {@link V}
     * @throws OXException if an error is occurred
     */
    abstract <V> V getPictureResource(AJAXRequestData req, ServerSession session, boolean eTagOnly) throws OXException;

}

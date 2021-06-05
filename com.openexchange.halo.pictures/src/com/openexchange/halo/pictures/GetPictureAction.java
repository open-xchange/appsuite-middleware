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

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.contact.picture.ContactPictureService;
import com.openexchange.contact.picture.PictureSearchData;
import com.openexchange.exception.OXException;
import com.openexchange.halo.ContactHalo;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetPictureAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @deprecated Use the new contacts/picture action instead
 */
@DispatcherNotes(allowPublicSession = true, defaultFormat = "file")
@Deprecated
public class GetPictureAction extends AbstractGetPictureAction {

    /**
     * Initializes a new {@link GetPictureAction}.
     *
     * @param services The OSGi service look-up
     */
    public GetPictureAction(ServiceLookup services) {
        super(services);
    }

    @SuppressWarnings("unchecked")
    @Override
    <V> V getPictureResource(AJAXRequestData req, ServerSession session, boolean eTagOnly) throws OXException {
        final ContactHalo contactHalo = services.getService(ContactHalo.class);
        if (null == contactHalo) {
            throw ServiceExceptionCode.absentService(ContactHalo.class);
        }

        Integer userId = null;
        String objectId = null;
        String folderId = null;
        ArrayList<String> emails = new ArrayList<>(3);
        boolean hadCriterium = false;
        if (req.isSet("internal_userid")) {
            hadCriterium = true;
            userId = I(req.getIntParameter("internal_userid"));
        } else if (req.isSet("userid")) {
            hadCriterium = true;
            userId = I(req.getIntParameter("userid"));
        } else if (req.isSet("user_id")) {
            hadCriterium = true;
            userId = I(req.getIntParameter("user_id"));
        }

        if (req.isSet("id") && !hadCriterium) {
            objectId = req.getParameter("id");
            if (req.isSet("folder")) {
                hadCriterium = true;
                folderId = req.getParameter("folder");
            }
        }

        if (req.isSet("email")) {
            hadCriterium = true;
            emails.add(req.getParameter("email"));
        } else if (req.isSet("email1")) {
            hadCriterium = true;
            emails.add(req.getParameter("email1"));
        }

        if (req.isSet("email2")) {
            hadCriterium = true;
            emails.add(req.getParameter("email2"));
        }

        if (req.isSet("email3")) {
            hadCriterium = true;
            emails.add(req.getParameter("email3"));
        }

        if (!hadCriterium && eTagOnly) {
            return null;
        }
        String accountId = req.getParameter("account_id");


        PictureSearchData data = new PictureSearchData(userId, accountId, folderId, objectId, emails);
        try {
            if (eTagOnly) {
                return (V) services.getServiceSafe(ContactPictureService.class).getETag(session, data);
            }
            return (V) services.getServiceSafe(ContactPictureService.class).getPicture(session, data);
        } catch (OXException x) {
            return null;
        }
    }
}

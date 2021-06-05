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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.mail.ArchiveDataWrapper;
import com.openexchange.mail.FolderAndId;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ArchiveAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class ArchiveAction extends AbstractArchiveMailAction {

    /**
     * Initializes a new {@link ArchiveAction}.
     *
     * @param services
     */
    public ArchiveAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult performArchive(MailRequest req) throws OXException {
        try {
            /*
             * Read in parameters
             */
            final String folderId = req.getParameter(AJAXServlet.PARAMETER_FOLDERID);
            boolean useDefaultName = AJAXRequestDataTools.parseBoolParameter("useDefaultName", req.getRequest(), true);
            boolean createIfAbsent = AJAXRequestDataTools.parseBoolParameter("createIfAbsent", req.getRequest(), true);
            JSONArray jArray = ((JSONArray) req.getRequest().getData());
            if (null == jArray) {
                throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
            }
            /*
             * Archive mails
             */
            List<ArchiveDataWrapper> retval = archive(jArray, folderId, useDefaultName, createIfAbsent, req);
            if (retval == null) {
                return new AJAXRequestResult(Boolean.TRUE, "native");
            }
            JSONArray json = new JSONArray(retval.size());
            for (ArchiveDataWrapper obj : retval) {
                JSONObject tmp = new JSONObject(4);
                tmp.put("id", obj.getId());
                tmp.put("created", obj.isCreated());
                json.put(tmp);
            }
            return new AJAXRequestResult(json, "json");
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private List<ArchiveDataWrapper> archive(JSONArray jArray, String folderId, boolean useDefaultName, boolean createIfAbsent, MailRequest req) throws JSONException, OXException {
        ServerSession session = req.getSession();
        int length = jArray.length();
        if (folderId == null) {
            List<FolderAndId> paraList = new ArrayList<FolderAndId>(length);
            for (int i = 0; i < length; i++) {
                JSONObject jObject = jArray.getJSONObject(i);
                String folder = jObject.getString(AJAXServlet.PARAMETER_FOLDERID);
                String id = jObject.getString(AJAXServlet.PARAMETER_ID);
                paraList.add(new FolderAndId(folder, id));
            }
            /*
             * Do archive mails
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            return mailInterface.archiveMultipleMail(paraList, session, useDefaultName, createIfAbsent);
        }

        List<String> ids = new ArrayList<String>(length);
        for (int i = 0; i < length; i++) {
            ids.add(jArray.getString(i));
        }
        /*
         * Do archive mails
         */
        final MailServletInterface mailInterface = getMailInterface(req);
        return mailInterface.archiveMail(folderId, ids, session, useDefaultName, createIfAbsent);
    }

}

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

import static com.openexchange.ajax.requesthandler.AJAXRequestDataBuilder.request;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.Dispatchers;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.compose.share.ShareReference;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.ShareService;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link ResolveShareReference}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.READ)
public class ResolveShareReference extends AbstractMailAction {

    /**
     * Initializes a new {@link ResolveShareReference}.
     */
    public ResolveShareReference(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException, JSONException {
        // Require reference string
        String reference = null;
        {
            Object data = req.getRequest().getData();
            if (data instanceof JSONObject) {
                reference = ((JSONObject) data).optString("reference", null);
            }

            if (null == reference) {
                reference = req.checkParameter("reference");
            }
        }

        // Get the ShareService
        ShareService shareService = ServerServiceRegistry.getInstance().getService(ShareService.class);
        if (null == shareService) {
            throw ServiceExceptionCode.absentService(ShareService.class);
        }

        // Get the IDBasedFolderAccessFactory
        IDBasedFolderAccessFactory folderFactory = ServerServiceRegistry.getInstance().getService(IDBasedFolderAccessFactory.class);
        if (null == folderFactory) {
            throw ServiceExceptionCode.absentService(IDBasedFolderAccessFactory.class);
        }

        // Get the IDBasedFileAccessFactory
        IDBasedFileAccessFactory fileFactory = ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class);
        if (null == fileFactory) {
            throw ServiceExceptionCode.absentService(IDBasedFileAccessFactory.class);
        }

        // Resolve...
        ShareReference shareReference;
        try {
            shareReference = ShareReference.parseFromMime(reference);
        } catch (Exception e) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create(e);
        }

        // Validate requesting user
        ServerSession session = req.getSession();
        String token = shareReference.getShareToken();
        if (session.getContextId() != shareReference.getContextId() || session.getUserId() != shareReference.getUserId()) {
            throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
        }

        // Validate share token
        GuestInfo guest = shareService.resolveGuest(token);
        if (null == guest) {
            throw ShareExceptionCodes.UNKNOWN_SHARE.create(token);
        }

        // Validate folder
        String folderId = shareReference.getFolder().getId();
        {
            IDBasedFolderAccess folderAccess = folderFactory.createAccess(session);
            if (false == folderAccess.exists(folderId)) {
                throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
            }
        }

        // ... and generate its JSON representation
        JSONObject jReference = new JSONObject(8);
        jReference.put("shareToken", token);
        jReference.put("contextId", shareReference.getContextId());
        jReference.put("userId", shareReference.getUserId());
        {
            // List folder's items
            IDBasedFileAccess fileAccess = fileFactory.createAccess(session);
            SearchIterator<File> searchIterator = fileAccess.getDocuments(folderId, Collections.singletonList(File.Field.ID), File.Field.FILENAME, SortDirection.ASC).results();
            try {
                // Iterate them & query individual file item
                Dispatcher ox = getService(Dispatcher.class);
                AJAXRequestData originalRequestData = req.getRequest();

                JSONArray jFileObjs = new JSONArray(8);
                while (searchIterator.hasNext()) {
                    File file = searchIterator.next();
                    if (null != file) {
                        String fileId = file.getId();
                        AJAXRequestData requestData = request().session(session).module("files").action("get").params("id", fileId, "folder", folderId, "timezone", "utc").format("json").build(originalRequestData);
                        AJAXRequestResult requestResult = perform(requestData, ox, session);
                        JSONObject jFileObj = ((JSONObject) requestResult.getResultObject());
                        jFileObjs.put(jFileObj);
                    }
                }
                jReference.put("files", jFileObjs);

            } finally {
                SearchIterators.close(searchIterator);
            }
        }

        if (null != shareReference.getExpiration()) {
            jReference.put("expiration", shareReference.getExpiration().getTime());
        }
        if (null != shareReference.getPassword()) {
            jReference.put("password", shareReference.getPassword());
        }
        return new AJAXRequestResult(jReference, "json");
    }

    private AJAXRequestResult perform(AJAXRequestData requestData, Dispatcher ox, ServerSession session) throws OXException {
        AJAXRequestResult requestResult = null;
        Exception exc = null;
        try {
            requestResult = ox.perform(requestData, null, session);
            return requestResult;
        } catch (OXException x) {
            exc = x;
            throw x;
        } catch (RuntimeException x) {
            exc = x;
            throw MailExceptionCode.UNEXPECTED_ERROR.create(x, x.getMessage());
        } finally {
            Dispatchers.signalDone(requestResult, exc);
        }
    }

}

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
 *    trademarks of the OX Software GmbH. group of companies.
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
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
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
@Action(method = RequestMethod.PUT, name = "resolve_share_reference", description = "Resolves specified share reference.", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
}, requestBody = "A JSON object providing the share reference to resolve: {\"reference\":\"...\"}"
, responseDescription = "Response: The JSON representation for the resolved share reference.")
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
            shareReference = ShareReference.parseFromReferenceString(reference);
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
                    String fileId = searchIterator.next().getId();
                    AJAXRequestData requestData = request().session(session).module("files").action("get").params("id", fileId, "folder", folderId, "timezone", "utc").format("json").build(originalRequestData);
                    AJAXRequestResult requestResult = perform(requestData, ox, session);
                    JSONObject jFileObj = ((JSONObject) requestResult.getResultObject());
                    jFileObjs.put(jFileObj);
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
            // Omit result on error. Let the UI deal with this
            exc = x;
            throw x;
        } catch (RuntimeException x) {
            // Omit result on error. Let the UI deal with this
            exc = x;
            throw MailExceptionCode.UNEXPECTED_ERROR.create(x, x.getMessage());
        } finally {
            Dispatchers.signalDone(requestResult, exc);
        }
    }

}

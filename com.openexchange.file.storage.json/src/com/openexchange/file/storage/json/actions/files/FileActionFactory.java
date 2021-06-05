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

package com.openexchange.file.storage.json.actions.files;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link FileActionFactory}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileActionFactory implements AJAXActionServiceFactory {

    /**
     * The singleton instance of {@link FileActionFactory}
     */
    public static FileActionFactory INSTANCE = new FileActionFactory();

    private final Map<String, AJAXActionService> actions;

    /**
     * Initializes a new {@link FileActionFactory}.
     */
    protected FileActionFactory() {
        super();
        ImmutableMap.Builder<String, AJAXActionService> actions = ImmutableMap.builder();
        actions.put("new", new NewAction());
        actions.put("update", new UpdateAction());
        actions.put("delete", new DeleteAction());
        actions.put("detach", new DetachAction());
        actions.put("revert", new RevertAction());
        actions.put("lock", new LockAction());
        actions.put("unlock", new UnlockAction());
        actions.put("copy", new CopyAction());
        actions.put("move", new MoveAction());

        actions.put("upload", new UploadAction());

        actions.put("all", new AllAction());
        actions.put("updates", new UpdatesAction());
        actions.put("list", new ListAction());
        actions.put("versions", new VersionsAction());
        actions.put("get", new GetAction());
        actions.put("search", new SearchAction());
        actions.put("advancedSearch", new AdvancedSearchAction());
        actions.put("shares", new SharesAction());
        actions.put("notify", new NotifyAction());
        actions.put("backwardLink", new BackwardLinkAction());

        actions.put("saveAs", new SaveAsAction());

        actions.put("document", new DocumentAction());
        actions.put("zipdocuments", new ZipDocumentsAction());
        actions.put("zipfolder", new ZipFolderAction());

        actions.put("documentdelta", new DocumentDeltaAction());
        actions.put("documentsig", new DocumentSigAction());
        actions.put("documentpatch", new DocumentPatchAction());

        actions.put("checkname", new CheckNameAction());

        actions.put("restore", new RestoreAction());

        this.actions = actions.build();
    }

    @Override
    public AJAXActionService createActionService(final String action) throws OXException {
        final AJAXActionService handler = actions.get(action);
        if (handler == null) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION.create(action);
        }
        return handler;
    }

}

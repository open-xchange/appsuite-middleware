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

package com.openexchange.drive.json.action;

import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;

/**
 * {@link DriveActionFactory}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AJAXActionService> actions;

    public DriveActionFactory() {
        super();
        ImmutableMap.Builder<String, AJAXActionService> actions = ImmutableMap.builder();
        actions.put("syncfolders", new SyncFoldersAction());
        actions.put("syncfiles", new SyncFilesAction());
        actions.put("upload", new UploadAction());
        actions.put("download", new DownloadAction());
        actions.put("listen", new ListenAction());
        actions.put("quota", new QuotaAction());
        actions.put("settings", new SettingsAction());
        actions.put("subscribe", new SubscribeAction());
        actions.put("unsubscribe", new UnsubscribeAction());
        actions.put("updateToken", new UpdateTokenAction());
        actions.put("fileMetadata", new FileMetadataAction());
        actions.put("directoryMetadata", new DirectoryMetadataAction());
        actions.put("jump", new JumpAction());
        actions.put("subfolders", new SubfoldersAction());
        actions.put("getLink", new GetLinkAction());
        actions.put("updateLink", new UpdateLinkAction());
        actions.put("deleteLink", new DeleteLinkAction());
        actions.put("sendLink", new SendLinkAction());
        actions.put("updateFile", new UpdateFileAction());
        actions.put("updateFolder", new UpdateFolderAction());
        actions.put("getFile", new GetFileAction());
        actions.put("getFolder", new GetFolderAction());
        actions.put("shares", new SharesAction());
        actions.put("notify", new NotifyAction());
        actions.put("autocomplete", new AutocompleteAction());
        actions.put("trashStats", new TrashStatsAction());
        actions.put("emptyTrash", new EmptyTrashAction());
        actions.put("moveFile", new MoveFileAction());
        actions.put("moveFolder", new MoveFolderAction());
        actions.put("trashContents", new TrashContentsAction());
        actions.put("deleteFromTrash", new DeleteFromTrashAction());
        actions.put("restoreFromTrash", new RestoreFromTrashAction());
        actions.put("syncfolder", new SyncFolderAction());
        this.actions = actions.build();
    }

    @Override
    public AJAXActionService createActionService(String action) throws OXException {
        return actions.get(action);
    }

}

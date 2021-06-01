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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AbstractFileFieldHandler;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.meta.FileFieldSet;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.util.GetSwitch;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.java.Streams;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link SaveAsAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SaveAsAction extends AbstractWriteAction {

    /**
     * Initialises a new {@link SaveAsAction}.
     */
    public SaveAsAction() {
        super();
    }

    @Override
    public AJAXRequestResult handle(InfostoreRequest request) throws OXException {
        request.require(Param.FOLDER_ID, Param.ATTACHED_ID, Param.MODULE, Param.ATTACHMENT).requireFileMetadata();

        int folderId = Integer.parseInt(request.getFolderId());
        int attachedId = request.getAttachedId();
        int moduleId = request.getModule();
        int attachment = request.getAttachment();

        final File file = request.getFile();
        final List<Field> sentColumns = request.getSentColumns();

        AttachmentBase attachments = request.getAttachmentBase();
        if (attachments == null) {
            throw ServiceExceptionCode.absentService(AttachmentBase.class);
        }
        IDBasedFileAccess fileAccess = request.getFileAccess();

        final ServerSession session = request.getSession();

        final User user = session.getUser();
        final UserConfiguration userConfiguration = session.getUserConfiguration();

        final AttachmentMetadata att = attachments.getAttachment(session, folderId, attachedId, moduleId, attachment, session.getContext(), user, userConfiguration);

        final FileFieldSet fileSet = new FileFieldSet();
        final GetSwitch attGet = new GetSwitch(att);

        File.Field.forAllFields(new AbstractFileFieldHandler() {

            @Override
            public Object handle(final Field field, final Object... args) {

                if (sentColumns.contains(field)) {
                    return null; // SKIP
                }

                // Otherwise copy from attachment

                final AttachmentField matchingAttachmentField = getMatchingAttachmentField(field);
                if (matchingAttachmentField == null) {
                    return null; // Not a field to copy
                }

                final Object value = matchingAttachmentField.doSwitch(attGet);
                field.doSwitch(fileSet, file, value);

                return null;
            }

        });

        file.setId(FileStorageFileAccess.NEW);
        scan(request, () -> attachments.getAttachedFile(session, folderId, attachedId, moduleId, attachment, session.getContext(), user, userConfiguration), att);
        InputStream fileData = attachments.getAttachedFile(session, folderId, attachedId, moduleId, attachment, session.getContext(), user, userConfiguration);
        try {
            /*
             * save attachment in storage, ignoring potential warnings
             */
            List<Field> modifiedColumns = null != sentColumns ? new ArrayList<>(sentColumns) : new ArrayList<>();
            modifiedColumns.add(Field.FILENAME);
            modifiedColumns.add(Field.FILE_SIZE);
            modifiedColumns.add(Field.FILE_MIMETYPE);
            modifiedColumns.add(Field.TITLE);
            modifiedColumns.add(Field.DESCRIPTION);
            String newID = fileAccess.saveDocument(file, fileData, FileStorageFileAccess.UNDEFINED_SEQUENCE_NUMBER, modifiedColumns, false, true, false);
            AJAXRequestResult result = new AJAXRequestResult(newID, new Date(file.getSequenceNumber()));
            List<OXException> warnings = fileAccess.getAndFlushWarnings();
            if (null != warnings && 0 < warnings.size()) {
                result.addWarnings(warnings);
            }
            return result;
        } finally {
            Streams.close(fileData);
        }
    }

    protected AttachmentField getMatchingAttachmentField(final File.Field fileField) {
        switch (fileField) {
            case FILENAME:
                return AttachmentField.FILENAME_LITERAL;
            case FILE_SIZE:
                return AttachmentField.FILE_SIZE_LITERAL;
            case FILE_MIMETYPE:
                return AttachmentField.FILE_MIMETYPE_LITERAL;
            case TITLE:
                return AttachmentField.FILENAME_LITERAL;
            case DESCRIPTION:
                return AttachmentField.COMMENT_LITERAL;
            default:
                return null;
        }
    }
}

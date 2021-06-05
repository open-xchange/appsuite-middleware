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

package com.openexchange.groupware.infostore.validation;

import java.util.Set;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link FilenamesMayNotContainSlashesValidator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class FilenamesMayNotContainSlashesValidator implements InfostoreValidator {

    private static final String NAME = FilenamesMayNotContainSlashesValidator.class.getSimpleName();

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public DocumentMetadataValidation validate(ServerSession session, DocumentMetadata metadata, DocumentMetadata originalDocument, Set<Metadata> updatedColumns) {
        String filename = metadata.getFileName();
        DocumentMetadataValidation validation = new DocumentMetadataValidation();
        if (filename != null && filename.indexOf('/') >= 0) {
            validation.setError(Metadata.FILENAME_LITERAL, "Filenames may not contain slashes.");
        }
        validation.setException(InfostoreExceptionCodes.VALIDATION_FAILED_SLASH.create());
        return validation;
    }

}

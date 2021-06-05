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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.L;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.config.lean.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link PermissionSizeValidator}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class PermissionSizeValidator implements InfostoreValidator {

    private static final Logger LOG = LoggerFactory.getLogger(PermissionSizeValidator.class);
    public static final Property MAX_OBJECT_PERMISSIONS = DefaultProperty.valueOf("com.openexchange.infostore.maxPermissionEntities", I(100));

    @Override
    public DocumentMetadataValidation validate(ServerSession session, DocumentMetadata metadata, DocumentMetadata originalDocument, Set<Metadata> updatedColumns) {
        return checkFilePermissions(session, metadata, Optional.ofNullable(originalDocument));
    }

    @Override
    public String getName() {
        return PermissionSizeValidator.class.getSimpleName();
    }

    /**
     * Checks if the file contains too many permissions.
     *
     * @param session The user session
     * @param file The file to check
     * @param previousFile The previous file in case of an update
     * @param result The {@link DocumentMetadataValidation}
     * @throws OXException in case the file contains too many permissions
     */
    private DocumentMetadataValidation checkFilePermissions(Session session, DocumentMetadata file, Optional<DocumentMetadata> previousFile) {
        DocumentMetadataValidation result = new DocumentMetadataValidation();
        if (file == null) {
            return result;
        }
        List<ObjectPermission> perms = file.getObjectPermissions();
        if (perms != null) {
            try {
                LeanConfigurationService lean = ServerServiceRegistry.getServize(LeanConfigurationService.class, true);
                int max = i((Integer) MAX_OBJECT_PERMISSIONS.getDefaultValue());
                if (lean == null) {
                    LOG.warn("Missing {} service. Falling back to default value of {}.", LeanConfigurationService.class.getSimpleName(), MAX_OBJECT_PERMISSIONS.getFQPropertyName());
                } else {
                    max = lean.getIntProperty(session.getUserId(), session.getContextId(), MAX_OBJECT_PERMISSIONS);
                }
                if (max > 0 && perms.size() > max) {
                    if (previousFile.isPresent() && previousFile.get().getObjectPermissions() != null && previousFile.get().getObjectPermissions().size() >= perms.size()) {
                        LOG.debug("Updated file with id {} in folder {} contains too many permissions but accept it anyway, because the overall number didn't increase.", I(previousFile.get().getId()), L(previousFile.get().getFolderId()));
                        return result;
                    }
                    result.setError(Metadata.OBJECT_PERMISSIONS_LITERAL, InfostoreExceptionCodes.TOO_MANY_PERMISSIONS.getMessage());
                    result.setException(InfostoreExceptionCodes.TOO_MANY_PERMISSIONS.create());
                }
            } catch (OXException e) {
                result.setError(Metadata.OBJECT_PERMISSIONS_LITERAL, e.getMessage());
                result.setException(e);
            }
        }
        return result;
    }

}

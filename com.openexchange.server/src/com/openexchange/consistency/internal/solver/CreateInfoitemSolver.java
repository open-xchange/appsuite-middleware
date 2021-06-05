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

package com.openexchange.consistency.internal.solver;

import static com.openexchange.java.Autoboxing.I;
import java.text.MessageFormat;
import java.util.Set;
import com.openexchange.consistency.Entity;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.user.User;

/**
 * {@link CreateInfoitemSolver}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class CreateInfoitemSolver implements ProblemSolver {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CreateInfoitemSolver.class);

    private static final String description = "This file needs attention";

    private static final String title = "Restoredfile";

    private static final String fileName = "Restoredfile";

    private static final String versioncomment = "";

    private static final String categories = "";

    private final DatabaseImpl database;

    private final User admin;

    public CreateInfoitemSolver(final DatabaseImpl database, final User admin) {
        super();
        this.database = database;
        this.admin = admin;
    }

    @Override
    public void solve(final Entity entity, final Set<String> problems) {
        final DocumentMetadata document = new DocumentMetadataImpl();
        document.setDescription(description);
        document.setTitle(title);
        document.setFileName(fileName);
        document.setVersionComment(versioncomment);
        document.setCategories(categories);

        for (final String identifier : problems) {
            try {
                Context context = entity.getContext();
                int fsOwner = database.getDocumentHolderFor(identifier, context);
                if (fsOwner < 0) {
                    fsOwner = admin.getId();
                    LOG.warn("No document holder found for identifier {} in context {}. Assigning to context admin.", identifier, I(context.getContextId()));
                }

                QuotaFileStorage storage = FileStorages.getQuotaFileStorageService().getQuotaFileStorage(fsOwner, context.getContextId(), Info.drive());
                try {
                    document.setFileSize(storage.getFileSize(identifier));
                    document.setFileMIMEType(storage.getMimeType(identifier));
                    database.startTransaction();
                    final int[] numbers = database.saveDocumentMetadata(identifier, document, admin, context);
                    database.commit();
                    if (numbers[2] == 1) {
                        LOG.info(MessageFormat.format("Dummy entry for {0} in database created. The admin of this context has now a new document", identifier));
                    }
                } catch (OXException e) {
                    LOG.error("{}", e.getMessage(), e);
                    try {
                        database.rollback();
                        return;
                    } catch (OXException e1) {
                        LOG.debug("{}", e1.getMessage(), e1);
                    }
                } catch (RuntimeException e) {
                    LOG.error("{}", e.getMessage(), e);
                    try {
                        database.rollback();
                        return;
                    } catch (OXException e1) {
                        LOG.debug("{}", e1.getMessage(), e1);
                    }
                } finally {
                    try {
                        database.finish();
                    } catch (OXException e) {
                        LOG.debug("{}", e.getMessage(), e);
                    }
                }

            } catch (OXException e) {
                LOG.error("{}", e.getMessage(), e);
            }
        }
    }

    @Override
    public String description() {
        return "create infoitem";
    }
}

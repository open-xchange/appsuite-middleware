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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.consistency.solver;

import java.text.MessageFormat;
import java.util.Set;
import com.openexchange.consistency.Entity;
import com.openexchange.consistency.Entity.EntityType;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.ldap.User;

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

    private FileStorage storage;

    private final User admin;

    public CreateInfoitemSolver(final DatabaseImpl database, final FileStorage storage, final User admin) {
        super();
        this.database = database;
        this.storage = storage;
        this.admin = admin;
    }

    @Override
    public void solve(final Entity entity, final Set<String> problems) {
        if (entity.getType().equals(EntityType.Context)) {
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
                        LOG.warn("No document holder found for identifier {} in context {}. Assigning to context admin.", identifier, context.getContextId());
                    }

                    QuotaFileStorage storage = FileStorages.getQuotaFileStorageService().getQuotaFileStorage(fsOwner, context.getContextId());
                    try {
                        document.setFileSize(storage.getFileSize(identifier));
                        document.setFileMIMEType(storage.getMimeType(identifier));
                        database.startTransaction();
                        final int[] numbers = database.saveDocumentMetadata(identifier, document, admin, context);
                        database.commit();
                        if (numbers[2] == 1) {
                            LOG.info(MessageFormat.format("Dummy entry for {0} in database created. The admin of this context has now a new document", identifier));
                        }
                    } catch (final OXException e) {
                        LOG.error("", e);
                        try {
                            database.rollback();
                            return;
                        } catch (final OXException e1) {
                            LOG.debug("", e1);
                        }
                    } catch (final RuntimeException e) {
                        LOG.error("", e);
                        try {
                            database.rollback();
                            return;
                        } catch (final OXException e1) {
                            LOG.debug("", e1);
                        }
                    } finally {
                        try {
                            database.finish();
                        } catch (final OXException e) {
                            LOG.debug("", e);
                        }
                    }

                } catch (OXException e) {
                    LOG.error("", e);
                }
            }
        }
    }

    @Override
    public String description() {
        return "create infoitem";
    }
}

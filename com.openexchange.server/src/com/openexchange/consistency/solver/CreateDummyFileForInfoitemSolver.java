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

import java.util.Set;
import com.openexchange.consistency.Entity;
import com.openexchange.consistency.Entity.EntityType;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.Info;
import com.openexchange.filestore.QuotaFileStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.ldap.User;

/**
 * {@link CreateDummyFileForInfostoreItemSolver}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class CreateDummyFileForInfoitemSolver extends CreateDummyFileSolver implements ProblemSolver {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CreateDummyFileForInfoitemSolver.class);

    private final DatabaseImpl database;

    private final User admin;

    /**
     * Initialises a new {@link CreateDummyFileForInfoitemSolver}.
     *
     * @param database The database
     * @param storage
     * @param admin
     */
    public CreateDummyFileForInfoitemSolver(final DatabaseImpl database, final FileStorage storage, User admin) {
        super(storage);
        this.database = database;
        this.admin = admin;
    }

    @Override
    public void solve(final Entity entity, final Set<String> problems) {
        if (entity.getType().equals(EntityType.Context)) {
            /*
             * Here we operate in two stages. First we create a dummy entry in the filestore. Second we update the Entries in the database
             */
            for (final String old_identifier : problems) {
                try {
                    Context context = entity.getContext();
                    int fsOwner = database.getDocumentHolderFor(old_identifier, context);

                    if (fsOwner < 0) {
                        LOG.warn("No document holder found for identifier {} in context {}. Assigning to context admin.", old_identifier, context.getContextId());
                        fsOwner = admin.getId();
                    }

                    QuotaFileStorage storage = FileStorages.getQuotaFileStorageService().getQuotaFileStorage(fsOwner, context.getContextId(), Info.drive(fsOwner));
                    String identifier = createDummyFile(storage);
                    database.startTransaction();
                    int changed = database.modifyDocument(old_identifier, identifier, "\nCaution! The file has changed", "text/plain", context);
                    database.commit();
                    if (changed == 1) {
                        LOG.info("Modified entry for identifier {} in context {} to new dummy identifier {}", old_identifier, context.getContextId(), identifier);
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
            }
        }
    }

    @Override
    public String description() {
        return "Create dummy file for infoitem";
    }
}

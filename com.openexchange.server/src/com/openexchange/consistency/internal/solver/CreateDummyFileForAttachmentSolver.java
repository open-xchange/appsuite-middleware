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

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Set;
import com.openexchange.consistency.Entity;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.groupware.attach.AttachmentBase;

/**
 * {@link CreateDummyFileForAttachment}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class CreateDummyFileForAttachmentSolver extends CreateDummyFileSolver implements ProblemSolver {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CreateDummyFileForAttachmentSolver.class);

    private final AttachmentBase attachments;

    public CreateDummyFileForAttachmentSolver(final AttachmentBase attachments, final FileStorage storage) {
        super(storage);
        this.attachments = attachments;
    }

    @Override
    public void solve(final Entity entity, final Set<String> problems) {
        /*
         * Here we operate in two stages. First we create a dummy entry in the filestore. Second we update the Entries in the database
         */
        final int size = problems.size();
        final Iterator<String> it = problems.iterator();
        for (int k = 0; k < size; k++) {
            try {
                final String identifier = createDummyFile(storage);
                final String old_identifier = it.next();
                attachments.setTransactional(true);
                attachments.startTransaction();
                final int changed = attachments.modifyAttachment(old_identifier, identifier, "\nCaution! The file has changed", "text/plain", entity.getContext());
                attachments.commit();
                if (changed == 1) {
                    LOG.info(MessageFormat.format("Created dummy entry for: {0}. New identifier is: {1}", old_identifier, identifier));
                }
            } catch (OXException e) {
                LOG.error("{}", e.getMessage(), e);
                try {
                    attachments.rollback();
                    return;
                } catch (OXException e1) {
                    LOG.error("{}", e1.getMessage(), e1);
                }
            } catch (RuntimeException e) {
                LOG.error("{}", e.getMessage(), e);
                try {
                    attachments.rollback();
                    return;
                } catch (OXException e1) {
                    LOG.debug("{}", e1.getMessage(), e1);
                }
            } finally {
                try {
                    attachments.finish();
                } catch (OXException e) {
                    LOG.debug("{}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public String description() {
        return "Create dummy file for attachment";
    }
}

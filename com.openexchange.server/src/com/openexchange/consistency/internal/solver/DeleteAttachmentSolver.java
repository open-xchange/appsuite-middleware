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
import java.util.Iterator;
import java.util.Set;
import com.openexchange.consistency.Entity;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;

/**
 * {@link DeleteAttachmentSolver}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class DeleteAttachmentSolver implements ProblemSolver {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeleteAttachmentSolver.class);

    private final AttachmentBase attachments;

    public DeleteAttachmentSolver(final AttachmentBase attachments) {
        this.attachments = attachments;
    }

    @Override
    public void solve(final Entity entity, final Set<String> problems) {
        // Now we go through the set an delete each superfluous entry:
        final Iterator<String> it = problems.iterator();
        while (it.hasNext()) {
            try {
                final String identifier = it.next();
                attachments.setTransactional(true);
                attachments.startTransaction();
                final int[] numbers = attachments.removeAttachment(identifier, entity.getContext());
                attachments.commit();
                if (numbers[0] == 1) {
                    LOG.info(MessageFormat.format("Inserted entry for identifier {0} and Context {1} in del_attachments", identifier, I(entity.getContext().getContextId())));
                }
                if (numbers[1] == 1) {
                    LOG.info(MessageFormat.format("Removed attachment database entry for: {0}", identifier));
                }
            } catch (OXException e) {
                LOG.debug("{}", e.getMessage(), e);
                try {
                    attachments.rollback();
                    return;
                } catch (OXException e1) {
                    LOG.debug("{}", e1.getMessage(), e1);
                }
                return;
            } catch (RuntimeException e) {
                LOG.error("{}", e.getMessage(), e);
                try {
                    attachments.rollback();
                    return;
                } catch (OXException e1) {
                    LOG.debug("{}", e1.getMessage(), e1);
                }
                return;
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
        return "delete attachment";
    }
}

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
import java.util.Iterator;
import java.util.Set;
import com.openexchange.consistency.Entity;
import com.openexchange.consistency.Entity.EntityType;
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
        if (entity.getType().equals(EntityType.Context)) {
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
                        LOG.info(MessageFormat.format("Inserted entry for identifier {0} and Context {1} in del_attachments", identifier, entity.getContext().getContextId()));
                    }
                    if (numbers[1] == 1) {
                        LOG.info(MessageFormat.format("Removed attachment database entry for: {0}", identifier));
                    }
                } catch (final OXException e) {
                    LOG.debug("", e);
                    try {
                        attachments.rollback();
                        return;
                    } catch (final OXException e1) {
                        LOG.debug("", e1);
                    }
                    return;
                } catch (final RuntimeException e) {
                    LOG.error("", e);
                    try {
                        attachments.rollback();
                        return;
                    } catch (final OXException e1) {
                        LOG.debug("", e1);
                    }
                    return;
                } finally {
                    try {
                        attachments.finish();
                    } catch (final OXException e) {
                        LOG.debug("", e);
                    }
                }
            }
        }
    }

    @Override
    public String description() {
        return "delete attachment";
    }
}

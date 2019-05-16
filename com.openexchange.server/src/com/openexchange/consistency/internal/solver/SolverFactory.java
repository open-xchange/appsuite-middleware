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

package com.openexchange.consistency.internal.solver;

import com.openexchange.consistency.RepairAction;
import com.openexchange.filestore.FileStorage;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.ldap.User;

/**
 * {@link SolverFactory}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class SolverFactory {

    /**
     * Creates a new InfoItem {@link ProblemSolver}
     *
     * @param action The repair action
     * @param database The database implementation
     * @param storage the file storage
     * @param admin The admin user
     * @return The new {@link ProblemSolver}
     */
    public static final ProblemSolver createInfoItemSolver(RepairAction action, DatabaseImpl database, FileStorage storage, User admin) {
        switch (action) {
            case CREATE_DUMMY:
                return new CreateDummyFileForInfoitemSolver(database, storage, admin);
            case DELETE:
                return new DeleteInfoitemSolver(database);
            case CREATE_ADMIN_INFOITEM:
            default:
                return new DoNothingSolver();
        }
    }

    /**
     * Creates a new attachment {@link ProblemSolver}
     *
     * @param action The repair action
     * @param attachmentBase The attachment base
     * @param storage the file storage
     * @return The new {@link ProblemSolver}
     */
    public static final ProblemSolver createAttachmentSolver(RepairAction action, AttachmentBase attachmentBase, FileStorage storage) {
        switch (action) {
            case CREATE_DUMMY:
                return new CreateDummyFileForAttachmentSolver(attachmentBase, storage);
            case DELETE:
                return new DeleteAttachmentSolver(attachmentBase);
            case CREATE_ADMIN_INFOITEM:
            default:
                return new DoNothingSolver();
        }
    }

    /**
     * Creates a new snippet {@link ProblemSolver}
     *
     * @param action The repair action
     * @param storage The file storage
     * @return the new {@link ProblemSolver}
     */
    public static final ProblemSolver createSnippetSolver(RepairAction action, FileStorage storage) {
        switch (action) {
            case CREATE_DUMMY:
                return new CreateDummyFileForSnippetSolver(storage);
            case DELETE:
                return new DeleteSnippetSolver();
            case CREATE_ADMIN_INFOITEM:
            default:
                return new DoNothingSolver();
        }
    }

    /**
     * Creates a new vcard {@link ProblemSolver}
     *
     * @param action The repair action
     * @param storage The file storage
     * @return the new {@link ProblemSolver}
     */
    public static final ProblemSolver createVCardSolver(RepairAction action) {
        switch (action) {
            case DELETE:
                return new DeleteBrokenVCardReferencesSolver();
            case CREATE_DUMMY:
            case CREATE_ADMIN_INFOITEM:
            default:
                return new DoNothingSolver();
        }
    }

    /**
     * Creates a new composition space attachment {@link ProblemSolver}
     *
     * @param action The repair action
     * @param storage The file storage
     * @return the new {@link ProblemSolver}
     */
    public static final ProblemSolver createCompositionSpaceAttachmentSolver(RepairAction action, FileStorage storage) {
        switch (action) {
            case DELETE:
                return new DeleteBrokenMailComposeAttachmentReferenceResolver();
            case CREATE_DUMMY:
                return new CreateDummyFileForMailComposeAttachmentSolver(storage);
            case CREATE_ADMIN_INFOITEM:
            default:
                return new DoNothingSolver();
        }
    }

    /**
     * Creates a new missing entry {@link ProblemSolver}
     *
     * @param action The repair action
     * @param storage The file storage
     * @return the new {@link ProblemSolver}
     */
    public static final ProblemSolver createMissingEntrySolver(RepairAction action, DatabaseImpl database, FileStorage storage, User admin) {
        switch (action) {
            case CREATE_ADMIN_INFOITEM:
                return new CreateInfoitemSolver(database, admin);
            case DELETE:
                return new RemoveFileSolver(storage);
            case CREATE_DUMMY:
            default:
                return new DoNothingSolver();
        }
    }
}

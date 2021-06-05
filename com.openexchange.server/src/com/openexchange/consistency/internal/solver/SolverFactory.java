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

import com.openexchange.consistency.RepairAction;
import com.openexchange.filestore.FileStorage;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.user.User;

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

    /**
     * Creates a preview solver
     *
     * @param action The repair action
     * @return the new {@link ProblemSolver}
     */
    public static final ProblemSolver createPreviewSolver(RepairAction action) {
        switch (action) {
            case DELETE:
                return new DeleteBrokenPreviewReferencesSolver();
            default:
                return new DoNothingSolver();
        }

    }
}

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

import com.openexchange.consistency.ConsistencyExceptionCodes;
import com.openexchange.consistency.RepairAction;
import com.openexchange.consistency.RepairPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.user.User;

/**
 * {@link PolicyResolver}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class PolicyResolver {

    private final ProblemSolver dbSolver;
    private final ProblemSolver attachmentSolver;
    private final ProblemSolver snippetSolver;
    private final ProblemSolver fileSolver;
    private final ProblemSolver vCardSolver;
    private final ProblemSolver csReferencesSolver;
    private final ProblemSolver previewSolver;

    /**
     * Initializes a new {@link PolicyResolver}.
     *
     * @param dbSolver The db {@link ProblemSolver}
     * @param attachmentSolver The attachment {@link ProblemSolver}
     * @param snippetSolver The snippet {@link ProblemSolver}
     * @param fileSolver The file {@link ProblemSolver}
     * @param vCardSolver The VCard {@link ProblemSolver}
     * @param csAttachmentSolver The composition space references {@link ProblemSolver}
     * @param previewSolver The preview {@link ProblemSolver}
     */
    private PolicyResolver(ProblemSolver dbSolver, ProblemSolver attachmentSolver, ProblemSolver snippetSolver, ProblemSolver fileSolver, ProblemSolver vCardSolver, ProblemSolver csAttachmentSolver, ProblemSolver previewSolver) {
        this.dbSolver = dbSolver;
        this.attachmentSolver = attachmentSolver;
        this.snippetSolver = snippetSolver;
        this.fileSolver = fileSolver;
        this.vCardSolver = vCardSolver;
        this.csReferencesSolver = csAttachmentSolver;
        this.previewSolver = previewSolver;
    }

    /**
     * Gets the dbSolver
     *
     * @return The dbSolver
     */
    public ProblemSolver getDbSolver() {
        return dbSolver;
    }

    /**
     * Gets the attachmentSolver
     *
     * @return The attachmentSolver
     */
    public ProblemSolver getAttachmentSolver() {
        return attachmentSolver;
    }

    /**
     * Gets the snippetSolver
     *
     * @return The snippetSolver
     */
    public ProblemSolver getSnippetSolver() {
        return snippetSolver;
    }
    
    
    /**
     * Gets the previewSolver
     *
     * @return The previewSolver
     */
    public ProblemSolver getPreviewSolver() {
        return previewSolver;
    }

    /**
     * Gets the fileSolver
     *
     * @return The fileSolver
     */
    public ProblemSolver getFileSolver() {
        return fileSolver;
    }

    /**
     * Gets the vCardSolver
     *
     * @return The vCardSolver
     */
    public ProblemSolver getvCardSolver() {
        return vCardSolver;
    }

    /**
     * Gets the composition space references solver
     *
     * @return The composition space references solver
     */
    public ProblemSolver getCompositionSpaceReferencesSolver() {
        return csReferencesSolver;
    }

    /**
     * Builds a {@link PolicyResolver} with the specified repair policy and action
     *
     * @param policy The repair policy
     * @param action The repair action
     * @param database The database implementation
     * @param attach the attachment base
     * @param storage The file storage
     * @param admin the admin user
     * @return The new {@link PolicyResolver}
     * @throws OXException if an unknown/unsupported policy is detected
     */
    public static PolicyResolver build(RepairPolicy policy, RepairAction action, DatabaseImpl database, AttachmentBase attach, FileStorage storage, User admin) throws OXException {
        ProblemSolver dbsolver = new DoNothingSolver();
        ProblemSolver attachmentsolver = new DoNothingSolver();
        ProblemSolver snippetsolver = new DoNothingSolver();
        ProblemSolver previewSolver = new DoNothingSolver();
        ProblemSolver filesolver = new DoNothingSolver();
        ProblemSolver vCardSolver = new DoNothingSolver();
        ProblemSolver csReferencesSolver = new DoNothingSolver();

        switch (policy) {
            case MISSING_ENTRY_FOR_FILE:
                filesolver = SolverFactory.createMissingEntrySolver(action, database, storage, admin);
                break;
            case MISSING_FILE_FOR_ATTACHMENT:
                attachmentsolver = SolverFactory.createAttachmentSolver(action, attach, storage);
                break;
            case MISSING_FILE_FOR_INFOITEM:
                dbsolver = SolverFactory.createInfoItemSolver(action, database, storage, admin);
                break;
            case MISSING_FILE_FOR_SNIPPET:
                snippetsolver = SolverFactory.createSnippetSolver(action, storage);
                break;
            case MISSING_FILE_FOR_VCARD:
                vCardSolver = SolverFactory.createVCardSolver(action);
                break;
            case MISSING_ATTACHMENT_FILE_FOR_MAIL_COMPOSE:
                csReferencesSolver = SolverFactory.createCompositionSpaceAttachmentSolver(action, storage);
                break;
            case MISSING_FILE_FOR_PREVIEW:
                previewSolver= SolverFactory.createPreviewSolver(action);
                break;
            default:
                throw ConsistencyExceptionCodes.UNKNOWN_POLICY.create(policy);
        }
        return new PolicyResolver(dbsolver, attachmentsolver, snippetsolver, filesolver, vCardSolver, csReferencesSolver, previewSolver);
    }
}

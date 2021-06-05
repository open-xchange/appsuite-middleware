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

package com.openexchange.consistency.internal;

import com.openexchange.consistency.internal.solver.ProblemSolver;

/**
 * {@link ProblemSolversToUse} - Provides the problem solvers to check a certain entity.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class ProblemSolversToUse {

    /**
     * Creates a new builder instance for given entity.
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>CheckEntityArgs</code> */
    public static class Builder {

        private ProblemSolver databaseSolver;
        private ProblemSolver attachmentSolver;
        private ProblemSolver snippetSolver;
        private ProblemSolver previewSolver;
        private ProblemSolver fileSolver;
        private ProblemSolver vCardSolver;
        private ProblemSolver compositionSpaceReferencesSolver;

        Builder() {
            super();
        }

        /**
         * Sets the database solver
         *
         * @param dbSolver The database solver to set
         */
        public Builder withDatabaseSolver(ProblemSolver databaseSolver) {
            this.databaseSolver = databaseSolver;
            return this;
        }

        /**
         * Sets the attachment solver
         *
         * @param attachmentSolver The attachment solver to set
         */
        public Builder withAttachmentSolver(ProblemSolver attachmentSolver) {
            this.attachmentSolver = attachmentSolver;
            return this;
        }

        /**
         * Sets the snippet solver
         *
         * @param snippetSolver The snippet solver to set
         */
        public Builder withSnippetSolver(ProblemSolver snippetSolver) {
            this.snippetSolver = snippetSolver;
            return this;
        }

        /**
         * Sets the preview solver
         *
         * @param previewSolver The preview solver to set
         */
        public Builder withPreviewSolver(ProblemSolver previewSolver) {
            this.previewSolver = previewSolver;
            return this;
        }

        /**
         * Sets the file solver
         *
         * @param fileSolver The file solver to set
         */
        public Builder withFileSolver(ProblemSolver fileSolver) {
            this.fileSolver = fileSolver;
            return this;
        }

        /**
         * Sets the vCard solver
         *
         * @param vCardSolver The vCard solver to set
         */
        public Builder withVCardSolver(ProblemSolver vCardSolver) {
            this.vCardSolver = vCardSolver;
            return this;
        }

        /**
         * Sets the composition space references solver
         *
         * @param compositionSpaceReferencesSolver The composition space references solver to set
         */
        public Builder withCompositionSpaceReferencesSolver(ProblemSolver compositionSpaceReferencesSolver) {
            this.compositionSpaceReferencesSolver = compositionSpaceReferencesSolver;
            return this;
        }

        /**
         * Builds the resulting instance of <code>CheckEntityArgs</code> from this builder's arguments.
         *
         * @return The <code>CheckEntityArgs</code> instance
         */
        public ProblemSolversToUse build() {
            return new ProblemSolversToUse(databaseSolver, attachmentSolver, snippetSolver, previewSolver, fileSolver, vCardSolver, compositionSpaceReferencesSolver);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final ProblemSolver databaseSolver;
    private final ProblemSolver attachmentSolver;
    private final ProblemSolver snippetSolver;
    private final ProblemSolver previewSolver;
    private final ProblemSolver fileSolver;
    private final ProblemSolver vCardSolver;
    private final ProblemSolver compositionSpaceReferencesSolver;

    /**
     * Initializes a new {@link ProblemSolversToUse}.
     */
    ProblemSolversToUse(ProblemSolver databaseSolver, ProblemSolver attachmentSolver, ProblemSolver snippetSolver, ProblemSolver previewSolver, ProblemSolver fileSolver, ProblemSolver vCardSolver, ProblemSolver compositionSpaceReferencesSolver) {
        super();
        this.databaseSolver = databaseSolver;
        this.attachmentSolver = attachmentSolver;
        this.snippetSolver = snippetSolver;
        this.previewSolver = previewSolver;
        this.fileSolver = fileSolver;
        this.vCardSolver = vCardSolver;
        this.compositionSpaceReferencesSolver = compositionSpaceReferencesSolver;
    }

    /**
     * Gets the database solver
     *
     * @return The database solver
     */
    public ProblemSolver getDatabaseSolver() {
        return databaseSolver;
    }

    /**
     * Gets the attachment solver
     *
     * @return The attachment solver
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
     * Gets the preview sSolver
     *
     * @return The preview solver
     */
    public ProblemSolver getPreviewSolver() {
        return previewSolver;
    }

    /**
     * Gets the file solver
     *
     * @return The file solver
     */
    public ProblemSolver getFileSolver() {
        return fileSolver;
    }

    /**
     * Gets the vCard solver
     *
     * @return The vCard solver
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
        return compositionSpaceReferencesSolver;
    }

}

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

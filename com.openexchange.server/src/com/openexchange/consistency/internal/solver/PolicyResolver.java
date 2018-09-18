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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

import com.openexchange.consistency.ConsistencyExceptionCodes;
import com.openexchange.consistency.RepairAction;
import com.openexchange.consistency.RepairPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.infostore.database.impl.DatabaseImpl;
import com.openexchange.groupware.ldap.User;

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

    /**
     * Initialises a new {@link PolicyResolver}.
     * 
     * @param dbSolver
     * @param attachmentSolver
     * @param snippetSolver
     * @param fileSolver
     * @param vCardSolver
     */
    private PolicyResolver(ProblemSolver dbSolver, ProblemSolver attachmentSolver, ProblemSolver snippetSolver, ProblemSolver fileSolver, ProblemSolver vCardSolver) {
        this.dbSolver = dbSolver;
        this.attachmentSolver = attachmentSolver;
        this.snippetSolver = snippetSolver;
        this.fileSolver = fileSolver;
        this.vCardSolver = vCardSolver;
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
        ProblemSolver filesolver = new DoNothingSolver();
        ProblemSolver vCardSolver = new DoNothingSolver();

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
            default:
                throw ConsistencyExceptionCodes.UNKNOWN_POLICY.create(policy);
        }
        return new PolicyResolver(dbsolver, attachmentsolver, snippetsolver, filesolver, vCardSolver);
    }
}

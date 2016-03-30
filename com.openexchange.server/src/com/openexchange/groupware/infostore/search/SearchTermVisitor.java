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

package com.openexchange.groupware.infostore.search;

import com.openexchange.exception.OXException;

/**
 * {@link SearchTermVisitor}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public interface SearchTermVisitor {

    /**
     * The visitation for AND term.
     *
     * @param andTerm The visited AND term
     * @throws OXException If visit attempt fails
     */
    void visit(AndTerm andTerm) throws OXException;

    /**
     * The visitation for OR term.
     *
     * @param orTerm The visited OR term
     * @throws OXException If visit attempt fails
     */
    void visit(OrTerm orTerm) throws OXException;

    /**
     * The visitation for not term.
     *
     * @param notTerm The visited not term
     * @throws OXException If visit attempt fails
     */
    void visit(NotTerm notTerm) throws OXException;

    /**
     * The visitation for meta term.
     *
     * @param metaTerm The visited meta term
     * @throws OXException If visit attempt fails
     */
    void visit(MetaTerm metaTerm) throws OXException;

    /**
     * The visitation for number-of-versions term.
     *
     * @param numberOfVersionsTerm The visited number-of-versions term
     * @throws OXException If visit attempt fails
     */
    void visit(NumberOfVersionsTerm numberOfVersionsTerm) throws OXException;

    /**
     * The visitation for last-modified UTC term.
     *
     * @param lastModifiedUtcTerm The visited last-modified UTC term
     * @throws OXException If visit attempt fails
     */
    void visit(LastModifiedUtcTerm lastModifiedUtcTerm) throws OXException;

    /**
     * The visitation for color label term.
     *
     * @param colorLabelTerm The visited color label term
     * @throws OXException If visit attempt fails
     */
    void visit(ColorLabelTerm colorLabelTerm) throws OXException;

    /**
     * The visitation for current version term.
     *
     * @param currentVersionTerm The current version term
     * @throws OXException If visit attempt fails
     */
    void visit(CurrentVersionTerm currentVersionTerm) throws OXException;

    /**
     * The visitation for version comment term.
     *
     * @param versionCommentTerm The version comment term
     * @throws OXException If visit attempt fails
     */
    void visit(VersionCommentTerm versionCommentTerm) throws OXException;

    /**
     * The visitation for file MD5 sum term.
     *
     * @param fileMd5SumTerm The file MD5 sum term
     * @throws OXException If visit attempt fails
     */
    void visit(FileMd5SumTerm fileMd5SumTerm) throws OXException;

    /**
     * The visitation for locked-until term.
     *
     * @param lockedUntilTerm The locked-until term
     * @throws OXException If visit attempt fails
     */
    void visit(LockedUntilTerm lockedUntilTerm) throws OXException;

    /**
     * The visitation for categories term.
     *
     * @param categoriesTerm The categories term
     * @throws OXException If visit attempt fails
     */
    void visit(CategoriesTerm categoriesTerm) throws OXException;

    /**
     * The visitation for sequence number term.
     *
     * @param sequenceNumberTerm The sequence number term
     * @throws OXException If visit attempt fails
     */
    void visit(SequenceNumberTerm sequenceNumberTerm) throws OXException;

    /**
     * The visitation for file MIME type term.
     *
     * @param fileMimeTypeTerm The file MIME type term
     * @throws OXException If visit attempt fails
     */
    void visit(FileMimeTypeTerm fileMimeTypeTerm) throws OXException;

    /**
     * The visitation for file name term.
     *
     * @param fileNameTerm The file name term
     * @throws OXException If visit attempt fails
     */
    void visit(FileNameTerm fileNameTerm) throws OXException;

    /**
     * The visitation for last modified term.
     *
     * @param lastModifiedTerm The visited last modified term
     * @throws OXException If visit attempt fails
     */
    void visit(LastModifiedTerm lastModifiedTerm) throws OXException;

    /**
     * The visitation for created term.
     *
     * @param createdTerm The visited created term
     * @throws OXException If visit attempt fails
     */
    void visit(CreatedTerm createdTerm) throws OXException;

    /**
     * The visitation for modified by term.
     *
     * @param modifiedByTerm The visited modified by term
     * @throws OXException If visit attempt fails
     */
    void visit(ModifiedByTerm modifiedByTerm) throws OXException;

    /**
     * The visitation for title term.
     *
     * @param titleTerm The visited title term
     * @throws OXException If visit attempt fails
     */
    void visit(TitleTerm titleTerm) throws OXException;

    /**
     * The visitation for version term.
     *
     * @param versionTerm The visited version term
     * @throws OXException If visit attempt fails
     */
    void visit(VersionTerm versionTerm) throws OXException;

    /**
     * The visitation for content term.
     *
     * @param contentTerm The visited content term
     * @throws OXException If visit attempt fails
     */
    void visit(ContentTerm contentTerm) throws OXException;

    /**
     * The visitation for file size term.
     *
     * @param fileSizeTerm The visited file size term
     * @throws OXException If visit attempt fails
     */
    void visit(FileSizeTerm fileSizeTerm) throws OXException;

    /**
     * The visitation for description term.
     *
     * @param descriptionTerm The visited description term
     * @throws OXException If visit attempt fails
     */
    void visit(DescriptionTerm descriptionTerm) throws OXException;

    /**
     * The visitation for url term.
     *
     * @param urlTerm The visited url term
     * @throws OXException If visit attempt fails
     */
    void visit(UrlTerm urlTerm) throws OXException;

    /**
     * The visitation for created by term.
     *
     * @param createdByTerm The visited created by term
     * @throws OXException If visit attempt fails
     */
    void visit(CreatedByTerm createdByTerm) throws OXException;
}

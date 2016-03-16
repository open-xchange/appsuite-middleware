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

package com.openexchange.file.storage.search;

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
     * @param term The visited AND term
     * @throws OXException If visit attempt fails
     */
    void visit(AndTerm term) throws OXException;

    /**
     * The visitation for OR term.
     *
     * @param term The visited OR term
     * @throws OXException If visit attempt fails
     */
    void visit(OrTerm term) throws OXException;

    /**
     * The visitation for not term.
     *
     * @param term The visited not term
     * @throws OXException If visit attempt fails
     */
    void visit(NotTerm term) throws OXException;

    /**
     * The visitation for meta term.
     *
     * @param term The visited meta term
     * @throws OXException If visit attempt fails
     */
    void visit(MetaTerm term) throws OXException;

    /**
     * The visitation for number-of-versions term.
     *
     * @param term The visited number-of-versions term
     * @throws OXException If visit attempt fails
     */
    void visit(NumberOfVersionsTerm term) throws OXException;

    /**
     * The visitation for last-modified UTC term.
     *
     * @param term The visited last-modified UTC term
     * @throws OXException If visit attempt fails
     */
    void visit(LastModifiedUtcTerm term) throws OXException;

    /**
     * The visitation for color label term.
     *
     * @param term The visited color label term
     * @throws OXException If visit attempt fails
     */
    void visit(ColorLabelTerm term) throws OXException;

    /**
     * The visitation for current version term.
     *
     * @param term The current version term
     * @throws OXException If visit attempt fails
     */
    void visit(CurrentVersionTerm term) throws OXException;

    /**
     * The visitation for version comment term.
     *
     * @param term The version comment term
     * @throws OXException If visit attempt fails
     */
    void visit(VersionCommentTerm term) throws OXException;

    /**
     * The visitation for file MD5 sum term.
     *
     * @param term The file MD5 sum term
     * @throws OXException If visit attempt fails
     */
    void visit(FileMd5SumTerm term) throws OXException;

    /**
     * The visitation for locked-until term.
     *
     * @param term The locked-until term
     * @throws OXException If visit attempt fails
     */
    void visit(LockedUntilTerm term) throws OXException;

    /**
     * The visitation for categories term.
     *
     * @param term The categories term
     * @throws OXException If visit attempt fails
     */
    void visit(CategoriesTerm term) throws OXException;

    /**
     * The visitation for sequence number term.
     *
     * @param term The sequence number term
     * @throws OXException If visit attempt fails
     */
    void visit(SequenceNumberTerm term) throws OXException;

    /**
     * The visitation for file MIME type term.
     *
     * @param term The file MIME type term
     * @throws OXException If visit attempt fails
     */
    void visit(FileMimeTypeTerm term) throws OXException;

    /**
     * The visitation for file name term.
     *
     * @param term The file name term
     * @throws OXException If visit attempt fails
     */
    void visit(FileNameTerm term) throws OXException;

    /**
     * The visitation for last modified term.
     *
     * @param term The visited last modified term
     * @throws OXException If visit attempt fails
     */
    void visit(LastModifiedTerm term) throws OXException;

    /**
     * The visitation for created term.
     *
     * @param term The visited created term
     * @throws OXException If visit attempt fails
     */
    void visit(CreatedTerm term) throws OXException;

    /**
     * The visitation for modified by term.
     *
     * @param term The visited modified by term
     * @throws OXException If visit attempt fails
     */
    void visit(ModifiedByTerm term) throws OXException;

    /**
     * The visitation for title term.
     *
     * @param term The visited title term
     * @throws OXException If visit attempt fails
     */
    void visit(TitleTerm term) throws OXException;

    /**
     * The visitation for version term.
     *
     * @param term The visited version term
     * @throws OXException If visit attempt fails
     */
    void visit(VersionTerm term) throws OXException;

    /**
     * The visitation for content term.
     *
     * @param term The visited content term
     * @throws OXException If visit attempt fails
     */
    void visit(ContentTerm term) throws OXException;

    /**
     * The visitation for file size term.
     *
     * @param term The visited file size term
     * @throws OXException If visit attempt fails
     */
    void visit(FileSizeTerm term) throws OXException;

    /**
     * The visitation for description term.
     *
     * @param term The visited description term
     * @throws OXException If visit attempt fails
     */
    void visit(DescriptionTerm term) throws OXException;

    /**
     * The visitation for url term.
     *
     * @param term The visited url term
     * @throws OXException If visit attempt fails
     */
    void visit(UrlTerm term) throws OXException;

    /**
     * The visitation for created by term.
     *
     * @param term The visited created by term
     * @throws OXException If visit attempt fails
     */
    void visit(CreatedByTerm term) throws OXException;
}

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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.solr;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;


/**
 * {@link SolrExceptionCodes}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum SolrExceptionCodes implements OXExceptionCode {

    /**
     * Could not find solr core entry for user %1$s and module %2$s in context %3$s. 
     */
    CORE_ENTRY_NOT_FOUND(SolrExceptionMessages.CORE_ENTRY_NOT_FOUND_MSG, Category.CATEGORY_ERROR, 1001),
    /**
     * Could not find solr core store for given attributes. %1$s.
     */
    CORE_STORE_ENTRY_NOT_FOUND(SolrExceptionMessages.CORE_STORE_ENTRY_NOT_FOUND_MSG, Category.CATEGORY_ERROR, 1002),
    /**
     * All core stores seem to be full.
     */
    NO_FREE_CORE_STORE(SolrExceptionMessages.NO_FREE_CORE_STORE_MSG, Category.CATEGORY_ERROR, 1003),
    /**
     * This cores instance directory (%1$s) already exists and its structure is inconsistent. 
     */
    INSTANCE_DIR_EXISTS(SolrExceptionMessages.INSTANCE_DIR_EXISTS_MSG, Category.CATEGORY_ERROR, 1004),
    /**
     * An index fault occurred: %1$s
     */
    INDEX_FAULT(SolrExceptionMessages.INDEX_FAULT_MSG, CATEGORY_ERROR, 1005),
    /**
     * The file or directory %1$s does not exist.
     */
    FILE_NOT_EXISTS_ERROR(SolrExceptionMessages.FILE_EXISTS_ERROR_MSG, CATEGORY_ERROR, 1006),
    /**
     * Could neither delegate solr request to a local nor to a remote server instance.
     */
    DELEGATION_ERROR(SolrExceptionMessages.DELEGATION_ERROR_MSG, CATEGORY_ERROR, 1007),
    /**
     * Could not parse URI: %1$s.
     */
    URI_PARSE_ERROR(SolrExceptionMessages.URI_PARSE_ERROR_MSG, Category.CATEGORY_ERROR, 1008),
    /**
     * Remote error: %1$s.
     */
    REMOTE_ERROR(SolrExceptionMessages.REMOTE_ERROR_MSG, Category.CATEGORY_ERROR, 1009),
    /**
     * Could not parse solr core identifier %1$s.
     */
    IDENTIFIER_PARSE_ERROR(SolrExceptionMessages.IDENTIFIER_PARSE_ERROR_MSG, Category.CATEGORY_ERROR, 1010),
    /**
     * Unknown module: %1$s.
     */
    UNKNOWN_MODULE(SolrExceptionMessages.UNKNOWN_MODULE_MSG, Category.CATEGORY_ERROR, 1011),
    /**
     * Can not reach solr core store. URI %1$s does not lead to an existing directory.
     */
    CORE_STORE_NOT_EXISTS_ERROR(SolrExceptionMessages.CORE_STORE_NOT_EXISTS_ERROR_MSG, Category.CATEGORY_ERROR, 1012),
    /**
     * The affected solr core %1$s is not started up yet. Please try again later.
     */
    CORE_NOT_STARTED(SolrExceptionMessages.CORE_NOT_STARTED_MSG, Category.CATEGORY_ERROR, 1013),
    /**
     * The document with uuid %1$s could not be found.
     */
    DOCUMENT_NOT_FOUND(SolrExceptionMessages.DOCUMENT_NOT_FOUND_MSG, Category.CATEGORY_ERROR, 1014),
    
    ;

    private final String message;

    private final int number;

    private final Category category;

    private SolrExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        number = detailNumber;
        this.category = category;
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getPrefix() {
        return "SOL";
    }

    @Override
    public String getMessage() {
        return message;
    }

}

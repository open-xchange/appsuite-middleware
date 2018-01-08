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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail;

import com.openexchange.mail.search.SearchTerm;

/**
 * {@link MailFetchArguments}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class MailFetchArguments {

    /**
     * Creates a copy.
     *
     * @param fetchArguments The fetch arguments to copy from
     * @return The copy
     */
    public static MailFetchArguments copy(MailFetchArguments fetchArguments) {
        return copy(fetchArguments, fetchArguments.fields, fetchArguments.headerNames);
    }

    /**
     * Creates a copy.
     *
     * @param fetchArguments The fetch arguments to copy from
     * @param fields The originally requested fields
     * @param headerNames The originally requested header names
     * @return The copy
     */
    public static MailFetchArguments copy(MailFetchArguments fetchArguments, MailField[] fields, String[] headerNames) {
        return new MailFetchArguments(fetchArguments.folder, fetchArguments.searchTerm, fetchArguments.sortField, fetchArguments.orderDir, fields, headerNames);
    }

    /**
     * Creates a new builder.
     *
     * @param folder The folder from which mails are fetched
     * @param fields The originally requested fields
     * @param headerNames The originally requested header names
     * @return The new builder
     */
    public static Builder builder(FullnameArgument folder, MailField[] fields, String[] headerNames) {
        return new Builder(folder, fields, headerNames);
    }

    /** The builder for an instance of <code>MailFetchArguments</code> */
    public static class Builder {

        private final FullnameArgument folder;
        private MailField[] fields;
        private String[] headerNames;
        private SearchTerm<?> searchTerm;
        private MailSortField sortField;
        private OrderDirection orderDir;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder(FullnameArgument folder, MailField[] fields, String[] headerNames) {
            super();
            this.folder = folder;
            this.fields = fields;
            this.headerNames = headerNames;
        }

        /**
         * Sets the originally requested fields
         *
         * @param fields The fields to set
         * @return This builder
         */
        public Builder setFields(MailField[] fields) {
            this.fields = fields;
            return this;
        }

        /**
         * Sets the originally requested header names
         *
         * @param headerNames The header names to set
         * @return This builder
         */
        public Builder setHeaderNames(String[] headerNames) {
            this.headerNames = headerNames;
            return this;
        }

        /**
         * Sets the search term.
         *
         * @param searchTerm The search term to set
         * @return This builder
         */
        public Builder setSearchTerm(SearchTerm<?> searchTerm) {
            this.searchTerm = searchTerm;
            return this;
        }

        /**
         * Sets the sort arguments.
         *
         * @param sortField The field to sort
         * @param orderDir The order direction
         * @return This builder
         */
        public Builder setSortOptions(MailSortField sortField, OrderDirection orderDir) {
            this.sortField = sortField;
            this.orderDir = orderDir;
            return this;
        }

        /**
         * Creates the appropriate <code>MailFetchArguments</code> instance from this builder's arguments.
         *
         * @return The <code>MailFetchArguments</code> instance
         */
        public MailFetchArguments build() {
            return new MailFetchArguments(folder, searchTerm, sortField, orderDir, fields, headerNames);
        }

    }

    // -------------------------------------------------------------------------------------------------

    private final FullnameArgument folder;
    private final SearchTerm<?> searchTerm;
    private final MailSortField sortField;
    private final OrderDirection orderDir;
    private final MailField[] fields;
    private final String[] headerNames;

    /**
     * Initializes a new {@link MailFetchArguments}.
     */
    MailFetchArguments(FullnameArgument folder, SearchTerm<?> searchTerm, MailSortField sortField, OrderDirection orderDir, MailField[] fields, String[] headerNames) {
        super();
        this.folder = folder;
        this.searchTerm = searchTerm;
        this.sortField = sortField;
        this.orderDir = orderDir;
        this.fields = fields;
        this.headerNames = headerNames;
    }

    /**
     * Gets the folder from which mails are fetched
     *
     * @return The folder from which mails are fetched
     */
    public FullnameArgument getFolder() {
        return folder;
    }

    /**
     * Gets the filtering search term or <code>null</code>
     *
     * @return The filtering search term or <code>null</code>
     */
    public SearchTerm<?> getSearchTerm() {
        return searchTerm;
    }

    /**
     * Gets the field to sort by or <code>null</code>
     *
     * @return The field to sort by or <code>null</code>
     */
    public MailSortField getSortField() {
        return sortField;
    }

    /**
     * Gets the order direction or <code>null</code>
     *
     * @return The order direction or <code>null</code>
     */
    public OrderDirection getOrderDir() {
        return orderDir;
    }

    /**
     * Gets the originally requested fields
     *
     * @return The originally requested fields
     */
    public MailField[] getFields() {
        return fields;
    }

    /**
     * Gets the originally requested header names
     *
     * @return The originally requested header names
     */
    public String[] getHeaderNames() {
        return headerNames;
    }

}

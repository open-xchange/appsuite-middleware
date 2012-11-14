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

package com.openexchange.index;

import java.util.Set;

/**
 * {@link QueryParameters} - Represents query parameters.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class QueryParameters {
    
    public static enum Order {
        ASC, DESC;
    }

    /**
     * The builder for {@link QueryParameters} instances.
     */
    public static final class Builder {

        int off;
        int len;
//        Set<String> folders;
        IndexField sortField;
        Order order;
//        Map<String, Object> parameters;
        SearchHandler handler;
        Object searchTerm;
        Set<AccountFolders> accountFolders;
        int module;
        Set<String> indexIds;

        /**
         * Initializes a new builder.
         */
//        public Builder(final Object searchTerm) {
//            super();
//            init();
//            this.searchTerm = searchTerm;
//        }
        
        public Builder() {
            super();
            init();
        }
        
        private void init() {
            off = 0;
            len = Integer.MAX_VALUE;
//            folders = null;
            sortField = null;
            order = null;
            handler = null;
            searchTerm = null;
            accountFolders = null;
            module = -1;
            indexIds = null;
        }

        /**
         * Initializes a new builder.
         */
//        public Builder(final Map<String, Object> parameters) {
//            super();
//            off = 0;
//            len = Integer.MAX_VALUE;
//            this.parameters = parameters;
//        }

        public Builder setSearchTerm(final Object searchTerm) {
            this.searchTerm = searchTerm;
            return this;
        }

        public Builder setOffset(final int off) {
            this.off = off;
            return this;
        }

        public Builder setLength(final int len) {
            this.len = len;
            return this;
        }
        
        public Builder setModule(final int module) {
            this.module = module;
            return this;
        }
        
        public Builder setIndexIds(final Set<String> indexIds) {
            this.indexIds = indexIds;
            return this;
        }

//        public Builder setParameters(final Map<String, Object> parameters) {
//            this.parameters = parameters;
//            return this;
//        }

        public Builder setHandler(final SearchHandler handler) {
            this.handler = handler;
            return this;
        }
        
//        public Builder setFolders(final Set<String> folders) {
//            this.folders = folders;
//            return this;
//        }
        
        public Builder setSortField(final IndexField sortField) {
            this.sortField = sortField;
            return this;
        }
        
        public Builder setOrder(final Order order) {
            this.order = order;
            return this;
        }
        
        /**
         * Sets accounts and corresponding folders to filter search results.
         * You can filter by multiple accounts and multiple folders per account.
         */
        public Builder setAccountFolders(final Set<AccountFolders> accountFolders) {
            this.accountFolders = accountFolders;
            return this;
        }

        public QueryParameters build() {
            return new QueryParameters(this);
        }
    }

    private final int off;

    private final int len;

//    private final Map<String, Object> parameters;

    private final SearchHandler handler;

    private final Object searchTerm;

//    private final Set<String> folders;
    
    private final IndexField sortField;
    
    private final Order order;
    
    private final Set<AccountFolders> accountFolders;
    
    private final int module;
    
    private final Set<String> indexIds;
    

    /**
     * Initializes a new {@link QueryParameters}.
     */
    QueryParameters(final Builder builder) {
        super();
        handler = builder.handler;
        len = builder.len;
        off = builder.off;
//        parameters = builder.parameters;
        searchTerm = builder.searchTerm;
//        folders = builder.folders;
        sortField = builder.sortField;
        order = builder.order;
        accountFolders = builder.accountFolders;
        module = builder.module;
        indexIds = builder.indexIds;
    }

    /**
     * Gets the search term or <code>null</code> if not set.
     *
     * @return The search term
     */
    public Object getSearchTerm() {
        return searchTerm;
    }

    /**
     * Gets the offset.
     * 
     * @return The offset
     */
    public int getOff() {
        return off;
    }

    /**
     * Gets the length.
     * 
     * @return The length
     */
    public int getLen() {
        return len;
    }
    
    public int getModule() {
        return module;
    }
    
    public Set<String> getIndexIds() {
        return indexIds;
    }
    
//    /**
//     * Gets the folder or <code>null</code> if not set.
//     * 
//     * @return The folder
//     */
//    public Set<String> getFolders() {
//        return folders;
//    }

//    /**
//     * Gets the parameters.
//     * 
//     * @return The parameters
//     */
//    public Map<String, Object> getParameters() {
//        return parameters;
//    }

    /**
     * Gets the handler.
     * 
     * @return The handler
     */
    public SearchHandler getHandler() {
        return handler;
    }
    
    /**
     * Gets the sortField
     *
     * @return The sortField
     */
    public IndexField getSortField() {
        return sortField;
    }
    
    /**
     * Gets the order
     *
     * @return The order
     */
    public Order getOrder() {
        return order;
    }
    
    /**
     * Gets the set of accounts and folders.
     * @return The account folders
     */
    public Set<AccountFolders> getAccountFolders() {
        return accountFolders;
    }

}

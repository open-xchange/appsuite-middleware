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

package com.openexchange.microsoft.graph.api;

/**
 * {@link MicrosoftGraphQueryParameters}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class MicrosoftGraphQueryParameters {

    private boolean count;
    private String expand;
    private String filter;
    private String format;
    private String orderBy;
    private String search;
    private String select;
    private int skip;
    private String skipToken;
    private int top;

    /**
     * Initialises a new {@link MicrosoftGraphQueryParameters}.
     */
    public MicrosoftGraphQueryParameters() {
        super();
    }

    /**
     * Gets the count
     *
     * @return The count
     */
    public boolean isCount() {
        return count;
    }

    /**
     * Gets the expand
     *
     * @return The expand
     */
    public String getExpand() {
        return expand;
    }

    /**
     * Gets the filter
     *
     * @return The filter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Gets the format
     *
     * @return The format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Gets the orderBy
     *
     * @return The orderBy
     */
    public String getOrderBy() {
        return orderBy;
    }

    /**
     * Gets the search
     *
     * @return The search
     */
    public String getSearch() {
        return search;
    }

    /**
     * Gets the select
     *
     * @return The select
     */
    public String getSelect() {
        return select;
    }

    /**
     * Gets the skip
     *
     * @return The skip
     */
    public int getSkip() {
        return skip;
    }

    /**
     * Gets the skipToken
     *
     * @return The skipToken
     */
    public String getSkipToken() {
        return skipToken;
    }

    /**
     * Gets the top
     *
     * @return The top
     */
    public int getTop() {
        return top;
    }

    /////////////// SETTERS ////////////////
    /**
     * Sets the count
     *
     * @param count The count to set
     */
    void setCount(boolean count) {
        this.count = count;
    }

    /**
     * Sets the expand
     *
     * @param expand The expand to set
     */
    void setExpand(String expand) {
        this.expand = expand;
    }

    /**
     * Sets the filter
     *
     * @param filter The filter to set
     */
    void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * Sets the format
     *
     * @param format The format to set
     */
    void setFormat(String format) {
        this.format = format;
    }

    /**
     * Sets the orderBy
     *
     * @param orderBy The orderBy to set
     */
    void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    /**
     * Sets the search
     *
     * @param search The search to set
     */
    void setSearch(String search) {
        this.search = search;
    }

    /**
     * Sets the select
     *
     * @param select The select to set
     */
    void setSelect(String select) {
        this.select = select;
    }

    /**
     * Sets the skip
     *
     * @param skip The skip to set
     */
    void setSkip(int skip) {
        this.skip = skip;
    }

    /**
     * Sets the skipToken
     *
     * @param skipToken The skipToken to set
     */
    void setSkipToken(String skipToken) {
        this.skipToken = skipToken;
    }

    /**
     * Sets the top
     *
     * @param top The top to set
     */
    void setTop(int top) {
        this.top = top;
    }

    //////////// BUILDER /////////////////

    /**
     * {@link Builder}
     */
    public static class Builder {

        private boolean count;
        private String expand;
        private String filter;
        private String format;
        private String orderBy;
        private String search;
        private String select;
        private int skip;
        private String skipToken;
        private int top;

        /**
         * Initialises a new {@link MicrosoftGraphQueryParameters.Builder}.
         */
        public Builder() {
            super();
        }

        /**
         * <p>
         * Use the <code>$count</code> query parameter to include a count of the
         * total number of items in a collection alongside the page of
         * data values returned from Microsoft Graph.
         * </p>
         * 
         * <p><b>Note:</b> <code>$count</code> is not supported for collections of
         * resources that derive from
         * <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/v1.0/resources/directoryobject">directoryObject</a>
         * like collections of
         * <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/v1.0/resources/user">users</a> or
         * <a href="https://developer.microsoft.com/en-us/graph/docs/api-reference/v1.0/resources/group">groups</a>.
         * </p>
         *
         * @param count The count to use
         * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/query_parameters#count-parameter">count parameter</a>
         */
        public Builder withCount(boolean count) {
            this.count = count;
            return this;
        }

        /**
         * <p>
         * Many Microsoft Graph resources expose both declared properties of the resource
         * as well as its relationships with other resources. These relationships are also
         * called reference properties or navigation properties, and they can reference
         * either a single resource or a collection of resources. For example, the mail folders,
         * manager, and direct reports of a user are all exposed as relationships.
         * </p>
         * 
         * <p>
         * Normally, you can query either the properties of a resource or one of its relationships
         * in a single request, but not both. You can use the $expand query string parameter to
         * include the expanded resource or collection referenced by a single relationship (navigation
         * property) in your results.
         * </p>
         * 
         * For example: <code>children</code>
         *
         * @param expand The expand to use
         * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/query_parameters#expand-parameter">expand parameter</a>
         */
        public Builder withExpand(String expand) {
            this.expand = expand;
            return this;
        }

        /**
         * <p>Use the $filter query parameter to retrieve just a subset of a collection.</p>
         * <p>
         * Support for $filter operators varies across Microsoft Graph APIs. The following logical operators are generally supported:
         * </p>
         * <ul>
         * <li>equals (eq)</li>
         * <li>not equals (ne)</li>
         * <li>greater than (gt)</li>
         * <li>greater than or equals (ge)</li>
         * <li>less than (lt), less than or equals (le)</li>
         * <li>and (and)</li>
         * <li>or (or)</li>
         * <li>not (not)</li>
         * </ul>
         * <p>
         * The <code>startswith</code> string operator is often supported. The <code>any</code> lambda
         * operator is supported for some APIs.
         * </p>
         * 
         * <p>For example: <code>from/emailAddress/address eq 'someuser@.com'</code></p>
         * 
         * @param filter The filter to use
         * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/query_parameters#filter-parameter">filter parameter</a>
         */
        public Builder withFilter(String filter) {
            this.filter = filter;
            return this;
        }

        /**
         * <p>
         * Use the $format query parameter to specify the media format of the items returned from Microsoft Graph.
         * </p>
         * 
         * <p><b>Note:</b> The $format query parameter supports a number of formats (for example, atom, xml,
         * and json) but results may not be returned in all formats.
         *
         * @param format The format to use
         * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/query_parameters#format-parameter">format parameter</a>
         */
        public Builder withFormat(String format) {
            this.format = format;
            return this;
        }

        /**
         * <p>Use the $orderby query parameter to specify the sort order of the items returned from Microsoft Graph.</p>
         * <p>For example: <code>from/emailAddress/name desc,subject</code>
         *
         * @param orderBy The orderBy to use
         * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/query_parameters#orderby-parameter">orderby parameter</a>
         */
        public Builder withOrderBy(String orderBy) {
            this.orderBy = orderBy;
            return this;
        }

        /**
         * <p>Use the $search query parameter to restrict the results of a request to match a search criterion.</p>
         *
         * @param search The search to use
         * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/query_parameters#search-parameter">search parameter</a>
         */
        public Builder withSearch(String search) {
            this.search = search;
            return this;
        }

        /**
         * <p>
         * Use the $select query parameter to return a set of properties
         * that are different than the default set for an individual resource
         * or a collection of resources. With $select, you can specify a subset
         * or a superset of the default properties.
         * </p>
         *
         * @param select The select to use
         * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/query_parameters#select-parameter">select parameter</a>
         */
        public Builder withSelect(String select) {
            this.select = select;
            return this;
        }

        /**
         * <p>
         * Use the $skip query parameter to set the number of items to skip at the start of a collection.
         * </p>
         *
         * @param skip The skip to use
         * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/query_parameters#skip-parameter">skip parameter</a>
         */
        public Builder withSkip(int skip) {
            this.skip = skip;
            return this;
        }

        /**
         * <p>
         * Some requests return multiple pages of data either due to server-side paging or due to the
         * use of the $top parameter to limit the page size of the response. Many Microsoft Graph APIs
         * use the skipToken query parameter to reference subsequent pages of the result. The $skiptoken
         * parameter contains an opaque token that references the next page of results and is returned in
         * the URL provided in the <code>@odata.nextLink</code> property in the response. To learn more, see
         * <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/paging">Paging</a>.
         * </p>
         *
         * @param skipToken The skipToken to use
         * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/query_parameters#skiptoken-parameter">skiptoken parameter</a>
         */
        public Builder withSkipToken(String skipToken) {
            this.skipToken = skipToken;
            return this;
        }

        /**
         * <p>Use the $top query parameter to specify the page size of the result set.</p>
         * <p>
         * If more items remain in the result set, the response body will contain an <code>@odata.nextLink</code>
         * parameter. This parameter contains a URL that you can use to get the next page of results.
         * To learn more, see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/paging">Paging</a>.
         * </p>
         *
         * @param top The top to use
         * @see <a href="https://developer.microsoft.com/en-us/graph/docs/concepts/query_parameters#top-parameter">top parameter</a>
         */
        public Builder withTop(int top) {
            this.top = top;
            return this;
        }

        /**
         * Builds the params
         * 
         * @return The {@link MicrosoftGraphQueryParameters}
         */
        public MicrosoftGraphQueryParameters build() {
            MicrosoftGraphQueryParameters p = new MicrosoftGraphQueryParameters();
            p.setCount(count);
            p.setExpand(expand);
            p.setFilter(filter);
            p.setFormat(format);
            p.setOrderBy(orderBy);
            p.setSearch(search);
            p.setSelect(select);
            p.setSkip(skip);
            p.setSkipToken(skipToken);
            p.setTop(top);
            return p;
        }
    }
}

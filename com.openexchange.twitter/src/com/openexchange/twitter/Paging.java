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

package com.openexchange.twitter;

/**
 * {@link Paging} - Controls pagination.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Paging {

    /**
     * Gets the page which specifies the page of results to retrieve.
     *
     * @return The page
     */
    int getPage();

    /**
     * Sets the page. Specifies the page of results to retrieve.
     *
     * @param page The page
     */
    void setPage(int page);

    /**
     * Gets the count which specifies the number of statuses to retrieve. May not be greater than 200.
     *
     * @return The count
     */
    int getCount();

    /**
     * Sets the given count. Specifies the number of statuses to retrieve. May not be greater than 200.
     *
     * @param count The count
     */
    void setCount(int count);

    /**
     * Sets the given count. Specifies the number of statuses to retrieve. May not be greater than 200.
     *
     * @param count The count
     * @return This paging with new count applied
     */
    Paging count(int count);

    /**
     * Gets the since id as a <code>long</code>. Returns only statuses with an ID greater than (that is, more recent than) the specified ID.
     *
     * @return The since id
     */
    long getSinceId();

    /**
     * Sets the since id as an <code>int</code>. Returns only statuses with an ID greater than (that is, more recent than) the specified ID.
     *
     * @param sinceId The since id
     */
    void setSinceId(int sinceId);

    /**
     * Sets the since id as an <code>int</code>. Returns only statuses with an ID greater than (that is, more recent than) the specified ID.
     *
     * @param sinceId The since id
     * @return This paging with new since id applied
     */
    Paging sinceId(int sinceId);

    /**
     * Sets the since id as a <code>long</code>. Returns only statuses with an ID greater than (that is, more recent than) the specified ID.
     *
     * @param sinceId
     */
    void setSinceId(long sinceId);

    /**
     * Sets the since id as a <code>long</code>. Returns only statuses with an ID greater than (that is, more recent than) the specified ID.
     *
     * @param sinceId The since id
     * @return This paging with new since id applied
     */
    Paging sinceId(long sinceId);

    /**
     * Gets the maximum id. Returns only statuses with an ID less than (that is, older than) or equal to the specified ID.
     *
     * @return The maximum id
     */
    long getMaxId();

    /**
     * Sets the maximum id. Returns only statuses with an ID less than (that is, older than) or equal to the specified ID.
     *
     * @param maxId The maximum id
     */
    void setMaxId(long maxId);

    /**
     * Sets the maximum id. Returns only statuses with an ID less than (that is, older than) or equal to the specified ID.
     *
     * @param maxId The maximum id
     * @return This paging with new maximum id applied
     */
    Paging maxId(long maxId);

}

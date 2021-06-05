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

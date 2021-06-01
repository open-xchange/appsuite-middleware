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

package com.openexchange.twitter.internal;

import com.openexchange.twitter.Paging;

/**
 * {@link PagingImpl} - The paging implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PagingImpl implements Paging {

    private final twitter4j.Paging twitter4jPaging;

    /**
     * Initializes a new {@link PagingImpl}.
     *
     * @param twitter4jPaging The twitter4j paging
     */
    public PagingImpl(final twitter4j.Paging twitter4jPaging) {
        super();
        this.twitter4jPaging = twitter4jPaging;
    }

    @Override
    public Paging count(final int count) {
        twitter4jPaging.setCount(count);
        return this;
    }

    @Override
    public int getCount() {
        return twitter4jPaging.getCount();
    }

    @Override
    public long getMaxId() {
        return twitter4jPaging.getMaxId();
    }

    @Override
    public int getPage() {
        return twitter4jPaging.getPage();
    }

    @Override
    public long getSinceId() {
        return twitter4jPaging.getSinceId();
    }

    @Override
    public Paging maxId(final long maxId) {
        twitter4jPaging.setMaxId(maxId);
        return this;
    }

    @Override
    public void setCount(final int count) {
        twitter4jPaging.setCount(count);
    }

    @Override
    public void setMaxId(final long maxId) {
        twitter4jPaging.setMaxId(maxId);
    }

    @Override
    public void setPage(final int page) {
        twitter4jPaging.setPage(page);
    }

    @Override
    public void setSinceId(final int sinceId) {
        twitter4jPaging.setSinceId(sinceId);
    }

    @Override
    public void setSinceId(final long sinceId) {
        twitter4jPaging.setSinceId(sinceId);
    }

    @Override
    public Paging sinceId(final int sinceId) {
        twitter4jPaging.setSinceId(sinceId);
        return this;
    }

    @Override
    public Paging sinceId(final long sinceId) {
        twitter4jPaging.setSinceId(sinceId);
        return this;
    }

    @Override
    public String toString() {
        return twitter4jPaging.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((twitter4jPaging == null) ? 0 : twitter4jPaging.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PagingImpl)) {
            return false;
        }
        final PagingImpl other = (PagingImpl) obj;
        if (twitter4jPaging == null) {
            if (other.twitter4jPaging != null) {
                return false;
            }
        } else if (!twitter4jPaging.equals(other.twitter4jPaging)) {
            return false;
        }
        return true;
    }

}

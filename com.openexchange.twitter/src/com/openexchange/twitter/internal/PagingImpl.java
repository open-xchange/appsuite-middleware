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

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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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



package com.openexchange.tools.iterator;

import java.util.LinkedList;
import java.util.Queue;

import com.openexchange.api2.OXException;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;

/**
 * @author marcus
 *
 */
public class PrefetchIterator<T> implements SearchIterator {

    private final SearchIterator delegate;

    private final boolean prefetch;

    private Queue<T> data;

    private OXException oxExc;

    private SearchIteratorException sie;

    private SearchIteratorException closeexc;

    /**
     * Default constructor.
     */
    public PrefetchIterator(final SearchIterator delegate) {
        this.delegate = delegate;
        prefetch = ServerConfig.getBoolean(Property.PrefetchEnabled);
        init();
    }

    private void init() {
        if (prefetch) {
            if (delegate.hasSize()) {
                data = new LinkedList<T>();
            } else {
                data = new LinkedList<T>();
            }
            while (delegate.hasNext()) {
                try {
                    data.offer((T) delegate.next());
                } catch (OXException e) {
                    oxExc = e;
                    break;
                } catch (SearchIteratorException e) {
                    sie = e;
                    break;
                }
            }
            try {
                delegate.close();
            } catch (SearchIteratorException e) {
                closeexc = e;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws SearchIteratorException {
        if (prefetch) {
            data.clear();
            if (null != closeexc) {
                throw closeexc;
            }
        } else {
            delegate.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        if (prefetch) {
            return !data.isEmpty() || null != oxExc || null != sie; 
        }
        return delegate.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasSize() {
        return prefetch || delegate.hasSize();
    }

    /**
     * {@inheritDoc}
     */
    public T next() throws SearchIteratorException, OXException {
        if (prefetch) {
            if (data.isEmpty()) {
                if (null != oxExc) {
                    throw oxExc;
                }
                if (null != sie) {
                    throw sie;
                }
                throw new SearchIteratorException("No such element!");
            }
            return data.poll();
        }
        return (T) delegate.next();
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        if (prefetch) {
            return data.size();
        }
        return delegate.size();
    }
}

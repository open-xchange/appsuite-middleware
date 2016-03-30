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

package com.openexchange.tools.pipesnfilters.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.pipesnfilters.DataSource;
import com.openexchange.tools.pipesnfilters.Filter;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersException;

/**
 * {@link Pipe}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
final class Pipe<T> implements DataSource<T>, DataSink<T> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Pipe.class);

    private final ThreadPoolService threadPool;
    private final BlockingQueue<Object> queue = new LinkedBlockingQueue<Object>();
    private final Object eof = new Object();

    private boolean finished = false;

    public Pipe(final ThreadPoolService threadPool) {
        super();
        this.threadPool = threadPool;
    }

    @Override
    public boolean hasData() {
        return !finished || !queue.isEmpty();
    }

    @Override
    public int getData(final Collection<T> col) throws PipesAndFiltersException {
        int retval = 0;
        if (queue.isEmpty()) {
            // Wait for at least 1 element.
            try {
                pass(queue.take(), col);
            } catch (final InterruptedException e) {
                // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
                Thread.currentThread().interrupt();
                throw new PipesAndFiltersException(e);
            }
        }
        final List<Object> tmps = new ArrayList<Object>(queue.size());
        retval += queue.drainTo(tmps);
        for (final Object tmp : tmps) {
            retval += pass(tmp, col);
        }
        return retval;
    }

    private int pass(final Object tmp, final Collection<T> col) throws PipesAndFiltersException {
        int retval = 0;
        if (eof.equals(tmp)) {
            finished = true;
        } else if (tmp instanceof PipesAndFiltersException) {
            finished = true;
            throw (PipesAndFiltersException) tmp;
        } else {
            @SuppressWarnings("unchecked")
            final T obj = (T) tmp;
            col.add(obj);
            retval++;
        }
        return retval;
    }

    @Override
    public void put(final T element) throws PipesAndFiltersException {
        try {
            queue.put(element);
        } catch (final InterruptedException e) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
            throw new PipesAndFiltersException(e);
        }
    }

    @Override
    public void exception(final PipesAndFiltersException e) {
        try {
            queue.put(e);
        } catch (final InterruptedException e1) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
            LOG.error("", e1);
        }
    }

    @Override
    public void finished() throws PipesAndFiltersException {
        try {
            queue.put(eof);
        } catch (final InterruptedException e) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
            throw new PipesAndFiltersException(e);
        }
    }

    @Override
    public <O> DataSource<O> addFilter(final Filter<T, O> filter) {
        final Pipe<O> pipe = new Pipe<O>(threadPool);
        final FilterTask<T, O> task = new FilterTask<T, O>(this, filter, pipe);
        threadPool.submit(task);
        return pipe;
    }
}

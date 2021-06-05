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
            } catch (InterruptedException e) {
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
        } catch (InterruptedException e) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
            throw new PipesAndFiltersException(e);
        }
    }

    @Override
    public void exception(final PipesAndFiltersException e) {
        try {
            queue.put(e);
        } catch (InterruptedException e1) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
            LOG.error("", e1);
        }
    }

    @Override
    public void finished() throws PipesAndFiltersException {
        try {
            queue.put(eof);
        } catch (InterruptedException e) {
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

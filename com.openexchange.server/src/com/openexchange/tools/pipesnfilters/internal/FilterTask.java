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
import java.util.List;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.tools.exceptions.ExceptionUtils;
import com.openexchange.tools.pipesnfilters.DataSource;
import com.openexchange.tools.pipesnfilters.Filter;
import com.openexchange.tools.pipesnfilters.PipesAndFiltersException;

/**
 * {@link FilterTask}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
class FilterTask<I, O> extends AbstractTask<Void> {

    private final DataSource<I> input;
    private final Filter<I, O> filter;
    private final DataSink<O> output;

    public FilterTask(DataSource<I> input, Filter<I, O> filter, DataSink<O> output) {
        super();
        this.input = input;
        this.filter = filter;
        this.output = output;
    }

    @Override
    public void setThreadName(ThreadRenamer threadRenamer) {
        threadRenamer.renamePrefix("Pipes&Filters " + filter.getClass().getName());
    }

    @Override
    public Void call() {
        List<I> inputBlock = new ArrayList<I>();
        try {
            while (input.hasData()) {
                input.getData(inputBlock);
                if (!inputBlock.isEmpty()) {
                    O[] outputBlock = filter.filter(inputBlock);
                    inputBlock.clear();
                    for (O outputElement : outputBlock) {
                        output.put(outputElement);
                    }
                }
            }
            output.finished();
        } catch (PipesAndFiltersException e) {
            output.exception(e);
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            output.exception(new PipesAndFiltersException(t));
        }
        return null;
    }
}

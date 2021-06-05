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

package com.openexchange.tools.pipesnfilters;

import java.util.Collection;

/**
 * The {@link DataSource} can be used in 2 different ways which should not be mixed up. Either use it as the end of the pipes & filters
 * construct and read the outcoming data through methods {@link #hasData()} and {@link #getData(Collection)} or append a {@link Filter} that
 * uses this {@link DataSource} as input. Reading the data can be done as followed:
 * <pre>
 * Collection&lt;I&gt; input;
 * DataSource&lt;O&gt; output = PipesAndFiltersService.create(input).addFilter(new SomeFilter&lt;I, O&gt;());
 * Collection&lt;O&gt; data;
 * while (output.hasData()) {
 *     output.getData(data);
 * }
 * </pre>
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface DataSource<I> {

    boolean hasData();

    int getData(Collection<I> col) throws PipesAndFiltersException;

    <O> DataSource<O> addFilter(Filter<I, O> filter);

}

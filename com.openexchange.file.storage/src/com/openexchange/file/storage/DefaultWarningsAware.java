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

package com.openexchange.file.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.exception.OXException;

/**
 * {@link DefaultWarningsAware} - The default implementation of {@link WarningsAware}.
 * <p>
 * Supposed to be used to add {@code WarningsAware} in a delegate-fashion:
 *
 * <pre>
 *
 * private final WarningsAware warningsAware = new DefaultWarningsAware();
 *
 * &#064;Override
 * public List&lt;OXException&gt; getWarnings() {
 *     return warningsAware.getWarnings();
 * }
 *
 * &#064;Override
 * public List&lt;OXException&gt; getAndFlushWarnings() {
 *     return warningsAware.getAndFlushWarnings();
 * }
 *
 * &#064;Override
 * public void addWarning(OXException warning) {
 *     warningsAware.addWarning(warning);
 * }
 *
 * &#064;Override
 * public void removeWarning(OXException warning) {
 *     warningsAware.removeWarning(warning);
 * }
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DefaultWarningsAware implements WarningsAware {

    private final List<OXException> warnings;

    /**
     * Initializes a new {@link DefaultWarningsAware}.
     */
    public DefaultWarningsAware() {
        this(false);
    }

    /**
     * Initializes a new {@link DefaultWarningsAware}.
     *
     * @param concurrent Whether concurrent access is supposed to be supported or not
     */
    public DefaultWarningsAware(final boolean concurrent) {
        super();
        warnings = concurrent ? new CopyOnWriteArrayList<OXException>() : new ArrayList<OXException>(4);
    }

    @Override
    public List<OXException> getWarnings() {
        return new ArrayList<OXException>(warnings);
    }

    @Override
    public List<OXException> getAndFlushWarnings() {
        final List<OXException> ret = new ArrayList<OXException>(warnings);
        warnings.clear();
        return ret;
    }

    @Override
    public void addWarning(final OXException warning) {
        warnings.add(warning);
    }

    @Override
    public void removeWarning(final OXException warning) {
        warnings.remove(warning);
    }

}

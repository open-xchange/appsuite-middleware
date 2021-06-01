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

package com.openexchange.groupware.settings;

import java.util.Collection;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractWarningAwareReadOnlyValue}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractWarningAwareReadOnlyValue extends ReadOnlyValue implements WarningsAware {

    protected final WarningsAware warningsAware;

    /**
     * Initializes a new {@link AbstractWarningAwareReadOnlyValue}.
     */
    protected AbstractWarningAwareReadOnlyValue() {
        super();
        warningsAware = new DefaultWarningsAware();
    }

    @Override
    public List<OXException> getWarnings() {
        return warningsAware.getWarnings();
    }

    @Override
    public List<OXException> getAndFlushWarnings() {
        return warningsAware.getAndFlushWarnings();
    }

    @Override
    public void addWarning(OXException warning) {
        warningsAware.addWarning(warning);
    }

    @Override
    public void addWarnings(Collection<OXException> warnings) {
        warningsAware.addWarnings(warnings);
    }

    @Override
    public void removeWarning(OXException warning) {
        warningsAware.removeWarning(warning);
    }

}

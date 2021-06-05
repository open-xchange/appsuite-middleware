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

package com.openexchange.logging;

import java.util.logging.Level;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link LogLevelService} for dynamically handling of logs.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.8.2
 */
@SingletonService
public interface LogLevelService {

    /**
     * Sets the log level for the defined class
     *
     * @param className Fully qualified name of the class the log level should be changed for
     * @param logLevel The new level for the class
     * @return <code>true</code> if log level could be successfully set; otherwise <code>false</code>
     */
    boolean set(String className, Level logLevel);

    /**
     * Resets the (previously changed) log level for defined class to the origin definition (valid before calling {@link LogLevelService#set(String, Level)}).
     * <p>
     * If {@link LogLevelService#set(String, Level)} hasn't been executed before for provided class name nothing will be done.
     *
     * @param className Fully qualified name of the class the log level should be reset for
     */
    void reset(String className);

}

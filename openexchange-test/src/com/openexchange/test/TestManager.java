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

package com.openexchange.test;

import com.openexchange.ajax.framework.AbstractAJAXResponse;

/**
 * This interface is implemented by test manager classes. These are classes
 * that manage tests by making sure you can check all kinds of responses,
 * get thrown errors, that after the tests the created objects are deleted
 * and, most importantly, that you can control the failure-behaviour of
 * the requests.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public interface TestManager {

    /**
     * Make sure that all requests called by the manager have
     * failOnError set to this value (if applicable).
     */
    public void setFailOnError(boolean doFail);

    /**
     * Check value of failOnError setting
     */
    public boolean getFailOnError();

    /**
     * Check value of failOnError setting
     */
    public boolean doesFailOnError();

    /**
     * Remove all entities created during the use of this manager.
     * Should not fail if entities have already been removed.
     */
    public void cleanUp();

    /**
     * Returns the last response that should have been executed.
     */
    public AbstractAJAXResponse getLastResponse();

    /**
     * Whether the manager got an exception while executing a request.
     */
    public boolean hasLastException();

    /**
     * Returns the last exception the manager received or null.
     */
    public Throwable getLastException();
}

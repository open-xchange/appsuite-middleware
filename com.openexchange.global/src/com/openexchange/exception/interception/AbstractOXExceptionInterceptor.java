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

package com.openexchange.exception.interception;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.openexchange.exception.OXException;


/**
 * Abstract implementation of {@link OXExceptionInterceptor} that should be used to create custom {@link OXExceptionInterceptor}s.<br>
 * <br>
 * With that you only have to define responsibilities and implement what should be do while intercepting by overriding {@link
 * AbstractOXExceptionInterceptor.intercept(OXException)}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public abstract class AbstractOXExceptionInterceptor implements OXExceptionInterceptor {

    /** List of {@link Responsibility} the extending {@link OXExceptionInterceptor} is responsible for **/
    protected final Queue<Responsibility> responsibilitites = new ConcurrentLinkedQueue<Responsibility>();

    /** The service ranking */
    protected final int ranking;

    /**
     * Initializes a new {@link AbstractOXExceptionInterceptor}.
     *
     * @param ranking The ranking of this {@link OXExceptionInterceptor} compared to other ones
     */
    protected AbstractOXExceptionInterceptor(int ranking) {
        super();
        this.ranking = ranking;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract OXExceptionArguments intercept(OXException oxException);

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Responsibility> getResponsibilities() {
        return responsibilitites;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addResponsibility(Responsibility responsibility) {
        this.responsibilitites.add(responsibility);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isResponsible(String module, String action) {
        for (Responsibility responsibility : responsibilitites) {
            if (responsibility.implies(module, action)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRanking() {
        return ranking;
    }
}

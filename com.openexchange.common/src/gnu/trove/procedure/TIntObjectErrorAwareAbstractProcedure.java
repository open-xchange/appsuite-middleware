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

package gnu.trove.procedure;

/**
 * {@link TIntObjectErrorAwareAbstractProcedure} - Provides access to an expected exception via {@link #getException()}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class TIntObjectErrorAwareAbstractProcedure<T, E extends Exception> extends AbstractErrorAware<E> implements TIntObjectProcedure<T> {

    /**
     * Initializes a new {@link TIntObjectErrorAwareAbstractProcedure}.
     */
    protected TIntObjectErrorAwareAbstractProcedure() {
        super();
    }

    @Override
    public final boolean execute(final int key, final T value) {
        try {
            return next(key, value);
        } catch (Exception e) {
            this.exception = valueOf(e);
            return false;
        }
    }

    /**
     * Executes this procedure. A false return value indicates that the application executing this procedure should not invoke this
     * procedure again.
     *
     * @param a a <code>int</code> value
     * @param b an <code>Object</code> value
     * @return true if additional invocations of the procedure are allowed.
     * @throws E The expected exception
     */
    protected abstract boolean next(int key, T value) throws E;

}

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

package com.openexchange.tx;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;

public abstract class AbstractService<T> implements TransactionAware {

    private final TLongObjectMap<T> txIds = new TLongObjectHashMap<T>();
    private final TLongObjectMap<StackTraceElement[]> startedTx = new TLongObjectHashMap<StackTraceElement[]>();

    protected abstract T createTransaction()throws TransactionException;
    protected abstract void commit(T transaction) throws TransactionException;
    protected abstract void rollback(T transaction)throws TransactionException;

    private static final boolean rememberStacks = false;

    @Override
    public void startTransaction() throws TransactionException {
        final long id = Thread.currentThread().getId();

        if (txIds.containsKey(id)) {
            throw new TransactionException("There is already a transaction active at this moment", startedTx.get(id));
        }

        final T txId = createTransaction();

        txIds.put(id, txId);
        if (rememberStacks) {
            // startedTx.put(id,Thread.currentThread().getStackTrace());
            // Using new Throwable().getStackTrace() is faster
            startedTx.put(id, new Throwable().getStackTrace());
        }
    }

    @Override
    public void commit() throws TransactionException{
        commit(getActiveTransaction());
    }

    @Override
    public void rollback() throws TransactionException{
        rollback(getActiveTransaction());
    }

    protected T getActiveTransaction(){
        return txIds.get(Thread.currentThread().getId());
    }

    @Override
    public void finish() throws TransactionException{
        txIds.remove(Thread.currentThread().getId());
        if (rememberStacks) {
            startedTx.remove(Thread.currentThread().getId());
        }
    }

}

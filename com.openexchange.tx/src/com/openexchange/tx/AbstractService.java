/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
        if(rememberStacks) {
            startedTx.remove(Thread.currentThread().getId());
        }
    }

}

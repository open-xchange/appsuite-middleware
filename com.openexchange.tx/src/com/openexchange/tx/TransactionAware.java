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

import com.openexchange.exception.OXException;

public interface TransactionAware {

    /**
     * Starts the transaction.
     *
     * @throws OXException If transaction start-up fails
     */
    public void startTransaction() throws OXException;

    /**
     * Commits the transaction.
     *
     * @throws OXException If transaction commit fails
     */
    public void commit() throws OXException;

    /**
     * Rolls-back the transaction.
     *
     * @throws OXException If transaction roll-back fails
     */
    public void rollback() throws OXException;

    /**
     * Performs possible clean-up operations after a commit/roll-back.
     *
     * @throws OXException If clean-up fails
     */
    public void finish() throws OXException;

    public void setTransactional(boolean transactional);

    public void setRequestTransactional(boolean transactional);

    /**
     * Sets whether auto-commit is activated or not.
     *
     * @param commits <code>true</code> if auto-commit is activated; otherwise <code>false</code>
     */
    public void setCommitsTransaction(boolean commits);

}

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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link TransactionAwares} - Utility class for {@code TransactionAware}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class TransactionAwares {

    /**
     * Initializes a new {@link TransactionAwares}.
     */
    private TransactionAwares() {
        super();
    }

    /**
     * Finishes specified transaction.
     *
     * @param transaction The transaction to finish
     */
    public static void finishSafe(final TransactionAware transaction) {
        if (null != transaction) {
            try {
                transaction.finish();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Rolls-back specified transaction.
     *
     * @param transaction The transaction to roll back
     */
    public static void rollbackSafe(final TransactionAware transaction) {
        if (null != transaction) {
            try {
                transaction.rollback();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Creates a new input stream that cares about finishing given transaction when closed.
     *
     * @param stream The stream to delegate to
     * @param transaction The transaction to finish
     * @return The transaction-finishing stream
     */
    public static InputStream finishingInputStream(InputStream stream, TransactionAware transaction) {
        if (stream == null) {
            return null;
        }

        return stream instanceof FinishingInputStream ? (FinishingInputStream) stream : new FinishingInputStream(stream, transaction);
    }

    /**
     * An input stream that also cares about finishing passed instance of <code>TransactionAware</code> when closed.
     */
    private static class FinishingInputStream extends FilterInputStream {

        private final TransactionAware transaction;

        /**
         * Initializes a new {@link FinishingInputStream}.
         *
         * @param stream The stream to delegate to
         * @param transaction The transaction instance to finish when stream is closed
         */
        FinishingInputStream(InputStream stream, TransactionAware transaction) {
            super(stream);
            this.transaction = transaction;
        }

        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                finishSafe(transaction);
            }
        }
    } // End of class FinishingInputStream

}

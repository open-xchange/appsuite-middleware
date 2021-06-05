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

package com.openexchange.mail.dataobjects;

import java.util.List;

/**
 * {@link ThreadedStructure} - A container providing thread-sorted mails.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ThreadedStructure {

    /**
     * Creates a new {@link ThreadedStructure} for specified list of thread-sorted mails.
     *
     * @param mails The thread-sorted mails
     * @return A new {@link ThreadedStructure} instance
     */
    public static ThreadedStructure valueOf(List<List<MailMessage>> mails) {
        return new ThreadedStructure(mails);
    }

    private final List<List<MailMessage>> mails;

    /**
     * Initializes a new {@link ThreadedStructure}.
     */
    private ThreadedStructure(List<List<MailMessage>> mails) {
        super();
        this.mails = mails;
    }

    /**
     * Gets the thread-sorted mails
     *
     * @return The thread-sorted mails
     */
    public List<List<MailMessage>> getMails() {
        return mails;
    }

}

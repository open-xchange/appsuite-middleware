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

package com.openexchange.mail;

import com.openexchange.mailaccount.MailAccount;

/**
 * {@link FullnameArgument} - Represents a full name argument; e.g. &quot;default347/INBOX/folder3&quot;.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FullnameArgument {

    private final int accountId;
    private final String fullName;
    private final int hash;

    /**
     * Initializes a new {@link FullnameArgument} with default account ID.
     *
     * @param fullName The full name
     */
    public FullnameArgument(String fullName) {
        this(MailAccount.DEFAULT_ID, fullName);
    }

    /**
     * Initializes a new {@link FullnameArgument}.
     *
     * @param accountId The account ID
     * @param fullName The full name
     */
    public FullnameArgument(int accountId, String fullName) {
        super();
        this.accountId = accountId;
        this.fullName = fullName;

        int prime = 31;
        int result = prime * 1 + accountId;
        result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
        hash = result;
    }

    /**
     * Gets the account identifier.
     *
     * @return The account identifier
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the full name.
     *
     * @return The full name
     */
    public String getFullname() {
        return fullName;
    }

    /**
     * Gets the full name.
     *
     * @return The full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Gets the prepared name<br>
     * Example:
     * <pre>
     * &quot;INBOX&quot; -&gt; &quot;default2/INBOX&quot;
     * </pre>
     *
     * @return The prepared name
     */
    public String getPreparedName() {
        return com.openexchange.mail.utils.MailFolderUtility.prepareFullname(accountId, fullName);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FullnameArgument)) {
            return false;
        }

        FullnameArgument other = (FullnameArgument) obj;
        if (accountId != other.accountId) {
            return false;
        }
        if (fullName == null) {
            if (other.fullName != null) {
                return false;
            }
        } else if (!fullName.equals(other.fullName)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return new StringBuilder(32).append("Account-ID=").append(accountId).append(" Full-Name=").append(fullName).toString();
    }
}

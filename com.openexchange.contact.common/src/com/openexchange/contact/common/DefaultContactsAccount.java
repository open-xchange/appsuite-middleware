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

package com.openexchange.contact.common;

import java.util.Date;
import org.json.JSONObject;

/**
 * {@link DefaultContactsAccount}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class DefaultContactsAccount implements ContactsAccount {

    private static final long serialVersionUID = 751461470879807481L;

    private final String providerId;
    private final int accountId;
    private final int userId;
    private final Date lastModified;
    private final JSONObject internalConfig;
    private final JSONObject userConfig;

    /**
     * Initializes a new {@link DefaultContactsAccount}.
     * 
     * @param providerId The provider identifier
     * @param accountId The account identifier
     * @param userId The user identifier
     * @param internalConfig The account's internal / protected configuration data
     * @param userConfig The account's external / user configuration data
     * @param lastModified The last modification date
     */
    public DefaultContactsAccount(String providerId, int accountId, int userId, JSONObject internalConfig, JSONObject userConfig, Date lastModified) {
        super();
        this.providerId = providerId;
        this.accountId = accountId;
        this.userId = userId;
        this.internalConfig = internalConfig;
        this.userConfig = userConfig;
        this.lastModified = lastModified;
    }

    @Override
    public int getAccountId() {
        return accountId;
    }

    @Override
    public String getProviderId() {
        return providerId;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public JSONObject getInternalConfiguration() {
        return internalConfig;
    }

    @Override
    public JSONObject getUserConfiguration() {
        return userConfig;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DefaultContactAccount [providerId=").append(providerId).append(", accountId=").append(accountId).append(", userId=").append(userId).append(", lastModified=").append(lastModified).append(", internalConfig=").append(internalConfig).append(", userConfig=").append(userConfig).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + accountId;
        result = prime * result + ((internalConfig == null) ? 0 : internalConfig.hashCode());
        result = prime * result + ((lastModified == null) ? 0 : lastModified.hashCode());
        result = prime * result + ((providerId == null) ? 0 : providerId.hashCode());
        result = prime * result + ((userConfig == null) ? 0 : userConfig.hashCode());
        result = prime * result + userId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultContactsAccount other = (DefaultContactsAccount) obj;
        if (accountId != other.accountId)
            return false;
        if (internalConfig == null) {
            if (other.internalConfig != null)
                return false;
        } else if (!internalConfig.equals(other.internalConfig))
            return false;
        if (lastModified == null) {
            if (other.lastModified != null)
                return false;
        } else if (!lastModified.equals(other.lastModified))
            return false;
        if (providerId == null) {
            if (other.providerId != null)
                return false;
        } else if (!providerId.equals(other.providerId))
            return false;
        if (userConfig == null) {
            if (other.userConfig != null)
                return false;
        } else if (!userConfig.equals(other.userConfig))
            return false;
        if (userId != other.userId)
            return false;
        return true;
    }

}

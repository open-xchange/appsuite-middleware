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

package com.openexchange.chronos.provider;

import java.util.Date;
import org.json.JSONObject;

/**
 * {@link DefaultCalendarAccount}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultCalendarAccount implements CalendarAccount {

    private static final long serialVersionUID = 8822850387106167336L;

    private final String providerId;
    private final int accountId;
    private final int userId;
    private final Date lastModified;
    private final JSONObject internalConfig;
    private final JSONObject userConfig;

    /**
     * Initializes a new {@link DefaultCalendarAccount}.
     *
     * @param providerId The provider identifier
     * @param accountId The account identifier
     * @param userId The user identifier
     * @param internalConfig The account's internal / protected configuration data
     * @param userConfig The account's external / user configuration data
     * @param lastModified The last modification date
     */
    public DefaultCalendarAccount(String providerId, int accountId, int userId, JSONObject internalConfig, JSONObject userConfig, Date lastModified) {
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
        return "DefaultCalendarAccount [providerId=" + providerId + ", accountId=" + accountId + ", userId=" + userId + ", lastModified=" + lastModified + ", internalConfig=" + internalConfig + ", userConfig=" + userConfig + "]";
    }

}

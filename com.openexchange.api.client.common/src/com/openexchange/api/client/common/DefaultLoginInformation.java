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

package com.openexchange.api.client.common;

import static com.openexchange.api.client.common.ApiClientUtils.parseInt;
import static com.openexchange.api.client.common.ApiClientUtils.parseString;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.annotation.Nullable;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.LoginInformation;
import com.openexchange.exception.OXException;

/**
 * {@link DefaultLoginInformation}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class DefaultLoginInformation implements LoginInformation {

    private String remoteSessionId;
    protected final Map<String, String> additionals = new HashMap<>(5);
    private String remoteMailAddress;
    private int remoteUserId;
    private int remoteContextId;
    private String folderId;
    private String module;
    private String item;
    private String loginType;

    /**
     * Initializes a new {@link DefaultLoginInformation}.
     */
    public DefaultLoginInformation() {
        super();
    }

    @Override
    @Nullable
    public String getRemoteSessionId() {
        return remoteSessionId;
    }

    @Override
    @Nullable
    public String getRemoteMailAddress() {
        return remoteMailAddress;
    }

    @Override
    public int getRemoteUserId() {
        return remoteUserId;
    }

    @Override
    public int getRemoteContextId() {
        return remoteContextId;
    }

    @Override
    @Nullable
    public String getRemoteFolderId() {
        return folderId;
    }

    @Override
    public String getModule() {
        return module;
    }

    @Override
    public String getItem() {
        return item;
    }

    @Override
    public String getLoginType() {
        return loginType;
    }

    @Override
    @Nullable
    public String getAdditional(String key) {
        return additionals.get(key);
    }

    /**
     * Sets the remoteSessionId
     *
     * @param remoteSessionId The remoteSessionId to set
     */
    public void setRemoteSessionId(String remoteSessionId) {
        this.remoteSessionId = remoteSessionId;
    }

    /**
     * Sets the remoteMailAddress
     *
     * @param remoteMailAddress The remoteMailAddress to set
     */
    public void setRemoteMailAddress(String remoteMailAddress) {
        this.remoteMailAddress = remoteMailAddress;
    }

    /**
     * Sets the remoteUserId
     *
     * @param remoteUserId The remoteUserId to set
     */
    public void setRemoteUserId(int remoteUserId) {
        this.remoteUserId = remoteUserId;
    }

    /**
     * Sets the remoteContextId
     *
     * @param remoteContextId The remoteContextId to set
     */
    public void setRemoteContextId(int remoteContextId) {
        this.remoteContextId = remoteContextId;
    }

    /**
     * Sets the folderId
     *
     * @param folderId The folderId to set
     */
    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    /**
     * Adds an additional value
     * 
     * @param key The key to save the value for
     * @param value The value
     */
    public void addAdditiona(String key, String value) {
        additionals.put(key, value);
    }

    /**
     * Sets the module
     *
     * @param module The module to set
     */
    public void setModule(String module) {
        this.module = module;
    }

    /**
     * Sets the item
     *
     * @param item The item to set
     */
    public void setItem(String item) {
        this.item = item;
    }

    /**
     * Sets the loginType
     *
     * @param loginType The loginType to set
     */
    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }

    /**
     * Parses the values of the map to a {@link DefaultLoginInformation}
     * <p>
     * Values from
     * <li> com.openexchange.share.servlet.utils.ShareRedirectUtils.getWebSessionRedirectURL()</li>
     * <li> com.openexchange.share.servlet.utils.LoginLocation</li>
     *
     * @param values values to parse
     * @return Login information
     * @throws OXException In case the remote server indicates an error
     */
    public static DefaultLoginInformation parse(Map<String, ? extends Object> values) throws OXException {
        DefaultLoginInformation information = new DefaultLoginInformation();
        for (Entry<String, ? extends Object> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (false == parseForLoginInformation(information, key, value)) {
                information.addAdditiona(key, String.valueOf(value));
            }
        }
        return information;
    }

    /**
     * Parses for login information
     *
     * @param information The object to add the parsed infos to
     * @param key The key to identify the property
     * @param value The value to set
     * @return <code>true</code> if the value was set, <code>false</code> otherwise
     * @throws OXException In case the remote server indicates an error
     */
    protected static boolean parseForLoginInformation(DefaultLoginInformation information, String key, Object value) throws OXException {
        switch (key) {
            case "session":
                information.setRemoteSessionId(parseString(value));
                return true;
            case "user":
                information.setRemoteMailAddress(parseString(value));
                return true;
            case "user_id":
                information.setRemoteUserId(parseInt(value));
                return true;
            case "context_id":
                information.setRemoteContextId(parseInt(value));
                return true;
            case "f":
            case "folder":
                information.setFolderId(parseString(value));
                return true;
            case "m":
            case "module":
                information.setModule(parseString(value));
                return true;
            case "i":
                information.setItem(parseString(value));
                return true;
            case "login_type":
                information.setLoginType(parseString(value));
                return true;
            case "error":
                throw ApiClientExceptions.REMOTE_SERVER_ERROR.create(value);
            default:
                // Fall trough
        }
        return false;
    }

}

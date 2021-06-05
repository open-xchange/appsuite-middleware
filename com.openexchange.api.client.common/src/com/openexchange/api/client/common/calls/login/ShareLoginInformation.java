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

package com.openexchange.api.client.common.calls.login;

import static com.openexchange.api.client.common.ApiClientUtils.parseString;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.api.client.common.DefaultLoginInformation;
import com.openexchange.exception.OXException;

/**
 * {@link ShareLoginInformation}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public class ShareLoginInformation extends DefaultLoginInformation {

    private String redirectUrl;
    private String token;
    private String loginName;
    private String share;
    private String target;
    private String status;
    private String message;
    private String messageType;

    /**
     * Initializes a new {@link ShareLoginInformation}.
     */
    public ShareLoginInformation() {
        super();
    }

    /**
     * Get the absolute path to the share as returned by the server
     *
     * @return The URL or <code>null</code> if not available
     */
    public String getRedirectUrl() {
        return redirectUrl;
    }

    /**
     * The login token to use
     *
     * @return The token
     */
    public String getToken() {
        return token;
    }


    /**
     * The login name of the user on the remote system
     *
     * @return The login name
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * The share
     *
     * @return The share
     */
    public String getShare() {
        return share;
    }

    /**
     * The share target
     *
     * @return The target
     */
    public String getTarget() {
        return target;
    }

    /**
     * The share status
     *
     * @return The status
     */
    public String getStatus() {
        return status;
    }

    /**
     * The message
     *
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * The message type
     *
     * @return The message types
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * Sets the redirectUrl
     *
     * @param redirectUrl The redirectUrl to set
     */
    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    /**
     * Sets the token
     *
     * @param token The token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Sets the loginName
     *
     * @param loginName The loginName to set
     */
    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    /**
     * Sets the share
     *
     * @param share The share to set
     */
    public void setShare(String share) {
        this.share = share;
    }

    /**
     * Sets the target
     *
     * @param target The target to set
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * Sets the status
     *
     * @param status The status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Sets the message
     *
     * @param message The message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets the messageType
     *
     * @param messageType The messageType to set
     */
    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    /**
     * Parses the values of the map to a {@link ShareLoginInformation}
     * <p>
     * Values from
     * <li> com.openexchange.share.servlet.utils.ShareRedirectUtils.getWebSessionRedirectURL()</li>
     * <li> com.openexchange.share.servlet.utils.LoginLocation</li>
     *
     * @param values values to parse
     * @return Login information
     * @throws OXException In case the remote server indicates an error
     */
    public static ShareLoginInformation parse(Map<String, ? extends Object> values) throws OXException {
        ShareLoginInformation information = new ShareLoginInformation();
        for (Entry<String, ? extends Object> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (parseForLoginInformation(information, key, value)) {
                continue;
            }
            switch (key) {
                case "token":
                    information.setToken(parseString(value));
                    break;
                case "login_name":
                    information.setLoginName(parseString(value));
                    break;
                case "share":
                    information.setShare(parseString(value));
                    break;
                case "target":
                    information.setTarget(parseString(value));
                    break;
                case "status":
                    information.setStatus(parseString(value));
                    break;
                case "message":
                    information.setMessage(parseString(value));
                    break;
                case "message_type":
                    information.setMessageType(parseString(value));
                    break;
                default:
                    information.addAdditiona(key, parseString(value));
                    break;
            }
        }
        return information;
    }

}

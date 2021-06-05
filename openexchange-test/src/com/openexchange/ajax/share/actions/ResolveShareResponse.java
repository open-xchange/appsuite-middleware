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

package com.openexchange.ajax.share.actions;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.java.Strings;
import junitx.framework.Assert;

/**
 * {@link ResolveShareResponse}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ResolveShareResponse extends AbstractAJAXResponse {

    /**
     * Status for "not found"
     */
    public static final String NOT_FOUND = "not_found";

    /**
     * Status for "not found, but continue to see other shares"
     */
    public static final String NOT_FOUND_CONTINUE = "not_found_continue";

    private final String path;
    private final Map<String, String> parameters;
    private final int statusCode;

    private RedeemResponse redeemResponse;

    /**
     * Initializes a new {@link ResolveShareResponse}.
     *
     * @param statusCode The HTTP status code
     * @param path The path
     * @param parameters The parameters
     */
    public ResolveShareResponse(int statusCode, String path, Map<String, String> parameters) {
        super(null);
        this.statusCode = statusCode;
        this.parameters = parameters;
        this.path = path;
    }

    /**
     * Initializes a new {@link ResolveShareResponse} based on a previous token redemption.
     *
     * @param delegate The initial share response
     * @param redeemResponse The following redeem response
     */
    public ResolveShareResponse(ResolveShareResponse delegate, RedeemResponse redeemResponse) {
        this(delegate.statusCode, delegate.path, delegate.parameters);
        this.redeemResponse = redeemResponse;
    }

    /**
     * Gets the response from the redeem token request used to transport an additional message to the client once it was executed.
     *
     * @return The redeem response, or <code>null</code> if not available
     */
    public RedeemResponse getRedeemResponse() {
        return redeemResponse;
    }

    public String getShare() {
        if (null != redeemResponse) {
            return redeemResponse.getShare();
        }
        return parameters.get("share");
    }

    public String getTarget() {
        if (null != redeemResponse) {
            return redeemResponse.getTarget();
        }
        return parameters.get("target");
    }

    public String getLoginType() {
        if (null != redeemResponse) {
            return redeemResponse.getLoginType();
        }
        return parameters.get("login_type");
    }

    public String getToken() {
        return parameters.get("token");
    }

    public String getLoginName() {
        String name = parameters.get("login_name");
        if (null != name) {
            try {
                return URLDecoder.decode(name, "ISO-8859-1");
            } catch (UnsupportedEncodingException e) {
                Assert.fail(e);
            }
        }
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getSessionID() {
        return parameters.get("session");
    }

    public String getUser() {
        return parameters.get("user");
    }

    public int getUserId() {
        String id = parameters.get("user_id");
        if (Strings.isNotEmpty(id)) {
            return Integer.valueOf(id).intValue();
        }
        return 0;
    }

    public String getLanguage() {
        return parameters.get("language");
    }

    public boolean isStore() {
        return Boolean.valueOf(parameters.get("store")).booleanValue();
    }

    public String getModule() {
        String app = parameters.get("app");
        return Strings.isEmpty(app) ? parameters.get("m") : app;
    }

    public String getFolder() {
        String folder = parameters.get("folder");
        return Strings.isEmpty(folder) ? parameters.get("f") : folder;
    }

    public String getItem() {
        String id = parameters.get("id");
        return Strings.isEmpty(id) ? parameters.get("i") : id;
    }

    public String getStatus() {
        if (null != redeemResponse) {
            return redeemResponse.getStatus();
        }
        return parameters.get("status");
    }

    /**
     * Gets the HTTP status code
     *
     * @return The statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        if (null != redeemResponse) {
            return redeemResponse.getMessage();
        }
        return parameters.get("message");
    }

    public String getMessageType() {
        if (null != redeemResponse) {
            return redeemResponse.getMessageType();
        }
        return parameters.get("message_type");
    }

}

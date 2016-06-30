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

package com.openexchange.ajax.share.actions;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import junitx.framework.Assert;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.java.Strings;

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

    public String getShare() {
        return parameters.get("share");
    }

    public String getTarget() {
        return parameters.get("target");
    }

    public String getLoginType() {
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
        if (false == Strings.isEmpty(id)) {
            return Integer.valueOf(id);
        }
        return 0;
    }

    public String getLanguage() {
        return parameters.get("language");
    }

    public boolean isStore() {
        return Boolean.valueOf(parameters.get("store"));
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
        return parameters.get("message");
    }

    public String getMessageType() {
        return parameters.get("message_type");
    }

}

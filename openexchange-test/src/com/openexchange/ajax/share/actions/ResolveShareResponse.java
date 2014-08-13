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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import java.util.Map;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.java.Strings;

/**
 * {@link ResolveShareResponse}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ResolveShareResponse extends AbstractAJAXResponse {

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
        return parameters.get("id");
    }

    /**
     * Gets the HTTP status code
     *
     * @return The statusCode
     */
    public int getStatusCode() {
        return statusCode;
    }

}

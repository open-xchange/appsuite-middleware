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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.api.client.common.calls.folders;

import java.util.Locale;
import java.util.Map;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.ApiClientUtils;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.parser.JsonObjectParser;
import com.openexchange.exception.OXException;

/**
 * {@link GetFolderCall}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.5
 */
public class GetFolderCall extends AbstractGetCall<RemoteFolder> {

    private final String id;
    private final String tree;
    private final String[] allowedModules;
    private final Locale language;

    public GetFolderCall(String id) {
        this(id, null, null, null);
    }

    /**
     * Initializes a new {@link GetFolderCall}.
     *
     * @param id The object id of the requested folder
     * @param tree The identifier of the folder tree
     * @param allowedModules An array of modules supported by the requesting client
     * @param language the locale to use
     */
    public GetFolderCall(String id, String tree, String[] allowedModules, Locale language) {
        super();
        this.id = id;
        this.tree = tree;
        this.allowedModules = allowedModules;
        this.language = language;
    }

    @Override
    @NonNull
    public String getModule() {
        return "/folders";
    }

    @Override
    protected void fillParameters(Map<String, String> parameters) {
        parameters.put("id", id);
        if (tree != null) {
            parameters.put("tree", tree);
        }
        if (allowedModules != null) {
            parameters.put("allowed_modules", ApiClientUtils.toCommaString(allowedModules));
        }
        if (language != null) {
            parameters.put("language", language.toLanguageTag());
        }

    }

    @Override
    protected String getAction() {
        return "get";
    }

    @Override
    public HttpResponseParser<RemoteFolder> getParser() throws OXException {
        return new JsonObjectParser<>(new RemoteFolderMapper());
    }
}

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

package com.openexchange.api.client.common.calls.folders;

import java.util.Locale;
import java.util.Map;
import com.openexchange.annotation.NonNull;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.ApiClientUtils;
import com.openexchange.api.client.common.calls.AbstractGetCall;
import com.openexchange.api.client.common.parser.JsonObjectParser;

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

    /**
     * 
     * Initializes a new {@link GetFolderCall}.
     * 
     * @param id The identifier of the folder to get
     */
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
        putIfPresent(parameters, "tree", tree);
        putIfPresent(parameters, "allowed_modules", ApiClientUtils.toCommaString(allowedModules));
        if (language != null) {
            putIfNotEmpty(parameters, "language", language.toLanguageTag());
        }
    }

    @Override
    protected String getAction() {
        return "get";
    }

    @Override
    public HttpResponseParser<RemoteFolder> getParser() {
        return new JsonObjectParser<>(new RemoteFolderMapper());
    }
}

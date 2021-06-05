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

package com.openexchange.test.resourcecache.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Header;

/**
 * {@link AbstractResourceCacheRequest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class AbstractResourceCacheRequest<T extends AbstractAJAXResponse> implements AJAXRequest<T> {

    private final String action;

    protected String cacheType = null;

    public AbstractResourceCacheRequest(String action) {
        super();
        this.action = action;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public String getServletPath() {
        return "/ajax/resourcecachetest";
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    @Override
    public Object getBody() throws IOException, JSONException {
        return null;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        List<Parameter> params = new ArrayList<Parameter>(Arrays.asList(getAdditionalParameters()));
        params.add(new URLParameter("action", action));
        if (cacheType != null) {
            params.add(new URLParameter("cacheType", cacheType));
        }

        return params.toArray(new Parameter[params.size()]);
    }

    protected Parameter[] getAdditionalParameters() {
        return new Parameter[0];
    }

}

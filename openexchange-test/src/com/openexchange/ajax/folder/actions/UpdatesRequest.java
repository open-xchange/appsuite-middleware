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

package com.openexchange.ajax.folder.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import com.openexchange.ajax.framework.CommonUpdatesRequest;
import com.openexchange.groupware.search.Order;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class UpdatesRequest extends CommonUpdatesRequest<FolderUpdatesResponse> {

    private final API api;

    private boolean altNames = false;

    public UpdatesRequest(final API api, final int[] columns, final int sort, final Order order, final Date lastModified) {
        this(api, columns, sort, order, lastModified, CommonUpdatesRequest.Ignore.DELETED);
    }

    public UpdatesRequest(final API api, final int[] columns, final int sort, final Order order, final Date lastModified, final CommonUpdatesRequest.Ignore ignore) {
        super(api.getUrl(), -1, columns, sort, order, lastModified, ignore, true);
        this.api = api;
    }

    @Override
    public FolderUpdatesParser getParser() {
        return new FolderUpdatesParser(isFailOnError(), getColumns());
    }

    @Override
    public Parameter[] getParameters() {
        final Parameter[] params = super.getParameters();
        final List<Parameter> l = new ArrayList<Parameter>(Arrays.asList(params));
        if (api.getTreeId() != -1) {
            l.add(new Parameter("tree", api.getTreeId()));
        }
        if (altNames) {
            l.add(new Parameter("altNames", Boolean.toString(altNames)));
        }
        return l.toArray(new Parameter[l.size()]);
    }

    public void setAltNames(boolean altNames) {
        this.altNames = altNames;
    }
}

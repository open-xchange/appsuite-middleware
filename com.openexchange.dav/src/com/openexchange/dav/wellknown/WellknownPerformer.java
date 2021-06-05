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

package com.openexchange.dav.wellknown;

import javax.servlet.http.HttpServletResponse;
import com.openexchange.dav.DAVFactory;
import com.openexchange.dav.DAVPerformer;
import com.openexchange.dav.actions.RedirectAction;
import com.openexchange.webdav.action.WebdavAction;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavMethod;

/**
 * {@link WellknownPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class WellknownPerformer extends DAVPerformer {

    private final WellknownFactory factory;
    private final WebdavAction redirectAction;

    /**
     * Initializes a new {@link WellknownPerformer}.
     *
     * @param location The location to redirect to
     * @param status The status to set in the response (should either be {@link HttpServletResponse#SC_MOVED_PERMANENTLY} or
     *            {@link HttpServletResponse#SC_MOVED_TEMPORARILY}
     */
    public WellknownPerformer(String location, int status) {
        super();
        this.redirectAction = new RedirectAction(location, status);
        this.factory = new WellknownFactory(new Protocol(), this);
    }

    @Override
    protected String getURLPrefix() {
        return factory.getURLPrefix();
    }

    @Override
    public DAVFactory getFactory() {
        return factory;
    }

    @Override
    protected WebdavAction getAction(WebdavMethod method) {
        return redirectAction;
    }

}

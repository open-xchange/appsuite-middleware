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

package com.openexchange.dav.push.subscribe;

import java.util.EnumMap;
import java.util.Map;
import com.openexchange.dav.DAVPerformer;
import com.openexchange.dav.actions.OPTIONSAction;
import com.openexchange.server.ServiceLookup;
import com.openexchange.webdav.action.WebdavAction;
import com.openexchange.webdav.action.WebdavExistsAction;
import com.openexchange.webdav.action.WebdavHeadAction;
import com.openexchange.webdav.action.WebdavIfAction;
import com.openexchange.webdav.action.WebdavIfMatchAction;
import com.openexchange.webdav.action.WebdavTraceAction;
import com.openexchange.webdav.protocol.Protocol;
import com.openexchange.webdav.protocol.WebdavMethod;

/**
 * {@link PushSubscribePerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.4
 */
public class PushSubscribePerformer extends DAVPerformer {

    private static final Protocol PROTOCOL = new Protocol();

    private final PushSubscribeFactory factory;
    private final Map<WebdavMethod, WebdavAction> actions;

    /**
     * Initializes a new {@link PushSubscribePerformer}.
     *
     * @param services A service lookup reference
     */
    public PushSubscribePerformer(ServiceLookup services) {
        super();
        this.factory = new PushSubscribeFactory(PROTOCOL, services, this);
        this.actions = initActions();
    }

    private EnumMap<WebdavMethod, WebdavAction> initActions() {
        EnumMap<WebdavMethod, WebdavAction> actions = new EnumMap<WebdavMethod, WebdavAction>(WebdavMethod.class);
        actions.put(WebdavMethod.OPTIONS, prepare(new OPTIONSAction(factory), true, true, new WebdavIfAction(0, false, false)));
        actions.put(WebdavMethod.POST, prepare(new PushSubscribeAction(factory), true, true, new WebdavIfMatchAction()));
        actions.put(WebdavMethod.GET, prepare(new PushSubscribeAction(factory), true, true, false, null, new WebdavExistsAction()));
        actions.put(WebdavMethod.HEAD, prepare(new WebdavHeadAction(), true, true, false, null, new WebdavExistsAction()));
        actions.put(WebdavMethod.TRACE, prepare(new WebdavTraceAction(), true, true, new WebdavIfAction(0, false, false)));
        makeLockNullTolerant(actions);
        return actions;
    }

    @Override
    protected String getURLPrefix() {
        return factory.getURLPrefix();
    }

    @Override
    public PushSubscribeFactory getFactory() {
        return factory;
    }

    @Override
    protected WebdavAction getAction(WebdavMethod method) {
        return actions.get(method);
    }

}

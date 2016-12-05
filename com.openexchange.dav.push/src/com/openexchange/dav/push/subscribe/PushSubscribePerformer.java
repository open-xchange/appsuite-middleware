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

package com.openexchange.dav.push.subscribe;

import java.util.EnumMap;
import java.util.Map;
import com.openexchange.dav.DAVPerformer;
import com.openexchange.server.ServiceLookup;
import com.openexchange.webdav.action.WebdavAction;
import com.openexchange.webdav.action.WebdavExistsAction;
import com.openexchange.webdav.action.WebdavHeadAction;
import com.openexchange.webdav.action.WebdavIfAction;
import com.openexchange.webdav.action.WebdavIfMatchAction;
import com.openexchange.webdav.action.WebdavOptionsAction;
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
        actions.put(WebdavMethod.OPTIONS, prepare(new WebdavOptionsAction(), true, true, new WebdavIfAction(0, false, false)));
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

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

package com.openexchange.carddav.action;

import static org.slf4j.LoggerFactory.getLogger;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.carddav.GroupwareCarddavFactory;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.actions.PUTAction;
import com.openexchange.exception.OXException;
import com.openexchange.webdav.action.WebdavRequest;
import com.openexchange.webdav.protocol.WebdavProtocolException;

/**
 * {@link CardDAVPUTAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CardDAVPUTAction extends PUTAction {

    private final GroupwareCarddavFactory factory;

    /**
     * Initializes a new {@link CardDAVPUTAction}.
     *
     * @param factory The underlying factory
     */
    public CardDAVPUTAction(GroupwareCarddavFactory factory) {
        super(factory.getProtocol());
        this.factory = factory;
    }

    @Override
    protected WebdavProtocolException getSizeExceeded(WebdavRequest request) {
        return new PreconditionException(DAVProtocol.CARD_NS.getURI(), "max-resource-size", HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
    protected long getMaxSize() {
        long maxVCardSize = factory.getState().getMaxVCardSize();
        Long maxUploadSize = null;
        try {
            maxUploadSize = factory.optConfigValue("MAX_UPLOAD_SIZE", Long.class, Long.valueOf(104857600));
        } catch (OXException e) {
            getLogger(CardDAVPUTAction.class).error("Error getting MAX_UPLOAD_SIZE value", e);
        }
        if (null == maxUploadSize || 0 >= maxUploadSize.longValue()) {
            return maxVCardSize;
        }
        if (0 >= maxVCardSize) {
            return maxUploadSize.longValue();
        }
        return Math.min(maxVCardSize, maxUploadSize.longValue());
    }

    @Override
    protected boolean includeResponseETag() {
        return true;
    }

}

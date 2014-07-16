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

package com.openexchange.share.json.actions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.modules.Module;
import com.openexchange.server.ServiceLookup;
import com.openexchange.share.json.Share;
import com.openexchange.share.json.ShareService;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link GETAction}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
@DispatcherNotes(noSession = true)
public class GETAction implements AJAXActionService {

    private static final Pattern PATH_PATTERN = Pattern.compile("/+([a-f0-9]{32})(?:/+items(?:/+([0-9]+))?)?/*");

    private final ServiceLookup services;


    /**
     * Initializes a new {@link GETAction}.
     * @param services
     */
    public GETAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    /*
     * http://ox.io/appsuite/api/share/ca1ccf2b2dd54b129c1b56e7cd65bdf9
     * http://ox.io/appsuite/api/share/ca1ccf2b2dd54b129c1b56e7cd65bdf9/items
     * http://ox.io/appsuite/api/share/ca1ccf2b2dd54b129c1b56e7cd65bdf9/items/123
     */
    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        ShareService shareService = services.getService(ShareService.class);

        String pathInfo = requestData.getPathInfo();
        Matcher matcher = PATH_PATTERN.matcher(pathInfo);
        if (matcher.matches()) {
            String token = matcher.group(1);
            Share share = shareService.resolveToken(token);
            if (share.isFolder()) {
                if (matcher.groupCount() > 1) {
                    String itemId = matcher.group(2);
                    return performGet(share.getModule(), share.getFolderId(), itemId);
                } else {
                    return performList();
                }
            } else {
                return performGet(share.getModule(), share.getFolderId(), share.getItemId());
            }
        } else {
            // TODO: throw exception
            return null;
        }
    }

    private AJAXRequestResult performGet(Module module, String folder, String item) {
        // TODO Auto-generated method stub
        return null;
    }

    private AJAXRequestResult performList() {
        // TODO Auto-generated method stub
        return null;
    }

}

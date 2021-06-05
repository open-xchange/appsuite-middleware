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

package com.openexchange.share.servlet.handler;

import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.share.servlet.auth.ShareLoginMethod;
import com.openexchange.share.servlet.internal.ShareServiceLookup;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link AbstractShareHandler}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public abstract class AbstractShareHandler implements ShareHandler {

    /**
     * Initializes a new {@link AbstractShareHandler}.
     */
    protected AbstractShareHandler() {
        super();
    }

    protected ShareLoginMethod getShareLoginMethod(AccessShareRequest shareRequest) throws OXException {
        /*
         * resolve context and guest
         */
        ContextService contextService = ShareServiceLookup.getService(ContextService.class, true);
        UserService userService = ShareServiceLookup.getService(UserService.class, true);
        Context context = contextService.getContext(shareRequest.getGuest().getContextID());
        User guest = userService.getUser(shareRequest.getGuest().getGuestID(), context);
        /*
         * add session enhancement as needed
         */
        Map<String, String> additionals = null != shareRequest.getTargetPath() ? shareRequest.getTargetPath().getAdditionals() : null;
        return new ShareLoginMethod(context, guest, (null == additionals || additionals.isEmpty()) ? null : new ShareLoginSessionEnhancement(additionals));
    }

    @Override
    public ShareHandlerReply handleNotFound(HttpServletRequest request, HttpServletResponse response, String status) throws IOException {
        return ShareHandlerReply.NEUTRAL;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class ShareLoginSessionEnhancement implements SessionEnhancement {

        private final Map<String, String> additionals;

        ShareLoginSessionEnhancement(Map<String, String> additionals) {
            super();
            this.additionals = additionals;
        }

        @Override
        public void enhanceSession(Session session) {
            StringBuilder keyBuilder = null;
            int reslen = 0;
            for (Map.Entry<String, String> entry : additionals.entrySet()) {
                if (null == keyBuilder) {
                    keyBuilder = new StringBuilder("com.openexchange.share.");
                    reslen = keyBuilder.length();
                } else {
                    keyBuilder.setLength(reslen);
                }
                session.setParameter(keyBuilder.append(entry.getKey()).toString(), entry.getValue());
            }
        }
    }

}

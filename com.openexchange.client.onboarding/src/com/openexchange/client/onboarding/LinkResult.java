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

package com.openexchange.client.onboarding;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;

/**
 * {@link LinkResult} - A result when a link is supposed to be returned.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class LinkResult implements Result {

    private final Link link;

    /**
     * Initializes a new {@link LinkResult}.
     *
     * @param link The link
     */
    public LinkResult(Link link) {
        super();
        this.link = link;
    }

    /**
     * Gets the link.
     *
     * @return The result string
     */
    public Link getLink() {
        return link;
    }

    @Override
    public ResultReply getReply() {
        return ResultReply.ACCEPT;
    }

    @Override
    public ResultObject getResultObject(OnboardingRequest request, Session session) throws OXException {
        OnboardingAction action = request.getAction();
        if (OnboardingAction.LINK != action) {
            throw OnboardingExceptionCodes.UNSUPPORTED_ACTION.create(null == action ? "null" : action.getId());
        }

        try {
            JSONObject jLink = new JSONObject(4);
            jLink.put("link", link.getUrl());
            {
                LinkType type = link.getType();
                jLink.put("type", null == type ? LinkType.COMMON.getId() : type.getId());
            }
            {
                String imageUrl = link.getImageUrl();
                if (Strings.isNotEmpty(imageUrl)) {
                    jLink.put("image", imageUrl);
                }
            }
            return new SimpleResultObject(jLink, "json");
        } catch (JSONException e) {
            throw OnboardingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}

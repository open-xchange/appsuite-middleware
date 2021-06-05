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

package com.openexchange.mail.json.compose.share.internal;

import com.openexchange.mail.json.compose.share.ShareComposeLink;


/**
 * {@link ShareComposeLinkImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ShareComposeLinkImpl implements ShareComposeLink {

    private final String name;
    private final String link;
    private final int hash;
    private final String type;

    /**
     * Initializes a new {@link ShareComposeLinkImpl}.
     */
    public ShareComposeLinkImpl(String name, String link, String type) {
        super();
        this.name = name;
        this.link = link;
        this.type = type;

        int prime = 31;
        int result = prime * 1 + ((link == null) ? 0 : link.hashCode());
        hash = result;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ShareComposeLinkImpl)) {
            return false;
        }
        ShareComposeLinkImpl other = (ShareComposeLinkImpl) obj;
        if (link == null) {
            if (other.link != null) {
                return false;
            }
        } else if (!link.equals(other.link)) {
            return false;
        }
        return true;
    }
}

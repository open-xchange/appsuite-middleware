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

package com.openexchange.mail.authenticity.internal;

import java.util.regex.Pattern;

/**
 * {@link TrustedDomain}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class TrustedDomain {

    private final String domain;
    private final Pattern pattern;
    private final Object image;

    /**
     *
     * Initializes a new {@link TrustedDomain}.
     *
     * @param domain The domain
     * @param image The image
     */
    public TrustedDomain(String domain, Object image) {
        super();
        this.domain = domain;
        pattern = Pattern.compile(toRegex(domain));

        this.image = image;
    }

    private String toRegex(String domain){

        domain = domain.replaceAll("\\.", "[.]").replaceAll("\\*", ".*");
        return domain;
    }

    /**
     * Gets the domain
     *
     * @return The domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Gets the image
     *
     * @return The image
     */
    public Object getImage() {
        return image;
    }

    /**
     * Checks whether this trusted domain matches the given domain
     * @param domain
     * @return
     */
    public boolean matches(String domain){
        return pattern.matcher(domain).matches();
    }
}

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

package com.openexchange.mail.authenticity.impl.trusted.internal;

import java.util.regex.Pattern;
import com.openexchange.java.Strings;
import com.openexchange.mail.authenticity.impl.trusted.Icon;

/**
 * {@link TrustedMail} specifies a trusted mail address or a group of trusted mail addresses via wild-cards.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class TrustedMail {

    private final String mail;
    private final Pattern pattern;
    private final Icon image;

    /**
     *
     * Initializes a new {@link TrustedMail}.
     *
     * @param mail The mail address. '*' and '?' wild-cards are allowed
     * @param image The image
     */
    public TrustedMail(String mail, Icon image) {
        super();
        this.mail = mail;
        this.pattern = Pattern.compile(Strings.wildcardToRegex(mail));
        this.image = image;
    }

    /**
     * Gets the mail address
     *
     * @return The mail address
     */
    public String getMail() {
        return mail;
    }

    /**
     * Gets the image
     *
     * @return The image or <code>null</code> if no image is specified
     */
    public Icon getImage() {
        return image;
    }

    /**
     * Checks whether this trusted mail matches the given mail address
     *
     * @param mailAddress
     * @return <code>true</code> if the {@link TrustedMail} matches the given mail address, <code>false</code> otherwise
     */
    public boolean matches(String mailAddress) {
        return pattern.matcher(mailAddress).matches();
    }

    @Override
    public String toString() {
        return new StringBuilder("TrustedDomain [").append("domain = ").append(mail).append(']').toString();
    }
}

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

package com.openexchange.mail.text;

import com.openexchange.exception.OXException;
import com.openexchange.mail.MailPath;
import com.openexchange.session.Session;


/**
 * {@link ImageUriGenerator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ImageUriGenerator {

    /**
     * Generates the image URI for given arguments.
     * <p>
     * The URI is supposed to be provided by <code>linkBuilder</code> instance using <code>linkBuilder.toString()</code>.
     *
     * @param linkBuilder The {@link StringBuilder} instance to re-use for building the string
     * @param prefix The attribute prefix; e.g. <code>"src="</code> or <code>"background="</code>
     * @param optAppendix The optional appendix
     * @param imageIdentifier The image identifier
     * @param mailPath The mail path for associated mail
     * @param session The associated session
     * @throws OXException If composing the image URI fails
     */
    void generateImageUri(StringBuilder linkBuilder, String prefix, String optAppendix, String imageIdentifier, MailPath mailPath, Session session) throws OXException;

    /**
     * Gets the plain image URI
     *
     * @param imageIdentifier The image identifier
     * @param mailPath The mail path
     * @param session Tne associated session
     * @return The plain image URI
     * @throws OXException If plain image URI cannot be returned
     */
    String getPlainImageUri(String imageIdentifier, MailPath mailPath, Session session) throws OXException;

}

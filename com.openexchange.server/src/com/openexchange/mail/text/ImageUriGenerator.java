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

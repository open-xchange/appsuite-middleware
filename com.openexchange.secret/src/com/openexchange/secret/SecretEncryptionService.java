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

package com.openexchange.secret;

import com.openexchange.exception.OXException;
import com.openexchange.session.Session;


/**
 * {@link SecretEncryptionService} - The secret encryption/decryption service.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SecretEncryptionService<T> {

    /**
     * Encrypts specified string using given session data.
     * <p>
     * The caller is required to have a special treatment for the {@link SecretExceptionCodes#EMPTY_SECRET} error code, which hints either
     * to a setup error or to a session providing insufficient data; e.g. missing password in single-sign on scenarios.
     *
     * @param session The session providing data
     * @param toEncrypt The string to encrypt
     * @return The encrypted string
     * @throws OXException If encryption fails
     * @see SecretExceptionCodes#EMPTY_SECRET
     */
    String encrypt(Session session, String toEncrypt) throws OXException;

    /**
     * Decrypts specified string using given session data.
     * <p>
     * The caller is required to have a special treatment for the {@link SecretExceptionCodes#EMPTY_SECRET} error code, which hints either
     * to a setup error or to a session providing insufficient data; e.g. missing password in single-sign on scenarios.
     *
     * @param session The session providing data
     * @param toDecrypt The string to decrypt
     * @return The decrypted string
     * @throws OXException If decryption fails
     * @see SecretExceptionCodes#EMPTY_SECRET
     */
    String decrypt(Session session, String toDecrypt) throws OXException;

    /**
     * Decrypts specified string using given session data.
     * <p>
     * The caller is required to have a special treatment for the {@link SecretExceptionCodes#EMPTY_SECRET} error code, which hints either
     * to a setup error or to a session providing insufficient data; e.g. missing password in single-sign on scenarios.
     *
     * @param session The session providing data
     * @param toDecrypt The string to decrypt
     * @param customizationNote The optional customization note
     * @return The decrypted string
     * @throws OXException If decryption fails
     * @see SecretExceptionCodes#EMPTY_SECRET
     */
    String decrypt(Session session, String toDecrypt, T customizationNote) throws OXException;

}

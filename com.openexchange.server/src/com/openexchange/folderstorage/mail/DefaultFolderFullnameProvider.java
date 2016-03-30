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

package com.openexchange.folderstorage.mail;

import com.openexchange.exception.OXException;

/**
 * {@link DefaultFolderFullnameProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface DefaultFolderFullnameProvider {

    /**
     * Gets the fullname of default INBOX folder
     *
     * @return The fullname of default INBOX folder
     * @throws OXException If INBOX folder's fullname cannot be returned
     */
    public String getINBOXFolder() throws OXException;

    /**
     * Gets the fullname of default confirmed ham folder
     *
     * @return The fullname of default confirmed ham folder
     * @throws OXException If confirmed ham folder's fullname cannot be returned
     */
    public String getConfirmedHamFolder() throws OXException;

    /**
     * Gets the fullname of default confirmed spam folder
     *
     * @return The fullname of default confirmed spam folder
     * @throws OXException If confirmed spam folder's fullname cannot be returned
     */
    public String getConfirmedSpamFolder() throws OXException;

    /**
     * Gets the fullname of default drafts folder
     *
     * @return The fullname of default drafts folder
     * @throws OXException If draft folder's fullname cannot be returned
     */
    public String getDraftsFolder() throws OXException;

    /**
     * Gets the fullname of default spam folder
     *
     * @return The fullname of default spam folder
     * @throws OXException If spam folder's fullname cannot be returned
     */
    public String getSpamFolder() throws OXException;

    /**
     * Gets the fullname of default sent folder
     *
     * @return The fullname of default sent folder
     * @throws OXException If sent folder's fullname cannot be returned
     */
    public String getSentFolder() throws OXException;

    /**
     * Gets the fullname of default trash folder
     *
     * @return The fullname of default trash folder
     * @throws OXException If trash folder's fullname cannot be returned
     */
    public String getTrashFolder() throws OXException;

}

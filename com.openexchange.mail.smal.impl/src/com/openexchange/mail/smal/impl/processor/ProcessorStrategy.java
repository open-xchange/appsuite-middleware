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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.mail.smal.impl.processor;

import com.openexchange.exception.OXException;

/**
 * {@link ProcessorStrategy} - The strategy for the {@link Processor processor}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ProcessorStrategy {

    /**
     * Determines if specified folder is considered as a folder with high attention; e.g. INBOX folder
     * 
     * @param folderInfo The information about the folder to check
     * @return <code>true</code> for high attention folder; otherwise <code>false</code>
     * @throws OXException If an error occurs
     */
    boolean hasHighAttention(MailFolderInfo folderInfo) throws OXException;

    /**
     * Signals whether contained messages shall be completely added to index immediately.
     * 
     * @param messageCount The message count or <code>-1</code> to consider folder's message count
     * @param folderInfo The information about the folder
     * @return <code>true</code> to fully add messages to index; otherwise <code>false</code>
     * @throws OXException If checking condition fails
     */
    boolean addFull(int messageCount, MailFolderInfo folderInfo) throws OXException;

    /**
     * Signals whether contained messages shall be added with its contents to index immediately.
     * 
     * @param messageCount The message count or <code>-1</code> to consider folder's message count
     * @param folderInfo The information about the folder
     * @return <code>true</code> to add messages with contents to index; otherwise <code>false</code>
     * @throws OXException If checking condition fails
     */
    boolean addHeadersAndContent(int messageCount, MailFolderInfo folderInfo) throws OXException;

    /**
     * Signals whether contained messages shall be added to index immediately only considering headers.
     * 
     * @param messageCount The message count or <code>-1</code> to consider folder's message count
     * @param folderInfo The information about the folder
     * @return <code>true</code> to perform a header-only add to index; otherwise <code>false</code>
     * @throws OXException If checking condition fails
     */
    boolean addHeadersOnly(int messageCount, MailFolderInfo folderInfo) throws OXException;
}

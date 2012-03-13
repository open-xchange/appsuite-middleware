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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.consistency;

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * FIXME {@link OXException} should not be thrown by MBeans. Normal JMX clients are not able to deserialize specialized
 * {@link Exception Exceptions}.
 *
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public interface ConsistencyMBean {

    // List

    // Missing
    List<String> listMissingFilesInContext(int contextId) throws OXException;

    Map<Integer, List<String>> listMissingFilesInFilestore(int filestoreId) throws OXException;

    Map<Integer, List<String>> listMissingFilesInDatabase(int databaseId) throws OXException;

    Map<Integer, List<String>> listAllMissingFiles() throws OXException;

    // Unassigned

    List<String> listUnassignedFilesInContext(int contextId) throws OXException;

    Map<Integer, List<String>> listUnassignedFilesInFilestore(int filestoreId) throws OXException;

    Map<Integer, List<String>> listUnassignedFilesInDatabase(int databaseId) throws OXException;

    Map<Integer, List<String>> listAllUnassignedFiles() throws OXException;

    // Repair

    void repairFilesInContext(int contextId, String resolverPolicy) throws OXException;

    void repairFilesInFilestore(int filestoreId, String resolverPolicy) throws OXException;

    void repairFilesInDatabase(int databaseId, String resolverPolicy) throws OXException;

    void repairAllFiles(String resolverPolicy) throws OXException;

}

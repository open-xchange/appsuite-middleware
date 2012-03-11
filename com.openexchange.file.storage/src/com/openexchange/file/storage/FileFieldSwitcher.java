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

package com.openexchange.file.storage;


/**
 * A {@link FileFieldSwitcher} allows to generically do work for a certain field
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public interface FileFieldSwitcher {
    public Object lastModified(Object...args);
    public Object created(Object...args);
    public Object modifiedBy(Object...args);
    public Object folderId(Object...args);
    public Object title(Object...args);
    public Object version(Object...args);
    public Object content(Object...args);
    public Object id(Object...args);
    public Object fileSize(Object...args);
    public Object description(Object...args);
    public Object url(Object...args);
    public Object createdBy(Object...args);
    public Object filename(Object...args);
    public Object fileMimetype(Object...args);
    public Object sequenceNumber(Object...args);
    public Object categories(Object...args);
    public Object lockedUntil(Object...args);
    public Object fileMd5sum(Object...args);
    public Object versionComment(Object...args);
    public Object currentVersion(Object...args);
    public Object colorLabel(Object...args);
    public Object lastModifiedUtc(Object...args);
    public Object numberOfVersions(Object...args);
}

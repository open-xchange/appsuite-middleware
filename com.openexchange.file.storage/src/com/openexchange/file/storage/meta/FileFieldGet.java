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

package com.openexchange.file.storage.meta;

import com.openexchange.file.storage.AbstractFileFieldSwitcher;


/**
 * {@link FileFieldGet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileFieldGet extends AbstractFileFieldSwitcher {

    @Override
    public Object categories(final Object... args) {
        return md( args ).getCategories();
    }

    @Override
    public Object colorLabel(final Object... args) {
        return md( args ).getColorLabel();
    }

    @Override
    public Object content(final Object... args) {
        return md( args ).getContent();
    }

    @Override
    public Object created(final Object... args) {
        return md( args ).getCreated();
    }

    @Override
    public Object createdBy(final Object... args) {
        return md( args ).getCreatedBy();
    }

    @Override
    public Object currentVersion(final Object... args) {
        return md( args ).isCurrentVersion();
    }

    @Override
    public Object description(final Object... args) {
        return md( args ).getDescription();
    }

    @Override
    public Object fileMd5sum(final Object... args) {
        return md( args ).getFileMD5Sum();
    }

    @Override
    public Object fileMimetype(final Object... args) {
        return md( args ).getFileMIMEType();
    }

    @Override
    public Object fileSize(final Object... args) {
        return md( args ).getFileSize();
    }

    @Override
    public Object filename(final Object... args) {
        return md( args ).getFileName();
    }

    @Override
    public Object folderId(final Object... args) {
        return md( args ).getFolderId();
    }

    @Override
    public Object id(final Object... args) {
        return md( args ).getId();
    }

    @Override
    public Object lastModified(final Object... args) {
        return md( args ).getLastModified();
    }

    @Override
    public Object lastModifiedUtc(final Object... args) {
        return md( args ).getLastModified();
    }

    @Override
    public Object lockedUntil(final Object... args) {
        return md( args ).getLockedUntil();
    }

    @Override
    public Object modifiedBy(final Object... args) {
        return md( args ).getModifiedBy();
    }

    @Override
    public Object numberOfVersions(final Object... args) {
        return md( args ).getNumberOfVersions();
    }

    @Override
    public Object sequenceNumber(final Object... args) {
        return md( args ).getSequenceNumber();
    }

    @Override
    public Object title(final Object... args) {
        return md( args ).getTitle();
    }

    @Override
    public Object url(final Object... args) {
        return md( args ).getURL();
    }

    @Override
    public Object version(final Object... args) {
        return md( args ).getVersion();
    }

    @Override
    public Object versionComment(final Object... args) {
        return md( args ).getVersionComment();
    }

    @Override
    public Object meta(Object... args) {
        return md( args ).getMeta();
    }

    @Override
    public Object objectPermissions(Object... args) {
        return md( args ).getObjectPermissions();
    }

    @Override
    public Object shareable(Object... args) {
        return md(args).isShareable();
    }
    
}

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

import java.util.List;
import java.util.Map;
import com.openexchange.file.storage.AbstractFileFieldSwitcher;
import com.openexchange.file.storage.FileStorageObjectPermission;

/**
 * {@link FileFieldSet}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileFieldSet extends AbstractFileFieldSwitcher {

    /**
     * Initializes a new {@link FileFieldSet}.
     */
    public FileFieldSet() {
        super();
    }

    @Override
    public Object categories(final Object... args) {
        md(args).setCategories(string(1, args));
        return ret(args);
    }

    @Override
    public Object colorLabel(final Object... args) {
        md(args).setColorLabel(integer(1, args));
        return ret(args);
    }

    @Override
    public Object content(final Object... args) {
        return ret(args);
    }

    @Override
    public Object created(final Object... args) {
        md(args).setCreated(date(1, args));
        return ret(args);
    }

    @Override
    public Object createdBy(final Object... args) {
        md(args).setCreatedBy(integer(1, args));
        return ret(args);
    }

    @Override
    public Object currentVersion(final Object... args) {
        return ret(args);
    }

    @Override
    public Object description(final Object... args) {
        md(args).setDescription(string(1, args));
        return ret(args);
    }

    @Override
    public Object fileMd5sum(final Object... args) {
        md(args).setFileMD5Sum(string(1, args));
        return ret(args);
    }

    @Override
    public Object fileMimetype(final Object... args) {
        md(args).setFileMIMEType(string(1, args));
        return ret(args);
    }

    @Override
    public Object fileSize(final Object... args) {
        md(args).setFileSize(longValue(1, args));
        return ret(args);
    }

    @Override
    public Object filename(final Object... args) {
        md(args).setFileName(string(1, args));
        return ret(args);
    }

    @Override
    public Object folderId(final Object... args) {
        md(args).setFolderId(string(1, args));
        return ret(args);
    }

    @Override
    public Object id(final Object... args) {
        md(args).setId(string(1, args));
        return ret(args);
    }

    @Override
    public Object lastModified(final Object... args) {
        md(args).setLastModified(date(1, args));
        return ret(args);
    }

    @Override
    public Object lastModifiedUtc(final Object... args) {
        md(args).setLastModified(date(1, args));
        return ret(args);
    }

    @Override
    public Object lockedUntil(final Object... args) {
        md(args).setLockedUntil(date(1, args));
        return ret(args);
    }

    @Override
    public Object modifiedBy(final Object... args) {
        md(args).setModifiedBy(integer(1, args));
        return ret(args);
    }

    @Override
    public Object numberOfVersions(final Object... args) {
        md(args).setNumberOfVersions(integer(1, args));
        return ret(args);
    }

    @Override
    public Object sequenceNumber(final Object... args) {
        return ret(args);
    }

    @Override
    public Object title(final Object... args) {
        md(args).setTitle(string(1, args));
        return ret(args);
    }

    @Override
    public Object url(final Object... args) {
        md(args).setURL(string(1, args));
        return ret(args);
    }

    @Override
    public Object version(final Object... args) {
        md(args).setVersion(string(1, args));
        return ret(args);
    }


    @Override
    public Object versionComment(final Object... args) {
        md(args).setVersionComment(string(1, args));
        return ret(args);
    }

    @Override
    public Object meta(Object... args) {
        md(args).setMeta((Map<String, Object>) args[1]);
        return null;
    }

    @Override
    public Object objectPermissions(Object... args) {
        md(args).setObjectPermissions((List<FileStorageObjectPermission>) args[1]);
        return null;
    }

    @Override
    public Object shareable(Object... args) {
        md(args).setShareable(bool(1, args));
        return null;
    }

    private Object ret(final Object[] args) {
        if (args.length >= 3 && null != args[2]) {
            return args[2];
        }
        return args[0];
    }

}

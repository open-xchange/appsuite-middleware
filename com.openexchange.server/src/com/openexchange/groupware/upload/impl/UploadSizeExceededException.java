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

package com.openexchange.groupware.upload.impl;

import static com.openexchange.groupware.upload.impl.UploadUtility.getSize;

/**
 * {@link UploadSizeExceededException} - The upload error with code MAX_UPLOAD_SIZE_EXCEEDED providing the possibility to convert bytes to a
 * human readable string; e.g. <code>88.3 MB</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UploadSizeExceededException extends UploadException {

    /**
     * No instance.
     */
    private UploadSizeExceededException(final int code, final String displayMessage, final Throwable cause, final Object[] displayArgs) {
        super(code, displayMessage, cause, displayArgs);
    }

    private static final long serialVersionUID = -6166524953168225923L;

    /**
     * Initializes a new {@link UploadException} for exceeded upload size.
     *
     * @param size The actual size in bytes
     * @param maxSize The max. allowed size in bytes
     * @param humanReadable <code>true</code> to convert bytes to a human readable string; otherwise <code>false</code>
     */
    public static UploadException create(final long size, final long maxSize, final boolean humanReadable) {
        return UploadException.UploadCode.MAX_UPLOAD_SIZE_EXCEEDED.create(
            humanReadable ? getSize(size, 2, false, true) : Long.valueOf(size),
            humanReadable ? getSize(maxSize, 2, false, true) : Long.valueOf(maxSize)).setAction(null);
    }

}

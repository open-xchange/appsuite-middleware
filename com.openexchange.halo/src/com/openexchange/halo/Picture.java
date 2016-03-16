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

package com.openexchange.halo;

import java.util.UUID;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.java.util.UUIDs;

/**
 * {@link Picture} - A picture.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 */
public class Picture {

    private String etag;
    private IFileHolder fileHolder;

    /**
     * Initializes a new {@link Picture} with a default random ETag.
     */
    public Picture() {
        // Initialize with a default random etag.
        this(UUIDs.getUnformattedString(UUID.randomUUID()), null);
    }

    /**
     * Initializes a new {@link Picture}.
     *
     * @param etag The associated ETag
     */
    public Picture(String etag) {
        this(etag, null);
    }

    /**
     * Initializes a new {@link Picture}.
     *
     * @param etag The associated ETag
     * @param fileHolder The file holder
     */
    public Picture(String etag, IFileHolder fileHolder) {
        this.etag = etag;
        this.fileHolder = fileHolder;
    }

    /**
     * Gets the etag
     *
     * @return The etag
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Sets the etag
     *
     * @param etag The etag to set
     */
    public void setEtag(String etag) {
        this.etag = etag;
    }

    /**
     * Gets the file holder
     *
     * @return The file holder
     */
    public IFileHolder getFileHolder() {
        return fileHolder;
    }

    /**
     * Sets the file holder
     *
     * @param fileHolder The file holder to set
     */
    public void setFileHolder(IFileHolder fileHolder) {
        this.fileHolder = fileHolder;
    }

}

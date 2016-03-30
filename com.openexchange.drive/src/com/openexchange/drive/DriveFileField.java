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

package com.openexchange.drive;

/**
 * {@link DriveFileField}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public enum DriveFileField {

    /**
     * A file's checksum (column 708, "checksum")
     */
    CHECKSUM,

    /**
     * The filename (column 702, "name")
     */
    NAME,

    /**
     * The content type / mime type of the file (column 703, "contentType")
     */
    CONTENT_TYPE,

    /**
     * The last modification timestamp of the file (column 5, "modified")
     */
    MODIFIED,

    /**
     * The creation timestamp of the file (column 4, "created")
     */
    CREATED,

    /**
     * A direct link to the file in the web interface (column 752, "directLink")
     */
    DIRECT_LINK,

    /**
     * A link to a medium-sized preview image for the file (column 750, "previewLink")
     */
    PREVIEW_LINK,

    /**
     * A link to a thumbnail image for the file (column 753, "thumbnailLink")
     */
    THUMBNAIL_LINK,

    /**
     * The fragments part of the direct link for the file (column 751, "directLinkFragments")
     */
    DIRECT_LINK_FRAGMENTS,

}

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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH.
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

package com.openexchange.file.storage.mail.find;

import java.util.Arrays;
import java.util.List;


/**
 * {@link MailDriveFindConstants}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public final class MailDriveFindConstants {

    /**
     * Initializes a new {@link MailDriveFindConstants}.
     */
    private MailDriveFindConstants() {
        super();
    }

    /** The virtual "global" field for file name and From/To */
    public static final String FIELD_GLOBAL = "global";

    /** The field for file name */
    public static final String FIELD_FILE_NAME = "filename";

    /** The field for From sender */
    public static final String FIELD_FROM = "from";

    /** The field for To recipient */
    public static final String FIELD_TO = "to";

    /** The field for file MIME type */
    public static final String FIELD_FILE_TYPE = "file_mimetype";

    /** The field for file size */
    public static final String FIELD_FILE_SIZE = "file_size";

    /** The field for subject */
    public static final String FIELD_SUBJECT = "subject";

    // ---------------------------------------------------------------------------------------------------------- //

    /** The fields to query for */
    public static final List<String> QUERY_FIELDS = Arrays.asList(new String[] { FIELD_FILE_NAME, FIELD_FROM, FIELD_TO });


}

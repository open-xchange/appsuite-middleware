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

package com.openexchange.onboarding;

/**
 * {@link ObjectResult} - A result when an on-boarding configuration has been successfully executed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class ObjectResult implements Result {

    /**
     * Creates a new {@link ObjectResult} returning a contributable object.
     *
     * @param resultObject The result object; e.g. <code>PListDict</code>
     * @param format The result object's format; e.g. <code>"plist"</code>
     * @return The result
     */
    public static ObjectResult contributingResult(Object resultObject, String format) {
        return new ObjectResult(resultObject, format, ResultReply.NEUTRAL);
    }

    /**
     * Creates a new {@link ObjectResult} returning a non-contributable (therefore final) object.
     *
     * @param resultObject The result object; e.g. <code>IFileHolder</code>
     * @param format The result object's format; e.g. <code>"file"</code>
     * @return The result
     */
    public static ObjectResult terminatingResult(Object resultObject, String format) {
        return new ObjectResult(resultObject, format, ResultReply.ACCEPT);
    }

    // --------------------------------------------------------------------------------------------------------------------------

    private final Object resultObject;
    private final String format;
    private final ResultReply reply;

    /**
     * Initializes a new {@link ObjectResult}.
     *
     * @param resultObject The result object; e.g. <code>IFileHolder</code>
     * @param format The result object's format; e.g. <code>"file"</code>
     */
    private ObjectResult(Object resultObject, String format, ResultReply reply) {
        super();
        this.resultObject = resultObject;
        this.format = format;
        this.reply = reply;
    }

    /**
     * Gets the result object
     *
     * @return The result object or <code>null</code>
     */
    public Object getResultObject() {
        return resultObject;
    }

    /**
     * Gets the format
     *
     * @return The format or <code>null</code>
     */
    public String getFormat() {
        return format;
    }

    @Override
    public ResultReply getReply() {
        return reply;
    }

}

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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.messaging;


/**
 * {@link ContentDisposition} - The Content-Disposition header.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public interface ContentDisposition extends ParameterizedMessagingHeader {

    /**
     * The constant for "inline" disposition.
     */
    public static final String INLINE = "inline";

    /**
     * The constant for "attachment" disposition.
     */
    public static final String ATTACHMENT = "attachment";

    /**
     * Applies given content disposition to this content disposition
     * 
     * @param contentDisposition The content disposition to apply
     */
    public void setContentDispositio(final ContentDisposition contentDisposition);

    /**
     * @return disposition
     */
    public String getDisposition();

    /**
     * Sets disposition
     */
    public void setDisposition(final String disposition);

    /**
     * Sets <code>"filename"</code> parameter
     */
    public void setFilenameParameter(final String filename);

    /**
     * @return the <code>"filename"</code> parameter value or <code>null</code> if not present
     */
    public String getFilenameParameter();

    /**
     * @return <code>true</code> if <code>"filename"</code> parameter is present, <code>false</code> otherwise
     */
    public boolean containsFilenameParameter();

    /**
     * Sets the Content-Disposition.
     * 
     * @param contentDisposition The Content-Disposition as a string
     * @throws MessagingException If applying Content-Disposition fails
     */
    public void setContentDisposition(final String contentDisposition) throws MessagingException;

    /**
     * Checks if disposition is inline
     * 
     * @return <code>true</code> if disposition is inline; otherwise <code>false</code>
     */
    public boolean isInline();

    /**
     * Checks if disposition is attachment
     * 
     * @return <code>true</code> if disposition is attachment; otherwise <code>false</code>
     */
    public boolean isAttachment();

}

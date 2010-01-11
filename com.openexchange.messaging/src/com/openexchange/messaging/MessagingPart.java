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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.util.Collection;
import java.util.Map;

/**
 * {@link MessagingPart} . A message part.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MessagingPart {

    /**
     * The part should be presented as an attachment.
     */
    public static final String ATTACHMENT = "attachment";

    /**
     * The part should be presented inline.
     */
    public static final String INLINE = "inline";

    /**
     * Gets the headers as an unmodifiable {@link Map}.
     * 
     * @return The headers as an unmodifiable {@link Map}.
     */
    public Map<String, Collection<MessageHeader>> getHeaders();

    /**
     * Gets the header associated with specified name or <code>null</code> if not present
     * 
     * @param name The header name
     * @return The header associated with specified name or <code>null</code> if not present
     */
    public Collection<MessageHeader> getHeader(String name);

    /**
     * Gets the disposition.
     * <p>
     * The disposition describes how the part should be presented (see RFC 2183). The return value should be compared case-insensitive. For
     * example:
     * <p>
     * 
     * <pre>
     * String disposition = part.getDisposition();
     * if (disposition == null || MessagingPart.ATTACHMENT.equalsIgnoreCase(disposition))
     *  // treat as attachment if not first part
     * </pre>
     * 
     * @return The disposition of this part, or null if unknown
     * @throws MessagingException
     * @see #ATTACHMENT
     * @see #INLINE
     */
    public String getDisposition() throws MessagingException;

    /**
     * Get the filename associated with this part, if possible.
     * <p>
     * Useful if this part represents an "attachment" that was loaded from a file. The filename will usually be a simple name, not including
     * directory components.
     * 
     * @return The filename to associate with this part
     */
    public String getFileName() throws MessagingException;

    /**
     * Gets the identifier.
     * 
     * @return The identifier
     */
    public String getId();

    /**
     * Gets the content.
     * 
     * @return The content
     */
    public MessagingContent getContent();

}

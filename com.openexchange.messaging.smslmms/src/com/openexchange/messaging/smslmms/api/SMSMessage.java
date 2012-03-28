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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.messaging.smslmms.api;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.messaging.CaptchaParams;

/**
 * {@link SMSMessage} - Represents a SMS/MMS message.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface SMSMessage {

    /**
     * The parameter name for captcha parameters.
     */
    public static final String PARAM_CAPTCHA_PARAMS = "__captchaParams";

    /**
     * Gets the message text.
     * 
     * @return The message text
     */
    public String getMessage();

    /**
     * Gets the associated files.
     * 
     * @return The files.
     */
    public List<ManagedFile> getFiles();

    /**
     * Adds specified attachment.
     * 
     * @param managedFile The attachment as a managed file
     * @throws OXException If attaching denoted file fails
     */
    void addAttachment(ManagedFile managedFile) throws OXException;

    /**
     * Adds the attachment associated with specified identifier.
     * 
     * @param attachmentId The attachment identifier
     * @throws OXException If attaching denoted file fails
     */
    void addAttachment(String attachmentId) throws OXException;

    /**
     * Gets the sender.
     * 
     * @return The sender
     */
    String getSender();

    /**
     * Gets the recipients.
     * 
     * @return The recipients
     */
    Set<String> getRecipients();

    /**
     * Gets the identifier.
     * 
     * @return The identifier or <code>null</code> if not available
     */
    String getId();

    /**
     * Gets the folder full name.
     * 
     * @return The folder full name or <code>null</code> if not available
     */
    String getFolder();

    /**
     * Get the size of this part in bytes. Return <code>-1</code> if the size cannot be determined.
     * 
     * @return The size of this part or <code>-1</code>
     * @throws OXException If size cannot be returned
     */
    long getSize() throws OXException;

    /**
     * Sets the captcha parameters
     * 
     * @param params The captcha parameters
     */
    void setCaptchaParameters(CaptchaParams captchaParams);

    /**
     * Gets the captcha parameters
     * 
     * @return The captcha parameters
     */
    CaptchaParams getCaptchaParams();

    /**
     * Gets all parameters of this message as a map.
     * <p>
     * Note: Any modifications applied to returned map will also be reflectedf in message's parameters.
     * 
     * @return The parameters as a map
     */
    Map<String, Object> getParameters();

    /**
     * Gets the associated parameter value.
     * 
     * @param name The parameter name
     * @return The parameter value or <code>null</code> if absent
     */
    Object getParameter(String name);

    /**
     * Puts specified parameter (and thus overwrites any existing parameter)
     * 
     * @param name The parameter name
     * @param value The parameter value
     */
    void putParameter(String name, Object value);

    /**
     * Puts specified parameter if not already present.
     * 
     * @param name The parameter name
     * @param value The parameter value
     * @return <code>true</code> if parameter has been put; otherwise <code>false</code> if already present
     */
    boolean putParameterIfAbsent(String name, Object value);

    /**
     * Clears all parameters associated with this message.
     */
    void clearParameters();

    /**
     * Checks if this message contains denoted parameter.
     * 
     * @param name The parameter name
     * @return <code>true</code> if such a parameter exists; <code>false</code> if absent
     */
    boolean containsParameter(String name);
}

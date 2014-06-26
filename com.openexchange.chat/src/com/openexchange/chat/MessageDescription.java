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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.chat;

/**
 * {@link MessageDescription} - Provides changeable attributes of a {@link Message message}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessageDescription {

    private String messageId;

    private String subject;

    private String text;

    /**
     * Initializes a new {@link MessageDescription}.
     *
     * @param messageId The message identifier; <code>null</code> for new messages
     */
    public MessageDescription(final String messageId) {
        super();
        this.messageId = messageId;
    }

    /**
     * Initializes a new {@link MessageDescription}.
     */
    public MessageDescription() {
        this(null);
    }

    /**
     * Sets the message identifier (aka packet identifier).
     *
     * @param messageId The message identifier to set
     */
    public void setMessageId(final String messageId) {
        this.messageId = messageId;
    }

    /**
     * Checks if this message description provides any changed attribute.
     *
     * @return <code>true</code> if this message description provides any changed attribute; otherwise <code>false</code>
     */
    public boolean hasAnyAttribute() {
        if (null != subject) {
            return true;
        }
        if (null != text) {
            return true;
        }
        return false;
    }

    /**
     * Gets the message identifier (aka packet identifier).
     *
     * @return The message identifier
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Gets the subject
     *
     * @return The subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the subject
     *
     * @param subject The subject to set
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }

    /**
     * Gets the text
     *
     * @return The text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text
     *
     * @param text The text to set
     */
    public void setText(final String text) {
        this.text = text;
    }

}

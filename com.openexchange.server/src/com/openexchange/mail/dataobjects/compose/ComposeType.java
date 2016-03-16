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

package com.openexchange.mail.dataobjects.compose;

/**
 * {@link ComposeType} - The compose type of a message
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum ComposeType {

    /**
     * New
     */
    NEW(0),
    /**
     * Forward
     */
    FORWARD(2),
    /**
     * Reply
     */
    REPLY(1),
    /**
     * Draft-Edit
     */
    DRAFT_EDIT(3),
    /**
     * Draft
     */
    DRAFT(4),
    /**
     * New SMS (special handling for contained text; e.g. no html-to-text conversion)
     */
    NEW_SMS(5),
    /**
     * Draft with <code>deleteDraftOnTransport</code> enabled.
     */
    DRAFT_DELETE_ON_TRANSPORT(6),
    /**
     * Draft with <code>deleteDraftOnTransport</code> explicitly disabled.
     */
    DRAFT_NO_DELETE_ON_TRANSPORT(7),

    ;

    private final int type;

    private ComposeType(final int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    /**
     * Gets the corresponding {@link ComposeType}
     *
     * @param type The send type as <code>int</code>
     * @return The corresponding {@link ComposeType} or <code>null</code>
     */
    public static final ComposeType getType(final int type) {
        final ComposeType[] types = ComposeType.values();
        for (final ComposeType composeType : types) {
            if (composeType.type == type) {
                return composeType;
            }
        }
        return null;
    }
}

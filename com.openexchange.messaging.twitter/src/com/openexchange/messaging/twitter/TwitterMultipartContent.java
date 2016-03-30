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

package com.openexchange.messaging.twitter;

import com.openexchange.messaging.MessagingBodyPart;
import com.openexchange.messaging.MultipartContent;
import com.openexchange.session.Session;
import com.openexchange.twitter.Status;

/**
 * {@link TwitterMultipartContent}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMultipartContent implements MultipartContent {

    /**
     * Gets a newly created instance of {@link TwitterMultipartContent}.
     *
     * @param status The twitter status to turn into a multipart/alternative content
     * @return A newly created instance of {@link TwitterMultipartContent}
     */
    public static TwitterMultipartContent newInstance(final Status status, final Session session) {
        final TwitterMultipartContent tmp = new TwitterMultipartContent();
        tmp.parts[0] = new TwitterMessagingBodyPart(status, false, tmp, session);
        tmp.parts[1] = new TwitterMessagingBodyPart(status, true, tmp, session);
        return tmp;
    }

    private final MessagingBodyPart[] parts;

    private TwitterMultipartContent() {
        super();
        parts = new MessagingBodyPart[2];
    }

    @Override
    public int getCount() {
        return parts.length;
    }

    @Override
    public MessagingBodyPart get(final int index) {
        return parts[index];
    }

    @Override
    public String getSubType() {
        return "alternative";
    }

}

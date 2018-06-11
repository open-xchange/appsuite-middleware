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

package com.openexchange.ajax.chronos.factory;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.openexchange.test.pool.TestUser;

/**
 * {@link ITipMailFactory}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ITipMailFactory {

    private TestUser from;
    private TestUser to;

    private String iCal;
    private String subject;

    /**
     * Initializes a new {@link ITipMailFactory}.
     * 
     * @param from The user who sends the mail
     * @param to The user who receive the mail
     * @param iCal The iCal file as {@link String}
     * 
     */
    public ITipMailFactory(TestUser from, TestUser to, String iCal) {
        super();
        this.from = from;
        this.to = to;
        this.iCal = iCal;
    }

    public ITipMailFactory setFrom(TestUser from) {
        this.from = from;
        return this;
    }

    public ITipMailFactory setTo(TestUser to) {
        this.to = to;
        return this;
    }

    public ITipMailFactory setICal(String iCal) {
        this.iCal = iCal;
        return this;
    }

    public ITipMailFactory setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String build() {
        StringBuilder sb = new StringBuilder();

        sb.append("Return-Path:");
        setUser(sb, from);
        addNewLine(sb);

        sb.append("Delivered-To:");
        setUser(sb, to);
        addNewLine(sb);

        addReceived(sb);

        sb.append("Date: ");
        addDateTime(sb, 0);
        addNewLine(sb);

        sb.append("From: \"").append(from.getUser()).append("\"");
        setUser(sb, from);
        addNewLine(sb);

        sb.append("Reply-To: \"").append(from.getUser()).append("\"");
        setUser(sb, from);
        addNewLine(sb);

        sb.append("To: ").append(to.getUser());
        setUser(sb, to);
        addNewLine(sb);

        sb.append("Subject: New appointment: ").append(subject);
        addNewLine(sb);

        addStatics(sb);
        addBoundary(sb);
        setBoundary(sb);

        sb.append("Content-Type: text/calendar; charset=UTF-8; method=REQUEST");
        addNewLine(sb);

        sb.append("Content-Transfer-Encoding: 7bit");
        addNewLine(sb);

        sb.append(iCal);

        setBoundary(sb);

        addNewLine(sb);
        return sb.toString();
    }

    private void setBoundary(StringBuilder sb) {
        sb.append("------=_Part_11_403635617.1528446307983");
        addNewLine(sb);
    }

    private void addBoundary(StringBuilder sb) {
        sb.append(" ------=_Part_10_470012375.1528446307462");
        addNewLine(sb);
        sb.append("Content-Type: multipart/alternative;");
        addNewLine(sb);
        sb.append("boundary=\"----=_Part_11_403635617.1528446307983\"");
        addNewLine(sb);
        addNewLine(sb);
    }

    private void addStatics(StringBuilder sb) {
        sb.append("MIME-Version: 1.0");
        addNewLine(sb);

        sb.append("Content-Type: multipart/mixed;");
        addNewLine(sb);

        sb.append("boundary=\"----=_Part_10_470012375.1528446307462\"");
        addNewLine(sb);

        sb.append("X-Priority: 3 (normal)");
        addNewLine(sb);

        sb.append("X-Mailer: Open-Xchange Mailer v7.10.0-Rev6");
        addNewLine(sb);

        sb.append("Auto-Submitted: auto-generated");
        addNewLine(sb);

        sb.append("X-Originating-Client: open-xchange-appsuite");
        addNewLine(sb);
        addNewLine(sb);
    }

    private void addReceived(StringBuilder sb) {
        sb.append("Received: from singlenode.example.com");
        addNewLine(sb);
        sb.append("by backend.oxoe.int (Dovecot) with LMTP id hGegCWQ9GltUKQAAkOuz7g");
        addNewLine(sb);
        sb.append("for");
        setFrom(to);
        sb.append(";");
        addDateTime(sb, 100);
        addNewLine(sb);
    }

    private void addDateTime(StringBuilder sb, int minus) {
        sb.append(new SimpleDateFormat("MMMM dd HH:mm:ss zzzz yyyy").format(new Date(System.currentTimeMillis() - minus)));
    }

    private void setUser(StringBuilder sb, TestUser user) {
        sb.append(" <").append(user.getLogin()).append("@").append(user.getContext()).append(">");
    }

    private void addNewLine(StringBuilder sb) {
        sb.append("\n");
    }

}

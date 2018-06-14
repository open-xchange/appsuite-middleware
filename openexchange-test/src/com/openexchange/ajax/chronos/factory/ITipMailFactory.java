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
import java.util.Base64;
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
        this(from, to, iCal, "Test invitation for iTIP");
    }

    /**
     * Initializes a new {@link ITipMailFactory}.
     * 
     * @param from The user who sends the mail
     * @param to The user who receive the mail
     * @param iCal The iCal file as {@link String}
     * @param subject The mail subject
     * 
     */
    public ITipMailFactory(TestUser from, TestUser to, String iCal, String subject) {
        super();
        this.from = from;
        this.to = to;
        this.iCal = iCal;
        this.subject = subject;
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
        addNewLine(sb);

        sb.append("MIME-Version: 1.0");
        addNewLine(sb);
        sb.append("Content-Type: text/plain; charset=UTF-8");
        addNewLine(sb);
        sb.append("Content-Transfer-Encoding: 7bit");
        addNewLine(sb, 2);
        sb.append("You have been invited to an event by");
        addNewLine(sb, 4);//@formatter:off
        sb.append("====[       Subject       ]====\n" + 
            "\n" + 
            "All times will be shown in the timezone Central European Time\n" + 
            "\n" + 
            "When: Friday, June 8, 2018 12:00 PM - 12:30 PM\n" + 
            "Where: Location\n" + 
            "\n" + 
            "\n" + 
            "====================================\n" + 
            "Description\n" + 
            "====================================\n" + 
            "\n" + 
            "== Participants: ==\n" + 
            "\n" + 
            "anton, anton (accepted)\n" + 
            "berta (waiting)\n" + 
            "\n" + 
            "== Resources ==\n" + 
            "\n" + 
            "\n" + 
            "== Details: ==\n" + 
            "\n" + 
            "Show as: Reserved\n" + 
            "Created: Friday, June 8, 2018 10:04 AM - anton, anton");//@formatter:on
        addNewLine(sb, 8);
        setBoundary(sb);
        addNewLine(sb);

        sb.append("MIME-Version: 1.0");
        addNewLine(sb);
        sb.append("Content-Type: text/html; charset=UTF-8");
        addNewLine(sb);
        sb.append("Content-Transfer-Encoding: quoted-printable");
        addNewLine(sb, 2);
        addHTML(sb);
        addNewLine(sb, 2);
        setBoundary(sb);
        addNewLine(sb);

        sb.append("Content-Type: text/calendar; charset=UTF-8; method=REQUEST");
        addNewLine(sb);

        sb.append("Content-Transfer-Encoding: 7bit");
        addNewLine(sb, 2);

        sb.append(iCal);
        addNewLine(sb, 2);

        setBoundary(sb);
        sb.append("--");
        addNewLine(sb, 2);

        addIcalAsAttachment(sb);
        sb.append("------=_Part_10_470012375.1528446307462--");
        addNewLine(sb);
        return sb.toString();
    }

    private void addIcalAsAttachment(StringBuilder sb) {
        sb.append("------=_Part_10_470012375.1528446307462\n");
        sb.append("Content-Type: application/ics; charset=UTF-8; method=REQUEST;\n");
        sb.append(" name=invite.ics\n");
        sb.append("Content-Transfer-Encoding: base64\n");
        sb.append("Content-Disposition: attachment; filename=invite.ics\n");
        addNewLine(sb);
        sb.append(Base64.getMimeEncoder().encodeToString(iCal.getBytes()));
        addNewLine(sb);
    }

    private void setBoundary(StringBuilder sb) {
        sb.append("------=_Part_11_403635617.1528446307983");
    }

    private void addBoundary(StringBuilder sb) {
        sb.append("------=_Part_10_470012375.1528446307462");
        addNewLine(sb);
        sb.append("Content-Type: multipart/alternative; ");
        addNewLine(sb);
        sb.append("\tboundary=\"----=_Part_11_403635617.1528446307983\"");
        addNewLine(sb, 2);
    }

    private void addStatics(StringBuilder sb) {
        sb.append("MIME-Version: 1.0");
        addNewLine(sb);

        sb.append("Content-Type: multipart/mixed; ");
        addNewLine(sb);

        sb.append("\tboundary=\"----=_Part_10_470012375.1528446307462\"");
        addNewLine(sb);

        sb.append("X-Priority: 3 (normal)");
        addNewLine(sb);

        sb.append("X-Mailer: Open-Xchange Mailer v7.10.0-Rev6");
        addNewLine(sb);

        sb.append("Auto-Submitted: auto-generated");
        addNewLine(sb);

        sb.append("X-Originating-Client: open-xchange-appsuite");
        addNewLine(sb, 2);
    }

    private void addReceived(StringBuilder sb) {
        sb.append("Received: from singlenode.example.com");
        addNewLine(sb);
        sb.append("\tby backend.oxoe.int (Dovecot) with LMTP id hGegCWQ9GltUKQAAkOuz7g");
        addNewLine(sb);
        sb.append("\tfor");
        setUser(sb, to);
        sb.append(";");
        addDateTime(sb, 100);
        addNewLine(sb);
    }

    private void addHTML(StringBuilder sb) {
        sb.append("<!doctype html>\n" + //@formatter:off
    "<html>\n" + 
    " <head>=20\n" + 
    "  <title></title>=20\n" + 
    "  <meta charset=3D\"UTF-8\">=20\n" + 
    "  <style type=3D\"text/css\">\n" + 
    "            .content {\n" + 
    " white-space: normal;\n" + 
    " color: black;\n" + 
    " font-family: Arial, Helvetica, sans-serif;\n" + 
    " font-size: 12px;\n" + 
    " cursor: default;\n" + 
    "}\n" + 
    "/* shown_as */\n" + 
    "\n" + 
    "\n" + 
    ".shown_as.reserved { background-color: #08c; } /* blue */\n" + 
    ".shown_as.temporary { background-color: #fc0; } /* yellow */\n" + 
    ".shown_as.absent { background-color: #913F3F; } /* red */\n" + 
    ".shown_as.free { background-color: #8EB360; } /* green */\n" + 
    "\n" + 
    ".shown_as_label.reserved { color: #08c; } /* blue */\n" + 
    ".shown_as_label.temporary { color: #fc0; } /* yellow */\n" + 
    ".shown_as_label.absent { color: #913F3F; } /* red */\n" + 
    ".shown_as_label.free { color: #8EB360; } /* green */\n" + 
    "\n" + 
    "em {\n" + 
    " font-weight: bold;\n" + 
    "}\n" + 
    "\n" + 
    "/* Detail view */\n" + 
    "\n" + 
    ".timezone {\n" + 
    " margin-bottom: 2em;\n" + 
    "}\n" + 
    "\n" + 
    ".justification, .attachmentNote {\n" + 
    " margin-top: 2em;\n" + 
    " margin-bottom: 2em;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .action {\n" + 
    " float: right;\n" + 
    " margin-right: 1em;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .link {\n" + 
    " cursor: pointer;\n" + 
    " text-decoration: underline;\n" + 
    " color: #00a0cd;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .calendar-buttons {\n" + 
    " height: 2em;\n" + 
    " text-align: right;\n" + 
    " line-height: 2em;\n" + 
    " border-bottom: 1px solid #f0f0f0;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .date {\n" + 
    "    font-size: 11pt;\n" + 
    "    color: #ccc;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .interval {\n" + 
    "    color: #555;\n" + 
    "    white-space: nowrap;\n" + 
    "    float: right;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .day {\n" + 
    "    color: #888;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .title {\n" + 
    "    font-size: 18pt;\n" + 
    "    line-height: 22pt;\n" + 
    "    margin: 0.25em 0 0.25em 0;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .location {\n" + 
    "    font-size: 11pt;\n" + 
    "    color: #888;\n" + 
    "    margin-bottom: 1em;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .label {\n" + 
    "    font-size: 9pt;\n" + 
    "    color: #888;\n" + 
    "    clear: both;\n" + 
    "    border-bottom: 1px solid #ccc;\n" + 
    "    padding: 1em 0 0.25em 0em;\n" + 
    "    margin-bottom: 0.5em;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .note {\n" + 
    "    max-width: 550px;\n" + 
    "    margin: 2em 0 1em 0;\n" + 
    "    -webkit-user-select: text;\n" + 
    "    -moz-user-select: text;\n" + 
    "    user-select: text;\n" + 
    "    cursor: text;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .participants {\n" + 
    "    min-height: 2em;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .participants table {\n" + 
    "    text-align: left;\n" + 
    "    vertical-align: left;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .participant {\n" + 
    "    line-height: 1.2 em;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .detail-label {\n" + 
    "    display: inline-block;\n" + 
    "    width: 110px;\n" + 
    "    white-space: nowrap;\n" + 
    "    color: #666;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .detail {\n" + 
    "    white-space: nowrap;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .detail.shown_as {\n" + 
    "    display: inline-block;\n" + 
    "    height: 1em;\n" + 
    "    width: 1em;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .participant .status { font-weight: bold; }\n" + 
    ".calendar-detail .participant .status.accepted { color: #8EB360; } /* green=\n" + 
    " */\n" + 
    ".calendar-detail .participant .status.declined { color: #913F3F; } /* red *=\n" + 
    "/\n" + 
    ".calendar-detail .participant .status.tentative { color: #c80; } /* orange =\n" + 
    "*/\n" + 
    "\n" + 
    ".calendar-detail .participant .comment {\n" + 
    "    color: #888;\n" + 
    "    display: block;\n" + 
    "    white-space: normal;\n" + 
    "    padding-left: 1em;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-detail .group {\n" + 
    "    margin: 0.75em 0 0.25em 0;\n" + 
    "    color: #333;\n" + 
    "}\n" + 
    "\n" + 
    ".person, .person-link {\n" + 
    " color: #00A0CD;\n" + 
    "}\n" + 
    "\n" + 
    ".clear-title {\n" + 
    " font-family: OpenSans, Helvetica, Arial, sans-serif;\n" + 
    " font-weight: 200;\n" + 
    " font-size: 20pt;\n" + 
    " line-height: 1.15em;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-action {\n" + 
    " margin-bottom: 2em;\n" + 
    " font-family: OpenSans, Helvetica, Arial, sans-serif;\n" + 
    " font-weight: 200;\n" + 
    " font-size: 12pt;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-action .changes{\n" + 
    "    margin-top: 2em;\n" + 
    " font-size: 11pt;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-action .changes .original {\n" + 
    "    font-weight: bold;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-action .changes .recurrencePosition {\n" + 
    "    font-weight: bold;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-action .changes .updated {\n" + 
    "    color: green;\n" + 
    "    font-weight: bold;\n" + 
    "}\n" + 
    "\n" + 
    ".calendar-action .status {  }\n" + 
    ".calendar-action  .status.accepted { color: #8EB360; } /* green */\n" + 
    ".calendar-action  .status.declined { color: #913F3F; } /* red */\n" + 
    ".calendar-action  .status.tentative { color: #c80; } /* orange */\n" + 
    "\n" + 
    "        </style>=20\n" + 
    " </head>\n" + 
    " <body>=20\n" + 
    "  <div class=3D\"content\">=20\n" + 
    "   <div class=3D\"timezone\">\n" + 
    "     All times will be shown in the timezone=20\n" + 
    "    <em>Eastern Standard Time</em>=20\n" + 
    "   </div>=20\n" + 
    "   <div class=3D\"calendar-action\">\n" + 
    "    You have been invited to an event by=20\n" + 
    "    <span class=3D\"person\">anton, anton</span>:\n" + 
    "   </div>=20\n" + 
    "   <div class=3D\"calendar-detail\">=20\n" + 
    "    <div class=3D\"date\">=20\n" + 
    "     <div class=3D\"interval\">\n" + 
    "       7:00 AM - 7:30 AM=20\n" + 
    "     </div>=20\n" + 
    "     <div class=3D\"day\">\n" + 
    "       Friday, June 8, 2018=20\n" + 
    "     </div>=20\n" + 
    "    </div>=20\n" + 
    "    <div class=3D\"title clear-title\">\n" + 
    "      asd=20\n" + 
    "    </div>=20\n" + 
    "    <div class=3D\"location\">=20\n" + 
    "    </div>=20\n" + 
    "    <div class=3D\"calendar-buttons\" style=3D\"display:none\"></div>=20\n" + 
    "    <div class=3D\"note\">=20\n" + 
    "    </div>=20\n" + 
    "    <div class=3D\"participants\">=20\n" + 
    "     <div class=3D\"label\">\n" + 
    "       Participants:=20\n" + 
    "     </div>=20\n" + 
    "     <div class=3D\"participant-list\">=20\n" + 
    "      <div class=3D\"participant\">=20\n" + 
    "       <span class=3D\"person\">anton, anton</span>=20\n" + 
    "       <span class=3D\"status accepted\">=E2=9C=93</span>=20\n" + 
    "       <span class=3D\"comment\"></span>=20\n" + 
    "      </div>=20\n" + 
    "      <div class=3D\"participant\">=20\n" + 
    "       <span class=3D\"person\">berta</span>=20\n" + 
    "       <span class=3D\"comment\"></span>=20\n" + 
    "      </div>=20\n" + 
    "     </div>=20\n" + 
    "     <div class=3D\"participants-clear\"></div>=20\n" + 
    "    </div>=20\n" + 
    "    <div class=3D\"participants\">=20\n" + 
    "     <div class=3D\"label\">\n" + 
    "       Resources=20\n" + 
    "     </div>=20\n" + 
    "     <div class=3D\"participant-list\">=20\n" + 
    "     </div>=20\n" + 
    "     <div class=3D\"participants-clear\"></div>=20\n" + 
    "    </div>=20\n" + 
    "    <div>=20\n" + 
    "     <div class=3D\"label\">\n" + 
    "       Details:=20\n" + 
    "     </div>\n" + 
    "     <span class=3D\"detail-label\">Show as:&nbsp;</span>\n" + 
    "     <span class=3D\"detail\"><span class=3D\"shown_as_label reserved\">Reserve=\n" + 
    "d</span></span>\n" + 
    "     <br>=20\n" + 
    "     <span class=3D\"detail-label\">Created:&nbsp;</span>\n" + 
    "     <span class=3D\"detail\"><span>Friday, June 8, 2018 4:25 AM</span> <span=\n" + 
    ">-</span> <span>anton, anton</span></span>=20\n" + 
    "    </div>=20\n" + 
    "    <div class=3D\"attachmentNote\">=20\n" + 
    "    </div>=20\n" + 
    "    <div class=3D\"justification\">=20\n" + 
    "    </div>=20\n" + 
    "   </div>=20\n" + 
    "  </div>=20\n" + 
    " </body>\n" + 
    "</html>");//@formatter:on
    }

    private void addDateTime(StringBuilder sb, int minus) {
        sb.append(new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date(System.currentTimeMillis() - minus)));
    }

    private void setUser(StringBuilder sb, TestUser user) {
        sb.append(" <").append(user.getLogin()).append(">");
    }

    private void addNewLine(StringBuilder sb) {
        sb.append("\n");
    }

    private void addNewLine(StringBuilder sb, int c) {
        for (int i = 0; i < c; i++) {
            addNewLine(sb);
        }
    }

}

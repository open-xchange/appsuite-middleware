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

package com.openexchange.mailaccount;

import com.openexchange.exception.OXException;


/**
 * {@link AttributeSwitch}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public interface AttributeSwitch {

    public Object id();
    public Object login();
    public Object password();
    public Object mailURL() throws OXException;
    public Object transportURL() throws OXException;
    public Object name();
    public Object primaryAddress();
    public Object personal();
    public Object spamHandler();
    public Object trash();
    public Object archive();
    public Object sent();
    public Object drafts();
    public Object spam();
    public Object confirmedSpam();
    public Object confirmedHam();
    public Object mailServer();
    public Object mailPort();
    public Object mailProtocol();
    public Object mailSecure();
    public Object transportServer();
    public Object transportPort();
    public Object transportProtocol();
    public Object transportSecure();
    public Object transportLogin();
    public Object transportPassword();
    public Object unifiedINBOXEnabled();
    public Object trashFullname();
    public Object archiveFullname();
    public Object sentFullname();
    public Object draftsFullname();
    public Object spamFullname();
    public Object confirmedSpamFullname();
    public Object confirmedHamFullname();
    public Object pop3RefreshRate();
    public Object pop3ExpungeOnQuit();
    public Object pop3DeleteWriteThrough();
    public Object pop3Storage();
    public Object pop3Path();
    public Object addresses();
    public Object replyTo();
    public Object transportAuth();
    public Object mailStartTls();
    public Object transportStartTls();
    public Object rootFolder();

}

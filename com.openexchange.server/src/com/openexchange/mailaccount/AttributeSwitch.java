/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
    public Object mailOAuth();
    public Object transportOAuth();
    public Object rootFolder();
    public Object mailDisabled();
    public Object transportDisabled();

}

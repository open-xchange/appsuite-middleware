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

package com.openexchange.mail.messagestorage;

import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.parser.MailMessageParser;
import com.openexchange.mail.parser.handlers.JsonMessageHandler;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.sessiond.impl.SessionObject;

/**
 * {@link MailRFC2231Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class MailRFC2231Test extends AbstractMailTest {

	private static final String RFC2231_1 = "From: Marcus Klein <m.klein@open-xchange.com>\n"
			+ "Organization: Netline Internet Service GmbH\n"
			+ "X-KMail-Fcc: sent-mail\n"
			+ "To: marcus@1337\n"
			+ "Date: Wed, 9 Jan 2008 11:01:10 +0100\n"
			+ "User-Agent: KMail/1.9.7\n"
			+ "MIME-Version: 1.0\n"
			+ "Content-Type: Multipart/Mixed;\n"
			+ "  boundary=\"Boundary-00=_mtJhHd7H54sG6XG\"\n"
			+ "X-KMail-Recipients: marcus@1337\n"
			+ "Status: R\n"
			+ "Subject: RFC2231-Test\n"
			+ "X-Status: N\n"
			+ "X-KMail-EncryptionState:  \n"
			+ "X-KMail-SignatureState:  \n"
			+ "X-KMail-MDN-Sent:  \n"
			+ "X-Length: 1307\n"
			+ "X-UID: 6\n"
			+ "\n"
			+ "--Boundary-00=_mtJhHd7H54sG6XG\n"
			+ "Content-Type: text/plain;\n"
			+ "  charset=\"utf-8\"\n"
			+ "Content-Transfer-Encoding: 7bit\n"
			+ "Content-Disposition: inline\n"
			+ "\n"
			+ "\n"
			+ "-- \n"
			+ "Marcus Klein\n"
			+ "--\n"
			+ "Netline Internet Service GmbH\n"
			+ "\n"
			+ "There are 10 kinds of humans - those, who understand the\n"
			+ "binary system, and those, who do not understand it.\n"
			+ "\n"
			+ "--Boundary-00=_mtJhHd7H54sG6XG\n"
			+ "Content-Type: text/plain;\n"
			+ "  charset=\"utf-8\";\n"
			+ "  name*=utf-8''test%20%C3%A4%C3%B6%C3%BC%2Etxt\n"
			+ "Content-Transfer-Encoding: base64\n"
			+ "Content-Disposition: attachment;\n"
			+ "	filename*=utf-8''test%20%C3%A4%C3%B6%C3%BC%2Etxt\n"
			+ "\n"
			+ "dGVzdMOkw7bDvAo=\n"
			+ "\n"
			+ "--Boundary-00=_mtJhHd7H54sG6XG\n"
			+ "Content-Type: text/plain; charset=\"utf-8\"; name*1*=utf-8''%EC%84%9C%EC%98%81%EC%A7%84; name*2*=funny%2Etxt\n"
			+ "Content-Transfer-Encoding: base64\n" + "Content-Disposition: attachment;\n"
			+ "	filename*=utf-8''%EC%84%9C%EC%98%81%EC%A7%84%2Etxt\n" + "\n" + "7ISc7JiB7KeE\n" + "\n"
			+ "--Boundary-00=_mtJhHd7H54sG6XG--\n";

	private static final String RFC2231_2 = "Return-Path:	<usera@dhcp164.netline.de>\n"
			+ "Received:	from dhcp164.netline.de ([unix socket]) by dhcp164.netline.de (Cyrus v2.2.12-Invoca-RPM-2.2.12-3.RHEL4.1) with LMTPA; Wed, 20 Sep 2006 17:16:57 +0200\n"
			+ "X-Sieve:	CMU Sieve 2.2\n"
			+ "Received:	by dhcp164.netline.de (Postfix, from userid 99) id B644FC770F; Wed, 20 Sep 2006 17:16:57 +0200 (CEST)\n"
			+ "Received:	from [192.168.32.167] (dhcp167.netline.de [192.168.32.167]) by dhcp164.netline.de (Postfix) with ESMTP id 77DB1C770C for <usera@dhcp164.netline.de>; Wed, 20 Sep 2006 17:16:57 +0200 (CEST)\n"
			+ "Message-ID:	<45110361.6050205@dhcp164.netline.de>\n"
			+ "Date:	Wed, 20 Sep 2006 11:01:21 +0200\n"
			+ "From:	user a <usera@dhcp164.netline.de>\n"
			+ "User-Agent:	Thunderbird 1.5.0.7 (Windows/20060909)\n"
			+ "MIME-Version:	1.0\n"
			+ "To:	user a <usera@dhcp164.netline.de>\n"
			+ "Subject:	sdfsdfsdf\n"
			+ "Content-Type:	multipart/mixed; boundary=\"------------090609000008050700040700\"\n"
			+ "X-Spam-Checker-Version:	SpamAssassin 3.0.6 (2005-12-07) on dhcp164.netline.de\n"
			+ "X-Spam-Level:	\n"
			+ "X-Spam-Status:	No, score=-2.6 required=5.0 tests=ALL_TRUSTED, DATE_IN_PAST_06_12 autolearn=failed version=3.0.6\n"
			+ "\n"
			+ "\n"
			+ "--------------090609000008050700040700\n"
			+ "Content-Type: text/plain; charset=ISO-8859-15; format=flowed\n"
			+ "Content-Transfer-Encoding: 7bit\n"
			+ "\n"
			+ "sdfsdfsdf\n"
			+ "\n"
			+ "--------------090609000008050700040700\n"
			+ "Content-Type: application/msword; name*0*=windows-1252''%41%63%63%EA%B4%6E%60%74%EA%64%20%46%69%6C%65%2E%64; name*1*=%6F%63\n"
			+ "Content-Transfer-Encoding: base64\n"
			+ "Content-Disposition: inline; filename*0*=windows-1252''%41%63%63%EA%B4%6E%60%74%EA%64%20%46%69%6C%65%2E; filename*1*=%64%6F%63\n"
			+ "\n" + "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\n"
			+ "xtxDEoUDMRz0h3S/1sxL+4w7e1hg3shrB4NJ8zccb3dvmD9q/Z3N3w+n4nAlVklU2p3IO8YY\n"
			+ "XNRZ3bOcRzMAoWsKWFUg84yBoWw0UA3Va9NjrAU1x8YVSMIzW+Lrb9/kA2FNrxPWzXXCur1O\n"
			+ "WHfXCev+OmE9XCes2XXCml8nrGTyv+M6FIVgudBEQRfka7qppL1ojRoBqnvTWghtvrtwJ3X3\n"
			+ "cS9iz5hVBlQ96A0VKoWi2r762mt5aONalrm3G3/+3dusaDhCwrM+gLQboHdvItIi6Jqmt+G6\n"
			+ "t0Sbgvl98AzRFZ7aSxsGRta6llGeSvtTjimHweOp9da+mZr7H/ikKfICf1aA2zhcvyC9ByeU\n"
			+ "skiiDp2jU5mVbGlmfviYjf1x0I7mhG5ys4akd221d4idRkJSWAOusywknEepjk5PnQ+zh9u+\n"
			+ "U+epzMSpWyIdulbkwJx9nHR0RAXeNaszN8X2Fb1WUKT07lxo4EA8eQji4DcgWhEIhzWwSjfz\n"
			+ "WYcSXmtIfqcOzr5WSrvsO064cQm7t557evdL+8Ji36d+nti/KHw37UqwX1cOl1gDwHzE4WKD\n"
			+ "wVNHLRNPqVcLCqwaH81s9aDxdPYpIsTcUjbYAEfe4+6fsx//BVBLBwgVFNKmWAYAAA4fAABQ\n"
			+ "SwMEFAAAAAAAG2IuNbdazngbBAAAGwQAAAgAAABtZXRhLnhtbDw/eG1sIHZlcnNpb249IjEu\n"
			+ "MCIgZW5jb2Rpbmc9IlVURi04Ij8+CjxvZmZpY2U6ZG9jdW1lbnQtbWV0YSB4bWxuczpvZmZp\n"
			+ "Y2U9InVybjpvYXNpczpuYW1lczp0YzpvcGVuZG9jdW1lbnQ6eG1sbnM6b2ZmaWNlOjEuMCIg\n"
			+ "eG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHhtbG5zOmRjPSJo\n"
			+ "dHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIgeG1sbnM6bWV0YT0idXJuOm9hc2lz\n"
			+ "Om5hbWVzOnRjOm9wZW5kb2N1bWVudDp4bWxuczptZXRhOjEuMCIgeG1sbnM6b29vPSJodHRw\n"
			+ "Oi8vb3Blbm9mZmljZS5vcmcvMjAwNC9vZmZpY2UiIG9mZmljZTp2ZXJzaW9uPSIxLjAiPjxv\n"
			+ "ZmZpY2U6bWV0YT48bWV0YTpnZW5lcmF0b3I+T3Blbk9mZmljZS5vcmcvMi4wJExpbnV4IE9w\n"
			+ "ZW5PZmZpY2Uub3JnX3Byb2plY3QvNjgwbTckQnVpbGQtOTA0NDwvbWV0YTpnZW5lcmF0b3I+\n"
			+ "PG1ldGE6Y3JlYXRpb24tZGF0ZT4yMDA2LTA5LTE0VDEyOjA0OjA1PC9tZXRhOmNyZWF0aW9u\n"
			+ "LWRhdGU+PGRjOmRhdGU+MjAwNi0wOS0xNFQxMjowODo0NDwvZGM6ZGF0ZT48ZGM6bGFuZ3Vh\n"
			+ "Z2U+ZW4tVVM8L2RjOmxhbmd1YWdlPjxtZXRhOmVkaXRpbmctY3ljbGVzPjI8L21ldGE6ZWRp\n"
			+ "dGluZy1jeWNsZXM+PG1ldGE6ZWRpdGluZy1kdXJhdGlvbj5QVDRNNDBTPC9tZXRhOmVkaXRp\n"
			+ "bmctZHVyYXRpb24+PG1ldGE6dXNlci1kZWZpbmVkIG1ldGE6bmFtZT0iSW5mbyAxIi8+PG1l\n"
			+ "dGE6dXNlci1kZWZpbmVkIG1ldGE6bmFtZT0iSW5mbyAyIi8+PG1ldGE6dXNlci1kZWZpbmVk\n"
			+ "IG1ldGE6bmFtZT0iSW5mbyAzIi8+PG1ldGE6dXNlci1kZWZpbmVkIG1ldGE6bmFtZT0iSW5m\n"
			+ "byA0Ii8+PG1ldGE6ZG9jdW1lbnQtc3RhdGlzdGljIG1ldGE6dGFibGUtY291bnQ9IjAiIG1l\n"
			+ "dGE6aW1hZ2UtY291bnQ9IjAiIG1ldGE6b2JqZWN0LWNvdW50PSIwIiBtZXRhOnBhZ2UtY291\n"
			+ "bnQ9IjEiIG1ldGE6cGFyYWdyYXBoLWNvdW50PSIxIiBtZXRhOndvcmQtY291bnQ9IjIiIG1l\n"
			+ "dGE6Y2hhcmFjdGVyLWNvdW50PSIxMyIvPjwvb2ZmaWNlOm1ldGE+PC9vZmZpY2U6ZG9jdW1l\n"
			+ "bnQtbWV0YT5QSwMEFAAIAAgAG2IuNQAAAAAAAAAAAAAAABgAAABUaHVtYm5haWxzL3RodW1i\n"
			+ "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\n"
			+ "--------------090609000008050700040700--";

	/**
	 *
	 */
	public MailRFC2231Test() {
		super();
	}

	/**
	 * @param name
	 */
	public MailRFC2231Test(final String name) {
		super(name);
	}

	public void testRFC2231Part1() {
		try {
            final SessionObject session = getSession();

            final MailMessage rfc2231Mail = MimeMessageConverter.convertMessage(RFC2231_1.getBytes(com.openexchange.java.Charsets.US_ASCII));
            final JsonMessageHandler messageHandler = new JsonMessageHandler(
                MailAccount.DEFAULT_ID,
                null,
                rfc2231Mail,
                DisplayMode.DISPLAY, false,
                session,
                UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), session.getContextId()), false, -1);
            new MailMessageParser().parseMailMessage(rfc2231Mail, messageHandler);
            final JSONObject jObject = messageHandler.getJSONObject();
            if (jObject.has(MailJSONField.ATTACHMENTS.getKey())) {
                final JSONArray jArray = jObject.getJSONArray(MailJSONField.ATTACHMENTS.getKey());
                final int len = jArray.length();
                assertTrue("Missing attachments although existence indicated through 'hasAttachments()'", len > 0);
                for (int i = 0; i < len; i++) {
                    final JSONObject attachObj = jArray.getJSONObject(i);
                    if (attachObj.has(MailJSONField.ATTACHMENT_FILE_NAME.getKey())) {
                        final String filename = attachObj.getString(MailJSONField.ATTACHMENT_FILE_NAME.getKey());
                        assertTrue(
                            "Unexpected filename",
                            "\uc11c\uc601\uc9c4.txt".equals(filename) || "test \u00e4\u00f6\u00fc.txt".equals(filename));
                    }
                }
            } else {
                fail("Missing attachments although existence indicated through 'hasAttachments()'");
            }

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testRFC2231Part2() {
        try {
            final SessionObject session = getSession();

            final MailMessage rfc2231Mail = MimeMessageConverter.convertMessage(RFC2231_2.getBytes(com.openexchange.java.Charsets.US_ASCII));
            final JsonMessageHandler messageHandler = new JsonMessageHandler(
                MailAccount.DEFAULT_ID,
                null,
                rfc2231Mail,
                DisplayMode.DISPLAY, false,
                session,
                UserSettingMailStorage.getInstance().getUserSettingMail(session.getUserId(), session.getContextId()), false, -1);
            new MailMessageParser().parseMailMessage(rfc2231Mail, messageHandler);
            final JSONObject jObject = messageHandler.getJSONObject();
            if (jObject.has(MailJSONField.ATTACHMENTS.getKey())) {
                final JSONArray jArray = jObject.getJSONArray(MailJSONField.ATTACHMENTS.getKey());
                final int len = jArray.length();
                assertTrue("Missing attachments although existence indicated through 'hasAttachments()'", len > 0);
                for (int i = 0; i < len; i++) {
                    final JSONObject attachObj = jArray.getJSONObject(i);
                    if (attachObj.has(MailJSONField.ATTACHMENT_FILE_NAME.getKey())) {
                        final String filename = attachObj.getString(MailJSONField.ATTACHMENT_FILE_NAME.getKey());
                        assertTrue("Missing filename", null != filename);
                        assertTrue("Unexpected filename", "Acc\u00ea\u00b4n\u0060t\u00ead File.doc".equals(filename));
                    }
                }
            } else {
                fail("Missing attachments although existence indicated through 'hasAttachments()'");
            }

        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}

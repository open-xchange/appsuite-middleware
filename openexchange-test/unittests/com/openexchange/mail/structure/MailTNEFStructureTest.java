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

package com.openexchange.mail.structure;

import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.structure.handler.MIMEStructureHandler;

/**
 * {@link MailTNEFStructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailTNEFStructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link MailTNEFStructureTest}.
     */
    public MailTNEFStructureTest() {
        super();
    }

    /**
     * Initializes a new {@link MailTNEFStructureTest}.
     *
     * @param name The test name
     */
    public MailTNEFStructureTest(final String name) {
        super(name);
    }



    private static final byte[] SIMPLE = ("From: postmaster@integralis.com\n" +
    		"To: martin.kauss@open-xchange.com\n" +
    		"Date: Wed, 10 Feb 2010 16:18:27 +0000\n" +
    		"MIME-Version: 1.0\n" +
    		"X-DSNContext: 335a7efd - 4523 - 00000001 - 80040546\n" +
    		"Message-ID: <FrMGaJe3s00001a1a@the-exch-bh.ai.pri>\n" +
    		"Subject: Delivery Status Notification (Failure)\n" +
    		"X-OriginalArrivalTime: 10 Feb 2010 16:18:27.0773 (UTC) FILETIME=[AD833ED0:01CAAA6C]\n" +
    		"Content-Type: multipart/report; report-type=delivery-status;\n" +
    		"    boundary=\"9B095B5ADSN=_01CA9AE238EA8D2400001AE0the?exch?bh.ai.p\"\n" +
    		"X-purgate-ID: expurgator32/1265818709-0000735A-7BA32A72/0-0/0-18\n" +
    		"X-purgate-size: 5390\n" +
    		"X-purgate-type: clean.bounce\n" +
    		"X-purgate-Ad: Categorized by eleven eXpurgate (R) http://www.eleven.de\n" +
    		"X-purgate: This mail is considered clean (visit http://www.eleven.de for further information)\n" +
    		"X-purgate: clean\n" +
    		"\n" +
    		"This is a MIME-formatted message.  \n" +
    		"Portions of this message may be unreadable without a MIME-capable mail program.\n" +
    		"\n" +
    		"--9B095B5ADSN=_01CA9AE238EA8D2400001AE0the?exch?bh.ai.p\n" +
    		"Content-Type: text/plain; charset=unicode-1-1-utf-7\n" +
    		"\n" +
    		"This is an automatically generated Delivery Status Notification.\n" +
    		"\n" +
    		"Delivery to the following recipients failed.\n" +
    		"\n" +
    		"       bill.wohlars@integralis.com\n" +
    		"\n" +
    		"\n" +
    		"\n" +
    		"\n" +
    		"--9B095B5ADSN=_01CA9AE238EA8D2400001AE0the?exch?bh.ai.p\n" +
    		"Content-Type: message/delivery-status\n" +
    		"\n" +
    		"Reporting-MTA: dns;the-exch-bh.ai.pri\n" +
    		"Received-From-MTA: dns;the-msw-scanner.integralis.com\n" +
    		"Arrival-Date: Wed, 10 Feb 2010 16:18:27 +0000\n" +
    		"\n" +
    		"Original-Recipient: rfc822;bill.wohlars@integralis.com\n" +
    		"Final-Recipient: rfc822;bill.wohlars@integralis.com\n" +
    		"Action: failed\n" +
    		"Status: 5.1.1\n" +
    		"\n" +
    		"--9B095B5ADSN=_01CA9AE238EA8D2400001AE0the?exch?bh.ai.p\n" +
    		"Content-Type: message/rfc822\n" +
    		"\n" +
    		"Received: from the-msw-scanner.integralis.com ([195.66.81.55]) by the-exch-bh.ai.pri with Microsoft SMTPSVC(6.0.3790.3959);\n" +
    		"     Wed, 10 Feb 2010 16:18:27 +0000\n" +
    		"Received: from keys.articon-integralis.com (unverified) by the-msw-scanner.integralis.com\n" +
    		" (Clearswift SMTPRS 5.2.5) with ESMTP id <T93c51686e7c34251371ba0@the-msw-scanner.integralis.com> for <bill.wohlars@integralis.com>;\n" +
    		" Wed, 10 Feb 2010 16:19:40 +0000\n" +
    		"Received: from ixe-mta-20.emailfiltering.com ([194.116.199.214])\n" +
    		"  by keys.articon-integralis.com (PGP Universal service);\n" +
    		"  Wed, 10 Feb 2010 16:18:27 +0000\n" +
    		"X-PGP-Universal: processed;\n" +
    		"    by keys.articon-integralis.com on Wed, 10 Feb 2010 16:18:27 +0000\n" +
    		"Received: from alcatraz.open-xchange.com ([217.6.212.138])\n" +
    		"    by ixe-mta-20.emailfiltering.com with emfmta (version 4.3.0.64.1.rd-3.2.3-libc2.3.2) vanilla id 23744690\n" +
    		"    for bill.wohlars@integralis.com; Wed, 10 Feb 2010 16:18:25 +0000\n" +
    		"Received: from localhost (localhost [127.0.0.1])\n" +
    		"    by mail.open-xchange.com (Postfix) with ESMTP id 5AC1728016\n" +
    		"    for <bill.wohlars@Integralis.Com>; Wed, 10 Feb 2010 17:18:24 +0100 (CET)\n" +
    		"Received: from mail.open-xchange.com ([127.0.0.1])\n" +
    		"    by localhost (mail.open-xchange.com [127.0.0.1]) (amavisd-new, port 10024)\n" +
    		"    with ESMTP id Kdqq7R9ZAgY6 for <bill.wohlars@Integralis.Com>;\n" +
    		"    Wed, 10 Feb 2010 17:18:24 +0100 (CET)\n" +
    		"Received: by mail.open-xchange.com (Postfix, from userid 1001)\n" +
    		"    id D17152801A; Wed, 10 Feb 2010 17:18:23 +0100 (CET)\n" +
    		"X-Spam-Checker-Version: SpamAssassin 3.2.3 (2007-08-08) on\n" +
    		"    mail.open-xchange.com\n" +
    		"X-Spam-Level: \n" +
    		"X-Spam-Status: No, score=-0.7 required=5.0 tests=AWL,BAYES_40,MISSING_MIMEOLE,\n" +
    		"    MSGID_MULTIPLE_AT,RDNS_NONE autolearn=no version=3.2.3\n" +
    		"Received: from ox.open-xchange.com (ox.open-xchange.com [10.20.30.100])\n" +
    		"    by mail.open-xchange.com (Postfix) with ESMTP id 7F0C72801A\n" +
    		"    for <bill.wohlars@Integralis.Com>; Wed, 10 Feb 2010 17:18:17 +0100 (CET)\n" +
    		"Received: from localhost (localhost.localdomain [127.0.0.1])\n" +
    		"    by ox.open-xchange.com (Postfix) with ESMTP id 8E3CB2AC4009\n" +
    		"    for <bill.wohlars@Integralis.Com>; Wed, 10 Feb 2010 17:18:17 +0100 (CET)\n" +
    		"Received: from ox.open-xchange.com ([127.0.0.1])\n" +
    		"    by localhost (ox.open-xchange.com [127.0.0.1]) (amavisd-new, port 10024)\n" +
    		"    with ESMTP id jMA81NK7+YQv for <bill.wohlars@Integralis.Com>;\n" +
    		"    Wed, 10 Feb 2010 17:18:17 +0100 (CET)\n" +
    		"Received: from gate4 (unknown [62.225.134.174])\n" +
    		"    by ox.open-xchange.com (Postfix) with ESMTP id 3FACB2AC4008\n" +
    		"    for <bill.wohlars@Integralis.Com>; Wed, 10 Feb 2010 17:18:17 +0100 (CET)\n" +
    		"From: \"Martin Kauss\" <martin.kauss@open-xchange.com>\n" +
    		"To: \"'Bill Wohlars'\" <bill.wohlars@Integralis.Com>\n" +
    		"Subject: =?Windows-1252?Q?Lesebest=E4tigung:_An_idea_addressing_your_increasing_co?=\n" +
    		"    =?Windows-1252?Q?st?=\n" +
    		"Date: Wed, 10 Feb 2010 17:18:04 +0100\n" +
    		"Message-ID: <000801caaa6c$a76403e0$f62c0ba0$@kauss@open-xchange.com>\n" +
    		"MIME-Version: 1.0\n" +
    		"X-Priority: 1 (Highest)\n" +
    		"X-MSMail-Priority: High\n" +
    		"X-Mailer: Microsoft Office Outlook 12.0\n" +
    		"X-MS-TNEF-Correlator: 00000000161985588D741D4085C9E6F5FFB923E5C4326100\n" +
    		"thread-index: AcmhwxbSK+hp2QcQSnC/nECdaY3GhEIp4t3S\n" +
    		"Importance: High\n" +
    		"Return-Path: martin.kauss@open-xchange.com\n" +
    		"X-OriginalArrivalTime: 10 Feb 2010 16:18:27.0340 (UTC) FILETIME=[AD412CC0:01CAAA6C]\n" +
    		"Content-Type: application/ms-tnef;\n" +
    		"    name=\"winmail.dat\"\n" +
    		"Content-Transfer-Encoding: base64\n" +
    		"Content-Disposition: attachment;\n" +
    		"    filename=\"winmail.dat\"\n" +
    		"\n" +
    		"eJ8+IhEQAQaQCAAEAAAAAAABAAEAAQeQBgAIAAAA5AQAAAAAAADoAAEIgAcAIAAAAElQTS5NaWNy\n" +
    		"b3NvZnQgTWFpbC5SZWFkIFJlY2VpcHQAAwsBCoABACEAAAAzQ0ZEQkNENjY5QjRBOTRCQTk1ODkx\n" +
    		"MjBEM0I4NEJBRQB0BwEDkAYAlAIAABgAAAALACkAAAAAAEAAMgBAHkaiaqrKAR4ASQABAAAAKAAA\n" +
    		"AEFuIGlkZWEgYWRkcmVzc2luZyB5b3VyIGluY3JlYXNpbmcgY29zdAACAUwAAQAAAHQAAAAAAAAA\n" +
    		"gSsfpL6jEBmdbgDdAQ9UAgAAAYBCAGkAbABsACAAVwBvAGgAbABhAHIAcwAAAFMATQBUAFAAAABi\n" +
    		"AGkAbABsAC4AdwBvAGgAbABhAHIAcwBAAEkAbgB0AGUAZwByAGEAbABpAHMALgBDAG8AbQAAAB4A\n" +
    		"TQABAAAADQAAAEJpbGwgV29obGFycwAAAABAAE4AgAfNFsOhyQFAAFUAAEGhi8WhyQEeAHAAAQAA\n" +
    		"ACgAAABBbiBpZGVhIGFkZHJlc3NpbmcgeW91ciBpbmNyZWFzaW5nIGNvc3QAAgFxAAEAAAAbAAAA\n" +
    		"AcmhwxbSK+hp2QcQSnC/nECdaY3GhEIp4t3SAB4AcgABAAAAAQAAAAAAAAAeAHMAAQAAAAEAAAAA\n" +
    		"AAAAHgB0AAEAAAAeAAAATWFydGluLmthdXNzQG9wZW4teGNoYW5nZS5jb20AAAALAAgMAAAAAAsA\n" +
    		"AQ4BAAAAAwAUDgEAAAAeAAEQAQAAABIAAAB3dXJkZSBnZWxlc2VuIGFtOgAAAAsAHw4BAAAAAgH4\n" +
    		"DwEAAAAQAAAAFhmFWI10HUCFyeb1/7kj5QIB+g8BAAAAEAAAABYZhViNdB1Ahcnm9f+5I+UDAP4P\n" +
    		"BQAAAAMADTT9P6UGAwAPNP0/pQYCARQ0AQAAABAAAABOSVRB+b+4AQCqADfZbgAAAgF/AAEAAAAx\n" +
    		"AAAAMDAwMDAwMDAxNjE5ODU1ODhENzQxRDQwODVDOUU2RjVGRkI5MjNFNUM0MzI2MTAwAAAAAIOS\n" +
    		"\n" +
    		"\n" +
    		"\n" +
    		"--9B095B5ADSN=_01CA9AE238EA8D2400001AE0the?exch?bh.ai.p--").getBytes();

    public void testMIMEStructure() {
        try {
            getSession();

            final MailMessage mail = MimeMessageConverter.convertMessage(SIMPLE);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            // System.out.println(jsonMailObject.toString(2));

            final JSONArray jsonBodyObject;
            {
                final Object bodyObject = jsonMailObject.opt("body");
                assertNotNull("Missing mail body.", bodyObject);

                assertTrue("Body object is not a JSON array.", (bodyObject instanceof JSONArray));
                jsonBodyObject = (JSONArray) bodyObject;
            }

            final JSONObject nestedMessage = jsonBodyObject.getJSONObject(2);

            final JSONObject headers = nestedMessage.getJSONObject("headers");
            final JSONObject ct = headers.getJSONObject("content-type");
            assertTrue("Should be a nested message, but isn't.", "message/rfc822".equalsIgnoreCase(ct.getString("type")));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}

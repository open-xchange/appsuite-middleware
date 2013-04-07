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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import org.json.JSONObject;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.structure.handler.MIMEStructureHandler;

/**
 * {@link SMIMEStructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SMIMEStructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link SMIMEStructureTest}.
     */
    public SMIMEStructureTest() {
        super();
    }

    /**
     * Initializes a new {@link SMIMEStructureTest}.
     *
     * @param name The test name
     */
    public SMIMEStructureTest(final String name) {
        super(name);
    }

    private static final byte[] SMIME = (
    		"From: Dennis Sieben <dennis.sieben@open-xchange.com>\n" +
    		"Organization: Open-Xchange\n" +
    		"To: \"Betten, Thorben\" <thorben.betten@open-xchange.com>\n" +
    		"Subject: Re: Abfrage von default Folder\n" +
    		"Date: Mon, 11 Jul 2011 13:08:51 +0200\n" +
    		"User-Agent: KMail/1.13.5 (Linux/2.6.32-32-generic; KDE/4.4.5; x86_64; ; )\n" +
    		"References: <677752318.207.1309941020013.JavaMail.open-xchange@ox6-unstable-1> <248313482.37.1310203730129.JavaMail.open-xchange@ox6-unstable-1>\n" +
    		"In-Reply-To: <248313482.37.1310203730129.JavaMail.open-xchange@ox6-unstable-1>\n" +
    		"X-Face: 1zUtkG'N~,{c(9p3%|?5Zd&Jn,\n" +
    		" =?iso-8859-1?q?b5ijT=5E9=5Ee=2Ev2Glyqw/bYd=24=3Ds4O*Wc9g=3Ay*!=7EdT4W6Fa=3B?=\n" +
    		" =?iso-8859-1?q?=0A=09=5Eh*?=@eeRo;tsT`TZ3iEbcO1Ju_d;jH34E+nVcj#RXVt~$'f4pPOe_c.CWIL]]m,\n" +
    		" =?iso-8859-1?q?hu-eV3=5F=25=0A=09o!=25?=\n" +
    		"MIME-Version: 1.0\n" +
    		"Content-Type: multipart/signed;\n" +
    		"  boundary=\"nextPart1984936.mcLEcBmFsP\";\n" +
    		"  protocol=\"application/pgp-signature\";\n" +
    		"  micalg=pgp-sha1\n" +
    		"Content-Transfer-Encoding: 7bit\n" +
    		"Message-Id: <201107111308.52222.dennis.sieben@open-xchange.com>\n" +
    		"\n" +
    		"--nextPart1984936.mcLEcBmFsP\n" +
    		"Content-Type: Text/Plain;\n" +
    		"  charset=\"iso-8859-1\"\n" +
    		"Content-Transfer-Encoding: quoted-printable\n" +
    		"\n" +
    		"Am Samstag, 9. Juli 2011, um 11:28:50 schrieb Betten, Thorben:\n" +
    		"> Hallo,\n" +
    		"> =20\n" +
    		"> bitte entschuldige die sp=E4te Antwort. Die Fehler liegt in der\n" +
    		"> Mailstore-MAL-Impl. Siehe angeh=E4ngten Patch.\n" +
    		"\n" +
    		"Danke.\n" +
    		"\n" +
    		"Das hei=DFt alle Methoden d=FCrfen auch mit dem default Folder aufgerufen w=\n" +
    		"erden?=20\n" +
    		"Ich glaube n=E4mlich das ist da noch nicht =FCberall so drin.\n" +
    		"\n" +
    		"Und wieso schaffe ich das auf Anhieb nicht den zu reproduzieren? Was muss d=\n" +
    		"enn=20\n" +
    		"passieren, damit der damit aufgerufen wird? Das scheint ja ein searchMessag=\n" +
    		"es=20\n" +
    		"call zu sein, aber wieso sucht jemand was im default Order?\n" +
    		"\n" +
    		"Gru=DF,\n" +
    		"\n" +
    		"    D7\n" +
    		"\n" +
    		"> \"Sieben, Dennis\" <dennis.sieben@open-xchange.com> hat am 6. Juli 2011 um\n" +
    		"> 10:30\n" +
    		">=20\n" +
    		"> geschrieben:\n" +
    		"> > Hallo Thorben,\n" +
    		"> > =20\n" +
    		"> > ist etwas bekannt das durch einen Fehler der Ordner default abgerufen\n" +
    		"> > wird. Der folgende Stack Trace tauch wohl hin und wieder bei 1und1 auf.\n" +
    		"> > Bevor ich mich auf die Suche mache ob das bei mir im Code irgendwie\n" +
    		"> > versteckt sein kann, wollte ich daher mal nachfragen.\n" +
    		"> > =20\n" +
    		"> > Gru=DF,\n" +
    		"> > =20\n" +
    		"> >     Dennis=20\n" +
    		"> > =20\n" +
    		"> > Jul  6 06:26:58 127.0.0.1 2011-07-06 06:26:58,386 open-xchange-groupware\n" +
    		"> > ERROR\n" +
    		"> > [com.openexchange.mail.oneandone.MailstoreMessageStorage][AJPListener-06\n" +
    		"> > 09217]:\n" +
    		"> > com.openexchange.mail.oneandone.MailstoreMessageStorage,getUnreadMessage\n" +
    		"> > sInFolder folder\n" +
    		"> >  not found: name=3D\"default\"\n" +
    		"> >  Jul  6 06:26:58 127.0.0.1\n" +
    		"> > de.schlund.nemesis.storeaccess0.UnknownFolderException: folder not foun=\n" +
    		"d:\n" +
    		"> > name=3D\"default\"\n" +
    		"> >  Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > de.schlund.nemesis.sgate0.MailboxSerializer.checkResponse(MailboxSerial=\n" +
    		"iz\n" +
    		"> > er.java:1302) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > de.schlund.nemesis.sgate0.MailboxSerializer.access$000(MailboxSerialize=\n" +
    		"r.\n" +
    		"> > java:15) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > de.schlund.nemesis.sgate0.MailboxSerializer$Task.unmarshalResponse(Mail=\n" +
    		"bo\n" +
    		"> > xSerializer.java:36) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > de.schlund.nemesis.rrp0.ConnectionImpl.readInput(ConnectionImpl.java:55=\n" +
    		"9)\n" +
    		"> >  Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > de.schlund.nemesis.rrp0.ConnectionImpl.perform(ConnectionImpl.java:159)\n" +
    		"> >  Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > de.schlund.nemesis.sgate0.GatewayManager$ConnectionImpl.perform(Gateway=\n" +
    		"Ma\n" +
    		"> > nager.java:354) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > de.schlund.nemesis.sgate0.GatewayManager.performTask(GatewayManager.jav=\n" +
    		"a:\n" +
    		"> > 544) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > de.schlund.nemesis.sgate0.MailboxSerializer.getFolderInfo(MailboxSerial=\n" +
    		"iz\n" +
    		"> > er.java:612) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > de.schlund.nemesis.sgate0.MailboxProcessorWrapper$MailboxImpl.getFolder=\n" +
    		"In\n" +
    		"> > fo(MailboxProcessorWrapper.java:144) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > sun.reflect.GeneratedMethodAccessor26.invoke(Unknown Source)\n" +
    		"> >  Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccesso=\n" +
    		"rI\n" +
    		"> > mpl.java:25) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > java.lang.reflect.Method.invoke(Method.java:597) Jul  6 06:26:58\n" +
    		"> > 127.0.0.1 at\n" +
    		"> > de.schlund.nemesis.storeaccess0.MailboxInterceptorFactory$LogIntercepto=\n" +
    		"r.\n" +
    		"> > invoke(MailboxInterceptorFactory.java:341) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > $Proxy65.getFolderInfo(Unknown Source) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > de.schlund.nemesis.storeaccess0.MailboxTaskFactory$13.perform(MailboxTa=\n" +
    		"sk\n" +
    		"> > Factory.java:167) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > de.schlund.nemesis.storeaccess0.MailboxTaskFactory$13.perform(MailboxTa=\n" +
    		"sk\n" +
    		"> > Factory.java:166) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > de.schlund.nemesis.sgate0.MailboxProcessorWrapper.process(MailboxProces=\n" +
    		"so\n" +
    		"> > rWrapper.java:376) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > de.schlund.nemesis.sgate0.MailboxAccessWrapper.getFolderInfo(MailboxAcc=\n" +
    		"es\n" +
    		"> > sWrapper.java:117) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.mail.oneandone.MailstoreMessageStorage.getUnreadMessag=\n" +
    		"es\n" +
    		"> > InFolder(MailstoreMessageStorage.java:857) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.mail.oneandone.MailstoreMessageStorage.searchMessages(=\n" +
    		"Ma\n" +
    		"> > ilstoreMessageStorage.java:517) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.folderstorage.mail.MailFolderImpl.getTotal(MailFolderI=\n" +
    		"mp\n" +
    		"> > l.java:428) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.folderstorage.internal.UserizedFolderImpl.getTotal(Use=\n" +
    		"ri\n" +
    		"> > zedFolderImpl.java:296) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.folder.json.writer.FolderWriter$18.writeField(FolderWr=\n" +
    		"it\n" +
    		"> > er.java:307) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.folder.json.writer.FolderWriter.writeSingle2Object(Fol=\n" +
    		"de\n" +
    		"> > rWriter.java:549) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.folder.json.actions.GetAction.perform(GetAction.java:1=\n" +
    		"14\n" +
    		"> > ) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.ajax.requesthandler.MultipleAdapter.performRequest(Mul=\n" +
    		"ti\n" +
    		"> > pleAdapter.java:107) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.ajax.Multiple.doAction(Multiple.java:214)\n" +
    		"> >  Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.ajax.Multiple.parseActionElement(Multiple.java:173)\n" +
    		"> >  Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.ajax.Multiple.doPut(Multiple.java:125)\n" +
    		"> >  Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > javax.servlet.http.HttpServlet.service(HttpServlet.java:619)\n" +
    		"> >  Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.ajax.AJAXServlet.service(AJAXServlet.java:413)\n" +
    		"> >  Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.ajax.SessionServlet.service(SessionServlet.java:219)\n" +
    		"> >  Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > javax.servlet.http.HttpServlet.service(HttpServlet.java:689)\n" +
    		"> >  Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.ajp13.najp.AJPv13RequestHandlerImpl.doServletService(A=\n" +
    		"JP\n" +
    		"> > v13RequestHandlerImpl.java:523) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.ajp13.AJPv13Request.response(AJPv13Request.java:134)\n" +
    		"> >  Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.ajp13.najp.AJPv13RequestHandlerImpl.createResponse(AJP=\n" +
    		"v1\n" +
    		"> > 3RequestHandlerImpl.java:335) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.ajp13.najp.AJPv13ConnectionImpl.createResponse(AJPv13C=\n" +
    		"on\n" +
    		"> > nectionImpl.java:227) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.ajp13.najp.AJPv13Task.call(AJPv13Task.java:365)\n" +
    		"> >  Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > java.util.concurrent.FutureTask$Sync.innerRun(FutureTask.java:303)\n" +
    		"> >  Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > java.util.concurrent.FutureTask.run(FutureTask.java:138)\n" +
    		"> >  Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.threadpool.internal.CustomThreadPoolExecutor$Worker.ru=\n" +
    		"nT\n" +
    		"> > ask(CustomThreadPoolExecutor.java:750) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > com.openexchange.threadpool.internal.CustomThreadPoolExecutor$Worker.ru=\n" +
    		"n(\n" +
    		"> > CustomThreadPoolExecutor.java:776) Jul  6 06:26:58 127.0.0.1 at\n" +
    		"> > java.lang.Thread.run(Thread.java:662)=20\n" +
    		"> > --\n" +
    		"> > Dennis Sieben\n" +
    		"> > Professional Services\n" +
    		"> > Open-Xchange GmbH\n" +
    		"> > Phone   +49 911 180 1400\n" +
    		"> > Fax     +49 911 180 1419\n" +
    		"> > -----------------------------------------------------------------------=\n" +
    		"=2D-\n" +
    		"> > ------ Open-Xchange AG,  Maxfeldstr. 9, 90409 N=FCrnberg, Amtsgericht\n" +
    		"> > N=FCrnberg HRB 24738\n" +
    		"> > Vorstand: Rafael Laguna de la Vera, Aufsichtsratsvorsitzender: Richard\n" +
    		"> > Seibt\n" +
    		"> >=20\n" +
    		"> > European Office: Open-Xchange GmbH, Martinstr. 41, D-57462 Olpe, Germany\n" +
    		"> > Amtsgericht Siegen, HRB 8718, Gesch=E4ftsf=FChrer: Frank Hoberg, Martin=\n" +
    		" Kauss\n" +
    		"> >=20\n" +
    		"> > US Office: Open-Xchange, Inc., 303 South Broadway, Tarrytown, New York\n" +
    		"> > 10591\n" +
    		"> > ------------------------------------------------------------------------\n" +
    		"> > -------\n" +
    		">=20\n" +
    		"> --\n" +
    		"> Thorben Betten\n" +
    		"> Engineering - Back-End-Team\n" +
    		"> Open-Xchange AG\n" +
    		">=20\n" +
    		"> Phone: +49 2761 8385-16, Fax: +49 2761 838530\n" +
    		">=20\n" +
    		"> -------------------------------------------------------------------------=\n" +
    		"=2D-\n" +
    		"> ---- Open-Xchange AG,  Maxfeldstr. 9, 90409 N=FCrnberg, Amtsgericht N=FCr=\n" +
    		"nberg\n" +
    		"> HRB 24738 Vorstand: Rafael Laguna de la Vera, Aufsichtsratsvorsitzender:\n" +
    		"> Richard Seibt\n" +
    		">=20\n" +
    		"> European Office: Open-Xchange GmbH, Martinstr. 41, D-57462 Olpe, Germany\n" +
    		"> Amtsgericht Siegen, HRB 8718, Gesch=E4ftsf=FChrer: Frank Hoberg, Martin K=\n" +
    		"auss\n" +
    		">=20\n" +
    		"> US Office: Open-Xchange, Inc., 303 South Broadway, Tarrytown, New York\n" +
    		"> 10591\n" +
    		"> --------------------------------------------------------------------------\n" +
    		"> -----\n" +
    		"\n" +
    		"\n" +
    		"=2D-=20\n" +
    		"Dennis Sieben\n" +
    		"Professional Services\n" +
    		"Open-Xchange GmbH\n" +
    		"Phone   +49 2761 8385 0\n" +
    		"=46ax     +49 911 180 1419\n" +
    		"=2D------------------------------------------------------------------------=\n" +
    		"=2D-----\n" +
    		"Open-Xchange AG,  Maxfeldstr. 9, 90409 N=FCrnberg, Amtsgericht N=FCrnberg H=\n" +
    		"RB=20\n" +
    		"24738\n" +
    		"Vorstand: Rafael Laguna de la Vera, Aufsichtsratsvorsitzender: Richard Seibt\n" +
    		"\n" +
    		"European Office: Open-Xchange GmbH, Martinstr. 41, D-57462 Olpe, Germany\n" +
    		"Amtsgericht Siegen, HRB 8718, Gesch=E4ftsf=FChrer: Frank Hoberg, Martin Kau=\n" +
    		"ss\n" +
    		"\n" +
    		"US Office: Open-Xchange, Inc., 303 South Broadway, Tarrytown, New York 10591\n" +
    		"=2D------------------------------------------------------------------------=\n" +
    		"=2D-----\n" +
    		"\n" +
    		"--nextPart1984936.mcLEcBmFsP\n" +
    		"Content-Type: application/pgp-signature; name=signature.asc \n" +
    		"Content-Description: This is a digitally signed message part.\n" +
    		"\n" +
    		"-----BEGIN PGP SIGNATURE-----\n" +
    		"Version: GnuPG v1.4.10 (GNU/Linux)\n" +
    		"\n" +
    		"iEYEABECAAYFAk4a2cQACgkQbxFTcj7mGIZwcgCgkaLypvYGEg5y2X5QqwE4VH1M\n" +
    		"cUMAn3wyZ32RdpW5jzRJv7zaIOpcT0Al\n" +
    		"=oXXY\n" +
    		"-----END PGP SIGNATURE-----\n" +
    		"\n" +
    		"--nextPart1984936.mcLEcBmFsP--\n").getBytes();

    public void testMIMEStructure() {
        try {
            getSession();

            final MailMessage mail = MimeMessageConverter.convertMessage(SMIME);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            // System.out.println(jsonMailObject.toString(2));

            assertTrue("Detected a body object, but shouldn't be there.", !jsonMailObject.hasAndNotNull("body"));

            assertTrue("Missing S/MIME body text.", jsonMailObject.hasAndNotNull("smime_body_text"));
            assertTrue("Missing S/MIME body data.", jsonMailObject.hasAndNotNull("smime_body_data"));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private static final byte[] SMIME2 = (
        "From: \"Jan Francsi\" <francsi@fh-luebeck.de>\n" +
        "To: <mainhardt@fh-luebeck.de>\n" +
        "Subject: Re: [OXTN#2011062910000191] OLOX 2 - smime Signierte Mails\n" +
        "Date: Thu, 30 Jun 2011 11:40:19 +0200\n" +
        "Message-ID: <000b01cc3709$ba120c00$2e362400$@de>\n" +
        "MIME-Version: 1.0\n" +
        "X-Mailer: Microsoft Office Outlook 12.0\n" +
        "Thread-Index: Acw3CbVDeI8cGLvTSKCDHRy/BEnkZA==\n" +
        "Content-Language: de\n" +
        "Content-Type: multipart/signed;\n" +
        "    protocol=\"application/x-pkcs7-signature\";\n" +
        "    micalg=MD5;\n" +
        "    boundary=\"----=_NextPart_000_0003_01CC371A.78F2B0B0\"\n" +
        "X-FHL-Server: mail\n" +
        "X-Scanned-By: MIMEDefang 2.64 on 193.175.120.25\n" +
        "X-FHL-Status: 1\n" +
        "X-PerlMx-Spam: Gauge=IIIIIIII, Probability=8%\n" +
        "\n" +
        "This is a multi-part message in MIME format.\n" +
        "\n" +
        "------=_NextPart_000_0003_01CC371A.78F2B0B0\n" +
        "Content-Type: multipart/related;\n" +
        "    boundary=\"----=_NextPart_001_0004_01CC371A.78F2B0B0\"\n" +
        "\n" +
        "\n" +
        "------=_NextPart_001_0004_01CC371A.78F2B0B0\n" +
        "Content-Type: multipart/alternative;\n" +
        "    boundary=\"----=_NextPart_002_0005_01CC371A.78F2D7C0\"\n" +
        "\n" +
        "\n" +
        "------=_NextPart_002_0005_01CC371A.78F2D7C0\n" +
        "Content-Type: text/plain;\n" +
        "    charset=\"UTF-8\"\n" +
        "Content-Transfer-Encoding: quoted-printable\n" +
        "\n" +
        "Hallo Dirk,\n" +
        "\n" +
        "=20\n" +
        "\n" +
        "das hier sollte eine Signierte Mail sein, die du weiter leiten kannst.\n" +
        "\n" +
        "=20\n" +
        "\n" +
        "Gru=C3=9F\n" +
        "\n" +
        "Jan\n" +
        "\n" +
        "=20\n" +
        "\n" +
        "-- =20\n" +
        "\n" +
        "Fachhochschule L=C3=BCbeck\n" +
        "\n" +
        "Jan Francsi B.Sc.\n" +
        "\n" +
        "-Rechenzentrum-\n" +
        "\n" +
        "M=C3=B6nkhofer Weg 239\n" +
        "\n" +
        "23562 L=C3=BCbeck\n" +
        "\n" +
        "Tel.: +49 (0) 451 300 5401\n" +
        "\n" +
        "E-Mail: francsi@fh-luebeck.de\n" +
        "\n" +
        "=20\n" +
        "\n" +
        "=20\n" +
        "\n" +
        "\"L=C3=BCbeck ist Stadt der Wissenschaft 2012\"\n" +
        "\n" +
        "fh-luebeck120px-email\n" +
        "\n" +
        "hl-stadt-wissenschaft-2012-email\n" +
        "\n" +
        "=20\n" +
        "\n" +
        "\n" +
        "------=_NextPart_002_0005_01CC371A.78F2D7C0\n" +
        "Content-Type: text/html;\n" +
        "    charset=\"UTF-8\"\n" +
        "Content-Transfer-Encoding: quoted-printable\n" +
        "\n" +
        "<html xmlns:v=3D\"urn:schemas-microsoft-com:vml\" =\n" +
        "xmlns:o=3D\"urn:schemas-microsoft-com:office:office\" =\n" +
        "xmlns:w=3D\"urn:schemas-microsoft-com:office:word\" =\n" +
        "xmlns:m=3D\"http://schemas.microsoft.com/office/2004/12/omml\" =\n" +
        "xmlns=3D\"http://www.w3.org/TR/REC-html40\"><head><meta =\n" +
        "http-equiv=3DContent-Type content=3D\"text/html; charset=3Dutf-8\"><meta =\n" +
        "name=3DGenerator content=3D\"Microsoft Word 12 (filtered medium)\"><!--[if =\n" +
        "!mso]><style>v\\:* {behavior:url(#default#VML);}\n" +
        "o\\:* {behavior:url(#default#VML);}\n" +
        "w\\:* {behavior:url(#default#VML);}\n" +
        ".shape {behavior:url(#default#VML);}\n" +
        "</style><![endif]--><style><!--\n" +
        "/* Font Definitions */\n" +
        "@font-face\n" +
        "    {font-family:\"Cambria Math\";\n" +
        "    panose-1:2 4 5 3 5 4 6 3 2 4;}\n" +
        "@font-face\n" +
        "    {font-family:Calibri;\n" +
        "    panose-1:2 15 5 2 2 2 4 3 2 4;}\n" +
        "@font-face\n" +
        "    {font-family:Tahoma;\n" +
        "    panose-1:2 11 6 4 3 5 4 4 2 4;}\n" +
        "/* Style Definitions */\n" +
        "p.MsoNormal, li.MsoNormal, div.MsoNormal\n" +
        "    {margin:0cm;\n" +
        "    margin-bottom:.0001pt;\n" +
        "    font-size:11.0pt;\n" +
        "    font-family:\"Calibri\",\"sans-serif\";}\n" +
        "a:link, span.MsoHyperlink\n" +
        "    {mso-style-priority:99;\n" +
        "    color:blue;\n" +
        "    text-decoration:underline;}\n" +
        "a:visited, span.MsoHyperlinkFollowed\n" +
        "    {mso-style-priority:99;\n" +
        "    color:purple;\n" +
        "    text-decoration:underline;}\n" +
        "p.MsoAcetate, li.MsoAcetate, div.MsoAcetate\n" +
        "    {mso-style-priority:99;\n" +
        "    mso-style-link:\"Sprechblasentext Zchn\";\n" +
        "    margin:0cm;\n" +
        "    margin-bottom:.0001pt;\n" +
        "    font-size:8.0pt;\n" +
        "    font-family:\"Tahoma\",\"sans-serif\";}\n" +
        "span.E-MailFormatvorlage17\n" +
        "    {mso-style-type:personal-compose;\n" +
        "    font-family:\"Calibri\",\"sans-serif\";\n" +
        "    color:windowtext;}\n" +
        "span.SprechblasentextZchn\n" +
        "    {mso-style-name:\"Sprechblasentext Zchn\";\n" +
        "    mso-style-priority:99;\n" +
        "    mso-style-link:Sprechblasentext;\n" +
        "    font-family:\"Tahoma\",\"sans-serif\";}\n" +
        ".MsoChpDefault\n" +
        "    {mso-style-type:export-only;}\n" +
        "@page WordSection1\n" +
        "    {size:612.0pt 792.0pt;\n" +
        "    margin:70.85pt 70.85pt 2.0cm 70.85pt;}\n" +
        "div.WordSection1\n" +
        "    {page:WordSection1;}\n" +
        "--></style><!--[if gte mso 9]><xml>\n" +
        "<o:shapedefaults v:ext=3D\"edit\" spidmax=3D\"2050\" />\n" +
        "</xml><![endif]--><!--[if gte mso 9]><xml>\n" +
        "<o:shapelayout v:ext=3D\"edit\">\n" +
        "<o:idmap v:ext=3D\"edit\" data=3D\"1\" />\n" +
        "</o:shapelayout></xml><![endif]--></head><body lang=3DDE link=3Dblue =\n" +
        "vlink=3Dpurple><div class=3DWordSection1><p class=3DMsoNormal>Hallo =\n" +
        "Dirk,<o:p></o:p></p><p class=3DMsoNormal><o:p>&nbsp;</o:p></p><p =\n" +
        "class=3DMsoNormal>das hier sollte eine Signierte Mail sein, die du =\n" +
        "weiter leiten kannst.<o:p></o:p></p><p =\n" +
        "class=3DMsoNormal><o:p>&nbsp;</o:p></p><p =\n" +
        "class=3DMsoNormal>Gru=C3=9F<o:p></o:p></p><p =\n" +
        "class=3DMsoNormal>Jan<o:p></o:p></p><p =\n" +
        "class=3DMsoNormal><o:p>&nbsp;</o:p></p><p class=3DMsoNormal>--=C2=A0 =\n" +
        "<o:p></o:p></p><p class=3DMsoNormal>Fachhochschule =\n" +
        "L=C3=BCbeck<o:p></o:p></p><p class=3DMsoNormal>Jan Francsi =\n" +
        "B.Sc.<o:p></o:p></p><p =\n" +
        "class=3DMsoNormal>-Rechenzentrum-<o:p></o:p></p><p =\n" +
        "class=3DMsoNormal>M=C3=B6nkhofer Weg 239<o:p></o:p></p><p =\n" +
        "class=3DMsoNormal>23562 L=C3=BCbeck<o:p></o:p></p><p =\n" +
        "class=3DMsoNormal>Tel.: +49 (0) 451 300 5401<o:p></o:p></p><p =\n" +
        "class=3DMsoNormal>E-Mail: francsi@fh-luebeck.de<o:p></o:p></p><p =\n" +
        "class=3DMsoNormal><o:p>&nbsp;</o:p></p><p =\n" +
        "class=3DMsoNormal><o:p>&nbsp;</o:p></p><p =\n" +
        "class=3DMsoNormal>&quot;L=C3=BCbeck ist Stadt der Wissenschaft =\n" +
        "2012&quot;<o:p></o:p></p><p class=3DMsoNormal><img width=3D120 =\n" +
        "height=3D83 id=3D\"Bild_x0020_1\" =\n" +
        "src=3D\"cid:image001.jpg@01CC371A.78CBEED0\" =\n" +
        "alt=3Dfh-luebeck120px-email><o:p></o:p></p><p class=3DMsoNormal><img =\n" +
        "width=3D124 height=3D87 id=3D\"Bild_x0020_2\" =\n" +
        "src=3D\"cid:image002.jpg@01CC371A.78CBEED0\" =\n" +
        "alt=3Dhl-stadt-wissenschaft-2012-email><o:p></o:p></p><p =\n" +
        "class=3DMsoNormal><o:p>&nbsp;</o:p></p></div></body></html>\n" +
        "------=_NextPart_002_0005_01CC371A.78F2D7C0--\n" +
        "\n" +
        "------=_NextPart_001_0004_01CC371A.78F2B0B0\n" +
        "Content-Type: image/jpeg;\n" +
        "    name=\"image001.jpg\"\n" +
        "Content-Transfer-Encoding: base64\n" +
        "Content-ID: <image001.jpg@01CC371A.78CBEED0>\n" +
        "\n" +
        "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\n" +
        "heCN+hA0B//Z\n" +
        "\n" +
        "------=_NextPart_001_0004_01CC371A.78F2B0B0\n" +
        "Content-Type: image/jpeg;\n" +
        "    name=\"image002.jpg\"\n" +
        "Content-Transfer-Encoding: base64\n" +
        "Content-ID: <image002.jpg@01CC371A.78CBEED0>\n" +
        "\n" +
        "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\n" +
        "\n" +
        "------=_NextPart_001_0004_01CC371A.78F2B0B0--\n" +
        "\n" +
        "------=_NextPart_000_0003_01CC371A.78F2B0B0\n" +
        "Content-Type: application/x-pkcs7-signature;\n" +
        "    name=\"smime.p7s\"\n" +
        "Content-Transfer-Encoding: base64\n" +
        "Content-Disposition: attachment;\n" +
        "    filename=\"smime.p7s\"\n" +
        "\n" +
        "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\n" +
        "ZS9jZ2ktYmluL3NlcnZpY2UvYWZfRG93bmxvYWRBUkwuY3JsPy1jcmxfZm9ybWF0PVhfNTA5Ji1p\n" +
        "c3N1ZXI9RFRfUk9PVF9DQV8yMB0GA1UdDgQWBBRJt8bP6D0ff+pEexMp9/EKcD7eZDAfBgNVHSME\n" +
        "GDAWgBQxw3kbuvVT1xfgiXotF2wKsyudMzAOBgNVHQ8BAf8EBAMCAQYwEgYDVR0TAQH/BAgwBgEB\n" +
        "/wIBAjANBgkqhkiG9w0BAQUFAAOCAQEAO+Fad8BIF9ypGOyBr1qJ8L0okqbKWRgScOwo8ueuf5Ys\n" +
        "5/JdGTH2Eyt0vb2Asrn3Z8k5onk74RER7mt4kTN+O18mJ3VTZY4zY+7Pc8OwkiNJIVB1I6EfGOKU\n" +
        "hT0/M+l3II2iveahhSlA9j9zMlgNCWum2oVswD+7jWZkViROrg0/MjUBW+mMgtlyWU+xhoXxdIVW\n" +
        "5cP4XPON7kezUwVw5+VNimmDKOETCYaeXsjqWB4MH/mk1FoEaP0oPosCtli19qEsN1cAZ6sjaI1j\n" +
        "pe+Za1z9S1b2q0CHNNQRkmzsh8UKCwczcrRvDB1ULNhRx8y/MNNDcvEyv4zOSWOoAPfyHDCCBU0w\n" +
        "ggQ1oAMCAQICBA+HryswDQYJKoZIhvcNAQEFBQAwWjELMAkGA1UEBhMCREUxEzARBgNVBAoTCkRG\n" +
        "Ti1WZXJlaW4xEDAOBgNVBAsTB0RGTi1QS0kxJDAiBgNVBAMTG0RGTi1WZXJlaW4gUENBIEdsb2Jh\n" +
        "bCAtIEcwMTAeFw0xMDAyMDIxNDA3MjNaFw0xOTA2MzAwMDAwMDBaMIHAMQswCQYDVQQGEwJERTEb\n" +
        "MBkGA1UECBMSU2NobGVzd2lnLUhvbHN0ZWluMRAwDgYDVQQHEwdMdWViZWNrMR8wHQYDVQQKExZG\n" +
        "YWNoaG9jaHNjaHVsZSBMdWViZWNrMRYwFAYDVQQLEw1SZWNoZW56ZW50cnVtMSgwJgYDVQQDEx9G\n" +
        "YWNoaG9jaHNjaHVsZSBMdWViZWNrIENBIC0gRzAxMR8wHQYJKoZIhvcNAQkBFhBjYUBmaC1sdWVi\n" +
        "ZWNrLmRlMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsGpgzmAytIkm2+NVrlLG0N0X\n" +
        "laQ/Ch8szj80DbcsiyBPDuiEHzqbZj8RLeuxjC5yhQe4IYAWFCDho9xtC+Rh6JKW9cNMCuluRaIg\n" +
        "I+K1wzN3M4znr7Vb/WuODuro3Cb98h7FF+v9yNhjzUOiOZJ0eBMea5fbH0i50YP15xE5LuJbFbdG\n" +
        "8JJKe+6+zD5f1j6GM/xtAUVxguOkB5ENez0OWb0ezwZDnshTJ7tN/M9SdBVqBJSebk3wczypSp/T\n" +
        "of7T5V6yaKSX70Xc8cJ9ZhWPk0+ODTMZsaGapAyxZRxX6huZheHkgD0h9YU4q7d1wJKWLe3gTRIn\n" +
        "Fw8Xm9b9yHB6pQIDAQABo4IBsjCCAa4wEgYDVR0TAQH/BAgwBgEB/wIBATALBgNVHQ8EBAMCAQYw\n" +
        "HQYDVR0OBBYEFD219Qhl7L1+ENrtlvOw+a9Jt1dTMB8GA1UdIwQYMBaAFEm3xs/oPR9/6kR7Eyn3\n" +
        "8QpwPt5kMBsGA1UdEQQUMBKBEGNhQGZoLWx1ZWJlY2suZGUwgYgGA1UdHwSBgDB+MD2gO6A5hjdo\n" +
        "dHRwOi8vY2RwMS5wY2EuZGZuLmRlL2dsb2JhbC1yb290LWNhL3B1Yi9jcmwvY2FjcmwuY3JsMD2g\n" +
        "O6A5hjdodHRwOi8vY2RwMi5wY2EuZGZuLmRlL2dsb2JhbC1yb290LWNhL3B1Yi9jcmwvY2Fjcmwu\n" +
        "Y3JsMIGiBggrBgEFBQcBAQSBlTCBkjBHBggrBgEFBQcwAoY7aHR0cDovL2NkcDEucGNhLmRmbi5k\n" +
        "ZS9nbG9iYWwtcm9vdC1jYS9wdWIvY2FjZXJ0L2NhY2VydC5jcnQwRwYIKwYBBQUHMAKGO2h0dHA6\n" +
        "Ly9jZHAyLnBjYS5kZm4uZGUvZ2xvYmFsLXJvb3QtY2EvcHViL2NhY2VydC9jYWNlcnQuY3J0MA0G\n" +
        "CSqGSIb3DQEBBQUAA4IBAQCYpZ/Y6yvJM9CtMr1yBU9xcTcR4v/z8Slo82Ihm5AwG9GrDshBHbKr\n" +
        "ltyolwDrgx0rov6WouAq9+tVJXfkWbHwpUbhjqM7rNT6J9e3tL0LiEEpInEjDmqkYgEtB84qhp/u\n" +
        "iFrUcrKxIptTxEOfQ/a2BnxO2Q/Pebje5KOr7b8hrwWRS255hb7MlFSzVEPZYOxHhGAkUIAWdgks\n" +
        "uMj8unRA6hMR+vVyLIzN0ORgRCraofs7rA79TQJO9CWR2Dq6pyJExXhFCkiGjtfnF/IFrwG/cM8k\n" +
        "mNGNzgptdDUtjI3yBnWDkIjk/IScdztJ9MrHCNGwMH0pZjLLEQR1OBhTbG+nMIIFWTCCBEGgAwIB\n" +
        "AgIEESblzDANBgkqhkiG9w0BAQUFADCBwDELMAkGA1UEBhMCREUxGzAZBgNVBAgTElNjaGxlc3dp\n" +
        "Zy1Ib2xzdGVpbjEQMA4GA1UEBxMHTHVlYmVjazEfMB0GA1UEChMWRmFjaGhvY2hzY2h1bGUgTHVl\n" +
        "YmVjazEWMBQGA1UECxMNUmVjaGVuemVudHJ1bTEoMCYGA1UEAxMfRmFjaGhvY2hzY2h1bGUgTHVl\n" +
        "YmVjayBDQSAtIEcwMTEfMB0GCSqGSIb3DQEJARYQY2FAZmgtbHVlYmVjay5kZTAeFw0xMDEyMTQx\n" +
        "MjUxMDlaFw0xMzEyMTMxMjUxMDlaMEQxCzAJBgNVBAYTAkRFMR8wHQYDVQQKExZGYWNoaG9jaHNj\n" +
        "aHVsZSBMdWViZWNrMRQwEgYDVQQDEwtKYW4gRnJhbmNzaTCCASIwDQYJKoZIhvcNAQEBBQADggEP\n" +
        "ADCCAQoCggEBAKOm+t+TfTT4yqKWCuWv4FUx+COyNntFWY65Y4mqWLINbfTAvlPot2ICJHNuoYxt\n" +
        "foKwA1xBvahvQor4mx0zlSg21hw6eyE7AcTTWHP1GENVAZmC7konWTUseZ5jBtaTswEgvwRnybka\n" +
        "/nRn97G86CZ+ifIufG2Dl0B3hAfxOe5oJjSZI0h78r7wvmyaiWka3IG2V8cjqirD9iWMyLr17fzu\n" +
        "9ocnntMhWflw+fAFfBqMa00Gt5GcnkUnttkHUnPBSVuOoDcyLugI4RkX+R1Vgws/4xZSXbZJVLYH\n" +
        "7cm5HCWhV8sWOsYEgj1q2C/QP+NFwEPqp1g9N25E7NBhmbkbb0UCAwEAAaOCAdQwggHQMAkGA1Ud\n" +
        "EwQCMAAwCwYDVR0PBAQDAgXgMCkGA1UdJQQiMCAGCCsGAQUFBwMCBggrBgEFBQcDBAYKKwYBBAGC\n" +
        "NxQCAjAdBgNVHQ4EFgQUBcZmmv0FynJup+qf2PzrNQtoUTEwHwYDVR0jBBgwFoAUPbX1CGXsvX4Q\n" +
        "2u2W87D5r0m3V1MwIAYDVR0RBBkwF4EVZnJhbmNzaUBmaC1sdWViZWNrLmRlMIGFBgNVHR8EfjB8\n" +
        "MDygOqA4hjZodHRwOi8vY2RwMS5wY2EuZGZuLmRlL2ZoLWx1ZWJlY2stY2EvcHViL2NybC9jYWNy\n" +
        "bC5jcmwwPKA6oDiGNmh0dHA6Ly9jZHAyLnBjYS5kZm4uZGUvZmgtbHVlYmVjay1jYS9wdWIvY3Js\n" +
        "L2NhY3JsLmNybDCBoAYIKwYBBQUHAQEEgZMwgZAwRgYIKwYBBQUHMAKGOmh0dHA6Ly9jZHAxLnBj\n" +
        "YS5kZm4uZGUvZmgtbHVlYmVjay1jYS9wdWIvY2FjZXJ0L2NhY2VydC5jcnQwRgYIKwYBBQUHMAKG\n" +
        "Omh0dHA6Ly9jZHAyLnBjYS5kZm4uZGUvZmgtbHVlYmVjay1jYS9wdWIvY2FjZXJ0L2NhY2VydC5j\n" +
        "cnQwDQYJKoZIhvcNAQEFBQADggEBAAalh2esi2ki6bFZ9greL2aKK41Zr5nJHcJXb17MlsxZqSWU\n" +
        "Inzr5m/YI8DkypE7qtTa016oW7sBstnVZIBd6vzFI5jcRO459R0IEckPoAUn4/CwxRJML9zdj4rt\n" +
        "+/ZvDLqKZrPMy1+zcN3dM/7so4hQatKryZBVLJlnp2z2kJWRvflljaU1yv5Si0Ya93d6uakHm+4E\n" +
        "zozGr3/jEFg8S/ujZ+D3NUST1V8N0EiXQBWvRzCvSm66Km7YoLVDC7leJUxi5sU+yD8WSBvC882J\n" +
        "YSoo3Gp+z+P/c/4/MwqqxZ/u6US5g5qayDwAxh0XpwBHBvMa2xs/5N35MVr9Vo6f0RExggRuMIIE\n" +
        "agIBATCByTCBwDELMAkGA1UEBhMCREUxGzAZBgNVBAgTElNjaGxlc3dpZy1Ib2xzdGVpbjEQMA4G\n" +
        "A1UEBxMHTHVlYmVjazEfMB0GA1UEChMWRmFjaGhvY2hzY2h1bGUgTHVlYmVjazEWMBQGA1UECxMN\n" +
        "UmVjaGVuemVudHJ1bTEoMCYGA1UEAxMfRmFjaGhvY2hzY2h1bGUgTHVlYmVjayBDQSAtIEcwMTEf\n" +
        "MB0GCSqGSIb3DQEJARYQY2FAZmgtbHVlYmVjay5kZQIEESblzDAMBggqhkiG9w0CBQUAoIICdjAY\n" +
        "BgkqhkiG9w0BCQMxCwYJKoZIhvcNAQcBMBwGCSqGSIb3DQEJBTEPFw0xMTA2MzAwOTQwMTJaMB8G\n" +
        "CSqGSIb3DQEJBDESBBDLMQg6xTmTjQl7vaLKYuHUMF8GCSqGSIb3DQEJDzFSMFAwDgYIKoZIhvcN\n" +
        "AwICAgCAMAsGCWCGSAFlAgEBBDANBggqhkiG9w0DAgIBQDAHBgUrDgMCBzANBggqhkiG9w0DAgIB\n" +
        "KDAKBggqhkiG9w0CBTCB2gYJKwYBBAGCNxAEMYHMMIHJMIHAMQswCQYDVQQGEwJERTEbMBkGA1UE\n" +
        "CBMSU2NobGVzd2lnLUhvbHN0ZWluMRAwDgYDVQQHEwdMdWViZWNrMR8wHQYDVQQKExZGYWNoaG9j\n" +
        "aHNjaHVsZSBMdWViZWNrMRYwFAYDVQQLEw1SZWNoZW56ZW50cnVtMSgwJgYDVQQDEx9GYWNoaG9j\n" +
        "aHNjaHVsZSBMdWViZWNrIENBIC0gRzAxMR8wHQYJKoZIhvcNAQkBFhBjYUBmaC1sdWViZWNrLmRl\n" +
        "AgQRJuXMMIHcBgsqhkiG9w0BCRACCzGBzKCByTCBwDELMAkGA1UEBhMCREUxGzAZBgNVBAgTElNj\n" +
        "aGxlc3dpZy1Ib2xzdGVpbjEQMA4GA1UEBxMHTHVlYmVjazEfMB0GA1UEChMWRmFjaGhvY2hzY2h1\n" +
        "bGUgTHVlYmVjazEWMBQGA1UECxMNUmVjaGVuemVudHJ1bTEoMCYGA1UEAxMfRmFjaGhvY2hzY2h1\n" +
        "bGUgTHVlYmVjayBDQSAtIEcwMTEfMB0GCSqGSIb3DQEJARYQY2FAZmgtbHVlYmVjay5kZQIEESbl\n" +
        "zDANBgkqhkiG9w0BAQEFAASCAQBtdITV/at8/hS9UIwb9s7P+cpusHref1WvhyIWNTOwLBJJHz8n\n" +
        "jKbB1tOBsnxWTRtp2xH4SdlUm5OCMMpJWu5Eymg0LaVmQ6y56/QjprryYSL4FVRYIKG4hFLSOQUc\n" +
        "ZSTQ9+eshSl+TUgkx7nJUvetfmGFT94MAXjcNHhcae8rbv+4FxC97+BV2iigP9sUhjX3b7WLKFIM\n" +
        "+143qlXSfnnnWFAgjCePzaRpl6nzl/WUqBNWldF9POxRqhTnuze1Kt06AyfqxK2Z8fX8FUj8h5Tj\n" +
        "sYOMGi8yNP9Ik5fjuUeLWSoVPnor3rE5YW68xyY79VLOb2pLIqMsF7x0XXkGVn31AAAAAAAA\n" +
        "\n" +
        "------=_NextPart_000_0003_01CC371A.78F2B0B0--\n" +
        "\n").getBytes();

    public void testAnotherMIMEStructure() {
        try {
            getSession();

            final MailMessage mail = MimeMessageConverter.convertMessage(SMIME2);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            // System.out.println(jsonMailObject.toString(2));

            assertTrue("Detected a body object, but shouldn't be there.", !jsonMailObject.hasAndNotNull("body"));

            assertTrue("Missing S/MIME body text.", jsonMailObject.hasAndNotNull("smime_body_text"));
            assertTrue("Missing S/MIME body data.", jsonMailObject.hasAndNotNull("smime_body_data"));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}

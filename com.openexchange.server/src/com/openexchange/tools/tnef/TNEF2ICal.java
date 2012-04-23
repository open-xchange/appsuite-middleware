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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.tools.tnef;

import static com.openexchange.tools.tnef.TNEF2ICalUtility.findNamedProp;
import static com.openexchange.tools.tnef.TNEF2ICalUtility.findNamedPropString;
import static com.openexchange.tools.tnef.TNEF2ICalUtility.findProp;
import static com.openexchange.tools.tnef.TNEF2ICalUtility.findPropString;
import static com.openexchange.tools.tnef.TNEF2ICalUtility.getEmailAddress;
import static com.openexchange.tools.tnef.TNEF2ICalUtility.isEmpty;
import static com.openexchange.tools.tnef.TNEF2ICalUtility.toDateTime;
import static com.openexchange.tools.tnef.TNEF2ICalUtility.toHexString;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Locale;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStamp;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Trigger;
import net.fortuna.ical4j.model.property.Uid;
import net.freeutils.tnef.Attr;
import net.freeutils.tnef.MAPIProp;
import net.freeutils.tnef.MAPIProps;
import net.freeutils.tnef.RawInputStream;
import net.freeutils.tnef.TNEFInputStream;
import com.openexchange.groupware.i18n.Notifications;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.mime.MimeDefaultSession;
import com.openexchange.tools.stream.UnsynchronizedByteArrayInputStream;

/**
 * {@link TNEF2ICal}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TNEF2ICal {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(TNEF2ICal.class));

    /**
     * Initializes a new {@link TNEF2ICal}.
     */
    private TNEF2ICal() {
        super();
    }

    public static void main(final String[] args) {
        try {
            final String mailSrc =
                "Content-Type: multipart/mixed;\n" +
                "    boundary=\"_000_28EB37E571CBA8449A1C8CD297726A5D01A3701A8BEXCHANGE04web_\"\n" +
                "From: Martin Schmidt <martin.schmidt@1und1.de>\n" +
                "To: \"holger.achtziger@open-xchange.com\" <holger.achtziger@open-xchange.com>\n" +
                "Date: Mon, 17 Jan 2011 15:23:34 +0100\n" +
                "Subject: Zugesagt: Termin - Zugriff auf 1&1 Kontakt- und\n" +
                " Kalenderdatenservices\n" +
                "Thread-Topic: Termin - Zugriff auf 1&1 Kontakt- und Kalenderdatenservices\n" +
                "Thread-Index: Acu2Uh0jgjhiOu5yT2Wm7GW2pEjh3gAAAK/A\n" +
                "Message-ID: <28EB37E571CBA8449A1C8CD297726A5D01A3701A8B@EXCHANGE04.webde.local>\n" +
                "Accept-Language: de-DE\n" + "Content-Language: de-DE\n" + "X-MS-Has-Attach: \n" +
                "X-MS-TNEF-Correlator: <28EB37E571CBA8449A1C8CD297726A5D01A3701A8B@EXCHANGE04.webde.local>\n" +
                "acceptlanguage: de-DE\n" + "MIME-Version: 1.0\n" + "X-Virus-Scanned: Symantec AntiVirus Scan Engine\n" +
                "X-UI-Msg-Verification: fbe98d9fa285fe4a1cde9af100d32d01\n" + "X-purgate-ID: 151428::1295274220-00006D31-66C4EB4B/0-0/0-0\n" +
                "X-purgate-type: clean\n" + "X-purgate-size: 9516\n" + "X-purgate-Ad: Categorized by eleven eXpurgate (R) http://www.eleven.de\n" +
                "X-purgate: This mail is considered clean (visit http://www.eleven.de for further information)\n" +
                "X-purgate: clean\n" +
                "\n" +
                "--_000_28EB37E571CBA8449A1C8CD297726A5D01A3701A8BEXCHANGE04web_\n" +
                "Content-Type: text/plain; charset=\"utf-8\"\n" + "Content-Transfer-Encoding: base64\n" +
                "\n" +
                "\n" +
                "--_000_28EB37E571CBA8449A1C8CD297726A5D01A3701A8BEXCHANGE04web_\n" +
                "Content-Disposition: attachment; filename=\"winmail.dat\"\n" +
                "Content-Transfer-Encoding: base64\n" +
                "Content-Type: application/ms-tnef; name=\"winmail.dat\"\n" +
                "\n" +
                "eJ8+ImBdAQaQCAAEAAAAAAABAAEAAQeQBgAIAAAA5AQAAAAAAADoAAEJgAEAIQAAADQxNzg1QjI3\n" +
                "QThGRkZGNDFCOEUxOTgyQzExNkRCMDVGAEkHAQ2ABAACAAAAAgACAAEFgAMADgAAANsHAQARAA4A\n" +
                "FwAiAAEAPAEBIIADAA4AAADbBwEAEQAOABcAIwABAD0BAQiABwAgAAAASVBNLk1pY3Jvc29mdCBT\n" +
                "Y2hlZHVsZS5NdGdSZXNwUABXCwEEgAEARgAAAFp1Z2VzYWd0OiBUZXJtaW4gLSBadWdyaWZmIGF1\n" +
                "ZiAxJjEgS29udGFrdC0gdW5kIEthbGVuZGVyZGF0ZW5zZXJ2aWNlcwCnGAEGAAMADgAAANsHAQAV\n" +
                "AAgAHgAAAAUAIwEBBwADAA4AAADbBwEAFQAJAB4AAAAFACQBAQgABQAEAAAA//////wDAQOQBgCU\n" +
                "GAAAlwAAAAIBfwABAAAARAAAADwyOEVCMzdFNTcxQ0JBODQ0OUExQzhDRDI5NzcyNkE1RDAxQTM3\n" +
                "MDFBOEJARVhDSEFOR0UwNC53ZWJkZS5sb2NhbD4AHwBCAAEAAAAeAAAATQBhAHIAdABpAG4AIABT\n" +
                "AGMAaABtAGkAZAB0AAAAAAAfAGUAAQAAADAAAABtAGEAcgB0AGkAbgAuAHMAYwBoAG0AaQBkAHQA\n" +
                "QAAxAHUAbgBkADEALgBkAGUAAAAfAGQAAQAAAAoAAABTAE0AVABQAAAAAAACAUEAAQAAAHAAAAAA\n" +
                "AAAAgSsfpL6jEBmdbgDdAQ9UAgAAAIBNAGEAcgB0AGkAbgAgAFMAYwBoAG0AaQBkAHQAAABTAE0A\n" +
                "VABQAAAAbQBhAHIAdABpAG4ALgBzAGMAaABtAGkAZAB0AEAAMQB1AG4AZAAxAC4AZABlAAAAHwAa\n" +
                "DAEAAAAeAAAATQBhAHIAdABpAG4AIABTAGMAaABtAGkAZAB0AAAAAAAfAB8MAQAAADAAAABtAGEA\n" +
                "cgB0AGkAbgAuAHMAYwBoAG0AaQBkAHQAQAAxAHUAbgBkADEALgBkAGUAAAAfAB4MAQAAAAoAAABT\n" +
                "AE0AVABQAAAAAAACARkMAQAAAHAAAAAAAAAAgSsfpL6jEBmdbgDdAQ9UAgAAAIBNAGEAcgB0AGkA\n" +
                "bgAgAFMAYwBoAG0AaQBkAHQAAABTAE0AVABQAAAAbQBhAHIAdABpAG4ALgBzAGMAaABtAGkAZAB0\n" +
                "AEAAMQB1AG4AZAAxAC4AZABlAAAACwBAOgEAAAAfABoAAQAAADwAAABJAFAATQAuAFMAYwBoAGUA\n" +
                "ZAB1AGwAZQAuAE0AZQBlAHQAaQBuAGcALgBSAGUAcwBwAC4AUABvAHMAAAADAPE/BwQAAAsAQDoB\n" +
                "AAAAAwD9P+QEAAACAQswAQAAABAAAABBeFsnqP//QbjhmCwRbbBfAwAXAAEAAABAADkAALddH1K2\n" +
                "ywFAAAgwmGSEIFK2ywELAAIAAQAAAAsAIwAAAAAAAwAmAAAAAAALACkAAAAAAAsAKwAAAAAAAwA2\n" +
                "AAAAAAACAUcAAQAAADIAAABjPURFO2E9IDtwPVdFQkRFO2w9RVhDSEFOR0UwNC0xMTAxMTcxNDIz\n" +
                "MzVaLTUzMDMxAAAAQABgAAA0fWRFucsBQABhAACcQcZNucsBAwBiAP////8fAHAAAQAAAHgAAABU\n" +
                "AGUAcgBtAGkAbgAgAC0AIABaAHUAZwByAGkAZgBmACAAYQB1AGYAIAAxACYAMQAgAEsAbwBuAHQA\n" +
                "YQBrAHQALQAgAHUAbgBkACAASwBhAGwAZQBuAGQAZQByAGQAYQB0AGUAbgBzAGUAcgB2AGkAYwBl\n" +
                "AHMAAAACAXEAAQAAABsAAAABy7ZSHSOCOGI67nJPZabsZbakSOHeAAAAr8AAHwA1EAEAAACIAAAA\n" +
                "PAAyADgARQBCADMANwBFADUANwAxAEMAQgBBADgANAA0ADkAQQAxAEMAOABDAEQAMgA5ADcANwAy\n" +
                "ADYAQQA1AEQAMAAxAEEAMwA3ADAAMQBBADgAQgBAAEUAWABDAEgAQQBOAEcARQAwADQALgB3AGUA\n" +
                "YgBkAGUALgBsAG8AYwBhAGwAPgAAAAMAgBD/////QADDEAA0fWRFucsBQADEEACcQcZNucsBHwDz\n" +
                "EAEAAACcAAAAWgB1AGcAZQBzAGEAZwB0ACUAMwBBACAAVABlAHIAbQBpAG4AIAAtACAAWgB1AGcA\n" +
                "cgBpAGYAZgAgAGEAdQBmACAAMQAlADIANgAxACAASwBvAG4AdABhAGsAdAAtACAAdQBuAGQAIABL\n" +
                "AGEAbABlAG4AZABlAHIAZABhAHQAZQBuAHMAZQByAHYAaQBjAGUAcwAuAEUATQBMAAAACwD0EAAA\n" +
                "AAALAPUQAAAAAAsA9hAAAAAAQAAHMJhkhCBStssBAgEQMAEAAABGAAAAAAAAAORXA+Nf7ppDg4ry\n" +
                "h0rzdUAHAGIp167nXb9GmyuFhonKwPgAAAORE/oAADEHeDKvdqpNoAd6HZW0i80AAOAzUE8AAAAA\n" +
                "AwDeP+QEAAAfAPg/AQAAAB4AAABNAGEAcgB0AGkAbgAgAFMAYwBoAG0AaQBkAHQAAAAAAAIB+T8B\n" +
                "AAAAawAAAAAAAADcp0DIwEIQGrS5CAArL+GCAQAAAAAAAAAvTz1XRUJERS9PVT1FUlNURSBBRE1J\n" +
                "TklTVFJBVElWRSBHUlVQUEUvQ049UkVDSVBJRU5UUy9DTj1NQVJUSU5TQ0hNSURUNjE5NjQ3NDYA\n" +
                "AB8A+j8BAAAAHgAAAE0AYQByAHQAaQBuACAAUwBjAGgAbQBpAGQAdAAAAAAAAgH7PwEAAABrAAAA\n" +
                "AAAAANynQMjAQhAatLkIACsv4YIBAAAAAAAAAC9PPVdFQkRFL09VPUVSU1RFIEFETUlOSVNUUkFU\n" +
                "SVZFIEdSVVBQRS9DTj1SRUNJUElFTlRTL0NOPU1BUlRJTlNDSE1JRFQ2MTk2NDc0NgAAAwAZQAAA\n" +
                "AAADABpAAAAAAAMACVkDAAAAAwAAgAggBgAAAAAAwAAAAAAAAEYAAAAAEIUAAGEcAAALAACACCAG\n" +
                "AAAAAADAAAAAAAAARgAAAAADhQAAAAAAAEAAAIAIIAYAAAAAAMAAAAAAAABGAAAAAAKFAAAANH1k\n" +
                "RbnLAUAAAIAIIAYAAAAAAMAAAAAAAABGAAAAAGCFAAAANH1kRbnLAR8AAIACIAYAAAAAAMAAAAAA\n" +
                "AABGAAAAAAiCAAABAAAAAgAAAAAAAAALAACAAiAGAAAAAADAAAAAAAAARgAAAAAVggAAAAAAAAMA\n" +
                "AIACIAYAAAAAAMAAAAAAAABGAAAAABiCAAAAAAAAQAAAgJDa2G4LRRsQmNoAqgA/EwUAAAAAGgAA\n" +
                "AICd3d9GtssBAwAAgCkDAgAAAAAAwAAAAAAAAEYBAAAAPAAAAHUAcgBuADoAcwBjAGgAZQBtAGEA\n" +
                "cwA6AGMAYQBsAGUAbgBkAGEAcgA6AHMAZQBxAHUAZQBuAGMAZQAAAAAAAAAfAACAKQMCAAAAAADA\n" +
                "AAAAAAAARgEAAAA6AAAAdQByAG4AOgBzAGMAaABlAG0AYQBzADoAYwBhAGwAZQBuAGQAYQByADoA\n" +
                "dgBlAHIAcwBpAG8AbgAAAAAAAQAAAAgAAAAyAC4AMAAAAAMAAIADIAYAAAAAAMAAAAAAAABGAAAA\n" +
                "AAGBAAAAAAAABQAAgAMgBgAAAAAAwAAAAAAAAEYAAAAAAoEAAAAAAAAAAAAAAwAAgAMgBgAAAAAA\n" +
                "wAAAAAAAAEYAAAAAE4EAAAEAAAALAACAAyAGAAAAAADAAAAAAAAARgAAAAAcgQAAAAAAAAsAAIAD\n" +
                "IAYAAAAAAMAAAAAAAABGAAAAACaBAAAAAAAAAwAAgAMgBgAAAAAAwAAAAAAAAEYAAAAAEIEAAAAA\n" +
                "AAADAACAAyAGAAAAAADAAAAAAAAARgAAAAARgQAAAAAAAAMAAIADIAYAAAAAAMAAAAAAAABGAAAA\n" +
                "ACqBAAAAAAAAAwAAgAMgBgAAAAAAwAAAAAAAAEYAAAAAKYEAAAAAAAALAACAAyAGAAAAAADAAAAA\n" +
                "AAAARgAAAAAkgQAAAAAAAAsAAIADIAYAAAAAAMAAAAAAAABGAAAAACyBAAAAAAAAHwAAgAMgBgAA\n" +
                "AAAAwAAAAAAAAEYAAAAAJ4EAAAEAAAACAAAAAAAAAAMAAIADIAYAAAAAAMAAAAAAAABGAAAAABKB\n" +
                "AAABAAAAHwAAgAMgBgAAAAAAwAAAAAAAAEYAAAAAIYEAAAEAAAACAAAAAAAAAAsAAIADIAYAAAAA\n" +
                "AMAAAAAAAABGAAAAAAOBAAAAAAAAAwAAgAMgBgAAAAAAwAAAAAAAAEYAAAAAI4EAAP///38LAACA\n" +
                "CCAGAAAAAADAAAAAAAAARgAAAAAOhQAAAAAAAAMAAIACIAYAAAAAAMAAAAAAAABGAAAAABOCAAA8\n" +
                "AAAAQAAAgAIgBgAAAAAAwAAAAAAAAEYAAAAAEYIAAACcQcZNucsBQAAAgAIgBgAAAAAAwAAAAAAA\n" +
                "AEYAAAAAEIIAAACcQcZNucsBQAAAgAIgBgAAAAAAwAAAAAAAAEYAAAAADoIAAACcQcZNucsBAwAA\n" +
                "gAIgBgAAAAAAwAAAAAAAAEYAAAAAAYIAAAAAAABAAACAAiAGAAAAAADAAAAAAAAARgAAAAASggAA\n" +
                "ADR9ZEW5ywFAAACAAiAGAAAAAADAAAAAAAAARgAAAAAPggAAADR9ZEW5ywFAAACAAiAGAAAAAADA\n" +
                "AAAAAAAARgAAAAANggAAADR9ZEW5ywEDAACAAiAGAAAAAADAAAAAAAAARgAAAAAXggAAAAAAAEAA\n" +
                "AICQ2thuC0UbEJjaAKoAPxMFAAAAAAEAAABgBuAfUrbLAQMAAIACIAYAAAAAAMAAAAAAAABGAAAA\n" +
                "AAWCAAACAAAAAwAAgJDa2G4LRRsQmNoAqgA/EwUAAAAAHAAAAAAAAAACAQCAkNrYbgtFGxCY2gCq\n" +
                "AD8TBQAAAAAjAAAAAQAAAFkAAAAEAAAAggDgAHTFtxAaguAIAAAAAAAAAAAAAAAAAAAAAAAAAAAx\n" +
                "AAAAdkNhbC1VaWQBAAAANDA5YjEwYjctZWQ1Yy00YzEyLTkxZTAtZDZlMWFjN2VlNWM3AAAAAEAA\n" +
                "AIACIAYAAAAAAMAAAAAAAABGAAAAADaCAAAAnEHGTbnLAUAAAIACIAYAAAAAAMAAAAAAAABGAAAA\n" +
                "ADWCAAAANH1kRbnLAUAAAIAIIAYAAAAAAMAAAAAAAABGAAAAABeFAAAAnEHGTbnLAUAAAIAIIAYA\n" +
                "AAAAAMAAAAAAAABGAAAAABaFAAAANH1kRbnLAQIAAICQ2thuC0UbEJjaAKoAPxMFAAAAABEAAAAA\n" +
                "AAAACwAAgJDa2G4LRRsQmNoAqgA/EwUAAAAACQAAAAAAAAADAACAkNrYbgtFGxCY2gCqAD8TBQAA\n" +
                "AAAWAAAAAAAAAAMAAICQ2thuC0UbEJjaAKoAPxMFAAAAABUAAAAAAAAAAgAAgJDa2G4LRRsQmNoA\n" +
                "qgA/EwUAAAAAGQAAAAAAAAADAACAkNrYbgtFGxCY2gCqAD8TBQAAAAAPAAAAAAAAAAMAAICQ2thu\n" +
                "C0UbEJjaAKoAPxMFAAAAABAAAAAAAAAACwAAgAIgBgAAAAAAwAAAAAAAAEYAAAAAKYIAAAAAAAAC\n" +
                "AQCAkNrYbgtFGxCY2gCqAD8TBQAAAAADAAAAAQAAAFkAAAAEAAAAggDgAHTFtxAaguAIAAAAAAAA\n" +
                "AAAAAAAAAAAAAAAAAAAxAAAAdkNhbC1VaWQBAAAANDA5YjEwYjctZWQ1Yy00YzEyLTkxZTAtZDZl\n" +
                "MWFjN2VlNWM3AAAAAAMAAIACIAYAAAAAAMAAAAAAAABGAAAAACSCAAD/////CwAAgJDa2G4LRRsQ\n" +
                "mNoAqgA/EwUAAAAACgAAAAAAAAALAACAkNrYbgtFGxCY2gCqAD8TBQAAAAAFAAAAAAAAAAsAAICQ\n" +
                "2thuC0UbEJjaAKoAPxMFAAAAAAQAAAABAAAAAgAAgJDa2G4LRRsQmNoAqgA/EwUAAAAAEwAAAAAA\n" +
                "AAADAACAkNrYbgtFGxCY2gCqAD8TBQAAAAAXAAAAAAAAAB8AAICQ2thuC0UbEJjaAKoAPxMFAAAA\n" +
                "AAcAAAABAAAAAgAAAAAAAAACAACAkNrYbgtFGxCY2gCqAD8TBQAAAAAYAAAAAAAAAB8AAIACIAYA\n" +
                "AAAAAMAAAAAAAABGAAAAADKCAAABAAAAAgAAAAAAAAALAACAAiAGAAAAAADAAAAAAAAARgAAAAAj\n" +
                "ggAAAAAAAAMAAIACIAYAAAAAAMAAAAAAAABGAAAAADGCAAAAAAAAAwAAgAggBgAAAAAAwAAAAAAA\n" +
                "AEYAAAAAAYUAAAAAAAAfAACAkNrYbgtFGxCY2gCqAD8TBQAAAAAGAAAAAQAAAAIAAAAAAAAAHwAA\n" +
                "gJDa2G4LRRsQmNoAqgA/EwUAAAAACAAAAAEAAAACAAAAAAAAAAsAAICQ2thuC0UbEJjaAKoAPxMF\n" +
                "AAAAAAsAAAAAAAAAAwAAgJDa2G4LRRsQmNoAqgA/EwUAAAAADQAAAAAAAAADAACAkNrYbgtFGxCY\n" +
                "2gCqAD8TBQAAAAAOAAAAAAAAAAMAAIAIIAYAAAAAAMAAAAAAAABGAAAAABiFAAAAAAAAAwAAgJDa\n" +
                "2G4LRRsQmNoAqgA/EwUAAAAADAAAAAAAAAAfAACAAiAGAAAAAADAAAAAAAAARgAAAAA0ggAAAQAA\n" +
                "AHQAAAAoAEcATQBUACsAMAAxADoAMAAwACkAIABBAG0AcwB0AGUAcgBkAGEAbQAsACAAQgBlAHIA\n" +
                "bABpAG4ALAAgAEIAZQByAG4ALAAgAFIAbwBtACwAIABTAHQAbwBjAGsAaABvAGwAbQAsACAAVwBp\n" +
                "AGUAbgAAAAIAAICQ2thuC0UbEJjaAKoAPxMFAAAAABIAAAAAAAAAHwAAgJDa2G4LRRsQmNoAqgA/\n" +
                "EwUAAAAAAgAAAAEAAAACAAAAAAAAAAIAAICQ2thuC0UbEJjaAKoAPxMFAAAAABQAAAAAAAAAAwAA\n" +
                "gAIgBgAAAAAAwAAAAAAAAEYAAAAAFIIAAAAAAAADAACAAiAGAAAAAADAAAAAAAAARgAAAABWggAA\n" +
                "AAAAAAMAAIACIAYAAAAAAMAAAAAAAABGAAAAAAeCAAAAAAAACwAAgAIgBgAAAAAAwAAAAAAAAEYA\n" +
                "AAAAV4IAAAAAAAADAACAAiAGAAAAAADAAAAAAAAARgAAAABZggAAAAAAAAMAAIACIAYAAAAAAMAA\n" +
                "AAAAAABGAAAAAEGCAAAAAAAAHwAAgAIgBgAAAAAAwAAAAAAAAEYAAAAAQ4IAAAEAAAACAAAAAAAA\n" +
                "AB8AAIACIAYAAAAAAMAAAAAAAABGAAAAAEKCAAABAAAAAgAAAAAAAAADAACAAiAGAAAAAADAAAAA\n" +
                "AAAARgAAAABFggAAAAAAAB8AAIACIAYAAAAAAMAAAAAAAABGAAAAAEeCAAABAAAAAgAAAAAAAAAf\n" +
                "AACAAiAGAAAAAADAAAAAAAAARgAAAABIggAAAQAAAAIAAAAAAAAAHwAAgAIgBgAAAAAAwAAAAAAA\n" +
                "AEYAAAAASYIAAAEAAAACAAAAAAAAAAsAAIACIAYAAAAAAMAAAAAAAABGAAAAADqCAAABAAAACwAA\n" +
                "gAggBgAAAAAAwAAAAAAAAEYAAAAABoUAAAAAAAALAACACCAGAAAAAADAAAAAAAAARgAAAACChQAA\n" +
                "AAAAAB8AAICQ2thuC0UbEJjaAKoAPxMFAAAAACQAAAABAAAAIAAAAEkAUABNAC4AQQBwAHAAbwBp\n" +
                "AG4AdABtAGUAbgB0AAAAHwAAgJDa2G4LRRsQmNoAqgA/EwUAAAAAHQAAAAEAAAACAAAAAAAAAAMA\n" +
                "AICQ2thuC0UbEJjaAKoAPxMFAAAAACYAAAAAAAAAHwAAgJDa2G4LRRsQmNoAqgA/EwUAAAAAKAAA\n" +
                "AAEAAAACAAAAAAAAAAIBAIACIAYAAAAAAMAAAAAAAABGAAAAAF6CAAABAAAAegAAAAIBNAACABcA\n" +
                "VwAuACAARQB1AHIAbwBwAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQBlAAEAAgE+AAIAQQYA\n" +
                "AAAAAAAAAAAAAAAAAMT///8AAAAAxP///wAACgAAAAUAAwAAAAAAAAAAAAMAAAAFAAIAAAAAAAAA\n" +
                "AAACAQCAAiAGAAAAAADAAAAAAAAARgAAAABfggAAAQAAAHoAAAACATQAAgAXAFcALgAgAEUAdQBy\n" +
                "AG8AcABlACAAUwB0AGEAbgBkAGEAcgBkACAAVABpAG0AZQABAAIBPgACAEEGAAAAAAAAAAAAAAAA\n" +
                "AADE////AAAAAMT///8AAAoAAAAFAAMAAAAAAAAAAAADAAAABQACAAAAAAAAAAAAAwANNPk/AAAC\n" +
                "ARQ0AQAAABAAAABUlKHAKX8QG6WHCAArKiUXHwA9AAEAAAAWAAAAWgB1AGcAZQBzAGEAZwB0ADoA\n" +
                "IAAAAAAAHwA3AAEAAACMAAAAWgB1AGcAZQBzAGEAZwB0ADoAIABUAGUAcgBtAGkAbgAgAC0AIABa\n" +
                "AHUAZwByAGkAZgBmACAAYQB1AGYAIAAxACYAMQAgAEsAbwBuAHQAYQBrAHQALQAgAHUAbgBkACAA\n" +
                "SwBhAGwAZQBuAGQAZQByAGQAYQB0AGUAbgBzAGUAcgB2AGkAYwBlAHMAAAAfAACAhgMCAAAAAADA\n" +
                "AAAAAAAARgEAAAAeAAAAYQBjAGMAZQBwAHQAbABhAG4AZwB1AGEAZwBlAAAAAAABAAAADAAAAGQA\n" +
                "ZQAtAEQARQAAAB8AAICGAwIAAAAAAMAAAAAAAABGAQAAACAAAAB4AC0AbQBzAC0AaABhAHMALQBh\n" +
                "AHQAdABhAGMAaAAAAAEAAAACAAAAAAAAAJer\n" +
                "\n" +
                "--_000_28EB37E571CBA8449A1C8CD297726A5D01A3701A8BEXCHANGE04web_--\n" + "";

            parseMailSource(mailSrc);

            System.out.println("\n\n------------------------------------\n\n");

            final String mailSrc2 = "Content-Type: multipart/mixed;\n" +
            		"    boundary=\"_000_D9106566FE81E04EAB80F67CB5612D2F858A506CEXCHANGE04webde_\"\n" +
            		"From: \"Christian Schneider (PM)\" <chr.schneider@1und1.de>\n" +
            		"To: \"holger.achtziger@open-xchange.com\" <holger.achtziger@open-xchange.com>\n" +
            		"Date: Mon, 17 Jan 2011 14:10:18 +0100\n" +
            		"Subject: Accepted: Termin - Zugriff auf 1&1 Kontakt- und\n" +
            		" Kalenderdatenservices\n" +
            		"Thread-Topic: Termin - Zugriff auf 1&1 Kontakt- und Kalenderdatenservices\n" +
            		"Thread-Index: Acu2R9D9l6A/qpJMQf2VYZjwPYG0fQAABJ0g\n" +
            		"Message-ID: <D9106566FE81E04EAB80F67CB5612D2F858A506C@EXCHANGE04.webde.local>\n" +
            		"Accept-Language: de-DE\n" +
            		"Content-Language: de-DE\n" +
            		"X-MS-Has-Attach: \n" +
            		"X-MS-TNEF-Correlator: <D9106566FE81E04EAB80F67CB5612D2F858A506C@EXCHANGE04.webde.local>\n" +
            		"acceptlanguage: de-DE\n" +
            		"MIME-Version: 1.0\n" +
            		"X-Virus-Scanned: Symantec AntiVirus Scan Engine\n" +
            		"X-UI-Msg-Verification: d9d2ed33d1bc662770bdc522d6129112\n" +
            		"X-purgate-ID: 151428::1295269834-00006D31-C1A029D9/0-0/0-0\n" +
            		"X-purgate-type: clean\n" +
            		"X-purgate-size: 9664\n" +
            		"X-purgate-Ad: Categorized by eleven eXpurgate (R) http://www.eleven.de\n" +
            		"X-purgate: This mail is considered clean (visit http://www.eleven.de for further information)\n" +
            		"X-purgate: clean\n" +
            		"\n" +
            		"--_000_D9106566FE81E04EAB80F67CB5612D2F858A506CEXCHANGE04webde_\n" +
            		"Content-Type: text/plain; charset=\"utf-8\"\n" +
            		"Content-Transfer-Encoding: base64\n" +
            		"\n" +
            		"\n" +
            		"--_000_D9106566FE81E04EAB80F67CB5612D2F858A506CEXCHANGE04webde_\n" +
            		"Content-Disposition: attachment; filename=\"winmail.dat\"\n" +
            		"Content-Transfer-Encoding: base64\n" +
            		"Content-Type: application/ms-tnef; name=\"winmail.dat\"\n" +
            		"\n" +
            		"eJ8+IvNeAQaQCAAEAAAAAAABAAEAAQeQBgAIAAAA5AQAAAAAAADoAAEJgAEAIQAAADZBRkZGQjY0\n" +
            		"MDNEQUIzNDFBMDU0Mzk0NkQwMUNCQjAwADMHAQ2ABAACAAAAAgACAAEFgAMADgAAANsHAQARAA0A\n" +
            		"CgASAAEAHgEBIIADAA4AAADbBwEAEQANAAoAEgABAB4BAQiABwAgAAAASVBNLk1pY3Jvc29mdCBT\n" +
            		"Y2hlZHVsZS5NdGdSZXNwUABXCwEEgAEARgAAAEFjY2VwdGVkOiBUZXJtaW4gLSBadWdyaWZmIGF1\n" +
            		"ZiAxJjEgS29udGFrdC0gdW5kIEthbGVuZGVyZGF0ZW5zZXJ2aWNlcwB2GAEGAAMADgAAANsHAQAV\n" +
            		"AAgAHgAAAAUAIwEBBwADAA4AAADbBwEAFQAJAB4AAAAFACQBAQgABQAEAAAA//////wDAQOQBgAA\n" +
            		"GQAAmAAAAAIBfwABAAAAQgAAADxEOTEwNjU2NkZFODFFMDRFQUI4MEY2N0NCNTYxMkQyRjg1OEE1\n" +
            		"MDZDQEVYQ0hBTkdFMDQud2ViZGUubG9jYWw+AAAAHwBCAAEAAAAyAAAAQwBoAHIAaQBzAHQAaQBh\n" +
            		"AG4AIABTAGMAaABuAGUAaQBkAGUAcgAgACgAUABNACkAAAAAAB8AZQABAAAALgAAAGMAaAByAC4A\n" +
            		"cwBjAGgAbgBlAGkAZABlAHIAQAAxAHUAbgBkADEALgBkAGUAAAAAAB8AZAABAAAACgAAAFMATQBU\n" +
            		"AFAAAAAAAAIBQQABAAAAggAAAAAAAACBKx+kvqMQGZ1uAN0BD1QCAAAAgEMAaAByAGkAcwB0AGkA\n" +
            		"YQBuACAAUwBjAGgAbgBlAGkAZABlAHIAIAAoAFAATQApAAAAUwBNAFQAUAAAAGMAaAByAC4AcwBj\n" +
            		"AGgAbgBlAGkAZABlAHIAQAAxAHUAbgBkADEALgBkAGUAAAAAAB8AGgwBAAAAMgAAAEMAaAByAGkA\n" +
            		"cwB0AGkAYQBuACAAUwBjAGgAbgBlAGkAZABlAHIAIAAoAFAATQApAAAAAAAfAB8MAQAAAC4AAABj\n" +
            		"AGgAcgAuAHMAYwBoAG4AZQBpAGQAZQByAEAAMQB1AG4AZAAxAC4AZABlAAAAAAAfAB4MAQAAAAoA\n" +
            		"AABTAE0AVABQAAAAAAACARkMAQAAAIIAAAAAAAAAgSsfpL6jEBmdbgDdAQ9UAgAAAIBDAGgAcgBp\n" +
            		"AHMAdABpAGEAbgAgAFMAYwBoAG4AZQBpAGQAZQByACAAKABQAE0AKQAAAFMATQBUAFAAAABjAGgA\n" +
            		"cgAuAHMAYwBoAG4AZQBpAGQAZQByAEAAMQB1AG4AZAAxAC4AZABlAAAAAAALAEA6AQAAAB8AGgAB\n" +
            		"AAAAPAAAAEkAUABNAC4AUwBjAGgAZQBkAHUAbABlAC4ATQBlAGUAdABpAG4AZwAuAFIAZQBzAHAA\n" +
            		"LgBQAG8AcwAAAAMA8T8HBAAACwBAOgEAAAADAP0/5AQAAAIBCzABAAAAEAAAAGr/+2QD2rNBoFQ5\n" +
            		"RtAcuwADABcAAQAAAEAAOQAAWSXjR7bLAUAACDB1E6/jR7bLAQsAAgABAAAACwAjAAAAAAADACYA\n" +
            		"AAAAAAsAKQAAAAAACwArAAAAAAADADYAAAAAAAIBRwABAAAAMgAAAGM9REU7YT0gO3A9V0VCREU7\n" +
            		"bD1FWENIQU5HRTA0LTExMDExNzEzMTAxOFotNTY5NjcAAABAAGAAADR9ZEW5ywFAAGEAAJxBxk25\n" +
            		"ywEDAGIA/////x8AcAABAAAAeAAAAFQAZQByAG0AaQBuACAALQAgAFoAdQBnAHIAaQBmAGYAIABh\n" +
            		"AHUAZgAgADEAJgAxACAASwBvAG4AdABhAGsAdAAtACAAdQBuAGQAIABLAGEAbABlAG4AZABlAHIA\n" +
            		"ZABhAHQAZQBuAHMAZQByAHYAaQBjAGUAcwAAAAIBcQABAAAAGwAAAAHLtkfQ/ZegP6qSTEH9lWGY\n" +
            		"8D2BtH0AAASdIAAfADUQAQAAAIQAAAA8AEQAOQAxADAANgA1ADYANgBGAEUAOAAxAEUAMAA0AEUA\n" +
            		"QQBCADgAMABGADYANwBDAEIANQA2ADEAMgBEADIARgA4ADUAOABBADUAMAA2AEMAQABFAFgAQwBI\n" +
            		"AEEATgBHAEUAMAA0AC4AdwBlAGIAZABlAC4AbABvAGMAYQBsAD4AAAADAIAQ/////0AAwxAANH1k\n" +
            		"RbnLAUAAxBAAnEHGTbnLAR8A8xABAAAAnAAAAEEAYwBjAGUAcAB0AGUAZAAlADMAQQAgAFQAZQBy\n" +
            		"AG0AaQBuACAALQAgAFoAdQBnAHIAaQBmAGYAIABhAHUAZgAgADEAJQAyADYAMQAgAEsAbwBuAHQA\n" +
            		"YQBrAHQALQAgAHUAbgBkACAASwBhAGwAZQBuAGQAZQByAGQAYQB0AGUAbgBzAGUAcgB2AGkAYwBl\n" +
            		"AHMALgBFAE0ATAAAAAsA9BAAAAAACwD1EAAAAAALAPYQAAAAAEAABzAUsqzjR7bLAQIBEDABAAAA\n" +
            		"RgAAAAAAAAA392kr4B3GSraD/UYLGy/BBwCpBDjYDbZ+Q58mkNCRtL6GAAABW1GQAAAcgEG0Gkoy\n" +
            		"QYibw/+ODfYvAACE+ghaAAAAAAMA3j/kBAAAHwD4PwEAAAAyAAAAQwBoAHIAaQBzAHQAaQBhAG4A\n" +
            		"IABTAGMAaABuAGUAaQBkAGUAcgAgACgAUABNACkAAAAAAAIB+T8BAAAAYgAAAAAAAADcp0DIwEIQ\n" +
            		"GrS5CAArL+GCAQAAAAAAAAAvTz1XRUJERS9PVT1FUlNURSBBRE1JTklTVFJBVElWRSBHUlVQUEUv\n" +
            		"Q049UkVDSVBJRU5UUy9DTj1DSFJTQ0hORUlERVIAAAAfAPo/AQAAADIAAABDAGgAcgBpAHMAdABp\n" +
            		"AGEAbgAgAFMAYwBoAG4AZQBpAGQAZQByACAAKABQAE0AKQAAAAAAAgH7PwEAAABiAAAAAAAAANyn\n" +
            		"QMjAQhAatLkIACsv4YIBAAAAAAAAAC9PPVdFQkRFL09VPUVSU1RFIEFETUlOSVNUUkFUSVZFIEdS\n" +
            		"VVBQRS9DTj1SRUNJUElFTlRTL0NOPUNIUlNDSE5FSURFUgAAAAMAGUAAAAAAAwAaQAAAAAADAAJZ\n" +
            		"AAAWAAMACVkDAAAAAwAAgAggBgAAAAAAwAAAAAAAAEYAAAAAEIUAAGEcAAALAACACCAGAAAAAADA\n" +
            		"AAAAAAAARgAAAAADhQAAAAAAAEAAAIAIIAYAAAAAAMAAAAAAAABGAAAAAAKFAAAANH1kRbnLAUAA\n" +
            		"AIAIIAYAAAAAAMAAAAAAAABGAAAAAGCFAAAANH1kRbnLAR8AAIACIAYAAAAAAMAAAAAAAABGAAAA\n" +
            		"AAiCAAABAAAAAgAAAAAAAAALAACAAiAGAAAAAADAAAAAAAAARgAAAAAVggAAAAAAAAMAAIACIAYA\n" +
            		"AAAAAMAAAAAAAABGAAAAABiCAAAAAAAAQAAAgJDa2G4LRRsQmNoAqgA/EwUAAAAAGgAAAICd3d9G\n" +
            		"tssBAwAAgCkDAgAAAAAAwAAAAAAAAEYBAAAAPAAAAHUAcgBuADoAcwBjAGgAZQBtAGEAcwA6AGMA\n" +
            		"YQBsAGUAbgBkAGEAcgA6AHMAZQBxAHUAZQBuAGMAZQAAAAAAAAAfAACAKQMCAAAAAADAAAAAAAAA\n" +
            		"RgEAAAA6AAAAdQByAG4AOgBzAGMAaABlAG0AYQBzADoAYwBhAGwAZQBuAGQAYQByADoAdgBlAHIA\n" +
            		"cwBpAG8AbgAAAAAAAQAAAAgAAAAyAC4AMAAAAAMAAIADIAYAAAAAAMAAAAAAAABGAAAAAAGBAAAA\n" +
            		"AAAABQAAgAMgBgAAAAAAwAAAAAAAAEYAAAAAAoEAAAAAAAAAAAAAAwAAgAMgBgAAAAAAwAAAAAAA\n" +
            		"AEYAAAAAE4EAAAEAAAALAACAAyAGAAAAAADAAAAAAAAARgAAAAAcgQAAAAAAAAsAAIADIAYAAAAA\n" +
            		"AMAAAAAAAABGAAAAACaBAAAAAAAAAwAAgAMgBgAAAAAAwAAAAAAAAEYAAAAAEIEAAAAAAAADAACA\n" +
            		"AyAGAAAAAADAAAAAAAAARgAAAAARgQAAAAAAAAMAAIADIAYAAAAAAMAAAAAAAABGAAAAACqBAAAA\n" +
            		"AAAAAwAAgAMgBgAAAAAAwAAAAAAAAEYAAAAAKYEAAAAAAAALAACAAyAGAAAAAADAAAAAAAAARgAA\n" +
            		"AAAkgQAAAAAAAAsAAIADIAYAAAAAAMAAAAAAAABGAAAAACyBAAAAAAAAHwAAgAMgBgAAAAAAwAAA\n" +
            		"AAAAAEYAAAAAJ4EAAAEAAAACAAAAAAAAAAMAAIADIAYAAAAAAMAAAAAAAABGAAAAABKBAAABAAAA\n" +
            		"HwAAgAMgBgAAAAAAwAAAAAAAAEYAAAAAIYEAAAEAAAACAAAAAAAAAAsAAIADIAYAAAAAAMAAAAAA\n" +
            		"AABGAAAAAAOBAAAAAAAAAwAAgAMgBgAAAAAAwAAAAAAAAEYAAAAAI4EAAP///38LAACACCAGAAAA\n" +
            		"AADAAAAAAAAARgAAAAAOhQAAAAAAAAMAAIACIAYAAAAAAMAAAAAAAABGAAAAABOCAAA8AAAAQAAA\n" +
            		"gAIgBgAAAAAAwAAAAAAAAEYAAAAAEYIAAACcQcZNucsBQAAAgAIgBgAAAAAAwAAAAAAAAEYAAAAA\n" +
            		"EIIAAACcQcZNucsBQAAAgAIgBgAAAAAAwAAAAAAAAEYAAAAADoIAAACcQcZNucsBAwAAgAIgBgAA\n" +
            		"AAAAwAAAAAAAAEYAAAAAAYIAAAAAAABAAACAAiAGAAAAAADAAAAAAAAARgAAAAASggAAADR9ZEW5\n" +
            		"ywFAAACAAiAGAAAAAADAAAAAAAAARgAAAAAPggAAADR9ZEW5ywFAAACAAiAGAAAAAADAAAAAAAAA\n" +
            		"RgAAAAANggAAADR9ZEW5ywEDAACAAiAGAAAAAADAAAAAAAAARgAAAAAXggAAAAAAAEAAAICQ2thu\n" +
            		"C0UbEJjaAKoAPxMFAAAAAAEAAABAFXTjR7bLAQMAAIACIAYAAAAAAMAAAAAAAABGAAAAAAWCAAAC\n" +
            		"AAAAAwAAgJDa2G4LRRsQmNoAqgA/EwUAAAAAHAAAAAAAAAACAQCAkNrYbgtFGxCY2gCqAD8TBQAA\n" +
            		"AAAjAAAAAQAAAFkAAAAEAAAAggDgAHTFtxAaguAIAAAAAAAAAAAAAAAAAAAAAAAAAAAxAAAAdkNh\n" +
            		"bC1VaWQBAAAANDA5YjEwYjctZWQ1Yy00YzEyLTkxZTAtZDZlMWFjN2VlNWM3AAAAAEAAAIACIAYA\n" +
            		"AAAAAMAAAAAAAABGAAAAADaCAAAAnEHGTbnLAUAAAIACIAYAAAAAAMAAAAAAAABGAAAAADWCAAAA\n" +
            		"NH1kRbnLAUAAAIAIIAYAAAAAAMAAAAAAAABGAAAAABeFAAAAnEHGTbnLAUAAAIAIIAYAAAAAAMAA\n" +
            		"AAAAAABGAAAAABaFAAAANH1kRbnLAQIAAICQ2thuC0UbEJjaAKoAPxMFAAAAABEAAAAAAAAACwAA\n" +
            		"gJDa2G4LRRsQmNoAqgA/EwUAAAAACQAAAAAAAAADAACAkNrYbgtFGxCY2gCqAD8TBQAAAAAWAAAA\n" +
            		"AAAAAAMAAICQ2thuC0UbEJjaAKoAPxMFAAAAABUAAAAAAAAAAgAAgJDa2G4LRRsQmNoAqgA/EwUA\n" +
            		"AAAAGQAAAAAAAAADAACAkNrYbgtFGxCY2gCqAD8TBQAAAAAPAAAAAAAAAAMAAICQ2thuC0UbEJja\n" +
            		"AKoAPxMFAAAAABAAAAAAAAAACwAAgAIgBgAAAAAAwAAAAAAAAEYAAAAAKYIAAAAAAAACAQCAkNrY\n" +
            		"bgtFGxCY2gCqAD8TBQAAAAADAAAAAQAAAFkAAAAEAAAAggDgAHTFtxAaguAIAAAAAAAAAAAAAAAA\n" +
            		"AAAAAAAAAAAxAAAAdkNhbC1VaWQBAAAANDA5YjEwYjctZWQ1Yy00YzEyLTkxZTAtZDZlMWFjN2Vl\n" +
            		"NWM3AAAAAAMAAIACIAYAAAAAAMAAAAAAAABGAAAAACSCAAD/////CwAAgJDa2G4LRRsQmNoAqgA/\n" +
            		"EwUAAAAACgAAAAAAAAALAACAkNrYbgtFGxCY2gCqAD8TBQAAAAAFAAAAAAAAAAsAAICQ2thuC0Ub\n" +
            		"EJjaAKoAPxMFAAAAAAQAAAABAAAAAgAAgJDa2G4LRRsQmNoAqgA/EwUAAAAAEwAAAAAAAAADAACA\n" +
            		"kNrYbgtFGxCY2gCqAD8TBQAAAAAXAAAAAAAAAB8AAICQ2thuC0UbEJjaAKoAPxMFAAAAAAcAAAAB\n" +
            		"AAAAAgAAAAAAAAACAACAkNrYbgtFGxCY2gCqAD8TBQAAAAAYAAAAAAAAAB8AAIACIAYAAAAAAMAA\n" +
            		"AAAAAABGAAAAADKCAAABAAAAAgAAAAAAAAALAACAAiAGAAAAAADAAAAAAAAARgAAAAAjggAAAAAA\n" +
            		"AAMAAIACIAYAAAAAAMAAAAAAAABGAAAAADGCAAAAAAAAAwAAgAggBgAAAAAAwAAAAAAAAEYAAAAA\n" +
            		"AYUAAAAAAAAfAACAkNrYbgtFGxCY2gCqAD8TBQAAAAAGAAAAAQAAAAIAAAAAAAAAHwAAgJDa2G4L\n" +
            		"RRsQmNoAqgA/EwUAAAAACAAAAAEAAAACAAAAAAAAAAsAAICQ2thuC0UbEJjaAKoAPxMFAAAAAAsA\n" +
            		"AAAAAAAAAwAAgJDa2G4LRRsQmNoAqgA/EwUAAAAADQAAAAAAAAADAACAkNrYbgtFGxCY2gCqAD8T\n" +
            		"BQAAAAAOAAAAAAAAAAMAAIAIIAYAAAAAAMAAAAAAAABGAAAAABiFAAAAAAAAAwAAgJDa2G4LRRsQ\n" +
            		"mNoAqgA/EwUAAAAADAAAAAAAAAAfAACAAiAGAAAAAADAAAAAAAAARgAAAAA0ggAAAQAAAHQAAAAo\n" +
            		"AFUAVABDACsAMAAxADoAMAAwACkAIABBAG0AcwB0AGUAcgBkAGEAbQAsACAAQgBlAHIAbABpAG4A\n" +
            		"LAAgAEIAZQByAG4ALAAgAFIAbwBtACwAIABTAHQAbwBjAGsAaABvAGwAbQAsACAAVwBpAGUAbgAA\n" +
            		"AAIAAICQ2thuC0UbEJjaAKoAPxMFAAAAABIAAAAAAAAAHwAAgJDa2G4LRRsQmNoAqgA/EwUAAAAA\n" +
            		"AgAAAAEAAAACAAAAAAAAAAIAAICQ2thuC0UbEJjaAKoAPxMFAAAAABQAAAAAAAAAAwAAgAIgBgAA\n" +
            		"AAAAwAAAAAAAAEYAAAAAFIIAAAAAAAADAACAAiAGAAAAAADAAAAAAAAARgAAAABWggAAAAAAAAMA\n" +
            		"AIACIAYAAAAAAMAAAAAAAABGAAAAAAeCAAAAAAAACwAAgAIgBgAAAAAAwAAAAAAAAEYAAAAAV4IA\n" +
            		"AAAAAAADAACAAiAGAAAAAADAAAAAAAAARgAAAABZggAAAAAAAAMAAIACIAYAAAAAAMAAAAAAAABG\n" +
            		"AAAAAEGCAAAAAAAAHwAAgAIgBgAAAAAAwAAAAAAAAEYAAAAAQ4IAAAEAAAACAAAAAAAAAB8AAIAC\n" +
            		"IAYAAAAAAMAAAAAAAABGAAAAAEKCAAABAAAAAgAAAAAAAAADAACAAiAGAAAAAADAAAAAAAAARgAA\n" +
            		"AABFggAAAAAAAB8AAIACIAYAAAAAAMAAAAAAAABGAAAAAEeCAAABAAAAAgAAAAAAAAAfAACAAiAG\n" +
            		"AAAAAADAAAAAAAAARgAAAABIggAAAQAAAAIAAAAAAAAAHwAAgAIgBgAAAAAAwAAAAAAAAEYAAAAA\n" +
            		"SYIAAAEAAAACAAAAAAAAAAsAAIACIAYAAAAAAMAAAAAAAABGAAAAADqCAAABAAAACwAAgAggBgAA\n" +
            		"AAAAwAAAAAAAAEYAAAAABoUAAAAAAAALAACACCAGAAAAAADAAAAAAAAARgAAAACChQAAAAAAAB8A\n" +
            		"AICQ2thuC0UbEJjaAKoAPxMFAAAAACQAAAABAAAAIAAAAEkAUABNAC4AQQBwAHAAbwBpAG4AdABt\n" +
            		"AGUAbgB0AAAAHwAAgJDa2G4LRRsQmNoAqgA/EwUAAAAAHQAAAAEAAAACAAAAAAAAAAMAAICQ2thu\n" +
            		"C0UbEJjaAKoAPxMFAAAAACYAAAAAAAAAHwAAgJDa2G4LRRsQmNoAqgA/EwUAAAAAKAAAAAEAAAAC\n" +
            		"AAAAAAAAAAIBAIACIAYAAAAAAMAAAAAAAABGAAAAAF6CAAABAAAAegAAAAIBNAACABcAVwAuACAA\n" +
            		"RQB1AHIAbwBwAGUAIABTAHQAYQBuAGQAYQByAGQAIABUAGkAbQBlAAEAAgE+AAIAQQYAAAAAAAAA\n" +
            		"AAAAAAAAAMT///8AAAAAxP///wAACgAAAAUAAwAAAAAAAAAAAAMAAAAFAAIAAAAAAAAAAAACAQCA\n" +
            		"AiAGAAAAAADAAAAAAAAARgAAAABfggAAAQAAAHoAAAACATQAAgAXAFcALgAgAEUAdQByAG8AcABl\n" +
            		"ACAAUwB0AGEAbgBkAGEAcgBkACAAVABpAG0AZQABAAIBPgACAEEGAAAAAAAAAAAAAAAAAADE////\n" +
            		"AAAAAMT///8AAAoAAAAFAAMAAAAAAAAAAAADAAAABQACAAAAAAAAAAAAAwANNPk/AAACARQ0AQAA\n" +
            		"ABAAAABUlKHAKX8QG6WHCAArKiUXHwA9AAEAAAAWAAAAQQBjAGMAZQBwAHQAZQBkADoAIAAAAAAA\n" +
            		"HwA3AAEAAACMAAAAQQBjAGMAZQBwAHQAZQBkADoAIABUAGUAcgBtAGkAbgAgAC0AIABaAHUAZwBy\n" +
            		"AGkAZgBmACAAYQB1AGYAIAAxACYAMQAgAEsAbwBuAHQAYQBrAHQALQAgAHUAbgBkACAASwBhAGwA\n" +
            		"ZQBuAGQAZQByAGQAYQB0AGUAbgBzAGUAcgB2AGkAYwBlAHMAAAAfAACAhgMCAAAAAADAAAAAAAAA\n" +
            		"RgEAAAAeAAAAYQBjAGMAZQBwAHQAbABhAG4AZwB1AGEAZwBlAAAAAAABAAAADAAAAGQAZQAtAEQA\n" +
            		"RQAAAB8AAICGAwIAAAAAAMAAAAAAAABGAQAAACAAAAB4AC0AbQBzAC0AaABhAHMALQBhAHQAdABh\n" +
            		"AGMAaAAAAAEAAAACAAAAAAAAAG62\n" +
            		"\n" +
            		"--_000_D9106566FE81E04EAB80F67CB5612D2F858A506CEXCHANGE04webde_--";

            parseMailSource(mailSrc2);

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseMailSource(final String mailSrc) throws MessagingException, IOException {
        final MimeMessage mimeMessage =
            new MimeMessage(
                MimeDefaultSession.getDefaultSession(),
                new UnsynchronizedByteArrayInputStream(mailSrc.getBytes(com.openexchange.java.Charsets.US_ASCII)));

        final Multipart mulitpart = (Multipart) mimeMessage.getContent();
        final int count = mulitpart.getCount();
        for (int i = 0; i < count; i++) {
            final BodyPart bodyPart = mulitpart.getBodyPart(i);
            if (bodyPart.getContentType().startsWith("application/ms-tnef")) {
                final TNEFInputStream tnefInputStream = new TNEFInputStream(bodyPart.getInputStream());
                /*
                 * Wrapping TNEF message
                 */
                final net.freeutils.tnef.Message message = new net.freeutils.tnef.Message(tnefInputStream);
                System.out.println(tnef2VPart(message));
            }
        }
    }

    /**
     * Checks if specified TNEF's message class name indicates a convertible VPart.
     * <p>
     * The message class name needs to be one of:
     * <ul>
     * <li>IPM.Microsoft Schedule.MtgCncl (CANCELED)</li>
     * <li>IPM.Microsoft Schedule.MtgReq (REQUEST)</li>
     * <li>IPM.Microsoft Schedule.MtgRespP (ACCEPTED)</li>
     * <li>IPM.Microsoft Schedule.MtgRespN (DECLINED)</li>
     * <li>IPM.Microsoft Schedule.MtgRespA (TENTATIVE)</li>
     * </ul>
     *
     * @param message The TNEF message
     * @return <code>true</code> if a convertible VPart; otherwise <code>false</code>
     */
    public static boolean isVPart(final net.freeutils.tnef.Message message) {
        try {
            final Attr messageClass = message.getAttribute(Attr.attMessageClass);
            return isVPart(messageClass == null ? "" : ((String) messageClass.getValue()).toUpperCase(Locale.ENGLISH));
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * Checks if specified TNEF's message class name indicates a convertible VPart.
     * <p>
     * The message class name needs to be one of:
     * <ul>
     * <li>IPM.Microsoft Schedule.MtgCncl (CANCELED)</li>
     * <li>IPM.Microsoft Schedule.MtgReq (REQUEST)</li>
     * <li>IPM.Microsoft Schedule.MtgRespP (ACCEPTED)</li>
     * <li>IPM.Microsoft Schedule.MtgRespN (DECLINED)</li>
     * <li>IPM.Microsoft Schedule.MtgRespA (TENTATIVE)</li>
     * </ul>
     *
     * @param messageClassName The message class name of TNEF message
     * @return <code>true</code> if a convertible VPart; otherwise <code>false</code>
     */
    public static boolean isVPart(final String messageClassName) {
        if (null == messageClassName) {
            return false;
        }
        final String mcn = messageClassName.toUpperCase(Locale.ENGLISH);
        return (mcn.startsWith("IPM.MICROSOFT SCHEDULE.") || mcn.startsWith("IPM.SCHEDULE.") || "IPM.APPOINTMENT".equals(mcn));
    }

    // http://api.kde.org/4.x-api/kdepimlibs-apidocs/ktnef/html/formatter_8cpp_source.html
    // ttp://www.pokorra.de/kolab/tnef/
    // http://api.kde.org/3.5-api/kdepim-apidocs/libkcal/html/incidenceformatter_8cpp_source.html

    /**
     * Converts specified TNEF message to a VCalendar if message class name is one of:
     * <ul>
     * <li>IPM.Microsoft Schedule.MtgCncl (CANCELED)</li>
     * <li>IPM.Microsoft Schedule.MtgReq (REQUEST)</li>
     * <li>IPM.Microsoft Schedule.MtgRespP (ACCEPTED)</li>
     * <li>IPM.Microsoft Schedule.MtgRespN (DECLINED)</li>
     * <li>IPM.Microsoft Schedule.MtgRespA (TENTATIVE)</li>
     * </ul>
     *
     * @param message The TNEF message to convert
     * @return The resulting VCalendar or <code>null</code> if not cannot be converted
     */
    public static net.fortuna.ical4j.model.Calendar tnef2VPart(final net.freeutils.tnef.Message message) {
        try {
            final Attr messageClass = message.getAttribute(Attr.attMessageClass);
            final String messageClassName;
            if (messageClass == null) {
                final MAPIProp prop = message.getMAPIProps().getProp(MAPIProp.PR_MESSAGE_CLASS);
                messageClassName = null == prop ? "" : prop.getValue().toString().toUpperCase(Locale.ENGLISH);
            } else {
                messageClassName = ((String) messageClass.getValue()).toUpperCase(Locale.ENGLISH);
            }
            final MAPIProps mapiProps = message.getMAPIProps();
            if (mapiProps == null) {
                return null;
            }


//            final FileWriter fw = new FileWriter("/home/thorben/Desktop/mapi.txt");
//            try {
//                final MAPIProp[] allProps = mapiProps.getProps();
//                for (final MAPIProp mapiProp : allProps) {
//                    fw.write(mapiProp.toString() + "\r\n");
//                }
//                fw.flush();
//            } finally {
//                fw.close();
//            }


            boolean bCompatMethodRequest = false;
            boolean bCompatMethodCancled = false;
            boolean bCompatMethodAccepted = false;
            boolean bCompatMethodAcceptedCond = false;
            boolean bCompatMethodDeclined = false;
            /*
             * Check if TNEF c can be converted to a VCal part
             */
            if (!isVPart(messageClassName)) {
                return null;
            }
            /*
             * Create VEvent instance
             */
            final net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar();
            /*
             * Check message class type
             */
            if (messageClassName.endsWith(".MTGREQ")) {
                bCompatMethodRequest = true;
            }
            if (messageClassName.endsWith(".MTGCNCL")) {
                bCompatMethodCancled = true;
            }
            if (messageClassName.endsWith(".MTGRESPP")) {
                bCompatMethodAccepted = true;
            }
            if (messageClassName.endsWith(".MTGRESPA")) {
                bCompatMethodAcceptedCond = true;
            }
            if (messageClassName.endsWith(".MTGRESPN")) {
                bCompatMethodDeclined = true;
            }
            /*
             * ProdId & Version
             */
            {
                final PropertyList propertyList = calendar.getProperties();
                propertyList.add(new ProdId(new StringBuilder("-//Microsoft Corporation//Outlook ").append(
                    findNamedProp("0x8554", "9.0", mapiProps)).append(" MIMEDIR//EN").toString()));
                propertyList.add(net.fortuna.ical4j.model.property.Version.VERSION_2_0);
            }
            /*
             * Sender
             */
            final String sSenderName = findProp(MAPIProp.PR_SENDER_NAME, mapiProps);
            final String sSenderSearchKeyEmail = getEmailAddress(findPropString(MAPIProp.PR_SENDER_EMAIL_ADDRESS, mapiProps));
            boolean bIsReply = false;
            if (bCompatMethodAccepted || bCompatMethodAcceptedCond || bCompatMethodDeclined) {
                bIsReply = true;
                calendar.getProperties().add(net.fortuna.ical4j.model.property.Method.REPLY);
            } else {
                if ("1".equals(findProp(MAPIProp.PR_REPLY_REQUESTED, mapiProps))) {
                    bIsReply = true;
                    calendar.getProperties().add(net.fortuna.ical4j.model.property.Method.REPLY);
                } else {
                    calendar.getProperties().add(net.fortuna.ical4j.model.property.Method.REQUEST);
                }
            }
            /*
             * VEvent
             */
            final VEvent event = new VEvent();
            final PropertyList eventPropertyList = event.getProperties();
            eventPropertyList.add(Clazz.PUBLIC);
            /*
             * Look for organizer property
             */
            String s = getEmailAddress(findPropString(MAPIProp.PR_SENDER_EMAIL_ADDRESS, mapiProps));
            if (isEmpty(s) && !bIsReply) {
                s = sSenderSearchKeyEmail;
            }
            // TODO: Use the common name?
            final String organizer;
            if (!isEmpty(s)) {
                organizer = s;
                final Organizer property = new Organizer(organizer);
                if (null != sSenderName) {
                    property.getParameters().add(new Cn(sSenderName));
                }
                eventPropertyList.add(property);
            } else {
                organizer = null;
            }
            /*
             * Attendees
             */
            s = findProp(MAPIProp.PR_DISPLAY_TO, mapiProps);
            final String[] attendees = null == s ? new String[0] : s.split(" *; *");
            if (attendees.length > 0) {
                for (final String sAttendee : attendees) {
                    final String addr = getEmailAddress(sAttendee);
                    if (null != addr) {
                        s = addr;
                        final Attendee attendee = generateAttendee(
                            bCompatMethodAccepted,
                            bCompatMethodAcceptedCond,
                            bCompatMethodDeclined,
                            bIsReply,
                            s);
                        eventPropertyList.add(attendee);
                    }
                }
            } else {
                s = sSenderSearchKeyEmail;
                if (!isEmpty(s)) {
                    final Attendee attendee = generateAttendee(
                        bCompatMethodAccepted,
                        bCompatMethodAcceptedCond,
                        bCompatMethodDeclined,
                        bIsReply,
                        s);
                    eventPropertyList.add(attendee);
                }
            }
            /*
             * Time zone ID
             */
            final String tzid;
            {
                String tmp = findNamedProp("0x8234", mapiProps);
                if (null == tmp) {
                    tzid = "GMT";
                } else {
                    final int p1 = tmp.indexOf('(') + 1;
                    final int p2 = tmp.indexOf(')', p1);
                    tmp = tmp.substring(p1, p2);
                    tzid = java.util.TimeZone.getTimeZone(tmp).getID();
                }
            }
            /*
             * Creation date
             */
            Date d = findProp(MAPIProp.PR_CREATION_TIME, mapiProps);
            if (d != null) {
                eventPropertyList.add(new Created(toDateTime(d, tzid)));
            }
            /*
             * Start date
             */
            d = findProp(MAPIProp.PR_START_DATE, mapiProps);
            if (d != null) {
                eventPropertyList.add(new DtStart(toDateTime(d, tzid)));
            }
            /*
             * End date
             */
            d = findProp(MAPIProp.PR_END_DATE, mapiProps);
            if (d != null) {
                eventPropertyList.add(new DtEnd(toDateTime(d, tzid)));
            }
            /*
             * Location
             */
            s = findNamedProp("0x8208", mapiProps);
            if (!isEmpty(s)) {
                eventPropertyList.add(new Location(s));
            }
            /*-
             * UID
             *
             * Is "0x23" OK  -  or should we look for "0x3" ??
             */
            {
                RawInputStream ris = findNamedProp("0x23", mapiProps);
                if (ris == null) {
                    ris = findNamedProp("0x3", mapiProps);
                }
                if (ris != null) {
                    s = toHexString(ris.toByteArray());
                    eventPropertyList.add(new Uid(s));
                }
            }
            /*-
             * Is this value in local time zone? Must it be
             * adjusted? Most likely this is a bug in the server or in
             * Outlook - we ignore it for now.
             */
            d = findNamedProp("0x8202", mapiProps);
            if (d != null) {
                eventPropertyList.add(new DtStamp(toDateTime(d, tzid)));
            }
            /*
             * Categories
             */
            s = findPropString(MAPIProp.PR_KEYWORD, mapiProps);
            if (!isEmpty(s)) {
                eventPropertyList.add(new Categories(s));
            }
            /*
             * Description
             */
            s = findPropString(MAPIProp.PR_BODY, mapiProps);
            if (!isEmpty(s)) {
                eventPropertyList.add(new Description(s));
            }
            /*
             * Summary
             */
            s = findPropString(MAPIProp.PR_CONVERSATION_TOPIC, mapiProps);
            if (!isEmpty(s)) {
                eventPropertyList.add(new Summary(s));
            }
            /*
             * Priority
             */
            s = findPropString(MAPIProp.PR_PRIORITY, mapiProps);
            if (!isEmpty(s)) {
                eventPropertyList.add(new Priority(Integer.parseInt(s.trim())));
            }
            /*
             * Locale
             */
            final Locale locale;
            s = findNamedPropString("acceptlanguage", mapiProps);
            if (s == null) {
                locale = Locale.ENGLISH;
            } else {
                final Locale tmp = LocaleTools.getLocale(s.replaceAll("-", "_"));
                locale = tmp == null ? Locale.ENGLISH : tmp;
            }
            /*
             * Is reminder flag set?
             */
            if (!isEmpty(findNamedPropString("0x8503", mapiProps))) {
                final VAlarm vAlarm = new VAlarm();
                /*
                 * Always DSIPLAY
                 */
                vAlarm.getProperties().add(Action.DISPLAY);
                d = findNamedProp("0x8502", mapiProps);
                final Date highNoonTime = d;
                /*
                 * Trigger
                 */
                d = findNamedProp("0x8560", mapiProps);
                vAlarm.getProperties().add(new Trigger(new DateTime(d)));
                /*
                 * Reminder
                 */
                vAlarm.getProperties().add(new Description(StringHelper.valueOf(locale).getString(Notifications.REMINDER)));
                event.getAlarms().add(vAlarm);
            }
            /*
             * Add to VCalendar
             */
            calendar.getComponents().add(event);
            return calendar;
        } catch (final NumberFormatException e) {
            LOG.error(e.getMessage(), e);
            return null;
        } catch (final IOException e) {
            LOG.error(e.getMessage(), e);
            return null;
        } catch (final URISyntaxException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    private static Attendee generateAttendee(final boolean bCompatMethodAccepted, final boolean bCompatMethodAcceptedCond, final boolean bCompatMethodDeclined, final boolean bIsReply, final String s) throws URISyntaxException {
        final Attendee attendee = new Attendee(s);
        if (bIsReply) {
            if (bCompatMethodAccepted) {
                attendee.getParameters().add(PartStat.ACCEPTED);
                attendee.getParameters().add(Role.REQ_PARTICIPANT);
            }
            if (bCompatMethodDeclined) {
                attendee.getParameters().add(PartStat.DECLINED);
                attendee.getParameters().add(Role.REQ_PARTICIPANT);
            }
            if (bCompatMethodAcceptedCond) {
                attendee.getParameters().add(PartStat.TENTATIVE);
                attendee.getParameters().add(Role.REQ_PARTICIPANT);
            }
        } else {
            attendee.getParameters().add(PartStat.NEEDS_ACTION);
            attendee.getParameters().add(Role.REQ_PARTICIPANT);
            attendee.getParameters().add(Rsvp.TRUE);
        }
        attendee.getParameters().add(CuType.INDIVIDUAL);
        return attendee;
    }

}

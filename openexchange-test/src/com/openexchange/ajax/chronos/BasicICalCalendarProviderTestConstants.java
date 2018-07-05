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

package com.openexchange.ajax.chronos;


/**
 * {@link BasicICalCalendarProviderTestConstants}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class BasicICalCalendarProviderTestConstants {
    // @formatter:off
    
    // contains 38 events
    public static final String GENERIC_RESPONSE = "BEGIN:VCALENDAR\n" + 
        "PRODID:-//Google Inc//Google Calendar 70.9054//EN\n" + 
        "VERSION:2.0\n" + 
        "CALSCALE:GREGORIAN\n" + 
        "METHOD:PUBLISH\n" + 
        "X-WR-TIMEZONE:Europe/Berlin\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170702T201500Z\n" + 
        "DTEND:20170702T211500Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:c9imcdb56krjib9mckpm2b9kcph36b9p70q6ab9p74pjgohk60s6cd9l6k@google.com\n" + 
        "CREATED:20170702T200145Z\n" + 
        "DESCRIPTION:\n" + 
        "LAST-MODIFIED:20170702T200145Z\n" + 
        "LOCATION:St. Petersburg\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland ist Confedcup-Sieger!\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170702T180000Z\n" + 
        "DTEND:20170702T200000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:d40uh9ek3j28sqqqkihduok78k@google.com\n" + 
        "CREATED:20170629T201453Z\n" + 
        "DESCRIPTION:Confed Cup 2017 in Russland\\, Endspiel.\\nSieger der 2 Halbfinal\n" + 
        " e treffen aufeinander\\nLive \u00fcbertragen im ZDF (Free TV\\, 2. Programm) und a\n" + 
        " ls LiveStream bei https://www.zdf.de/live-tv\n" + 
        "LAST-MODIFIED:20170702T195803Z\n" + 
        "LOCATION:St. Petersburg\\, Russland. Live-TV-\u00fcbertragung im ZDF (2. Programm\n" + 
        " )\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Finale Confed Cup. Chile - Deutschland 0:1\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170630T184500Z\n" + 
        "DTEND:20170630T204500Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:aps3hi7lokj0o6oknj1ijk9pf4@google.com\n" + 
        "CREATED:20170628T063053Z\n" + 
        "DESCRIPTION:Endspiel U21-Europameisterschaft.\\nU21 Nationalmannschaften Deu\n" + 
        " tschland gegen Schweden \\nLive zu sehen im ZDF (2. Programm\\, Free TV) und \n" + 
        " per gratis LiveStream bei https://www.zdf.de/live-tv\n" + 
        "LAST-MODIFIED:20170702T152610Z\n" + 
        "LOCATION:Krakau\\, Polen. Live-\u00fcbertragung im ZDF und per LiveStream bei zdf\n" + 
        " .de\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:U21-EM Finale: Deutschland-Spanien 1:0\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170629T180000Z\n" + 
        "DTEND:20170629T200000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:75hjsi887k9de9n6gfotv5epgc@google.com\n" + 
        "CREATED:20170625T170211Z\n" + 
        "DESCRIPTION:\u26bd  Confed Cup 2017 in Russland\\, 2. Halbfinale\\nLive-\u00fcbertragun\n" + 
        " g in der ARD (1. Programm\\, FREE) und als Free-LiveStream bei daserste.de\n" + 
        "LAST-MODIFIED:20170629T200553Z\n" + 
        "LOCATION:Sotschi\\, Russland. Live im ARD TV und ARD Stream\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland - Mexiko (Halbfinale Confed Cup) 4:1\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170627T160000Z\n" + 
        "DTEND:20170627T180000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:4l2pshhim64sm7bhpecibn6peo@google.com\n" + 
        "CREATED:20170626T061912Z\n" + 
        "DESCRIPTION:\u26bd U21-Europameisterschaft aus Polen: Halbfinale\\nDie ARD (DasEr\n" + 
        " ste) \u00fcbertr\u00e4gt live im FreeTV - zus\u00e4tzlich gibt es einen Livestream: Dasers\n" + 
        " te.de\n" + 
        "LAST-MODIFIED:20170628T204752Z\n" + 
        "LOCATION:Tychy\\, Polen. TV-\u00fcbertragung im 1. Programm + InternetStream\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:\u26bd U21-EM-Halbfinale: Deutschland - England 4:3 i.E.\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170622T180000Z\n" + 
        "DTEND:20170622T200000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:1ntsjm81ci87tflpe5mb20gmac@google.com\n" + 
        "CREATED:20170616T055647Z\n" + 
        "DESCRIPTION:Confed Cup 2017 in Russland\\, Gruppenphase\\nDas L\u00e4nderspiel Deu\n" + 
        " tschland-Chile im Confed Cup wird live im Fernsehen \u00fcbertragen. Das Erste z\n" + 
        " eigt diese Partie live im Free-TV und im Internet in der Mediathek.\n" + 
        "LAST-MODIFIED:20170622T202449Z\n" + 
        "LOCATION:Kazan\\, Russland. TV-\u00fcbertragung in der ARD\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:\u26bd Deutschland-Chile (Confed Cup) 1:1\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170619T150000Z\n" + 
        "DTEND:20170619T170000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:fv5hhhvurecuki2e3ng9pa944k@google.com\n" + 
        "CREATED:20170615T205638Z\n" + 
        "DESCRIPTION:Confed Cup 2017 in Russland\\, Gruppenphase\\nB3-B4\\nDas L\u00e4ndersp\n" + 
        " iel Australien-Deutschland im Confed Cup wird live im Fernsehen \u00fcbertragen \n" + 
        " und es gibt einen gratis-LiveStream. Live im ZDF Fernsehen und als legaler \n" + 
        " HD LiveStream bei zdf.de zu sehen\\nLive-Reporter: B\u00e9la R\u00e9thy\n" + 
        "LAST-MODIFIED:20170620T044052Z\n" + 
        "LOCATION:Sotschi\\, Russland. TV-\u00fcbertragung im ZDF (2. Programm)\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:\u26bd Australien-Deutschland (Confed Cup) 2:3\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170625T150000Z\n" + 
        "DTEND:20170625T170000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:t0ti3ji34u0g1sbccd3gqt95ig@google.com\n" + 
        "CREATED:20170616T055738Z\n" + 
        "DESCRIPTION:Confed Cup 2017 in Russland\\, Gruppenphase\\nDas L\u00e4nderspiel Deu\n" + 
        " tschland-Kamerun im Confed Cup wird live im Fernsehen \u00fcbertragen. Das ZDF \u00fc\n" + 
        " bertr\u00e4gt live im Free-TV und per Internetstream.\n" + 
        "LAST-MODIFIED:20170616T170241Z\n" + 
        "LOCATION:Sotschi\\, Russland. TV-\u00fcbertragung im ZDF\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:\u26bd Deutschland-Kamerun (Confed Cup)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170617T150000Z\n" + 
        "DTEND:20170617T170000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:i9spbhu9lt3d8g3hpm8kap9quo@google.com\n" + 
        "ATTENDEE;CUTYPE=INDIVIDUAL;ROLE=REQ-PARTICIPANT;PARTSTAT=ACCEPTED;CN='Spiel\n" + 
        " e der deutschen Nationalmannschaft (keine anderen Teams) - http://www.fussb\n" + 
        " all-spielplan.de';X-NUM-GUESTS=0:mailto:i4aeldvbflqu7u6jpn8uvv9738@group.ca\n" + 
        " lendar.google.com\n" + 
        "CREATED:20161118T071951Z\n" + 
        "DESCRIPTION:\u26bd Confed Cup 2017 in Russland\\, Gruppenphase\\nDas L\u00e4nderspiel d\n" + 
        " es Confed Cups zwischen Russland und Neuseeland wird live im Fernsehen bei \n" + 
        " der ARD (Das Erste) \u00fcbertragen.\\nLivestream: http://www.daserste.de/live/in\n" + 
        " dex.html\n" + 
        "LAST-MODIFIED:20170616T162902Z\n" + 
        "LOCATION:St. Petersburg\\, Russland. TV-\u00fcbertragung in der ARD + Stream in d\n" + 
        " er Mediathek\n" + 
        "SEQUENCE:1\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:\u26bd Confed Cup Er\u00f6ffnungsspiel (Russland \u2013 Neuseeland)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170901T184500Z\n" + 
        "DTEND:20170901T204500Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:2u8srqjb5h5pll4jrnhl1041sc@google.com\n" + 
        "CREATED:20170503T071426Z\n" + 
        "DESCRIPTION:Qualifikationsspiel f\u00fcr die Weltmeisterschaft 2018 in Russland.\n" + 
        " \\nLive Fernseh \u00fcbertragung auf RTL und per Internet Livestream bei http://w\n" + 
        " ww.ran.de/\\nhttp://www.fussball-spielplan.de/kalender-fuer-die-deutsche-nat\n" + 
        " ionalmannschaft-und-die-em-wm-endrunden/\n" + 
        "LAST-MODIFIED:20170503T071426Z\n" + 
        "LOCATION:Eden-Arena. Prag Live auf RTL TV\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Tschechien-Deutschland (WM Quali)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170606T184500Z\n" + 
        "DTEND:20170606T204500Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:kms7112b01f5badee7dkaq26bg@google.com\n" + 
        "CREATED:20170426T070729Z\n" + 
        "DESCRIPTION:25 Jahre nach der Europameisterschaft 1992 in Schweden kommt es\n" + 
        "  im Rahmen eines Freundschaftsspiels zur Neuauflage des Finales von G\u00f6tebor\n" + 
        " g zwischen D\u00e4nemark und Deutschland. Anl\u00e4sslich des 25-j\u00e4hrigen Jubil\u00e4ums d\n" + 
        " es EM-Titelgewinns war die deutsche Nationalmannschaft Wunschgegner des D\u00e4n\n" + 
        " ischen Fu\u00dfball-Verbandes. Das ZDF \u00fcbertr\u00e4gt live im FreeTV und per Internet\n" + 
        "  Streaming.\n" + 
        "LAST-MODIFIED:20170426T070729Z\n" + 
        "LOCATION:Kopenhagen. Live im ZDF \u00fcbertragen (und per LiveStream)\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:\u26bd D\u00e4nemark - Deutschland (Freundschaftsspiel)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170322T194500Z\n" + 
        "DTEND:20170322T214500Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:bhft6ejt1r0tt02s3gid6rs3bk@google.com\n" + 
        "CREATED:20161021T061533Z\n" + 
        "DESCRIPTION:Freundschaftsspiel der Fu\u00dfball-Nationalmannschaften von Deutsch\n" + 
        " land und England.\\nWird live im FreeTV \u00fcbertragen bei DasErste (ARD) und au\n" + 
        " ch in der Internet-Mediathek bei daserste.de live gestreamt.\n" + 
        "LAST-MODIFIED:20170316T082458Z\n" + 
        "LOCATION:Signal Iduna Park\\, Dortmund. Live auf DasErste im Free TV \u00fcbertra\n" + 
        " gen \n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland-England (Testspiel)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20171114T111200Z\n" + 
        "DTEND:20171114T121200Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:gbs5ogotqp3l3a1ob0o8kb22e8@google.com\n" + 
        "CREATED:20161203T084256Z\n" + 
        "DESCRIPTION:Testspiel (Freundschaftsspiel) der deutschen Nationalmannschaft\n" + 
        " .\\nGegner und TV-\u00fcbertragungs-Details noch unbekannt.\n" + 
        "LAST-MODIFIED:20161203T084256Z\n" + 
        "LOCATION:RheinEnergieStadion K\u00f6ln. TV-Sender + LiveStream Website noch unbe\n" + 
        " kannt\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland - Testgegner (noch unbekannt)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20171110T111200Z\n" + 
        "DTEND:20171110T121200Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:dcl746sd4jag343bkq6egpqtkg@google.com\n" + 
        "CREATED:20161203T084009Z\n" + 
        "DESCRIPTION:Testspiel (Freundschaftsspiel) der deutschen Nationalmannschaft\n" + 
        " .\\nOrt und TV-\u00fcbertragungs-Details noch unbekannt.\n" + 
        "LAST-MODIFIED:20161203T084056Z\n" + 
        "LOCATION:Stadion in Deutschland. TV-Sender + LiveStream Website noch unbeka\n" + 
        " nnt\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland - Testgegner (noch unbekannt)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20171008T184500Z\n" + 
        "DTEND:20171008T204500Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:3hlitmuasnm6q9a5eggf9f60ak@google.com\n" + 
        "CREATED:20160904T080230Z\n" + 
        "DESCRIPTION:Qualifikationsspiel f\u00fcr die Weltmeisterschaft 2018 in Russland.\n" + 
        " \\nLive Fernseh \u00fcbertragung auf RTL und per Internet Livestream bei http://w\n" + 
        " ww.ran.de/\n" + 
        "LAST-MODIFIED:20161203T083917Z\n" + 
        "LOCATION:Fritz-Walter-Stadion Kaiserslautern. Live im RTL TV.\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland-Aserbaidschan (WM Quali)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170904T184500Z\n" + 
        "DTEND:20170904T204500Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:27eic99tp8rvttbtuv4ul7ddig@google.com\n" + 
        "CREATED:20160904T080327Z\n" + 
        "DESCRIPTION:Qualifikationsspiel f\u00fcr die Weltmeisterschaft 2018 in Russland.\n" + 
        " \\nLive Fernseh \u00fcbertragung auf RTL und per Internet Livestream bei http://w\n" + 
        " ww.ran.de/\n" + 
        "LAST-MODIFIED:20161203T083825Z\n" + 
        "LOCATION:Mercedes-Benz Arena Stuttgart. Live im RTL TV.\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland-Norwegen (WM Quali)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170610T184500Z\n" + 
        "DTEND:20170610T204500Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:653dj7f98ll36ug0rlpde28qog@google.com\n" + 
        "CREATED:20160904T080308Z\n" + 
        "DESCRIPTION:Qualifikationsspiel f\u00fcr die Weltmeisterschaft 2018 in Russland.\n" + 
        " \\nLive Fernseh \u00fcbertragung auf RTL und per Internet Livestream bei http://w\n" + 
        " ww.ran.de/\n" + 
        "LAST-MODIFIED:20161203T082934Z\n" + 
        "LOCATION:Stadion N\u00fcrnberg. Live im RTL TV.\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland-San Marino (WM Quali)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161115T194500Z\n" + 
        "DTEND:20161115T204500Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:0p76hukqsdf6jvbql9kt30g1g8@google.com\n" + 
        "CREATED:20161020T074536Z\n" + 
        "DESCRIPTION:Freundschaftsspiel der Nationalmannschaften Italien gegen Deuts\n" + 
        " chland. Live \u00fcbertragen auf DasErste (Free TV) und per LiveStream bei http:\n" + 
        " //www.daserste.de/live/index.html\n" + 
        "LAST-MODIFIED:20161117T075251Z\n" + 
        "LOCATION:Giuseppe-Meazza-Stadion\\, Mailand. Live \u00fcbertragung in der ARD\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Italien-Deutschland (Testspiel) 0:0\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161111T194500Z\n" + 
        "DTEND:20161111T214500Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:c0l3hod8jvqnet15bs5pfjl1eg@google.com\n" + 
        "CREATED:20160904T080038Z\n" + 
        "DESCRIPTION:Qualifikationsspiel f\u00fcr die Weltmeisterschaft 2018 in Russland.\n" + 
        " \\nLive Fernseh \u00fcbertragung auf RTL und per Internet Livestream bei http://w\n" + 
        " ww.ran.de/\n" + 
        "LAST-MODIFIED:20161113T093726Z\n" + 
        "LOCATION:San Marino. Live im RTL TV.\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:San Marino - Deutschland (WM Quali) 0:8\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161011T184500Z\n" + 
        "DTEND:20161011T204500Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:dftp1r9qdvjb45nvhl44kk0hmg@google.com\n" + 
        "CREATED:20160904T080031Z\n" + 
        "DESCRIPTION:Qualifikationsspiel f\u00fcr die Weltmeisterschaft 2018 in Russland.\n" + 
        " \\nLive Fernseh \u00fcbertragung auf RTL und per Internet Livestream bei http://w\n" + 
        " ww.ran.de/\n" + 
        "LAST-MODIFIED:20161020T073643Z\n" + 
        "LOCATION:Hannover. Live im RTL TV.\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland-Nordirland (WM Quali) 2:0\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161008T184500Z\n" + 
        "DTEND:20161008T204500Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:s4ptl2n4fmda4l6s0l20a5nmgs@google.com\n" + 
        "CREATED:20160904T080021Z\n" + 
        "DESCRIPTION:Qualifikationsspiel f\u00fcr die Weltmeisterschaft 2018 in Russland.\n" + 
        " \\nLive Fernseh \u00fcbertragung auf RTL und per Internet Livestream bei http://w\n" + 
        " ww.ran.de/\n" + 
        "LAST-MODIFIED:20161020T073628Z\n" + 
        "LOCATION:Volksparkstadion Hamburg. Live im RTL TV.\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland-Tschechien (WM Quali) 3:0\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160904T183000Z\n" + 
        "DTEND:20160904T203000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:7dpcknkqf9u79mukmsstjrgeko@google.com\n" + 
        "CREATED:20160904T075732Z\n" + 
        "DESCRIPTION:Qualifikationsspiel f\u00fcr die Weltmeisterschaft 2018 in Russland.\n" + 
        " \\nLive Fernseh \u00fcbertragung auf RTL und per Internet Livestream bei http://w\n" + 
        " ww.ran.de/\n" + 
        "LAST-MODIFIED:20161020T073553Z\n" + 
        "LOCATION:Stadion Ullev\u00e5l Oslo. Live bei RTL TV \u00fcbertragen\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Norwegen-Deutschland (WM Quali) 0:3\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160831T184500Z\n" + 
        "DTEND:20160831T204500Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:d5rovddk8bcn9a7dnmuard4j50@google.com\n" + 
        "CREATED:20160803T180643Z\n" + 
        "DESCRIPTION:Freundschaftsspiel der deutschen Nationmannschaft gegen Finnlan\n" + 
        " d. \\nLive im Fernsehen \u00fcbertragen im ZDF (2. Programm\\, Free TV) und per In\n" + 
        " ternet LiveStream bei www.zdf.de/ZDFmediathek/beitrag/live/1822600/Das-ZDF-\n" + 
        " im-Livestream\\n\n" + 
        "LAST-MODIFIED:20161020T073539Z\n" + 
        "LOCATION:M\u00f6nchengladbach. Live im TV \u00fcbertragen im ZDF\n" + 
        "SEQUENCE:1\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland - Finnland (FSP / Testspiel) 2:0\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20171005T184500Z\n" + 
        "DTEND:20171005T204500Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:jp58dhvm3aa60eu5r25ljon4ac@google.com\n" + 
        "CREATED:20160904T080220Z\n" + 
        "DESCRIPTION:Qualifikationsspiel f\u00fcr die Weltmeisterschaft 2018 in Russland.\n" + 
        " \\nLive Fernseh \u00fcbertragung auf RTL und per Internet Livestream bei http://w\n" + 
        " ww.ran.de/\n" + 
        "LAST-MODIFIED:20160904T080220Z\n" + 
        "LOCATION:Stadion in Nordirland (Belfast\u00df). Live auf RTL TV\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Nordirland-Deutschland (WM Quali)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170326T160000Z\n" + 
        "DTEND:20170326T180000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:1d9v3942a4thjbdq5iio3hef18@google.com\n" + 
        "CREATED:20160904T080129Z\n" + 
        "DESCRIPTION:Qualifikationsspiel f\u00fcr die Weltmeisterschaft 2018 in Russland.\n" + 
        " \\nLive Fernseh \u00fcbertragung auf RTL und per Internet Livestream bei http://w\n" + 
        " ww.ran.de/\n" + 
        "LAST-MODIFIED:20160904T080129Z\n" + 
        "LOCATION:Stadion in Aserbaidschan. Live im RTL TV.\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Aserbaidschan-Deutschland (WM Quali)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160820T203000Z\n" + 
        "DTEND:20160820T223000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:chi6cdpn69j3ib9n6co36b9k64ojeb9o6lhmab9mcoom2e9kcks38e1gcc@google.com\n" + 
        "CREATED:20160819T190829Z\n" + 
        "DESCRIPTION:\u26bd Live-\u00fcbertragung im ersten deutschen Fernsehen (ARD). Olympis\n" + 
        " ches Finale der Herren. Zus\u00e4tzlich im kostenlosen LiveStream: http://rio.zd\n" + 
        " f.de/live/\n" + 
        "LAST-MODIFIED:20160819T190829Z\n" + 
        "LOCATION:Rio de Janeiro - TV-\u00fcbertragung in der ARD\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:\u26bd Deutschland - Brasilien (FINALE)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160817T190000Z\n" + 
        "DTEND:20160817T210000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:fb61s80t9drohvng4q2c6ic434@google.com\n" + 
        "CREATED:20160815T132310Z\n" + 
        "DESCRIPTION:\u26bdHalbfinale beim olympischen Fu\u00dfballturnier in Rio: Deutschland\n" + 
        "  trifft auf Nigeria.\\nDas zweite deutsche Fernsehen \u00fcbertr\u00e4gt live im TV (F\n" + 
        " ree-TV)\\nInternetstream:\\nhttp://rio.zdf.de/live/\n" + 
        "LAST-MODIFIED:20160815T132532Z\n" + 
        "LOCATION:\u26bd-Live\u00fcbertragung auf dem Sender vom zweiten deutschen Fernsehen (\n" + 
        " ZDF). Und auf http://rio.zdf.de/live/\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Olympia \u26bdFu\u00dfball (M) Halbfinale: Deutschland - Nigeria\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160807T190000Z\n" + 
        "DTEND:20160807T210000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:msc1j4psqhahig749rlgu2pi44@google.com\n" + 
        "CREATED:20160804T154257Z\n" + 
        "DESCRIPTION:Olympisches Fu\u00dfball-Turnier mit der deutschen U21-Nationalmanns\n" + 
        " chaft in Brasilien\\nGruppe C\\nLive Fernseh \u00fcbertragung im ZDF (2. Programm)\n" + 
        "  und per Internet LiveStream\n" + 
        "LAST-MODIFIED:20160804T154257Z\n" + 
        "LOCATION:Arena Fonte Nova\\, Salvador\\, Brasilien. Live ausgestrahlt im ZDF\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:U21 Deutschland - U21 S\u00fcdkorea (Olympia)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160804T200000Z\n" + 
        "DTEND:20160804T213000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:k3rhcel7k5uc96cjann1l94u1o@google.com\n" + 
        "CREATED:20160804T154012Z\n" + 
        "DESCRIPTION:Olympisches Fu\u00dfball-Turnier mit der deutschen U21-Nationalmanns\n" + 
        " chaft in Brasilien\\nGruppe C\\nLive Fernseh \u00fcbertragung im ZDF (Free TV) und\n" + 
        "  per Internet LiveStream in der ZDF-Mediathek\n" + 
        "LAST-MODIFIED:20160804T154012Z\n" + 
        "LOCATION:Arena Fonte Nova\\, Salvador\\, Brasilien. Live im ZDF \u00fcbertragen\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:U21 Mexico - U21-Deutschland (Olympia)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160710T190000Z\n" + 
        "DTEND:20160710T210000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:j18ok6kh81cmegkeqfbdms61os@google.com\n" + 
        "CREATED:20160623T191909Z\n" + 
        "DESCRIPTION:Sieger Halbfinale 1 (Portugal) gegen Sieger Halbfinale 2\\nFinal\n" + 
        " e der Europameisterschaft 2016 (Frankreich).\\nPortugal ist der neue Europam\n" + 
        " eister.\\n\n" + 
        "LAST-MODIFIED:20160711T062019Z\n" + 
        "LOCATION:Saint-Denis. Live\u00fcbertragung im Free TV (ARD\\, 1. Programm)\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:EM-Endspiel: Portugal - Frankreich. 1:0 (n.V.)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160707T190000Z\n" + 
        "DTEND:20160707T210000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:pfdnk1k5q04t1q1uv40egbmri0@google.com\n" + 
        "CREATED:20160623T191727Z\n" + 
        "DESCRIPTION:Halbfinale 2 (von 2) der EM 2016\\nSieger Viertelfinale 3 (Deuts\n" + 
        " chland) gegen Sieger Viertelfinale 4 (Frankreich)\\nDas Halbfinale der deuts\n" + 
        " chen Nationalmannschaft.\\nLive im Fernsehen \u00fcbertragen im ZDF (Free TV) und\n" + 
        "  als legaler Internet LiveStream in der ZDF Mediathek bei zdf.de/ZDFmediath\n" + 
        " ek/beitrag/live/1822600/Das-ZDF-im-Livestream\n" + 
        "LAST-MODIFIED:20160707T204657Z\n" + 
        "LOCATION:Marseille. Live\u00fcbertragung im ZDF und per Internet LiveStream\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland - Frankreich (HF2). 0:2\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160702T190000Z\n" + 
        "DTEND:20160702T210000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:l2mo51eu6pt0pfo96o9pqqud4o@google.com\n" + 
        "CREATED:20160621T211251Z\n" + 
        "DESCRIPTION:Viertelfinale 3 (von 4) bei der EM 2016\\nSieger Achtelfinale 5 \n" + 
        " (Deutschland) gegen Sieger Achtelfinale 7 (Italien)\\nDas Viertelfinale mit \n" + 
        " der deutschen Nationalmannschaft..\\n\n" + 
        "LAST-MODIFIED:20160702T214909Z\n" + 
        "LOCATION:Bordeaux. Live \u00fcbertragung in der ARD (1. Programm) und als LiveSt\n" + 
        " ream\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland - Italien (VF3). 6:5. n.E. 1:1 (n.V.)\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160626T160000Z\n" + 
        "DTEND:20160626T180000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:ds0kf4ojq585c3ffmt1mpl3ims@google.com\n" + 
        "CREATED:20160609T094549Z\n" + 
        "DESCRIPTION:Achtelfinale EM 2016 (K.O. Phase)\\nAchtelfinale 5 (von 8)\n" + 
        "LAST-MODIFIED:20160626T175057Z\n" + 
        "LOCATION:Lille. Kann man live im ZDF zu sehen oder von zdf.de streamen\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland - Slowakei. 3:0\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160621T160000Z\n" + 
        "DTEND:20160621T180000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:36in24de3svsi0gil94bl4d6fo@google.com\n" + 
        "CREATED:20160528T145752Z\n" + 
        "DESCRIPTION:Gruppenphase der EM 2016.\\nGruppe C ('deutsche Gruppe'\\n3. Spie\n" + 
        " l der deutschen Nationalmannschaft\\nZeitgleich mit Ukraine - Polen\\n\n" + 
        "LAST-MODIFIED:20160621T174747Z\n" + 
        "LOCATION:Paris\\, Frankreich. Live-TV-\u00fcbertragung auf DasErste und per ARD-L\n" + 
        " iveStream\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Nordirland - Deutschland (C). 0:1\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160616T190000Z\n" + 
        "DTEND:20160616T210000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:2v1b7v7u51anf8ifmj2ju1r138@google.com\n" + 
        "CREATED:20160528T145732Z\n" + 
        "DESCRIPTION:Gruppenphase der EM\\, Gruppe C ('deutsche Gruppe')\\n2. Spiel de\n" + 
        " r deutschen Nationalmannschaft bei der Euro 2016\n" + 
        "LAST-MODIFIED:20160618T092240Z\n" + 
        "LOCATION:Saint-Denis. Live \u00fcbertragung im ZDF\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland-Polen (C). 0:0\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160612T190000Z\n" + 
        "DTEND:20160612T210000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:tcvgrrf9s9b86317vagqrig0ls@google.com\n" + 
        "CREATED:20160528T145721Z\n" + 
        "DESCRIPTION:Gruppenphase der EM\\, Gruppe C (Deutsche Gruppe)\\n1. Spiel der \n" + 
        " deutschen Nationalmannschaft\n" + 
        "LAST-MODIFIED:20160614T100659Z\n" + 
        "LOCATION:Lille. Live \u00fcbertragen auf DasErste (Free TV)\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland-Ukraine (C) 2:0\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160604T160000Z\n" + 
        "DTEND:20160604T180000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:n0ijg1eqjc3oot2htcte9d3au0@google.com\n" + 
        "CREATED:20160528T145709Z\n" + 
        "DESCRIPTION:L\u00e4nderspiel (Freundschaftsspiel) der deutschen Nationalmannscha\n" + 
        " ft 'Die Mannschaft' vor der Europameisterschaft 2016.\\nLive Fernseh\u00fcbertrag\n" + 
        " ung im ZDF (Free TV) und per gratis Internet LiveStream bei zdf.de/ZDFmedia\n" + 
        " thek/beitrag/live/1822600/Das-ZDF-im-Livestream\n" + 
        "LAST-MODIFIED:20160605T085128Z\n" + 
        "LOCATION:Veltins-Arena\\, Gelsenkirchen. Live-\u00fcbertragung im ZDF (2. Program\n" + 
        " m)\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland - Ungarn (Testspiel) 2:0\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160529T154500Z\n" + 
        "DTEND:20160529T174500Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:uf251cb8l2ao8dsmi2qfriuadg@google.com\n" + 
        "CREATED:20160528T145700Z\n" + 
        "DESCRIPTION:L\u00e4nderspiel (Testspiel) der deutschen Nationalmannschaft (Herre\n" + 
        " n) vor dem EM 2016\\nLive im Fernsehen \u00fcbertragen auf DasErste (ARD\\, Free T\n" + 
        " v) und kostenlosem\\, legalen Internet LiveStream via http://www.daserste.de\n" + 
        " /live/index.html\\n\n" + 
        "LAST-MODIFIED:20160530T064819Z\n" + 
        "LOCATION:WWK Arena\\, Augsburg. Live \u00fcbertragung in der ARD (1. Programm)\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland - Slowakei (Testspiel) 1:3\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "END:VCALENDAR\n" + 
        "";

    public static final String FEED_WITH_CHANGE_EXCEPTION = "BEGIN:VCALENDAR\n" + 
        "VERSION:2.0\n" + 
        "PRODID:-//Open-Xchange//7.10.0-Rev1//EN\n" + 
        "BEGIN:VTIMEZONE\n" + 
        "TZID:Europe/Berlin\n" + 
        "TZURL:http://tzurl.org/zoneinfo-outlook/Europe/Berlin\n" + 
        "X-LIC-LOCATION:Europe/Berlin\n" + 
        "BEGIN:DAYLIGHT\n" + 
        "TZOFFSETFROM:+0100\n" + 
        "TZOFFSETTO:+0200\n" + 
        "TZNAME:CEST\n" + 
        "DTSTART:19700329T020000\n" + 
        "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n" + 
        "END:DAYLIGHT\n" + 
        "BEGIN:STANDARD\n" + 
        "TZOFFSETFROM:+0200\n" + 
        "TZOFFSETTO:+0100\n" + 
        "TZNAME:CET\n" + 
        "DTSTART:19701025T030000\n" + 
        "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n" + 
        "END:STANDARD\n" + 
        "END:VTIMEZONE\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTAMP:20170802T115927Z\n" + 
        "ATTENDEE;CN=\"caesar, caesar\";PARTSTAT=ACCEPTED;CUTYPE=INDIVIDUAL:mailto:cae\n" + 
        " sar@context7.oxoe.int\n" + 
        "ATTENDEE;CN=\"dora, dora\";PARTSTAT=NEEDS-ACTION;CUTYPE=INDIVIDUAL:mailto:dor\n" + 
        " a@context7.oxoe.int\n" + 
        "CLASS:PUBLIC\n" + 
        "CREATED:20170802T115927Z\n" + 
        "DTEND;TZID=Europe/Berlin:20140901T110000\n" + 
        "DTSTART;TZID=Europe/Berlin:20140901T100000\n" + 
        "LAST-MODIFIED:20170802T115927Z\n" + 
        "ORGANIZER;CN=\"caesar, caesar\";SENT-BY=\"mailto:dora@context7.oxoe.int\":mailt\n" + 
        " o:caesar@context7.oxoe.int\n" + 
        "SEQUENCE:0\n" + 
        "SUMMARY:testOtherPrivateToThirdPartySubfolderWithParticipants\n" + 
        "TRANSP:OPAQUE\n" + 
        "UID:8faae68e-540d-45d7-b50f-a920e0ebf14d\n" + 
        "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTAMP:20170802T120022Z\n" + 
        "ATTENDEE;CN=\"dora, dora\";PARTSTAT=NEEDS-ACTION;CUTYPE=INDIVIDUAL:mailto:dor\n" + 
        " a@context7.oxoe.int\n" + 
        "ATTENDEE;CN=\"emil, emil\";PARTSTAT=ACCEPTED;CUTYPE=INDIVIDUAL:mailto:emil@co\n" + 
        " ntext7.oxoe.int\n" + 
        "CLASS:PUBLIC\n" + 
        "CREATED:20170802T120021Z\n" + 
        "DTEND;TZID=Europe/Berlin:20140901T110000\n" + 
        "DTSTART;TZID=Europe/Berlin:20140901T100000\n" + 
        "LAST-MODIFIED:20170802T120022Z\n" + 
        "ORGANIZER;CN=\"emil, emil\";SENT-BY=\"mailto:dora@context7.oxoe.int\":mailto:em\n" + 
        " il@context7.oxoe.int\n" + 
        "SEQUENCE:0\n" + 
        "SUMMARY:testOtherPrivateToOtherSubfolder\n" + 
        "TRANSP:OPAQUE\n" + 
        "UID:7fb109aa-f885-47ad-97bd-f221c60671d9\n" + 
        "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTAMP:20170802T120026Z\n" + 
        "ATTENDEE;CN=\"berta, berta\";PARTSTAT=ACCEPTED;CUTYPE=INDIVIDUAL:mailto:berta\n" + 
        " @context7.oxoe.int\n" + 
        "ATTENDEE;CN=\"dora, dora\";PARTSTAT=ACCEPTED;CUTYPE=INDIVIDUAL:mailto:dora@co\n" + 
        " ntext7.oxoe.int\n" + 
        "CLASS:PUBLIC\n" + 
        "CREATED:20170802T120026Z\n" + 
        "DTEND;TZID=Europe/Berlin:20170802T150000\n" + 
        "DTSTART;TZID=Europe/Berlin:20170802T140000\n" + 
        "LAST-MODIFIED:20170802T120026Z\n" + 
        "ORGANIZER;CN=\"dora, dora\":mailto:dora@context7.oxoe.int\n" + 
        "SEQUENCE:0\n" + 
        "SUMMARY:Appointment for bug 16194\n" + 
        "TRANSP:OPAQUE\n" + 
        "UID:cb2ac160-9aaa-4dba-a5c8-ce309c079c0f\n" + 
        "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTAMP:20170802T120033Z\n" + 
        "ATTENDEE;CN=Standard group;PARTSTAT=ACCEPTED;CUTYPE=GROUP:urn:uuid:00000007\n" + 
        " -0000-0001-00fc-c0e11e000002\n" + 
        "ATTENDEE;CN=Context Administrator;PARTSTAT=NEEDS-ACTION;CUTYPE=INDIVIDUAL;M\n" + 
        " EMBER=\"urn:uuid:00000007-0000-0001-00fc-c0e11e000002\":mailto:oxadmin@conte\n" + 
        " xt7.oxoe.int\n" + 
        "ATTENDEE;CN=\"anton, anton\";PARTSTAT=NEEDS-ACTION;CUTYPE=INDIVIDUAL;MEMBER=\"\n" + 
        " urn:uuid:00000007-0000-0001-00fc-c0e11e000002\":mailto:anton@context7.oxoe.\n" + 
        " int\n" + 
        "ATTENDEE;CN=\"berta, berta\";PARTSTAT=ACCEPTED;CUTYPE=INDIVIDUAL:mailto:berta\n" + 
        " @context7.oxoe.int\n" + 
        "ATTENDEE;CN=\"caesar, caesar\";PARTSTAT=ACCEPTED;CUTYPE=INDIVIDUAL;MEMBER=\"ur\n" + 
        " n:uuid:00000007-0000-0001-00fc-c0e11e000002\":mailto:caesar@context7.oxoe.i\n" + 
        " nt\n" + 
        "ATTENDEE;CN=\"dora, dora\";PARTSTAT=NEEDS-ACTION;CUTYPE=INDIVIDUAL;MEMBER=\"ur\n" + 
        " n:uuid:00000007-0000-0001-00fc-c0e11e000002\":mailto:dora@context7.oxoe.int\n" + 
        " \n" + 
        "ATTENDEE;CN=\"no-reply, no-reply\";PARTSTAT=NEEDS-ACTION;CUTYPE=INDIVIDUAL;ME\n" + 
        " MBER=\"urn:uuid:00000007-0000-0001-00fc-c0e11e000002\":mailto:no-reply@conte\n" + 
        " xt7.oxoe.int\n" + 
        "CLASS:PUBLIC\n" + 
        "CREATED:20170802T120032Z\n" + 
        "DTEND;TZID=Europe/Berlin:20151101T100000\n" + 
        "DTSTART;TZID=Europe/Berlin:20151101T090000\n" + 
        "LAST-MODIFIED:20170802T120033Z\n" + 
        "ORGANIZER;CN=\"berta, berta\":mailto:berta@context7.oxoe.int\n" + 
        "SEQUENCE:0\n" + 
        "SUMMARY:Bug41794Test\n" + 
        "TRANSP:OPAQUE\n" + 
        "UID:5c1b4a48-4e6c-462d-9c71-66d51734ea96\n" + 
        "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTAMP:20170803T113210Z\n" + 
        "ATTENDEE;CN=\"dora, dora\";PARTSTAT=ACCEPTED;CUTYPE=INDIVIDUAL:mailto:dora@co\n" + 
        " ntext7.oxoe.int\n" + 
        "CLASS:PUBLIC\n" + 
        "CREATED:20170803T113210Z\n" + 
        "DTEND;TZID=Europe/Berlin:20140901T110000\n" + 
        "DTSTART;TZID=Europe/Berlin:20140901T100000\n" + 
        "LAST-MODIFIED:20170803T113210Z\n" + 
        "ORGANIZER;CN=\"dora, dora\";SENT-BY=\"mailto:anton@context7.oxoe.int\":mailto:d\n" + 
        " ora@context7.oxoe.int\n" + 
        "SEQUENCE:0\n" + 
        "SUMMARY:testOtherSubfolderToThirdPartySubfolder\n" + 
        "TRANSP:OPAQUE\n" + 
        "UID:0112519a-2f81-4afd-a8eb-0ecb78d2ca7b\n" + 
        "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTAMP:20170803T114556Z\n" + 
        "ATTENDEE;CN=Standard group;PARTSTAT=ACCEPTED;CUTYPE=GROUP:urn:uuid:00000007\n" + 
        " -0000-0001-00fc-c0e11e000002\n" + 
        "ATTENDEE;CN=Context Administrator;PARTSTAT=NEEDS-ACTION;CUTYPE=INDIVIDUAL;M\n" + 
        " EMBER=\"urn:uuid:00000007-0000-0001-00fc-c0e11e000002\":mailto:oxadmin@conte\n" + 
        " xt7.oxoe.int\n" + 
        "ATTENDEE;CN=\"berta, berta\";PARTSTAT=NEEDS-ACTION;CUTYPE=INDIVIDUAL:mailto:b\n" + 
        " erta@context7.oxoe.int\n" + 
        "ATTENDEE;CN=\"caesar, caesar\";PARTSTAT=NEEDS-ACTION;CUTYPE=INDIVIDUAL;MEMBER\n" + 
        " =\"urn:uuid:00000007-0000-0001-00fc-c0e11e000002\":mailto:caesar@context7.ox\n" + 
        " oe.int\n" + 
        "ATTENDEE;CN=\"dora, dora\";PARTSTAT=NEEDS-ACTION;CUTYPE=INDIVIDUAL;MEMBER=\"ur\n" + 
        " n:uuid:00000007-0000-0001-00fc-c0e11e000002\":mailto:dora@context7.oxoe.int\n" + 
        " \n" + 
        "ATTENDEE;CN=\"emil, emil\";PARTSTAT=NEEDS-ACTION;CUTYPE=INDIVIDUAL;MEMBER=\"ur\n" + 
        " n:uuid:00000007-0000-0001-00fc-c0e11e000002\":mailto:emil@context7.oxoe.int\n" + 
        " \n" + 
        "ATTENDEE;CN=\"no-reply, no-reply\";PARTSTAT=NEEDS-ACTION;CUTYPE=INDIVIDUAL;ME\n" + 
        " MBER=\"urn:uuid:00000007-0000-0001-00fc-c0e11e000002\":mailto:no-reply@conte\n" + 
        " xt7.oxoe.int\n" + 
        "ATTENDEE;CN=test-resource-1;PARTSTAT=ACCEPTED;CUTYPE=RESOURCE:urn:uuid:0000\n" + 
        " 0007-0000-0009-00fc-c0e11e000003\n" + 
        "CATEGORIES:testcat1,testcat2,testcat3\n" + 
        "CLASS:PUBLIC\n" + 
        "CREATED:20170803T114556Z\n" + 
        "DESCRIPTION:note\n" + 
        "DTEND;VALUE=DATE:20170804\n" + 
        "DTSTART;VALUE=DATE:20170803\n" + 
        "LAST-MODIFIED:20170803T114556Z\n" + 
        "LOCATION:Location\n" + 
        "ORGANIZER:mailto:someone.else@example.com\n" + 
        "SEQUENCE:0\n" + 
        "SUMMARY:testListWithAllFields\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "UID:1234567890abcdef1501760756455\n" + 
        "X-MICROSOFT-CDO-ALLDAYEVENT:TRUE\n" + 
        "X-MICROSOFT-CDO-BUSYSTATUS:FREE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTAMP:20170803T114613Z\n" + 
        "ATTENDEE;CN=\"caesar, caesar\";PARTSTAT=ACCEPTED;CUTYPE=INDIVIDUAL:mailto:cae\n" + 
        " sar@context7.oxoe.int\n" + 
        "ATTENDEE;CN=\"dora, dora\";PARTSTAT=NEEDS-ACTION;CUTYPE=INDIVIDUAL:mailto:dor\n" + 
        " a@context7.oxoe.int\n" + 
        "CLASS:PUBLIC\n" + 
        "CREATED:20170803T114612Z\n" + 
        "DTEND;TZID=Europe/Berlin:20140901T110000\n" + 
        "DTSTART;TZID=Europe/Berlin:20140901T100000\n" + 
        "LAST-MODIFIED:20170803T114613Z\n" + 
        "ORGANIZER;CN=\"caesar, caesar\";SENT-BY=\"mailto:emil@context7.oxoe.int\":mailt\n" + 
        " o:caesar@context7.oxoe.int\n" + 
        "SEQUENCE:0\n" + 
        "SUMMARY:testOtherPrivateToOwnPrivateWithParticipants\n" + 
        "TRANSP:OPAQUE\n" + 
        "UID:1134966a-a95b-44ef-bade-4270298b9bd8\n" + 
        "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTAMP:20180125T115944Z\n" + 
        "ATTENDEE;CN=\"dora, dora\";PARTSTAT=ACCEPTED;CUTYPE=INDIVIDUAL:mailto:dora@co\n" + 
        " ntext7.oxoe.int\n" + 
        "CLASS:PUBLIC\n" + 
        "CREATED:20180125T115835Z\n" + 
        "DTEND;TZID=Europe/Berlin:20180123T130000\n" + 
        "DTSTART;TZID=Europe/Berlin:20180123T123000\n" + 
        "LAST-MODIFIED:20180125T115944Z\n" + 
        "ORGANIZER;CN=\"dora, dora\":mailto:dora@context7.oxoe.int\n" + 
        "RRULE:FREQ=WEEKLY;COUNT=4;BYDAY=TU\n" + 
        "SEQUENCE:0\n" + 
        "SUMMARY:Test-Series\n" + 
        "TRANSP:OPAQUE\n" + 
        "UID:da554dcc-2c4b-4e11-a360-e3b2d3ca0408\n" + 
        "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n" + 
        "BEGIN:VALARM\n" + 
        "TRIGGER:-PT15M\n" + 
        "UID:0f9c2d0e-a005-4c9b-850e-fdb15531996a\n" + 
        "ACTION:DISPLAY\n" + 
        "DESCRIPTION:Reminder\n" + 
        "END:VALARM\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTAMP:20180125T115934Z\n" + 
        "ATTENDEE;CN=\"dora, dora\";PARTSTAT=ACCEPTED;CUTYPE=INDIVIDUAL:mailto:dora@co\n" + 
        " ntext7.oxoe.int\n" + 
        "CLASS:PUBLIC\n" + 
        "CREATED:20180125T115934Z\n" + 
        "DTEND;TZID=Europe/Berlin:20180131T130000\n" + 
        "DTSTART;TZID=Europe/Berlin:20180131T123000\n" + 
        "LAST-MODIFIED:20180125T115934Z\n" + 
        "ORGANIZER;CN=\"dora, dora\":mailto:dora@context7.oxoe.int\n" + 
        "RECURRENCE-ID:20180130T113000Z\n" + 
        "RRULE:FREQ=WEEKLY;COUNT=4;BYDAY=TU\n" + 
        "SEQUENCE:1\n" + 
        "SUMMARY:Test-Series - shifted day\n" + 
        "TRANSP:OPAQUE\n" + 
        "UID:da554dcc-2c4b-4e11-a360-e3b2d3ca0408\n" + 
        "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n" + 
        "BEGIN:VALARM\n" + 
        "TRIGGER:-PT15M\n" + 
        "UID:a5733b0f-b94c-481f-b3f0-1ba76949dd72\n" + 
        "ACTION:DISPLAY\n" + 
        "DESCRIPTION:Reminder\n" + 
        "END:VALARM\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTAMP:20180125T115955Z\n" + 
        "ATTENDEE;CN=\"dora, dora\";PARTSTAT=ACCEPTED;CUTYPE=INDIVIDUAL:mailto:dora@co\n" + 
        " ntext7.oxoe.int\n" + 
        "CLASS:PUBLIC\n" + 
        "CREATED:20180125T115944Z\n" + 
        "DTEND;TZID=Europe/Berlin:20180206T110000\n" + 
        "DTSTART;TZID=Europe/Berlin:20180206T103000\n" + 
        "LAST-MODIFIED:20180125T115955Z\n" + 
        "ORGANIZER;CN=\"dora, dora\":mailto:dora@context7.oxoe.int\n" + 
        "RECURRENCE-ID:20180206T113000Z\n" + 
        "RRULE:FREQ=WEEKLY;COUNT=4;BYDAY=TU\n" + 
        "SEQUENCE:2\n" + 
        "SUMMARY:Test-Series - shifted time\n" + 
        "TRANSP:OPAQUE\n" + 
        "UID:da554dcc-2c4b-4e11-a360-e3b2d3ca0408\n" + 
        "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n" + 
        "BEGIN:VALARM\n" + 
        "TRIGGER:-PT15M\n" + 
        "UID:d13da3c9-7ea3-48d7-a468-9f985e6edfc8\n" + 
        "ACTION:DISPLAY\n" + 
        "DESCRIPTION:Reminder\n" + 
        "END:VALARM\n" + 
        "END:VEVENT\n" + 
        "END:VCALENDAR\n" + 
        "";

    // contains 102 events
    public static final String RESPONSE_WITH_ADDITIONAL_PROPERTIES = "BEGIN:VCALENDAR\n" + 
        "PRODID:-//Google Inc//Google Calendar 70.9054//EN\n" + 
        "VERSION:2.0\n" + 
        "CALSCALE:GREGORIAN\n" + 
        "METHOD:PUBLISH\n" + 
        "REFRESH-INTERVAL;VALUE=DURATION:P1W\n" + 
        "X-WR-CALNAME:FC Schalke 04\n" + 
        "X-WR-TIMEZONE:Europe/Berlin\n" + 
        "X-WR-CALDESC:Alle Spiele von FC Schalke 04\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170814T163000Z\n" + 
        "DTEND:20170814T183000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:qhsvmkcg7gec4e0db581frbldk@google.com\n" + 
        "CREATED:20170629T140203Z\n" + 
        "DESCRIPTION:DFB-Pokal\\, round 1\n" + 
        "LAST-MODIFIED:20170914T183628Z\n" + 
        "LOCATION:\n" + 
        "SEQUENCE:9\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:BFC Dynamo - FC Schalke 04 (0:2)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161026T184500Z\n" + 
        "DTEND:20161026T204500Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:oulqks617srop7lvsbgagnig2o@google.com\n" + 
        "CREATED:20160914T222128Z\n" + 
        "DESCRIPTION:DFB-Pokal\\, round 2\n" + 
        "LAST-MODIFIED:20170802T154846Z\n" + 
        "LOCATION:Max Morlock Stadion\\, N\u00fcrnberg\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:1. FC N\u00fcrnberg - FC Schalke 04 (2:3)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170910T160000Z\n" + 
        "DTEND:20170910T180000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:v0bdvib44p2dpj4n4sghbro3mk@google.com\n" + 
        "CREATED:20170629T140218Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 3. matchday \n" + 
        "LAST-MODIFIED:20170711T140407Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - VfB Stuttgart\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170827T160000Z\n" + 
        "DTEND:20170827T180000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:dhljnanst91u7d9ib2v2dqbn2k@google.com\n" + 
        "CREATED:20170629T140217Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 2. matchday \n" + 
        "LAST-MODIFIED:20170711T140401Z\n" + 
        "LOCATION:HDI-Arena\\, Hannover!TOlll!!!\n" + 
        "SEQUENCE:2\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Hannover 96 - FC Schalke 04\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170630T184500Z\n" + 
        "DTEND:20170630T204500Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:aps3hi7lokj0o6oknj1ijk9pf4@google.com\n" + 
        "CREATED:20170628T063053Z\n" + 
        "DESCRIPTION:Endspiel U21-Europameisterschaft.\\nU21 Nationalmannschaften Deu\n" + 
        " tschland gegen Schweden \\nLive zu sehen im ZDF (2. Programm\\, Free TV) und \n" + 
        " per gratis LiveStream bei https://www.zdf.de/live-tv\n" + 
        "LAST-MODIFIED:20170702T152610Z\n" + 
        "LOCATION:Krakau\\, Polen. Live-\u00fcbertragung im ZDF und per LiveStream bei zdf\n" + 
        " .de\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:U21-EM Finale: Deutschland-Spanien 1:0\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170629T180000Z\n" + 
        "DTEND:20170629T200000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:75hjsi887k9de9n6gfotv5epgc@google.com\n" + 
        "CREATED:20170625T170211Z\n" + 
        "DESCRIPTION:\u26bd  Confed Cup 2017 in Russland\\, 2. Halbfinale\\nLive-\u00fcbertragun\n" + 
        " g in der ARD (1. Programm\\, FREE) und als Free-LiveStream bei daserste.de\n" + 
        "LAST-MODIFIED:20170629T200553Z\n" + 
        "LOCATION:Sotschi\\, Russland. Live im ARD TV und ARD Stream\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Deutschland - Mexiko (Halbfinale Confed Cup) 4:1\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170627T160000Z\n" + 
        "DTEND:20170627T180000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:4l2pshhim64sm7bhpecibn6peo@google.com\n" + 
        "CREATED:20170626T061912Z\n" + 
        "DESCRIPTION:\u26bd U21-Europameisterschaft aus Polen: Halbfinale\\nDie ARD (DasEr\n" + 
        " ste) \u00fcbertr\u00e4gt live im FreeTV - zus\u00e4tzlich gibt es einen Livestream: Dasers\n" + 
        " te.de\n" + 
        "LAST-MODIFIED:20170628T204752Z\n" + 
        "LOCATION:Tychy\\, Polen. TV-\u00fcbertragung im 1. Programm + InternetStream\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:\u26bd U21-EM-Halbfinale: Deutschland - England 4:3 i.E.\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170622T180000Z\n" + 
        "DTEND:20170622T200000Z\n" + 
        "DTSTAMP:20170817T144059Z\n" + 
        "UID:1ntsjm81ci87tflpe5mb20gmac@google.com\n" + 
        "CREATED:20170616T055647Z\n" + 
        "DESCRIPTION:Confed Cup 2017 in Russland\\, Gruppenphase\\nDas L\u00e4nderspiel Deu\n" + 
        " tschland-Chile im Confed Cup wird live im Fernsehen \u00fcbertragen. Das Erste z\n" + 
        " eigt diese Partie live im Free-TV und im Internet in der Mediathek.\n" + 
        "LAST-MODIFIED:20170622T202449Z\n" + 
        "LOCATION:Kazan\\, Russland. TV-\u00fcbertragung in der ARD\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:\u26bd Deutschland-Chile (Confed Cup) 1:1\n" + 
        "TRANSP:OPAQUE\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170819T163000Z\n" + 
        "DTEND:20170819T183000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:8hsqf3vpiresfr1il8vi5h6tr4@google.com\n" + 
        "CREATED:20170629T140212Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 1. matchday \n" + 
        "LAST-MODIFIED:20170711T140352Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - RB Leipzig\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20171125T111200Z\n" + 
        "DTEND:20171125T131200Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:am5cg0l5csdoco6pikkseq88rs@google.com\n" + 
        "CREATED:20170629T140252Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 13. matchday \\nAchtung: noch nicht terminiert.\\\n" + 
        " nTo be announced.\n" + 
        "LAST-MODIFIED:20170629T140252Z\n" + 
        "LOCATION:Signal-Iduna-Park\\, Dortmund\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Borussia Dortmund - FC Schalke 04 (T.B.A.)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20171118T111200Z\n" + 
        "DTEND:20171118T131200Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:bcrfkrlkgdqps4m85c7qltbksg@google.com\n" + 
        "CREATED:20170629T140247Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 12. matchday \\nAchtung: noch nicht terminiert.\\\n" + 
        " nTo be announced.\n" + 
        "LAST-MODIFIED:20170629T140247Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Hamburger SV (T.B.A.)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20171104T111200Z\n" + 
        "DTEND:20171104T131200Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:rdrj9ei72a2m11ldtdvr3ean0c@google.com\n" + 
        "CREATED:20140625T012301Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 11. matchday \\nAchtung: noch nicht terminiert.\\\n" + 
        " nTo be announced.\n" + 
        "LAST-MODIFIED:20170629T140246Z\n" + 
        "LOCATION:Schwarzwaldstadion\\, Freiburg\n" + 
        "SEQUENCE:8\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:SC Freiburg - FC Schalke 04 (T.B.A.)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20171028T101200Z\n" + 
        "DTEND:20171028T121200Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:1ppjg3i8263cp445mbvbbccno0@google.com\n" + 
        "CREATED:20170629T140244Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 10. matchday \\nAchtung: noch nicht terminiert.\\\n" + 
        " nTo be announced.\n" + 
        "LAST-MODIFIED:20170629T140244Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - VfL Wolfsburg (T.B.A.)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20171021T101200Z\n" + 
        "DTEND:20171021T121200Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:1plioi1svkqgbu3m01qd007uic@google.com\n" + 
        "CREATED:20170629T140239Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 9. matchday \\nAchtung: noch nicht terminiert.\\n\n" + 
        " To be announced.\n" + 
        "LAST-MODIFIED:20170629T140239Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - 1. FSV Mainz 05 (T.B.A.)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20171014T101200Z\n" + 
        "DTEND:20171014T121200Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:itl4ehr2o7ehq12ruch68k2jvk@google.com\n" + 
        "CREATED:20170629T140238Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 8. matchday \\nAchtung: noch nicht terminiert.\\n\n" + 
        " To be announced.\n" + 
        "LAST-MODIFIED:20170629T140238Z\n" + 
        "LOCATION:Olympiastadion\\, Berlin\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Hertha BSC - FC Schalke 04 (T.B.A.)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170930T101200Z\n" + 
        "DTEND:20170930T121200Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:gm19ash7k45klk8a5u0nkqdm9g@google.com\n" + 
        "CREATED:20170629T140233Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 7. matchday \\nAchtung: noch nicht terminiert.\\n\n" + 
        " To be announced.\n" + 
        "LAST-MODIFIED:20170629T140233Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:0\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Bayer 04 Leverkusen (T.B.A.)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170520T133000Z\n" + 
        "DTEND:20170520T153000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:lao67mi5n3lsqp2gcui6despho@google.com\n" + 
        "CREATED:20160629T214546Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 34. matchday \n" + 
        "LAST-MODIFIED:20170520T153716Z\n" + 
        "LOCATION:Audi-Sportpark\\, Ingolstadt\n" + 
        "SEQUENCE:2\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Ingolstadt 04 - FC Schalke 04 (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170513T133000Z\n" + 
        "DTEND:20170513T153000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:40pvlch1ha0ru0qcqiso2n41i4@google.com\n" + 
        "CREATED:20160629T214539Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 33. matchday \n" + 
        "LAST-MODIFIED:20170513T153825Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Hamburger SV (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170507T153000Z\n" + 
        "DTEND:20170507T173000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:is623r8t942v9r3drsimtrkb50@google.com\n" + 
        "CREATED:20160629T214536Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 32. matchday \n" + 
        "LAST-MODIFIED:20170507T173604Z\n" + 
        "LOCATION:Schwarzwaldstadion\\, Freiburg\n" + 
        "SEQUENCE:4\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:SC Freiburg - FC Schalke 04 (2:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170428T183000Z\n" + 
        "DTEND:20170428T203000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:1a07d8l4mhsdp00djokilt2eh4@google.com\n" + 
        "CREATED:20160629T214533Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 31. matchday \n" + 
        "LAST-MODIFIED:20170428T203627Z\n" + 
        "LOCATION:BayArena\\, Leverkusen\n" + 
        "SEQUENCE:4\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Bayer 04 Leverkusen - FC Schalke 04 (1:4)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170423T153000Z\n" + 
        "DTEND:20170423T173000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:853ccp88b87e98pf2r5cj4avv8@google.com\n" + 
        "CREATED:20160712T211405Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 30. matchday \n" + 
        "LAST-MODIFIED:20170423T173611Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - RB Leipzig (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170420T190500Z\n" + 
        "DTEND:20170420T210500Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:2ijam6j2k2nufe6bl1oirifbhg@google.com\n" + 
        "CREATED:20170319T113639Z\n" + 
        "DESCRIPTION:Europa League\\, quarter-final\n" + 
        "LAST-MODIFIED:20170422T212413Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Ajax Amsterdam (n.V. 3:2)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170416T153000Z\n" + 
        "DTEND:20170416T173000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:7a5i29i01u15fcf7gc8b25cu0g@google.com\n" + 
        "CREATED:20160629T214523Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 29. matchday \n" + 
        "LAST-MODIFIED:20170416T173602Z\n" + 
        "LOCATION:Jonathan-Heimes-Stadion am B\u00f6llenfalltor\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:SV Darmstadt 98 - FC Schalke 04 (2:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170413T190500Z\n" + 
        "DTEND:20170413T210500Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:lllper6l1ksuvreva2490ghr24@google.com\n" + 
        "CREATED:20170319T113632Z\n" + 
        "DESCRIPTION:Europa League\\, quarter-final\n" + 
        "LAST-MODIFIED:20170413T211241Z\n" + 
        "LOCATION:Amsterdam Arena\\, Amsterdam\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Ajax Amsterdam - FC Schalke 04 (2:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170408T133000Z\n" + 
        "DTEND:20170408T153000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:0s7d62g6pcnvdiuk94olsldgps@google.com\n" + 
        "CREATED:20160629T214515Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 28. matchday \n" + 
        "LAST-MODIFIED:20170408T153720Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - VfL Wolfsburg (4:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170404T180000Z\n" + 
        "DTEND:20170404T200000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:g844ffcbnsohdqdd0dcrkvlth8@google.com\n" + 
        "CREATED:20160629T214514Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 27. matchday \n" + 
        "LAST-MODIFIED:20170404T200004Z\n" + 
        "LOCATION:Weserstadion\\, Bremen\n" + 
        "SEQUENCE:4\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Werder Bremen - FC Schalke 04 (3:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170401T133000Z\n" + 
        "DTEND:20170401T153000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:vum2uqf1gd854n308kchcsg0po@google.com\n" + 
        "CREATED:20160629T214507Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 26. matchday \n" + 
        "LAST-MODIFIED:20170401T153728Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Borussia Dortmund (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170319T143000Z\n" + 
        "DTEND:20170319T163000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:kjtslm5l5aecc5pf3814ogaqg4@google.com\n" + 
        "CREATED:20160629T214505Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 25. matchday \n" + 
        "LAST-MODIFIED:20170319T163628Z\n" + 
        "LOCATION:Opel-Arena\\, Mainz\n" + 
        "SEQUENCE:4\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:1. FSV Mainz 05 - FC Schalke 04 (0:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170316T200500Z\n" + 
        "DTEND:20170316T220500Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:mpvsq4v3fj9e9ktq1bnahsi1l8@google.com\n" + 
        "CREATED:20170224T203633Z\n" + 
        "DESCRIPTION:Europa League\\, eigth-final\n" + 
        "LAST-MODIFIED:20170316T221206Z\n" + 
        "LOCATION:Borussia-Park\\, M\u00f6nchengladbach\n" + 
        "SEQUENCE:2\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Bor. M\u00f6nchengladbach - FC Schalke 04 (2:2)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170312T143000Z\n" + 
        "DTEND:20170312T163000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:5pphlkva728ih8ab8r2ma3d2l8@google.com\n" + 
        "CREATED:20160629T214459Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 24. matchday \n" + 
        "LAST-MODIFIED:20170312T163603Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - FC Augsburg (3:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170309T200500Z\n" + 
        "DTEND:20170309T220500Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:ekrpcj1f20o1sf3fd1n292gdi0@google.com\n" + 
        "CREATED:20170224T203604Z\n" + 
        "DESCRIPTION:Europa League\\, eigth-final\n" + 
        "LAST-MODIFIED:20170309T221211Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Bor. M\u00f6nchengladbach (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170304T173000Z\n" + 
        "DTEND:20170304T193000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:obojrnsjndraaskk29h3khhfs8@google.com\n" + 
        "CREATED:20160914T222137Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 23. matchday \n" + 
        "LAST-MODIFIED:20170304T193737Z\n" + 
        "LOCATION:Borussia-Park\\, M\u00f6nchengladbach\n" + 
        "SEQUENCE:4\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Bor. M\u00f6nchengladbach - FC Schalke 04 (4:2)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170301T194500Z\n" + 
        "DTEND:20170301T214500Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:51sg4ckt6kdfp63mqi00buugqg@google.com\n" + 
        "CREATED:20170210T202430Z\n" + 
        "DESCRIPTION:DFB-Pokal\\, quarter-final\n" + 
        "LAST-MODIFIED:20170301T214927Z\n" + 
        "LOCATION:Allianz-Arena\\, M\u00fcnchen\n" + 
        "SEQUENCE:2\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Bayern M\u00fcnchen - FC Schalke 04 (3:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170226T163000Z\n" + 
        "DTEND:20170226T183000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:al2u6fq1vgj5a8oqdq4o0h4a0s@google.com\n" + 
        "CREATED:20160629T214452Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 22. matchday \n" + 
        "LAST-MODIFIED:20170226T183612Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - TSG Hoffenheim (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170222T170000Z\n" + 
        "DTEND:20170222T190000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:splsq185tvrmqb1ocpsojlnc5c@google.com\n" + 
        "CREATED:20161213T184823Z\n" + 
        "DESCRIPTION:Europa League\\, Zw.\n" + 
        "LAST-MODIFIED:20170222T202558Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - PAOK Saloniki (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170219T163000Z\n" + 
        "DTEND:20170219T183000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:5rcn5bvcgp088210f1m93rb12o@google.com\n" + 
        "CREATED:20160914T222135Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 21. matchday \n" + 
        "LAST-MODIFIED:20170219T183602Z\n" + 
        "LOCATION:RheinEnergieStadion\\, K\u00f6ln\n" + 
        "SEQUENCE:4\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:1. FC K\u00f6ln - FC Schalke 04 (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170216T200500Z\n" + 
        "DTEND:20170216T220500Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:4p5g1qsb4k789ufmccr60mmrhg@google.com\n" + 
        "CREATED:20161213T184802Z\n" + 
        "DESCRIPTION:Europa League\\, Zw.\n" + 
        "LAST-MODIFIED:20170216T221205Z\n" + 
        "LOCATION:\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:PAOK Saloniki - FC Schalke 04 (0:3)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170211T173000Z\n" + 
        "DTEND:20170211T193000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:6gqhojgieults08ate2reqedp0@google.com\n" + 
        "CREATED:20160629T214443Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 20. matchday \n" + 
        "LAST-MODIFIED:20170211T193616Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Hertha BSC (2:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170208T173000Z\n" + 
        "DTEND:20170208T193000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:lft9q2as74kn1065e9kq1e9aqc@google.com\n" + 
        "CREATED:20161028T210007Z\n" + 
        "DESCRIPTION:DFB-Pokal\\, eigth-final\n" + 
        "LAST-MODIFIED:20170208T201350Z\n" + 
        "LOCATION:Hardtwaldstadion\\, Sandhausen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:SV Sandhausen - FC Schalke 04 (1:4)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170204T143000Z\n" + 
        "DTEND:20170204T163000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:qpo1crk2suaku14j2u7hip7lrk@google.com\n" + 
        "CREATED:20160914T222133Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 19. matchday \n" + 
        "LAST-MODIFIED:20170204T181928Z\n" + 
        "LOCATION:Allianz-Arena\\, M\u00fcnchen\n" + 
        "SEQUENCE:4\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Bayern M\u00fcnchen - FC Schalke 04 (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170127T193000Z\n" + 
        "DTEND:20170127T213000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:la2r8irgtlduq0i3gdnejngmh0@google.com\n" + 
        "CREATED:20160629T214435Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 18. matchday \n" + 
        "LAST-MODIFIED:20170127T221226Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Eintracht Frankfurt (0:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20170121T143000Z\n" + 
        "DTEND:20170121T163000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:c39uo1ap7d581dl126vui7iluc@google.com\n" + 
        "CREATED:20160629T214427Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 17. matchday \n" + 
        "LAST-MODIFIED:20170121T163635Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - FC Ingolstadt 04 (1:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161220T190000Z\n" + 
        "DTEND:20161220T210000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:7hh231dulaescihv68r9mtfgt8@google.com\n" + 
        "CREATED:20160629T214425Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 16. matchday \n" + 
        "LAST-MODIFIED:20161220T210008Z\n" + 
        "LOCATION:Volksparkstadion\\, Hamburg\n" + 
        "SEQUENCE:4\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Hamburger SV - FC Schalke 04 (2:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161217T143000Z\n" + 
        "DTEND:20161217T163000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:pfu9mda5hmmltvvrjvgvkvcu3o@google.com\n" + 
        "CREATED:20160629T214419Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 15. matchday \n" + 
        "LAST-MODIFIED:20161217T163701Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - SC Freiburg (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161211T163000Z\n" + 
        "DTEND:20161211T183000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:pe16r6v3vcglvgll06n0jjktc4@google.com\n" + 
        "CREATED:20160629T214412Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 14. matchday \n" + 
        "LAST-MODIFIED:20161211T183608Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Bayer 04 Leverkusen (0:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161208T200500Z\n" + 
        "DTEND:20161208T220500Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:861sgnrkoaadhjmkog51oogh2c@google.com\n" + 
        "CREATED:20160826T161344Z\n" + 
        "DESCRIPTION:Europa League\\, group stage\\, 6.match\n" + 
        "LAST-MODIFIED:20161208T221216Z\n" + 
        "LOCATION:Bullen-Arena\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:RB Salzburg - FC Schalke 04 (2:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161203T173000Z\n" + 
        "DTEND:20161203T193000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:fkofn118h391mb4iutir22pn90@google.com\n" + 
        "CREATED:20160712T211402Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 13. matchday \n" + 
        "LAST-MODIFIED:20161203T193702Z\n" + 
        "LOCATION:Red Bull Arena\\, Leipzig\n" + 
        "SEQUENCE:4\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:RB Leipzig - FC Schalke 04 (2:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161127T143000Z\n" + 
        "DTEND:20161127T163000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:g5cr8jkngt3rgh0dsc1npt0o64@google.com\n" + 
        "CREATED:20160629T214404Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 12. matchday \n" + 
        "LAST-MODIFIED:20161127T163608Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:9\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - SV Darmstadt 98 (3:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161124T180000Z\n" + 
        "DTEND:20161124T200000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:bmchqj3q0oi6esh737t3uia8ig@google.com\n" + 
        "CREATED:20160826T161337Z\n" + 
        "DESCRIPTION:Europa League\\, group stage\\, 5.match\n" + 
        "LAST-MODIFIED:20161124T200106Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - OGC Nizza (2:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161119T143000Z\n" + 
        "DTEND:20161119T163000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:ck9efbp103set0de01vl76g1tc@google.com\n" + 
        "CREATED:20160629T214402Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 11. matchday \n" + 
        "LAST-MODIFIED:20161119T163604Z\n" + 
        "LOCATION:Volkswagen Arena\\, Wolfsburg\n" + 
        "SEQUENCE:4\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:VfL Wolfsburg - FC Schalke 04 (0:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161106T163000Z\n" + 
        "DTEND:20161106T183000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:kcm19ngd89gj2v7pbvo1qo1o7c@google.com\n" + 
        "CREATED:20160629T214355Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 10. matchday \n" + 
        "LAST-MODIFIED:20161106T183603Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Werder Bremen (3:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161103T200500Z\n" + 
        "DTEND:20161103T220500Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:unfe1k5ghlmh1f4391lchm8ero@google.com\n" + 
        "CREATED:20160826T161330Z\n" + 
        "DESCRIPTION:Europa League\\, group stage\\, 4.match\n" + 
        "LAST-MODIFIED:20161103T221220Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - FK Krasnodar (2:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161029T163000Z\n" + 
        "DTEND:20161029T183000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:81tljkf5r1qtfeq3r9463ah49c@google.com\n" + 
        "CREATED:20160629T214352Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 9. matchday \n" + 
        "LAST-MODIFIED:20161029T183614Z\n" + 
        "LOCATION:Signal-Iduna-Park\\, Dortmund\n" + 
        "SEQUENCE:4\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Borussia Dortmund - FC Schalke 04 (0:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161023T153000Z\n" + 
        "DTEND:20161023T173000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:ff465ni42rjcgmrldd24g8a14s@google.com\n" + 
        "CREATED:20160629T214345Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 8. matchday\n" + 
        "LAST-MODIFIED:20161023T173617Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - 1. FSV Mainz 05 (3:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161020T170000Z\n" + 
        "DTEND:20161020T190000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:7lnl240sg1vck8r8ngena6df6o@google.com\n" + 
        "CREATED:20160826T161323Z\n" + 
        "DESCRIPTION:Europa League\\, group stage\\, 3.match\n" + 
        "LAST-MODIFIED:20161020T190033Z\n" + 
        "LOCATION:\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FK Krasnodar - FC Schalke 04 (0:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161015T133000Z\n" + 
        "DTEND:20161015T153000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:u5uq252hsjsqc6fgn21o56h5oo@google.com\n" + 
        "CREATED:20160629T214344Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 7. matchday\n" + 
        "LAST-MODIFIED:20161015T153603Z\n" + 
        "LOCATION:WWK Arena\\, Augsburg\n" + 
        "SEQUENCE:4\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Augsburg - FC Schalke 04 (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20161002T153000Z\n" + 
        "DTEND:20161002T173000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:is0ke7k720ngntnl38ao5g4bes@google.com\n" + 
        "CREATED:20160914T222121Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 6. matchday\n" + 
        "LAST-MODIFIED:20161002T173603Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Bor. M\u00f6nchengladbach (4:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160929T170000Z\n" + 
        "DTEND:20160929T190000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:0l3nt5leccroac85r69kgukhbo@google.com\n" + 
        "CREATED:20160826T161316Z\n" + 
        "DESCRIPTION:Europa League\\, group stage\\, 2.match\n" + 
        "LAST-MODIFIED:20160929T190027Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - RB Salzburg (3:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160925T133000Z\n" + 
        "DTEND:20160925T153000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:2dua2p9ql68phvhh31h6bhqhc8@google.com\n" + 
        "CREATED:20160629T214335Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 5. Spieltag \n" + 
        "LAST-MODIFIED:20160925T153644Z\n" + 
        "LOCATION:Wirsol Rhein-Neckar-Arena\\, Sinsheim\n" + 
        "SEQUENCE:4\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:TSG Hoffenheim - FC Schalke 04 (2:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160921T180000Z\n" + 
        "DTEND:20160921T200000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:h9midggvq7u1h08ad241rq2d70@google.com\n" + 
        "CREATED:20160914T222115Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 4. Spieltag \n" + 
        "LAST-MODIFIED:20160921T200004Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - 1. FC K\u00f6ln (1:3)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160918T153000Z\n" + 
        "DTEND:20160918T173000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:nvuflqh34scngbl4us0ucj66cs@google.com\n" + 
        "CREATED:20160629T214325Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 3. Spieltag \n" + 
        "LAST-MODIFIED:20160918T173602Z\n" + 
        "LOCATION:Olympiastadion\\, Berlin\n" + 
        "SEQUENCE:4\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Hertha BSC - FC Schalke 04 (2:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160915T190500Z\n" + 
        "DTEND:20160915T210500Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:1fob6n1ljbe16j3s53j1kcseu4@google.com\n" + 
        "CREATED:20160826T161221Z\n" + 
        "DESCRIPTION:Europa League\\, Gruppenphase\\, 1.Spiel\n" + 
        "LAST-MODIFIED:20160915T211209Z\n" + 
        "LOCATION:Du Ray\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:OGC Nizza - FC Schalke 04 (0:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160909T183000Z\n" + 
        "DTEND:20160909T203000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:mfjo5b62bo1a80v2hfvi6j8qb0@google.com\n" + 
        "CREATED:20140625T012255Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 2. Spieltag \n" + 
        "LAST-MODIFIED:20160909T203602Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:15\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Bayern M\u00fcnchen (0:2)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160130T143000Z\n" + 
        "DTEND:20160130T163000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:sdlht9keosvkq61r1f1hv7akfk@google.com\n" + 
        "CREATED:20150626T130425Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 19. Spieltag \n" + 
        "LAST-MODIFIED:20160907T092634Z\n" + 
        "LOCATION:Jonathan-Heimes-Stadion am B\u00f6llenfalltor\n" + 
        "SEQUENCE:8\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:SV Darmstadt 98 - FC Schalke 04 (0:2)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20150822T133000Z\n" + 
        "DTEND:20150822T153000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:qq4na1a5d0475mihiab8hhj8gs@google.com\n" + 
        "CREATED:20150626T130410Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 2. Spieltag \n" + 
        "LAST-MODIFIED:20160907T092625Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:12\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - SV Darmstadt 98 (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160827T133000Z\n" + 
        "DTEND:20160827T153000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:kmj4d55v4139k6n9tage31igac@google.com\n" + 
        "CREATED:20160629T214316Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 1. Spieltag \n" + 
        "LAST-MODIFIED:20160827T153745Z\n" + 
        "LOCATION:Commerzbank-Arena\\, Frankfurt\n" + 
        "SEQUENCE:4\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Eintracht Frankfurt - FC Schalke 04 (1:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160820T133000Z\n" + 
        "DTEND:20160820T153000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:e8gogfmblsfvvmh3g1n2m6fjd8@google.com\n" + 
        "CREATED:20160629T214308Z\n" + 
        "DESCRIPTION:DFB-Pokal\\, Runde 1\n" + 
        "LAST-MODIFIED:20160820T153707Z\n" + 
        "LOCATION:\n" + 
        "SEQUENCE:9\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC 08 Villingen - FC Schalke 04 (1:4)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20140818T183000Z\n" + 
        "DTEND:20140818T203000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:o9q2f80j20pvs7t6cba967s46s@google.com\n" + 
        "CREATED:20140625T012254Z\n" + 
        "DESCRIPTION:DFB-Pokal\\, Runde 1\n" + 
        "LAST-MODIFIED:20160804T132856Z\n" + 
        "LOCATION:DDV-Stadion\\, Dresden\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Dynamo Dresden - FC Schalke 04 (2:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160212T193000Z\n" + 
        "DTEND:20160212T213000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:dpuhcumlogae63occ7lafocur4@google.com\n" + 
        "CREATED:20150626T130426Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 21. Spieltag \n" + 
        "LAST-MODIFIED:20160628T141133Z\n" + 
        "LOCATION:Opel-Arena\\, Mainz\n" + 
        "SEQUENCE:8\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:1. FSV Mainz 05 - FC Schalke 04 (2:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20150913T153000Z\n" + 
        "DTEND:20150913T173000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:42q5u65r6f7a5il81j9d83g0cg@google.com\n" + 
        "CREATED:20150626T130412Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 4. Spieltag \n" + 
        "LAST-MODIFIED:20160628T141126Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:12\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - 1. FSV Mainz 05 (2:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20150424T183000Z\n" + 
        "DTEND:20150424T203000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:lqgu731cktjh7g3ene2j4ple7o@google.com\n" + 
        "CREATED:20140625T012315Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 30. Spieltag \n" + 
        "LAST-MODIFIED:20160628T141123Z\n" + 
        "LOCATION:Opel-Arena\\, Mainz\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:1. FSV Mainz 05 - FC Schalke 04 (2:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20141129T143000Z\n" + 
        "DTEND:20141129T163000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:1a4ocffosbdk892e5aufks12h4@google.com\n" + 
        "CREATED:20140625T012303Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 13. Spieltag \n" + 
        "LAST-MODIFIED:20160628T141115Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:9\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - 1. FSV Mainz 05 (4:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160514T133000Z\n" + 
        "DTEND:20160514T153000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:m7j5up6cudgvuia9cv1v20fmoc@google.com\n" + 
        "CREATED:20150626T130436Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 34. Spieltag \n" + 
        "LAST-MODIFIED:20160514T153821Z\n" + 
        "LOCATION:Wirsol Rhein-Neckar-Arena\\, Sinsheim\n" + 
        "SEQUENCE:2\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:TSG Hoffenheim - FC Schalke 04 (1:4)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160507T133000Z\n" + 
        "DTEND:20160507T153000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:b8s5khvjdgqeh8391f5qu64g14@google.com\n" + 
        "CREATED:20150626T130435Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 33. Spieltag \n" + 
        "LAST-MODIFIED:20160507T153850Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - FC Augsburg (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160430T133000Z\n" + 
        "DTEND:20160430T153000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:5t8lqslar5qmuepbh2iph8sa5s@google.com\n" + 
        "CREATED:20150626T130434Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 32. Spieltag \n" + 
        "LAST-MODIFIED:20160430T153706Z\n" + 
        "LOCATION:HDI-Arena\\, Hannover\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Hannover 96 - FC Schalke 04 (1:3)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160423T163000Z\n" + 
        "DTEND:20160423T183000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:jundkl3m5bmj1le7n9ao4g7p34@google.com\n" + 
        "CREATED:20150626T130433Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 31. Spieltag \n" + 
        "LAST-MODIFIED:20160423T183614Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:9\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Bayer 04 Leverkusen (2:3)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160416T163000Z\n" + 
        "DTEND:20160416T183000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:edse7il2984qrdhg0f9fc94eg8@google.com\n" + 
        "CREATED:20150626T130433Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 30. Spieltag \n" + 
        "LAST-MODIFIED:20160416T183605Z\n" + 
        "LOCATION:Allianz-Arena\\, M\u00fcnchen\n" + 
        "SEQUENCE:8\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Bayern M\u00fcnchen - FC Schalke 04 (3:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160410T133000Z\n" + 
        "DTEND:20160410T153000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:og9jndevrg7gt4fq4k1eo2nb5c@google.com\n" + 
        "CREATED:20150626T130432Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 29. Spieltag \n" + 
        "LAST-MODIFIED:20160410T170010Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:9\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Borussia Dortmund (2:2)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160402T133000Z\n" + 
        "DTEND:20160402T153000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:obe2d7kmk0i8rlc2goffk20nr4@google.com\n" + 
        "CREATED:20150626T130431Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 28. Spieltag \n" + 
        "LAST-MODIFIED:20160402T153630Z\n" + 
        "LOCATION:Audi-Sportpark\\, Ingolstadt\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Ingolstadt 04 - FC Schalke 04 (3:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160318T193000Z\n" + 
        "DTEND:20160318T213000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:eljaqe3b92aj4mc4hqu9mkn710@google.com\n" + 
        "CREATED:20150626T130430Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 27. Spieltag \n" + 
        "LAST-MODIFIED:20160318T213621Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:9\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Bor. M\u00f6nchengladbach (2:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160311T193000Z\n" + 
        "DTEND:20160311T213000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:08tp5og097kokefmq72c3pkv8s@google.com\n" + 
        "CREATED:20150626T130430Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 26. Spieltag \n" + 
        "LAST-MODIFIED:20160311T213602Z\n" + 
        "LOCATION:Olympiastadion\\, Berlin\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Hertha BSC - FC Schalke 04 (2:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160305T143000Z\n" + 
        "DTEND:20160305T163000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:ueorld6bvdqqenfuojihlp537o@google.com\n" + 
        "CREATED:20150626T130429Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 25. Spieltag \n" + 
        "LAST-MODIFIED:20160305T163720Z\n" + 
        "LOCATION:RheinEnergieStadion\\, K\u00f6ln\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:1. FC K\u00f6ln - FC Schalke 04 (1:3)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160302T190000Z\n" + 
        "DTEND:20160302T210000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:luv5vgvo071m1nkn0uaf2ju6j4@google.com\n" + 
        "CREATED:20150626T130428Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 24. Spieltag \n" + 
        "LAST-MODIFIED:20160302T210220Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:13\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Hamburger SV (3:2)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160228T183000Z\n" + 
        "DTEND:20160228T203000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:orb2tntr64bfud38vocmfvdjn0@google.com\n" + 
        "CREATED:20150626T130428Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 23. Spieltag \n" + 
        "LAST-MODIFIED:20160228T203656Z\n" + 
        "LOCATION:Commerzbank-Arena\\, Frankfurt\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Eintracht Frankfurt - FC Schalke 04 (0:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160225T180000Z\n" + 
        "DTEND:20160225T200000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:bat6t09tqqcekm2931lp1nq974@google.com\n" + 
        "CREATED:20151215T034353Z\n" + 
        "DESCRIPTION:Europa League\\, Zw.\n" + 
        "LAST-MODIFIED:20160225T214924Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Schachtar Donezk (0:3)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160221T163000Z\n" + 
        "DTEND:20160221T183000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:ds2vm5j15121namlniqlsksqcg@google.com\n" + 
        "CREATED:20150626T130427Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 22. Spieltag \n" + 
        "LAST-MODIFIED:20160221T183606Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:9\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - VfB Stuttgart (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160218T200500Z\n" + 
        "DTEND:20160218T220500Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:lrusl2ctk6gaq6t90n3s062q40@google.com\n" + 
        "CREATED:20151215T034343Z\n" + 
        "DESCRIPTION:Europa League\\, Zw.\n" + 
        "LAST-MODIFIED:20160218T220027Z\n" + 
        "LOCATION:\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Schachtar Donezk - FC Schalke 04 (0:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160206T143000Z\n" + 
        "DTEND:20160206T163000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:mal5ko4c9brt4e0039lq4pk1ok@google.com\n" + 
        "CREATED:20150626T130425Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 20. Spieltag \n" + 
        "LAST-MODIFIED:20160206T163649Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:9\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - VfL Wolfsburg (3:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20160124T163000Z\n" + 
        "DTEND:20160124T183000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:gnt6a6liqs69e9j3545gqqgo9c@google.com\n" + 
        "CREATED:20150626T130424Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 18. Spieltag \n" + 
        "LAST-MODIFIED:20160124T183607Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:12\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Werder Bremen (1:3)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20151218T193000Z\n" + 
        "DTEND:20151218T213000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:sv1ee3rlrd84ad22ro76vksbag@google.com\n" + 
        "CREATED:20150626T130423Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 17. Spieltag \n" + 
        "LAST-MODIFIED:20151218T213606Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:9\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - TSG Hoffenheim (1:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20151213T143000Z\n" + 
        "DTEND:20151213T163000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:ehsp3n2s86gd59bdn2fd6lo94k@google.com\n" + 
        "CREATED:20150626T130422Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 16. Spieltag \n" + 
        "LAST-MODIFIED:20151213T163603Z\n" + 
        "LOCATION:WWK Arena\\, Augsburg\n" + 
        "SEQUENCE:8\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Augsburg - FC Schalke 04 (2:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20151210T200500Z\n" + 
        "DTEND:20151210T220500Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:e1pmhpljt18binhcsd2bru9qos@google.com\n" + 
        "CREATED:20150828T161246Z\n" + 
        "DESCRIPTION:Europa League\\, Gruppenphase\\, 6.Spiel\n" + 
        "LAST-MODIFIED:20151210T221236Z\n" + 
        "LOCATION:\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Asteras Tripolis - FC Schalke 04 (0:4)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20151204T193000Z\n" + 
        "DTEND:20151204T213000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:5oocdn20p7uafevoppvf163778@google.com\n" + 
        "CREATED:20150626T130421Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 15. Spieltag \n" + 
        "LAST-MODIFIED:20151205T005226Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:9\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Hannover 96 (3:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20151129T163000Z\n" + 
        "DTEND:20151129T183000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:gm97fi94mmfvfelgda2hl4r0js@google.com\n" + 
        "CREATED:20150626T130421Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 14. Spieltag \n" + 
        "LAST-MODIFIED:20151129T183626Z\n" + 
        "LOCATION:BayArena\\, Leverkusen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Bayer 04 Leverkusen - FC Schalke 04 (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20151126T180000Z\n" + 
        "DTEND:20151126T200000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:ahcjbd68o0896lr9hsulkjl4hg@google.com\n" + 
        "CREATED:20150828T161241Z\n" + 
        "DESCRIPTION:Europa League\\, Gruppenphase\\, 5.Spiel\n" + 
        "LAST-MODIFIED:20151126T200034Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - APOEL Nikosia (1:0)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20151121T173000Z\n" + 
        "DTEND:20151121T193000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:2j2glinspmtlt71sav0df3h6t0@google.com\n" + 
        "CREATED:20150626T130420Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 13. Spieltag \n" + 
        "LAST-MODIFIED:20151121T193621Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:9\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Bayern M\u00fcnchen (1:3)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20151108T143000Z\n" + 
        "DTEND:20151108T163000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:2o72r0h1gd8rc1dfavoefq6o38@google.com\n" + 
        "CREATED:20150626T130419Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 12. Spieltag \n" + 
        "LAST-MODIFIED:20151108T163632Z\n" + 
        "LOCATION:Signal-Iduna-Park\\, Dortmund\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Borussia Dortmund - FC Schalke 04 (3:2)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20151105T200500Z\n" + 
        "DTEND:20151105T220500Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:skmv57f93p8on5f6s6scpgo6e8@google.com\n" + 
        "CREATED:20150828T161235Z\n" + 
        "DESCRIPTION:Europa League\\, Gruppenphase\\, 4.Spiel\n" + 
        "LAST-MODIFIED:20151105T221215Z\n" + 
        "LOCATION:\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Sparta Prag - FC Schalke 04 (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20151031T143000Z\n" + 
        "DTEND:20151031T163000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:5ue51n9n1tu1evp28qeghe1dus@google.com\n" + 
        "CREATED:20150626T130418Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 11. Spieltag \n" + 
        "LAST-MODIFIED:20151031T163705Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:9\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - FC Ingolstadt 04 (1:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20151028T193000Z\n" + 
        "DTEND:20151028T213000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:t4ikqonc0fi3ls2vof85q1tt3k@google.com\n" + 
        "CREATED:20150815T153659Z\n" + 
        "DESCRIPTION:DFB-Pokal\\, Runde 2\n" + 
        "LAST-MODIFIED:20151028T213627Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Bor. M\u00f6nchengladbach (0:2)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20151025T163000Z\n" + 
        "DTEND:20151025T183000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:ting0qqetndtia00eo873l2ul0@google.com\n" + 
        "CREATED:20150626T130418Z\n" + 
        "DESCRIPTION:1. Bundesliga\\, 10. Spieltag \n" + 
        "LAST-MODIFIED:20151025T183632Z\n" + 
        "LOCATION:Borussia-Park\\, M\u00f6nchengladbach\n" + 
        "SEQUENCE:6\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:Bor. M\u00f6nchengladbach - FC Schalke 04 (3:1)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTART:20151022T170000Z\n" + 
        "DTEND:20151022T190000Z\n" + 
        "DTSTAMP:20170817T143843Z\n" + 
        "UID:unrnn6cl25don48iod063p1gic@google.com\n" + 
        "CREATED:20150828T161228Z\n" + 
        "DESCRIPTION:Europa League\\, Gruppenphase\\, 3.Spiel\n" + 
        "LAST-MODIFIED:20151022T190059Z\n" + 
        "LOCATION:VELTINS-Arena\\, Gelsenkirchen\n" + 
        "SEQUENCE:3\n" + 
        "STATUS:CONFIRMED\n" + 
        "SUMMARY:FC Schalke 04 - Sparta Prag (2:2)\n" + 
        "TRANSP:TRANSPARENT\n" + 
        "END:VEVENT\n" + 
        "END:VCALENDAR\n" + 
        "";

    public static final String FEED_WITH_SERIES_AND_CHANGE_EXCEPTION = "BEGIN:VCALENDAR\n" + 
        "VERSION:2.0\n" + 
        "PRODID:-//Open-Xchange//7.10.0-Rev1//EN\n" + 
        "BEGIN:VTIMEZONE\n" + 
        "TZID:Europe/Berlin\n" + 
        "TZURL:http://tzurl.org/zoneinfo-outlook/Europe/Berlin\n" + 
        "X-LIC-LOCATION:Europe/Berlin\n" + 
        "BEGIN:DAYLIGHT\n" + 
        "TZOFFSETFROM:+0100\n" + 
        "TZOFFSETTO:+0200\n" + 
        "TZNAME:CEST\n" + 
        "DTSTART:19700329T020000\n" + 
        "RRULE:FREQ=YEARLY;BYMONTH=3;BYDAY=-1SU\n" + 
        "END:DAYLIGHT\n" + 
        "BEGIN:STANDARD\n" + 
        "TZOFFSETFROM:+0200\n" + 
        "TZOFFSETTO:+0100\n" + 
        "TZNAME:CET\n" + 
        "DTSTART:19701025T030000\n" + 
        "RRULE:FREQ=YEARLY;BYMONTH=10;BYDAY=-1SU\n" + 
        "END:STANDARD\n" + 
        "END:VTIMEZONE\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTAMP:20180125T115944Z\n" + 
        "ATTENDEE;CN=\"dora, dora\";PARTSTAT=ACCEPTED;CUTYPE=INDIVIDUAL:mailto:dora@co\n" + 
        " ntext7.oxoe.int\n" + 
        "CLASS:PUBLIC\n" + 
        "CREATED:20180125T115835Z\n" + 
        "DTEND;TZID=Europe/Berlin:20180123T130000\n" + 
        "DTSTART;TZID=Europe/Berlin:20180123T123000\n" + 
        "LAST-MODIFIED:20180125T115944Z\n" + 
        "ORGANIZER;CN=\"dora, dora\":mailto:dora@context7.oxoe.int\n" + 
        "RRULE:FREQ=WEEKLY;COUNT=4;BYDAY=TU\n" + 
        "SEQUENCE:0\n" + 
        "SUMMARY:Test-Series\n" + 
        "TRANSP:OPAQUE\n" + 
        "UID:da554dcc-2c4b-4e11-a360-e3b2d3ca0408\n" + 
        "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n" + 
        "BEGIN:VALARM\n" + 
        "TRIGGER:-PT15M\n" + 
        "UID:0f9c2d0e-a005-4c9b-850e-fdb15531996a\n" + 
        "ACTION:DISPLAY\n" + 
        "DESCRIPTION:Reminder\n" + 
        "END:VALARM\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTAMP:20180125T115934Z\n" + 
        "ATTENDEE;CN=\"dora, dora\";PARTSTAT=ACCEPTED;CUTYPE=INDIVIDUAL:mailto:dora@co\n" + 
        " ntext7.oxoe.int\n" + 
        "CLASS:PUBLIC\n" + 
        "CREATED:20180125T115934Z\n" + 
        "DTEND;TZID=Europe/Berlin:20180131T130000\n" + 
        "DTSTART;TZID=Europe/Berlin:20180131T123000\n" + 
        "LAST-MODIFIED:20180125T115934Z\n" + 
        "ORGANIZER;CN=\"dora, dora\":mailto:dora@context7.oxoe.int\n" + 
        "RECURRENCE-ID:20180130T113000Z\n" + 
        "RRULE:FREQ=WEEKLY;COUNT=4;BYDAY=TU\n" + 
        "SEQUENCE:1\n" + 
        "SUMMARY:Test-Series - shifted day\n" + 
        "TRANSP:OPAQUE\n" + 
        "UID:da554dcc-2c4b-4e11-a360-e3b2d3ca0408\n" + 
        "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n" + 
        "BEGIN:VALARM\n" + 
        "TRIGGER:-PT15M\n" + 
        "UID:a5733b0f-b94c-481f-b3f0-1ba76949dd72\n" + 
        "ACTION:DISPLAY\n" + 
        "DESCRIPTION:Reminder\n" + 
        "END:VALARM\n" + 
        "END:VEVENT\n" + 
        "BEGIN:VEVENT\n" + 
        "DTSTAMP:20180125T115955Z\n" + 
        "ATTENDEE;CN=\"dora, dora\";PARTSTAT=ACCEPTED;CUTYPE=INDIVIDUAL:mailto:dora@co\n" + 
        " ntext7.oxoe.int\n" + 
        "CLASS:PUBLIC\n" + 
        "CREATED:20180125T115944Z\n" + 
        "DTEND;TZID=Europe/Berlin:20180206T110000\n" + 
        "DTSTART;TZID=Europe/Berlin:20180206T103000\n" + 
        "LAST-MODIFIED:20180125T115955Z\n" + 
        "ORGANIZER;CN=\"dora, dora\":mailto:dora@context7.oxoe.int\n" + 
        "RECURRENCE-ID:20180206T113000Z\n" + 
        "RRULE:FREQ=WEEKLY;COUNT=4;BYDAY=TU\n" + 
        "SEQUENCE:2\n" + 
        "SUMMARY:Test-Series - shifted time\n" + 
        "TRANSP:OPAQUE\n" + 
        "UID:da554dcc-2c4b-4e11-a360-e3b2d3ca0408\n" + 
        "X-MICROSOFT-CDO-BUSYSTATUS:BUSY\n" + 
        "BEGIN:VALARM\n" + 
        "TRIGGER:-PT15M\n" + 
        "UID:d13da3c9-7ea3-48d7-a468-9f985e6edfc8\n" + 
        "ACTION:DISPLAY\n" + 
        "DESCRIPTION:Reminder\n" + 
        "END:VALARM\n" + 
        "END:VEVENT\n" + 
        "END:VCALENDAR\n" + 
        "";

    // @formatter:on
}

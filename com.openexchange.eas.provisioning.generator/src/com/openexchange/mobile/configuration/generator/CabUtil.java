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

package com.openexchange.mobile.configuration.generator;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;


public class CabUtil {
    public static class CFHEADER {
        public static int MSCF = 0x4D534346;
    }

    public static class CFFOLDER {
        public static short tcompTYPE_NONE = 0x0000;
        public static short tcompTYPE_MSZIP = 0x0001;
        public static short tcompTYPE_QUANTUM = 0x0002;
        public static short tcompTYPE_LZX = 0x0003;
    }

    public static void writeCabFile(final DataOutputStream pw, final String data) throws IOException {

        // signature ; offset 00
        pw.writeInt(CFHEADER.MSCF);
        // reserved1 ; offset 04
        pw.writeInt(0);
        // cbCabinet (totalSize) ; offset 08
        // Only in this special case where we know the size of the header (79)
        pw.writeInt(toLittleEndian(79 + data.length()));
        // reserved2 ; offset 0C
        pw.writeInt(0);
        // coffFiles (offset) ; offset 10
        pw.writeInt(0x2C000000);
        // reserved3 ; offset 14
        pw.writeInt(0);
        // versionMinor ; offset 18
        pw.writeByte(0x03);
        // versionMajor ; offset 19
        pw.writeByte(0x01);
        // cFolders (we allow only 1 here) ; offset 1A
        pw.writeShort(0x0100);

        // cFiles (we allow only 1 here) ; offset 1C
        pw.writeShort(0x0100);

        // flags ; offset 1E
        pw.writeShort(0);

        // setID ; offset 20
        pw.writeShort(0x1337);

        // iCabinet ; offset 22
        pw.writeShort(0);

        writeCFFolder(pw);

        writeCFFile(pw, data.length());

        writeCFData(pw, data);

        pw.close();
    }

    private static void writeCFFolder(DataOutputStream pw) throws IOException {
        // coffCabStart
        // File offset ; offset 24
        pw.writeInt(0x47000000);

        // cCFData (always 1 here) ; offset 28
        pw.writeShort(0x0100);

        // typeCompress ; offset 2A
        pw.writeShort(CFFOLDER.tcompTYPE_NONE);

    }

    private static void writeCFFile(DataOutputStream pw, final int filesize) throws IOException {
        // cbFile ; offset 2C
        pw.writeInt(toLittleEndian(filesize));

        // uoffFolderStart ; offset 30
        pw.writeInt(0);

        // iFolder ; offset 34
        pw.writeShort(0);

        final Calendar cal = Calendar.getInstance();
        // date ; offset 36
        pw.writeShort(toLittleEndian(getDate(cal)));

        // time ; offset 38
        pw.writeShort(getTime(cal));

        // attribs ; offset 40
        pw.writeShort(0x2000);

        // szName ; offset 3c
        pw.writeBytes("_setup.xml");
        pw.writeByte(0);
    }

    // protected to be able to test this
    protected static int toLittleEndian(final int value) {
        return ((value & 0x000000ff) << 24) + ((value & 0x0000ff00) << 8) +
        ((value & 0x00ff0000) >>> 8) + ((value & 0xff000000) >>> 24);
    }

    private static void writeCFData(DataOutputStream pw, final String data) throws IOException {
        // csum (this version has no checksum
        pw.writeInt(0);

        // cbData
        pw.writeShort(toLittleEndian((short)data.length()));

        // cbUncomp
        pw.writeShort(toLittleEndian((short)data.length()));

        // ab[cbData]
        pw.writeBytes(data);
    }

    // protected to be able to test this
    protected static short toLittleEndian(final short value) {
        return (short)(((value & 0x00ff) << 8) + ((value & 0xff00) >> 8));
    }

    // protected to be able to test this
    protected static short getTime(final Calendar cal) {
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        final int hour = cal.get(Calendar.HOUR_OF_DAY);
        final int minute = cal.get(Calendar.MINUTE);
        final int second = cal.get(Calendar.SECOND);
        return (short)((hour << 11) + (minute << 5) + (second/2));
    }

    // protected to be able to test this
    protected static short getDate(final Calendar cal) {
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH) + 1;
        final int day = cal.get(Calendar.DAY_OF_MONTH);
        return (short)(((year - 1980) << 9) + (month << 5) + day);
    }
}


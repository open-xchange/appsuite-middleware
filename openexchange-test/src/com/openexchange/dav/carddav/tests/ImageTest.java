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

package com.openexchange.dav.carddav.tests;

import static org.junit.Assert.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.dav.StatusCodes;
import com.openexchange.dav.SyncToken;
import com.openexchange.dav.carddav.CardDAVTest;
import com.openexchange.dav.carddav.VCardResource;
import com.openexchange.groupware.container.Contact;
import net.sourceforge.cardme.vcard.arch.EncodingType;
import net.sourceforge.cardme.vcard.types.PhotoType;
import net.sourceforge.cardme.vcard.types.media.ImageMediaType;

/**
 * {@link ImageTest} - Tests contact images via the CardDAV interface
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ImageTest extends CardDAVTest {

    static final byte[] PNG_100x100 = {
        -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 100, 0, 0, 0, 100, 8, 2, 0, 0, 0, -1, -128, 2, 3, 0, 0, 0,
        -30, 73, 68, 65, 84, 120, -38, -19, -40, 73, 14, -124, 32, 16, 64, 81, 19, -18, 127, 102, 76, 92, 58, 20, -72, 64, 9, -11, -2, -82,
        -23, -87, -6, 69, 13, -19, -74, -87, -81, 82, 10, 4, 73, -54, 83, -43, 17, 44, 88, -80, 96, -63, -126, 37, 88, -80, 96, -63, -126,
        5, 75, -80, 96, -63, -126, 5, 11, -106, 96, -63, -126, 5, 11, 22, 44, -63, -126, 5, 11, 22, 44, 88, -126, 5, 11, 22, 44, 88, -80,
        4, 11, 22, 44, 88, -80, 96, 13, -99, -32, 52, -54, -85, 23, 92, -97, 26, 52, -10, 44, 88, -63, -105, 54, 71, -6, 108, -20, -119,
        -80, -98, 86, 94, 61, -68, 93, 89, -16, 52, 124, 90, 12, 78, -52, -98, -73, -61, -126, 5, 11, -42, 15, 23, -8, 24, 43, -41, 5, -66,
        -71, 117, 8, 14, -100, 116, 91, -121, -98, 77, 105, 115, 37, -53, -90, 116, -2, -49, 92, 4, -85, 121, -36, -63, -86, -3, -1, -121,
        96, 69, 63, -61, 45, 26, -73, 104, 96, -63, 18, 44, 88, -80, 96, -63, -126, 37, 88, -80, 96, -63, -126, 5, 75, -80, 96, -63, -126,
        5, 11, -106, 96, -63, -126, 5, 11, 22, 44, -63, -126, 5, 11, -42, 122, 88, -110, 36, 37, 107, 7, 58, -3, 68, -102, -42, 88, 97,
        118, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
    };

    static final byte[] GIF_100x100 = {
        -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 100, 0, 0, 0, 100, 8, 2, 0, 0, 0, -1, -128, 2, 3, 0, 0, 0,
        -30, 73, 68, 65, 84, 120, -38, -19, -40, 73, 14, -124, 32, 16, 64, 81, 19, -18, 127, 102, 76, 92, 58, 20, -72, 64, 9, -11, -2, -82,
        -23, -87, -6, 69, 13, -19, -74, -87, -81, 82, 10, 4, 73, -54, 83, -43, 17, 44, 88, -80, 96, -63, -126, 37, 88, -80, 96, -63, -126,
        5, 75, -80, 96, -63, -126, 5, 11, -106, 96, -63, -126, 5, 11, 22, 44, -63, -126, 5, 11, 22, 44, 88, -126, 5, 11, 22, 44, 88, -80,
        4, 11, 22, 44, 88, -80, 96, 13, -99, -32, 52, -54, -85, 23, 92, -97, 26, 52, -10, 44, 88, -63, -105, 54, 71, -6, 108, -20, -119,
        -80, -98, 86, 94, 61, -68, 93, 89, -16, 52, 124, 90, 12, 78, -52, -98, -73, -61, -126, 5, 11, -42, 15, 23, -8, 24, 43, -41, 5, -66,
        -71, 117, 8, 14, -100, 116, 91, -121, -98, 77, 105, 115, 37, -53, -90, 116, -2, -49, 92, 4, -85, 121, -36, -63, -86, -3, -1, -121,
        96, 69, 63, -61, 45, 26, -73, 104, 96, -63, 18, 44, 88, -80, 96, -63, -126, 37, 88, -80, 96, -63, -126, 5, 75, -80, 96, -63, -126,
        5, 11, -106, 96, -63, -126, 5, 11, 22, 44, -63, -126, 5, 11, -42, 122, 88, -110, 36, 37, 107, 7, 58, -3, 68, -102, -42, 88, 97,
        118, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126
    };

    static final byte[] JPG_400x250 = {
        -1, -40, -1, -32, 0, 16, 74, 70, 73, 70, 0, 1, 1, 1, 0, 72, 0, 72, 0, 0, -1, -31, 0, 22, 69, 120, 105, 102, 0, 0, 77, 77, 0,
        42, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, -1, -2, 0, 19, 67, 114, 101, 97, 116, 101, 100, 32, 119, 105, 116, 104, 32, 71, 73, 77, 80,
        -1, -37, 0, 67, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -37, 0, 67, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -64, 0, 17, 8, 0, -6, 1, -112, 3, 1, 34, 0, 2, 17, 1, 3, 17, 1, -1, -60, 0, 26,
        0, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 11, 10, 8, 9, 7, 1, -1, -60, 0, 52, 16, 1, 0, 0, 4, 2, 9, 4, 1, 4, 2, 1,
        5, 0, 0, 0, 0, 0, 1, 5, 6, 8, 4, 7, 2, 3, 9, 10, 26, 57, 113, -72, -40, 88, 89, -104, -88, 23, 19, 20, 21, 22, 17, 18, 24, 37,
        38, 49, 66, 120, -1, -60, 0, 20, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -60, 0, 20, 17, 1, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -38, 0, 12, 3, 1, 0, 2, 17, 3, 17, 0, 63, 0, -60, 109, -80, -37, 6, 122, 94, 94, 122, 81, 22,
        -51, 109, 84, 62, -122, 100, 103, 102, 99, -22, -22, 45, 10, 30, -119, -123, 65, 72, -46, -111, -98, -23, 82, -108, 125, 71,
        92, 84, 63, -21, 80, -41, -109, -102, 126, -109, -106, 70, 87, 74, -45, -45, -7, -82, -98, -100, -34, 123, 39, -122, -100, 37,
        -65, -91, -85, -46, -45, -99, 105, -22, 117, 26, 94, -72, 112, -70, 109, -41, -12, 57, -10, 110, -49, 60, -125, 126, -18, -71,
        115, -42, -79, -98, -73, 55, -39, -35, -64, -85, -14, 9, 2, -16, -70, 109, -41, -12, 57, -10, 110, -49, 60, -125, 56, 93, 54,
        -21, -6, 28, -5, 55, 103, -98, 65, -85, -12, 2, 64, -68, 46, -101, 117, -3, 14, 125, -101, -77, -49, 32, -50, 23, 77, -70, -2,
        -121, 62, -51, -39, -25, -112, 106, -3, 0, -112, 47, 11, -90, -35, 127, 67, -97, 102, -20, -13, -56, 51, -123, -45, 110, -65,
        -95, -49, -77, 118, 121, -28, 26, -65, 64, 36, 11, -62, -23, -73, 95, -48, -25, -39, -69, 60, -14, 12, -31, 116, -37, -81, -24,
        115, -20, -35, -98, 121, 6, -81, -48, 9, 2, -16, -70, 109, -41, -12, 57, -10, 110, -49, 60, -125, 56, 93, 54, -21, -6, 28, -5,
        55, 103, -98, 65, -85, -12, 2, 64, -68, 46, -101, 117, -3, 14, 125, -101, -77, -49, 32, -50, 23, 77, -70, -2, -121, 62, -51,
        -39, -25, -112, 106, -3, 0, -112, 47, 11, -90, -35, 127, 67, -97, 102, -20, -13, -56, 51, -123, -45, 110, -65, -95, -49, -77,
        118, 121, -28, 26, -65, 64, 36, 11, -62, -23, -73, 95, -48, -25, -39, -69, 60, -14, 12, -31, 116, -37, -81, -24, 115, -20, -35,
        -98, 121, 6, -81, -48, 9, 2, -16, -70, 109, -41, -12, 57, -10, 110, -49, 60, -125, 56, 93, 54, -21, -6, 28, -5, 55, 103, -98,
        65, -85, -12, 2, 64, -68, 46, -101, 117, -3, 14, 125, -101, -77, -49, 32, -50, 23, 77, -70, -2, -121, 62, -51, -39, -25, -112,
        106, -3, 0, -112, 47, 11, -90, -35, 127, 67, -97, 102, -20, -13, -56, 51, -123, -45, 110, -65, -95, -49, -77, 118, 121, -28,
        26, -65, 64, 36, 11, -62, -23, -73, 95, -48, -25, -39, -69, 60, -14, 12, -31, 116, -37, -81, -24, 115, -20, -35, -98, 121, 6,
        -81, -48, 9, 2, -16, -70, 109, -41, -12, 57, -10, 110, -49, 60, -125, 56, 93, 54, -21, -6, 28, -5, 55, 103, -98, 65, -85, -12,
        2, 64, -68, 46, -101, 117, -3, 14, 125, -101, -77, -49, 32, -50, 23, 77, -70, -2, -121, 62, -51, -39, -25, -112, 106, -3, 0,
        -112, 47, 11, -90, -35, 127, 67, -97, 102, -20, -13, -56, 51, -123, -45, 110, -65, -95, -49, -77, 118, 121, -28, 26, -65, 64,
        36, 11, -62, -23, -73, 95, -48, -25, -39, -69, 60, -14, 12, -31, 116, -37, -81, -24, 115, -20, -35, -98, 121, 6, -81, -48, 9,
        2, -16, -70, 109, -41, -12, 57, -10, 110, -49, 60, -125, 56, 93, 54, -21, -6, 28, -5, 55, 103, -98, 65, -85, -12, 2, 64, -68,
        46, -101, 117, -3, 14, 125, -101, -77, -49, 32, -50, 23, 77, -70, -2, -121, 62, -51, -39, -25, -112, 106, -3, 0, -112, 47, 11,
        -90, -35, 127, 67, -97, 102, -20, -13, -56, 51, -123, -45, 110, -65, -95, -49, -77, 118, 121, -28, 26, -65, 64, 36, 11, -62,
        -23, -73, 95, -48, -25, -39, -69, 60, -14, 12, -31, 116, -37, -81, -24, 115, -20, -35, -98, 121, 6, -81, -48, 9, 2, -16, -70,
        109, -41, -12, 57, -10, 110, -49, 60, -125, 56, 93, 54, -21, -6, 28, -5, 55, 103, -98, 65, -85, -12, 2, 64, -68, 46, -101, 117,
        -3, 14, 125, -101, -77, -49, 32, -50, 23, 77, -70, -2, -121, 62, -51, -39, -25, -112, 106, -3, 0, -112, 47, 11, -90, -35, 127,
        67, -97, 102, -20, -13, -56, 51, -123, -45, 110, -65, -95, -49, -77, 118, 121, -28, 26, -65, 64, 36, 11, -62, -23, -73, 95,
        -48, -25, -39, -69, 60, -14, 13, -28, 125, -49, 91, 6, 122, 89, -90, 122, 86, -10, -51, 114, -76, 62, -122, 91, -25, 102, 92,
        106, -23, -35, 10, -30, -119, -46, -88, 41, 26, -82, 50, 45, 42, -82, -113, -89, 43, -118, 123, -3, -86, 26, 14, 115, 80, 82,
        115, 56, -51, 41, 90, -122, 65, 53, -48, -45, -108, 79, 103, 16, -48, -124, -53, -12, -75, -102, 90, 19, -83, 13, 118, -93, 70,
        -19, 73, 3, 111, 70, -13, -42, -66, 110, -74, -55, -39, -35, -65, 1, -70, -27, -49, 90, -58, 122, -36, -33, 103, 119, 2, -81,
        -54, 64, -37, -82, 92, -11, -84, 103, -83, -51, -10, 119, 112, 42, -4, -128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 3, 111, 70, -13, -42, -66, 110, -74, -55, -39, -35, -65, 43, -14, -112,
        54, -12, 111, 61, 107, -26, -21, 108, -99, -99, -37, -16, 27, -82, 92, -11, -84, 103, -83, -51, -10, 119, 112, 42, -4, -92, 13,
        -70, -27, -49, 90, -58, 122, -36, -33, 103, 119, 2, -81, -56, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -112, 54, -12, 111, 61, 107, -26, -21, 108, -99, -99, -37, -14, -65, 41, 3, 111, 70,
        -13, -42, -66, 110, -74, -55, -39, -35, -65, 1, -70, -27, -49, 90, -58, 122, -36, -33, 103, 119, 2, -81, -54, 64, -37, -82, 92,
        -11, -84, 103, -83, -51, -10, 119, 112, 42, -4, -128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 3, 111, 70, -13, -42, -66, 110, -74, -55, -39, -35, -65, 43, -14, -112, 54, -12, 111, 61, 107,
        -26, -21, 108, -99, -99, -37, -16, 27, -82, 92, -11, -84, 103, -83, -51, -10, 119, 112, 42, -4, -92, 13, -70, -27, -49, 90,
        -58, 122, -36, -33, 103, 119, 2, -81, -56, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, -112, 54, -12, 111, 61, 107, -26, -21, 108, -99, -99, -37, -14, -65, 41, 3, 111, 70, -13, -42, -66, 110,
        -74, -55, -39, -35, -65, 1, -70, -27, -49, 90, -58, 122, -36, -33, 103, 119, 2, -81, -54, 64, -37, -82, 92, -11, -84, 103, -83,
        -51, -10, 119, 112, 42, -4, -128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 9, 3, 111, 70, -13, -42, -66, 110, -74, -55, -39, -35, -65, 43, -14, -112, 54, -12, 111, 61, 107, -26, -21, 108, -99,
        -99, -37, -16, 27, -82, 92, -11, -84, 103, -83, -51, -10, 119, 112, 42, -4, -92, 13, -70, -27, -49, 90, -58, 122, -36, -33,
        103, 119, 2, -81, -56, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        -112, 54, -12, 111, 61, 107, -26, -21, 108, -99, -99, -37, -14, -65, 41, 3, 111, 70, -13, -42, -66, 110, -74, -55, -39, -35,
        -65, 1, -70, -27, -49, 90, -58, 122, -36, -33, 103, 119, 2, -81, -54, 64, -37, -82, 92, -11, -84, 103, -83, -51, -10, 119, 112,
        42, -4, -128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, -32, 23, 99, 88, 126, 61, -75, -117, -106, -81, -1, 0, 49, 127, -57,
        127, -24, -10, -1, 0, -100, -107, -121, -4, -128, -4, 123, -7, 111, -16, 103, -11, -100, -70, -88, -25, 95, -104, -65, 20, -2,
        -41, 27, -7, 55, -15, -105, -20, -65, -70, -2, 61, -3, -106, 47, -5, -97, -16, -97, -41, 63, 107, -120, -2, 75, -12, -76, -8,
        -45, 11, -76, -34, -51, -19, -98, -33, -84, 86, 99, 117, 87, -85, 79, 79, -25, 23, 73, 108, -97, -108, -14, -46, -32, -22,
        -100, -92, -84, 50, -86, 73, 114, 114, -116, -90, -56, -54, 43, 52, -77, 87, 58, 49, 52, -108, -106, -109, -60, 83, 25, 31,
        -120, -85, -87, -54, -102, 85, 93, -53, 50, -78, -88, -59, -45, 83, 40, 77, 42, -52, 22, 92, 80, -14, -87, -27, 73, -123, -43,
        72, -63, -22, 80, -31, 107, 74, -38, 101, 98, 23, -49, -105, -103, -121, -102, -74, -71, 114, -76, 22, 101, 80, -71, 73, 13,
        61, 110, 105, 77, -15, 122, -70, -121, 47, 113, 89, 117, -128, -48, -64, 76, 38, -102, 51, -54, -38, 65, -103, -14, 74, 46,
        -93, -90, 105, -83, 124, -70, 81, 56, -58, -32, -22, -119, -60, -93, 7, 78, -29, -80, -46, 89, -50, -73, 7, 52, -41, -24, -54,
        38, 49, -61, 115, -18, 77, 109, -39, -39, 39, 112, 57, -25, 42, -74, -20, -93, -67, -68, -77, -86, -77, 126, -96, -98, 104,
        -45, 52, -44, -121, 91, 37, -52, 74, 110, 65, 86, 84, 90, -19, 127, -19, 112, 50, 58, 67, 49, 42, -70, 46, 71, -106, -43, 108,
        -38, 111, -117, -114, -122, 14, 67, -128, -90, -22, -23, -90, 42, -96, -58, 107, -80, -8, 73, 38, -85, 31, -120, -60, -31, -11,
        90, -48, -11, -84, 121, -67, -98, -101, 94, -10, 108, 91, 77, -57, -45, -10, -111, -98, 119, 111, -106, -39, 121, 112, 85, 30,
        38, -97, -63, -22, 40, 57, -90, 30, -84, -58, -22, 100, 88, -54, -85, 87, -122, -41, -45, -72, 74, -18, -78, -110, -45, -109,
        58, 11, 45, 113, 51, -116, 38, 59, 1, 48, -63, 106, 51, 14, -88, -91, -11, -70, -39, 92, -58, 93, 53, -1, 0, 72, 75, -90, 24,
        60, 86, -65, -88, -83, 114, -24, -14, 38, -12, 114, 38, -122, -71, 123, 104, -82, 127, 37, 100, -106, 101, 127, 102, -2, -107,
        90, -1, 0, 89, -84, 104, -17, -26, -65, -89, 86, 53, 13, 1, 81, -1, 0, -37, -107, -3, 61, 74, -43, -78, -17, -29, -86, -38, 86,
        125, 41, -1, 0, -85, 72, 112, 31, -68, -3, -121, -17, -16, 31, -70, -106, 98, -80, 88, -36, 64, 125, -4, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 18, 6, -34, -115, -25, -83, 124, -35, 109, -109, -77, -69, 126, 87, -27, 32, 109, -24, -34, 122,
        -41, -51, -42, -39, 59, 59, -73, -32, 55, 92, -71, -21, 88, -49, 91, -101, -20, -18, -32, 85, -7, 72, 27, 117, -53, -98, -75,
        -116, -11, -71, -66, -50, -18, 5, 95, -112, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 109, 98, -27, 101, -76, -77, -1, 0,
        -128, 47, 35, -73, 92, -58, 101, 119, 62, -13, 3, 45, 50, -97, -125, 107, 52, 115, -117, 44, 42, 28, -34, -53, 44, -72, -78,
        44, -44, -82, -85, 26, 74, -106, -94, 49, 121, -111, 59, -106, -54, -87, 59, 15, -76, 121, -20, 115, 39, 13, 68, -53, -80,
        -109, 9, -91, 65, -121, -55, 124, 78, 3, 15, -100, 83, 60, 44, -98, 95, 49, -99, 106, 101, 116, 38, 55, 25, 38, -105, -29, -90,
        -104, 108, 38, 19, 93, -86, 61, -91, 54, 115, -102, -73, -25, 108, -77, 123, 100, -53, 91, -88, -97, -38, 116, -125, 49, 103,
        -111, -109, 103, -91, 77, 76, 101, 125, 47, -103, -45, -68, -47, -56, 73, -43, 35, 87, -45, 21, -10, 73, 97, 52, 106, 121, -60,
        -105, 70, -122, -47, -83, -75, -45, -7, 36, -61, 21, 93, -55, 117, -8, -87, -58, 7, 9, 78, 107, -23, -8, -53, 113, -46, 122,
        -110, 111, -87, -113, -51, 106, 45, -104, 84, -114, -99, -58, 108, -108, -50, 90, 3, 50, -79, 84, 85, 7, -78, 127, 45, -82, 3,
        42, 104, -100, -85, -57, 82, 26, 53, 62, 59, 52, 41, 28, -28, -73, 106, 66, -34, 41, -67, 70, 50, -68, -48, -86, 100, 26, 52,
        110, 43, 47, -92, -44, 126, 10, 119, -120, -60, 104, -47, -43, 102, -115, 91, -82, -41, -23, -53, -76, 117, 84, -44, 53, 112,
        -57, -21, 3, 45, 55, 49, 78, 85, 27, 98, -77, 107, 108, 69, -54, 108, -96, -95, -21, -100, 94, 73, 86, 91, 51, 114, -102, -37,
        -15, 121, -125, -87, -96, 42, 124, -89, -63, 93, -83, -54, 82, 55, 73, 65, -25, 77, 101, -121, -93, 36, -11, -124, -94, -107,
        -97, -43, 117, 47, -4, 114, -94, -22, 124, -108, -58, -24, -51, -27, 24, 57, -57, -18, 102, 50, 105, 52, -57, 81, -121, -64,
        85, 82, 61, 108, -53, -122, -13, 71, 54, -23, -5, -120, -73, -38, 34, -47, -13, 63, 109, -74, 120, -52, 106, 26, 15, 19, 65,
        -52, -24, -53, 10, -93, 119, 116, -23, 122, 30, -30, -14, -73, 48, 50, -13, 31, 43, -102, 83, 20, -43, 1, -115, -53, 25, -107,
        23, -123, -93, 107, 10, 126, 115, 43, -61, -55, -76, -79, 84, 126, 110, 73, 48, -13, 108, 47, -17, 100, -77, 73, -114, 46, 69,
        54, -103, -32, -15, 84, -62, 1, -115, 60, -106, -71, -5, 98, -39, -7, 117, -101, 102, 109, 90, -2, 50, 39, 49, 107, 75, -117,
        -67, -69, -55, -50, 12, -37, -73, -22, 126, 95, -112, 117, -106, 106, -21, 47, 78, -37, 51, -122, -100, -106, 106, 50, 107, 34,
        -24, 106, -98, 69, 76, -49, 36, 58, -36, 29, 29, -84, -116, -18, 65, -113, -44, -42, 51, 122, 122, -118, -90, 38, 117, 28, -57,
        9, -118, -100, -31, 102, 18, 41, -10, 30, 91, -23, -114, -21, -82, -81, 89, -86, -40, 83, 99, 58, 58, -51, 13, 45, 94, -108,
        116, 46, 87, 89, 13, 29, 56, 71, 70, 49, -43, -21, -82, -2, -32, 53, -38, -83, 56, 66, 63, -6, -21, 53, 90, 122, 26, -51, 8,
        -1, 0, -29, 75, 67, 75, 71, 74, 31, -26, 17, -125, -21, -105, -89, -77, 114, -7, 110, 99, 50, -13, 87, 29, -109, 91, 95, 51,
        -54, -41, 45, -29, 60, 37, -78, 89, 86, 98, 91, -76, -109, 33, 50, -89, 50, 99, 40, -64, -31, 40, 121, 21, 5, 81, 97, 50, 103,
        56, 103, 83, 90, 123, 49, 50, 119, 7, 88, 96, 100, -102, -38, -126, 111, -122, -110, 107, -26, 122, 58, 21, -116, -2, -93, -97,
        106, 52, -31, -4, -100, 112, 122, -81, 71, -83, 82, -39, -14, -70, -51, -19, -49, 39, -83, 127, 37, -16, 24, -23, 126, 89, 100,
        -91, 21, 44, -94, -87, 125, 25, -74, 43, 87, -114, -99, 76, 53, 88, 56, -21, 113, 83, 57, -4, -1, 0, 27, -88, -61, -32, -16,
        -8, -54, -122, -89, -99, -30, -26, 85, 29, 65, -118, -62, -32, -80, 56, 60, 68, -26, 107, -114, -41, 96, -16, 24, 44, 54, -98,
        -85, 11, -87, 14, -127, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, -127, -73, -93, 121, -21, 95, 55, 91, 100, -20, -18,
        -33, -107, -7, 72, 27, 122, 55, -98, -75, -13, 117, -74, 78, -50, -19, -8, 13, -41, 46, 122, -42, 51, -42, -26, -5, 59, -72,
        21, 126, 82, 6, -35, 114, -25, -83, 99, 61, 110, 111, -77, -69, -127, 87, -28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 72, 27, 122, 55, -98, -75, -13, 117, -74, 78, -50, -19, -7, 95, -108,
        -127, -73, -93, 121, -21, 95, 55, 91, 100, -20, -18, -33, -128, -35, 114, -25, -83, 99, 61, 110, 111, -77, -69, -127, 87, -27,
        32, 109, -41, 46, 122, -42, 51, -42, -26, -5, 59, -72, 21, 126, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, -127, -73, -93, 121, -21, 95, 55, 91, 100, -20, -18, -33, -107, -7, 72, 27, 122,
        55, -98, -75, -13, 117, -74, 78, -50, -19, -8, 13, -41, 46, 122, -42, 51, -42, -26, -5, 59, -72, 21, 126, 82, 6, -35, 114, -25,
        -83, 99, 61, 110, 111, -77, -69, -127, 87, -28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 72, 27, 122, 55, -98, -75, -13, 117, -74, 78, -50, -19, -7, 95, -108, -127, -73, -93, 121, -21, 95,
        55, 91, 100, -20, -18, -33, -128, -35, 114, -25, -83, 99, 61, 110, 111, -77, -69, -127, 87, -27, 32, 109, -41, 46, 122, -42,
        51, -42, -26, -5, 59, -72, 21, 126, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 4, -127, -73, -93, 121, -21, 95, 55, 91, 100, -20, -18, -33, -107, -7, 72, 27, 122, 55, -98, -75, -13, 117, -74,
        78, -50, -19, -8, 13, -41, 46, 122, -42, 51, -42, -26, -5, 59, -72, 21, 126, 82, 6, -35, 114, -25, -83, 99, 61, 110, 111, -77,
        -69, -127, 87, -28, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        72, 27, 122, 55, -98, -75, -13, 117, -74, 78, -50, -19, -7, 95, -108, -127, -73, -93, 121, -21, 95, 55, 91, 100, -20, -18, -33,
        -128, -35, 114, -25, -83, 99, 61, 110, 111, -77, -69, -127, 87, -27, 32, 109, -41, 46, 122, -42, 51, -42, -26, -5, 59, -72, 21,
        126, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, -127, -73,
        -93, 121, -21, 95, 55, 91, 100, -20, -18, -33, -107, -7, 72, 27, 122, 55, -98, -75, -13, 117, -74, 78, -50, -19, -8, 28, 25,
        -77, 2, -8, -12, 54, 110, 95, 86, 70, -34, -106, -122, 87, -57, 58, 63, 12, 105, 102, 84, 127, 26, 70, -73, -122, 93, -24, 84,
        -65, -112, -14, 107, 48, -78, -97, -4, 106, -85, 79, -22, 85, -65, -15, 58, 50, 88, -42, -38, 83, -51, 45, 8, -45, -109, 120,
        78, 127, -116, -47, -111, -24, -57, 83, -6, -16, -100, 106, 117, -33, -57, 51, 31, 107, -40, 124, -40, -113, -119, -84, 2, 0,
        -33, -33, 28, -52, 125, -81, 97, -13, 98, 62, 38, -100, 115, 49, -10, -67, -121, -51, -120, -8, -102, -64, 32, 13, -3, -15,
        -52, -57, -38, -10, 31, 54, 35, -30, 105, -57, 51, 31, 107, -40, 124, -40, -113, -119, -84, 2, 0, -33, -33, 28, -52, 125, -81,
        97, -13, 98, 62, 38, -100, 115, 49, -10, -67, -121, -51, -120, -8, -102, -64, 32, 13, -3, -15, -52, -57, -38, -10, 31, 54, 35,
        -30, 105, -57, 51, 31, 107, -40, 124, -40, -113, -119, -84, 2, 0, -33, -33, 28, -52, 125, -81, 97, -13, 98, 62, 38, -100, 115,
        49, -10, -67, -121, -51, -120, -8, -102, -64, 32, 13, -3, -15, -52, -57, -38, -10, 31, 54, 35, -30, 105, -57, 51, 31, 107, -40,
        124, -40, -113, -119, -84, 2, 0, -33, -33, 28, -52, 125, -81, 97, -13, 98, 62, 38, -100, 115, 49, -10, -67, -121, -51, -120,
        -8, -102, -64, 32, 13, -3, -15, -52, -57, -38, -10, 31, 54, 35, -30, 105, -57, 51, 31, 107, -40, 124, -40, -113, -119, -84, 2,
        0, -33, -33, 28, -52, 125, -81, 97, -13, 98, 62, 38, -100, 115, 49, -10, -67, -121, -51, -120, -8, -102, -64, 32, 13, -3, -15,
        -52, -57, -38, -10, 31, 54, 35, -30, 105, -57, 51, 31, 107, -40, 124, -40, -113, -119, -84, 2, 0, -33, -33, 28, -52, 125, -81,
        97, -13, 98, 62, 38, -100, 115, 49, -10, -67, -121, -51, -120, -8, -102, -64, 32, 13, -3, -15, -52, -57, -38, -10, 31, 54, 35,
        -30, 105, -57, 51, 31, 107, -40, 124, -40, -113, -119, -84, 2, 0, -33, -33, 28, -52, 125, -81, 97, -13, 98, 62, 38, -100, 115,
        49, -10, -67, -121, -51, -120, -8, -102, -64, 32, 13, -3, -15, -52, -57, -38, -10, 31, 54, 35, -30, 105, -57, 51, 31, 107, -40,
        124, -40, -113, -119, -84, 2, 0, -33, -33, 28, -52, 125, -81, 97, -13, 98, 62, 38, -100, 115, 49, -10, -67, -121, -51, -120,
        -8, -102, -64, 32, 13, -3, -15, -52, -57, -38, -10, 31, 54, 35, -30, 105, -57, 51, 31, 107, -40, 124, -40, -113, -119, -84, 2,
        0, -33, -33, 28, -52, 125, -81, 97, -13, 98, 62, 38, -100, 115, 49, -10, -67, -121, -51, -120, -8, -102, -64, 32, 13, -3, -15,
        -52, -57, -38, -10, 31, 54, 35, -30, 105, -57, 51, 31, 107, -40, 124, -40, -113, -119, -84, 2, 0, -33, -33, 28, -52, 125, -81,
        97, -13, 98, 62, 38, -100, 115, 49, -10, -67, -121, -51, -120, -8, -102, -64, 32, 13, -3, -15, -52, -57, -38, -10, 31, 54, 35,
        -30, 105, -57, 51, 31, 107, -40, 124, -40, -113, -119, -84, 2, 0, -33, -33, 28, -52, 125, -81, 97, -13, 98, 62, 38, -100, 115,
        49, -10, -67, -121, -51, -120, -8, -102, -64, 32, 13, -3, -15, -52, -57, -38, -10, 31, 54, 35, -30, 105, -57, 51, 31, 107, -40,
        124, -40, -113, -119, -84, 2, 0, -33, -33, 28, -52, 125, -81, 97, -13, 98, 62, 38, -100, 115, 49, -10, -67, -121, -51, -120,
        -8, -102, -64, 32, 13, -3, -15, -52, -57, -38, -10, 31, 54, 35, -30, 107, 34, 27, 79, -17, -113, 67, 105, 29, -11, 103, -107,
        -23, 105, -27, 124, 114, 95, -13, 62, -106, 90, -57, -15, -92, 43, 120, 102, 38, -123, 53, -8, -13, 38, -78, -9, 41, -1, 0,
        -58, -74, -76, -2, -91, 68, 127, 45, -93, 58, -115, 19, -93, 60, -47, -48, -123, 57, 40, -124, -101, -7, 61, 41, 30, -108, 117,
        -33, -95, 25, -58, -69, -49, 48, 31, -1, -39
    };

	public ImageTest() {
		super();
	}

	@Test
	public void testCroppedImage() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		String syncToken = super.fetchSyncToken();
		/*
		 * create contact
		 */
    	String uid = randomUID();
    	String firstName = "bild";
    	String lastName = "otto";
        String vCard =
        		"BEGIN:VCARD" + "\r\n" +
        		"VERSION:3.0" + "\r\n" +
        		"PRODID:-//Apple Inc.//Address Book 6.1//EN" + "\r\n" +
				"N:" + lastName + ";" + firstName + ";;;" + "\r\n" +
				"FN:" + firstName + " " + lastName + "\r\n" +
        		"PHOTO;ENCODING=b;TYPE=JPEG;X-ABCROP-RECTANGLE=ABClipRect_1&11&11&25&25&ZNtYcAgH/lm2pubKd1ul0g==:" + "\r\n" +
        		" /9j/4AAQSkZJRgABAQAAAQABAAD/4QBARXhpZgAATU0AKgAAAAgAAYdpAAQAAAABAAAAGgAAAA" + "\r\n" +
        		" AAAqACAAQAAAABAAAAMKADAAQAAAABAAAAMAAAAAD/2wBDAAIBAQIBAQICAQICAgICAwUDAwMD" + "\r\n" +
        		" AwYEBAMFBwYHBwcGBgYHCAsJBwgKCAYGCQ0JCgsLDAwMBwkNDg0MDgsMDAv/2wBDAQICAgMCAw" + "\r\n" +
        		" UDAwULCAYICwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsL" + "\r\n" +
        		" Cwv/wAARCAAwADADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8" + "\r\n" +
        		" QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2Jy" + "\r\n" +
        		" ggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhI" + "\r\n" +
        		" WGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl" + "\r\n" +
        		" 5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAg" + "\r\n" +
        		" ECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl" + "\r\n" +
        		" 8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiI" + "\r\n" +
        		" mKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq" + "\r\n" +
        		" 8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD997+/g0qxnutUnitra2jaWaaVwkcSKMszMeAAASSeAB" + "\r\n" +
        		" Xzb8Q/2p/Evxu8KX3/AAyVEttoRXyP+ErvISY7t2YKP7Njz++GMnzm+UZ4BIr5r/4Ltf8ABRjw" + "\r\n" +
        		" /wDDPUdF+BDX+owL4miXUfGL2NvvuF0vdmOzhYsoD3DoVZs/LGrDOWxXT/sJf8FCvD/xN+Fc0m" + "\r\n" +
        		" n6BF4Y8I+FiILNdRu1DxQxpjzGVQEijHzKACw+Q4I6V8ZnmbVJYlYOjUdOP2pJat9ovp5ta9E0" + "\r\n" +
        		" 0z9jyHwzzFcPw4onhvaU3LSLso8t7JvVOTlK6jFXSiuaV00e2eF/2Zn+IehwH9ojW/G2v3MSEl" + "\r\n" +
        		" pNclt7QqQN37qHy9ucc8n+lfEf7b3j28/ZRa90L9hHxD8VtK8T2yiWc3WvTnS4YwxUGO2vA5nH" + "\r\n" +
        		" BCkFU64Lcivtrxv+2XY674EN14Rk0+0hlUNELpXJmXsxRSpRT1APPTPcD4a/a5/bG8UeF/DOp6" + "\r\n" +
        		" jdeFPA3jG3eEQ3dv5V0ktvEpZhPEvmMXZCxJClWxyM4wfBxWDoShfByfOvt68z8k27v8fI+q8N" + "\r\n" +
        		" 1z5qpZrhlOi2l7FtRpXvvJW5bK70vFX3dj1/9jH/AIK7+Lvh18L/AAvJ/wAFHNMWPw9rcKJY+P" + "\r\n" +
        		" 8ATLc/Zom8wxldYgBzasGG0zoDGSOQpNfo1YahBqtjBdaXPDc21zGssM0Th45UYZVlYcEEEEEc" + "\r\n" +
        		" EGv5o/C//BTSWfwHd/DXxxpA1/whrVzc3Fq8d2fPtTNGN0Y+UieLO47XxzIWLHAr9FP+Ddj/AI" + "\r\n" +
        		" KRQfEzS9S/Z78e3l3car4NtXv/AAncXEJ8y60kPh7eRwSC9u0iKpJ5jdRn5a9/hrO62Kf1bE3b" + "\r\n" +
        		" tdO33p/p1Ovxi8H45Fha3EGWU4woxquMoRleKjLWEoq7asmlNbapx0ufGP8AwUs+Inhb4x/8FR" + "\r\n" +
        		" Pjj/wtrUFiXSpU0DT5dyq1ubKBSu3dgZDtKMZAYE5POa8Q0j9ttdf8R6H4b8MRfZPCejW3lmAs" + "\r\n" +
        		" Gk1KSPayPOFxuVXLOEORuO45PTa/4ODvhZ/wp/8A4Kz/ABBhilKW3jq0stf3AFB+9hEbjGefng" + "\r\n" +
        		" bJzzz0zXxD4s1SX4fXVneabe/aLqQtG+6MISMd8d68qrlNWOPqV6jbvJtdr/0tO35fs2TcQUcT" + "\r\n" +
        		" wNhKNCklRhTgnrs9Fe2ju73dk93trzfpsn7XztYfvrwhY48uzPhT+J7V4p8ef+CiOm6BFcReHz" + "\r\n" +
        		" c67qK5VY7YkRIenzykYHTGF3H1r44tvFmq+JVt5PFF5NMoYGK33Yij567ehPuea+xf25f2RofE" + "\r\n" +
        		" uiXnjj4cJFa6nbRedqdoMLHfKBzKnYSDuON2M/e+97H1OnhqlKGJb9++2yatv6337nkYHh55pg" + "\r\n" +
        		" 51aEbSjbRLVp/8Ntu156HyN4c+O91b+KtX1PU9JsbdNUnMjLDGY1sweSqZ6KTyR68194f8EbfH" + "\r\n" +
        		" 1r8IP+Cqvwd1HRJY7k+KHn0G5kV/MjKXdu5AUKecSLFz0yuegr84ZNNh8W2zpdXk9ogdhsVB++" + "\r\n" +
        		" K4yDnByMjj8fp+gv8Awbl+CB8Z/wDgp34Bsrgt5Hw8sLvW1fYWUiKLyYlPPXM+Bk9gecVdXI6l" + "\r\n" +
        		" HFU8RRdkpa+ell+O/wB5WO4rjS4PzLKMztPD+xl7JvlupXu0rarTVN+i0sfqJ/wcNf8ABLxf2z" + "\r\n" +
        		" PgfH45+G9msnj7wWjTWOwMJLxNvz27NnG2QIgUHAEip/fav535/Cv/AAmGox2Oqo2narYu6Sw3" + "\r\n" +
        		" KNFKkijGx1PKsCMYIGDnNf2g3NtHeW8kN5GksUqlHR1DK6kYIIPBBHavzw/4Kb/8EDvBn7Y2/w" + "\r\n" +
        		" ASfCUJ4a8cQjdFfRbVeUhhtSUHCzIBx85DgDh+cV7GYYSVWLlT38t15n89+GXiHhuHv+E3Noc+" + "\r\n" +
        		" GlJN+Wq+a8n09D+fTWPgWV02zg8FTyz6tEjSMpO5bpgy/Ino2C2PXFfVnwN8XaX8b/hL4nT4v6" + "\r\n" +
        		" nqs8WnJF/Z8Ekj26w+WSPMm24ZyW2jaSQAQSC33dT4sf8ABFD9pD4DyLcwWtlrsIkLRNEkto7h" + "\r\n" +
        		" cdFZSD1Gfm71sfCD/gi9+0F8Z7NHuraz0WKJFR8Rz3Ui5B4YRhQBwSPnxxXlwwOZSoRVROSW0r" + "\r\n" +
        		" ary1t5dnv0P7Hw/G/BNKhUx+ExCo0pKF4RlHVw0Vpczs2nLm/m0b63+StX8LaZZXN7b6VpkGt6" + "\r\n" +
        		" xq19utEt4nlldyAgRI+S7MQexYnFfvR/wQH/AOCZEP7G3wVfxj47sYofHXi9RPqGVO61G35LdG" + "\r\n" +
        		" HGyNXdTjIMjyH+Fa0f+CYv/BDzwh+yBotrrvxHX+3vGM8QaTULlALiPJyY0XBFuhHVUO87vmbK" + "\r\n" +
        		" 4r7+t7eOzt44bSNIoolCIiKFVFAwAAOgA7V6mBwlWm3OvK7vp/W33H8o+MXivhOMOXLMloKnho" + "\r\n" +
        		" O7fWb6u+7V+rbb9D//2Q==" + "\r\n" +
        		"REV:2012-05-24T09:51:40Z" + "\r\n" +
				"UID:" + uid + "\r\n" +
        		"END:VCARD"
        	;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCard(uid, vCard));
        /*
         * verify contact on server
         */
        Contact contact = super.getContact(uid);
        super.rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("firstname wrong", firstName, contact.getGivenName());
        assertEquals("lastname wrong", lastName, contact.getSurName());
        assertEquals("wrong numer of images", 1, contact.getNumberOfImages());
        assertNotNull("no image found in contact", contact.getImage1());
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(contact.getImage1()));
        assertEquals("image width wrong", 25, image.getWidth());
        assertEquals("image height wrong", 25, image.getHeight());
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken);
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        assertTrue("PHOTO wrong", 0 < card.getVCard().getPhotos().size());
        byte[] vCardPhoto = card.getVCard().getPhotos().get(0).getPhoto();
        assertNotNull("POHTO wrong", vCardPhoto);
        BufferedImage vCardImage = ImageIO.read(new ByteArrayInputStream(vCardPhoto));
        assertEquals("POHTO width wrong", 25, vCardImage.getWidth());
        assertEquals("POHTO height wrong", 25, vCardImage.getHeight());
	}

	@Test
	public void testNegativeCropOffset() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create contact
		 */
    	String uid = randomUID();
    	String firstName = "bild";
    	String lastName = "wurst";
        String vCard =
        		"BEGIN:VCARD" + "\r\n" +
        		"VERSION:3.0" + "\r\n" +
        		"PRODID:-//Apple Inc.//Address Book 6.1//EN" + "\r\n" +
				"N:" + lastName + ";" + firstName + ";;;" + "\r\n" +
				"FN:" + firstName + " " + lastName + "\r\n" +
				"PHOTO;ENCODING=b;TYPE=JPEG;X-ABCROP-RECTANGLE=ABClipRect_1&-76&-76&200&200&XKZcdOASW3junIR92qq6RA==:" + "\r\n" +
				" /9j/4AAQSkZJRgABAQAAAQABAAD/4QBARXhpZgAATU0AKgAAAAgAAYdpAAQAAAABAAAAGgAAAA" + "\r\n" +
				" AAAqACAAQAAAABAAAAMKADAAQAAAABAAAAMAAAAAD/2wBDAAIBAQIBAQICAQICAgICAwUDAwMD" + "\r\n" +
				" AwYEBAMFBwYHBwcGBgYHCAsJBwgKCAYGCQ0JCgsLDAwMBwkNDg0MDgsMDAv/2wBDAQICAgMCAw" + "\r\n" +
				" UDAwULCAYICwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsL" + "\r\n" +
				" Cwv/wAARCAAwADADASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8" + "\r\n" +
				" QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2Jy" + "\r\n" +
				" ggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhI" + "\r\n" +
				" WGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl" + "\r\n" +
				" 5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAg" + "\r\n" +
				" ECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl" + "\r\n" +
				" 8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiI" + "\r\n" +
				" mKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq" + "\r\n" +
				" 8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD96/Fvi3TPAfhm+1nxnf2umaVpkLXF1d3MgjigjUZLMx" + "\r\n" +
				" 6Cvzp1X/gpT8aP+Cj3jTWPDH/BMHTdM8HeBdKmks9S+JHiGEyB2UHelhBg73CEPjaxUYJZMivD" + "\r\n" +
				" v+DkP9ufV/GnjGy+AXwgvjFpdl5V34nntyN01w/+rti4JwqI+9lIHzMAQdor70/4JofBHwN4D+" + "\r\n" +
				" COj2nwWvr+fRPDtgNG+wXVssRgmKo8sjMnEryFi5cdTIScE4H6vhOGqPC/D9LiHMaKqVa7fsoS" + "\r\n" +
				" TcIxVvfmtpOV1yRl7ltZc11E+VebRzXMKmXUKvKqfxWdpN9k+iXVrW+mi1fgWh/8EgtD8X3X9o" + "\r\n" +
				" ftM/tD/Gzx3rlwwkuGttTi0uzaTJJKQ7ZCASf7+a+uG/ZM8O6Xtm8A6nquhXqLhZ7WXymzjGS0" + "\r\n" +
				" Wxjkcda8K/aO8WaL+z943/sPSvEGqajrMbpJNDJbrHBao4DIC+cu20g8cep7V9NReIyYkIbqoP" + "\r\n" +
				" 6V83xZWxeKp4aviJ80JKTguRQSXu/DGMYpJ6bK3Y9LLIYanUqwoL3lbm1bd9d229dzk4fip4t+" + "\r\n" +
				" BOow2vxaD+JNAkBWPUbeMG7iwBgHG0SDg5BAfkkF+leyaBr9l4q0W21Hw5dQ3tjeRiWGaJtySK" + "\r\n" +
				" e4P+cV514mlg8S+H7ux1ILJFPGRg87TjIYe4NcH+zZ8RLnwH8Q18Ka5OzaRrRdrFXORa3QBcqp" + "\r\n" +
				" J+VZFDnHTzFyBmQk/GHsH4j/ABO1T/hc/wC078QfFHjWcSXur67eTMWxyPOYBfQAKAPwFfWnww" + "\r\n" +
				" /4KJ678PPgTovgn4fal/wj8lhM13fajG4a51GXcNvzdEQIsalcEsVOTg7a+Rv2nPhvN8IPj740" + "\r\n" +
				" 0XU45Yryw1q6hmGepErc8ccgg8cVwH9oH1l/76P+NfteN+kRwFiMNQy7H4evJUOVJezg0nFcu3" + "\r\n" +
				" tNUul12drpHNgfoFeMOZ1Z55lGY4SNPEx5k/bVLuE7TWqo6N6ap91ezZ9n/tdftot8btf0jxBq" + "\r\n" +
				" 8Vpb66lpFZX81tkR3zJIdkoTnY21grDJBxkYHyj9VYHb7NFkn/Vr39hX87E1ytwymcSMUIYZJO" + "\r\n" +
				" D19a+oE/4LH/HSNAq69o+FGBnRLY8D/gNflHiB4v8ACmdUsHRyalVjGlzpqUIqyly2S996Kz9F" + "\r\n" +
				" ZI/ZvD/6E/ijk31medYrCznUcWmqtR3tzXv+5Xderuz9g79pHt2SHcSwwT6CvKvitNJ4O1HStY" + "\r\n" +
				" sVQz6VeQ3iBgcMY5FfBwc4IUqeRwTX5pf8Pkfjr/0HtH/8Edr/APEV1/7Pv7cvxf8A2s/jl4Q8" + "\r\n" +
				" HeJdZ02SDXtUgtJQulwxARs43ksi5A2g8ivzuhxvl+JqRpQjPmk0lot27fzH2ub/AEWeL8jwNf" + "\r\n" +
				" McXXwyp0oSnK1Sp8MU5P8A5dLoj3P/AILg/sSy2PjiD4seDrNG0rVxHZ62sYUGC6AISYqAPldV" + "\r\n" +
				" ALc/MuSRuGfgzT/gy+qIGsoy+f7ozX9Fvinwtp3jbw7e6R4usrfUdM1GJoLm2nQPHMh6gg1+e/" + "\r\n" +
				" x3/wCCTXiT4Ta5ca5+y68XiPRizStol1II7y2GWO2J2IWVQAAASG5wAeteHxZwpWr1pY3BRvf4" + "\r\n" +
				" ore/dd79Vvf8Ps/Bfx8hlOVU+Hc4q8jpaUqkn7rj0hN/Zcdoyfu8tk2rLm/OP/hQd3/z7Sf980" + "\r\n" +
				" f8KDu/+faT/vmvubQfFth4WkNh8XPAuvaVfwko6yabJww4IyFwcHril1zxFB4ymFh8GPh/4k1m" + "\r\n" +
				" /lBEYh0uU5IGSc7ccAHv2NfnX1DFc/J7KV+1nf7rH7b/AMRexXN/DXJ/P7SHJbvzc1rfM+DtQ+" + "\r\n" +
				" Db6Wm68jKfUYr9BP8AgiD+w/cW3iOX4teOLAw6fbxPa6AJkwbiQ/LJcKCOVUblDf3icH5TXU/s" + "\r\n" +
				" 9f8ABJnXPiX4kh8QftXFNG0dSJYtCtJla6uDwVE7gFUTnlQS3BB21+gfhzw5YeENCtNM8MWkFh" + "\r\n" +
				" p9jEIbe3hQKkSDoABX6PwlwpVw1VY3GRs18Met+77eS3vqfhvjV49QzrK58O5PV5/aaVakfh5d" + "\r\n" +
				" +SD+1d/FJe7y6K93b//Z" + "\r\n" +
				"REV:2012-05-24T12:32:30Z" + "\r\n" +
				"UID:" + uid + "\r\n" +
        		"END:VCARD"
        	;
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCard(uid, vCard));
        /*
         * verify contact on server
         */
        Contact contact = super.getContact(uid);
        super.rememberForCleanUp(contact);
        assertEquals("uid wrong", uid, contact.getUid());
        assertEquals("firstname wrong", firstName, contact.getGivenName());
        assertEquals("lastname wrong", lastName, contact.getSurName());
        assertEquals("wrong numer of images", 1, contact.getNumberOfImages());
        assertNotNull("no image found in contact", contact.getImage1());
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(contact.getImage1()));
        assertEquals("image width wrong", 200, image.getWidth());
        assertEquals("image height wrong", 200, image.getHeight());
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        assertTrue("PHOTO wrong", 0 < card.getVCard().getPhotos().size());
        byte[] vCardPhoto = card.getVCard().getPhotos().get(0).getPhoto();
        assertNotNull("POHTO wrong", vCardPhoto);
        BufferedImage vCardImage = ImageIO.read(new ByteArrayInputStream(vCardPhoto));
        assertEquals("POHTO width wrong", 200, vCardImage.getWidth());
        assertEquals("POHTO height wrong", 200, vCardImage.getHeight());
	}

	@Test
	public void testAddPhotoOnServer() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create contact on server
		 */
    	String uid = randomUID();
    	String firstName = "test";
    	String lastName = "kimberly";
		Contact contact = new Contact();
		contact.setSurName(lastName);
		contact.setGivenName(firstName);
		contact.setDisplayName(firstName + " " + lastName);
		contact.setUid(uid);
		super.rememberForCleanUp(super.create(contact));
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        /*
         * update contact on server
         */
    	String updatedFirstName = "test2";
    	String udpatedLastName = "waldemar2";
		contact.setSurName(udpatedLastName);
		contact.setGivenName(updatedFirstName);
		contact.setDisplayName(updatedFirstName + " " + udpatedLastName);
		contact.setImage1(PNG_100x100);
        contact.setImageContentType("image/png");
		contact = super.update(contact);
        /*
         * verify contact on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        card = assertContains(uid, addressData);
        assertEquals("N wrong", updatedFirstName, card.getGivenName());
        assertEquals("N wrong", udpatedLastName, card.getFamilyName());
        assertEquals("FN wrong", updatedFirstName + " " + udpatedLastName, card.getFN());
        assertTrue("PHOTO wrong", 0 < card.getVCard().getPhotos().size());
        byte[] vCardPhoto = card.getVCard().getPhotos().get(0).getPhoto();
        assertNotNull("POHTO wrong", vCardPhoto);
        Assert.assertArrayEquals("image data wrong", contact.getImage1(), vCardPhoto);
    }

	@Test
	public void testAddPhotoOnClient() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create contact on server
		 */
    	String uid = randomUID();
    	String firstName = "test";
    	String lastName = "jaqueline";
		Contact contact = new Contact();
		contact.setSurName(lastName);
		contact.setGivenName(firstName);
		contact.setDisplayName(firstName + " " + lastName);
		contact.setUid(uid);
		super.rememberForCleanUp(super.create(contact));
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        /*
         * update contact on client
         */
        PhotoType photo = new PhotoType();
        photo.setImageMediaType(ImageMediaType.PNG);
        photo.setEncodingType(EncodingType.BINARY);
        photo.setPhoto(PNG_100x100);
        card.getVCard().addPhoto(photo);
		assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCardUpdate(card.getUID(), card.toString(), "\"" + card.getETag() + "\""));
        /*
         * verify updated contact on server
         */
        Contact updatedContact = super.getContact(uid);
        super.rememberForCleanUp(updatedContact);
        assertEquals("wrong numer of images", 1, updatedContact.getNumberOfImages());
        assertNotNull("no image found in contact", updatedContact.getImage1());
        /*
         * verify updated contact on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        card = assertContains(uid, addressData);
        assertTrue("PHOTO wrong", 0 < card.getVCard().getPhotos().size());
        byte[] vCardPhoto = card.getVCard().getPhotos().get(0).getPhoto();
        assertNotNull("POHTO wrong", vCardPhoto);
        Assert.assertArrayEquals("image data wrong", updatedContact.getImage1(), vCardPhoto);
    }

	@Test
	public void testRemovePhotoOnServer() throws Exception {
		/*
		 * fetch sync token for later synchronization
		 */
		SyncToken syncToken = new SyncToken(super.fetchSyncToken());
		/*
		 * create contact on server
		 */
    	String uid = randomUID();
    	String firstName = "test";
    	String lastName = "kimberly";
		Contact contact = new Contact();
		contact.setSurName(lastName);
		contact.setGivenName(firstName);
		contact.setDisplayName(firstName + " " + lastName);
		contact.setImage1(PNG_100x100);
        contact.setImageContentType("image/png");
		contact.setUid(uid);
		super.rememberForCleanUp(super.create(contact));
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        assertTrue("PHOTO wrong", 0 < card.getVCard().getPhotos().size());
        byte[] vCardPhoto = card.getVCard().getPhotos().get(0).getPhoto();
        assertNotNull("POHTO wrong", vCardPhoto);
        Assert.assertArrayEquals("image data wrong", contact.getImage1(), vCardPhoto);
        /*
         * update contact on server
         */
		contact.setImage1(null);
		contact.setNumberOfImages(0);
		contact = super.update(contact);
        /*
         * verify contact on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        card = assertContains(uid, addressData);
        assertTrue("PHOTO wrong", null == card.getVCard().getPhotos() || 0 == card.getVCard().getPhotos().size());
    }

	@Test
    public void testRemovePhotoOnClient() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken());
        /*
         * create contact on server
         */
        String uid = randomUID();
        String firstName = "test";
        String lastName = "kimberly";
        Contact contact = new Contact();
        contact.setSurName(lastName);
        contact.setGivenName(firstName);
        contact.setDisplayName(firstName + " " + lastName);
        contact.setImage1(PNG_100x100);
        contact.setImageContentType("image/png");
        contact.setUid(uid);
        super.rememberForCleanUp(super.create(contact));
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        assertTrue("PHOTO wrong", 0 < card.getVCard().getPhotos().size());
        byte[] vCardPhoto = card.getVCard().getPhotos().get(0).getPhoto();
        assertNotNull("POHTO wrong", vCardPhoto);
        Assert.assertArrayEquals("image data wrong", contact.getImage1(), vCardPhoto);
        /*
         * update contact on client
         */
        card.getVCard().removePhoto(card.getVCard().getPhotos().get(0));
        assertEquals("response code wrong", StatusCodes.SC_CREATED, super.putVCardUpdate(card.getUID(), card.toString(), "\"" + card.getETag() + "\""));
        /*
         * verify updated contact on server
         */
        Contact updatedContact = super.getContact(uid);
        super.rememberForCleanUp(updatedContact);
        assertEquals("wrong numer of images", 0, updatedContact.getNumberOfImages());
        assertNull("image found in contact", updatedContact.getImage1());
        /*
         * verify updated contact on client
         */
        eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        addressData = super.addressbookMultiget(eTags.keySet());
        card = assertContains(uid, addressData);
        assertTrue("PHOTO wrong", null == card.getVCard().getPhotos() || 0 == card.getVCard().getPhotos().size());
    }

	@Test
    public void testScalingImagesOnServer() throws Exception {
        /*
         * fetch sync token for later synchronization
         */
        SyncToken syncToken = new SyncToken(super.fetchSyncToken());
        /*
         * create contact on server
         */
        String uid = randomUID();
        String firstName = "chantalle";
        String lastName = "dick";
        Contact contact = new Contact();
        contact.setSurName(lastName);
        contact.setGivenName(firstName);
        contact.setDisplayName(firstName + " " + lastName);
        contact.setImage1(JPG_400x250);
        contact.setImageContentType("image/jpeg");
        contact.setUid(uid);
        super.rememberForCleanUp(super.create(contact));
        /*
         * verify contact on client
         */
        Map<String, String> eTags = super.syncCollection(syncToken).getETagsStatusOK();
        assertTrue("no resource changes reported on sync collection", 0 < eTags.size());
        List<VCardResource> addressData = super.addressbookMultiget(eTags.keySet());
        VCardResource card = assertContains(uid, addressData);
        assertEquals("N wrong", firstName, card.getGivenName());
        assertEquals("N wrong", lastName, card.getFamilyName());
        assertEquals("FN wrong", firstName + " " + lastName, card.getFN());
        assertTrue("PHOTO wrong", 0 < card.getVCard().getPhotos().size());
        byte[] vCardPhoto = card.getVCard().getPhotos().get(0).getPhoto();
        assertNotNull("POHTO wrong", vCardPhoto);
        Assert.assertTrue("image not scaled", contact.getImage1().length > vCardPhoto.length);
    }

}

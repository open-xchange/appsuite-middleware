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

package com.openexchange.ajax.share.bugs;

import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXRequest.Parameter;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.ajax.infostore.actions.GetDocumentRequest;
import com.openexchange.ajax.infostore.actions.GetDocumentResponse;
import com.openexchange.ajax.share.GuestClient;
import com.openexchange.ajax.share.ShareTest;
import com.openexchange.ajax.share.actions.ExtendedPermissionEntity;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageGuestObjectPermission;
import com.openexchange.file.storage.FileStorageObjectPermission;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Bug40548Test}
 *
 * [MacBook air]> "Drive"> "My files">"pictures"> changing to "tiles"or"icons" doesn't show a preview
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug40548Test extends ShareTest {

    /**
     * Initializes a new {@link Bug40548Test}.
     *
     * @param name The test name
     */
    public Bug40548Test(String name) {
        super(name);
    }

    public void testFilePreviewWithParallelGuestSessions() throws Exception {
        /*
         * create folder and a shared image inside
         */
        byte[] contents = {
            -1, -40, -1, -32, 0, 16, 74, 70, 73, 70, 0, 1, 1, 1, 0, 71, 0, 71, 0, 0, -1, -31, 0, 22, 69, 120, 105, 102, 0, 0, 77, 77, 0,
            42, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, -1, -37, 0, 67, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1,
            -37, 0, 67, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -64, 0, 17, 8, 0, 100, 0, 100, 3, 1, 34,
            0, 2, 17, 1, 3, 17, 1, -1, -60, 0, 29, 0, 1, 1, 1, 1, 0, 2, 3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 9, 11, 7, 6, 10, 2, 3, 8, 5, -1,
            -60, 0, 47, 16, 0, 0, 5, 2, 2, 10, 3, 0, 2, 3, 1, 0, 0, 0, 0, 0, 0, 5, 6, 7, 8, 3, 4, 2, 9, 1, 10, 20, 26, 57, 88, -120, -88,
            -72, -40, 21, 22, 23, 37, 38, 17, 24, 81, -111, -1, -60, 0, 20, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -60,
            0, 20, 17, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -38, 0, 12, 3, 1, 0, 2, 17, 3, 17, 0, 63, 0, -12, 126, -117,
            -111, 113, -10, -102, 47, -78, 26, 52, 70, -124, 55, -23, 79, 107, -107, -10, 111, -91, 34, -66, -52, -114, 71, 124, -41, -45,
            -111, -54, 21, -6, -113, -5, 26, -3, 66, -107, 73, 23, 124, 114, 73, 42, 124, 109, -4, -79, -11, -122, -39, -80, 108, 22, 27,
            81, -99, -43, -107, -107, -59, 127, -35, 113, -49, 95, -111, -98, -26, 97, -25, -80, 65, -86, -29, -57, 94, 12, -11, 51, -31,
            -28, -126, 26, -3, 0, -56, 23, 117, -57, 61, 126, 70, 123, -103, -121, -98, -63, 6, -21, -114, 122, -4, -116, -9, 51, 15, 61,
            -126, 26, -3, 0, 12, -127, 119, 92, 115, -41, -28, 103, -71, -104, 121, -20, 16, 110, -72, -25, -81, -56, -49, 115, 48, -13,
            -40, 33, -81, -48, 0, -56, 23, 117, -57, 61, 126, 70, 123, -103, -121, -98, -63, 6, -21, -114, 122, -4, -116, -9, 51, 15, 61,
            -126, 26, -3, 0, 12, -127, 119, 92, 115, -41, -28, 103, -71, -104, 121, -20, 16, 110, -72, -25, -81, -56, -49, 115, 48, -13,
            -40, 33, -81, -48, 0, -56, 23, 117, -57, 61, 126, 70, 123, -103, -121, -98, -63, 15, -49, -14, -113, 33, 92, -40, 97, 115, 18,
            -71, -110, -14, 94, 41, -2, 108, -55, 54, -65, 89, -5, -86, -41, -9, 56, -38, -79, -8, 95, -72, -84, 83, -56, 4, -25, -11,
            -60, 3, -62, -86, 86, -104, -4, -118, -75, 84, 66, 83, -4, 73, 13, -2, -57, -73, -19, -9, -5, 41, 101, -83, -19, -19, -74,
            -49, 66, 0, -21, 71, 112, 40, -100, -35, 51, 121, -121, 31, 64, 100, 15, -93, 30, 60, 58, 63, -58, 28, 88, -76, 104, -1, 0,
            -102, 49, 105, -47, -93, -1, 0, 52, 105, 1, -15, 0, 23, -5, 85, -57, -114, -68, 25, -22, 103, -61, -55, 4, 53, -6, 25, 2, -22,
            -72, -15, -41, -125, 61, 76, -8, 121, 32, -122, -65, 64, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, -21, 71, 112, 40, -100, -35, 51,
            121, -121, 31, 69, -2, 16, 7, 90, 59, -127, 68, -26, -23, -101, -52, 56, -6, 3, 32, 80, 0, 1, 127, -75, 92, 120, -21, -63,
            -98, -90, 124, 60, -112, 67, 95, -95, -112, 46, -85, -113, 29, 120, 51, -44, -49, -121, -110, 8, 107, -12, 0, 0, 0, 0, 57, -6,
            117, -40, 107, 21, -21, -73, 21, -83, 73, -71, 109, -6, -95, -51, 103, -2, -93, -6, -37, 116, -99, 89, 39, 14, -41, 109, 119,
            -24, 4, -11, -44, 72, 47, -47, 82, 37, -90, 87, 42, 4, 79, -35, -109, -10, -41, 39, -87, 31, -77, 23, -106, 125, -112, -98,
            -34, -71, -103, 54, -37, 101, 74, -91, 124, 61, 0, 0, 0, 0, 0, 0, 0, 64, 29, 104, -18, 5, 19, -101, -90, 111, 48, -29, -24,
            -65, -62, 0, -21, 71, 112, 40, -100, -35, 51, 121, -121, 31, 64, 100, 10, 0, 0, 47, -10, -85, -113, 29, 120, 51, -44, -49,
            -121, -110, 8, 107, -12, 50, 5, -43, 113, -29, -81, 6, 122, -103, -16, -14, 65, 13, 126, -128, 0, 0, 7, -81, 9, -108, -24, 85,
            53, -78, 71, 89, 108, -31, 10, -62, -58, 4, -30, -37, 47, -88, -63, 25, -34, -76, 115, -100, 78, -41, 24, -108, 57, -110, 21,
            85, 94, 11, 58, -113, 82, 120, -66, 88, 45, 9, -43, -59, -26, -18, -54, 125, -66, 55, 74, 89, -93, -101, -69, 82, -70, -88,
            -77, 20, -93, 122, 96, 108, -98, -79, 53, -45, 115, 117, 72, -42, -37, -14, -43, -66, 114, 25, -78, 53, 81, -122, 44, 102, 97,
            39, 34, 76, 65, 37, -53, -91, -43, 34, -114, 85, 30, 107, 54, -19, 106, -23, -30, -108, -88, 2, 103, -94, -118, 73, 51, 90,
            65, -39, -107, 29, -103, 24, 54, -123, -51, 73, -6, -43, 75, 104, -96, 111, -37, 111, -110, 90, 47, 104, -92, -108, 41, 50, 5,
            122, -86, -64, -34, -28, -39, 72, 93, -61, 44, -36, -42, -15, -1, 0, 84, 107, -107, 72, 70, 73, 104, -102, 117, 24, -27, -28,
            37, 102, 18, 8, -121, 93, 6, 113, 98, -89, 64, 44, 15, -102, -84, -76, 95, -108, -70, -6, -47, 46, -86, 41, -83, 116, 74, 127,
            77, 58, 121, 90, -123, -99, -35, -31, 85, -27, -35, -99, 77, 23, 22, -73, 22, -11, -22, -38, -35, 91, -42, -85, -3, 56, -25,
            -106, -42, 103, 83, -85, 46, 76, -71, -30, -37, -59, 41, -93, 5, 92, -84, -43, 44, 28, 40, 121, -42, -42, -59, 8, 7, 8, -70,
            103, 43, 27, -62, -92, 51, 110, -16, 17, -58, -45, -102, -8, -22, 95, 51, -75, 81, 73, 67, -21, 114, -108, -70, 101, 124, 87,
            114, 70, -85, -64, 72, -102, 78, 28, -88, 74, -108, 55, -74, -122, 36, -26, -63, -34, -28, -90, 122, -81, -38, -126, 84, -55,
            22, 6, 25, -71, 57, 69, -80, 72, -8, -108, -29, -103, -77, 75, -27, -26, 105, 18, -58, -27, -92, 84, -68, 14, -102, 82, -98,
            28, 11, -14, 102, 109, -82, 70, -86, 83, -54, -110, -28, -38, 64, -13, 104, 72, -24, 92, 42, 105, -34, -90, 79, 78, -20, 111,
            -76, -39, 95, -48, -87, 98, 98, 91, 97, -13, 79, -21, 3, 61, -78, 46, 59, -59, 84, -100, 61, -113, -20, -70, -69, 49, 57, 65,
            41, 95, -120, -117, 65, 12, -91, 116, 106, 44, -30, -86, 41, 89, 21, -46, -87, -27, -5, -28, -17, -39, -71, -83, -47, -115,
            43, -9, 45, -93, -73, 111, 86, -120, 37, -70, 98, -94, 64, -26, -98, 51, 18, 5, 77, -19, -47, 66, -123, 85, -11, -37, 13, 11,
            31, -70, 74, 100, 88, -8, -90, 101, 44, -119, -111, 16, -67, -76, -54, 62, 67, 38, -91, 83, -104, 108, -11, 56, 13, 94, 106,
            113, 50, -69, -80, 98, -38, 58, 106, -68, 52, -21, 47, -115, -102, -121, -111, 6, -102, 61, 112, 106, -91, 22, -121, 120, 107,
            -86, 49, 32, -113, 49, 89, -89, 83, -121, 119, -90, 85, -118, -88, -42, -84, 104, 97, 121, 113, -60, -28, 116, 1, 114, -95,
            115, 3, 17, -92, -126, -22, 86, -27, 101, 1, -26, -101, 21, 57, 92, -107, -60, 114, 57, 66, -57, -21, 56, -77, -105, -115, 92,
            82, 14, 61, 16, 34, -100, 88, -20, -32, -44, 77, 20, 89, -97, 92, 93, 46, -112, -52, 41, -23, -43, -13, -26, -74, -74, -94,
            -87, -67, 38, -75, -92, -37, -46, -85, 107, -114, -51, 26, 104, 86, 20, 90, 25, -26, 15, -104, -102, -81, 53, 3, -68, -74,
            103, 35, 39, 24, 27, -85, -12, 62, 95, 55, -14, -100, -43, 115, 31, -116, -36, -91, 57, 35, -98, -74, -1, 0, 97, -46, 109,
            -95, 58, -91, -76, 60, 93, 30, -40, 95, 16, 52, -105, 41, 85, 69, -22, 124, -59, 16, -78, 65, 85, 94, 88, 57, 72, -61, -13,
            42, 107, 91, -76, -83, -31, 89, 126, 59, -22, 61, 61, 114, -121, 112, 94, -119, 101, -98, -52, -108, -108, -114, 12, -125, 96,
            -91, 94, -106, -65, 45, 52, -44, 120, 115, 92, -8, -126, 74, -93, -91, 18, 27, -73, 41, 111, 35, 19, -82, 50, 77, -106, 99,
            -106, 106, -85, -13, 99, -41, 24, -122, -47, 32, -119, 62, 90, 27, -84, 77, 77, 46, -82, -17, -42, -121, -21, 34, -5, 108, 52,
            10, -119, -53, -88, 83, -9, 10, 0, 16, 7, 90, 59, -127, 68, -26, -23, -101, -52, 56, -6, 47, -16, -128, 58, -47, -36, 10, 39,
            55, 76, -34, 97, -57, -48, 25, 2, -128, 0, 11, -3, -86, -29, -57, 94, 12, -11, 51, -31, -28, -126, 26, -3, 12, -127, 117, 92,
            120, -21, -63, -98, -90, 124, 60, -112, 67, 95, -96, 0, 0, 1, -60, 17, -15, -106, 55, 55, -83, 50, -123, -124, 64, 71, -42,
            65, 14, -59, -85, -117, 84, 100, -54, -74, 93, 30, -44, 32, -45, 45, 50, -100, -95, 96, 89, -116, -111, 90, 84, -95, 110, 73,
            72, 44, -111, -25, 69, -86, -110, 106, -107, 10, 84, 118, 38, 68, -9, 54, -89, 101, -107, 49, -40, 25, -46, -70, -75, -59,
            -118, -106, -98, -98, -109, 73, -91, 80, 73, 84, -54, 21, 10, -103, 79, -94, -47, 40, -76, -7, 50, 77, 28, -114, 73, -109, 23,
            39, 18, -87, 52, -86, 112, -70, -40, -99, 60, -103, 76, -89, -119, -19, -84, -54, 8, 83, -28, 69, 22, 118, 101, 100, -60, -59,
            118, 118, -91, -59, 101, -42, -74, -42, 54, 54, -44, 45, -88, 82, -91, -121, -56, 0, 0, 120, 11, -104, -44, 53, -81, 82, 64,
            -63, -66, 121, 27, 84, 3, -76, -126, 54, -57, 70, -95, -86, 33, -52, 71, 39, 87, -119, 3, 60, 118, -40, -76, -29, -73, -58,
            96, -102, 84, -105, 26, -109, 94, -29, -95, -113, 78, -100, 84, 113, 92, -39, 84, -45, 75, 22, -99, 56, -80, 105, -61, -89,
            78, -99, 35, -49, -128, 7, 49, 105, 89, 54, 105, -125, 73, -45, 65, 49, 77, 35, 98, -53, 33, -87, 94, 87, 49, -92, -117, 105,
            80, 73, 86, -27, 39, 76, -62, -21, 5, 42, 119, 55, -44, -45, -120, -30, -94, 98, 124, 23, -105, 24, 40, 81, -63, 94, -25, 13,
            -98, -118, -43, 112, 81, -91, -122, -90, 60, 90, 41, -32, -47, -93, -89, 0, 0, 8, 3, -83, 29, -64, -94, 115, 116, -51, -26,
            28, 125, 23, -8, 64, 29, 104, -18, 5, 19, -101, -90, 111, 48, -29, -24, 12, -127, 64, 0, 5, -2, -43, 113, -29, -81, 6, 122,
            -103, -16, -14, 65, 13, 126, -122, 48, -71, 10, -54, 54, 38, 23, 102, -59, 20, -28, -68, -105, 92, -2, 106, -55, 54, -65, -71,
            -3, -43, 107, -11, -107, -118, -57, -31, 126, -29, 27, 94, 36, 2, 115, -6, -30, 1, 60, -86, 86, -104, -4, -118, -75, 84, 66,
            83, -4, 73, 13, -2, -57, -73, -19, -9, -5, 41, 101, -83, -19, -19, -66, -113, -101, -47, -39, 20, 115, -51, -37, 52, -61, -11,
            -12, 5, -2, 1, 0, 119, -93, -78, 40, -25, -101, -74, 105, -121, -21, -24, 111, 71, 100, 81, -49, 55, 108, -45, 15, -41, -48,
            23, -8, 4, 1, -34, -114, -56, -93, -98, 110, -39, -90, 31, -81, -95, -67, 29, -111, 71, 60, -35, -77, 76, 63, 95, 64, 95, -32,
            16, 7, 122, 59, 34, -114, 121, -69, 102, -104, 126, -66, -122, -12, 118, 69, 28, -13, 118, -51, 48, -3, 125, 1, 127, -128, 64,
            29, -24, -20, -118, 57, -26, -19, -102, 97, -6, -6, 27, -47, -39, 20, 115, -51, -37, 52, -61, -11, -12, 5, -2, 16, 7, 90, 59,
            -127, 68, -26, -23, -101, -52, 56, -6, 27, -47, -39, 20, 115, -51, -37, 52, -61, -11, -12, 72, 28, -6, -77, -22, -54, 122,
            105, 101, 61, 43, 35, 68, 104, -107, -97, -91, 61, -82, 87, -31, -97, 74, 69, 126, 25, 36, -111, -33, 53, -12, -23, 38, -50,
            -81, -44, 127, -40, -41, -20, -22, 85, 36, 93, -15, -55, 36, -87, -15, -73, -14, -57, -42, 27, 102, -63, -80, 88, 109, 70,
            119, 86, 118, 87, 1, -100, 24, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 15, -1, -39
        };
        FileStorageGuestObjectPermission guestPermission = randomGuestObjectPermission();
        String filename = randomUID();
        FolderObject folder = insertPrivateFolder(EnumAPI.OX_NEW, FolderObject.INFOSTORE, getDefaultFolder(FolderObject.INFOSTORE));
        File file = insertSharedFile(folder.getObjectID(), filename, guestPermission, contents);
        /*
         * check permissions
         */
        FileStorageObjectPermission matchingPermission = null;
        for (FileStorageObjectPermission permission : file.getObjectPermissions()) {
            if (permission.getEntity() != client.getValues().getUserId()) {
                matchingPermission = permission;
                break;
            }
        }
        assertNotNull("No matching permission in created file found", matchingPermission);
        checkPermissions(guestPermission, matchingPermission);
        /*
         * download preview using the sharing user's session
         */
        GetDocumentResponse getPreviewResponse = getPreview(getClient(), file.getFolderId(), file.getId());
        assertEquals(HttpServletResponse.SC_OK, getPreviewResponse.getStatusCode());
        assertNotNull(getPreviewResponse.getContentAsByteArray());
        /*
         * discover & check guest
         */
        ExtendedPermissionEntity guest = discoverGuestEntity(file.getFolderId(), file.getId(), matchingPermission.getEntity());
        checkGuestPermission(guestPermission, guest);
        /*
         * check access to share & download a preview, using the same ajax session as the sharing user
         */
        String shareURL = discoverShareURL(guest);
        AJAXSession sharedSession = getSession();
        String oldSessionID = sharedSession.getId();
        try {
            sharedSession.setId(null);
            GuestClient guestClient = new GuestClient(sharedSession, shareURL, guestPermission.getRecipient(), true, false);
            guestClient.checkShareModuleAvailable();
            guestClient.checkShareAccessible(guestPermission);
            getPreviewResponse = getPreview(guestClient, guestClient.getFolder(), guestClient.getItem());
            //FIXME requires different subdomain for guests, so don't verify this part for now
//            assertEquals(HttpServletResponse.SC_OK, getPreviewResponse.getStatusCode());
//            assertNotNull(getPreviewResponse.getContentAsByteArray());
        } finally {
            // restore sharing user's session ID for teardown
            sharedSession.setId(oldSessionID);
        }
        /*
         * download preview using the sharing user's session
         */
        getPreviewResponse = getPreview(getClient(), file.getFolderId(), file.getId());
        assertEquals(HttpServletResponse.SC_OK, getPreviewResponse.getStatusCode());
        assertNotNull(getPreviewResponse.getContentAsByteArray());
    }

    private static GetDocumentResponse getPreview(AJAXClient client, String folderID, String fileID) throws Exception {
        GetDocumentRequest getPreviewRequest = new GetDocumentRequest(folderID, fileID);
        getPreviewRequest.setAdditionalParameters(
            new Parameter("delivery", "view"),
            new Parameter("scaleType", "cover"),
            new Parameter("width", "200"),
            new Parameter("height", "150"),
            new Parameter("format", "thumbnail_image"),
            new Parameter("content_type", "image/jpeg")
        );
        String oldSessionID = client.getSession().getId();
        try {
            client.getSession().setId(null);
            return client.execute(getPreviewRequest);

        } finally {
            client.getSession().setId(oldSessionID);
        }
    }

}

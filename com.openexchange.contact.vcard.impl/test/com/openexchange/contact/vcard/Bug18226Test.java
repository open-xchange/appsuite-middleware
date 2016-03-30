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

package com.openexchange.contact.vcard;

import java.awt.image.BufferedImage;
import com.openexchange.groupware.container.Contact;
import com.openexchange.java.Streams;

/**
 * {@link Bug18226Test}
 *
 * Contact image missing when importing OX vcards
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug18226Test extends VCardTest {

    /**
     * Initializes a new {@link Bug18226Test}.
     */
    public Bug18226Test() {
        super();
    }

    public void testImportVCard() throws Exception {
        /*
         * import vCard
         */
        byte[] vCard = {
            66, 69, 71, 73, 78, 58, 86, 67, 65, 82, 68, 10, 86, 69, 82, 83, 73, 79, 78, 58, 51, 46, 48, 10, 80, 82, 79, 68, 73, 68, 58,
            79, 80, 69, 78, 45, 88, 67, 72, 65, 78, 71, 69, 10, 70, 78, 58, 87, 117, 114, 115, 116, 92, 44, 32, 72, 97, 110, 115, 10, 78,
            58, 87, 117, 114, 115, 116, 59, 72, 97, 110, 115, 59, 59, 59, 10, 80, 72, 79, 84, 79, 59, 69, 78, 67, 79, 68, 73, 78, 71, 61,
            66, 59, 84, 89, 80, 69, 61, 71, 73, 70, 58, 10, 32, 82, 48, 108, 71, 79, 68, 108, 104, 77, 65, 65, 119, 65, 76, 77, 65, 65,
            69, 90, 72, 69, 51, 90, 51, 73, 70, 108, 97, 71, 67, 85, 108, 67, 109, 112, 114, 72, 82, 85, 86, 66, 103, 103, 73, 65, 109,
            78, 107, 71, 122, 69, 120, 68, 88, 70, 121, 72, 49, 70, 82, 70, 106, 115, 55, 69, 66, 56, 102, 67, 86, 53, 102, 10, 32, 71,
            103, 65, 65, 65, 72, 108, 54, 73, 83, 72, 53, 66, 65, 65, 65, 65, 65, 65, 65, 76, 65, 65, 65, 65, 65, 65, 119, 65, 68, 65, 65,
            65, 65, 84, 47, 56, 77, 108, 74, 113, 55, 48, 52, 54, 56, 50, 55, 47, 50, 65, 111, 106, 108, 117, 81, 75, 65, 116, 84, 73, 65,
            111, 82, 107, 71, 69, 65, 70, 73, 98, 104, 10, 32, 79, 76, 87, 120, 74, 76, 65, 110, 70, 55, 100, 103, 85, 77, 102, 114, 98,
            82, 82, 65, 111, 100, 73, 65, 77, 71, 111, 79, 68, 75, 88, 85, 85, 82, 65, 52, 77, 81, 118, 98, 86, 68, 109, 52, 88, 104, 66,
            98, 97, 99, 70, 114, 105, 89, 97, 70, 104, 103, 97, 90, 89, 106, 52, 72, 71, 89, 76, 88, 117, 117, 50, 43, 10, 32, 73, 81,
            106, 114, 66, 55, 49, 101, 85, 75, 56, 82, 87, 110, 85, 52, 86, 109, 115, 67, 83, 89, 73, 77, 66, 51, 107, 66, 89, 73, 73, 79,
            65, 69, 86, 114, 83, 73, 52, 75, 99, 109, 115, 69, 65, 53, 83, 87, 97, 119, 67, 97, 101, 82, 73, 67, 103, 87, 100, 51, 110,
            119, 56, 74, 67, 52, 53, 78, 112, 81, 101, 72, 10, 32, 90, 119, 120, 43, 105, 54, 70, 49, 66, 103, 85, 75, 112, 81, 56, 65,
            111, 109, 69, 70, 65, 72, 103, 69, 68, 99, 68, 66, 68, 81, 101, 98, 73, 73, 67, 79, 67, 65, 65, 73, 68, 77, 122, 78, 68, 65,
            77, 76, 99, 83, 73, 69, 120, 52, 53, 110, 66, 110, 103, 105, 77, 114, 114, 87, 83, 103, 68, 70, 72, 114, 76, 100, 10, 32, 87,
            119, 79, 75, 73, 97, 102, 106, 89, 81, 97, 50, 73, 98, 110, 112, 89, 97, 81, 102, 104, 117, 47, 113, 55, 66, 48, 69, 114, 102,
            82, 83, 67, 79, 65, 88, 67, 88, 118, 54, 108, 70, 84, 104, 107, 77, 66, 100, 81, 72, 103, 99, 71, 110, 65, 55, 75, 67, 83, 82,
            66, 109, 111, 77, 114, 54, 109, 54, 73, 67, 79, 105, 10, 32, 71, 119, 89, 90, 53, 108, 108, 85, 49, 121, 57, 66, 111, 52, 49,
            98, 77, 65, 120, 73, 113, 53, 65, 70, 112, 68, 112, 73, 70, 81, 103, 65, 78, 68, 107, 69, 81, 83, 81, 74, 68, 102, 75, 120,
            70, 70, 73, 103, 50, 119, 82, 87, 77, 56, 80, 65, 107, 111, 65, 122, 53, 53, 83, 74, 80, 71, 88, 54, 100, 73, 67, 103, 10, 32,
            81, 115, 43, 104, 81, 103, 90, 69, 65, 65, 65, 55, 10, 65, 68, 82, 59, 84, 89, 80, 69, 61, 119, 111, 114, 107, 58, 59, 59, 59,
            59, 59, 59, 10, 65, 68, 82, 59, 84, 89, 80, 69, 61, 104, 111, 109, 101, 58, 59, 59, 59, 59, 59, 59, 10, 82, 69, 86, 58, 50, 48,
            49, 49, 48, 50, 48, 50, 84, 48, 57, 50, 55, 53, 56, 46, 49, 54, 57, 90, 10, 85, 73, 68, 58, 55, 64, 100, 101, 98, 105, 97, 110,
            53, 120, 54, 52, 46, 110, 101, 116, 108, 105, 110, 101, 46, 100, 101, 10, 69, 78, 68, 58, 86, 67, 65, 82, 68, 10
        };
        Contact contact = getMapper().importVCard(parse(vCard), null, null, null);
        /*
         * verify imported contact
         */
        assertNotNull(contact);
        assertEquals("Wurst, Hans", contact.getDisplayName());
        assertEquals("Wurst", contact.getSurName());
        assertEquals("Hans", contact.getGivenName());
        assertEquals("7@debian5x64.netline.de", contact.getUid());
        assertEquals("image/gif", contact.getImageContentType());
        assertNotNull(contact.getImage1());
        BufferedImage bufferedImage = javax.imageio.ImageIO.read(Streams.newByteArrayInputStream(contact.getImage1()));
        assertNotNull(bufferedImage);
        assertTrue(0 < bufferedImage.getWidth() && 0 < bufferedImage.getHeight());

    }

}

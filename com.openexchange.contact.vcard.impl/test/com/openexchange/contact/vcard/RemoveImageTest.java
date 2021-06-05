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

package com.openexchange.contact.vcard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

/**
 * {@link RemoveImageTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class RemoveImageTest extends VCardTest {

    /**
     * Initializes a new {@link RemoveImageTest}.
     */
    public RemoveImageTest() {
        super();
    }

         @Test
     public void testRemoveImageFromKeptVCard() throws Exception {
        VCardParameters parameters = getService().createParameters().setKeepOriginalVCard(true).setRemoveImageFromKeptVCard(true);
        /*
         * import vCard
         */
        String vCard =
            "BEGIN:VCARD\n" +
            "VERSION:3.0\n" +
            "PRODID:OPEN-XCHANGE\n" +
            "FN:Nachname, Vorname\n" +
            "N:Nachname;Vorname;;;\n" +
            "NICKNAME:Spitzname\n" +
            "PHOTO;ENCODING=BASE64;JPEG:/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAIBAQEBAQIBAQE\r\n" +
            " CAgICAgQDAgICAgUEBAMEBgUGBgYFBgYGBwkIBgcJBwYGCAsICQoKCgoKBggLDAsKDAkKCgr/\r\n" +
            " 2wBDAQICAgICAgUDAwUKBwYHCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKC\r\n" +
            " b2wxBqZRuiZQ8z9VmhGMY/wA803yB/dr4Z8a/tiftAaX8DPhLYzeM9U0a98WeCtH0tvFv9mW8\r\n" +
            " 39ra1qWnWcseqSLIirBYWkn2gXc8TP5L3EI8qTcNnrOp/tsa7P8AseD9prxf8N7zwesGtiwuN\r\n" +
            " Ni1D7Tv/eFVmhuJoofOjO5VJKrh1dRu2ZM+zZKg2fO+g/8ABEP45eC/Dmn+HPBX7ZWhab/Zt2\r\n" +
            " 9xAyfDS2kWF3WRWeNXlPlsVkKkrjhn5+Yg/Xn7HPwP+PHwC+GV74L/AGiv2kLn4natc6kLm21\r\n" +
            " u6077L9ni8qNPJEe9wVDozgZwN5AA6n1ouMnJpkpDPk9cD+ZqnVqTiqUn6GtStiffUqnNzO+/\r\n" +
            " /B8zlvG/wN+D3xF+IXhL4u+O/AdnqfibwFNdS+D9WuVYyaW1yiRzmIbsDesaA5B+6OlX/EXg3\r\n" +
            " wH4t1fT/EPizwRDqF7pyXENhczMzNCk8kbyqPRWaCIsvIOxfSttSxIKjpSPdvEyBEyd+1/xP/\r\n" +
            " 1qcXpyvdC5tDmdH+CHwg8K/EGb4w6B8PtKs/E95pkljJrUK4nFqxhJhz0K/wCj2/H/AExX0pu\r\n" +
            " jfBH4TaR8ZtX/AGgvDfw7tbXxd4g09LPVvXXKt9qnhQQqI3OcY220Axxwi/jxv7S/7eH7Kn7H\r\n" +
            " R0lP2g/iBeaJJrOuRaVYNa6PcXXm3MymRYz5UbhV2jJY4xxz1r0fwR498K+PtGbXfCGr/a7ZJ\r\n" +
            " 9j7w3CvijVraDxdbyWg/4lFlOVN2WP8AZ7GUhnJKv5Ifacule3Qz+QTKD3HanDUELEk9T6UKS\r\n" +
            " juVGVjxGe31HUdS1/4da/8Ask6APDvgrV4W8GzHT1mi1DzhPLK8UL2IS1O9V3PE8py+Tgnnn/\r\n" +
            " AmoQfD/wAeab4J8F/8E3tC0qz1LTJJ9V1/RdO+zR2p8yTFviLTVWUsYYGO50+/nB2Lv+jnuoZ\r\n" +
            " l2S9ByOKYLrys+SP0p+0iPnP/2Q==\r\n" +
            "ADR;TYPE=work:;;;;;;\n" +
            "ADR;TYPE=home:;;;;;;\n" +
            "REV:20090902T125118.045Z\n" +
            "UID:39614@192.168.33.100\n" +
            "END:VCARD\n"
        ;
        VCardImport vCardImport = importVCard(vCard, parameters);
        /*
         * verify imported vCard
         */
        assertNotNull(vCardImport.getContact());
        assertNotNull(vCardImport.getContact().getImage1());
        assertNotNull(vCardImport.getVCard());
        String originalVCard = Streams.stream2string(vCardImport.getClosingStream(), Charsets.UTF_8_NAME);
        assertTrue(originalVCard.contains("PHOTO"));
        assertTrue(originalVCard.length() < vCard.length() / 2);
    }

         @Test
     public void testDontRemoveImageFromKeptVCard_1() throws Exception {
        VCardParameters parameters = getService().createParameters().setKeepOriginalVCard(true).setRemoveImageFromKeptVCard(false);
        /*
         * import vCard
         */
        String vCard =
            "BEGIN:VCARD\n" +
            "VERSION:3.0\n" +
            "PRODID:OPEN-XCHANGE\n" +
            "FN:Nachname, Vorname\n" +
            "N:Nachname;Vorname;;;\n" +
            "NICKNAME:Spitzname\n" +
            "PHOTO;ENCODING=BASE64;JPEG:/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAIBAQEBAQIBAQE\r\n" +
            " CAgICAgQDAgICAgUEBAMEBgUGBgYFBgYGBwkIBgcJBwYGCAsICQoKCgoKBggLDAsKDAkKCgr/\r\n" +
            " 2wBDAQICAgICAgUDAwUKBwYHCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKC\r\n" +
            " b2wxBqZRuiZQ8z9VmhGMY/wA803yB/dr4Z8a/tiftAaX8DPhLYzeM9U0a98WeCtH0tvFv9mW8\r\n" +
            " 39ra1qWnWcseqSLIirBYWkn2gXc8TP5L3EI8qTcNnrOp/tsa7P8AseD9prxf8N7zwesGtiwuN\r\n" +
            " Ni1D7Tv/eFVmhuJoofOjO5VJKrh1dRu2ZM+zZKg2fO+g/8ABEP45eC/Dmn+HPBX7ZWhab/Zt2\r\n" +
            " 9xAyfDS2kWF3WRWeNXlPlsVkKkrjhn5+Yg/Xn7HPwP+PHwC+GV74L/AGiv2kLn4natc6kLm21\r\n" +
            " u6077L9ni8qNPJEe9wVDozgZwN5AA6n1ouMnJpkpDPk9cD+ZqnVqTiqUn6GtStiffUqnNzO+/\r\n" +
            " /B8zlvG/wN+D3xF+IXhL4u+O/AdnqfibwFNdS+D9WuVYyaW1yiRzmIbsDesaA5B+6OlX/EXg3\r\n" +
            " wH4t1fT/EPizwRDqF7pyXENhczMzNCk8kbyqPRWaCIsvIOxfSttSxIKjpSPdvEyBEyd+1/xP/\r\n" +
            " 1qcXpyvdC5tDmdH+CHwg8K/EGb4w6B8PtKs/E95pkljJrUK4nFqxhJhz0K/wCj2/H/AExX0pu\r\n" +
            " jfBH4TaR8ZtX/AGgvDfw7tbXxd4g09LPVvXXKt9qnhQQqI3OcY220Axxwi/jxv7S/7eH7Kn7H\r\n" +
            " R0lP2g/iBeaJJrOuRaVYNa6PcXXm3MymRYz5UbhV2jJY4xxz1r0fwR498K+PtGbXfCGr/a7ZJ\r\n" +
            " 9j7w3CvijVraDxdbyWg/4lFlOVN2WP8AZ7GUhnJKv5Ifacule3Qz+QTKD3HanDUELEk9T6UKS\r\n" +
            " juVGVjxGe31HUdS1/4da/8Ask6APDvgrV4W8GzHT1mi1DzhPLK8UL2IS1O9V3PE8py+Tgnnn/\r\n" +
            " AmoQfD/wAeab4J8F/8E3tC0qz1LTJJ9V1/RdO+zR2p8yTFviLTVWUsYYGO50+/nB2Lv+jnuoZ\r\n" +
            " l2S9ByOKYLrys+SP0p+0iPnP/2Q==\r\n" +
            "ADR;TYPE=work:;;;;;;\n" +
            "ADR;TYPE=home:;;;;;;\n" +
            "REV:20090902T125118.045Z\n" +
            "UID:39614@192.168.33.100\n" +
            "END:VCARD\n"
        ;
        VCardImport vCardImport = importVCard(vCard, parameters);
        /*
         * verify imported vCard
         */
        assertNotNull(vCardImport.getContact());
        assertNotNull(vCardImport.getContact().getImage1());
        assertNotNull(vCardImport.getVCard());
        String originalVCard = Streams.stream2string(vCardImport.getClosingStream(), Charsets.UTF_8_NAME);
        assertTrue(originalVCard.contains("PHOTO"));
        assertFalse(originalVCard.length() < vCard.length() / 2);
    }

         @Test
     public void testDontRemoveImageFromKeptVCard_2() throws Exception {
        VCardParameters parameters = getService().createParameters().setKeepOriginalVCard(false).setRemoveImageFromKeptVCard(true);
        /*
         * import vCard
         */
        String vCard =
            "BEGIN:VCARD\n" +
            "VERSION:3.0\n" +
            "PRODID:OPEN-XCHANGE\n" +
            "FN:Nachname, Vorname\n" +
            "N:Nachname;Vorname;;;\n" +
            "NICKNAME:Spitzname\n" +
            "PHOTO;ENCODING=BASE64;JPEG:/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAIBAQEBAQIBAQE\r\n" +
            " CAgICAgQDAgICAgUEBAMEBgUGBgYFBgYGBwkIBgcJBwYGCAsICQoKCgoKBggLDAsKDAkKCgr/\r\n" +
            " 2wBDAQICAgICAgUDAwUKBwYHCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKC\r\n" +
            " b2wxBqZRuiZQ8z9VmhGMY/wA803yB/dr4Z8a/tiftAaX8DPhLYzeM9U0a98WeCtH0tvFv9mW8\r\n" +
            " 39ra1qWnWcseqSLIirBYWkn2gXc8TP5L3EI8qTcNnrOp/tsa7P8AseD9prxf8N7zwesGtiwuN\r\n" +
            " Ni1D7Tv/eFVmhuJoofOjO5VJKrh1dRu2ZM+zZKg2fO+g/8ABEP45eC/Dmn+HPBX7ZWhab/Zt2\r\n" +
            " 9xAyfDS2kWF3WRWeNXlPlsVkKkrjhn5+Yg/Xn7HPwP+PHwC+GV74L/AGiv2kLn4natc6kLm21\r\n" +
            " u6077L9ni8qNPJEe9wVDozgZwN5AA6n1ouMnJpkpDPk9cD+ZqnVqTiqUn6GtStiffUqnNzO+/\r\n" +
            " /B8zlvG/wN+D3xF+IXhL4u+O/AdnqfibwFNdS+D9WuVYyaW1yiRzmIbsDesaA5B+6OlX/EXg3\r\n" +
            " wH4t1fT/EPizwRDqF7pyXENhczMzNCk8kbyqPRWaCIsvIOxfSttSxIKjpSPdvEyBEyd+1/xP/\r\n" +
            " 1qcXpyvdC5tDmdH+CHwg8K/EGb4w6B8PtKs/E95pkljJrUK4nFqxhJhz0K/wCj2/H/AExX0pu\r\n" +
            " jfBH4TaR8ZtX/AGgvDfw7tbXxd4g09LPVvXXKt9qnhQQqI3OcY220Axxwi/jxv7S/7eH7Kn7H\r\n" +
            " R0lP2g/iBeaJJrOuRaVYNa6PcXXm3MymRYz5UbhV2jJY4xxz1r0fwR498K+PtGbXfCGr/a7ZJ\r\n" +
            " 9j7w3CvijVraDxdbyWg/4lFlOVN2WP8AZ7GUhnJKv5Ifacule3Qz+QTKD3HanDUELEk9T6UKS\r\n" +
            " juVGVjxGe31HUdS1/4da/8Ask6APDvgrV4W8GzHT1mi1DzhPLK8UL2IS1O9V3PE8py+Tgnnn/\r\n" +
            " AmoQfD/wAeab4J8F/8E3tC0qz1LTJJ9V1/RdO+zR2p8yTFviLTVWUsYYGO50+/nB2Lv+jnuoZ\r\n" +
            " l2S9ByOKYLrys+SP0p+0iPnP/2Q==\r\n" +
            "ADR;TYPE=work:;;;;;;\n" +
            "ADR;TYPE=home:;;;;;;\n" +
            "REV:20090902T125118.045Z\n" +
            "UID:39614@192.168.33.100\n" +
            "END:VCARD\n"
        ;
        VCardImport vCardImport = importVCard(vCard, parameters);
        /*
         * verify imported vCard
         */
        assertNotNull(vCardImport.getContact());
        assertNotNull(vCardImport.getContact().getImage1());
        assertNull(vCardImport.getVCard());
    }

}

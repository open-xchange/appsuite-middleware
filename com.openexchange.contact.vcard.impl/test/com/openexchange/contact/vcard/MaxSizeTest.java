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

import com.openexchange.contact.vcard.impl.internal.VCardExceptionCodes;
import com.openexchange.contact.vcard.impl.internal.VCardParametersImpl;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

/**
 * {@link MaxSizeTest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class MaxSizeTest extends VCardTest {

    /**
     * Initializes a new {@link MaxSizeTest}.
     */
    public MaxSizeTest() {
        super();
    }

    public void testImportTooLargeVCard() throws Exception {
        String vCardString =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\r\n" +
            "PRODID:-//Apple Inc.//Address Book 6.1//EN\r\n" +
            "N:;;;;\r\n" +
            "FN:Apple GmbH\r\n" +
            "ORG:Apple GmbH;\r\n" +
            "item1.ADR;type=WORK;type=pref:;;Arnulfstra\u00dfe 19;M\u00fcnchen;;80335;Germany\r\n" +
            "item1.X-ABADR:de\r\n" +
            "item2.URL;type=pref:http://www.apple.de\r\n" +
            "item2.X-ABLabel:_$!<HomePage>!$_\r\n" +
            "PHOTO;ENCODING=b;TYPE=JPEG:/9j/4AAQSkZJRgABAQAAAQABAAD/4gLQSUNDX1BST0ZJTEUA\r\n" +
            " AQEAAALAYXBwbAIAAABtbnRyUkdCIFhZWiAH1wAJAAQAAAAAAABhY3NwQVBQTAAAAAAAAAAAAA\r\n" +
            " AAAAAAAAAAAAAAAAAAAAAA9tYAAQAAAADTLWFwcGwrE5C12jl7mNFiWx8hc7nyAAAAAAAAAAAA\r\n" +
            " AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAxyWFlaAAABFAAAABRnWFlaAAABKAAAABRiWFlaAAABPA\r\n" +
            " AAABR3dHB0AAABUAAAABRjaGFkAAABZAAAACxyVFJDAAABkAAAAA5nVFJDAAABoAAAAA5iVFJD\r\n" +
            " AAABsAAAAA52Y2d0AAABwAAAADBuZGluAAAB8AAAADhkZXNjAAACKAAAAHRjcHJ0AAACnAAAAC\r\n" +
            " RYWVogAAAAAAAAeb4AAEGFAAAEtFhZWiAAAAAAAABW9wAArFsAABz2WFlaIAAAAAAAACYhAAAS\r\n" +
            " OgAAsXtYWVogAAAAAAAA81EAAQAAAAEW03NmMzIAAAAAAAEMQwAABd7///MlAAAHkwAA/ZD///\r\n" +
            " uh///9ogAAA9wAAMBqY3VydgAAAAAAAAABAc0AAGN1cnYAAAAAAAAAAQHNAABjdXJ2AAAAAAAA\r\n" +
            " AAEBzQAAdmNndAAAAAAAAAABAAEAAAAAAAAAAQAAAAEAAAAAAAAAAQAAAAEAAAAAAAAAAQAAbm\r\n" +
            " RpbgAAAAAAAAAwAACgAQAAVwsAAEetAACYUgAAJ60AABHsAABQDAAAVDkAAczNAAHMzQABzM1k\r\n" +
            " ZXNjAAAAAAAAABpDYWxpYnJhdGVkIFJHQiBDb2xvcnNwYWNlAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" +
            " AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" +
            " AAAAAHRleHQAAAAAQ29weXJpZ2h0IEFwcGxlLCBJbmMuLCAyMDA3AP/hAEBFeGlmAABNTQAqAA\r\n" +
            " AACAABh2kABAAAAAEAAAAaAAAAAAACoAIABAAAAAEAAAFAoAMABAAAAAEAAAFAAAAAAP/bAEMA\r\n" +
            " AgEBAgEBAgIBAgICAgIDBQMDAwMDBgQEAwUHBgcHBwYGBgcICwkHCAoIBgYJDQkKCwsMDAwHCQ\r\n" +
            " 0ODQwOCwwMC//bAEMBAgICAwIDBQMDBQsIBggLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsL\r\n" +
            " CwsLCwsLCwsLCwsLCwsLCwsLCwsLC//AABEIAUABQAMBIgACEQEDEQH/xAAfAAABBQEBAQEBAQ\r\n" +
            " AAAAAAAAAAAQIDBAUGBwgJCgv/xAC1EAACAQMDAgQDBQUEBAAAAX0BAgMABBEFEiExQQYTUWEH\r\n" +
            " InEUMoGRoQgjQrHBFVLR8CQzYnKCCQoWFxgZGiUmJygpKjQ1Njc4OTpDREVGR0hJSlNUVVZXWF\r\n" +
            " laY2RlZmdoaWpzdHV2d3h5eoOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPE\r\n" +
            " xcbHyMnK0tPU1dbX2Nna4eLj5OXm5+jp6vHy8/T19vf4+fr/xAAfAQADAQEBAQEBAQEBAAAAAA\r\n" +
            " AAAQIDBAUGBwgJCgv/xAC1EQACAQIEBAMEBwUEBAABAncAAQIDEQQFITEGEkFRB2FxEyIygQgU\r\n" +
            " QpGhscEJIzNS8BVictEKFiQ04SXxFxgZGiYnKCkqNTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZW\r\n" +
            " ZnaGlqc3R1dnd4eXqCg4SFhoeIiYqSk5SVlpeYmZqio6Slpqeoqaqys7S1tre4ubrCw8TFxsfI\r\n" +
            " ycrS09TV1tfY2dri4+Tl5ufo6ery8/T19vf4+fr/2gAMAwEAAhEDEQA/AP38ooooAKKKKACiii\r\n" +
            " gAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKK\r\n" +
            " KACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAoo\r\n" +
            " ooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACi\r\n" +
            " iigAooooAKKKKACiiigAooooAKKKKACiiigAqj4k8T6d4P0iW/8AFF7b2FnCMvNO4RR7c9SewH\r\n" +
            " JriPj/APtHaR8C9IC3WL7WrlC1tYo2CR03yH+FM/icYHcj4p+Jvxb174u64b7xtfPcEE+VAvyw\r\n" +
            " W4PaNOg+vU45JoA+ifih/wAFBrLT5JLb4T6ab9xkC8vQY4fqsQwzD6lfpXh3jL9pvxz44kb+1f\r\n" +
            " EV9BE3/LGzb7NGB6YjwWH+8TXBUUAfU3/BPPX9U1m58VJq17eXdtCtsyrNK0gR2MuSMngkLz64\r\n" +
            " FfTVeD/8E/fCjaR8JL7U51w2sXzFD/ejjAQf+P8AmV7xQAUUUUAFFFFABRRRQAUUUUAFFFFABR\r\n" +
            " RRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFcJ+0H8cLP4G+CHvr\r\n" +
            " gJPqV1mKwtif9dJjlm77FyCT9B1IrtdS1GDSNPnu9TlSC2tY2llkY4WNFGSx9gATX59/H74w3P\r\n" +
            " xq+Il1qk++Oxi/cWMBP+phB4yP7zfePucdAKAOZ8U+KL/xp4gutU8TXMl3fXjmSWVzyT6D0AGA\r\n" +
            " AOAAAKz6KKACpLO0l1C7igskaWadxHGijJdicAD3JNR17j+w78HW8b/EP/hINWiJ0zw8wdCR8s\r\n" +
            " 1z1QD/AHfvn0O31oA+sPhb4KT4dfDvRtEh2k6dapFIy9Hkxl2/Fix/Gt+iigAooooAKKKKACii\r\n" +
            " igAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooA8I\r\n" +
            " /b1+JzeFfhvbaDpshS68QSES4PK28eCw9tzFB7jcK+Nq9a/bX8YN4p+PeoQq+6DR4o7GPnjgb3\r\n" +
            " /wDH3YfhXktABRRXUfCn4Q638Y/Eaad4Pti4BBnuHBENqv8Aedu3fA6nsKAIfhb8MdT+LnjG20\r\n" +
            " bwvGWlmO6WUg7LaMfekc9gP1OAOTX6AfDX4eaf8LPBdlonhtCLezTBdh88znlnb3J59ug4ArL+\r\n" +
            " CfwS0n4IeFRYeH1865mw93eOoEl0/v6KOcL29yST2VABRRRQAUUUUAFFFFABRRRQAUUUUAFFFF\r\n" +
            " ABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUyedLaFpLl0jjQZZmOAo9ST0rz/wAW/tWe\r\n" +
            " AvBzvHf+IbW5mQ48uzVrk59NyAqPxIoA9DorwfUP+ChPg62m22Om+ILlR1cQxID9MyZ/QVBaf8\r\n" +
            " FEPCj/APH9o3iGPj/lmkL/AM5BQB7/AEV5P4X/AG1fh/4llWObU7jS5H4C3tuyD8XXco/EivTt\r\n" +
            " H1yy8RWKXWgXlrfW0n3ZbeVZUb6MpIoAtUUUUAfnD8Xr2TUfiv4mnuc75dVumIPb963H4VR8K+\r\n" +
            " CNY8c332bwfpl7qU3dbeFn2+7EcKPc4r79vvgH4L1PxBPqmp+GtJub66kMssksAcSOeSxU/KST\r\n" +
            " yTjmuo07TbbSLRLfSbeC1gj4WOGMIi/QDgUAfKvwl/YB1DUpYrv4vXa6fb8MbG1cPO/s8nKp/w\r\n" +
            " AB3fUV9O+DvBOlfD/QotM8HWMFhZQ9I4x9492YnlmPckkmtSgnHWgAorkPFvx88G+Biy+JvEem\r\n" +
            " QyJ96KOXzpV+sce5v0rgNZ/b78Daa7LYR65qGOjQ2qqp/wC/jqf0oA9uor55l/4KKeHRIfJ0HW\r\n" +
            " mXsWaIE/huNaWif8FAfBeouq6taa5p7HqzwJIg/FHLf+O0Ae6UVyXgn48eD/iI6J4S8QafcTyf\r\n" +
            " dgd/Jmb6RyAMfwFdbQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUVV1vW\r\n" +
            " 7Tw5pNxf69cRWlnaIZJppGwsajuTQBZdxGhaQhVUZJJwAK8G+Nv7c2keCpJdP+GUcWuakhKvcM\r\n" +
            " x+xwn2I5lP8AukD/AGu1eSftLftb3vxXmm0jwQ89h4cUlXP3Zb/3f0T0Tv1PoPFKAOn+Ifxk8S\r\n" +
            " /FO7aTxvq11dx5ytuG2QR/7sa4UfXGfU1zFFFABRRRQAVqeFPG2r+BdSF34P1K8024HV4JSm4e\r\n" +
            " jAcMPY5FZdFAH0T8Nf8AgoJrGkeXb/E/TotWhHBurXENwPcp9xj7DZXvPgf9p7wP49hQ6Xr1na\r\n" +
            " zv/wAu96wtpQfTD4DH/dJr8/aKAP0y/wCEm037OZv7QsfKXq/nrtH45xXG+Mv2ovAvgdWGp+IL\r\n" +
            " O5mX/ljZE3Lk+n7vIU/7xFfn/RQB9M/EH/gofcT+ZD8MNFSBTwtzqDb3+oiQ4B+rH6V4j45+OH\r\n" +
            " iz4jlx4w12/uYZOtur+VB/37TC/pXKUUAFFFFABRRRQAV6Z8Kf2sPF/wALJI4or5tW01MA2d8x\r\n" +
            " kVR6I/3k9sHHsa8zooA/QD4I/tH+H/jfYhdHk+xatGu6bT52Hmr6sh6SL7jkdwM16BX5kaRrF1\r\n" +
            " 4f1SC90S4mtLu2cSRTRMVeNh3BFfaf7LH7UEXxl0/+yvFZjt/ElpHubA2pfIOsiDsw/iX8RxkK\r\n" +
            " AeyUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFADZZVgiZ52VEQFmZjgKB1JNfE37WP7\r\n" +
            " Ssvxc199I8KzMnhuwkwu04+3yD/lo3+yP4R+J5OB6r+3Z8cT4Z8Px+EfDkxW+1VPMvmQ8xW/IC\r\n" +
            " Z7FyDn/ZB/vV8i0AFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABV/wv\r\n" +
            " rt94Z8RWWoeGZZIdQtJlkgaPlt4PAx3z0x3ziotE0S78Savb2Gg28t3eXbiOGGMZaRj0Ar7S/Z\r\n" +
            " s/ZO0/4RWsWqeLVg1DxI43b8borH/Ziz1b1fr2GBnIB6f4G1u78SeDtMv9esZdNvbu2SSe1kGG\r\n" +
            " gcj5lIPI59ecdea1aKKACiiigAooooAKKKKACiiigAooooAKKKKACquuazb+HdFu9Q1eQRWtjC\r\n" +
            " 9xM5/hRVLE/kDVqvF/26vHZ8K/BdtOtX23Gv3C23BwREvzufp8qqf9+gD5D+JHjm6+JXjrU9c1\r\n" +
            " cnztRnMgXOfLToiD2VQq/hWHRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRR\r\n" +
            " QAUdTxRXtf7E/wAFl+I3xAbWdeiEmk+Hysm1h8s9weUX3C4LH6KDwaAPZ/2Pf2cU+GXh5Nf8XW\r\n" +
            " 4/4SDUo8ojrzYRHog9HYct3H3fXPt1FFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAV8e\r\n" +
            " /wDBQnxWdT+J2l6TG2YtKsfMI9JJWJP/AI6kdfYVfn9+1Lrp8QftAeKJmbcIrv7MPbylWPH/AI\r\n" +
            " 4aAOAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAK+//ANl7wAvw6+CW\r\n" +
            " i2rJsuryIX11kYJklAbB91Xav/Aa+EfB+jf8JH4t0vTjn/T7yK24/wBtwv8AWv0uRBGgWMBVUY\r\n" +
            " AHQCgBaKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAr82viZeHUPiP4guJDlp9SuZCf\r\n" +
            " UmVj/Wv0lr80/GyGPxnq6t1W9mB/7+NQBl0UUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFF\r\n" +
            " FABRRRQAUUUUAdb8BYhN8bfCSv0/te1P5Sqf6V+idfm/8ACfVV0P4peG72Y4S11S2lY/7KyqT+\r\n" +
            " gNfpBQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABX5xfGGxOmfFrxRbkY8nVrpR9\r\n" +
            " BM2P0r9Ha+Bf2tNGOh/tC+JY8YWadLhT6+ZGjn9WP5UAec0UUUAFFFFABRRRQAUUUUAFFFFABR\r\n" +
            " RRQAUUUUAFFFFABRRRQAUUUUAKrFGBQkEHII6iv0g+FvjFPiB8OtF1mFgx1C0jlkx/DJjDr+DB\r\n" +
            " h+Ffm9X1l/wT6+Jy6j4b1HwpqEg8/TnN5aKTy0Tn5wP91yD/20oA+jqKKKACiiigAooooAKKKK\r\n" +
            " ACiiigAooooAKKKKACiiigAr48/4KEeGjp3xU0zU0XEep6eEJx1kjcg/+OvHX2HXgn/BQTwgdY\r\n" +
            " +Fmn6tAu59GvQHOPuxyjaT/wB9rEPxoA+OqKKKACiiigAooooAKKKKACiiigAooooAKKKKACii\r\n" +
            " igAooooAKKKKACug+FvxDvPhX4807XdF+aSyky8ecCaM8Oh+qkj2OD2rn6KAP0w8KeJ7Pxp4bs\r\n" +
            " tW8Pyiaz1CFZom74I6EdiOhHYgitCvjv8AYx/aPj+H+pf8Ix42uBHo1/JutZ5G+WymPUEnojHv\r\n" +
            " 0Dc9ya+xAcjI5zQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFc78W/BQ+Ivw01vRWAL39\r\n" +
            " o6RZ6CUDdGfwcKfwroqKAPzBkjaGRklUq6khgRgg+hptem/tc/Dv8A4V58b9UW3j2Weqn+0bfA\r\n" +
            " 4xISXA+kgcY9MV5lQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFfR\r\n" +
            " P7L/7Y/wDwh9tb+HfitJLLpqYjtL85Z7QdkkHVkHYjlenI+787UUAfp3Y30Op2cVxp00VxbzqH\r\n" +
            " jljYMkinkEEcEH1qWvhP9mL40eKvBvjrSdD8K3X2jT9VvY7d7K4BeFd7gM6jOUIBJypGccg192\r\n" +
            " UAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFAHhv7dvwvPjD4Yxa5pse688OuZHwOWt3wH/7\r\n" +
            " 5IVvYBq+Ma/TrUdPh1awntdRjWa3uY2iljbkOjDBB9iCRX53/Gr4Zz/CP4k6lol2HMUEm+2kb/\r\n" +
            " ltC3KN9ccH3BFAHK0UUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFA\r\n" +
            " Htf7CHgY+J/jN/adwha30G2afd281/kQH3wXYf7lfatePfsS/DU+BPg5DfXybL3xC/218jlYsY\r\n" +
            " iH025f/tpXsNABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAV4h+298F/8AhPvAI17RIt+q\r\n" +
            " +H0Z3Cj5prbq6++3749t3rXt9I6CRCsgDKwwQRkEUAfmBRXqX7V3wOb4N/EN30mIjQ9WLT2RA+\r\n" +
            " WI5+eH/gJIx/ske9eW0AFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFdn8Av\r\n" +
            " hZL8YPihp2kBW+ybvPvXH/LOBSC3PYnhR7sK4yvuH9jr4In4V/DwX+uRbNa11VnmDDDW8XWOL2\r\n" +
            " ODuPucH7tAHrlvbx2lukVqixxRKERFGAoAwAB2FPoooAKKKKACiiigAooooAKKKKACiiigAooo\r\n" +
            " oAKKKKACiiigDlfjL8KrH4yeArvRdZwjyDzLafGTbTAHa4/MgjuCRX5+eMfCN/4D8TXmkeJ4Gt\r\n" +
            " 76xkMciHofQg91IwQe4Ir9La8c/a0/ZwX4w+HxqnheNV8R6bGfLHA+2xjnyif7w5Kn1JB65AB8\r\n" +
            " RUU+4t5LS4eK7R4pYmKOjqVZGBwQQehB7UygAooooAKKKKACiiigAooooAKKKKACiiigAooooA\r\n" +
            " KKK674LfCDUfjT43g0nRAY4R+8u7krlbWLPLH1PYDufbJAB3v7GnwCPxN8YDXPEkJOhaLIGww+\r\n" +
            " W7nGCqe6rwzfgO9fatZXgnwZp/w+8LWej+F4BBZWMexF7t3LMe7EkknuTWrQAUUUUAFFFFABRR\r\n" +
            " RQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAeA/tYfsmr8QEm8R/DeBE1xRuurVcKt+B/E\r\n" +
            " vYSf+hfXr8gXNtJZ3EkN5G8UsTFHR1KsjA4IIPIIPav09rzH45fsreHvjTvu5AdK1vbgX0CA+Z\r\n" +
            " 6CVOA/1yG6c44oA+DaK9U8f/sceOfA87m10w61aKTtm08+aWHvH98H8CPc157feDdY0yQpqWla\r\n" +
            " lbuDgrLauhB+hFAGbRXRaD8IvFPii4WPQfD2s3LN3W0cKPcsRgD6mvW/h1+wD4k18pN8QLy10K\r\n" +
            " 3PJiTFxcH2wp2L9dx+lAHgVa/hvwBrvjFgPCmjapqQJxm2tXlUfUqMCvt74e/sk+B/h6qPDpKa\r\n" +
            " rdr1uNRxcNn1CEbF+oXPvXpMUSQRKkCqiIMKqjAA9AKAPgmx/ZG+IuoKDB4ZuFyM/vbiCL/0Nx\r\n" +
            " TtS/ZB+IumZM3huaRfWG5hlz+CuT+lfe1FAH5q+JvAet+DJdni3SNS005wPtNu8Yb6EjB/Csmv\r\n" +
            " 09ngS6haO5RJI3G1lYZVh6EHrXmPxB/Y88D+PvMkXTTo92/Pn6cRCM+8eCh/75B96APhCivd/i\r\n" +
            " L+wP4o8Nu83gS4tdftRyEyLe4A91Y7T+DZPpXlurfBvxboU7R6r4a12Fk6k2MhU/RguD+BoA5q\r\n" +
            " iup0P4IeMfEkypo/hnXJd3Rms3RB9XYBR+Jr2H4Wf8E/9W1W4jufixeR6XaggtaWriW4f2LjKJ\r\n" +
            " 9Ru/CgDxv4V/CXWvjD4lTTfB9sZDkGedgRDap/edu3fA6nsDX3b8Gfg7pfwU8Hx6X4dXzJWw91\r\n" +
            " dMuJLqTH3j6AdAvYe+SdXwP4C0j4caBHpnguxhsbSPnagyzt3Z2PLN7kk1sUAFFFFABRRRQAUU\r\n" +
            " UUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABR\r\n" +
            " RRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAF\r\n" +
            " FFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQA\r\n" +
            " UUUUAf/Z\r\n" +
            "X-ABShowAs:COMPANY\r\n" +
            "REV:2012-01-04T08:43:10Z\r\n" +
            "UID:521d99c2-b56b-4781-bb19-994f08c89836\r\n" +
            "END:VCARD\r\n"
        ;
        byte[] vCardBytes = vCardString.getBytes(Charsets.UTF_8);
        VCardParametersImpl parameters = new VCardParametersImpl();
        parameters.setMaxVCardSize(vCardBytes.length - 1);
        OXException expectedException = null;
        try {
            getService().importVCard(Streams.newByteArrayInputStream(vCardBytes), null, parameters);
        } catch (OXException e) {
            expectedException = e;
        }
        assertNotNull("no exception thrown", expectedException);
        assertTrue("wrong exception thrown", VCardExceptionCodes.MAXIMUM_SIZE_EXCEEDED.equals(expectedException));
    }

    public void testImportVCardWithTooLargePhoto() throws Exception {
        String vCardString =
            "BEGIN:VCARD\r\n" +
            "VERSION:3.0\r\n" +
            "PRODID:-//Apple Inc.//Address Book 6.1//EN\r\n" +
            "N:;;;;\r\n" +
            "FN:Apple GmbH\r\n" +
            "ORG:Apple GmbH;\r\n" +
            "item1.ADR;type=WORK;type=pref:;;Arnulfstra\u00dfe 19;M\u00fcnchen;;80335;Germany\r\n" +
            "item1.X-ABADR:de\r\n" +
            "item2.URL;type=pref:http://www.apple.de\r\n" +
            "item2.X-ABLabel:_$!<HomePage>!$_\r\n" +
            "PHOTO;ENCODING=b;TYPE=JPEG:/9j/4AAQSkZJRgABAQAAAQABAAD/4gLQSUNDX1BST0ZJTEUA\r\n" +
            " AQEAAALAYXBwbAIAAABtbnRyUkdCIFhZWiAH1wAJAAQAAAAAAABhY3NwQVBQTAAAAAAAAAAAAA\r\n" +
            " AAAAAAAAAAAAAAAAAAAAAA9tYAAQAAAADTLWFwcGwrE5C12jl7mNFiWx8hc7nyAAAAAAAAAAAA\r\n" +
            " AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAxyWFlaAAABFAAAABRnWFlaAAABKAAAABRiWFlaAAABPA\r\n" +
            " AAABR3dHB0AAABUAAAABRjaGFkAAABZAAAACxyVFJDAAABkAAAAA5nVFJDAAABoAAAAA5iVFJD\r\n" +
            " AAABsAAAAA52Y2d0AAABwAAAADBuZGluAAAB8AAAADhkZXNjAAACKAAAAHRjcHJ0AAACnAAAAC\r\n" +
            " RYWVogAAAAAAAAeb4AAEGFAAAEtFhZWiAAAAAAAABW9wAArFsAABz2WFlaIAAAAAAAACYhAAAS\r\n" +
            " OgAAsXtYWVogAAAAAAAA81EAAQAAAAEW03NmMzIAAAAAAAEMQwAABd7///MlAAAHkwAA/ZD///\r\n" +
            " uh///9ogAAA9wAAMBqY3VydgAAAAAAAAABAc0AAGN1cnYAAAAAAAAAAQHNAABjdXJ2AAAAAAAA\r\n" +
            " AAEBzQAAdmNndAAAAAAAAAABAAEAAAAAAAAAAQAAAAEAAAAAAAAAAQAAAAEAAAAAAAAAAQAAbm\r\n" +
            " RpbgAAAAAAAAAwAACgAQAAVwsAAEetAACYUgAAJ60AABHsAABQDAAAVDkAAczNAAHMzQABzM1k\r\n" +
            " ZXNjAAAAAAAAABpDYWxpYnJhdGVkIFJHQiBDb2xvcnNwYWNlAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" +
            " AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\r\n" +
            " AAAAAHRleHQAAAAAQ29weXJpZ2h0IEFwcGxlLCBJbmMuLCAyMDA3AP/hAEBFeGlmAABNTQAqAA\r\n" +
            " AACAABh2kABAAAAAEAAAAaAAAAAAACoAIABAAAAAEAAAFAoAMABAAAAAEAAAFAAAAAAP/bAEMA\r\n" +
            " AgEBAgEBAgIBAgICAgIDBQMDAwMDBgQEAwUHBgcHBwYGBgcICwkHCAoIBgYJDQkKCwsMDAwHCQ\r\n" +
            " 0ODQwOCwwMC//bAEMBAgICAwIDBQMDBQsIBggLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsLCwsL\r\n" +
            " CwsLCwsLCwsLCwsLCwsLCwsLCwsLC//AABEIAUABQAMBIgACEQEDEQH/xAAfAAABBQEBAQEBAQ\r\n" +
            " AAAAAAAAAAAQIDBAUGBwgJCgv/xAC1EAACAQMDAgQDBQUEBAAAAX0BAgMABBEFEiExQQYTUWEH\r\n" +
            " InEUMoGRoQgjQrHBFVLR8CQzYnKCCQoWFxgZGiUmJygpKjQ1Njc4OTpDREVGR0hJSlNUVVZXWF\r\n" +
            " laY2RlZmdoaWpzdHV2d3h5eoOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPE\r\n" +
            " xcbHyMnK0tPU1dbX2Nna4eLj5OXm5+jp6vHy8/T19vf4+fr/xAAfAQADAQEBAQEBAQEBAAAAAA\r\n" +
            " AAAQIDBAUGBwgJCgv/xAC1EQACAQIEBAMEBwUEBAABAncAAQIDEQQFITEGEkFRB2FxEyIygQgU\r\n" +
            " QpGhscEJIzNS8BVictEKFiQ04SXxFxgZGiYnKCkqNTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZW\r\n" +
            " ZnaGlqc3R1dnd4eXqCg4SFhoeIiYqSk5SVlpeYmZqio6Slpqeoqaqys7S1tre4ubrCw8TFxsfI\r\n" +
            " ycrS09TV1tfY2dri4+Tl5ufo6ery8/T19vf4+fr/2gAMAwEAAhEDEQA/AP38ooooAKKKKACiii\r\n" +
            " gAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKK\r\n" +
            " KACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAoo\r\n" +
            " ooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACi\r\n" +
            " iigAooooAKKKKACiiigAooooAKKKKACiiigAqj4k8T6d4P0iW/8AFF7b2FnCMvNO4RR7c9SewH\r\n" +
            " JriPj/APtHaR8C9IC3WL7WrlC1tYo2CR03yH+FM/icYHcj4p+Jvxb174u64b7xtfPcEE+VAvyw\r\n" +
            " W4PaNOg+vU45JoA+ifih/wAFBrLT5JLb4T6ab9xkC8vQY4fqsQwzD6lfpXh3jL9pvxz44kb+1f\r\n" +
            " EV9BE3/LGzb7NGB6YjwWH+8TXBUUAfU3/BPPX9U1m58VJq17eXdtCtsyrNK0gR2MuSMngkLz64\r\n" +
            " FfTVeD/8E/fCjaR8JL7U51w2sXzFD/ejjAQf+P8AmV7xQAUUUUAFFFFABRRRQAUUUUAFFFFABR\r\n" +
            " RRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFcJ+0H8cLP4G+CHvr\r\n" +
            " gJPqV1mKwtif9dJjlm77FyCT9B1IrtdS1GDSNPnu9TlSC2tY2llkY4WNFGSx9gATX59/H74w3P\r\n" +
            " xq+Il1qk++Oxi/cWMBP+phB4yP7zfePucdAKAOZ8U+KL/xp4gutU8TXMl3fXjmSWVzyT6D0AGA\r\n" +
            " AOAAAKz6KKACpLO0l1C7igskaWadxHGijJdicAD3JNR17j+w78HW8b/EP/hINWiJ0zw8wdCR8s\r\n" +
            " 1z1QD/AHfvn0O31oA+sPhb4KT4dfDvRtEh2k6dapFIy9Hkxl2/Fix/Gt+iigAooooAKKKKACii\r\n" +
            " igAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooA8I\r\n" +
            " /b1+JzeFfhvbaDpshS68QSES4PK28eCw9tzFB7jcK+Nq9a/bX8YN4p+PeoQq+6DR4o7GPnjgb3\r\n" +
            " /wDH3YfhXktABRRXUfCn4Q638Y/Eaad4Pti4BBnuHBENqv8Aedu3fA6nsKAIfhb8MdT+LnjG20\r\n" +
            " bwvGWlmO6WUg7LaMfekc9gP1OAOTX6AfDX4eaf8LPBdlonhtCLezTBdh88znlnb3J59ug4ArL+\r\n" +
            " CfwS0n4IeFRYeH1865mw93eOoEl0/v6KOcL29yST2VABRRRQAUUUUAFFFFABRRRQAUUUUAFFFF\r\n" +
            " ABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUyedLaFpLl0jjQZZmOAo9ST0rz/wAW/tWe\r\n" +
            " AvBzvHf+IbW5mQ48uzVrk59NyAqPxIoA9DorwfUP+ChPg62m22Om+ILlR1cQxID9MyZ/QVBaf8\r\n" +
            " FEPCj/APH9o3iGPj/lmkL/AM5BQB7/AEV5P4X/AG1fh/4llWObU7jS5H4C3tuyD8XXco/EivTt\r\n" +
            " H1yy8RWKXWgXlrfW0n3ZbeVZUb6MpIoAtUUUUAfnD8Xr2TUfiv4mnuc75dVumIPb963H4VR8K+\r\n" +
            " CNY8c332bwfpl7qU3dbeFn2+7EcKPc4r79vvgH4L1PxBPqmp+GtJub66kMssksAcSOeSxU/KST\r\n" +
            " yTjmuo07TbbSLRLfSbeC1gj4WOGMIi/QDgUAfKvwl/YB1DUpYrv4vXa6fb8MbG1cPO/s8nKp/w\r\n" +
            " AB3fUV9O+DvBOlfD/QotM8HWMFhZQ9I4x9492YnlmPckkmtSgnHWgAorkPFvx88G+Biy+JvEem\r\n" +
            " QyJ96KOXzpV+sce5v0rgNZ/b78Daa7LYR65qGOjQ2qqp/wC/jqf0oA9uor55l/4KKeHRIfJ0HW\r\n" +
            " mXsWaIE/huNaWif8FAfBeouq6taa5p7HqzwJIg/FHLf+O0Ae6UVyXgn48eD/iI6J4S8QafcTyf\r\n" +
            " dgd/Jmb6RyAMfwFdbQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUVV1vW\r\n" +
            " 7Tw5pNxf69cRWlnaIZJppGwsajuTQBZdxGhaQhVUZJJwAK8G+Nv7c2keCpJdP+GUcWuakhKvcM\r\n" +
            " x+xwn2I5lP8AukD/AGu1eSftLftb3vxXmm0jwQ89h4cUlXP3Zb/3f0T0Tv1PoPFKAOn+Ifxk8S\r\n" +
            " /FO7aTxvq11dx5ytuG2QR/7sa4UfXGfU1zFFFABRRRQAVqeFPG2r+BdSF34P1K8024HV4JSm4e\r\n" +
            " jAcMPY5FZdFAH0T8Nf8AgoJrGkeXb/E/TotWhHBurXENwPcp9xj7DZXvPgf9p7wP49hQ6Xr1na\r\n" +
            " zv/wAu96wtpQfTD4DH/dJr8/aKAP0y/wCEm037OZv7QsfKXq/nrtH45xXG+Mv2ovAvgdWGp+IL\r\n" +
            " O5mX/ljZE3Lk+n7vIU/7xFfn/RQB9M/EH/gofcT+ZD8MNFSBTwtzqDb3+oiQ4B+rH6V4j45+OH\r\n" +
            " iz4jlx4w12/uYZOtur+VB/37TC/pXKUUAFFFFABRRRQAV6Z8Kf2sPF/wALJI4or5tW01MA2d8x\r\n" +
            " kVR6I/3k9sHHsa8zooA/QD4I/tH+H/jfYhdHk+xatGu6bT52Hmr6sh6SL7jkdwM16BX5kaRrF1\r\n" +
            " 4f1SC90S4mtLu2cSRTRMVeNh3BFfaf7LH7UEXxl0/+yvFZjt/ElpHubA2pfIOsiDsw/iX8RxkK\r\n" +
            " AeyUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFADZZVgiZ52VEQFmZjgKB1JNfE37WP7\r\n" +
            " Ssvxc199I8KzMnhuwkwu04+3yD/lo3+yP4R+J5OB6r+3Z8cT4Z8Px+EfDkxW+1VPMvmQ8xW/IC\r\n" +
            " Z7FyDn/ZB/vV8i0AFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABV/wv\r\n" +
            " rt94Z8RWWoeGZZIdQtJlkgaPlt4PAx3z0x3ziotE0S78Savb2Gg28t3eXbiOGGMZaRj0Ar7S/Z\r\n" +
            " s/ZO0/4RWsWqeLVg1DxI43b8borH/Ziz1b1fr2GBnIB6f4G1u78SeDtMv9esZdNvbu2SSe1kGG\r\n" +
            " gcj5lIPI59ecdea1aKKACiiigAooooAKKKKACiiigAooooAKKKKACquuazb+HdFu9Q1eQRWtjC\r\n" +
            " 9xM5/hRVLE/kDVqvF/26vHZ8K/BdtOtX23Gv3C23BwREvzufp8qqf9+gD5D+JHjm6+JXjrU9c1\r\n" +
            " cnztRnMgXOfLToiD2VQq/hWHRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRR\r\n" +
            " QAUdTxRXtf7E/wAFl+I3xAbWdeiEmk+Hysm1h8s9weUX3C4LH6KDwaAPZ/2Pf2cU+GXh5Nf8XW\r\n" +
            " 4/4SDUo8ojrzYRHog9HYct3H3fXPt1FFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAV8e\r\n" +
            " /wDBQnxWdT+J2l6TG2YtKsfMI9JJWJP/AI6kdfYVfn9+1Lrp8QftAeKJmbcIrv7MPbylWPH/AI\r\n" +
            " 4aAOAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAK+//ANl7wAvw6+CW\r\n" +
            " i2rJsuryIX11kYJklAbB91Xav/Aa+EfB+jf8JH4t0vTjn/T7yK24/wBtwv8AWv0uRBGgWMBVUY\r\n" +
            " AHQCgBaKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAr82viZeHUPiP4guJDlp9SuZCf\r\n" +
            " UmVj/Wv0lr80/GyGPxnq6t1W9mB/7+NQBl0UUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFF\r\n" +
            " FABRRRQAUUUUAdb8BYhN8bfCSv0/te1P5Sqf6V+idfm/8ACfVV0P4peG72Y4S11S2lY/7KyqT+\r\n" +
            " gNfpBQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABX5xfGGxOmfFrxRbkY8nVrpR9\r\n" +
            " BM2P0r9Ha+Bf2tNGOh/tC+JY8YWadLhT6+ZGjn9WP5UAec0UUUAFFFFABRRRQAUUUUAFFFFABR\r\n" +
            " RRQAUUUUAFFFFABRRRQAUUUUAKrFGBQkEHII6iv0g+FvjFPiB8OtF1mFgx1C0jlkx/DJjDr+DB\r\n" +
            " h+Ffm9X1l/wT6+Jy6j4b1HwpqEg8/TnN5aKTy0Tn5wP91yD/20oA+jqKKKACiiigAooooAKKKK\r\n" +
            " ACiiigAooooAKKKKACiiigAr48/4KEeGjp3xU0zU0XEep6eEJx1kjcg/+OvHX2HXgn/BQTwgdY\r\n" +
            " +Fmn6tAu59GvQHOPuxyjaT/wB9rEPxoA+OqKKKACiiigAooooAKKKKACiiigAooooAKKKKACii\r\n" +
            " igAooooAKKKKACug+FvxDvPhX4807XdF+aSyky8ecCaM8Oh+qkj2OD2rn6KAP0w8KeJ7Pxp4bs\r\n" +
            " tW8Pyiaz1CFZom74I6EdiOhHYgitCvjv8AYx/aPj+H+pf8Ix42uBHo1/JutZ5G+WymPUEnojHv\r\n" +
            " 0Dc9ya+xAcjI5zQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFc78W/BQ+Ivw01vRWAL39\r\n" +
            " o6RZ6CUDdGfwcKfwroqKAPzBkjaGRklUq6khgRgg+hptem/tc/Dv8A4V58b9UW3j2Weqn+0bfA\r\n" +
            " 4xISXA+kgcY9MV5lQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFfR\r\n" +
            " P7L/7Y/wDwh9tb+HfitJLLpqYjtL85Z7QdkkHVkHYjlenI+787UUAfp3Y30Op2cVxp00VxbzqH\r\n" +
            " jljYMkinkEEcEH1qWvhP9mL40eKvBvjrSdD8K3X2jT9VvY7d7K4BeFd7gM6jOUIBJypGccg192\r\n" +
            " UAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFAHhv7dvwvPjD4Yxa5pse688OuZHwOWt3wH/7\r\n" +
            " 5IVvYBq+Ma/TrUdPh1awntdRjWa3uY2iljbkOjDBB9iCRX53/Gr4Zz/CP4k6lol2HMUEm+2kb/\r\n" +
            " ltC3KN9ccH3BFAHK0UUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFA\r\n" +
            " Htf7CHgY+J/jN/adwha30G2afd281/kQH3wXYf7lfatePfsS/DU+BPg5DfXybL3xC/218jlYsY\r\n" +
            " iH025f/tpXsNABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAV4h+298F/8AhPvAI17RIt+q\r\n" +
            " +H0Z3Cj5prbq6++3749t3rXt9I6CRCsgDKwwQRkEUAfmBRXqX7V3wOb4N/EN30mIjQ9WLT2RA+\r\n" +
            " WI5+eH/gJIx/ske9eW0AFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFdn8Av\r\n" +
            " hZL8YPihp2kBW+ybvPvXH/LOBSC3PYnhR7sK4yvuH9jr4In4V/DwX+uRbNa11VnmDDDW8XWOL2\r\n" +
            " ODuPucH7tAHrlvbx2lukVqixxRKERFGAoAwAB2FPoooAKKKKACiiigAooooAKKKKACiiigAooo\r\n" +
            " oAKKKKACiiigDlfjL8KrH4yeArvRdZwjyDzLafGTbTAHa4/MgjuCRX5+eMfCN/4D8TXmkeJ4Gt\r\n" +
            " 76xkMciHofQg91IwQe4Ir9La8c/a0/ZwX4w+HxqnheNV8R6bGfLHA+2xjnyif7w5Kn1JB65AB8\r\n" +
            " RUU+4t5LS4eK7R4pYmKOjqVZGBwQQehB7UygAooooAKKKKACiiigAooooAKKKKACiiigAooooA\r\n" +
            " KKK674LfCDUfjT43g0nRAY4R+8u7krlbWLPLH1PYDufbJAB3v7GnwCPxN8YDXPEkJOhaLIGww+\r\n" +
            " W7nGCqe6rwzfgO9fatZXgnwZp/w+8LWej+F4BBZWMexF7t3LMe7EkknuTWrQAUUUUAFFFFABRR\r\n" +
            " RQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAeA/tYfsmr8QEm8R/DeBE1xRuurVcKt+B/E\r\n" +
            " vYSf+hfXr8gXNtJZ3EkN5G8UsTFHR1KsjA4IIPIIPav09rzH45fsreHvjTvu5AdK1vbgX0CA+Z\r\n" +
            " 6CVOA/1yG6c44oA+DaK9U8f/sceOfA87m10w61aKTtm08+aWHvH98H8CPc157feDdY0yQpqWla\r\n" +
            " lbuDgrLauhB+hFAGbRXRaD8IvFPii4WPQfD2s3LN3W0cKPcsRgD6mvW/h1+wD4k18pN8QLy10K\r\n" +
            " 3PJiTFxcH2wp2L9dx+lAHgVa/hvwBrvjFgPCmjapqQJxm2tXlUfUqMCvt74e/sk+B/h6qPDpKa\r\n" +
            " rdr1uNRxcNn1CEbF+oXPvXpMUSQRKkCqiIMKqjAA9AKAPgmx/ZG+IuoKDB4ZuFyM/vbiCL/0Nx\r\n" +
            " TtS/ZB+IumZM3huaRfWG5hlz+CuT+lfe1FAH5q+JvAet+DJdni3SNS005wPtNu8Yb6EjB/Csmv\r\n" +
            " 09ngS6haO5RJI3G1lYZVh6EHrXmPxB/Y88D+PvMkXTTo92/Pn6cRCM+8eCh/75B96APhCivd/i\r\n" +
            " L+wP4o8Nu83gS4tdftRyEyLe4A91Y7T+DZPpXlurfBvxboU7R6r4a12Fk6k2MhU/RguD+BoA5q\r\n" +
            " iup0P4IeMfEkypo/hnXJd3Rms3RB9XYBR+Jr2H4Wf8E/9W1W4jufixeR6XaggtaWriW4f2LjKJ\r\n" +
            " 9Ru/CgDxv4V/CXWvjD4lTTfB9sZDkGedgRDap/edu3fA6nsDX3b8Gfg7pfwU8Hx6X4dXzJWw91\r\n" +
            " dMuJLqTH3j6AdAvYe+SdXwP4C0j4caBHpnguxhsbSPnagyzt3Z2PLN7kk1sUAFFFFABRRRQAUU\r\n" +
            " UUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABR\r\n" +
            " RRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAF\r\n" +
            " FFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQA\r\n" +
            " UUUUAf/Z\r\n" +
            "X-ABShowAs:COMPANY\r\n" +
            "REV:2012-01-04T08:43:10Z\r\n" +
            "UID:521d99c2-b56b-4781-bb19-994f08c89836\r\n" +
            "END:VCARD\r\n"
        ;
        byte[] vCardBytes = vCardString.getBytes(Charsets.UTF_8);
        VCardParametersImpl parameters = new VCardParametersImpl();
        parameters.setMaxContactImageSize(500);
        VCardImport vCardImport = importVCard(vCardBytes, parameters);
        assertNotNull("no contact imported", vCardImport.getContact());
        assertTrue("no warnings", 0 < vCardImport.getWarnings().size());
        OXException warning = vCardImport.getWarnings().get(0);
        assertNotNull("no warning", warning);
        assertTrue("unexpected warning", VCardExceptionCodes.CONVERSION_FAILED.equals(warning));
        assertNull("image data imported", vCardImport.getContact().getImage1());
    }

}

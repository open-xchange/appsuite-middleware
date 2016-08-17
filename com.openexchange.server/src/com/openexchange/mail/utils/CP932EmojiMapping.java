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

package com.openexchange.mail.utils;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

/**
 * {@link CP932EmojiMapping} - Emoji mapping for CP932/Shift-JIS charset.
 *
 * @author <a href='mailto:thorben.betten@open-xchange.com'>Thorben Betten</a>
 */
public class CP932EmojiMapping {

    private static final CP932EmojiMapping INSTANCE = new CP932EmojiMapping();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static CP932EmojiMapping getInstance() {
        return INSTANCE;
    }

    /**
     * The unknown character: <code>'&#65533;'</code>
     */
    private static final char UNKNOWN = '\ufffd';

    // ------------------------------------------------------------------ //

    private final TIntIntMap map;

    /**
     * Initializes a new {@link CP932EmojiMapping}.
     */
    private CP932EmojiMapping() {
        super();
        map = initMapping();
    }

    /**
     * Gets the character representing an unknown character.
     *
     * @return The unknown character
     */
    public char getUnknown() {
        return UNKNOWN;
    }

    /**
     * Replaces all occurrences in specified content.
     *
     * @param content The content
     * @return The replaced content
     */
    public String replaceIn(final String content) {
        if (null == content) {
            return content;
        }
        final int length = content.length();
        final StringBuilder sb = new StringBuilder(length);
        final char unknown = UNKNOWN;
        for (int i = 0; i < length; i++) {
            final char c = content.charAt(i);
            final int character = map.get(c);
            sb.append(unknown == character ? c : (char) character);
        }
        return sb.toString();
    }

    /**
     * Gets the character mapping.
     *
     * @param c The character to map
     * @return The mapped character or '\ufffd'
     */
    public char mappingFor(final char c) {
        return (char) map.get(c);
    }

    private TIntIntMap initMapping() {
        final TIntIntMap map = new TIntIntHashMap(256, Constants.DEFAULT_LOAD_FACTOR, 0, UNKNOWN);
        map.put('\uE524', '\uF740');
        map.put('\uE525', '\uF741');
        map.put('\uE526', '\uF742');
        map.put('\uE527', '\uF743');
        map.put('\uE528', '\uF744');
        map.put('\uE529', '\uF745');
        map.put('\uE52A', '\uF746');
        map.put('\uE52B', '\uF747');
        map.put('\uE52C', '\uF748');
        map.put('\uE52D', '\uF749');
        map.put('\uE52E', '\uF74A');
        map.put('\uE52F', '\uF74B');
        map.put('\uE530', '\uF74C');
        map.put('\uE531', '\uF74D');
        map.put('\uE532', '\uF74E');
        map.put('\uE533', '\uF74F');
        map.put('\uE534', '\uF750');
        map.put('\uE535', '\uF751');
        map.put('\uE536', '\uF752');
        map.put('\uE537', '\uF753');
        map.put('\uE538', '\uF754');
        map.put('\uE539', '\uF755');
        map.put('\uE53A', '\uF756');
        map.put('\uE53B', '\uF757');
        map.put('\uE53C', '\uF758');
        map.put('\uE53D', '\uF759');
        map.put('\uE53E', '\uF75A');
        map.put('\uE53F', '\uF75B');
        map.put('\uE540', '\uF75C');
        map.put('\uE541', '\uF75D');
        map.put('\uE542', '\uF75E');
        map.put('\uE543', '\uF75F');
        map.put('\uE544', '\uF760');
        map.put('\uE545', '\uF761');
        map.put('\uE546', '\uF762');
        map.put('\uE547', '\uF763');
        map.put('\uE548', '\uF764');
        map.put('\uE549', '\uF765');
        map.put('\uE54A', '\uF766');
        map.put('\uE54B', '\uF767');
        map.put('\uE54C', '\uF768');
        map.put('\uE54D', '\uF769');
        map.put('\uE54E', '\uF76A');
        map.put('\uE54F', '\uF76B');
        map.put('\uE550', '\uF76C');
        map.put('\uE551', '\uF76D');
        map.put('\uE552', '\uF76E');
        map.put('\uE553', '\uF76F');
        map.put('\uE554', '\uF770');
        map.put('\uE555', '\uF771');
        map.put('\uE556', '\uF772');
        map.put('\uE557', '\uF773');
        map.put('\uE558', '\uF774');
        map.put('\uE559', '\uF775');
        map.put('\uE55A', '\uF776');
        map.put('\uE55B', '\uF777');
        map.put('\uE55C', '\uF778');
        map.put('\uE55D', '\uF779');
        map.put('\uE55E', '\uF77A');
        map.put('\uE55F', '\uF77B');
        map.put('\uE560', '\uF77C');
        map.put('\uE561', '\uF77D');
        map.put('\uE562', '\uF77E');
        map.put('\uE563', '\uF780');
        map.put('\uE564', '\uF781');
        map.put('\uE565', '\uF782');
        map.put('\uE566', '\uF783');
        map.put('\uE567', '\uF784');
        map.put('\uE568', '\uF785');
        map.put('\uE569', '\uF786');
        map.put('\uE56A', '\uF787');
        map.put('\uE56B', '\uF788');
        map.put('\uE56C', '\uF789');
        map.put('\uE56D', '\uF78A');
        map.put('\uE56E', '\uF78B');
        map.put('\uE56F', '\uF78C');
        map.put('\uE570', '\uF78D');
        map.put('\uE571', '\uF78E');
        map.put('\uE572', '\uF78F');
        map.put('\uE573', '\uF790');
        map.put('\uE574', '\uF791');
        map.put('\uE575', '\uF792');
        map.put('\uE576', '\uF793');
        map.put('\uE577', '\uF794');
        map.put('\uE578', '\uF795');
        map.put('\uE579', '\uF796');
        map.put('\uE57A', '\uF797');
        map.put('\uE57B', '\uF798');
        map.put('\uE57C', '\uF799');
        map.put('\uE57D', '\uF79A');
        map.put('\uE57E', '\uF79B');
        map.put('\uE57F', '\uF79C');
        map.put('\uE580', '\uF79D');
        map.put('\uE581', '\uF79E');
        map.put('\uE582', '\uF79F');
        map.put('\uE583', '\uF7A0');
        map.put('\uE584', '\uF7A1');
        map.put('\uE585', '\uF7A2');
        map.put('\uE586', '\uF7A3');
        map.put('\uE587', '\uF7A4');
        map.put('\uE588', '\uF7A5');
        map.put('\uE589', '\uF7A6');
        map.put('\uE58A', '\uF7A7');
        map.put('\uE58B', '\uF7A8');
        map.put('\uE58C', '\uF7A9');
        map.put('\uE58D', '\uF7AA');
        map.put('\uE58E', '\uF7AB');
        map.put('\uE58F', '\uF7AC');
        map.put('\uE590', '\uF7AD');
        map.put('\uE591', '\uF7AE');
        map.put('\uE592', '\uF7AF');
        map.put('\uE593', '\uF7B0');
        map.put('\uE594', '\uF7B1');
        map.put('\uE595', '\uF7B2');
        map.put('\uE596', '\uF7B3');
        map.put('\uE597', '\uF7B4');
        map.put('\uE598', '\uF7B5');
        map.put('\uE599', '\uF7B6');
        map.put('\uE59A', '\uF7B7');
        map.put('\uE59B', '\uF7B8');
        map.put('\uE59C', '\uF7B9');
        map.put('\uE59D', '\uF7BA');
        map.put('\uE59E', '\uF7BB');
        map.put('\uE59F', '\uF7BC');
        map.put('\uE5A0', '\uF7BD');
        map.put('\uE5A1', '\uF7BE');
        map.put('\uE5A2', '\uF7BF');
        map.put('\uE5A3', '\uF7C0');
        map.put('\uE5A4', '\uF7C1');
        map.put('\uE5A5', '\uF7C2');
        map.put('\uE5A6', '\uF7C3');
        map.put('\uE5A7', '\uF7C4');
        map.put('\uE5A8', '\uF7C5');
        map.put('\uE5A9', '\uF7C6');
        map.put('\uE5AA', '\uF7C7');
        map.put('\uE5AB', '\uF7C8');
        map.put('\uE5AC', '\uF7C9');
        map.put('\uE5AD', '\uF7CA');
        map.put('\uE5AE', '\uF7CB');
        map.put('\uE5AF', '\uF7CC');
        map.put('\uE5B0', '\uF7CD');
        map.put('\uE5B1', '\uF7CE');
        map.put('\uE5B2', '\uF7CF');
        map.put('\uE5B3', '\uF7D0');
        map.put('\uE5B4', '\uF7D1');
        map.put('\uE5B5', '\uF7D2');
        map.put('\uE5B6', '\uF7D3');
        map.put('\uE5B7', '\uF7D4');
        map.put('\uE5B8', '\uF7D5');
        map.put('\uE5B9', '\uF7D6');
        map.put('\uE5BA', '\uF7D7');
        map.put('\uE5BB', '\uF7D8');
        map.put('\uE5BC', '\uF7D9');
        map.put('\uE5BD', '\uF7DA');
        map.put('\uE5BE', '\uF7DB');
        map.put('\uE5BF', '\uF7DC');
        map.put('\uE5C0', '\uF7DD');
        map.put('\uE5C1', '\uF7DE');
        map.put('\uE5C2', '\uF7DF');
        map.put('\uE5C3', '\uF7E0');
        map.put('\uE5C4', '\uF7E1');
        map.put('\uE5C5', '\uF7E2');
        map.put('\uE5C6', '\uF7E3');
        map.put('\uE5C7', '\uF7E4');
        map.put('\uE5C8', '\uF7E5');
        map.put('\uE5C9', '\uF7E6');
        map.put('\uE5CA', '\uF7E7');
        map.put('\uE5CB', '\uF7E8');
        map.put('\uE5CC', '\uF7E9');
        map.put('\uE5CD', '\uF7EA');
        map.put('\uE5CE', '\uF7EB');
        map.put('\uE5CF', '\uF7EC');
        map.put('\uE5D0', '\uF7ED');
        map.put('\uE5D1', '\uF7EE');
        map.put('\uE5D2', '\uF7EF');
        map.put('\uE5D3', '\uF7F0');
        map.put('\uE5D4', '\uF7F1');
        map.put('\uE5D5', '\uF7F2');
        map.put('\uE5D6', '\uF7F3');
        map.put('\uE5D7', '\uF7F4');
        map.put('\uE5D8', '\uF7F5');
        map.put('\uE5D9', '\uF7F6');
        map.put('\uE5DA', '\uF7F7');
        map.put('\uE5DB', '\uF7F8');
        map.put('\uE5DC', '\uF7F9');
        map.put('\uE5DD', '\uF7FA');
        map.put('\uE5DE', '\uF7FB');
        map.put('\uE5DF', '\uF7FC');
        map.put('\uE69C', '\uF940');
        map.put('\uE69D', '\uF941');
        map.put('\uE69E', '\uF942');
        map.put('\uE69F', '\uF943');
        map.put('\uE6A0', '\uF944');
        map.put('\uE6A1', '\uF945');
        map.put('\uE6A2', '\uF946');
        map.put('\uE6A3', '\uF947');
        map.put('\uE6A4', '\uF948');
        map.put('\uE6A5', '\uF949');
        map.put('\uE6A6', '\uF94A');
        map.put('\uE6A7', '\uF94B');
        map.put('\uE6A8', '\uF94C');
        map.put('\uE6A9', '\uF94D');
        map.put('\uE6AA', '\uF94E');
        map.put('\uE6AB', '\uF94F');
        map.put('\uE6AC', '\uF950');
        map.put('\uE6AD', '\uF951');
        map.put('\uE6AE', '\uF952');
        map.put('\uE6AF', '\uF953');
        map.put('\uE6B0', '\uF954');
        map.put('\uE6B1', '\uF955');
        map.put('\uE6B2', '\uF956');
        map.put('\uE6B3', '\uF957');
        map.put('\uE6B4', '\uF958');
        map.put('\uE6B5', '\uF959');
        map.put('\uE6B6', '\uF95A');
        map.put('\uE6B7', '\uF95B');
        map.put('\uE6B8', '\uF95C');
        map.put('\uE6B9', '\uF95D');
        map.put('\uE6BA', '\uF95E');
        map.put('\uE6BB', '\uF95F');
        map.put('\uE6BC', '\uF960');
        map.put('\uE6BD', '\uF961');
        map.put('\uE6BE', '\uF962');
        map.put('\uE6BF', '\uF963');
        map.put('\uE6C0', '\uF964');
        map.put('\uE6C1', '\uF965');
        map.put('\uE6C2', '\uF966');
        map.put('\uE6C3', '\uF967');
        map.put('\uE6C4', '\uF968');
        map.put('\uE6C5', '\uF969');
        map.put('\uE6C6', '\uF96A');
        map.put('\uE6C7', '\uF96B');
        map.put('\uE6C8', '\uF96C');
        map.put('\uE6C9', '\uF96D');
        map.put('\uE6CA', '\uF96E');
        map.put('\uE6CB', '\uF96F');
        map.put('\uE6CC', '\uF970');
        map.put('\uE6CD', '\uF971');
        map.put('\uE6CE', '\uF972');
        map.put('\uE6CF', '\uF973');
        map.put('\uE6D0', '\uF974');
        map.put('\uE6D1', '\uF975');
        map.put('\uE6D2', '\uF976');
        map.put('\uE6D3', '\uF977');
        map.put('\uE6D4', '\uF978');
        map.put('\uE6D5', '\uF979');
        map.put('\uE6D6', '\uF97A');
        map.put('\uE6D7', '\uF97B');
        map.put('\uE6D8', '\uF97C');
        map.put('\uE6D9', '\uF97D');
        map.put('\uE6DA', '\uF97E');
        map.put('\uE6DB', '\uF980');
        map.put('\uE6DC', '\uF981');
        map.put('\uE6DD', '\uF982');
        map.put('\uE6DE', '\uF983');
        map.put('\uE6DF', '\uF984');
        map.put('\uE6E0', '\uF985');
        map.put('\uE6E1', '\uF986');
        map.put('\uE6E2', '\uF987');
        map.put('\uE6E3', '\uF988');
        map.put('\uE6E4', '\uF989');
        map.put('\uE6E5', '\uF98A');
        map.put('\uE6E6', '\uF98B');
        map.put('\uE6E7', '\uF98C');
        map.put('\uE6E8', '\uF98D');
        map.put('\uE6E9', '\uF98E');
        map.put('\uE6EA', '\uF98F');
        map.put('\uE6EB', '\uF990');
        map.put('\uE6EC', '\uF991');
        map.put('\uE6ED', '\uF992');
        map.put('\uE6EE', '\uF993');
        map.put('\uE6EF', '\uF994');
        map.put('\uE6F0', '\uF995');
        map.put('\uE6F1', '\uF996');
        map.put('\uE6F2', '\uF997');
        map.put('\uE6F3', '\uF998');
        map.put('\uE6F4', '\uF999');
        map.put('\uE6F5', '\uF99A');
        map.put('\uE6F6', '\uF99B');
        map.put('\uE6F7', '\uF99C');
        map.put('\uE6F8', '\uF99D');
        map.put('\uE6F9', '\uF99E');
        map.put('\uE6FA', '\uF99F');
        map.put('\uE6FB', '\uF9A0');
        map.put('\uE6FC', '\uF9A1');
        map.put('\uE6FD', '\uF9A2');
        map.put('\uE6FE', '\uF9A3');
        map.put('\uE6FF', '\uF9A4');
        map.put('\uE700', '\uF9A5');
        map.put('\uE701', '\uF9A6');
        map.put('\uE702', '\uF9A7');
        map.put('\uE703', '\uF9A8');
        map.put('\uE704', '\uF9A9');
        map.put('\uE705', '\uF9AA');
        map.put('\uE706', '\uF9AB');
        map.put('\uE707', '\uF9AC');
        map.put('\uE708', '\uF9AD');
        map.put('\uE709', '\uF9AE');
        map.put('\uE70A', '\uF9AF');
        map.put('\uE70B', '\uF9B0');
        map.put('\uE70C', '\uF9B1');
        map.put('\uE70D', '\uF9B2');
        map.put('\uE70E', '\uF9B3');
        map.put('\uE70F', '\uF9B4');
        map.put('\uE710', '\uF9B5');
        map.put('\uE711', '\uF9B6');
        map.put('\uE712', '\uF9B7');
        map.put('\uE713', '\uF9B8');
        map.put('\uE714', '\uF9B9');
        map.put('\uE715', '\uF9BA');
        map.put('\uE716', '\uF9BB');
        map.put('\uE717', '\uF9BC');
        map.put('\uE718', '\uF9BD');
        map.put('\uE719', '\uF9BE');
        map.put('\uE71A', '\uF9BF');
        map.put('\uE71B', '\uF9C0');
        map.put('\uE71C', '\uF9C1');
        map.put('\uE71D', '\uF9C2');
        map.put('\uE71E', '\uF9C3');
        map.put('\uE71F', '\uF9C4');
        map.put('\uE720', '\uF9C5');
        map.put('\uE721', '\uF9C6');
        map.put('\uE722', '\uF9C7');
        map.put('\uE723', '\uF9C8');
        map.put('\uE724', '\uF9C9');
        map.put('\uE725', '\uF9CA');
        map.put('\uE726', '\uF9CB');
        map.put('\uE727', '\uF9CC');
        map.put('\uE728', '\uF9CD');
        map.put('\uE729', '\uF9CE');
        map.put('\uE72A', '\uF9CF');
        map.put('\uE72B', '\uF9D0');
        map.put('\uE72C', '\uF9D1');
        map.put('\uE72D', '\uF9D2');
        map.put('\uE72E', '\uF9D3');
        map.put('\uE72F', '\uF9D4');
        map.put('\uE730', '\uF9D5');
        map.put('\uE731', '\uF9D6');
        map.put('\uE732', '\uF9D7');
        map.put('\uE733', '\uF9D8');
        map.put('\uE734', '\uF9D9');
        map.put('\uE735', '\uF9DA');
        map.put('\uE736', '\uF9DB');
        map.put('\uE737', '\uF9DC');
        map.put('\uE738', '\uF9DD');
        map.put('\uE739', '\uF9DE');
        map.put('\uE73A', '\uF9DF');
        map.put('\uE73B', '\uF9E0');
        map.put('\uE73C', '\uF9E1');
        map.put('\uE73D', '\uF9E2');
        map.put('\uE73E', '\uF9E3');
        map.put('\uE73F', '\uF9E4');
        map.put('\uE740', '\uF9E5');
        map.put('\uE741', '\uF9E6');
        map.put('\uE742', '\uF9E7');
        map.put('\uE743', '\uF9E8');
        map.put('\uE744', '\uF9E9');
        map.put('\uE745', '\uF9EA');
        map.put('\uE746', '\uF9EB');
        map.put('\uE747', '\uF9EC');
        map.put('\uE748', '\uF9ED');
        map.put('\uE749', '\uF9EE');
        map.put('\uE74A', '\uF9EF');
        map.put('\uE74B', '\uF9F0');
        map.put('\uE74C', '\uF9F1');
        map.put('\uE74D', '\uF9F2');
        map.put('\uE74E', '\uF9F3');
        map.put('\uE74F', '\uF9F4');
        map.put('\uE750', '\uF9F5');
        map.put('\uE751', '\uF9F6');
        map.put('\uE752', '\uF9F7');
        map.put('\uE753', '\uF9F8');
        map.put('\uE754', '\uF9F9');
        map.put('\uE755', '\uF9FA');
        map.put('\uE756', '\uF9FB');
        map.put('\uE757', '\uF9FC');
        map.put('\u6D96', '\uFB40');
        map.put('\u6DAC', '\uFB41');
        map.put('\u6DCF', '\uFB42');
        map.put('\u6DF8', '\uFB43');
        map.put('\u6DF2', '\uFB44');
        map.put('\u6DFC', '\uFB45');
        map.put('\u6E39', '\uFB46');
        map.put('\u6E5C', '\uFB47');
        map.put('\u6E27', '\uFB48');
        map.put('\u6E3C', '\uFB49');
        map.put('\u6EBF', '\uFB4A');
        map.put('\u6F88', '\uFB4B');
        map.put('\u6FB5', '\uFB4C');
        map.put('\u6FF5', '\uFB4D');
        map.put('\u7005', '\uFB4E');
        map.put('\u7007', '\uFB4F');
        map.put('\u7028', '\uFB50');
        map.put('\u7085', '\uFB51');
        map.put('\u70AB', '\uFB52');
        map.put('\u710F', '\uFB53');
        map.put('\u7104', '\uFB54');
        map.put('\u715C', '\uFB55');
        map.put('\u7146', '\uFB56');
        map.put('\u7147', '\uFB57');
        map.put('\uFA15', '\uFB58');
        map.put('\u71C1', '\uFB59');
        map.put('\u71FE', '\uFB5A');
        map.put('\u72B1', '\uFB5B');
        map.put('\u72BE', '\uFB5C');
        map.put('\u7324', '\uFB5D');
        map.put('\uFA16', '\uFB5E');
        map.put('\u7377', '\uFB5F');
        map.put('\u73BD', '\uFB60');
        map.put('\u73C9', '\uFB61');
        map.put('\u73D6', '\uFB62');
        map.put('\u73E3', '\uFB63');
        map.put('\u73D2', '\uFB64');
        map.put('\u7407', '\uFB65');
        map.put('\u73F5', '\uFB66');
        map.put('\u7426', '\uFB67');
        map.put('\u742A', '\uFB68');
        map.put('\u7429', '\uFB69');
        map.put('\u742E', '\uFB6A');
        map.put('\u7462', '\uFB6B');
        map.put('\u7489', '\uFB6C');
        map.put('\u749F', '\uFB6D');
        map.put('\u7501', '\uFB6E');
        map.put('\u756F', '\uFB6F');
        map.put('\u7682', '\uFB70');
        map.put('\u769C', '\uFB71');
        map.put('\u769E', '\uFB72');
        map.put('\u769B', '\uFB73');
        map.put('\u76A6', '\uFB74');
        map.put('\uFA17', '\uFB75');
        map.put('\u7746', '\uFB76');
        map.put('\u52AF', '\uFB77');
        map.put('\u7821', '\uFB78');
        map.put('\u784E', '\uFB79');
        map.put('\u7864', '\uFB7A');
        map.put('\u787A', '\uFB7B');
        map.put('\u7930', '\uFB7C');
        map.put('\uFA18', '\uFB7D');
        map.put('\uFA19', '\uFB7E');
        map.put('\uFA1A', '\uFB80');
        map.put('\u7994', '\uFB81');
        map.put('\uFA1B', '\uFB82');
        map.put('\u799B', '\uFB83');
        map.put('\u7AD1', '\uFB84');
        map.put('\u7AE7', '\uFB85');
        map.put('\uFA1C', '\uFB86');
        map.put('\u7AEB', '\uFB87');
        map.put('\u7B9E', '\uFB88');
        map.put('\uFA1D', '\uFB89');
        map.put('\u7D48', '\uFB8A');
        map.put('\u7D5C', '\uFB8B');
        map.put('\u7DB7', '\uFB8C');
        map.put('\u7DA0', '\uFB8D');
        map.put('\u7DD6', '\uFB8E');
        map.put('\u7E52', '\uFB8F');
        map.put('\u7F47', '\uFB90');
        map.put('\u7FA1', '\uFB91');
        map.put('\uFA1E', '\uFB92');
        map.put('\u8301', '\uFB93');
        map.put('\u8362', '\uFB94');
        map.put('\u837F', '\uFB95');
        map.put('\u83C7', '\uFB96');
        map.put('\u83F6', '\uFB97');
        map.put('\u8448', '\uFB98');
        map.put('\u84B4', '\uFB99');
        map.put('\u8553', '\uFB9A');
        map.put('\u8559', '\uFB9B');
        map.put('\u856B', '\uFB9C');
        map.put('\uFA1F', '\uFB9D');
        map.put('\u85B0', '\uFB9E');
        map.put('\uFA20', '\uFB9F');
        map.put('\uFA21', '\uFBA0');
        map.put('\u8807', '\uFBA1');
        map.put('\u88F5', '\uFBA2');
        map.put('\u8A12', '\uFBA3');
        map.put('\u8A37', '\uFBA4');
        map.put('\u8A79', '\uFBA5');
        map.put('\u8AA7', '\uFBA6');
        map.put('\u8ABE', '\uFBA7');
        map.put('\u8ADF', '\uFBA8');
        map.put('\uFA22', '\uFBA9');
        map.put('\u8AF6', '\uFBAA');
        map.put('\u8B53', '\uFBAB');
        map.put('\u8B7F', '\uFBAC');
        map.put('\u8CF0', '\uFBAD');
        map.put('\u8CF4', '\uFBAE');
        map.put('\u8D12', '\uFBAF');
        map.put('\u8D76', '\uFBB0');
        map.put('\uFA23', '\uFBB1');
        map.put('\u8ECF', '\uFBB2');
        map.put('\uFA24', '\uFBB3');
        map.put('\uFA25', '\uFBB4');
        map.put('\u9067', '\uFBB5');
        map.put('\u90DE', '\uFBB6');
        map.put('\uFA26', '\uFBB7');
        map.put('\u9115', '\uFBB8');
        map.put('\u9127', '\uFBB9');
        map.put('\u91DA', '\uFBBA');
        map.put('\u91D7', '\uFBBB');
        map.put('\u91DE', '\uFBBC');
        map.put('\u91ED', '\uFBBD');
        map.put('\u91EE', '\uFBBE');
        map.put('\u91E4', '\uFBBF');
        map.put('\u91E5', '\uFBC0');
        map.put('\u9206', '\uFBC1');
        map.put('\u9210', '\uFBC2');
        map.put('\u920A', '\uFBC3');
        map.put('\u923A', '\uFBC4');
        map.put('\u9240', '\uFBC5');
        map.put('\u923C', '\uFBC6');
        map.put('\u924E', '\uFBC7');
        map.put('\u9259', '\uFBC8');
        map.put('\u9251', '\uFBC9');
        map.put('\u9239', '\uFBCA');
        map.put('\u9267', '\uFBCB');
        map.put('\u92A7', '\uFBCC');
        map.put('\u9277', '\uFBCD');
        map.put('\u9278', '\uFBCE');
        map.put('\u92E7', '\uFBCF');
        map.put('\u92D7', '\uFBD0');
        map.put('\u92D9', '\uFBD1');
        map.put('\u92D0', '\uFBD2');
        map.put('\uFA27', '\uFBD3');
        map.put('\u92D5', '\uFBD4');
        map.put('\u92E0', '\uFBD5');
        map.put('\u92D3', '\uFBD6');
        map.put('\u9325', '\uFBD7');
        map.put('\u9321', '\uFBD8');
        map.put('\u92FB', '\uFBD9');
        map.put('\uFA28', '\uFBDA');
        map.put('\u931E', '\uFBDB');
        map.put('\u92FF', '\uFBDC');
        map.put('\u931D', '\uFBDD');
        map.put('\u9302', '\uFBDE');
        map.put('\u9370', '\uFBDF');
        map.put('\u9357', '\uFBE0');
        map.put('\u93A4', '\uFBE1');
        map.put('\u93C6', '\uFBE2');
        map.put('\u93DE', '\uFBE3');
        map.put('\u93F8', '\uFBE4');
        map.put('\u9431', '\uFBE5');
        map.put('\u9445', '\uFBE6');
        map.put('\u9448', '\uFBE7');
        map.put('\u9592', '\uFBE8');
        map.put('\uF9DC', '\uFBE9');
        map.put('\uFA29', '\uFBEA');
        map.put('\u969D', '\uFBEB');
        map.put('\u96AF', '\uFBEC');
        map.put('\u9733', '\uFBED');
        map.put('\u973B', '\uFBEE');
        map.put('\u9743', '\uFBEF');
        map.put('\u974D', '\uFBF0');
        map.put('\u974F', '\uFBF1');
        map.put('\u9751', '\uFBF2');
        map.put('\u9755', '\uFBF3');
        map.put('\u9857', '\uFBF4');
        map.put('\u9865', '\uFBF5');
        map.put('\uFA2A', '\uFBF6');
        map.put('\uFA2B', '\uFBF7');
        map.put('\u9927', '\uFBF8');
        map.put('\uFA2C', '\uFBF9');
        map.put('\u999E', '\uFBFA');
        map.put('\u9A4E', '\uFBFB');
        map.put('\u9AD9', '\uFBFC');
        return map;
    }

}

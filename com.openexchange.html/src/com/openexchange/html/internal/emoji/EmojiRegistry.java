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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.html.internal.emoji;

import java.io.File;
import java.io.FileReader;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * {@link EmojiRegistry} - The Emoji registry based on <code>"emoji.json"</code> file from <a href="https://github.com/kcthota/emoji4j">emoji4j</a>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class EmojiRegistry {

    private static final EmojiRegistry INSTANCE = new EmojiRegistry();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static EmojiRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * The main method to generate the code from specified <code>"emoji.json"</code> file.
     *
     * @param args The arguments
     * @throws Exception If code generation fails
     */
    public static void main(String[] args) throws Exception {
        FileReader reader = new FileReader(new File("~/git/emoji4j/src/main/resources/emoji.json"));
        try {
            JSONArray jEmojis = new JSONArray(reader);

            System.out.println("gnu.trove.map.TIntObjectMap<String> emojis = new gnu.trove.map.hash.TIntObjectHashMap<String>(" + (jEmojis.length()) + ");");

            for (int i = 0, k = jEmojis.length(); k-- > 0; i++) {
                JSONObject jEmoji = jEmojis.getJSONObject(i);
                int codePoint = jEmoji.getString("emoji").codePointAt(0);
                String hex = Integer.toHexString(codePoint).toUpperCase();
                while (hex.length() < 4) {
                    hex = "0" + hex;
                }
                System.out.println("emojis.put(" + codePoint + ", \"\\u" + hex + "\"); // " + jEmoji.optString("description", "<no-description>"));
            }

        } finally {
            reader.close();
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private final gnu.trove.map.TIntObjectMap<String> emojis;

    /**
     * Initializes a new {@link EmojiRegistry}.
     */
    private EmojiRegistry() {
        super();
        emojis = initEmojiRegistry();
    }

    /**
     * Checks if specified character is known to be an Emoji character.
     *
     * @param c The character to check
     * @return <code>true</code> if character is an Emoji character; otherwise <code>false</code>
     */
    public boolean isEmoji(char c) {
        return emojis.containsKey(c);
    }

    /**
     * Checks if specified code point is known to be an Emoji character.
     *
     * @param codePoint The code point to check
     * @return <code>true</code> if codePoint is an Emoji character; otherwise <code>false</code>
     */
    public boolean isEmoji(int codePoint) {
        return emojis.containsKey(codePoint);
    }

    private gnu.trove.map.TIntObjectMap<String> initEmojiRegistry() {
        gnu.trove.map.TIntObjectMap<String> emojis = new gnu.trove.map.hash.TIntObjectHashMap<String>(845);
        emojis.put(128516, "\u1F604"); // smiling face with open mouth and smiling eyes
        emojis.put(128515, "\u1F603"); // smiling face with open mouth
        emojis.put(128512, "\u1F600"); // grinning face
        emojis.put(128522, "\u1F60A"); // smiling face with smiling eyes
        emojis.put(9786, "\u263A"); // white smiling face
        emojis.put(128521, "\u1F609"); // winking face
        emojis.put(128525, "\u1F60D"); // smiling face with heart-shaped eyes
        emojis.put(128536, "\u1F618"); // face throwing a kiss
        emojis.put(128538, "\u1F61A"); // kissing face with closed eyes
        emojis.put(128535, "\u1F617"); // kissing face
        emojis.put(128537, "\u1F619"); // kissing face with smiling eyes
        emojis.put(128540, "\u1F61C"); // face with stuck-out tongue and winking eye
        emojis.put(128541, "\u1F61D"); // face with stuck-out tongue and tightly-closed eyes
        emojis.put(128539, "\u1F61B"); // face with stuck-out tongue
        emojis.put(128563, "\u1F633"); // flushed face
        emojis.put(128513, "\u1F601"); // grinning face with smiling eyes
        emojis.put(128532, "\u1F614"); // pensive face
        emojis.put(128524, "\u1F60C"); // relieved face
        emojis.put(128530, "\u1F612"); // unamused face
        emojis.put(128542, "\u1F61E"); // disappointed face
        emojis.put(128547, "\u1F623"); // persevering face
        emojis.put(128546, "\u1F622"); // crying face
        emojis.put(128514, "\u1F602"); // face with tears of joy
        emojis.put(128557, "\u1F62D"); // loudly crying face
        emojis.put(128554, "\u1F62A"); // sleepy face
        emojis.put(128549, "\u1F625"); // disappointed but relieved face
        emojis.put(128560, "\u1F630"); // face with open mouth and cold sweat
        emojis.put(128517, "\u1F605"); // smiling face with open mouth and cold sweat
        emojis.put(128531, "\u1F613"); // face with cold sweat
        emojis.put(128553, "\u1F629"); // weary face
        emojis.put(128555, "\u1F62B"); // tired face
        emojis.put(128552, "\u1F628"); // fearful face
        emojis.put(128561, "\u1F631"); // face screaming in fear
        emojis.put(128544, "\u1F620"); // angry face
        emojis.put(128545, "\u1F621"); // pouting face
        emojis.put(128548, "\u1F624"); // face with look of triumph
        emojis.put(128534, "\u1F616"); // confounded face
        emojis.put(128518, "\u1F606"); // smiling face with open mouth and tightly-closed eyes
        emojis.put(128523, "\u1F60B"); // face savouring delicious food
        emojis.put(128567, "\u1F637"); // face with medical mask
        emojis.put(128526, "\u1F60E"); // smiling face with sunglasses
        emojis.put(128564, "\u1F634"); // sleeping face
        emojis.put(128565, "\u1F635"); // dizzy face
        emojis.put(128562, "\u1F632"); // astonished face
        emojis.put(128543, "\u1F61F"); // worried face
        emojis.put(128550, "\u1F626"); // frowning face with open mouth
        emojis.put(128551, "\u1F627"); // anguished face
        emojis.put(128520, "\u1F608"); // smiling face with horns
        emojis.put(128127, "\u1F47F"); // imp
        emojis.put(128558, "\u1F62E"); // face with open mouth
        emojis.put(128556, "\u1F62C"); // grimacing face
        emojis.put(128528, "\u1F610"); // neutral face
        emojis.put(128533, "\u1F615"); // confused face
        emojis.put(128559, "\u1F62F"); // hushed face
        emojis.put(128566, "\u1F636"); // face without mouth
        emojis.put(128519, "\u1F607"); // smiling face with halo
        emojis.put(128527, "\u1F60F"); // smirking face
        emojis.put(128529, "\u1F611"); // expressionless face
        emojis.put(128114, "\u1F472"); // man with gua pi mao
        emojis.put(128115, "\u1F473"); // man with turban
        emojis.put(128110, "\u1F46E"); // police officer
        emojis.put(128119, "\u1F477"); // construction worker
        emojis.put(128130, "\u1F482"); // guardsman
        emojis.put(128118, "\u1F476"); // baby
        emojis.put(128102, "\u1F466"); // boy
        emojis.put(128103, "\u1F467"); // girl
        emojis.put(128104, "\u1F468"); // man
        emojis.put(128105, "\u1F469"); // woman
        emojis.put(128116, "\u1F474"); // older man
        emojis.put(128117, "\u1F475"); // older woman
        emojis.put(128113, "\u1F471"); // person with blond hair
        emojis.put(128124, "\u1F47C"); // baby angel
        emojis.put(128120, "\u1F478"); // princess
        emojis.put(128570, "\u1F63A"); // smiling cat face with open mouth
        emojis.put(128568, "\u1F638"); // grinning cat face with smiling eyes
        emojis.put(128571, "\u1F63B"); // smiling cat face with heart-shaped eyes
        emojis.put(128573, "\u1F63D"); // kissing cat face with closed eyes
        emojis.put(128572, "\u1F63C"); // cat face with wry smile
        emojis.put(128576, "\u1F640"); // weary cat face
        emojis.put(128575, "\u1F63F"); // crying cat face
        emojis.put(128569, "\u1F639"); // cat face with tears of joy
        emojis.put(128574, "\u1F63E"); // pouting cat face
        emojis.put(128121, "\u1F479"); // japanese ogre
        emojis.put(128122, "\u1F47A"); // japanese goblin
        emojis.put(128584, "\u1F648"); // see-no-evil monkey
        emojis.put(128585, "\u1F649"); // hear-no-evil monkey
        emojis.put(128586, "\u1F64A"); // speak-no-evil monkey
        emojis.put(128128, "\u1F480"); // skull
        emojis.put(128125, "\u1F47D"); // extraterrestrial alien
        emojis.put(128169, "\u1F4A9"); // pile of poo
        emojis.put(128293, "\u1F525"); // fire
        emojis.put(10024, "\u2728"); // sparkles
        emojis.put(127775, "\u1F31F"); // glowing star
        emojis.put(128171, "\u1F4AB"); // dizzy symbol
        emojis.put(128165, "\u1F4A5"); // collision symbol
        emojis.put(128162, "\u1F4A2"); // anger symbol
        emojis.put(128166, "\u1F4A6"); // splashing sweat symbol
        emojis.put(128167, "\u1F4A7"); // droplet
        emojis.put(128164, "\u1F4A4"); // sleeping symbol
        emojis.put(128168, "\u1F4A8"); // dash symbol
        emojis.put(128066, "\u1F442"); // ear
        emojis.put(128064, "\u1F440"); // eyes
        emojis.put(128067, "\u1F443"); // nose
        emojis.put(128069, "\u1F445"); // tongue
        emojis.put(128068, "\u1F444"); // mouth
        emojis.put(128077, "\u1F44D"); // thumbs up sign
        emojis.put(128078, "\u1F44E"); // thumbs down sign
        emojis.put(128076, "\u1F44C"); // ok hand sign
        emojis.put(128074, "\u1F44A"); // fisted hand sign
        emojis.put(9994, "\u270A"); // raised fist
        emojis.put(9996, "\u270C"); // victory hand
        emojis.put(128075, "\u1F44B"); // waving hand sign
        emojis.put(9995, "\u270B"); // raised hand
        emojis.put(128080, "\u1F450"); // open hands sign
        emojis.put(128070, "\u1F446"); // white up pointing backhand index
        emojis.put(128071, "\u1F447"); // white down pointing backhand index
        emojis.put(128073, "\u1F449"); // white right pointing backhand index
        emojis.put(128072, "\u1F448"); // white left pointing backhand index
        emojis.put(128588, "\u1F64C"); // person raising both hands in celebration
        emojis.put(128591, "\u1F64F"); // person with folded hands
        emojis.put(9757, "\u261D"); // white up pointing index
        emojis.put(128079, "\u1F44F"); // clapping hands sign
        emojis.put(128170, "\u1F4AA"); // flexed biceps
        emojis.put(128694, "\u1F6B6"); // pedestrian
        emojis.put(127939, "\u1F3C3"); // runner
        emojis.put(128131, "\u1F483"); // dancer
        emojis.put(128107, "\u1F46B"); // man and woman holding hands
        emojis.put(128106, "\u1F46A"); // family
        emojis.put(128108, "\u1F46C"); // two men holding hands
        emojis.put(128109, "\u1F46D"); // two women holding hands
        emojis.put(128143, "\u1F48F"); // kiss
        emojis.put(128145, "\u1F491"); // couple with heart
        emojis.put(128111, "\u1F46F"); // woman with bunny ears
        emojis.put(128582, "\u1F646"); // face with ok gesture
        emojis.put(128581, "\u1F645"); // face with no good gesture
        emojis.put(128129, "\u1F481"); // information desk person
        emojis.put(128587, "\u1F64B"); // happy person raising one hand
        emojis.put(128134, "\u1F486"); // face massage
        emojis.put(128135, "\u1F487"); // haircut
        emojis.put(128133, "\u1F485"); // nail polish
        emojis.put(128112, "\u1F470"); // bride with veil
        emojis.put(128590, "\u1F64E"); // person with pouting face
        emojis.put(128589, "\u1F64D"); // person frowning
        emojis.put(128583, "\u1F647"); // person bowing deeply
        emojis.put(127913, "\u1F3A9"); // top hat
        emojis.put(128081, "\u1F451"); // crown
        emojis.put(128082, "\u1F452"); // womans hat
        emojis.put(128095, "\u1F45F"); // athletic shoe
        emojis.put(128094, "\u1F45E"); // mans shoe
        emojis.put(128097, "\u1F461"); // womans sandal
        emojis.put(128096, "\u1F460"); // high-heeled shoe
        emojis.put(128098, "\u1F462"); // womans boots
        emojis.put(128085, "\u1F455"); // t-shirt
        emojis.put(128084, "\u1F454"); // necktie
        emojis.put(128090, "\u1F45A"); // womans clothes
        emojis.put(128087, "\u1F457"); // dress
        emojis.put(127933, "\u1F3BD"); // running shirt with sash
        emojis.put(128086, "\u1F456"); // jeans
        emojis.put(128088, "\u1F458"); // kimono
        emojis.put(128089, "\u1F459"); // bikini
        emojis.put(128188, "\u1F4BC"); // briefcase
        emojis.put(128092, "\u1F45C"); // handbag
        emojis.put(128093, "\u1F45D"); // pouch
        emojis.put(128091, "\u1F45B"); // purse
        emojis.put(128083, "\u1F453"); // eyeglasses
        emojis.put(127872, "\u1F380"); // ribbon
        emojis.put(127746, "\u1F302"); // closed umbrella
        emojis.put(128132, "\u1F484"); // lipstick
        emojis.put(128155, "\u1F49B"); // yellow heart
        emojis.put(128153, "\u1F499"); // blue heart
        emojis.put(128156, "\u1F49C"); // purple heart
        emojis.put(128154, "\u1F49A"); // green heart
        emojis.put(10084, "\u2764"); // heavy black heart
        emojis.put(128148, "\u1F494"); // broken heart
        emojis.put(128151, "\u1F497"); // growing heart
        emojis.put(128147, "\u1F493"); // beating heart
        emojis.put(128149, "\u1F495"); // two hearts
        emojis.put(128150, "\u1F496"); // sparkling heart
        emojis.put(128158, "\u1F49E"); // revolving hearts
        emojis.put(128152, "\u1F498"); // heart with arrow
        emojis.put(128140, "\u1F48C"); // love letter
        emojis.put(128139, "\u1F48B"); // kiss mark
        emojis.put(128141, "\u1F48D"); // ring
        emojis.put(128142, "\u1F48E"); // gem stone
        emojis.put(128100, "\u1F464"); // bust in silhouette
        emojis.put(128101, "\u1F465"); // busts in silhouette
        emojis.put(128172, "\u1F4AC"); // speech balloon
        emojis.put(128099, "\u1F463"); // footprints
        emojis.put(128173, "\u1F4AD"); // thought balloon
        emojis.put(128054, "\u1F436"); // dog face
        emojis.put(128058, "\u1F43A"); // wolf face
        emojis.put(128049, "\u1F431"); // cat face
        emojis.put(128045, "\u1F42D"); // mouse face
        emojis.put(128057, "\u1F439"); // hamster face
        emojis.put(128048, "\u1F430"); // rabbit face
        emojis.put(128056, "\u1F438"); // frog face
        emojis.put(128047, "\u1F42F"); // tiger face
        emojis.put(128040, "\u1F428"); // koala
        emojis.put(128059, "\u1F43B"); // bear face
        emojis.put(128055, "\u1F437"); // pig face
        emojis.put(128061, "\u1F43D"); // pig nose
        emojis.put(128046, "\u1F42E"); // cow face
        emojis.put(128023, "\u1F417"); // boar
        emojis.put(128053, "\u1F435"); // monkey face
        emojis.put(128018, "\u1F412"); // monkey
        emojis.put(128052, "\u1F434"); // horse face
        emojis.put(128017, "\u1F411"); // sheep
        emojis.put(128024, "\u1F418"); // elephant
        emojis.put(128060, "\u1F43C"); // panda face
        emojis.put(128039, "\u1F427"); // penguin
        emojis.put(128038, "\u1F426"); // bird
        emojis.put(128036, "\u1F424"); // baby chick
        emojis.put(128037, "\u1F425"); // front-facing baby chick
        emojis.put(128035, "\u1F423"); // hatching chick
        emojis.put(128020, "\u1F414"); // chicken
        emojis.put(128013, "\u1F40D"); // snake
        emojis.put(128034, "\u1F422"); // turtle
        emojis.put(128027, "\u1F41B"); // bug
        emojis.put(128029, "\u1F41D"); // honeybee
        emojis.put(128028, "\u1F41C"); // ant
        emojis.put(128030, "\u1F41E"); // lady beetle
        emojis.put(128012, "\u1F40C"); // snail
        emojis.put(128025, "\u1F419"); // octopus
        emojis.put(128026, "\u1F41A"); // spiral shell
        emojis.put(128032, "\u1F420"); // tropical fish
        emojis.put(128031, "\u1F41F"); // fish
        emojis.put(128044, "\u1F42C"); // dolphin
        emojis.put(128051, "\u1F433"); // spouting whale
        emojis.put(128011, "\u1F40B"); // whale
        emojis.put(128004, "\u1F404"); // cow
        emojis.put(128015, "\u1F40F"); // ram
        emojis.put(128000, "\u1F400"); // rat
        emojis.put(128003, "\u1F403"); // water buffalo
        emojis.put(128005, "\u1F405"); // tiger
        emojis.put(128007, "\u1F407"); // rabbit
        emojis.put(128009, "\u1F409"); // dragon
        emojis.put(128014, "\u1F40E"); // horse
        emojis.put(128016, "\u1F410"); // goat
        emojis.put(128019, "\u1F413"); // rooster
        emojis.put(128021, "\u1F415"); // dog
        emojis.put(128022, "\u1F416"); // pig
        emojis.put(128001, "\u1F401"); // mouse
        emojis.put(128002, "\u1F402"); // ox
        emojis.put(128050, "\u1F432"); // dragon face
        emojis.put(128033, "\u1F421"); // blowfish
        emojis.put(128010, "\u1F40A"); // crocodile
        emojis.put(128043, "\u1F42B"); // bactrian camel
        emojis.put(128042, "\u1F42A"); // dromedary camel
        emojis.put(128006, "\u1F406"); // leopard
        emojis.put(128008, "\u1F408"); // cat
        emojis.put(128041, "\u1F429"); // poodle
        emojis.put(128062, "\u1F43E"); // paw prints
        emojis.put(128144, "\u1F490"); // bouquet
        emojis.put(127800, "\u1F338"); // cherry blossom
        emojis.put(127799, "\u1F337"); // tulip
        emojis.put(127808, "\u1F340"); // four leaf clover
        emojis.put(127801, "\u1F339"); // rose
        emojis.put(127803, "\u1F33B"); // sunflower
        emojis.put(127802, "\u1F33A"); // hibiscus
        emojis.put(127809, "\u1F341"); // maple leaf
        emojis.put(127811, "\u1F343"); // leaf fluttering in wind
        emojis.put(127810, "\u1F342"); // fallen leaf
        emojis.put(127807, "\u1F33F"); // herb
        emojis.put(127806, "\u1F33E"); // ear of rice
        emojis.put(127812, "\u1F344"); // mushroom
        emojis.put(127797, "\u1F335"); // cactus
        emojis.put(127796, "\u1F334"); // palm tree
        emojis.put(127794, "\u1F332"); // evergreen tree
        emojis.put(127795, "\u1F333"); // deciduous tree
        emojis.put(127792, "\u1F330"); // chestnut
        emojis.put(127793, "\u1F331"); // seedling
        emojis.put(127804, "\u1F33C"); // blossom
        emojis.put(127760, "\u1F310"); // globe with meridians
        emojis.put(127774, "\u1F31E"); // sun with face
        emojis.put(127773, "\u1F31D"); // full moon with face
        emojis.put(127770, "\u1F31A"); // new moon with face
        emojis.put(127761, "\u1F311"); // new moon symbol
        emojis.put(127762, "\u1F312"); // waxing crescent moon symbol
        emojis.put(127763, "\u1F313"); // first quarter moon symbol
        emojis.put(127764, "\u1F314"); // waxing gibbous moon symbol
        emojis.put(127765, "\u1F315"); // full moon symbol
        emojis.put(127766, "\u1F316"); // waning gibbous moon symbol
        emojis.put(127767, "\u1F317"); // last quarter moon symbol
        emojis.put(127768, "\u1F318"); // waning crescent moon symbol
        emojis.put(127772, "\u1F31C"); // last quarter moon with face
        emojis.put(127771, "\u1F31B"); // first quarter moon with face
        emojis.put(127769, "\u1F319"); // crescent moon
        emojis.put(127757, "\u1F30D"); // earth globe europe-africa
        emojis.put(127758, "\u1F30E"); // earth globe americas
        emojis.put(127759, "\u1F30F"); // earth globe asia-australia
        emojis.put(127755, "\u1F30B"); // volcano
        emojis.put(127756, "\u1F30C"); // milky way
        emojis.put(127776, "\u1F320"); // shooting star
        emojis.put(11088, "\u2B50"); // white medium star
        emojis.put(9728, "\u2600"); // black sun with rays
        emojis.put(9925, "\u26C5"); // sun behind cloud
        emojis.put(9729, "\u2601"); // cloud
        emojis.put(9889, "\u26A1"); // high voltage sign
        emojis.put(9748, "\u2614"); // umbrella with rain drops
        emojis.put(10052, "\u2744"); // snowflake
        emojis.put(9924, "\u26C4"); // snowman without snow
        emojis.put(127744, "\u1F300"); // cyclone
        emojis.put(127745, "\u1F301"); // foggy
        emojis.put(127752, "\u1F308"); // rainbow
        emojis.put(127754, "\u1F30A"); // water wave
        emojis.put(127885, "\u1F38D"); // pine decoration
        emojis.put(128157, "\u1F49D"); // heart with ribbon
        emojis.put(127886, "\u1F38E"); // japanese dolls
        emojis.put(127890, "\u1F392"); // school satchel
        emojis.put(127891, "\u1F393"); // graduation cap
        emojis.put(127887, "\u1F38F"); // carp streamer
        emojis.put(127878, "\u1F386"); // fireworks
        emojis.put(127879, "\u1F387"); // firework sparkler
        emojis.put(127888, "\u1F390"); // wind chime
        emojis.put(127889, "\u1F391"); // moon viewing ceremony
        emojis.put(127875, "\u1F383"); // jack-o-lantern
        emojis.put(128123, "\u1F47B"); // ghost
        emojis.put(127877, "\u1F385"); // father christmas
        emojis.put(127876, "\u1F384"); // christmas tree
        emojis.put(127873, "\u1F381"); // wrapped present
        emojis.put(127883, "\u1F38B"); // tanabata tree
        emojis.put(127881, "\u1F389"); // party popper
        emojis.put(127882, "\u1F38A"); // confetti ball
        emojis.put(127880, "\u1F388"); // balloon
        emojis.put(127884, "\u1F38C"); // crossed flags
        emojis.put(128302, "\u1F52E"); // crystal ball
        emojis.put(127909, "\u1F3A5"); // movie camera
        emojis.put(128247, "\u1F4F7"); // camera
        emojis.put(128249, "\u1F4F9"); // video camera
        emojis.put(128252, "\u1F4FC"); // videocassette
        emojis.put(128191, "\u1F4BF"); // optical disc
        emojis.put(128192, "\u1F4C0"); // dvd
        emojis.put(128189, "\u1F4BD"); // minidisc
        emojis.put(128190, "\u1F4BE"); // floppy disk
        emojis.put(128187, "\u1F4BB"); // personal computer
        emojis.put(128241, "\u1F4F1"); // mobile phone
        emojis.put(9742, "\u260E"); // black telephone
        emojis.put(128222, "\u1F4DE"); // telephone receiver
        emojis.put(128223, "\u1F4DF"); // pager
        emojis.put(128224, "\u1F4E0"); // fax machine
        emojis.put(128225, "\u1F4E1"); // satellite antenna
        emojis.put(128250, "\u1F4FA"); // television
        emojis.put(128251, "\u1F4FB"); // radio
        emojis.put(128266, "\u1F50A"); // speaker with three sound waves
        emojis.put(128265, "\u1F509"); // speaker with one sound wave
        emojis.put(128264, "\u1F508"); // speaker
        emojis.put(128263, "\u1F507"); // speaker with cancellation stroke
        emojis.put(128276, "\u1F514"); // bell
        emojis.put(128277, "\u1F515"); // bell with cancellation stroke
        emojis.put(128226, "\u1F4E2"); // public address loudspeaker
        emojis.put(128227, "\u1F4E3"); // cheering megaphone
        emojis.put(9203, "\u23F3"); // hourglass with flowing sand
        emojis.put(8987, "\u231B"); // hourglass
        emojis.put(9200, "\u23F0"); // alarm clock
        emojis.put(8986, "\u231A"); // watch
        emojis.put(128275, "\u1F513"); // open lock
        emojis.put(128274, "\u1F512"); // lock
        emojis.put(128271, "\u1F50F"); // lock with ink pen
        emojis.put(128272, "\u1F510"); // closed lock with key
        emojis.put(128273, "\u1F511"); // key
        emojis.put(128270, "\u1F50E"); // right-pointing magnifying glass
        emojis.put(128161, "\u1F4A1"); // electric light bulb
        emojis.put(128294, "\u1F526"); // electric torch
        emojis.put(128262, "\u1F506"); // high brightness symbol
        emojis.put(128261, "\u1F505"); // low brightness symbol
        emojis.put(128268, "\u1F50C"); // electric plug
        emojis.put(128267, "\u1F50B"); // battery
        emojis.put(128269, "\u1F50D"); // left-pointing magnifying glass
        emojis.put(128705, "\u1F6C1"); // bathtub
        emojis.put(128704, "\u1F6C0"); // bath
        emojis.put(128703, "\u1F6BF"); // shower
        emojis.put(128701, "\u1F6BD"); // toilet
        emojis.put(128295, "\u1F527"); // wrench
        emojis.put(128297, "\u1F529"); // nut and bolt
        emojis.put(128296, "\u1F528"); // hammer
        emojis.put(128682, "\u1F6AA"); // door
        emojis.put(128684, "\u1F6AC"); // smoking symbol
        emojis.put(128163, "\u1F4A3"); // bomb
        emojis.put(128299, "\u1F52B"); // pistol
        emojis.put(128298, "\u1F52A"); // hocho
        emojis.put(128138, "\u1F48A"); // pill
        emojis.put(128137, "\u1F489"); // syringe
        emojis.put(128176, "\u1F4B0"); // money bag
        emojis.put(128180, "\u1F4B4"); // banknote with yen sign
        emojis.put(128181, "\u1F4B5"); // banknote with dollar sign
        emojis.put(128183, "\u1F4B7"); // banknote with pound sign
        emojis.put(128182, "\u1F4B6"); // banknote with euro sign
        emojis.put(128179, "\u1F4B3"); // credit card
        emojis.put(128184, "\u1F4B8"); // money with wings
        emojis.put(128242, "\u1F4F2"); // mobile phone with rightwards arrow at left
        emojis.put(128231, "\u1F4E7"); // e-mail symbol
        emojis.put(128229, "\u1F4E5"); // inbox tray
        emojis.put(128228, "\u1F4E4"); // outbox tray
        emojis.put(9993, "\u2709"); // envelope
        emojis.put(128233, "\u1F4E9"); // envelope with downwards arrow above
        emojis.put(128232, "\u1F4E8"); // incoming envelope
        emojis.put(128239, "\u1F4EF"); // postal horn
        emojis.put(128235, "\u1F4EB"); // closed mailbox with raised flag
        emojis.put(128234, "\u1F4EA"); // closed mailbox with lowered flag
        emojis.put(128236, "\u1F4EC"); // open mailbox with raised flag
        emojis.put(128237, "\u1F4ED"); // open mailbox with lowered flag
        emojis.put(128238, "\u1F4EE"); // postbox
        emojis.put(128230, "\u1F4E6"); // package
        emojis.put(128221, "\u1F4DD"); // memo
        emojis.put(128196, "\u1F4C4"); // page facing up
        emojis.put(128195, "\u1F4C3"); // page with curl
        emojis.put(128209, "\u1F4D1"); // bookmark tabs
        emojis.put(128202, "\u1F4CA"); // bar chart
        emojis.put(128200, "\u1F4C8"); // chart with upwards trend
        emojis.put(128201, "\u1F4C9"); // chart with downwards trend
        emojis.put(128220, "\u1F4DC"); // scroll
        emojis.put(128203, "\u1F4CB"); // clipboard
        emojis.put(128197, "\u1F4C5"); // calendar
        emojis.put(128198, "\u1F4C6"); // tear-off calendar
        emojis.put(128199, "\u1F4C7"); // card index
        emojis.put(128193, "\u1F4C1"); // file folder
        emojis.put(128194, "\u1F4C2"); // open file folder
        emojis.put(9986, "\u2702"); // black scissors
        emojis.put(128204, "\u1F4CC"); // pushpin
        emojis.put(128206, "\u1F4CE"); // paperclip
        emojis.put(10002, "\u2712"); // black nib
        emojis.put(9999, "\u270F"); // pencil
        emojis.put(128207, "\u1F4CF"); // straight ruler
        emojis.put(128208, "\u1F4D0"); // triangular ruler
        emojis.put(128213, "\u1F4D5"); // closed book
        emojis.put(128215, "\u1F4D7"); // green book
        emojis.put(128216, "\u1F4D8"); // blue book
        emojis.put(128217, "\u1F4D9"); // orange book
        emojis.put(128211, "\u1F4D3"); // notebook
        emojis.put(128212, "\u1F4D4"); // notebook with decorative cover
        emojis.put(128210, "\u1F4D2"); // ledger
        emojis.put(128218, "\u1F4DA"); // books
        emojis.put(128214, "\u1F4D6"); // open book
        emojis.put(128278, "\u1F516"); // bookmark
        emojis.put(128219, "\u1F4DB"); // name badge
        emojis.put(128300, "\u1F52C"); // microscope
        emojis.put(128301, "\u1F52D"); // telescope
        emojis.put(128240, "\u1F4F0"); // newspaper
        emojis.put(127912, "\u1F3A8"); // artist palette
        emojis.put(127916, "\u1F3AC"); // clapper board
        emojis.put(127908, "\u1F3A4"); // microphone
        emojis.put(127911, "\u1F3A7"); // headphone
        emojis.put(127932, "\u1F3BC"); // musical score
        emojis.put(127925, "\u1F3B5"); // musical note
        emojis.put(127926, "\u1F3B6"); // multiple musical notes
        emojis.put(127929, "\u1F3B9"); // musical keyboard
        emojis.put(127931, "\u1F3BB"); // violin
        emojis.put(127930, "\u1F3BA"); // trumpet
        emojis.put(127927, "\u1F3B7"); // saxophone
        emojis.put(127928, "\u1F3B8"); // guitar
        emojis.put(128126, "\u1F47E"); // alien monster
        emojis.put(127918, "\u1F3AE"); // video game
        emojis.put(127183, "\u1F0CF"); // playing card black joker
        emojis.put(127924, "\u1F3B4"); // flower playing cards
        emojis.put(126980, "\u1F004"); // mahjong tile red dragon
        emojis.put(127922, "\u1F3B2"); // game die
        emojis.put(127919, "\u1F3AF"); // direct hit
        emojis.put(127944, "\u1F3C8"); // american football
        emojis.put(127936, "\u1F3C0"); // basketball and hoop
        emojis.put(9917, "\u26BD"); // soccer ball
        emojis.put(9918, "\u26BE"); // baseball
        emojis.put(127934, "\u1F3BE"); // tennis racquet and ball
        emojis.put(127921, "\u1F3B1"); // billiards
        emojis.put(127945, "\u1F3C9"); // rugby football
        emojis.put(127923, "\u1F3B3"); // bowling
        emojis.put(9971, "\u26F3"); // flag in hole
        emojis.put(128693, "\u1F6B5"); // mountain bicyclist
        emojis.put(128692, "\u1F6B4"); // bicyclist
        emojis.put(127937, "\u1F3C1"); // chequered flag
        emojis.put(127943, "\u1F3C7"); // horse racing
        emojis.put(127942, "\u1F3C6"); // trophy
        emojis.put(127935, "\u1F3BF"); // ski and ski boot
        emojis.put(127938, "\u1F3C2"); // snowboarder
        emojis.put(127946, "\u1F3CA"); // swimmer
        emojis.put(127940, "\u1F3C4"); // surfer
        emojis.put(127907, "\u1F3A3"); // fishing pole and fish
        emojis.put(9749, "\u2615"); // hot beverage
        emojis.put(127861, "\u1F375"); // teacup without handle
        emojis.put(127862, "\u1F376"); // sake bottle and cup
        emojis.put(127868, "\u1F37C"); // baby bottle
        emojis.put(127866, "\u1F37A"); // beer mug
        emojis.put(127867, "\u1F37B"); // clinking beer mugs
        emojis.put(127864, "\u1F378"); // cocktail glass
        emojis.put(127865, "\u1F379"); // tropical drink
        emojis.put(127863, "\u1F377"); // wine glass
        emojis.put(127860, "\u1F374"); // fork and knife
        emojis.put(127829, "\u1F355"); // slice of pizza
        emojis.put(127828, "\u1F354"); // hamburger
        emojis.put(127839, "\u1F35F"); // french fries
        emojis.put(127831, "\u1F357"); // poultry leg
        emojis.put(127830, "\u1F356"); // meat on bone
        emojis.put(127837, "\u1F35D"); // spaghetti
        emojis.put(127835, "\u1F35B"); // curry and rice
        emojis.put(127844, "\u1F364"); // fried shrimp
        emojis.put(127857, "\u1F371"); // bento box
        emojis.put(127843, "\u1F363"); // sushi
        emojis.put(127845, "\u1F365"); // fish cake with swirl design
        emojis.put(127833, "\u1F359"); // rice ball
        emojis.put(127832, "\u1F358"); // rice cracker
        emojis.put(127834, "\u1F35A"); // cooked rice
        emojis.put(127836, "\u1F35C"); // steaming bowl
        emojis.put(127858, "\u1F372"); // pot of food
        emojis.put(127842, "\u1F362"); // oden
        emojis.put(127841, "\u1F361"); // dango
        emojis.put(127859, "\u1F373"); // cooking
        emojis.put(127838, "\u1F35E"); // bread
        emojis.put(127849, "\u1F369"); // doughnut
        emojis.put(127854, "\u1F36E"); // custard
        emojis.put(127846, "\u1F366"); // soft ice cream
        emojis.put(127848, "\u1F368"); // ice cream
        emojis.put(127847, "\u1F367"); // shaved ice
        emojis.put(127874, "\u1F382"); // birthday cake
        emojis.put(127856, "\u1F370"); // shortcake
        emojis.put(127850, "\u1F36A"); // cookie
        emojis.put(127851, "\u1F36B"); // chocolate bar
        emojis.put(127852, "\u1F36C"); // candy
        emojis.put(127853, "\u1F36D"); // lollipop
        emojis.put(127855, "\u1F36F"); // honey pot
        emojis.put(127822, "\u1F34E"); // red apple
        emojis.put(127823, "\u1F34F"); // green apple
        emojis.put(127818, "\u1F34A"); // tangerine
        emojis.put(127819, "\u1F34B"); // lemon
        emojis.put(127826, "\u1F352"); // cherries
        emojis.put(127815, "\u1F347"); // grapes
        emojis.put(127817, "\u1F349"); // watermelon
        emojis.put(127827, "\u1F353"); // strawberry
        emojis.put(127825, "\u1F351"); // peach
        emojis.put(127816, "\u1F348"); // melon
        emojis.put(127820, "\u1F34C"); // banana
        emojis.put(127824, "\u1F350"); // pear
        emojis.put(127821, "\u1F34D"); // pineapple
        emojis.put(127840, "\u1F360"); // roasted sweet potato
        emojis.put(127814, "\u1F346"); // aubergine
        emojis.put(127813, "\u1F345"); // tomato
        emojis.put(127805, "\u1F33D"); // ear of maize
        emojis.put(127968, "\u1F3E0"); // house building
        emojis.put(127969, "\u1F3E1"); // house with garden
        emojis.put(127979, "\u1F3EB"); // school
        emojis.put(127970, "\u1F3E2"); // office building
        emojis.put(127971, "\u1F3E3"); // japanese post office
        emojis.put(127973, "\u1F3E5"); // hospital
        emojis.put(127974, "\u1F3E6"); // bank
        emojis.put(127978, "\u1F3EA"); // convenience store
        emojis.put(127977, "\u1F3E9"); // love hotel
        emojis.put(127976, "\u1F3E8"); // hotel
        emojis.put(128146, "\u1F492"); // wedding
        emojis.put(9962, "\u26EA"); // church
        emojis.put(127980, "\u1F3EC"); // department store
        emojis.put(127972, "\u1F3E4"); // european post office
        emojis.put(127751, "\u1F307"); // sunset over buildings
        emojis.put(127750, "\u1F306"); // cityscape at dusk
        emojis.put(127983, "\u1F3EF"); // japanese castle
        emojis.put(127984, "\u1F3F0"); // european castle
        emojis.put(9978, "\u26FA"); // tent
        emojis.put(127981, "\u1F3ED"); // factory
        emojis.put(128508, "\u1F5FC"); // tokyo tower
        emojis.put(128510, "\u1F5FE"); // silhouette of japan
        emojis.put(128507, "\u1F5FB"); // mount fuji
        emojis.put(127748, "\u1F304"); // sunrise over mountains
        emojis.put(127749, "\u1F305"); // sunrise
        emojis.put(127747, "\u1F303"); // night with stars
        emojis.put(128509, "\u1F5FD"); // statue of liberty
        emojis.put(127753, "\u1F309"); // bridge at night
        emojis.put(127904, "\u1F3A0"); // carousel horse
        emojis.put(127905, "\u1F3A1"); // ferris wheel
        emojis.put(9970, "\u26F2"); // fountain
        emojis.put(127906, "\u1F3A2"); // roller coaster
        emojis.put(128674, "\u1F6A2"); // ship
        emojis.put(9973, "\u26F5"); // sailboat
        emojis.put(128676, "\u1F6A4"); // speedboat
        emojis.put(128675, "\u1F6A3"); // rowboat
        emojis.put(9875, "\u2693"); // anchor
        emojis.put(128640, "\u1F680"); // rocket
        emojis.put(9992, "\u2708"); // airplane
        emojis.put(128186, "\u1F4BA"); // seat
        emojis.put(128641, "\u1F681"); // helicopter
        emojis.put(128642, "\u1F682"); // steam locomotive
        emojis.put(128650, "\u1F68A"); // tram
        emojis.put(128649, "\u1F689"); // station
        emojis.put(128670, "\u1F69E"); // mountain railway
        emojis.put(128646, "\u1F686"); // train
        emojis.put(128644, "\u1F684"); // high-speed train
        emojis.put(128645, "\u1F685"); // high-speed train with bullet nose
        emojis.put(128648, "\u1F688"); // light rail
        emojis.put(128647, "\u1F687"); // metro
        emojis.put(128669, "\u1F69D"); // monorail
        emojis.put(128651, "\u1F68B"); // tram car
        emojis.put(128643, "\u1F683"); // railway car
        emojis.put(128654, "\u1F68E"); // trolleybus
        emojis.put(128652, "\u1F68C"); // bus
        emojis.put(128653, "\u1F68D"); // oncoming bus
        emojis.put(128665, "\u1F699"); // recreational vehicle
        emojis.put(128664, "\u1F698"); // oncoming automobile
        emojis.put(128663, "\u1F697"); // automobile
        emojis.put(128661, "\u1F695"); // taxi
        emojis.put(128662, "\u1F696"); // oncoming taxi
        emojis.put(128667, "\u1F69B"); // articulated lorry
        emojis.put(128666, "\u1F69A"); // delivery truck
        emojis.put(128680, "\u1F6A8"); // police cars revolving light
        emojis.put(128659, "\u1F693"); // police car
        emojis.put(128660, "\u1F694"); // oncoming police car
        emojis.put(128658, "\u1F692"); // fire engine
        emojis.put(128657, "\u1F691"); // ambulance
        emojis.put(128656, "\u1F690"); // minibus
        emojis.put(128690, "\u1F6B2"); // bicycle
        emojis.put(128673, "\u1F6A1"); // aerial tramway
        emojis.put(128671, "\u1F69F"); // suspension railway
        emojis.put(128672, "\u1F6A0"); // mountain cableway
        emojis.put(128668, "\u1F69C"); // tractor
        emojis.put(128136, "\u1F488"); // barber pole
        emojis.put(128655, "\u1F68F"); // bus stop
        emojis.put(127915, "\u1F3AB"); // ticket
        emojis.put(128678, "\u1F6A6"); // vertical traffic light
        emojis.put(128677, "\u1F6A5"); // horizontal traffic light
        emojis.put(9888, "\u26A0"); // warning sign
        emojis.put(128679, "\u1F6A7"); // construction sign
        emojis.put(128304, "\u1F530"); // japanese symbol for beginner
        emojis.put(9981, "\u26FD"); // fuel pump
        emojis.put(127982, "\u1F3EE"); // izakaya lantern
        emojis.put(127920, "\u1F3B0"); // slot machine
        emojis.put(9832, "\u2668"); // hot springs
        emojis.put(128511, "\u1F5FF"); // moyai
        emojis.put(127914, "\u1F3AA"); // circus tent
        emojis.put(127917, "\u1F3AD"); // performing arts
        emojis.put(128205, "\u1F4CD"); // round pushpin
        emojis.put(128681, "\u1F6A9"); // triangular flag on post
        emojis.put(127471, "\u1F1EF"); // regional indicator symbol letter j + regional indicator symbol letter p
        emojis.put(127472, "\u1F1F0"); // regional indicator symbol letter k + regional indicator symbol letter r
        emojis.put(127465, "\u1F1E9"); // regional indicator symbol letter d + regional indicator symbol letter e
        emojis.put(127464, "\u1F1E8"); // regional indicator symbol letter c + regional indicator symbol letter n
        emojis.put(127482, "\u1F1FA"); // regional indicator symbol letter u + regional indicator symbol letter s
        emojis.put(127467, "\u1F1EB"); // regional indicator symbol letter f + regional indicator symbol letter r
        emojis.put(127466, "\u1F1EA"); // regional indicator symbol letter e + regional indicator symbol letter s
        emojis.put(127470, "\u1F1EE"); // regional indicator symbol letter i + regional indicator symbol letter t
        emojis.put(127479, "\u1F1F7"); // regional indicator symbol letter r + regional indicator symbol letter u
        emojis.put(127468, "\u1F1EC"); // regional indicator symbol letter g + regional indicator symbol letter b
        emojis.put(49, "\u0031"); // digit one + combining enclosing keycap
        emojis.put(50, "\u0032"); // digit two + combining enclosing keycap
        emojis.put(51, "\u0033"); // digit three + combining enclosing keycap
        emojis.put(52, "\u0034"); // digit four + combining enclosing keycap
        emojis.put(53, "\u0035"); // digit five + combining enclosing keycap
        emojis.put(54, "\u0036"); // digit six + combining enclosing keycap
        emojis.put(55, "\u0037"); // digit seven + combining enclosing keycap
        emojis.put(56, "\u0038"); // digit eight + combining enclosing keycap
        emojis.put(57, "\u0039"); // digit nine + combining enclosing keycap
        emojis.put(48, "\u0030"); // digit zero + combining enclosing keycap
        emojis.put(128287, "\u1F51F"); // keycap ten
        emojis.put(128290, "\u1F522"); // input symbol for numbers
        emojis.put(35, "\u0023"); // number sign + combining enclosing keycap
        emojis.put(128291, "\u1F523"); // input symbol for symbols
        emojis.put(11014, "\u2B06"); // upwards black arrow
        emojis.put(11015, "\u2B07"); // downwards black arrow
        emojis.put(11013, "\u2B05"); // leftwards black arrow
        emojis.put(10145, "\u27A1"); // black rightwards arrow
        emojis.put(128288, "\u1F520"); // input symbol for latin capital letters
        emojis.put(128289, "\u1F521"); // input symbol for latin small letters
        emojis.put(128292, "\u1F524"); // input symbol for latin letters
        emojis.put(8599, "\u2197"); // north east arrow
        emojis.put(8598, "\u2196"); // north west arrow
        emojis.put(8600, "\u2198"); // south east arrow
        emojis.put(8601, "\u2199"); // south west arrow
        emojis.put(8596, "\u2194"); // left right arrow
        emojis.put(8597, "\u2195"); // up down arrow
        emojis.put(128260, "\u1F504"); // anticlockwise downwards and upwards open circle arrows
        emojis.put(9664, "\u25C0"); // black left-pointing triangle
        emojis.put(9654, "\u25B6"); // black right-pointing triangle
        emojis.put(128316, "\u1F53C"); // up-pointing small red triangle
        emojis.put(128317, "\u1F53D"); // down-pointing small red triangle
        emojis.put(8617, "\u21A9"); // leftwards arrow with hook
        emojis.put(8618, "\u21AA"); // rightwards arrow with hook
        emojis.put(8505, "\u2139"); // information source
        emojis.put(9194, "\u23EA"); // black left-pointing double triangle
        emojis.put(9193, "\u23E9"); // black right-pointing double triangle
        emojis.put(9195, "\u23EB"); // black up-pointing double triangle
        emojis.put(9196, "\u23EC"); // black down-pointing double triangle
        emojis.put(10549, "\u2935"); // arrow pointing rightwards then curving downwards
        emojis.put(10548, "\u2934"); // arrow pointing rightwards then curving upwards
        emojis.put(127383, "\u1F197"); // squared ok
        emojis.put(128256, "\u1F500"); // twisted rightwards arrows
        emojis.put(128257, "\u1F501"); // clockwise rightwards and leftwards open circle arrows
        emojis.put(128258, "\u1F502"); // clockwise rightwards and leftwards open circle arrows with circled one overlay
        emojis.put(127381, "\u1F195"); // squared new
        emojis.put(127385, "\u1F199"); // squared up with exclamation mark
        emojis.put(127378, "\u1F192"); // squared cool
        emojis.put(127379, "\u1F193"); // squared free
        emojis.put(127382, "\u1F196"); // squared ng
        emojis.put(128246, "\u1F4F6"); // antenna with bars
        emojis.put(127910, "\u1F3A6"); // cinema
        emojis.put(127489, "\u1F201"); // squared katakana koko
        emojis.put(127535, "\u1F22F"); // squared cjk unified ideograph-6307
        emojis.put(127539, "\u1F233"); // squared cjk unified ideograph-7a7a
        emojis.put(127541, "\u1F235"); // squared cjk unified ideograph-6e80
        emojis.put(127540, "\u1F234"); // squared cjk unified ideograph-5408
        emojis.put(127538, "\u1F232"); // squared cjk unified ideograph-7981
        emojis.put(127568, "\u1F250"); // circled ideograph advantage
        emojis.put(127545, "\u1F239"); // squared cjk unified ideograph-5272
        emojis.put(127546, "\u1F23A"); // squared cjk unified ideograph-55b6
        emojis.put(127542, "\u1F236"); // squared cjk unified ideograph-6709
        emojis.put(127514, "\u1F21A"); // squared cjk unified ideograph-7121
        emojis.put(128699, "\u1F6BB"); // restroom
        emojis.put(128697, "\u1F6B9"); // mens symbol
        emojis.put(128698, "\u1F6BA"); // womens symbol
        emojis.put(128700, "\u1F6BC"); // baby symbol
        emojis.put(128702, "\u1F6BE"); // water closet
        emojis.put(128688, "\u1F6B0"); // potable water symbol
        emojis.put(128686, "\u1F6AE"); // put litter in its place symbol
        emojis.put(127359, "\u1F17F"); // negative squared latin capital letter p
        emojis.put(9855, "\u267F"); // wheelchair symbol
        emojis.put(128685, "\u1F6AD"); // no smoking symbol
        emojis.put(127543, "\u1F237"); // squared cjk unified ideograph-6708
        emojis.put(127544, "\u1F238"); // squared cjk unified ideograph-7533
        emojis.put(127490, "\u1F202"); // squared katakana sa
        emojis.put(9410, "\u24C2"); // circled latin capital letter m
        emojis.put(128706, "\u1F6C2"); // passport control
        emojis.put(128708, "\u1F6C4"); // baggage claim
        emojis.put(128709, "\u1F6C5"); // left luggage
        emojis.put(128707, "\u1F6C3"); // customs
        emojis.put(127569, "\u1F251"); // circled ideograph accept
        emojis.put(12953, "\u3299"); // circled ideograph secret
        emojis.put(12951, "\u3297"); // circled ideograph congratulation
        emojis.put(127377, "\u1F191"); // squared cl
        emojis.put(127384, "\u1F198"); // squared sos
        emojis.put(127380, "\u1F194"); // squared id
        emojis.put(128683, "\u1F6AB"); // no entry sign
        emojis.put(128286, "\u1F51E"); // no one under eighteen symbol
        emojis.put(128245, "\u1F4F5"); // no mobile phones
        emojis.put(128687, "\u1F6AF"); // do not litter symbol
        emojis.put(128689, "\u1F6B1"); // non-potable water symbol
        emojis.put(128691, "\u1F6B3"); // no bicycles
        emojis.put(128695, "\u1F6B7"); // no pedestrians
        emojis.put(128696, "\u1F6B8"); // children crossing
        emojis.put(9940, "\u26D4"); // no entry
        emojis.put(10035, "\u2733"); // eight spoked asterisk
        emojis.put(10055, "\u2747"); // sparkle
        emojis.put(10062, "\u274E"); // negative squared cross mark
        emojis.put(9989, "\u2705"); // white heavy check mark
        emojis.put(10036, "\u2734"); // eight pointed black star
        emojis.put(128159, "\u1F49F"); // heart decoration
        emojis.put(127386, "\u1F19A"); // squared vs
        emojis.put(128243, "\u1F4F3"); // vibration mode
        emojis.put(128244, "\u1F4F4"); // mobile phone off
        emojis.put(127344, "\u1F170"); // negative squared latin capital letter a
        emojis.put(127345, "\u1F171"); // negative squared latin capital letter b
        emojis.put(127374, "\u1F18E"); // negative squared ab
        emojis.put(127358, "\u1F17E"); // negative squared latin capital letter o
        emojis.put(128160, "\u1F4A0"); // diamond shape with a dot inside
        emojis.put(10175, "\u27BF"); // double curly loop
        emojis.put(9851, "\u267B"); // black universal recycling symbol
        emojis.put(9800, "\u2648"); // aries
        emojis.put(9801, "\u2649"); // taurus
        emojis.put(9802, "\u264A"); // gemini
        emojis.put(9803, "\u264B"); // cancer
        emojis.put(9804, "\u264C"); // leo
        emojis.put(9805, "\u264D"); // virgo
        emojis.put(9806, "\u264E"); // libra
        emojis.put(9807, "\u264F"); // scorpius
        emojis.put(9808, "\u2650"); // sagittarius
        emojis.put(9809, "\u2651"); // capricorn
        emojis.put(9810, "\u2652"); // aquarius
        emojis.put(9811, "\u2653"); // pisces
        emojis.put(9934, "\u26CE"); // ophiuchus
        emojis.put(128303, "\u1F52F"); // six pointed star with middle dot
        emojis.put(127975, "\u1F3E7"); // automated teller machine
        emojis.put(128185, "\u1F4B9"); // chart with upwards trend and yen sign
        emojis.put(128178, "\u1F4B2"); // heavy dollar sign
        emojis.put(128177, "\u1F4B1"); // currency exchange
        emojis.put(169, "\u00A9"); // copyright sign
        emojis.put(174, "\u00AE"); // registered sign
        emojis.put(8482, "\u2122"); // trade mark sign
        emojis.put(10060, "\u274C"); // cross mark
        emojis.put(8252, "\u203C"); // double exclamation mark
        emojis.put(8265, "\u2049"); // exclamation question mark
        emojis.put(10071, "\u2757"); // heavy exclamation mark symbol
        emojis.put(10067, "\u2753"); // black question mark ornament
        emojis.put(10069, "\u2755"); // white exclamation mark ornament
        emojis.put(10068, "\u2754"); // white question mark ornament
        emojis.put(11093, "\u2B55"); // heavy large circle
        emojis.put(128285, "\u1F51D"); // top with upwards arrow above
        emojis.put(128282, "\u1F51A"); // end with leftwards arrow above
        emojis.put(128281, "\u1F519"); // back with leftwards arrow above
        emojis.put(128283, "\u1F51B"); // on with exclamation mark with left right arrow above
        emojis.put(128284, "\u1F51C"); // soon with rightwards arrow above
        emojis.put(128259, "\u1F503"); // clockwise downwards and upwards open circle arrows
        emojis.put(128347, "\u1F55B"); // clock face twelve oclock
        emojis.put(128359, "\u1F567"); // clock face twelve-thirty
        emojis.put(128336, "\u1F550"); // clock face one oclock
        emojis.put(128348, "\u1F55C"); // clock face one-thirty
        emojis.put(128337, "\u1F551"); // clock face two oclock
        emojis.put(128349, "\u1F55D"); // clock face two-thirty
        emojis.put(128338, "\u1F552"); // clock face three oclock
        emojis.put(128350, "\u1F55E"); // clock face three-thirty
        emojis.put(128339, "\u1F553"); // clock face four oclock
        emojis.put(128351, "\u1F55F"); // clock face four-thirty
        emojis.put(128340, "\u1F554"); // clock face five oclock
        emojis.put(128352, "\u1F560"); // clock face five-thirty
        emojis.put(128341, "\u1F555"); // clock face six oclock
        emojis.put(128342, "\u1F556"); // clock face seven oclock
        emojis.put(128343, "\u1F557"); // clock face eight oclock
        emojis.put(128344, "\u1F558"); // clock face nine oclock
        emojis.put(128345, "\u1F559"); // clock face ten oclock
        emojis.put(128346, "\u1F55A"); // clock face eleven oclock
        emojis.put(128353, "\u1F561"); // clock face six-thirty
        emojis.put(128354, "\u1F562"); // clock face seven-thirty
        emojis.put(128355, "\u1F563"); // clock face eight-thirty
        emojis.put(128356, "\u1F564"); // clock face nine-thirty
        emojis.put(128357, "\u1F565"); // clock face ten-thirty
        emojis.put(128358, "\u1F566"); // clock face eleven-thirty
        emojis.put(10006, "\u2716"); // heavy multiplication x
        emojis.put(10133, "\u2795"); // heavy plus sign
        emojis.put(10134, "\u2796"); // heavy minus sign
        emojis.put(10135, "\u2797"); // heavy division sign
        emojis.put(9824, "\u2660"); // black spade suit
        emojis.put(9829, "\u2665"); // black heart suit
        emojis.put(9827, "\u2663"); // black club suit
        emojis.put(9830, "\u2666"); // black diamond suit
        emojis.put(128174, "\u1F4AE"); // white flower
        emojis.put(128175, "\u1F4AF"); // hundred points symbol
        emojis.put(10004, "\u2714"); // heavy check mark
        emojis.put(9745, "\u2611"); // ballot box with check
        emojis.put(128280, "\u1F518"); // radio button
        emojis.put(128279, "\u1F517"); // link symbol
        emojis.put(10160, "\u27B0"); // curly loop
        emojis.put(12336, "\u3030"); // wavy dash
        emojis.put(12349, "\u303D"); // part alternation mark
        emojis.put(128305, "\u1F531"); // trident emblem
        emojis.put(9724, "\u25FC"); // black medium square
        emojis.put(9723, "\u25FB"); // white medium square
        emojis.put(9726, "\u25FE"); // black medium small square
        emojis.put(9725, "\u25FD"); // white medium small square
        emojis.put(9642, "\u25AA"); // black small square
        emojis.put(9643, "\u25AB"); // white small square
        emojis.put(128314, "\u1F53A"); // up-pointing red triangle
        emojis.put(128306, "\u1F532"); // black square button
        emojis.put(128307, "\u1F533"); // white square button
        emojis.put(9899, "\u26AB"); // medium black circle
        emojis.put(9898, "\u26AA"); // medium white circle
        emojis.put(128308, "\u1F534"); // large red circle
        emojis.put(128309, "\u1F535"); // large blue circle
        emojis.put(128315, "\u1F53B"); // down-pointing red triangle
        emojis.put(11036, "\u2B1C"); // white large square
        emojis.put(11035, "\u2B1B"); // black large square
        emojis.put(128310, "\u1F536"); // large orange diamond
        emojis.put(128311, "\u1F537"); // large blue diamond
        emojis.put(128312, "\u1F538"); // small orange diamond
        emojis.put(128313, "\u1F539"); // small blue diamond
        return emojis;
    }

}

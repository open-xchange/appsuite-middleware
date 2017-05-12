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

package com.openexchange.html.internal.filtering;

import static com.openexchange.java.Strings.toLowerCase;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.html.services.ServiceRegistry;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.Streams;

/**
 * {@link FilterMaps}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FilterMaps {

    private static final Set<String> NUM_ATTRIBS = ImmutableSet.copyOf(new HashSet<String>(0));

    private static final Set<String> SINGLE_TAGS = ImmutableSet.of("wbr", "time");

    private static volatile Map<String, Map<String, Set<String>>> staticHTMLMap;

    private static volatile Map<String, Set<String>> staticStyleMap;

    private static volatile Map<String, Set<String>> staticHeightWidthStyleMap;

    private static final Map<String, Set<String>> IMAGE_STYLE_MAP;

    static {
        Map<String, Set<String>> imageStyleMap = new HashMap<>();
        Set<String> values = new HashSet<>();
        /*
         * background
         */
        values.add("iNc");
        values.add("scroll");
        values.add("fixed");
        values.add("transparent");
        values.add("top");
        values.add("bottom");
        values.add("center");
        values.add("left");
        values.add("right");
        values.add("repeat");
        values.add("repeat-x");
        values.add("repeat-y");
        values.add("no-repeat");
        imageStyleMap.put("background", ImmutableSet.copyOf(values));
        values = new HashSet<>();
        /*
         * background-image
         */
        values.add("i"); // Only "cid:" URLs
        imageStyleMap.put("background-image", ImmutableSet.copyOf(values));
        IMAGE_STYLE_MAP = ImmutableMap.copyOf(imageStyleMap);
    }

    /**
     * Gets the special marker set for a numeric attribute value.
     *
     * @return The special marker set for a numeric attribute value
     */
    public static Set<String> getNumAttribs() {
        return NUM_ATTRIBS;
    }

    /**
     * Gets the static HTML map.
     *
     * @return The HTML map
     */
    public static Map<String, Map<String, Set<String>>> getStaticHTMLMap() {
        Map<String, Map<String, Set<String>>> staticHTMLMap = FilterMaps.staticHTMLMap;
        if (null == staticHTMLMap) {
            loadWhitelist();
            staticHTMLMap = FilterMaps.staticHTMLMap;
        }
        return staticHTMLMap;
    }

    /**
     * Gets the static CSS map.
     *
     * @return The CSS map
     */
    public static Map<String, Set<String>> getStaticStyleMap() {
        Map<String, Set<String>> staticStyleMap = FilterMaps.staticStyleMap;
        if (null == staticStyleMap) {
            loadWhitelist();
            staticStyleMap = FilterMaps.staticStyleMap;
        }
        return staticStyleMap;
    }

    /**
     * Gets the image CSS map.
     *
     * @return The image CSS map
     */
    public static Map<String, Set<String>> getImageStyleMap() {
        return IMAGE_STYLE_MAP;
    }

    /**
     * Gets the height/width map.
     *
     * @return The height/width map
     */
    public static Map<String, Set<String>> getHeightWidthStyleMap() {
        Map<String, Set<String>> staticHeightWidthStyleMap = FilterMaps.staticHeightWidthStyleMap;
        if (null == staticHeightWidthStyleMap) {
            loadWhitelist();
            staticHeightWidthStyleMap = FilterMaps.staticHeightWidthStyleMap;
        }
        return staticHeightWidthStyleMap;
    }

    /**
     * Gets the set of extra single tags.
     *
     * @return The set of extra single tag
     */
    public static Set<String> getSingleTags() {
        return SINGLE_TAGS;
    }

    private static final byte[] DEFAULT_WHITELIST = String
        .valueOf(
            "# HTML tags and attributes\n"
                + "\n"
                + "html.tag.a=\",href,name,tabindex,target,type,\"\n"
                + "html.tag.area=\",alt,coords,href,nohref[nohref],shape[:rect:circle:poly:default:],tabindex,target,\"\n"
                + "html.tag.b=\"\"\n"
                + "html.tag.basefont=\",color,face,size,\"\n"
                + "html.tag.bdo=\",dir[:ltr:rtl:]\"\n"
                + "html.tag.blockquote=\",type,\"\n"
                + "html.tag.body=\",alink,background,bgcolor,link,text,vlink,\"\n"
                + "html.tag.br=\",clear[:left:right:all:none:]\"\n"
                + "html.tag.button=\",disabled[disabled],name,tabindex,type[:button:submit:reset:],value,\"\n"
                + "html.tag.caption=\",align[:top:bottom:left:right:]\"\n"
                + "html.tag.col=\",align[:left:center:right:justify:char:],char,charoff,span[],valign[:top:middle:bottom:baseline:],width,\"\n"
                + "html.tag.colgroup=\",align[:left:center:right:justify:char:],char,charoff,span[],valign[:top:middle:bottom:baseline:],width,\"\n"
                + "html.tag.del=\",datetime,\"\n"
                + "html.tag.dir=\",compact[compact]\"\n"
                + "html.tag.div=\",align[:left:center:right:justify:]\"\n"
                + "html.tag.dl=\",compact[compact]\"\n"
                + "html.tag.em=\"\"\n"
                + "html.tag.font=\",color,face,size,\"\n"
                + "html.tag.h1=\",align[:left:center:right:justify:]\"\n"
                + "html.tag.h2=\",align[:left:center:right:justify:]\"\n"
                + "html.tag.h3=\",align[:left:center:right:justify:]\"\n"
                + "html.tag.h4=\",align[:left:center:right:justify:]\"\n"
                + "html.tag.h5=\",align[:left:center:right:justify:]\"\n"
                + "html.tag.h6=\",align[:left:center:right:justify:]\"\n"
                + "html.tag.hr=\",align[:left:center:right:],noshade[noshade],size,width,\"\n"
                + "html.tag.html=\",version,xmlns,\"\n"
                + "html.tag.img=\",align[:top:middle:bottom:left:right:],alt,border,height,hspace,ismap[ismap],name,src,usemap,vspace,width,\"\n"
                + "html.tag.ins=\",datetime,\"\n"
                + "html.tag.label=\",for,\"\n"
                + "html.tag.legend=\",align[:left:top:right:bottom:]\"\n"
                + "html.tag.li=\",type[:disc:square:circle:1:a:A:i:I:],value[],\"\n"
                + "html.tag.map=\",name,\"\n"
                + "html.tag.meta=\",http-equiv[:content-type:],\"\n"
                + "html.tag.ol=\",compact[compact],start[],type[:1:a:A:i:I:],\"\n"
                + "html.tag.optgroup=\",disabled[disabled],label,\"\n"
                + "html.tag.option=\",disabled[disabled],label,selected[selected],value,\"\n"
                + "html.tag.p=\",align[:left:center:right:justify:]\"\n"
                + "html.tag.pre=\",width[],\"\n"
                + "html.tag.select=\",disabled[disabled],multiple[multiple],name,size,tabindex[],\"\n"
                + "html.tag.span=\"\"\n"
                + "html.tag.strong=\"\"\n"
                + "html.tag.style=\",media,type,\"\n"
                + "html.tag.table=\",align[:left:center:right:],background,border,bgcolor,cellpadding,cellspacing,frame[:void:above:below:hsides:ihs:rhs:vsides:box:border:],rules[:none:groups:rows:cols:all:],summary,width,\"\n"
                + "html.tag.tbody=\",align[:left:center:right:justify:char:],char,charoff,valign[:top:middle:bottom:baseline:],\"\n"
                + "html.tag.td=\",abbr,align[:left:center:right:justify:char:],axis,background,bgcolor,char,charoff,colspan[],headers,height,nowrap[nowrap],rowspan[],scope[:row:col:rowgroup:colgroup:],valign[:top:middle:bottom:baseline:],width,\"\n"
                + "html.tag.textarea=\",cols[],disabled[disabled],name,readonly[readonly],rows[],tabindex[],\"\n"
                + "html.tag.tfoot=\",align[:left:center:right:justify:char:],char,charoff,valign[:top:middle:bottom:baseline:],\"\n"
                + "html.tag.th=\",abbr,align[:left:center:right:justify:char:],axis,bgcolor,char,charoff,colspan[],headers,height,nowrap[nowrap],rowspan[],scope[:row:col:rowgroup:colgroup:],valign[:top:middle:bottom:baseline:],width,\"\n"
                + "html.tag.thead=\",align[:left:center:right:justify:char:],char,charoff,valign[:top:middle:bottom:baseline:],\"\n"
                + "html.tag.tr=\",align[:left:center:right:justify:char:],bgcolor,char,charoff,valign[:top:middle:bottom:baseline:],height,\"\n"
                + "html.tag.u=\"\"\n"
                + "html.tag.ul=\",compact[compact],type[:disc:square:circle:],\"\n"
                + "\n"
                + "\n"
                + "# CSS key-value-pairs.\n"
                + "# An empty value indicates a reference to style's combi-map.\n"
                + "# Placeholders:\n"
                + "# c: Any CSS color value\n"
                + "# u: An URL; e.g. url(http://www.somewhere.com/myimage.jpg);\n"
                + "# n: Any CSS number value without '%'\n"
                + "# N: Any CSS number value\n"
                + "# *: Any value allowed\n"
                + "# d: delete\n"
                + "# t: time\n"
                + "\n"
                + "html.style.azimuth=\",left-side,left-side behind,far-left,far-left behind,left,left behind,center-left,center-left behind,center,center behind,center-right,center-right behind,right,right behind,far-right,far-right behind,right-side,right behind,\"\n"
                + "html.style.background=\"\"\n"
                + "html.style.background-attachment=\",scroll,fixed,\"\n"
                + "html.style.background-color=\"c,transparent,\"\n"
                + "html.style.background-image=\"u\"\n"
                + "html.style.background-position=\",N,top,bottom,center,left,right,\"\n"
                + "html.style.background-repeat=\",repeat,repeat-x,repeat-y,no-repeat,\"\n"
                + "html.style.border=\"\"\n"
                + "html.style.border-bottom=\"\"\n"
                + "html.style.border-bottom-color=\"c,transparent,\"\n"
                + "html.style.border-bottom-style=\",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.border-bottom-width=\"n\"\n"
                + "html.style.border-collapse=\",separate,collapse,\"\n"
                + "html.style.border-color=\"c,transparent,\"\n"
                + "html.style.border-left=\"\"\n"
                + "html.style.border-left-color=\"c,transparent,\"\n"
                + "html.style.border-left-style=\",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.border-left-width=\"n\"\n"
                + "html.style.border-right=\"\"\n"
                + "html.style.border-right-color=\"c,transparent,\"\n"
                + "html.style.border-right-style=\",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.border-right-width=\"n\"\n"
                + "html.style.border-spacing=\"N\"\n"
                + "html.style.border-style=\"\"\n"
                + "html.style.border-top=\"\"\n"
                + "html.style.border-top-color=\"c,transparent,\"\n"
                + "html.style.border-top-style=\",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.border-top-width=\"n\"\n"
                + "html.style.border-width=\"\"\n"
                + "html.style.bottom=\"N,auto,\"\n"
                + "html.style.caption-side=\",top,bottom,left,right,\"\n"
                + "html.style.centerline=\"d\"\n"
                + "html.style.clear=\",left,right,both,none,\"\n"
                + "html.style.clip=\"d\"\n"
                + "html.style.color=\"c,transparent,\"\n"
                + "html.style.content=\"d\"\n"
                + "html.style.counter-increment=\"d\"\n"
                + "html.style.counter-reset=\"d\"\n"
                + "html.style.counter=\"d\"\n"
                + "html.style.cue=\"u\"\n"
                + "html.style.cue-after=\"u\"\n"
                + "html.style.cue-before=\"u\"\n"
                + "html.style.cursor=\",auto,default,crosshair,pointer,move,n-resize,ne-resize,e-resize,se-resize,s-resize,sw-resize,w-resize,nw-resize,text,wait,help,\"\n"
                + "html.style.definition-src=\"d\"\n"
                + "html.style.direction=\",ltr,rtl,\"\n"
                + "html.style.display=\",block,inline,list-item,marker,run-in,compact,none,table,inline-table,table-row,table-cell,table-row-group,table-header-group,table-footer-group,table-column,table-column-group,table-caption,\"\n"
                + "html.style.empty-cells=\",show,hide,\"\n"
                + "html.style.elevation=\",below,level,above,higher,lower,\"\n"
                + "html.style.filter=\"d\" \n"
                + "html.style.float=\",left,right,none,\"\n"
                + "html.style.font=\"\"\n"
                + "html.style.font-family=\"*\"\n"
                + "html.style.font-color=\"c,transparent,\"\n"
                + "html.style.font-size=\"N,xx-small,x-small,small,medium,large,x-large,xx-large,smaller,larger,\"\n"
                + "html.style.font-stretch=\",wider,narrower,condensed,semi-condensed,extra-condensed,ultra-condensed,expanded,semi-expanded,extra-expanded,ultra-expanded,normal,\"\n"
                + "html.style.font-style=\",italic,oblique,normal,\"\n"
                + "html.style.font-variant=\",small-caps,normal,\"\n"
                + "html.style.font-weight=\",bold,bolder,lighter,100,200,300,400,500,600,700,800,900,normal,\"\n"
                + "html.style.height=\"N,auto,\"\n"
                + "html.style.left=\"N,auto,\"\n"
                + "html.style.letter-spacing=\"n\"\n"
                + "html.style.line-height=\"N\"\n"
                + "html.style.list-style=\"\"    \n"
                + "html.style.list-style-image=\"u,none,\"\n"
                + "html.style.list-style-position=\",inside,outside,\"\n"
                + "html.style.list-style-type=\",decimal,lower-roman,upper-roman,lower-alpha,lower-latin,upper-alpha,upper-latin,disc,circle,square,none,lower-greek,hebrew,decimal-leading-zero,cjk-ideographic,hiragana,katakana,hiragana-iroha,katakana-iroha,\"\n"
                + "html.style.margin=\"\"\n"
                + "html.style.margin-bottom=\"N,auto,inherit,\"\n"
                + "html.style.margin-left=\"N,auto,inherit,\"\n"
                + "html.style.margin-right=\"N,auto,inherit,\"\n"
                + "html.style.margin-top=\"N,auto,inherit,\"\n"
                + "html.style.max-height=\"N\"\n"
                + "html.style.max-width=\"N\"\n"
                + "html.style.min-height=\"N\"\n"
                + "html.style.min-width=\"N\"\n"
                + "html.style.orphans=\"0\"\n"
                + "html.style.overflow=\",visible,hidden,scroll,auto,\"\n"
                + "html.style.padding=\"\"\n"
                + "html.style.padding-bottom=\"N\"\n"
                + "html.style.padding-left=\"N\"\n"
                + "html.style.padding-right=\"N\"\n"
                + "html.style.padding-top=\"N\"\n"
                + "html.style.page-break-after=\",always,avoid,left,right,inherit,auto,\"\n"
                + "html.style.page-break-before=\",always,avoid,left,right,inherit,auto,\"\n"
                + "html.style.page-break-inside=\",avoid,auto,\"\n"
                + "html.style.pause=\"t\"\n"
                + "html.style.pause-after=\"t\"\n"
                + "html.style.pause-before=\"t\"\n"
                + "html.style.pitch=\",x-low,low,medium,high,x-high,\"\n"
                + "html.style.pitch-range=\"0\"\n"
                + "html.style.play-during=\"u,mix,repeat,auto,\"\n"
                + "html.style.position=\",absolute,fixed,relative,static,\"\n"
                + "html.style.quotes=\"d\"\n"
                + "html.style.richness=\"0\"\n"
                + "html.style.right=\"N,auto,\"\n"
                + "html.style.scrollbar-3dlight-color=\"c\"\n"
                + "html.style.scrollbar-arrow-color=\"c\"\n"
                + "html.style.scrollbar-base-color=\"c\"\n"
                + "html.style.scrollbar-darkshadow-color=\"c\"\n"
                + "html.style.scrollbar-face-color=\"c\"\n"
                + "html.style.scrollbar-highlight-color=\"c\"\n"
                + "html.style.scrollbar-shadow-color=\"c\"\n"
                + "html.style.scrollbar-track-color=\"c\"\n"
                + "html.style.speak=\",none,normal,spell-out,\"\n"
                + "html.style.speak-header=\",always,once,\"\n"
                + "html.style.speak-numeral=\",digits,continuous,\"\n"
                + "html.style.speak-punctuation=\",code,none,\"\n"
                + "html.style.speech-rate=\"0,x-slow,slow,slower,medium,faster,fast,x-fase,\"\n"
                + "html.style.stress=\"0\"\n"
                + "html.style.table-layout=\",auto,fixed,\"\n"
                + "html.style.text-align=\",left,center,right,justify,\"\n"
                + "html.style.text-decoration=\",underline,overline,line-through,blink,none,\"\n"
                + "html.style.text-indent=\"N\"\n"
                + "html.style.text-shadow=\"nc,none,\" or color\n"
                + "html.style.text-transform=\",capitalize,uppercase,lowercase,none,\"\n"
                + "html.style.top=\"N,auto,\"\n"
                + "html.style.vertical-align=\",top,middle,bottom,baseline,sub,super,text-top,text-bottom,\"\n"
                + "html.style.visibility=\",hidden,visible,\"\n"
                + "html.style.voice-family=\",male,female,old,young,child,\"\n"
                + "html.style.volume=\"0,silent,x-soft,soft,medium,loud,x-loud,\"\n"
                + "html.style.white-space=\",normal,pre,nowrap,\"\n"
                + "html.style.widows=\"0\"\n"
                + "html.style.width=\"N,auto,\"\n"
                + "html.style.word-spacing=\"n\"\n"
                + "html.style.z-index=\"0\"\n"
                + "\n"
                + "\n"
                + "# CSS combi-map\n"
                + "\n"
                + "html.style.combimap.background=\"uNc,scroll,fixed,transparent,top,bottom,center,left,right,repeat,repeat-x,repeat-y,no-repeat,radial-gradient,\"\n"
                + "html.style.combimap.border=\"Nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,separate,collapse,\"\n"
                + "html.style.combimap.border-bottom=\"nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.combimap.border-left=\"nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.combimap.border-right=\"nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.combimap.border-style=\",none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.combimap.border-top=\"nc,transparent,none,hidden,dotted,dashed,solid,double,groove,ridge,inset,outset,\"\n"
                + "html.style.combimap.border-width=\"n\"\n"
                + "html.style.combimap.font=\"N*,xx-small,x-small,small,medium,large,x-large,xx-large,smaller,larger,wider,narrower,condensed,semi-condensed,extra-condensed,ultra-condensed,expanded,semi-expanded,extra-expanded,ultra-expanded,normal,italic,oblique,small-caps,bold,bolder,lighter,100,200,300,400,500,600,700,800,900,\"\n"
                + "html.style.combimap.list-style=\"u,none,inside,outside,decimal,lower-roman,upper-roman,lower-alpha,lower-latin,upper-alpha,upper-latin,disc,circle,square,lower-greek,hebrew,decimal-leading-zero,cjk-ideographic,hiragana,katakana,hiragana-iroha,katakana-iroha,\"\n"
                + "html.style.combimap.margin=\"N,auto,inherit,\"\n" + "html.style.combimap.padding=\"N\"\n").getBytes();

    /**
     * Parses specified HTML map.
     *
     * @param htmlMapStr The HTML map string
     * @return The parsed map
     */
    public static Map<String, Map<String, Set<String>>> parseHTMLMap(final String htmlMapStr) {
        Pattern pattern = Pattern.compile("html\\.tag\\.(\\p{Alnum}+)\\s*(?:=\\s*\"(\\p{Print}*)\")?", Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(htmlMapStr);

        Map<String, Map<String, Set<String>>> tagMap = new HashMap<>();
        Pattern attributePattern = Pattern.compile("([\\p{Alnum}-_]+)(?:\\[([\\p{Print}&&[^\\]]]*)\\])?");
        while (m.find()) {
            String attributes = m.group(2);
            String tagName = toLowerCase(m.group(1));
            if (null == attributes) {
                tagMap.put(tagName, null);
            } else {
                Matcher attribMatcher = attributePattern.matcher(attributes);
                Map<String, Set<String>> attribMap = new HashMap<>();
                while (attribMatcher.find()) {
                    String values = attribMatcher.group(2);
                    String attributeName = toLowerCase(attribMatcher.group(1));
                    if (null == values) {
                        attribMap.put(attributeName, null);
                    } else if (values.length() == 0) {
                        attribMap.put(attributeName, NUM_ATTRIBS);
                    } else {
                        Set<String> valueSet = new HashSet<>();
                        String[] valArr = values.charAt(0) == ':' ? values.substring(1).split("\\s*:\\s*") : values.split("\\s*:\\s*");
                        for (String value : valArr) {
                            valueSet.add(toLowerCase(value));
                        }
                        attribMap.put(attributeName, valueSet);
                    }
                }
                tagMap.put(tagName, attribMap);
            }
        }
        return tagMap;
    }

    /**
     * Parses specified style map
     *
     * @param styleMapStr The style map string
     * @return The parsed map
     */
    public static Map<String, Set<String>> parseStyleMap(final String styleMapStr) {
        Pattern valuePattern = Pattern.compile("([\\p{Alnum}*-_ &&[^,]]+)");

        // Parse the combination map
        Map<String, Set<String>> combiMap = parseCombiMap(styleMapStr, valuePattern);

        // Parse style map
        Pattern pattern = Pattern.compile("html\\.style\\.([\\p{Alnum}-_]+)\\s*=\\s*\"([\\p{Print}&&[^\"]]*)\"", Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(styleMapStr);
        Map<String, Set<String>> styleMap = new HashMap<>();
        while (m.find()) {
            final String values = m.group(2);
            if (values.length() == 0) {
                /*
                 * Fetch from combination map
                 */
                final String cssElement = toLowerCase(m.group(1));
                styleMap.put(cssElement, combiMap.get(cssElement));
            } else {
                /*
                 * Parse values
                 */
                final Matcher valueMatcher = valuePattern.matcher(m.group(2));
                final Set<String> valueSet = new HashSet<>();
                while (valueMatcher.find()) {
                    valueSet.add(valueMatcher.group());
                }
                styleMap.put(toLowerCase(m.group(1)), valueSet);
            }
        }
        return styleMap;
    }

    /**
     * Parses specified combination map for CSS elements.
     *
     * @param combiMapStr The string representation for combination map
     * @return The parsed map
     */
    private static Map<String, Set<String>> parseCombiMap(String combiMapStr, Pattern valuePattern) {
        Pattern pattern = Pattern.compile("html\\.style\\.combimap\\.([\\p{Alnum}-_]+)\\s*=\\s*\"([\\p{Print}&&[^\"]]+)\"", Pattern.CASE_INSENSITIVE);
        Matcher m = pattern.matcher(combiMapStr);

        Map<String, Set<String>> combiMap = new HashMap<>();
        while (m.find()) {
            Matcher valueMatcher = valuePattern.matcher(m.group(2));
            Set<String> valueSet = new HashSet<>();
            while (valueMatcher.find()) {
                valueSet.add(valueMatcher.group());
            }
            combiMap.put(toLowerCase(m.group(1)), valueSet);
        }
        return combiMap;
    }

    /**
     * Loads the white list.
     */
    public static synchronized void loadWhitelist() {
        final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FilterMaps.class);
        if (null == staticHTMLMap) {
            String mapStr;
            {
                ConfigurationService service = ServiceRegistry.getInstance().getService(ConfigurationService.class);
                File whitelist = null == service ? null : service.getFileByName("whitelist.properties");
                if (null == whitelist) {
                    LOG.warn("Using default white list");
                    mapStr = new String(DEFAULT_WHITELIST);
                } else {
                    BufferedReader reader = null;
                    try {
                        reader = new BufferedReader(new AsciiReader(new FileInputStream(whitelist)));
                        StringBuilder sb = new StringBuilder((int) whitelist.length());
                        for (String line; (line = reader.readLine()) != null;) {
                            if (line.length() > 0 && '#' != line.charAt(0)) {
                                // No comment line
                                sb.append(line).append("\r\n");
                            }
                        }
                        mapStr = sb.toString();
                    } catch (final Exception e) {
                        LOG.warn("Using default white list", e);
                        mapStr = new String(DEFAULT_WHITELIST);
                    } finally {
                        Streams.close(reader);
                    }
                }
            }

            Map<String, Map<String, Set<String>>> map = parseHTMLMap(mapStr);
            if (!map.containsKey("html")) {
                map.put("html", null);
            }
            if (!map.containsKey("head")) {
                map.put("head", null);
            }
            if (!map.containsKey("body")) {
                map.put("body", null);
            }
            for (final String tagName : SINGLE_TAGS) {
                if (!map.containsKey(tagName)) {
                    map.put(tagName, null);
                }
            }
            staticHTMLMap = Collections.unmodifiableMap(map);
            staticStyleMap = Collections.unmodifiableMap(parseStyleMap(mapStr));
            String heightWidthStyle = "html.style.height=\"N,auto,\"\n" + "html.style.width=\"N,auto,\"\n";
            staticHeightWidthStyleMap = Collections.unmodifiableMap(parseStyleMap(heightWidthStyle));
        }
    }

    /**
     * Resets the white list.
     */
    public static synchronized void resetWhitelist() {
        staticHTMLMap = null;
        staticStyleMap = null;
        staticHeightWidthStyleMap = null;
    }

}

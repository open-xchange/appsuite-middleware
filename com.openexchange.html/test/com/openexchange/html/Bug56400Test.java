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

package com.openexchange.html;

import static org.junit.Assert.assertTrue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import com.openexchange.html.HtmlSanitizeOptions.ParserPreference;

/**
 * {@link Bug56400Test}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Bug56400Test extends AbstractSanitizing {

    public Bug56400Test() {
        super();
    }

    @Test
    public void testIntactLink() throws Exception {
        String content = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
            "  <head>\n" +
            "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
            "    <meta content=\"telephone=no\" name=\"format-detection\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
            "    <!--[if !mso]><!-->\n" +
            "    <link href=\"https://fonts.googleapis.com/css?family=Open+Sans:300,300i,400,400i,600,600i,700,700i,800\" rel=\"stylesheet\">\n" +
            "    <!--<![endif]-->\n" +
            "    <title>Verify Email</title>\n" +
            "    <style type=\"text/css\">\n" +
            "      body {\n" +
            "      -webkit-text-size-adjust: 100% !important;\n" +
            "      -ms-text-size-adjust: 100% !important;\n" +
            "      -webkit-font-smoothing: antialiased !important;\n" +
            "      }\n" +
            "      img {\n" +
            "      border: 0 !important;\n" +
            "      outline: none !important;\n" +
            "      }\n" +
            "      table {\n" +
            "      border-collapse: collapse;\n" +
            "      mso-table-lspace: 0px;\n" +
            "      mso-table-rspace: 0px;\n" +
            "      }\n" +
            "      p {\n" +
            "      Margin: 0px !important;\n" +
            "      Padding: 0px !important;\n" +
            "      }\n" +
            "      td, a, span {\n" +
            "      border-collapse: collapse;\n" +
            "      mso-line-height-rule: exactly;\n" +
            "      }\n" +
            "      .ExternalClass * {\n" +
            "      line-height: 100%;\n" +
            "      }\n" +
            "      span.MsoHyperlink {\n" +
            "      mso-style-priority: 99;\n" +
            "      color: inherit;\n" +
            "      }\n" +
            "      span.MsoHyperlinkFollowed {\n" +
            "      mso-style-priority: 99;\n" +
            "      color: inherit;\n" +
            "      }\n" +
            "      .em_preheader {\n" +
            "      font-family: 'Open Sans', Arial, sans-serif;\n" +
            "      font-size: 10px;\n" +
            "      line-height: 13px;\n" +
            "      color: #787878;\n" +
            "      text-decoration: none;\n" +
            "      text-align: center;\n" +
            "      }\n" +
            "      .em_preheader a {\n" +
            "      font-family: 'Open Sans', Arial, sans-serif;\n" +
            "      font-size: 10px;\n" +
            "      line-height: 13px;\n" +
            "      color: #3575d3;\n" +
            "      text-decoration: underline;\n" +
            "      }\n" +
            "      .em_btn, .em_btn a {\n" +
            "      font-family: 'Open Sans', Arial, sans-serif;\n" +
            "      font-size: 20px;\n" +
            "      color: #ffffff;\n" +
            "      text-decoration: none;\n" +
            "      text-align: center;\n" +
            "      font-weight: normal;\n" +
            "      }\n" +
            "      .em_greyfont, .em_greyfont a {\n" +
            "      font-family: 'Open Sans', Arial, sans-serif;\n" +
            "      font-size: 14px;\n" +
            "      line-height: 18px;\n" +
            "      color: #5f6165;\n" +
            "      text-decoration: none;\n" +
            "      }\n" +
            "      .connect {\n" +
            "      border-collapse: collapse;\n" +
            "      mso-line-height-rule: exactly;\n" +
            "      color: #ffffff;\n" +
            "      font-family: \"Open Sans\", Arial, sans-serif;\n" +
            "      font-size: 26px;\n" +
            "      line-height: 28px;\n" +
            "      text-align: left;\n" +
            "      }\n" +
            "      .em_link {\n" +
            "      border-collapse: collapse;\n" +
            "      mso-line-height-rule: exactly;\n" +
            "      font-family: \"Open Sans\", Arial, sans-serif;\n" +
            "      font-size: 14px;\n" +
            "      line-height: 18px;\n" +
            "      color: #3575d3 !important;\n" +
            "      text-decoration: underline !important;\n" +
            "      white-space: nowrap !important;\n" +
            "      word-break: keep-all !important;\n" +
            "      }\n" +
            "      .em_greyfont2, .em_greyfont2 a {\n" +
            "      font-family: 'Open Sans', Arial, sans-serif;\n" +
            "      font-size: 12px;\n" +
            "      line-height: 14px;\n" +
            "      color: #777a7f;\n" +
            "      text-decoration: none;\n" +
            "      }\n" +
            "      .em_lightbluefont, .em_lightbluefont a {\n" +
            "      font-family: 'Open Sans', Arial, sans-serif;\n" +
            "      font-size: 16px;\n" +
            "      line-height: 22px;\n" +
            "      color: #3575d3;\n" +
            "      text-decoration: none;\n" +
            "      }\n" +
            "        .em_grey a{\n" +
            "            color: #555555;\n" +
            "            text-decoration: none;\n" +
            "            \n" +
            "        }\n" +
            "      \n" +
            "        .em_blue3 {\n" +
            "            font-family: 'Open Sans', Arial, sans-serif;\n" +
            "            font-size: 24px;\n" +
            "            line-height: 26px;\n" +
            "            color: #3575d3;\n" +
            "            text-decoration: none;\n" +
            "        }\n" +
            "        .em_blue3 a {\n" +
            "            font-family: 'Open Sans', Arial, sans-serif;\n" +
            "            font-size: 24px;\n" +
            "            line-height: 26px;\n" +
            "            color: #3575d3;\n" +
            "            text-decoration: none;\n" +
            "        }\n" +
            "      .em_tracking, .em_tracking a {\n" +
            "      color: #3575d3 !important;\n" +
            "      text-decoration: underline;\n" +
            "      white-space: nowrap !important;\n" +
            "      word-break: keep-all !important;\n" +
            "      }\n" +
            "     @media only screen and (max-width:600px) {\n" +
            "      .em_wrapper {\n" +
            "      width: 100% !important;\n" +
            "      }\n" +
            "      .em_main_table {\n" +
            "      width: 100% !important;\n" +
            "      }\n" +
            "      .em_hide {\n" +
            "      display: none !important;\n" +
            "      }\n" +
            "      .em_spacer {\n" +
            "      width: 10px !important;\n" +
            "      }\n" +
            "      .em_height {\n" +
            "      height: 20px !important;\n" +
            "      }\n" +
            "      .em_btn {\n" +
            "      font-size: 18px !important;\n" +
            "      }\n" +
            "          .em_cent {\n" +
            "        text-align: center !important;\n" +
            "        \n" +
            "    }\n" +
            "      }\n" +
            "    </style>\n" +
            "  </head>\n" +
            "    <body style=\"margin: 0px;padding: 0px;-webkit-text-size-adjust: 100% !important;-ms-text-size-adjust: 100% !important;-webkit-font-smoothing: antialiased !important;\" bgcolor=\"#f1f3f4\">\n" +
            "    <table width=\"100%\" border=\"0\" align=\"center\" cellspacing=\"0\" cellpadding=\"0\" bgcolor=\"#f1f3f4\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "      <!-- Emailer Starts Here-->\n" +
            "      <tr>\n" +
            "        <td align=\"center\" valign=\"top\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">\n" +
            "          <table style=\"table-layout: fixed;border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\" width=\"600\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" class=\"em_main_table\">\n" +
            "            <tr>\n" +
            "              <td class=\"em_hide\" style=\"line-height:1px; font-size:1px;\" width=\"600\"><img src=\"http://image.e.hostingmessages.com/lib/fe9212727362027d76/m/1/mojo-spacer.gif\" height=\"1\"  width=\"600\" alt=\"\" style=\"max-height:1px; min-height:1px; display:block; width:600px; min-width:600px;\" border=\"0\" /></td>\n" +
            "            </tr>\n" +
            "            <!--Header-->\n" +
            "            <tr>\n" +
            "              <td align=\"center\" valign=\"top\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">\n" +
            "                <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                  <tbody>\n" +
            "                    <tr>\n" +
            "                      <td align=\"center\" valign=\"top\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">\n" +
            "                        <table width=\"100%\" align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                          <tr>\n" +
            "                            <td width=\"10\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                            <td align=\"center\" valign=\"top\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">\n" +
            "                              <table width=\"100%\" align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                                <tr>\n" +
            "                                  <td height=\"20\" style=\"font-size: 1px;line-height: 1px;border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                                </tr>\n" +
            "                                <tr>\n" +
            "                                  <td align=\"center\" class=\"em_preheader\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;font-family: \"Open Sans\", Arial, sans-serif;font-size: 10px;line-height: 13px;color: #787878;text-decoration: none;text-align: center;\">Please verify your contact information&nbsp; <a href=\"\" target=\"_blank\" class=\"em_preheader\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;font-family: \"Open Sans\", Arial, sans-serif;font-size: 10px;line-height: 13px;color: #3575d3;text-decoration: underline;text-align: center; white-space: nowrap;\"></a></td>\n" +
            "                                </tr>\n" +
            "                                <tr>\n" +
            "                                  <td height=\"10\" class=\"em_height\" style=\"font-size: 1px;line-height: 1px;border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                                </tr>\n" +
            "                              </table>\n" +
            "                            </td>\n" +
            "                            <td width=\"10\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                          </tr>\n" +
            "                        </table>\n" +
            "                      </td>\n" +
            "                    </tr>\n" +
            "                  </tbody>\n" +
            "                </table>\n" +
            "              </td>\n" +
            "            </tr>\n" +
            "            <!--//Header--> \n" +
            "            <!--Content-->\n" +
            "            <tr>\n" +
            "              <td align=\"center\" valign=\"top\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">\n" +
            "                <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                  <tbody>\n" +
            "                    <tr>\n" +
            "                      <td bgcolor=\"#3575d3\" valign=\"top\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">\n" +
            "                        <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                          <tr>\n" +
            "                            <td class=\"full_width_image\" style=\"line-height: 0px;font-size: 0px;border-collapse: collapse;mso-line-height-rule: exactly;\"><img src=\"https://bluehost-cdn.com/media/shared/marketing/domains/_bh/20170101_BH_E1_upper_image.jpg\" width=\"600\" style=\"display: block;width: 100%;max-width: 600px;height: auto;border: 0 !important;outline: none !important;\" border=\"0\"></td>\n" +
            "                          </tr>\n" +
            "                          <tr>\n" +
            "                            <td height=\"20\" style=\"line-height: 1px;font-size: 1px;border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                          </tr>\n" +
            "                          <tr>\n" +
            "                            <td style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">\n" +
            "                              <table width=\"143\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"left\" class=\"em_wrapper\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                                        <tbody>\n" +
            "                                          <tr>\n" +
            "                                            <td style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">\n" +
            "                                              <table border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                                                <tr>\n" +
            "                                                  <td width=\"15\" class=\"em_hide\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"></td>\n" +
            "                                                  <td align=\"left\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"><a href=\"http://www.bluehost.com?utm_source=ebemail&utm_medium=email&utm_content=%%jobid%%_1.0%20Verify%20Domain&utm_campaign=On-boarding%20Series_v2&campaign_genre=Upsell\" target=\"_blank\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"><img src=\"https://bluehost-cdn.com/media/shared/marketing/domains/_bh/20170101_BH_E1_logo.png\" alt=\"bluehost\" width=\"128\" height=\"21\" style=\"display: block;max-width: 128px;border: 0 !important;outline: none !important;\" border=\"0\"></a></td>\n" +
            "                                                  \n" +
            "                                                </tr>\n" +
            "                                              </table>\n" +
            "                                            </td>\n" +
            "                                          </tr>\n" +
            "                                        </tbody>\n" +
            "                                      </table>\n" +
            "                            </td>\n" +
            "                          </tr>\n" +
            "                            <tr>\n" +
            "                            <td height=\"16\" style=\"line-height: 1px;font-size: 1px;border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                          </tr>\n" +
            "                        </table>\n" +
            "                      </td>\n" +
            "                    </tr>\n" +
            "                   \n" +
            "                    <tr>\n" +
            "                      <td valign=\"top\" align=\"center\" style=\"background-color: #ffffff;border-collapse: collapse;mso-line-height-rule: exactly;\">\n" +
            "                        <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                          <tbody>\n" +
            "                            <tr>\n" +
            "                              <td width=\"40\" class=\"em_spacer\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                              <td valign=\"top\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">\n" +
            "                                <table width=\"100%\" align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                                  <tbody>\n" +
            "                                   <tr>\n" +
            "                                       <td valign=\"top\"><table align=\"center\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
            "  <tbody>\n" +
            "    <tr>\n" +
            "      <td width=\"10\" class=\"em_hide\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "      <td valign=\"top\"><table align=\"center\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
            "  <tbody>\n" +
            "    <tr>\n" +
            "                                      <td height=\"38\" class=\"em_height\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                                    </tr>\n" +
            "                                      <tr>\n" +
            "                                          <td align=\"left\" valign=\"middle\" class=\"em_blue3\" style=\"border-collapse: collapse;mso-line-height-rule: exactly; font-family: \"Open Sans\", Arial, sans-serif;font-size: 24px;line-height: 26px;color: #3575d3;text-decoration: none;\">One Last Step to Activate Your Domain</td>\n" +
            "                                      </tr>\n" +
            "                                      <tr>\n" +
            "                                      <td height=\"30\" class=\"em_height\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                                    </tr>\n" +
            "                                    <tr>\n" +
            "                                      <td align=\"left\" valign=\"top\" class=\"em_greyfont\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;font-family: \"Open Sans\", Arial, sans-serif;font-size: 14px;line-height: 18px;color: #5f6165;text-decoration: none;\">Heather,<br />\n" +
            "<br />Congratulations! Your new domain <strong>onthetinkatrail.com</strong> has been registered with Bluehost. Now all you have to do is <a href=\"https://my.bluehost.com/cgi/dm/whois_validate/852519/1511764350_0WQUhsYxbw1/accept\" target=\"_blank\" class=\"em_tracking em_link\">verify your email address</a> and your domain will be ready to go.\n" +
            "\n" +
            "                                      </td>\n" +
            "                                    </tr>\n" +
            "                                    <tr>\n" +
            "                                      <td height=\"35\" class=\"em_height\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"></td>\n" +
            "                                    </tr>\n" +
            "                                    <tr>\n" +
            "                                        <td valign=\"top\" align=\"center\">\n" +
            "                                        <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "  <tbody>\n" +
            "    <tr>\n" +
            "      <td style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                                      <td align=\"center\" width=\"218\" valign=\"top\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">\n" +
            "                                        <table width=\"218\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\">\n" +
            "                                          <tbody>\n" +
            "                                            <tr>\n" +
            "                                              <td align=\"center\" valign=\"top\" background=\"https://bluehost-cdn.com/media/shared/marketing/domains/_bh/20170101_BH_E1_btn_bg2.jpg\" height=\"44\" style=\"background-position:center top;background-repeat:no-repeat;\"><table width=\"218\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                                                                                  <tr>\n" +
            "                                                                                    <td style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">\n" +
            "                                                                                      <table width=\"218\" style=\"max-width: 218px;border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\">\n" +
            "                                                                                        <tr>\n" +
            "                                                                                          <td width=\"4\" style=\"line-height: 0px;font-size: 0px;border-collapse: collapse;mso-line-height-rule: exactly;width:4px;height:4px;\"><img src=\"https://bluehost-cdn.com/media/shared/marketing/domains/_bh/20170101_BH_E1_curve_5.png\" width=\"4\" height=\"4\" style=\"display: block;border: 0 !important;outline: none !important;\" border=\"0\"></td>\n" +
            "                                                                                          <td width=\"210\" bgcolor=\"#06ba00\" height=\"4\" style=\"line-height: 0px;font-size: 0px;border-collapse: collapse;mso-line-height-rule: exactly;width:210px;height:4px;\"><img src=\"http://image.e.hostingmessages.com/lib/fe9212727362027d76/m/1/mojo-spacer.gif\" width=\"210\" height=\"4\" style=\"display: block;border: 0 !important;outline: none !important;\" border=\"0\"></td>\n" +
            "                                                                                          <td width=\"4\" style=\"line-height: 0px;font-size: 0px;border-collapse: collapse;mso-line-height-rule: exactly;width:4px;height:4px;\"><img src=\"https://bluehost-cdn.com/media/shared/marketing/domains/_bh/20170101_BH_E1_curve_6.png\" width=\"4\" height=\"4\" style=\"display: block;border: 0 !important;outline: none !important;\" border=\"0\"></td>\n" +
            "                                                                                        </tr>\n" +
            "                                                                                      </table>\n" +
            "                                                                                    </td>\n" +
            "                                                                                  </tr>\n" +
            "                                                                                  <tr>\n" +
            "                                                                                    <td height=\"36\" bgcolor=\"#06ba00\" class=\"em_btn\" valign=\"middle\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;font-family: \"Open Sans\", Arial, sans-serif;font-size: 20px;color: #ffffff;text-decoration: none;text-align: center;font-weight: normal;height:36px;\"><a href=\"https://my.bluehost.com/cgi/dm/whois_validate/852519/1511764350_0WQUhsYxbw1/accept\" target=\"_blank\" class=\"em_btn\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;font-family: \"Open Sans\", Arial, sans-serif;font-size: 20px;color: #ffffff;text-decoration: none;text-align: center;font-weight: normal; line-height: 36px;\">Verify Your Email</a></td>\n" +
            "                                                                                  </tr>\n" +
            "                                                                                  <tr>\n" +
            "                                                                                    <td style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">\n" +
            "                                                                                      <table width=\"218\" style=\"max-width: 218px;border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
            "                                                                                        <tr>\n" +
            "                                                                                          <td width=\"4\" style=\"line-height: 0px;font-size: 0px;border-collapse: collapse;mso-line-height-rule: exactly;width:4px;height:4px;\"><img src=\"https://bluehost-cdn.com/media/shared/marketing/domains/_bh/20170101_BH_E1_curve_7.png\" width=\"4\" height=\"4\" style=\"display: block;border: 0 !important;outline: none !important;\" border=\"0\"></td>\n" +
            "                                                                                          <td width=\"210\" bgcolor=\"#06ba00\" height=\"4\" style=\"line-height: 0px;font-size: 0px;border-collapse: collapse;mso-line-height-rule: exactly;width:210px;height:4px;\"><img src=\"http://image.e.hostingmessages.com/lib/fe9212727362027d76/m/1/mojo-spacer.gif\" width=\"210\" height=\"4\" style=\"display: block;border: 0 !important;outline: none !important;\" border=\"0\"></td>\n" +
            "                                                                                          <td width=\"4\" style=\"line-height: 0px;font-size: 0px;border-collapse: collapse;mso-line-height-rule: exactly;width:4px;height:4px;\"><img src=\"https://bluehost-cdn.com/media/shared/marketing/domains/_bh/20170101_BH_E1_curve_8.png\" width=\"4\" height=\"4\" style=\"display: block;border: 0 !important;outline: none !important;\" border=\"0\"></td>\n" +
            "                                                                                        </tr>\n" +
            "                                                                                      </table>\n" +
            "                                                                                    </td>\n" +
            "                                                                                  </tr>\n" +
            "                                                                                </table></td>\n" +
            "                                            </tr>\n" +
            "                                          </tbody>\n" +
            "                                        </table>\n" +
            "\n" +
            "                                      </td>\n" +
            "                                      <td style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "    </tr>\n" +
            "  </tbody>\n" +
            "</table>\n" +
            "</td>\n" +
            "                                    </tr>\n" +
            "                                      <tr>\n" +
            "                                      <td height=\"35\" class=\"em_height\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                                    </tr>\n" +
            "                                      <tr>\n" +
            "                                      <td align=\"left\" valign=\"top\" class=\"em_greyfont\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;font-family: \"Open Sans\", Arial, sans-serif;font-size: 14px;line-height: 18px;color: #5f6165;text-decoration: none;\">Please be sure to verify your email address within the next 9 days &mdash; otherwise <strong>onthetinkatrail.com</strong> will be deactivated. <br />\n" +
            "<br />\n" +
            "\n" +
            "\n" +
            "                                          By verifying your email, you&rsquo;ll also be agreeing to our <a href=\"https://my.bluehost.com/terms?utm_source=ebemail&utm_medium=email&utm_content=%%jobid%%_1.0%20Verify%20Domain&utm_campaign=On-boarding%20Series_v2&campaign_genre=Upsell\" target=\"_blank\" class=\"em_tracking em_link\">Terms of Service</a>.<br />\n" +
            "<br />\n" +
            "\n" +
            "\n" +
            "Thanks again for choosing Bluehost for your web hosting.<br />\n" +
            "<br />\n" +
            "\n" +
            "\n" +
            "Sincerely,<br />\n" +
            "The Bluehost Team </td>\n" +
            "                                    </tr>\n" +
            "                                    <tr>\n" +
            "                                      <td height=\"50\" class=\"em_height\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                                    </tr>\n" +
            "  </tbody>\n" +
            "</table>\n" +
            "</td>\n" +
            "      <td width=\"10\" class=\"em_hide\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "    </tr>\n" +
            "  </tbody>\n" +
            "</table>\n" +
            "</td>\n" +
            "                                      </tr>\n" +
            "                                      <tr>\n" +
            "                                          <td height=\"1\" style=\"font-size: 0px; line-height: 0px;\" bgcolor=\"#dadadb\"><img src=\"https://bluehost-cdn.com/media/shared/marketing/domains/_bh/20170101_BH_E1_spacer.gif\" width=\"1\" height=\"1\" border=\"0\" style=\"display: block;\"></td>\n" +
            "                                      </tr>\n" +
            "                                  </tbody>\n" +
            "                                </table>\n" +
            "                              </td>\n" +
            "                              <td width=\"40\" class=\"em_spacer\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                            </tr>\n" +
            "                          </tbody>\n" +
            "                        </table>\n" +
            "                      </td>\n" +
            "                    </tr>\n" +
            "                    \n" +
            "                    <tr>\n" +
            "                      <td valign=\"top\" align=\"center\" bgcolor=\"#ffffff\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">\n" +
            "                        <table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                          <tbody>\n" +
            "                            <tr>\n" +
            "                              <td width=\"10\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                              <td align=\"center\" valign=\"top\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">\n" +
            "                                <table width=\"100%\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                                  <tr>\n" +
            "                                    <td height=\"16\" class=\"em_height\" style=\"border-collapse: collapse;mso-line-height-rule: exactly; font-size: 1px; line-height: 1px;\">&nbsp;</td>\n" +
            "                                  </tr>\n" +
            "                                  <tr>\n" +
            "                                    <td align=\"center\" class=\"em_lightbluefont\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;font-family: \"Open Sans\", Arial, sans-serif;font-size: 16px;line-height: 22px;color: #3575d3;text-decoration: none;\">Need help? Get in touch any time.\n" +
            "</td>\n" +
            "                                  </tr>\n" +
            "                                    <tr>\n" +
            "                                        <td align=\"center\" valign=\"middle\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;font-family: \"Open Sans\", Arial, sans-serif;font-size: 14px;line-height: 19px;color: #555556;text-decoration: none;\"><span style=\"color: #3575d3; text-decoration: underline;\"><a href=\"mailto:domains@bluehost.com\" class=\"em_tracking\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;font-family: \"Open Sans\", Arial, sans-serif;font-size: 14px;line-height: 21px;color: #3575d3 !important;text-decoration: underline;white-space: nowrap !important;word-break: keep-all !important;\">Email us</a></span> or call <span class=\"em_grey\"><a href=\"tel:18884014678\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;font-family: \"Open Sans\", Arial, sans-serif;font-size: 14px;line-height: 21px;color: #555555 !important;text-decoration: none;white-space: nowrap !important;word-break: keep-all !important;\">888-401-4678</a></span>.</td>\n" +
            "                                    </tr>\n" +
            "                                  <tr>\n" +
            "                                    <td height=\"25\" style=\"font-size: 1px;line-height: 1px;border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                                  </tr>\n" +
            "                                  \n" +
            "                                </table>\n" +
            "                              </td>\n" +
            "                              <td width=\"10\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                            </tr>\n" +
            "                          </tbody>\n" +
            "                        </table>\n" +
            "                      </td>\n" +
            "                    </tr>\n" +
            "                    <tr>\n" +
            "            <td bgcolor=\"#3575d3\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"><table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                <tr>\n" +
            "                  <td style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"><table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" bgcolor=\"#3173d6\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                      <tr>\n" +
            "                        <td height=\"24\" class=\"em_h20_20\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"></td>\n" +
            "                      </tr>\n" +
            "                      <tr>\n" +
            "                        <td valign=\"top\" align=\"center\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"><table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                            <tr>\n" +
            "                              <td style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"><table width=\"265\" align=\"left\" class=\"em_wrapper\" cellpadding=\"0\" cellspacing=\"0\" border=\"0\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                                  <tr>\n" +
            "                                    <td style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"><table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                                        <tr>\n" +
            "                                          <td width=\"18\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"></td>\n" +
            "                                          <td valign=\"middle\" class=\"em_cent connect\"><span style=\"font-weight: bold;border-collapse: collapse;mso-line-height-rule: exactly;\">Connect</span> With Us</td>\n" +
            "                                          <td width=\"18\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"></td>\n" +
            "                                        </tr>\n" +
            "                                      </table></td>\n" +
            "                                  </tr>\n" +
            "                                  <tr>\n" +
            "                                    <td height=\"8\" style=\"line-height: 1px;font-size: 1px;border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                                  </tr>\n" +
            "                                </table>\n" +
            "                                <table width=\"236\" class=\"em_wrapper\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"right\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                                  <tr>\n" +
            "                                    <td style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"><table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                                        <tr>\n" +
            "                                          <td width=\"26\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"></td>\n" +
            "                                          <td align=\"center\" valign=\"middle\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"><table width=\"184\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" align=\"center\" style=\"border-collapse: collapse;mso-table-lspace: 0px;mso-table-rspace: 0px;\">\n" +
            "                                              <tr>\n" +
            "                                                <td width=\"12\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"><a href=\"https://www.facebook.com/bluehost\" target=\"_blank\" style=\"text-decoration: none;color: #ffffff;border-collapse: collapse;mso-line-height-rule: exactly;\"><img src=\"http://image.e.hostingmessages.com/lib/fe9212727362027d76/m/1/mojo-fb.png\" alt=\"fb\" width=\"12\" height=\"23\" style=\"display: block;max-width: 12px;font-family: Arial, sans-serif;font-size: 10px;line-height: 19px;color: #ffffff;font-weight: bold;border: 0 !important;outline: none !important;\" border=\"0\"></a></td>\n" +
            "                                                <td style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                                                <td width=\"22\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"><a href=\"https://twitter.com/bluehost\" target=\"_blank\" style=\"text-decoration: none;color: #ffffff;border-collapse: collapse;mso-line-height-rule: exactly;\"><img src=\"http://image.e.hostingmessages.com/lib/fe9212727362027d76/m/1/mojo-twitter.png\" alt=\"tw\" width=\"22\" height=\"19\" style=\"display: block;max-width: 22px;font-family: Arial, sans-serif;font-size: 10px;line-height: 19px;color: #ffffff;font-weight: bold;border: 0 !important;outline: none !important;\" border=\"0\"></a></td>\n" +
            "                                                <td style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                                                <td width=\"22\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"><a href=\"https://plus.google.com/+bluehost/posts\" target=\"_blank\" style=\"text-decoration: none;color: #ffffff;border-collapse: collapse;mso-line-height-rule: exactly;\"><img src=\"http://image.e.hostingmessages.com/lib/fe9212727362027d76/m/1/google.png\" alt=\"google+\" width=\"22\" height=\"21\" style=\"display: block;max-width: 22px;font-family: Arial, sans-serif;font-size: 10px;line-height: 21px;color: #ffffff;font-weight: bold;border: 0 !important;outline: none !important;\" border=\"0\"></a></td>\n" +
            "                                                <td style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                                                <td width=\"21\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"><a href=\"https://www.youtube.com/user/bluehost\" target=\"_blank\" style=\"text-decoration: none;color: #ffffff;border-collapse: collapse;mso-line-height-rule: exactly;\"><img src=\"http://image.e.hostingmessages.com/lib/fe9212727362027d76/m/1/mojo-youtube.png\" alt=\"yt\" width=\"21\" height=\"27\" style=\"display: block;max-width: 21px;font-family: Arial, sans-serif;font-size: 10px;line-height: 27px;color: #ffffff;font-weight: bold;border: 0 !important;outline: none !important;\" border=\"0\"></a></td>\n" +
            "                                                <td style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                                                <td width=\"21\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"><a href=\"https://www.linkedin.com/company/bluehost-com\" target=\"_blank\" style=\"text-decoration: none;color: #ffffff;border-collapse: collapse;mso-line-height-rule: exactly;\"><img src=\"http://image.e.hostingmessages.com/lib/fe9212727362027d76/m/1/img_2.png\" alt=\"in\" width=\"21\" height=\"21\" style=\"display: block;max-width: 21px;font-family: Arial, sans-serif;font-size: 10px;line-height: 21px;color: #ffffff;font-weight: bold;border: 0 !important;outline: none !important;\" border=\"0\"></a></td>\n" +
            "                                              </tr>\n" +
            "                                            </table></td>\n" +
            "                                          <td width=\"26\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\"></td>\n" +
            "                                        </tr>\n" +
            "                                      </table></td>\n" +
            "                                  </tr>\n" +
            "                                  <tr>\n" +
            "                                    <td height=\"8\" style=\"line-height: 1px;font-size: 1px;border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                                  </tr>\n" +
            "                                </table></td>                            </tr>\n" +
            "                          </table></td>\n" +
            "                      </tr>\n" +
            "                    </table></td>\n" +
            "                </tr>\n" +
            "                <tr>\n" +
            "                  <td class=\"full_width_image\" style=\"line-height: 0px;font-size: 0px;border-collapse: collapse;mso-line-height-rule: exactly;\"><img src=\"https://bluehost-cdn.com/media/shared/marketing/domains/_bh/20170101_BH_E1_btm_image.jpg\" width=\"600\" height=\"15\" style=\"display: block;width: 100%;max-width: 600px;height: auto;border: 0 !important;outline: none !important;\" border=\"0\"></td>\n" +
            "                </tr>\n" +
            "              </table></td>\n" +
            "          </tr>\n" +
            "                    <!--//Content-->\n" +
            "                    <tr>\n" +
            "                      <td height=\"25\" style=\"font-size: 1px;line-height: 1px;border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                    </tr>\n" +
            "                    <tr>\n" +
            "                        <td valign=\"top\">\n" +
            "                            <table align=\"center\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n" +
            "  <tbody>\n" +
            "    <tr>\n" +
            "      <td width=\"50\"  class=\"em_spacer\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "      \n" +
            "                      <td align=\"left\" valign=\"top\" class=\"em_greyfont2\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;font-family: \"Open Sans\", Arial, sans-serif;font-size: 12px;line-height: 14px;color: #777a7f;text-decoration: none;\">This note is being sent due to the registration, transfer, or change to the WHOIS information on your domain(s). <br />\n" +
            "<br />\n" +
            "\n" +
            "\n" +
            "Please note that failure to verify the Registrant contact email address will lead to deactivation of the respective domain name(s) if not completed within 15 days from the date of that action. Once deactivated, the domain names will not function until the email address is verified. <br />\n" +
            "<br />\n" +
            "\n" +
            "\n" +
            "                          Affected domain names : onthetinkatrail.com\n" +
            "                      </td>\n" +
            "                    \n" +
            "      <td width=\"50\"  class=\"em_spacer\" style=\"border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "    </tr>\n" +
            "  </tbody>\n" +
            "</table>\n" +
            "</td>\n" +
            "                    </tr>\n" +
            "                    <tr>\n" +
            "                      <td height=\"25\" style=\"font-size: 1px;line-height: 1px;border-collapse: collapse;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
            "                    </tr>\n" +
            "                  </tbody>\n" +
            "                </table>\n" +
            "              </td>\n" +
            "            </tr>\n" +
            "          </table>\n" +
            "        </td>\n" +
            "      </tr>\n" +
            "      <!-- Emailer Ends Here //-->\n" +
            "    </table>\n" +
            "    <div class=\"em_hide\" style=\" white-space:nowrap; font:20px courier; color:#f1f3f4; background-color:#f1f3f4;\">&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;</div>\n" +
            "  \n" +
            "\n" +
            "<br>\n" +
            "\n" +
            "</body>  \n" +
            "</html>";

        HtmlSanitizeOptions.Builder options = HtmlSanitizeOptions.builder();
        options.setOptConfigName(null);
        options.setDropExternalImages(false);
        options.setModified(null);
        options.setCssPrefix(null);
        options.setParserPreference(ParserPreference.JERICHO);

        String sanitized = getHtmlService().sanitize(content, options.build()).getContent();

        Pattern p = Pattern.compile("<a[^>]*>Verify Your Email</a>");
        Matcher m = p.matcher(sanitized);
        assertTrue("Unexpected content", m.find());
    }

}

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

package com.openexchange.mail.structure;

import org.json.JSONObject;
import com.openexchange.mail.AbstractMailTest;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.converters.MimeMessageConverter;
import com.openexchange.mail.structure.handler.MIMEStructureHandler;

/**
 * {@link MailSimpleStructureTest} - Test for output of structured JSON mail object.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailSimpleStructureTest extends AbstractMailTest {

    /**
     * Initializes a new {@link MailSimpleStructureTest}.
     */
    public MailSimpleStructureTest() {
        super();
    }

    /**
     * Initializes a new {@link MailSimpleStructureTest}.
     *
     * @param name The test name
     */
    public MailSimpleStructureTest(final String name) {
        super(name);
    }



    private static final byte[] SIMPLE = ("Date: Sat, 14 Nov 2009 17:03:09 +0100 (CET)\n" +
    		"From: Alice Doe <alice@foobar.com>\n" +
    		"To: bob@foobar.com\n" +
    		"Message-ID: <1837640730.5.1258214590077.JavaMail.foobar@foobar>\n" +
    		"Subject: The mail subject\n" +
    		"MIME-Version: 1.0\n" +
    		"Content-Type: text/plain; charset=US-ASCII\n" +
    		"Content-Disposition: inline; filename=foo.txt\n" +
    		"X-Priority: 3\n" +
    		"\n" +
    		"Mail text.\n" +
    		"\n" +
    		"People have been asking for support for the IMAP IDLE command for quite\n" +
    		"a few years and I think I've finally figured out how to provide such\n" +
    		"support safely. The difficulty isn't in executing the command, which\n" +
    		"is quite straightforward, the difficulty is in deciding how to expose\n" +
    		"it to applications, and inhandling the multithreading issues that\n" +
    		"arise.").getBytes();

    private static final byte[] SIMPLE2 = ("Date: Sat, 14 Nov 2009 17:03:09 +0100 (CET)\n" +
        "From: Alice Doe <alice@foobar.com>\n" +
        "To: bob@foobar.com\n" +
        "Message-ID: <1837640730.5.1258214590077.JavaMail.foobar@foobar>\n" +
        "Subject: The mail subject\n" +
        "MIME-Version: 1.0\n" +
        "Content-Type: text/plain; charset=\"utf-8\"\n" +
        "Content-Transfer-Encoding: base64\n" +
        "Content-Disposition: inline; filename=foo.txt\n" +
        "X-Priority: 3\n" +
        "\n" +
        "SGkgSG9sZ2VyLA0KDQpUaGFua3MgZm9yIHRoZSBpbmZvcm1hdGlvbi4uIFNoYXJpbmcgYSBzYW1w\n" +
        "bGUgQk9NIGFuZCBhcHByZWNpYXRlIGlmIHlvdSBjb3VsZCBwcm92aWRlIGRldGFpbHMgaW4gdGhl\n" +
        "IHNhbWUgZm9ybWF0Li4gIEFwcHJlY2lhdGUgaWYgeW91IHByb3Zpc2lvbiBkZXRhaWxzIGZvciB0\n" +
        "aGUgYmVsb3cgbWVudGlvbmVkIHBvaW50cw0KDQoNCmEuICAgIFJlY29tbWVuZCB0aGUgUHJvdmlz\n" +
        "aW9uaW5nIFNlcnZlciAoQXBhcnQgZnJvbSBQYXJhbGxlbHMpIHRoYXQgd291bGQgd29yayB3aXRo\n" +
        "IE9wZW4gRXhjaGFuZ2UNCg0KYi4gICAgUmVjb21tZW5kIEFudGktVmlydXMgLyBBbnRpLVNwYW0N\n" +
        "Cg0KYy4gICAgIFJlY29tbWVuZCBCYWNrLXVwIHRvb2wgZm9yIHRoZSBFbWFpbC9BcmNoaXZhbA0K\n" +
        "DQpkLiAgICBDYW4gdGhlIHNlcnZpY2VzIGxpa2UgIEJsYWNrLWJlcnJ5ICAvIEFjdGl2ZS1TeW5j\n" +
        "IGJlIHVzZWQgPz8NCg0KVGhhbmtzIGFuZCBSZWdhcmRzDQoNCkdva3VsbmF0aA0KDQoNCkZyb206\n" +
        "IEFjaHR6aWdlciwgSG9sZ2VyIFttYWlsdG86aG9sZ2VyLmFjaHR6aWdlckBvcGVuLXhjaGFuZ2Uu\n" +
        "Y29tXQ0KU2VudDogVGh1cnNkYXksIE9jdG9iZXIgMjksIDIwMDkgODozNiBQTQ0KVG86IEdva3Vs\n" +
        "bmF0aCBDLjsgS2VsdGluZywgRXJuc3QNClN1YmplY3Q6IFJlOiBJbnRyb2R1Y3Rpb29uIEtyeXB0\n" +
        "b3MgTmV0d29ya3MNCg0KDQpIZWxsbyBHb2t1bG5hdGgsDQoNCg0KDQoiS2VsdGluZywgRXJuc3Qi\n" +
        "IDxlcm5zdC5rZWx0aW5nQG9wZW4teGNoYW5nZS5jb20+IGhhdCBhbSAyOS4gT2t0b2JlciAyMDA5\n" +
        "IHVtIDExOjAwIGdlc2NocmllYmVuOg0KDQoNCg0KSGVsbG8gR29rdWxuYXRoLA0KDQoNCg0KbGV0\n" +
        "IG1lIGludHJvZHVjZSBIb2xnZXIuIEhvbGdlciBpcyB3b3JraW5nIGZvciBTdGVwaGFuwrRzIFBy\n" +
        "b2Zlc3Npb25hbCBTZXJ2aWNlcyB0ZWFtLCBhbmQgd2lsbCBiZSB3b3JraW5nIHdpdGggeW91IG9u\n" +
        "IHRoZSBvcHBvcnR1bml0eS4gSGUgd2lsbCBzZW5kIHlvdSB0aGlzIGFmdGVybm9vbiBhIGRyYWZ0\n" +
        "IHByb3Bvc2FsLCBzbyB3ZSBjYW4gc3RhcnQgdGhlIGRpc2N1c3Npb24gd2l0aCB5b3VyIHByb3Nw\n" +
        "ZWN0Lg0KDQoNCg0KDQoNCkVybnN0IGFza2VkIG1lIHRvIGdpdmUgYSByb3VnaCBvdmVydmlldyBm\n" +
        "b3Igd2hhdCBpcyBuZWVkZWQgdG8gc2V0IHVwIGEgbmV3IGluZnJhc3RydWN0dXJlIGZvcg0KDQo1\n" +
        "MC4wMDAgdXNlcnMuIEluIGZhY3QgdGhpcyBkZXBlbmRzIG9uIHRoZSB3b3JrbG9hZCBnZW5lcmF0\n" +
        "ZWQgYnkgdGhlbS4gRm9yIGV4YW1wbGUgYSBDb21wYW55DQoNCndpdGggNTAuMDAwIGVtcGxveWVl\n" +
        "cyB3aWxsIGhhdmUgbW9yZSBjb25jdXJyZW50IHVzZXJzIHRoYW4gYSBob3N0ZXIgc2VydmluZyBi\n" +
        "dXNpbmVzcy9wcml2YXRlDQoNCmN1c3RvbWVycyBpbiBkaWZmZXJlbnQgdGltZXpvbmVzLg0KDQoN\n" +
        "Cg0KQXMgc3RhcnQgaSB3b3VsZCBnbyBmb3Igd2hhdCB3ZSBoYXZlIGluIGNoYXB0ZXIgMi4zIG9m\n" +
        "IG91ciBzaXppbmcgZG9jdW1lbnQgZm9yIHRoZSBPcGVuLVhjaGFuZ2UNCg0KcGFydDoNCg0KICAg\n" +
        "ICAgICAgICAgICAgICAgICAgICMgUkFNIENQVSBDb3Jlcw0KT1ggQXBwbGljYXRpb24gc2VydmVy\n" +
        "IDIgOEdCIDQNCk15c3FsIHNlcnZlcnMgICAgICAgICAyIDRHQiAyDQpBcGFjaGUgc2VydmVycyAg\n" +
        "ICAgICAgMA0KRmlsZSBzdG9yZSBTZXJ2ZXJzICAgIDENCkxvYWQgQmFsYW5jZXIgICAgICAgICAx\n" +
        "DQoNCg0KDQpGb3IgVGhlIGltYXAgc2VydmVyLCB0aGUgY2FsY3VsYXRpb24gaXMgdG8gaGF2ZSBh\n" +
        "dCBtYXhpbXVtIDM1MDAgY29uY3VycmVudCBzZXNzaW9ucyBvbiBhIG1hY2hpbmUNCg0KZnJvbSB0\n" +
        "aGUgc2FtZSBzaXplIGFzIHRoZSBBcHBsaWNhdGlvbiBzZXJ2ZXIgYWJvdmUgYW5kIGEgc2VwYXJh\n" +
        "dGUgTG9hZCBiYWxhbmNlci4gRm9yIGJlc3QNCg0Kc2NhbGFiaWxpdHkgd2UgcmVjb21tZW5kIGRv\n" +
        "dmVjb3QgYmFzZWQgb24gd2hhdCB3ZSBoYXZlIGhlYXJkIGZyb20gb3VyIGN1c3RvbWVycy4gVGhl\n" +
        "IHNtcHQNCg0Kc2VydmljZSAocG9zZml4KSBjYW4gcnVuIG9uIHRoZSBzYW1lIGhvc3RzLCB0b28u\n" +
        "DQoNCg0KDQpUaGUgZmlsZSBzdG9yZSBhbmQgaW1hcCBzcGFjZSBzaG91bGQgYmUgb24gTkZTLCB0\n" +
        "byBiZSBvbiB0aGUgc2F2ZSBzaWRlLCBhIGNvbW1lcmNpYWwgTkZTIFNlcnZlcg0KDQpsaWtlIHBy\n" +
        "b3ZpZGVkIGJ5IG5ldGFwcCBzaG91bGQgYmUgdXNlZC4gQXQgbGVhc3QgaWYgdGhlcmUgYXJlIG1p\n" +
        "bGxpb25zIG9mIHVzZXJzIHRvIGJlIGV4cGVjdGVkDQoNCmZvciB0aGUgZnV0dXJlLg0KDQoNCg0K\n" +
        "Rm9yIHRoZSBPcGVyYXRpbmcgc3lzdGVtIG9uIHRoZSBTZXJ2ZXJzIGxpbnV4IGlzIHJlcXVpcmVk\n" +
        "LiBUaGVyZSB3ZSBzdXBwb3J0IGRlYmlhbiwgU1VTRSBhbmQNCg0KUmVkaGF0LiBPdXIgYmlnZXN0\n" +
        "IGluc3RhbGxhdGlvbiBydW5zIG9uIGRlYmlhbi4NCg0KDQoNClRoZSBkZXNpZ24gb2YgdGhlIE9w\n" +
        "ZW4tWGNoYW5nZSBhcHBsaWNhdGlvbiBhcyB3ZWxsIGFzIGRvdmVjb3QgaXMgdG8gc2NhbGUgdmVy\n" +
        "dGljYWwgYW5kIGhvcml6b250YWwuDQoNCg0KDQpBcyBmYXIgYXMgaSB1bmRlcnN0b29kLCB0aGUg\n" +
        "Y29tcGxldGUgaW5zdGFsbGF0aW9uIHdpbGwgYmUgZG9uZSBieSBrcnlwdG9zbmV0d29ya3M/DQoN\n" +
        "Cg0KDQpUaGUgUXVlc3Rpb24gaSBoYXZlIGlzIGFib3V0IGhvdyBwcm92aXNpb25pbmcgc2hvdWxk\n" +
        "IGJlIGRvbmUuIFdoYXQgaXMgY3VycmVudGx5IGluIHBsYWNlIHRvDQoNCm1hbmdlIHVzZXIgYWNj\n" +
        "b3VudHMvYmlsbGluZy8uLi4/DQoNCg0KDQpUaGFuayB5b3UsDQoNCkhvbGdlcg0KDQoNCg0KDQoN\n" +
        "CkVybnN0DQoNCg0KDQoNCk9uIE9jdG9iZXIgMjIsIDIwMDkgYXQgNzoxNSBBTSBHb2t1bG5hdGgg\n" +
        "QyA8Z29rdWxuYXRoLmNAa3J5cHRvc25ldHdvcmtzLmNvbT4gd3JvdGU6DQoNCj4gRmluZSBFcm5z\n" +
        "dC4uIFdlIHNoYWxsIGhhdmUgdGhlIGRpc2N1c3Npb24uLg0KPg0KPiBUaGFua3MgYW5kIFJlZ2Fy\n" +
        "ZHMNCj4NCj4gR29rdWxuYXRoDQo+DQo+IC0tLS0tT3JpZ2luYWwgTWVzc2FnZS0tLS0tDQo+IEZy\n" +
        "b206IGVybnN0LmtlbHRpbmdAb3Blbi14Y2hhbmdlLmNvbSBbbWFpbHRvOmVybnN0LmtlbHRpbmdA\n" +
        "b3Blbi14Y2hhbmdlLmNvbV0NCj4gU2VudDogV2VkbmVzZGF5LCBPY3RvYmVyIDIxLCAyMDA5IDY6\n" +
        "MDcgUE0NCj4gVG86IGdva3VsbmF0aC5jQGtyeXB0b3NuZXR3b3Jrcy5jb20NCj4gU3ViamVjdDog\n" +
        "TmV3IEFwcG9pbnRtZW50OiBUZWNobmljYWwgZGlzY3Vzc2lvbiBmb3IgSG9zdGluZyBwcm9qZWN0\n" +
        "DQo+DQo+IEEgbmV3IGFwcG9pbnRtZW50IHdhcyBjcmVhdGVkIGJ5IEtlbHRpbmcsIEVybnN0Lg0K\n" +
        "Pg0KPiBBcHBvaW50bWVudA0KPiA9PT09PT09PT09PQ0KPiBDcmVhdGVkIGJ5OiBLZWx0aW5nLCBF\n" +
        "cm5zdA0KPiBDcmVhdGVkIGF0OiBPY3QgMjEsIDIwMDkgMjozNzowMCBQTSwgQ0VTVA0KPiBEZXNj\n" +
        "cmlwdGlvbjogVGVjaG5pY2FsIGRpc2N1c3Npb24gZm9yIEhvc3RpbmcgcHJvamVjdA0KPiBMb2Nh\n" +
        "dGlvbjogU2t5cGUNCj4NCj4gU3RhcnQgZGF0ZTogT2N0IDIzLCAyMDA5IDk6MDA6MDAgQU0sIENF\n" +
        "U1QgRW5kIGRhdGU6IE9jdCAyMywgMjAwOSAxMDowMDowMCBBTSwgQ0VTVA0KPg0KPiBDb21tZW50\n" +
        "czoNCj4gSGVsbG8gZ29rdWxuYXRoLA0KPg0KPiBkbyB5b3UgaGF2ZSB0aW1lIG9uIEZyaWRheSBh\n" +
        "dCA5YW0gQ0VULCB0byBjb250aW51ZSBvdXIgZGlzY3Vzc2lvbiBmcm9tIHRvZGF5IHRvZ2V0aGVy\n" +
        "IHdpdGggU3RlcGhhbiBmcm9tIG91ciBQcm9mZXNzaW9uYWwgU2VydmljZXMgdGVhbS4NCj4NCj4g\n" +
        "RXJuc3QNCj4NCj4NCj4gLS0tLS0tLS0tLSBPcmlnaW5hbCBNZXNzYWdlIC0tLS0tLS0tLS0NCj4g\n" +
        "RnJvbTogZ29rdWxuYXRoLmNAa3J5cHRvc25ldHdvcmtzLmNvbQ0KPiBUbzogZXJuc3Qua2VsdGlu\n" +
        "Z0BvcGVuLXhjaGFuZ2UuY29tDQo+IFJlY2VpdmVkOiAxMC0yMC0yMDA5IDAxOjI1IFBNDQo+IFN1\n" +
        "YmplY3Q6IE91ciBkaXNjdXNzaW9uDQo+DQo+IEhpIEVybnN0LCA8YnIgLz48YnIgLz4gPGJyIC8+\n" +
        "PGJyIC8+TGV0J3MgZmFzdC10cmFjayBvdXIgZWFybGllciBkaXNjdXNzaW9ucyBmb3IgYSByZXF1\n" +
        "aXJlbWVudC4gPGJyIC8+PGJyIC8+IDxiciAvPjxiciAvPkkgd291bGQgbGlrZSB0byB1bmRlcnN0\n" +
        "YW5kIHRoZSBhcmNoaXRlY3R1cmUsIHNpemluZywgQm9NICZhbXA7IGNvc3RzIHRvIGJ1aWxkIGE8\n" +
        "YnIgLz5idXNpbmVzcyBjYXNlIGZvciAxIE1pbGxpb24gbWFpbGJveGVzIG9uIHByaW9yaXR5IGJh\n" +
        "c2lzLiBDb3VsZCB5b3Ugc2hhcmU8YnIgLz53aXRoIGRldGFpbHMgPGJyIC8+PGJyIC8+IDxiciAv\n" +
        "PjxiciAvPlRoYW5rcyBhbmQgUmVnYXJkcyw8YnIgLz48YnIgLz4gPGJyIC8+PGJyIC8+R29rdWxu\n" +
        "YXRoLkM8YnIgLz48YnIgLz5WUC0gQnVzaW5lc3MgU29sdXRpb25zPGJyIC8+PGJyIC8+S3J5cHRv\n" +
        "cyBOZXR3b3JrcyBQdnQuIEx0ZC48YnIgLz48YnIgLz4zNiwgTmF0V2VzdCBWZW5rYXRyYW1hbmEs\n" +
        "IDFzdCBGbG9vciwgS2FtYWtvdGkgTmFnYXIsIDxiciAvPjxiciAvPlBhbGxpa2FybmFpIENoZW5u\n" +
        "YWkgLSA2MDAxMDA8YnIgLz48YnIgLz5IYW5kIFBob25lOiArOTEtOTk0MDAtNTIzNTc8YnIgLz48\n" +
        "YnIgLz5PZmZpY2U6ICs5MSA0NCA0MzkxNTE1MTxiciAvPjxiciAvPjxhIGhyZWY9Imh0dHA6Ly93\n" +
        "d3cua3J5cHRvc25ldHdvcmtzLmNvbSIgdGFyZ2V0PSJfYmxhbmsiPnd3dy5rcnlwdG9zbmV0d29y\n" +
        "a3MuY29tPC9hPjxiciAvPjxiciAvPiA8YnIgLz48YnIgLz4tIFdlIG1ha2UgSVQgc2ltcGxlIC48\n" +
        "YnIgLz48YnIgLz4gPGJyIC8+PGJyIC8+JnF1b3Q7VGhlIGluZm9ybWF0aW9uIGNvbnRhaW5lZCBp\n" +
        "biB0aGlzIGNvbW11bmljYXRpb24gaXMgaW50ZW5kZWQgc29sZWx5IGZvciB0aGU8YnIgLz51c2Ug\n" +
        "b2YgdGhlIGluZGl2aWR1YWwgb3IgZW50aXR5IHRvIHdob20gaXQgaXMgYWRkcmVzc2VkIGFuZCBv\n" +
        "dGhlcnM8YnIgLz5hdXRob3JpemVkIHRvIHJlY2VpdmUgaXQuIEl0IG1heSBjb250YWluIGNvbmZp\n" +
        "ZGVudGlhbCBvciBsZWdhbGx5IHByaXZpbGVnZWQ8YnIgLz5pbmZvcm1hdGlvbi5JZiB5b3UgYXJl\n" +
        "IG5vdCB0aGUgaW50ZW5kZWQgcmVjaXBpZW50IHlvdSBhcmUgaGVyZWJ5IG5vdGlmaWVkPGJyIC8+\n" +
        "dGhhdCBhbnkgZGlzY2xvc3VyZSwgY29weWluZywgZGlzdHJpYnV0aW9uIG9yIHRha2luZyBhbnkg\n" +
        "YWN0aW9uIGluIHJlbGlhbmNlPGJyIC8+b24gdGhlIGNvbnRlbnRzIG9mIHRoaXMgaW5mb3JtYXRp\n" +
        "b24gaXMgc3RyaWN0bHkgcHJvaGliaXRlZCBhbmQgbWF5IGJlPGJyIC8+dW5sYXdmdWwuIElmIHlv\n" +
        "dSBoYXZlIHJlY2VpdmVkIHRoaXMgY29tbXVuaWNhdGlvbiBpbiBlcnJvciwgcGxlYXNlIG5vdGlm\n" +
        "eSB1czxiciAvPmltbWVkaWF0ZWx5IGJ5IHJlc3BvbmRpbmcgdG8gdGhpcyBlbWFpbCBhbmQgdGhl\n" +
        "biBkZWxldGUgaXQgZnJvbSB5b3VyIHN5c3RlbS48YnIgLz5LcnlwdG9zIE5ldHdvcmtzIGlzIG5l\n" +
        "aXRoZXIgbGlhYmxlIGZvciB0aGUgcHJvcGVyIGFuZCBjb21wbGV0ZSB0cmFuc21pc3Npb248YnIg\n" +
        "Lz5vZiB0aGUgaW5mb3JtYXRpb24gY29udGFpbmVkIGluIHRoaXMgY29tbXVuaWNhdGlvbiBub3Ig\n" +
        "Zm9yIGFueSBkZWxheSBpbiBpdHM8YnIgLz5yZWNlaXB0JnF1b3Q7PGJyIC8+PGJyIC8+IDxiciAv\n" +
        "PjxiciAvPg0KPg0KPg0KPiBQYXJ0aWNpcGFudHMNCj4gPT09PT09PT09PT09DQo+ICdFcm5zdCBL\n" +
        "ZWx0aW5nJyAoZXh0ZXJuYWwpDQo+IEVybnN0IChleHRlcm5hbCkNCj4gR29rdWxuYXRoIEMgKGV4\n" +
        "dGVybmFsKQ0KPiBLZWx0aW5nLCBFcm5zdCAoYWNjZXB0ZWQpDQo+IE1hcnRpbiwgU3RlcGhhbiAo\n" +
        "d2FpdGluZykNCj4NCj4gUmVzb3VyY2VzDQo+ID09PT09PT09PQ0KPiBObyByZXNvdXJjZXMgaGF2\n" +
        "ZSBiZWVuIHNjaGVkdWxlZC4NCj4NCj4gPT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09\n" +
        "PT09PT09PT09DQo+DQoNCg0KDQotLS0tLS0tLS0tDQpFcm5zdCBLZWx0aW5nDQpWUCBTYWxlcyBB\n" +
        "UEFDDQoNCmVybnN0LmtlbHRpbmdAb3Blbi14Y2hhbmdlLmNvbSAgU2t5cGU6IGVrZWx0aW5nDQpN\n" +
        "b2IgKzQ5IDE3NCAzNDQwIDU1MywgUGhvbmUgKzQ5IDQxMDEgLSA4MDg3IDI0LCBGYXggKzQ5IDQx\n" +
        "MDEgLSA4MDg3IDE4DQpXZWIgaHR0cDovL3d3dy5vcGVuLXhjaGFuZ2UuY29tDQotLS0tLS0tLS0t\n" +
        "DQpPcGVuLVhjaGFuZ2UgQUcsICBNYXhmZWxkc3RyLiA5LCA5MDQwOSBOw7xybmJlcmcsIEFtdHNn\n" +
        "ZXJpY2h0IE7DvHJuYmVyZyBIUkIgMjQ3MzgNClZvcnN0YW5kOiBSYWZhZWwgTGFndW5hIGRlIGxh\n" +
        "IFZlcmEsIEF1ZnNpY2h0c3JhdHN2b3JzaXR6ZW5kZXI6IFJpY2hhcmQgU2VpYnQNCg0KRXVyb3Bl\n" +
        "YW4gT2ZmaWNlOiBPcGVuLVhjaGFuZ2UgR21iSCwgTWFydGluc3RyLiA0MSwgRC01NzQ2MiBPbHBl\n" +
        "LCBHZXJtYW55DQpBbXRzZ2VyaWNodCBTaWVnZW4sIEhSQiA4NzE4LCBHZXNjaMOkZnRzZsO8aHJl\n" +
        "cjogRnJhbmsgSG9iZXJnLCBNYXJ0aW4gS2F1c3MNCi0tLS0tLS0tLS0NCg0KDQoNCi0tDQpIb2xn\n" +
        "ZXIgQWNodHppZ2VyDQpTZW5pb3IgU3lzdGVtIEVuZ2luZWVyDQpPcGVuLVhjaGFuZ2UgR21iSA0K\n" +
        "DQpQaG9uZTogKzQ5IDE3MiAyNDk0NTgxLCBGYXg6ICArNDkgOTExIDE4MCAxNDE5DQoNCi0tLS0t\n" +
        "LS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0t\n" +
        "LS0tLS0tLS0tLS0tLS0tLS0NCk9wZW4tWGNoYW5nZSBBRywgIE1heGZlbGRzdHIuIDksIDkwNDA5\n" +
        "IE7DvHJuYmVyZywgQW10c2dlcmljaHQgTsO8cm5iZXJnIEhSQiAyNDczOA0KVm9yc3RhbmQ6ICAg\n" +
        "IFJhZmFlbCBMYWd1bmEgZGUgbGEgVmVyYSwgQXVmc2ljaHRzcmF0c3ZvcnNpdHplbmRlcjogUmlj\n" +
        "aGFyZCBTZWlidA0KDQpFdXJvcGVhbiBPZmZpY2U6ICAgICAgICBPcGVuLVhjaGFuZ2UgR21iSCwg\n" +
        "TWFydGluc3RyLiA0MSwgRC01NzQ2MiBPbHBlLCBHZXJtYW55DQpBbXRzZ2VyaWNodCBTaWVnZW4s\n" +
        "IEhSQiA4NzE4LCBHZXNjaMOkZnRzZsO8aHJlcjogICAgICAgRnJhbmsgSG9iZXJnLCBNYXJ0aW4g\n" +
        "S2F1c3MNCg0KVVMgT2ZmaWNlOiAgICBPcGVuLVhjaGFuZ2UsIEluYy4sIDMwMyBTb3V0aCBCcm9h\n" +
        "ZHdheSwgVGFycnl0b3duLCBOZXcgWW9yayAxMDU5MQ0KLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0t\n" +
        "LS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLQ0K\n" +
        "DQpfX19fX19fX19fX19fX19fX19fX19fX19fX19fX19fXw0KRGlzY2xhaW1lcjogVGhlIGluZm9y\n" +
        "bWF0aW9uIGNvbnRhaW5lZCBpbiB0aGlzIGUtbWFpbCBhbmQgYXR0YWNobWVudHMgKGlmIGFueSkg\n" +
        "YXJlIHByaXZpbGVnZWQgYW5kIGNvbmZpZGVudGlhbCBhbmQgYXJlIGludGVuZGVkIGZvciB0aGUg\n" +
        "aW5kaXZpZHVhbChzKSBvciBlbnRpdHkoaWVzKSBuYW1lZCBpbiB0aGlzIGUtbWFpbC4gSWYgeW91\n" +
        "IGFyZSBub3QgdGhlIGludGVuZGVkIHJlY2lwaWVudCwgb3IgZW1wbG95ZWUgb3IgYWdlbnQsIHlv\n" +
        "dSBhcmUgaGVyZWJ5IG5vdGlmaWVkIHRoYXQgZGlzc2VtaW5hdGlvbiwgZGlzdHJpYnV0aW9uIG9y\n" +
        "IGNvcHlpbmcgb2YgdGhpcyBjb21tdW5pY2F0aW9uIG9yIGF0dGFjaG1lbnRzIHRoZXJlb2YgaXMg\n" +
        "c3RyaWN0bHkgcHJvaGliaXRlZC4gSUYgWU9VIFJFQ0VJVkVEIHRoaXMgY29tbXVuaWNhdGlvbiBp\n" +
        "biBlcnJvciwgcGxlYXNlIGltbWVkaWF0ZWx5IG5vdGlmeSB0aGUgc2VuZGVyIGFuZCBERUxFVEUg\n" +
        "dGhlIG9yaWdpbmFsIG1lc3NhZ2UgZnJvbSB0aGUgSW5ib3guDQo=\n" +
        "\n" +
        "--_000_412AE9CFB27D0D40B5A7FEEA6EEE2F440DB295F1E1vsr00a00802kr_\n" +
        "Content-Type: text/html; charset=\"utf-8\"\n" +
        "Content-Transfer-Encoding: base64\n" +
        "\n" +
        "PGh0bWwgeG1sbnM6dj0idXJuOnNjaGVtYXMtbWljcm9zb2Z0LWNvbTp2bWwiIHhtbG5zOm89InVy\n" +
        "bjpzY2hlbWFzLW1pY3Jvc29mdC1jb206b2ZmaWNlOm9mZmljZSIgeG1sbnM6dz0idXJuOnNjaGVt\n" +
        "YXMtbWljcm9zb2Z0LWNvbTpvZmZpY2U6d29yZCIgeG1sbnM6eD0idXJuOnNjaGVtYXMtbWljcm9z\n" +
        "b2Z0LWNvbTpvZmZpY2U6ZXhjZWwiIHhtbG5zOnA9InVybjpzY2hlbWFzLW1pY3Jvc29mdC1jb206\n" +
        "b2ZmaWNlOnBvd2VycG9pbnQiIHhtbG5zOmE9InVybjpzY2hlbWFzLW1pY3Jvc29mdC1jb206b2Zm\n" +
        "aWNlOmFjY2VzcyIgeG1sbnM6ZHQ9InV1aWQ6QzJGNDEwMTAtNjVCMy0xMWQxLUEyOUYtMDBBQTAw\n" +
        "QzE0ODgyIiB4bWxuczpzPSJ1dWlkOkJEQzZFM0YwLTZEQTMtMTFkMS1BMkEzLTAwQUEwMEMxNDg4\n" +
        "MiIgeG1sbnM6cnM9InVybjpzY2hlbWFzLW1pY3Jvc29mdC1jb206cm93c2V0IiB4bWxuczp6PSIj\n" +
        "Um93c2V0U2NoZW1hIiB4bWxuczpiPSJ1cm46c2NoZW1hcy1taWNyb3NvZnQtY29tOm9mZmljZTpw\n" +
        "dWJsaXNoZXIiIHhtbG5zOnNzPSJ1cm46c2NoZW1hcy1taWNyb3NvZnQtY29tOm9mZmljZTpzcHJl\n" +
        "YWRzaGVldCIgeG1sbnM6Yz0idXJuOnNjaGVtYXMtbWljcm9zb2Z0LWNvbTpvZmZpY2U6Y29tcG9u\n" +
        "ZW50OnNwcmVhZHNoZWV0IiB4bWxuczpvZGM9InVybjpzY2hlbWFzLW1pY3Jvc29mdC1jb206b2Zm\n" +
        "aWNlOm9kYyIgeG1sbnM6b2E9InVybjpzY2hlbWFzLW1pY3Jvc29mdC1jb206b2ZmaWNlOmFjdGl2\n" +
        "YXRpb24iIHhtbG5zOmh0bWw9Imh0dHA6Ly93d3cudzMub3JnL1RSL1JFQy1odG1sNDAiIHhtbG5z\n" +
        "OnE9Imh0dHA6Ly9zY2hlbWFzLnhtbHNvYXAub3JnL3NvYXAvZW52ZWxvcGUvIiB4bWxuczpydGM9\n" +
        "Imh0dHA6Ly9taWNyb3NvZnQuY29tL29mZmljZW5ldC9jb25mZXJlbmNpbmciIHhtbG5zOkQ9IkRB\n" +
        "VjoiIHhtbG5zOlJlcGw9Imh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vcmVwbC8iIHhtbG5z\n" +
        "Om10PSJodHRwOi8vc2NoZW1hcy5taWNyb3NvZnQuY29tL3NoYXJlcG9pbnQvc29hcC9tZWV0aW5n\n" +
        "cy8iIHhtbG5zOngyPSJodHRwOi8vc2NoZW1hcy5taWNyb3NvZnQuY29tL29mZmljZS9leGNlbC8y\n" +
        "MDAzL3htbCIgeG1sbnM6cHBkYT0iaHR0cDovL3d3dy5wYXNzcG9ydC5jb20vTmFtZVNwYWNlLnhz\n" +
        "ZCIgeG1sbnM6b2lzPSJodHRwOi8vc2NoZW1hcy5taWNyb3NvZnQuY29tL3NoYXJlcG9pbnQvc29h\n" +
        "cC9vaXMvIiB4bWxuczpkaXI9Imh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vc2hhcmVwb2lu\n" +
        "dC9zb2FwL2RpcmVjdG9yeS8iIHhtbG5zOmRzPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3ht\n" +
        "bGRzaWcjIiB4bWxuczpkc3A9Imh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vc2hhcmVwb2lu\n" +
        "dC9kc3AiIHhtbG5zOnVkYz0iaHR0cDovL3NjaGVtYXMubWljcm9zb2Z0LmNvbS9kYXRhL3VkYyIg\n" +
        "eG1sbnM6eHNkPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxL1hNTFNjaGVtYSIgeG1sbnM6c3ViPSJo\n" +
        "dHRwOi8vc2NoZW1hcy5taWNyb3NvZnQuY29tL3NoYXJlcG9pbnQvc29hcC8yMDAyLzEvYWxlcnRz\n" +
        "LyIgeG1sbnM6ZWM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMDQveG1sZW5jIyIgeG1sbnM6c3A9\n" +
        "Imh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vc2hhcmVwb2ludC8iIHhtbG5zOnNwcz0iaHR0\n" +
        "cDovL3NjaGVtYXMubWljcm9zb2Z0LmNvbS9zaGFyZXBvaW50L3NvYXAvIiB4bWxuczp4c2k9Imh0\n" +
        "dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiB4bWxuczp1ZGNzPSJodHRw\n" +
        "Oi8vc2NoZW1hcy5taWNyb3NvZnQuY29tL2RhdGEvdWRjL3NvYXAiIHhtbG5zOnVkY3hmPSJodHRw\n" +
        "Oi8vc2NoZW1hcy5taWNyb3NvZnQuY29tL2RhdGEvdWRjL3htbGZpbGUiIHhtbG5zOnVkY3AycD0i\n" +
        "aHR0cDovL3NjaGVtYXMubWljcm9zb2Z0LmNvbS9kYXRhL3VkYy9wYXJ0dG9wYXJ0IiB4bWxuczp3\n" +
        "Zj0iaHR0cDovL3NjaGVtYXMubWljcm9zb2Z0LmNvbS9zaGFyZXBvaW50L3NvYXAvd29ya2Zsb3cv\n" +
        "IiB4bWxuczpkc3NzPSJodHRwOi8vc2NoZW1hcy5taWNyb3NvZnQuY29tL29mZmljZS8yMDA2L2Rp\n" +
        "Z3NpZy1zZXR1cCIgeG1sbnM6ZHNzaT0iaHR0cDovL3NjaGVtYXMubWljcm9zb2Z0LmNvbS9vZmZp\n" +
        "Y2UvMjAwNi9kaWdzaWciIHhtbG5zOm1kc3NpPSJodHRwOi8vc2NoZW1hcy5vcGVueG1sZm9ybWF0\n" +
        "cy5vcmcvcGFja2FnZS8yMDA2L2RpZ2l0YWwtc2lnbmF0dXJlIiB4bWxuczptdmVyPSJodHRwOi8v\n" +
        "c2NoZW1hcy5vcGVueG1sZm9ybWF0cy5vcmcvbWFya3VwLWNvbXBhdGliaWxpdHkvMjAwNiIgeG1s\n" +
        "bnM6bT0iaHR0cDovL3NjaGVtYXMubWljcm9zb2Z0LmNvbS9vZmZpY2UvMjAwNC8xMi9vbW1sIiB4\n" +
        "bWxuczptcmVscz0iaHR0cDovL3NjaGVtYXMub3BlbnhtbGZvcm1hdHMub3JnL3BhY2thZ2UvMjAw\n" +
        "Ni9yZWxhdGlvbnNoaXBzIiB4bWxuczpzcHdwPSJodHRwOi8vbWljcm9zb2Z0LmNvbS9zaGFyZXBv\n" +
        "aW50L3dlYnBhcnRwYWdlcyIgeG1sbnM6ZXgxMnQ9Imh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5j\n" +
        "b20vZXhjaGFuZ2Uvc2VydmljZXMvMjAwNi90eXBlcyIgeG1sbnM6ZXgxMm09Imh0dHA6Ly9zY2hl\n" +
        "bWFzLm1pY3Jvc29mdC5jb20vZXhjaGFuZ2Uvc2VydmljZXMvMjAwNi9tZXNzYWdlcyIgeG1sbnM6\n" +
        "cHB0c2w9Imh0dHA6Ly9zY2hlbWFzLm1pY3Jvc29mdC5jb20vc2hhcmVwb2ludC9zb2FwL1NsaWRl\n" +
        "TGlicmFyeS8iIHhtbG5zOnNwc2w9Imh0dHA6Ly9taWNyb3NvZnQuY29tL3dlYnNlcnZpY2VzL1No\n" +
        "YXJlUG9pbnRQb3J0YWxTZXJ2ZXIvUHVibGlzaGVkTGlua3NTZXJ2aWNlIiB4bWxuczpaPSJ1cm46\n" +
        "c2NoZW1hcy1taWNyb3NvZnQtY29tOiIgeG1sbnM6c3Q9IiYjMTsiIHhtbG5zPSJodHRwOi8vd3d3\n" +
        "LnczLm9yZy9UUi9SRUMtaHRtbDQwIj4NCjxoZWFkPg0KPG1ldGEgaHR0cC1lcXVpdj0iQ29udGVu\n" +
        "dC1UeXBlIiBjb250ZW50PSJ0ZXh0L2h0bWw7IGNoYXJzZXQ9dXRmLTgiPg0KPG1ldGEgbmFtZT0i\n" +
        "R2VuZXJhdG9yIiBjb250ZW50PSJNaWNyb3NvZnQgV29yZCAxMiAoZmlsdGVyZWQgbWVkaXVtKSI+\n" +
        "DQo8c3R5bGU+DQo8IS0tDQogLyogRm9udCBEZWZpbml0aW9ucyAqLw0KIEBmb250LWZhY2UNCgl7\n" +
        "Zm9udC1mYW1pbHk6Q2FsaWJyaTsNCglwYW5vc2UtMToyIDE1IDUgMiAyIDIgNCAzIDIgNDt9DQpA\n" +
        "Zm9udC1mYWNlDQoJe2ZvbnQtZmFtaWx5OlRhaG9tYTsNCglwYW5vc2UtMToyIDExIDYgNCAzIDUg\n" +
        "NCA0IDIgNDt9DQpAZm9udC1mYWNlDQoJe2ZvbnQtZmFtaWx5OiJCb29rbWFuIE9sZCBTdHlsZSI7\n" +
        "DQoJcGFub3NlLTE6MiA1IDYgNCA1IDUgNSAyIDIgNDt9DQogLyogU3R5bGUgRGVmaW5pdGlvbnMg\n" +
        "Ki8NCiBwLk1zb05vcm1hbCwgbGkuTXNvTm9ybWFsLCBkaXYuTXNvTm9ybWFsDQoJe21hcmdpbjow\n" +
        "aW47DQoJbWFyZ2luLWJvdHRvbTouMDAwMXB0Ow0KCWZvbnQtc2l6ZToxMi4wcHQ7DQoJZm9udC1m\n" +
        "YW1pbHk6IlRpbWVzIE5ldyBSb21hbiIsInNlcmlmIjt9DQphOmxpbmssIHNwYW4uTXNvSHlwZXJs\n" +
        "aW5rDQoJe21zby1zdHlsZS1wcmlvcml0eTo5OTsNCgljb2xvcjpibHVlOw0KCXRleHQtZGVjb3Jh\n" +
        "dGlvbjp1bmRlcmxpbmU7fQ0KYTp2aXNpdGVkLCBzcGFuLk1zb0h5cGVybGlua0ZvbGxvd2VkDQoJ\n" +
        "e21zby1zdHlsZS1wcmlvcml0eTo5OTsNCgljb2xvcjpwdXJwbGU7DQoJdGV4dC1kZWNvcmF0aW9u\n" +
        "OnVuZGVybGluZTt9DQpwDQoJe21zby1zdHlsZS1wcmlvcml0eTo5OTsNCgltc28tbWFyZ2luLXRv\n" +
        "cC1hbHQ6YXV0bzsNCgltYXJnaW4tcmlnaHQ6MGluOw0KCW1zby1tYXJnaW4tYm90dG9tLWFsdDph\n" +
        "dXRvOw0KCW1hcmdpbi1sZWZ0OjBpbjsNCglmb250LXNpemU6MTIuMHB0Ow0KCWZvbnQtZmFtaWx5\n" +
        "OiJUaW1lcyBOZXcgUm9tYW4iLCJzZXJpZiI7fQ0KcC5Nc29MaXN0UGFyYWdyYXBoLCBsaS5Nc29M\n" +
        "aXN0UGFyYWdyYXBoLCBkaXYuTXNvTGlzdFBhcmFncmFwaA0KCXttc28tc3R5bGUtcHJpb3JpdHk6\n" +
        "MzQ7DQoJbWFyZ2luLXRvcDowaW47DQoJbWFyZ2luLXJpZ2h0OjBpbjsNCgltYXJnaW4tYm90dG9t\n" +
        "OjBpbjsNCgltYXJnaW4tbGVmdDouNWluOw0KCW1hcmdpbi1ib3R0b206LjAwMDFwdDsNCglmb250\n" +
        "LXNpemU6MTIuMHB0Ow0KCWZvbnQtZmFtaWx5OiJUaW1lcyBOZXcgUm9tYW4iLCJzZXJpZiI7fQ0K\n" +
        "c3Bhbi5FbWFpbFN0eWxlMTgNCgl7bXNvLXN0eWxlLXR5cGU6cGVyc29uYWwtcmVwbHk7DQoJZm9u\n" +
        "dC1mYW1pbHk6IkJvb2ttYW4gT2xkIFN0eWxlIiwic2VyaWYiOw0KCWNvbG9yOndpbmRvd3RleHQ7\n" +
        "DQoJZm9udC13ZWlnaHQ6bm9ybWFsOw0KCWZvbnQtc3R5bGU6bm9ybWFsO30NCi5Nc29DaHBEZWZh\n" +
        "dWx0DQoJe21zby1zdHlsZS10eXBlOmV4cG9ydC1vbmx5Ow0KCWZvbnQtc2l6ZToxMC4wcHQ7fQ0K\n" +
        "QHBhZ2UgU2VjdGlvbjENCgl7c2l6ZTo4LjVpbiAxMS4waW47DQoJbWFyZ2luOjEuMGluIDEuMGlu\n" +
        "IDEuMGluIDEuMGluO30NCmRpdi5TZWN0aW9uMQ0KCXtwYWdlOlNlY3Rpb24xO30NCiAvKiBMaXN0\n" +
        "IERlZmluaXRpb25zICovDQogQGxpc3QgbDANCgl7bXNvLWxpc3QtaWQ6NDg4NjY4OTcyOw0KCW1z\n" +
        "by1saXN0LXR5cGU6aHlicmlkOw0KCW1zby1saXN0LXRlbXBsYXRlLWlkczoyMDIwNTEzOTE0IDY3\n" +
        "Njk4NzEzIDY3Njk4NzEzIDY3Njk4NzE1IDY3Njk4NzAzIDY3Njk4NzEzIDY3Njk4NzE1IDY3Njk4\n" +
        "NzAzIDY3Njk4NzEzIDY3Njk4NzE1O30NCkBsaXN0IGwwOmxldmVsMQ0KCXttc28tbGV2ZWwtbnVt\n" +
        "YmVyLWZvcm1hdDphbHBoYS1sb3dlcjsNCgltc28tbGV2ZWwtdGFiLXN0b3A6bm9uZTsNCgltc28t\n" +
        "bGV2ZWwtbnVtYmVyLXBvc2l0aW9uOmxlZnQ7DQoJdGV4dC1pbmRlbnQ6LS4yNWluO30NCm9sDQoJ\n" +
        "e21hcmdpbi1ib3R0b206MGluO30NCnVsDQoJe21hcmdpbi1ib3R0b206MGluO30NCi0tPg0KPC9z\n" +
        "dHlsZT48IS0tW2lmIGd0ZSBtc28gOV0+PHhtbD4NCiA8bzpzaGFwZWRlZmF1bHRzIHY6ZXh0PSJl\n" +
        "ZGl0IiBzcGlkbWF4PSIxMDI2IiAvPg0KPC94bWw+PCFbZW5kaWZdLS0+PCEtLVtpZiBndGUgbXNv\n" +
        "IDldPjx4bWw+DQogPG86c2hhcGVsYXlvdXQgdjpleHQ9ImVkaXQiPg0KICA8bzppZG1hcCB2OmV4\n" +
        "dD0iZWRpdCIgZGF0YT0iMSIgLz4NCiA8L286c2hhcGVsYXlvdXQ+PC94bWw+PCFbZW5kaWZdLS0+\n" +
        "DQo8L2hlYWQ+DQo8Ym9keSBsYW5nPSJFTi1VUyIgbGluaz0iYmx1ZSIgdmxpbms9InB1cnBsZSI+\n" +
        "DQo8ZGl2IGNsYXNzPSJTZWN0aW9uMSI+DQo8cCBjbGFzcz0iTXNvTm9ybWFsIj48c3BhbiBzdHls\n" +
        "ZT0iZm9udC1zaXplOjEwLjBwdDtmb250LWZhbWlseTomcXVvdDtCb29rbWFuIE9sZCBTdHlsZSZx\n" +
        "dW90OywmcXVvdDtzZXJpZiZxdW90OyI+SGkgSG9sZ2VyLDxvOnA+PC9vOnA+PC9zcGFuPjwvcD4N\n" +
        "CjxwIGNsYXNzPSJNc29Ob3JtYWwiPjxzcGFuIHN0eWxlPSJmb250LXNpemU6MTAuMHB0O2ZvbnQt\n" +
        "ZmFtaWx5OiZxdW90O0Jvb2ttYW4gT2xkIFN0eWxlJnF1b3Q7LCZxdW90O3NlcmlmJnF1b3Q7Ij48\n" +
        "bzpwPiZuYnNwOzwvbzpwPjwvc3Bhbj48L3A+DQo8cCBjbGFzcz0iTXNvTm9ybWFsIj48c3BhbiBz\n" +
        "dHlsZT0iZm9udC1zaXplOjEwLjBwdDtmb250LWZhbWlseTomcXVvdDtCb29rbWFuIE9sZCBTdHls\n" +
        "ZSZxdW90OywmcXVvdDtzZXJpZiZxdW90OyI+VGhhbmtzIGZvciB0aGUgaW5mb3JtYXRpb24uLiBT\n" +
        "aGFyaW5nIGEgc2FtcGxlIEJPTSBhbmQgYXBwcmVjaWF0ZSBpZiB5b3UgY291bGQgcHJvdmlkZSBk\n" +
        "ZXRhaWxzIGluIHRoZSBzYW1lIGZvcm1hdC4uJm5ic3A7IEFwcHJlY2lhdGUgaWYgeW91IHByb3Zp\n" +
        "c2lvbiBkZXRhaWxzIGZvciB0aGUgYmVsb3cNCiBtZW50aW9uZWQgcG9pbnRzICZuYnNwOyZuYnNw\n" +
        "OzxvOnA+PC9vOnA+PC9zcGFuPjwvcD4NCjxwIGNsYXNzPSJNc29Ob3JtYWwiPjxzcGFuIHN0eWxl\n" +
        "PSJmb250LXNpemU6MTAuMHB0O2ZvbnQtZmFtaWx5OiZxdW90O0Jvb2ttYW4gT2xkIFN0eWxlJnF1\n" +
        "b3Q7LCZxdW90O3NlcmlmJnF1b3Q7Ij48bzpwPiZuYnNwOzwvbzpwPjwvc3Bhbj48L3A+DQo8cCBj\n" +
        "bGFzcz0iTXNvTGlzdFBhcmFncmFwaCIgc3R5bGU9InRleHQtaW5kZW50Oi0uMjVpbjttc28tbGlz\n" +
        "dDpsMCBsZXZlbDEgbGZvMSI+PCFbaWYgIXN1cHBvcnRMaXN0c10+PHNwYW4gc3R5bGU9ImZvbnQt\n" +
        "c2l6ZToxMC4wcHQ7Zm9udC1mYW1pbHk6JnF1b3Q7Qm9va21hbiBPbGQgU3R5bGUmcXVvdDssJnF1\n" +
        "b3Q7c2VyaWYmcXVvdDsiPjxzcGFuIHN0eWxlPSJtc28tbGlzdDpJZ25vcmUiPmEuPHNwYW4gc3R5\n" +
        "bGU9ImZvbnQ6Ny4wcHQgJnF1b3Q7VGltZXMgTmV3IFJvbWFuJnF1b3Q7Ij4mbmJzcDsmbmJzcDsm\n" +
        "bmJzcDsNCjwvc3Bhbj48L3NwYW4+PC9zcGFuPjwhW2VuZGlmXT48c3BhbiBzdHlsZT0iZm9udC1z\n" +
        "aXplOjEwLjBwdDtmb250LWZhbWlseTomcXVvdDtCb29rbWFuIE9sZCBTdHlsZSZxdW90OywmcXVv\n" +
        "dDtzZXJpZiZxdW90OyI+UmVjb21tZW5kIHRoZSBQcm92aXNpb25pbmcgU2VydmVyIChBcGFydCBm\n" +
        "cm9tIFBhcmFsbGVscykgdGhhdCB3b3VsZCB3b3JrIHdpdGggT3BlbiBFeGNoYW5nZQ0KPG86cD48\n" +
        "L286cD48L3NwYW4+PC9wPg0KPHAgY2xhc3M9Ik1zb0xpc3RQYXJhZ3JhcGgiIHN0eWxlPSJ0ZXh0\n" +
        "LWluZGVudDotLjI1aW47bXNvLWxpc3Q6bDAgbGV2ZWwxIGxmbzEiPjwhW2lmICFzdXBwb3J0TGlz\n" +
        "dHNdPjxzcGFuIHN0eWxlPSJmb250LXNpemU6MTAuMHB0O2ZvbnQtZmFtaWx5OiZxdW90O0Jvb2tt\n" +
        "YW4gT2xkIFN0eWxlJnF1b3Q7LCZxdW90O3NlcmlmJnF1b3Q7Ij48c3BhbiBzdHlsZT0ibXNvLWxp\n" +
        "c3Q6SWdub3JlIj5iLjxzcGFuIHN0eWxlPSJmb250OjcuMHB0ICZxdW90O1RpbWVzIE5ldyBSb21h\n" +
        "biZxdW90OyI+Jm5ic3A7Jm5ic3A7Jm5ic3A7DQo8L3NwYW4+PC9zcGFuPjwvc3Bhbj48IVtlbmRp\n" +
        "Zl0+PHNwYW4gc3R5bGU9ImZvbnQtc2l6ZToxMC4wcHQ7Zm9udC1mYW1pbHk6JnF1b3Q7Qm9va21h\n" +
        "biBPbGQgU3R5bGUmcXVvdDssJnF1b3Q7c2VyaWYmcXVvdDsiPlJlY29tbWVuZCBBbnRpLVZpcnVz\n" +
        "IC8gQW50aS1TcGFtICZuYnNwOzxvOnA+PC9vOnA+PC9zcGFuPjwvcD4NCjxwIGNsYXNzPSJNc29M\n" +
        "aXN0UGFyYWdyYXBoIiBzdHlsZT0idGV4dC1pbmRlbnQ6LS4yNWluO21zby1saXN0OmwwIGxldmVs\n" +
        "MSBsZm8xIj48IVtpZiAhc3VwcG9ydExpc3RzXT48c3BhbiBzdHlsZT0iZm9udC1zaXplOjEwLjBw\n" +
        "dDtmb250LWZhbWlseTomcXVvdDtCb29rbWFuIE9sZCBTdHlsZSZxdW90OywmcXVvdDtzZXJpZiZx\n" +
        "dW90OyI+PHNwYW4gc3R5bGU9Im1zby1saXN0Oklnbm9yZSI+Yy48c3BhbiBzdHlsZT0iZm9udDo3\n" +
        "LjBwdCAmcXVvdDtUaW1lcyBOZXcgUm9tYW4mcXVvdDsiPiZuYnNwOyZuYnNwOyZuYnNwOyZuYnNw\n" +
        "Ow0KPC9zcGFuPjwvc3Bhbj48L3NwYW4+PCFbZW5kaWZdPjxzcGFuIHN0eWxlPSJmb250LXNpemU6\n" +
        "MTAuMHB0O2ZvbnQtZmFtaWx5OiZxdW90O0Jvb2ttYW4gT2xkIFN0eWxlJnF1b3Q7LCZxdW90O3Nl\n" +
        "cmlmJnF1b3Q7Ij5SZWNvbW1lbmQgQmFjay11cCB0b29sIGZvciB0aGUgRW1haWwvQXJjaGl2YWw8\n" +
        "bzpwPjwvbzpwPjwvc3Bhbj48L3A+DQo8cCBjbGFzcz0iTXNvTGlzdFBhcmFncmFwaCIgc3R5bGU9\n" +
        "InRleHQtaW5kZW50Oi0uMjVpbjttc28tbGlzdDpsMCBsZXZlbDEgbGZvMSI+PCFbaWYgIXN1cHBv\n" +
        "cnRMaXN0c10+PHNwYW4gc3R5bGU9ImZvbnQtc2l6ZToxMC4wcHQ7Zm9udC1mYW1pbHk6JnF1b3Q7\n" +
        "Qm9va21hbiBPbGQgU3R5bGUmcXVvdDssJnF1b3Q7c2VyaWYmcXVvdDsiPjxzcGFuIHN0eWxlPSJt\n" +
        "c28tbGlzdDpJZ25vcmUiPmQuPHNwYW4gc3R5bGU9ImZvbnQ6Ny4wcHQgJnF1b3Q7VGltZXMgTmV3\n" +
        "IFJvbWFuJnF1b3Q7Ij4mbmJzcDsmbmJzcDsmbmJzcDsNCjwvc3Bhbj48L3NwYW4+PC9zcGFuPjwh\n" +
        "W2VuZGlmXT48c3BhbiBzdHlsZT0iZm9udC1zaXplOjEwLjBwdDtmb250LWZhbWlseTomcXVvdDtC\n" +
        "b29rbWFuIE9sZCBTdHlsZSZxdW90OywmcXVvdDtzZXJpZiZxdW90OyI+Q2FuIHRoZSBzZXJ2aWNl\n" +
        "cyBsaWtlICZuYnNwO0JsYWNrLWJlcnJ5ICZuYnNwOy8gQWN0aXZlLVN5bmMgYmUgdXNlZCA/Pw0K\n" +
        "PG86cD48L286cD48L3NwYW4+PC9wPg0KPHAgY2xhc3M9Ik1zb05vcm1hbCI+PHNwYW4gc3R5bGU9\n" +
        "ImZvbnQtc2l6ZToxMC4wcHQ7Zm9udC1mYW1pbHk6JnF1b3Q7Qm9va21hbiBPbGQgU3R5bGUmcXVv\n" +
        "dDssJnF1b3Q7c2VyaWYmcXVvdDsiPjxvOnA+Jm5ic3A7PC9vOnA+PC9zcGFuPjwvcD4NCjxwIGNs\n" +
        "YXNzPSJNc29Ob3JtYWwiPjxzcGFuIHN0eWxlPSJmb250LXNpemU6MTAuMHB0O2ZvbnQtZmFtaWx5\n" +
        "OiZxdW90O0Jvb2ttYW4gT2xkIFN0eWxlJnF1b3Q7LCZxdW90O3NlcmlmJnF1b3Q7Ij5UaGFua3Mg\n" +
        "YW5kIFJlZ2FyZHM8bzpwPjwvbzpwPjwvc3Bhbj48L3A+DQo8cCBjbGFzcz0iTXNvTm9ybWFsIj48\n" +
        "c3BhbiBzdHlsZT0iZm9udC1zaXplOjEwLjBwdDtmb250LWZhbWlseTomcXVvdDtCb29rbWFuIE9s\n" +
        "ZCBTdHlsZSZxdW90OywmcXVvdDtzZXJpZiZxdW90OyI+PG86cD4mbmJzcDs8L286cD48L3NwYW4+\n" +
        "PC9wPg0KPHAgY2xhc3M9Ik1zb05vcm1hbCI+PHNwYW4gc3R5bGU9ImZvbnQtc2l6ZToxMC4wcHQ7\n" +
        "Zm9udC1mYW1pbHk6JnF1b3Q7Qm9va21hbiBPbGQgU3R5bGUmcXVvdDssJnF1b3Q7c2VyaWYmcXVv\n" +
        "dDsiPkdva3VsbmF0aA0KPG86cD48L286cD48L3NwYW4+PC9wPg0KPHAgY2xhc3M9Ik1zb05vcm1h\n" +
        "bCI+PHNwYW4gc3R5bGU9ImZvbnQtc2l6ZToxMC4wcHQ7Zm9udC1mYW1pbHk6JnF1b3Q7Qm9va21h\n" +
        "biBPbGQgU3R5bGUmcXVvdDssJnF1b3Q7c2VyaWYmcXVvdDsiPjxvOnA+Jm5ic3A7PC9vOnA+PC9z\n" +
        "cGFuPjwvcD4NCjxwIGNsYXNzPSJNc29Ob3JtYWwiPjxzcGFuIHN0eWxlPSJmb250LXNpemU6MTAu\n" +
        "MHB0O2ZvbnQtZmFtaWx5OiZxdW90O0Jvb2ttYW4gT2xkIFN0eWxlJnF1b3Q7LCZxdW90O3Nlcmlm\n" +
        "JnF1b3Q7Ij48bzpwPiZuYnNwOzwvbzpwPjwvc3Bhbj48L3A+DQo8ZGl2Pg0KPGRpdiBzdHlsZT0i\n" +
        "Ym9yZGVyOm5vbmU7Ym9yZGVyLXRvcDpzb2xpZCAjQjVDNERGIDEuMHB0O3BhZGRpbmc6My4wcHQg\n" +
        "MGluIDBpbiAwaW4iPg0KPHAgY2xhc3M9Ik1zb05vcm1hbCI+PGI+PHNwYW4gc3R5bGU9ImZvbnQt\n" +
        "c2l6ZToxMC4wcHQ7Zm9udC1mYW1pbHk6JnF1b3Q7VGFob21hJnF1b3Q7LCZxdW90O3NhbnMtc2Vy\n" +
        "aWYmcXVvdDsiPkZyb206PC9zcGFuPjwvYj48c3BhbiBzdHlsZT0iZm9udC1zaXplOjEwLjBwdDtm\n" +
        "b250LWZhbWlseTomcXVvdDtUYWhvbWEmcXVvdDssJnF1b3Q7c2Fucy1zZXJpZiZxdW90OyI+IEFj\n" +
        "aHR6aWdlciwgSG9sZ2VyIFttYWlsdG86aG9sZ2VyLmFjaHR6aWdlckBvcGVuLXhjaGFuZ2UuY29t\n" +
        "XQ0KPGJyPg0KPGI+U2VudDo8L2I+IFRodXJzZGF5LCBPY3RvYmVyIDI5LCAyMDA5IDg6MzYgUE08\n" +
        "YnI+DQo8Yj5Ubzo8L2I+IEdva3VsbmF0aCBDLjsgS2VsdGluZywgRXJuc3Q8YnI+DQo8Yj5TdWJq\n" +
        "ZWN0OjwvYj4gUmU6IEludHJvZHVjdGlvb24gS3J5cHRvcyBOZXR3b3JrczxvOnA+PC9vOnA+PC9z\n" +
        "cGFuPjwvcD4NCjwvZGl2Pg0KPC9kaXY+DQo8cCBjbGFzcz0iTXNvTm9ybWFsIj48bzpwPiZuYnNw\n" +
        "OzwvbzpwPjwvcD4NCjxwIHN0eWxlPSJtYXJnaW46MGluO21hcmdpbi1ib3R0b206LjAwMDFwdCI+\n" +
        "SGVsbG8gR29rdWxuYXRoLDxvOnA+PC9vOnA+PC9wPg0KPHAgc3R5bGU9Im1hcmdpbjowaW47bWFy\n" +
        "Z2luLWJvdHRvbTouMDAwMXB0Ij4mbmJzcDs8bzpwPjwvbzpwPjwvcD4NCjxkaXYgc3R5bGU9Im1h\n" +
        "cmdpbi10b3A6My43NXB0O21hcmdpbi1ib3R0b206My43NXB0Ij4NCjxwIGNsYXNzPSJNc29Ob3Jt\n" +
        "YWwiPjxicj4NCiZxdW90O0tlbHRpbmcsIEVybnN0JnF1b3Q7ICZsdDtlcm5zdC5rZWx0aW5nQG9w\n" +
        "ZW4teGNoYW5nZS5jb20mZ3Q7IGhhdCBhbSAyOS4gT2t0b2JlciAyMDA5IHVtIDExOjAwIGdlc2No\n" +
        "cmllYmVuOjxicj4NCjxicj4NCjxicj4NCjxvOnA+PC9vOnA+PC9wPg0KPHAgc3R5bGU9Im1hcmdp\n" +
        "bjowaW47bWFyZ2luLWJvdHRvbTouMDAwMXB0Ij5IZWxsbyBHb2t1bG5hdGgsPG86cD48L286cD48\n" +
        "L3A+DQo8cCBzdHlsZT0ibWFyZ2luOjBpbjttYXJnaW4tYm90dG9tOi4wMDAxcHQiPiZuYnNwOzxv\n" +
        "OnA+PC9vOnA+PC9wPg0KPHAgc3R5bGU9Im1hcmdpbjowaW47bWFyZ2luLWJvdHRvbTouMDAwMXB0\n" +
        "Ij5sZXQgbWUgaW50cm9kdWNlIEhvbGdlci4gSG9sZ2VyIGlzIHdvcmtpbmcgZm9yIFN0ZXBoYW7C\n" +
        "tHMgUHJvZmVzc2lvbmFsIFNlcnZpY2VzIHRlYW0sIGFuZCB3aWxsIGJlIHdvcmtpbmcgd2l0aCB5\n" +
        "b3Ugb24gdGhlIG9wcG9ydHVuaXR5LiBIZSB3aWxsIHNlbmQgeW91IHRoaXMgYWZ0ZXJub29uIGEg\n" +
        "ZHJhZnQgcHJvcG9zYWwsIHNvIHdlIGNhbiBzdGFydCB0aGUgZGlzY3Vzc2lvbg0KIHdpdGggeW91\n" +
        "ciBwcm9zcGVjdC48bzpwPjwvbzpwPjwvcD4NCjxwIHN0eWxlPSJtYXJnaW46MGluO21hcmdpbi1i\n" +
        "b3R0b206LjAwMDFwdCI+Jm5ic3A7PG86cD48L286cD48L3A+DQo8L2Rpdj4NCjxwIHN0eWxlPSJt\n" +
        "YXJnaW46MGluO21hcmdpbi1ib3R0b206LjAwMDFwdCI+Jm5ic3A7PG86cD48L286cD48L3A+DQo8\n" +
        "cCBzdHlsZT0ibWFyZ2luOjBpbjttYXJnaW4tYm90dG9tOi4wMDAxcHQiPkVybnN0IGFza2VkIG1l\n" +
        "IHRvIGdpdmUgYSByb3VnaCBvdmVydmlldyBmb3Igd2hhdCBpcyBuZWVkZWQgdG8gc2V0IHVwIGEg\n" +
        "bmV3IGluZnJhc3RydWN0dXJlIGZvcjxvOnA+PC9vOnA+PC9wPg0KPHAgc3R5bGU9Im1hcmdpbjow\n" +
        "aW47bWFyZ2luLWJvdHRvbTouMDAwMXB0Ij41MC4wMDAgdXNlcnMuIEluIGZhY3QgdGhpcyBkZXBl\n" +
        "bmRzIG9uIHRoZSB3b3JrbG9hZCBnZW5lcmF0ZWQgYnkgdGhlbS4gRm9yIGV4YW1wbGUgYSBDb21w\n" +
        "YW55PG86cD48L286cD48L3A+DQo8cCBzdHlsZT0ibWFyZ2luOjBpbjttYXJnaW4tYm90dG9tOi4w\n" +
        "MDAxcHQiPndpdGggNTAuMDAwIGVtcGxveWVlcyB3aWxsIGhhdmUgbW9yZSBjb25jdXJyZW50IHVz\n" +
        "ZXJzIHRoYW4gYSBob3N0ZXIgc2VydmluZyBidXNpbmVzcy9wcml2YXRlPG86cD48L286cD48L3A+\n" +
        "DQo8cCBzdHlsZT0ibWFyZ2luOjBpbjttYXJnaW4tYm90dG9tOi4wMDAxcHQiPmN1c3RvbWVycyBp\n" +
        "biBkaWZmZXJlbnQgdGltZXpvbmVzLjxvOnA+PC9vOnA+PC9wPg0KPHAgc3R5bGU9Im1hcmdpbjow\n" +
        "aW47bWFyZ2luLWJvdHRvbTouMDAwMXB0Ij4mbmJzcDs8bzpwPjwvbzpwPjwvcD4NCjxwIHN0eWxl\n" +
        "PSJtYXJnaW46MGluO21hcmdpbi1ib3R0b206LjAwMDFwdCI+QXMgc3RhcnQgaSB3b3VsZCBnbyBm\n" +
        "b3Igd2hhdCB3ZSBoYXZlIGluIGNoYXB0ZXIgMi4zIG9mIG91ciBzaXppbmcgZG9jdW1lbnQgZm9y\n" +
        "IHRoZSBPcGVuLVhjaGFuZ2U8bzpwPjwvbzpwPjwvcD4NCjxwIHN0eWxlPSJtYXJnaW46MGluO21h\n" +
        "cmdpbi1ib3R0b206LjAwMDFwdCI+cGFydDo8bzpwPjwvbzpwPjwvcD4NCjxwIHN0eWxlPSJtYXJn\n" +
        "aW46MGluO21hcmdpbi1ib3R0b206LjAwMDFwdCI+Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5i\n" +
        "c3A7Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A7\n" +
        "Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A7ICMgUkFNIENQVSBDb3Jl\n" +
        "czxicj4NCk9YIEFwcGxpY2F0aW9uIHNlcnZlciAyIDhHQiA0PGJyPg0KTXlzcWwgc2VydmVycyZu\n" +
        "YnNwOyZuYnNwOyZuYnNwOyZuYnNwOyZuYnNwOyZuYnNwOyZuYnNwOyZuYnNwOyAyIDRHQiAyPGJy\n" +
        "Pg0KQXBhY2hlIHNlcnZlcnMmbmJzcDsmbmJzcDsmbmJzcDsmbmJzcDsmbmJzcDsmbmJzcDsmbmJz\n" +
        "cDsgMDxicj4NCkZpbGUgc3RvcmUgU2VydmVycyZuYnNwOyZuYnNwOyZuYnNwOyAxPGJyPg0KTG9h\n" +
        "ZCBCYWxhbmNlciZuYnNwOyZuYnNwOyZuYnNwOyZuYnNwOyZuYnNwOyZuYnNwOyZuYnNwOyZuYnNw\n" +
        "OyAxPG86cD48L286cD48L3A+DQo8cCBzdHlsZT0ibWFyZ2luOjBpbjttYXJnaW4tYm90dG9tOi4w\n" +
        "MDAxcHQiPiZuYnNwOzxvOnA+PC9vOnA+PC9wPg0KPHAgc3R5bGU9Im1hcmdpbjowaW47bWFyZ2lu\n" +
        "LWJvdHRvbTouMDAwMXB0Ij5Gb3IgVGhlIGltYXAgc2VydmVyLCB0aGUgY2FsY3VsYXRpb24gaXMg\n" +
        "dG8gaGF2ZSBhdCBtYXhpbXVtIDM1MDAgY29uY3VycmVudCBzZXNzaW9ucyBvbiBhIG1hY2hpbmU8\n" +
        "bzpwPjwvbzpwPjwvcD4NCjxwIHN0eWxlPSJtYXJnaW46MGluO21hcmdpbi1ib3R0b206LjAwMDFw\n" +
        "dCI+ZnJvbSB0aGUgc2FtZSBzaXplIGFzIHRoZSBBcHBsaWNhdGlvbiBzZXJ2ZXIgYWJvdmUgYW5k\n" +
        "IGEgc2VwYXJhdGUgTG9hZCBiYWxhbmNlci4gRm9yIGJlc3Q8bzpwPjwvbzpwPjwvcD4NCjxwIHN0\n" +
        "eWxlPSJtYXJnaW46MGluO21hcmdpbi1ib3R0b206LjAwMDFwdCI+c2NhbGFiaWxpdHkgd2UgcmVj\n" +
        "b21tZW5kIGRvdmVjb3QgYmFzZWQgb24gd2hhdCB3ZSBoYXZlIGhlYXJkIGZyb20gb3VyIGN1c3Rv\n" +
        "bWVycy4gVGhlIHNtcHQ8bzpwPjwvbzpwPjwvcD4NCjxwIHN0eWxlPSJtYXJnaW46MGluO21hcmdp\n" +
        "bi1ib3R0b206LjAwMDFwdCI+c2VydmljZSAocG9zZml4KSBjYW4gcnVuIG9uIHRoZSBzYW1lIGhv\n" +
        "c3RzLCB0b28uPG86cD48L286cD48L3A+DQo8cCBzdHlsZT0ibWFyZ2luOjBpbjttYXJnaW4tYm90\n" +
        "dG9tOi4wMDAxcHQiPiZuYnNwOzxvOnA+PC9vOnA+PC9wPg0KPHAgc3R5bGU9Im1hcmdpbjowaW47\n" +
        "bWFyZ2luLWJvdHRvbTouMDAwMXB0Ij5UaGUgZmlsZSBzdG9yZSBhbmQgaW1hcCBzcGFjZSBzaG91\n" +
        "bGQgYmUgb24gTkZTLCB0byBiZSBvbiB0aGUgc2F2ZSBzaWRlLCBhIGNvbW1lcmNpYWwgTkZTIFNl\n" +
        "cnZlcjxvOnA+PC9vOnA+PC9wPg0KPHAgc3R5bGU9Im1hcmdpbjowaW47bWFyZ2luLWJvdHRvbTou\n" +
        "MDAwMXB0Ij5saWtlIHByb3ZpZGVkIGJ5IG5ldGFwcCBzaG91bGQgYmUgdXNlZC4gQXQgbGVhc3Qg\n" +
        "aWYgdGhlcmUgYXJlIG1pbGxpb25zIG9mIHVzZXJzIHRvIGJlIGV4cGVjdGVkPG86cD48L286cD48\n" +
        "L3A+DQo8cCBzdHlsZT0ibWFyZ2luOjBpbjttYXJnaW4tYm90dG9tOi4wMDAxcHQiPmZvciB0aGUg\n" +
        "ZnV0dXJlLjxvOnA+PC9vOnA+PC9wPg0KPHAgc3R5bGU9Im1hcmdpbjowaW47bWFyZ2luLWJvdHRv\n" +
        "bTouMDAwMXB0Ij4mbmJzcDs8bzpwPjwvbzpwPjwvcD4NCjxwIHN0eWxlPSJtYXJnaW46MGluO21h\n" +
        "cmdpbi1ib3R0b206LjAwMDFwdCI+Rm9yIHRoZSBPcGVyYXRpbmcgc3lzdGVtIG9uIHRoZSBTZXJ2\n" +
        "ZXJzIGxpbnV4IGlzIHJlcXVpcmVkLiBUaGVyZSB3ZSBzdXBwb3J0IGRlYmlhbiwgU1VTRSBhbmQ8\n" +
        "bzpwPjwvbzpwPjwvcD4NCjxwIHN0eWxlPSJtYXJnaW46MGluO21hcmdpbi1ib3R0b206LjAwMDFw\n" +
        "dCI+UmVkaGF0LiBPdXIgYmlnZXN0IGluc3RhbGxhdGlvbiBydW5zIG9uIGRlYmlhbi48bzpwPjwv\n" +
        "bzpwPjwvcD4NCjxwIHN0eWxlPSJtYXJnaW46MGluO21hcmdpbi1ib3R0b206LjAwMDFwdCI+Jm5i\n" +
        "c3A7PG86cD48L286cD48L3A+DQo8cCBzdHlsZT0ibWFyZ2luOjBpbjttYXJnaW4tYm90dG9tOi4w\n" +
        "MDAxcHQiPlRoZSBkZXNpZ24gb2YgdGhlIE9wZW4tWGNoYW5nZSBhcHBsaWNhdGlvbiBhcyB3ZWxs\n" +
        "IGFzIGRvdmVjb3QgaXMgdG8gc2NhbGUgdmVydGljYWwgYW5kIGhvcml6b250YWwuPG86cD48L286\n" +
        "cD48L3A+DQo8cCBzdHlsZT0ibWFyZ2luOjBpbjttYXJnaW4tYm90dG9tOi4wMDAxcHQiPiZuYnNw\n" +
        "OzxvOnA+PC9vOnA+PC9wPg0KPHAgc3R5bGU9Im1hcmdpbjowaW47bWFyZ2luLWJvdHRvbTouMDAw\n" +
        "MXB0Ij5BcyBmYXIgYXMgaSB1bmRlcnN0b29kLCB0aGUgY29tcGxldGUgaW5zdGFsbGF0aW9uIHdp\n" +
        "bGwgYmUgZG9uZSBieSBrcnlwdG9zbmV0d29ya3M/PG86cD48L286cD48L3A+DQo8cCBzdHlsZT0i\n" +
        "bWFyZ2luOjBpbjttYXJnaW4tYm90dG9tOi4wMDAxcHQiPiZuYnNwOzxvOnA+PC9vOnA+PC9wPg0K\n" +
        "PHAgc3R5bGU9Im1hcmdpbjowaW47bWFyZ2luLWJvdHRvbTouMDAwMXB0Ij5UaGUgUXVlc3Rpb24g\n" +
        "aSBoYXZlIGlzIGFib3V0IGhvdyBwcm92aXNpb25pbmcgc2hvdWxkIGJlIGRvbmUuIFdoYXQgaXMg\n" +
        "Y3VycmVudGx5IGluIHBsYWNlIHRvPG86cD48L286cD48L3A+DQo8cCBzdHlsZT0ibWFyZ2luOjBp\n" +
        "bjttYXJnaW4tYm90dG9tOi4wMDAxcHQiPm1hbmdlIHVzZXIgYWNjb3VudHMvYmlsbGluZy8uLi4/\n" +
        "PG86cD48L286cD48L3A+DQo8cCBzdHlsZT0ibWFyZ2luOjBpbjttYXJnaW4tYm90dG9tOi4wMDAx\n" +
        "cHQiPiZuYnNwOzxvOnA+PC9vOnA+PC9wPg0KPHAgc3R5bGU9Im1hcmdpbjowaW47bWFyZ2luLWJv\n" +
        "dHRvbTouMDAwMXB0Ij5UaGFuayB5b3UsPG86cD48L286cD48L3A+DQo8cCBzdHlsZT0ibWFyZ2lu\n" +
        "OjBpbjttYXJnaW4tYm90dG9tOi4wMDAxcHQiPkhvbGdlcjxvOnA+PC9vOnA+PC9wPg0KPHAgc3R5\n" +
        "bGU9Im1hcmdpbjowaW47bWFyZ2luLWJvdHRvbTouMDAwMXB0Ij4mbmJzcDs8bzpwPjwvbzpwPjwv\n" +
        "cD4NCjxwIHN0eWxlPSJtYXJnaW46MGluO21hcmdpbi1ib3R0b206LjAwMDFwdCI+Jm5ic3A7PG86\n" +
        "cD48L286cD48L3A+DQo8ZGl2IHN0eWxlPSJtYXJnaW4tdG9wOjMuNzVwdDttYXJnaW4tYm90dG9t\n" +
        "OjMuNzVwdCI+DQo8YmxvY2txdW90ZSBzdHlsZT0iYm9yZGVyOm5vbmU7Ym9yZGVyLWxlZnQ6c29s\n" +
        "aWQgYmx1ZSAxLjBwdDtwYWRkaW5nOjBpbiAwaW4gMGluIDguMHB0Ow0KbWFyZ2luLWxlZnQ6MGlu\n" +
        "O21hcmdpbi10b3A6NS4wcHQ7bWFyZ2luLWJvdHRvbTo1LjBwdCI+DQo8cCBzdHlsZT0ibWFyZ2lu\n" +
        "OjBpbjttYXJnaW4tYm90dG9tOi4wMDAxcHQiPkVybnN0PG86cD48L286cD48L3A+DQo8cCBzdHls\n" +
        "ZT0ibWFyZ2luOjBpbjttYXJnaW4tYm90dG9tOi4wMDAxcHQiPiZuYnNwOzxvOnA+PC9vOnA+PC9w\n" +
        "Pg0KPHA+Jm5ic3A7PG86cD48L286cD48L3A+DQo8ZGl2IHN0eWxlPSJtYXJnaW4tdG9wOjMuNzVw\n" +
        "dDttYXJnaW4tYm90dG9tOjMuNzVwdCI+DQo8cCBjbGFzcz0iTXNvTm9ybWFsIj48c3BhbiBzdHls\n" +
        "ZT0iZm9udC1mYW1pbHk6JnF1b3Q7Q291cmllciBOZXcmcXVvdDsiPk9uIE9jdG9iZXIgMjIsIDIw\n" +
        "MDkgYXQgNzoxNSBBTSBHb2t1bG5hdGggQyAmbHQ7Z29rdWxuYXRoLmNAa3J5cHRvc25ldHdvcmtz\n" +
        "LmNvbSZndDsgd3JvdGU6PGJyPg0KPGJyPg0KJmd0OyBGaW5lIEVybnN0Li4gV2Ugc2hhbGwgaGF2\n" +
        "ZSB0aGUgZGlzY3Vzc2lvbi4uPGJyPg0KJmd0Ozxicj4NCiZndDsgVGhhbmtzIGFuZCBSZWdhcmRz\n" +
        "PGJyPg0KJmd0Ozxicj4NCiZndDsgR29rdWxuYXRoPGJyPg0KJmd0Ozxicj4NCiZndDsgLS0tLS1P\n" +
        "cmlnaW5hbCBNZXNzYWdlLS0tLS08YnI+DQomZ3Q7IEZyb206IGVybnN0LmtlbHRpbmdAb3Blbi14\n" +
        "Y2hhbmdlLmNvbSBbbWFpbHRvOmVybnN0LmtlbHRpbmdAb3Blbi14Y2hhbmdlLmNvbV08YnI+DQom\n" +
        "Z3Q7IFNlbnQ6IFdlZG5lc2RheSwgT2N0b2JlciAyMSwgMjAwOSA2OjA3IFBNPGJyPg0KJmd0OyBU\n" +
        "bzogZ29rdWxuYXRoLmNAa3J5cHRvc25ldHdvcmtzLmNvbTxicj4NCiZndDsgU3ViamVjdDogTmV3\n" +
        "IEFwcG9pbnRtZW50OiBUZWNobmljYWwgZGlzY3Vzc2lvbiBmb3IgSG9zdGluZyBwcm9qZWN0PGJy\n" +
        "Pg0KJmd0Ozxicj4NCiZndDsgQSBuZXcgYXBwb2ludG1lbnQgd2FzIGNyZWF0ZWQgYnkgS2VsdGlu\n" +
        "ZywgRXJuc3QuPGJyPg0KJmd0Ozxicj4NCiZndDsgQXBwb2ludG1lbnQ8YnI+DQomZ3Q7ID09PT09\n" +
        "PT09PT09PGJyPg0KJmd0OyBDcmVhdGVkIGJ5OiBLZWx0aW5nLCBFcm5zdDxicj4NCiZndDsgQ3Jl\n" +
        "YXRlZCBhdDogT2N0IDIxLCAyMDA5IDI6Mzc6MDAgUE0sIENFU1Q8YnI+DQomZ3Q7IERlc2NyaXB0\n" +
        "aW9uOiBUZWNobmljYWwgZGlzY3Vzc2lvbiBmb3IgSG9zdGluZyBwcm9qZWN0PGJyPg0KJmd0OyBM\n" +
        "b2NhdGlvbjogU2t5cGU8YnI+DQomZ3Q7PGJyPg0KJmd0OyBTdGFydCBkYXRlOiBPY3QgMjMsIDIw\n" +
        "MDkgOTowMDowMCBBTSwgQ0VTVCBFbmQgZGF0ZTogT2N0IDIzLCAyMDA5IDEwOjAwOjAwIEFNLCBD\n" +
        "RVNUPGJyPg0KJmd0Ozxicj4NCiZndDsgQ29tbWVudHM6PGJyPg0KJmd0OyBIZWxsbyBnb2t1bG5h\n" +
        "dGgsPGJyPg0KJmd0Ozxicj4NCiZndDsgZG8geW91IGhhdmUgdGltZSBvbiBGcmlkYXkgYXQgOWFt\n" +
        "IENFVCwgdG8gY29udGludWUgb3VyIGRpc2N1c3Npb24gZnJvbSB0b2RheSB0b2dldGhlciB3aXRo\n" +
        "IFN0ZXBoYW4gZnJvbSBvdXIgUHJvZmVzc2lvbmFsIFNlcnZpY2VzIHRlYW0uPGJyPg0KJmd0Ozxi\n" +
        "cj4NCiZndDsgRXJuc3Q8YnI+DQomZ3Q7PGJyPg0KJmd0Ozxicj4NCiZndDsgLS0tLS0tLS0tLSBP\n" +
        "cmlnaW5hbCBNZXNzYWdlIC0tLS0tLS0tLS08YnI+DQomZ3Q7IEZyb206IGdva3VsbmF0aC5jQGty\n" +
        "eXB0b3NuZXR3b3Jrcy5jb208YnI+DQomZ3Q7IFRvOiBlcm5zdC5rZWx0aW5nQG9wZW4teGNoYW5n\n" +
        "ZS5jb208YnI+DQomZ3Q7IFJlY2VpdmVkOiAxMC0yMC0yMDA5IDAxOjI1IFBNPGJyPg0KJmd0OyBT\n" +
        "dWJqZWN0OiBPdXIgZGlzY3Vzc2lvbjxicj4NCiZndDs8YnI+DQomZ3Q7IEhpIEVybnN0LCAmbHQ7\n" +
        "YnIgLyZndDsmbHQ7YnIgLyZndDsgJmx0O2JyIC8mZ3Q7Jmx0O2JyIC8mZ3Q7TGV0J3MgZmFzdC10\n" +
        "cmFjayBvdXIgZWFybGllciBkaXNjdXNzaW9ucyBmb3IgYSByZXF1aXJlbWVudC4gJmx0O2JyIC8m\n" +
        "Z3Q7Jmx0O2JyIC8mZ3Q7ICZsdDticiAvJmd0OyZsdDticiAvJmd0O0kgd291bGQgbGlrZSB0byB1\n" +
        "bmRlcnN0YW5kIHRoZSBhcmNoaXRlY3R1cmUsIHNpemluZywgQm9NICZhbXA7YW1wOyBjb3N0cyB0\n" +
        "byBidWlsZCBhJmx0O2JyIC8mZ3Q7YnVzaW5lc3MgY2FzZSBmb3IgMSBNaWxsaW9uIG1haWxib3hl\n" +
        "cyBvbiBwcmlvcml0eQ0KIGJhc2lzLiBDb3VsZCB5b3Ugc2hhcmUmbHQ7YnIgLyZndDt3aXRoIGRl\n" +
        "dGFpbHMgJmx0O2JyIC8mZ3Q7Jmx0O2JyIC8mZ3Q7ICZsdDticiAvJmd0OyZsdDticiAvJmd0O1Ro\n" +
        "YW5rcyBhbmQgUmVnYXJkcywmbHQ7YnIgLyZndDsmbHQ7YnIgLyZndDsgJmx0O2JyIC8mZ3Q7Jmx0\n" +
        "O2JyIC8mZ3Q7R29rdWxuYXRoLkMmbHQ7YnIgLyZndDsmbHQ7YnIgLyZndDtWUC0gQnVzaW5lc3Mg\n" +
        "U29sdXRpb25zJmx0O2JyIC8mZ3Q7Jmx0O2JyIC8mZ3Q7S3J5cHRvcyBOZXR3b3JrcyBQdnQuIEx0\n" +
        "ZC4mbHQ7YnIgLyZndDsmbHQ7YnIgLyZndDszNiwgTmF0V2VzdCBWZW5rYXRyYW1hbmEsIDFzdCBG\n" +
        "bG9vciwgS2FtYWtvdGkgTmFnYXIsDQogJmx0O2JyIC8mZ3Q7Jmx0O2JyIC8mZ3Q7UGFsbGlrYXJu\n" +
        "YWkgQ2hlbm5haSAtIDYwMDEwMCZsdDticiAvJmd0OyZsdDticiAvJmd0O0hhbmQgUGhvbmU6ICYj\n" +
        "NDM7OTEtOTk0MDAtNTIzNTcmbHQ7YnIgLyZndDsmbHQ7YnIgLyZndDtPZmZpY2U6ICYjNDM7OTEg\n" +
        "NDQgNDM5MTUxNTEmbHQ7YnIgLyZndDsmbHQ7YnIgLyZndDsmbHQ7YSBocmVmPSZxdW90O2h0dHA6\n" +
        "Ly93d3cua3J5cHRvc25ldHdvcmtzLmNvbSZxdW90OyB0YXJnZXQ9JnF1b3Q7X2JsYW5rJnF1b3Q7\n" +
        "Jmd0O3d3dy5rcnlwdG9zbmV0d29ya3MuY29tJmx0Oy9hJmd0OyZsdDticiAvJmd0OyZsdDticiAv\n" +
        "Jmd0OyAmbHQ7YnIgLyZndDsmbHQ7YnIgLyZndDstIFdlIG1ha2UgSVQgc2ltcGxlIC4mbHQ7YnIN\n" +
        "CiAvJmd0OyZsdDticiAvJmd0OyAmbHQ7YnIgLyZndDsmbHQ7YnIgLyZndDsmYW1wO3F1b3Q7VGhl\n" +
        "IGluZm9ybWF0aW9uIGNvbnRhaW5lZCBpbiB0aGlzIGNvbW11bmljYXRpb24gaXMgaW50ZW5kZWQg\n" +
        "c29sZWx5IGZvciB0aGUmbHQ7YnIgLyZndDt1c2Ugb2YgdGhlIGluZGl2aWR1YWwgb3IgZW50aXR5\n" +
        "IHRvIHdob20gaXQgaXMgYWRkcmVzc2VkIGFuZCBvdGhlcnMmbHQ7YnIgLyZndDthdXRob3JpemVk\n" +
        "IHRvIHJlY2VpdmUgaXQuIEl0IG1heSBjb250YWluIGNvbmZpZGVudGlhbCBvciBsZWdhbGx5IHBy\n" +
        "aXZpbGVnZWQmbHQ7YnINCiAvJmd0O2luZm9ybWF0aW9uLklmIHlvdSBhcmUgbm90IHRoZSBpbnRl\n" +
        "bmRlZCByZWNpcGllbnQgeW91IGFyZSBoZXJlYnkgbm90aWZpZWQmbHQ7YnIgLyZndDt0aGF0IGFu\n" +
        "eSBkaXNjbG9zdXJlLCBjb3B5aW5nLCBkaXN0cmlidXRpb24gb3IgdGFraW5nIGFueSBhY3Rpb24g\n" +
        "aW4gcmVsaWFuY2UmbHQ7YnIgLyZndDtvbiB0aGUgY29udGVudHMgb2YgdGhpcyBpbmZvcm1hdGlv\n" +
        "biBpcyBzdHJpY3RseSBwcm9oaWJpdGVkIGFuZCBtYXkgYmUmbHQ7YnIgLyZndDt1bmxhd2Z1bC4g\n" +
        "SWYgeW91IGhhdmUNCiByZWNlaXZlZCB0aGlzIGNvbW11bmljYXRpb24gaW4gZXJyb3IsIHBsZWFz\n" +
        "ZSBub3RpZnkgdXMmbHQ7YnIgLyZndDtpbW1lZGlhdGVseSBieSByZXNwb25kaW5nIHRvIHRoaXMg\n" +
        "ZW1haWwgYW5kIHRoZW4gZGVsZXRlIGl0IGZyb20geW91ciBzeXN0ZW0uJmx0O2JyIC8mZ3Q7S3J5\n" +
        "cHRvcyBOZXR3b3JrcyBpcyBuZWl0aGVyIGxpYWJsZSBmb3IgdGhlIHByb3BlciBhbmQgY29tcGxl\n" +
        "dGUgdHJhbnNtaXNzaW9uJmx0O2JyIC8mZ3Q7b2YgdGhlIGluZm9ybWF0aW9uIGNvbnRhaW5lZCBp\n" +
        "biB0aGlzDQogY29tbXVuaWNhdGlvbiBub3IgZm9yIGFueSBkZWxheSBpbiBpdHMmbHQ7YnIgLyZn\n" +
        "dDtyZWNlaXB0JmFtcDtxdW90OyZsdDticiAvJmd0OyZsdDticiAvJmd0OyAmbHQ7YnIgLyZndDsm\n" +
        "bHQ7YnIgLyZndDs8YnI+DQomZ3Q7PGJyPg0KJmd0Ozxicj4NCiZndDsgUGFydGljaXBhbnRzPGJy\n" +
        "Pg0KJmd0OyA9PT09PT09PT09PT08YnI+DQomZ3Q7ICdFcm5zdCBLZWx0aW5nJyAoZXh0ZXJuYWwp\n" +
        "PGJyPg0KJmd0OyBFcm5zdCAoZXh0ZXJuYWwpPGJyPg0KJmd0OyBHb2t1bG5hdGggQyAoZXh0ZXJu\n" +
        "YWwpPGJyPg0KJmd0OyBLZWx0aW5nLCBFcm5zdCAoYWNjZXB0ZWQpPGJyPg0KJmd0OyBNYXJ0aW4s\n" +
        "IFN0ZXBoYW4gKHdhaXRpbmcpPGJyPg0KJmd0Ozxicj4NCiZndDsgUmVzb3VyY2VzPGJyPg0KJmd0\n" +
        "OyA9PT09PT09PT08YnI+DQomZ3Q7IE5vIHJlc291cmNlcyBoYXZlIGJlZW4gc2NoZWR1bGVkLjxi\n" +
        "cj4NCiZndDs8YnI+DQomZ3Q7ID09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09PT09\n" +
        "PT09PTxicj4NCiZndDs8bzpwPjwvbzpwPjwvc3Bhbj48L3A+DQo8L2Rpdj4NCjxwIHN0eWxlPSJt\n" +
        "YXJnaW46MGluO21hcmdpbi1ib3R0b206LjAwMDFwdCI+Jm5ic3A7PG86cD48L286cD48L3A+DQo8\n" +
        "cCBzdHlsZT0ibXNvLW1hcmdpbi10b3AtYWx0OjMuNzVwdDttYXJnaW4tcmlnaHQ6MGluO21hcmdp\n" +
        "bi1ib3R0b206My43NXB0Ow0KbWFyZ2luLWxlZnQ6MGluIj4NCjxzcGFuIHN0eWxlPSJmb250LWZh\n" +
        "bWlseTomcXVvdDtDb3VyaWVyIE5ldyZxdW90OyI+PGJyPg0KLS0tLS0tLS0tLTxicj4NCkVybnN0\n" +
        "IEtlbHRpbmc8YnI+DQpWUCBTYWxlcyBBUEFDPGJyPg0KPGJyPg0KZXJuc3Qua2VsdGluZ0BvcGVu\n" +
        "LXhjaGFuZ2UuY29tJm5ic3A7IFNreXBlOiBla2VsdGluZzxicj4NCk1vYiAmIzQzOzQ5IDE3NCAz\n" +
        "NDQwIDU1MywgUGhvbmUgJiM0Mzs0OSA0MTAxIC0gODA4NyAyNCwgRmF4ICYjNDM7NDkgNDEwMSAt\n" +
        "IDgwODcgMTg8YnI+DQpXZWIgaHR0cDovL3d3dy5vcGVuLXhjaGFuZ2UuY29tPGJyPg0KLS0tLS0t\n" +
        "LS0tLTxicj4NCk9wZW4tWGNoYW5nZSBBRywmbmJzcDsgTWF4ZmVsZHN0ci4gOSwgOTA0MDkgTsO8\n" +
        "cm5iZXJnLCBBbXRzZ2VyaWNodCBOw7xybmJlcmcgSFJCIDI0NzM4PGJyPg0KVm9yc3RhbmQ6IFJh\n" +
        "ZmFlbCBMYWd1bmEgZGUgbGEgVmVyYSwgQXVmc2ljaHRzcmF0c3ZvcnNpdHplbmRlcjogUmljaGFy\n" +
        "ZCBTZWlidDxicj4NCjxicj4NCkV1cm9wZWFuIE9mZmljZTogT3Blbi1YY2hhbmdlIEdtYkgsIE1h\n" +
        "cnRpbnN0ci4gNDEsIEQtNTc0NjIgT2xwZSwgR2VybWFueTxicj4NCkFtdHNnZXJpY2h0IFNpZWdl\n" +
        "biwgSFJCIDg3MTgsIEdlc2Now6RmdHNmw7xocmVyOiBGcmFuayBIb2JlcmcsIE1hcnRpbiBLYXVz\n" +
        "czxicj4NCi0tLS0tLS0tLS08bzpwPjwvbzpwPjwvc3Bhbj48L3A+DQo8L2Jsb2NrcXVvdGU+DQo8\n" +
        "L2Rpdj4NCjxwIHN0eWxlPSJtYXJnaW46MGluO21hcmdpbi1ib3R0b206LjAwMDFwdCI+Jm5ic3A7\n" +
        "PG86cD48L286cD48L3A+DQo8cCBzdHlsZT0ibXNvLW1hcmdpbi10b3AtYWx0OjMuNzVwdDttYXJn\n" +
        "aW4tcmlnaHQ6MGluO21hcmdpbi1ib3R0b206My43NXB0Ow0KbWFyZ2luLWxlZnQ6MGluIj4NCjxz\n" +
        "cGFuIHN0eWxlPSJmb250LWZhbWlseTomcXVvdDtDb3VyaWVyIE5ldyZxdW90OyI+LS08YnI+DQpI\n" +
        "b2xnZXIgQWNodHppZ2VyPGJyPg0KU2VuaW9yIFN5c3RlbSBFbmdpbmVlcjxicj4NCk9wZW4tWGNo\n" +
        "YW5nZSBHbWJIPGJyPg0KPGJyPg0KUGhvbmU6ICYjNDM7NDkgMTcyIDI0OTQ1ODEsIEZheDombmJz\n" +
        "cDsgJiM0Mzs0OSA5MTEgMTgwIDE0MTk8YnI+DQo8YnI+DQotLS0tLS0tLS0tLS0tLS0tLS0tLS0t\n" +
        "LS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0t\n" +
        "PGJyPg0KT3Blbi1YY2hhbmdlIEFHLCZuYnNwOyBNYXhmZWxkc3RyLiA5LCA5MDQwOSBOw7xybmJl\n" +
        "cmcsIEFtdHNnZXJpY2h0IE7DvHJuYmVyZyBIUkIgMjQ3Mzg8YnI+DQpWb3JzdGFuZDombmJzcDsg\n" +
        "Jm5ic3A7IFJhZmFlbCBMYWd1bmEgZGUgbGEgVmVyYSwgQXVmc2ljaHRzcmF0c3ZvcnNpdHplbmRl\n" +
        "cjogUmljaGFyZCBTZWlidDxicj4NCjxicj4NCkV1cm9wZWFuIE9mZmljZTombmJzcDsgJm5ic3A7\n" +
        "ICZuYnNwOyAmbmJzcDsgT3Blbi1YY2hhbmdlIEdtYkgsIE1hcnRpbnN0ci4gNDEsIEQtNTc0NjIg\n" +
        "T2xwZSwgR2VybWFueTxicj4NCkFtdHNnZXJpY2h0IFNpZWdlbiwgSFJCIDg3MTgsIEdlc2Now6Rm\n" +
        "dHNmw7xocmVyOiZuYnNwOyAmbmJzcDsgJm5ic3A7ICZuYnNwO0ZyYW5rIEhvYmVyZywgTWFydGlu\n" +
        "IEthdXNzPGJyPg0KPGJyPg0KVVMgT2ZmaWNlOiZuYnNwOyAmbmJzcDsgT3Blbi1YY2hhbmdlLCBJ\n" +
        "bmMuLCAzMDMgU291dGggQnJvYWR3YXksIFRhcnJ5dG93biwgTmV3IFlvcmsgMTA1OTE8YnI+DQot\n" +
        "LS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0tLS0t\n" +
        "LS0tLS0tLS0tLS0tLS0tLS0tLS0tPG86cD48L286cD48L3NwYW4+PC9wPg0KPC9kaXY+DQo8YnI+\n" +
        "DQo8aHI+DQo8Zm9udCBmYWNlPSJBcmlhbCIgY29sb3I9IkdyYXkiIHNpemU9IjEiPkRpc2NsYWlt\n" +
        "ZXI6IFRoZSBpbmZvcm1hdGlvbiBjb250YWluZWQgaW4gdGhpcyBlLW1haWwgYW5kIGF0dGFjaG1l\n" +
        "bnRzIChpZiBhbnkpIGFyZSBwcml2aWxlZ2VkIGFuZCBjb25maWRlbnRpYWwgYW5kIGFyZSBpbnRl\n" +
        "bmRlZCBmb3IgdGhlIGluZGl2aWR1YWwocykgb3IgZW50aXR5KGllcykgbmFtZWQgaW4gdGhpcyBl\n" +
        "LW1haWwuIElmIHlvdSBhcmUgbm90IHRoZSBpbnRlbmRlZCByZWNpcGllbnQsDQogb3IgZW1wbG95\n" +
        "ZWUgb3IgYWdlbnQsIHlvdSBhcmUgaGVyZWJ5IG5vdGlmaWVkIHRoYXQgZGlzc2VtaW5hdGlvbiwg\n" +
        "ZGlzdHJpYnV0aW9uIG9yIGNvcHlpbmcgb2YgdGhpcyBjb21tdW5pY2F0aW9uIG9yIGF0dGFjaG1l\n" +
        "bnRzIHRoZXJlb2YgaXMgc3RyaWN0bHkgcHJvaGliaXRlZC4gSUYgWU9VIFJFQ0VJVkVEIHRoaXMg\n" +
        "Y29tbXVuaWNhdGlvbiBpbiBlcnJvciwgcGxlYXNlIGltbWVkaWF0ZWx5IG5vdGlmeSB0aGUgc2Vu\n" +
        "ZGVyIGFuZCBERUxFVEUgdGhlDQogb3JpZ2luYWwgbWVzc2FnZSBmcm9tIHRoZSBJbmJveC48YnI+\n" +
        "DQo8L2ZvbnQ+DQo8L2JvZHk+DQo8L2h0bWw+DQo=").getBytes();

    public void testMIMEStructure() {
        try {
            getSession();

            final MailMessage mail = MimeMessageConverter.convertMessage(SIMPLE);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            // System.out.println(jsonMailObject.toString(2));

            final JSONObject jsonBodyObject;
            {
                final Object bodyObject = jsonMailObject.opt("body");
                assertNotNull("Missing mail body.", bodyObject);

                assertTrue("Body object is not a JSON object.", (bodyObject instanceof JSONObject));
                jsonBodyObject = (JSONObject) bodyObject;
            }

            final String id = jsonBodyObject.getString("id");
            assertEquals("Wring part ID.", "1", id);

            final JSONObject jsonHeaderObject = jsonMailObject.getJSONObject("headers");

            final JSONObject jsonContentTypeObject = jsonHeaderObject.getJSONObject("content-type");
            final JSONObject paramsObject = jsonContentTypeObject.getJSONObject("params");
            assertTrue("Charset parameter should be UTF-8, but isn't.", "UTF-8".equalsIgnoreCase(paramsObject.getString("charset")));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public void testMIMEStructure2() {
        try {
            getSession();

            final MailMessage mail = MimeMessageConverter.convertMessage(SIMPLE2);

            final MIMEStructureHandler handler = new MIMEStructureHandler(-1L);
            new StructureMailMessageParser().parseMailMessage(mail, handler);

            final JSONObject jsonMailObject = handler.getJSONMailObject();
            assertNotNull("Structured JSON mail object is null.", jsonMailObject);

            // System.out.println(jsonMailObject.toString(2));

            final JSONObject jsonBodyObject;
            {
                final Object bodyObject = jsonMailObject.opt("body");
                assertNotNull("Missing mail body.", bodyObject);

                assertTrue("Body object is not a JSON object.", (bodyObject instanceof JSONObject));
                jsonBodyObject = (JSONObject) bodyObject;
            }

            final String id = jsonBodyObject.getString("id");
            assertEquals("Wring part ID.", "1", id);

            final JSONObject jsonHeaderObject = jsonMailObject.getJSONObject("headers");
            assertFalse("Content-Transfer-Encoding header should not be present.", jsonHeaderObject.hasAndNotNull("content-transfer-encoding"));
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

}

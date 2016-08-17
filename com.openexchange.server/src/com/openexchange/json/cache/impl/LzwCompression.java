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

package com.openexchange.json.cache.impl;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;
import com.openexchange.java.Streams;

/**
 * {@link LzwCompression} - Performs LZW compression/decompression.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LzwCompression {

    private final int dictionary[][] = new int[4000][4000];

    private final int pattern[] = new int[4000];

    private final int element[] = new int[4000];

    private final int chars[] = new int[256];

    private final int chfreq[] = new int[256];

    private final float chprobs[] = new float[256];

    private final int ofreq[] = new int[4000];

    private int dfree, psize, esize;

    private int top, tc;

    private int chnext;

    /**
     * Initializes a new {@link LzwCompression}.
     */
    public LzwCompression() {
        super();
    }

    /**
     * Initializes variables
     */
    public void init() {
        for (int i = 0; i < 4000; i++) {
            for (int j = 0; j < 4000; j++) {
                dictionary[i][j] = -1;
            }
        }

        for (int i = 0; i < 4000; i++) {
            pattern[i] = -1;
            element[i] = -1;
            ofreq[i] = 0;
        }

        for (int i = 0; i < 256; i++) {
            chars[i] = -1;
            chfreq[i] = 0;
            chprobs[i] = 0.0f;
        }

        dictionary[0][0] = 0;// ASCII codes
        dictionary[1][0] = 1;
        dictionary[2][0] = 2;
        dictionary[3][0] = 3;
        dictionary[4][0] = 4;
        dictionary[5][0] = 5;
        dictionary[6][0] = 6;
        dictionary[7][0] = 7;
        dictionary[8][0] = 8;
        dictionary[9][0] = 9;
        dictionary[10][0] = 10;
        dictionary[11][0] = 11;
        dictionary[12][0] = 12;
        dictionary[13][0] = 13;
        dictionary[14][0] = 14;
        dictionary[15][0] = 15;
        dictionary[16][0] = 16;
        dictionary[17][0] = 17;
        dictionary[18][0] = 18;
        dictionary[19][0] = 19;
        dictionary[20][0] = 20;
        dictionary[21][0] = 21;
        dictionary[22][0] = 22;
        dictionary[23][0] = 23;
        dictionary[24][0] = 24;
        dictionary[25][0] = 25;
        dictionary[26][0] = 26;
        dictionary[27][0] = 27;
        dictionary[28][0] = 28;
        dictionary[29][0] = 29;
        dictionary[30][0] = 30;
        dictionary[31][0] = 31;
        dictionary[32][0] = 32;
        dictionary[33][0] = 33;
        dictionary[34][0] = 34;
        dictionary[35][0] = 35;
        dictionary[36][0] = 36;
        dictionary[37][0] = 37;
        dictionary[38][0] = 38;
        dictionary[39][0] = 39;
        dictionary[40][0] = 40;
        dictionary[41][0] = 41;
        dictionary[42][0] = 42;
        dictionary[43][0] = 43;
        dictionary[44][0] = 44;
        dictionary[45][0] = 45;
        dictionary[46][0] = 46;
        dictionary[47][0] = 47;
        dictionary[48][0] = 48;
        dictionary[49][0] = 49;
        dictionary[50][0] = 50;
        dictionary[51][0] = 51;
        dictionary[52][0] = 52;
        dictionary[53][0] = 53;
        dictionary[54][0] = 54;
        dictionary[55][0] = 55;
        dictionary[56][0] = 56;
        dictionary[57][0] = 57;
        dictionary[58][0] = 58;
        dictionary[59][0] = 59;
        dictionary[60][0] = 60;
        dictionary[61][0] = 61;
        dictionary[62][0] = 62;
        dictionary[63][0] = 63;
        dictionary[64][0] = 64;
        dictionary[65][0] = 65;
        dictionary[66][0] = 66;
        dictionary[67][0] = 67;
        dictionary[68][0] = 68;
        dictionary[69][0] = 69;
        dictionary[70][0] = 70;
        dictionary[71][0] = 71;
        dictionary[72][0] = 72;
        dictionary[73][0] = 73;
        dictionary[74][0] = 74;
        dictionary[75][0] = 75;
        dictionary[76][0] = 76;
        dictionary[77][0] = 77;
        dictionary[78][0] = 78;
        dictionary[79][0] = 79;
        dictionary[80][0] = 80;
        dictionary[81][0] = 81;
        dictionary[82][0] = 82;
        dictionary[83][0] = 83;
        dictionary[84][0] = 84;
        dictionary[85][0] = 85;
        dictionary[86][0] = 86;
        dictionary[87][0] = 87;
        dictionary[88][0] = 88;
        dictionary[89][0] = 89;
        dictionary[90][0] = 90;
        dictionary[91][0] = 91;
        dictionary[92][0] = 92;
        dictionary[93][0] = 93;
        dictionary[94][0] = 94;
        dictionary[95][0] = 95;
        dictionary[96][0] = 96;
        dictionary[97][0] = 97;
        dictionary[98][0] = 98;
        dictionary[99][0] = 99;
        dictionary[100][0] = 100;
        dictionary[101][0] = 101;
        dictionary[102][0] = 102;
        dictionary[103][0] = 103;
        dictionary[104][0] = 104;
        dictionary[105][0] = 105;
        dictionary[106][0] = 106;
        dictionary[107][0] = 107;
        dictionary[108][0] = 108;
        dictionary[109][0] = 109;
        dictionary[110][0] = 110;
        dictionary[111][0] = 111;
        dictionary[112][0] = 112;
        dictionary[113][0] = 113;
        dictionary[114][0] = 114;
        dictionary[115][0] = 115;
        dictionary[116][0] = 116;
        dictionary[117][0] = 117;
        dictionary[118][0] = 118;
        dictionary[119][0] = 119;
        dictionary[120][0] = 120;
        dictionary[121][0] = 121;
        dictionary[122][0] = 122;
        dictionary[123][0] = 123;
        dictionary[124][0] = 124;
        dictionary[125][0] = 125;
        dictionary[126][0] = 126;
        dictionary[127][0] = 127;
        dictionary[128][0] = 128;
        dictionary[129][0] = 129;
        dictionary[130][0] = 130;
        dictionary[131][0] = 131;
        dictionary[132][0] = 132;
        dictionary[133][0] = 133;
        dictionary[134][0] = 134;
        dictionary[135][0] = 135;
        dictionary[136][0] = 136;
        dictionary[137][0] = 137;
        dictionary[138][0] = 138;
        dictionary[139][0] = 139;
        dictionary[140][0] = 140;
        dictionary[141][0] = 141;
        dictionary[142][0] = 142;
        dictionary[143][0] = 143;
        dictionary[144][0] = 144;
        dictionary[145][0] = 145;
        dictionary[146][0] = 146;
        dictionary[147][0] = 147;
        dictionary[148][0] = 148;
        dictionary[149][0] = 149;
        dictionary[150][0] = 150;
        dictionary[151][0] = 151;
        dictionary[152][0] = 152;
        dictionary[153][0] = 153;
        dictionary[154][0] = 154;
        dictionary[155][0] = 155;
        dictionary[156][0] = 156;
        dictionary[157][0] = 157;
        dictionary[158][0] = 158;
        dictionary[159][0] = 159;
        dictionary[160][0] = 160;
        dictionary[161][0] = 161;
        dictionary[162][0] = 162;
        dictionary[163][0] = 163;
        dictionary[164][0] = 164;
        dictionary[165][0] = 165;
        dictionary[166][0] = 166;
        dictionary[167][0] = 167;
        dictionary[168][0] = 168;
        dictionary[169][0] = 169;
        dictionary[170][0] = 170;
        dictionary[171][0] = 171;
        dictionary[172][0] = 172;
        dictionary[173][0] = 173;
        dictionary[174][0] = 174;
        dictionary[175][0] = 175;
        dictionary[176][0] = 176;
        dictionary[177][0] = 177;
        dictionary[178][0] = 178;
        dictionary[179][0] = 179;
        dictionary[180][0] = 180;
        dictionary[181][0] = 181;
        dictionary[182][0] = 182;
        dictionary[183][0] = 183;
        dictionary[184][0] = 184;
        dictionary[185][0] = 185;
        dictionary[186][0] = 186;
        dictionary[187][0] = 187;
        dictionary[188][0] = 188;
        dictionary[189][0] = 189;
        dictionary[190][0] = 190;
        dictionary[191][0] = 191;
        dictionary[192][0] = 192;
        dictionary[193][0] = 193;
        dictionary[194][0] = 194;
        dictionary[195][0] = 195;
        dictionary[196][0] = 196;
        dictionary[197][0] = 197;
        dictionary[198][0] = 198;
        dictionary[199][0] = 199;
        dictionary[200][0] = 200;
        dictionary[201][0] = 201;
        dictionary[202][0] = 202;
        dictionary[203][0] = 203;
        dictionary[204][0] = 204;
        dictionary[205][0] = 205;
        dictionary[206][0] = 206;
        dictionary[207][0] = 207;
        dictionary[208][0] = 208;
        dictionary[209][0] = 209;
        dictionary[210][0] = 210;
        dictionary[211][0] = 211;
        dictionary[212][0] = 212;
        dictionary[213][0] = 213;
        dictionary[214][0] = 214;
        dictionary[215][0] = 215;
        dictionary[216][0] = 216;
        dictionary[217][0] = 217;
        dictionary[218][0] = 218;
        dictionary[219][0] = 219;
        dictionary[220][0] = 220;
        dictionary[221][0] = 221;
        dictionary[222][0] = 222;
        dictionary[223][0] = 223;
        dictionary[224][0] = 224;
        dictionary[225][0] = 225;
        dictionary[226][0] = 226;
        dictionary[227][0] = 227;
        dictionary[228][0] = 228;
        dictionary[229][0] = 229;
        dictionary[230][0] = 230;
        dictionary[231][0] = 231;
        dictionary[232][0] = 232;
        dictionary[233][0] = 233;
        dictionary[234][0] = 234;
        dictionary[235][0] = 235;
        dictionary[236][0] = 236;
        dictionary[237][0] = 237;
        dictionary[238][0] = 238;
        dictionary[239][0] = 239;
        dictionary[240][0] = 240;
        dictionary[241][0] = 241;
        dictionary[242][0] = 242;
        dictionary[243][0] = 243;
        dictionary[244][0] = 244;
        dictionary[245][0] = 245;
        dictionary[246][0] = 246;
        dictionary[247][0] = 247;
        dictionary[248][0] = 248;
        dictionary[249][0] = 249;
        dictionary[250][0] = 250;
        dictionary[251][0] = 251;
        dictionary[252][0] = 252;
        dictionary[253][0] = 253;
        dictionary[254][0] = 254;
        dictionary[255][0] = 255;

        dfree = 256;
        psize = esize = top = tc = 0;
        chnext = 0;
    }

    /**
     * Calculates entropy of the source
     */
    private float calcEntropy() {
        float sum = 0.0f;
        float inf = 0.0f;

        for (int i = 0; i < 256; i++) {
            chprobs[i] = (float) chfreq[i] / (float) tc;
            if (chprobs[i] > 0) {
                inf = chprobs[i] * ((float) Math.log((1 / (double) chprobs[i])) / (float) Math.log(2));
                sum += inf;
            }
        }

        return sum;
    }

    /**
     * Calculates average code length
     */
    private float calcAvCodeLength() {
        int t = 0;
        int sum = 0;

        for (int i = 0; i < 4000; i++) {
            if (ofreq[i] > 0) {
                if (i < 256) {
                    t = ofreq[i];
                } else {
                    t = 3 * ofreq[i];
                }

                sum += t;
            }
        }

        return (float) sum / (float) top;
    }

    /**
     * Checks whether a character from input file has already been read
     */
    private int inchars(final int x) {
        for (int i = 0; i < chnext; i++) {
            if (chars[i] == x) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Checks whether a pattern exist in the dictionary
     */
    private boolean indict(final int x) {
        pattern[psize] = x;
        psize++;
        int index = -1;

        index = inchars(x);

        if (index != -1) {
            chfreq[index]++;
            tc++;
        } else {
            chars[chnext] = x;
            chfreq[chnext]++;
            chnext++;
            tc++;
        }

        for (int i = 0; i < 4000; i++) {
            if (pattern[0] == dictionary[i][0]) {
                int n = 1;

                for (int j = 1; j < psize; j++) {
                    if (pattern[j] == dictionary[i][j]) {
                        n++;
                    }
                }

                if (n == psize) {
                    return true;
                }
            }
        }

        pattern[psize - 1] = -1;
        psize--;
        return false;
    }

    /**
     * Gets dictionary index of the pattern
     */
    private void index(final OutputStream out) throws IOException {

        for (int i = 0; i < 4000; i++) {
            if (pattern[0] == dictionary[i][0]) {
                int n = 1;

                for (int j = 1; j < psize; j++) {
                    if (pattern[j] == dictionary[i][j]) {
                        n++;
                    }
                }

                if (n == psize) {
                    ofreq[i]++;
                    top++;

                    if (i > 255) {
                        int k = 0;
                        out.write(236);

                        while (i > 255) {
                            i -= 255;
                            k++;
                        }

                        out.write(k);
                    }

                    out.write(i);
                    return;
                }
            }
        }
    }

    /**
     * Adds pattern to the dictionary
     */
    private void add(final int x) {
        for (int i = 0; i < psize; i++) {
            dictionary[dfree][i] = pattern[i];
        }
        dictionary[dfree][psize] = x;
        dfree++;
    }

    /**
     * Clears the pattern
     */
    private void clear() {
        for (int i = 0; i < psize; i++) {
            pattern[i] = -1;
        }
        psize = 0;
    }

    /**
     * Compresses given input to specified output stream.
     *
     * @param in The input stream to compress
     * @param out The compressed output stream
     * @throws IOException If an I/O error occurs
     */
    public void compress(final InputStream in, final OutputStream out) throws IOException {
        try {
            int x = in.read();
            while (x != -1) {
                if (indict(x)) {
                    ;
                } else {
                    index(out);
                    add(x);
                    clear();
                    psize = 0;
                    pattern[0] = x;
                    psize++;
                }

                x = in.read();
            }
            out.flush();
            index(out);
        } finally {
            close(in);
            close(out);
        }
    }

    /**
     * Decompresses given input to specified output stream.
     *
     * @param in The input stream to decompress
     * @param out The decompressed output stream
     * @throws IOException If an I/O error occurs
     */
    public void decompress(final InputStream in, final OutputStream out) throws IOException {
        try {
            int x = in.read();
            if (x != -1) {
                element[0] = dictionary[x][0];
                esize++;
                out.write(dictionary[x][0]);
            }
            pattern[0] = element[0];
            psize++;
            while ((x = in.read()) != -1) {
                if (x == 236) {
                    final int k = in.read();
                    final int l = in.read();
                    x = k * 255 + l;
                }

                if (x >= dfree) {
                    for (int i = 0; i < esize; i++) {
                        element[i] = -1;
                    }
                    esize = 0;
                    for (int i = 0; i < psize; i++) {
                        element[i] = pattern[i];
                        esize++;
                    }
                    element[esize] = pattern[0];
                    esize++;
                } else {
                    for (int i = 0; i < esize; i++) {
                        element[i] = -1;
                    }
                    esize = 0;
                    for (int i = 0; i < 4000; i++) {
                        if (dictionary[x][i] != -1) {
                            element[i] = dictionary[x][i];
                            esize++;
                        } else {
                            break;
                        }

                    }
                }

                for (int i = 0; i < esize; i++) {
                    out.write(element[i]);
                }
                out.flush();
                for (int i = 0; i < psize; i++) {
                    dictionary[dfree][i] = pattern[i];
                }
                dictionary[dfree][psize] = element[0];
                dfree++;
                clear();
                for (int i = 0; i < esize; i++) {
                    pattern[i] = element[i];
                    psize++;
                }
            }
        } finally {
            close(in);
            close(out);
        }
    }

    private static void close(final Closeable closeable) {
        Streams.close(closeable);
    }

    public static void main(final String args[]) throws Exception {
        String infile, outfile;
        final Scanner cons = new Scanner(System.in);
        int ch = -1;

        System.out.println("Implementation of LZW compression and decompression algorithm\n\n");

        while (ch != 3) {
            System.out.println("1. Compress a file");
            System.out.println("2. Decompress a file");
            System.out.println("3. Exit\n");
            System.out.print("Option: ");
            ch = Integer.parseInt(cons.nextLine());
            System.out.println();

            if (ch == 1) {
                System.out.print("Input file: ");
                infile = cons.nextLine();
                System.out.print("Output file: ");
                outfile = cons.nextLine();
                final LzwCompression lzw = new LzwCompression();
                lzw.init();
                lzw.compress(new FileInputStream(infile), new FileOutputStream(outfile));
                final File in = new File(infile);
                final File out = new File(outfile);
                final float cr = (float) out.length() / (float) in.length();
                final float entropy = lzw.calcEntropy();
                final float acl = lzw.calcAvCodeLength();
                System.out.println();
                System.out.print("Entropy of the source: " + entropy + "\n");
                System.out.print("Compression ratio: " + cr + "\n");
                System.out.print("Average code length in bytes: " + acl + "\n\n");
            } else if (ch == 2) {
                System.out.print("Input file: ");
                infile = cons.nextLine();
                System.out.print("Output file: ");
                outfile = cons.nextLine();
                final LzwCompression lzw = new LzwCompression();
                lzw.init();
                lzw.decompress(new FileInputStream(infile), new FileOutputStream(outfile));
                System.out.print("\n\n");
            }
        }
    }
}

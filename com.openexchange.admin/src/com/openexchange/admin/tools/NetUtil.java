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

package com.openexchange.admin.tools;

import com.openexchange.admin.rmi.exceptions.InvalidDataException;

/**
 * @author choeger
 *
 */
public class NetUtil {

    /**
     * INTERNAL: check if address or mask is a valid dotted decimal notation
     *
     * @param qdot
     * @return
     */
    private final static boolean isValidDDN(final String qdot) {
        if (qdot.length() == 0) {
            return false;
        }
        if (qdot.replaceAll("[0-9.]", "").length() > 0) {
            return false;
        }
        final String[] sddn = qdot.split("\\.");
        if (sddn.length != 4) {
            return false;
        }
        for (final String p : sddn) {
            final int ip = Integer.parseInt(p);
            if (ip < 0 || ip > 255) {
                return false;
            }
        }
        return true;
    }

    /**
     * INTERNAL: convert ddn string representation to int array ddn
     *
     * @param ddn
     * @return
     */
    private final static int[] stringDDN2Int(final String ddn) {
        int[] ret = new int[4];
        final String[] ddnarr = ddn.split("\\.");
        for (int i = 0; i < 4; i++) {
            ret[i] = Integer.parseInt(ddnarr[i]);
        }
        return ret;
    }

    /**
     * INTERNAL: convert ddn to long
     *
     * @param ddn
     * @return
     */
    private final static long stringDDN2Long(final String ddn) {
        long ret = 0;
        int shift = 3;
        for (final String part : ddn.split("\\.")) {
            long lpart = Long.parseLong(part) << (shift-- * 8);
            ret |= lpart;
        }
        return ret;
    }

    /**
     * check if broadcast address matches with given network and netmask
     * network could also be an ip address fitting in the network
     *
     * @param broadcast
     * @param net
     * @param mask
     * @return
     */
    public final static boolean isValidBroadcast(final String broadcast, final String net, final String mask) {
        if (net == null || mask == null || broadcast == null) {
            return false;
        }
        if (!isValidDDN(net) || !isValidDDN(mask) || !isValidDDN(broadcast)) {
            return false;
        }

        long lmask = stringDDN2Long(mask);
        long lnet = stringDDN2Long(net);
        long lbcast = stringDDN2Long(broadcast);

        long invlmask = 0x00000000ffffff & ~lmask;

        if ((lnet & lmask | invlmask) == lbcast) {
            return true;
        }

        return false;
    }

    /**
     * check if mask is a valid netmask in dotted decimal notation.
     *
     * @param mask
     * @return
     */
    public final static boolean isValidNetmask(final String mask) {
        if (mask == null) {
            return false;
        }
        if (isValidDDN(mask)) {
            int[] imask = stringDDN2Int(mask);

            // do the real check:
            // there must not follow a '1' after a '0' in a netmask
            boolean foundZero = false;
            for (int p = 0; p < 4; p++) {
                for (int bs = 7; bs >= 0; bs--) {
                    final int bit = 1 << bs;
                    final int erg = (imask[p] & bit);
                    if (erg == 0) {
                        foundZero = true;
                    } else if (erg == bit && foundZero) {
                        return false;
                    }
                }
            }

            return true;
        }
        return false;
    }

    /**
     * check if address is a valid ip address in dotted decimal notation.
     *
     * @param address
     * @return
     */
    public final static boolean isValidIPAddress(final String address) {
        if (address == null) {
            return false;
        }
        return isValidDDN(address);
    }

    /**
     * check if ipmask is a valid network definition either in cidr or dotted
     * decimal notation.
     *
     * @param ipmask
     * @return
     */
    public final static boolean isValidIPNetmask(final String ipmask) {
        if (ipmask == null) {
            return false;
        }
        int pos;
        if ((pos = ipmask.indexOf('/')) < 0) {
            return false;
        }
        final String ip = ipmask.substring(0, pos);
        if (!isValidIPAddress(ip)) {
            return false;
        }
        final String mask = pos < ipmask.length() - 1 ? ipmask.substring(pos + 1) : "";
        if (mask.indexOf('.') >= 0) {
            return isValidNetmask(mask);
        }
        if (mask.replaceAll("[0-9]", "").length() > 0) {
            return false;
        }
        return true;
    }

    /**
     * check if ipmask is a valid network definition either in cidr or dotted
     * decimal notation.
     *
     * @param ipmask
     * @throws InvalidDataException
     */
    public final static void checkValidIPNetmask(final String ipmask) throws InvalidDataException {
        if (!isValidIPNetmask(ipmask)) {
            throw new InvalidDataException(ipmask + " is not a valid network definition");
        }
    }

    /**
     * return dotted decimal notation representation as a String of the CIDR
     * representation of the netmask
     *
     * @param cidr
     * @return
     */
    public final static String CIDR2Mask(final int cidr) {
        int mask = cidr;
        StringBuilder retBuilder = new StringBuilder();
        for (int p = 0; p < 4; p++) {
            int bitset = 0;
            for (int bs = 7; bs >= 0; bs--) {
                if (mask > 0) {
                    bitset |= (1 << bs);
                    mask--;
                }
            }
            retBuilder.append(bitset).append(p < 3 ? "." : "");
        }
        return retBuilder.toString();
    }
}

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

package com.openexchange.imap.cache;

import static org.junit.Assert.assertNull;
import org.junit.Test;

/**
 * {@link ListLsubCollectionTest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class ListLsubCollectionTest {

    /**
     * Initializes a new {@link ListLsubCollectionTest}.
     */
    public ListLsubCollectionTest() {
        super();
    }

    @Test
    public void testCollectionContent() throws Exception {
        String rootList = "* LIST (\\Noselect) \"/\" \"\"\n" +
            "s OK Completed (0.000 secs)";
        String allList = "* LIST (\\HasChildren) \"/\" INBOX\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/Drafts\n" +
            "* LIST (\\HasNoChildren) \"/\" \"INBOX/Neuer Ordner\"\n" +
            "* LIST (\\HasChildren) \"/\" INBOX/RECOVERY-20161025T130142\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/Drafts\n" +
            "* LIST (\\HasNoChildren) \"/\" \"INBOX/RECOVERY-20161025T130142/Neuer Ordner\"\n" +
            "* LIST (\\HasChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/Drafts\n" +
            "* LIST (\\HasNoChildren) \"/\" \"INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/Neuer Ordner\"\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/SPAM\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/Sent\n" +
            "* LIST (\\HasNoChildren) \"/\" \"INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/Sent Items\"\n" +
            "* LIST (\\HasChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/Trash\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/Trash/Drafts\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/Trash/INBOX\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/Trash/SPAM\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/Trash/Sent\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/Trash/Trash\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/Trash/ghh\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/Trash/gmail431\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/Trash/li\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/Trash/rul6rl\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/VIRUS\n" +
            "* LIST (\\HasChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/gmx\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/gmx/Drafts\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/gmx/INBOX\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/gmx/SPAM\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/gmx/Sent\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/gmx/Trash\n" +
            "* LIST (\\HasChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/htptestipgoogle\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/htptestipgoogle/Entw&APw-rfe\n" +
            "* LIST (\\HasNoChildren) \"/\" \"INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/htptestipgoogle/Gesendete Objekte\"\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/htptestipgoogle/INBOX\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/htptestipgoogle/Papierkorb\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/htptestipgoogle/Spam\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/jupjup\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/RECOVERY-20161024T192425/sss\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/SPAM\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/Sent\n" +
            "* LIST (\\HasNoChildren) \"/\" \"INBOX/RECOVERY-20161025T130142/Sent Items\"\n" +
            "* LIST (\\HasChildren) \"/\" INBOX/RECOVERY-20161025T130142/Trash\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/Trash/Drafts\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/Trash/INBOX\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/Trash/SPAM\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/Trash/Sent\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/Trash/Trash\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/Trash/ghh\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/Trash/gmail431\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/Trash/li\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/Trash/rul6rl\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/VIRUS\n" +
            "* LIST (\\HasChildren) \"/\" INBOX/RECOVERY-20161025T130142/gmx\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/gmx/Drafts\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/gmx/INBOX\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/gmx/SPAM\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/gmx/Sent\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/gmx/Trash\n" +
            "* LIST (\\HasChildren) \"/\" INBOX/RECOVERY-20161025T130142/htptestipgoogle\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/htptestipgoogle/Entw&APw-rfe\n" +
            "* LIST (\\HasNoChildren) \"/\" \"INBOX/RECOVERY-20161025T130142/htptestipgoogle/Gesendete Objekte\"\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/htptestipgoogle/INBOX\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/htptestipgoogle/Papierkorb\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/htptestipgoogle/Spam\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/jupjup\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/RECOVERY-20161025T130142/sss\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/SPAM\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/Sent\n" +
            "* LIST (\\HasNoChildren) \"/\" \"INBOX/Sent Items\"\n" +
            "* LIST (\\HasChildren) \"/\" INBOX/Trash\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/Trash/Drafts\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/Trash/INBOX\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/Trash/L&APY-schen\n" +
            "* LIST (\\HasChildren) \"/\" \"INBOX/Trash/Neuer Ordner\"\n" +
            "* LIST (\\HasNoChildren) \"/\" \"INBOX/Trash/Neuer Ordner/ordner.\"\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/Trash/SPAM\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/Trash/Sent\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/Trash/Trash\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/Trash/ghh\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/Trash/gmail431\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/Trash/li\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/Trash/rul6rl\n" +
            "* LIST (\\HasChildren) \"/\" INBOX/gmx\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/gmx/Drafts\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/gmx/INBOX\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/gmx/SPAM\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/gmx/Sent\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/gmx/Trash\n" +
            "* LIST (\\HasChildren) \"/\" INBOX/htptestipgoogle\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/htptestipgoogle/Entw&APw-rfe\n" +
            "* LIST (\\HasNoChildren) \"/\" \"INBOX/htptestipgoogle/Gesendete Objekte\"\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/htptestipgoogle/INBOX\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/htptestipgoogle/Papierkorb\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/htptestipgoogle/Spam\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/jupjup\n" +
            "* LIST (\\HasNoChildren) \"/\" \"INBOX/neuer ordnertest\"\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/quatsch\n" +
            "* LIST (\\HasNoChildren) \"/\" INBOX/sss\n" +
            ". OK Completed (0.020 secs 98 calls)";
        String allLsub = "* LSUB (\\HasChildren) \"/\" INBOX\n" +
            "* LSUB () \"/\" INBOX/Drafts\n" +
            "* LSUB () \"/\" \"INBOX/Neuer Ordner\"\n" +
            "* LSUB (\\HasChildren) \"/\" INBOX/SPAM\n" +
            "* LSUB () \"/\" INBOX/Sent\n" +
            "* LSUB () \"/\" \"INBOX/Sent Items\"\n" +
            "* LSUB (\\HasChildren) \"/\" INBOX/Trash\n" +
            "* LSUB () \"/\" INBOX/Trash/L&APY-schen\n" +
            "* LSUB (\\HasChildren) \"/\" \"INBOX/Trash/Neuer Ordner\"\n" +
            "* LSUB () \"/\" \"INBOX/Trash/Neuer Ordner/ordner.\"\n" +
            "* LSUB () \"/\" INBOX/Trash/ghh\n" +
            "* LSUB () \"/\" INBOX/Trash/gmail431\n" +
            "* LSUB () \"/\" INBOX/Trash/li\n" +
            "* LSUB () \"/\" INBOX/Trash/rul6rl\n" +
            "* LSUB () \"/\" INBOX/VIRUS\n" +
            "* LSUB () \"/\" INBOX/jupjup\n" +
            "* LSUB () \"/\" \"INBOX/neuer ordnertest\"\n" +
            "* LSUB () \"/\" INBOX/sss\n" +
            ". OK Completed (0.050 secs 21 calls)";
        String namespace = "* NAMESPACE ((\"INBOX/\" \"/\")) ((\"user/\" \"/\")) ((\"\" \"/\"))\n" +
            ". OK Completed";

        ListLsubCollection collection = ListLsubCollection.craftListLsubCollectionFrom(rootList, allList, allLsub, namespace);

        ListLsubEntry entry = collection.getLsub("user");
        assertNull("\"user\" LSUB entry should not exist", entry);
    }

}

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

#@(#) Tables for the chat.

CREATE TABLE chat (
 cid INT4 unsigned NOT NULL,
 user INT4 unsigned NOT NULL,
 chatId INT4 unsigned NOT NULL,
 subject VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 createdAt BIGINT(64) DEFAULT NULL,
 PRIMARY KEY (cid, chatId),
 INDEX `user` (cid, user)
 -- FOREIGN KEY (cid, user) REFERENCES user (cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE chatMember (
 cid INT4 unsigned NOT NULL,
 user INT4 unsigned NOT NULL,
 chatId INT4 unsigned NOT NULL,
 opMode INT4 unsigned NOT NULL,
 PRIMARY KEY (cid, user, chatId),
 INDEX `user` (cid, user)
 -- FOREIGN KEY (cid, user) REFERENCES user (cid, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE chatMessage (
 cid INT4 unsigned NOT NULL,
 user INT4 unsigned NOT NULL,
 chatId INT4 unsigned NOT NULL,
 messageId BINARY(16) NOT NULL,
 message TEXT NOT NULL,
 createdAt BIGINT(64) DEFAULT NULL,
 PRIMARY KEY (cid, chatId, messageId),
 INDEX `user` (cid, user),
 INDEX `chat` (cid, chatId),
 INDEX `userMessage` (cid, user, chatId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE chatMessageMap (
 cid INT4 unsigned NOT NULL,
 chatId INT4 unsigned NOT NULL,
 customId VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 messageId BINARY(16) NOT NULL,
 PRIMARY KEY (cid, chatId, messageId),
 INDEX `chat` (cid, chatId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE chatPresence (
 cid INT4 unsigned NOT NULL,
 user INT4 unsigned NOT NULL,
 type INT4 unsigned NOT NULL DEFAULT 0, -- AVAILABLE
 mode INT4 unsigned NOT NULL DEFAULT 1, -- AVAILABLE
 statusMessage VARCHAR(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
 lastModified BINARY(16) NOT NULL,
 PRIMARY KEY (cid, user),
 INDEX `available` (cid, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;


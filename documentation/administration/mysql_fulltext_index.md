---
title: MySQL Fulltext Index
icon: fas fa-file-alt
tags: Administration, Database, Contacts, Files, Infostore
---

# Introduction

*Requirement*: In order to use the ``FULLTEXT`` index capability, an appropriate MySQL version needs to be in place. ``FULLTEXT`` indexes are supported starting with MySQL v5.6.4 (see http://dev.mysql.com/doc/refman/5.6/en/fulltext-restrictions.html for details).

## Contacts module
With Open-Xchange Server v7.8.1 the MySQL ``FULLTEXT`` index is supported for retrieving auto-complete results. Once enabled, an appropriate index is created on the ``prg_contacts`` table automatically and is used afterwards to serve the "find as you type" auto-completion requests in a more efficient way. 

Moreover, using a MySQL ``FULLTEXT`` index provides improved results when searching for "tokens". For instance, an E-Mail address gets tokenized in the following way:

```
jane.doe@somewhere.com
yields the tokens: "jane", "doe", "somewhere", and "com"
```

Thus a user is able to start typing e.g. *somewhere* or *doe* to hit that search result. Without ``FULLTEXT`` index support, the user is supposed to enter *jane* to get that hit.

## Files module
With Open-Xchange server v7.10.5 another ``FULLTEXT`` index to improve searching for files in the Drive module was introduced. This index is created on the ``infostore_document`` table as soon as this feature is enabled via configuration.

This index significantly improves the duration of a search request, especially if many files are stored. Please note that the ``FULLTEXT`` index uses "tokens", the file ``very_important_document.pdf`` yields the tokens ``very``, ``important``, ``document``, ``pdf``. Searching for *important* or *document* will return this document as result, but searching for *ocument* does not.

# Enabling usage of MySQL ``FULLTEXT`` index

## Contacts module
For enabling the usage for a MySQL ``FULLTEXT`` index the property ``com.openexchange.contact.fulltextAutocomplete`` needs to be set to ``true`` and a restart is supposed to be performed. Once set to ``true``, an appropriate update task (``com.openexchange.contact.storage.rdb.groupware.AddFulltextIndexTask``) gets executed on next login attempts for associated database schemas.

An administrator can even influence what fields are supposed to be considered for ``FULLTEXT``-backed auto-complete executions by modifying the ``com.openexchange.contact.fulltextIndexFields`` property.

**Please Note:** As explained above, the update task gets only executed once (the time when a first login attempt for an associated database schema happens). In consequence, modifying the ``com.openexchange.contact.fulltextIndexFields`` property later on has no effect (even a restart does not get the changes applied). In order to apply the changes applied to ``com.openexchange.contact.fulltextIndexFields`` property, the associated update task is required being re-executed using the ``forceupdatetask`` command-line tool:

```
/opt/open-xchange/sbin/forceupdatetask --task com.openexchange.contact.storage.rdb.groupware.AddFulltextIndexTask <other command-line arguments>
```

Otherwise, the ``FULLTEXT`` index will stop working.

## Files module
For enabling the usage for ``FULLTEXT`` index in the files module, the property ``com.openexchange.infostore.fulltextSearch`` needs to be set to ``true``. This registers an update task (``InfostoreDocumentAddFulltextIndexUpdateTask``) at the next server restart, which creates the index after the next login attempt for associated database schemas.

Additional the minimum search pattern length to use the ``FULLTEXT`` index can be configured with the property ``com.openexchange.infostore.fulltextSearchMinimumPatternLength``, which defaults to 3. This value should be adjusted to the ``innodb_ft_min_token_size`` MySQL property. **Note:** Changing MySQL properties most likely requires the ``FULLTEXT`` index to be rebuild. See 'MySQL configuration options' section.


# MySQL configuration options

An administrator may want to change how the MySQL ``FULLTEXT`` index works. MySQL only supports to change [[http://dev.mysql.com/doc/refman/5.7/en/fulltext-fine-tuning.html|minimum/maximum word length]] and [[http://dev.mysql.com/doc/refman/5.7/en/fulltext-stopwords.html|stop-words list]] for the InnoDB storage engine.

The MySQL default value for the minimum word length is set to 3. In case users should be allowed to also retrieve results when typing less than 3 characters, the ``innodb_ft_min_token_size`` needs to be changed accordingly in the MySQL configuration file:

```
innodb_ft_min_token_size=2
```

**Please Note:** Changing any of the MySQL configuration options requires that ``FULLTEXT`` indexes are re-created. Taken from MySQL reference manual:

> [...]  
> Rebuilding InnoDB Full-Text Indexes
> 
> If you modify full-text variables that affect indexing (``innodb_ft_min_token_size, innodb_ft_max_token_size, innodb_ft_server_stopword_table, innodb_ft_user_stopword_table, innodb_ft_enable_stopword, ngram_token_size``) you must rebuild your ``FULLTEXT`` indexes after making the changes. Modifying the ``innodb_ft_min_token_size, innodb_ft_max_token_size``, or ``ngram_token_size`` variables, which cannot be set dynamically, require restarting the server and rebuilding the indexes.
> 
> To rebuild the ``FULLTEXT`` indexes for an InnoDB table, use ``ALTER TABLE`` with the ``DROP INDEX`` and ``ADD INDEX`` options to drop and re-create each index.  
> [...]

Thus an administrator may manually drop and re-add the index or simply re-execute the associated update tasks (``com.openexchange.contact.storage.rdb.groupware.AddFulltextIndexTask`` and ``InfostoreDocumentAddFulltextIndexUpdateTask``) as explained above using ``forceupdatetask`` command-line tool.

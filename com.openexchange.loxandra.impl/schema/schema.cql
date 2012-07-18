CREATE KEYSPACE OX WITH strategy_class = 'SimpleStrategy' AND strategy_options:replication_factor = 3;

USE OX;

CREATE COLUMNFAMILY Counters (KEY varchar PRIMARY KEY) WITH default_validation = CounterColumnType and comparator = UUIDType AND gc_grace_seconds = 300;
CREATE COLUMNFAMILY Person (KEY uuid PRIMARY KEY) with comparator = 'CompositeType(UTF8Type, UTF8Type)' and default_validation = BytesType AND comment = 'Person records' AND gc_grace_seconds = 300;
CREATE COLUMNFAMILY PersonFolder (KEY uuid PRIMARY KEY) with comparator = 'CompositeType(UTF8Type, UTF8Type)' AND default_validation = BytesType AND comment = 'Person folder records' AND gc_grace_seconds = 300;
//CREATE COLUMNFAMILY PersonFolderPermissions (KEY uuid PRIMARY KEY) WITH default_validation = BytesType AND comment = 'Permissions for person folders' AND gc_grace_seconds = 300;

// First attempt for transactions
// Column name consists out of 3 components (thus a composite type): OpSeqNum, Column Family Name, Column Name (if composite, components separated by ":", without the ").
CREATE COLUMNFAMILY TransactionLog (KEY uuid PRIMARY KEY) with comparator = 'CompositeType(UTF8Type, UTF8Type)' AND default_validation = BytesType AND comment = 'Transaction Log' AND gc_grace_seconds = 300;

//CREATE COLUMNFAMILY Attachment;
//CREATE COLUMNFAMILY InfoStore;
//CREATE COLUMNFAMILY Task;

// the person-person mapping, aka who knows whom aka the contact list for every person
// the KEY is the UUID of the Person who has contacts
//CREATE COLUMNFAMILY Person_knows_Person (KEY uuid PRIMARY KEY) WITH comparator = 'CompositeType (UTF8Type, UTF8Type)' AND default_validation = BytesType;

// Persons who exist within a specific context, CONTEXT is the UUID of the context
//CREATE COLUMNFAMILY PersonContext (KEY uuid PRIMARY KEY);

CREATE COLUMNFAMILY session (KEY varchar PRIMARY KEY) WITH comparator = varchar AND default_validation = BytesType AND gc_grace_seconds = 300;
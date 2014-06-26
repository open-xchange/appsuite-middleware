# Introduction
The OX DB Service aims to give access to OX SQL databases by way of an http based API. Using a PUT request, clients can query or update the OX user data databases or the configdb. Additionally, this service contains a mechanism for updating a database with new tables. Usually this service listens at an OX host port 8009 under /preliminary/database, but this can vary depending on the network setup, so its best to keep this value in a configuration option for your service.

# Reading from the databases

Databases in an OX setup are usually set up in a master/slave setup. This means that you do all reads from the slave dbs and all writes on the database master. Depending on the kinds of queries you want to send, you have to pick the url that matches either a write or a read operation. You can also choose to either contact the configdb or an ox database assigned to a context.

## Reading from the configdb

Let's select some metadata about the first 3 contexts from the database:

Query:
PUT http://localhost:8009/preliminary/database/v1/configdb/readOnly

SELECT * FROM context ORDER BY cid LIMIT 3;

Response:
{
    "results": {
        "result": {
            "rows": [
                {
                    "filestore_id": 4,
                    "enabled": true,
                    "quota_max": 1073741824,
                    "filestore_login": null,
                    "filestore_passwd": null,
                    "name": "test@test@test",
                    "filestore_name": "1_ctx_store",
                    "reason_id": null,
                    "cid": 1
                },
                {
                    "filestore_id": 4,
                    "enabled": true,
                    "quota_max": 1048576000,
                    "filestore_login": null,
                    "filestore_passwd": null,
                    "name": "5",
                    "filestore_name": "5_ctx_store",
                    "reason_id": null,
                    "cid": 5
                },
                {
                    "filestore_id": 4,
                    "enabled": true,
                    "quota_max": 10485760,
                    "filestore_login": null,
                    "filestore_passwd": null,
                    "name": "6",
                    "filestore_name": "6_ctx_store",
                    "reason_id": null,
                    "cid": 6
                }
            ]
        }
    }
}

The query is addressed to /preliminary/database/v1/configdb/readOnly, meaning we want the slave (the 'readOnly' part of the URL) for the configdb. The query is simply PUT verbatim as a string. Note though, that for using substitutions – and that's something you want to use for parameters from the scary internet to avoid SQL Injection – you'll have to send a more complex JSON structure as a query. We'll talk about that later. 

The response contains all rows as JSON objects with the columns as keys, and the values as values, somewhat deeply nested in response.results.result.rows. 

## Reading from the OX db

Let's query an OX DB: 

PUT http://localhost:8009/preliminary/database/v1/oxdb/1/readOnly

SELECT id, mail, preferredLanguage FROM user WHERE cid=1;

Response:

{
    "results": {
        "result": {
            "rows": [
                {
                    "id": 2,
                    "mail": "admin@ox.invalid",
                    "preferredLanguage": "en_US"
                },
                {
                    "id": 3,
                    "mail": "zig@ox.invalid",
                    "preferredLanguage": "en_US"
                },
                {
                    "id": 4,
                    "mail": "zag@ox.invalid",
                    "preferredLanguage": "en_US"
                }
            ]
        }
    }
}

The query this time is addressed to /preliminary/database/v1/oxdb/1/readOnly. "oxdb" in the URL denotes that we want to query the OX user data database, the "1" is the contextId whose database we want to connect to, "readOnly" again means, we want to talk to the reading slave.

## Using Substitutions and Prepared Statements

As said before, when adding variables to a query, it's best to use the built-in features of JDBC and use substitutions and prepared statements. Let's do this with the above query and use a substitution for the cid:


PUT http://localhost:8009/preliminary/database/v1/oxdb/1/readOnly

{
	"allUsers": {
		"query": "SELECT id, mail, preferredLanguage FROM user WHERE cid = ?",
		"params": [1]
	}
}

Which gives as a response:

{
    "results": {
        "allUsers": {
            "rows": [
                {
                    "id": 2,
                    "mail": "admin@ox.invalid",
                    "preferredLanguage": "en_US"
                },
                {
                    "id": 3,
                    "mail": "zig@ox.invalid",
                    "preferredLanguage": "en_US"
                },
                {
                    "id": 4,
                    "mail": "zag@ox.invalid",
                    "preferredLanguage": "en_US"
                }
            ]
        }
    }
}

Let's look at the request first. Clients need to PUT a JSON Object that can contain one or more queries, in our example, just one called "allUsers". The Object below "allUsers" has the attributes "query", containing the SQL query with the question marks and "params" which are the substitutions for the query, in the same order as the question marks appear. Just like you would use a PreparedStatement in JDBC (that's actually how it's implemented). 

In the response, the query name ("allUsers") appears as the key containing the resulting rows.

Let's try a more complex query, with more substitutions. This time we're selecting all aliases of user 3:

PUT http://localhost:8009/preliminary/database/v1/oxdb/1/readOnly

{
	"aliases": {
		"query": "SELECT value FROM user_attribute WHERE cid = ? AND id = ? AND name = ?",
		"params": [1, 3, "alias"]
	}
}

Responds with:

{
    "results": {
        "aliases": {
            "rows": [
                {
                    "value": "zig@ox.invalid"
                },
                {
                    "value": "zig@zigzag.com"
                }
            ]
        }
    }
}

## Sending multiple statements in a request

We can send multiple queries (and updates) in the same request:

PUT http://localhost:8009/preliminary/database/v1/oxdb/1/readOnly

{
    "allUsers": {
		"query": "SELECT id, mail, preferredLanguage FROM user WHERE cid = ?",
		"params": [1]
	},
	"aliases": {
		"query": "SELECT value FROM user_attribute WHERE cid = ? AND id = ? AND name = ?",
		"params": [1, 3, "alias"]
	}
}

Responds with:

{
    "results": {
        "allUsers": {
            "rows": [
                {
                    "id": 2,
                    "mail": "admin@ox.invalid",
                    "preferredLanguage": "en_US"
                },
                {
                    "id": 3,
                    "mail": "zig@ox.invalid",
                    "preferredLanguage": "en_US"
                },
                {
                    "id": 4,
                    "mail": "zag@ox.invalid",
                    "preferredLanguage": "en_US"
                }
            ]
        },
        "aliases": {
            "rows": [
                {
                    "value": "zig@ox.invalid"
                },
                {
                    "value": "zig@zigzag.com"
                }
            ]
        }
    }
}

The response then contains two keys, one for each query.

## What about errors?

Errors in the SQL syntax, or connection problems will cancel all requests and return the error: 

PUT http://localhost:8009/preliminary/database/v1/oxdb/1/readOnly

{
    "allUsers": {
		"query": "SELECT id, mail, preferredLanguage FROM user WHERE cid = ?",
		"params": [1]
	},
	"aliases": {
		"query": "SELECT value FROM userAttribute WHERE cid = ? AND id = ? AND name = ?",
		"params": [1, 3, "alias"]
	}
}

Responds with:

{
    "error": "Table 'db_5.userattribute' doesn't exist",
    "results": {
        "allUsers": {
            "rows": [
                {
                    "id": 2,
                    "mail": "admin@ox.invalid",
                    "preferredLanguage": "en_US"
                },
                {
                    "id": 3,
                    "mail": "zig@ox.invalid",
                    "preferredLanguage": "en_US"
                },
                {
                    "id": 4,
                    "mail": "zag@ox.invalid",
                    "preferredLanguage": "en_US"
                }
            ]
        },
        "aliases": {
            "error": "Table 'db_5.userattribute' doesn't exist",
            "query": "SELECT value FROM userAttribute WHERE cid = ? AND id = ? AND name = ?"
        }
    }
}

A typo in the second query aborted the request and returns the error, as seen in the "aliases" object.

## What about long results?

The DatabaseService imposes some limits on clients. A client can only issue 100 statements in a single request and a result can only contain 1000 rows, the system tells us about it when it shortens the response:

PUT http://localhost:8009/preliminary/database/v1/configdb/readOnly

SELECT * FROM context ORDER BY cid LIMIT 2000;

Responds with:

{
    "results": {
        "result": {
            "exceeded": true,
            "rows": [
                {
                    "filestore_id": 4,
                    "enabled": true,
                    "quota_max": 1073741824,
                    "filestore_login": null,
                    "filestore_passwd": null,
                    "name": "test@test@test",
                    "filestore_name": "1_ctx_store",
                    "reason_id": null,
                    "cid": 1
                },
                {
                    "filestore_id": 4,
                    "enabled": true,
                    "quota_max": 1048576000,
                    "filestore_login": null,
                    "filestore_passwd": null,
                    "name": "5",
                    "filestore_name": "5_ctx_store",
                    "reason_id": null,
                    "cid": 5
                }, 
								...
            		
            ]
        }
    }
}

The "result" object (or the object named after our query, "result" is just the default name) then contains the "exceeded" attribute.

# Writing to the databases

The equivalent to "readOnly" for querying the database is "writable" for issuing updates and inserts. Obviously you'll have to exercise caution when updating our tables. We'll look at how you can set up your own tables in the OX DBs later in this guide. For now let's insert an arbitrary value into the user_attribute table: 

PUT http://localhost:8009/preliminary/database/v1/oxdb/1/writable

{
	"insertAttribute": {
        "query": "INSERT INTO user_attribute (cid, id, name, value, uuid) VALUES (?,?,?,?,?)",
        "params": [1, 3, "com.openexchange.example.preferredGreeting", "Aloha", "d8541b89624e"] 
	}
}

And the response:
{
    "results": {
        "insertAttribute": {
            "updated": 1
        }
    }
}

The response tells us that one row was changed as a result of the operation. Other than that, the query functions in much the same way as a read query.

## About transactions

When you send multiple updates in a request to a writable database, they are considered to be part of one transaction. A transaction is automatically committed after all requests have been sent. If an error occurs, the transaction is rolled back and all changes are undone. Let's insert two values into user_attributes: 

PUT 

{
	"insertDish": {
        "query": "INSERT INTO user_attribute (cid, id, name, value, uuid) VALUES (?,?,?,?,?)",
        "params": [1, 3, "com.openexchange.example.favoriteDish", "Sushi", "c8541b89624e"] 
	},
	"insertColor": {
        "query": "INSERT INTO user_attribute (cid, id, name, value, uuid) VALUES (?,?,?,?,?)",
        "params": [1, 3, "com.openexchange.example.favoriteColor", "Green", "b8541b89624e"] 
	}
}

Responds with:

{
    "results": {
        "insertDish": {
            "updated": 1
        },
        "insertColor": {
            "updated": 1
        }
    }
}

And, for fun, let's retrieve the values:

PUT http://localhost:8009/preliminary/database/v1/oxdb/1/readOnly

{
  "exampleAttributes": {
    "query": "SELECT name, value FROM user_attribute WHERE cid = ? AND id = ? AND name LIKE 'com.openexchange.example%'",
    "params": [1,3]
  }
}

Response: 
{
    "results": {
        "exampleAttributes": {
            "rows": [
                {
                    "name": "com.openexchange.example.favoriteColor",
                    "value": "Green"
                },
                {
                    "name": "com.openexchange.example.favoriteDish",
                    "value": "Sushi"
                },
                {
                    "name": "com.openexchange.example.preferredGreeting",
                    "value": "Aloha"
                }
            ]
        }
   


Let's see an automatic rollback:

PUT http://localhost:8009/preliminary/database/v1/oxdb/1/writable
{
  "exampleAttributes": {
    "query": "DELETE FROM user_attribute WHERE cid = ? AND id = ? AND name LIKE 'com.openexchange.example%'",
    "params": [1,3]
  },
  "failingQueryForcingRollback": {
    "query": "UPDATE tableThatDoesNotExist SET columnThatDoesNotExist = 12"
  }
}

Results in: 

{
    "error": "Table 'db_5.tablethatdoesnotexist' doesn't exist",
    "results": {
        "exampleAttributes": {
            "updated": 3
        },
        "failingQueryForcingRollback": {
            "error": "Table 'db_5.tablethatdoesnotexist' doesn't exist",
            "query": "UPDATE tableThatDoesNotExist SET columnThatDoesNotExist = 12"
        }
    }
}

Since also the DELETE was rolled back, when we reissue the select from above, all three values are still there. 


### Transactions that span multiple requests

You can keep a transaction open at the end of an request to get a transaction that spans multiple requests. This is useful, if, for example, you have to retrieve a value, to a computation on it, and then write it back. A transaction will be kept open, if you set the "keepOpen" parameter to "true" in your request: 

PUT http://localhost:8009/preliminary/database/v1/oxdb/1/writable?keepOpen=true

{
  "exampleAttributes": {
    "query": "DELETE FROM user_attribute WHERE cid = ? AND id = ? AND name LIKE 'com.openexchange.example%'",
    "params": [1,3]
  }
}

Returns:

{
    "tx": "b693848c1f534cde94c439a4abe2dbfc",
    "results": {
        "exampleAttributes": {
            "updated": 3
        }
    }
}

As you can see, apart from the result of the query, it also contains a transaction id as the "tx" attribute. We can use this to do further queries or updates in the same transaction:

PUT http://localhost:8009/preliminary/database/v1/transaction/b693848c1f534cde94c439a4abe2dbfc?keepOpen=true

{
  "exampleAttributes": {
    "query": "SELECT name, value FROM user_attribute WHERE cid = ? AND id = ? AND name LIKE 'com.openexchange.example%'",
    "params": [1,3],
    "resultSet": true
  }
}

Responds with:
{
    "tx": "b693848c1f534cde94c439a4abe2dbfc",
    "results": {
        "exampleAttributes": {
            "rows": []
        }
    }
}

Since the transaction was started on a write connection, we have to include the "resultSet" attribute in our query, to tell the server, that we would like to see a result set for our query. This, then returns no rows, as we've deleted all our custom attributes. The transaction is still being kept open, since we've also set the "keepOpen" parameter on the request. 

Let's finish off the transaction by issuing an INSERT and leaving off the keepOpen parameter. The transaction is then committed. 

PUT http://localhost:8009/preliminary/database/v1/transaction/b693848c1f534cde94c439a4abe2dbfc

{
	"insertAttribute": {
        "query": "INSERT INTO user_attribute (cid, id, name, value, uuid) VALUES (?,?,?,?,?)",
        "params": [1, 3, "com.openexchange.example.preferredGreeting", "Aloha", "d8541b89624e"] 
	}
}

You can also commit or roll back the transaction by issuing these requests: 

GET /transaction/[transactionId]/commit
or
GET /transaction/[transactionId]/rollback

Which simply return a 200 status code when done. If a transaction can not be found (on all requests in the /transaction/ namespace), a 404 is returned. Transactions are automatically closed and rolled back, if they weren't contact in a 2 minute interval.

# Setting up your own tables in the OX DB

Usually there will be more convenient, higher level APIs to access the OX data stored in the database (or elsewhere, as sometimes there is an abstraction layer involved that fetches data from other places you need not care about). Usually it is a good idea to use the highest level API you can find for accessing groupware data. This service shines, though, when you add you own tables to our schemas or another database that is entrusted to our system. The DatabaseService contains some functionality for managing the evolution of your database schema, that you may find useful. 

## Version Negotiation

We'll introduce two new concepts here. Assume your database schema, the tables and their declaration, have a "version", and of course this version is scoped to your "module". Say the first version (version "1") of your software requires a table called "greetings" that stores the preferred greeting for a person. So to get from a system that never interacted with your module to version "1" you would have to create that table:

CREATE TABLE myModule_greeting (greeting TEXT, cid int(10), uid int(10), PRIMARY KEY (cid, uid));

It is a good idea to prefix your table names with a prefix, so as not to clash with other tables. In this case "myModule_" 

Your database statements will probably not work unless these tables are part of the schema. So in order to have the system check, that the database schema matches your version "1" in your module, you can include two headers in the request:

X-OX-DB-MODULE: com.openexchange.myModule
X-OX-DB-VERSION: 1

to have the system check for a valid version. If, as will happen on first contact, the schema does not have the correct version for your module, the PUT request will fail with a status code of 409 (Conflict) and the header X-OX-DB-VERSION of the response will be set to the current version of the schema/module combination. Now it's up to you to migrate from the given version to your target version. If X-OX-DB-VERSION is the empty String, this means the system was never touched by your module before and you have to create all tables. 

PUT http://localhost:8009/preliminary/database/v1/oxdb/1/writable
X-OX-DB-MODULE: com.openexchange.myModule
X-OX-DB-VERSION: 1

{
  "insertGreeting": {
    "query": "INSERT INTO myModule_greeting (cid, uid, greeting) VALUES (?,?,?)",
    "params": [1, 3, "Aloha"] 
  }
}

Returns a 409 with X-OX-DB-VERSION set to the empty string.


## Updating a schema
So let's update the schema. For that, we need to issue a special call to state our intent to update the schema. Apart from wrapping everything in a transaction, this will also lock the schema, so no one interferes with our update. This is the request we have to issue:

PUT http://localhost:8009/preliminary/database/v1/migration/for/1/to/1/forModule/com.openexchange.myModule

{
  "createGreetingTable": {
    "query": "CREATE TABLE myModule_greeting (greeting TEXT, cid int(10), uid int(10), PRIMARY KEY (cid, uid))"
  }
}

Which gives us:
{
    "results": {
        "createGreetingTable": {
            "updated": 0
        }
    }
}

The URL is structured like this: /migration/for/[ctxId]/to/[newVersionId]/forModule/[moduleName]. When upgrading from one version to another, a slightly longer version of the URL is needed that looks like that:  /migration/for/[ctxId]/from/[oldVersionId]/to/[newVersionId]/forModule/[moduleName]. We will look at this later. 

Now we can issue our original request and save that greeting:

PUT http://localhost:8009/preliminary/database/v1/oxdb/1/writable
X-OX-DB-MODULE: com.openexchange.myModule
X-OX-DB-VERSION: 1

{
  "insertGreeting": {
    "query": "INSERT INTO myModule_greeting (cid, uid, greeting) VALUES (?,?,?)",
    "params": [1, 3, "Aloha"] 
  }
}

Which finally responds:

{
    "results": {
        "insertGreeting": {
            "updated": 1
        }
    }
}

### What happens when two systems want to update the schema at the same time? 

If a schema is being updated by another of your systems, a call to /migration/ will be answered with a 423 (Locked) status code, so you know a schema is in the process of being updated. 

The transactions for a schema update are special in that their timeout is not just two minutes, but eight hours (same goes for the lock). Ample time, if, for example, when updating from one version to another, you have to modify table entries, which can be very many. 

### Spreading a schema update across multiple requests

You can spread a schema update across multiple requests (but one transaction and wrapped by one lock) by using the "keepOpen" parameter, and subsequently the /transaction/[transactionId] calls, as you would for working with regular transactions. As an example, let's update our schema from version 1 to version 2, we're creating a new table called myModule_greetingEntries, retrieve all entries in the myModule_greeting table and updating them to their lowercase versions, while copying the normal case versions to the greetingEntries table.

First let's create the new table

PUT http://localhost:8009/preliminary/database/v1/migration/for/1/from/1/to/2/forModule/com.openexchange.myModule?keepOpen=true

{
  "createGreetingTable": {
    "query": "CREATE TABLE myModule_greetingEntries (greeting TEXT, greetingKey VARCHAR(128), PRIMARY KEY (greetingKey))"
  }
}

Response:
{
    "tx": "d759a07e6de34890a37ea9885fc48ad7",
    "results": {
        "createGreetingTable": {
            "updated": 0
        }
    }
}

Then let's select the entries, 1000 at a time to stay within limits, notice we have to change the values in every context that share this schema, as this migration will only be done *once per schema* which could contain the data of any number of contexts. So we have to handle all of it.

PUT http://localhost:8009/preliminary/database/v1/transaction/d759a07e6de34890a37ea9885fc48ad7?keepOpen=true

{
  "selectGreetings": {
    "query": "SELECT * FROM myModule_greeting ORDER BY cid, uid LIMIT 1000",
    "resultSet": true
  }
}

Response:
{
    "tx": "d759a07e6de34890a37ea9885fc48ad7",
    "results": {
        "selectGreetings": {
            "rows": [
                {
                    "uid": 3,
                    "greeting": "Aloha",
                    "cid": 1
                }
            ]
        }
    }
}
Phew, only one to handle, alright, let's insert that in the new table and update the old table

PUT http://localhost:8009/preliminary/database/v1/transaction/d759a07e6de34890a37ea9885fc48ad7?keepOpen=true

{
  "insertGreetingEntry": {
    "query": "INSERT INTO myModule_greetingEntries (greetingKey, greeting) VALUES (?, ?)",
    "params": ["aloha", "Aloha"]
  },
  "updateGreeting": {
  	"query": "UPDATE myModule_greeting SET greeting = ? WHERE greeting = ?",
    "params": ["aloha", "Aloha"]
  }
}

Response:
{
    "tx": "d759a07e6de34890a37ea9885fc48ad7",
    "results": {
        "insertGreetingEntry": {
            "updated": 1
        },
        "updateGreeting": {
            "updated": 1
        }
    }
}

And, for style points, issue a commit. We could have simply left the "keepOpen" parameter off in the last call to get the same result.

GET http://localhost:8009/preliminary/database/v1/transaction/d759a07e6de34890a37ea9885fc48ad7/commit

Let's issue a query in this new version of our schema:

PUT http://localhost:8009/preliminary/database/v1/oxdb/1/readOnly
X-OX-DB-MODULE: com.openexchange.myModule
X-OX-DB-VERSION: 1

{
  "selectGreeting": {
    "query": "SELECT entry.greeting FROM myModule_greetingEntries AS entry JOIN myModule_greeting AS greeting ON greeting.greeting = entry.greetingKey WHERE cid = ? AND uid = ?",
    "params": [1, 3]
  }
}

{
    "results": {
        "selectGreeting": {
            "rows": [
                {
                    "greeting": "Aloha"
                }
            ]
        }
    }
}

And we can select the greeting, now via a join.

## Force-Unlocking a schema

You can unlock a schema forcibly without waiting for the expiry of the lock by issuing a GET request:

GET http://localhost:8009/preliminary/database/v1//unlock/for/1/andModule/com.openexchange.myModule

specifying the context name and module in the URL.

# Accessing NON-OX schemas and database servers

You can also access NON-OX databases (both servers and schemas) using our REST service, but for that we'll have to look at some of the internals of our database pooling implementation and talk  about a few concepts. 
OX connects to database servers which were registered with the 'registerdatabase' command line tool. A database can be either a master database, used for writing, or a slave database used for reading. There might not be a reading slave in a setup though. The command 'listdatabase' shows you a list of all databases that are known to your OX installation along with their IDs. You will need these IDs to reference the databases:

root@beta:/opt/open-xchange/sbin# ./listdatabase 
 id name       hostname  master mid weight maxctx curctx hlimit max inital
  3 oxdatabase io2.ox.io true     0    100   1000    363 true   100      0
444 ciscoDB    db.ox.io  true     0    100      0      0 true   100      0

Whenever I talk about a writeId, you can use the id number of a master server, the readId always has to point to a slave server that was set up to sync with the master. In case you don't use a master/slave setup, just use the masters database id for both the readId and writeId. Here is the call to register a database:

./registerdatabase --name=ciscoDB --hostname=db.ox.io --dbpasswd=superSecret --master=true --maxunit=0
database 444 registered

The name is an arbitrary name, just pick something nice and memorable. The hostname points to the host the database is living on, the dbpasswd is the database password. In this case I'm registering a master database, if this is set to false, you will have to provide the masters database id in --masterid. Maxunit tells the system how many contexts it can place in this database, and, in case this server only handles your custom database, has to be set to 0. 

This part provides the hostname for the database to use. Next comes the "schema" name, which is the name of the database you created with "CREATE DATABASE".

Lastly there is something we refer to as the partitionId. If your database can logically be partitioned into sections that are updated and queried completely independently of one another, that region should get its own "partitionId". The partitionId determines which parts of the database get invalidated when an update is done. This is done by our replication monitor, which makes sure you always read up-to-date data. Consider this example: 

On the master
You do an update to a table

On the slave
You query the same table

At times, the slave will not have applied the update yet, and you would read old data. So, what really happens, when using our replication monitor is this:

On the master
You do an update to a table
The database service increments a transaction counter for the partition you selected

For reading, the replication monitor checks the transaction counter on the slave and for the given partition, if it matches the one it previously wrote or retrieved from the master server, you will use the slave DB for reading (as it is up-to-date), otherwise the system will fall back to using the master. 

If parts of your database are completely independent of one another, you can partition the database with partitionIds. We use the contextId for this. Clearly this is an optimization strategy, so you might well not need this. In that case just omit the partitionId (it will then default to "0"). If you want to use a partitionId, you have to register it first. Let's register a couple of partitionIds: 

PUT http://localhost:8009/preliminary/database/v1/pool/w/[writeId]/[schemaName]/partitions

With the body consisting of a JSONArray with the IDs you need registered. For example:

PUT http://localhost:8009/preliminary/database/v1/pool/w/2/myCustomSchema/partitions
[1,2,3,4,5]

Which responds with a 200 (OK).

All these parts, the readId of the slave, the writeId of the master, the schema and optionally the partitionId make up the address of your database: 

PUT http://localhost:8009/preliminary/database/v1/pool/r/[readId]/w/[writeId]/[schema]/[partitionId]

Add /writable or /readOnly to it to get the address to do updates and queries. If you need to do a schema migration the URL looks like this:

PUT http://localhost:8009/preliminary/database/v1/migration/for/pool/r/[readId]/w/[writeId]/[schema]/[partitionId]/from/[oldVersion]/to/[toVersion]/forModule/[module]

Optional parts are again the partitionId and the /from/[oldVersion] part for the initial update. 

This is the address to unlock a locked schema
GET http://localhost:8009/preliminary/database/v1/unlock/pool/r/[readId]/w/[writeId]/[schema]/[partitionId]/andModule/[module]

There are some tables that need to exist for the replication monitor and the migration system to work. So for every schema that you want to access via our service, you have to issue a call like this: 

http://localhost:8009/preliminary/database/v1/init/w/[writeId]/[schema]

Let's walk through a typical interaction with the database, including the creation of the DB schema.

## Creating the schema to use

In this example I will use an existing and already registered database server (id: 2) and a custom schema named appropriately "myCustomSchema". I will not be using a master/slave setup, so will use the same (master) database ID for reading and writing. I will not partition the database, so leave off the partitionId.     

## Initializing the schema 

Before using the schema at all, we have to initialize the database:

GET http://localhost:8009/preliminary/database/v1/init/w/2/myCustomSchema

which responds with a 200 (OK). Now we're ready to use the schema with our service. 

## Inserting into a table, but failing the version constraint

So let's try inserting into the greetings table. "Which greetings table?", you ask. It's not there yet, but the migration system will tell us about it: 

PUT http://localhost:8009/preliminary/database/v1/pool/r/2/w/2/myCustomSchema/writable
X-OX-DB-MODULE: com.openexchange.myModule
X-OX-DB-VERSION: 1

{
  "insertGreeting": {
    "query": "INSERT INTO greetings (cid, uid, greeting) VALUES (?, ?, ?)",
    "params": [1,3, "Aloha"]
  }
}

To which the server responds with a 409 (Conflict) to tell us, the schema is not matching our asked for version. 

## Updating the database

So, let's do the migration on this database:

PUT http://localhost:8009/preliminary/database/v1//migration/for/pool/r/2/w/2/myCustomSchema/to/1/forModule/com.openexchange.myModule
{
  "createGreetingTable": {
    "query": "CREATE TABLE greetings (cid INT(10), uid INT(10), greeting VARCHAR(128))"
  }
}

Which garners this response: 
{
    "results": {
        "createGreetingTable": {
            "updated": 0
        }
    }
}

## Inserting into the table

Alright, the table is there and our versioning constraint is met, so the insert goes through:

PUT http://localhost:8009/preliminary/database/v1/pool/r/2/w/2/myCustomSchema/writable
X-OX-DB-MODULE: com.openexchange.myModule
X-OX-DB-VERSION: 1

{
  "insertGreeting": {
    "query": "INSERT INTO greetings (cid, uid, greeting) VALUES (?, ?, ?)",
    "params": [1,3, "Aloha"]
  }
}

To which the server responds:
{
    "results": {
        "insertGreeting": {
            "updated": 1
        }
    }
}

## Querying the table

Let's query the table for the greeting: 

PUT http://localhost:8009/preliminary/database/v1/pool/r/2/w/2/myCustomSchema/readOnly
X-OX-DB-MODULE: com.openexchange.myModule
X-OX-DB-VERSION: 1

{
  "selectGreeting": {
    "query": "SELECT greeting FROM greetings WHERE cid = ? AND uid = ?",
    "params": [1,3]
  }
}

Response: 
{
    "results": {
        "selectGreeting": {
            "rows": [
                {
                    "greeting": "Aloha"
                }
            ]
        }
    }
}

## Transactions

Transactions work exactly the same as with the other database types. Just include a "keepOpen=true" parameter in the call and use the transaction id of the response for further queries. 

# Advanced Topics

## How to retrieve generated keys

In order to retrieve keys automatically generated by the database (typically by autoincrement fields), just include the attribute 'generatedKeys' in the query: 

PUT ...

{
  "insertValue": {
    "query": "INSERT INTO myTable (someCol, otherCol) VALUES (?, ?)",
    "params": [1,3],
		"generatedKeys": true
  }
}

Responds with:
{
    "results": {
        "insertGreeting": {
            "updated": 1,
            "generatedKeys": [
                6
            ]
        }
    }
}

with the generated keys showing up as a JSON array.

# TODOs
JSON Parser Errors don't show up nicely in responses!!!!!

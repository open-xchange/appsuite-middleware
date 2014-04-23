# Introduction
The OX DB Service aims to give access to OX SQL databases by way of an http based API. Using a PUT request, clients can query or update the OX user data databases or the configdb. Additionally, this service contains a mechanism for updating a database with new tables. Usually this service listens at an OX host port 8009 under /rest/database, but this can vary depending on the network setup, so its best to keep this value in a configuration option for your service.

# Reading from the databases

Databases in an OX setup are usually set up in a master/slave setup. This means that you do all reads from the slave dbs and all writes on the database master. Depending on the kinds of queries you want to send, you have to pick the url that matches either a write or a read operation. You can also choose to either contact the configdb or an ox database assigned to a context.

## Reading from the configdb

Let's select some metadata about the first 3 contexts from the database:

Query:
PUT http://localhost:8009/rest/database/configdb/readOnly

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

The query is addressed to /rest/database/configdb/readOnly, meaning we want the slave (the 'readOnly' part of the URL) for the configdb. The query is simply PUT verbatim as a string. Note though, that for using substitutions – and that's something you want to use for parameters from the scary internet to avoid SQL Injection – you'll have to send a more complex JSON structure as a query. We'll talk about that later. 

The response contains all rows as JSON objects with the columns as keys, and the values as values, somewhat deeply nested in response.results.result.rows. 

## Reading from the OX db

Let's query an OX DB: 

PUT http://localhost:8009/rest/database/oxdb/1/readOnly

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

The query this time is addressed to /rest/database/oxdb/1/readOnly. "oxdb" in the URL denotes that we want to query the OX user data database, the "1" is the contextId whose database we want to connect to, "readOnly" again means, we want to talk to the reading slave.

## Using Substitutions and Prepared Statements

As said before, when adding variables to a query, it's best to use the built-in features of JDBC and use substitutions and prepared statements. Let's do this with the above query and use a substitution for the cid:


PUT http://localhost:8009/rest/database/oxdb/1/readOnly

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

PUT http://localhost:8009/rest/database/oxdb/1/readOnly

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

PUT http://localhost:8009/rest/database/oxdb/1/readOnly

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

PUT http://localhost:8009/rest/database/oxdb/1/readOnly

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

PUT http://localhost:8009/rest/database/configdb/readOnly

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

PUT http://localhost:8009/rest/database/oxdb/1/writable

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

PUT http://localhost:8009/rest/database/oxdb/1/readOnly

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

PUT http://localhost:8009/rest/database/oxdb/1/
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

PUT http://localhost:8009/rest/database/oxdb/1/writable?keepOpen=true

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

PUT http://localhost:8009/rest/database/transaction/b693848c1f534cde94c439a4abe2dbfc?keepOpen=true

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

PUT http://localhost:8009/rest/database/transaction/b693848c1f534cde94c439a4abe2dbfc

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

PUT http://localhost:8009/rest/database/oxdb/1/writable
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

PUT http://localhost:8009/rest/database/migration/for/1/to/1/forModule/com.openexchange.myModule

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

PUT http://localhost:8009/rest/database/oxdb/1/writable
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

PUT http://localhost:8009/rest/database/migration/for/1/from/1/to/2/forModule/com.openexchange.myModule?keepOpen=true

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

PUT http://localhost:8009/rest/database/transaction/d759a07e6de34890a37ea9885fc48ad7?keepOpen=true

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

PUT http://localhost:8009/rest/database/transaction/d759a07e6de34890a37ea9885fc48ad7?keepOpen=true

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

GET http://localhost:8009/rest/database/transaction/d759a07e6de34890a37ea9885fc48ad7/commit

Let's issue a query in this new version of our schema:

PUT http://localhost:8009/rest/database/oxdb/1/readOnly
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

GET http://localhost:8009/rest/database//unlock/for/1/andModule/com.openexchange.myModule

specifying the context name and module in the URL.

# TODOs
Accessing arbitrary databases
JSON Parser Errors!!!!!
The files located in this folder show how to implement custom database migration based on Java classes.

Programmatically you are able to do exactly the same as by configuring XML files i. e. create an
implementation of CustomPrecondition will help you to define conditions that have to be true to get
the defined change executed.

Both example implementations will update the table 'login2context' table to have the column 'login_info'
a type of varchar(255) instead of varchar(128).

- ExampleCustomSqlChange:
- ExampleCustomTaskChange:

- custom.changelog.xml:
	Shows how to include one of the java classes mentioned above. The given filename has to be provided by calling
	com.openexchange.database.migration.DBMigrationExecutorService.execute(String, List<ResourceAccessor>)
	as shown in com.openexchange.database.migration.ox.osgi.OXMigrationActivator.

	To get your custom java classes found you have to provide a ResourceAccessor which has access to the desired class (see
	com.openexchange.database.migration.ox.internal.accessors.SimpleClassLoaderResourceAccessor.SimpleClassLoaderResourceAccessor() ).

	You are able to define you desired changes within the XML file (getter and setter required in java class) or to hard code
	it within the classes (as shown in the examples)
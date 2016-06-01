# HowTo: OX HTTP API documentation
This article provides step-by-step instructions to extend/change the documentation of the HTTP API.
The documentation should be stored in a folder _http_api/_ in a subdirectory of the backend repository.
For detailed information about Swagger's/OpenAPI's specification please click [here](http://swagger.io/specification/).

## Index
1. [Add a new parameter to a request](#add-a-new-parameter-to-a-request)
2. [Add a new request](#add-a-new-request)
3. [Add a new response / request body](#add-a-new-response-request-body)
  1. [Add a new model](#add-a-new-model)
  2. [Add a request body](#add-a-request-body)
  3. [Change an existing model](#change-an-existing-model)
4. [Add a new module](#add-a-new-module)
5. [Add information to the overview section](#add-information-to-the-overview-section)
  1. [Change version information](#change-version-information)
  2. [Add a column identifiers table](#add-a-column-identifiers-table)
6. [Write anchor links to overview headlines or model definitions](#write-anchor-links-to-overview-headlines-or-model-definitions)

___

## Add a new parameter to a request
First you should decide whether it is a "global" parameter or a "local" parameter. A "global" parameter is not
automatically added to all requests, but must only be defined once and can be referenced several times. So if
the new parameter is a parameter that is used in multiple (say >= 5) requests it would be recommendable to
define it as global. **Normally** a parameter is a local one, only contained in a specific request. Even though
the parameter is used more times it is not compulsory to define it as "global".

### Add a local parameter
Let's assume that the `/group?action=get` request was extended by a new parameter `foobar` of type integer.
The request belongs to the groups module. Go to the documentation folder and navigate to
_http_api/paths/08-groups/_ and open _GetRequest.yaml_. Now we add the definition of `foobar` to the `parameters`
list.

```yaml
/group?action=get:
  get:
    operationId: getGroup
    tags:
      - groups
    summary: Gets a group.
    parameters:
      - $ref: "#/parameters/gblQueryParamSession"
      - in: query
        name: id
        type: integer
        description: The ID of the group.
        required: true
# *** NEW PARAMETER *****************************
      - in: query
	    name: foobar
		type: integer
		description: Our new fancy parameter (available since vX.Y.Z).
		required: false
# *** NEW PARAMETER *****************************
    responses:
      200:
        description: |
          A JSON object containing the group data. In case of errors the responsible fields in the
          response are filled (see [Error handling](#error-handling)).
        schema:
          $ref: "#/definitions/GroupResponse"
```

Each parameter represents a new list element which is instructed with a dash (`-`) followed by the `in` key that
specifies the location of the parameter. That can be `query` (for normal query parameters), `formData` (if parameter
is sent in a form field of a POST request), `path` (if parameter takes place in the route, like `/group/{foobar}`),
`header` (for header parameters) or `body` (see [Add a request body](#add-a-request-body)).

The `name` property appoints the name of the parameter and the `description` property contains details of the parameter.
The `type` property specifies the parameter type. As previously mentioned our new parameter is of type integer. Base data types
for parameters are `integer`, `string`, and `boolean`. With an additional `format` property you can define the type more
precisely. For instance, a `long` data type is specified by `format: int64`. By default the format of integer is `int32`.
Finally you must denote whether it is a mandatory parameter or not. This is done by adding the `required` property and set it to `true` (mandatory), otherwise `false`.

Remember, a session parameter can always be added using the "global" reference `- $ref: "#/parameters/gblQueryParamSession"`.

### Add a global parameter
Following the example from above the parameter `foobar` is used in many requests. Thus we define it as a "global" parameter.
Therefor go to _http_api/parameters/_ and open _index.yaml_. Now append the definition of the parameter, like:

```yaml
gblQueryParamFooBar:
  name: foobar
  in: query
  type: integer
  description: Our new fancy parameter (available since vX.Y.Z).
  required: false
```

A "global" parameter gets a key (here `gblQueryParamFooBar`) followed by the properties of the parameter similar
to the ones explained [above](#add-a-local-parameter). Do not forget to indent the properties!

After defining the parameter you can use it in a request's `parameters` section by referencing the key:

```yaml
/group?action=get:
  get:
    operationId: getGroup
    tags:
      - groups
    summary: Gets a group.
    parameters:
      - $ref: "#/parameters/gblQueryParamSession"
      - in: query
        name: id
        type: integer
        description: The ID of the group.
        required: true
# *** NEW PARAMETER *****************************
      - $ref: "#/parameters/gblQueryParamFooBar"
# *** NEW PARAMETER *****************************
    responses:
      200:
        description: |
          A JSON object containing the group data. In case of errors the responsible fields in the
          response are filled (see [Error handling](#error-handling)).
        schema:
          $ref: "#/definitions/GroupResponse"
```

## Add a new request
To add a new request, first navigate to the folder of the module the request belongs to. As an example, this might be
_http_api/paths/08-groups/_. **All requests are stored in a corresponding module folder inside the _paths/_ folder.**
It is possible that the module folder is divided into several subfolders, too (for an example see _http_api/paths/30-messaging/_).
Nevertheless, usually the request files are stored directly in the module's folder. Back to the example, we now want to add
a `/group?action=has` request. To do so, add a new empty YAML file _HasRequest.yaml_ to the corresponding module folder. A naming
convention for request files is to use the request's action name with a starting uppercase letter followed by "Request".
Additionally open the _index.yaml_ that is also placed in the same folder and add the newly created request file, like:

```yaml
requests:
  - GetRequest.yaml
  - ...
  - HasRequest.yaml
```

Save and close the _index.yaml_ file. Then open the YAML file of the new request.

Each request file starts with the definition of the API endpoint. Here it is

```yaml
/group?action=has:
```

Next we must specify the HTTP method of the request. This can be `get` (GET), `put` (PUT), or `post` (POST) in case of the OX HTTP API.
It is also possible to add multiple methods for an endpoint like:

```yaml
/group?action=has:
  get:
    # definition ...
  put:
    # definition ...
```

In our example we only deal with a GET. The definition of the request follows after the method key. Please consider the identation!
First we always need to specify an `operationId` and a tag. The operation identifier is used in a code generation
process to name the request function. If you do not specify this id the name might be illegible. The tag is important
for grouping of requests that belong to the same module. It is possible to add multiple tags, however, just add one module name in the `tags` list.
Look at other request files of the module to find out the correct name (attention: case sensitivity).

```yaml
/group?action=has:
  get:
    operationId: hasGroup
	tags:
	  - groups
```

Afterwards each request should have a short `summary`. This can be followed by a description that goes into detail.
If the description shall be multilined use a pipe to introduce multiline descriptions, like:

```yaml
description: |
  This is
  
  a multiline description.
```

It is also possible to use Markdown syntax in `description` properties (not only limited to request descriptions)!  
As a next step you have to consider about the consumption and production type of the request. Normally the OX HTTP API consumes
`application/x-www-form-urlencoded` and produces `application/json`. If the request consumes or produces another type,
e.g. consumes `application/json` and produces `text/html`, you have to override it like:

```yaml
/group?action=has:
  get:
    operationId: hasGroup
	tags:
	  - groups
	summary: Indicates whether the given group exists.
	consumes:
	  - application/json
	produces:
	  - text/html
```

Remember, this is usually only necessary for PUT and POST methods of the HTTP API!

Finally we need to define the parameters and responses of the request. Take a look at [Add a new parameter to a request](#add-a-new-parameter-to-a-request)
that provides detailed information on adding parameters. Responses are specified below the `responses` key of the request definition.

```yaml
/group?action=has:
  get:
    operationId: hasGroup
	tags:
	  - groups
	summary: Indicates whether the given group exists.
	parameters:
	  - $ref: "#/parameters/gblQueryParamSession"
      # ...
	responses:
	  200:
	    description: |
		  A JSON object containing the value `true` if the group exists, otherwise `false`. In
		  case of errors the responsible fields in the response are filled (see
		  [Error handling](#error-handling)).
		schema:
		  $ref: "#/definitions/GroupExistenceResponse"
```

Because a request can have multiple responses each response is identified by its status code as a key.
Usually this is `200` in case of the HTTP API. The key is followed by a description of the response and the
specification of the response schema. The schema contains the data type of the result. If the result isn't a complex
object you can directly put the definition into the request file like:

```yaml
#...
    responses:
	  200:
	    description: ...
		schema:
		  type: string
		  format: binary
```

This indicates that the response is a binary stream. If the response is a complex object like a JSON object, it is
recommended to outsource the definition of the response in a model and only place a reference to the
model inside of the response's schema as seen above (`$ref: "#/definitions/GroupExistenceResponse"`). Look at [Add a new response / request body](#add-a-new-response-request-body)
to learn how to write a response model.

Normally each description of a response of the OX HTTP API ends with the sentence "In case of errors the responsible fields in the
response are filled (see \[Error handling\](#error-handling))". Please retain this convention when you write the definition of a new request
that returns a normal JSON object as defined in the low level protocol and error handling section of the API.

#### Checklist
[x] Request YAML file added in right directory?  
[x] Request YAML file referenced in corresponding _index.yaml_?  
[x] Endpoint, operationId, tag, and summary specified?  
[x] Consumes and produces type specified, if necessary?  
[x] Parameters defined?  
[x] Responses defined?

## Add a new response / request body
As you could see in [Add a new request](#add-a-new-request), responses that represent complex objects are defined in
a model. **All models (of responses as well as request bodies) are located in the subfolders of directory _definitions/_
inside of the _http_api/_ documentation folder.** The subfolders represent modules to group the models as with request files.

### Add a new model
Now we want to add a new model that represents the response of the example request shown at [Add a new request](#add-a-new-request).
The request belongs to the groups module so we navigate to the appropriate folder in the _definitions/_ directory.
Then add a new YAML file that should be named after the model name, like "GroupExistenceResponse.yaml". A naming convention
is that every response model ends with suffix "Response" and every request body model with suffix "Body" (with a few exceptions).
Furthermore each model name should start with the modules name. This makes it easier to assign models to modules, for instance in a
generated client library.

If you define a new model this model is always of type `object`. Thus the content of the new model file (_GroupExistenceResponse.yaml_)
starts with:
```yaml
type: object
properties:
```
Below the key `properties` you have to add the object properties. In case of the HTTP API almost every response
consists of error fields and the payload that is stored in the `data` property. To avoid rewriting the error fields every
time, the common response properties are stored in the _defintions/common/CommonResponseData.yaml_ file. We now can reference
this file in our response using a relative path to add the content of this file to ours:

```yaml
type: object
properties:
  $ref: ../common/CommonResponseData.yaml
```

Finally we must add the payload which is stored in the `data` field:

```yaml
type: object
properties:
  $ref: ../common/CommonResponseData.yaml
  data:
	type: boolean
	description: Indicates whether the group exists or not.
```

The field name represents the key followed by the type of the field, which may be `integer`, `string`, `boolean`, `array`, or
`object` and the description of the field. As mentioned in [Add a new parameter to a request](#add-a-new-parameter-to-a-request)
we can refine an integer or a string type using the `format` property, e.g. to specify the data type `long`:

```yaml
#...
  data:
	type: integer
	format: int64
	description: This returns a long.
```

As said before a field can be an array, too. If you specify a field with type `array` you must always add the property `items`
which describes the type of the elements:

```yaml
#...
  data:
	type: array
	description: An array that contains elements of a specific type.
	items:
	  type: string
```

In the example above we have an array of strings. It is also possible to have an array of objects, as well as an array of arrays. If you cannot
determine the concrete type of the elements (because of the elements differ in type) you may only indicate that it
is an object without specifying the `properties` of the object, like:

```yaml
  data:
	type: array
	description: An array that contains elements of different types.
	items:
	  type: object
```

Otherwise, if the elements are objects with well-defined properties, you should outsource the definition of the object
in a separate model and reference it using `$ref: "#/definitions/MyObjectModel"`:

```yaml
#...
  data:
	$ref: "#/definitions/MyObjectModel"
```

To create such a model follow the same steps as described in this section. Example: a model with 2 properties

```yaml
type: object
properties:
  firstName:
	type: string
	description: The first name of a person.
  lastName:
	type: string
	description: The sur name of a person.
```

The body of every model is stored in a YAML file as seen above. But that is not enough to enable referencing them using the
well-known `$ref: "#/definitions/ModelName"`. For that we have to navigate to the _defintions_ folder and open
the _index.yaml_ file. In this file you find the assemblage of model YAML files and model names. To add a new model
go to the section of the specific module (see the comments inside the file) and add the new model by applying the name
as a key and reference the corresponding YAML file. As an example we add the new _GroupExistenceResponse_ model:

```yaml
#...
# Module: groups
#...
GroupUpdatesResponse:
  $ref: ./groups/GroupUpdatesResponse.yaml
GroupExistenceResponse:
  $ref: ./groups/GroupExistenceResponse.yaml
#...
```

Now it is possible to access the definition of the new model with `$ref: "#/definitions/GroupExistenceResponse"` in
a request or in another model.

**Attention:** The position of a model in the _defintions/index.yaml_ file is important. If you want to use the model
in other models then the model must stand before these models in the _index.yaml_. Otherwise the model is not referenceable.

#### Checklist
[x] Model YAML file added in right directory?  
[x] Content of YAML file starts with `type: object` and `properties:`?  
[x] If model represents a response: do you have imported the common response fields?  
[x] Model YAML file referenced in _defintions/index.yaml_?

### Add a request body
This section explains how to add a body parameter to a request. Other parameter types are presented in
[Add a new parameter to a request](#add-a-new-parameter-to-a-request).
To add a request body, you have to open the YAML file of the corresponding request. The body of a request is
delivered as a request parameter. Therefore go to the `parameters` section of the request. A general definition
of a request body parameter is:

```yaml
- in: body
  name: body
  description: A JSON object ...
  required: true
  schema:
    $ref: "#/definitions/MyRequestBody"
```

By specifying a request body you have to consider to add a

```yaml
consumes:
  - application/json
```

to the request definition, as mentioned in [Add a new request](#add-a-new-request).
The example above implies that the request body consists of a JSON object. This JSON object has to be
defined in a model. See [Add a new model](#add-a-new-model) for further instructions.  
However, it is also possible to define a (JSON) array or another data type like `string` as body schema.
If it is a JSON array then define it in the request's YAML file and only outsource possible JSON objects
that the array contains.
```yaml
- in: body
  name: body
  description: A JSON array of JSON objects ...
  required: true
  schema:
    type: array
	items:
	  $ref: "#/definitions/ObjectModelName"
```
If the request body is not mandatory, set `required` to `false`.

As already addressed, you can use [GFM syntax](https://help.github.com/categories/writing-on-github/) in `description` properties
to format the text.  
Example:

```yaml
#...
description: A **JSON object** with the fields `id` and `folder`.
#...
```

#### Checklist
[x] Added body parameter definition to the right request?  
[x] Does the request definition support the HTTP method PUT?  
[x] Added a `consumes` property to the request definition, if necessary?  
[x] Do you have modeled the request body, if necessary?

### Change an existing model
If you need to change the data of a response or a request body simply navigate to the appropriate YAML file (_http_api/defintions/.../..._).
Open it and change or add a new field like:

```yaml
type: object
properties:
  #...
  newField:
    type: string
	description: This is a new property (available since vX.Y.Z).
```

## Add a new module
If you want to add a new module open the base _index.yaml_ of folder _http_api/_ and go to section `tags`. Then
add a new list element (`-`) to the global tags list and specify the tag name and a description.  
Afterwards go to _http_api/paths/_ and add a new subfolder for the module. The naming convention is
_modulenumber-modulename_. At last go to _http_api/definitions/_, add a subfolder with the name of the module
and open the _index.yaml_ in the _definitions/_ folder. Add a new section introduced with

```yaml
...
# Module: module name
```

and finally you can
* [Add a new request](#add-a-new-request)
* [Add a new response / request body](#add-a-new-response-request-body)

**Note:** when adding a new request use the module name specified in the global tags list as the tag for the request!

## Add information to the overview section
A documentation created after the Swagger/OpenAPI specification can have one overview section at the beginning.
You can modify this section in file _index.yaml_ of folder _http_api/info/_

### Change version information
The contact data and version information is stored in _http_api/info/index.yaml_. Open the YAML file and jump to the end.
Their you find the contact data and version information:

```yaml
#...
contact:
  name: Open-Xchange GmbH
  email: info@open-xchange.com
  url: https://www.open-xchange.com/
version: 7.8.2
```

### Add a column identifiers table
The column identifiers tables are placed in the overview section. So open _http_api/info/index.yaml_ and jump
to (in `description` property):

```yaml
## Column identifiers
```

Now you can add a new headline and the table of column identifiers. Use multiple columns to avoid high tables.
The tables must be formatted in [GFM syntax](https://help.github.com/categories/writing-on-github/).

## Write anchor links to overview headlines or model definitions
You can make reference to a specific section of the overview or to a definition of a response's / request body's data with
a Markdown link.  
Example 1: you want to add an anchor link to the Low level protocol section

```yaml
#...
description: ..., see [Low level protocol](#low-level-protocol), too.
#...
```
Example 2: you want to refer to a model's definition

```yaml
#...
- in: formData
  name: json_0
  type: string
  description: A JSON object as defined in [ContactData](#/definitions/ContactData) model.
  required: true
#...
```
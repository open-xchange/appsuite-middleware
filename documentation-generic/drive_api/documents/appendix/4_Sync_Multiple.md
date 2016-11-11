# Synchronize files for multiple folders

Utilizing the [multiple request](https://documentation.open-xchange.com/components/middleware/http-api/develop/index.html?version=develop#/Multiple), it is possible to execute multiple "syncfiles" requests in parallel. Especially during an initial synchronization and in combination with "inline" mode for .drive-meta files, this may reduce the number of necessary requests. Therefore, the parameters and file versions of each syncfiles request are serializied into a JSON array of the multiple-request body. In the same way, the response contains the resulting actions of each "syncfiles" result in an JSON array of the response.

The following shows an example of executing three syncfiles actions inside a single mutliple request:
```
==> PUT http://local.ox/ajax/multiple?session=0833ca06093a4bad826347a30bf7ace7&continue=true
  > Content:
    [{
      "module": "drive",
      "action": "syncfiles",
      "root": 33,
      "apiVersion": 4,
      "path": "/",
      "driveMeta": "inline",
      "data": {
        "clientVersions": [],
        "originalVersions": []
      }
    },
    {
      "module": "drive",
      "action": "syncfiles",
      "root": 33,
      "apiVersion": 4,
      "path": "/Pictures",
      "driveMeta": "inline",
      "data": {
        "clientVersions": [],
        "originalVersions": []
      }
    },
    {
      "module": "drive",
      "action": "syncfiles",
      "root": 33,
      "apiVersion": 4,
      "path": "/Music",
      "driveMeta": "inline",
      "data": {
        "clientVersions": [],
        "originalVersions": []
      }
    }]

<== HTTP 200 OK (92.2341 ms elapsed, 2058 bytes received)
<   Content:
    [{
      "data": [{
        "action": "download",
        "newVersion": {
          "checksum": "e9000b2444dfbc780f91df6586e24615",
          "name": ".drive-meta"
        },
        "totalLength": 1663,
        "data": {
          "id": "33",
          "default_folder": true,
          "has_subfolders": true,
          "own_rights": 403710016,
          "permissions": [{
            "bits": 403710016,
            "entity": 4,
            "group": false
          }],
          "extended_permissions": [{
            "entity": 4,
            "bits": 403710016,
            "type": "user",
            "display_name": "Jens Mander",
            "contact": {
              "email1": "jens@local.ox",
              "last_name": "Mander",
              "first_name": "Jens",
              "image1_url": "/ajax/image/user/picture?id=4&timestamp=1453992059223"
            }
          }],
          "jump": ["permissions"],
          "shareable": true,
          "files": [{
            "name": "versions.txt",
            "created": 1458723308686,
            "modified": 1458723332449,
            "created_by": 4,
            "modified_by": 4,
            "content_type": "text/plain",
            "preview": "http://local.ox/ajax/files?action=document&format=preview_image&folder=33&id=33/488915&version=1&context=1&user=4&delivery=download&scaleType=contain&width=1600&height=1600",
            "thumbnail": "http://local.ox/ajax/files?action=document&format=preview_image&folder=33&id=33/488915&version=1&context=1&user=4&delivery=download&scaleType=contain&width=200&height=150",
            "shareable": true,
            "number_of_versions": 4,
            "version": "1",
            "versions": [{
              "name": "versions.txt",
              "file_size": 1,
              "created": 1458723308686,
              "modified": 1458723308686,
              "created_by": 4,
              "modified_by": 4,
              "version": "1"
            },
            {
              "name": "versions.txt",
              "file_size": 3,
              "created": 1458723311033,
              "modified": 1458723311019,
              "created_by": 4,
              "modified_by": 4,
              "version": "2",
              "version_comment": ""
            },
            {
              "name": "versions.txt",
              "file_size": 5,
              "created": 1458723313260,
              "modified": 1458723332449,
              "created_by": 4,
              "modified_by": 4,
              "version": "3",
              "version_comment": ""
            },
            {
              "name": "versions.txt",
              "file_size": 8,
              "created": 1458723316567,
              "modified": 1458723316551,
              "created_by": 4,
              "modified_by": 4,
              "version": "4",
              "version_comment": ""
            }],
            "jump": ["preview",
            "edit",
            "permissions",
            "version_history"]
          }]
        },
        "path": "/",
        "modified": 1467622558800
      },
      {
        "action": "download",
        "newVersion": {
          "checksum": "c4ca4238a0b923820dcc509a6f75849b",
          "name": "versions.txt"
        },
        "totalLength": 1,
        "created": 1458723308686,
        "path": "/",
        "modified": 1458723332449
      }]
    },
    {
      "data": [{
        "action": "download",
        "newVersion": {
          "checksum": "46114c4a55ed74b836da0fd83809fb06",
          "name": ".drive-meta"
        },
        "totalLength": 2531,
        "data": {
          "id": "103432",
          "default_folder": true,
          "has_subfolders": true,
          "type": 20,
          "own_rights": 403710016,
          "permissions": [{
            "bits": 403710016,
            "entity": 4,
            "group": false
          },
          {
            "bits": 257,
            "entity": 192,
            "group": false
          }],
          "extended_permissions": [{
            "entity": 4,
            "bits": 403710016,
            "type": "user",
            "display_name": "Jens Mander",
            "contact": {
              "email1": "jens@local.ox",
              "last_name": "Mander",
              "first_name": "Jens",
              "image1_url": "/ajax/image/user/picture?id=4&timestamp=1453992059223"
            }
          },
          {
            "entity": 192,
            "bits": 257,
            "type": "guest",
            "display_name": "Otto Example",
            "contact": {
              "email1": "otto@example.com"
            }
          }],
          "jump": ["permissions"],
          "shared": true,
          "shareable": true,
          "files": [{
            "name": "Desert.jpg",
            "created": 1458717785226,
            "modified": 1458717785226,
            "created_by": 4,
            "modified_by": 4,
            "content_type": "image/jpeg",
            "preview": "http://local.ox/ajax/files?action=document&folder=103432&id=103432/488906&version=1&context=1&user=4&delivery=download&scaleType=contain&width=1600&height=1600&shrinkOnly=true&rotate=true",
            "thumbnail": "http://local.ox/ajax/files?action=document&folder=103432&id=103432/488906&version=1&context=1&user=4&delivery=download&scaleType=contain&width=200&height=150&shrinkOnly=true&rotate=true",
            "shareable": true,
            "number_of_versions": 1,
            "version": "1",
            "jump": ["preview",
            "permissions",
            "version_history"]
          },
          {
            "name": "Hydrangeas.jpg",
            "created": 1458717785374,
            "modified": 1458717785374,
            "created_by": 4,
            "modified_by": 4,
            "content_type": "image/jpeg",
            "preview": "http://local.ox/ajax/files?action=document&folder=103432&id=103432/488907&version=1&context=1&user=4&delivery=download&scaleType=contain&width=1600&height=1600&shrinkOnly=true&rotate=true",
            "thumbnail": "http://local.ox/ajax/files?action=document&folder=103432&id=103432/488907&version=1&context=1&user=4&delivery=download&scaleType=contain&width=200&height=150&shrinkOnly=true&rotate=true",
            "shareable": true,
            "number_of_versions": 1,
            "version": "1",
            "jump": ["preview",
            "permissions",
            "version_history"]
          },
          {
            "name": "2110.JPG",
            "created": 1460283874321,
            "modified": 1460443249927,
            "created_by": 4,
            "modified_by": 4,
            "content_type": "image/jpeg",
            "preview": "http://local.ox/ajax/files?action=document&folder=103432&id=103432/494179&version=1&context=1&user=4&delivery=download&scaleType=contain&width=1600&height=1600&shrinkOnly=true&rotate=true",
            "thumbnail": "http://local.ox/ajax/files?action=document&folder=103432&id=103432/494179&version=1&context=1&user=4&delivery=download&scaleType=contain&width=200&height=150&shrinkOnly=true&rotate=true",
            "shareable": true,
            "number_of_versions": 1,
            "version": "1",
            "jump": ["preview",
            "permissions",
            "version_history"]
          }]
        },
        "path": "/Pictures",
        "modified": 1459842066104
      },
      {
        "action": "download",
        "newVersion": {
          "checksum": "7c1e3c12567f8279dff97faee04af9c2",
          "name": "2110.JPG"
        },
        "totalLength": 4421093,
        "created": 1460283874321,
        "path": "/Pictures",
        "modified": 1460443249927
      },
      {
        "action": "download",
        "newVersion": {
          "checksum": "ba45c8f60456a672e003a875e469d0eb",
          "name": "Desert.jpg"
        },
        "totalLength": 845941,
        "created": 1458717785226,
        "path": "/Pictures",
        "modified": 1458717785226
      },
      {
        "action": "download",
        "newVersion": {
          "checksum": "bdf3bf1da3405725be763540d6601144",
          "name": "Hydrangeas.jpg"
        },
        "totalLength": 595284,
        "created": 1458717785374,
        "path": "/Pictures",
        "modified": 1458717785374
      }]
    },
    {
      "data": [{
        "action": "download",
        "newVersion": {
          "checksum": "9829c1949b6347cec22467e34b0814dd",
          "name": ".drive-meta"
        },
        "totalLength": 438,
        "data": {
          "id": "103434",
          "default_folder": true,
          "type": 22,
          "own_rights": 403710016,
          "permissions": [{
            "bits": 403710016,
            "entity": 4,
            "group": false
          }],
          "extended_permissions": [{
            "entity": 4,
            "bits": 403710016,
            "type": "user",
            "display_name": "Jens Mander",
            "contact": {
              "email1": "jens@local.ox",
              "last_name": "Mander",
              "first_name": "Jens",
              "image1_url": "/ajax/image/user/picture?id=4&timestamp=1453992059223"
            }
          }],
          "jump": ["permissions"],
          "shareable": true,
          "files": []
        },
        "path": "/Music",
        "modified": 1465453273921
      }]
    }]
```

---
title: Backend Documentation
description: 
---

# Introduction
This project is meant to contain all public documentation of backend components, concepts and APIs. Articles must be written in [Markdown](http://daringfireball.net/projects/markdown/), while [GitHub-flavored markdown](https://help.github.com/articles/github-flavored-markdown/) is explicitly permitted. See the [Cheatsheet](https://github.com/adam-p/markdown-here/wiki/Markdown-Cheatsheet) for a full reference of this markdown dialect. Links to resources like images or other articles must always be relative.


# Eclipse Integration

You can easily write your docs from within your eclipse IDE and even have a almost-live preview. For that you need to install two additional plugins:

  * [Markdown Text Editor](https://marketplace.eclipse.org/content/markdown-text-editor) and
  * [GitHub Flavored Markdown viewer](https://marketplace.eclipse.org/content/github-flavored-markdown-viewer-plugin-eclipse)


# MDWiki

For publishing the docs [MDWiki](http://www.mdwiki.info/) will be used. New articles must be made accessible via the navigation menu by extending `navigation.md` in the `mdwiki` subfolder. You can easily review your changes by making this folder available through a HTTP server. You don't even need an apache or nginx for that, a simple HTTP server can also be run via python or npm. Just open a terminal and switch to the documentation directory.

## Python:
```bash
    python -m SimpleHTTPServer 8080
```

## NodeJS (install via `sudo npm install -g http-server`):

```bash
    http-server
```

Both commands will launch the server on `localhost:8080`. You can then visit the Wiki via `http://localhost:8080/mdwiki/`.

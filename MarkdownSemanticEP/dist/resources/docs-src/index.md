---
Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org
---

# <p align="center">Markdown Semantic Eclipse Plug-in</p>

| [Home](index.html) || [Screenshots](screenshots.html) || [Code](https://github.com/iuscl-ide/MarkdownSemanticEP) |
***

Markdown Semantic is an Eclipse plug-in for displaying and editing [Markdown (.md)](http://daringfireball.net/projects/markdown/syntax) files.
It is based on two components:

- [Flexmark](https://github.com/vsch/flexmark-java) - to parse, format and render Markdown
- [SemanticUI](https://semantic-ui.com/) - to apply style to the rendered HTML

It also uses [highlight.js](https://highlightjs.org/) to format rendered source code

The main goals are speed and quality for both the syntax highlight and the resulted HTML. These qualities don’t seem to be entirely fulfilled by the existing Markdown Eclipse plug-ins 

Besides the normal MD formatting, some particular preferences can be applied (such as text justify, center headers) but these will be visible only on the exported HTML, not if the MD is rendered in another engine

### Install

Install as a normal Eclipse plug-in:

``` Eclipse
Help -> Install new software...
```    
    
And then add the site `http://markdownsemanticep.org/update`

### Features

| Feature                              | Notes                                         |
|--------------------------------------|-----------------------------------------------|
| **Markdown Code formatting**                                                        ||
| Format Markdown selected text        |                                               |
| Make paragraphs 80 characters wide   |                                               |
| Repair broken paragraphs             | Smart remove enters from broken text          |
| Edit in word wrap mode               |                                               |
| **Markdown HTML formatting preferences**                                            ||
| Headers Font                         |                                               |
| Line under headers                   |                                               |
| Center align headers                 |                                               |
| Text Font                            |                                               |
| Justify text paragraphs              |                                               |
| Center align images                  |                                               |
| Code Font                            |                                               |
| Show border around code              |                                               |
| Show background for code             |                                               |
| Show pop-up for code language        | The language declared in MD fenced code block |
| Alternate table rows background      |                                               |
| Show table rows appear as selectable |                                               |
| **Markdown extensions**                                                             ||
| Abbreviation                         |                                               |
| Aside                                |                                               |
| Autolink                             |                                               |
| Definition                           |                                               |
| Emoji                                | Only for codes, like `:something:`            |
| Footnotes                            |                                               |
| Strikethrough                        |                                               |
| Subscript                            |                                               |
| Ins                                  |                                               |
| Superscript                          |                                               |
| Tables                               | With MD text formatting!                      |
| Toc                                  |                                               |
| Typographic                          | Not (c)                                       |

<!-- |  |  | -->
***
<p align="center">| Website hosted by <img style="height:14px;" src="https://assets-cdn.github.com/images/modules/logos_page/GitHub-Logo.png"> || &copy; 2017 <a href="http://markdownsemanticep.org">markdownsemanticep.org</a> |</p>
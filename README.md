---
Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org
---

## <p align="center">Markdown Semantic Eclipse Plug-in</p>

Markdown Semantic is an Eclipse plug-in that displays and edits [Markdown (.md)](http://daringfireball.net/projects/markdown/syntax) files.
It's based on two components:

- [Flexmark](https://github.com/vsch/flexmark-java) - to parse, format and render Markdown
- [SemanticUI](https://semantic-ui.com/) - to apply style to the rendered HTML

It also uses [highlight.js](https://highlightjs.org/) to format rendered source code

The main goals are speed and quality for both the syntax highlight and the resulted HTML. These qualities don’t seem to be entirely fulfilled by the existing Markdown Eclipse plug-ins 

Besides the normal MD formatting, some particular preferences can be applied (such as text justify, center headers) but these will be visible only on the exported HTML, not if the MD is rendered in another engine

More information and some screenshots can be found on the project site [markdownsemanticep.org](http://markdownsemanticep.org)

***

Install as a normal Eclipse plug-in:

``` Eclipse
Help -> Install new software...
```    
    
And then add the site `http://markdownsemanticep.org/update`   
***

<p align="center">&copy; 2017 <a href="http://markdownsemanticep.org">markdownsemanticep.org</a></p>
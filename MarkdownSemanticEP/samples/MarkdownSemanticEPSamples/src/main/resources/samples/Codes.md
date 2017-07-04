## Codes

### Java

``` java


	/** Compact parsed regions */
	private void compactRegions(ArrayList<TypedRegion> regions) {
		
		if (regions.size() == 0) {
			return;
		}
		
		ArrayList<TypedRegion> zeroLengthRegions = new ArrayList<>();
		for (TypedRegion region : regions) {
			if (region.getLength() == 0) {
				zeroLengthRegions.add(region);
			}
		}
		regions.removeAll(zeroLengthRegions);
		
		String prevType = regions.get(0).getType();
		int indexCompact = 1;
		while (indexCompact < regions.size()) {
			TypedRegion region = regions.get(indexCompact);
			String newType = region.getType();
			if (newType.equals(prevType)) {
				TypedRegion prevRegion = regions.get(indexCompact - 1);
				regions.set(indexCompact - 1, new TypedRegion(prevRegion.getOffset(),
						prevRegion.getLength() + region.getLength(), prevType));
				regions.remove(indexCompact);
			}
			else {
				prevType = newType;
				indexCompact++;
			}
		}
	}
	

```

### JavaScript

``` js
var foo = function (bar) {
  return bar++;
};

console.log(foo(5));
```

### Markdown

*Highlight.js* fails miserably for Markdown

``` markdown
---
Header
---
<!-- Start md -->
# Heading
***BoldItalic***
**Bold1 *Italic1 **Bold2** Italic1* Bold1**
        *Italic1           Italic1*
Something
www.md.com

**Inline <!-- Here is the comment --> comment**

**Start comment**
<!--
To be commented
-->
*End comment*

*<p align="center">**HTML** part</p>*

one *one* normalu normal *two* two

++HTML block:++

<div>
<!-- Another inside comment -->
HTML Text
</div>
```
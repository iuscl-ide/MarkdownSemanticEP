/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TypedRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.markdownsemanticep.activator.L;
import org.markdownsemanticep.activator.R;
import org.markdownsemanticep.engine.MarkdownSemanticEPEngine;

import com.vladsch.flexmark.ast.AutoLink;
import com.vladsch.flexmark.ast.Code;
import com.vladsch.flexmark.ast.Emphasis;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.HtmlBlock;
import com.vladsch.flexmark.ast.HtmlCommentBlock;
import com.vladsch.flexmark.ast.HtmlInline;
import com.vladsch.flexmark.ast.HtmlInlineComment;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.ImageRef;
import com.vladsch.flexmark.ast.IndentedCodeBlock;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.MailLink;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ast.NodeVisitor;
import com.vladsch.flexmark.ast.Reference;
import com.vladsch.flexmark.ast.StrongEmphasis;
import com.vladsch.flexmark.ast.ThematicBreak;
import com.vladsch.flexmark.ast.VisitHandler;
import com.vladsch.flexmark.ast.Visitor;
import com.vladsch.flexmark.ext.gfm.strikethrough.Strikethrough;
import com.vladsch.flexmark.ext.ins.Ins;
import com.vladsch.flexmark.ext.jekyll.front.matter.JekyllFrontMatterBlock;

/** Partitions from parser */
public class MarkdownSemanticEPDocumentPartitioner implements IDocumentPartitioner {

	/* MD basic partitions */
	public static final String _000NORMAL = "000NORMAL";
	public static final String _001HEADING = "001HEADING";
	
	public static final String _101BOLD = "101BOLD";
	public static final String _102ITALIC = "102ITALIC";
	public static final String _103UNDERLINE = "103UNDERLINE";
	public static final String _104STRIKETROUGH = "104STRIKETROUGH";

	public static final String _201HEADERCOMMENT = "201HEADERCOMMENT";
	public static final String _202LINK = "202LINK";
	public static final String _203IMAGE = "203IMAGE";

	public static final String _301DIVIDERLINE = "301DIVIDERLINE";

	public static final String _401HTMLCOMMENTBLOCK = "401HTMLCOMMENTBLOCK";
	public static final String _402HTMLINLINECOMMENT = "402HTMLINLINECOMMENT";
	public static final String _403HTMLINLINE = "403HTMLINLINE";
	public static final String _404HTMLBLOCK = "404HTMLBLOCK";

	public static final String _501INLINECODE = "501INLINECODE";
	public static final String _502FENCEDCODEBLOCK = "502FENCEDCODEBLOCK";
	public static final String _503INDENTEDCODEBLOCK = "503INDENTEDCODEBLOCK";

	
	/** All possibilities, to construct */
	private static final ArrayList<String> arrayPartitionTypes = new ArrayList<>();
	/** All possibilities */
	private static String[] partitionTypes;
	/** The text attributes */
	private static final HashMap<String, TextAttribute> partitionTypesTextAttributes = new HashMap<>();

	/* Construct possibilities */
	static {
		addPartitionTypesAttributes(_000NORMAL, _101BOLD, _102ITALIC, _103UNDERLINE, _104STRIKETROUGH, _202LINK, _203IMAGE);
		
		addPartitionTypesAttributes(_000NORMAL + "_" + _001HEADING, _101BOLD, _102ITALIC, _103UNDERLINE, _104STRIKETROUGH, _202LINK, _203IMAGE);
		
		addPartitionTypesAttributes(_000NORMAL + "_" + _201HEADERCOMMENT);
		
		addPartitionTypesAttributes(_000NORMAL + "_" + _301DIVIDERLINE);
		
		addPartitionTypesAttributes(_000NORMAL + "_" + _401HTMLCOMMENTBLOCK);
		addPartitionTypesAttributes(_000NORMAL + "_" + _402HTMLINLINECOMMENT);
		addPartitionTypesAttributes(_000NORMAL + "_" + _403HTMLINLINE);
		addPartitionTypesAttributes(_000NORMAL + "_" + _404HTMLBLOCK);

		addPartitionTypesAttributes(_000NORMAL + "_" + _501INLINECODE);
		addPartitionTypesAttributes(_000NORMAL + "_" + _502FENCEDCODEBLOCK);
		addPartitionTypesAttributes(_000NORMAL + "_" + _503INDENTEDCODEBLOCK);
		
		partitionTypes = (String[]) arrayPartitionTypes.toArray(new String[arrayPartitionTypes.size()]);
		
		for (String partitionType : arrayPartitionTypes) {
			addPartitionTypesTextAttributes(partitionType);
		}
	}
	
	/** One prefix and all combination of attributes */
    private static void addPartitionTypesAttributes(String prefix, String... attributes) {
    	
    	arrayPartitionTypes.add(prefix);
    	int length = attributes.length;
        for (int index = 0; index < length; index++) {
        	addPartitionTypesAttributes(prefix + "_" + attributes[index], Arrays.copyOfRange(attributes, index + 1, length));
        }
    }  

    /** Cache text attributes */
    private static void addPartitionTypesTextAttributes(String partitionType) {
    	
		String[] types = partitionType.split("_");
		
		Color fgColor = null;
		Color bgColor = null;
		int style = SWT.NORMAL;
		
		for (String type : types) {
			
			switch (type) {
			case _001HEADING:
				fgColor = R.getColor(R.Colors.HEADING_FG_COLOR);
				style = style | SWT.BOLD;
				break;
			case _101BOLD:
				style = style | SWT.BOLD;
				break;
			case _102ITALIC:
				style = style | SWT.ITALIC;
				break;
			case _104STRIKETROUGH:
				style = style | TextAttribute.STRIKETHROUGH;
				break;
			case _103UNDERLINE:
				style = style | TextAttribute.UNDERLINE;
				break;
			case _201HEADERCOMMENT:
				fgColor = R.getColor(R.Colors.HEADER_COMMENT_FG_COLOR); 
				break;
			case _202LINK:
				fgColor = R.getColor(R.Colors.LINK_FG_COLOR); 
				break;
			case _203IMAGE:
				bgColor = R.getColor(R.Colors.IMAGE_BG_COLOR); 
				break;
			case _301DIVIDERLINE:
				fgColor = R.getColor(R.Colors.DIVIDER_LINE_FG_COLOR); 
				style = style | SWT.BOLD;
				break;
			case _401HTMLCOMMENTBLOCK:
			case _402HTMLINLINECOMMENT:
				fgColor = R.getColor(R.Colors.COMMENT_FG_COLOR); 
				break;
			case _403HTMLINLINE:
			case _404HTMLBLOCK:				
				fgColor = R.getColor(R.Colors.HTML_FG_COLOR); 
				break;
			case _501INLINECODE:
				bgColor = R.getColor(R.Colors.INLINE_CODE_BG_COLOR); 
				break;
			case _502FENCEDCODEBLOCK:
				bgColor = R.getColor(R.Colors.FENCED_CODE_BG_COLOR); 
				break;
			case _503INDENTEDCODEBLOCK:				
				bgColor = R.getColor(R.Colors.INDENTED_CODE_BG_COLOR); 
				break;
			default:
				break;
			}
		}
		
		partitionTypesTextAttributes.put(partitionType, new TextAttribute(fgColor, bgColor, style));
    }  
	
    /** Always trough here to have the same thing */
    public static String[] getPartitionTypes() {
    	return partitionTypes;
    }

    /** Cached */
    public static TextAttribute findTextAttribute(String partitionType) {
    	
    	return partitionTypesTextAttributes.get(partitionType);
    }

    /* For color */
    private static RGB FENCED_CODE_BG_COLOR_RGB = R.getColor(R.Colors.FENCED_CODE_BG_COLOR).getRGB();
    private static RGB COMMENT_FG_COLOR_RGB = R.getColor(R.Colors.COMMENT_FG_COLOR).getRGB();
    private static RGB HEADER_COMMENT_FG_COLOR_RGB = R.getColor(R.Colors.HEADER_COMMENT_FG_COLOR).getRGB();
    
    
    /* Non static */
	
    private StyledText styledText;
    
    /** The document */
    private MarkdownSemanticEPDocument document;
    
    /** The original parsed ones */
	private final HashMap<String, String> linearPartitions = new HashMap<>();

	/** The calculated, non-overlapping ones */
	private final ArrayList<TypedRegion> parsedRegions = new ArrayList<>();
	
	/** After document modification */
	private int aboutToBeChangedEndOffset = -1;
	private int aboutToBeChangedTail = 0;

	private int changedStartOffset = 0;
	private int changedEndOffset = -1;

	/** Key is the offset, and for equal offset, the later ones are put after first ones which are theirs parent */
	private String findKey(int offset) {
		return findKey(offset, false);
	}

	/** Key is the offset, and for equal offset, the later ones are put before first ones which are theirs parent */
	private String findEndKey(int offset) {
		return findKey(offset, true);
	}
	
	/** Find key */
	private String findKey(int offset, boolean findEndKey) {

		String keyStart = "" + offset;
		keyStart = "00000000".substring(keyStart.length()) + keyStart;
		
		int cnt = 50000000;
		String cntKey = "" + cnt;
		String key = keyStart + "_" + "00000000".substring(cntKey.length()) + cntKey;
		while (linearPartitions.containsKey(key)) {
			cnt = findEndKey ? cnt - 1 : cnt + 1;
			cntKey = "" + cnt;
			key = keyStart + "_" + "00000000".substring(cntKey.length()) + cntKey;
		}
		
		return key;
	}
	
	/** Linearize the partition nodes */
	private NodeVisitor partitionsNodeVisitor = new NodeVisitor(
		new VisitHandler<Heading>(Heading.class, new Visitor<Heading>() {
			@Override
			public void visit(Heading heading) {

				linearPartitions.put(findKey(heading.getStartOffset()), _001HEADING);
				linearPartitions.put(findEndKey(heading.getEndOffset()), _001HEADING + ")");
				partitionsNodeVisitor.visitChildren(heading);
			}
		}),
		new VisitHandler<Emphasis>(Emphasis.class, new Visitor<Emphasis>() {
			@Override
			public void visit(Emphasis emphasis) {

				linearPartitions.put(findKey(emphasis.getStartOffset()), _102ITALIC);
				linearPartitions.put(findEndKey(emphasis.getEndOffset()), _102ITALIC + ")");
				partitionsNodeVisitor.visitChildren(emphasis);
			}
		}),
		new VisitHandler<StrongEmphasis>(StrongEmphasis.class, new Visitor<StrongEmphasis>() {
			@Override
			public void visit(StrongEmphasis strongEmphasis) {

				linearPartitions.put(findKey(strongEmphasis.getStartOffset()), _101BOLD);
				linearPartitions.put(findEndKey(strongEmphasis.getEndOffset()), _101BOLD + ")");
				partitionsNodeVisitor.visitChildren(strongEmphasis);
			}
		}),
		new VisitHandler<JekyllFrontMatterBlock>(JekyllFrontMatterBlock.class, new Visitor<JekyllFrontMatterBlock>() {
			@Override
			public void visit(JekyllFrontMatterBlock jekyllFrontMatterBlock) {

				linearPartitions.put(findKey(jekyllFrontMatterBlock.getStartOffset()), _201HEADERCOMMENT);
				linearPartitions.put(findEndKey(jekyllFrontMatterBlock.getEndOffset()), _201HEADERCOMMENT + ")");
				partitionsNodeVisitor.visitChildren(jekyllFrontMatterBlock);
			}
		}),
		new VisitHandler<ThematicBreak>(ThematicBreak.class, new Visitor<ThematicBreak>() {
			@Override
			public void visit(ThematicBreak thematicBreak) {

				linearPartitions.put(findKey(thematicBreak.getStartOffset()), _301DIVIDERLINE);
				linearPartitions.put(findEndKey(thematicBreak.getEndOffset()), _301DIVIDERLINE + ")");
				partitionsNodeVisitor.visitChildren(thematicBreak);
			}
		}),
		new VisitHandler<Strikethrough>(Strikethrough.class, new Visitor<Strikethrough>() {
			@Override
			public void visit(Strikethrough strikethrough) {

				linearPartitions.put(findKey(strikethrough.getStartOffset()), _104STRIKETROUGH);
				linearPartitions.put(findEndKey(strikethrough.getEndOffset()), _104STRIKETROUGH + ")");
				partitionsNodeVisitor.visitChildren(strikethrough);
			}
		}),
		new VisitHandler<Ins>(Ins.class, new Visitor<Ins>() {
			@Override
			public void visit(Ins ins) {

				linearPartitions.put(findKey(ins.getStartOffset()), _103UNDERLINE);
				linearPartitions.put(findEndKey(ins.getEndOffset()), _103UNDERLINE + ")");
				partitionsNodeVisitor.visitChildren(ins);
			}
		}),
		new VisitHandler<Link>(Link.class, new Visitor<Link>() {
			@Override
			public void visit(Link link) {

				linearPartitions.put(findKey(link.getStartOffset()), _202LINK);
				linearPartitions.put(findEndKey(link.getEndOffset()), _202LINK + ")");
				partitionsNodeVisitor.visitChildren(link);
			}
		}),
		new VisitHandler<MailLink>(MailLink.class, new Visitor<MailLink>() {
			@Override
			public void visit(MailLink mailLink) {

				linearPartitions.put(findKey(mailLink.getStartOffset()), _202LINK);
				linearPartitions.put(findEndKey(mailLink.getEndOffset()), _202LINK + ")");
				partitionsNodeVisitor.visitChildren(mailLink);
			}
		}),
		new VisitHandler<AutoLink>(AutoLink.class, new Visitor<AutoLink>() {
			@Override
			public void visit(AutoLink autoLink) {

				linearPartitions.put(findKey(autoLink.getStartOffset()), _202LINK);
				linearPartitions.put(findEndKey(autoLink.getEndOffset()), _202LINK + ")");
				partitionsNodeVisitor.visitChildren(autoLink);
			}
		}),
		new VisitHandler<Image>(Image.class, new Visitor<Image>() {
			@Override
			public void visit(Image image) {

				linearPartitions.put(findKey(image.getStartOffset()), _203IMAGE);
				linearPartitions.put(findEndKey(image.getEndOffset()), _203IMAGE + ")");
				partitionsNodeVisitor.visitChildren(image);
			}
		}),
		new VisitHandler<ImageRef>(ImageRef.class, new Visitor<ImageRef>() {
			@Override
			public void visit(ImageRef imageRef) {

				linearPartitions.put(findKey(imageRef.getStartOffset()), _203IMAGE);
				linearPartitions.put(findEndKey(imageRef.getEndOffset()), _203IMAGE + ")");
				partitionsNodeVisitor.visitChildren(imageRef);
			}
		}),
		new VisitHandler<Reference>(Reference.class, new Visitor<Reference>() {
			@Override
			public void visit(Reference reference) {
					
				linearPartitions.put(findKey(reference.getStartOffset()), _203IMAGE);
				linearPartitions.put(findEndKey(reference.getEndOffset()), _203IMAGE + ")");
				partitionsNodeVisitor.visitChildren(reference);
			}
		}),
		new VisitHandler<HtmlCommentBlock>(HtmlCommentBlock.class, new Visitor<HtmlCommentBlock>() {
			@Override
			public void visit(HtmlCommentBlock htmlCommentBlock) {

				linearPartitions.put(findKey(htmlCommentBlock.getStartOffset()), _401HTMLCOMMENTBLOCK);
				linearPartitions.put(findEndKey(htmlCommentBlock.getEndOffset()), _401HTMLCOMMENTBLOCK + ")");
				partitionsNodeVisitor.visitChildren(htmlCommentBlock);
			}
		}),
		new VisitHandler<HtmlInlineComment>(HtmlInlineComment.class, new Visitor<HtmlInlineComment>() {
			@Override
			public void visit(HtmlInlineComment htmlInlineComment) {

				linearPartitions.put(findKey(htmlInlineComment.getStartOffset()), _402HTMLINLINECOMMENT);
				linearPartitions.put(findEndKey(htmlInlineComment.getEndOffset()), _402HTMLINLINECOMMENT + ")");
				partitionsNodeVisitor.visitChildren(htmlInlineComment);
			}
		}),
		new VisitHandler<HtmlInline>(HtmlInline.class, new Visitor<HtmlInline>() {
			@Override
			public void visit(HtmlInline htmlInline) {
				
				linearPartitions.put(findKey(htmlInline.getStartOffset()), _403HTMLINLINE);
				linearPartitions.put(findEndKey(htmlInline.getEndOffset()), _403HTMLINLINE + ")");
				partitionsNodeVisitor.visitChildren(htmlInline);
			}
		}),
		new VisitHandler<HtmlBlock>(HtmlBlock.class, new Visitor<HtmlBlock>() {
			@Override
			public void visit(HtmlBlock htmlBlock) {
				
				linearPartitions.put(findKey(htmlBlock.getStartOffset()), _403HTMLINLINE);
				linearPartitions.put(findEndKey(htmlBlock.getEndOffset()), _403HTMLINLINE + ")");
				partitionsNodeVisitor.visitChildren(htmlBlock);
			}
		}),
		new VisitHandler<Code>(Code.class, new Visitor<Code>() {
			@Override
			public void visit(Code codeBlock) {

				linearPartitions.put(findKey(codeBlock.getStartOffset()), _501INLINECODE);
				linearPartitions.put(findEndKey(codeBlock.getEndOffset()), _501INLINECODE + ")");
				partitionsNodeVisitor.visitChildren(codeBlock);
			}
		}),
		new VisitHandler<FencedCodeBlock>(FencedCodeBlock.class, new Visitor<FencedCodeBlock>() {
			@Override
			public void visit(FencedCodeBlock fencedCodeBlock) {

				linearPartitions.put(findKey(fencedCodeBlock.getStartOffset()), _502FENCEDCODEBLOCK);
				linearPartitions.put(findEndKey(fencedCodeBlock.getEndOffset()), _502FENCEDCODEBLOCK + ")");
				partitionsNodeVisitor.visitChildren(fencedCodeBlock);
			}
		}),
		new VisitHandler<IndentedCodeBlock>(IndentedCodeBlock.class, new Visitor<IndentedCodeBlock>() {
			@Override
			public void visit(IndentedCodeBlock indentedCodeBlock) {

				linearPartitions.put(findKey(indentedCodeBlock.getStartOffset()), _503INDENTEDCODEBLOCK);
				linearPartitions.put(findEndKey(indentedCodeBlock.getEndOffset()), _503INDENTEDCODEBLOCK + ")");
				partitionsNodeVisitor.visitChildren(indentedCodeBlock);
			}
		})		
	);

	/** Calculate non-overlapping partitions from the parsed partitions */ 
	private void parseDocumentRegions(int parseStartOffset, int parseEndOffset) {
		
		String documentText = document.get();

//		L.p("parseDocumentRegions, parseStartOffset " + parseStartOffset + " -- parseEndOffset " + parseEndOffset);
		
		String textToParse = documentText.substring(parseStartOffset, parseEndOffset);
//		L.p("------------------------------------------------------------------------------------------------");
//		L.p(textToParse);
//		L.p("------------------------------------------------------------------------------------------------");
		
		Node documentNode = MarkdownSemanticEPEngine.parseMarkdown(textToParse);
		
		/* Linearize */
		linearPartitions.clear();
		partitionsNodeVisitor.visitChildren(documentNode);

		ArrayList<String> linearKeys = new ArrayList<>(linearPartitions.keySet());
		Collections.sort(linearKeys);
		
		ArrayList<Integer> linearOffsets = new ArrayList<>();
		ArrayList<String> linearTypes = new ArrayList<>();
		
		for (String key : linearKeys) {
			linearOffsets.add(Integer.parseInt(key.split("_")[0]));
			linearTypes.add(linearPartitions.get(key));
		}

		/* Non overlapping */
		parsedRegions.clear();
		
		Stack<String> typeStack = new Stack<>();
		typeStack.push(_000NORMAL);
		
		int linearOffsetsSize = linearOffsets.size();
		Integer lastOffset = 0;
		
		for (int index = 0; index < linearOffsetsSize; index++) {
			
			int offset = linearOffsets.get(index);
			
			String lastType = typeStack.peek();
			String newType = linearTypes.get(index);
			
			/* Based on the idea that all the attributes should be taken only once */
			HashSet<String> previousUniqueTypes = new HashSet<>(); 
			for (int stackIndex = 0; stackIndex < typeStack.size(); stackIndex++) {
				previousUniqueTypes.add(typeStack.get(stackIndex));
			}		
			
			/* Exception _402HTMLINLINECOMMENT */
			doTypeException(previousUniqueTypes, _402HTMLINLINECOMMENT);
			
			/* Exception _403HTMLINLINE */
			doTypeException(previousUniqueTypes, _403HTMLINLINE);

			doTypeException(previousUniqueTypes, _501INLINECODE);
//			doTypeException(previousUniqueTypes, _502FENCEDCODEBLOCK);
//			doTypeException(previousUniqueTypes, _503INDENTEDCODEBLOCK);
			
			ArrayList<String> previousTypes = new ArrayList<>(previousUniqueTypes);
			Collections.sort(previousTypes);
			
			String overlappingPartitionType = "";
			String sep = "";
			for (String type : previousTypes) {
				overlappingPartitionType = overlappingPartitionType + sep + type;
				sep = "_";
			}
			
			if (newType.equals(lastType + ")")) { // same type
				typeStack.pop();
			}
			else { // different type
				typeStack.push(newType);
			}

			int length = offset - lastOffset;
			if (length > 0) {
				parsedRegions.add(new TypedRegion(parseStartOffset + lastOffset, length, overlappingPartitionType));
			}
			lastOffset = offset;
		}

		int textSize = textToParse.length();
		
		/* No regions found */
		if (parsedRegions.size() == 0) {
			//regions.add(new TypedRegion(changedStartOffset + 0, textSize, _000NORMAL));
			parsedRegions.add(new TypedRegion(parseStartOffset, textSize, _000NORMAL));
		}
		
		TypedRegion firstPartition = parsedRegions.get(0);
		if (firstPartition.getLength() == 0) { // first not undefined
			parsedRegions.remove(0);
		}
		
		TypedRegion lastPartition = parsedRegions.get(parsedRegions.size() - 1);
		int lastPosition = lastPartition.getOffset() + lastPartition.getLength();
		if (lastPosition != parseStartOffset + textSize) {
		//if (lastPosition != parseEndOffset) {
			parsedRegions.add(new TypedRegion(lastPosition, (parseStartOffset + textSize) - lastPosition, _000NORMAL));
			//regions.add(new TypedRegion(lastPosition, textSize - lastPosition, _000NORMAL));
			//regions.add(new TypedRegion(lastPosition, parseEndOffset - lastPosition, _000NORMAL));
		}

		/* Compact parsed regions */
		compactRegions(parsedRegions);
		
//		L.p("--------------------------------------------------------------------------------");
//		for (int index = 0; index < parsedRegions.size(); index++) {
//			TypedRegion region = parsedRegions.get(index);
//			L.p("Region " + region.getOffset() + " -> " + region.getLength() + " : " + region.getType());
//		}
	}

	/** Type from colors */
	private String findTypeAtOffset(int offset) {
		
		StyleRange styleRange = styledText.getStyleRangeAtOffset(offset);
		
		if (styleRange != null) {
			if ((styleRange.foreground != null) && (isSameRGB(styleRange.foreground.getRGB(), HEADER_COMMENT_FG_COLOR_RGB))) {
				/* In the header */
				return _201HEADERCOMMENT;
			}
			if ((styleRange.background != null) && (isSameRGB(styleRange.background.getRGB(), FENCED_CODE_BG_COLOR_RGB))) {
				/* In a code */
				return _502FENCEDCODEBLOCK;
			}
			if ((styleRange.foreground != null) && (isSameRGB(styleRange.foreground.getRGB(), COMMENT_FG_COLOR_RGB))) {
				/* In a comment */
				return _401HTMLCOMMENTBLOCK;
			}
		}
		
		return null;
	}

	/** All in a block because no separators inside */
	private boolean isOneBlock(int startOffset, int endOffset, String existingStart, String existingEnd,
			String blockStyle, String blockStyleType, String startSeparator, String endSeparator) {
		
		String documentText = document.get();
		
		String blockText = documentText.substring(startOffset, endOffset);
		if (blockStyle.equals(blockStyleType)) {

			/* Verify if a separator was introduced */
			boolean cont = (blockText.indexOf(startSeparator) == -1) && (blockText.indexOf(endSeparator) == -1);
			
			if (cont) {
				/* No separator was introduced */
				
				/* Verify if a separator was possibly affected */
				for (int index = 1; index < startSeparator.length(); index++) {
					String startSeparatorPart = startSeparator.substring(0, index); 
					if (existingStart.endsWith(startSeparatorPart)) {
						return false;
					}
				}
				for (int index = 0; index < endSeparator.length() - 1; index++) {
					String endSeparatorPart = endSeparator.substring(index); 
					if (existingEnd.startsWith(endSeparatorPart)) {
						return false;
					}
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	/** Calculate non-overlapping partitions from blocks or from the parsed partitions */ 
	private void createParserPartitions(int damageStartOffset, int damageEndOffset) {

		String documentText = document.get();
		int textSize = documentText.length();

		if (textSize == 0) {
			parsedRegions.clear();
			parsedRegions.add(new TypedRegion(0, 0, _000NORMAL));
			return;
		}
		
		int startBlockOffset = damageStartOffset;
		if (startBlockOffset < 5) {
			startBlockOffset = 0;
		}
		else {
			startBlockOffset = startBlockOffset - 5;
		}
		String existingStart = documentText.substring(startBlockOffset, damageStartOffset);
		
		int endBlockOffset = damageEndOffset;
		if (textSize - endBlockOffset < 4) {
			endBlockOffset = textSize;
		}
		else {
			endBlockOffset = endBlockOffset + 4;
		}
		String existingEnd = documentText.substring(damageEndOffset, endBlockOffset);
		
		String startStyle = findTypeAtOffset(startBlockOffset);
		String endStyle = null;
		if (textSize == endBlockOffset) {
			endStyle = findTypeAtOffset(endBlockOffset - 1);
		}
		else {
			endStyle = findTypeAtOffset(endBlockOffset);
		}
		
		if (startStyle != null && endStyle != null) {
			
			if (startStyle.equals(endStyle)) {
				/* Same style */
				
				int damageSize = damageEndOffset - damageStartOffset;
				
				if (isOneBlock(startBlockOffset, endBlockOffset, existingStart, existingEnd, startStyle, _201HEADERCOMMENT, "---", "---")) {
					parsedRegions.clear();
					parsedRegions.add(new TypedRegion(damageStartOffset, damageSize, _000NORMAL + "_" + _201HEADERCOMMENT));
					return;
				}
				if (isOneBlock(startBlockOffset, endBlockOffset, existingStart, existingEnd, startStyle, _201HEADERCOMMENT, "---", "...")) {
					parsedRegions.clear();
					parsedRegions.add(new TypedRegion(damageStartOffset, damageSize, _000NORMAL + "_" + _201HEADERCOMMENT));
					return;
				}
				if (isOneBlock(startBlockOffset, endBlockOffset, existingStart, existingEnd, startStyle, _502FENCEDCODEBLOCK, "```", "```")) {
					parsedRegions.clear();
					parsedRegions.add(new TypedRegion(damageStartOffset, damageSize, _000NORMAL + "_" + _502FENCEDCODEBLOCK));
					return;
				}
				if (isOneBlock(startBlockOffset, endBlockOffset, existingStart, existingEnd, startStyle, _401HTMLCOMMENTBLOCK, "<!--", "-->")) {
					
					parsedRegions.clear();
					parsedRegions.add(new TypedRegion(damageStartOffset, damageSize, _000NORMAL + "_" + _401HTMLCOMMENTBLOCK));
					return;
				}
			}
		}
		
		int startParseOffset = findStartParseOffset(damageStartOffset);
		int endParseOffset = findEndParseOffset(startParseOffset, damageEndOffset);

		parseDocumentRegions(startParseOffset, endParseOffset);
		
		TypedRegion lastParsedRegion = parsedRegions.get(parsedRegions.size() - 1);

		String lastParsedRegionType = lastParsedRegion.getType();
		if (lastParsedRegionType.equals(_000NORMAL + "_" + _201HEADERCOMMENT) ||
				lastParsedRegionType.equals(_000NORMAL + "_" + _502FENCEDCODEBLOCK) ||
				lastParsedRegionType.equals(_000NORMAL + "_" + _401HTMLCOMMENTBLOCK) ||
				lastParsedRegionType.equals(_000NORMAL + "_" + _402HTMLINLINECOMMENT)) {
			/* First parse ends in block */
			parseDocumentRegions(startParseOffset, textSize);
		}
	}

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
	
	/** Clear and put only the type */
	private void doTypeException(HashSet<String> previousUniqueTypes, String type) {
		
		if (previousUniqueTypes.contains(type)) {
			previousUniqueTypes.clear();
			previousUniqueTypes.add(_000NORMAL);
			previousUniqueTypes.add(type);
		}
	}

	/** Start parse piece */
	private int findStartParseOffset(int damageStartOffset) {
		
		String documentText = document.get();
		String enter = document.getDefaultLineDelimiter();
		String doubleEnter = enter + enter;
		int doubleEnterSize = doubleEnter.length();

		if (damageStartOffset < doubleEnterSize) {
			/* No place before for a double enter */
			return 0;
		}
		
		int startParseOffset = damageStartOffset - doubleEnterSize;
		
		boolean cont = true;
		do {
			startParseOffset = documentText.lastIndexOf(doubleEnter, startParseOffset);
			if (startParseOffset != -1) {
				/* Double enter found before */
				StyleRange styleRange = styledText.getStyleRangeAtOffset(startParseOffset);
//				L.p("findStartParseOffset, doubleEnterIndex " + startParseOffset + " -- styleRange " + styleRange);
				
				if (styleRange == null) {
					/* Double enter on normal */
					return startParseOffset;
				}
				if ((styleRange.foreground != null) && (isSameRGB(styleRange.foreground.getRGB(), HEADER_COMMENT_FG_COLOR_RGB))) {
					/* In the header */
					return 0;
				}
				else if ((styleRange.background != null) && (isSameRGB(styleRange.background.getRGB(), FENCED_CODE_BG_COLOR_RGB))) {
					/* In a code */
					startParseOffset = startParseOffset - doubleEnterSize;
				}
				else if ((styleRange.foreground != null) && (isSameRGB(styleRange.foreground.getRGB(), COMMENT_FG_COLOR_RGB))) {
					/* In a comment */
					startParseOffset = startParseOffset - doubleEnterSize;
				}
				else {
					/* Double enter on normal */
					return startParseOffset;
				}
			}
			else {
				/* No double enter until the beginning of the document */
				return 0;
			}
		}
		while (cont);
		
		return startParseOffset;
	}

	/** End parse piece */
	private int findEndParseOffset(int startParseOffset, int damageEndOffset) {
		
		String documentText = document.get();
		int textSize = documentText.length();
		String enter = document.getDefaultLineDelimiter();
		String doubleEnter = enter + enter;
		int doubleEnterSize = doubleEnter.length();

		if (damageEndOffset > textSize - doubleEnterSize) {
			/* No more place after for a double enter */
			return damageEndOffset;
		}
		
		int endParseOffset = damageEndOffset;
		
		boolean cont = true;
		do {
			endParseOffset = documentText.indexOf(doubleEnter, endParseOffset);
			if (endParseOffset != -1) {
				/* Double enter found after */
				StyleRange styleRange = styledText.getStyleRangeAtOffset(endParseOffset);
//				L.p("findEndParseOffset, endParseOffset " + endParseOffset + " -- styleRange " + styleRange);

				if (styleRange == null) {
					/* Double enter on normal */
					return endParseOffset + doubleEnter.length();
				}
				if ((styleRange.foreground != null) && (isSameRGB(styleRange.foreground.getRGB(), HEADER_COMMENT_FG_COLOR_RGB))) {
					/* In header */
					endParseOffset = endParseOffset + doubleEnter.length();
				}
				else if ((styleRange.background != null) && (isSameRGB(styleRange.background.getRGB(), FENCED_CODE_BG_COLOR_RGB))) {
					/* In a code */
					endParseOffset = endParseOffset + doubleEnter.length();
				}
				else if ((styleRange.foreground != null) && (isSameRGB(styleRange.foreground.getRGB(), COMMENT_FG_COLOR_RGB))) {
					/* In a comment */
					endParseOffset = endParseOffset + doubleEnter.length();
				}
				else {
					/* Double enter on normal */
					return endParseOffset + doubleEnter.length();
				}
			}
			else {
				/* No double enter until the end of document */
				return textSize;
			}
		}
		while (cont);

		return endParseOffset;
	}
	
	/** This will go into MarkdownSemanticEPSourceViewerConfiguration MarkdownSemanticEPDamagerRepairer getDamageRegion */
	@Override
	public ITypedRegion getPartition(int offset) {
//		L.p("getPartition " + offset);
		return new TypedRegion(offset, changedEndOffset - offset, _000NORMAL);
		//return new TypedRegion(offset, changedEndOffset - offset, "__dftl_partition_content_type");
	}

	/** Never */
	@Override
	public String[] getLegalContentTypes() {
		L.p("getLegalContentTypes");
		return null;
	}
	
	/** No idea */
	@Override
	public String getContentType(int offset) {
//		L.p("getContentType " + offset);

//		return _000NORMAL;
		return "__dftl_partition_content_type";
	}

	/** After change, get the change */
	@Override
	public boolean documentChanged(DocumentEvent documentEvent) {

		changedStartOffset = documentEvent.getOffset();
		int changedSize = documentEvent.getDocument().getLength();
		changedEndOffset = changedSize - aboutToBeChangedTail;

//		L.p("documentChanged: changedStartOffset " + changedStartOffset + ", changedEndOffset " + changedEndOffset);
		
		return false;
	}

	/** Before change, get the change */
	@Override
	public void documentAboutToBeChanged(DocumentEvent documentEvent) {

		changedStartOffset = documentEvent.getOffset();
		aboutToBeChangedEndOffset = changedStartOffset + documentEvent.getLength();
		int aboutToBeChangedSize = documentEvent.getDocument().getLength();
		aboutToBeChangedTail = aboutToBeChangedSize - aboutToBeChangedEndOffset;
		changedEndOffset = aboutToBeChangedEndOffset;
		
//		L.p("documentAboutToBeChanged: aboutToBeChangedEndOffset " + aboutToBeChangedEndOffset + ", aboutToBeChangedTail " + aboutToBeChangedTail);
	}
	
	/** Not used */
	@Override
	public void disconnect() {
		L.p("disconnect");
	}
	
	/** Here is aware of the document */
	@Override
	public void connect(IDocument document) {
		//L.p("connect");
		this.document = (MarkdownSemanticEPDocument) document; 
	}

	/** Parse and create regions */
	@Override
	public ITypedRegion[] computePartitioning(int damageOffset, int damageLength) {
		
//		L.p("computePartitioning damageOffset=" + damageOffset + ", damageLength=" + damageLength);
		createParserPartitions(damageOffset, damageOffset + damageLength);
		return (TypedRegion[]) parsedRegions.toArray(new TypedRegion[parsedRegions.size()]);
		
//		return new TypedRegion[0];
	}

	/** To obtain the underlying styled text */
	public void setStyledText(StyledText styledText) {
		this.styledText = styledText;
	}

	/** Compare two RGB */
	private boolean isSameRGB(RGB scrRGB, RGB destRGB) {
		
		if ((scrRGB.blue == destRGB.blue) && (scrRGB.green == destRGB.green) && (scrRGB.red == destRGB.red)) {
			return true;
		}
		return false;
	}
	
}

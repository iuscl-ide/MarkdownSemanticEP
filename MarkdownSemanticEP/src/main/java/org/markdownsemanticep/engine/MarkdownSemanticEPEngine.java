/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.engine;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.markdownsemanticep.activator.L;
import org.markdownsemanticep.activator.R;
import org.markdownsemanticep.editors.MarkdownSemanticEPEmojis;
import org.markdownsemanticep.preferences.MarkdownSemanticEPPreferences;
import org.markdownsemanticep.preferences.MarkdownSemanticEPPreferences.PreferenceKey;

import com.vladsch.flexmark.Extension;
import com.vladsch.flexmark.ast.AutoLink;
import com.vladsch.flexmark.ast.Code;
import com.vladsch.flexmark.ast.Document;
import com.vladsch.flexmark.ast.FencedCodeBlock;
import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Image;
import com.vladsch.flexmark.ast.ImageRef;
import com.vladsch.flexmark.ast.IndentedCodeBlock;
import com.vladsch.flexmark.ast.Link;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ast.ThematicBreak;
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension;
import com.vladsch.flexmark.ext.aside.AsideExtension;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.emoji.Emoji;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.emoji.internal.EmojiCheatSheet;
import com.vladsch.flexmark.ext.emoji.internal.EmojiCheatSheet.EmojiShortcut;
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension;
import com.vladsch.flexmark.ext.ins.InsExtension;
import com.vladsch.flexmark.ext.jekyll.front.matter.JekyllFrontMatterExtension;
import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TableCell;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.ext.toc.TocExtension;
import com.vladsch.flexmark.ext.typographic.TypographicExtension;
import com.vladsch.flexmark.formatter.internal.Formatter;
import com.vladsch.flexmark.html.AttributeProvider;
import com.vladsch.flexmark.html.CustomNodeRenderer;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html.HtmlRenderer.Builder;
import com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.IndependentAttributeProviderFactory;
import com.vladsch.flexmark.html.renderer.AttributablePart;
import com.vladsch.flexmark.html.renderer.CoreNodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.superscript.SuperscriptExtension;
import com.vladsch.flexmark.util.html.Attribute;
import com.vladsch.flexmark.util.html.Attributes;
import com.vladsch.flexmark.util.options.DataHolder;
import com.vladsch.flexmark.util.options.MutableDataHolder;

/** Markdown operations */
public class MarkdownSemanticEPEngine {

	/** Markdown parser */
	private final static Parser parser;

	private static final HashMap<Node, MarkdownSemanticEPPreferences> documentPreferences = new HashMap<>();
	private static final HashMap<Node, HashMap<String, String>> documentOtherPreferences = new HashMap<>();
	
	/** Markdown renderer */
	private final static HtmlRenderer renderer;

	/** Markdown formatter */
	private final static Formatter formatter;

	/** Index template */
	private static final String indexHtmlTemplate;
	
	/** Specific CSS template */
	private static final String markdownSemanticCssResTemplate;
	
	private static final String[] headerFonts = { "Times New Roman", "Lato", "Helvetica Neue", "Arial", "Helvetica", "sans-serif" };
	private static final String[] textFonts = { "Calibri", "Lucida Grande", "FreeSans", "Lato", "Helvetica Neue", "Arial", "Helvetica", "sans-serif" };
	private static final String[] codeFonts = { "Consolas", "Monaco", "Menlo", "Ubuntu Mono", "source-code-pro", "monospace" };
	
	/** Own HTML rendering */
	private static class MarkdownSemanticEPRenderExtension implements HtmlRendererExtension {

		/** To create like the other extensions */
        public static MarkdownSemanticEPRenderExtension create() {
            return new MarkdownSemanticEPRenderExtension();
        }

        /** Own rendering */
		@Override
		public void extend(final Builder rendererBuilder, final String rendererType) {
			
			rendererBuilder.nodeRendererFactory(new NodeRendererFactory() {
				@Override
				public NodeRenderer create(final DataHolder paramDataHolder) {
					return new NodeRenderer() {
						/* To replace the HTML tag */
						@Override
						public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
							
							NodeRenderingHandler<ThematicBreak> thematicBreakRenderingHandler = new NodeRenderingHandler<ThematicBreak>(ThematicBreak.class, new CustomNodeRenderer<ThematicBreak>() {
			                    @Override
			                    public void render(ThematicBreak node, NodeRendererContext context, HtmlWriter html) {
			                        //html.srcPos(node.getChars()).withAttr().tagVoidLine("hr");
			                        //"<div class=\"ui divider\"></div>"
			                    	html.attr("class", "ui divider");
			                    	html.srcPos(node.getChars()).withAttr().tag("div");
			                    	html.tag("/div").line();
			                    }
			                });
							
							Set<NodeRenderingHandler<?>> nodeRenderingHandlers = new HashSet<>();
							nodeRenderingHandlers.add(thematicBreakRenderingHandler);
							return nodeRenderingHandlers;
						}
					};
				}
			});
			
			rendererBuilder.attributeProviderFactory(new IndependentAttributeProviderFactory() {
				@Override
				public AttributeProvider create(NodeRendererContext context) {
					return new AttributeProvider() {
						/* HTML attributes of a MD node */
						@Override
						public void setAttributes(final Node node, final AttributablePart part, final Attributes attributes) {

//							L.p("node = " + node + " part = " + part.getName());
							
							/* Heading */
							if ((node instanceof Heading) && (part == AttributablePart.NODE)) {
								
								Document document = findDocument(node);
								MarkdownSemanticEPPreferences preferences = documentPreferences.get(document);
								
								String cssClass = "ui header";
								String preferenceShowDividerUnderHeaders = preferences.getPreference(PreferenceKey.ShowDividerUnderHeaders);
								if (preferenceShowDividerUnderHeaders.equals("true")) {
									cssClass = "ui dividing header";
								}
								attributes.addValue("class", cssClass);

								String preferenceCenterAlignHeaders = preferences.getPreference(PreferenceKey.CenterAlignHeaders);
								if (preferenceCenterAlignHeaders.equals("true")) {
									attributes.addValue("align", "center");
								}
							}
							
							/* Emojis */
							if (node instanceof Emoji) {
								
								Attribute attribute = attributes.get("src");
								String emojiSeq = attribute.getValue();
								String emojiKey = emojiSeq.substring(5, emojiSeq.indexOf(".png"));
								EmojiShortcut emojiShortcut = EmojiCheatSheet.shortCutMap.get(emojiKey);
								if (emojiShortcut != null) {
									emojiSeq = emojiShortcut.url;
								}
								attributes.replaceValue("src", emojiSeq);
							}
							
							/* Table */
							if (node instanceof TableBlock) {

								Document document = findDocument(node);
								MarkdownSemanticEPPreferences preferences = documentPreferences.get(document);
								
								String cssClass = "ui collapsing compact";
								String preferenceAlternateTableRowsBackground = preferences.getPreference(PreferenceKey.AlternateTableRowsBackground);
								if (preferenceAlternateTableRowsBackground.equals("true")) {
									cssClass = cssClass + " striped";
								}
								String preferenceShowTableRowsAppearSelectable = preferences.getPreference(PreferenceKey.ShowTableRowsAppearSelectable);
								if (preferenceShowTableRowsAppearSelectable.equals("true")) {
									cssClass = cssClass + " selectable";
								}
								cssClass = cssClass + " celled table";
								
								attributes.addValue("class", cssClass);
							}

							/* Table Cell */
							if (node instanceof TableCell) {
								
								Attribute attribute = attributes.get("align");
								if (attribute != null) {
									String alignSeq = attribute.getValue();
									String alignValue = "left";
									if (alignSeq.equals("right")) {
										alignValue = "right";
									}
									else if (alignSeq.equals("center")) {
										alignValue = "center";
									}
									
									attributes.addValue("class", alignValue + " aligned");
									attributes.remove(attribute);
								}
							}
							
							/* In-line code block */
							if ((node instanceof Code) && (part == AttributablePart.NODE)) {

								Document document = findDocument(node);
								MarkdownSemanticEPPreferences preferences = documentPreferences.get(document);
								String cssClass = "ui inlinecodeMDSE";
								String preferenceHaveShowBackgroundForCode = preferences.getPreference(PreferenceKey.ShowBackgroundForCode);
								if (preferenceHaveShowBackgroundForCode.equals("true")) {
									cssClass = cssClass + " yellowercodeMDSE";
								}
								else {
									cssClass = cssClass + " grayercodeMDSE";
								}

								attributes.addValue("class", cssClass);
							}

							/* Indented and fenced code block */
							if (((node instanceof IndentedCodeBlock) || (node instanceof FencedCodeBlock)) && (part == AttributablePart.NODE)) {
								
								Document document = findDocument(node);
								MarkdownSemanticEPPreferences preferences = documentPreferences.get(document);
								String cssClass = "ui";

								String preferenceShowBorderAroundCode = preferences.getPreference(PreferenceKey.ShowBorderAroundCode);
								String preferenceShowBackgroundForCode = preferences.getPreference(PreferenceKey.ShowBackgroundForCode);

								if (preferenceShowBorderAroundCode.equals("true")) {
									cssClass = cssClass + " segment";
									if (preferenceShowBackgroundForCode.equals("true")) {
										cssClass = cssClass + " yellowcodeMDSE";
									}
								}
								else {
									cssClass = cssClass + " basic segment";
									if (preferenceShowBackgroundForCode.equals("true")) {
										cssClass = cssClass + " yellowcodeMDSE";
									}
									else {
										cssClass = cssClass + " graycodeMDSE";
									}
								}

								attributes.addValue("class", cssClass);

								String preferenceShowPopupForCodeLanguage = preferences.getPreference(PreferenceKey.ShowPopupForCodeLanguage);
								if (preferenceShowPopupForCodeLanguage.equals("true")) {
									String codeLanguage = null;
									if (node instanceof FencedCodeBlock) {
										FencedCodeBlock fencedCodeBlock = (FencedCodeBlock) node;
										if (fencedCodeBlock.getInfo() != null) {
											codeLanguage = fencedCodeBlock.getInfo().toString();	
										}
									}

									if (codeLanguage != null) {
										attributes.addValue("data-tooltip", codeLanguage);
										attributes.addValue("data-position", "top right");
									}
								}
							}
							
							/* Indented and fenced code block */
							if (((node instanceof IndentedCodeBlock) || (node instanceof FencedCodeBlock)) && (part == CoreNodeRenderer.CODE_CONTENT)) {
								
								String cssClass = "";
								Attribute attribute = attributes.get("class");
								if (attribute != null) {
									cssClass = attribute.getValue();
								}
								
								Document document = findDocument(node);
								MarkdownSemanticEPPreferences preferences = documentPreferences.get(document);

								String preferenceShowBorderAroundCode = preferences.getPreference(PreferenceKey.ShowBorderAroundCode);
								String preferenceHaveShowBackgroundForCode = preferences.getPreference(PreferenceKey.ShowBackgroundForCode);

								if (preferenceShowBorderAroundCode.equals("true")) {
									if (preferenceHaveShowBackgroundForCode.equals("true")) {
										cssClass = cssClass + " yellowcodeMDSE";
									}
								}
								else {
									if (preferenceHaveShowBackgroundForCode.equals("true")) {
										cssClass = cssClass + " yellowcodeMDSE";
									}
									else {
										cssClass = cssClass + " graycodeMDSE";
									}
								}

								attributes.addValue("class", cssClass);
							}
							
							/* Link and AutoLink */
							if (((node instanceof Link) || (node instanceof AutoLink)) && (part == AttributablePart.LINK)) {

								Document document = findDocument(node);
								if (documentOtherPreferences.get(document).get("isExport").equals("false")) {
									attributes.addValue("target", "_blank");	
								}
							}
							
							/* Image and ImageRef */
							if (((node instanceof Image) || (node instanceof ImageRef)) && (part == AttributablePart.LINK)) {

								String cssClass = "inlineImageMDSE";

								Document document = findDocument(node);
								MarkdownSemanticEPPreferences preferences = documentPreferences.get(document);
								String preferenceCenterAlignImages = preferences.getPreference(PreferenceKey.CenterAlignImages);
								if (preferenceCenterAlignImages.equals("true")) {
									if ((node.getParent().getFirstChild() == node) && (node.getParent().getLastChild() == node)) {
										cssClass = "centeredImageMDSE";
									}
								}
								
								attributes.addValue("class", cssClass);
							}
						}
					};
				}
			});
		}

		/** No idea */
		@Override
		public void rendererOptions(final MutableDataHolder options) {
//			L.p("rendererOptions = " + options);
			/* Add any configuration settings to options you want to apply to everything, here */
		}
		
		/** Document root */
		private Document findDocument(Node node) {
			
			Node doc = node;
			while (doc.getParent() != null) {
				doc = doc.getParent();
			}
			
			return (Document) doc;
		}
	}
	
	/* Only once */
	static {
		/* Flags of emojis */
		MarkdownSemanticEPEmojis.initFlags();
		
		String indexHtmlResTemplate = R.getTextResourceAsString("html/index.html");
		String semanticCssResTemplate = R.getTextResourceAsString("css/semantic-reduced.min.css");
		indexHtmlTemplate = indexHtmlResTemplate.replace("${SemanticCss}", semanticCssResTemplate);

		markdownSemanticCssResTemplate = R.getTextResourceAsString("css/markdown-semantic-ep.css");
		
/*

| Extension                        | Used | Obs.                                       |
|----------------------------------|-----:|--------------------------------------------|
| flexmark-ext-abbreviation        |  Yes |                                            |
| flexmark-ext-anchorlink          |    - | Makes all heading a link                   |
| flexmark-ext-aside               |  Yes |                                            |
| flexmark-ext-autolink            |  Yes |                                            |
| flexmark-ext-definition          |  Yes |                                            |
| flexmark-ext-emoji               |  Yes | Only for codes, like `:something:`         |
| flexmark-ext-escaped-character   |    - | No effect                                  |
| flexmark-ext-footnotes           |  Yes |                                            |
| flexmark-ext-gfm-strikethrough   |  Yes | Contains also Subscript                    |
| ~~flexmark-ext-gfm-tables~~      |    - | Deprecated                                 |
| flexmark-ext-gfm-tasklist        |    - | Can't be used in tables, not really needed |
| flexmark-ext-ins                 |  Yes |                                            |
| flexmark-ext-jekyll-front-matter |  Yes |                                            |
| flexmark-ext-jekyll-tag          |    - | {% include %} Not working                  |
| flexmark-ext-spec-example        |    - | What is it?                                |
| flexmark-ext-superscript         |  Yes |                                            |
| flexmark-ext-tables              |  Yes |                                            |
| flexmark-ext-toc                 |  Yes |                                            |
| flexmark-ext-typographic         |  Yes | No (c)                                     |
| flexmark-ext-wikilink            |    - | Not used                                   |
| flexmark-ext-xwiki-macros        |    - | { macro } Not used                         |
| flexmark-ext-yaml-front-matter   |    - | Same as jekyll-front-matter ?              |

*/
		
		List<Extension> extensions = Arrays.asList(
				AbbreviationExtension.create(),
				AsideExtension.create(),
				FootnoteExtension.create(),
				AutolinkExtension.create(),
				DefinitionExtension.create(),
				StrikethroughSubscriptExtension.create(),
				InsExtension.create(),
				JekyllFrontMatterExtension.create(),
				SuperscriptExtension.create(),
				TablesExtension.create(),
				TypographicExtension.create(),
				TocExtension.create(),
				EmojiExtension.create(),
				
				MarkdownSemanticEPRenderExtension.create());
		
		parser = Parser.builder().extensions(extensions).build();
		renderer = HtmlRenderer.builder().extensions(extensions).build();
		formatter = Formatter.builder().extensions(extensions).build();
	}
	
	/** Parse */
	public static Node parseMarkdown(String markdownString) {
		
		return parser.parse(markdownString);
	}
	
	/** Build */
	public static String buildIndexHtml(String baseHref, String markdownString, MarkdownSemanticEPPreferences preferences) {

		/* HTML template */
		String markdownHtml = indexHtmlTemplate;

		/* baseHref */
		String baseHrefValue = "";
		if (baseHref != null) {
			baseHrefValue = baseHref;
		}
		
		/* CSS insert */
		markdownHtml = markdownHtml.replace("${MarkdownSemanticEpCss}", buildSpecificCss(preferences));
		
		/* Transform */
		Node document = parseMarkdown(markdownString);
		
		documentPreferences.put(document, preferences);

		HashMap<String, String> otherPreferences = new HashMap<>();
		if (baseHrefValue.equals("")) {
			/* Export */
			otherPreferences.put("isExport", "true");	
		}
		else {
			/* Internal */
			otherPreferences.put("isExport", "false");	
		}
		documentOtherPreferences.put(document, otherPreferences);
		
		String html = renderer.render(document);
		
		/* HTML insert */
		String indexHtml = markdownHtml.replace("${markdownHtml}", html).replace("${baseHref}", baseHrefValue);
		
		return indexHtml;
	}

	/** Create the specific CSS */
	private static String buildSpecificCss(MarkdownSemanticEPPreferences preferences) {
		
		/* Specific for SemanticUI */

		/** The template */
		String markdownSemanticCss = markdownSemanticCssResTemplate;

		/* TextFont */
		String preferenceTextFont = preferences.getPreference(PreferenceKey.TextFont);
		String textfontFamily = getFontFamily(preferenceTextFont, textFonts);
		int textFontSize = getFontSize(preferenceTextFont);

		/* Calibri,"Lucida Grande,FreeSans */
		/* Lato,"Helvetica Neue",Arial,Helvetica,sans-serif; */
		
		markdownSemanticCss = markdownSemanticCss.replace("${TextFontFamily}", textfontFamily);
		markdownSemanticCss = markdownSemanticCss.replace("${TextFontSize}", textFontSize + "px");
		
		/* JustifyTextParagraphs */
		String preferenceJustifyTextParagraphs = preferences.getPreference(PreferenceKey.JustifyTextParagraphs);
		if (preferenceJustifyTextParagraphs.equals("true")) {
			markdownSemanticCss = markdownSemanticCss.replace("${TextAlign}", "justify");
		}
		else {
			markdownSemanticCss = markdownSemanticCss.replace("${TextAlign}", "left");
		}
		
		/* HeadersFont */
		String preferenceHeadersFont = preferences.getPreference(PreferenceKey.HeadersFont);
		String headersfontName = getFontFamily(preferenceHeadersFont, headerFonts);
		int headersFontSize = getFontSize(preferenceHeadersFont);

		markdownSemanticCss = markdownSemanticCss.replace("${HeaderFontFamily}", headersfontName);
		
		markdownSemanticCss = markdownSemanticCss.replace("${Header1FontSize}", headersFontSize * 2 + "px");
		markdownSemanticCss = markdownSemanticCss.replace("${Header2FontSize}", (int)((double)headersFontSize * 1.71428571d) + "px");
		markdownSemanticCss = markdownSemanticCss.replace("${Header3FontSize}", (int)((double)headersFontSize * 1.28571429d) + "px");
		//markdownHtml = markdownHtml.replace("${Header4FontSize}", (int)((double)headersFontSize * 1.07142857d) + "px");
		markdownSemanticCss = markdownSemanticCss.replace("${Header4FontSize}", (int)((double)headersFontSize * 1.09142857d) + "px");
		markdownSemanticCss = markdownSemanticCss.replace("${Header5FontSize}", headersFontSize + "px");
		markdownSemanticCss = markdownSemanticCss.replace("${Header6FontSize}", (int)((double)headersFontSize * 0.92857143d) + "px");

		/* CodeFont */
		String preferenceCodeFont = preferences.getPreference(PreferenceKey.CodeFont);
		String codefontFamily = getFontFamily(preferenceCodeFont, codeFonts);
		int codeFontSize = getFontSize(preferenceCodeFont);

		/* "Monaco","Menlo","Ubuntu Mono","Consolas","source-code-pro",monospace; */
		
		markdownSemanticCss = markdownSemanticCss.replace("${CodeFontFamily}", codefontFamily);
		markdownSemanticCss = markdownSemanticCss.replace("${CodeFontSize}", codeFontSize + "px");
		
		return markdownSemanticCss;
	}
	
	/** Concatenate with defaults */
	private static String getFontFamily(String preferenceFont, String[] fonts) {
		
		String fontFamily = "";
		String sep = "";
		String font = ""; 
		int sepSpace = preferenceFont.lastIndexOf(" ");
		if (sepSpace > -1) {
			font = preferenceFont.substring(0, sepSpace);
			if (font.contains(" ")) {
				fontFamily = "'" + font + "'";
			}
			else {
				fontFamily = font;	
			}
			sep = ", ";
		}
		
		for (String defaultFont : fonts) {
			
			if (!defaultFont.equals(font)) {
				if (defaultFont.contains(" ")) {
					fontFamily = fontFamily + sep + "'" + defaultFont + "'";
				}
				else {
					fontFamily = fontFamily + sep + defaultFont;
				}
				sep = ", ";
			}
		}
		
		return fontFamily;
	}

	/** One size */
	private static Integer getFontSize(String preferenceFont) {
		
		int sepSpace = preferenceFont.lastIndexOf(" ");
		Integer fontSize = null;
		
		try {
			fontSize = Integer.parseInt(preferenceFont.substring(sepSpace + 1));	
		}
		catch (NumberFormatException numberFormatException) {
			L.e("NumberFormatException in getFontSize", numberFormatException);
		}
		
		return fontSize;
	}
	
	/** Format */
	public static String formatMarkdown(String markdownString) {
		
		return formatter.render(parser.parse(markdownString));
	}
}

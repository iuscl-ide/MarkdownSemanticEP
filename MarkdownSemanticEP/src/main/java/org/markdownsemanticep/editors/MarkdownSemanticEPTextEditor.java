/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.editors;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.markdownsemanticep.activator.L;

/** Markdown text editor */
public class MarkdownSemanticEPTextEditor extends TextEditor {

	private MarkdownSemanticEPEditor parentMultiPageEditorPart;
	private MarkdownSemanticEPContentOutlinePage contentOutlinePage;

	private MarkdownSemanticEPSourceViewerConfiguration sourceViewerConfiguration;
	
	/** The actual test editor */
	public MarkdownSemanticEPTextEditor(MarkdownSemanticEPEditor parentMultiPageEditorPart) {
		super();
		
		this.parentMultiPageEditorPart = parentMultiPageEditorPart;
		MarkdownSemanticEPDocumentProvider documentProvider = new MarkdownSemanticEPDocumentProvider();
		setDocumentProvider(documentProvider);
		sourceViewerConfiguration = new MarkdownSemanticEPSourceViewerConfiguration(documentProvider);
		setSourceViewerConfiguration(sourceViewerConfiguration);
	}

	/** Only to have word wrap by default, even if performance crumbles */
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		this.setWordWrap(true);
	}

	/** Outline page is requested */
	@Override
	public <T> T getAdapter(Class<T> adapter) {

		if (IContentOutlinePage.class.equals(adapter)) {
			if (contentOutlinePage == null) {
				contentOutlinePage = new MarkdownSemanticEPContentOutlinePage(getDocumentProvider(), this);
				contentOutlinePage.setInput(getEditorInput());
			}
			return adapter.cast(contentOutlinePage);
		}
		
		return super.getAdapter(adapter);
	}

	/** Status line offset event */
	@Override
	protected void handleCursorPositionChanged() {

		MarkdownSemanticEPEditorContributor.getPositionField().setText(getOffsetStatus());
		super.handleCursorPositionChanged();
	}

	/** For status line offset */
	public String getOffsetStatus() {
		
		ITextSelection textSelection = (ITextSelection) getSelectionProvider().getSelection();
		return "> " + textSelection.getOffset();
	}
	
	/** the parentMultiPageEditorPart */
	public MarkdownSemanticEPEditor getParentMultiPageEditorPart() {
		return parentMultiPageEditorPart;
	}

	/** Repair broken paragraph for editor */
	public void repairBrokenParagraphContributor() {
		
		doParagraphContributor(false);
	}

	/** Repair broken paragraph for editor */
	public void format80ParagraphContributor() {
		
		doParagraphContributor(true);
	}

	/** Paragraph for editor */
	private void doParagraphContributor(boolean format80) {
		
		MarkdownSemanticEPDocument document = (MarkdownSemanticEPDocument) this.getDocumentProvider().getDocument(this.getEditorInput());
		String enter = document.getDefaultLineDelimiter();
		
		TextSelection textSelection = (TextSelection) this.getSelectionProvider().getSelection();
		String selection = textSelection.getText();
		
		String formattedSelection = repairBrokenParagraph(selection, enter);
		
		if (format80) {
			formattedSelection = format80Paragraph(formattedSelection, enter);
		}
		
		try {
			document.replace(textSelection.getOffset(), textSelection.getLength(), formattedSelection);
		}
		catch (BadLocationException badLocationException) {
			L.e("BadLocationException in repairBrokenParagraph", badLocationException);
		}
	}
	
	/** Repair broken paragraph */
	private String repairBrokenParagraph(String text, String enter) {
		
		String one = new Character((char) 1) + "";
		
		/* Double enter */
		text = text.replace(enter + enter, one + one);
		text = text.replace(one + enter, one + one);
		
		/* Sign and enter */
		text = text.replace("\"" + enter, "\"" + one);
		text = text.replace("'" + enter, "'" + one);
		
		text = text.replace("-" + enter, "-" + one);
		text = text.replace("_" + enter, "_" + one);
		
		text = text.replace("." + enter, "." + one);
		text = text.replace("?" + enter, "?" + one);
		text = text.replace("!" + enter, "!" + one);
		
		/* Enter and sign */
		text = text.replace(enter + "1", one + "1");
		text = text.replace(enter + "2", one + "2");
		text = text.replace(enter + "3", one + "3");
		text = text.replace(enter + "4", one + "4");
		text = text.replace(enter + "5", one + "5");
		text = text.replace(enter + "6", one + "6");
		text = text.replace(enter + "7", one + "7");
		text = text.replace(enter + "8", one + "8");
		text = text.replace(enter + "9", one + "9");
		text = text.replace(enter + "0", one + "0");
		
		text = text.replace(enter + "\"", one + "\"");
		text = text.replace(enter + "'", one + "'");
		text = text.replace(enter + "-", one + "-");
		text = text.replace(enter + "_", one + "_");

		if (enter.endsWith("\n")) {
			text = text.replaceAll("\n[\\s]*", "\n");	
		}
		else {
			text = text.replaceAll("\r[\\s]*", "\r");
		}
		
		/* Main replace */
		text = text.replace(enter, ' ' + "");
		text = text.replace(one, enter);

		return text;
	}

	/** Repair broken paragraph */
	private String format80Paragraph(String text, String enter) {
		
		Character one = new Character((char) 1);
		text = text.replace(enter, one + "");
		StringBuffer stringBuffer = new StringBuffer();
		
		char[] chars = text.toCharArray();
		boolean endOfString = false;
		int start = 0;
		int end = start;
		int charsWidth = 80;
				
		while (start < chars.length - 1) {
		    int charCount = 0;
		    int lastSpace = 0;

		    while (charCount < charsWidth) {

		    	if (chars[charCount + start] == one) {
		        	lastSpace = charCount;
		            break;
		        }
		        
		    	if (chars[charCount + start] == ' ') {
		            lastSpace = charCount;
		        }
		        charCount++;
		        
		        if (charCount + start == text.length()) {
		            endOfString = true;
		            break;
		        }
		    }
		    
		    if (endOfString) {
		    	end = text.length();
		    }
		    else {
		    	
		    	if (lastSpace > 0) {
		    		end = lastSpace + start;
		    	}
		    	else {
		    		end = charCount + start;
		    	}
		    }
		    
		    stringBuffer.append(text.substring(start, end) + enter);
		    start = end;
		    
		    if (end < chars.length) {
		    	if ((chars[end] == ' ') || (chars[end] == one)) {
		    		start = end + 1;
		    	}
		    }
		}

		return stringBuffer.toString();
	}

}

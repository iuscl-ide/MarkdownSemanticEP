/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.StatusLineContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.markdownsemanticep.activator.L;
import org.markdownsemanticep.activator.R;
import org.markdownsemanticep.engine.MarkdownSemanticEPEngine;

/** Global actions for multi-page editor */
public class MarkdownSemanticEPEditorContributor extends MultiPageEditorActionBarContributor {

	/** BrowserViewer page contributor */
	private class BrowserViewerPageActionContributor extends EditorActionBarContributor {
		/** Add to status line */
		@Override
		public void contributeToStatusLine(IStatusLineManager statusLineManager) {
		    statusLineManager.add(linkField);
			super.contributeToStatusLine(statusLineManager);
		}
	}
	
	/** TextEditor page contributor */
	private class TextEditorPageActionContributor extends TextEditorActionContributor {
		/** Add to menu */		
		@Override
		public void contributeToMenu(IMenuManager menuManager) {
//			super.contributeToMenu(menuManager);
			if (mdMenuManager != null) {
				mdMenuManager.add(showWordWrapAction);
				mdMenuManager.add(formatMdAction);
				mdMenuManager.add(format80ColumnsAction);
				mdMenuManager.add(repairPragraphAction);
			}
		}
		/** Add to tool bar */
		@Override
		public void contributeToToolBar(IToolBarManager toolBarManager) {
//			super.contributeToToolBar(toolBarManager);
			if (mdToolBarManager != null) {
				mdToolBarManager.add(showWordWrapAction);
				mdToolBarManager.add(formatMdAction);
				mdToolBarManager.add(format80ColumnsAction);
				mdToolBarManager.add(repairPragraphAction);
			}
		}
		/** Add to status line */
		@Override
		public void contributeToStatusLine(IStatusLineManager statusLineManager) {
//			statusLineManager.add(spellCheckStatusField);
			super.contributeToStatusLine(statusLineManager);
			statusLineManager.add(positionField);
		}
	}

	/** The actual editor */
	private MarkdownSemanticEPEditor multiPageEditor;
	
	/** Contributed menu */
	private MenuManager mdMenuManager;
	/** Contributed tool bar */
	private ToolBarManager mdToolBarManager;
	
	/** Export as one HTML file */
	private Action exportAsHtmlAction;
	private String exportAsHtmlActionId = "6104584428303702872L";
	
	/** Format Markdown text */
	private Action formatMdAction;
	private String formatMdActionId = "2742120702435930150L";

	/** Show word wrap */
	private Action showWordWrapAction;
	private String showWordWrapActionId = "5619259105440021229L";

	/** Format text to 80 columns */
	private Action format80ColumnsAction;
	private String format80ColumnsActionId = "4674461198291535316L";

	/** Repair broken paragraph */
	private Action repairPragraphAction;
	private String repairPragraphActionId = "3896685104007020728L";

	/** Browser hover link */
	private static StatusLineContributionItem linkField;
	/** Text editor offset */
	private static StatusLineContributionItem positionField;
	/** Text editor spell check status */
//	private static StatusLineContributionItem spellCheckStatusField;

	/** First page */
	private BrowserViewerPageActionContributor browserViewerPageActionContributor;
	/** Second page */
	private TextEditorPageActionContributor textEditorPageActionContributor;
	
	/** Creates a multi-page contributor */
	public MarkdownSemanticEPEditorContributor() {
		super();

		createActions();
		browserViewerPageActionContributor = new BrowserViewerPageActionContributor();
		textEditorPageActionContributor = new TextEditorPageActionContributor();
		
		linkField = new StatusLineContributionItem("linkField", 120);
		linkField.setText("");

		positionField = new StatusLineContributionItem("positionField", 16);
		positionField.setText("0");

//		spellCheckStatusField = new StatusLineContributionItem("spellCheckStatusField", 50);
//		spellCheckStatusField.setText("s");
	}

	/** The bars are available here */
	@Override
	public void init(IActionBars bars) {

		browserViewerPageActionContributor.init(bars);
		textEditorPageActionContributor.init(bars);
		super.init(bars);
	}

	/** Returns the action registered with the given text editor */
	protected IAction getAction(ITextEditor editor, String actionID) {
		return (editor == null ? null : editor.getAction(actionID));
	}

	/** The editor class is available here */
	@Override
	public void setActiveEditor(IEditorPart part) {

		if (part != null) {
			multiPageEditor = (MarkdownSemanticEPEditor) part;
			MarkdownSemanticEPTextEditor markdownSemanticEPTextEditor = (MarkdownSemanticEPTextEditor) multiPageEditor.getEditor();
			textEditorPageActionContributor.setActiveEditor(markdownSemanticEPTextEditor);
			positionField.setText(markdownSemanticEPTextEditor.getOffsetStatus());
			
			showWordWrapAction.setChecked(markdownSemanticEPTextEditor.isWordWrapEnabled());
			
			super.setActiveEditor(multiPageEditor);
		}
	}

	/** When page change, change the status bar */
	public void setActivePage(IEditorPart activeEditor) {

		IActionBars bars = getActionBars();
		if ((multiPageEditor != null) && (bars != null)) {

			/* Menu */
//			bars.getMenuManager().removeAll();
			mdMenuManager.remove(showWordWrapActionId);
			mdMenuManager.remove(formatMdActionId);
			mdMenuManager.remove(format80ColumnsActionId);
			mdMenuManager.remove(repairPragraphActionId);

			/* Tool bar */
//			bars.getToolBarManager().removeAll();
			mdToolBarManager.remove(showWordWrapActionId);
			mdToolBarManager.remove(formatMdActionId);
			mdToolBarManager.remove(format80ColumnsActionId);
			mdToolBarManager.remove(repairPragraphActionId);
			
			/* Status line */
			bars.getStatusLineManager().removeAll();
			
			if (multiPageEditor.getActivePage() == multiPageEditor.getBrowserViewerPageIndex()) {
				browserViewerPageActionContributor.contributeToStatusLine(bars.getStatusLineManager());
			}
			if (multiPageEditor.getActivePage() == multiPageEditor.getTextEditorPageIndex()) {
				textEditorPageActionContributor.contributeToMenu(bars.getMenuManager());
				textEditorPageActionContributor.contributeToToolBar(bars.getToolBarManager());
				textEditorPageActionContributor.contributeToStatusLine(bars.getStatusLineManager());
			}

			/* Update */
			mdToolBarManager.update(true);
			bars.updateActionBars();
		}
	}
	
	/** Eclipse actions */
	private void createActions() {
		
		/* Export Markdown Document as HTML File */
		exportAsHtmlAction = new Action() {
			public void run() {
				multiPageEditor.exportAsHtml();
			}
		};
		
		exportAsHtmlAction.setId(exportAsHtmlActionId);
		exportAsHtmlAction.setText("Export Markdown as HTML file...");
		exportAsHtmlAction.setToolTipText("Export Markdown Document as HTML File");
		exportAsHtmlAction.setImageDescriptor(R.getImageDescriptor("md-action-exportashtml"));

		/* Format Selected Markdown Source Text */
		formatMdAction = new Action() {
			public void run() {
				
				MarkdownSemanticEPTextEditor markdownSemanticEPTextEditor = (MarkdownSemanticEPTextEditor) multiPageEditor.getEditor();
				
				TextSelection textSelection = (TextSelection) markdownSemanticEPTextEditor.getSelectionProvider().getSelection();
				String selection = textSelection.getText();
//				L.p(selection);
				String formattedSelection = MarkdownSemanticEPEngine.formatMarkdown(selection);
//				L.p(formattedSelection);
				IDocument document = markdownSemanticEPTextEditor.getDocumentProvider().getDocument(markdownSemanticEPTextEditor.getEditorInput());
				try {
					document.replace(textSelection.getOffset(), textSelection.getLength(), formattedSelection);
				}
				catch (BadLocationException badLocationException) {
					L.e("BadLocationException in formatMdAction", badLocationException);
				}
			}
		};
		
		formatMdAction.setId(formatMdActionId);
		formatMdAction.setText("Format Markdown Source");
		formatMdAction.setToolTipText("Format Selected Markdown Source Text");
		formatMdAction.setImageDescriptor(R.getImageDescriptor("md-action-format-md"));
		
		/** Show word wrap */
		showWordWrapAction = new Action() {
			public void run() {
				MarkdownSemanticEPTextEditor markdownSemanticEPTextEditor = (MarkdownSemanticEPTextEditor) multiPageEditor.getEditor();
				boolean wordWrapEnabled = markdownSemanticEPTextEditor.isWordWrapEnabled();
				wordWrapEnabled = !wordWrapEnabled;
				markdownSemanticEPTextEditor.setWordWrap(wordWrapEnabled);
			}
		};
		showWordWrapAction.setChecked(false);
		showWordWrapAction.setId(showWordWrapActionId);
		showWordWrapAction.setText("Show Text Word Wrap");
		showWordWrapAction.setToolTipText("Show Text Word Wrap");
		showWordWrapAction.setImageDescriptor(R.getImageDescriptor("md-action-word-wrap"));

		/** Format text to 80 columns */
		format80ColumnsAction = new Action() {
			public void run() {
				MarkdownSemanticEPTextEditor markdownSemanticEPTextEditor = (MarkdownSemanticEPTextEditor) multiPageEditor.getEditor();
				markdownSemanticEPTextEditor.format80ParagraphContributor();
			}
		};
		format80ColumnsAction.setId(format80ColumnsActionId);
		format80ColumnsAction.setText("Format to 80 Columns");
		format80ColumnsAction.setToolTipText("Format Selected Text to 80 Columns");
		format80ColumnsAction.setImageDescriptor(R.getImageDescriptor("md-action-create-80"));

		/** Repair broken paragraph */
		repairPragraphAction = new Action() {
			public void run() {
				MarkdownSemanticEPTextEditor markdownSemanticEPTextEditor = (MarkdownSemanticEPTextEditor) multiPageEditor.getEditor();
				markdownSemanticEPTextEditor.repairBrokenParagraphContributor();
			}
		};
		repairPragraphAction.setId(repairPragraphActionId);
		repairPragraphAction.setText("Repair Broken Paragraphs");
		repairPragraphAction.setToolTipText("Repair Broken Selected Text Paragraphs");
		repairPragraphAction.setImageDescriptor(R.getImageDescriptor("md-action-repair-paragraph"));
		
	}

	/** Initial, fix contribution */
	public void contributeToMenu(IMenuManager menuManager) {
		mdMenuManager = new MenuManager("Markdown Semantic");
		menuManager.prependToGroup(IWorkbenchActionConstants.MB_ADDITIONS, mdMenuManager);
		mdMenuManager.add(exportAsHtmlAction);
		mdMenuManager.add(new Separator());
	}

	/** Initial, fix contribution */
	public void contributeToToolBar(IToolBarManager toolBarManager) {
		mdToolBarManager = (ToolBarManager) toolBarManager;
		mdToolBarManager.add(exportAsHtmlAction);
		mdToolBarManager.add(new Separator());
	}

	/** the linkField */
	public static StatusLineContributionItem getLinkField() {
		return linkField;
	}

	/** the positionField */
	public static StatusLineContributionItem getPositionField() {
		return positionField;
	}
	
}

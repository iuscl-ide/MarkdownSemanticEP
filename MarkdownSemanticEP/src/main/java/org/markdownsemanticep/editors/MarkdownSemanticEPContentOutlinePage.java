/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.editors;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

/** The outline view with its tree view */
public class MarkdownSemanticEPContentOutlinePage extends ContentOutlinePage {

	/** Tree labels */
	public class OutlineLabelProvider extends LabelProvider {

		public String getText(Object element) {
			return ((MarkdownSemanticEPOutlineNode)element).getLabel();
		}

		public Image getImage(Object element) {
			return ((MarkdownSemanticEPOutlineNode)element).getImage();
		}
	}

	/** Tree nodes */
	public class OutlineContentProvider implements ITreeContentProvider {

		public Object[] getChildren(Object parentElement) {
			return ((MarkdownSemanticEPOutlineNode)parentElement).findChildOutlineNodes();
		}

		public Object getParent(Object element) {
			return ((MarkdownSemanticEPOutlineNode)element).getParentOutlineNode();
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
			/* */
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			/* Change MarkdownSemanticEPMdEditor */
		}		
	}
	
	/** Document provider */
	private IDocumentProvider documentProvider;
	private IEditorInput editorInput;
	private MarkdownSemanticEPTextEditor mdEditor;
	
	/** The view */
	public MarkdownSemanticEPContentOutlinePage(IDocumentProvider documentProvider, MarkdownSemanticEPTextEditor mdEditor) {
		super();
		
		this.documentProvider = documentProvider;
		this.mdEditor = mdEditor;
	}

	/** Customize the content */
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		setUpOutline();
		
		TreeViewer tree = getTreeViewer();
		tree.setAutoExpandLevel(7);
		tree.setContentProvider(new OutlineContentProvider());
		tree.setLabelProvider(new OutlineLabelProvider());
		tree.setInput(getDocument().getOutline());
		tree.addSelectionChangedListener(new ISelectionChangedListener() {
			
			public void selectionChanged(SelectionChangedEvent event) {
				
				Object selected = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (null == selected) {
					return;
				}
				MarkdownSemanticEPOutlineNode selectedOutlineNode = (MarkdownSemanticEPOutlineNode)selected;
				mdEditor.getParentMultiPageEditorPart().activateTextEditorPage();
				mdEditor.selectAndReveal(selectedOutlineNode.getStart(), selectedOutlineNode.getLength());
			}
		});
	}
	
	/** When modified */
	private void setUpOutline() {
		final MarkdownSemanticEPDocument document = getDocument();
		
		document.setOutlineListener(new MarkdownSemanticEPDocument.MarkdownSemanticEPOutlineListener() {
			
			public void outlineChanged(MarkdownSemanticEPOutlineNode node) {
				
				final TreeViewer tree = getTreeViewer();
				if (!tree.getControl().isDisposed()) {
					tree.getControl().getDisplay().asyncExec(new Runnable() {
						
						public void run() {
							
							tree.setInput(document.getOutline());
						};
					});
				}
			}
		});
	}
	
	/** Actual document */
	private MarkdownSemanticEPDocument getDocument() {
		return (MarkdownSemanticEPDocument) documentProvider.getDocument(editorInput);
	}
	
	/** The input */
	public void setInput(IEditorInput editorInput) {
		this.editorInput = editorInput;
	}
}

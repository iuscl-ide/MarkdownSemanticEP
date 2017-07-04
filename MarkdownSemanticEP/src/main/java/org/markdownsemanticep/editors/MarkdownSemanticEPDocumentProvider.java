/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.editors.text.FileDocumentProvider;

/** For colors */
public class MarkdownSemanticEPDocumentProvider extends FileDocumentProvider {

	private MarkdownSemanticEPDocumentPartitioner documentPartitioner;
	
	/** Partitions */
	protected IDocument createDocument(Object element) throws CoreException {
		
		IDocument document = super.createDocument(element);
//		L.p("MarkdownSemanticEPDocumentProvider");
		if (document != null) {
			documentPartitioner = new MarkdownSemanticEPDocumentPartitioner(); 
			documentPartitioner.connect(document);
			document.setDocumentPartitioner(documentPartitioner);
		}
		return document;
	}
	
	/** Empty */
	protected IDocument createEmptyDocument() {
		return new MarkdownSemanticEPDocument();
	}
	
	public void setStyledText(StyledText styledText) {
		documentPartitioner.setStyledText(styledText);
	}
}

/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.editors;

import org.eclipse.jface.text.Document;

/** The outline root node */
public class MarkdownSemanticEPDocument extends Document {

	/** To change the outline */
	public interface MarkdownSemanticEPOutlineListener {
		
		void outlineChanged(MarkdownSemanticEPOutlineNode newOutlineNode);
	}

	private MarkdownSemanticEPOutlineNode rootOutlineNode;
	private MarkdownSemanticEPOutlineListener outlineListener;
	
	/** Create the initial empty document node */
	public MarkdownSemanticEPDocument() {

		rootOutlineNode = new MarkdownSemanticEPOutlineNode();
	}
	
	/** The root outline node */
	public MarkdownSemanticEPOutlineNode getOutline() {
		return rootOutlineNode;
	}

	/** Recalculate the whole outline */
	public void updateOutline(MarkdownSemanticEPOutlineNode outlineNode) {
		this.rootOutlineNode = outlineNode;
		if (null != outlineListener) {
			outlineListener.outlineChanged(outlineNode);
		}
	}

	/** Glue the listener to the interface */
	public void setOutlineListener(MarkdownSemanticEPOutlineListener outlineListener) {
		this.outlineListener = outlineListener;
	}
}

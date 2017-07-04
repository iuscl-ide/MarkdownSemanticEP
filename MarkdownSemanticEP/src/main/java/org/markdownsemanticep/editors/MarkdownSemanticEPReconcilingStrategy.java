/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.editors;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.markdownsemanticep.activator.L;
import org.markdownsemanticep.activator.R;
import org.markdownsemanticep.engine.MarkdownSemanticEPEngine;

import com.vladsch.flexmark.ast.Heading;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ast.NodeVisitor;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.ast.VisitHandler;
import com.vladsch.flexmark.ast.Visitor;

/** For outline */
public class MarkdownSemanticEPReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {
	private final ArrayList<Heading> linearHeadings = new ArrayList<>();
	private StringBuilder headingStringBuilder;
	private MarkdownSemanticEPDocument document;

	/** Linearize the headings */
	private NodeVisitor headingsNodeVisitor = new NodeVisitor(
			new VisitHandler<Heading>(Heading.class, new Visitor<Heading>() {
				@Override
				public void visit(Heading heading) {
					linearHeadings.add(heading);
					headingsNodeVisitor.visitChildren(heading);
				}
			}));

	/** Concatenate heading label */
	private NodeVisitor headingTextsNodeVisitor = new NodeVisitor(new VisitHandler<Text>(Text.class, new Visitor<Text>() {
		@Override
		public void visit(Text text) {
			String textString = text.getChars().toString().trim();
			//L.p("->" + textString + "<-");
			String separator = "";
			if (headingStringBuilder.length() > 0) {
				separator = " ";
			}
			headingStringBuilder.append(separator + textString);
			headingTextsNodeVisitor.visitChildren(text);
		}
	}));

	/** First reconcile */
	@Override
	public void initialReconcile() {
//		L.p("initial reconcile");
		updateDocumentOutline();
	}

	/** Not for now */
	@Override
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
		/* We are fast */
	}

	/** Reconcile here */
	@Override
	public void reconcile(IRegion partition) {
		updateDocumentOutline();
	}

	/** Not clear */
	@Override
	public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
		L.p("dirty reconcile");
		/* don't know */
	}

	/** To store the document */
	@Override
	public void setDocument(IDocument document) {
		this.document = (MarkdownSemanticEPDocument) document;
	}

	/** Actual outline reconcile */
	private void updateDocumentOutline() {
		
		/* View, outside of tree */
		MarkdownSemanticEPOutlineNode viewOutlineNode = new MarkdownSemanticEPOutlineNode();

		ArrayList<MarkdownSemanticEPOutlineNode> createdOutlineNodes = new ArrayList<>();

		/* Parse document in a node */
		Node documentMdNode = MarkdownSemanticEPEngine.parseMarkdown(document.get());
		MarkdownSemanticEPOutlineNode documentOutlineNode = viewOutlineNode.addChildFromMdNode(documentMdNode);
		documentOutlineNode.setStart(0);
		documentOutlineNode.setLength(0);
		documentOutlineNode.setImage(R.getImage("md-file-toolbar-nottext"));
		documentOutlineNode.setLabel("Document");
		documentOutlineNode.setType("Document");
		documentOutlineNode.setSubType("0");
		createdOutlineNodes.add(documentOutlineNode);
		
		linearHeadings.clear();
		headingsNodeVisitor.visitChildren(documentMdNode);

		/* Put headings in a hierarchical tree */
		for (Heading mdHeading : linearHeadings) {

			int parentIndex = createdOutlineNodes.size() - 1;
			MarkdownSemanticEPOutlineNode parentOutlineNode = createdOutlineNodes.get(parentIndex);
			int mdHeadingLevel = mdHeading.getLevel();
			while (Integer.parseInt(parentOutlineNode.getSubType()) >= mdHeadingLevel) {
				parentIndex--;
				parentOutlineNode = createdOutlineNodes.get(parentIndex);
			}
			
			MarkdownSemanticEPOutlineNode headingOutlineNode = parentOutlineNode.addChildFromMdNode(mdHeading);
			String level = "" + mdHeadingLevel;
			headingOutlineNode.setType("Heading");
			headingOutlineNode.setSubType(level);
			headingOutlineNode.setImage(R.getImage("md-heading-" + level));
			headingStringBuilder = new StringBuilder();
			headingTextsNodeVisitor.visitChildren(mdHeading);
			headingOutlineNode.setLabel(headingStringBuilder.toString());
			int offset = mdHeading.getStartOffset();
			headingOutlineNode.setStart(offset);
			headingOutlineNode.setLength(mdHeading.getEndOffset() - offset);
			createdOutlineNodes.add(headingOutlineNode);
		}
		
		document.updateOutline(viewOutlineNode);
	}
}

/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.editors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

/** Markdown editor source viewer */
public class MarkdownSemanticEPSourceViewerConfiguration extends TextSourceViewerConfiguration {

	
	/** One per region type */
	private class MarkdownSemanticEPDamagerRepairer implements IPresentationDamager, IPresentationRepairer {

		/** The document */
//	    private MarkdownSemanticEPDocument document;

		/** Create the style ranges that will be styled */
		@Override
		public void createPresentation(TextPresentation textPresentation, ITypedRegion typedRegion) {
//			L.p("createPresentation " + typedRegion.getOffset() + " -> " + typedRegion.getLength() + " : " + typedRegion.getType());
			
			TextAttribute textAttribute = MarkdownSemanticEPDocumentPartitioner.findTextAttribute(typedRegion.getType());
			int style = textAttribute.getStyle(); 
			int fontStyle = style & (SWT.ITALIC | SWT.BOLD | SWT.NORMAL);
			
			StyleRange styleRange = new StyleRange(typedRegion.getOffset(), typedRegion.getLength(),
					textAttribute.getForeground(), textAttribute.getBackground(), fontStyle);

			styleRange.strikeout = (style & TextAttribute.STRIKETHROUGH) != 0;
			styleRange.underline = (style & TextAttribute.UNDERLINE) != 0;
			
			textPresentation.addStyleRange(styleRange);
		}

	    /** To know the document; why called so many times? */
		@Override
		public void setDocument(IDocument document) {
			//L.p("setDocument");
//			this.document = (MarkdownSemanticEPDocument) document; 
		}

		/** This will go into MarkdownSemanticEPDocumentPartitioner computePartitioning */
		@Override
		public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent documentEvent, boolean documentPartitioningChanged) {
//			L.p("getDamageRegion " + partition + ", event: " + documentEvent.getLength());
			return partition;
		}
	}

	
	private MarkdownSemanticEPDocumentProvider documentProvider;
	
	public MarkdownSemanticEPSourceViewerConfiguration(MarkdownSemanticEPDocumentProvider documentProvider) {
		super();
		this.documentProvider = documentProvider;
	}

	/** Only for reconcile strategy */
	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		
		MarkdownSemanticEPReconcilingStrategy reconcilingStrategy = new MarkdownSemanticEPReconcilingStrategy(); 
		MonoReconciler monoReconciler = new MonoReconciler(reconcilingStrategy, false);
		monoReconciler.setProgressMonitor(new NullProgressMonitor()); 
		monoReconciler.setDelay(500); 

		return monoReconciler; 
	}

//	/** Partition types */
//	@Override
//	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
//		L.p("getConfiguredContentTypes");
//		return new String[] { "__md_content_type" };
////		return MarkdownSemanticEPDocumentPartitioner.getPartitionTypes();
//	}

	/** Link attributes */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {

		documentProvider.setStyledText(sourceViewer.getTextWidget());
		
		PresentationReconciler presentationReconciler = new PresentationReconciler();
		MarkdownSemanticEPDamagerRepairer damagerRepairer = new MarkdownSemanticEPDamagerRepairer();
		
		for (String partitionType : MarkdownSemanticEPDocumentPartitioner.getPartitionTypes()) {
			presentationReconciler.setDamager(damagerRepairer, partitionType);
			presentationReconciler.setRepairer(damagerRepairer, partitionType);
		}
		
		return presentationReconciler;
	}

	/** No idea */
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, true);
			}
		};
	}
}

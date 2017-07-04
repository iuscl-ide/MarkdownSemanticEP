/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.editors;

import java.util.ArrayList;

import org.eclipse.swt.graphics.Image;

import com.vladsch.flexmark.ast.Node;

/** The data for outline tree view nodes */
public class MarkdownSemanticEPOutlineNode {
	
	public static final MarkdownSemanticEPOutlineNode[] NO_CHILDREN = new MarkdownSemanticEPOutlineNode[0];
	
	private String label;
	private Image image;

	private MarkdownSemanticEPOutlineNode parentOutlineNode;
	private ArrayList<MarkdownSemanticEPOutlineNode> childOutlineNodes;
	
	private String type;
	private String subType;

	private int start = 0;
	private int length = 0;
	
	/** To create the document */
	public MarkdownSemanticEPOutlineNode() {
		/* */
	}	
	
	/** Internal for child */
	private MarkdownSemanticEPOutlineNode(MarkdownSemanticEPOutlineNode parentOutlineNode) {
		this.parentOutlineNode = parentOutlineNode;
	}
	
	/** To create from parser */
	public MarkdownSemanticEPOutlineNode addChildFromMdNode(Node mdNode) {

		MarkdownSemanticEPOutlineNode child = new MarkdownSemanticEPOutlineNode(this);
		if (null == childOutlineNodes) {
			childOutlineNodes = new ArrayList<>();
		}
		childOutlineNodes.add(child);
		
		return child;
	}
	
	/** Children, as array */
	public MarkdownSemanticEPOutlineNode[] findChildOutlineNodes() {
		
		if (null == childOutlineNodes) {
			return NO_CHILDREN;
		}
		return (MarkdownSemanticEPOutlineNode[]) childOutlineNodes.toArray(new MarkdownSemanticEPOutlineNode[childOutlineNodes.size()]);
	}

	/** the label */
	public String getLabel() {
		return label;
	}

	/** label the label to set */
	public void setLabel(String label) {
		this.label = label;
	}

	/** the image */
	public Image getImage() {
		return image;
	}

	/** image the image to set */
	public void setImage(Image image) {
		this.image = image;
	}

	/** the type */
	public String getType() {
		return type;
	}

	/** type the type to set */
	public void setType(String type) {
		this.type = type;
	}

	/** the subType */
	public String getSubType() {
		return subType;
	}

	/** subType the subType to set */
	public void setSubType(String subType) {
		this.subType = subType;
	}

	/** the parentOutlineNode */
	public MarkdownSemanticEPOutlineNode getParentOutlineNode() {
		return parentOutlineNode;
	}

	/** the start */
	public int getStart() {
		return start;
	}

	/** start the start to set */
	public void setStart(int start) {
		this.start = start;
	}

	/** the length */
	public int getLength() {
		return length;
	}

	/** length the length to set */
	public void setLength(int length) {
		this.length = length;
	}
	
	
}

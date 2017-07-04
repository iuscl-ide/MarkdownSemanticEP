/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/** The root page, empty */
public class MarkdownSemanticEPRootPreferencesPage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public MarkdownSemanticEPRootPreferencesPage() {
		super(GRID);
		setDescription("Expand the tree to edit preferences for a specific feature.");
	}
	
	/** Nothing, because root */
	public void createFieldEditors() {
		/* */
	}

	/** */
	public void init(IWorkbench workbench) {
		/* */
	}
}
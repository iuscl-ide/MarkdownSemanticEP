/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.preferences;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.markdownsemanticep.activator.MarkdownSemanticEPActivator;
import org.markdownsemanticep.preferences.MarkdownSemanticEPPreferences.PreferenceKey;

/** The global preferences for markdown display */
 public class MarkdownSemanticEPDisplayPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage {

	 /** It will be used also for local properties */
	 private MarkdownSemanticEPDisplayPreferencesUI preferencesUI;

	 private MarkdownSemanticEPPreferences globalPreferences = MarkdownSemanticEPPreferences.getGlobalPreferences();

	 /** Constructor, each time ? */
	 public MarkdownSemanticEPDisplayPreferencesPage() {
		super();
		setPreferenceStore(MarkdownSemanticEPActivator.getDefault().getPreferenceStore());
		setDescription("These global preferences will be the base for every document rendering");
		
		preferencesUI = new MarkdownSemanticEPDisplayPreferencesUI(globalPreferences);
	}
	
	/** Each time it shows */
	@Override
	protected Control createContents(Composite parentComposite) {

		GridLayout parentCompositeLayout = new GridLayout();
		parentCompositeLayout.marginWidth = 0;
		parentCompositeLayout.horizontalSpacing = 0;
		parentCompositeLayout.marginLeft = 0;
		parentCompositeLayout.marginRight = 0;
		parentComposite.setLayout(parentCompositeLayout);

		Composite baseComposite = new Composite(parentComposite, SWT.NONE);
		baseComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		baseComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));

		preferencesUI.createPreferencesPanel(baseComposite);
		//preferencesUI.loadProperties();
		return null;
	}

	/** Intentionally left blank */
	public void init(IWorkbench workbench) {
		/* Intentionally left blank */
		
		MarkdownSemanticEPActivator.getDefault().getPreferenceStore();
		
	}

	/** Save the global */
	private void saveGlobalProperties() {
		
		IPreferenceStore preferenceStore = MarkdownSemanticEPActivator.getDefault().getPreferenceStore();
		
		for (PreferenceKey preferenceKey : PreferenceKey.values()) {
			preferenceStore.setValue(preferenceKey.name(), globalPreferences.getPreference(preferenceKey));
		}
	}
	
	/** Load defaults */
	@Override
	protected void performDefaults() {
		
		preferencesUI.loadDefaultPreferences();
	}

	/** Save */
	@Override
	protected void performApply() {
		
		preferencesUI.savePreferences();
		/* Save the global */
		saveGlobalProperties();
	}

	/** Save before close */
	@Override
	public boolean performOk() {

		preferencesUI.savePreferences();
		/* Save the global */
		saveGlobalProperties();

		return super.performOk();
	}

	/** Can leave */
	@Override
	public boolean performCancel() {
		
		if (preferencesUI.hasUnsavedPreferences()) {
			boolean confirmResult = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Markdown Semantic Preferences",
					"There are unsaved preferences, please confirm that you want to quit");
			if (!confirmResult) {
				return false;
			}
		}
		
		return true;
	}
	
}
 
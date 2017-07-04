/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.editors;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.markdownsemanticep.activator.F;
import org.markdownsemanticep.activator.R;
import org.markdownsemanticep.engine.MarkdownSemanticEPEngine;
import org.markdownsemanticep.preferences.MarkdownSemanticEPDisplayPreferencesUI;
import org.markdownsemanticep.preferences.MarkdownSemanticEPPreferences;
import org.markdownsemanticep.preferences.MarkdownSemanticEPPreferences.PreferenceKey;

/** Creates the multi-page editor */
public class MarkdownSemanticEPEditor extends MultiPageEditorPart implements IResourceChangeListener {

	/** The browser will display the html markdown */
	private Browser viewerBrowser;
	
	private int browserViewerPageIndex;

	/** The Markdown editor */
	private MarkdownSemanticEPTextEditor textEditor;
	private IEditorPart textEditorPart;
	
	private int textEditorPageIndex;

	/** The preferences */
	private MarkdownSemanticEPPreferences preferences;
	private MarkdownSemanticEPPreferences workPreferences;
	private MarkdownSemanticEPDisplayPreferencesUI preferencesUI;

	private int preferencesPageIndex;
	
	/** Build engine */
//	private MarkdownSemanticEPEngine engine;
	
	/** Creates the multi-page editor */
	public MarkdownSemanticEPEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		//this.setTitleImage(R.getImage("md-file-toolbar-nottext"));
	}

	/** Get the Markdown as text */
	public String getMarkdownText() {
		
		return textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).get();
	}
	
	private String findLocalBaseHref() {
		
		IFile iFile = (IFile) textEditor.getEditorInput().getAdapter(IFile.class);
		IPath filePathAndName = iFile.getLocation();
		File file = filePathAndName.toFile();
		String rootPath = file.getPath().substring(0, file.getPath().length() - file.getName().length());
		rootPath = rootPath.replace("\\", "/");
		
		return "<base href=\"file:///" + rootPath + "\">";
	}
	
	/** Put in browser what is in editor */
	public void refresh() {
		
		
		String indexHtml = MarkdownSemanticEPEngine.buildIndexHtml(findLocalBaseHref(), getMarkdownText(), workPreferences);
		viewerBrowser.setText(indexHtml);
	}

	/** Export as HTML */
	public void exportAsHtml() {

		String mdFileName = this.getTitle();
		String name = mdFileName.substring(0, mdFileName.length() - 3);
		
		FileDialog saveFileDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
		saveFileDialog.setOverwrite(false);
		saveFileDialog.setFilterNames(new String[] { "HTML (*.html)", "All files (*.*)" });
		saveFileDialog.setFilterExtensions(new String[] { "*.html", "*.*" });
		saveFileDialog.setText("Export MD file \"" + mdFileName + "\" as HTML");
		saveFileDialog.setFileName(name + ".html");
		saveFileDialog.setFilterIndex(0);
		
		String saveFileDialogResult = saveFileDialog.open();
		if (saveFileDialogResult != null) {
			String indexHtml = MarkdownSemanticEPEngine.buildIndexHtml(null, getMarkdownText(), workPreferences);
			
			InputStream inputStream = new ByteArrayInputStream(indexHtml.getBytes(StandardCharsets.UTF_8));
			F.saveInputStreamInFile(inputStream, new File(saveFileDialogResult));
			
			MessageDialog dialog = new MessageDialog(null, "Markdown Semantic", R.getImage("md-file-toolbar-nottext"),
					"The MD file \"" + mdFileName + "\" was successfully exported as HTML file \"" + saveFileDialogResult + "\"", MessageDialog.INFORMATION,
	                new String[] { IDialogConstants.OK_LABEL }, 0);
	        /* OK is the default */
	        dialog.open();
		}
	}
	
//	/** Parse the input */
//	public Node getParsed() {
//
//		return engine.parseMarkdown(getMarkdownText());
//	}

	/** Creates the viewer page of the multi-page editor */
	private void createBrowserViewerPage() {
		
		Composite viewerPageComposite = new Composite(getContainer(), SWT.NONE);
	    
	    GridLayout viewerPageGridLayout = new GridLayout();
	    viewerPageGridLayout.marginWidth = 1;
	    viewerPageGridLayout.marginHeight = 1;
	    viewerPageGridLayout.verticalSpacing = 0;
	    viewerPageGridLayout.horizontalSpacing = 0;
	    viewerPageGridLayout.numColumns = 1;
	    viewerPageComposite.setLayout(viewerPageGridLayout);
		
		viewerBrowser = new Browser(viewerPageComposite, SWT.NONE);
		viewerBrowser.setText("html");
		viewerBrowser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewerBrowser.addStatusTextListener(new StatusTextListener() {
			
			@Override
			public void changed(StatusTextEvent statusTextEvent) {
				MarkdownSemanticEPEditorContributor.getLinkField().setText(statusTextEvent.text);
			}
		});

		browserViewerPageIndex = addPage(viewerPageComposite);
		setPageText(browserViewerPageIndex, "Display");
	}
	
	/**
	 * Creates text editor page of the multi-page editor */
	private void createTextEditorPage() {
		try {
			textEditor = new MarkdownSemanticEPTextEditor(this);
			textEditorPageIndex = addPage(textEditor, getEditorInput());
			textEditorPart = getEditor(textEditorPageIndex);
			
			//textEditor.set
			
			setPageText(textEditorPageIndex, "md Editor");
		}
		catch (PartInitException partInitException) {
			ErrorDialog.openError(getSite().getShell(), "Error creating nested text editor", null, partInitException.getStatus());
		}
	}

	/** Select and reveal MD editor page */
	public void activateTextEditorPage() {
		if (this.getActivePage() != textEditorPageIndex) {
			this.setActiveEditor(textEditorPart);
		}
	}
	
	/** Preferences for file */
	private void createPreferencesPage() {
		
		/* Form */
		Composite pageContainer = getContainer();

		FormToolkit formsToolkit = new FormToolkit(pageContainer.getDisplay());
		Form preferencesForm = formsToolkit.createForm(pageContainer);
		preferencesForm.setText("\"" + textEditor.getTitle() + "\" Markdown Semantic Preferences");
		preferencesForm.setImage(R.getImage("md-file-toolbar-nottext"));
		preferencesForm.setToolBarVerticalAlignment(SWT.BOTTOM);
		formsToolkit.decorateFormHeading(preferencesForm);

		Action action;
		ActionContributionItem actionContributionItem;
		ISharedImages iSharedImages = PlatformUI.getWorkbench().getSharedImages();
		
		/* Restore Globals */
		action = new Action("mdsact_RestoreGlobals", Action.AS_PUSH_BUTTON) {
			public void run() {
				
				preferencesUI.loadDefaultPreferences();
			}
		};
		
		action.setImageDescriptor(R.getImageDescriptor("md-preference-toolbar-reload"));
		action.setToolTipText("Restore defaults from global preferences");
		action.setText("Restore Globals");
		actionContributionItem = new ActionContributionItem(action);
		actionContributionItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		preferencesForm.getToolBarManager().add(actionContributionItem);

		/* Clear Modifications */
		action = new Action("mdsact_ClearModifications", Action.AS_PUSH_BUTTON) {
			public void run() {
				
				preferencesUI.clearModifiedPreferences();
			}
		};

		action.setImageDescriptor(iSharedImages.getImageDescriptor(ISharedImages.IMG_ETOOL_CLEAR));
		action.setToolTipText("Clear modified preferences for this document");
		action.setText("Clear Modifications");
		actionContributionItem = new ActionContributionItem(action);
		actionContributionItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		preferencesForm.getToolBarManager().add(actionContributionItem);

		/* Apply to Document */
		action = new Action("mdsact_ApplyToDocument", Action.AS_PUSH_BUTTON) {
			public void run() {
				
				preferencesUI.savePreferences();
				refresh();
			}
		};

		action.setImageDescriptor(R.getImageDescriptor("md-preference-toolbar-apply"));
		action.setToolTipText("Apply to document");
		action.setText("Apply to Document");
		actionContributionItem = new ActionContributionItem(action);
		actionContributionItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		preferencesForm.getToolBarManager().add(actionContributionItem);
		
		/* Save Preferences */
		action = new Action("mdsact_SavePreferences", Action.AS_PUSH_BUTTON) {
			public void run() {
				
				preferencesUI.savePreferences();
				preferences.copyPreferences(workPreferences);
				
				savePreferencesToPropertiesIFile(getPreferencesPropertiesIFile());
				refresh();
			}
		};

		action.setImageDescriptor(iSharedImages.getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));
		action.setToolTipText("Save these preferences for this document");
		action.setText("Save Preferences");
		actionContributionItem = new ActionContributionItem(action);
		actionContributionItem.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		preferencesForm.getToolBarManager().add(actionContributionItem);
		
		preferencesForm.getToolBarManager().update(true);

		/* Section */
		Composite formComposite = preferencesForm.getBody();
		GridLayout formGridLayout = new GridLayout();
		formGridLayout.numColumns = 2;
		formGridLayout.marginWidth = 6;
		formGridLayout.marginHeight = 12;		
		formComposite.setLayout(formGridLayout);

		Section displayPreferencesSection = formsToolkit.createSection(formComposite, Section.DESCRIPTION | Section.TITLE_BAR);
		GridData sectionGridData = new GridData();
		sectionGridData.minimumHeight = 300;
		//sectionGridData.minimumWidth = 300;
		sectionGridData.widthHint = 500;
		displayPreferencesSection.setLayoutData(sectionGridData);

		Composite rightFillerComposite = new Composite(formComposite, SWT.NONE);
		GridData rightFillerGridData = new GridData();
		rightFillerGridData.horizontalAlignment = GridData.FILL;
		rightFillerGridData.minimumWidth = 6;
		rightFillerGridData.grabExcessHorizontalSpace = true;
		rightFillerComposite.setLayoutData(rightFillerGridData);
	
		displayPreferencesSection.setText("Display Preferences"); //$NON-NLS-1$
		displayPreferencesSection.setDescription("These values are used to override the default ones"); //$NON-NLS-1$
		displayPreferencesSection.marginWidth = 0;
		displayPreferencesSection.marginHeight = 0;
		Composite sectionComposite = formsToolkit.createComposite(displayPreferencesSection, SWT.WRAP);

		displayPreferencesSection.setClient(sectionComposite);

		/* Put the UI */
		preferencesUI.createPreferencesPanel(sectionComposite);

		displayPreferencesSection.getClient().setFocus();
		
		preferencesPageIndex = addPage(preferencesForm);
		setPageText(preferencesPageIndex, "Preferences");
	}
	
	/** Creates the pages of the multi-page editor */
	protected void createPages() {
		
		createBrowserViewerPage();
		createTextEditorPage();
		
		 
//		engine = new MarkdownSemanticEPEngine();
		preferences = MarkdownSemanticEPPreferences.createLocalPreferences();

		IFile mdPrefsIFile = getPreferencesPropertiesIFile(); 
		loadPreferencesFromPropertiesIFile(mdPrefsIFile);
		
		workPreferences = MarkdownSemanticEPPreferences.createEditPreferences(preferences);
		preferencesUI = new MarkdownSemanticEPDisplayPreferencesUI(workPreferences);
		
		createPreferencesPage();
		
		this.setPartName(textEditor.getTitle());
	}
	
	/** Preferences file for document */
	private IFile getPreferencesPropertiesIFile() {
		
		IFile mdIFile = (IFile) this.getEditorInput().getAdapter(IFile.class);
		String mdFileFullPath = mdIFile.getFullPath().toString();
		String mdPrefsFileFullPath = mdFileFullPath + ".prefs";
		IPath mdPrefsFileFullIPath = new Path(mdPrefsFileFullPath);
		return ResourcesPlugin.getWorkspace().getRoot().getFile(mdPrefsFileFullIPath);
	}

	/** Load preferences file for document */
	private void loadPreferencesFromPropertiesIFile(IFile mdPrefsIFile) {

		File propertiesFile = new File(mdPrefsIFile.getRawLocation().toOSString());
		
		if (!propertiesFile.exists()) {
			return;
		}
		
		Properties properties = F.loadPropertiesFile(propertiesFile);

		for (PreferenceKey preferenceKey : PreferenceKey.values()) {
			if (properties.containsKey(preferenceKey.name())) {
				preferences.setPreference(preferenceKey, properties.getProperty(preferenceKey.name()));
			}
		}
	}

	/** Save preferences file for document */
	private void savePreferencesToPropertiesIFile(IFile mdPrefsIFile) {

		Properties properties = new Properties();

		int propertiesToSave = 0;
		for (PreferenceKey preferenceKey : PreferenceKey.values()) {
			if (!preferences.isDefaultPreference(preferenceKey)) {
				properties.setProperty(preferenceKey.name(), preferences.getPreference(preferenceKey));
				propertiesToSave++;
			}
		}
		
		File propertiesFile = new File(mdPrefsIFile.getRawLocation().toOSString());
		
		if (propertiesToSave == 0) {
			/* All are default, delete the properties file */
			if (propertiesFile.exists()) {
				F.deleteFolder(propertiesFile);	
			}
		}
		else {
			F.savePropertiesInFile(properties, " \"" + getEditorInput().getName() + "\" Markdown Semantic Preferences", propertiesFile);
		}
	}

	/** He knows */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	
	/** Saves the multi-page editor's document */
	public void doSave(IProgressMonitor monitor) {
		textEditorPart.doSave(monitor);
	}
	
	/** Saves the multi-page editor's document as another file */
	public void doSaveAs() {
		textEditorPart.doSaveAs();
		setPageText(textEditorPageIndex, textEditorPart.getTitle());
		setInput(textEditorPart.getEditorInput());
	}
	
	/** He knows */
	public void gotoMarker(IMarker marker) {
		setActivePage(textEditorPageIndex);
		IDE.gotoMarker(textEditorPart, marker);
	}
	
	/** He knows */
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
	
		if (!(editorInput instanceof IFileEditorInput)) {
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		}
		super.init(site, editorInput);
	}
	
	/** Method declared on IEditorPart */
	public boolean isSaveAsAllowed() {
		return true;
	}
	
	/** Refreshes contents of viewer page when it is activated */
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		
		if (newPageIndex == browserViewerPageIndex) {
			refresh();
		}
		if (newPageIndex == preferencesPageIndex) {
			preferencesUI.setInitialFocus();
		}
	}
	
	/** Closes all project files on project close */
	public void resourceChanged(final IResourceChangeEvent event) {
		
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {
				
				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; i++) {
						if (((FileEditorInput)textEditor.getEditorInput()).getFile().getProject().equals(event.getResource())) {
							IEditorPart editorPart = pages[i].findEditor(textEditor.getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}            
			});
		}
	}
	
	/** Workbench request for outline view */
	@Override
	public <T> T getAdapter(Class<T> adapter) {
		
		/* Outline view */
		if (adapter.equals(IContentOutlinePage.class)) {
			return textEditor.getAdapter(adapter);
		}
		
		/* Other adapters */
		return super.getAdapter(adapter);
	}
	
	/** The browser will display the html markdown */
	public Browser getViewerBrowser() {
		return viewerBrowser;
	}

	/** The actual text editor */
	public MarkdownSemanticEPTextEditor getEditor() {
		return textEditor;
	}

	/** the browserViewerPageIndex */
	public int getBrowserViewerPageIndex() {
		return browserViewerPageIndex;
	}

	/** the textEditorPageIndex */
	public int getTextEditorPageIndex() {
		return textEditorPageIndex;
	}

	/** the preferencesPageIndex */
	public int getPreferencesPageIndex() {
		return preferencesPageIndex;
	}
	
}

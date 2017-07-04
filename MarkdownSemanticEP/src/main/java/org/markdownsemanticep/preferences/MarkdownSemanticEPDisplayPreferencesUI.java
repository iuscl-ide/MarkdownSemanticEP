package org.markdownsemanticep.preferences;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.markdownsemanticep.activator.R;
import org.markdownsemanticep.preferences.MarkdownSemanticEPPreferences.PreferenceKey;

public class MarkdownSemanticEPDisplayPreferencesUI {

	private final static int numberOfColumns = 4;
	private final static Color backgroundColor = R.getFormsToolkit().getColors().getBackground();
	private final static Color labelForegroundColor = R.getFormsToolkit().getColors().getColor("org.eclipse.ui.forms.TITLE");

	private MarkdownSemanticEPPreferences preferences;

	private MarkdownSemanticEPPreferences editPreferences;

	private final HashMap<PreferenceKey, Composite> flagComposites = new HashMap<>();
	
	private Text headersFontText;
	private Button lineUnderHeadersButton;
	private Button centerAlignHeadersButton;
	
	private Text textFontText;
	private Button justifyTextParagraphsButton;
	private Button centerAlignImagesButton;
	
	private Text codeFontText;
	private Button showBorderAroundCodeButton;
	private Button showBackgroundForCodeButton;
	private Button showPopupForCodeLanguageButton;
	
	private Button alternateTableRowsBackgroundButton;
	private Button showTableRowsAppearSelectableButton;
	
	
	/** Constructor, in global and in each local */
	public MarkdownSemanticEPDisplayPreferencesUI(MarkdownSemanticEPPreferences preferences) {
		super();
		this.preferences = preferences;
	}

	/** Focus for first editable */
	public void setInitialFocus() {

		headersFontText.setFocus();
	}

	/** Save edit preferences and affect properties */
	public void savePreferences() {

		preferences.copyPreferences(editPreferences);
	}

	/** Save edit preferences and affect properties */
	public boolean hasUnsavedPreferences() {

		for (PreferenceKey preferenceKey : PreferenceKey.values()) {
			if (!editPreferences.getPreference(preferenceKey).equals(preferences.getPreference(preferenceKey))) {
				return true;
			}
		}
		return false;
	}

	/** Load initial preferences only in the edit */
	public void clearModifiedPreferences() {

		editPreferences.copyPreferences(preferences);
		reloadPreferences();
	}
	
	/** Load default preferences only in the edit */
	public void loadDefaultPreferences() {

		editPreferences.resetPreferences();
		reloadPreferences();
	}

	/** Reload current preferences only in the edit */
	private void reloadPreferences() {

		headersFontText.setText(editPreferences.getPreference(PreferenceKey.HeadersFont));
		lineUnderHeadersButton.setSelection(editPreferences.getPreference(PreferenceKey.ShowDividerUnderHeaders).equals("true") ? true : false);
		centerAlignHeadersButton.setSelection(editPreferences.getPreference(PreferenceKey.CenterAlignHeaders).equals("true") ? true : false);
		
		textFontText.setText(editPreferences.getPreference(PreferenceKey.TextFont)); 
		justifyTextParagraphsButton.setSelection(editPreferences.getPreference(PreferenceKey.JustifyTextParagraphs).equals("true") ? true : false);
		centerAlignImagesButton.setSelection(editPreferences.getPreference(PreferenceKey.CenterAlignImages).equals("true") ? true : false);
		
		codeFontText.setText(editPreferences.getPreference(PreferenceKey.CodeFont));
		showBorderAroundCodeButton.setSelection(editPreferences.getPreference(PreferenceKey.ShowBorderAroundCode).equals("true") ? true : false);
		showBackgroundForCodeButton.setSelection(editPreferences.getPreference(PreferenceKey.ShowBackgroundForCode).equals("true") ? true : false);
		showPopupForCodeLanguageButton.setSelection(editPreferences.getPreference(PreferenceKey.ShowPopupForCodeLanguage).equals("true") ? true : false);
		
		alternateTableRowsBackgroundButton.setSelection(editPreferences.getPreference(PreferenceKey.AlternateTableRowsBackground).equals("true") ? true : false);
		showTableRowsAppearSelectableButton.setSelection(editPreferences.getPreference(PreferenceKey.ShowTableRowsAppearSelectable).equals("true") ? true : false);
	}

	/** Called each time */
	public void createPreferencesPanel(Composite containerComposite) {
		
		editPreferences = MarkdownSemanticEPPreferences.createEditPreferences(preferences);
		
		GridLayout containerGridLayout = new GridLayout();
		containerGridLayout.numColumns = numberOfColumns;
		containerGridLayout.marginWidth = 2;
		containerGridLayout.marginHeight = 15;		
		containerComposite.setLayout(containerGridLayout);

		/* Headers Font */
		headersFontText = createFontRow(containerComposite, "Headers Font:", "Font...", PreferenceKey.HeadersFont);
		
		/* Line under headers */
		lineUnderHeadersButton = createCheckRow(containerComposite, "Show line under headers", PreferenceKey.ShowDividerUnderHeaders);

		/* Center align headers */
		centerAlignHeadersButton = createCheckRow(containerComposite, "Center align headers", PreferenceKey.CenterAlignHeaders);

		createSeparatorRow(containerComposite);
		
		/* Text Font */
		textFontText = createFontRow(containerComposite, "Text Font:", "Font...", PreferenceKey.TextFont);

		/* Justify text paragraphs */
		justifyTextParagraphsButton = createCheckRow(containerComposite, "Justify text paragraphs", PreferenceKey.JustifyTextParagraphs);

		/* Center align images */
		centerAlignImagesButton = createCheckRow(containerComposite, "Center align images", PreferenceKey.CenterAlignImages);

		createSeparatorRow(containerComposite);
		
		/* Code Font */
		codeFontText = createFontRow(containerComposite, "Code Font:", "Font...", PreferenceKey.CodeFont);

		/* Show border around code */
		showBorderAroundCodeButton = createCheckRow(containerComposite, "Show border around code", PreferenceKey.ShowBorderAroundCode);

		/* Show background for code */
		showBackgroundForCodeButton = createCheckRow(containerComposite, "Show background for code", PreferenceKey.ShowBackgroundForCode);

		/* Show pop-up for code language */
		showPopupForCodeLanguageButton = createCheckRow(containerComposite, "Show pop-up for code language", PreferenceKey.ShowPopupForCodeLanguage);

		createSeparatorRow(containerComposite);
		
		/* Alternate table rows background */
		alternateTableRowsBackgroundButton = createCheckRow(containerComposite, "Alternate table rows background", PreferenceKey.AlternateTableRowsBackground);
		
		/* Show table rows appear as selectable */
		showTableRowsAppearSelectableButton = createCheckRow(containerComposite, "Show table rows appear as selectable", PreferenceKey.ShowTableRowsAppearSelectable);
	}

	/** A font per row */
	private Text createFontRow(Composite containerComposite, String labelText, String buttonText, final PreferenceKey preferenceKey) {

		final Composite flagComposite = createFlagComposite(containerComposite, preferenceKey);
		
		Label fontLabel = new Label(containerComposite, SWT.NONE); 
		fontLabel.setText(labelText);
		fontLabel.setForeground(labelForegroundColor);
		fontLabel.setBackground(backgroundColor);
		
		final Text fontText = new Text(containerComposite, SWT.SINGLE | SWT.BORDER);
		fontText.setText(editPreferences.getPreference(preferenceKey));
		fontText.setBackground(backgroundColor);
		GridData fontTextGridData = new GridData();
		fontTextGridData.horizontalAlignment = GridData.FILL;
		fontTextGridData.minimumWidth = 100;
		fontTextGridData.grabExcessHorizontalSpace = true;
		fontText.setLayoutData(fontTextGridData);
		
		fontText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent modifyEvent) {
				editPreferences.setPreference(preferenceKey, fontText.getText());
				flagComposite.redraw();
			}
		});
		
		Button fontButton = new Button(containerComposite, SWT.PUSH | SWT.FLAT);
		fontButton.setBackground(backgroundColor);
		fontButton.setText(buttonText);
		fontButton.pack();
		GridData fontButtonGridData = new GridData();
		fontButtonGridData.horizontalAlignment = GridData.FILL;
		fontButtonGridData.widthHint = fontButton.getSize().x + 32;
		fontButton.setLayoutData(fontButtonGridData);
		
		fontButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				
				String fontString = fontText.getText();
				String fontStringResult = executeFontDialog(fontString, "Markdown Headers Font");
				fontText.setText(fontStringResult);
			}
		});
		
		return fontText;
	}

	/** A check per row */
	private Button createCheckRow(Composite containerComposite, String checkboxText, final PreferenceKey preferenceKey) {

		final Composite flagComposite = createFlagComposite(containerComposite, preferenceKey);
		
		final Button checkButton = new Button(containerComposite, SWT.CHECK | SWT.FLAT);
		checkButton.setBackground(backgroundColor);
		checkButton.setText(checkboxText);
		checkButton.setSelection(editPreferences.getPreference(preferenceKey).equals("true") ? true : false);
		
		checkButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				
				if (checkButton.getSelection()) {
					editPreferences.setPreference(preferenceKey, "true");
				}
				else {
					editPreferences.setPreference(preferenceKey, "false");
				}
				flagComposite.redraw();
			}
		});
		
		GridData checkGridData = new GridData();
		checkGridData.grabExcessHorizontalSpace = true;
		checkGridData.horizontalSpan = numberOfColumns - 1;
		checkButton.setLayoutData(checkGridData);
		
		return checkButton;
	}

	/** Just a separator */
	private void createSeparatorRow(Composite containerComposite) {

		Composite separatorComposite = new Composite(containerComposite, SWT.NONE);
		separatorComposite.setBackground(backgroundColor);
		GridData separatorGridData = new GridData();
		separatorGridData.grabExcessHorizontalSpace = true;
		separatorGridData.heightHint = 2;
		separatorGridData.horizontalSpan = numberOfColumns;
		separatorComposite.setLayoutData(separatorGridData);
	}

	/** The flag for not default */
	private Composite createFlagComposite(Composite containerComposite, final PreferenceKey preferenceKey) {
		
		Composite flagComposite = new Composite(containerComposite, SWT.NONE);
		flagComposite.setBackground(backgroundColor);

		GridData flagGridData = new GridData();
		flagGridData.widthHint = 24;
		flagGridData.heightHint = 16;
		flagComposite.setLayoutData(flagGridData);

		flagComposite.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent paintEvent) {
				if (editPreferences.isDefaultPreference(preferenceKey)) {
					//
				}
				else {
					paintEvent.gc.drawImage(R.getImage("md-preference-toolbar-notdefault"), 0, 0);	
				}
			}
		});

		flagComposites.put(preferenceKey, flagComposite);
		
		return flagComposite;
	}
	
//	private void redrawFlags() {
//		
//		for (PreferenceKey preferenceKey : PreferenceKey.values()) {
//			
//			flagComposites.get(preferenceKey).redraw();
//		}
//	}
	
	/** Modify font */
	private String executeFontDialog(String fontString, String fontDialogTitle) {
		
		FontDialog fontDialog = new FontDialog(Display.getCurrent().getActiveShell());
		fontDialog.setText(fontDialogTitle);
		
		int sepSpace = fontString.lastIndexOf(" ");
		if (sepSpace == -1) {
			
			return fontString;
			// TODO the default values
		}
		
		String fontName = fontString.substring(0, sepSpace);
		int fontSize = Integer.parseInt(fontString.substring(sepSpace + 1));
		
		FontData fontData = new FontData(fontName, fontSize, SWT.NONE);
		FontData[] fontDatas = new FontData[1];
		fontDatas[0] = fontData;
		
		fontDialog.setFontList(fontDatas);
		FontData fontDataResult = fontDialog.open();
		if (fontDataResult != null) {
			return fontDataResult.getName() + " " + fontDataResult.getHeight();
		}
		
		return fontString;
	}

}

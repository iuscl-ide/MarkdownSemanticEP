/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.preferences;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.markdownsemanticep.activator.L;
import org.markdownsemanticep.activator.MarkdownSemanticEPActivator;

/** Markdown preferences */
public class MarkdownSemanticEPPreferences {

	/** The preference keys */
	public enum PreferenceKey {
		HeadersFont, ShowDividerUnderHeaders, CenterAlignHeaders,
		TextFont, JustifyTextParagraphs, CenterAlignImages,
		CodeFont, ShowBorderAroundCode, ShowBackgroundForCode, ShowPopupForCodeLanguage,
		AlternateTableRowsBackground, ShowTableRowsAppearSelectable }
	
	private final static String internalDigestKey ="org.markdownsemantic.preferences.digest";
	
	private final static MarkdownSemanticEPPreferences defaultPreferences = new MarkdownSemanticEPPreferences();
	private final static MarkdownSemanticEPPreferences globalPreferences = new MarkdownSemanticEPPreferences();
	
	private MarkdownSemanticEPPreferences parentPreferences;
	
	private final HashMap<PreferenceKey, String> preferencesMap = new HashMap<>();
	
	/* Initialize once */
	static {

		defaultPreferences.setPreference(PreferenceKey.HeadersFont, "Times New Roman 22");
		defaultPreferences.setPreference(PreferenceKey.ShowDividerUnderHeaders, "true");
		defaultPreferences.setPreference(PreferenceKey.CenterAlignHeaders, "false");
		defaultPreferences.setPreference(PreferenceKey.TextFont, "Calibri 16");
		defaultPreferences.setPreference(PreferenceKey.JustifyTextParagraphs, "false");
		defaultPreferences.setPreference(PreferenceKey.CenterAlignImages, "true");
		defaultPreferences.setPreference(PreferenceKey.CodeFont, "Consolas 13");
		defaultPreferences.setPreference(PreferenceKey.ShowBorderAroundCode, "true");
		defaultPreferences.setPreference(PreferenceKey.ShowBackgroundForCode, "false");
		defaultPreferences.setPreference(PreferenceKey.ShowPopupForCodeLanguage, "false");
		defaultPreferences.setPreference(PreferenceKey.AlternateTableRowsBackground, "false");
		defaultPreferences.setPreference(PreferenceKey.ShowTableRowsAppearSelectable, "false");

		globalPreferences.parentPreferences = defaultPreferences;
		
		IPreferenceStore preferenceStore = MarkdownSemanticEPActivator.getDefault().getPreferenceStore();
		
		String defaultPropertiesDigest = calculatePropertiesMD5(defaultPreferences);
//		L.i("defaultPropertiesDigest = " + defaultPropertiesDigest);
		if (preferenceStore.contains(internalDigestKey)) {
			
			if (defaultPropertiesDigest.equals(preferenceStore.getString(internalDigestKey))) {
				/* Same keys ? */ 
			}
			else {
				/* Keys are different */
				for (PreferenceKey preferenceKey : PreferenceKey.values()) {
					String preferenceKeyName = preferenceKey.name();
					if (!preferenceStore.contains(preferenceKeyName)) {
						preferenceStore.setValue(preferenceKeyName, defaultPreferences.getPreference(preferenceKey));
					}
				}
			}
			/* Already saved, load in global */
			for (PreferenceKey preferenceKey : PreferenceKey.values()) {
				globalPreferences.setPreference(preferenceKey, preferenceStore.getString(preferenceKey.name()));
			}
		}
		else {
			/* First time, default */
			preferenceStore.setValue(internalDigestKey, defaultPropertiesDigest);
			//preferenceStore.setValue(internalVersionKey, MarkdownSemanticEPActivator.getDefault().getBundle().getVersion().toString());
			for (PreferenceKey preferenceKey : PreferenceKey.values()) {
				preferenceStore.setValue(preferenceKey.name(), defaultPreferences.getPreference(preferenceKey));
			}
			globalPreferences.resetPreferences();

			L.i("First run, default global preferences created");
		}
	}

	/** Private constructor */
	private MarkdownSemanticEPPreferences() {
		/* Empty */
	}
	
	/** New preferences with global preferences as parent */
	public static MarkdownSemanticEPPreferences createLocalPreferences() {
		
		MarkdownSemanticEPPreferences localPreferences = new MarkdownSemanticEPPreferences();
		localPreferences.parentPreferences = globalPreferences;
		localPreferences.resetPreferences();
		
		return localPreferences;
	}

	/** New preferences with other preferences as parent */
	public static MarkdownSemanticEPPreferences createEditPreferences(MarkdownSemanticEPPreferences srcPreferences) {
		
		MarkdownSemanticEPPreferences destPreferences = new MarkdownSemanticEPPreferences();
		destPreferences.parentPreferences = srcPreferences.parentPreferences;
		destPreferences.copyPreferences(srcPreferences);
		
		return destPreferences;
	}

	/** Global preferences, only one */
	public static MarkdownSemanticEPPreferences getGlobalPreferences() {
		
		return globalPreferences;
	}

	/** Copy from another, only properties */
	public void copyPreferences(MarkdownSemanticEPPreferences srcPreferences) {
		
		preferencesMap.clear();
		for (PreferenceKey preferenceKey : PreferenceKey.values()) {
			setPreference(preferenceKey, srcPreferences.getPreference(preferenceKey));
		}
	}
	
	/** Reset preferences (defaults) */
	public void resetPreferences() {
		
		copyPreferences(parentPreferences);
	}

	/** Access the values */
	public  String getPreference(PreferenceKey preferenceKey) {
		return preferencesMap.get(preferenceKey);
	}

	/** Put the values */
	public String setPreference(PreferenceKey preferenceKey, String preference) {
		return preferencesMap.put(preferenceKey, preference);
	}

	/** Compare value with parent value */
	public boolean isDefaultPreference(PreferenceKey preferenceKey) {
		if (getPreference(preferenceKey).equals(parentPreferences.getPreference(preferenceKey))) {
			return true;
		}
		return false;
	}

	/** Default properties changed */
	private static String calculatePropertiesMD5(MarkdownSemanticEPPreferences preferences) {
		
		MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException noSuchAlgorithmException) {
        	L.e("NoSuchAlgorithmException in calculatePropertiesMD5", noSuchAlgorithmException);
            return null;
        }

        StringBuffer digestInputString = new StringBuffer();
		for (PreferenceKey preferenceKey : PreferenceKey.values()) {
			digestInputString.append(preferenceKey.name() + ";");
		}
        digest.update(digestInputString.toString().getBytes());
        
        byte[] md5sum = digest.digest();
        BigInteger bigInt = new BigInteger(1, md5sum);
        String output = bigInt.toString(16);
        /*  Fill to 32 chars */
        output = String.format("%32s", output).replace(' ', '0');
        return output;
	}
}

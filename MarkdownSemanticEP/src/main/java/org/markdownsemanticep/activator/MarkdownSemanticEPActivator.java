/* Markdown Semantic Eclipse Plug-in - (c) 2017 markdownsemanticep.org */
package org.markdownsemanticep.activator;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/** The activator class controls the plug-in life cycle */
public class MarkdownSemanticEPActivator extends AbstractUIPlugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "org.markdownsemanticep"; //$NON-NLS-1$

	/** The shared instance */
	private static MarkdownSemanticEPActivator plugin;

	/** Plug-in root folder */
	private File pluginFolder;

	
	/** The constructor */
	public MarkdownSemanticEPActivator() {
		/* Resources */
		R.loadResourceRegistry();
		
		/* Plug-in root folder */
		URL pluginRootURL = FileLocator.find(Platform.getBundle(PLUGIN_ID), new Path("/"), null);
		try {
			pluginFolder = (new File(FileLocator.resolve(pluginRootURL).toURI())).getCanonicalFile();
		}
		catch (IOException ioException) {
			L.e("IOException in MarkdownSemanticEPActivator", ioException);
		}
		catch (URISyntaxException uriSyntaxException) {
			L.e("URISyntaxException in MarkdownSemanticEPActivator", uriSyntaxException);
		}

		/* Log */
		File logFile = new File(pluginFolder, "log/MarkdownSemanticEP.log");
		L.initLog(logFile);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/** Returns the shared instance */
	public static MarkdownSemanticEPActivator getDefault() {
		return plugin;
	}

	/** Returns an image descriptor for the image file at the given plug-in relative path */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/** Returns plug-in root folder */
	public File getPluginFolder() {
		return pluginFolder;
	}
	
}

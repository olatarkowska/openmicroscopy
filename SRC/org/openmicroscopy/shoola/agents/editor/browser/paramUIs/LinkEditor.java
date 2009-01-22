 /*
 * org.openmicroscopy.shoola.agents.editor.browser.paramUIs.LinkEditor 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.editor.browser.paramUIs;

//Java Imports

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

//Third-party libraries

//Application-internal dependencies

import org.openmicroscopy.shoola.agents.editor.EditorAgent;
import org.openmicroscopy.shoola.agents.editor.IconManager;
import org.openmicroscopy.shoola.agents.editor.browser.FieldPanel;
import org.openmicroscopy.shoola.agents.editor.model.params.IParam;
import org.openmicroscopy.shoola.agents.editor.uiComponents.CustomButton;
import org.openmicroscopy.shoola.agents.editor.uiComponents.PopupMenuButton;
import org.openmicroscopy.shoola.agents.editor.uiComponents.RelativeFileChooser;
import org.openmicroscopy.shoola.agents.editor.util.BareBonesBrowserLaunch;
import org.openmicroscopy.shoola.agents.editor.util.FilePathMethods;
import org.openmicroscopy.shoola.agents.editor.util.PreferencesManager;
import org.openmicroscopy.shoola.agents.editor.util.XMLMethods;
import org.openmicroscopy.shoola.util.filter.file.EditorFileFilter;

/** 
 * This is the UI class for editing a Link.
 * 
 * ** NO-LONGER-USED! **
 * 
 * The link can be either an "Absolute" path to a local file,
 * a "Relative" path from the file that contains this parameter (must be 
 * saved somewhere), or a URL.
 * Clicking the link will attempt to open the file or link. 
 * If it is a link to an editor file, it should open in Editor. 
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class LinkEditor 
	extends AbstractParamEditor 
	implements ActionListener
{
	
	/**
	 * A link (stored in the VALUE attribute of the dataField) that is the
	 * URL to a local file or a web-page.
	 * This will be passed to the BareBonesBrowserLauncher when user clicks button. 
	 * This should open a web page, but also it seems to launch an appropriate application
	 * for other files. 
	 */
	private String 				URLlink;
	
	/**
	 * This is the type of link that is currently set for this field. 
	 * Possible values are LOCAL_LINK, RELATIVE_LINK, URL_LINK, 
	 * LOCAL_EDITOR_LINK, RELATIVE_EDITOR_LINK, BROKEN_LINK
	 * It is set by updateLink(), depending on the attribute retrieved from dataField. 
	 * Used to set the icon etc. 
	 */
	private int 				linkType;
	
	/**
	 * Defines a local file that is linked with an "Absolute" file path
	 */
	public static final int 	LOCAL_LINK = 0;
	
	/**
	 * Defines a local file that is linked with a "Relative" file path
	 */
	public static final int 	RELATIVE_LINK = 1;
	
	/**
	 * Defines a URL link
	 */
	public static final int 	URL_LINK = 2;
	
	/**
	 * Defines a local OMERO.editor file that is linked with an "Absolute" path
	 */
	public static final int 	LOCAL_EDITOR_LINK = 3;
	
	/**
	 * Defines a local OMERO.editor file that is linked with a "Relative" path
	 */
	public static final int 	RELATIVE_EDITOR_LINK = 4;
	
	/**
	 * Defines that the linked file cannot be found. 
	 */
	public static final int 	BROKEN_LINK = 5;
	
	
	/**
	 * The Icon to indicate that the current link is local
	 */
	private Icon 				linkLocalIcon;
	
	/**
	 * The Icon to indicate that the current link is local and relative
	 */
	private Icon 				linkRelativeIcon;
	
	/**
	 * The Icon to indicate that the current link is to a local editor file
	 */
	private Icon 				editorLinkIcon;
	
	/**
	 * The Icon to indicate that the current link is a relative link to
	 * a local editor file
	 */
	private Icon 				editorRelativeLinkIcon;
	
	/**
	 * This icon is displayed if the link is broken (file not found)
	 */
	private Icon 				brokenLinkIcon;
	
	/**
	 * The Icon to indicate that the current link is a URL
	 */
	private Icon 				wwwIcon;
	
	/**
	 * A button that has blue underlined text displaying a link
	 */
	private JButton 			linkButton;
	
	/**
	 * The button that users click to choose a link. 
	 * Provides a pop-up menu with options..
	 */
	private JButton 			getLinkButton;
	
	/**
	 * Initialises the UI components
	 */
	private void initialise() 
	{
		IconManager imF = IconManager.getInstance();
		linkLocalIcon = imF.getIcon(IconManager.LINK_LOCAL_ICON);
		linkRelativeIcon = imF.getIcon(IconManager.LINK_RELATIVE_ICON);
		editorLinkIcon = imF.getIcon(IconManager.LINK_SCIENCE_ICON);
		editorRelativeLinkIcon =imF.getIcon(IconManager.LINK_SCIENCE_RELATIVE_ICON);
		brokenLinkIcon = imF.getIcon(IconManager.FILE_CLOSE_ICON);
		Icon chooseLinkIcon = imF.getIcon(IconManager.WRENCH_ICON);
		wwwIcon = imF.getIcon(IconManager.WWW_ICON);
		
		linkButton = new CustomButton();
		linkButton.addActionListener(this);
		linkButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		linkButton.setForeground(Color.BLUE);
		
		Action[] getLinkActions = new Action[] {
				new GetURLAction(),
				new GetLinkPathAction()};
		getLinkButton = new PopupMenuButton("Choose a link to a URL or local file", 
				chooseLinkIcon, getLinkActions);
	}
	
	/**
	 * Builds the UI. Adds a link and a get-link button.
	 */
	private void buildUI() 
	{	
		add(linkButton);
		
		add(getLinkButton);
	}
	
	/**
	 * Updates the local value of URLlink with the parameter value.
	 * Then calls refreshLink() to display new link.
	 */
	private void updateLink() {
		
		FileFilter filter = new EditorFileFilter();
		
		/*
		 * First check the value of the Absolute file path attribute
		 */
		//URLlink = getParameter().getAttribute(DataRefParam.ABSOLUTE_FILE_LINK);
		if (URLlink != null) {
			
			File linkedFile = new File(URLlink);
			if (! linkedFile.exists()) {
				linkType = BROKEN_LINK;
			} else {
				if (filter.accept(linkedFile)) 
						linkType = LOCAL_EDITOR_LINK;
				else
					linkType = LOCAL_LINK;	
			}
		}
		
		// if null, check the Relative file path attribute..
		else {
			//URLlink = getParameter().getAttribute(DataRefParam.RELATIVE_FILE_LINK);
			// .. and if not null, convert it to absolute path.
			if (URLlink != null) {
				
				// check if file exists (need to get full path first)!
				File editorFile = null;	//TODO get reference to file!!
				
				if (editorFile == null)		{
					linkType = BROKEN_LINK;
					refreshLink();
					return;
				}
				
				URLlink = FilePathMethods.getAbsolutePathFromRelativePath
					(editorFile, URLlink);
				File linkedFile = new File(URLlink);
				
				// if file does not exist - broken link
				if (! linkedFile.exists()) {
					linkType = BROKEN_LINK;
				} else {
					// otherwise, set the link type according to file type
					if (filter.accept(linkedFile))
						linkType = RELATIVE_EDITOR_LINK;
					else
						linkType = RELATIVE_LINK;
				}
			}
		}
		// if still null, check the URL 
		if (URLlink == null) {
			//URLlink = getParameter().getAttribute(DataRefParam.URL_LINK);
			linkType = URL_LINK;
		}
		
		refreshLink();
	}

	/**
	 * Displays the current value of URLlink. 
	 */
	private void refreshLink() {
		
		if (URLlink == null) {
			linkButton.setText("");
			linkButton.setToolTipText("No link set");
			linkButton.setIcon(linkLocalIcon); 
			linkButton.setEnabled(false);
			return;
		} 
		
		int len = URLlink.length();
		linkButton.setText(len < 30 ? URLlink : "..." + URLlink.substring(len-29, len));
		linkButton.setToolTipText("Open the link to: " + URLlink);
		linkButton.setEnabled(true);
		
		switch (linkType) {
			case LOCAL_LINK: 
				linkButton.setIcon(linkLocalIcon);
				break;
			case RELATIVE_LINK:
				linkButton.setIcon(linkRelativeIcon);
				break;
			case LOCAL_EDITOR_LINK:
				linkButton.setIcon(editorLinkIcon);
				break;
			case RELATIVE_EDITOR_LINK:
				linkButton.setIcon(editorRelativeLinkIcon);
				break;
			case URL_LINK:
				linkButton.setIcon(wwwIcon);
				break;
			case BROKEN_LINK:
				linkButton.setIcon(brokenLinkIcon);
				linkButton.setEnabled(false);
				linkButton.setToolTipText("FILE NOT FOUND: " + URLlink);
				break;
		}
	
	}

	/**
	 * An Action that is used in the get-link pop-up menu. 
	 * Allows users to enter a URL in a dialog box. 
	 * 
	 * @author will
	 *
	 */
	private class GetURLAction extends AbstractAction 
	{
		
		/**
		 * Creates an instance. Sets the Name, Description and Icon. 
		 */
		public GetURLAction() 
		{
			putValue(Action.NAME, "Set URL");
			putValue(Action.SHORT_DESCRIPTION, "Link to a web page");
			putValue(Action.SMALL_ICON, wwwIcon); 
		}
		
		/**
		 * Opens a dialog box for users to enter a URL
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) 
		{
			String url = (String)JOptionPane.showInputDialog(
	                null, "Enter URL:", "Enter URL", JOptionPane.PLAIN_MESSAGE,
	                wwwIcon, null, "http://");
	
			if ((url == null) || (url.length() == 0)) // user canceled
				return;
			
			// If the user did not input a valid URL, try adding "http://"
			try {
				new URL(url);
			} catch (MalformedURLException ex) {
				// This will make a valid URL. BUT may not be valid link.
				url = "http://" + url;
			}
			
			// Save the URL to dataField
			//saveLinkToParam(DataRefParam.URL_LINK, url);
		}
	}

	/**
	 * An Action that is used in the get-link pop-up menu. 
	 * Allows users to choose a link to a  local file using a file chooser. 
	 * 
	 * @author will
	 *
	 */
	private class GetLinkPathAction extends AbstractAction 
	{
		
		/**
		 * Creates an instance. Sets the Name, Description, and Icon
		 */
		public GetLinkPathAction() 
		{
			putValue(Action.NAME, "Set Link to local file");
			putValue(Action.SHORT_DESCRIPTION, "Choose a file, that will be linked from this file");
			putValue(Action.SMALL_ICON, linkLocalIcon); 
		}
		
		/**
		 * Opens a dialog box for users to choose a local file. 
		 * 
		 * @see ActionListener#actionPerformed(ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) 
		{
			getAndSaveLink();
		}
	}

	

	/**
	 * Opens a fileChooser, for the user to pick a file.
	 * Then this file path is saved to dataField. 
	 * If the user checks "Relative link" then filePath is saved as relative. 
	 */
	private void getAndSaveLink() 
	{	
		String currentFilePath = PreferencesManager.getPreference(
				PreferencesManager.CURRENT_IMAGES_FOLDER);
		
		String startDir = null;	//TODO		get the directory of the editor file
		
		// This creates a modal FileChooser dialog. 
		RelativeFileChooser relFileChooser = new RelativeFileChooser(null,
				startDir, currentFilePath);
		
		
		String linkedFilePath = relFileChooser.getPath();
		
		if (linkedFilePath == null) 
			return;		// user canceled
	
		
	}

	/**
	 * Creates an instance of the class. 
	 * Initiates, then builds the UI. 
	 * 
	 * @param param		The parameter to edit by this field
	 */
	public LinkEditor(IParam param) 
	{	
		super(param);
		
		initialise();
		
		buildUI();
		
		// Set the link, based on the value of the relative attributes
		updateLink();
	}
	
	/**
	 * This listener for the linkButton uses BareBonesBrowserLauncher, passing it
	 * URLlink. 
	 * 
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		if (linkType==LOCAL_EDITOR_LINK) {
			
			File f = new File(URLlink);
			// if link to local file that exists, open in Editor. 
			if (f.exists())
				EditorAgent.openLocalFile(f);
			
		}
		else if (linkType==RELATIVE_EDITOR_LINK) {
			// need to get absolute path from relative path! 
		}
		else {
			/*
			 * If not null, Check that the URLlink is a valid URL...
			 */
			if (URLlink != null) {
				try {
					new URL(URLlink);
					// if not, add the file extension...
				} catch (MalformedURLException ex) {
					 if (System.getProperty("os.name").startsWith("Mac OS")) {
						 URLlink = "file://" + URLlink;
				     } else {
				    	 URLlink = "file:///" + URLlink;
				    }
				}
			}
			BareBonesBrowserLaunch.openURL(URLlink);
		}
	}

	/**
	 * Calls the super.attributeEdited(), then 
	 * fires propertyChange to refresh link, and size of panel
	 * 
	 * @see AbstractParamEditor#attributeEdited(String, Object)
	 */
	public void attributeEdited(String attributeName, Object newValue) 
	{	 
		super.attributeEdited(attributeName, newValue);
		
		firePropertyChange(FieldPanel.UPDATE_EDITING_PROPERTY, null, null);
	}

	/**
	 * @see ITreeEditComp#getEditDisplayName()
	 */
	public String getEditDisplayName() { return "Edit Link"; }

}

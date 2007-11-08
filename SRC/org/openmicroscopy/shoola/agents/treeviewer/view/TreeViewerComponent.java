/*
 * org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerComponent
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.treeviewer.view;




//Java imports
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.events.SaveData;
import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerTranslator;
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.treeviewer.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.treeviewer.clsf.Classifier;
import org.openmicroscopy.shoola.agents.treeviewer.cmd.PropertiesCmd;
import org.openmicroscopy.shoola.agents.treeviewer.editors.Editor;
import org.openmicroscopy.shoola.agents.treeviewer.editors.EditorFactory;
import org.openmicroscopy.shoola.agents.treeviewer.editors.EditorSaverDialog;
import org.openmicroscopy.shoola.agents.treeviewer.finder.ClearVisitor;
import org.openmicroscopy.shoola.agents.treeviewer.finder.Finder;
import org.openmicroscopy.shoola.agents.treeviewer.profile.ProfileEditor;
import org.openmicroscopy.shoola.agents.treeviewer.profile.ProfileEditorFactory;
import org.openmicroscopy.shoola.agents.treeviewer.util.AddExistingObjectsDialog;
import org.openmicroscopy.shoola.agents.treeviewer.util.UserManagerDialog;
import org.openmicroscopy.shoola.agents.util.DataHandler;
import org.openmicroscopy.shoola.env.data.events.ExitApplication;
import org.openmicroscopy.shoola.env.data.model.TimeRefObject;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

import pojos.CategoryData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;

/** 
* Implements the {@link TreeViewer} interface to provide the functionality
* required of the tree viewer component.
* This class is the component hub and embeds the component's MVC triad.
* It manages the component's state machine and fires state change 
* notifications as appropriate, but delegates actual functionality to the
* MVC sub-components.
*
* @see org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerModel
* @see org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerWin
* @see org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerControl
*
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @version 2.2
* <small>
* (<b>Internal version:</b> $Revision$ $Date$)
* </small>
* @since OME2.2
*/
class TreeViewerComponent
 	extends AbstractComponent
 	implements TreeViewer
{
  
	/** The Model sub-component. */
	private TreeViewerModel     model;

	/** The Controller sub-component. */
	private TreeViewerControl   controller;

	/** The View sub-component. */
	private TreeViewerWin       view;

	/** The dialog used to display the form when a new node is created. */
	private EditorDialog        editorDialog;

	/** 
	 * The dialog used to ask a question to user when selecting a new node
	 * and some have been modified.
	 */
	private  EditorSaverDialog	saverDialog;

	/** The dialog presenting the list of available users. */
	private UserManagerDialog	switchUserDialog;

	/** 
	 * Displays the user groups.
	 * 
	 * @param map 	The map whose key is a <code>GroupData</code>s
	 * 				and the value a collection of 
	 * 				<code>ExperimenterData</code>s.
	 */
	private void displayUserGroups(Map map)
	{
		if (switchUserDialog == null) {
			JFrame f = (JFrame) TreeViewerAgent.getRegistry().getTaskBar();
			switchUserDialog = new UserManagerDialog(f, model.getUserDetails(), 
					map);
			switchUserDialog.addPropertyChangeListener(controller);
			//switchUserDialog.pack();
			switchUserDialog.setDefaultSize();
		}
		UIUtilities.centerAndShow(switchUserDialog);
	}

	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straight 
	 * after to complete the MVC set up.
	 * 
	 * @param model The Model sub-component.
	 */
	TreeViewerComponent(TreeViewerModel model)
	{
		if (model == null) throw new NullPointerException("No model."); 
		this.model = model;
		controller = new TreeViewerControl(this);
		view = new TreeViewerWin();
		Finder f = new Finder(this);
		model.setFinder(f);
		f.addPropertyChangeListener(controller);
	}

	/** 
	 * Links up the MVC triad. 
	 * 
	 * @param bounds	The bounds of the component invoking a new 
	 * 					{@link TreeViewer}.
	 */
	void initialize(Rectangle bounds)
	{
		controller.initialize(view);
		view.initialize(controller, model, bounds);
	}

	/**
	 * Sets the ids used to copy rendering settings.
	 * 
	 * @param pixelsID	The id of the pixels set of reference.
	 */
	void setRndSettings(long pixelsID)
	{
		if (model.getState() == DISCARDED) return;
		model.setRndSettings(pixelsID);
	}

	/**
	 * Returns the Model sub-component.
	 * 
	 * @return See above.
	 */
	TreeViewerModel getModel() { return model; }

	/**
	 * Sets to <code>true</code> if the component is recycled, 
	 * to <code>false</code> otherwise.
	 * 
	 * @param b The value to set.
	 */
	void setRecycled(boolean b) { model.setRecycled(b); }

	/**
	 * Returns <code>true</code> if there is annotation to save,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	boolean hasAnnotationToSave()
	{
		Editor editor = model.getEditor();
		if (editor == null) return false;
		return editor.hasAnnotationToSave();
	}
	
	void saveOnClose(SaveData evt)
	{
		Editor editor = model.getEditor();
		switch (evt.getType()) {
			case SaveData.DATA_MANAGER_ANNOTATION:
				if (editor == null) 
					editor.saveData();
				break;
			case SaveData.DATA_MANAGER_EDIT:
				if (editor == null) 
					editor.saveData();
				break;
		};
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getState()
	 */
	public int getState() { return model.getState(); }

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#activate()
	 */
	public void activate()
	{
		switch (model.getState()) {
		case NEW:
			model.getSelectedBrowser().activate(); 
			view.setOnScreen();
			//view.toFront();
			model.setState(READY);
			break;
		case DISCARDED:
			throw new IllegalStateException(
					"This method can't be invoked in the DISCARDED state.");
		} 
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getBrowsers()
	 */
	public Map getBrowsers() { return model.getBrowsers(); }

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#discard()
	 */
	public void discard()
	{
		Map browsers = getBrowsers();
		Iterator i = browsers.values().iterator();
		while (i.hasNext())
			((Browser) i.next()).discard();
		model.discard();
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getSelectedBrowser()
	 */
	public Browser getSelectedBrowser() { return model.getSelectedBrowser(); }

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setSelectedBrowser(Browser)
	 */
	public void setSelectedBrowser(Browser browser)
	{
		switch (model.getState()) {
		case DISCARDED:
		case SAVE:
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED or SAVE " +
			"state.");
		}
		Browser oldBrowser = model.getSelectedBrowser();
		if (oldBrowser == null || !oldBrowser.equals(browser)) {
			model.setSelectedBrowser(browser);
			if (browser != null) browser.activate();
			removeEditor();
			firePropertyChange(SELECTED_BROWSER_PROPERTY, oldBrowser, browser);
		}
		view.updateMenuItems();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#displayBrowser(int)
	 */
	public void displayBrowser(int browserType)
	{
		switch (model.getState()) {
		case DISCARDED:
		case SAVE:
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED or SAVE " +
			"state.");
		}
		Map browsers = model.getBrowsers();
		Browser browser = (Browser) browsers.get(new Integer(browserType));
		if (browser.isDisplayed()) {
			view.removeBrowser(browser);
		} else {
			model.setSelectedBrowser(browser);
			view.addBrowser(browser);
		}
		browser.setDisplayed(!browser.isDisplayed());
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#createDataObject(DataObject)
	 */
	public void createDataObject(DataObject object)
	{
		switch (model.getState()) {
			case DISCARDED:
			case SAVE:
				throw new IllegalStateException(
						"This method cannot be invoked in the DISCARDED " +
						"or SAVE state.");
		}
		removeEditor();
		//tmp solution
		if (object == null) return;
		TreeImageDisplay parent =  
							model.getSelectedBrowser().getLastSelectedDisplay();
		int editorType = CREATE_EDITOR;
		model.setEditorType(editorType);
		Editor editor = EditorFactory.getEditor(this, object, editorType, 
				parent);
		editor.addPropertyChangeListener(controller);
		editor.activate();
		model.setEditor(editor);
		editorDialog = new EditorDialog(view, editor);
		UIUtilities.centerAndShow(editorDialog);
		onComponentStateChange(false);
	}
	
	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#showProperties(DataObject, int)
	 */
	public void showProperties(TreeImageDisplay node, int editorIndex)
	{
		switch (model.getState()) {
			case DISCARDED:
			case SAVE:
				throw new IllegalStateException(
						"This method cannot be invoked in the DISCARDED " +
						"or SAVE state.");
		}
		if (node  == null) return;
		Object object = node.getUserObject();
        if (!(object instanceof DataObject)) return;
		removeEditor();
		//tmp solution
		
		if (object == null) return;
		if (object instanceof ExperimenterData) {
			ExperimenterData exp = (ExperimenterData) object;
			if (exp.getId() != model.getUserDetails().getId()) return;
			ProfileEditor pEditor = ProfileEditorFactory.getEditor(exp);
			pEditor.addPropertyChangeListener(controller);
			pEditor.activate();
			view.addComponent(pEditor.getUI());

			return;
		}
		if (editorIndex != -1) EditorFactory.setEditorSelectedPane(editorIndex);
		DataObject ho = (DataObject) object;
		model.setEditorType(PROPERTIES_EDITOR);
		Editor editor = EditorFactory.getEditor(this, ho, 
												PROPERTIES_EDITOR, null);
		editor.addPropertyChangeListener(controller);
		editor.activate();
		model.setEditor(editor);
		editor.addSiblings(
				model.getSelectedBrowser().getSelectedDataObjects());
		view.addComponent(editor.getUI());
		editor.setDefaultButton(view.getRootPane());
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#cancel()
	 */
	public void cancel()
	{
		if (model.getState() != DISCARDED) {
			model.cancel();
			fireStateChange(); 
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#removeEditor()
	 */
	public void removeEditor()
	{
		switch (model.getState()) {
		case DISCARDED:
			//case SAVE: 
			throw new IllegalStateException("This method cannot be " +
			"invoked in the DISCARDED, SAVE state.");
		}
		if (editorDialog != null) editorDialog.close();
		model.setEditorType(NO_EDITOR);
		view.removeAllFromWorkingPane();
		firePropertyChange(REMOVE_EDITOR_PROPERTY, Boolean.FALSE, Boolean.TRUE);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getUserDetails()
	 */
	public ExperimenterData getUserDetails() { return model.getUserDetails(); }

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#showFinder(boolean)
	 */
	public void showFinder(boolean b)
	{
		switch (model.getState()) {
		case DISCARDED:
			throw new IllegalStateException("This method should cannot " +
			"be invoked in the DISCARDED state.");
		}
		if (model.getSelectedBrowser() == null) return;
		Finder finder = model.getFinder();
		if (b == finder.isDisplay())  return;
		Boolean oldValue = 
			finder.isDisplay() ? Boolean.TRUE : Boolean.FALSE,
					newValue = b ? Boolean.TRUE : Boolean.FALSE;
		view.showFinder(b);
		firePropertyChange(FINDER_VISIBLE_PROPERTY, oldValue, newValue);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#closeWindow()
	 */
	public void closeWindow()
	{
		cancel();
		if (TreeViewerFactory.isLastViewer()) {
			EventBus bus = TreeViewerAgent.getRegistry().getEventBus();
			bus.post(new ExitApplication());
		} else discard();

	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#removeObject(TreeImageDisplay)
	 */
	public void removeObject(TreeImageDisplay node)
	{
		/*
      switch (model.getState()) {
          case READY:
          case NEW:  
          case LOADING_THUMBNAIL:
              break;
          default:
              throw new IllegalStateException("This method should only be " +
              "invoked in the READY or NEW state.");
      }
		 */
		if (node == null)
			throw new IllegalArgumentException("No node to remove.");
		if (!(node.getUserObject() instanceof DataObject))
			throw new IllegalArgumentException("Can only remove DataObject.");
		model.fireDataObjectsDeletion(node);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#removeObjects(List)
	 */
	public void removeObjects(List nodes)
	{
		/*
      switch (model.getState()) {
          case READY:
          case NEW:  
          case LOADING_THUMBNAIL:
              break;
          default:
              throw new IllegalStateException("This method should only be " +
              "invoked in the READY or NEW state.");
      }
		 */
		model.fireDataObjectsDeletion(nodes);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#classify(Set, int)
	 */
	public void classify(Set<ImageData> images, int mode)
	{
		switch (model.getState()) {
		//case READY:
		case DISCARDED:
			throw new IllegalStateException("This method should cannot " +
			"be invoked in the DISCARDED state.");
		}

		if (images == null) 
			throw new IllegalArgumentException("Object cannot be null.");
		if (images.size() == 0)
			throw new IllegalArgumentException("No images to classify or " +
			"declassify.");
		DataHandler dh = model.classifyImageObjects(view, images, mode);
		dh.addPropertyChangeListener(controller);
		dh.activate();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setThumbnail(BufferedImage)
	 */
	public void setThumbnail(BufferedImage thumbnail)
	{
		if (model.getState() == LOADING_THUMBNAIL) {
			model.setState(READY);
			Editor editor = model.getEditor();
			if (thumbnail != null && editor != null) 
				editor.setThumbnail(thumbnail);
			fireStateChange();
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#retrieveThumbnail(ImageData)
	 */
	public void retrieveThumbnail(ImageData image)
	{
		if (model.getState() != DISCARDED) {
			if (image == null)
				throw new IllegalArgumentException("No image.");
			model.fireThumbnailLoading(image);
			fireStateChange();
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onSelectedDisplay()
	 */
	public void onSelectedDisplay()
	{
		switch (model.getState()) {
		case DISCARDED:
		case SAVE:  
			throw new IllegalStateException("This method cannot be " +
			"invoked in the DISCARDED, SAVE state.");
		}
		int editor = model.getEditorType();
		removeEditor();
		if (editor != TreeViewer.CREATE_EDITOR) {
			PropertiesCmd cmd = new PropertiesCmd(this);
			cmd.execute();
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onDataObjectSave(DataObject, int)
	 */
	public void onDataObjectSave(DataObject data, int operation)
	{
		int state = model.getState();
		if (operation == REMOVE_OBJECT && state != SAVE)
			throw new IllegalStateException("This method can only be " +
			"invoked in the SAVE state");
		switch (state) {
			case DISCARDED:
				throw new IllegalStateException("This method cannot be " +
				"invoked in the DISCARDED state");
		}
		if (data == null) 
			throw new IllegalArgumentException("No data object. ");
		switch (operation) {
			case CREATE_OBJECT:
			case UPDATE_OBJECT: 
			case REMOVE_OBJECT:  
				break;
			default:
				throw new IllegalArgumentException("Save operation not " +
						"supported.");
		}  
		model.setEditor(null);
		//int editor = model.getEditorType();
		//removeEditor(); //remove the currently selected editor.
		if (operation == REMOVE_OBJECT) {
			model.setState(READY);
			fireStateChange();
		}
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		Browser browser = model.getSelectedBrowser();
		browser.refreshEdition(data, operation);
		//browser.refreshLoggedExperimenterData();
		if (operation == UPDATE_OBJECT) {
			Map browsers = model.getBrowsers();
			Iterator i = browsers.keySet().iterator();
			while (i.hasNext()) {
				browser = (Browser) browsers.get(i.next());
				if (!(browser.equals(model.getSelectedBrowser())))
					browser.refreshEdition(data, operation);
			}
		}
		onSelectedDisplay();
		if (saverDialog != null) {
			saverDialog.close();
			saverDialog = null;
		}
		setStatus(false, "", true);
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onNodesRemoved()
	 */
	public void onNodesRemoved()
	{
		if (model.getState()!= SAVE)
			throw new IllegalStateException("This method can only be " +
			"invoked in the SAVE state");
		model.setState(READY);
		fireStateChange();
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		Map browsers = model.getBrowsers();
		Browser browser;
		Iterator i = browsers.keySet().iterator();
		while (i.hasNext()) {
			browser = (Browser) browsers.get(i.next());
			browser.refreshTree();
		}
		model.setEditor(null);
		onSelectedDisplay();
		setStatus(false, "", true);
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#clearFoundResults()
	 */
	public void clearFoundResults()
	{
		switch (model.getState()) {
		//case LOADING_THUMBNAIL:
		case DISCARDED:
		case SAVE:  
			throw new IllegalStateException("This method cannot be " +
					"invoked in the DISCARDED, SAVE or LOADING_THUMBNAIL " +
			"state");
		}
		removeEditor(); //remove the currently selected editor.
		Browser browser = model.getSelectedBrowser();
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (browser != null) {
			browser.accept(new ClearVisitor());
			browser.setFoundInBrowser(null); 
		}
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onImageClassified(ImageData[], Set, int)
	 */
	public void onImageClassified(ImageData[] images, Set categories, int mode)
	{
		switch (model.getState()) {
		case DISCARDED:
			throw new IllegalStateException("This method cannot be " +
					"invoked in the DISCARDED, SAVE or LOADING_THUMBNAIL " +
			"state");
		}
		if (categories == null)
			throw new IllegalArgumentException("Categories shouln't be null.");
		if (images == null)
			throw new IllegalArgumentException("No image.");
		if (images.length == 0)
			throw new IllegalArgumentException("No image.");
		if (mode != Classifier.CLASSIFY_MODE && 
				mode != Classifier.DECLASSIFY_MODE)
			throw new IllegalArgumentException("Classification mode not " +
			"supported.");
		TreeImageDisplay d = getSelectedBrowser().getLastSelectedDisplay();
		Map browsers = model.getBrowsers();
		Iterator b = browsers.keySet().iterator();
		Browser browser;
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		while (b.hasNext()) {
			browser = (Browser) browsers.get(b.next());
			browser.refreshTree();
			//browser.refreshClassification(images, categories, mode);
		}
		getSelectedBrowser().setSelectedDisplay(d);

		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
	}

	/**            
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#moveToBack()
	 */
	public void moveToBack()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		view.toBack();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#moveToFront()
	 */
	public void moveToFront()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		view.toFront();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setHierarchyRoot(long, ExperimenterData)
	 */
	public void setHierarchyRoot(long userGroupID, 
			ExperimenterData experimenter)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (experimenter == null) return;
		/*
  	TreeViewer viewer = TreeViewerFactory.getTreeViewer(experimenter, 
  														userGroupID, 
  														view.getBounds());
  	EditorFactory.setEditorSelectedPane(Editor.PROPERTIES_EDITOR);
      EditorFactory.setSubSelectedPane(Editor.ANNOTATION_INDEX);
      if (viewer != null) {
      	if (viewer.isRecycled()) viewer.moveToFront();
      	else viewer.activate();
      }
		 */
		Map browsers = model.getBrowsers();
		Iterator i = browsers.keySet().iterator();
		Browser browser;
		while (i.hasNext()) {
			browser = (Browser) browsers.get(i.next());
			browser.addExperimenter(experimenter);
		}
		//Creates a new 
		/*
  	removeEditor();
      model.setExperimenter(experimenter);
      model.setHierarchyRoot(experimenter.getId(), userGroupID);
      //Reset editor selected pane
      EditorFactory.setEditorSelectedPane(Editor.PROPERTIES_EDITOR);
      EditorFactory.setSubSelectedPane(Editor.ANNOTATION_INDEX);
      if (model.getState() == READY)
          firePropertyChange(HIERARCHY_ROOT_PROPERTY, Boolean.FALSE, 
          					Boolean.TRUE);
		 */

	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#isObjectWritable(Object)
	 */
	public boolean isObjectWritable(Object ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		//Check if current user can write in object
		long id = model.getUserDetails().getId();
		long groupId = model.getUserGroupID();
		return TreeViewerTranslator.isWritable(ho, id, groupId);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#addExistingObjects(DataObject)
	 */
	public void addExistingObjects(DataObject ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (ho == null) 
			throw new IllegalArgumentException("No object.");
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		model.fireDataExistingObjectsLoader(ho);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setExistingObjects(Set)
	 */
	public void setExistingObjects(Set nodes)
	{
		if (model.getState() != LOADING_DATA)
			throw new IllegalStateException(
					"This method cannot be invoked in the LOADING_DATA state.");
		if (nodes == null)
			throw new IllegalArgumentException("Nodes cannot be null.");
		view.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		Set n = TreeViewerTranslator.transformIntoCheckNodes(nodes, 
				getUserDetails().getId(), model.getUserGroupID());
		model.setState(LOADING_SELECTION);
		AddExistingObjectsDialog 
		dialog = new AddExistingObjectsDialog(view, n);
		dialog.addPropertyChangeListener(controller);
		UIUtilities.centerAndShow(dialog);  
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#addExistingObjects(Set)
	 */
	public void addExistingObjects(Set set)
	{
		if (model.getState() != LOADING_SELECTION)
			throw new IllegalStateException(
					"This method cannot be invoked in the LOADING_DATA state.");
		view.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (set == null || set.size() == 0) model.setState(READY);
		else model.fireAddExistingObjects(set);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#showMenu(int, Component, Point)
	 */
	public void showMenu(int menuID, Component c, Point p)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		switch (menuID) {
		case MANAGER_MENU:
		case CLASSIFIER_MENU:  
			break;
		default:
			throw new IllegalArgumentException("Menu not supported.");
		}
		view.showMenu(menuID, c, p);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setStatus(boolean, String, boolean)
	 */
	public void setStatus(boolean enable, String text, boolean hide)
	{
		view.setStatus(text, hide);
		view.setStatusIcon(enable);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#onComponentStateChange(boolean)
	 */
	public void onComponentStateChange(boolean b)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		Browser browser = model.getSelectedBrowser();
		if (browser != null) browser.onComponentStateChange(b);
		Boolean oldValue = Boolean.TRUE;
		if (b) oldValue = Boolean.FALSE;
		view.onStateChanged(b);
		firePropertyChange(ON_COMPONENT_STATE_CHANGED_PROPERTY, oldValue, 
				new Boolean(b));
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setNodesToCopy(TreeImageDisplay[], int)
	 */
	public void setNodesToCopy(TreeImageDisplay[] nodes, int index)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (nodes == null || nodes.length == 0) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Copy action", "You first need to select " +
			"the nodes to copy."); 
			return;
		}
		switch (index) {
		case CUT_AND_PASTE:
		case COPY_AND_PASTE:    
			break;

		default:
			throw new IllegalArgumentException("Index not supported.");
		}
		model.setNodesToCopy(nodes, index);
		//controller.getAction(TreeViewerControl.PASTE_OBJECT).setEnabled(true);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#paste(TreeImageDisplay[])
	 */
	public void paste(TreeImageDisplay[] parents)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
		if (parents == null || parents.length == 0) {
			un.notifyInfo("Paste action", "You first need to select " +
			"the nodes to copy into"); 
		}
		TreeImageDisplay[] nodes = model.getNodesToCopy();
		if (nodes == null || nodes.length == 0) return; //shouldn't happen
		boolean b = model.paste(parents);
		if (!b) {
			un.notifyInfo("Paste action", "The nodes to copy cannot " +
			"be added to the selected nodes."); 
		} else fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#saveInEditor(boolean)
	 */
	public void saveInEditor(boolean b)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (b) {
			model.getEditor().saveData();
			model.setEditor(null);
			//onSelectedDisplay();
		} else {
			if (saverDialog != null) {
				saverDialog.close();
				saverDialog = null;
			}
			model.setEditor(null);
			removeEditor();
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getUI()
	 */
	public JFrame getUI()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
			"in the DISCARDED state.");
		return view;
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getEditorType()
	 */
	public int getEditorType()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
			"in the DISCARDED state.");
		return model.getEditorType();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#annotate(Class, Set)
	 */
	public void annotate(Class klass, Set<DataObject> nodes)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
			"in the DISCARDED state.");
		if (nodes == null)
			throw new IllegalArgumentException("No dataObject to annotate");
		if (nodes.size() == 1) {
			PropertiesCmd cmd = new PropertiesCmd(this);
			cmd.execute();
			return;
		}
		if (ImageData.class.equals(klass) || DatasetData.class.equals(klass)) {
			DataHandler dh = model.annotateDataObjects(view, klass, nodes);
			dh.addPropertyChangeListener(controller);
			dh.activate();
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#hasDataToSave()
	 */
	public boolean hasDataToSave()
	{
		Editor editor = model.getEditor();
		if (editor == null) return false;
		return editor.hasDataToSave();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#showPreSavingDialog()
	 */
	public void showPreSavingDialog()
	{
		Editor editor = model.getEditor();
		if (editor == null) return;
		if (!(editor.hasDataToSave())) return;
		IconManager icons = IconManager.getInstance();
		saverDialog  = new EditorSaverDialog(view, 
				icons.getIcon(IconManager.QUESTION));
		saverDialog.addPropertyChangeListener(
				EditorSaverDialog.SAVING_DATA_EDITOR_PROPERTY, 
				controller);
		UIUtilities.centerAndShow(saverDialog);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getUserGroupID()
	 */
	public long getUserGroupID()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		return model.getUserGroupID();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setAvailableGroups(Map)
	 */
	public void setAvailableGroups(Map map)
	{
		if (model.getState() != LOADING_DATA) return;
		model.setUserGroups(map);
		displayUserGroups(map);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#retrieveUserGroups()
	 */
	public void retrieveUserGroups()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		Map m = model.getAvailableUserGroups();
		if (m == null) {
			model.fireUserGroupsRetrieval();
			fireStateChange();
		} else displayUserGroups(m);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getExperimenterNames()
	 */
	public String getExperimenterNames()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		return model.getExperimenterNames();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#getSelectedExperimenter()
	 */
	public ExperimenterData getSelectedExperimenter()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		Browser b = model.getSelectedBrowser();
		ExperimenterData exp = model.getExperimenter();
		if (b != null) {
			TreeImageDisplay node = b.getLastSelectedDisplay();
			if (node != null) exp = b.getNodeOwner(node);
		}
		return exp;
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#annotateChildren(Class, Set)
	 */
	public void annotateChildren(Class klass, Set<DataObject> nodes)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (nodes == null || nodes.size() == 0)
			throw new IllegalArgumentException("No specified container.");
		if (DatasetData.class.equals(klass) ||
				CategoryData.class.equals(klass)) {
			DataHandler dh = model.annotateChildren(view, klass, nodes);
			dh.addPropertyChangeListener(controller);
			dh.activate();
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#classifyChildren(Class, Set)
	 */
	public void classifyChildren(Class klass, Set<DataObject> nodes)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		if (nodes == null || nodes.size() == 0)
			throw new IllegalArgumentException("No specified container.");
		if (DatasetData.class.equals(klass) ||
				CategoryData.class.equals(klass)) {
			DataHandler dh = model.classifyChildren(view, nodes);
			dh.addPropertyChangeListener(controller);
			dh.activate();
		}

	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#isRecycled()
	 */
	public boolean isRecycled() 
	{ 
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		return model.isRecycled(); 
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#isRollOver()
	 */
	public boolean isRollOver()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		return model.isRollOver();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#setRollOver(boolean)
	 */
	public void setRollOver(boolean rollOver)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		model.setRollOver(rollOver);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#isReadable(DataObject)
	 */
	public boolean isReadable(DataObject ho)
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException(
					"This method cannot be invoked in the DISCARDED state.");
		//Check if current user can write in object
		long id = model.getUserDetails().getId();
		long groupId = model.getUserGroupID();
		return TreeViewerTranslator.isReadable(ho, id, groupId);
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#removeExperimenterData()
	 */
	public void removeExperimenterData()
	{
		//TODO: Check state

		Browser browser = model.getSelectedBrowser();
		TreeImageDisplay expNode = browser.getLastSelectedDisplay();
		Object uo = expNode.getUserObject();
		if (uo == null || !(uo instanceof ExperimenterData)) return;
		ExperimenterData exp = (ExperimenterData) uo;
		Map browsers = model.getBrowsers();
		Iterator i = browsers.keySet().iterator();
		while (i.hasNext()) {
			browser = (Browser) browsers.get(i.next());
			browser.removeExperimenter(exp);
		}
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#hasRndSettings()
	 */
	public boolean hasRndSettings()
	{
		if (model.getState() == DISCARDED)
			throw new IllegalStateException("This method cannot be invoked " +
			"in the DISCARDED state.");
		return model.hasRndSettingsToPaste();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#pasteRndSettings(Set, Class)
	 */
	public void pasteRndSettings(Set<Long> ids, Class klass)
	{
		//TODO Check state.
		if (!hasRndSettings()) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Paste settings", "No rendering settings to" +
			"paste. Please first copy settings.");
			return;
		}
		if (ids == null || ids.size() == 0) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Paste settings", "Please select the nodes" +
			"you wish to apply the settings to.");
			return;
		}
		model.firePasteRenderingSettings(ids, klass);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#pasteRndSettings(TimeRefObject)
	 */
	public void pasteRndSettings(TimeRefObject ref)
	{
		//TODO Check state.
		if (!hasRndSettings()) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Paste settings", "No rendering settings to" +
			"paste. Please first copy settings.");
			return;
		}
		if (ref == null) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Paste settings", "Please select the nodes" +
			"you wish to apply the settings to.");
			return;
		}
		model.firePasteRenderingSettings(ref);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#rndSettingsPasted(Map)
	 */
	public void rndSettingsPasted(Map map)
	{
		if (map == null || map.size() != 2) return;
		Collection failure = (Collection) map.get(Boolean.FALSE);
		UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
		if (failure.size() == 0) {
			un.notifyInfo("Paste settings", "Rendering settings have been " +
			"applied to all selected images.");
		} else {
			String s = "";
			Iterator i = failure.iterator();
			int index = 1;
			int size = failure.size();
			while (i.hasNext()) {
				s += (Long) i.next();
				if (index != size) {
					if (index%10 == 0) s += "\n";
					else s += ", ";
				}
				index++;
			}
			s.trim();
			un.notifyInfo("Paste settings", "Rendering settings couldn't be " +
					"applied to the following images: \n"+s);
		}
		model.setState(READY);
		fireStateChange();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#annotate(TimeRefObject)
	 */
	public void annotate(TimeRefObject ref)
	{
		if (ref == null)
			throw new IllegalArgumentException("No time object");
		//TODO: check state
		DataHandler dh = model.annotateDataObjects(view, ref);
		dh.addPropertyChangeListener(controller);
		dh.activate();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#classify(TimeRefObject)
	 */
	public void classify(TimeRefObject ref)
	{
		if (ref == null)
			throw new IllegalArgumentException("No time object");
		//TODO: check state
		DataHandler dh = model.classifyDataObjects(view, ref);
		dh.addPropertyChangeListener(controller);
		dh.activate();
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#resetRndSettings(Set, Class)
	 */
	public void resetRndSettings(Set<Long> ids, Class klass)
	{
		if (ids == null || ids.size() == 0) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Reset settings", "Please select the nodes" +
			"you wish to reset the rendering settings.");
			return;
		}
		model.fireResetRenderingSettings(ids, klass);
		fireStateChange();
		
	}

	/**
	 * Implemented as specified by the {@link TreeViewer} interface.
	 * @see TreeViewer#resetRndSettings(TimeRefObject)
	 */
	public void resetRndSettings(TimeRefObject ref)
	{
		if (ref == null) {
			UserNotifier un = TreeViewerAgent.getRegistry().getUserNotifier();
			un.notifyInfo("Reset settings", "Please select the nodes" +
			"you wish to reset the rendering settings.");
			return;
		}
		model.fireResetRenderingSettings(ref);
		fireStateChange();
		
	}
	
}

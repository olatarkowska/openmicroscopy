/*
 * org.openmicroscopy.shoola.agents.rnd.model.WavelengthPane
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.agents.rnd.editor;

//Java imports
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.Border;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.rnd.model.WavelengthData;
import org.openmicroscopy.shoola.util.ui.TableComponent;
import org.openmicroscopy.shoola.util.ui.TableComponentCellEditor;
import org.openmicroscopy.shoola.util.ui.TableComponentCellRenderer;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class WavelengthPane
	extends JPanel
{
	public static final Dimension	DIM_SCROLL_TABLE = new Dimension(40, 60);
	
	private WavelengthEditorManager			manager;
	private JTextArea						interpretationArea;
	private JButton							saveButton;
	
	WavelengthPane(WavelengthEditorManager manager)
	{
		this.manager = manager;
		buildGUI();
	}

	JTextArea getInterpretationArea()
	{
		return interpretationArea;
	}
	
	JButton getSaveButton()
	{
		return saveButton;
	}
	
	/** Build and layout the GUI. */
	private void buildGUI()
	{
		setLayout(new GridLayout(1, 1));
		add(buildSummaryPanel());
		Border b = BorderFactory.createEmptyBorder(0, 0, 10, 10);
		setBorder(b);
	}

	/** Build the panel with info. */
	private JPanel buildSummaryPanel()
	{
		JPanel  p = new JPanel();
		//save button
		saveButton = new JButton("Save");
		saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		//make panel transparent
		saveButton.setOpaque(false);
		//suppress button press decoration
		saveButton.setContentAreaFilled(false); 
		saveButton.setToolTipText("Save data to the DB");
		saveButton.setEnabled(false);

		JPanel controls = new JPanel(), all = new JPanel();
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		all.setLayout(gridbag);  
		controls.setLayout(new BoxLayout(controls, BoxLayout.X_AXIS));
		controls.add(saveButton);
		controls.setOpaque(false); //make panel transparent
		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.EAST;
		gridbag.setConstraints(controls,c); 
		all.add(controls);
		all.setOpaque(false); //make panel transparent
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.add(buildTable());
		//TODO: add save button if needed.
		//p.add(all);
		//make panel transparent
		p.setOpaque(false);
		
		return p;
	}
	
	/** 
	* A <code>2x2</code> table model to view wavelength summary.
	* The first column contains the property names (id, name, description)
	* and the second column holds the corresponding values. 
	* <code>name</code> and <code>description</code> values
	* are marked as editable. 
	*/
	private JTable buildTable()
	{
		JTable table = new TableComponent(2, 2);
		table.setTableHeader(null);
		
		// Labels
		table.setValueAt(new JLabel(" Emission"), 0, 0);
		table.setValueAt(new JLabel(" Interpretation"), 1, 0);

		WavelengthData wd = manager.getWavelengthData();
		table.setValueAt(new JLabel(""+wd.nanometer), 0, 0);
	
		//textfields
		interpretationArea = new JTextArea(wd.info);
		interpretationArea.setForeground(WavelengthEditor.STEELBLUE);
		//TODO: setEditable to true.
		//interpretationArea.setEditable(true);
		interpretationArea.setLineWrap(true);
		interpretationArea.setWrapStyleWord(true);
		JScrollPane scrollPane  = new JScrollPane(interpretationArea);
		scrollPane.setPreferredSize(DIM_SCROLL_TABLE);
		table.setValueAt(scrollPane, 1, 1);

		table.setDefaultRenderer(JComponent.class, 
								new TableComponentCellRenderer());
		table.setDefaultEditor(JComponent.class, 
								new TableComponentCellEditor());
								
		return table;
	}
	
}

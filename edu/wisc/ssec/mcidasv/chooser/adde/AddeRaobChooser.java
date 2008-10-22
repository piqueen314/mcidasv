/*
 * $Id$
 *
 * Copyright 2007-2008
 * Space Science and Engineering Center (SSEC)
 * University of Wisconsin - Madison,
 * 1225 W. Dayton Street, Madison, WI 53706, USA
 *
 * http://www.ssec.wisc.edu/mcidas
 *
 * This file is part of McIDAS-V.
 * 
 * McIDAS-V is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * McIDAS-V is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with this program.  If not, see http://www.gnu.org/licenses
 */

package edu.wisc.ssec.mcidasv.chooser.adde;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.w3c.dom.Element;

import ucar.unidata.data.sounding.RaobDataSet;
import ucar.unidata.idv.chooser.IdvChooserManager;
import ucar.unidata.idv.chooser.adde.AddeServer;
import ucar.unidata.util.GuiUtils;
import ucar.unidata.util.LogUtil;
import ucar.unidata.xml.XmlUtil;

import edu.wisc.ssec.mcidasv.chooser.SoundingSelector;




/**
 * A chooser class for selecting Raob data.
 * Mostly just a wrapper around a
 *  {@link ucar.unidata.view.sounding.SoundingSelector}
 * that does most of the work
 *
 * @author IDV development team
 * @version $Revision$Date: 2008/10/08 20:26:28 $
 */


public class AddeRaobChooser extends AddeChooser {

    /** The data source id (from datasources.xml) that we create */
    public static final String DATASOURCE_RAOB = "RAOB";

    /**
     * An xml attribute to show or not show the server selector
     * in the gui
     */
    public static final String ATTR_SHOWSERVER = "showserver";


    /** Does most of the work */
    private SoundingSelector soundingChooser;

    /** Accounting information */
    protected static String user = "idv";
    protected static String proj = "0";

    protected boolean firstTime = true;
    protected boolean retry = true;

    /** Not connected */
//    protected static final int STATE_UNCONNECTED = 0;

    /** The xml node from choosers.xml that defines this chooser */
    private static Element myChooserNode;

    /**
     * Construct a <code>RaobChooser</code> using the manager
     * and the root XML that defines this object.
     *
     * @param mgr  <code>IdvChooserManager</code> that controls this chooser.
     * @param root root element of the XML that defines this object
     */
    public AddeRaobChooser(IdvChooserManager mgr, Element chooserNode) {
        super(mgr, chooserNode);
        myChooserNode = chooserNode;
    }

    /**
     * Handle actions.
     *
     * @param e  ActionEvent to handle.
     */
    public void actionPerformed(ActionEvent e) {
    	System.out.println(e.getActionCommand());
    	super.actionPerformed(e);
        String cmd = e.getActionCommand();
        if (cmd.equals(CMD_CONNECT)) {
            soundingChooser.doUpdate(false);
            //            servers.saveState(serverSelector);
        }
    }
    
    /**
     * Handle when the user presses the update button
     *
     * @throws Exception On badness
     */
    public void handleUpdate() throws Exception {
    	soundingChooser.doUpdate();
    }

    /**
     * Is the group selector editable?  Override if ya want.
     * @return
     */
    protected boolean isGroupEditable() {
    	return false;
    }

    /**
     * get default display to create
     *
     * @return default display
     */
    protected String getDefaultDisplayType() {
        return "raob_skewt";
    }
    
    /**
     * This allows derived classes to provide their own name for labeling, etc.
     *
     * @return  the dataset name
     */
    public String getDataName() {
        return "Sounding Data";
    }

    /**
     * Get the descriptor widget label
     *
     * @return  label for the descriptor  widget
     */
    public String getDescriptorLabel() {
        return "Point Type";
    }
    
    /**
     * Get the data type for this chooser
     *
     * @return the data type
     */
    public String getDataType() {
        return "POINT";
    }

    /**
     * get the adde server grup type to use
     *
     * @return group type
     */
    @Override protected String getGroupType() {
        return AddeServer.TYPE_POINT;
    }
    
    /**
     * Load the data source in a thread
     */
    public void doLoadInThread() {
        List soundings = soundingChooser.getSelectedSoundings();
        if (soundings.size() == 0) {
            userMessage("Please select one or more soundings.");
            return;
        }
        Hashtable ht = new Hashtable();
        getDataSourceProperties(ht);

        makeDataSource(new RaobDataSet(soundingChooser.getSoundingAdapter(),
                                       soundings), DATASOURCE_RAOB, ht);
        soundingChooser.getAddeChooser().saveServerState();
    }

    /**
     * Show the given error to the user. If it was an Adde exception
     * that was a bad server error then print out a nice message.
     *
     * @param excp The exception
     */
    protected void handleConnectionError(Exception excp) {
        String aes = excp.toString();
        if ((aes.indexOf("Accounting data")) >= 0) {
            JTextField projFld   = null;
            JTextField userFld   = null;
            JComponent contents  = null;
            JLabel     label     = null;
            AddeChooser addeChooser = soundingChooser.getAddeChooser();
            if (firstTime == false) {
                retry = false;
            } else {
                if (projFld == null) {
                    projFld            = new JTextField("", 10);
                    userFld            = new JTextField("", 10);
                    GuiUtils.tmpInsets = GuiUtils.INSETS_5;
                    contents = GuiUtils.doLayout(new Component[] {
                        GuiUtils.rLabel("User ID:"),
                        userFld, GuiUtils.rLabel("Project #:"), projFld, }, 2,
                            GuiUtils.WT_N, GuiUtils.WT_N);
                    label    = new JLabel(" ");
                    contents = GuiUtils.topCenter(label, contents);
                    contents = GuiUtils.inset(contents, 5);
                }
                String lbl = (firstTime
                              ? "The server: " + addeChooser.getServer()
                                + " requires a user ID & project number for access"
                              : "Authentication for server: " + addeChooser.getServer()
                                + " failed. Please try again");
                label.setText(lbl);

                if ( !GuiUtils.showOkCancelDialog(null, "ADDE Project/User name",
                        contents, null)) {
                    //setState(STATE_UNCONNECTED);
                    return;
                }
                user = userFld.getText().trim();
                proj  = projFld.getText().trim();
            }
            if (firstTime == true) {
                firstTime = false;
                try {
                    addeChooser.handleUpdate();
                } catch (Exception e) {
                    System.out.println("handleUpdate exception e=" + e);
                }
            }
            return;
        }
        String message = excp.getMessage().toLowerCase();
        if (message.indexOf("with position 0") >= 0) {
            LogUtil.userErrorMessage("Unable to handle archive dataset");
            return;
        }
        //super.handleConnectionError(excp);
    }

    /**
     * Make the UI for this selector.
     * 
     * @return The gui
     */   
    public JComponent doMakeContents() {
//        boolean showServer = XmlUtil.getAttribute(myChooserNode, ATTR_SHOWSERVER, true);
        soundingChooser = new SoundingSelector(this, getPreferenceList(PREF_ADDESERVERS), false, true) {
            public void doLoad() {
                AddeRaobChooser.this.doLoad();
            }

            public void doCancel() {
                //                closeChooser();
            }

        };        
        setInnerPanel(soundingChooser.getSoundingPanel());
        return super.doMakeContents();
    }
    
}
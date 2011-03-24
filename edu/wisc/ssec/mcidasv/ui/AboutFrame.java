/*
 * $Id$
 *
 * This file is part of McIDAS-V
 *
 * Copyright 2007-2011
 * Space Science and Engineering Center (SSEC)
 * University of Wisconsin - Madison
 * 1225 W. Dayton Street, Madison, WI 53706, USA
 * http://www.ssec.wisc.edu/mcidas
 * 
 * All Rights Reserved
 * 
 * McIDAS-V is built on Unidata's IDV and SSEC's VisAD libraries, and
 * some McIDAS-V source code is based on IDV and VisAD source code.  
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
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

package edu.wisc.ssec.mcidasv.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;

import ucar.unidata.util.GuiUtils;

import edu.wisc.ssec.mcidasv.Constants;
import edu.wisc.ssec.mcidasv.McIDASV;
import edu.wisc.ssec.mcidasv.StateManager;
import edu.wisc.ssec.mcidasv.util.SystemState;

public class AboutFrame extends javax.swing.JFrame {

    private final McIDASV mcv;

    /** Creates new form AboutFrame */
    public AboutFrame(final McIDASV mcv) {
        this.mcv = mcv;
        initComponents();
    }

    private javax.swing.JPanel buildAboutMcv() {
        StateManager stateManager = (StateManager)mcv.getStateManager();

        javax.swing.JEditorPane editor = new javax.swing.JEditorPane();
        editor.setEditable(false);
        editor.setContentType("text/html");
        String html = stateManager.getMcIdasVersionAbout();
        editor.setText(html);
        editor.setBackground(new javax.swing.JPanel().getBackground());
        editor.addHyperlinkListener(mcv);

        final javax.swing.JLabel iconLbl = new javax.swing.JLabel(
            GuiUtils.getImageIcon(mcv.getProperty(Constants.PROP_SPLASHICON, ""))
        );
        iconLbl.setToolTipText("McIDAS-V homepage");
        iconLbl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        iconLbl.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                javax.swing.event.HyperlinkEvent link = null;
                try {
                    link = new javax.swing.event.HyperlinkEvent(
                        iconLbl,
                        javax.swing.event.HyperlinkEvent.EventType.ACTIVATED,
                        new URL(mcv.getProperty(Constants.PROP_HOMEPAGE, ""))
                    );
                } catch (MalformedURLException e) {}
                mcv.hyperlinkUpdate(link);
            }
        });
        javax.swing.JPanel contents = GuiUtils.topCenter(
            GuiUtils.inset(iconLbl, 5),
            GuiUtils.inset(editor, 5)
        );
        contents.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED,
                Color.gray, Color.gray));
        return contents;
    }

    private String getSystemInformation() {
//        return new SystemState(mcv).getStateAsString(true);
        return SystemState.getStateAsString(mcv, true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        tabbedPanel = new javax.swing.JTabbedPane();
        mcvTab = new javax.swing.JPanel();
        mcvPanel = buildAboutMcv();
        sysTab = new javax.swing.JPanel();
        sysScrollPane = new javax.swing.JScrollPane();
        sysTextArea = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("About McIDAS-V");

        javax.swing.GroupLayout mcvTabLayout = new javax.swing.GroupLayout(mcvTab);
        mcvTab.setLayout(mcvTabLayout);
        mcvTabLayout.setHorizontalGroup(
            mcvTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//            .addComponent(mcvPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mcvPanel)
        );
        mcvTabLayout.setVerticalGroup(
            mcvTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//            .addComponent(mcvPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(mcvPanel)
        );

        tabbedPanel.addTab("McIDAS-V", mcvTab);
        sysTextArea.setText(getSystemInformation());
        sysTextArea.setEditable(false);
        sysTextArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, 0, 12)); // NOI18N
        sysTextArea.setCaretPosition(0);
        sysTextArea.setLineWrap(false);
        sysScrollPane.setViewportView(sysTextArea);

        javax.swing.GroupLayout sysTabLayout = new javax.swing.GroupLayout(sysTab);
        sysTab.setLayout(sysTabLayout);
        sysTabLayout.setHorizontalGroup(
            sysTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sysTabLayout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(sysScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
//                .addContainerGap())
                .addComponent(sysScrollPane))
        );
        sysTabLayout.setVerticalGroup(
            sysTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sysTabLayout.createSequentialGroup()
//                .addComponent(sysScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
//                .addContainerGap())
                .addComponent(sysScrollPane))
        );

        tabbedPanel.addTab("System Information", sysTab);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//            .addComponent(tabbedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 511, Short.MAX_VALUE)
            .addComponent(tabbedPanel)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
//            .addComponent(tabbedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
            .addComponent(tabbedPanel)
        );

        pack();
        setSize(450, 375);
        setLocationRelativeTo(mcv.getIdvUIManager().getFrame());
    }// </editor-fold>

    // Variables declaration - do not modify
    private javax.swing.JPanel mcvPanel;
    private javax.swing.JPanel mcvTab;
    private javax.swing.JScrollPane sysScrollPane;
    private javax.swing.JPanel sysTab;
    private javax.swing.JTextArea sysTextArea;
    private javax.swing.JTabbedPane tabbedPanel;
    // End of variables declaration

}

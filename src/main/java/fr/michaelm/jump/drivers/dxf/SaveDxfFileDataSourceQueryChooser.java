/*
 * Library name : dxf
 * (C) 2021 Michaël Michaud
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * For more information, contact:
 *
 * m.michael.michaud@orange.fr
 *
 */

package fr.michaelm.jump.drivers.dxf;

import java.awt.Component;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.datasource.SaveFileDataSourceQueryChooser;
import com.vividsolutions.jump.workbench.model.*;
import com.vividsolutions.jump.workbench.WorkbenchContext;


/**
 * User interface to save a JUMP layer into a DXF file
 * Add an option to the standard panel
 * - option to create "_" suffixed layers for holes in polygon
 * @author Michaël Michaud
 */
// History
// 2006-11-12 : remove the header option after L. Becker and R. Littlefield
//              have fix the bug in the header writing
// 2006-10-18 : add two options (one for header writing and the other to suffix
//              layers containing holes) and a function to create valid DXF
//              layer names from JUMP layer names
public class SaveDxfFileDataSourceQueryChooser extends SaveFileDataSourceQueryChooser {
    
    // Array making it possible to replace any of the 383 first unicode
    // characters by a valid character for DXF layer name or file name
    // (removes accents, escape characters and most of special symbols)
    private static final String[] asciiChar = new String[] {
        "","","","","","","","","","","","","","","","",                 //00-0F
        "","","","","","","","","","","","","","","","",                 //10-1F
        "_","_","_","_","_","_","_","_","_","_","_","_","_","-",".","_", //20-2F
        "0","1","2","3","4","5","6","7","8","9","_","_","_","_","_","_", //30-3F
        "_","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O", //40-4F
        "P","Q","R","S","T","U","V","W","X","Y","Z","_","_","_","_","_", //50-5F
        "_","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o", //60-6F
        "p","q","r","s","t","u","v","w","x","y","z","_","_","_","_","",  //70-7F
        "","","","","","","","","","","","","","","","",                 //80-8F
        "","","","","","","","","","","","","","","","",                 //90-9F
        "_","_","c","L","_","Y","_","_","","c","a","","_","-","r","",    //A0-AF
        "o","_","2","3","","u","_",".","","1","o","","_","_","_","_",    //B0-BF
        "A","A","A","A","A","A","AE","C","E","E","E","E","I","I","I","I",//C0-CF
        "E","N","O","O","O","O","O","x","O","U","U","U","U","Y","T","SS",//D0-DF
        "a","a","a","a","a","a","ae","c","e","e","e","e","i","i","i","i",//E0-EF
        "e","n","o","o","o","o","o","_","0","u","u","u","u","y","t","y", //F0-FF
        "A","a","A","a","A","a","C","c","C","c","C","c","C","c","D","d", //100-10F
        "D","d","E","e","E","e","E","e","E","e","E","e","G","g","G","g", //110-11F
        "G","g","G","g","H","h","H","h","I","i","I","i","I","i","I","i", //120-12F
        "I","i","IJ","ij","J","j","K","k","k","L","l","L","l","L","l","L", //130-13F
        "l","L","l","N","n","N","n","N","n","n","N","n","O","o","O","o", //140-14F
        "O","o","OE","oe","R","r","R","r","R","r","S","s","S","s","S","s", //150-15F
        "S","s","T","t","T","t","T","t","U","u","U","u","U","u","U","u", //160-16F
        "U","u","U","u","W","w","Y","y","Y","Z","z","Z","z","Z","z","_"  //170-17F
    };

    WorkbenchContext context;
    JPanel optionPanel = new JPanel();
    JCheckBox suffixCB = new JCheckBox("Put polygon holes in layers with a '_' suffix", true);
    
    SaveDxfFileDataSourceQueryChooser(Class readerWriterDataSourceClass, String description,
        String[] extensions, WorkbenchContext workbenchContext) {
        super(readerWriterDataSourceClass, description, extensions, workbenchContext);
        this.context = workbenchContext;
        Box box = new Box(BoxLayout.Y_AXIS);
        optionPanel.add(box);
        //box.add(headerCB);
        box.add(suffixCB);
    }
    
    protected Map<String,Object> toProperties(File file) {
        Map<String,Object> properties = new HashMap<>(super.toProperties(file));
        Layer selectedLayer = context.getLayerableNamePanel().getSelectedLayers()[0];
        String layerName = toAscii(selectedLayer.getName()).substring(0, Math.min(selectedLayer.getName().length(), 31));
        properties.put("LAYER_NAME", layerName);
        // If the layer schema has an attribute "LAYER" the value of this
        // attribute is used for the DXF layer name
        if (selectedLayer instanceof Layer) {
            FeatureSchema fs = selectedLayer.getFeatureCollectionWrapper().getFeatureSchema();
            if (fs.hasAttribute("LAYER")) {
                Set<String> layerSet = new HashSet<>();
                List<Feature> features = selectedLayer.getFeatureCollectionWrapper().getFeatures();
                for (Feature feature : features) {
                    if (feature.getString("LAYER") != null &&
                        feature.getString("LAYER").trim().length()>0 &&
                        !feature.getString("LAYER").endsWith("_")) {
                        layerSet.add(feature.getString("LAYER"));
                    }
                }
                String[] set = layerSet.toArray(new String[0]);
                StringBuilder sb = new StringBuilder();
                for (int i = 0 ; i < set.length ; i++) {
                    if (i > 0) sb.append("\n");
                    sb.append(set[i]);
                }
                properties.put("LAYER_NAME", sb.toString());
            }
        }
        properties.put("SUFFIX", suffixCB.isSelected());
        return properties;
    }

    protected Component getSouthComponent1() {
        return optionPanel;
    }
    
   /**
    * Remove accents using the asciiChar array.
    */
    public static String toAscii(String in) {
        String input = in.trim();
        StringBuilder output = new StringBuilder();
        for(int i = 0 ; i < input.length() ; i++) {
	        int carVal = input.charAt(i);
            if (carVal < 384) {output.append(asciiChar[carVal]);}
            else {output.append("_");}
        }
        return output.toString();
    }

}

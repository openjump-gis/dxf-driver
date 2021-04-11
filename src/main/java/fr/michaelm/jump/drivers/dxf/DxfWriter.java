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

import com.vividsolutions.jump.feature.*;
import com.vividsolutions.jump.io.JUMPWriter;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.IllegalParametersException;

import java.io.*;


/**
 * DXF writer
 * @author Michaël Michaud
 */
// History
// 2006-11-12 : Much clean-up made on 2006-11-12 for version 0.5
public class DxfWriter implements JUMPWriter {
    
    //DxfFile dxfFile = null;

    /** Creates new DxfWriter */
    public DxfWriter() {}

    /**
     * Main method - write the featurecollection to a DXF file.
     *
     * @param featureCollection collection to write
     * @param dp 'OutputFile' or 'DefaultValue' to specify where to write.
     */
    public void write(FeatureCollection featureCollection, DriverProperties dp)
                                  throws Exception {
        String dxfFileName;
        String fname;
        int loc;
        dxfFileName = dp.getProperty("File");
        
        if (dxfFileName == null) {
            dxfFileName = dp.getProperty("DefaultValue");
        }
        if (dxfFileName == null) {
            throw new IllegalParametersException("no File property specified");
        }

        // Fix on 2016-09-29 to make it compatible with OpenJUMP 1.9.1
        //String[] layerNameProp = (String[])dp.get("LAYER_NAME");
        //String[] layerNames = layerNameProp == null ? new String[]{}:layerNameProp;
        String layerNameProperty = dp.getProperty("LAYER_NAME");
        String[] layerNames;
        if (layerNameProperty == null) layerNames = new String[]{""};
        else layerNames = layerNameProperty.split("\n");
        
        // Check if the writer has to create layers with "_" suffix for layers with holes
        // Warning : using getProperty instead of get return null
        // because SUFFIX is not a String
        boolean suffix = true;
        Object suffixObject = dp.get("SUFFIX");
        if (suffixObject != null) {
            if (suffixObject instanceof Boolean) suffix = (Boolean)suffixObject;
            else if (suffixObject instanceof String) suffix = Boolean.parseBoolean(suffixObject.toString());
        }
        
        loc = dxfFileName.lastIndexOf(File.separatorChar);
        fname = dxfFileName.substring(loc + 1); // ie. "/data1/hills.dxf" -> "hills.dxf"
        loc = fname.lastIndexOf(".");
        if (loc == -1) {
            throw new IllegalParametersException("Filename must end in '.dxf'");
        }

        FileWriter fw = new FileWriter(dxfFileName);
        DxfFile.write(featureCollection, layerNames, fw, 2, suffix);
        fw.close();
    }
    
}

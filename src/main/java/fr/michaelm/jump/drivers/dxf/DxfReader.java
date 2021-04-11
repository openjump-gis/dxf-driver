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
import com.vividsolutions.jump.io.JUMPReader;
import com.vividsolutions.jump.io.DriverProperties;
import com.vividsolutions.jump.io.IllegalParametersException;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;


/**
 * DXF reader.
 * Use the file name to read in the DriverProperties parameter, read the file
 * and return a FeatureCollection.
 * @author Michaël Michaud
 */
public class DxfReader implements JUMPReader {

    //private DxfFile dxfFile = null;

    /** Creates new DxfReader */
    public DxfReader() {
    }

    /**
     * Main method to read a DXF file. 
     * @param dp 'InputFile' or 'DefaultValue' to specify input .dxf file.
     *
     */
    public FeatureCollection read(DriverProperties dp) throws Exception {
        FeatureCollection result;
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

        loc = dxfFileName.lastIndexOf(File.separatorChar);
        fname = dxfFileName.substring(loc + 1);
        loc = fname.lastIndexOf(".");
        if (loc == -1) {
            throw new IllegalParametersException("Filename must end in '.dxf'");
        }

        //dxfFile = getDXFFile(dxfFileName, dp.getProperty("CompressedFile"));
        DxfFile dxfFile;
        GeometryFactory factory = new GeometryFactory();
        dxfFile = DxfFile.createFromFile(new File(dxfFileName));
        result = dxfFile.read(factory);
        System.gc();
        return result;
    }
    
    private Collection<Exception> exceptions;
    public Collection<Exception> getExceptions() {
        if (exceptions == null) exceptions = new ArrayList<>();
        return exceptions;
    }

}

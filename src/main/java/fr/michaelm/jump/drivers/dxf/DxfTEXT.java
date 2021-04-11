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

import java.io.RandomAccessFile;
import java.io.IOException;

import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.FeatureCollection;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;


/**
 * A TEXT and its static readEntity method to read a TEXT in a DXF file.
 * @author Michaël Michaud
 */
// History
// 2012-09-22 : Fix a bug preventing TEXT entities to be read
// 2011-04-10 : Add TEXT_ROTATION
// 2006-11-12 : Bug fixed x==Double.NaN --> Double.isNaN(x)
public class DxfTEXT extends DxfENTITY {

    public DxfTEXT() {super("DEFAULT");}

    public static DxfGroup readEntity(RandomAccessFile raf, 
                                      FeatureCollection entities) 
                                      throws IOException {
        Feature feature = new BasicFeature(DxfFile.DXF_SCHEMA);
        feature.setAttribute("LTYPE", "BYLAYER");
        feature.setAttribute("THICKNESS", 0.0);
        feature.setAttribute("COLOR", 256); // equivalent to BYLAYER
        feature.setAttribute("TEXT", "");
        feature.setAttribute("TEXT_HEIGHT", 0.0);
        feature.setAttribute("TEXT_ROTATION", 0.0);
        feature.setAttribute("TEXT_STYLE", "STANDARD");
        double x=Double.NaN, y=Double.NaN, z=Double.NaN;
        DxfGroup group;
        GeometryFactory gf = new GeometryFactory(DPM,0);
        while (null != (group = DxfGroup.readGroup(raf))) {
            int code = group.getCode();
            if (code == 0) break;
            if (DxfFile.DEBUG) group.print(12);
            if (code==8) feature.setAttribute("LAYER", group.getValue());
            else if (code==6) feature.setAttribute("LTYPE", group.getValue());
            else if (code==39) feature.setAttribute("THICKNESS", group.getDoubleValue());
            else if (code==62) feature.setAttribute("COLOR", group.getIntValue());
            else if (code==10) x = group.getDoubleValue();
            else if (code==20) y = group.getDoubleValue();
            else if (code==30) z = group.getDoubleValue();
            else if (code==1) feature.setAttribute("TEXT", group.getValue());
            else if (code==40) feature.setAttribute("TEXT_HEIGHT", group.getDoubleValue());
            else if (code==50) feature.setAttribute("TEXT_ROTATION", group.getDoubleValue());
            else if (code==7) feature.setAttribute("TEXT_STYLE", group.getValue());
            //else {}
        }
        if (!Double.isNaN(x) && !Double.isNaN(y)) {
            feature.setGeometry(gf.createPoint(new Coordinate(x,y,z)));
            if (DxfFile.DEBUG) System.out.println("        " + feature.getString("LAYER") + " : " + feature.getGeometry());
            entities.add(feature);
        }
        return group;
    }

}

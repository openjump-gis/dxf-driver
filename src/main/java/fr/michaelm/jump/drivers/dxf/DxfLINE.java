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
 * LINE DXF entity.
 * This class has a static method reading a DXF LINE and adding the new
 * feature to a FeatureCollection
 * @author Michaël Michaud
 */
public class DxfLINE extends DxfENTITY {

    public DxfLINE() {super("DEFAULT");}

    public static DxfGroup readEntity(RandomAccessFile raf, FeatureCollection entities)
                                                            throws IOException {
        Feature feature = new BasicFeature(DxfFile.DXF_SCHEMA);
        feature.setAttribute("LTYPE", "BYLAYER");
        feature.setAttribute("THICKNESS", 0.0);
        feature.setAttribute("COLOR", 256); // equivalent to BYLAYER
        double x1=Double.NaN, y1=Double.NaN, z1=Double.NaN;
        double x2=Double.NaN, y2=Double.NaN, z2=Double.NaN;
        DxfGroup group;
        while (null != (group = DxfGroup.readGroup(raf))) {
            int code = group.getCode();
            if (code==0) break;
            if (DxfFile.DEBUG) group.print(12);
            if (code==8) feature.setAttribute("LAYER", group.getValue());
            else if (code==6) feature.setAttribute("LTYPE", group.getValue());
            else if (code==39) feature.setAttribute("THICKNESS", group.getDoubleValue());
            else if (code==62) feature.setAttribute("COLOR", group.getIntValue());
            else if (code==10) x1 = group.getDoubleValue();
            else if (code==20) y1 = group.getDoubleValue();
            else if (code==30) z1 = group.getDoubleValue();
            else if (code==11) x2 = group.getDoubleValue();
            else if (code==21) y2 = group.getDoubleValue();
            else if (code==31) z2 = group.getDoubleValue();
            //else {}
        }
        if (!Double.isNaN(x1) && !Double.isNaN(y1) && !Double.isNaN(x2) && !Double.isNaN(y2)) {
            GeometryFactory gf = new GeometryFactory(DPM,0);
            feature.setGeometry(gf.createLineString(
                new Coordinate[]{new Coordinate(x1,y1,z1),new Coordinate(x2,y2,z2)})
            );
            if (DxfFile.DEBUG) System.out.println("        " + feature.getString("LAYER") + " : " + feature.getGeometry());
            entities.add(feature);
        }
        return group;
    }

}

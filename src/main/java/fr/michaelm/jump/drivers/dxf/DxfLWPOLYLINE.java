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
import org.locationtech.jts.geom.*;

/**
 * LWPOLYLINE DXF entity.
 * This class has a static method reading a DXF LWPOLYLINE and adding the new
 * feature to a FeatureCollection
 * @author Michaël Michaud
 */
public class DxfLWPOLYLINE extends DxfENTITY {

    public DxfLWPOLYLINE() {super("DEFAULT");}

    public static DxfGroup readEntity(RandomAccessFile raf, FeatureCollection entities)
                                                            throws IOException {
        Feature feature = new BasicFeature(entities.getFeatureSchema());
        GeometryFactory gf = new GeometryFactory(DPM,0);
        String geomType = "LineString";
        CoordinateList coordList = new CoordinateList();
        feature.setAttribute("LTYPE", "BYLAYER");
        feature.setAttribute("ELEVATION", 0.0);
        feature.setAttribute("THICKNESS", 0.0);
        feature.setAttribute("COLOR", 256); // equivalent to BYLAYER
        double x=Double.NaN;
        double y=Double.NaN;
        double z=Double.NaN;
        Coordinate coord = null;
        DxfGroup group = DxfFile.ENTITIES;
        int code;
        while (0 != (code = group.getCode())) {
            if (DxfFile.DEBUG) group.print(12);
            if (code==8) {
                feature.setAttribute("LAYER", group.getValue());
            }
            else if (code==6) {
                feature.setAttribute("LTYPE", group.getValue());
            }
            else if (code==38) {
                feature.setAttribute("ELEVATION", group.getDoubleValue());
                z = group.getDoubleValue();
            }
            else if (code==39) {
                feature.setAttribute("THICKNESS", group.getDoubleValue());
            }
            else if (code==62) {
                feature.setAttribute("COLOR", group.getIntValue());
            }
            else if (code==70) {
                if ((group.getIntValue()&1)==1) geomType = "Polygon";
            }
            else if (code==10) {
                coord = new Coordinate();
                coord.x = group.getDoubleValue();
            }
            else if (code==20) {
                if (coord != null) {
                    coord.y = group.getDoubleValue();
                    coordList.add(new Coordinate(x, y, z), true);
                }
            }
            //else {}
            group = DxfGroup.readGroup(raf);
        }
        if (geomType.equals("LineString")) {
            // Handle cases where coordList does not describe a valid Line
            if (coordList.size() == 1) {
                feature.setGeometry(gf.createPoint(coordList.getCoordinate(0)));
            }
            else if (coordList.size() == 2 &&
                     coordList.getCoordinate(0).equals(coordList.getCoordinate(1))) {
                feature.setGeometry(gf.createPoint(coordList.getCoordinate(0)));
            }
            else {
                feature.setGeometry(gf.createLineString(coordList.toCoordinateArray()));
            }
            if (DxfFile.DEBUG) System.out.println("        " + feature.getString("LAYER") + " : " + feature.getGeometry());
            entities.add(feature);
        }
        else if (geomType.equals("Polygon")) {
            coordList.closeRing();
            // Handle cases where coordList does not describe a valid Polygon
            if (coordList.size() == 1) {
                feature.setGeometry(gf.createPoint(coordList.getCoordinate(0)));
            }
            else if (coordList.size() == 2 &&
                     coordList.getCoordinate(0).equals(coordList.getCoordinate(1))) {
                feature.setGeometry(gf.createPoint(coordList.getCoordinate(0)));
            }
            else if (coordList.size() == 2 || coordList.size() == 3) {
                feature.setGeometry(gf.createLineString(coordList.toCoordinateArray()));
            }
            else {
                feature.setGeometry(gf.createPolygon(gf.createLinearRing(coordList.toCoordinateArray())));
            }
            if (DxfFile.DEBUG) System.out.println("        " + feature.getString("LAYER") + " : " + feature.getGeometry());
            entities.add(feature);
        }
        //else {}
        return group;
    }

}

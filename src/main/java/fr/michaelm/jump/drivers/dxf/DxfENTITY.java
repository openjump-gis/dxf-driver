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


import com.vividsolutions.jump.feature.Feature;
import org.locationtech.jts.geom.*;


/**
 * A DXF ENTITY is equivalent to a JUMP feature. This class is the parent class
 * for POLYLINE, POINT, LINE and every kind of geometric entity present in a
 * DXF file.
 * @author Michaël Michaud
 */
// History
// 2012-02-23 : fixed a bug in line export (it did always export z=0)
// 2006-10-19 : add multi-geometry export
//              add attribute tests an ability to export ANY jump layer
//              add ability to export holes in a separate layer or not
public class DxfENTITY {

    public final static DxfGroup LINE = new DxfGroup(0, "LINE");
    public final static DxfGroup POINT = new DxfGroup(0, "POINT");
    public final static DxfGroup CIRCLE = new DxfGroup(0, "CIRCLE");
    public final static DxfGroup ARC = new DxfGroup(0, "ARC");
    public final static DxfGroup TRACE = new DxfGroup(0, "TRACE");
    public final static DxfGroup SOLID = new DxfGroup(0, "SOLID");
    public final static DxfGroup TEXT = new DxfGroup(0, "TEXT");
    public final static DxfGroup SHAPE = new DxfGroup(0, "SHAPE");
    public final static DxfGroup BLOCK = new DxfGroup(0, "BLOCK");
    public final static DxfGroup ENDBLK = new DxfGroup(0, "ENDBLK");
    public final static DxfGroup INSERT = new DxfGroup(0, "INSERT");
    public final static DxfGroup ATTDEF = new DxfGroup(0, "ATTDEF");
    public final static DxfGroup ATTRIB = new DxfGroup(0, "ATTRIB");
    public final static DxfGroup POLYLINE = new DxfGroup(0, "POLYLINE");
    public final static DxfGroup LWPOLYLINE = new DxfGroup(0, "LWPOLYLINE");
    public final static DxfGroup VERTEX = new DxfGroup(0, "VERTEX");
    public final static DxfGroup SEQEND = new DxfGroup(0, "SEQEND");
    public final static DxfGroup _3DFACE = new DxfGroup(0, "3DFACE");
    public final static DxfGroup VIEWPORT = new DxfGroup(0, "VIEWPORT");
    public final static DxfGroup DIMENSION = new DxfGroup(0, "DIMENSION");
    public final static PrecisionModel DPM = new PrecisionModel();
    public static int precision = 4;

    private String layerName = "DEFAULT";

    public String getLayerName() {return layerName;}

    public void setLayerName(String layerName) {
        this.layerName = layerName;
    }

    public DxfENTITY(String layerName) {
        this.layerName = layerName;
    }

    public static String feature2Dxf(Feature feature, String layerName, boolean suffix) {
        Geometry g = feature.getGeometry();
        if (g.getGeometryType().equals("Point")) {
            return point2Dxf(feature, layerName);
        }
        else if (g.getGeometryType().equals("LineString")) {
            return lineString2Dxf(feature, layerName);
        }
        else if (g.getGeometryType().equals("Polygon")) {
            return polygon2Dxf(feature, layerName, suffix);
        }
        else if (g instanceof GeometryCollection) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0 ; i < g.getNumGeometries() ; i++) {
                Feature ff = feature.clone(true);
                ff.setGeometry(g.getGeometryN(i));
                sb.append(feature2Dxf(ff, layerName, suffix));
            }
            return sb.toString();
        }
        else {
            return null;
        }
    }

    public static String point2Dxf(Feature feature, String layerName) {
        StringBuilder sb;
        boolean hasText = (feature.getSchema().hasAttribute("TEXT") &&
                           feature.getAttribute("TEXT") != null);
        if (hasText) {sb = new StringBuilder(DxfGroup.toString(0, "TEXT"));}
        else {sb = new StringBuilder(DxfGroup.toString(0, "POINT"));}
        if (feature.getSchema().hasAttribute("LAYER") &&
            !feature.getString("LAYER").trim().equals("")) {
            sb.append(DxfGroup.toString(8, feature.getAttribute("LAYER")));
        }
        else {sb.append(DxfGroup.toString(8, layerName));}
        if (feature.getSchema().hasAttribute("LTYPE") &&
            !feature.getAttribute("LTYPE").equals("BYLAYER")) {
            sb.append(DxfGroup.toString(6, feature.getAttribute("LTYPE")));
        }
        //if (feature.getSchema().hasAttribute("ELEVATION") &&
        //    feature.getAttribute("ELEVATION") != null &&
        //    !feature.getAttribute("ELEVATION").equals(new Float(0f))) {
        //    sb.append(DxfGroup.toString(38, feature.getAttribute("ELEVATION")));
        //}
        if (feature.getSchema().hasAttribute("THICKNESS") &&
            feature.getAttribute("THICKNESS") != null &&
            !feature.getAttribute("THICKNESS").equals(0f)) {
            sb.append(DxfGroup.toString(39, feature.getAttribute("THICKNESS")));
        }
        if (feature.getSchema().hasAttribute("COLOR") &&
            feature.getAttribute("COLOR") != null &&
            (Integer)feature.getAttribute("COLOR") != 256) {
            sb.append(DxfGroup.toString(62, feature.getAttribute("COLOR").toString()));
        }
        Coordinate coord = feature.getGeometry().getCoordinate();
        sb.append(DxfGroup.toString(10, coord.x, precision));
        sb.append(DxfGroup.toString(20, coord.y, precision));
        if (!Double.isNaN(coord.z)) sb.append(DxfGroup.toString(30, coord.z, precision));
        if (hasText) {
            sb.append(DxfGroup.toString(1, feature.getAttribute("TEXT")));
        }
        if (hasText && feature.getSchema().hasAttribute("TEXT_HEIGHT") &&
            feature.getAttribute("TEXT_HEIGHT") != null) {
            sb.append(DxfGroup.toString(40, feature.getAttribute("TEXT_HEIGHT")));
        }
        if (hasText && feature.getSchema().hasAttribute("TEXT_ROTATION") &&
            feature.getAttribute("TEXT_ROTATION") != null) {
            sb.append(DxfGroup.toString(50, feature.getAttribute("TEXT_ROTATION")));
        }
        if (hasText && feature.getSchema().hasAttribute("TEXT_STYLE") &&
            feature.getAttribute("TEXT_STYLE") != null) {
            sb.append(DxfGroup.toString(7, feature.getAttribute("TEXT_STYLE")));
        }
        return sb.toString();
    }

    public static String lineString2Dxf(Feature feature, String layerName) {
        LineString geom = (LineString)feature.getGeometry();
        Coordinate[] coords = geom.getCoordinates();
        // Correction added by L. Becker and R Littlefield on 2006-11-08
        // It writes 2 points-only polylines in a line instead of a polyline
        // to make it possible to incorporate big dataset in View32
        boolean isLine = (coords.length == 2);
        StringBuilder sb;
        if (!isLine) {
        	sb = new StringBuilder(DxfGroup.toString(0, "POLYLINE"));
        }
        else {
            sb = new StringBuilder(DxfGroup.toString(0, "LINE"));
        }
        if (feature.getSchema().hasAttribute("LAYER") &&
            !feature.getString("LAYER").trim().equals("")) {
            sb.append(DxfGroup.toString(8, feature.getAttribute("LAYER")));
        }
        else {sb.append(DxfGroup.toString(8, layerName));}
        if (feature.getSchema().hasAttribute("LTYPE") &&
            !feature.getAttribute("LTYPE").equals("BYLAYER")) {
            sb.append(DxfGroup.toString(6, feature.getAttribute("LTYPE")));
        }
        if (feature.getSchema().hasAttribute("ELEVATION") &&
            feature.getAttribute("ELEVATION") != null) {
            sb.append(DxfGroup.toString(38, feature.getAttribute("ELEVATION")));
        }
        if (feature.getSchema().hasAttribute("THICKNESS") &&
            feature.getAttribute("THICKNESS") != null) {
            sb.append(DxfGroup.toString(39, feature.getAttribute("THICKNESS")));
        }
        if (feature.getSchema().hasAttribute("COLOR") &&
            feature.getAttribute("THICKNESS") != null) {
            sb.append(DxfGroup.toString(62, feature.getAttribute("COLOR").toString()));
        }
        // modified by L. Becker and R. Littlefield (add the Line case)
        if (isLine){
            sb.append(DxfGroup.toString(10, coords[0].x, precision));
            sb.append(DxfGroup.toString(20, coords[0].y, precision));
	        if (!Double.isNaN(coords[0].z)) {
	            sb.append(DxfGroup.toString(30, coords[0].z, precision));
	        }
            sb.append(DxfGroup.toString(11, coords[1].x, precision));
            sb.append(DxfGroup.toString(21, coords[1].y, precision));
	        if (!Double.isNaN(coords[1].z)) {
	            sb.append(DxfGroup.toString(31, coords[1].z, precision));
	        }
        }
        else {
            sb.append(DxfGroup.toString(66, 1));
            sb.append(DxfGroup.toString(10, "0.0"));
            sb.append(DxfGroup.toString(20, "0.0"));
            if (!Double.isNaN(coords[0].z)) sb.append(DxfGroup.toString(30, "0.0"));
            sb.append(DxfGroup.toString(70, 8));
            
            for (Coordinate coord : coords) {
                sb.append(DxfGroup.toString(0, "VERTEX"));
                if (feature.getSchema().hasAttribute("LAYER") &&
                    !feature.getString("LAYER").trim().equals("")) {
                    sb.append(DxfGroup.toString(8, feature.getAttribute("LAYER")));
                }
                else {sb.append(DxfGroup.toString(8, layerName));}
                sb.append(DxfGroup.toString(10, coord.x, precision));
                sb.append(DxfGroup.toString(20, coord.y, precision));
                if (!Double.isNaN(coord.z)) sb.append(DxfGroup.toString(30, coord.z, precision));
                sb.append(DxfGroup.toString(70, 32));
            }
            sb.append(DxfGroup.toString(0, "SEQEND"));
        }
        return sb.toString();
    }

    public static String polygon2Dxf(Feature feature, String layerName, boolean suffix) {
        Polygon geom = (Polygon)feature.getGeometry();
        Coordinate[] coords = geom.getExteriorRing().getCoordinates();
        StringBuilder sb = new StringBuilder(DxfGroup.toString(0, "POLYLINE"));
        sb.append(DxfGroup.toString(8, layerName));
        if (feature.getSchema().hasAttribute("LTYPE") &&
                feature.getAttribute("LTYPE") != null &&
                !feature.getAttribute("LTYPE").equals("BYLAYER")) {
            sb.append(DxfGroup.toString(6, feature.getAttribute("LTYPE")));
        }
        if (feature.getSchema().hasAttribute("ELEVATION") &&
            feature.getAttribute("ELEVATION") != null) {
            sb.append(DxfGroup.toString(38, feature.getAttribute("ELEVATION")));
        }
        if (feature.getSchema().hasAttribute("THICKNESS") &&
            feature.getAttribute("THICKNESS") != null) {
            sb.append(DxfGroup.toString(39, feature.getAttribute("THICKNESS")));
        }
        if (feature.getSchema().hasAttribute("COLOR") &&
            feature.getAttribute("COLOR") != null) {
            sb.append(DxfGroup.toString(62, feature.getAttribute("COLOR").toString()));
        }
        sb.append(DxfGroup.toString(66, 1));
        sb.append(DxfGroup.toString(10, "0.0"));
        sb.append(DxfGroup.toString(20, "0.0"));
        if (!Double.isNaN(coords[0].z)) sb.append(DxfGroup.toString(30, "0.0"));
        sb.append(DxfGroup.toString(70, 9));
        for (Coordinate coord : coords) {
            sb.append(DxfGroup.toString(0, "VERTEX"));
            sb.append(DxfGroup.toString(8, layerName));
            sb.append(DxfGroup.toString(10, coord.x, precision));
            sb.append(DxfGroup.toString(20, coord.y, precision));
            if (!Double.isNaN(coord.z)) sb.append(DxfGroup.toString(30, coord.z, precision));
            sb.append(DxfGroup.toString(70, 32));
        }
        sb.append(DxfGroup.toString(0, "SEQEND"));
        for (int h = 0 ; h < geom.getNumInteriorRing() ; h++) {
            //System.out.println("polygon2Dxf (hole)" + suffix);
            sb.append(DxfGroup.toString(0, "POLYLINE"));
            if (suffix) sb.append(DxfGroup.toString(8, layerName+"_"));
            else sb.append(DxfGroup.toString(8, layerName));
            if (feature.getSchema().hasAttribute("LTYPE") &&
                !feature.getAttribute("LTYPE").equals("BYLAYER")) {
                sb.append(DxfGroup.toString(6, feature.getAttribute("LTYPE")));
            }
            if (feature.getSchema().hasAttribute("THICKNESS") &&
                feature.getAttribute("THICKNESS") != null) {
                sb.append(DxfGroup.toString(39, feature.getAttribute("THICKNESS")));
            }
            if (feature.getSchema().hasAttribute("COLOR") &&
                feature.getAttribute("COLOR") != null) {
                sb.append(DxfGroup.toString(62, feature.getAttribute("COLOR")));
            }
            sb.append(DxfGroup.toString(66, 1));
            sb.append(DxfGroup.toString(10, "0.0"));
            sb.append(DxfGroup.toString(20, "0.0"));
            if (!Double.isNaN(coords[0].z)) sb.append(DxfGroup.toString(30, "0.0"));
            sb.append(DxfGroup.toString(70, 9));
            coords = geom.getInteriorRingN(h).getCoordinates();
            for (Coordinate coord : coords) {
                sb.append(DxfGroup.toString(0, "VERTEX"));
                if (suffix) sb.append(DxfGroup.toString(8, layerName+"_"));
                else sb.append(DxfGroup.toString(8, layerName));
                sb.append(DxfGroup.toString(10, coord.x, precision));
                sb.append(DxfGroup.toString(20, coord.y, precision));
                if (!Double.isNaN(coord.z)) sb.append(DxfGroup.toString(30, coord.z, precision));
                sb.append(DxfGroup.toString(70, 32));
            }
            sb.append(DxfGroup.toString(0, "SEQEND"));
        }
        
        return sb.toString();
    }

}

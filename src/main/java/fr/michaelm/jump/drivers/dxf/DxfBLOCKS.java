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
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureDataset;


/**
 * A DXF block contains a block of geometries. The dxf driver can read entities
 * inside a block, but it will not remember that the entities are in a same
 * block.
 * @author Michaël Michaud
 */
// History
public class DxfBLOCKS {

    FeatureCollection entities;

    public DxfBLOCKS() {
        entities = new FeatureDataset(DxfFile.DXF_SCHEMA);
    }

    public static DxfBLOCKS readBlocks(RandomAccessFile raf) 
                                     throws NumberFormatException, IOException {
        return readEntities(raf);
    }

    public static DxfBLOCKS readEntities(RandomAccessFile raf) 
                                     throws NumberFormatException, IOException {
        DxfBLOCKS dxfEntities = new DxfBLOCKS();
        DxfGroup group = new DxfGroup(2, "BLOCKS");
        while (group != null && !group.equals(DxfFile.ENDSEC)) {
             if (group.getCode() == 0) {
                 if (DxfFile.DEBUG) group.print(8);
                 if (group.getValue().equals("POINT")) {
                     group = DxfPOINT.readEntity(raf, dxfEntities.entities);
                 }
                 else if (group.getValue().equals("TEXT")) {
                     group = DxfTEXT.readEntity(raf, dxfEntities.entities);
                 }
                 else if (group.getValue().equals("LINE")) {
                     group = DxfLINE.readEntity(raf, dxfEntities.entities);
                 }
                 else if (group.getValue().equals("POLYLINE")) {
                     group = DxfPOLYLINE.readEntity(raf, dxfEntities.entities);
                 }
                 else if (group.getValue().equals("LWPOLYLINE")) {
                     group = DxfLWPOLYLINE.readEntity(raf, dxfEntities.entities);
                 }
                 else {
                     group = DxfGroup.readGroup(raf);
                 }
             }
             else {
                 group = DxfGroup.readGroup(raf);
             }
        }
        return dxfEntities;
    }

}

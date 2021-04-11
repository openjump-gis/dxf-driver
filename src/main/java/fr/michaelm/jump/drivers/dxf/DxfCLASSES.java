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


/**
 * Dxf section between the HEADER and the TABLES sections.
 * Not mandatory for DXF 12, don't use it.
 * @author Michaël Michaud
 */
public class DxfCLASSES {

    public DxfCLASSES() {}

    public static DxfCLASSES readClasses(RandomAccessFile raf) 
                                     throws NumberFormatException, IOException {
        DxfCLASSES classes = new DxfCLASSES();
        DxfGroup group;
        // readGroup returns a DxfGroup or throws an Exception. It does not return null.
        while (null != (group = DxfGroup.readGroup(raf)) && !group.equals(DxfFile.ENDSEC)) {
            // Read until end of CLASSES section without doing anything
        }
        return classes;
    }

    public String toString() {
        return DxfFile.SECTION.toString() + DxfFile.CLASSES + DxfFile.ENDSEC;
    }
}

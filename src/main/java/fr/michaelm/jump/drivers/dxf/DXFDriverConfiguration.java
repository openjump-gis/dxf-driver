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

import com.vividsolutions.jump.workbench.plugin.Extension;
import com.vividsolutions.jump.workbench.plugin.PlugInContext;

/**
 * This is the entry class to declare the dxf driver to JUMP.
 * You can put the &lt;extension&gt;drivers.dxf.DXFDriverConfiguration&lt;/extension&gt;
 * element in the workbench-properties.xml file or put the .jar file containing
 * the driver in the ext directory of your installation.
 * @author Michaël Michaud
 * @version 1.0.0
 */
// History
// 1.0.0 (2021-04-11) : * refactoring for OpenJUMP 2, JTS 1.18
// 0.9.0 (2018-06-02) : * fix a regression preventing export of MultiPolygons
//                      * use java 5 features (foreach, autoboxing, generics...)
// 0.8.1 (2016-09-29) : * makes it compatible with 1.9.1 release
//                        (remove multi-layer export capability - use java 1.7)
// 0.8.0 (2013-10-09) : * make it compatible with next 1.7.0 OpenJUMP release 
// 0.7.8 (2012-09-22) : * Fix a bug preventing TEXT entities to be read
//                      * Bug fixed x==Double.NaN --> Double.isNaN(x) in DXFPoint
// 0.7.7 (2012-02-23) : * Fixed bug 3492384 preventing export of z in "lines"
// 0.7.6 (2012-01-16) : * Compile again for java 1.5 compatibility
// 0.7.5 (2011-12-02) : * Throws NumberFormatException
// 0.7.4 (2011-11-20) : * fixed a compatibility problem between 0.7.3 and OJ1.4.3
// 0.7.3 (2011-04-10) : * fixed a bug in DxfBLOCKS
//                      * clean up the code
// 0.7.2 (2011-04-07) : * add TEXT_ROTATION attribute from Giuseppe Aruta code
// 0.7 (2010-11-01)   : * read polygons with less than 3 points as lines
//                        and lines with less than two different points as points
// 0.6 ()             : * added DxfLWPOLYLINE
// 0.5 (2006-11-12)   : * remove the header writing option after L. Becker and
//                        R. Littlefield have fix the bug in the header writing
//                      * bug fixed x==Double.NaN --> Double.isNaN(x)
// 0.4.x (2006-10-19) : * add optional '_' suffix
//                      * add optional header writer
//                      * DXF layer name is taken from layer attribute if it exists or
//                        from layer name else if
//                      * add multi-geometry export
//                      * add attribute tests an ability to export ANY jump layer
//                      * add ability to export holes in a separate layer or not
//                      * replace standard SaveFileDataSourceQueryChooser by a
//                        SaveDxfFileDataSourceQueryChooser with options for header
//                        for entity handles and for layer name.
//                      * add two options (one for header writing and the other
//                        to suffix layers containing holes) and a function to
//                        create valid DXF layer names from JUMP layer names
// 0.4 (2006-10-15)   : * makes it possible to export any JUMP layer (in 0.3,
//                        layers which were not issued from a dxf file could not
//                        be exported because it misses some attributes).
// 0.3 (2003-12-10)
// 0.2
public class DXFDriverConfiguration extends Extension {

    public void configure(PlugInContext context) {
        new InstallDXFDataSourceQueryChooserPlugIn().initialize(context);
    }

    public String getName() {return "DXF driver";}

    public String getVersion() {return "1.0.0 (2021-04-11)";}

}

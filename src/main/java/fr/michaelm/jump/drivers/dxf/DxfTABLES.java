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
import java.util.Map;
import java.util.HashMap;


/**
 * The TABLES section of a DXF file. It contains LAYERs, LTYPEs...
 * There is a static reader to read the TABLES section in a DXF file
 * and a toString method able to write the section in a DXF form
 * @author Michaël Michaud
 */
// History
// 2006-11-12 : Bug fixed x==Double.NaN --> Double.isNaN(x)
public class DxfTABLES {

    public final static DxfGroup TABLE    = new DxfGroup(0, "TABLE");
    public final static DxfGroup ENDTAB   = new DxfGroup(0, "ENDTAB");
    public final static DxfGroup NBMAX    = new DxfGroup(70, "NBMAX");
    public final static DxfGroup APPID    = new DxfGroup(2, "APPID");
    public final static DxfGroup DIMSTYLE = new DxfGroup(2, "DIMSTYLE");
    public final static DxfGroup LTYPE    = new DxfGroup(2, "LTYPE");
    public final static DxfGroup LAYER    = new DxfGroup(2, "LAYER");
    public final static DxfGroup STYLE    = new DxfGroup(2, "STYLE");
    public final static DxfGroup UCS      = new DxfGroup(2, "UCS");
    public final static DxfGroup VIEW     = new DxfGroup(2, "VIEW");
    public final static DxfGroup VPORT    = new DxfGroup(2, "VPORT");
    
    private Map<String,DxfTABLE_ITEM> appId;
    private Map<String,DxfTABLE_ITEM> dimStyle;
    private Map<String,DxfTABLE_ITEM> lType;
    private Map<String,DxfTABLE_ITEM> layer;
    private Map<String,DxfTABLE_ITEM> style;
    private Map<String,DxfTABLE_ITEM> ucs;
    private Map<String,DxfTABLE_ITEM> view;
    private Map<String,DxfTABLE_ITEM> vPort;

    public DxfTABLES() {
        appId    = new HashMap<>();
        dimStyle = new HashMap<>();
        lType    = new HashMap<>();
        layer    = new HashMap<>();
        style    = new HashMap<>();
        ucs      = new HashMap<>();
        view     = new HashMap<>();
        vPort    = new HashMap<>();
    }

    public static DxfTABLES readTables(RandomAccessFile raf) throws NumberFormatException, IOException {
        DxfTABLES tables = new DxfTABLES();
        DxfGroup group;
        //String nomVariable = null;
        // Iteration over each table
        while (null != (group = DxfGroup.readGroup(raf))) {
            if (group.equals(DxfFile.ENDSEC)) break;
            //Map map = null;
            else if (group.equals(TABLE)) {
                // Lecture du groupe portant le nom de la table
                group = DxfGroup.readGroup(raf);
                //if (group == null) break; // never happens
                if (DxfFile.DEBUG) group.print(4);
                if (group.equals(APPID)) {
                    tables.appId = DxfTABLE_APPID_ITEM.readTable(raf);
                }
                else if (group.equals(DIMSTYLE)) {
                    tables.dimStyle = DxfTABLE_DIMSTYLE_ITEM.readTable(raf);
                }
                else if (group.equals(LTYPE)) {
                    tables.lType = DxfTABLE_LTYPE_ITEM.readTable(raf);
                }
                else if (group.equals(LAYER)) {
                    tables.layer = DxfTABLE_LAYER_ITEM.readTable(raf);
                }
                else if (group.equals(STYLE)) {
                    tables.style = DxfTABLE_STYLE_ITEM.readTable(raf);
                }
                else if (group.equals(UCS)) {
                    tables.ucs = DxfTABLE_UCS_ITEM.readTable(raf);
                }
                else if (group.equals(VIEW)) {
                    tables.view = DxfTABLE_VIEW_ITEM.readTable(raf);
                }
                else if (group.equals(VPORT)) {
                    tables.vPort = DxfTABLE_VPORT_ITEM.readTable(raf);
                }
                //else if (group.getCode() == 999) {}
                //else {}
            }
            //else if (group.getCode() == 999) {}
            //else {}
        }
        return tables;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(DxfFile.SECTION.toString());
        sb.append(DxfFile.TABLES);
        if (vPort.size() > 0) {
            sb.append(DxfTABLES.TABLE.toString());
            sb.append(DxfTABLES.VPORT.toString());
            sb.append(DxfGroup.toString(70, Integer.toString(vPort.size())));
            //Iterator it = vPort.keySet().iterator();
            for (DxfTABLE_ITEM item : vPort.values()) {
                sb.append(DxfGroup.toString(0, "VPORT"));
                sb.append(item.toString());
            }
            sb.append(DxfTABLES.ENDTAB.toString());
        }
        if (appId.size() > 0) {
            sb.append(DxfTABLES.TABLE.toString());
            sb.append(DxfTABLES.APPID.toString());
            sb.append(DxfGroup.toString(70, Integer.toString(appId.size())));
            //Iterator it = appId.keySet().iterator();
            for (DxfTABLE_ITEM item : appId.values()) {
                sb.append(DxfGroup.toString(0, "APPID"));
                sb.append(item.toString());
            }
            sb.append(DxfTABLES.ENDTAB.toString());
        }
        if (dimStyle.size() > 0) {
            sb.append(DxfTABLES.TABLE.toString());
            sb.append(DxfTABLES.DIMSTYLE.toString());
            sb.append(DxfGroup.toString(70, Integer.toString(dimStyle.size())));
            //Iterator it = dimStyle.keySet().iterator();
            for (DxfTABLE_ITEM item : dimStyle.values()) {
                sb.append(DxfGroup.toString(0, "DIMSTYLE"));
                sb.append(item.toString());
            }
            sb.append(DxfTABLES.ENDTAB.toString());
        }
        if (lType.size() > 0) {
            sb.append(DxfTABLES.TABLE.toString());
            sb.append(DxfTABLES.LTYPE.toString());
            sb.append(DxfGroup.toString(70, Integer.toString(lType.size())));
            //Iterator it = lType.keySet().iterator();
            for (DxfTABLE_ITEM item : lType.values()) {
                sb.append(DxfGroup.toString(0, "LTYPE"));
                sb.append(item.toString());
            }
            sb.append(DxfTABLES.ENDTAB.toString());
        }
        if (layer.size() > 0) {
            sb.append(DxfTABLES.TABLE.toString());
            sb.append(DxfTABLES.LAYER.toString());
            sb.append(DxfGroup.toString(70, Integer.toString(layer.size())));
            //Iterator it = layer.keySet().iterator();
            for (DxfTABLE_ITEM item : layer.values()) {
                sb.append(DxfGroup.toString(0, "LAYER"));
                sb.append(item.toString());
            }
            sb.append(DxfTABLES.ENDTAB.toString());
        }
        if (style.size() > 0) {
            sb.append(DxfTABLES.TABLE.toString());
            sb.append(DxfTABLES.STYLE.toString());
            sb.append(DxfGroup.toString(70, Integer.toString(style.size())));
            //Iterator it = style.keySet().iterator();
            for (DxfTABLE_ITEM item : style.values()) {
                sb.append(DxfGroup.toString(0, "STYLE"));
                sb.append(item.toString());
            }
            sb.append(DxfTABLES.ENDTAB.toString());
        }
        if (ucs.size() >0) {
            sb.append(DxfTABLES.TABLE.toString());
            sb.append(DxfTABLES.UCS.toString());
            sb.append(DxfGroup.toString(70, Integer.toString(ucs.size())));
            //Iterator it = ucs.keySet().iterator();
            for (DxfTABLE_ITEM item : ucs.values()) {
                sb.append(DxfGroup.toString(0, "UCS"));
                sb.append(item.toString());
            }
            sb.append(DxfTABLES.ENDTAB.toString());
        }
        if (view.size() >0) {
            sb.append(DxfTABLES.TABLE.toString());
            sb.append(DxfTABLES.VIEW.toString());
            sb.append(DxfGroup.toString(70, Integer.toString(view.size())));
            //Iterator it = view.keySet().iterator();
            for (DxfTABLE_ITEM item : view.values()) {
                sb.append(DxfGroup.toString(0, "VIEW"));
                sb.append(item.toString());
            }
            sb.append(DxfTABLES.ENDTAB.toString());
        }
        sb.append(DxfFile.ENDSEC);
        return sb.toString();
    }

}

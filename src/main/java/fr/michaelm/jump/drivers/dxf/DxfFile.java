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
import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.feature.FeatureDataset;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.FeatureCollection;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.GeometryFactory;

/**
 * A whole dataset contained in a DXF file, and main methods to read from and
 * to write to the file.
 * 
 * @author Michaël Michaud
 */
public class DxfFile {
    
    public static boolean DEBUG = false;
    
    public final static DxfGroup SECTION  = new DxfGroup(0, "SECTION");
    public final static DxfGroup ENDSEC   = new DxfGroup(0, "ENDSEC");
    public final static DxfGroup EOF      = new DxfGroup(0, "EOF");
    public final static DxfGroup HEADER   = new DxfGroup(2, "HEADER");

    // CLASSES section is used from version 13
    public final static DxfGroup CLASSES  = new DxfGroup(2, "CLASSES");
    public final static DxfGroup TABLES   = new DxfGroup(2, "TABLES");
    public final static DxfGroup BLOCKS   = new DxfGroup(2, "BLOCKS");
    public final static DxfGroup ENTITIES = new DxfGroup(2, "ENTITIES");

    // OBJECTS section is used from version 13
    public final static DxfGroup OBJECTS  = new DxfGroup(2, "OBJECTS");

    // Common FeatureSchema for ENTITIES
    public final static FeatureSchema DXF_SCHEMA = new FeatureSchema();
    public static boolean DXF_SCHEMA_INITIALIZED = false;
    //static int iterator = 0;
    private DxfHEADER header = null;
    private DxfCLASSES classes = null;
    private DxfTABLES tables = null;
    private DxfBLOCKS blocks = null;
    private DxfENTITIES entities = null;
    private int coordinatePrecision = 2;

    FeatureCollection features;
    
    public DxfFile() {
        initializeDXF_SCHEMA();
    }
    
   /**
    * Initialize a JUMP FeatureSchema to load dxf data keeping some graphic
    * attributes.
    */
    public static void initializeDXF_SCHEMA() {
        if (DXF_SCHEMA.getAttributeCount() != 0) return;
        // codes 10,20,30... used by POINT, TEXT, LINE, POLYLINE, LWPOLYLINE 
        DXF_SCHEMA.addAttribute("GEOMETRY", AttributeType.GEOMETRY);
        // code 8, common to all entities
        DXF_SCHEMA.addAttribute("LAYER", AttributeType.STRING);
        // code 6, common to all entities
        DXF_SCHEMA.addAttribute("LTYPE", AttributeType.STRING);
        // code 38 used by LWPOLYLINE 
        DXF_SCHEMA.addAttribute("ELEVATION", AttributeType.DOUBLE);
        // code 39 used by POINT, TEXT, LINE, POLYLINE, LWPOLYLINE 
        DXF_SCHEMA.addAttribute("THICKNESS", AttributeType.DOUBLE);
        // code 62, common to all entities
        DXF_SCHEMA.addAttribute("COLOR", AttributeType.INTEGER);
        // code 1 used by TEXT
        DXF_SCHEMA.addAttribute("TEXT", AttributeType.STRING);
        // code 40 used by TEXT
        DXF_SCHEMA.addAttribute("TEXT_HEIGHT", AttributeType.DOUBLE);
        // code 50 used by TEXT
        DXF_SCHEMA.addAttribute("TEXT_ROTATION", AttributeType.DOUBLE);
        // code 7 used by TEXT
        DXF_SCHEMA.addAttribute("TEXT_STYLE", AttributeType.STRING);
    }
    
    public int getCoordinatePrecision(){
        return coordinatePrecision;
    }

    public void setCoordinatePrecision(int coordinatePrecision) {
        this.coordinatePrecision = coordinatePrecision;
    }
    
    public static DxfFile createFromFile(File file) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        return createFromFile(raf);
    }

    public static DxfFile createFromFile(RandomAccessFile raf) 
                                     throws NumberFormatException, IOException {
        DxfFile dxfFile = new DxfFile();
        initializeDXF_SCHEMA();
        dxfFile.features = new FeatureDataset(DXF_SCHEMA);
        DxfGroup group;
        try {
            while (null != (group = DxfGroup.readGroup(raf))) {
                if (group.equals(SECTION)) {
                    group = DxfGroup.readGroup(raf);
                    //if (group == null) break; // never happens, readGroup throws Exception
                    if (DxfFile.DEBUG) group.print(0);
                    if (group.equals(HEADER)) {
                        dxfFile.header = DxfHEADER.readHeader(raf);
                    }
                    else if (group.equals(CLASSES)) {
                        dxfFile.classes = DxfCLASSES.readClasses(raf);
                    }
                    else if (group.equals(TABLES)) {
                        dxfFile.tables = DxfTABLES.readTables(raf);
                    }
                    else if (group.equals(BLOCKS)) {
                        dxfFile.blocks = DxfBLOCKS.readEntities(raf);
                        dxfFile.features.addAll(dxfFile.blocks.entities.getFeatures());
                    }
                    else if (group.equals(ENTITIES)) {
                        dxfFile.entities = DxfENTITIES.readEntities(raf);
                        dxfFile.features.addAll(dxfFile.entities.entities.getFeatures());
                    }
                    else if (group.equals(OBJECTS)) {
                        //objects = DxfOBJECTS.readObjects(br);
                    }
                    else if (group.getCode() == 999) {
                        System.out.println("Comment : " + group.getValue());
                    }
                    else {
                        //System.out.println("Group " + group.getCode() + " " + group.getValue() + " UNKNOWN");
                    }
                }
                else if (group.getCode() == 999) {
                    //System.out.println("Comment : " + group.getValue());
                }
                else if (group.equals(EOF)) {
                    break;
                }
                else {
                    //System.out.println("Group " + group.getCode() + " " + group.getValue() + " UNKNOWN");
                }
            }
        } finally {
            raf.close();   
        }
        return dxfFile;
    }

    public FeatureCollection read(GeometryFactory gf) {
        return features;
    }

    public FeatureCollection getFeatureCollection() {
        return null;
    }

    public static void write(FeatureCollection features, String[] layerNames,
                             FileWriter fw, int precision, boolean suffix) {

        Envelope envelope = features.getEnvelope();

        Date date = new Date(System.currentTimeMillis());
        try {
            // COMMENTAIRES DU TRADUCTEUR
            fw.write(DxfGroup.toString(999, features.size() + " features"));
            fw.write(DxfGroup.toString(999, "TRANSLATED BY DXF Driver 0.9.0"));
            fw.write(DxfGroup.toString(999, "DATE : " + date));
            
            // ECRITURE DU HEADER
            fw.write(DxfGroup.toString(0, "SECTION"));
            fw.write(DxfGroup.toString(2, "HEADER"));
            fw.write(DxfGroup.toString(9, "$ACADVER"));
                fw.write(DxfGroup.toString(1, "AC1009"));
            fw.write(DxfGroup.toString(9, "$CECOLOR"));
                fw.write(DxfGroup.toString(62, 256));
            fw.write(DxfGroup.toString(9, "$CELTYPE"));
                fw.write(DxfGroup.toString(6, "DUPLAN"));
            fw.write(DxfGroup.toString(9, "$CLAYER"));
                fw.write(DxfGroup.toString(8, "0"));   // corrected by L. Becker on 2006-11-08
            fw.write(DxfGroup.toString(9, "$ELEVATION"));
                fw.write(DxfGroup.toString(40, 0.0, 3));
            fw.write(DxfGroup.toString(9, "$EXTMAX"));
                fw.write(DxfGroup.toString(10, envelope.getMaxX(), 6));
                fw.write(DxfGroup.toString(20, envelope.getMaxY(), 6));
                //fw.write(DxfGroup.toString(30, envelope.getMaxX(), 6));
            fw.write(DxfGroup.toString(9, "$EXTMIN"));
                fw.write(DxfGroup.toString(10, envelope.getMinX(), 6));
                fw.write(DxfGroup.toString(20, envelope.getMinY(), 6));
                //fw.write(DxfGroup.toString(30, envelope.getMaxX(), 6));
            fw.write(DxfGroup.toString(9, "$INSBASE"));
                fw.write(DxfGroup.toString(10, 0.0, 1));
                fw.write(DxfGroup.toString(20, 0.0, 1));
                fw.write(DxfGroup.toString(30, 0.0, 1));
            fw.write(DxfGroup.toString(9, "$LIMCHECK"));
                fw.write(DxfGroup.toString(70, 1));
            fw.write(DxfGroup.toString(9, "$LIMMAX"));
                fw.write(DxfGroup.toString(10, envelope.getMaxX(), 6));
                fw.write(DxfGroup.toString(20, envelope.getMaxY(), 6));
            fw.write(DxfGroup.toString(9, "$LIMMIN"));
                fw.write(DxfGroup.toString(10, envelope.getMinX(), 6));
                fw.write(DxfGroup.toString(20, envelope.getMinY(), 6));
            fw.write(DxfGroup.toString(9, "$LUNITS"));
                fw.write(DxfGroup.toString(70, 2));
            fw.write(DxfGroup.toString(9, "$LUPREC"));
                fw.write(DxfGroup.toString(70, 2));
            fw.write(DxfGroup.toString(0, "ENDSEC"));

            // ECRITURE DES TABLES
            fw.write(DxfGroup.toString(0, "SECTION"));
            fw.write(DxfGroup.toString(2, "TABLES"));
                fw.write(DxfGroup.toString(0, "TABLE"));
                fw.write(DxfGroup.toString(2, "STYLE"));
                fw.write(DxfGroup.toString(70, 1));
                fw.write(DxfGroup.toString(0, "STYLE")); // added by L. Becker on 2006-11-08
                    DxfTABLE_STYLE_ITEM style =
                        new DxfTABLE_STYLE_ITEM("STANDARD", 0, 0f, 1f, 0f, 0, 1.0f, "xxx.txt", "yyy.txt");
                    fw.write(style.toString());
                    fw.write(DxfGroup.toString(0, "ENDTAB"));
                fw.write(DxfGroup.toString(0, "TABLE"));
                fw.write(DxfGroup.toString(2, "LTYPE"));
                fw.write(DxfGroup.toString(70, 1));
                fw.write(DxfGroup.toString(0, "LTYPE")); // added by L. Becker on 2006-11-08
                    DxfTABLE_LTYPE_ITEM ltype =
                        new DxfTABLE_LTYPE_ITEM("CONTINUE", 0, "", 65, 0f, new float[0]);
                    fw.write(ltype.toString());
                    fw.write(DxfGroup.toString(0, "ENDTAB"));
                fw.write(DxfGroup.toString(0, "TABLE"));
                fw.write(DxfGroup.toString(2, "LAYER"));
                fw.write(DxfGroup.toString(70, 2));
                for (String layerName : layerNames) {
                    DxfTABLE_LAYER_ITEM dxfLayer =
                        new DxfTABLE_LAYER_ITEM(layerName, 0, 131, "CONTINUE");
                    fw.write(DxfGroup.toString(0, "LAYER")); // added by L. Becker on 2006-11-08
                    fw.write(dxfLayer.toString());
                    if (suffix) {
                        dxfLayer = new DxfTABLE_LAYER_ITEM(layerName + "_",
                                                        0, 131, "CONTINUE");
                        fw.write(DxfGroup.toString(0, "LAYER")); // added by L. Becker on 2006-11-08
                        fw.write(dxfLayer.toString());
                    }
                }
                fw.write(DxfGroup.toString(0, "ENDTAB"));
                fw.write(DxfGroup.toString(0, "ENDSEC"));
                
                // ECRITURE DES FEATURES
                fw.write(DxfGroup.toString(0, "SECTION"));
                fw.write(DxfGroup.toString(2, "ENTITIES"));
                for (Feature feature : features.getFeatures()) {
                    // use the layer attribute for layer name
                    String entity;
                    if (feature.getSchema().hasAttribute("LAYER")) {
                        entity = DxfENTITY.feature2Dxf(feature, feature.getString("LAYER"), suffix);
                    }
                    // use the JUMP layer name for DXF layer name
                    else if (layerNames.length > 0) {
                        entity = DxfENTITY.feature2Dxf(feature, layerNames[0], suffix);
                    }
                    else {
                        entity = DxfENTITY.feature2Dxf(feature, "0", suffix);
                    }
                    if (entity != null) {
                        fw.write(entity);
                    }
                }
                fw.write(DxfGroup.toString(0, "ENDSEC"));
                
            // FIN DE FICHIER
            fw.write(DxfGroup.toString(0, "EOF"));
            fw.flush();
        } catch(IOException ioe) {
            ioe.printStackTrace();
        } finally {
            if (null != fw) {
                try {
                    fw.close();
                } catch(IOException ignored){}
            }
        }
    }

    public static void main(String[] args) {
        JFileChooser jfc = new JFileChooser("C:/Michael/Test/dxf");
        File f = null;
        int r = jfc.showOpenDialog(new JFrame());
        if(r == JFileChooser.APPROVE_OPTION) {
            f = jfc.getSelectedFile();
        }
        try {
            DxfFile dxfFile = DxfFile.createFromFile(f);
            f = new File("C:/Michael/Test/dxf/essai.dxf");
            java.io.BufferedWriter bw = new java.io.BufferedWriter(new java.io.FileWriter(f));
            bw.write(dxfFile.header.toString());
            bw.write(dxfFile.tables.toString());
            bw.write(dxfFile.entities.toString());
            bw.write(DxfGroup.toString(0, "EOF"));
            bw.close();
        } catch(IOException ignored) {}
    }

}

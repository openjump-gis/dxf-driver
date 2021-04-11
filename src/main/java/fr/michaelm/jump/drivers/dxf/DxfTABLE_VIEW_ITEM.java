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
import java.util.LinkedHashMap;

/**
 * The VIEW item in the TABLES section
 * There is a static reader to read the item in a DXF file
 * and a toString method able to write it in a DXF form
 * @author Michaël Michaud
 */
public class DxfTABLE_VIEW_ITEM extends DxfTABLE_ITEM {
    private float viewHeight;
    private float viewWidth;
    private double viewCenterPointX;
    private double viewCenterPointY;
    private double[] viewDirectionFromTarget;
    private double[] targetPoint;
    private float lensLength;
    private double frontClippingPlaneOffset;
    private double backClippingPlaneOffset;
    private float twistAngle;
    private int viewMode;

    public DxfTABLE_VIEW_ITEM(String name, int flags) {
        super(name, flags);
        this.viewHeight = 0f;
        this.viewWidth = 0f;
        this.viewCenterPointX = 0.0;
        this.viewCenterPointY = 0;
        this.viewDirectionFromTarget = new double[3];
        this.targetPoint = new double[3];
        this.lensLength = 0f;
        this.frontClippingPlaneOffset = 0.0;
        this.backClippingPlaneOffset = 0.0;
        this.twistAngle = 0f;
        this.viewMode = 0;
    }

    public DxfTABLE_VIEW_ITEM(String name, int flags,
                              float viewHeight,
                              float viewWidth,
                              double viewCenterPointX,
                              double viewCenterPointY,
                              double[] viewDirectionFromTarget,
                              double[] targetPoint,
                              float lensLength,
                              double frontClippingPlaneOffset,
                              double backClippingPlaneOffset,
                              float twistAngle,
                              int viewMode) {
        super(name, flags);
        this.viewHeight = viewHeight;
        this.viewWidth = viewWidth;
        this.viewCenterPointX = viewCenterPointX;
        this.viewCenterPointY = viewCenterPointY;
        this.viewDirectionFromTarget = viewDirectionFromTarget;
        this.targetPoint = targetPoint;
        this.lensLength = lensLength;
        this.frontClippingPlaneOffset = frontClippingPlaneOffset;
        this.backClippingPlaneOffset = backClippingPlaneOffset;
        this.twistAngle = twistAngle;
        this.viewMode = viewMode;
    }

    public float getViewHeight() {return viewHeight;}
    public float getViewWidth() {return viewWidth;}
    public double getViewCenterPointX() {return viewCenterPointX;}
    public double getViewCenterPointY() {return viewCenterPointY;}
    public double[] getViewDirectionFromTarget() {return viewDirectionFromTarget;}
    public double[] getTargetPoint() {return targetPoint;}
    public float getLensLength() {return lensLength;}
    public double getFrontClippingPlaneOffset() {return frontClippingPlaneOffset;}
    public double getBackClippingPlaneOffset() {return backClippingPlaneOffset;}
    public float getTwistAngle() {return twistAngle;}
    public int getViewMode() {return viewMode;}

    public void setViewHeight(float viewHeight) {this.viewHeight = viewHeight;}
    public void setViewWidth(float viewWidth) {this.viewWidth = viewWidth;}
    public void setViewCenterPointX(double viewCenterPointX) {this.viewCenterPointX = viewCenterPointX;}
    public void setViewCenterPointY(double viewCenterPointY) {this.viewCenterPointY = viewCenterPointY;}
    public void setViewDirectionFromTarget(double[] viewDirectionFromTarget) {this.viewDirectionFromTarget = viewDirectionFromTarget;}
    public void setTargetPoint(double[] targetPoint) {this.targetPoint = targetPoint;}
    public void setLensLength(float lensLength) {this.lensLength = lensLength;}
    public void setFrontClippingPlaneOffset(double frontClippingPlaneOffset) {this.frontClippingPlaneOffset = frontClippingPlaneOffset;}
    public void setBackClippingPlaneOffset(double backClippingPlaneOffset) {this.backClippingPlaneOffset = backClippingPlaneOffset;}
    public void setTwistAngle(float twistAngle) {this.twistAngle = twistAngle;}
    public void setViewMode(int viewMode) {this.viewMode = viewMode;}

    public static Map<String,DxfTABLE_ITEM> readTable(RandomAccessFile raf) throws IOException {
        DxfTABLE_VIEW_ITEM item = new DxfTABLE_VIEW_ITEM("DEFAULT", 0);
        Map<String,DxfTABLE_ITEM> table  = new LinkedHashMap<>();
        DxfGroup group;
        while (null != (group = DxfGroup.readGroup(raf)) && !group.equals(ENDTAB)) {
            //group = DxfGroup.readGroup(raf);
            if (DxfFile.DEBUG) group.print(8);
            int code = group.getCode();
            if (group.equals(VIEW)) {
                item = new DxfTABLE_VIEW_ITEM("DEFAULT", 0);
            }
            else if (code==2) {
                item.setName(group.getValue());
                table.put(item.getName(), item);
            }
            //else if (code==5) {}   // tag appeared in version 13 of DXF
            //else if (code==100) {} // tag appeared in version 13 of DXF
            else if (code==70) {
                item.setFlags(group.getIntValue());
            }
            else if (code==40) {
                item.setViewHeight(group.getFloatValue());
            }
            else if (code==41) {
                item.setViewWidth(group.getFloatValue());
            }
            else if (code==10) {
                item.setViewCenterPointX(group.getDoubleValue());
            }
            else if (code==20) {
                item.setViewCenterPointY(group.getDoubleValue());
            }
            else if (code==11) {
              item.getViewDirectionFromTarget()[0] = group.getDoubleValue();
            }
            else if (code==21) {
              item.getViewDirectionFromTarget()[1] = group.getDoubleValue();
            }
            else if (code==31) {
              item.getViewDirectionFromTarget()[2] = group.getDoubleValue();
            }
            else if (code==12) {
              item.getTargetPoint()[0] = group.getDoubleValue();
            }
            else if (code==22) {
              item.getTargetPoint()[1] = group.getDoubleValue();
            }
            else if (code==32) {
              item.getTargetPoint()[2] = group.getDoubleValue();
            }
            else if (code==42) {
              item.setLensLength(group.getFloatValue());
            }
            else if (code==43) {
              item.setFrontClippingPlaneOffset(group.getDoubleValue());
            }
            else if (code==44) {
              item.setBackClippingPlaneOffset(group.getDoubleValue());
            }
            else if (code==50) {
              item.setTwistAngle(group.getFloatValue());
            }
            else if (code==71) {
              item.setViewMode(group.getIntValue());
            }
            //else {}
        }
        return table;
    }

    public String toString() {
        return super.toString() +
            DxfGroup.toString(40, viewHeight, 6) +
            DxfGroup.toString(41, viewWidth, 6) +
            DxfGroup.toString(10, viewCenterPointX, 6) +
            DxfGroup.toString(20, viewCenterPointY, 6) +
            DxfGroup.toString(11, viewDirectionFromTarget[0], 6) +
            DxfGroup.toString(21, viewDirectionFromTarget[1], 6) +
            DxfGroup.toString(31, viewDirectionFromTarget[2], 6) +
            DxfGroup.toString(12, targetPoint[0], 6) +
            DxfGroup.toString(22, targetPoint[1], 6) +
            DxfGroup.toString(32, targetPoint[2], 6) +
            DxfGroup.toString(42, lensLength, 6) +
            DxfGroup.toString(43, frontClippingPlaneOffset, 6) +
            DxfGroup.toString(44, backClippingPlaneOffset, 6) +
            DxfGroup.toString(50, twistAngle, 6) +
            DxfGroup.toString(71, viewMode);
    }

}

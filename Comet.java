/**
* Comet.java
* Created in 2012 by Benjamin Gyori
* National University of Singapore
* e-mail: bgyori@nus.edu.sg
*
* Comet.java defines the Comet class used to represent a
* comet with its parameters and status flag.
*
* This plugin is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3 
* as published by the Free Software Foundation.
*
* This work is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* When you use this plugin for your work, please cite
* Gyori BM, Venkatachalam G, et al. OpenComet: An automated tool for 
* comet assay image analysis

* You should have received a copy of the GNU General Public License
* along with this plugin; if not, write to the Free Software
* Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/


import ij.gui.Roi;

public class Comet {
	public Comet(Roi roi){
		this.cometRoi = (Roi)roi.clone();
		this.status = Comet.VALID;
	} 
	
	public int id;
	public int status;
	public static final int VALID = 1;
	public static final int INVALID = 2;
	public static final int OUTLIER = 4;
	public static final int DELETED = 8;
	
	public Roi cometRoi;
	public Roi headRoi;
	public Roi oldRoi;
	
	// Position in image
	public int x,y, height, width;
	
	// Profiles
	public double[] cometProfile;
	public double[] bgProfile;
	public double[] headProfile;
	public double[] tailProfile;
	public double profileMax;
	
	// Output measurements
	public double cometArea;
	public double cometIntensity;
	public double cometLength;
	public double cometDNA;
	public double headArea;
	public double headIntensity;
	public double headLength;
	public double headDNA;
	public double headDNAPercent;
	public int headCentroid;
	public double tailArea;
	public double tailLength;
	public double tailIntensity;
	public double tailDNA;
	public double tailDNApercent;
	public int tailCentroid;
	public double tailMoment;
	public double tailOliveMoment;
	
	// Internal parameters
	public int[] histogram;
	public double area;
	public double areaConvexHull;
	public double mean;
	public double convexity;
	public double symmetry;
	public double perimeter;
	public double circularity;
	public double centerlineDiff;
	public double hratio;
	public int headFrontCenterY; // are these needed?
	public int headRoiCenterY;
	
	public String getMeasurementString(String sep){
		String flagStr;
		if(status==VALID)
			flagStr = "normal";
		else if (status==OUTLIER)
			flagStr = "outlier";
		else if (status==DELETED)
			flagStr = "deleted";
		else
			return "";
		
		String result = id + sep
				+ flagStr + sep 
				+ cometArea + sep
				+ cometIntensity + sep
				+ cometLength + sep 
				+ cometDNA + sep 
				+ headArea + sep 
				+ headIntensity + sep 
				+ headLength + sep 
				+ headDNA + sep 
				+ headDNAPercent + sep
				+ tailArea + sep 
				+ tailIntensity + sep 
				+ tailLength + sep
				+ tailDNA + sep 
				+ tailDNApercent + sep
				+ tailMoment + sep
				+ tailOliveMoment;
		return result;
		}
}

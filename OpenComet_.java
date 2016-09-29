/**
* OpenComet_.java
* Created in 2012 by Benjamin Gyori
* National University of Singapore
* e-mail: bgyori@nus.edu.sg
*
* OpenComet_.java is the outside wrapper for the OpenComet plug-in.
* It displays the GUI and manages all input-output functions
* by the plug-in.
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

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

//import CometStatistics.CometAnalyzer;
import java.util.HashMap;
import java.util.Map;


public class OpenComet_ implements PlugIn, MouseListener {
	//private JLabel inFilesLabel, outDirLabel;
	private JLabel inFilesStatusLabel, outDirStatusLabel;
	private JButton inFilesButton, outDirButton, runButton;
	private JButton updateOutputButton;
	private JButton liveButton;
	private ActionListener inFilesButtonLis, outDirButtonLis, runButtonLis;
	private ActionListener updateOutputButtonLis;
	private ActionListener liveButtonLis;
	private JFileChooser inFileChooser, outDirChooser;
	private JTextField outFileNameField;
	private JCheckBox bgCorrectCheck;
	private JRadioButton headFindingAuto, headFindingProfile, headFindingBrightest;
	private int cometOptions;

	private static Color labelInvalidColor = new Color(200,0,0);
	private static Color labelValidColor = new Color(0,150,0);
	
	File[] inFiles;
	File outDir;
	String outDirPath;
	String outFileName;
	
	private static Color cometValidColor = Color.red;
	private static Color cometInvalidColor = Color.gray;
	private static Color cometOutlierColor = Color.orange;	
	private static Color cometColor = Color.yellow;
	private static Color headColor = Color.green;
	private static Color tailColor = Color.blue;
	
	private HashMap<ImagePlus,Comet[]> Comets;
	
	public OpenComet_(){}

	public void run(String arg) {
		IJ.log("Welcome to OpenComet");
		makeLayout();
		updateOutputButton.setEnabled(false);
		}
			


	private void runOnInput(int type){
		// Make an instance of the comet analyzer class
		CometAnalyzer cometAnalyzer = new CometAnalyzer();
		Comets = new HashMap<ImagePlus,Comet[]>();
		cometOptions = 0;
		
		// Setup comet analysis options
		if(bgCorrectCheck.isSelected())	cometOptions |= CometAnalyzer.COMETFIND_BGCORRECT;
		if(headFindingAuto.isSelected()) cometOptions |= CometAnalyzer.HEADFIND_AUTO;
		if(headFindingProfile.isSelected()) cometOptions |= CometAnalyzer.HEADFIND_PROFILE;
		if(headFindingBrightest.isSelected()) cometOptions |= CometAnalyzer.HEADFIND_BRIGHTEST;
		
		String tmpText = outFileNameField.getText();
		if((tmpText!=null) && (tmpText.length()>1)){
			outFileName = tmpText;
		}
		
		IJ.log(cometOptions+"");
		
		
		if(type==0) {
		// Open each input image and run comet analysis
			// Iterate over each input file
			for(int i=0;i<inFiles.length;i++){
				// Try to open file as image
				ImagePlus imp = IJ.openImage(inFiles[i].getPath());
				
				// If image could be opened, run comet analysis
				if(imp!=null){
					String imageKey =  inFiles[i].getName();
					
					IJ.log("Run started, image key: "+ imageKey);

					Comet[] cometsOut = cometAnalyzer.cometAnalyzerRun(imp,cometOptions);
					
					IJ.log("Run complete, image key: "+ imageKey);
					
					storeComets(cometsOut,imp,imageKey);
					
					// if(cometsOut!=null && cometsOut.length>0){
						// ImageProcessor ip = imp.getProcessor();
						// int imgType = imp.getType();
						// ColorProcessor ip_out = null;
						// if(imgType == 0){
							// TypeConverter t = new TypeConverter(ip,false);
							// ip_out = (ColorProcessor)t.convertToRGB();
							// }
						//RGB
						// else if(imgType == 4){
							// ip_out = (ColorProcessor)(ip.duplicate());
							// }
						

						// String imgOutFileName = imageKey +"_out.tif";					
						// ImagePlus img_out = new ImagePlus(imgOutFileName,ip_out);
						
						// Comets.put(img_out, cometsOut);
						
						
						// for(int j=0;j<cometsOut.length;j++){
							// if(cometsOut[j].status==Comet.VALID)
								// printArray(cometsOut[j].cometProfile);
						// }
						
						
						// Overlay cometOverlay = new Overlay();
						// drawComets(ip_out, cometOverlay, cometsOut);
						// img_out.setOverlay(cometOverlay);
						
						// img_out.show();
						// img_out.getCanvas().addMouseListener(this);
						
						// ImagePlus img_out_save = img_out.flatten();
						// IJ.save(img_out_save, outDirPath + imgOutFileName);
						// }
					// else {
						// IJ.log("No comets stored");
					// }
				 }
				else {
					IJ.log("Could not open " + inFiles[i].getName() + ", unsupported format");
					}
				}
			}
			else {
				ImageWindow imw = WindowManager.getCurrentWindow();
				if(imw!=null){
					ImagePlus img = imw.getImagePlus();
					ImagePlus img2 = imw.getImagePlus().duplicate();
					if(img!=null){
						Comet[] cometsOut = cometAnalyzer.cometAnalyzerRun(img,cometOptions);
						if(cometsOut==null || cometsOut.length==0){
							IJ.log("No comets found.");
						}
						else {
							IJ.log("Number of comets found: "+cometsOut.length);
						}
						storeComets(cometsOut,img2,outFileName+"_image");
						}
					}
				}
			
			printComets(outFileName);
			
			updateOutputButton.setEnabled(true);
			runButton.setEnabled(false);
			}
			
			
		private void storeComets(Comet[] cometsOut, ImagePlus imp,String imageKey){
			ImageProcessor ip = imp.getProcessor();
			int imgType = imp.getType();
			ColorProcessor ip_out = null;
			if(imgType == ImagePlus.GRAY8){
				TypeConverter t = new TypeConverter(ip,false);
				ip_out = (ColorProcessor)t.convertToRGB();
			}
			else if (imgType == ImagePlus.GRAY16 || imgType == ImagePlus.GRAY32){
				ImageProcessor ip_temp = ip.convertToByte(true); 
				TypeConverter t = new TypeConverter(ip_temp,false);
				ip_out = (ColorProcessor)t.convertToRGB();
			}
			else if(imgType == ImagePlus.COLOR_RGB){
				ip_out = (ColorProcessor)(ip.duplicate());
			}
			else {
				IJ.log("Unhandled image type: "+imgType);
			}
				
			String imgOutFileName = imageKey +"_out.tif";					
			ImagePlus img_out = new ImagePlus(imgOutFileName,ip_out);
			
			if(cometsOut!=null && cometsOut.length>0){
				Comets.put(img_out, cometsOut);
				
				for(int j=0;j<cometsOut.length;j++){
					if(cometsOut[j].status==Comet.VALID)
						printArray(cometsOut[j].cometProfile);
				}
				
				Overlay cometOverlay = new Overlay();
				drawComets(ip_out, cometOverlay, cometsOut);
				img_out.setOverlay(cometOverlay);
			} else {
				IJ.log("No comets in image stored.");
			}
				
			img_out.show();
			img_out.getCanvas().addMouseListener(this);
			
			ImagePlus img_out_save = img_out.flatten();
			IJ.save(img_out_save, outDirPath + imgOutFileName);
		}
	
	private void drawComets(ImageProcessor ip, Overlay overlay, Comet[] comets){
		for(int i=0;i<comets.length;i++){
			if(comets[i].status == Comet.INVALID){
				comets[i].cometRoi.setStrokeColor(cometInvalidColor);
				overlay.add(comets[i].cometRoi);
			}
			else if (comets[i].status == Comet.OUTLIER){
				comets[i].cometRoi.setStrokeColor(cometOutlierColor);
				comets[i].headRoi.setStrokeColor(cometOutlierColor);
				//overlay.add(comets[i].cometRoi);
				overlay.add(comets[i].oldRoi);
				overlay.add(comets[i].headRoi);
				
				ip.setColor(Color.red);
				TextRoi textRoi = new TextRoi(comets[i].x+comets[i].width,
						comets[i].y, ""+comets[i].id, new Font("Arial", Font.BOLD, 22));
				textRoi.drawPixels(ip);
			}
			else if(comets[i].status == Comet.VALID){
				comets[i].cometRoi.setStrokeColor(cometValidColor);
				comets[i].headRoi.setStrokeColor(cometValidColor);
				overlay.add(comets[i].cometRoi);
				overlay.add(comets[i].headRoi);
				
				ip.setColor(Color.red);
				TextRoi textRoi = new TextRoi(comets[i].x+comets[i].width,
						comets[i].y, ""+comets[i].id, new Font("Arial", Font.BOLD, 22));
				textRoi.drawPixels(ip);
				ImageProcessor ipProfile = getCometProfilePlot(comets[i]);
				ip.copyBits(ipProfile, comets[i].x, comets[i].y,Blitter.COPY_TRANSPARENT);
			}
			
		}
	}
	
	private ImageProcessor getCometProfilePlot(Comet comet){
		ImageProcessor ip = new ColorProcessor(comet.width, comet.height);
		ip.invert();

		double heightStep = comet.height / comet.profileMax;
		drawProfilePlot(ip, comet.cometProfile, cometColor, heightStep);
		drawProfilePlot(ip, comet.headProfile, headColor, heightStep);
		drawProfilePlot(ip, comet.tailProfile, tailColor, heightStep);
		
		return ip;
		}

	private void drawProfilePlot(ImageProcessor ip, double[] profile, Color col, double heightStep){
		int height = ip.getHeight();
		int width = ip.getWidth();
		int x1,x2,y1,y2;
		ip.setColor(col);
		for(int x=0;x<width-1;x++){
			x1 = x; x2 = x+1;
			y1 = (int)((height - 1) - profile[x]*heightStep);
			y2 = (int)((height - 1) - profile[x+1]*heightStep);
			ip.drawLine(x1,y1,x2,y2);
			}
		}

	private void printComets(String fileName){
		// Setup output data file
		try {
			String outFileName = outDirPath + fileName + ".xls";
			FileOutputStream fos = new FileOutputStream(outFileName);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			PrintWriter outPrintWriter = new PrintWriter(bos);
			
			IJ.log("Out filename: " + outFileName);		
			
			String sep = "\t";
			String headerstr = "FileName" + sep + "Num" + sep + "Flag" + sep + "CometArea" + sep + 
					"CometIntensity" + sep + 
					"CometLength" + sep + "CometDNA" + sep +
					"HeadArea" + sep + "HeadIntensity" + sep +
					"HeadLength" + sep + "HeadDNA"+ sep +
					"HeadDNAPercent" + sep + 
					"TailArea" + sep + "TailIntensity" + sep +
					"TailLength" + sep + "TailDNA"+ sep + 
					"TailDNAPercent" + sep + "TailMoment"
					+ sep + "OliveMoment";
			outPrintWriter.println(headerstr);
			
			for (Map.Entry<ImagePlus,Comet[]> element : Comets.entrySet()) {
				    ImagePlus imp = element.getKey();
					String imgTitle = imp.getTitle();
					IJ.log("Image title: "+imgTitle);
					int endIdx = imgTitle.lastIndexOf('_');
					if(endIdx > 0){
						String imageKey = imgTitle.substring(0,endIdx);
					    Comet[] imageComets = element.getValue();
					    for(int i=0;i<imageComets.length;i++){
					    	if(imageComets[i].status != Comet.INVALID){
					    		String outstr = imageKey + sep;
					    		outstr += imageComets[i].getMeasurementString(sep);
					    		outPrintWriter.println(outstr);
					    	}
					    }
					}
				}
			
			CometStatistics cometStats = new CometStatistics(Comets,Comet.VALID);
			String outstr = cometStats.getStatisticsString(sep,"normal");
			outPrintWriter.print(outstr);
			
			cometStats = new CometStatistics(Comets,Comet.VALID|Comet.OUTLIER);
			outstr = cometStats.getStatisticsString(sep,"normal+outlier");
			outPrintWriter.print(outstr);
			
			outPrintWriter.close();
		}
		catch (Exception ex) {
			IJ.log(ex.toString());
			}
	}
	
	private void makeLayout(){
		ij.util.Java2.setSystemLookAndFeel();
		JDialog mainDialog = new JDialog(ij.IJ.getInstance(),"OpenComet",false);
		mainDialog.setLocationRelativeTo(ij.IJ.getInstance());
		mainDialog.setLayout(new BorderLayout(0,10));
		
		//ImagePlus imlogo = IJ.openImage("opencometlogo.gif");
		//if (imlogo != null) {
		//      mainDialog.setIconImage(imlogo.getImage());
		//    }

		inFileChooser = new JFileChooser();
		outDirChooser = new JFileChooser();

		runButtonLis = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if((inFiles == null) || (inFiles.length == 0)){
					IJ.log("Select input files");
					}
				else if((outDir == null)){
					IJ.log("Select output directory");
					}
				else {
					runOnInput(0);
					}
				}
			};
			
		liveButtonLis = new ActionListener(){
			public void actionPerformed(ActionEvent e){	
				if((outDir == null)){
					IJ.log("Select output directory");
					}
				else {
					runOnInput(1);
				}
			}
		};

		inFilesButtonLis = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				inFileChooser.setDialogTitle("Input Files");
				inFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				inFileChooser.setMultiSelectionEnabled(true);
				
				inFileChooser.showOpenDialog(null);
				
				inFiles = inFileChooser.getSelectedFiles();
				if(inFiles.length == 0){
					inFilesStatusLabel.setText("Choose input files");
					inFilesStatusLabel.setForeground(labelInvalidColor);
					}
				else if(inFiles.length == 1){
					inFilesStatusLabel.setText("1 file selected");
					inFilesStatusLabel.setForeground(labelValidColor);
					}
				else {
					inFilesStatusLabel.setText(inFiles.length+" files selected");
					inFilesStatusLabel.setForeground(labelValidColor);
					}
					
				inFilesStatusLabel.repaint(); 
	    			}
			};
			
		outDirButtonLis = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				outDirChooser.setDialogTitle("Output Directory");
				outDirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				outDirChooser.setMultiSelectionEnabled(false);
			
				int result = outDirChooser.showOpenDialog(null);
				outDir = outDirChooser.getSelectedFile();  
				if(result==0){
					outDirPath = outDir+"";
					int len = outDirPath.length();
					
					if(outDirPath.charAt(len-1) != File.separatorChar){
						outDirPath = outDirPath + File.separatorChar;
						}
				
					
					String outDirStatusLabelText = outDirPath ;

					if(len > 25){
						outDirStatusLabelText = outDirPath.substring(0,12) + "..." + outDirPath.substring(len-12,len);
						}

					boolean canWrite = checkWriteAccess(outDirPath);
					
					if(canWrite) {
				  		outDirStatusLabel.setText(outDirStatusLabelText);
						outDirStatusLabel.setForeground(labelValidColor);
						} else {
				  		outDirStatusLabel.setText(outDirStatusLabelText);
						outDirStatusLabel.setForeground(labelInvalidColor);
						}
					}
				else {
					outDirStatusLabel.setText("Choose output directory");
					outDirStatusLabel.setForeground(labelInvalidColor);
					}
					
					outDirStatusLabel.repaint(); 
				}
			};
			
		updateOutputButtonLis = new ActionListener(){
			public void actionPerformed(ActionEvent e){	
				printComets(outFileName + "_update");
				
				for (Map.Entry<ImagePlus,Comet[]> element : Comets.entrySet()) {
				    ImagePlus imp = element.getKey();
				    if(imp.getCanvas()==null) continue;
				    ImagePlus impSave = imp.flatten();
				    IJ.save(impSave, outDirPath + imp.getTitle());
				}
			}
		}; 
		
		// liveButtonLis = new ActionListener(){
			// public void actionPerformed(ActionEvent e){	
				// ImageWindow imw = WindowManager.getCurrentWindow();
				// if(imw!=null){
					// ImagePlus img = imw.getImagePlus();
					// if(img!=null){
						// CometAnalyzer cometAnalyzer = new CometAnalyzer();
						// Comet[] cometsOut = cometAnalyzer.cometAnalyzerRun(img,3);
						// IJ.log("Number of comets found: "+cometsOut.length);
					// }
				// }
				
				
			// }
		// }; 

		// Set up main dialog layout
		Panel mainPanel = new Panel();
		mainPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(2,2,8,8);
		
		// Input files label 
		JLabel inFilesLabel = new JLabel("Input files");
		c.fill = GridBagConstraints.NONE; c.gridx = 0; c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(inFilesLabel, c);
		// Input files browse button
		inFilesButton = new JButton("Browse");
		c.fill = GridBagConstraints.NONE; c.gridx = 1; c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(inFilesButton, c);
		inFilesButton.addActionListener(inFilesButtonLis);
		// Input files status label
		inFilesStatusLabel = new JLabel("Choose input files");
		inFilesStatusLabel.setForeground(labelInvalidColor);
		c.fill = GridBagConstraints.NONE; c.gridx = 0; c.gridy = 1;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_START;
		
		mainPanel.add(inFilesStatusLabel, c);

		// Output directory label
		JLabel outDirLabel = new JLabel("Output directory");
		c.fill = GridBagConstraints.NONE; c.gridx = 0; c.gridy = 2;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(outDirLabel, c);
		// Output directory browse button
		outDirButton = new JButton("Browse");
		c.fill = GridBagConstraints.NONE; c.gridx = 1; c.gridy = 2;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(outDirButton, c);
		outDirButton.addActionListener(outDirButtonLis);
		// Output directory status label
		outDirStatusLabel = new JLabel("Choose output directory");
		outDirStatusLabel.setForeground(labelInvalidColor);
		c.fill = GridBagConstraints.NONE; c.gridx = 0; c.gridy = 3;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(outDirStatusLabel, c);
		
		
		JLabel outFileNameLabel = new JLabel("Output file name");
		c.fill = GridBagConstraints.NONE; c.gridx = 0; c.gridy = 4;
		c.gridwidth = 1;
		c.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(outFileNameLabel, c);
		
		Date dateNow = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MMdd_HHmmss");
		outFileName = "comets_" + dateFormat.format(dateNow);
		
		outFileNameField = new JTextField(20);
		outFileNameField.setText(outFileName);
		c.fill = GridBagConstraints.NONE; c.gridx = 0; c.gridy = 5;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(outFileNameField, c);

		// Comet finding options
		JSeparator cometFindingSep = new JSeparator(SwingConstants.HORIZONTAL);
		cometFindingSep.setPreferredSize(new Dimension(1,1));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 6;
		mainPanel.add(cometFindingSep,c);
		
		JLabel cometFindingLabel = new JLabel("Comet finding");
		bgCorrectCheck = new JCheckBox("Background correction",true);
		
		c.fill = GridBagConstraints.NONE; c.gridx = 0; c.gridy = 7;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(cometFindingLabel, c);
		c.gridy = 8;
		mainPanel.add(bgCorrectCheck, c);

		// Head finding options
		JSeparator headFindingSep = new JSeparator(SwingConstants.HORIZONTAL);
		headFindingSep.setPreferredSize(new Dimension(1,1));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridy = 9;
		mainPanel.add(headFindingSep,c);
		
		JLabel headFindingLabel = new JLabel("Head finding");

		Panel headFindingGroupPanel = new Panel();
		headFindingGroupPanel.setLayout(new BoxLayout(headFindingGroupPanel, BoxLayout.Y_AXIS));
		headFindingAuto = new JRadioButton("Auto",true);
		headFindingProfile = new JRadioButton("Profile analysis");
		headFindingBrightest = new JRadioButton("Brigthest region");
		
		ButtonGroup headFindingGroup = new ButtonGroup();
		headFindingGroup.add(headFindingAuto);
		headFindingGroup.add(headFindingProfile);
		headFindingGroup.add(headFindingBrightest);
		headFindingGroupPanel.add(headFindingAuto);
		headFindingGroupPanel.add(headFindingProfile);
		headFindingGroupPanel.add(headFindingBrightest);
		
		c.fill = GridBagConstraints.NONE; c.gridx = 0; c.gridy = 10;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(headFindingLabel, c);

		c.fill = GridBagConstraints.NONE; c.gridx = 0; c.gridy = 11;
		c.gridwidth = 2;
		c.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(headFindingGroupPanel, c);
		//mainPanel.add(headFindingAuto, c);
		//c.gridy = 10;
		//mainPanel.add(headFindingProfile, c);
		//c.gridy = 11;
		//mainPanel.add(headFindingBrightest, c);
		
		// Run button panel
		Panel runPanel = new Panel();
		runButton = new JButton("Run");
		updateOutputButton = new JButton("Update");
		liveButton = new JButton("Live");
		runPanel.add(runButton);
		runPanel.add(updateOutputButton);
		runPanel.add(liveButton);
		runButton.addActionListener(runButtonLis);
		updateOutputButton.addActionListener(updateOutputButtonLis);
		liveButton.addActionListener(liveButtonLis);
		
		// Add panels to main dialog
		Box mainBox = Box.createVerticalBox();
		mainBox.add(mainPanel);
		mainBox.add(Box.createVerticalStrut(3) );
		mainDialog.add(mainBox, BorderLayout.CENTER);
		mainDialog.add(runPanel, BorderLayout.SOUTH);
		mainDialog.pack();
		mainDialog.setResizable(false);
		mainDialog.setVisible(true);
		}

	public void mousePressed (MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		ImageCanvas canvas = (ImageCanvas)e.getSource();
		ImagePlus imp = canvas.getImage();
		x = canvas.offScreenX(x);
		y = canvas.offScreenY(y);
		IJ.log(x+","+y);
		Comet comet = findClickedComet(imp, x, y);
		if(comet!=null){
			IJ.log("Comet found");
			comet.status = Comet.DELETED;
			comet.cometRoi.setStrokeColor(Color.gray);
			comet.oldRoi.setStrokeColor(Color.gray);
			if(comet.headRoi!=null){
				comet.headRoi.setStrokeColor(Color.gray);
				}
			}
		}
	
	
	private Comet findClickedComet(ImagePlus imp, int x,int y){
		Comet[] comets = (Comet[])Comets.get(imp);
		if(comets!=null){
			for(int i=0;i<comets.length;i++){
				if(comets[i].cometRoi.contains(x, y)){
					return comets[i];
				}
			}
		}
		return null;
	}
	
	public void mouseReleased (MouseEvent e) {}
	public void mouseClicked (MouseEvent e) {}
	public void mouseEntered (MouseEvent e) {}
	public void mouseExited (MouseEvent e) {}

	private boolean checkWriteAccess(String path){
		File tmpFile = new File(path+"tmp");
		try {
			tmpFile.createNewFile();
			tmpFile.delete();
			}
		catch(IOException e) {
			return false;
			}
		return true;
		}
	
	private void printArray(double[] arr){
		String s = "";
		for(int i=0;i<arr.length;i++){
			s += arr[i];
			if(i<arr.length-1) s+=",";
			}
		IJ.log(s);
		}
}


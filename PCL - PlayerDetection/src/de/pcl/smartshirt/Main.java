package de.pcl.smartshirt;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
 
public class Main {
	
	public static void main( String[] args ) throws InterruptedException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);		
	    Test t = new Test();
	    
	    //Index: O - Externe Kamera
	    //Index: 1 - Interne Kamera
	    VideoCapture camera = new VideoCapture(0);

        Mat frame = new Mat();
        camera.read(frame); 

        if(!camera.isOpened()) {
            System.out.println("CameraError");
        } else {                  
        	JFrame jFrame = new JFrame();        	
        	JPanel jPanel = new JPanel();
        	JPanel panelLow = new JPanel();
        	JPanel panelUp = new JPanel();
        	
        	FlowLayout flowLayout = new FlowLayout();	
        	GridLayout gridLayout = new GridLayout(0, 2);
        	
        	jFrame.setLayout(flowLayout);
        	jPanel.setLayout(flowLayout);
        	panelLow.setLayout(gridLayout);
        	panelUp.setLayout(gridLayout);
        	
        	panelLow.setBorder(BorderFactory.createTitledBorder("HSV-Lower-Range"));
        	panelUp.setBorder(BorderFactory.createTitledBorder("HSV-Upper-Range"));
        	
        	JLabel cPic = new JLabel();
        	JLabel bwPic = new JLabel();
        	
        	JLabel hLow = new JLabel("H-Lower:");
        	JLabel sLow = new JLabel("S-Lower:");
        	JLabel vLow = new JLabel("V-Lower:");
        	JSlider hSilderLow = new JSlider(0, 360, 0);
        	JSlider sSilderLow = new JSlider(0, 255, 100);
        	JSlider vSilderLow = new JSlider(0, 255, 100);
        	
        	JLabel hUp = new JLabel("H-Upper:");
        	JLabel sUp = new JLabel("S-Upper:");
        	JLabel vUp = new JLabel("V-Upper:");
        	JSlider hSilderUp = new JSlider(0, 360, 0);
        	JSlider sSilderUp = new JSlider(0, 255, 255);
        	JSlider vSilderUp = new JSlider(0, 255, 255);  
        	
        	panelLow.add(hLow);
        	panelLow.add(hSilderLow);
        	panelLow.add(sLow);
        	panelLow.add(sSilderLow);
        	panelLow.add(vLow);
        	panelLow.add(vSilderLow);
        	
        	panelUp.add(hUp);
        	panelUp.add(hSilderUp);
        	panelUp.add(sUp);
        	panelUp.add(sSilderUp);
        	panelUp.add(vUp);
        	panelUp.add(vSilderUp); 
        	
        	jPanel.add(panelLow);
        	jPanel.add(panelUp);

        	jFrame.add(cPic);
        	jFrame.add(bwPic); 
        	jFrame.add(jPanel);        
        	       	
        	jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        	jFrame.setTitle("LiveStream BlackWhite");
        	jFrame.setSize(1300, 630);
        	jFrame.setVisible(false);   
        	
        	boolean firstRun = true;
        	
            while (true) {        
                if (camera.read(frame)) {
                	Mat hsvFrame = new Mat();
                	Imgproc.cvtColor(frame, hsvFrame, Imgproc.COLOR_BGR2HSV);             	
                	
                	hLow.setText("H-Lower: " + hSilderLow.getValue());
                	sLow.setText("S-Lower: " + sSilderLow.getValue());
                	vLow.setText("V-Lower: " + vSilderLow.getValue());
                	
                	hUp.setText("H-Upper: " + hSilderUp.getValue());
                	sUp.setText("S-Upper: " + sSilderUp.getValue());
                	vUp.setText("V-Upper: " + vSilderUp.getValue());
                	
                	//Team 1 - Farbbereich blau
                	Mat bwFrame = new Mat();
                	Scalar lowerb = new Scalar(hSilderLow.getValue(), sSilderLow.getValue(), vSilderLow.getValue());
                	Scalar upperb = new Scalar(hSilderUp.getValue(), sSilderUp.getValue(), vSilderUp.getValue());
                	Core.inRange(hsvFrame, lowerb, upperb, bwFrame);
                	
//                	//Team 2 - Farbbereich grün
//                	Mat destFrame = new Mat();
//                	Scalar lowerb = new Scalar(hSilderLow.getValue(), sSilderLow.getValue(), vSilderLow.getValue());
//                	Scalar upperb = new Scalar(hSilderUp.getValue(), sSilderUp.getValue(), vSilderUp.getValue());
//                	Core.inRange(hsvFrame, lowerb, upperb, destFrame);
                	
                	Size s = new Size(640, 480);
                	Imgproc.resize(bwFrame, bwFrame, s);
                	Imgproc.resize(frame, frame, s);
                	
                    BufferedImage cImage = t.MatToBufferedImage(frame);
                    BufferedImage bwImage = t.MatToBufferedImage(bwFrame);
                    cPic.setIcon(new ImageIcon(cImage));                    
                    bwPic.setIcon(new ImageIcon(bwImage));
                    
                    
                    if(firstRun) {
                    	firstRun = !firstRun;
                    	jFrame.setLocationRelativeTo(null);
                    	jFrame.setVisible(true);   
                    }
                }
            }   
        }
        camera.release();	
   }
}
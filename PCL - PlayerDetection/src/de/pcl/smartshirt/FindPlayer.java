package de.pcl.smartshirt;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
 
public class FindPlayer {
	
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
               //if (camera.read(frame)) {
            		
            		Thread.sleep(100);
            	
            		BufferedImage image = null;
					try {
						image = ImageIO.read(new File("pics/blau.jpg"));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
            		byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            		frame.put(0, 0, pixels);
                	
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
                	
                	//morphologische Basis-Operation: Closing (Dilatation>Erosion)
                	bwFrame = imDialte(bwFrame, 0, 5);
                	bwFrame = imErode(bwFrame, 0, 5);
                	
                	//morphologische Basis-Operation: Opening (Erosion>Dilatation)
                	bwFrame = imErode(bwFrame, 0, 5);
                	bwFrame = imDialte(bwFrame, 0, 5);                	
                	
                	List<RotatedRect> rects = findRectContour(bwFrame);
                	
                	//Convert to color pic
                	Imgproc.cvtColor(bwFrame, bwFrame, Imgproc.COLOR_GRAY2BGR);
                	
                	for(RotatedRect r:rects) {
                		Point[] pt = new Point[4];
                		r.points(pt);
             
                		Scalar color = new Scalar(0, 0, 255);                		
                		for(int i=0; i<4; i++) {
                			Imgproc.line(bwFrame, pt[i], pt[(i+1)%4], color, 3, 8, 0);
                		}           
                		
                		Imgproc.circle(bwFrame, r.center, 2, color, 3);
                		               		
                		//Rect RotatedRect = r.boundingRect();
                    	//Imgproc.rectangle(bwFrame, new Point(r.x,r.y), new Point(r.x+r.width,r.y+r.height), new Scalar(255, 0, 0), 5);
                    	//Imgproc.rectangle(bwFrame, RotatedRect.tl(), RotatedRect.br(), new Scalar(255, 0, 0), 3, 8, 0);
                	}
                	
                	if(rects.size() == 1) {
                		//System.out.println("Distance: " + getDistance(rects.get(0).center, rects.get(1).center));
                		
                		getDirection(rects.get(0));
                		
                		
                	}
                	
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
        //}
        //camera.release();	
	}
	
	public static Mat imErode(Mat srcImg, int erosionElem, int erosionSize) {
		int erosionType = 0;
		if(erosionElem == 0) { 
			erosionType = Imgproc.MORPH_RECT; 
		} else if(erosionElem == 1 ) { 
			erosionType = Imgproc.MORPH_CROSS; 
		} else if(erosionElem == 2) { 
			erosionType = Imgproc.MORPH_ELLIPSE; 
		}

		Mat destImg = new Mat();
		Mat element = Imgproc.getStructuringElement(erosionType, new Size(2*erosionSize + 1, 2*erosionSize+1), new Point(erosionSize, erosionSize));
		Imgproc.erode(srcImg, destImg, element);     
		
		return destImg;
	}

	
	public static Mat imDialte(Mat srcImg, int dilationElem, int dilationSize) {
		int dilationType = 0;
		if(dilationElem == 0) { 
			dilationType = Imgproc.MORPH_RECT; 
		} else if(dilationElem == 1 ) { 
			dilationType = Imgproc.MORPH_CROSS; 
		} else if(dilationElem == 2) { 
			dilationType = Imgproc.MORPH_ELLIPSE; 
		}

		Mat destImg = new Mat();
		Mat element = Imgproc.getStructuringElement(dilationType, new Size(2*dilationSize + 1, 2*dilationSize+1), new Point(dilationSize, dilationSize));
		Imgproc.dilate(srcImg, destImg, element);     
		
		return destImg;
	}
	
	public static List<RotatedRect> findRectContour(Mat srcImg) {		
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(srcImg.clone(), contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		
		//TODO: NOCH FALSCHE CONTOUREN DENKE ICH!!! SONST SOLLTE ES KLAPPEN! 
		//SIEHE: http://answers.opencv.org/question/25755/drawing-bounding-box-in-java/
		
		//List<Rect> rectList = new ArrayList<Rect>();
		List<RotatedRect> rectList = new ArrayList<RotatedRect>();
		
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		for(int i = 0; i< contours.size(); i++) {			
			//Convert contours(i) from MatOfPoint to MatOfPoint2f
	        MatOfPoint2f contour2f = new MatOfPoint2f( contours.get(i).toArray() );
	        //Processing on mMOP2f1 which is in type MatOfPoint2f
	        double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
	        Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

	        //Convert back to MatOfPoint
	        MatOfPoint points = new MatOfPoint(approxCurve.toArray());

	        //Get bounding rect of contour
	        //Rect rect = Imgproc.boundingRect(points);
			RotatedRect rect = Imgproc.minAreaRect(contour2f);
	        rectList.add(rect);
		}
		return rectList;		
	}
	
	
	
	public static double getDistance(Point p1, Point p2) {
		return Math.sqrt(Math.pow(p1.x-p2.x, 2) + Math.pow(p1.y-p2.y, 2));
	}
	
	public static double getDirection(RotatedRect rect1) {
		double dir = 0;
		
		System.out.println(rect1.angle);
		
		return dir;
	}
	
	public static double getAngle(RotatedRect rect) {
		return 0;		
	}
	
	
	public static void generateAlert() {
		
	}
	
}
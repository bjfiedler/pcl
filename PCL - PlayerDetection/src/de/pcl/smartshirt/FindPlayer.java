package de.pcl.smartshirt;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

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
	private static boolean cameraMode = false;

	

	public static void main( String[] args ) throws InterruptedException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);		
		Test t = new Test();
		FindPlayerView view = new FindPlayerView();
		Scalar lowerb = new Scalar(0, 0, 0);
		Scalar upperb = new Scalar(0, 0, 0);
		view.registerLowerBoundScalar(lowerb);
		view.registerUpperBoundScalar(upperb);
		
		//Index: O - Externe Kamera (wenn vorhanden)
		//Index: 1 - Interne Kamera (wenn keine externe angeschlossen)
		VideoCapture camera = new VideoCapture(0);

		Mat frame = new Mat();
		camera.read(frame); 

		if(!camera.isOpened() && view.isCameraMode()) {
			System.out.println("CameraError");
		} else {                  
			boolean firstRun = true;

			while (true) {
				Thread.sleep(100);
				
				// Initialize frame
				if (!view.isCameraMode()) { // Initialize frame from picture
					BufferedImage image = null;
					try {
						image = ImageIO.read(new File("pics/blau.jpg"));
					} catch (IOException e) {
						e.printStackTrace();
					}

					byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
					frame.put(0, 0, pixels);
				} else if (!camera.read(frame)) {
					continue;
				}

				// Process Frame
				Mat hsvFrame = new Mat();
				Imgproc.cvtColor(frame, hsvFrame, Imgproc.COLOR_BGR2HSV);             	

				

				//Team 1 - Farbbereich blau
				Mat bwFrame = new Mat();
				//Scalar lowerb = new Scalar(hSilderLow.getValue(), sSilderLow.getValue(), vSilderLow.getValue());
				//Scalar upperb = new Scalar(hSilderUp.getValue(), sSilderUp.getValue(), vSilderUp.getValue());
				
				Core.inRange(hsvFrame, lowerb, upperb, bwFrame);

				//morphologische Basis-Operation: Closing (Dilatation>Erosion)
				bwFrame = imDialte(bwFrame, 0, 5);
				bwFrame = imErode(bwFrame, 0, 5);

				//morphologische Basis-Operation: Opening (Erosion>Dilatation)
				bwFrame = imErode(bwFrame, 0, 5);
				bwFrame = imDialte(bwFrame, 0, 5);                	

				//RotatedRects im Bild finden
				List<RotatedRect> rects = findRectContour(bwFrame);

				//Convert to color pic
				Imgproc.cvtColor(bwFrame, bwFrame, Imgproc.COLOR_GRAY2BGR);

				//Gefundene RotatedRects ins Bild zeichnen
				for(RotatedRect r:rects) {
					Point[] pt = new Point[4];
					r.points(pt);

					Scalar color = new Scalar(0, 0, 255);                		
					for(int i=0; i<4; i++) {
						Imgproc.line(bwFrame, pt[i], pt[(i+1)%4], color, 3, 8, 0);
					}           

					Imgproc.circle(bwFrame, r.center, 2, color, 3);
				}
				
				//=============================================================================================
				//=============================================================================================				
				
				//calculate and draw plaver oriantation
				if(rects.size() != 0 && rects.size() % 2 == 0) { 
					//Get closest edges from two rectangles
					ArrayList<Point> startPoints = getStartPointsFromRec(rects.get(0), rects.get(1));
					
					//Get line (short side) for each rectangle
					ArrayList<Point> line1 = getLineFromRect(startPoints.get(0), rects.get(0));
					ArrayList<Point> line2 = getLineFromRect(startPoints.get(1), rects.get(1));

					//Get Intersection point (null if non exists)
					Point pIntersection = getLineIntersection(line1, line2);
					if(pIntersection != null) {

						//Draw green rectangle for player direction visualisation
						Scalar color = new Scalar(0, 255, 0);
						Imgproc.line(bwFrame, pIntersection, line1.get(0), color);
						Imgproc.line(bwFrame, pIntersection, line2.get(0), color);
						Imgproc.line(bwFrame, startPoints.get(0), startPoints.get(1), color);

						//Draw intersection point for player direction visualisation
						color = new Scalar(255, 0, 0);
						Imgproc.circle(bwFrame, pIntersection, 2, color, 10);
						
						//Get player orientation vector and value
						Point pOrthToPoint = getOrthogonalIntersectionPoint(pIntersection, startPoints.get(0), getDeirectionalVectorForLine(startPoints.get(0), startPoints.get(1)));
						double playerOrientation = getDirection(pOrthToPoint, pIntersection);

						//Draw player orientation vector
						//Imgproc.circle(bwFrame, newP, 2, color, 10);
						color = new Scalar(0, 0, 255);
						Imgproc.line(bwFrame, pOrthToPoint, pIntersection, color);
						
						//TODO: Handle Events!
						System.out.println(playerOrientation);
					}					
				}

				//=============================================================================================
				//=============================================================================================	
				
//            	//Team 2 - Farbbereich grün
//            	Mat destFrame = new Mat();
//            	Scalar lowerb = new Scalar(hSilderLow.getValue(), sSilderLow.getValue(), vSilderLow.getValue());
//            	Scalar upperb = new Scalar(hSilderUp.getValue(), sSilderUp.getValue(), vSilderUp.getValue());
//            	Core.inRange(hsvFrame, lowerb, upperb, destFrame);

				//Visualize results
				view.updateView(bwFrame, frame);
				
				if(firstRun) {
					firstRun = !firstRun;
					view.show();
				}
			}
		}   
		if (view.isCameraMode()) { camera.release(); }
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
		Imgproc.findContours(srcImg.clone(), contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		List<RotatedRect> rectList = new ArrayList<RotatedRect>();
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		for(int i = 0; i< contours.size(); i++) {			
			//Convert contours(i) from MatOfPoint to MatOfPoint2f
			MatOfPoint2f contour2f = new MatOfPoint2f( contours.get(i).toArray() );
			//Processing on mMOP2f1 which is in type MatOfPoint2f
			double approxDistance = Imgproc.arcLength(contour2f, true)*0.02;
			Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
			//Get bounding rect of contour
			RotatedRect rect = Imgproc.minAreaRect(contour2f);
			rectList.add(rect);
		}
		return rectList;		
	}	

	public static double getDistance(Point p1, Point p2) {
		return Math.sqrt(Math.pow(p1.x-p2.x, 2) + Math.pow(p1.y-p2.y, 2));
	}

	public static double getDirection(Point p1, Point p2) {
		Point v = new Point((p1.x-p2.x)/getDistance(p1, p2), (p1.y-p2.y)/getDistance(p1, p2));			
		return Math.atan2(v.y, v.x);
	}

	public static ArrayList<Point> getLineFromRect(Point startPoint, RotatedRect rect) {
		ArrayList<Point> line = new ArrayList<Point>();	

		Point[] pt = new Point[4];
		rect.points(pt);

		double min = Double.MAX_VALUE;
		Point p = startPoint;
		Point q = null;
		for(int i=0; i<pt.length;i++) {
			if(!pt[i].equals(startPoint)) {
			
				double dist = getDistance(p, pt[i]); 
				if(dist < min) {
					min = dist;
					q = pt[i];
				}
			}
		}

		Point v = getDeirectionalVectorForLine(p, q);

		line.add(p);
		line.add(v);	
		return line;
	}

	
	public static ArrayList<Point> getStartPointsFromRec(RotatedRect rect1, RotatedRect rect2) {
		ArrayList<Point> startPoints = new ArrayList<Point>();
		
		Point[] ptsRec1 = new Point[4];
		rect1.points(ptsRec1);
		Point[] ptsRec2 = new Point[4];
		rect2.points(ptsRec2);
		
		Point start1 = null;
		Point start2 = null;
		double minDist = Double.MAX_VALUE;
		
		for(Point p1:ptsRec1) {
			for(Point p2:ptsRec2) {
				if(getDistance(p1, p2) < minDist) {
					minDist = getDistance(p1, p2);
					start1 = p1;
					start2 = p2;
				}
			}
		}
		
		startPoints.add(start1);
		startPoints.add(start2);	
		return startPoints;
	}	
	
	public static Point getOrthogonalIntersectionPoint(Point p, Point linePoint, Point lineVector) {		
		Point newP = new Point((linePoint.x-p.x)*lineVector.x, (linePoint.y-p.y)*lineVector.y); 
		Point newV = new Point(lineVector.x*lineVector.x, lineVector.y*lineVector.y);
		
		double sumNewP = -1*(newP.x+newP.y);
		double sumNewV = newV.x+newV.y;
		
		double r = sumNewP/sumNewV;
		return new Point(linePoint.x+r*lineVector.x, linePoint.y+r*lineVector.y);
	}
	
	
	public static Point getDeirectionalVectorForLine(Point p1, Point p2) {
		return new Point(p1.x-p2.x, p1.y-p2.y);
	}
	
	public static Point getLineIntersection(ArrayList<Point> l1, ArrayList<Point> l2) {
		Point intersectionPoint = null;		
		/*
		 ** Muss gegeben sein. a ist Aufpunkt der ersten Gerade, b Aufpunkt der zweiten.
		 ** u ist Richtungsvektor der ersten Gerade, v der der zweiten.
		 */

		double a1 = l1.get(0).x;
		double a2 = l1.get(0).y;

		double u1 = l1.get(1).x;
		double u2 = l1.get(1).y;

		double b1 = l2.get(0).x;
		double b2 = l2.get(0).y;

		double v1 = l2.get(1).x;
		double v2 = l2.get(1).y;		


		double D = (u1*v2 - u2*v1);
		if (D != 0) { //schneiden sich 
			double D1 = (b1-a1)*v2 - v1*(b2-a2);
			double D2 = u1*(b2-a2) - u2*(b1-a1);
			double lambda = D1/D;
			double my = -D2/D;
			double p1 = a1 + lambda*u1;
			double p2 = a2 + lambda*u2;

			intersectionPoint = new Point(p1,p2);
		} 

		return intersectionPoint;
	}

	public static void generateAlert() {
		//TODO: Alert erzeugen
	}
}
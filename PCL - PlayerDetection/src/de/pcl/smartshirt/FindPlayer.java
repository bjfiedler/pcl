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
	private static Scalar lowerBoundBlue = new Scalar(0, 0, 0); 
	private static Scalar upperBoundBlue = new Scalar(0, 0, 0);
	private static Scalar lowerBoundGreen = new Scalar(0, 46, 60);
	private static Scalar upperBoundGreen = new Scalar(80, 255, 255);
	private static final String BLUE_TEAM = "BLUE_TEAM";
	private static final String GREEN_TEAM = "GREEN_TEAM";
	

	public static void main( String[] args ) throws InterruptedException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);		
		Test t = new Test();
		
		FindPlayerView view = new FindPlayerView();
		view.registerLowerBoundScalar(lowerBoundBlue);
		view.registerUpperBoundScalar(upperBoundBlue);
		view.registerLowerBoundScalarGreen(lowerBoundGreen);
		view.registerUpperBoundScalarGreen(upperBoundGreen);
		
		//Index: O - Externe Kamera (wenn vorhanden)
		//Index: 1 - Interne Kamera (wenn keine externe angeschlossen)
		VideoCapture camera = new VideoCapture(0);

		Mat frame = new Mat();
		camera.read(frame); 

		if(!camera.isOpened() && view.isCameraMode()) {
			System.out.println("CameraError");
		} else {                  
			boolean firstRun = true;

			int counter = 0;
			Player bluePlayerMean = null;
			Player greenPlayerMean = null;
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

				
			
			
				//=============================================================================================
				//=============================================================================================				
				//Team 1 - Farbbereich blau
				Mat bwFrameBlue = new Mat();
				List<RotatedRect> candidatesBlue = detectPlayerCandidates(BLUE_TEAM, hsvFrame, bwFrameBlue);

				
				//Gefundene RotatedRects ins Bild zeichnen
				Imgproc.cvtColor(bwFrameBlue, bwFrameBlue, Imgproc.COLOR_GRAY2BGR);
				drawCandidates(candidatesBlue, BLUE_TEAM, bwFrameBlue);
				drawCandidates(candidatesBlue, BLUE_TEAM, frame);
				Player[] bluePlayer = detectPlayer(candidatesBlue, BLUE_TEAM, bwFrameBlue);


				//=============================================================================================
				//=============================================================================================	
				//Team 1 - Farbbereich gr�n
				Mat bwFrameGreen = new Mat();
				List<RotatedRect> candidatesGreen = detectPlayerCandidates(GREEN_TEAM, hsvFrame, bwFrameGreen);
				//Convert to color pic
				
				//Gefundene RotatedRects ins Bild zeichnen
				Imgproc.cvtColor(bwFrameGreen, bwFrameGreen, Imgproc.COLOR_GRAY2BGR);
				drawCandidates(candidatesGreen, GREEN_TEAM, bwFrameGreen);
				drawCandidates(candidatesGreen, GREEN_TEAM, frame);
				Player[] greenPlayer = detectPlayer(candidatesGreen, GREEN_TEAM, bwFrameGreen);
				//=============================================================================================
				//=============================================================================================				
					
				//Visualize results
				view.updateView(bwFrameBlue, bwFrameGreen, frame);

				
				
				if(bluePlayer[0] != null) {
					if(bluePlayerMean == null) {
						bluePlayerMean = bluePlayer[0];
					}
					bluePlayerMean.setDirection((bluePlayerMean.getDirection() + bluePlayer[0].getDirection())/2);
				}
				
				if(greenPlayer[0] != null) {
					if(greenPlayerMean == null) {
						greenPlayerMean = greenPlayer[0];
					}
					greenPlayerMean.setDirection((greenPlayerMean.getDirection() + greenPlayer[0].getDirection())/2);
				}
				
				
				if(counter != 0 && counter%10 == 0) {
					counter = 0;
					if(bluePlayerMean!= null && greenPlayerMean != null) {
						determineAttackedPlayer(new Player[] {bluePlayerMean}, new Player[] {greenPlayerMean});
					}
				}
				counter ++;
				
				if(firstRun) {
					firstRun = !firstRun;
					view.show();
				}
			}
		}   
		if (view.isCameraMode()) { camera.release(); }
	}
	
	public static List<RotatedRect> detectPlayerCandidates(String team, Mat inputFrame, Mat bwFrame) {
		//Mat bwFrame = new Mat();
		if (team.equals(BLUE_TEAM)) {
			Core.inRange(inputFrame, lowerBoundBlue, upperBoundBlue, bwFrame);
		} else {
			Core.inRange(inputFrame, lowerBoundGreen, upperBoundGreen, bwFrame);
		}

		//morphologische Basis-Operation: Closing (Dilatation>Erosion)
		bwFrame = imDialte(bwFrame, 0, 5);
		bwFrame = imErode(bwFrame, 0, 5);

		//morphologische Basis-Operation: Opening (Erosion>Dilatation)
		bwFrame = imErode(bwFrame, 0, 5);
		bwFrame = imDialte(bwFrame, 0, 5);                	

		//RotatedRects im Bild finden
		List<RotatedRect> rects = findRectContour(bwFrame);
		
		return rects;
	}
	
	public static void drawCandidates(List<RotatedRect> candidates, String team, Mat outputFrame) {
		//Gefundene RotatedRects ins Bild zeichnen
		Scalar color;
		if (team.equals(BLUE_TEAM)) {
			color = new Scalar(255, 0, 0);
		} else {
			color = new Scalar(0, 255, 0);
		}
		for(RotatedRect r:candidates) {
			Point[] pt = new Point[4];
			r.points(pt);

			                		
			for(int i=0; i<4; i++) {
				Imgproc.line(outputFrame, pt[i], pt[(i+1)%4], color, 3, 8, 0);
			}           

			Imgproc.circle(outputFrame, r.center, 2, color, 3);
		}
	}
	
	public static Player[] detectPlayer(List<RotatedRect> candidates, String team, Mat outputFrame ) {
		Player[] player = new Player[]{null};
		//calculate and draw player orientation
		if(candidates.size() != 0 && candidates.size() % 2 == 0) { 
			//Get closest edges from two rectangles
			ArrayList<Point> startPoints = getStartPointsFromRec(candidates.get(0), candidates.get(1));
			
			//Get line (short side) for each rectangle
			ArrayList<Point> line1 = getLineFromRect(startPoints.get(0), candidates.get(0));
			ArrayList<Point> line2 = getLineFromRect(startPoints.get(1), candidates.get(1));

			//Get Intersection point (null if non exists)
			Point pIntersection = getLineIntersection(line1, line2);
			if(pIntersection != null) {

				//Draw green rectangle for player direction visualization
				Scalar color = new Scalar(0, 255, 0);
				Imgproc.line(outputFrame, pIntersection, line1.get(0), color);
				Imgproc.line(outputFrame, pIntersection, line2.get(0), color);
				Imgproc.line(outputFrame, startPoints.get(0), startPoints.get(1), color);

				//Draw intersection point for player direction visualization
				color = new Scalar(255, 0, 0);
				Imgproc.circle(outputFrame, pIntersection, 2, color, 10);
				
				//Get player orientation vector and value
				Point pOrthToPoint = getOrthogonalIntersectionPoint(pIntersection, startPoints.get(0), getDeirectionalVectorForLine(startPoints.get(0), startPoints.get(1)));
				double playerOrientation = getDirection(pOrthToPoint, pIntersection);

				//Draw player orientation vector
				//Imgproc.circle(bwFrame, newP, 2, color, 10);
				color = new Scalar(0, 0, 255);
				Imgproc.line(outputFrame, pOrthToPoint, pIntersection, color);
				
				//TODO: Handle Events!
				Point c1 = candidates.get(0).center;
				Point c2 = candidates.get(1).center;
				Point position = new Point((c1.x + c2.x) / 2, (c1.y + c2.y) / 2);
				Player p = new Player(1, team, position, playerOrientation);
				player = new Player[]{p};
				
				//System.out.println("Direction: " + playerOrientation);
			}					
		}
		return player;
	}
	
	public static void determineAttackedPlayer(Player[] blueTeam, Player[] greenTeam) {
		// TODO Determine threshold
		double distanceThreshold = 400.00; 
		PlayingField field = new PlayingField();
		
		for (Player bluePlayer: blueTeam) {
			for (Player greenPlayer: greenTeam) {
				double dist = getDistance(bluePlayer.getPosition(), greenPlayer.getPosition());
				if (dist > distanceThreshold) {
					continue;
				}
				
				if (field.isPlayerAttacked(bluePlayer, greenPlayer) != PlayingField.NOT_ATTACKED) {
					generateAlert(bluePlayer, PlayingField.ATTACK_DIRECTION_BEHIND);
				} else if (field.isPlayerAttacked(greenPlayer, bluePlayer) != PlayingField.NOT_ATTACKED) {
					generateAlert(greenPlayer, PlayingField.ATTACK_DIRECTION_BEHIND);
				}
			}
		}
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
		
//		start1.x = (start1.x + rect1.center.x)/2;
//		start1.y = (start1.y + rect1.center.y)/2;
//		start2.x = (start2.x + rect2.center.x)/2;
//		start2.y = (start2.y + rect2.center.y)/2;
		
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

	public static void generateAlert(Player player, int attackDirection) {
		//TODO: Alert erzeugen
		System.out.println(player.getTeam() + " attacked from behind!");
	}
}
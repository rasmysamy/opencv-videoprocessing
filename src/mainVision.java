import javafx.geometry.BoundingBox;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
//import ui.*;

public class mainVision {

	public static boolean willRequestImage;
	
	
	public static void main(String[] args) 
	{
		System.out.println(System.getProperty("java.library.path"));

		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		

		
		

		
		secondaryWindow altWindow = new secondaryWindow();
		Mat image = new Mat();
		//image = Imgcodecs.imread("C:\\Users\\Samy Rasmy\\Documents\\2019-Targets\\0.png");
//		cmodule.m_camera.read(image);
//		if(image.height() == 0){
//			System.out.println("Image load fail");
//		}
//		else {
//			cmodule.m_log.info("Image load sucess");
//			cmodule.m_log.info("X = "+image.cols());
//			cmodule.m_log.info("Y = "+image.rows());
//			cmodule.m_log.info("Channels: " + image.channels());
//		}
		
		VideoCapture camera = new VideoCapture("C:\\Users\\Samy Rasmy\\Downloads\\drive-download-20190124T020714Z-001\\20190115_143320_202_608.mov");
		while(image.cols() == 0)
			camera.read(image);
		
		willRequestImage = false;

		imageWindow window = new imageWindow(image);
		window.setFPSRate(0);

		String csvOut = "";
		int counter = 0;
		
		
		while(true)
		{
			if(window.isLive() | window.getUpdateStatus()) willRequestImage = true;
			counter++;
			

			//Scalar minsHSV = new Scalar	(minH, minS, minV);
			//Scalar maxsHSV = new Scalar(maxH, maxS, maxV);
			//cmodule.setHSV(minsHSV, maxsHSV);altWindow.getToSliders()
			
			camera.read(image);
			try {
				Thread.sleep((long)(1000/29.97));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Mat mask = new Mat();
			Imgproc.cvtColor(image, mask, Imgproc.COLOR_BGR2GRAY);
			mask.setTo(new Scalar(0));
			Rect boundingRect = new Rect();

			boundingRect.x = 30;
			boundingRect.y = 30;
			boundingRect.width = 30;
			boundingRect.height = 30;

			ArrayList<Point> boundingList = new ArrayList<Point>();



			boundingRect.x = (int)(altWindow.getFromSliders().val[0]/180 * image.cols());
			boundingRect.y = (int)(altWindow.getFromSliders().val[1]/255 * image.rows());
			boundingRect.width = (int)(altWindow.getToSliders().val[1]);
			boundingRect.height = (int)(altWindow.getToSliders().val[2]);

			boundingList.add(new Point(boundingRect.x+boundingRect.width*0.5, boundingRect.y+boundingRect.height*0.5));
			boundingList.add(new Point(boundingRect.x+boundingRect.width*-0.5, boundingRect.y+boundingRect.height*0.5));
			boundingList.add(new Point(boundingRect.x+boundingRect.width*-0.5, boundingRect.y+boundingRect.height*-0.5));
			boundingList.add(new Point(boundingRect.x+boundingRect.width*0.5, boundingRect.y+boundingRect.height*-0.5));

			//System.out.println(boundingRect);

			MatOfPoint boundingPoint = new MatOfPoint();
			boundingPoint.fromList(boundingList);

			Imgproc.fillConvexPoly(mask, boundingPoint, new Scalar(255), 8, 0);
			Mat hsvImage = new Mat();
			Imgproc.cvtColor(image, hsvImage, Imgproc.COLOR_BGR2HSV);
			Mat tempMat = new Mat();
			Core.inRange(hsvImage, new Scalar(0, 80, 30), new Scalar(180, 240, 250), tempMat);

			hsvImage = new Mat();
			Core.bitwise_and(tempMat, mask, tempMat);
			Imgproc.cvtColor(tempMat, tempMat, Imgproc.COLOR_GRAY2BGR);
			Core.bitwise_and(image, tempMat, hsvImage);

			Imgproc.cvtColor(tempMat, tempMat, Imgproc.COLOR_BGR2GRAY);
			Core.multiply(image, new Scalar(0.2, 0.2, 0.2), mask);
			Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2HSV);
			Scalar averageColor = Core.mean(image, tempMat);

			csvOut = csvOut + counter + ","+ averageColor.val[0] + "\n";
			if(averageColor.val[0] == 0 && counter > 1300)
				break;

			Core.add(hsvImage, mask, hsvImage);

			Imgproc.rectangle(hsvImage, new Point(boundingRect.x+boundingRect.width*-0.5, boundingRect.y+boundingRect.height*-0.5),
					new Point(boundingRect.x+boundingRect.width*0.5, boundingRect.y+boundingRect.height*0.5), new Scalar(255, 0, 0));

			window.update(hsvImage);
			altWindow.setAngle(0);
			//rrCom.setAngle(cmodule.m_degree, 0.0D);
			//System.out.println(rrCom.getAngle()[0]);
			window.setFPSRate((int)(0));
		}

		System.out.println(csvOut);
		PrintWriter out = null;
		try {
			out = new PrintWriter("1.csv");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		out.println(csvOut);

	}
	
	
}

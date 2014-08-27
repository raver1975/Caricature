package com.klemstinegroup;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.GlintFilter;
import com.jhlabs.image.InvertFilter;
import com.jhlabs.image.LaplaceFilter;
import com.jhlabs.image.PosterizeFilter;
import com.jhlabs.image.SwimFilter;
import com.jhlabs.image.ThresholdFilter;

public class Main1 {
	int EDGES_THRESHOLD = 80;
	int LAPLACIAN_FILTER_SIZE = 5;
	int MEDIAN_BLUR_FILTER_SIZE = 7;
	int repetitions = 7; // Repetitions for strong cartoon effect.
	int ksize = 1; // Filter size. Has a large effect on speed.
	double sigmaColor = 9; // Filter color strength.
	double sigmaSpace = 7; // Spatial strength. Affects speed.
	CanvasFrame cf = new CanvasFrame("Rotoscopor");
	
	PosterizeFilter pf=new PosterizeFilter();
	SwimFilter sf=new SwimFilter();
	LaplaceFilter lf=new LaplaceFilter();
	ThresholdFilter tf=new ThresholdFilter();
	InvertFilter invf=new InvertFilter();
	GlintFilter gf=new GlintFilter();
//	GaussianFilter gf=new GaussianFilter();
	private float t;
	
	public Main1() throws Exception  {
		start();
	}
	public void start() throws Exception{
		OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
		grabber.start();
		cf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		IplImage image = grabber.grab();
		IplImage gray = IplImage.create(image.cvSize(), IPL_DEPTH_8U, 1);
		cvCvtColor(image, gray, CV_BGR2GRAY);
		IplImage edges = IplImage.create(gray.cvSize(), gray.depth(), gray.nChannels());
		IplImage temp = IplImage.create(image.cvSize(), image.depth(), image.nChannels());
		// long time=System.currentTimeMillis();
		
		pf.setNumLevels(20);
		tf.setLowerThreshold(00);
		tf.setUpperThreshold(50);
		sf.setAmount(10);
		sf.setTurbulence(1f);
//		gf.setRadius(10);
		gf.setAmount(.1f);
		gf.setThreshold(.8f);
		gf.setLength(30);
		while (cf.isVisible()) {

			// long timenow=System.currentTimeMillis();
			// System.out.println(timenow-time);
			// time=timenow;
			image = grabber.grab();
cf.showImage(colorReduce(image));
//			// fastNlMeansDenoising(image, image, 3, 7, 21);
//			cvCvtColor(image, gray, CV_BGR2GRAY);
//			cvSmooth(gray, gray, CV_MEDIAN, MEDIAN_BLUR_FILTER_SIZE, 0, 0, 0);
//			cvLaplace(gray, edges, LAPLACIAN_FILTER_SIZE);
//
//			cvThreshold(edges, edges, EDGES_THRESHOLD, 255, CV_THRESH_BINARY_INV);
//			 cvErode(edges, edges, null,1);
//			 cvDilate(edges, edges, null,1);
//
//			for (int i = 0; i < repetitions; i++) {
//				cvSmooth(image, temp, CV_BILATERAL, ksize, 0, sigmaColor, sigmaSpace);
//				cvSmooth(temp, image, CV_BILATERAL, ksize, 0, sigmaColor, sigmaSpace);
//			}
//			temp = IplImage.create(image.cvSize(), image.depth(), image.nChannels());
//			cvZero(temp);
//			cvCopy(image, temp, edges);
//			
//			cf.showImage(colorReduce(temp));
//			System.out.print(cf.isVisible());
//			System.out.println(cf.isShowing());
		}
		System.exit(0);
	}

	public BufferedImage colorReduce(IplImage image) {
//		CvMat mtx = CvMat.createHeader(image.height(), image.width(), image.depth());
		BufferedImage bi=image.getBufferedImage();
		BufferedImage bi1=new BufferedImage(image.width(),image.height(),BufferedImage.TYPE_INT_ARGB);
		gf.filter(bi,bi1);
		
		BufferedImage bo=new BufferedImage(image.width(),image.height(),BufferedImage.TYPE_INT_ARGB);
		pf.filter(bi1,bo);

		BufferedImage bx=new BufferedImage(image.width(),image.height(),BufferedImage.TYPE_INT_ARGB);
		BufferedImage by=new BufferedImage(image.width(),image.height(),BufferedImage.TYPE_INT_ARGB);
		lf.filter(bi1, bx);
//		gf.filter(bx, by);
//		return IplImage.createFrom(bo);
		tf.filter(bx,bi1);
		invf.filter(bi1, bx);
//		return bx;
		//--
		applyGrayscaleMaskToAlpha(bo, bx);
		BufferedImage combined = new BufferedImage(image.width(), image.height(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = combined.getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, image.width(),image.height());
		g.drawImage(bo, 0, 0, null);
		sf.filter(combined, bx);
		sf.setTime(t+=.1f);
		return bx;
		//--
//		cvGetMat(image, mtx, null, 0);
//		// Total number of elements, combining components from each channel
//		for (int i = 0; i < mtx.rows(); i++) {
//			for (int j = 0; j < mtx.cols(); j++) {
//				CvScalar rgb = cvGet2D(mtx, i, j);
//				int r = (int) rgb.val(0);
//				int  g = (int) rgb.val(1);
//				int b = (int) rgb.val(2);
//				int rgbint = r;
//				rgbint = (rgbint << 8) + g;
//				rgbint = (rgbint << 8) + b;
////				r = r /gg ;
////				g = g/gg;
////				b = b/gg;
////				r=r*gg;
////				g=g*gg;
////				b=b*gg;
//				rgbint=rgbint/gg;
//				rgbint=rgbint*gg;
//				r= (rgbint >> 16) & 0xFF;
//				g= (rgbint >> 8) & 0xFF;
//				b= rgbint & 0xFF;
//				CvScalar scalar = new CvScalar();
//				scalar.setVal(0, r);
//				scalar.setVal(1, g);
//				scalar.setVal(2, b);
//				cvSet2D(mtx, i, j, scalar);
//			}
//		}

//		IplImage result = new IplImage(mtx);
//		return result;
//	return null;
	}
	
	public void applyGrayscaleMaskToAlpha(BufferedImage image, BufferedImage mask)
	{
	    int width = image.getWidth();
	    int height = image.getHeight();
	 
	    int[] imagePixels = image.getRGB(0, 0, width, height, null, 0, width);
	    int[] maskPixels = mask.getRGB(0, 0, width, height, null, 0, width);
	 
	    for (int i = 0; i < imagePixels.length; i++)
	    {
	        int color = imagePixels[i] & 0x00ffffff; // Mask preexisting alpha
	        int alpha = maskPixels[i] << 24; // Shift green to alpha
	        imagePixels[i] = color | alpha;
	    }
	 
	    image.setRGB(0, 0, width, height, imagePixels, 0, width);
	}
	       
//	for (int y = 0; y < image.getHeight(); y++) {
//	    for (int x = 0; x < image.getWidth(); x++) {
//	        Color c = new Color(image.getRGB(x, y));
//	        Color maskC = new Color(mask.getRGB(x, y));
//	        Color maskedColor = new Color(c.getRed(), c.getGreen(), c.getBlue(),
//	                maskC.getRed());
//	        resultImg.setRGB(x, y, maskedColor.getRGB());
//	    }
//	}

	public static IplImage render(IplImage image, AbstractBufferedImageOp rf) {
		BufferedImage bi = new BufferedImage(image.width(), image.height(),
				BufferedImage.TYPE_INT_ARGB);
		BufferedImage bi2 = new BufferedImage(image.width(), image.height(),
				BufferedImage.TYPE_INT_ARGB);
		bi.getGraphics().drawImage(image.getBufferedImage(), 0, 0, null);
		rf.filter(bi, bi2);
		BufferedImage bi1 = new BufferedImage(image.width(), image.height(),
				BufferedImage.TYPE_3BYTE_BGR);
		bi1.getGraphics().drawImage(bi2, 0, 0, null);
		image = IplImage.createFrom(bi1);
		return image;
	}
	
	public static void main(String[] args) throws Exception {
		new Main1();
	}

}

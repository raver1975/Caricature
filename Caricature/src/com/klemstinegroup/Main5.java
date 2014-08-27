package com.klemstinegroup;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvDrawContours;
import static com.googlecode.javacv.cpp.opencv_core.cvZero;
import static com.googlecode.javacv.cpp.opencv_core.cvNot;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BILATERAL;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_CLOCKWISE;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MEDIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_POLY_APPROX_DP;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_RETR_LIST;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY_INV;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvApproxPoly;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvBoundingRect;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvContourPerimeter;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvConvexHull2;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvFindContours;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvLaplace;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
//import static com.googlecode.javacv.cpp.opencv_imgproc.*;
//import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_core.*;
import com.googlecode.javacpp.Loader;
import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameRecorder;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.avcodec;
import com.googlecode.javacv.cpp.opencv_core.CvContour;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.GrayscaleFilter;
import com.jhlabs.image.LaplaceFilter;
import com.jhlabs.image.PosterizeFilter;
import com.jhlabs.image.SwimFilter;

public class Main5 {
	int EDGES_THRESHOLD = 70;
	int LAPLACIAN_FILTER_SIZE = 5;
	int MEDIAN_BLUR_FILTER_SIZE = 7;
	int repetitions = 7; // Repetitions for strong cartoon effect.
	int ksize = 1; // Filter size. Has a large effect on speed.
	double sigmaColor = 9; // Filter color strength.
	double sigmaSpace = 7; // Spatial strength. Affects speed.
	static int NUM_COLORS = 16;
	static int gg = (256 / NUM_COLORS);

	// SwimFilter sf = new SwimFilter();
	// SwimFilter sf1 = new SwimFilter();
	LaplaceFilter lf = new LaplaceFilter();
	GrayscaleFilter gf = new GrayscaleFilter();
	PosterizeFilter glf = new PosterizeFilter();

	CanvasFrame cf = new CanvasFrame("MyFaceIsMelting", 1);
	private float t1;
	private float t2;
	int frames = 1000;
	private int recthighgap = 100;
	private int rectwidthgap = 100;

	public Main5() throws Exception, com.googlecode.javacv.FrameRecorder.Exception {
		start();
	}

	public void start() throws Exception, com.googlecode.javacv.FrameRecorder.Exception {
		// sf.setAmount(20f);
		// sf.setTurbulence(1f);
		// sf.setEdgeAction(sf.CLAMP);
		// sf1.setEdgeAction(sf1.CLAMP);
		// sf1.setAmount(30f);
		// sf1.setTurbulence(1f);
		// sf1.setScale(300);
		// sf1.setStretch(50);
		glf.setNumLevels(NUM_COLORS);
		FrameGrabber grabber = new OpenCVFrameGrabber(0);
		grabber.start();
		cf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		IplImage image = grabber.grab();
		IplImage gray = IplImage.create(image.cvSize(), IPL_DEPTH_8U, 1);
		cvCvtColor(image, gray, CV_BGR2GRAY);
		IplImage edges = IplImage.create(gray.cvSize(), gray.depth(), gray.nChannels());
		IplImage temp = IplImage.create(image.cvSize(), image.depth(), image.nChannels());
		while (true) {
			// image = render(render(grabber.grab(), sf), sf1);
			image = grabber.grab();
			cvCvtColor(image, gray, CV_BGR2GRAY);
			cvSmooth(gray, gray, CV_MEDIAN, MEDIAN_BLUR_FILTER_SIZE, 0, 0, 0);
			cvLaplace(gray, edges, LAPLACIAN_FILTER_SIZE);
			cvThreshold(edges, edges, 80, 255, CV_THRESH_BINARY_INV);
			// cvErode(edges, edges, null,2);
			// cvDilate(edges, edges, null,1);
			// create contours around white regions
			// CvSeq contour = new CvSeq();
			// CvMemStorage storage = CvMemStorage.create();
			// cvSmooth(edges, edges, CV_MEDIAN, MEDIAN_BLUR_FILTER_SIZE, 0, 0,
			// 0);
			// cvNot(edges,edges);

			// cvFindContours(edges, storage, contour,
			// Loader.sizeof(CvContour.class), CV_RETR_TREE,
			// com.googlecode.javacv.cpp.opencv_imgproc.CV_CHAIN_APPROX_SIMPLE);//
			// CV_CHAIN_APPROX_SIMPLE);
			// // loop through all detected contours
			// cvZero(edges);
			// for (; contour != null && !contour.isNull(); contour =
			// contour.h_next()) {
			// // CvSeq approx = cvApproxPoly(contour,
			// // Loader.sizeof(CvContour.class), storage, CV_POLY_APPROX_DP,
			// // cvContourPerimeter(contour) * 0.001, 0);
			// // CvRect rec = cvBoundingRect(contour, 0);
			// // if (rec.height() > recthighgap && rec.width() > rectwidthgap)
			// {
			// int area= Math.abs((int) cvContourArea(contour, CV_WHOLE_SEQ,
			// -1));
			// int perimeter=(int) cvArcLength(contour, CV_WHOLE_SEQ, -1)+1;
			//
			// if (perimeter>100&&area>100&& area/perimeter==0){
			// // System.out.println(area+"\t"+perimeter+"\t"+(area/perimeter));
			// // CvMemStorage storage1 = CvMemStorage.create();
			// // CvSeq convexContour = cvConvexHull2(contour, storage1,
			// // CV_CLOCKWISE, 1);
			// cvDrawContours(edges, contour, CvScalar.WHITE,
			// CvScalar.WHITE, 127,1, 8);
			// }
			// }
			// cvNot(edges,edges);

			for (int i = 0; i < repetitions; i++) {
				cvSmooth(image, temp, CV_BILATERAL, ksize, 0, sigmaColor, sigmaSpace);
				cvSmooth(temp, image, CV_BILATERAL, ksize, 0, sigmaColor, sigmaSpace);
			}
			temp = IplImage.create(image.cvSize(), image.depth(), image.nChannels());
			cvZero(temp);

			cvCopy(image, temp, edges);
			// sf.setTime(t1 += .02f);
			// sf1.setTime(t2 += .02f);

			cf.showImage(render(temp, glf));
		}
	}

	public static void main(String[] args) throws Exception, com.googlecode.javacv.FrameRecorder.Exception {
		if (args.length>0){
			NUM_COLORS=Integer.parseInt(args[0]);
		}
		gg = (256 / NUM_COLORS);
		new Main5();
	}

	public static IplImage render(IplImage image, AbstractBufferedImageOp rf) {
		BufferedImage bi = new BufferedImage(image.width(), image.height(), BufferedImage.TYPE_INT_ARGB);
		BufferedImage bi2 = new BufferedImage(image.width(), image.height(), BufferedImage.TYPE_INT_ARGB);
		bi.getGraphics().drawImage(image.getBufferedImage(), 0, 0, null);
		rf.filter(bi, bi2);
		BufferedImage bi1 = new BufferedImage(image.width(), image.height(), BufferedImage.TYPE_3BYTE_BGR);
		bi1.getGraphics().drawImage(bi2, 0, 0, null);
		image = IplImage.createFrom(bi1);
		return image;
	}

	public static FFmpegFrameRecorder getRecorder(String name, int width, int height, double frameRate, int quality) {
		FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(name, width, height);
		// recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
		// recorder.setFormat("mp4");
		// recorder.setFrameRate(frameRate);
		// recorder.setVideoBitrate(114 * 1024 * 1024);
		// recorder.setSampleRate(sampleRate);
		// recorder.setSampleFormat(sampleFormat);

		recorder.setVideoCodec(1);
		// recorder.setFormat("mp4");
		recorder.setFrameRate(frameRate);
		recorder.setVideoBitrate(quality * 1024 * 1024);
		try {
			recorder.start();
		} catch (com.googlecode.javacv.FrameRecorder.Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return recorder;
	}

	public static IplImage copy(IplImage image) {
		// if (img==null||img.isNull())return null;
		IplImage copy = null;
		if (image.roi() != null)
			copy = IplImage.create(image.roi().width(), image.roi().height(), image.depth(), image.nChannels());
		else
			copy = IplImage.create(image.cvSize(), image.depth(), image.nChannels());
		cvCopy(image, copy);
		return copy;
	}

}

package com.klemstinegroup;

import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvZero;
import static com.googlecode.javacv.cpp.opencv_core.cvNot;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BILATERAL;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MEDIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY_INV;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvLaplace;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;

import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FFmpegFrameRecorder;
import com.googlecode.javacv.FrameGrabber;
import com.googlecode.javacv.FrameRecorder;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.avcodec;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.GrayscaleFilter;
import com.jhlabs.image.LaplaceFilter;
import com.jhlabs.image.PosterizeFilter;
import com.jhlabs.image.SwimFilter;

public class Main3 {
	int EDGES_THRESHOLD = 70;
	int LAPLACIAN_FILTER_SIZE = 5;
	int MEDIAN_BLUR_FILTER_SIZE = 7;
	int repetitions = 7; // Repetitions for strong cartoon effect.
	int ksize = 1; // Filter size. Has a large effect on speed.
	double sigmaColor = 9; // Filter color strength.
	double sigmaSpace = 7; // Spatial strength. Affects speed.
	int NUM_COLORS = 16;
	int gg = (256 / NUM_COLORS);

	SwimFilter sf = new SwimFilter();
	SwimFilter sf1 = new SwimFilter();
	LaplaceFilter lf = new LaplaceFilter();
	GrayscaleFilter gf = new GrayscaleFilter();
	PosterizeFilter glf = new PosterizeFilter();

	CanvasFrame cf = new CanvasFrame("Rotoscopor", 1);
	FFmpegFrameRecorder recorder;
	private float t1;
	private float t2;
	int frames=1000;

	public Main3() throws Exception, com.googlecode.javacv.FrameRecorder.Exception {
		// cf.setUndecorated(true);
		start();
	}

	public void start() throws Exception, com.googlecode.javacv.FrameRecorder.Exception {

		sf.setAmount(20f);
		sf.setTurbulence(1f);
		// sf.setScale(200);
		// sf.setStretch(50);
		sf.setEdgeAction(sf.CLAMP);

		sf1.setEdgeAction(sf1.CLAMP);
		sf1.setAmount(30f);
		sf1.setTurbulence(1f);
		sf1.setScale(300);
		sf1.setStretch(50);

		// sf.setAngle(angle);

		// glf.setAmount(.01f);
		glf.setNumLevels(10);
		// glf.setLength(300);
		// glf.setGlintOnly(true);
		// glf.setThreshold(.99f);
		// glf.setAmount(.2f);
		// glf.setRadius(5);
		// glf.setEdgeAction(glf.CLAMP_EDGES);
		FrameGrabber grabber = new OpenCVFrameGrabber(0);
		grabber.start();
		cf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		IplImage image = grabber.grab();
		IplImage gray = IplImage.create(image.cvSize(), IPL_DEPTH_8U, 1);
		cvCvtColor(image, gray, CV_BGR2GRAY);
		IplImage edges = IplImage.create(gray.cvSize(), gray.depth(), gray.nChannels());
		IplImage temp = IplImage.create(image.cvSize(), image.depth(), image.nChannels());
		IplImage copy = IplImage.create(image.cvSize(), image.depth(), image.nChannels());
		// long time=System.currentTimeMillis();
//		recorder = this.getRecorder("out.mp4", image.width(), image.height(), 30, 20);
//
//		for (int num = 0; num <frames; num++) {
//			image = grabber.grab();
//			cf.showImage(image);
//			recorder.record(image);
//		}
//		recorder.stop();
		recorder = this.getRecorder("out1.mp4", image.width(), image.height(), 30, 10);
		grabber = new FFmpegFrameGrabber("out.mp4");
		grabber.start();
		

		for (int num = 0; num <frames; num++) {

			// long timenow=System.currentTimeMillis();
			// System.out.println(timenow-time);
			// time=timenow;
			image = render(render(render(grabber.grab(), glf), sf), sf1);
			// cvCopy(image, copy);
			// colorReduce(image);
			// fastNlMeansDenoising(image, image, 3, 7, 21);
			cvCvtColor(image, gray, CV_BGR2GRAY);
			cvSmooth(gray, gray, CV_MEDIAN, MEDIAN_BLUR_FILTER_SIZE, 0, 0, 0);
			cvLaplace(gray, edges, LAPLACIAN_FILTER_SIZE);

			// edges=render(image,lf);
			// cvSmooth(edges, edges, CV_MEDIAN, MEDIAN_BLUR_FILTER_SIZE, 0, 0,
			// 0);

			cvThreshold(edges, edges, 80, 255, CV_THRESH_BINARY_INV);

			// cvCvtColor(image, gray, CV_BGR2GRAY);
			// cvSmooth(gray, gray, CV_MEDIAN, MEDIAN_BLUR_FILTER_SIZE, 0, 0,
			// 0);
			// cvLaplace(gray, edges, LAPLACIAN_FILTER_SIZE);
			//
			// cvThreshold(edges, edges, EDGES_THRESHOLD, 255,
			// CV_THRESH_BINARY_INV);
			// cvErode(edges, edges, null,1);
			// cvDilate(edges, edges, null,2);

			for (int i = 0; i < repetitions; i++) {
				cvSmooth(image, temp, CV_BILATERAL, ksize, 0, sigmaColor, sigmaSpace);
				cvSmooth(temp, image, CV_BILATERAL, ksize, 0, sigmaColor, sigmaSpace);
			}
			temp = IplImage.create(image.cvSize(), image.depth(), image.nChannels());
			cvZero(temp);
			// cvNot(temp,temp);
			// cvAnd(image, edges, temp,null);
			cvCopy(image, temp, edges);
			sf.setTime(t1 += .01f);
			sf1.setTime(t2 += .01f);

			cf.showImage(temp);
			recorder.record(temp);
		}
		recorder.stop();
		System.exit(0);
	}

	// public IplImage colorReduce(IplImage image) {
	// CvMat mtx = CvMat.createHeader(image.height(), image.width(),
	// image.depth());
	// cvGetMat(image, mtx, null, 0);
	// // Total number of elements, combining components from each channel
	// for (int i = 0; i < mtx.rows(); i++) {
	// for (int j = 0; j < mtx.cols(); j++) {
	// CvScalar rgb = cvGet2D(mtx, i, j);
	// int r = (int) rgb.val(0);
	// int g = (int) rgb.val(1);
	// int b = (int) rgb.val(2);
	// int rgbint = r;
	// rgbint = (rgbint << 8) + g;
	// rgbint = (rgbint << 8) + b;
	// // r = r /gg ;
	// // g = g/gg;
	// // b = b/gg;
	// // r=r*gg;
	// // g=g*gg;
	// // b=b*gg;
	// rgbint=rgbint/gg;
	// rgbint=rgbint*gg;
	// r= (rgbint >> 16) & 0xFF;
	// g= (rgbint >> 8) & 0xFF;
	// b= rgbint & 0xFF;
	// CvScalar scalar = new CvScalar();
	// scalar.setVal(0, r);
	// scalar.setVal(1, g);
	// scalar.setVal(2, b);
	// cvSet2D(mtx, i, j, scalar);
	// }
	// }
	//
	// IplImage result = new IplImage(mtx);
	// return result;
	// }

	// private IplImage specialEffects(IplImage temp) {

	// }
	public static void main(String[] args) throws Exception, com.googlecode.javacv.FrameRecorder.Exception {
		new Main3();
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
//		recorder.setVideoCodec(avcodec.AV_CODEC_ID_MPEG4);
		// recorder.setFormat("mp4");
		// recorder.setFrameRate(frameRate);
		// recorder.setVideoBitrate(114 * 1024 * 1024);
		// recorder.setSampleRate(sampleRate);
		// recorder.setSampleFormat(sampleFormat);

		 recorder.setVideoCodec(1);
//		recorder.setFormat("mp4");
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

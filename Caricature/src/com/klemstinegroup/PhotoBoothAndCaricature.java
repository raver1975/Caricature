package com.klemstinegroup;

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
import com.googlecode.javacpp.*;
import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.opencv_core.*;

import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_features2d.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_video.*;
import static com.googlecode.javacv.cpp.opencv_calib3d.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_core.cvClearMemStorage;
import static com.googlecode.javacv.cpp.opencv_core.cvCopy;
import static com.googlecode.javacv.cpp.opencv_core.cvGetSeqElem;
import static com.googlecode.javacv.cpp.opencv_core.cvLoad;
import static com.googlecode.javacv.cpp.opencv_core.cvPoint;
import static com.googlecode.javacv.cpp.opencv_core.cvRectangle;
import static com.googlecode.javacv.cpp.opencv_core.cvResetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvSetImageROI;
import static com.googlecode.javacv.cpp.opencv_core.cvZero;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BILATERAL;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_MEDIAN;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_THRESH_BINARY_INV;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvLaplace;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvSmooth;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvThreshold;
import static com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
import static com.googlecode.javacv.cpp.opencv_objdetect.cvHaarDetectObjects;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.print.PrintException;
import javax.swing.JOptionPane;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.GrayscaleFilter;
import com.jhlabs.image.LaplaceFilter;
import com.jhlabs.image.PosterizeFilter;
import com.jhlabs.image.SwimFilter;

public class PhotoBoothAndCaricature implements KeyListener, Printable {
	public static final String FACE_XML_FILE = "haarcascade_frontalface_alt.xml";
	public static final String NOSE_XML_FILE = "nose.xml";
	public static int stretchX = 20;
	public static int stretchY = 20;
	float size = 3.f;
	PosterizeFilter pf = new PosterizeFilter();
	static int randmust = 0;
	public boolean running = true;
	CvFont font = new CvFont();

	// CanvasFrame cf = new CanvasFrame("Caricature");
	CanvasFrame cf1 = new CanvasFrame("Caricature1");
	CanvasFrame cf2 = new CanvasFrame("Caricature2");
	int x1, y1, x2, y2;
	private boolean mustdetect;
	static boolean mustacheOn = false;
	private static boolean acid;
	int posterizelevels = 5;
	private BufferedImage saveImage;
	//
	PrinterJob job = PrinterJob.getPrinterJob();
	private boolean printsmall = true;
	int maxwidth = printsmall ? 187 : 384; // 187,384

	int EDGES_THRESHOLD = 70;
	int LAPLACIAN_FILTER_SIZE = 5;    //5
	int MEDIAN_BLUR_FILTER_SIZE = 7;  //7
 	int repetitions = 7; // Repetitions for strong cartoon effect.
	int ksize = 1; // Filter size. Has a large effect on speed.
	double sigmaColor = 9; // Filter color strength.
	double sigmaSpace = 7; // Spatial strength. Affects speed.
	int NUM_COLORS = 16;
	int gg = (256 / NUM_COLORS);
	private float t1;
	private float t2;

	SwimFilter sf = new SwimFilter();
	SwimFilter sf1 = new SwimFilter();
	LaplaceFilter lf = new LaplaceFilter();
	GrayscaleFilter gf = new GrayscaleFilter();
	// PosterizeFilter glf = new PosterizeFilter();

	public PhotoBoothAndCaricature() throws Exception {
		// new Thread(new Runnable() {
		// public void run() {
		// try {
		// start();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// }).start();
		start();
	}

	public void start() throws Exception {
		cvInitFont(font, CV_FONT_NORMAL, .5f, .5f, 0, 1, CV_AA);
		sf.setAmount(10f);
		sf.setTurbulence(1f);
		sf.setEdgeAction(sf.CLAMP);
		sf1.setEdgeAction(sf1.CLAMP);
		sf1.setAmount(20f);
		sf1.setTurbulence(1f);
		sf1.setScale(300);
		sf1.setStretch(30);
		// glf.setNumLevels(2);
		// cf.getCanvas().addKeyListener(this);
		cf1.getCanvas().addKeyListener(this);
		cf2.getCanvas().addKeyListener(this);
		// cf.getCanvas().setFocusable(true);
		cf1.getCanvas().setFocusable(true);
		cf2.getCanvas().setFocusable(true);
		// cf.addWindowListener(new java.awt.event.WindowAdapter() {
		// @Override
		// public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		// System.out.println("exiting");
		// running = false;
		// System.exit(-1);Runtime.getRuntime().halt(-1);
		// }
		// });
		cf1.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				System.out.println("exiting");
				running = false;
				System.exit(-1);
				Runtime.getRuntime().halt(-1);
			}
		});
		cf2.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				System.out.println("exiting");
				running = false;
				System.exit(-1);
				Runtime.getRuntime().halt(-1);
			}
		});

		pf.setNumLevels(posterizelevels);

		File f = new File("images");
		if (!f.exists())
			f.mkdir();
		BufferedImage mustache = null;
		try {
			mustache = thresholdImage(ImageIO.read(new File("mustache.png")), 16);
		} catch (IOException e) {
			e.printStackTrace();
		}
		OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
		grabber.start();

		IplImage image = grabber.grab();

		CvHaarClassifierCascade face_cascade = new CvHaarClassifierCascade(cvLoad(FACE_XML_FILE));
		CvHaarClassifierCascade nose_cascade = new CvHaarClassifierCascade(cvLoad(NOSE_XML_FILE));
		CvRect r = new CvRect(image.width() / 2 - 200, image.height() / 2 - 200, 400, 400);
		IplImage gray = IplImage.create(image.cvSize(), IPL_DEPTH_8U, 1);
		cvCvtColor(image, gray, CV_BGR2GRAY);
		IplImage edges = IplImage.create(gray.cvSize(), gray.depth(), gray.nChannels());
		IplImage temp12 = IplImage.create(image.cvSize(), image.depth(), image.nChannels());
		while (running) {
			image = grabber.grab();

			CvMemStorage storage = CvMemStorage.create();
			CvSeq sign = cvHaarDetectObjects(image, face_cascade, storage, 1.5, 3, CV_HAAR_DO_CANNY_PRUNING);

			int total_Faces = sign.total();
			if (total_Faces > 0) {
				CvRect r2 = new CvRect(cvGetSeqElem(sign, 0));
				int maxw = r2.x() + r2.width();
				int maxh = r2.y() + r2.height();
				// detect nose
				cvSetImageROI(image, r2);
				IplImage face = copy(image);
				cvResetImageROI(image);
				CvMemStorage storage1 = CvMemStorage.create();
				CvSeq sign1 = cvHaarDetectObjects(face, nose_cascade, storage1, 1.15, 3,
						com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_SCALE_IMAGE
								| com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING
								| com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_FIND_BIGGEST_OBJECT);
				face.release();
				int total_mouth = sign1.total();
				if (total_mouth > 0) {
					CvRect r3 = new CvRect(cvGetSeqElem(sign1, 0));
					x1 = (int) ((float) r3.width() * size);
					y1 = r3.height();
					x2 = r3.x() + r2.x() - ((int) ((float) r3.width() / 1f)) + 5;
					y2 = r3.y() + r2.y() + r3.height() / 2 + 5;
					mustdetect = true;
				}

				cvClearMemStorage(storage1);

				if (mustacheOn && mustdetect) {
					BufferedImage combined = image.getBufferedImage();
					Graphics g = combined.getGraphics();
					g.drawImage(resizeImage(mustache, x1, y1), x2, y2, null);
					image.release();
					image = IplImage.createFrom(combined);
					mustdetect = false;
				}

				for (int i = 0; i < total_Faces; i++) {
					CvRect r1 = new CvRect(cvGetSeqElem(sign, i));
					r2.x(Math.min(r2.x(), r1.x()));
					r2.y(Math.min(r2.y(), r1.y()));
					if (r1.x() + r1.width() > maxw)
						maxw = r1.x() + r1.width();
					if (r1.y() + r1.height() > maxh)
						maxh = r1.y() + r1.height();
					r2.width(maxw - r2.x());
					r2.height(maxh - r2.y());
				}
				r.x(r.x() + (r2.x() - stretchX - r.x()) / 5);
				r.y(r.y() + (r2.y() - 2 * stretchY - r.y()) / 5);
				r.width(r.width() + (r2.width() + 2 * stretchX - r.width()) / 5);
				r.height(r.height() + (r2.height() + 3 * stretchY - r.height()) / 5);

			} else {
				r.x(r.x() - 6);
				r.y(r.y() - 6);
				r.width(r.width() + 10);
				r.height(r.height() + 10);
			}
			if (r.x() < 0)
				r.x(0);
			if (r.y() < 0)
				r.y(0);
			if (r.width() > image.width())
				r.width(image.width());
			if (r.height() > image.height())
				r.height(image.height());

			cvClearMemStorage(storage);

			if (acid) {
				// ---------------------------------------------------------------
				IplImage copy11 = copy(image);
				copy11 = render(render(copy11, sf), sf1);
				cvCvtColor(copy11, gray, CV_BGR2GRAY);
				cvSmooth(gray, gray, CV_MEDIAN, MEDIAN_BLUR_FILTER_SIZE, 0, 0, 0);
				cvLaplace(gray, edges, LAPLACIAN_FILTER_SIZE);
				cvThreshold(edges, edges, 80, 255, CV_THRESH_BINARY_INV);
				temp12 = IplImage.create(copy11.cvSize(), copy11.depth(), copy11.nChannels());
				for (int i = 0; i < repetitions; i++) {
					cvSmooth(copy11, temp12, CV_BILATERAL, ksize, 0, sigmaColor, sigmaSpace);
					cvSmooth(temp12, copy11, CV_BILATERAL, ksize, 0, sigmaColor, sigmaSpace);
				}

				temp12 = IplImage.create(copy11.cvSize(), copy11.depth(), copy11.nChannels());
				cvZero(temp12);

				cvCopy(copy11, temp12, edges);
				sf.setTime(t1 += .07f);
				sf1.setTime(t2 += .05f);

				// IplImage b = render(temp12, glf);
				// cvSetImageROI(temp12, r);
				// IplImage copy66 = copy(temp12);
				// cvResetImageROI(temp12);
				// b.release();

			}

			else {
				temp12 = copy(image);
			}
			cf1.showImage(temp12);
			// -----------------------------------------------------------------------------------------------------
			// cf1.showImage(copy);
			IplImage copy = copy(temp12);
			cvResetImageROI(temp12);
			cvSetImageROI(temp12, r);
			copy.release();
			copy = copy(temp12);
			cvResetImageROI(temp12);

			int newheight = (int) ((maxwidth / (double) copy.width()) * copy.height());
			IplImage copy1 = IplImage.createFrom((AutoCorrectionFilter.filter(copy.getBufferedImage())));
			copy = IplImage
					.createFrom(resizeImage(AutoCorrectionFilter.filter(copy.getBufferedImage()), maxwidth, newheight));
			
			IplImage gray3 = IplImage.create(copy.cvSize(), IPL_DEPTH_8U, 1);
			IplImage gray4 = IplImage.create(copy1.cvSize(), IPL_DEPTH_8U, 1);
			cvCvtColor(copy, gray3, CV_BGR2GRAY);
			cvCvtColor(copy1, gray4, CV_BGR2GRAY);
			gray3 = render(gray3, pf);
			gray4 = render(gray4, pf);
			System.out.println(gray4.width()+","+gray4.height());
			System.out.println(copy1.width()+","+copy1.height());

			cvRectangle(image, cvPoint(r.x(), r.y()), cvPoint(r.width() + r.x(), r.height() + r.y()), CvScalar.RED, 2,
					CV_AA, 0);
			if (acid)
				cvPutText(image, "Acid", new CvPoint(10, 15), font, CvScalar.RED);
			if (mustacheOn)
				cvPutText(image, "Mustache", new CvPoint(10, 30), font, CvScalar.RED);
			cvPutText(image, "F11 to Print", new CvPoint(525, 15), font, CvScalar.RED);

			// saveImage = floydSteinbergDithering(gray3.getBufferedImage());
			saveImage = gray3.getBufferedImage();

			IplImage _3image = IplImage.create(gray4.width(), gray4.height(), image.depth(), image.nChannels());
			cvConvertScale(gray4, _3image, 1, 0);
			// System.out.println(gray3.nChannels());
			// System.out.println(_3image.nChannels());
			// cvCvtColor(gray3,_3image,CV_GRAY2BGR);

			cvSetImageROI(image, r);
			System.out.println(image.depth() + "\t" + image.roi().width() + "," + image.roi().height());
			System.out.println(_3image.depth() + "\t" + _3image.width() + "," + _3image.height());
			cvCopy(_3image, image);
			cvResetImageROI(image);
			cf2.showImage(image);
			_3image.release();
			// cf.showImage(saveImage);
			copy.release();
			gray3.release();
			image.release();
			System.gc();
		}
		System.out.println("exiting");
		System.exit(-1);
		Runtime.getRuntime().halt(-1);
	}

	public static BufferedImage thresholdImage(BufferedImage image, int threshold) {
		WritableRaster raster = image.getRaster();
		int[] pixels = new int[image.getWidth() * 4];
		for (int y = 0; y < image.getHeight(); y++) {
			raster.getPixels(0, y, image.getWidth(), 1, pixels);
			for (int i = 0; i < pixels.length; i += 4) {
				if (pixels[i + 3] < 40) {
					pixels[i] = 0;
					pixels[i + 1] = 0;
					pixels[i + 2] = 0;
					pixels[i + 3] = 0;
				} else {
					int h = (int) (Math.random() * randmust);
					pixels[i] = h;
					pixels[i + 1] = h;
					pixels[i + 2] = h;
					pixels[i + 3] = 255;
				}
			}
			raster.setPixels(0, y, image.getWidth(), 1, pixels);
		}
		return image;
	}

	public static void main(String[] args) throws Exception {
		new PhotoBoothAndCaricature();
	}

	public static IplImage copy(IplImage image) {
		IplImage copy = null;
		if (image.roi() != null)
			copy = IplImage.create(image.roi().width(), image.roi().height(), image.depth(), image.nChannels());
		else
			copy = IplImage.create(image.cvSize(), image.depth(), image.nChannels());
		cvCopy(image, copy);
		return copy;
	}

	public static IplImage render(IplImage image, AbstractBufferedImageOp rf) {

		BufferedImage bi = new BufferedImage(image.width(), image.height(), BufferedImage.TYPE_INT_ARGB);
		BufferedImage bi2 = new BufferedImage(image.width(), image.height(), BufferedImage.TYPE_INT_ARGB);
		bi.getGraphics().drawImage(image.getBufferedImage(), 0, 0, null);
		rf.filter(bi, bi2);
		BufferedImage bi1 = new BufferedImage(image.width(), image.height(), BufferedImage.TYPE_3BYTE_BGR);
		bi1.getGraphics().drawImage(bi2, 0, 0, null);
		image.release();
		image = IplImage.createFrom(bi1);
		return image;
	}

	static public BufferedImage resizeImage(BufferedImage image, int width, int height) {
		int type = 0;
		type = image.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : image.getType();
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_A)
			PhotoBoothAndCaricature.acid = !PhotoBoothAndCaricature.acid;
		if (e.getKeyCode() == KeyEvent.VK_M)
			PhotoBoothAndCaricature.mustacheOn = !PhotoBoothAndCaricature.mustacheOn;
		if (e.getKeyCode() == KeyEvent.VK_F11) {
			Toolkit.getDefaultToolkit().beep();
			try {

				System.out.println("pressed");
				ImageIO.write(saveImage, "png", new File("./images/pic" + System.currentTimeMillis() + ".png"));
				try {
					PrinterTSP.print(saveImage);
				} catch (PrintException e1) {
					e1.printStackTrace();
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent paramKeyEvent) {

	}

	@Override
	public int print(Graphics paramGraphics, PageFormat paramPageFormat, int page) throws PrinterException {
		if (page > 0) {
			return NO_SUCH_PAGE;
		}
		Graphics2D g2d = (Graphics2D) paramGraphics;
		AffineTransform pOrigTransform = g2d.getTransform();
		g2d.translate(paramPageFormat.getImageableX(), paramPageFormat.getImageableY());
		g2d.drawImage(saveImage, 0, 0, null);
		g2d.setTransform(pOrigTransform);
		return PAGE_EXISTS;
	}

}
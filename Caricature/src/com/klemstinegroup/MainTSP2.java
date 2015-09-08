package com.klemstinegroup;

import static com.googlecode.javacv.cpp.opencv_core.CV_AA;
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.print.PrintException;
import javax.swing.JFrame;

import com.googlecode.javacv.CanvasFrame;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.OpenCVFrameGrabber;
import com.googlecode.javacv.cpp.opencv_core.CvMemStorage;
import com.googlecode.javacv.cpp.opencv_core.CvRect;
import com.googlecode.javacv.cpp.opencv_core.CvScalar;
import com.googlecode.javacv.cpp.opencv_core.CvSeq;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_objdetect.CvHaarClassifierCascade;
import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.GrayscaleFilter;
import com.jhlabs.image.LaplaceFilter;
import com.jhlabs.image.PosterizeFilter;
import com.jhlabs.image.SwimFilter;

public class MainTSP2 implements KeyListener, Printable {
	public static final String FACE_XML_FILE = "haarcascade_frontalface_alt.xml";
	public static final String NOSE_XML_FILE = "nose.xml";
	public static int stretchX = 40;
	public static int stretchY = 40;
	float size = 3.f;
	PosterizeFilter pf = new PosterizeFilter();
	static int randmust = 0;

	CanvasFrame cf = new CanvasFrame("Caricature");
	CanvasFrame cf1 = new CanvasFrame("Caricature1");
	CanvasFrame cf2 = new CanvasFrame("Caricature2");
	int x1, y1, x2, y2;
	private boolean mustdetect;
	static boolean mustacheOn = false;
	int posterizelevels = 5;
	private BufferedImage saveImage;
	//
	PrinterJob job = PrinterJob.getPrinterJob();
	private boolean printsmall = true;
	int maxwidth = printsmall ? 187 : 384; // 187,384

	int EDGES_THRESHOLD = 70;
	int LAPLACIAN_FILTER_SIZE = 5;
	int MEDIAN_BLUR_FILTER_SIZE = 7;
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
	//PosterizeFilter glf = new PosterizeFilter();

	public MainTSP2() throws Exception {
		new Thread(new Runnable() {
			public void run() {
				try {
					start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void start() throws Exception {
		sf.setAmount(20f);
		sf.setTurbulence(1f);
		sf.setEdgeAction(sf.CLAMP);
		sf1.setEdgeAction(sf1.CLAMP);
		sf1.setAmount(30f);
		sf1.setTurbulence(1f);
		sf1.setScale(300);
		sf1.setStretch(50);
		//glf.setNumLevels(2);
		cf.getCanvas().addKeyListener(this);
		cf1.getCanvas().addKeyListener(this);
		cf2.getCanvas().addKeyListener(this);
		cf.getCanvas().setFocusable(true);
		cf1.getCanvas().setFocusable(true);
		cf2.getCanvas().setFocusable(true);
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

		cf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cf1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cf2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		IplImage image = grabber.grab();

		CvHaarClassifierCascade face_cascade = new CvHaarClassifierCascade(cvLoad(FACE_XML_FILE));
		CvHaarClassifierCascade nose_cascade = new CvHaarClassifierCascade(cvLoad(NOSE_XML_FILE));
		CvRect r = new CvRect(image.width() / 2 - 200, image.height() / 2 - 200, 400, 400);
		IplImage gray = IplImage.create(image.cvSize(), IPL_DEPTH_8U, 1);
		cvCvtColor(image, gray, CV_BGR2GRAY);
		IplImage edges = IplImage.create(gray.cvSize(), gray.depth(), gray.nChannels());
		IplImage temp12 = IplImage.create(image.cvSize(), image.depth(), image.nChannels());
		while (cf.isVisible()) {
			image = grabber.grab();

			if (mustacheOn && mustdetect) {
				BufferedImage combined = image.getBufferedImage();
				Graphics g = combined.getGraphics();
				g.drawImage(resizeImage(mustache, x1, y1), x2, y2, null);
				image.release();
				image = IplImage.createFrom(combined);
				mustdetect = false;
			}

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
				r.x(r.x() - 3);
				r.y(r.y() - 3);
				r.width(r.width() + 5);
				r.height(r.height() + 5);
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
			sf.setTime(t1 += .02f);
			sf1.setTime(t2 += .02f);

			//IplImage b = render(temp12, glf);
			//cvSetImageROI(temp12, r);
			//IplImage copy66 = copy(temp12);
			//cvResetImageROI(temp12);
			// b.release();
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
			copy = IplImage
					.createFrom(resizeImage(AutoCorrectionFilter.filter(copy.getBufferedImage()), maxwidth, newheight));
			IplImage gray3 = IplImage.create(copy.cvSize(), IPL_DEPTH_8U, 1);
			cvCvtColor(copy, gray3, CV_BGR2GRAY);
			gray3 = render(gray3, pf);

			cvRectangle(image, cvPoint(r.x(), r.y()), cvPoint(r.width() + r.x(), r.height() + r.y()), CvScalar.RED, 2,
					CV_AA, 0);
			cf2.showImage(image);
			saveImage = gray3.getBufferedImage();
			cf.showImage(saveImage);
			copy.release();
			gray3.release();
			image.release();
			System.gc();
		}
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
		new MainTSP2();
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

	static class C3 {
		int r, g, b;

		public C3(int c) {
			Color color = new Color(c);
			this.r = color.getRed();
			this.g = color.getGreen();
			this.b = color.getBlue();
		}

		public C3(int r, int g, int b) {
			this.r = r;
			this.g = g;
			this.b = b;
		}

		public C3 add(C3 o) {
			return new C3(r + o.r, g + o.g, b + o.b);
		}

		public C3 sub(C3 o) {
			return new C3(r - o.r, g - o.g, b - o.b);
		}

		public C3 mul(double d) {
			return new C3((int) (d * r), (int) (d * g), (int) (d * b));
		}

		public int diff(C3 o) {
			int Rdiff = o.r - this.r;
			int Gdiff = o.g - this.g;
			int Bdiff = o.b - this.b;
			int distanceSquared = Rdiff * Rdiff + Gdiff * Gdiff + Bdiff * Bdiff;
			return distanceSquared;
		}

		public int toRGB() {
			return toColor().getRGB();
		}

		public Color toColor() {
			return new Color(clamp(r), clamp(g), clamp(b));
		}

		public int clamp(int c) {
			return Math.max(0, Math.min(255, c));
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_M)
			MainTSP2.mustacheOn = !MainTSP2.mustacheOn;
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
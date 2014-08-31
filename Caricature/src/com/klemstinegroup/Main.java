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
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;
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
import com.jhlabs.image.PosterizeFilter;

public class Main implements KeyListener, Printable {
	public static final String FACE_XML_FILE = "haarcascade_frontalface_alt.xml";
	public static final String NOSE_XML_FILE = "nose.xml";
	public static int stretchX = 40;
	public static int stretchY = 80;
	float size = 2.f;
	PosterizeFilter pf = new PosterizeFilter();
	static int randmust = 100;
	
	CanvasFrame cf = new CanvasFrame("Caricature");
	CanvasFrame cf1 = new CanvasFrame("Caricature1");
	CanvasFrame cf2 = new CanvasFrame("Caricature2");
	int x1, y1, x2, y2;
	private boolean mustdetect;
	static boolean mustacheOn = true;
	int posterizelevels = 5;
	private BufferedImage saveImage;
//	PrinterJob job = PrinterJob.getPrinterJob();
	private boolean printsmall=true;
	int maxwidth = printsmall?187:384;   //187,384
	
	public Main() throws Exception {
		new Thread(new Runnable() {
			public void run() {
				try {
					start();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void start() throws Exception {
//		PageFormat format = new PageFormat();
//		Paper paper = new Paper();
//
//		double paperWidth = 2.25;// 3.25
//		double paperHeight = 2.25;// 11.69
//		double leftMargin = 0.12;
//		double rightMargin = 0.10;
//		double topMargin = 0;
//		double bottomMargin = 0.01;
//		paper.setSize(paperWidth * 72, paperHeight * 72);
//		paper.setImageableArea(leftMargin * 72, topMargin * 72, (paperWidth - leftMargin - rightMargin) * 72, (paperHeight - topMargin - bottomMargin) * 72);
//
//		format.setPaper(paper);
//		format.setOrientation(PageFormat.LANDSCAPE);
//		job.setPrintable(this,format);
//		job.printDialog();
		// KeyboardFocusManager manager =
		// KeyboardFocusManager.getCurrentKeyboardFocusManager();
		// manager.addKeyEventDispatcher(new MyDispatcher());
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
		grabber.setImageWidth(1600);
		grabber.setImageHeight(1200);
		// grabber.setImageWidth(1280);
		// grabber.setImageHeight(1024);
		grabber.start();

		cf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cf1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		cf2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		IplImage image = grabber.grab();
		CvHaarClassifierCascade face_cascade = new CvHaarClassifierCascade(cvLoad(FACE_XML_FILE));
		CvHaarClassifierCascade nose_cascade = new CvHaarClassifierCascade(cvLoad(NOSE_XML_FILE));
		CvRect r = new CvRect(image.width() / 2 - 200, image.height() / 2 - 200, 400, 400);
		while (cf.isVisible()) {
			image = grabber.grab();
			CvMemStorage storage = CvMemStorage.create();
			CvSeq sign = cvHaarDetectObjects(image, face_cascade, storage, 1.5, 3, CV_HAAR_DO_CANNY_PRUNING);

			int total_Faces = sign.total();
			if (total_Faces > 0) {
				CvRect r2 = new CvRect(cvGetSeqElem(sign, 0));
				// cvRectangle(image, cvPoint(r2.x(), r2.y()),
				// cvPoint(r2.width() + r2.x(), r2.height() + r2.y()),
				// CvScalar.BLUE, 2, CV_AA, 0);
				int maxw = r2.x() + r2.width();
				int maxh = r2.y() + r2.height();
				// detect nose
				cvSetImageROI(image, r2);
				IplImage face = copy(image);
				cvResetImageROI(image);
				CvMemStorage storage1 = CvMemStorage.create();
				CvSeq sign1 = cvHaarDetectObjects(face, nose_cascade, storage1, 1.15, 3, com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_SCALE_IMAGE | com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING | com.googlecode.javacv.cpp.opencv_objdetect.CV_HAAR_FIND_BIGGEST_OBJECT);
				face.release();
				int total_mouth = sign1.total();
				if (total_mouth > 0) {
					CvRect r3 = new CvRect(cvGetSeqElem(sign1, 0));
					// cvRectangle(
					// image,
					// cvPoint(r3.x() + r2.x(), r3.y() + r2.y()),
					// cvPoint(r3.width() / 2 + r2.x() + r3.x(),
					// r3.height() + r2.y() + r3.y()),
					// CvScalar.YELLOW, 2, CV_AA, 0);

					// paint both images, preserving the alpha channels

					x1 = (int) ((float) r3.width() * size);
					y1 = r3.height();
					x2 = r3.x() + r2.x() - ((int) ((float) r3.width() / 2f)) + 5;
					y2 = r3.y() + r2.y() + r3.height() / 2 + 5;
					mustdetect = true;
				}

				cvClearMemStorage(storage1);

				for (int i = 0; i < total_Faces; i++) {
					CvRect r1 = new CvRect(cvGetSeqElem(sign, i));
					// cvRectangle(image, cvPoint(r1.x(), r1.y()),
					// cvPoint(r1.width() + r1.x(), r1.height() + r1.y()),
					// CvScalar.BLUE, 2, CV_AA, 0);

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
				if (r.x() < 0)
					r.x(0);
				if (r.y() < 0)
					r.y(0);
				if (r.width() > image.width())
					r.width(image.width());
				if (r.height() > image.height())
					r.height(image.height());
			}
			cvClearMemStorage(storage);

			cvSetImageROI(image, r);
			IplImage copy = copy(image);
			cvResetImageROI(image);
			cf1.showImage(copy);

			if (mustacheOn && mustdetect) {
				BufferedImage combined = image.getBufferedImage();
				Graphics g = combined.getGraphics();
				g.drawImage(resizeImage(mustache, x1, y1), x2, y2, null);
				image.release();
				image = IplImage.createFrom(combined);
				mustdetect = false;
				cvSetImageROI(image, r);
				copy.release();
				copy = copy(image);
				cvResetImageROI(image);
				// cf1.showImage(copy);
			}
			// equalize
			// copy = IplImage.createFrom(HistogramEQ.histogramEqualization(copy
			// .getBufferedImage(1f, false)));
			// try {
			// copy =
			// IplImage.createFrom(HistogramEqualization.HistogramEqualization(copy
			// .getBufferedImage(1f, false)));
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			int newheight = (int) ((maxwidth / (double) copy.width()) * copy.height());
			copy = IplImage.createFrom(resizeImage(AutoCorrectionFilter.filter(copy.getBufferedImage()), maxwidth, newheight));
			IplImage gray = IplImage.create(copy.cvSize(), IPL_DEPTH_8U, 1);
			cvCvtColor(copy, gray, CV_BGR2GRAY);
			gray = render(gray, pf);

			cvRectangle(image, cvPoint(r.x(), r.y()), cvPoint(r.width() + r.x(), r.height() + r.y()), CvScalar.RED, 2, CV_AA, 0);
			cf2.showImage(image);

			// cvThreshold(gray, gray, EDGES_THRESHOLD, 255, CV_THRESH_BINARY);
			saveImage = floydSteinbergDithering(gray.getBufferedImage());
			cf.showImage(saveImage);
			copy.release();
			gray.release();
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
				// System.out.println(pixels[i+3]+" ");
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
		new Main();
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

	private static C3 findClosestPaletteColor(C3 c, C3[] palette) {
		C3 closest = palette[0];

		for (C3 n : palette)
			if (n.diff(c) < closest.diff(c))
				closest = n;

		return closest;
	}

	private static BufferedImage floydSteinbergDithering(BufferedImage img) {
		// BufferedImage img=new BufferedImage(imag.getWidth(),imag.getHeight(),
		// BufferedImage.TYPE_INT_GRAY);
		// img.getGraphics().drawImage(imag,0,0,null);
		C3[] palette = new C3[] { new C3(0, 0, 0), new C3(255, 255, 255) };

		int w = img.getWidth();
		int h = img.getHeight();

		C3[][] d = new C3[h][w];

		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				d[y][x] = new C3(img.getRGB(x, y));

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {

				C3 oldColor = d[y][x];
				C3 newColor = findClosestPaletteColor(oldColor, palette);
				img.setRGB(x, y, newColor.toColor().getRGB());

				C3 err = oldColor.sub(newColor);

				if (x + 1 < w)
					d[y][x + 1] = d[y][x + 1].add(err.mul(7. / 16));
				if (x - 1 >= 0 && y + 1 < h)
					d[y + 1][x - 1] = d[y + 1][x - 1].add(err.mul(3. / 16));
				if (y + 1 < h)
					d[y + 1][x] = d[y + 1][x].add(err.mul(5. / 16));
				if (x + 1 < w && y + 1 < h)
					d[y + 1][x + 1] = d[y + 1][x + 1].add(err.mul(1. / 16));
			}
		}

		return img;
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
			Main.mustacheOn = !Main.mustacheOn;
		if (e.getKeyCode() == KeyEvent.VK_F11) {
			Toolkit.getDefaultToolkit().beep();
			try {
				System.out.println("pressed");
				ImageIO.write(saveImage, "png", new File("./images/pic" + System.currentTimeMillis() + ".png"));
				try {
					if (printsmall)
					Printer.print(saveImage);
					
					else {PrinterTest.print(saveImage);
					PrinterTest.printMakerspace();} 
					
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

//		 paramPageFormat.setOrientation(PageFormat.LANDSCAPE);
//		 Paper pPaper = paramPageFormat.getPaper();
//		 pPaper.setSize(164.592,164.592);
//		 pPaper.setImageableArea(10, 10, pPaper.getWidth()-20, pPaper.getHeight()-20);
//		 paramPageFormat.setPaper(pPaper);
		// System.out.println(pPaper.getWidth()+","+pPaper.getHeight());
		// System.out.println(pPaper.getImageableWidth()+","+pPaper.getImageableHeight());
		// System.out.println(pPaper.getImageableX()+","+pPaper.getImageableY());
		// System.out.println(saveImage.getWidth()+","+saveImage.getHeight());
		//
		Graphics2D g2d = (Graphics2D) paramGraphics;
		AffineTransform pOrigTransform = g2d.getTransform();
		g2d.translate(paramPageFormat.getImageableX(), paramPageFormat.getImageableY());
		g2d.drawImage(saveImage, 0, 0, null);
		g2d.setTransform(pOrigTransform);
		return PAGE_EXISTS;
	}

}

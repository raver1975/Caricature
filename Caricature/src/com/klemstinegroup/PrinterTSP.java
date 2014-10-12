package com.klemstinegroup;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.IOException;

import javax.print.PrintException;

public class PrinterTSP {

	public static void print(final BufferedImage image) throws PrintException, IOException {

		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(new Printable() {
			public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
				if (pageIndex != 0) {
					return NO_SUCH_PAGE;
				}
				graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
				String s="Thank you for visiting the\nAppleton Makerspace!\n\nOpen to the public every\nThursday night at 6pm.\n\nAppletonMakerspace.org\n121R B North Douglas St.\nAppleton WI 54914";
				int y=image.getHeight();
				String[] sp=s.split("\n");
				for (String g:sp){
				graphics.drawString(g,0,y+=10);
				}
				return PAGE_EXISTS;
			}
		},getMinimumMarginPageFormat(printJob));
		try {
			printJob.print();
		} catch (PrinterException e1) {
			e1.printStackTrace();
		}

	}
	
	static private PageFormat getMinimumMarginPageFormat(PrinterJob printJob) {
	    PageFormat pf0 = printJob.defaultPage();
	    PageFormat pf1 = (PageFormat) pf0.clone();
	    Paper p = pf0.getPaper();
	    p.setImageableArea(0, 0,pf0.getWidth(), pf0.getHeight());
	    pf1.setPaper(p);
	    PageFormat pf2 = printJob.validatePage(pf1);
	    return pf2;     
	}
}

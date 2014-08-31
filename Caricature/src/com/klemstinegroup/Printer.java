package com.klemstinegroup;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;

public class Printer {

	public static void print(BufferedImage image) throws PrintException, IOException {


		System.out.println(image.getWidth() + "," + image.getHeight());
		byte[] data = new byte[300000];
		int cnt = 0;
		data[cnt++] = (byte) 0x1B;
		data[cnt++] = (byte) 0x40;
		data[cnt++] = (byte) 0x1B;
		data[cnt++] = (byte) 0x33;
		data[cnt++] = (byte) 24;
		
		int width=image.getWidth();
		int height=image.getHeight();
		for (int i = 0; i < height; i += 24) {
			data[cnt++] = (byte) 0x1b;
			data[cnt++] = (byte) 0x2A;
			data[cnt++] = (byte) 0x21;
			data[cnt++] = (byte) (width % 256);
			data[cnt++] = (byte) (width/ 256);
			for (int j = 0; j < width; j++) {
				byte a = 0, b = 0, c = 0;
				for (int k = 0; k < 8; k++) {
					a <<= 1;
					b <<= 1;
					c <<= 1;
					if (i + k < image.getHeight())
						a += image.getRGB(j, i + k) == -1 ? 0 : 1;
					if (i + 8 + k < image.getHeight())
						b += image.getRGB(j, i + k + 8) == -1 ? 0 : 1;
					if (i + 16 + k < image.getHeight())
						c += image.getRGB(j, i + k + 16) == -1 ? 0 : 1;
				}
				data[cnt++] = a;
				data[cnt++] = b;
				data[cnt++] = c;
			}
//			data[cnt++] = 10;
			 data[cnt++] = (byte) 0x0a;
		}
		 data[cnt++] = (byte) 0x0a;
//		 data[cnt++] = (byte) 0x0a;
		 String s="Thank you for visiting the\nAppleton Makerspace!\n\nOpen to the public every\nThursday night at 6pm.\n\nAppletonMakerspace.org\n121R B North Douglas St.\nAppleton WI 54914";
		 for (int i=0;i<s.length();i++){
			 data[cnt++]=(byte) s.charAt(i);
		 }
		 data[cnt++] = (byte) 0x0a;
		 data[cnt++] = (byte) 0x0a;
		 data[cnt++] = (byte) 0x0a;
		 data[cnt++] = (byte) 0x0a;
		 data[cnt++] = (byte) 0x0a;
		 data[cnt++] = (byte) 0x0a;
		byte[] dataout = new byte[cnt];
		System.arraycopy(data, 0, dataout, 0, cnt);
		System.out.println(cnt + " bytes");
		
		PrintService service = PrintServiceLookup.lookupDefaultPrintService();
		String defaultPrinter = PrintServiceLookup.lookupDefaultPrintService().getName();
		System.out.println("Default printer: " + defaultPrinter);
		PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
		pras.add(new Copies(1));
		DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
		Doc doc = new SimpleDoc(dataout, flavor, null);
		DocPrintJob job = service.createPrintJob();
		job.print(doc, pras);

	}
}

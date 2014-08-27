package com.klemstinegroup;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Map;

public class AutoCorrectionFilter {

    private final static int AUTO_COLOR_VALUE = 20;
    private static int[] pixels;
    private static int[] lumPixels = new int[256];
    private static int[] redPixels = new int[256];
    private static int[] greenPixels = new int[256];
    private static int[] bluePixels = new int[256];
    private static double clipping = 0.001;
    private static int[] largestOccs = new int[3];
    private static boolean contrast = true;
    private static boolean levels = true;
    private static boolean colors = true;
    private static boolean colorsPossible = false;

    public String getName() {
        return "AutoCorrection";
    }

    public String getDescription() {
        return "Do an auto correction of your photo";
    }

    public static BufferedImage filter(BufferedImage bi) {
        int width = bi.getWidth();
        int height = bi.getHeight();
        pixels = new int[width * height];
        bi.getRGB(0, 0, width, height, pixels, 0, width);
        BufferedImage dstBi = new BufferedImage(width, height, bi.getType());

        if (colors) {
            autoColor();
        }
        if (contrast) {
            autoContrast();
        }
        if (levels) {
            autoLevels();
        }

        dstBi.setRGB(0, 0, width, height, pixels, 0, width);

        return dstBi;
    }
    
    public void setContrast(Boolean contrast) {
        contrast = contrast;
    }

    public void setLevels(Boolean levels) {
        levels = levels;
    }

    public void setColors(boolean colors) {
        colors = colors;
    }

    public static boolean isColorsPossible() {
        return colorsPossible;
    }
    

    /**
     * calculates the auto contrast
     */
    private static void autoContrast() {
        createLuminanceArray();
        int clipNum = round(pixels.length * clipping);
        int max = getMaximum(clipNum, lumPixels);
        int min = getMinimum(clipNum, lumPixels);

        double contrast = 255.0 / (max - min);
        double brightness = 127.5 - ((max + min)/2.0);

        changeContrastBrightness(contrast, brightness);

    }

    /**
     * searches for the maximum in the given array
     * @param clipNum 
     * @param arr 
     * @return maximum
     */
    private static int getMaximum(int clipNum, int[] arr) {
        int max = 0;

        int i = 255;
        while (max < clipNum) {
            max += arr[i];
            i--;
        }
        i++;
        max = i;

        return max;
    }

    /**
     * searches for the minimum in the given array
     * @param clipNum
     * @param arr
     * @return minimum
     */
    private static int getMinimum(int clipNum, int[] arr) {
        int min = 0;

        int i = 0;
        while (min < clipNum) {
            min += arr[i];
            i++;
        }
        i--;
        min = i;

        return min;
    }

    /**
     * adjust the auto contrast which means to change the brightness and contrast
     * @param contrast
     * @param brightness 
     */
    private static void changeContrastBrightness(double contrast, double brightness) {
        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];

            int a = (argb >> 24) & 0xff;
            int r = (argb >> 16) & 0xff;
            int g = (argb >> 8) & 0xff;
            int b = argb & 0xff;

            double Y = 0.299 * r + 0.587 * g + 0.114 * b; // Calculates Luminance
            double Cb = -0.168736 * r - 0.331264 * g + 0.5 * b;
            double Cr = 0.5 * r - 0.418688 * g - 0.081312 * b;

            Y = contrast * (Y + brightness - 127.5) + 127.5; // Kontrast und Helligkeit wird verändert

            int rn = (int) (Y + 1.402 * Cr + 0.5);
            int gn = (int) (Y - 0.3441 * Cb - 0.7141 * Cr + 0.5);
            int bn = (int) (Y + 1.772 * Cb + 0.5);

            int[] clipped = clipping(rn, gn, bn);

            pixels[i] = (a << 24) | (clipped[0] << 16) | (clipped[1] << 8) | clipped[2];
        }

    }

    /**
     * calculates the auto level values
     */
    private static void autoLevels() {
        createRedArray();
        createGreenArray();
        createBlueArray();

        int clipNum = round(pixels.length * clipping);
        double[] factors = new double[6];

        int max = getMaximum(clipNum, redPixels);
        int min = getMinimum(clipNum, redPixels);

        factors[0] = 255.0 / (max - min); //contrastR
        factors[1] = 127.5 - ((max + min) / 2.0); // brightnessR

        max = getMaximum(clipNum, greenPixels);
        min = getMinimum(clipNum, greenPixels);

        factors[2] = 255.0 / (max - min); //contrastG
        factors[3] = 127.5 - ((max + min) / 2.0); // brightnessG

        max = getMaximum(clipNum, bluePixels);
        min = getMinimum(clipNum, bluePixels);

        factors[4] = 255.0 / (max - min); //contrastB
        factors[5] = 127.5 - ((max + min) / 2.0); // brightnessB

        changeLevels(factors);

    }

    /**
     * adjusts the auto levels  
     * @param factors values for the adjustment
     */
    private static void changeLevels(double[] factors) {
        for (int i = 0; i < pixels.length; i++) {
            int argb = pixels[i];

            int a = (argb >> 24) & 0xff;
            int r = (argb >> 16) & 0xff;
            int g = (argb >> 8) & 0xff;
            int b = argb & 0xff;

            int rn = (int) (factors[0] * (r + factors[1] - 127.5) + 127.5);
            int gn = (int) (factors[2] * (g + factors[3] - 127.5) + 127.5);
            int bn = (int) (factors[4] * (b + factors[5] - 127.5) + 127.5);

            int[] clipped = clipping(rn, gn, bn);

            pixels[i] = (a << 24) | (clipped[0] << 16) | (clipped[1] << 8) | clipped[2];

        }
    }

    /**
     * adjusts auto color - move histogram to the left if it's cut at the right
     */
    private static void autoColor() {
        createRedArray();
        createGreenArray();
        createBlueArray();

        int[] minIndices = new int[3];
        Arrays.fill(minIndices, 0);
        colorsPossible = false;

        if (largestOccs[0] > (255 - AUTO_COLOR_VALUE)) {
            minIndices[0] = getMinIndex(redPixels);
            colorsPossible = true;
        }
        if (largestOccs[1] > (255 - AUTO_COLOR_VALUE)) {
            minIndices[1] = getMinIndex(greenPixels);
            colorsPossible = true;
        }
        if (largestOccs[2] > (255 - AUTO_COLOR_VALUE)) {
            minIndices[2] = getMinIndex(bluePixels);
            colorsPossible = true;
        }
        if (isColorsPossible()) {
            for (int i = 0; i < pixels.length; i++) {
                int argb = pixels[i];

                int a = (argb >> 24) & 0xff;
                int r = (argb >> 16) & 0xff;
                int g = (argb >> 8) & 0xff;
                int b = argb & 0xff;

                r -= minIndices[0];
                g -= minIndices[1];
                b -= minIndices[2];

                int[] clipped = clipping(r, g, b);

                pixels[i] = (a << 24) | (clipped[0] << 16) | (clipped[1] << 8) | clipped[2];

            }
        }

    }

    /**
     * searches for the index of the first value
     * @param arr 
     * @return index
     */
    private static int getMinIndex(int[] arr) {
        int min = 0;
        int i = -1;
        while (min == 0) {
            i++;
            min = arr[i];
        }
        if (i > AUTO_COLOR_VALUE) {
            i = AUTO_COLOR_VALUE;
        }
        return i;
    }

    /**
     * clipped the value when necessary (value should be between 0-255)
     * @param r red
     * @param g green
     * @param b blue
     * @return clipped array with in it r, g, b
     */
    private static int[] clipping(int r, int g, int b) {

        if (r > 255) {
            r = 255;
        } else if (r < 0) {
            r = 0;
        }

        if (g > 255) {
            g = 255;
        } else if (g < 0) {
            g = 0;
        }

        if (b > 255) {
            b = 255;
        } else if (b < 0) {
            b = 0;
        }


        int[] clipped = {r, g, b};
        return clipped;
    }

    /**
     * calculates the luminance values and counts them
     */
    private static void createLuminanceArray() {
        Arrays.fill(lumPixels, 0);

        for (int i = 0; i < pixels.length; i++) {
            int rgb = pixels[i];

            int r = (rgb >> 16) & 0xff;
            int g = (rgb >> 8) & 0xff;
            int b = rgb & 0xff;

            double lum = 0.299 * r + 0.587 * g + 0.114 * b; // Calculates Luminance

            int lumR = round(lum);

            lumPixels[lumR]++;

        }

    }

    /**
     * counts occurrences of the 256 levels of red
     */
    private static void createRedArray() {
        Arrays.fill(redPixels, 0);
        int largestOcc = 0;
        int index = 0;

        for (int i = 0; i < pixels.length; i++) {
            int rgb = pixels[i];

            int r = (rgb >> 16) & 0xff;

            redPixels[r]++;

            if (redPixels[r] > largestOcc) {
                largestOcc = redPixels[r];
                index = r;
            }
        }
        largestOccs[0] = index;
    }

    /**
     * counts occurrences of the 256 levels of green
     */
    private static void createGreenArray() {
        Arrays.fill(greenPixels, 0);
        int largestOcc = 0;
        int index = 0;

        for (int i = 0; i < pixels.length; i++) {
            int rgb = pixels[i];

            int g = (rgb >> 8) & 0xff;

            greenPixels[g]++;

            if (greenPixels[g] > largestOcc) {
                largestOcc = greenPixels[g];
                index = g;
            }
        }
        largestOccs[1] = index;
    }

    /**
     * counts occurrences of the 256 levels of blue
     */
    private static void createBlueArray() {
        Arrays.fill(bluePixels, 0);
        int largestOcc = 0;
        int index = 0;

        for (int i = 0; i < pixels.length; i++) {
            int rgb = pixels[i];

            int b = rgb & 0xff;

            bluePixels[b]++;

            if (bluePixels[b] > largestOcc) {
                largestOcc = bluePixels[b];
                index = b;
            }
        }
        largestOccs[2] = index;
    }

    /**
     * Rounds double to int
     */
    private static int round(double lum) {
        int r = 0;

        if (lum >= 0) {
            r = (int) (lum + 0.5);
        } else {
            r = (int) (lum - 0.5);
        }
        return r;
    }


}
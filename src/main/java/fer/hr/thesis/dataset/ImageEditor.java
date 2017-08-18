package fer.hr.thesis.dataset;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * <p>
 * The class ImageEditor converts grayscale images to binary vectors and saves
 * them in a single textual file.
 * </p>
 * 
 * <p>
 * The program takes four arguments. The first argument is the source directory
 * where grayscale images are saved. The second directory is the file where
 * binarized versions of the images are going to be saved. Normalized binary
 * images are saved to the third directory and binary vectors representing
 * images are saved to the third file.
 * </p>
 * 
 * @author Dunja Vesinger
 * @version 1.0.0
 */
public class ImageEditor {
	/**
	 * Dimension to which the images are scaled.
	 */
	private static final int IMG_DIMENSION = 30;
	/**
	 * Number of classes inside dataset.
	 */
	private static final int NUM_OF_CLASSES = 6;

	/**
	 * Threshold of binarization.
	 */
	private static final int BINARY_TRESHOLD = 220;

	/**
	 * Method run on program start.
	 * 
	 * @param args
	 *            Command line arguments
	 */
	public static void main(String[] args) {
		if (args.length != 4) {
			System.out.println("Invalid number of arguments. Program takes 4 paths.");
		}

		// binarize
		Path source = Paths.get(args[0]);
		Path binDest = Paths.get(args[1]);
		binarizeImages(source, binDest, BINARY_TRESHOLD);

		// normalize
		Path normDest = Paths.get(args[2]);
		normalizeImages(binDest, normDest);

		// save as binary vectors
		Path dest = Paths.get(args[3]);
		try {
			saveImagesToFile(normDest, dest, NUM_OF_CLASSES, IMG_DIMENSION, IMG_DIMENSION);
		} catch (IOException e) {
			System.out.println(e.getStackTrace());
		}
	}

	/**
	 * Parses an integer array from a string.
	 * 
	 * @param line
	 *            String to be parsed
	 * @return Integer array from the string
	 */
	public static int[] parseLine(String line) {
		String[] elements = line.replace("[", "").replace("]", "").split(", ");
		int[] imageVector = new int[elements.length];
		for (int i = 0; i < elements.length; i++) {
			imageVector[i] = Integer.parseInt(elements[i]);
		}

		return imageVector;
	}

	/**
	 * Saves all the images from <code>source</code> directory to
	 * <code>dest</code> file in a for of binary vectors. Width, height and the
	 * number of classes in dataset are saved on top of the file.
	 * 
	 * @param source
	 *            Directory from which images are loaded
	 * @param dest
	 *            File to which the images are saved
	 * @param numOfClasses
	 *            Number of classes in dataset
	 * @param imgWidth
	 *            Width of an image
	 * @param imgHeight
	 *            Height of an image
	 * @throws IOException
	 *             If unable to write to file
	 */
	public static void saveImagesToFile(Path source, Path dest, int numOfClasses, int imgWidth, int imgHeight)
			throws IOException {
		List<int[]> images = new ArrayList<>();
		DatasetVisitor imageVisitor = new DatasetVisitor(images, true, numOfClasses);
		Files.walkFileTree(source, imageVisitor);

		writeToFile(dest, images, numOfClasses, imgWidth, imgHeight);

	}

	/**
	 * Saves all the arrays from images list to <code>dest</code> file. Width,
	 * height and the number of classes in dataset are saved on top of the file.
	 * 
	 * @param dest
	 *            File to which the images are saved
	 * @param images
	 *            List of arrays representing images
	 * @param numOfClasses
	 *            Number of classes in dataset
	 * @param imgWidth
	 *            Width of an image
	 * @param imgHeight
	 *            Height of an image
	 * @throws IOException
	 *             If unable to write to file
	 */
	private static void writeToFile(Path dest, List<int[]> images, int numOfClasses, int imgWidth, int imgHeight)
			throws IOException {
		FileOutputStream fos = new FileOutputStream(dest.toFile());
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

		// write number of classes and image dimensions
		bw.write(String.valueOf(numOfClasses));
		bw.newLine();
		bw.write(String.valueOf(imgWidth));
		bw.newLine();
		bw.append(String.valueOf(imgHeight));
		bw.newLine();

		// append each image to new file
		for (int[] imageVector : images) {
			bw.append(Arrays.toString(imageVector));
			bw.newLine();
		}
		bw.close();
	}

	/**
	 * Each image from <code>source</code> directory is normalized and scaled
	 * and saved to <code>dest</code> directory under the same file name and
	 * directory structure.
	 * 
	 * @param source
	 *            Directory from which images are loaded
	 * @param dest
	 *            Directory to which images are saved
	 */
	public static void normalizeImages(Path source, Path dest) {

		FileVisitor<Path> imageVisitor = new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

				BufferedImage img = ImageIO.read(file.toFile());
				img = normalizeImage(cropBoundingBox(img), IMG_DIMENSION);

				Path destFile = Paths.get(dest.toString(), file.getParent().getFileName().toString(),
						file.getFileName().toString());
				destFile.toFile().getParentFile().mkdirs();
				ImageIO.write(img, "png", destFile.toFile());

				return FileVisitResult.CONTINUE;
			}

		};

		try {
			Files.walkFileTree(source, imageVisitor);
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	/**
	 * Finds a bounding box of an image and crops it accordingly.
	 * 
	 * @param img
	 *            Image to be cropped
	 * @return Cropped image
	 */
	public static BufferedImage cropBoundingBox(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();

		int minX = width;
		int minY = height;
		int maxX = 0;
		int maxY = 0;

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int rgbCode = img.getRGB(x, y);
				int value = (rgbCode >> 16) & 0xff;
				if (value == 0) {
					minX = Integer.min(minX, x);
					minY = Integer.min(minY, y);
					maxX = Integer.max(maxX, x);
					maxY = Integer.max(maxY, y);
				}
			}
		}

		return img.getSubimage(minX, minY, maxX - minX, maxY - minY);
	}

	/**
	 * Normalizes the given image and scales it to the given dimensions.
	 * 
	 * @param img
	 *            Image to be normalized
	 * @param dimension
	 *            Width and height of the final image
	 * @return Normalized image
	 */
	public static BufferedImage normalizeImage(BufferedImage img, int dimension) {
		int width = img.getWidth();
		int height = img.getHeight();
		int maxDim = Integer.max(img.getWidth(), img.getHeight());
		double scalingFactor = ((double) dimension) / maxDim;
		int newHeight = (int) Math.round(height * scalingFactor);
		int newWidth = (int) Math.round(width * scalingFactor);

		int height_offset = (dimension - newHeight) / 2;
		int width_offset = (dimension - newWidth) / 2;

		BufferedImage resized = new BufferedImage(dimension, dimension, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resized.createGraphics();
		g.setBackground(Color.WHITE);
		g.fillRect(0, 0, width, height);

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(img, width_offset, height_offset, newWidth + width_offset, newHeight + height_offset, 0, 0, width,
				height, null);

		g.dispose();

		for (int i = 0; i < dimension; i++) {
			for (int j = 0; j < dimension; j++) {
				int transparency = ((resized.getRGB(i, j) & 0xff000000) >> 24);
				if (transparency == 0) {
					resized.setRGB(i, j, 0xffffffff);
				}

			}
		}

		return resized;
	}

	/**
	 * Each image from <code>source</code> directory is binarized with the given
	 * threshold and saved to <code>dest</code> directory under the same file
	 * name and directory structure.
	 * 
	 * @param source
	 *            Directory from which images are loaded
	 * @param dest
	 *            Directory to which images are saved
	 * @param threshold
	 *            Threshold of the binarization
	 */
	public static void binarizeImages(Path source, Path dest, int treshold) {

		FileVisitor<Path> imageVisitor = new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

				BufferedImage image = ImageIO.read(file.toFile());
				image = binarizeImage(image, treshold);

				Path destFile = Paths.get(dest.toString(), file.getFileName().toString());
				ImageIO.write(image, "png", destFile.toFile());

				return FileVisitResult.CONTINUE;
			}

		};

		try {
			Files.walkFileTree(source, imageVisitor);
		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	/**
	 * Binarizes the given image using the given threshold.
	 * @param img Image to be binarized
	 * @param treshold Binarization threshold
	 * @return Binary image
	 */
	public static BufferedImage binarizeImage(BufferedImage img, int treshold) {
		int width = img.getWidth();
		int height = img.getHeight();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int p = img.getRGB(x, y);

				int a = (p >> 24) & 0xff;
				int r = (p >> 16) & 0xff;
				int g = (p >> 8) & 0xff;
				int b = p & 0xff;

				if (r > treshold || g > treshold || b > treshold) {
					img.setRGB(x, y, 0xffffff);
				} else {
					img.setRGB(x, y, 0);
				}
			}
		}
		return img;
	}

}

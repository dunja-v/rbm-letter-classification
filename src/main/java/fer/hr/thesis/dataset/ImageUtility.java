package fer.hr.thesis.dataset;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.imageio.ImageIO;

/**
 * Utility class containing methods used for image encoding, decoding,
 * converting to binary vectors and from binary vectors to images.
 * 
 * @author Dunja Vesinger
 * @version 1.0.0
 */
public class ImageUtility {

	/**
	 * The ASCII value of the first sign used in encoding.
	 */
	private static final int ENCODING_BEGIN = 65;

	/**
	 * Private constructor. Disables the creation of an instance of this class.
	 */
	private ImageUtility() {
	}

	/**
	 * Reads image from file.
	 * 
	 * @param file
	 *            Image file
	 * @return Loaded image
	 * @throws IOException
	 *             If image cannot be read from file
	 */
	public static BufferedImage readImage(File file) throws IOException {
		BufferedImage img;
		img = ImageIO.read(file);
		return img;
	}

	/**
	 * Converts given image to vector and returns it in a from of integer array.
	 * 
	 * @param img
	 *            Image
	 * @return Vector
	 */
	public static int[] imageToVector(BufferedImage img) {

		int width = img.getWidth();
		int height = img.getHeight();

		int[] input = new int[width * height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int rgbCode = img.getRGB(x, y);
				int value = (rgbCode >> 16) & 0xff;
				input[x * height + y] = (value > 0) ? 1 : 0;
			}
		}
		return input;

	}

	/**
	 * Creates an image from the given vector with the given width and height.
	 * 
	 * @param imageVector
	 *            Vector containing image pixel values
	 * @param width
	 *            Width of the image
	 * @param height
	 *            Height of the image
	 * @return Image
	 */
	public static BufferedImage imageFromVector(int[] imageVector, int width, int height) {
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (imageVector[x * height + y] > 0)
					img.setRGB(x, y, 0xffffff);
				else {
					img.setRGB(x, y, 0);
				}
			}
		}
		return img;
	}

	/**
	 * Creates an image from the given vector with the given width and height
	 * and saves it to file.
	 * 
	 * @param imageVector
	 *            Vector containing image pixel values
	 * @param fileName
	 *            Name of the file
	 * @param width
	 *            Width of the image
	 * @param height
	 *            Height of the image
	 */
	public static void saveAsImage(int[] imageVector, String fileName, int width, int height) {

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (imageVector[x * height + y] > 0)
					img.setRGB(x, y, 0xffffff);
				else {
					img.setRGB(x, y, 0);
				}
			}
		}

		Path path = Paths.get(fileName);
		File file = path.toFile();

		try {
			ImageIO.write(img, "png", file);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds one-hot encoding at the end of the vector for the given letter and
	 * the number of possible classes.
	 * 
	 * @param imgVector
	 *            Image vector
	 * @param letter
	 *            Letter of the encoding
	 * @param numOfClasses
	 *            Number of possible classes
	 * @return Encoded image vector
	 */
	public static int[] addOneHotEncoding(int[] imgVector, char letter, int numOfClasses) {
		int[] encoding = new int[numOfClasses];
		encoding[letter - ENCODING_BEGIN] = 1;

		int[] encodedInput = new int[imgVector.length + encoding.length];
		System.arraycopy(imgVector, 0, encodedInput, 0, imgVector.length);
		System.arraycopy(encoding, 0, encodedInput, imgVector.length, encoding.length);

		return encodedInput;
	}

	/**
	 * Adds zeros at the end of the image vector. The number of zeros is equal
	 * to the number of possible classes.
	 * 
	 * @param imgVector
	 *            Image vector
	 * @param numOfClasses
	 *            Number of possible classes
	 * @return Image vector with empty label
	 */
	public static int[] addEmptyEncoding(int[] imgVector, int numOfClasses) {
		int[] encoding = new int[numOfClasses];
		int[] encodedInput = new int[imgVector.length + encoding.length];
		System.arraycopy(imgVector, 0, encodedInput, 0, imgVector.length);
		System.arraycopy(encoding, 0, encodedInput, imgVector.length, encoding.length);

		return encodedInput;
	}

	/**
	 * Removes the number of elements equal to the number of possible classes
	 * from the end of the vector.
	 * 
	 * @param encodedImgVector
	 *            Image vector with encoding
	 * @param numOfClasses
	 *            Number of possible classes
	 * @return Image vector with no encoding
	 */
	public static int[] removeOneHotEncoding(int[] encodedImgVector, int numOfClasses) {

		int[] imgVector = new int[encodedImgVector.length - numOfClasses];
		System.arraycopy(encodedImgVector, 0, imgVector, 0, imgVector.length);

		return imgVector;
	}

	/**
	 * Returns the last <code>numOfClasses</code> elements of the image vector.
	 * 
	 * @param encodedImgVector
	 *            Image vector with encoding
	 * @param numOfClasses
	 *            Number of possible classes
	 * @return Encoding of the image
	 */
	public static int[] getEncoding(int[] encodedImgVector, int numOfClasses) {
		int[] encoding = new int[numOfClasses];
		System.arraycopy(encodedImgVector, encodedImgVector.length - numOfClasses - 1, encoding, 0, numOfClasses);
		return encoding;
	}

	/**
	 * Returns the name of the letter corresponding to the encoding of the
	 * image.
	 * 
	 * @param imgVector
	 *            Image vector with encoding
	 * @param numOfClasses
	 *            Number of possible classes
	 * @return Name of the letter corresponding to the encoding of the image
	 */
	public static String getLetterClassName(int[] imgVector, int numOfClasses) {
		int[] encoding = new int[numOfClasses];
		System.arraycopy(imgVector, imgVector.length - numOfClasses, encoding, 0, numOfClasses);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numOfClasses; i++) {
			if (encoding[i] == 1) {
				sb.append(Character.getName(i + ENCODING_BEGIN));
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	/**
	 * Returns a copy of the example, but fills the encoding elements with
	 * zeros.
	 * 
	 * @param example
	 *            Example
	 * @param numOfClasses
	 *            Number of possible classes
	 * @return Copy of the examples with zeros instead of encoding
	 */
	public static int[] getClearedLabelCopy(int[] example, int numOfClasses) {
		int[] clearedExample = new int[example.length];
		System.arraycopy(example, 0, clearedExample, 0, example.length - numOfClasses);
		return clearedExample;
	}

	/**
	 * Calculates the entropy inside dataset and returns it.
	 * 
	 * @param dataset
	 *            Dataset
	 * @return Entropy of the dataset
	 */
	public static double calculateEntropy(List<int[]> dataset) {
		double[] probabilities = new double[dataset.get(0).length];
		for (int i = 0; i < dataset.size(); i++) {
			for (int j = 0; j < probabilities.length; j++) {
				if (dataset.get(i)[j] == 1) {
					probabilities[j]++;
				}
			}
		}
		for (int j = 0; j < probabilities.length; j++) {
			probabilities[j] /= dataset.size();
		}

		double entropy = 0;
		for (int k = 0; k < probabilities.length; k++) {
			entropy -= probabilities[k] * Math.log10(probabilities[k]) / Math.log10(2);
		}
		return entropy;

	}

}

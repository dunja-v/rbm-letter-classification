package fer.hr.thesis.dataset;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import javax.imageio.ImageIO;

import fer.hr.thesis.dataset.ImageUtility;

/**
 * <p>
 * The DatasetVisitor class represents a FileVisitor which saves all the images
 * found in a list of vectors.
 * </p>
 * 
 * <p>
 * For the given subtree of directories, the visitor attempts to read every file
 * as image and stores it as a vector. If the encoding field is set to
 * <code>True</code>, additional one-hot encoding is attached at the end of the
 * vector for the given number of classes. The classes are encoded based on the
 * first letter of the parent directory in which they are found.
 * </p>
 * 
 * @author Dunja Vesinger
 * @version 1.0.0
 */
public class DatasetVisitor extends SimpleFileVisitor<Path> {
	/**
	 * List of images saved in a form o vectors.
	 */
	private List<int[]> examples;
	/**
	 * True if one-hot encoding should be added, false otherwise.
	 */
	private boolean encoded;
	/**
	 * Number of classes present in dataset, determines the length of encoding.
	 */
	private int numOfClasses;

	/**
	 * Creates a new DatasetVisitor.
	 * @param examples List to which vectors are saved
	 * @param encoded If examples should be encoded
	 * @param numOfClasses Number of classes in dataset
	 */
	public DatasetVisitor(List<int[]> examples, boolean encoded, int numOfClasses) {
		this.examples = examples;
		this.encoded = encoded;
		this.numOfClasses = numOfClasses;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		BufferedImage img = ImageIO.read(file.toFile());
		int[] input = ImageUtility.imageToVector(img);

		if (encoded) {
			String parentFileName = file.toFile().getParentFile().getName();
			char letter = parentFileName.charAt(0);
			input = ImageUtility.addOneHotEncoding(input, letter, numOfClasses);
		} else {
			input = ImageUtility.addEmptyEncoding(input, numOfClasses);
		}
		examples.add(input);

		return FileVisitResult.CONTINUE;
	}

}

package fer.hr.thesis.dataset;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.imageio.ImageIO;

public class DatasetEditor {

	private static final int MARGIN_LEFT = 231;
	private static final int MARGIN_TOP = 160;

	private static final int ELEM_WIDTH = 108;
	private static final int ELEM_HEIGHT = 108;
	private static final int OFFSET = 5;
	private static final int VERTICAL_OFFSET = 14;
	private static final int HORIZONTAL_OFFSET = 15;

	private static final String RAW_DATASET_PATH = "rawDataset";
	private static final String CROPPED_DATASET_PATH = "croppedImages";
	private static final String BINARY_DATASET_PATH = "binaryImages";

	private static final int BINARY_TRESHOLD = 220;

	public static void cropLetterImageToFiles(Path dest, Path source, AlphabetSection section, int fileNum)
			throws IOException {
		BufferedImage image = ImageIO.read(source.toFile());
		int sectionAddition = (section == AlphabetSection.SECOND) ? 13 : 0;

		for (int i = 0; i < 10; i++) {
			for (int letterIndex = 0; letterIndex < 13; letterIndex++) {
				BufferedImage letter = image.getSubimage(i * (ELEM_WIDTH + HORIZONTAL_OFFSET) + OFFSET + MARGIN_LEFT,
						letterIndex * (ELEM_HEIGHT + VERTICAL_OFFSET) + OFFSET + MARGIN_TOP, ELEM_WIDTH, ELEM_HEIGHT);

				ImageIO.write(letter, "png", new File(
						dest.toString() + "/" + fileNum + (char) (letterIndex + 65 + sectionAddition) + i + ".png"));
			}
		}

	}

	public static BufferedImage grayscale(BufferedImage img) {
		// get image width and height
		int width = img.getWidth();
		int height = img.getHeight();

		// convert to grayscale
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int p = img.getRGB(x, y);

				int a = (p >> 24) & 0xff;
				int r = (p >> 16) & 0xff;
				int g = (p >> 8) & 0xff;
				int b = p & 0xff;

				// calculate average
				int avg = (r + g + b) / 3;

				// replace RGB value with avg
				p = (a << 24) | (avg << 16) | (avg << 8) | avg;

				img.setRGB(x, y, p);
			}
		}

		return img;
	}

	public static BufferedImage binarize(BufferedImage img) {
		int width = img.getWidth();
		int height = img.getHeight();

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int p = img.getRGB(x, y);

				int a = (p >> 24) & 0xff;
				int r = (p >> 16) & 0xff;
				int g = (p >> 8) & 0xff;
				int b = p & 0xff;

				if (r > BINARY_TRESHOLD || g > BINARY_TRESHOLD || b > BINARY_TRESHOLD) {
					img.setRGB(x, y, 0xffffff);
				} else {
					img.setRGB(x, y, 0);
				}
			}
		}
		return img;
	}

	public static void croppDataset() {

		Path sourceDir = Paths.get(RAW_DATASET_PATH);

		FileVisitor<Path> imageVisitor = new SimpleFileVisitor<Path>() {
			private int fileNum = 1;

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

				try {
					int fileNumber = Integer
							.parseInt(file.getName(file.getNameCount() - 1).toString().replaceAll(".png", ""));
					AlphabetSection section = (fileNumber % 2 == 0) ? AlphabetSection.FIRST : AlphabetSection.SECOND;

					cropLetterImageToFiles(Paths.get(CROPPED_DATASET_PATH), file, section, fileNum);
					fileNum++;
					return FileVisitResult.CONTINUE;
				} catch (IOException e) {
					e.printStackTrace();
					return FileVisitResult.CONTINUE;
				}
			}

		};

		try {
			Files.walkFileTree(sourceDir, imageVisitor);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void grayscaleDataset(Path source, Path dest) {

		FileVisitor<Path> imageVisitor = new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

				BufferedImage image = ImageIO.read(file.toFile());
				image = grayscale(image);

				Path destFile = Paths.get(dest.toString(), file.toString());
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
	
	public static void binariseDataset(Path source, Path dest) {

		FileVisitor<Path> imageVisitor = new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

				BufferedImage image = ImageIO.read(file.toFile());
				image = binarize(image);

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

	public static void main(String[] args) {
		binariseDataset(Paths.get(CROPPED_DATASET_PATH), Paths.get(BINARY_DATASET_PATH));
	}

}

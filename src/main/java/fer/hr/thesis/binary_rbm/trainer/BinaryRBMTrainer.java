package fer.hr.thesis.binary_rbm.trainer;

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

import fer.hr.thesis.binary_rbm.BinaryRBM;

public class BinaryRBMTrainer {

	public static void trainBinaryRBM(BinaryRBM rbm, Path dataset, double learningRate, int numOfEpochs) {

		FileVisitor<Path> datasetVisitor = new SimpleFileVisitor<Path>() {
			int exampleIndex = 0;

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				int[] input = readImageToVector(file);

				trainExample(rbm, input, learningRate, 1, 108, 108, exampleIndex);
				exampleIndex++;

				return super.visitFile(file, attrs);
			}

		};

		try {
			for (int i = 0; i < numOfEpochs; i++) {
				Files.walkFileTree(dataset, datasetVisitor);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void trainExample(BinaryRBM rbm, int[] input, double learningRate, int numOfEpochs, int width,
			int height, int exampleIndex) {

		rbm.setOriginalData(input);

		for (int i = 0; i < numOfEpochs; i++) {

			rbm.setVisible(input);
			rbm.setInitialHiddenProbabilities(rbm.updateHiddenNeurons());

			rbm.setFinalVisibleProbabilities(rbm.updateVisibleNeurons());
			rbm.setFinalHiddenProbabilities(rbm.updateHiddenNeurons());

			rbm.updateWeights(learningRate);
			rbm.updateBiases(learningRate);

			if (exampleIndex % 300 == 0) {
				saveAsImage(rbm.getVisible().getAsVector(), "Generated_" + exampleIndex + "_" + i + ".png", width,
						height);
			}

			double squareError = 0;
			for (int j = 0; j < input.length; j++) {
				squareError += Math.pow(input[j] - rbm.getVisible().getElement(j), 2);
			}
			System.out.println(squareError);

		}

	}

	public static int[] readImageToVector(Path file) throws IOException {
		BufferedImage img;

		img = ImageIO.read(file.toFile());

		int width = img.getWidth();
		int height = img.getHeight();

		int[] input = new int[width * height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int rgbCode = img.getRGB(x, y);
				int value = (rgbCode >> 16) & 0xff;
				input[x * width + y] = (value > 0) ? 1 : 0;
			}
		}
		return input;
	}

	public static void saveAsImage(int[] imageVector, String fileName, int width, int height) {

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (imageVector[x * width + y] > 0)
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

}

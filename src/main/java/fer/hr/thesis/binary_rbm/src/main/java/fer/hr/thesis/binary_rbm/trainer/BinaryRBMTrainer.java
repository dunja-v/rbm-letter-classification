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
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import fer.hr.thesis.binary_rbm.BinaryRBM;

public class BinaryRBMTrainer {

	public static void trainBinaryRBM(BinaryRBM rbm, Path dataset, double learningRate, int numOfEpochs) {

		List<int[]> trainingExamples = new ArrayList<>();

		FileVisitor<Path> datasetVisitor = new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				int[] input = readImageToVector(file);
				trainingExamples.add(input);

				return super.visitFile(file, attrs);
			}

		};

		try {

			Files.walkFileTree(dataset, datasetVisitor);
			if (trainingExamples.isEmpty()) {
				return;
			}
			rbm.setVisibleBiases(claculateInitialBiases(trainingExamples));
			trainExamples(rbm, trainingExamples, learningRate, numOfEpochs);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static double[] claculateInitialBiases(List<int[]> trainingExamples) {
		
		double[] probabilities = calculateInputVectorProbailities(trainingExamples);
		
		for (int j = 0; j < probabilities.length; j++) {
			probabilities[j] = Math.log(probabilities[j] / (1 - probabilities[j]));
		}
		return probabilities;
	}

	private static double[] calculateInputVectorProbailities(List<int[]> trainingExamples) {
		double[] probabilities = new double[trainingExamples.get(0).length];

		for (int i = 0; i < trainingExamples.size(); i++) {
			for (int j = 0; j < probabilities.length; j++) {
				if (trainingExamples.get(i)[j] == 1) {
					probabilities[j]++;
				}
			}
		}

		for (int j = 0; j < probabilities.length; j++) {
			probabilities[j] /= trainingExamples.size();
		}

		return probabilities;
	}

	private static void trainExamples(BinaryRBM rbm, List<int[]> trainingExamples, double learningRate,
			int numOfEpochs) {

		for (int i = 0; i < numOfEpochs; i++) {

			double squareError = 0;

			for (int[] example : trainingExamples) {
				squareError += trainExample(rbm, example, learningRate, 1, 108, 108);

			}

			if (i % 100 == 0) {
				//learningRate /= 2;
				saveAsImage(rbm.getVisible().getAsVector(), "Generated_" + i + ".png", 108, 108);

			}

			System.out.println("Square error in epoch " + i + ": " + squareError / trainingExamples.size());
		}

	}

	private static double trainExample(BinaryRBM rbm, int[] input, double learningRate, int numOfEpochs, int width,
			int height) {

		rbm.setOriginalData(input);
		double squareError = 0;

		for (int i = 0; i < numOfEpochs; i++) {

			rbm.setVisible(input);
			rbm.setInitialHiddenProbabilities(rbm.updateHiddenNeurons());

			rbm.setFinalVisibleProbabilities(rbm.updateVisibleNeurons());
			rbm.setFinalHiddenProbabilities(rbm.updateHiddenNeurons());

			rbm.updateWeights(learningRate);
			rbm.updateBiases(learningRate);

			// if (exampleIndex % 300 == 0) {
			// saveAsImage(rbm.getVisible().getAsVector(), "Generated_" +
			// exampleIndex + "_" + i + ".png", width,
			// height);
			// }

			for (int j = 0; j < input.length; j++) {
				squareError += Math.pow(input[j] - rbm.getVisible().getElement(j), 2);
			}

		}

		return squareError / numOfEpochs;

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

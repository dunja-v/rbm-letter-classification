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

	public static void trainBinaryRBM(BinaryRBM rbm, Path dataset, int numOfValidationExamples, double learningRate,
			int maxNumOfEpochs) {

		List<int[]> trainingExamples = new ArrayList<>();
		List<int[]> validationExamples = new ArrayList<>();

		FileVisitor<Path> datasetVisitor = new SimpleFileVisitor<Path>() {

			private int letterNum = 0;
			private int dirNum = 0;

			@Override
			public FileVisitResult preVisitDirectory(Path arg0, BasicFileAttributes arg1) throws IOException {
				letterNum = 0;
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path arg0, IOException arg1) throws IOException {
				 dirNum++;
				 if(dirNum >= 1){
				 return FileVisitResult.TERMINATE;
				 }
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				int[] input = readImageToVector(file);
				input = addOneHotEncoding(input, file);

				if (letterNum < numOfValidationExamples) {
					validationExamples.add(input);

				} else {
					trainingExamples.add(input);
				}

				letterNum++;
				return FileVisitResult.CONTINUE;
			}
		};

		try {

			Files.walkFileTree(dataset, datasetVisitor);
			if (trainingExamples.isEmpty()) {
				return;
			}
			System.out.println("Loaded examples: " + trainingExamples.size());
			rbm.setVisibleBiases(claculateInitialBiases(trainingExamples));
			trainExamples(rbm, trainingExamples, validationExamples, learningRate, maxNumOfEpochs);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static double[] claculateInitialBiases(List<int[]> trainingExamples) {

		double[] probabilities = calculateInputVectorProbailities(trainingExamples);

		for (int j = 0; j < probabilities.length; j++) {
			if(probabilities[j] == 0){
				probabilities[j] = -7;
			}else if(probabilities[j] == 1){
				probabilities[j] = 7;
			}else{
				probabilities[j] = Math.log(probabilities[j] / (1 - probabilities[j]));
			}
			
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

	private static void trainExamples(BinaryRBM rbm, List<int[]> trainingExamples, List<int[]> validationExamples,
			double learningRate, int numOfEpochs) {

		double squareError = 1000;
		double classificationError;

		for (int i = 0; i < numOfEpochs; i++) {

			squareError = 0;
			classificationError = 0;

			for (int[] example : trainingExamples) {
				squareError += trainExample(rbm, example, learningRate, 1, 108, 108);
				if (classificationError(rbm.getVisible().getAsVector(), example)) {
					classificationError++;
				}
			}

			if (i % 50 == 0) {
				saveAsImage(rbm.getVisible().getAsVector(), "Generated_" + i + ".png", 108, 108);
			}

			double trainingFreeEnergy = calculateFreeEnergy(rbm, trainingExamples);
			double validationFreeEnergy = calculateFreeEnergy(rbm, validationExamples);

			System.out.println("Square error in epoch " + i + ": " + squareError / trainingExamples.size());
			System.out.println("Missclasified examples in epoch " + i + ": " + classificationError);
			System.out.println("Free energy on training set: " + trainingFreeEnergy);
			System.out.println("Free energy on validation set: " + validationFreeEnergy);		
		}
	}

	private static double calculateFreeEnergy(BinaryRBM rbm, List<int[]> examples) {
		double freeEnergy = 0;

		for (int[] example : examples) {
			rbm.setVisible(example);
			rbm.updateHiddenNeurons();
			freeEnergy += rbm.freeEnergy();
		}

		return freeEnergy / examples.size();
	}

	private static boolean classificationError(int[] generated, int[] original) {

		boolean error = false;

		for (int i = 1; i <= 26; i++) {
			int index = generated.length - i;
			if (generated[index] != original[index]) {
				error = true;
			}
		}
		return error;
	}

	private static double trainExample(BinaryRBM rbm, int[] input, double learningRate, int numOfEpochs, int width,
			int height) {

		rbm.setOriginalData(input);
		double squareError = 0;

		for (int i = 0; i < numOfEpochs; i++) {

			rbm.setVisible(input);
			rbm.setInitialHiddenProbabilities(rbm.updateHiddenNeurons());

			rbm.setFinalVisibleProbabilities(rbm.updateFinalVisibleNeurons());
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

	private static int[] addOneHotEncoding(int[] input, Path file) {
		String letter = file.toFile().getParentFile().getName();
		int[] encoding = new int[26];
		encoding[letter.charAt(0) - 65] = 1;

		int[] encodedInput = new int[input.length + encoding.length];
		System.arraycopy(input, 0, encodedInput, 0, input.length);
		System.arraycopy(encoding, 0, encodedInput, input.length, encoding.length);

		return encodedInput;
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

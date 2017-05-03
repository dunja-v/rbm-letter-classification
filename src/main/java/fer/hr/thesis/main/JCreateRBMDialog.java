package fer.hr.thesis.main;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import fer.hr.thesis.binary_rbm.BinaryRBM;
import fer.hr.thesis.binary_rbm.neuron_layer.BinaryNeuronLayer;

public class JCreateRBMDialog extends JDialog {

	private BinaryRBM rbm;

	public JCreateRBMDialog() {

		initGUI();
		setTitle("Create new RBM");
		pack();
	}

	private void initGUI() {

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		add(p);
		p.setBorder(BorderFactory.createEmptyBorder(20, 20, 5, 20));

		JLabel lLeariningRate = new JLabel("Learning rate:");
		JTextArea tLearningRate = new JTextArea("0.001");
		p.add(lLeariningRate);
		p.add(tLearningRate);

		JLabel lHiddenNum = new JLabel("Number of hidden neurons:");
		JTextArea tHiddenNum = new JTextArea("400");
		p.add(lHiddenNum);
		p.add(tHiddenNum);

		JLabel lEpochs = new JLabel("Maximum number of epochs:");
		JTextArea tEpochs = new JTextArea("1000");
		p.add(lEpochs);
		p.add(tEpochs);

		JLabel lImgWidth = new JLabel("Image width:");
		JTextArea tImgWidth = new JTextArea("108");
		p.add(lImgWidth);
		p.add(tImgWidth);

		JLabel lImgHeight = new JLabel("Image height:");
		JTextArea tImgHeight = new JTextArea("108");
		p.add(lImgHeight);
		p.add(tImgHeight);

		JButton btnOK = new JButton("OK");
		p.add(btnOK);

		btnOK.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				double learningRate;
				try {
					learningRate = Double.parseDouble(tLearningRate.getText());
					if (learningRate < 0) {
						tLearningRate.setText("Must be a positive decimal number!");
					}
				} catch (NumberFormatException | NullPointerException ex) {
					tLearningRate.setText("Must be a decimal number!");
					return;
				}

				int numOfHidden = getParameter(tHiddenNum);
				int imageWidth = getParameter(tImgWidth);
				int imageHeight = getParameter(tImgHeight);
				int epochs = getParameter(tEpochs);

				if (numOfHidden < 0 | imageHeight < 0 | imageWidth < 0) {
					return;
				}

				rbm = new BinaryRBM(new BinaryNeuronLayer(new int[imageWidth * imageHeight + 26]),
						new BinaryNeuronLayer(new int[numOfHidden]));

				dispose();
			}
		});

	}

	private int getParameter(JTextArea t) {

		try {
			int param = Integer.parseInt(t.getText());

			if (param <= 0) {
				t.setText("Must be a positive integer!");
				return -1;
			}
			return param;

		} catch (NumberFormatException | NullPointerException ex) {
			t.setText("Must be an integer!");
			return -1;
		}

	}

	public BinaryRBM getRbm() {
		return rbm;
	}

}

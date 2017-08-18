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

/**
 * Dialog used for choosing a number of hidden neurons for the RBM to be
 * created.
 * 
 * @author Dunja Vesinger
 * @version 1.0.0
 */
public class JCreateRBMDialog extends JDialog {

	/**
	 * Number used for serialization.
	 */
	private static final long serialVersionUID = 2152102758371456897L;
	/**
	 * Number of hidden neurons.
	 */
	private int numOfHidden;

	/**
	 * Constructs a new JCreateRBMDialog.
	 */
	public JCreateRBMDialog() {

		initGUI();
		setTitle("Create new RBM");
		pack();
	}

	/**
	 * Initialized the graphical user interface of the dialog.
	 */
	private void initGUI() {

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		add(p);
		p.setBorder(BorderFactory.createEmptyBorder(20, 20, 5, 20));

		JLabel lHiddenNum = new JLabel("Number of hidden neurons:");
		JTextArea tHiddenNum = new JTextArea("400");
		p.add(lHiddenNum);
		p.add(tHiddenNum);

		JButton btnOK = new JButton("OK");
		p.add(btnOK);

		btnOK.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				numOfHidden = getParameter(tHiddenNum);

				if (numOfHidden < 0) {
					return;
				}

				dispose();
			}
		});

	}

	/**
	 * Tries to parse a positive integer from the text in the text area. Prints
	 * and error on the text area if the value is invalid.
	 * 
	 * @param t
	 *            Text area containing the parameter value
	 * @return Value of the parameter or -1 if the input is invalid
	 */
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

	/**
	 * Returns the number of hidden neurons chosen.
	 * 
	 * @return Number of hidden neurons
	 */
	public int getNumOfHidden() {
		return numOfHidden;
	}

}

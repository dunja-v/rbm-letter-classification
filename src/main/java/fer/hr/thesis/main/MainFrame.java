package fer.hr.thesis.main;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import fer.hr.thesis.binary_rbm.BinaryRBM;

public class MainFrame extends JFrame {

	private BinaryRBM rbm;

	public MainFrame() {

		setTitle("Handwritten Letter Classification");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(640, 480);

		initGUI();

		setVisible(true);

	}

	private void initGUI() {

		Container cp = getContentPane();
		cp.setLayout(new FlowLayout());

		JMenuBar menuBar = new JMenuBar();

		// RBM menu items
		JMenu rbmMenu = new JMenu("RBM");
		menuBar.add(rbmMenu);

		JMenuItem newRBM = new JMenuItem("New...");
		rbmMenu.add(newRBM);

		JMenuItem loadRBM = new JMenuItem("Load...");
		rbmMenu.add(loadRBM);
		
		JMenuItem saveRBM = new JMenuItem("Save as...");
		saveRBM.setEnabled(false);
		rbmMenu.add(saveRBM);
		
		newRBM.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JCreateRBMDialog jdCreate = new JCreateRBMDialog();
				jdCreate.setModal(true);
				jdCreate.setVisible(true);
				
				rbm = jdCreate.getRbm();				
				if(rbm != null){
					saveRBM.setEnabled(true);
				}

			}
		});
		
		loadRBM.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showDialog(cp, "Load");

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();

					try {
						rbm = BinaryRBM.readRBMFromFile(file);
						JOptionPane.showMessageDialog(cp, "RBM Successfully loaded.", "Message",  JOptionPane.INFORMATION_MESSAGE);
						saveRBM.setEnabled(true);
						
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(cp, "Unable to load RBM from file.", "Error",  JOptionPane.ERROR_MESSAGE);
					}
				}

			}
		});

		saveRBM.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				int returnVal = chooser.showDialog(cp, "Save");

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();

					try {
						BinaryRBM.writeRBMToFile(file, rbm);
						JOptionPane.showMessageDialog(cp, "RBM Successfully saved.", "Message",  JOptionPane.INFORMATION_MESSAGE);
						
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(cp, "Unable to save RBM to file.", "Error",  JOptionPane.ERROR_MESSAGE);
					}
				}

				
			}
		});

		// Dataset menu items
		JMenu datasetMenu = new JMenu("Dataset");
		menuBar.add(datasetMenu);

		JMenuItem setPath = new JMenuItem("Set path...");
		datasetMenu.add(setPath);

		JMenuItem setValidationPercentage = new JMenuItem("Set validation percentage...");
		datasetMenu.add(setValidationPercentage);

		// Image menu items
		JMenu imgMenu = new JMenu("Image");
		menuBar.add(imgMenu);

		JMenuItem loadImg = new JMenuItem("Load...");
		imgMenu.add(loadImg);

		JMenuItem saveImg = new JMenuItem("Save as...");
		saveImg.setEnabled(false);
		imgMenu.add(saveImg);

		setJMenuBar(menuBar);

	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(MainFrame::new);

	}

}

package de.pcl.smartshirt;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.nio.LongBuffer;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class FindPlayerView {
	protected static ColorConfig cConfig;

	private static JList<String> cList;
	private static JTextField configName;

	private JFrame jFrame;
	private JLabel hLow;
	private JLabel sLow;
	private JLabel vLow;
	private JSlider hSliderLow;
	private JSlider sSliderLow;
	private JSlider vSliderLow;
	private JLabel hUp;
	private JLabel sUp;
	private JLabel vUp;
	private JSlider hSliderUp;
	private JSlider sSliderUp;
	private JSlider vSliderUp;
	private JLabel originalPic;
	private JLabel processedPic;
	
	private Scalar upperBoundScalar;
	private Scalar lowerBoundScalar;
	
	
	
	public FindPlayerView() {
		cConfig = new ColorConfig();
		setup();
	}
	

	private void setup() {
		jFrame = new JFrame();        	
		JPanel jPanel = new JPanel();
		JPanel panelLow = new JPanel();
		JPanel panelUp = new JPanel();

		FlowLayout flowLayout = new FlowLayout();	
		GridLayout gridLayout = new GridLayout(0, 2);

		jFrame.setLayout(flowLayout);
		jPanel.setLayout(flowLayout);
		panelLow.setLayout(gridLayout);
		panelUp.setLayout(gridLayout);

		panelLow.setBorder(BorderFactory.createTitledBorder("HSV-Lower-Range"));
		panelUp.setBorder(BorderFactory.createTitledBorder("HSV-Upper-Range"));

		originalPic = new JLabel();
		processedPic = new JLabel();

		hLow = new JLabel("H-Lower:");
		sLow = new JLabel("S-Lower:");
		vLow = new JLabel("V-Lower:");
		hSliderLow = new JSlider(0, 360, 0);
		sSliderLow = new JSlider(0, 255, 100);
		vSliderLow = new JSlider(0, 255, 100);

		hUp = new JLabel("H-Upper:");
		sUp = new JLabel("S-Upper:");
		vUp = new JLabel("V-Upper:");
		hSliderUp = new JSlider(0, 360, 0);
		sSliderUp = new JSlider(0, 255, 255);
		vSliderUp = new JSlider(0, 255, 255);
		
		hSliderLow.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				hLow.setText("H-Lower: " + hSliderLow.getValue());
				if (lowerBoundScalar != null) {
					double[] val = lowerBoundScalar.val;
					val[0] = hSliderLow.getValue();
					lowerBoundScalar.set(val);
				}
			}
		});
		
		sSliderLow.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				sLow.setText("S-Lower: " + sSliderLow.getValue());
				if (lowerBoundScalar != null) {
					double[] val = lowerBoundScalar.val;
					val[1] = sSliderLow.getValue();
					lowerBoundScalar.set(val);
				}
			}
		});
		
		vSliderLow.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				vLow.setText("V-Lower: " + vSliderLow.getValue());
				if (lowerBoundScalar != null) {
					double[] val = lowerBoundScalar.val;
					val[2] = vSliderLow.getValue();
					lowerBoundScalar.set(val);
				}
			}
		});
		
		hSliderUp.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				hUp.setText("H-Upper: " + hSliderUp.getValue());
				if (upperBoundScalar != null) {
					double[] val = upperBoundScalar.val;
					val[0] = hSliderUp.getValue();
					upperBoundScalar.set(val);
				}
			}
		});
		
		sSliderUp.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				sUp.setText("S-Upper: " + sSliderUp.getValue());
				if (upperBoundScalar != null) {
					double[] val = upperBoundScalar.val;
					val[1] = sSliderUp.getValue();
					upperBoundScalar.set(val);
				}
			}
		});
		
		vSliderUp.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				vUp.setText("V-Upper: " + vSliderUp.getValue());
				if (upperBoundScalar != null) {
					double[] val = upperBoundScalar.val;
					val[2] = vSliderUp.getValue();
					upperBoundScalar.set(val);
				}
			}
		});

		panelLow.add(hLow);
		panelLow.add(hSliderLow);
		panelLow.add(sLow);
		panelLow.add(sSliderLow);
		panelLow.add(vLow);
		panelLow.add(vSliderLow);

		panelUp.add(hUp);
		panelUp.add(hSliderUp);
		panelUp.add(sUp);
		panelUp.add(sSliderUp);
		panelUp.add(vUp);
		panelUp.add(vSliderUp); 

		jPanel.add(panelLow);
		jPanel.add(panelUp);

		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String name = configName.getText();
				cConfig.saveConfig(name, 
						hSliderLow.getValue(), 
						sSliderLow.getValue(), 
						vSliderLow.getValue(), 
						hSliderUp.getValue(), 
						sSliderUp.getValue(), 
						vSliderUp.getValue());
				updateConfigList();
			}
		});

		JButton remove = new JButton("Remove");
		remove.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String name = cList.getSelectedValue();
				cConfig.removeConfig(name);
				updateConfigList();
			}
		});

		configName = new JTextField("default");
		cList = new JList<String>();
		cList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				String name = cList.getSelectedValue();
				if (name == null) {
					return;
				}
				cConfig.selectConfig(cList.getSelectedValue());
				configName.setText(name);

				hSliderLow.setValue(cConfig.getLowerH());
				sSliderLow.setValue(cConfig.getLowerS());
				vSliderLow.setValue(cConfig.getLowerV()); 
				hSliderUp.setValue(cConfig.getUpperH());
				sSliderUp.setValue(cConfig.getUpperS());
				vSliderUp.setValue(cConfig.getUpperV());
			}
		});
		updateConfigList();

		jPanel.add(configName);
		jPanel.add(save);
		jPanel.add(cList);
		jPanel.add(remove);

		jFrame.add(originalPic);
		jFrame.add(processedPic); 
		jFrame.add(jPanel);        

		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setTitle("LiveStream BlackWhite");
		jFrame.setSize(1300, 630);
		jFrame.setVisible(false);   

	}
	
	
	
	public void updateSlider() {
		hLow.setText("H-Lower: " + hSliderLow.getValue());
		sLow.setText("S-Lower: " + sSliderLow.getValue());
		vLow.setText("V-Lower: " + vSliderLow.getValue());

		hUp.setText("H-Upper: " + hSliderUp.getValue());
		sUp.setText("S-Upper: " + sSliderUp.getValue());
		vUp.setText("V-Upper: " + vSliderUp.getValue());
	}
	
	
	
	public void updateView(Mat processedFrame, Mat originalFrame) {
		Size s = new Size(640, 480);
		Imgproc.resize(processedFrame, processedFrame, s);
		Imgproc.resize(originalFrame, originalFrame, s);

		BufferedImage originalImage = Test.MatToBufferedImage(originalFrame);
		BufferedImage processedImage = Test.MatToBufferedImage(processedFrame);
		originalPic.setIcon(new ImageIcon(originalImage));                    
		processedPic.setIcon(new ImageIcon(processedImage));
	}
	
	
	
	public void registerLowerBoundScalar(Scalar lb) {
		lowerBoundScalar = lb;
	}
	
	
	
	public void registerUpperBoundScalar(Scalar ub) {
		upperBoundScalar = ub;
	}
	
	
	
	public void show() {
		jFrame.setLocationRelativeTo(null);
		jFrame.setVisible(true);   
	}
	
	
	
	private static void updateConfigList() {
		List<String> configs = cConfig.listConfigs();
		DefaultListModel<String> model = new DefaultListModel<String>();

		for (String c : configs) {
			model.addElement(c);
		}
		cList.setModel(model);
	}
}

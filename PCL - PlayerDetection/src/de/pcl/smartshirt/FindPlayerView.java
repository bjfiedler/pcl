package de.pcl.smartshirt;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractButton;
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
	private boolean cameraMode = true;

	private JList<String> cList;
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
	private JLabel processedPicBlue;
	private JLabel processedPicGreen;
	
	private Scalar upperBoundScalar;
	private Scalar lowerBoundScalar;
	
	
	private JSlider hSliderLowGreen;
	private JSlider sSliderLowGreen;
	private JSlider vSliderLowGreen;
	private JLabel hLowGreen;
	private JLabel sLowGreen;
	private JLabel vLowGreen;
	private JSlider hSliderUpGreen;
	private JSlider sSliderUpGreen;
	private JSlider vSliderUpGreen;
	private JLabel vUpGreen;
	private JLabel sUpGreen;
	private JLabel hUpGreen;
	private Scalar lowerBoundScalarGreen;
	private Scalar upperBoundScalarGreen;
	private JTextField configNameGreen;
	private JList<String> cListGreen;
		
	
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
		processedPicBlue = new JLabel();
		processedPicGreen = new JLabel();

		hSliderLow = new JSlider(0, 360, 0);
		sSliderLow = new JSlider(0, 255, 100);
		vSliderLow = new JSlider(0, 255, 100);
		hLow = new JLabel("H-Lower: " + hSliderLow.getValue());
		sLow = new JLabel("S-Lower: " + sSliderLow.getValue());
		vLow = new JLabel("V-Lower: " + vSliderLow.getValue());

		hSliderUp = new JSlider(0, 360, 0);
		sSliderUp = new JSlider(0, 255, 255);
		vSliderUp = new JSlider(0, 255, 255);
		hUp = new JLabel("H-Upper: " + hSliderUp.getValue());
		sUp = new JLabel("S-Upper: " + sSliderUp.getValue());
		vUp = new JLabel("V-Upper: " + vSliderUp.getValue());
		
		hSliderLow.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				hLow.setText("H-Lower: " + hSliderLow.getValue());
				if (lowerBoundScalar != null) {
					double[] val = lowerBoundScalar.val;
					val[0] = hSliderLow.getValue();
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
		
		JButton switchMode = new JButton("Switch Input Mode");
		switchMode.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				cameraMode = !cameraMode;
			}
		});
		
		JButton vibrateBtn = new JButton("Make Vibration");
		vibrateBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					FindPlayer.sendUDP("JKL");
					Thread.sleep(1000);
					FindPlayer.sendUDP("jkl");					
				} catch (IOException | InterruptedException e1) {
					e1.printStackTrace();
				}
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

		jPanel.add(configName);
		jPanel.add(save);
		jPanel.add(cList);
		jPanel.add(remove);

		
		JPanel jPanelPic = new JPanel();
		
		
		
		jPanelPic.add(originalPic);
		jPanelPic.add(processedPicBlue);
		jPanelPic.add(processedPicGreen);
		jFrame.add(jPanelPic);
		jFrame.add(jPanel); 
		jFrame.add(vibrateBtn);
		jFrame.add(setupSlider());
		jFrame.add(switchMode);
		

		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setTitle("LiveStream BlackWhite");
		jFrame.setSize(1440, 770);
		jFrame.setVisible(false);   
		
		
		
		updateConfigList();

	}
	
	
	
	public void updateView(Mat processedFrameBlue, Mat processedFrameGreen, Mat originalFrame) {
		Size s = new Size(640, 480);
		Imgproc.resize(originalFrame, originalFrame, s);
		s = new Size(320, 240);
		Imgproc.resize(processedFrameBlue, processedFrameBlue, s);
		Imgproc.resize(processedFrameGreen, processedFrameGreen, s);
		

		BufferedImage originalImage = Test.MatToBufferedImage(originalFrame);
		BufferedImage processedImageBlue = Test.MatToBufferedImage(processedFrameBlue);
		BufferedImage processedImageGreen = Test.MatToBufferedImage(processedFrameGreen);
		originalPic.setIcon(new ImageIcon(originalImage));                    
		processedPicBlue.setIcon(new ImageIcon(processedImageBlue));
		processedPicGreen.setIcon(new ImageIcon(processedImageGreen));
	}
	
	
	
	public void registerLowerBoundScalar(Scalar lb) {
		lowerBoundScalar = lb;
		lb.set(new double[] {hSliderLow.getValue(), sSliderLow.getValue(), vSliderLow.getValue()});
	}
	
	
	
	public void registerUpperBoundScalar(Scalar ub) {
		upperBoundScalar = ub;
		ub.set(new double[] {hSliderUp.getValue(), sSliderUp.getValue(), vSliderUp.getValue()});
	}

	
	
	
	
	public void registerLowerBoundScalarGreen(Scalar lb) {
		lowerBoundScalarGreen = lb;
		lb.set(new double[] {hSliderLowGreen.getValue(), sSliderLowGreen.getValue(), vSliderLowGreen.getValue()});
	}
	
	
	
	public void registerUpperBoundScalarGreen(Scalar ub) {
		upperBoundScalarGreen = ub;
		ub.set(new double[] {hSliderUpGreen.getValue(), sSliderUpGreen.getValue(), vSliderUpGreen.getValue()});
	}
	
	
	
	
	
	public void show() {
		jFrame.setLocationRelativeTo(null);
		jFrame.setVisible(true);   
	}
	
	
	
	public boolean isCameraMode() {
		return cameraMode;
	}
	
	
	
	private void updateConfigList() {
		List<String> configs = cConfig.listConfigs();
		DefaultListModel<String> model = new DefaultListModel<String>();

		for (String c : configs) {
			model.addElement(c);
		}
		cList.setModel(model);
		cListGreen.setModel(model);
	}
	
	
	
	private JPanel setupSlider() {
		JPanel jPanel = new JPanel();
		JPanel panelLow = new JPanel();
		JPanel panelUp = new JPanel();

		FlowLayout flowLayout = new FlowLayout();	
		GridLayout gridLayout = new GridLayout(0, 2);

		jPanel.setLayout(flowLayout);
		panelLow.setLayout(gridLayout);
		panelUp.setLayout(gridLayout);

		panelLow.setBorder(BorderFactory.createTitledBorder("HSV-Lower-Range (Green)"));
		panelUp.setBorder(BorderFactory.createTitledBorder("HSV-Upper-Range (Green)"));

		hSliderLowGreen = new JSlider(0, 360, 0);
		sSliderLowGreen = new JSlider(0, 255, 100);
		vSliderLowGreen = new JSlider(0, 255, 100);
		hLowGreen = new JLabel("H-Lower: " + hSliderLow.getValue());
		sLowGreen = new JLabel("S-Lower: " + sSliderLow.getValue());
		vLowGreen = new JLabel("V-Lower: " + vSliderLow.getValue());

		hSliderUpGreen = new JSlider(0, 360, 0);
		sSliderUpGreen = new JSlider(0, 255, 255);
		vSliderUpGreen = new JSlider(0, 255, 255);
		hUpGreen = new JLabel("H-Upper: " + hSliderUp.getValue());
		sUpGreen = new JLabel("S-Upper: " + sSliderUp.getValue());
		vUpGreen = new JLabel("V-Upper: " + vSliderUp.getValue());
		
		hSliderLowGreen.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				hLowGreen.setText("H-Lower: " + hSliderLowGreen.getValue());
				if (lowerBoundScalarGreen != null) {
					double[] val = lowerBoundScalarGreen.val;
					val[0] = hSliderLowGreen.getValue();
				}
			}
		});
		
		sSliderLowGreen.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				sLowGreen.setText("S-Lower: " + sSliderLowGreen.getValue());
				if (lowerBoundScalarGreen != null) {
					double[] val = lowerBoundScalarGreen.val;
					val[1] = sSliderLowGreen.getValue();
				}
			}
		});
		
		vSliderLowGreen.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				vLowGreen.setText("V-Lower: " + vSliderLowGreen.getValue());
				if (lowerBoundScalarGreen != null) {
					double[] val = lowerBoundScalarGreen.val;
					val[2] = vSliderLowGreen.getValue();
				}
			}
		});
		
		hSliderUpGreen.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				hUpGreen.setText("H-Upper: " + hSliderUpGreen.getValue());
				if (upperBoundScalarGreen != null) {
					double[] val = upperBoundScalarGreen.val;
					val[0] = hSliderUpGreen.getValue();
				}
			}
		});
		
		sSliderUpGreen.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				sUpGreen.setText("S-Upper: " + sSliderUpGreen.getValue());
				if (upperBoundScalarGreen != null) {
					double[] val = upperBoundScalarGreen.val;
					val[1] = sSliderUpGreen.getValue();
				}
			}
		});
		
		vSliderUpGreen.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				vUpGreen.setText("V-Upper: " + vSliderUpGreen.getValue());
				if (upperBoundScalarGreen != null) {
					double[] val = upperBoundScalarGreen.val;
					val[2] = vSliderUpGreen.getValue();
				}
			}
		});

		panelLow.add(hLowGreen);
		panelLow.add(hSliderLowGreen);
		panelLow.add(sLowGreen);
		panelLow.add(sSliderLowGreen);
		panelLow.add(vLowGreen);
		panelLow.add(vSliderLowGreen);

		panelUp.add(hUpGreen);
		panelUp.add(hSliderUpGreen);
		panelUp.add(sUpGreen);
		panelUp.add(sSliderUpGreen);
		panelUp.add(vUpGreen);
		panelUp.add(vSliderUpGreen); 

		jPanel.add(panelLow);
		jPanel.add(panelUp);

		
		
		
		JButton save = new JButton("Save");
		save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String name = configNameGreen.getText();
				cConfig.saveConfig(name, 
						hSliderLowGreen.getValue(), 
						sSliderLowGreen.getValue(), 
						vSliderLowGreen.getValue(), 
						hSliderUpGreen.getValue(), 
						sSliderUpGreen.getValue(), 
						vSliderUpGreen.getValue());
				updateConfigList();
			}
		});

		JButton remove = new JButton("Remove");
		remove.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				String name = cListGreen.getSelectedValue();
				cConfig.removeConfig(name);
				updateConfigList();
			}
		});

		configNameGreen = new JTextField("default");
		cListGreen = new JList<String>();
		cListGreen.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				String name = cListGreen.getSelectedValue();
				if (name == null) {
					return;
				}
				cConfig.selectConfig(cListGreen.getSelectedValue());
				configNameGreen.setText(name);

				hSliderLowGreen.setValue(cConfig.getLowerH());
				sSliderLowGreen.setValue(cConfig.getLowerS());
				vSliderLowGreen.setValue(cConfig.getLowerV()); 
				hSliderUpGreen.setValue(cConfig.getUpperH());
				sSliderUpGreen.setValue(cConfig.getUpperS());
				vSliderUpGreen.setValue(cConfig.getUpperV());
			}
		});

		jPanel.add(configNameGreen);
		jPanel.add(save);
		jPanel.add(cListGreen);
		jPanel.add(remove);
		
		return jPanel;
	}
	
	
	
	
}

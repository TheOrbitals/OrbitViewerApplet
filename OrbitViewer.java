/**
 * Orbit Projector
 *
 * Example (Comet)
 *
 *   <APPLET CODE="OrbitViewer" WIDTH=510 HEIGHT=400>
 *   <PARAM NAME="Name"  VALUE="1P/Halley">
 *   <PARAM NAME="T"     VALUE="19860209.7695">
 *   <PARAM NAME="e"     VALUE="0.967267">
 *   <PARAM NAME="q"     VALUE="0.587096">
 *   <PARAM NAME="Peri"  VALUE="111.8466">
 *   <PARAM NAME="Node"  VALUE=" 58.1440">
 *   <PARAM NAME="Incl"  VALUE="162.2393">
 *   <PARAM NAME="Eqnx"  VALUE="1950.0">
 *   </APPLET>
 *
 * Example (Minor Planet)
 *
 *   <APPLET CODE="OrbitViewer" WIDTH=510 HEIGHT=400>
 *   <PARAM NAME="Name"  VALUE="Ceres(1)">
 *   <PARAM NAME="Epoch" VALUE="19991118.5">
 *   <PARAM NAME="M"     VALUE="356.648434">
 *   <PARAM NAME="e"     VALUE="0.07831587">
 *   <PARAM NAME="a"     VALUE="2.76631592">
 *   <PARAM NAME="Peri"  VALUE=" 73.917708">
 *   <PARAM NAME="Node"  VALUE=" 80.495123">
 *   <PARAM NAME="Incl"  VALUE=" 10.583393">
 *   <PARAM NAME="Eqnx"  VALUE="2000.0">
 *   </APPLET>
 *
 * Optional paramter "Date" specifies initial date to display.
 * If "Date" parameter omitted, it start with current date.
 *
 *   <PARAM NAME="Date"  VALUE="19860209.7695">
 *
 */

import java.applet.*;
import java.awt.*;
import java.util.*;
import java.math.*;
import astro.*;

/**
 * Main Applet Class
 */
public class OrbitViewer extends java.applet.Applet {
	/**
	 * Components
	 */
	private Scrollbar		scrollHorz;
	private Scrollbar		scrollVert;
	private Scrollbar		scrollZoom;
	private OrbitCanvas		orbitCanvas;
	private Button			buttonDate;

	private Button			buttonRevPlay;
	private Button			buttonRevStep;
	private Button			buttonStop;
	private Button			buttonForStep;
	private Button			buttonForPlay;
	private Choice			choiceTimeStep;
        private Choice                  choiceCenterObject;
        private Choice                  choiceOrbitObject;

	private Checkbox		checkPlanetName;
	private Checkbox		checkObjectName;
	private Checkbox		checkDistanceLabel;
	private Checkbox		checkDateLabel;

	private DateDialog		dateDialog = null;

	/**
	 * Player thread
	 */
	private OrbitPlayer		orbitPlayer;
	Thread					playerThread = null;

	/**
	 * Current Time Setting
	 */
	private ATime atime;

	/**
	 * Time step
	 */
	static final int timeStepCount = 8;
	static final String timeStepLabel[] = {
                "1 Hour",
		"1 Day",   "3 Days",   "10 Days",
		"1 Month", "3 Months", "6 Months",
		"1 Year"
	};
	static final TimeSpan timeStepSpan[] = {
                new TimeSpan(0, 0,  0, 1, 0, 0.0),
		new TimeSpan(0, 0,  1, 0, 0, 0.0),
		new TimeSpan(0, 0,  3, 0, 0, 0.0),
		new TimeSpan(0, 0, 10, 0, 0, 0.0),
		new TimeSpan(0, 1,  0, 0, 0, 0.0),
		new TimeSpan(0, 3,  0, 0, 0, 0.0),
		new TimeSpan(0, 6,  0, 0, 0, 0.0),
		new TimeSpan(1, 0,  0, 0, 0, 0.0),
	};
	public TimeSpan timeStep = timeStepSpan[1];
	public int      playDirection = ATime.F_INCTIME;

        /**
         * Centered Object
         */
        static final int CenterObjectCount = 11;
        static final String CenterObjectLabel[] = {
                "Sun",   "Asteroid/Comet", "Mercury", "Venus", "Earth",
                "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto"
        };
        public int CenterObjectSelected = 0;

        /**
         * Orbits Displayed
         */
        static final int OrbitDisplayCount = 14;
        static final String OrbitDisplayLabel[] = {
                "Default Orbits", "All Orbits", "No Orbits", "------",
                "Asteroid/Comet", "Mercury", "Venus", "Earth",
                "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Pluto"
        };
        public int OrbitCount = 11;
        public boolean OrbitDisplay[] = {false, true, true, true, true, true, true,
                                         false, false, false, false };
        public boolean OrbitDisplayDefault[] = {false, true, true, true, true, true, true,
                                         false, false, false, false };



	/**
	 * Limit of ATime
	 */
//	private ATime minATime = new ATime(-30000,1,1,0,0,0.0,0.0);
        private ATime minATime = new ATime( 1600,1,1,0,0,0.0,0.0);
	private ATime maxATime = new ATime( 2200,1,1,0,0,0.0,0.0);

	/**
	 * Initial Settings
	 */
	static final int initialScrollVert = 90+40;
	static final int initialScrollHorz = 255;
	static final int initialScrollZoom = 67;
	static final int fontSize = 16;

	/**
	 * Applet information
	 */
	public String getAppletInfo() {
		return "OrbitViewer v1.3 Copyright(C) 1996-2001 by O.Ajiki/R.Baalke";
	}

	/**
	 * Parameter Information
	 */
	public String[][] getParameterInfo() {
		String info[][] = {
			{ "Name",
			  "String", "Name of the object          ex. 1P/Halley"     },
			{ "T",
			  "double", "Time of perihelion passage  ex. 19860209.7695" },
			{ "e",
			  "double", "Eccentricity                ex. 0.967267"      },
			{ "q",
			  "double", "Perihelion distance AU      ex. 0.587096"      },
			{ "Peri",
			  "double", "Argument of perihelion deg. ex. 111.8466"      },
			{ "Node",
			  "double", "Ascending node deg.         ex.  58.1440"      },
			{ "Incl",
			  "double", "Inclination deg.            ex. 162.2393"      },
			{ "Eqnx",
			  "double", "Year of equinox             ex. 1950.0"        },
			{ "Epoch",
			  "double", "Year/Month/Day of epoch     ex. 19991118.5"    },
			{ "M",
			  "double", "Mean anomaly deg.           ex. 356.648434"    },
			{ "a",
			  "double", "Semimajor axis AU           ex. 2.76631592"    },
			{ "Date",
			  "double", "Initial date                ex. 19860209.7695" },
		};
		return info;
	}

	/**
	 * Convert time in format "YYYYMMDD.D" to ATime
	 */
	private ATime ymdStringToAtime(String strYmd) {
		double fYmd = Double.valueOf(strYmd).doubleValue();
		int nYear = (int)Math.floor(fYmd / 10000.0);
		fYmd -= (double)nYear * 10000.0;
		int nMonth = (int)Math.floor(fYmd / 100.0);
		double fDay = fYmd - (double)nMonth * 100.0;
		return new ATime(nYear, nMonth, fDay, 0.0);
	}

	/**
	 * Get required double parameter
	 */
	private double getRequiredParameter(String strName) {
		String strValue = getParameter(strName);
		if (strValue == null) {
			throw new Error("Required parameter '"
							   + strName + "' not found.");
		}
		return Double.valueOf(strValue).doubleValue();
	}

	/**
	 * Get orbital elements of the object from applet parameter
	 */
	private Comet getObject() {
		String strName = getParameter("Name");
		if (strName == null) {
			strName = "Object";
		}
		double e, q;
		ATime T;
		String strParam;
		if ((strParam = getParameter("e")) == null) {
			throw new Error("required parameter 'e' not found.");
		}
		e = Double.valueOf(strParam).doubleValue();
		if ((strParam = getParameter("T")) != null) {
			T = ymdStringToAtime(strParam);
			if ((strParam = getParameter("q")) != null) {
				q = Double.valueOf(strParam).doubleValue();
			} else if ((strParam = getParameter("a")) != null) {
				double a = Double.valueOf(strParam).doubleValue();
				if (Math.abs(e - 1.0) < 1.0e-15) {
					throw new Error("Orbit is parabolic, but 'q' not found.");
				}
				q = a * (1.0 - e);
			} else {
				throw new Error("Required parameter 'q' or 'a' not found.");
			}
		} else if ((strParam = getParameter("Epoch")) != null) {
			ATime Epoch = ymdStringToAtime(strParam);
			if (e > 0.95) {
				throw new
					Error("Orbit is nearly parabolic, but 'T' not found.");
			}
			double a;
			if ((strParam = getParameter("a")) != null) {
				a = Double.valueOf(strParam).doubleValue();
				q = a * (1.0 - e);
			} else if ((strParam = getParameter("q")) != null) {
				q = Double.valueOf(strParam).doubleValue();
				a = q / (1.0 - e);
			} else {
				throw new Error("Required parameter 'q' or 'a' not found.");
			}
			if (q < 1.0e-15) {
				throw new Error("Too small perihelion distance.");
			}
			double n = Astro.GAUSS / (a * Math.sqrt(a));
			if ((strParam = getParameter("M")) == null) {
				throw new Error("Required parameter 'M' not found.");
			}
			double M = Double.valueOf(strParam).doubleValue()
				* Math.PI / 180.0;
			if (M < Math.PI) {
				T = new ATime(Epoch.getJd() - M / n, 0.0);
			} else {
				T = new ATime(Epoch.getJd() + (Math.PI*2.0 - M) / n, 0.0);
			}
		} else {
			throw new Error("Required parameter 'T' or 'Epoch' not found.");
		}
		return new Comet(strName, T.getJd(), e, q,
						 getRequiredParameter("Peri")*Math.PI/180.0,
						 getRequiredParameter("Node")*Math.PI/180.0,
						 getRequiredParameter("Incl")*Math.PI/180.0,
						 getRequiredParameter("Eqnx"));
	}

	/**
	 * Limit ATime between minATime and maxATime
	 */
	private ATime limitATime(ATime atime) {
		if (atime.getJd() <= minATime.getJd()) {
			return new ATime(minATime);
		} else if (maxATime.getJd() <= atime.getJd()) {
			return new ATime(maxATime);
		}
		return atime;
	}

	/**
	 * Set date and redraw canvas
	 */
	private void setNewDate() {
		this.atime = limitATime(this.atime);
		orbitCanvas.setDate(this.atime);
		orbitCanvas.repaint();
	}

	/**
	 * OrbitPlayer interface
	 */
	public ATime getAtime() {
		return atime;
	}
	public void setNewDate(ATime atime) {
		this.atime = limitATime(atime);
		orbitCanvas.setDate(this.atime);
		orbitCanvas.repaint();
	}

	/**
	 * Initialization of applet
	 */
	public void init() {
		this.setBackground(Color.white);
		//
		// Main Panel
		//
		Panel mainPanel = new Panel();
		GridBagLayout gblMainPanel = new GridBagLayout();
		GridBagConstraints gbcMainPanel = new GridBagConstraints();
		gbcMainPanel.fill = GridBagConstraints.BOTH;
		mainPanel.setLayout(gblMainPanel);

		// Orbit Canvas
		Comet object = getObject();
		String strParam;
		if ((strParam = getParameter("Date")) != null) {
			this.atime = ymdStringToAtime(strParam);
		} else {
			Date date = new Date();
			this.atime = new ATime(date.getYear() + 1900, date.getMonth() + 1,
								   (double)date.getDate(), 0.0);
		}
		orbitCanvas = new OrbitCanvas(object, this.atime);
		gbcMainPanel.weightx = 1.0;
		gbcMainPanel.weighty = 1.0;
		gbcMainPanel.gridwidth = GridBagConstraints.RELATIVE;
		gblMainPanel.setConstraints(orbitCanvas, gbcMainPanel);
		mainPanel.add(orbitCanvas);

		// Vertical Scrollbar
		scrollVert = new Scrollbar(Scrollbar.VERTICAL,
								   initialScrollVert, 12, 0, 180+12);
		gbcMainPanel.weightx = 0.0;
		gbcMainPanel.weighty = 0.0;
		gbcMainPanel.gridwidth = GridBagConstraints.REMAINDER;
		gblMainPanel.setConstraints(scrollVert, gbcMainPanel);
		mainPanel.add(scrollVert);
		orbitCanvas.setRotateVert(180 - scrollVert.getValue());

		// Horizontal Scrollbar
		scrollHorz = new Scrollbar(Scrollbar.HORIZONTAL,
								   initialScrollHorz, 15, 0, 360+15);
		gbcMainPanel.weightx = 1.0;
		gbcMainPanel.weighty = 0.0;
		gbcMainPanel.gridwidth = 1;
		gblMainPanel.setConstraints(scrollHorz, gbcMainPanel);
		mainPanel.add(scrollHorz);
		orbitCanvas.setRotateHorz(270 - scrollHorz.getValue());

		// Right-Bottom Corner Rectangle
		Panel cornerPanel = new Panel();
		gbcMainPanel.weightx = 0.0;
		gbcMainPanel.weighty = 0.0;
		gbcMainPanel.gridwidth = GridBagConstraints.REMAINDER;
		gblMainPanel.setConstraints(cornerPanel, gbcMainPanel);
		mainPanel.add(cornerPanel);

		//
		// Control Panel
		//
		Panel ctrlPanel = new Panel();
		GridBagLayout gblCtrlPanel = new GridBagLayout();
		GridBagConstraints gbcCtrlPanel = new GridBagConstraints();
		gbcCtrlPanel.fill = GridBagConstraints.BOTH;
		ctrlPanel.setLayout(gblCtrlPanel);
		ctrlPanel.setBackground(Color.white);

		// Set Date Button
		buttonDate = new Button(" Date ");
		buttonDate.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcCtrlPanel.gridx = 0;
		gbcCtrlPanel.gridy = 0;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 1.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 2;
		gbcCtrlPanel.insets = new Insets(0, 0, 0, 12);
		gblCtrlPanel.setConstraints(buttonDate, gbcCtrlPanel);
		ctrlPanel.add(buttonDate);

		// Reverse-Play Button
		buttonRevPlay = new Button("<<");
		buttonRevPlay.setFont(new Font("Dialog", Font.BOLD, fontSize-2));
		gbcCtrlPanel.gridx = 1;
		gbcCtrlPanel.gridy = 0;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 0, 3, 0);
		gblCtrlPanel.setConstraints(buttonRevPlay, gbcCtrlPanel);
		ctrlPanel.add(buttonRevPlay);

		// Reverse-Step Button
		buttonRevStep = new Button("|<");
		buttonRevStep.setFont(new Font("Dialog", Font.BOLD, fontSize-2));
		gbcCtrlPanel.gridx = 2;
		gbcCtrlPanel.gridy = 0;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 0, 3, 0);
		gblCtrlPanel.setConstraints(buttonRevStep, gbcCtrlPanel);
		ctrlPanel.add(buttonRevStep);

		// Stop Button
		buttonStop = new Button("||");
		buttonStop.setFont(new Font("Dialog", Font.BOLD, fontSize-2));
		gbcCtrlPanel.gridx = 3;
		gbcCtrlPanel.gridy = 0;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 0, 3, 0);
		gblCtrlPanel.setConstraints(buttonStop, gbcCtrlPanel);
		ctrlPanel.add(buttonStop);

		// Step Button
		buttonForStep = new Button(">|");
		buttonForStep.setFont(new Font("Dialog", Font.BOLD, fontSize-2));
		gbcCtrlPanel.gridx = 4;
		gbcCtrlPanel.gridy = 0;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 0, 3, 0);
		gblCtrlPanel.setConstraints(buttonForStep, gbcCtrlPanel);
		ctrlPanel.add(buttonForStep);

		// Play Button
		buttonForPlay = new Button(">>");
		buttonForPlay.setFont(new Font("Dialog", Font.BOLD, fontSize-2));
		gbcCtrlPanel.gridx = 5;
		gbcCtrlPanel.gridy = 0;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 0, 3, 0);
		gblCtrlPanel.setConstraints(buttonForPlay, gbcCtrlPanel);
		ctrlPanel.add(buttonForPlay);

		// Step choice box
		choiceTimeStep = new Choice();
		choiceTimeStep.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcCtrlPanel.gridx = 1;
		gbcCtrlPanel.gridy = 1;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 5;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
		gblCtrlPanel.setConstraints(choiceTimeStep, gbcCtrlPanel);
		ctrlPanel.add(choiceTimeStep);
		for (int i = 0; i < timeStepCount; i++) {
			choiceTimeStep.addItem(timeStepLabel[i]);
                choiceTimeStep.select(timeStepLabel[1]);
		}

               // Center Object Label
                Label centerLabel = new Label("Center:");
                centerLabel.setAlignment(Label.LEFT);
                centerLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
                gbcCtrlPanel.gridx = 0;
                gbcCtrlPanel.gridy = 2;
                gbcCtrlPanel.weightx = 0.0;
                gbcCtrlPanel.weighty = 1.0;
                gbcCtrlPanel.gridwidth = 1;
                gbcCtrlPanel.gridheight = 1;
                gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
                gblCtrlPanel.setConstraints(centerLabel, gbcCtrlPanel);
                ctrlPanel.add(centerLabel);

               // Center Object choice box
                choiceCenterObject = new Choice();
                choiceCenterObject.setFont(new Font("Dialog", Font.PLAIN, fontSize));
                gbcCtrlPanel.gridx = 1;
                gbcCtrlPanel.gridy = 2;
                gbcCtrlPanel.weightx = 0.0;
                gbcCtrlPanel.weighty = 0.0;
                gbcCtrlPanel.gridwidth = 5;
                gbcCtrlPanel.gridheight = 1;
                gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
                gblCtrlPanel.setConstraints(choiceCenterObject, gbcCtrlPanel);
                ctrlPanel.add(choiceCenterObject);
                for (int i = 0; i < CenterObjectCount; i++) {
                        choiceCenterObject.addItem(CenterObjectLabel[i]);
                }
                orbitCanvas.SelectCenterObject(0);

               // Display Orbits Label
                Label orbitLabel = new Label("Orbits:");
                orbitLabel.setAlignment(Label.LEFT);
                orbitLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
                gbcCtrlPanel.gridx = 0;
                gbcCtrlPanel.gridy = 3;
                gbcCtrlPanel.weightx = 0.0;
                gbcCtrlPanel.weighty = 1.0;
                gbcCtrlPanel.gridwidth = 1;
                gbcCtrlPanel.gridheight = 1;
                gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
                gblCtrlPanel.setConstraints(orbitLabel, gbcCtrlPanel);
                ctrlPanel.add(orbitLabel);

              // Display Orbit choice box
                choiceOrbitObject = new Choice();
                choiceOrbitObject.setFont(new Font("Dialog", Font.PLAIN, fontSize));
                gbcCtrlPanel.gridx = 1;
                gbcCtrlPanel.gridy = 3;
                gbcCtrlPanel.weightx = 0.0;
                gbcCtrlPanel.weighty = 0.0;
                gbcCtrlPanel.gridwidth = 5;
                gbcCtrlPanel.gridheight = 1;
                gbcCtrlPanel.insets = new Insets(0, 0, 0, 0);
                gblCtrlPanel.setConstraints(choiceOrbitObject, gbcCtrlPanel);
                ctrlPanel.add(choiceOrbitObject);
                for (int i = 0; i < OrbitDisplayCount; i++) {
                        choiceOrbitObject.addItem(OrbitDisplayLabel[i]);
                }
                for (int i = 0; i < OrbitCount; i++) {
                        OrbitDisplay[i] = OrbitDisplayDefault[i];
                }
                orbitCanvas.SelectOrbits(OrbitDisplay, OrbitCount);


		// Date Label Checkbox
		checkDateLabel = new Checkbox("Date Label");
		checkDateLabel.setState(true);
		checkDateLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcCtrlPanel.gridx = 6;
		gbcCtrlPanel.gridy = 0;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 12, 0, 0);
		gblCtrlPanel.setConstraints(checkDateLabel, gbcCtrlPanel);
		ctrlPanel.add(checkDateLabel);
		orbitCanvas.switchPlanetName(checkDateLabel.getState());

		// Planet Name Checkbox
		checkPlanetName = new Checkbox("Planet Labels");
		checkPlanetName.setState(true);
		checkPlanetName.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcCtrlPanel.gridx = 7;
		gbcCtrlPanel.gridy = 0;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 12, 0, 0);
		gblCtrlPanel.setConstraints(checkPlanetName, gbcCtrlPanel);
		ctrlPanel.add(checkPlanetName);
		orbitCanvas.switchPlanetName(checkPlanetName.getState());

		// Distance Label Checkbox
		checkDistanceLabel = new Checkbox("Distance");
		checkDistanceLabel.setState(true);
		checkDistanceLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcCtrlPanel.gridx = 6;
		gbcCtrlPanel.gridy = 1;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 12, 0, 0);
		gblCtrlPanel.setConstraints(checkDistanceLabel, gbcCtrlPanel);
		ctrlPanel.add(checkDistanceLabel);
		orbitCanvas.switchPlanetName(checkDistanceLabel.getState());

		// Object Name Checkbox
		checkObjectName = new Checkbox("Object Label");
		checkObjectName.setState(true);
		checkObjectName.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcCtrlPanel.gridx = 7;
		gbcCtrlPanel.gridy = 1;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 0.0;
		gbcCtrlPanel.gridwidth = 1;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 12, 0, 0);
		gblCtrlPanel.setConstraints(checkObjectName, gbcCtrlPanel);
		ctrlPanel.add(checkObjectName);
		orbitCanvas.switchObjectName(checkObjectName.getState());

		// Zoom Label
		Label zoomLabel = new Label("Zoom:");
		zoomLabel.setAlignment(Label.LEFT);
		zoomLabel.setFont(new Font("Dialog", Font.PLAIN, fontSize));
		gbcCtrlPanel.gridx = 6;
		gbcCtrlPanel.gridy = 2;
		gbcCtrlPanel.weightx = 0.0;
		gbcCtrlPanel.weighty = 1.0;
		gbcCtrlPanel.gridwidth = 2;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(10, 12, 0, 0);
		gblCtrlPanel.setConstraints(zoomLabel, gbcCtrlPanel);
		ctrlPanel.add(zoomLabel);

		// Zoom Scrollbar
		scrollZoom = new Scrollbar(Scrollbar.HORIZONTAL,
								   initialScrollZoom, 15, 5, 450);
		gbcCtrlPanel.gridx = 6;
		gbcCtrlPanel.gridy = 3;
		gbcCtrlPanel.weightx = 1.0;
		gbcCtrlPanel.weighty = 1.0;
		gbcCtrlPanel.gridwidth  = 2;
		gbcCtrlPanel.gridheight = 1;
		gbcCtrlPanel.insets = new Insets(0, 12, 6, 2);
		gblCtrlPanel.setConstraints(scrollZoom, gbcCtrlPanel);
		ctrlPanel.add(scrollZoom);
		orbitCanvas.setZoom(scrollZoom.getValue());

		//
		// Applet Layout
		//
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		setLayout(gbl);
		gbc.fill = GridBagConstraints.BOTH;

		// Main Panel
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbl.setConstraints(mainPanel, gbc);
		add(mainPanel);

		// Control Panel
		gbc.weightx = 1.0;
		gbc.weighty = 0.0;
		gbc.gridwidth  = GridBagConstraints.REMAINDER;
		gbc.gridheight = GridBagConstraints.REMAINDER;
		gbc.insets = new Insets(6, 0, 0, 0);
		gbl.setConstraints(ctrlPanel, gbc);
		add(ctrlPanel);

		// Player Thread
		orbitPlayer = new OrbitPlayer(this);
		playerThread = null;
	}

	/**
	 * Override Function start()
	 */
	public void start() {
		// if you want, you can initialize date here
	}

	/**
	 * Override Function stop()
	 */
	public void stop() {
		if (dateDialog != null) {
			dateDialog.dispose();
			endDateDialog(null);
		}
		if (playerThread != null) {
			playerThread.stop();
			playerThread = null;
			buttonDate.enable();
		}
	}

	/**
	 * Destroy the applet
	 */
	public void destroy() {
		removeAll();
	}

	/**
	 * Event Handler
	 */
    public boolean handleEvent(Event evt) {
		switch (evt.id) {
		case Event.SCROLL_ABSOLUTE:
		case Event.SCROLL_LINE_DOWN:
		case Event.SCROLL_LINE_UP:
		case Event.SCROLL_PAGE_UP:
		case Event.SCROLL_PAGE_DOWN:
			if (evt.target == scrollHorz) {
				orbitCanvas.setRotateHorz(270 - scrollHorz.getValue());
			} else if (evt.target == scrollVert) {
				orbitCanvas.setRotateVert(180 - scrollVert.getValue());
			} else if (evt.target == scrollZoom) {
				orbitCanvas.setZoom(scrollZoom.getValue());
			} else {
				return false;
			}
			orbitCanvas.repaint();
			return true;
		case Event.ACTION_EVENT:
			if (evt.target == buttonDate) {					// Set Date
				dateDialog = new DateDialog(this, atime);
				buttonDate.disable();
				return true;
			} else if (evt.target == buttonForPlay) {		// ForPlay
				if (playerThread != null
					&&  playDirection != ATime.F_INCTIME) {
					playerThread.stop();
					playerThread = null;
				}
				if (playerThread == null) {
					buttonDate.disable();
					playDirection = ATime.F_INCTIME;
					playerThread = new Thread(orbitPlayer);
					playerThread.setPriority(Thread.MIN_PRIORITY);
					playerThread.start();
				}
			} else if (evt.target == buttonRevPlay) {		// RevPlay
				if (playerThread != null
					&&  playDirection != ATime.F_DECTIME) {
					playerThread.stop();
					playerThread = null;
				}
				if (playerThread == null) {
					buttonDate.disable();
					playDirection = ATime.F_DECTIME;
					playerThread = new Thread(orbitPlayer);
					playerThread.setPriority(Thread.MIN_PRIORITY);
					playerThread.start();
				}
			} else if (evt.target == buttonStop) {			// Stop
				if (playerThread != null) {
					playerThread.stop();
					playerThread = null;
					buttonDate.enable();
				}
			} else if (evt.target == buttonForStep) {		// +1 Step
				atime.changeDate(timeStep, ATime.F_INCTIME);
				setNewDate();
				return true;
			} else if (evt.target == buttonRevStep) {		// -1 Step
				atime.changeDate(timeStep, ATime.F_DECTIME);
				setNewDate();
				return true;
			} else if (evt.target == checkPlanetName) {		// Planet Name
				orbitCanvas.switchPlanetName(checkPlanetName.getState());
				orbitCanvas.repaint();
				return true;
			} else if (evt.target == checkObjectName) {		// Object Name
				orbitCanvas.switchObjectName(checkObjectName.getState());
				orbitCanvas.repaint();
				return true;
			} else if (evt.target == checkDistanceLabel) {	// Distance
				orbitCanvas.switchDistanceLabel(checkDistanceLabel.getState());
				orbitCanvas.repaint();
				return true;
			} else if (evt.target == checkDateLabel) {		// Date
				orbitCanvas.switchDateLabel(checkDateLabel.getState());
				orbitCanvas.repaint();
				return true;
			} else if (evt.target == choiceTimeStep) {		// Time Step
				for (int i = 0; i < timeStepCount; i++) {
					if ((String)evt.arg == timeStepLabel[i]) {
						timeStep = timeStepSpan[i];
						break;
					}
				}
			} else if (evt.target == choiceCenterObject) {    // Center Object
				for (int i = 0; i < CenterObjectCount; i++) {
					if ((String)evt.arg == CenterObjectLabel[i]) {
						CenterObjectSelected = i;
						orbitCanvas.SelectCenterObject(i);
						orbitCanvas.repaint();
						break;
					}
				}
			} else if (evt.target == choiceOrbitObject) {    // Orbit Display
				for (int i = 0; i < OrbitDisplayCount; i++) {
					if ((String)evt.arg == OrbitDisplayLabel[i]) {
						if (i == 1) {
							for (int j = 0; j < OrbitCount; j++) {
								OrbitDisplay[j] = true;
							}
						}
						else if (i == 2) {
							for (int j = 0; j < OrbitCount; j++) {
								OrbitDisplay[j] = false;
							}
						}
						else if (i == 0) {
							for (int j = 0; j < OrbitCount; j++) {
								OrbitDisplay[j] = OrbitDisplayDefault[j];
							}
						}
						else if (i > 3) {
							if (OrbitDisplay[i-3]) {
								OrbitDisplay[i-3] = false;
							}
							else {
								OrbitDisplay[i-3] = true;
							}
						}
						evt.arg = OrbitDisplayLabel[0];
						orbitCanvas.SelectOrbits(OrbitDisplay, OrbitCount);
						orbitCanvas.repaint();
						break;
					}
				}
			}
			return false;
		default:
			return false;
		}
    }

	/**
	 * message sent by DateDialog (when disposed)
	 */
	public void endDateDialog(ATime atime) {
		dateDialog = null;
		buttonDate.enable();
		if (atime != null) {
			this.atime = limitATime(atime);
			orbitCanvas.setDate(atime);
			orbitCanvas.repaint();
		}
	}
}

/**
 * Player Class
 */
class OrbitPlayer implements Runnable {
	OrbitViewer	orbitViewer;

	/**
	 * Constructor
	 */
	public OrbitPlayer(OrbitViewer orbitViewer) {
		this.orbitViewer = orbitViewer;
	}

	/**
	 * Play forever
	 */
	public void run() {
		while (true) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				break;
			}
			ATime atime = orbitViewer.getAtime();
			atime.changeDate(orbitViewer.timeStep, orbitViewer.playDirection);
			orbitViewer.setNewDate(atime);
		}
	}
}

/**
 * Orbit Canvas
 */
class OrbitCanvas extends Canvas {

	/**
	 * Orbital Element (Initialized in Constructor)
	 */
	private Comet object;

	/**
	 * Orbital Curve Class (Initialized in Constructor)
	 */
	private CometOrbit  objectOrbit;
	private PlanetOrbit planetOrbit[];
	private double epochPlanetOrbit;

	/**
	 * Date
	 */
	private ATime atime;

	/**
	 * Position of the Object and Planets
	 */
	private Xyz objectPos;
	private Xyz planetPos[];
  private int CenterObjectSelected;
  private boolean OrbitDisplay[];

	/**
	 * Projection Parameters
	 */
	private double fRotateH = 0.0;
	private double fRotateV = 0.0;
	private double fZoom = 5.0;

	/**
	 * Rotation Matrix
	 */
	private Matrix mtxToEcl;
	private double epochToEcl;
	private Matrix mtxRotate;
	private int nX0, nY0;	// Origin

	/**
	 * Size of Canvas
	 */
	private Dimension sizeCanvas;

	/**
	 * Colors
	 */
	private Color colorObjectOrbitUpper = new Color(0x00f5ff);
	private Color colorObjectOrbitLower = new Color(0x0000ff);
	private Color colorObject           = new Color(0x00ffff);
	private Color colorObjectName       = new Color(0x00cccc);
	private Color colorPlanetOrbitUpper = new Color(0xffffff);
	private Color colorPlanetOrbitLower = new Color(0x808080);
	private Color colorPlanet			= new Color(0x00ff00);
	private Color colorPlanetName		= new Color(0x00aa00);
	private Color colorSun              = new Color(0xd04040);
	private Color colorAxisPlus         = new Color(0xffff00);
	private Color colorAxisMinus        = new Color(0x555500);
	private Color colorInformation      = new Color(0xffffff);

	/**
	 * Fonts
	 */
	private Font fontObjectName  = new Font("Helvetica", Font.BOLD, 14);
	private Font fontPlanetName  = new Font("Helvetica", Font.PLAIN, 14);
	private Font fontInformation = new Font("Helvetica", Font.BOLD, 14);

	/**
	 * off-screen Image
	 */
	Image offscreen;

	/**
	 * Object Name Drawing Flag
	 */
	boolean bPlanetName;
	boolean bObjectName;
	boolean bDistanceLabel;
	boolean bDateLabel;

	/**
	 * Constructor
	 */
	public OrbitCanvas(Comet object, ATime atime) {
		planetPos = new Xyz[9];
                OrbitDisplay = new boolean[11];
		this.object = object;
		this.objectOrbit = new CometOrbit(object, 120);
		this.planetOrbit = new PlanetOrbit[9];
		updatePlanetOrbit(atime);
		updateRotationMatrix(atime);
		// Set Initial Date
		this.atime = atime;
		setDate(this.atime);
		// no offscreen image
		offscreen = null;
		// no name labels
		bPlanetName = false;
		bObjectName = false;
		bDistanceLabel = true;
		bDateLabel = true;
		repaint();
	}

	/**
	 * Make Planet Orbit
	 */
	private void updatePlanetOrbit(ATime atime) {
		for (int i = Planet.MERCURY; i <= Planet.PLUTO; i++) {
			this.planetOrbit[i - Planet.MERCURY]
				= new PlanetOrbit(i, atime, 48);
		}
		this.epochPlanetOrbit = atime.getJd();
	}

	/**
	 * Rotation Matrix Equatorial(2000)->Ecliptic(DATE)
	 */
	private void updateRotationMatrix(ATime atime) {
		Matrix mtxPrec = Matrix.PrecMatrix(Astro.JD2000, atime.getJd());
		Matrix mtxEqt2Ecl = Matrix.RotateX(ATime.getEp(atime.getJd()));
		this.mtxToEcl = mtxEqt2Ecl.Mul(mtxPrec);
		this.epochToEcl = atime.getJd();
	}

	/**
	 * Horizontal Rotation Parameter Set
	 */
	public void setRotateHorz(int nRotateH) {
		this.fRotateH = (double)nRotateH;
	}

	/**
	 * Vertical Rotation Parameter Set
	 */
	public void setRotateVert(int nRotateV) {
		this.fRotateV = (double)nRotateV;
	}

	/**
	 * Zoom Parameter Set
	 */
	public void setZoom(int nZoom) {
		this.fZoom = (double)nZoom;
	}

	/**
	 * Date Parameter Set
	 */
	public void setDate(ATime atime) {
		this.atime = atime;
		objectPos = object.GetPos(atime.getJd());
		for (int i = 0; i < 9; i++) {
			planetPos[i] = Planet.getPos(Planet.MERCURY+i, atime);
		}
	}

	/**
	 * Switch Planet Name ON/OFF
	 */
	public void switchPlanetName(boolean bPlanetName) {
		this.bPlanetName = bPlanetName;
	}


        /**
         * Select Orbits
         */
        public void SelectOrbits(boolean OrbitDisplay[], int OrbitCount) {
           for (int i=0; i< OrbitCount; i++)
           {
                this.OrbitDisplay[i] = OrbitDisplay[i];
           }
        }

        /**
         * Select Center Object
         */
        public void SelectCenterObject(int CenterObjectSelected) {
                this.CenterObjectSelected = CenterObjectSelected;
        }

	/**
	 * Switch Object Name ON/OFF
	 */
	public void switchObjectName(boolean bObjectName) {
		this.bObjectName = bObjectName;
	}

	/**
	 * Switch Distance Label ON/OFF
	 */
	public void switchDistanceLabel(boolean bDistanceLabel) {
		this.bDistanceLabel = bDistanceLabel;
	}

	/**
	 * Switch Date Label ON/OFF
	 */
	public void switchDateLabel(boolean bDateLabel) {
		this.bDateLabel = bDateLabel;
	}

	/**
	 * Get (X,Y) on Canvas from Xyz
	 */
	private Point getDrawPoint(Xyz xyz) {
		// 600 means 5...fZoom...100 -> 120AU...Width...6AU
		double fMul = this.fZoom * (double)sizeCanvas.width / 600.0
							* (1.0 + xyz.fZ / 250.0);		// Parse
		int nX = this.nX0 + (int)Math.round(xyz.fX * fMul);
		int nY = this.nY0 - (int)Math.round(xyz.fY * fMul);
		return new Point(nX, nY);
	}

	/**
	 * Draw Planets' Orbit
	 */
	private void drawPlanetOrbit(Graphics g, PlanetOrbit planetOrbit,
						 Color colorUpper, Color colorLower) {
		Point point1, point2;
		Xyz xyz = planetOrbit.getAt(0).Rotate(this.mtxToEcl)
							  .Rotate(this.mtxRotate);
		point1 = getDrawPoint(xyz);
		for (int i = 1; i <= planetOrbit.getDivision(); i++) {
			xyz = planetOrbit.getAt(i).Rotate(this.mtxToEcl);
			if (xyz.fZ >= 0.0) {
				g.setColor(colorUpper);
			} else {
				g.setColor(colorLower);
			}
			xyz = xyz.Rotate(this.mtxRotate);
			point2 = getDrawPoint(xyz);
			g.drawLine(point1.x, point1.y, point2.x, point2.y);
			point1 = point2;
		}
	}

        /**
         * Draw Earth's Orbit
         */
        private void drawEarthOrbit(Graphics g, PlanetOrbit planetOrbit,
                                                 Color colorUpper, Color colorLower) {
                Point point1, point2;
                Xyz xyz = planetOrbit.getAt(0).Rotate(this.mtxToEcl)
                                                          .Rotate(this.mtxRotate);
                point1 = getDrawPoint(xyz);
                for (int i = 1; i <= planetOrbit.getDivision(); i++) {
                        xyz = planetOrbit.getAt(i).Rotate(this.mtxToEcl);
                        g.setColor(colorUpper);
                        xyz = xyz.Rotate(this.mtxRotate);
                        point2 = getDrawPoint(xyz);
                        g.drawLine(point1.x, point1.y, point2.x, point2.y);
                        point1 = point2;
                }
        }

	/**
	 * Draw Planets' Body
	 */
	private void drawPlanetBody(Graphics og, Xyz planetPos, String strName) {
		Xyz xyz = planetPos.Rotate(this.mtxRotate);
		Point point = getDrawPoint(xyz);
		og.setColor(colorPlanet);
		og.fillArc(point.x - 2, point.y - 2, 5, 5, 0, 360);
		if (bPlanetName) {
			og.setColor(colorPlanetName);
			og.drawString(strName, point.x + 5, point.y);
		}
	}

	/**
	 * Draw Ecliptic Axis
	 */
	private void drawEclipticAxis(Graphics og) {
		Xyz xyz;
		Point point;

		og.setColor(colorAxisMinus);
		// -X
		xyz = (new Xyz(-50.0, 0.0,  0.0)).Rotate(this.mtxRotate);
		point = getDrawPoint(xyz);
		og.drawLine(this.nX0, this.nY0, point.x, point.y);

		// -Z
		xyz = (new Xyz(0.0, 00.0, -50.0)).Rotate(this.mtxRotate);
		point = getDrawPoint(xyz);
		og.drawLine(this.nX0, this.nY0, point.x, point.y);

		og.setColor(colorAxisPlus);
		// +X
		xyz = (new Xyz( 50.0, 0.0,  0.0)).Rotate(this.mtxRotate);
		point = getDrawPoint(xyz);
		og.drawLine(this.nX0, this.nY0, point.x, point.y);
		// +Z
		xyz = (new Xyz(0.0, 00.0,  50.0)).Rotate(this.mtxRotate);
		point = getDrawPoint(xyz);
		og.drawLine(this.nX0, this.nY0, point.x, point.y);
	}

	/**
	 * update (paint without clearing background)
	 */
	public void update(Graphics g) {
                 Point point3;
                 Xyz xyz, xyz1;

		// Calculate Drawing Parameter
		Matrix mtxRotH = Matrix.RotateZ(this.fRotateH * Math.PI / 180.0);
		Matrix mtxRotV = Matrix.RotateX(this.fRotateV * Math.PI / 180.0);
		this.mtxRotate = mtxRotV.Mul(mtxRotH);

		this.nX0 = this.sizeCanvas.width  / 2;
		this.nY0 = this.sizeCanvas.height / 2;

                if (Math.abs(epochToEcl - atime.getJd()) > 365.2422 * 5) {
                        updateRotationMatrix(atime);
                }

                // If center object is comet/asteroid
                if (CenterObjectSelected == 1 )   {
                   xyz = this.objectOrbit.getAt(0).Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
                   xyz = this.objectPos.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
                   point3 = getDrawPoint(xyz);

                   this.nX0 = this.sizeCanvas.width - point3.x;
                   this.nY0 = this.sizeCanvas.height - point3.y;

                   if (Math.abs(epochToEcl - atime.getJd()) > 365.2422 * 5) {
                        updateRotationMatrix(atime);
                   }
                }
                // If center object is one of the planets
                else if (CenterObjectSelected > 1 )   {
                   xyz = planetPos[CenterObjectSelected -2].Rotate(this.mtxRotate);

                   point3 = getDrawPoint(xyz);

                   this.nX0 = this.sizeCanvas.width - point3.x;
                   this.nY0 = this.sizeCanvas.height - point3.y;

                   if (Math.abs(epochToEcl - atime.getJd()) > 365.2422 * 5) {
                        updateRotationMatrix(atime);
                   }
                }

		// Get Off-Screen Image Graphics Context
		Graphics og = offscreen.getGraphics();

		// Draw Frame
		og.setColor(Color.black);
		og.fillRect(0, 0, sizeCanvas.width - 1, sizeCanvas.height - 1);

		// Draw Ecliptic Axis
		drawEclipticAxis(og);

		// Draw Sun
		og.setColor(colorSun);
		og.fillArc(this.nX0 - 2, this.nY0 - 2, 5, 5, 0, 360);

		// Draw Orbit of Object

                xyz = this.objectOrbit.getAt(0).Rotate(this.mtxToEcl)
								   .Rotate(this.mtxRotate);
	        Point point1, point2;
		point1 = getDrawPoint(xyz);
                if (OrbitDisplay[0] || OrbitDisplay[1]) {

		   for (int i = 1; i <= this.objectOrbit.getDivision(); i++) {
			xyz = this.objectOrbit.getAt(i).Rotate(this.mtxToEcl);
			if (xyz.fZ >= 0.0) {
				og.setColor(colorObjectOrbitUpper);
			} else {
				og.setColor(colorObjectOrbitLower);
			}
			xyz = xyz.Rotate(this.mtxRotate);
			point2 = getDrawPoint(xyz);
			og.drawLine(point1.x, point1.y, point2.x, point2.y);
			point1 = point2;
		   }
                }

		// Draw Object Body
		xyz = this.objectPos.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
		point1 = getDrawPoint(xyz);
		og.setColor(colorObject);
		og.fillArc(point1.x - 2, point1.y - 2, 5, 5, 0, 360);
		og.setFont(fontObjectName);
		if (bObjectName) {
			og.setColor(colorObjectName);
			og.drawString(object.getName(), point1.x + 5, point1.y);
		}

		//  Draw Orbit of Planets
		if (Math.abs(epochPlanetOrbit - atime.getJd()) > 365.2422 * 5) {
			updatePlanetOrbit(atime);
		}
		og.setFont(fontPlanetName);

		if (OrbitDisplay[0] || OrbitDisplay[10]) {
			drawPlanetOrbit(og, planetOrbit[Planet.PLUTO-Planet.MERCURY],
							colorPlanetOrbitUpper, colorPlanetOrbitLower);
		}
		drawPlanetBody(og, planetPos[8], "Pluto");

		if (OrbitDisplay[0] || OrbitDisplay[9]) {

			drawPlanetOrbit(og, planetOrbit[Planet.NEPTUNE-Planet.MERCURY],
							colorPlanetOrbitUpper, colorPlanetOrbitLower);
		}
		drawPlanetBody(og, planetPos[7], "Neptune");

		if (OrbitDisplay[0] || OrbitDisplay[8]) {
			drawPlanetOrbit(og, planetOrbit[Planet.URANUS-Planet.MERCURY],
							colorPlanetOrbitUpper, colorPlanetOrbitLower);
		}
		drawPlanetBody(og, planetPos[6], "Uranus");

		if (OrbitDisplay[0] || OrbitDisplay[7]) {
			drawPlanetOrbit(og, planetOrbit[Planet.SATURN-Planet.MERCURY],
							colorPlanetOrbitUpper, colorPlanetOrbitLower);
		}
		drawPlanetBody(og, planetPos[5], "Saturn");

		if (OrbitDisplay[0] || OrbitDisplay[6]) {
			drawPlanetOrbit(og, planetOrbit[Planet.JUPITER-Planet.MERCURY],
							colorPlanetOrbitUpper, colorPlanetOrbitLower);
		}
		drawPlanetBody(og, planetPos[4], "Jupiter");

		if (fZoom * 1.524 >= 7.5) {
			if (OrbitDisplay[0] || OrbitDisplay[5]) {

				drawPlanetOrbit(og, planetOrbit[Planet.MARS-Planet.MERCURY],
								colorPlanetOrbitUpper, colorPlanetOrbitLower);
			}
			drawPlanetBody(og, planetPos[3], "Mars");
		}
		if (fZoom * 1.000 >= 7.5) {
                        if (OrbitDisplay[0] || OrbitDisplay[4]) {

			   drawEarthOrbit(og, planetOrbit[Planet.EARTH-Planet.MERCURY],
						colorPlanetOrbitUpper, colorPlanetOrbitUpper);
                        }
			drawPlanetBody(og, planetPos[2], "Earth");

		}
		if (fZoom * 0.723 >= 7.5) {
                        if (OrbitDisplay[0] || OrbitDisplay[3]) {
			   drawPlanetOrbit(og, planetOrbit[Planet.VENUS-Planet.MERCURY],
						colorPlanetOrbitUpper, colorPlanetOrbitLower);
                        }
			drawPlanetBody(og, planetPos[1], "Venus");
		}
		if (fZoom * 0.387 >= 7.5) {
                        if (OrbitDisplay[0] || OrbitDisplay[2]) {
			   drawPlanetOrbit(og, planetOrbit[Planet.MERCURY-Planet.MERCURY],
						colorPlanetOrbitUpper, colorPlanetOrbitLower);
                        }
			drawPlanetBody(og, planetPos[0], "Mercury");
		}

		// Information
		og.setFont(fontInformation);
		og.setColor(colorInformation);
		FontMetrics fm = og.getFontMetrics();

		// Object Name String
		point1.x = fm.charWidth('A');
//		point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight() / 3;
		point1.y = 2 * fm.charWidth('A');
		og.drawString(object.getName(), point1.x, point1.y);

		if (bDistanceLabel) {
			// Earth & Sun Distance
			double edistance, sdistance;
			double xdiff, ydiff, zdiff;
//			BigDecimal a,v;
			String strDist;
			xyz  = this.objectPos.Rotate(this.mtxToEcl).Rotate(this.mtxRotate);
			xyz1 = planetPos[2].Rotate(this.mtxRotate);
			sdistance = Math.sqrt((xyz.fX * xyz.fX) + (xyz.fY * xyz.fY) +
								  (xyz.fZ * xyz.fZ)) + .0005;
			sdistance = (int)(sdistance * 1000.0)/1000.0;
			xdiff = xyz.fX - xyz1.fX;
			ydiff = xyz.fY - xyz1.fY;
			zdiff = xyz.fZ - xyz1.fZ;
			edistance = Math.sqrt((xdiff * xdiff) + (ydiff * ydiff) +
								  (zdiff * zdiff)) + .0005;
			edistance = (int)(edistance * 1000.0)/1000.0;
//			a = new BigDecimal (edistance);
//			v = a.setScale (3, BigDecimal.ROUND_HALF_UP);
			strDist = "Earth Distance: " + edistance + " AU";
			point1.x = fm.charWidth('A');
//			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight() / 3;
			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight();
			og.drawString(strDist, point1.x, point1.y);

//			a = new BigDecimal (sdistance);
//			v = a.setScale (3, BigDecimal.ROUND_HALF_UP);
			strDist = "Sun Distance  : " + sdistance + " AU";
			point1.x = fm.charWidth('A');
			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight() / 3;
			og.drawString(strDist, point1.x, point1.y);
		}

		if (bDateLabel) {
			// Date String
			String strDate = ATime.getMonthAbbr(atime.getMonth())
				+ " " + atime.getDay() + ", " + atime.getYear();
			point1.x = this.sizeCanvas.width  - fm.stringWidth(strDate)
				- fm.charWidth('A');
			point1.y = this.sizeCanvas.height - fm.getDescent() - fm.getHeight() / 3;
//			point1.y = 2 * fm.charWidth('A');
			og.drawString(strDate, point1.x, point1.y);
		}

		// Border
		og.clearRect(0,                    sizeCanvas.height - 1,
					 sizeCanvas.width,     sizeCanvas.height     );
		og.clearRect(sizeCanvas.width - 1, 0,
					 sizeCanvas.width,     sizeCanvas.height     );

		g.drawImage(offscreen, 0, 0, null);
	}

	/**
	 * paint if invalidate the canvas
	 */
	public void paint(Graphics g) {
		if (offscreen == null) {
			this.sizeCanvas = size();
			offscreen = createImage(this.sizeCanvas.width,
									this.sizeCanvas.height);
			update(g);
		} else {
			g.drawImage(offscreen, 0, 0, null);
		}
	}
}

/**
 *  Date Setting Dialog
 */
class DateDialog extends Frame {

	protected TextField		tfYear;
	protected TextField		tfDate;
	protected Choice		choiceMonth;

	protected Button		buttonOk;
	protected Button		buttonCancel;
	protected Button		buttonToday;

	protected OrbitViewer	objectOrbit;

	public DateDialog(OrbitViewer objectOrbit, ATime atime) {
		this.objectOrbit = objectOrbit;

		// Layout
		setLayout(new GridLayout(2, 3, 4, 4));
		setFont(new Font("Dialog", Font.PLAIN, 14));

		// Controls
		choiceMonth = new Choice();
		for (int i = 0; i < 12; i++) {
			choiceMonth.addItem(ATime.getMonthAbbr(i + 1));
		}
		choiceMonth.select(atime.getMonth() - 1);
		add(choiceMonth);

		Integer iDate = new Integer(atime.getDay());
		tfDate = new TextField(iDate.toString(), 2);
		add(tfDate);

		Integer iYear = new Integer(atime.getYear());
		tfYear = new TextField(iYear.toString(), 4);
		add(tfYear);

		buttonToday = new Button("Today");
		add(buttonToday);
		buttonOk = new Button("OK");
		add(buttonOk);
		buttonCancel = new Button("Cancel");
		add(buttonCancel);

		pack();

		setTitle("Date");
		setResizable(false);
		show();
	}

	/**
	 * Event Handler
	 */
    public boolean handleEvent(Event evt) {
		if (evt.id == Event.ACTION_EVENT) {
			ATime atime = null;
			if (evt.target == buttonOk) {
				int nYear = Integer.valueOf(tfYear.getText()).intValue();
				int nMonth = choiceMonth.getSelectedIndex() + 1;
				int nDate  = Integer.valueOf(tfDate.getText()).intValue();
				if (1600 <= nYear && nYear <= 2199 &&
							1 <= nMonth && nMonth <= 12 &&
							1 <= nDate  && nDate  <= 31) {
					atime = new ATime(nYear, nMonth, (double)nDate, 0.0);
				}
			} else if (evt.target == buttonToday) {
				Date date = new Date();
				choiceMonth.select(date.getMonth());
				tfDate.setText(Integer.toString(date.getDate()));
				tfYear.setText(Integer.toString(date.getYear() + 1900));
				return false;
			} else if (evt.target != buttonCancel) {
				return false;
			}
			dispose();
			objectOrbit.endDateDialog(atime);
			return true;
		}
		return false;	// super.handleEvent(evt);
	}
}

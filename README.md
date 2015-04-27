OrbitViewer Version 1.3
============================

Copyright &copy; 1996-2001 Osamu Ajiki <osam-a@astroarts.co.jp>
AstroArts Inc.  <http://www.astroarts.com/>

Copyright &copy; 2000-2001 Ron Baalke <baalke@zagami.jpl.nasa.gov>
NASA/JPL  <http://neo.jpl.nasa.gov/>

All Rights Reserved.


## ABSTRACT

OrbitViewer is an interactive applet that displays the orbit of small
bodies (comets or asteroids) in the solar system in 3D.  The orbit may
be played forwards or backwards like a movie.

History: This applet was created by Osamu Ajiki (AstroArts Inc.) in 1996. It was
further modified by Ron Baalke (NASA/JPL) in 2000-2001.


## LICENCE

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.


## COMPILE

Extract the package with archiver utility which can treat tar ball.
If you use UNIX environment, you can do it by the following command:

    gzip -dc OrbitViewer-1.3.tar.gz | tar xf -

Now, you have directory named OrbitViewer-1.3, and the directory
contains all distributed files.

This package contains executable byte code archive "OrbitViewer.jar",
so you don't have to comile it.  You need JAVA compiler to compile it
yourself.  If you have JDK by Sun Microsystems, you can compile it by
the following command:

    javac OrbitViewer.java

If you have 'make' utility, you can compile it and make jar archive
file by the following command:

    make

To minimize requirement of runtime environment, and to make it
runnable on older version of JAVA compatible WEB browsers, this applet
only uses JAVA 1.0 API.  The JAVA compiler complains 'deprecated', but
I just ignore it.


## RUN

You can use JAVA compatible WEB browser or appletviewer to run this.
If you use WEB browser, open the html file with 'Open File' command of
your browser.  If you use appletviewer, type as follows:

    appletviewer halley.html

This package contains two sample HTML files.

    halley.html (Orbit of comet Halley)
    ceres.html  (Orbit of asteroid Ceres)


## PARAMETERS

To view orbit of small bodies, you need to know the orbital elements
of the object.  You can find it on the WEB. (see LINKS section)

### COMETS

In general, orbital elements of comets are represented by six
parameters: time of perihelion passage (T), eccentricity (e),
perihelion distance (q), argument of perihelion (omega), ascending
node (Omega) and inclination (i).  Argument of perihelion, ascending
node and inclination have its equinox.

You can display orbit of comet by passing the following parameters to
OrbitViewer.

    Name:   Name of the object
    T:      Time of perihelion passage (T)
    e:      Eccentricity (e)
    q:      Perihelion distance (q; AU)
    Peri:   Argument of perihelion (omega; deg.)
    Node:   Ascending node (Omega; deg.)
    Incl:   Inclination (i; deg.)
    Eqnx:   Equinox (year)

Example (Comet Halley)

    <APPLET CODE="OrbitViewer.class"
     ARCHIVE="OrbitViewer.jar"  WIDTH=510 HEIGHT=460>
      <PARAM NAME="Name"  VALUE="1P/Halley">
      <PARAM NAME="T"     VALUE="19860209.7695">
      <PARAM NAME="e"     VALUE="0.967267">
      <PARAM NAME="q"     VALUE="0.587096">
      <PARAM NAME="Peri"  VALUE="111.8466">
      <PARAM NAME="Node"  VALUE=" 58.1440">
      <PARAM NAME="Incl"  VALUE="162.2393">
      <PARAM NAME="Eqnx"  VALUE="1950.0">
    </APPLET>

### ASTEROIDS

In general, orbital elements of asteroiods are represented by seven
parameters: epoch, mean anomaly (M), eccentricity (e), semi-major axis
(a), argument of perihelion (omega), ascending node (Omega) and
inclination (i).  Argument of perihelion, ascending node and
inclination have its equinox.

You can display orbit of asteroid by passing the following parameters
to OrbitViewer.

    Name:   Name of the object
    Epoch:  Epoch of the mean anomaly
    M:      Mean anomaly (M; deg.)
    a:      Semi-major axis (a; AU)
    Peri:   Argument of perihelion (omega; deg.)
    Node:   Ascending node (Omega; deg.)
    Incl:   Inclination (i; deg.)
    Eqnx:   Equinox (year)

Example (Asteroid Ceres)

    <APPLET CODE="OrbitViewer.class"
     ARCHIVE="OrbitViewer.jar"  WIDTH=510 HEIGHT=460>
      <PARAM NAME="Name"  VALUE="Ceres(1)">
      <PARAM NAME="Epoch" VALUE="19991118.5">
      <PARAM NAME="M"     VALUE="356.648434">
      <PARAM NAME="e"     VALUE="0.07831587">
      <PARAM NAME="a"     VALUE="2.76631592">
      <PARAM NAME="Peri"  VALUE=" 73.917708">
      <PARAM NAME="Node"  VALUE=" 80.495123">
      <PARAM NAME="Incl"  VALUE=" 10.583393">
      <PARAM NAME="Eqnx"  VALUE="2000.0">
    </APPLET>


## DOWNLOAD

Latest version of this applet is located at following URL:
http://www.astroarts.com/products/OrbitViewer/index.html


## LINKS

Near Earth Object Program - Orbits (NASA/JPL)
http://neo.jpl.nasa.gov/orbits/

Asteroid Orbit Viewer (AstroArts Inc.)
http://www.astroarts.com/simulation/asteroid-orbit.php

Cometary Orbit Viewer (AstroArts Inc.)
http://www.astroarts.com/simulation/cometary-orbit.php

Asteroid Orbital Elements (Lowell Observatory)
ftp://ftp.lowell.edu/pub/elgb/astorb.html

Elements & Ephemerides: Observable Comets (IAU)
http://cfa-www.harvard.edu/iau/Ephemerides/Comets/index.html

# Cell3DMeasurements
A Fiji plugin set to measure cell features in a multi-channel 3D image.
It uses three plugins insequence to identify, segment, and measure nuclei and complete cells in fluorescent microscopy image stacks:
1. The **Create Marker Images** plugin uses a 3D Laplacian of Gaussian filter (the [LoG3D](http://bigwww.epfl.ch/sage/soft/LoG3D/) plugin by Daniel Sage) to identify the centres of nuclei. This results in a list of coordinates of the centres and a 'marker image' that is used as the base for the next plugin. Note that the LoG3D plugin must be installed to use the **Create Marker Images** plugin.
2. The **Marker Controlled Watershed** plugin takes the *marker images* produced by the previous plugin and uses the marker dots as seeds for a watershed algorithm. This deviates from a normal watershed in that the segments that are formed are strictly limited to the seeds and all seeds will produce a segment. Note that the plugin can also be used to segment the actin channel as well based on the same nuclei marker points. This plugin uses the [MorphoLibJ](https://imagej.net/MorphoLibJ) set of plugins which should be installed for this plugin to work.
3. The **Feature Extraction** plugin uses the segments produced by the previous plugin to measure all types of features on the nuclei and  the cell (with or without the nucleus). This constitutes values dependent on the size and shape of the segments, as well as the intensity of any of the channels of the image. Furthermore, the plugin will do a *migration analysis* on request (given an actin channel) which will determine the mode of cell migration (single vs collective) of any cell. There is also an approximation of the distance migrated per cell.


A step by step protocol for using these plugins is described in the [manual](https://github.com/Mverp/CellMigrationAnalysisManual).


This project uses the following dependencies:
- My [Utils](https://github.com/Mverp/Utils) base package for these types of plugins
- The [ImageJ](https://imagej.net) IJ package
- The [3D ImageJ Suite](http://imagejdocu.tudor.lu/doku.php?id=plugin:stacks:3d_ij_suite:start) plugin for 3D measurements
- The [MorphoLibJ](https://imagej.net/MorphoLibJ) plugin for 3D measurements

Please note that the current pom uses a local system path to various of the dependencies as they do not have a proper maven repository yet. Please update these paths to your own system if you wish to use this pom file. 

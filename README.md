# NBody-GPGPU-Simulation
Distributed Systems seminar work, UT 2016. A system for computing n-body gravity interaction. Includes nice visualization.

The repository contents are an Ecplipse project.

Run the MainWindow class to execute the visualization.

Requires Aparapi system library from following link:
https://github.com/aparapi/aparapi
Add the appropriate "dll" file for windows or "so" for linux to system libray folder.

Visualization controlls:

Mouse wheel for zoom

Left click to create body, drag to creat a body with initialized speed (longer drag - higher velocity).

Space to visualize Barnes-Hut tree.

Right click on a body to set it as the camera's frame of reference.

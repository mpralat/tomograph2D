# tomograph2D
A simple 2D tomograph application.

## Creating a sinogram file
We use the [Radon Transform](https://en.wikipedia.org/wiki/Radon_transform) to create the Sinogram of an input image. 

Each projection creates one row of the Sinogram. Each iteration consists of several projection. A single projection is computing the sum of all pixels on the line segment defined by an emitter (the red dot) and one of the detectors (green dots). 


<a href="url"><img src="https://github.com/mpralat/tomograph2D/blob/master/output/model.png" align="center" height="300" width="300" ></a>

To make the image sharper and less blurry, we've added a filtered backprojection to the Sinogram image.
<tr>
<td>
<a href="url"><img src="https://github.com/mpralat/tomograph2D/blob/master/src/test_image.png" align="center" height="250" width="250" ></a>
</td>
<td>
<a href="url"><img src="https://github.com/mpralat/tomograph2D/blob/master/output/GrayScale.jpg" align="center" height="250" width="250" ></a>
</td>
<td>
<a href="url"><img src="https://github.com/mpralat/tomograph2D/blob/master/output/GrayScaleWithFilterd.jpg" align="center" height="250" width="250" ></a>
</td>
</tr>

## Reconstructing the image
To reconstruct the image, the Inverse Radon Transform was applied to the filtered Sinogram image.
<a href="url"><img src="https://github.com/mpralat/tomograph2D/blob/master/output/output.jpg" align="center" height="250" width="250" ></a>

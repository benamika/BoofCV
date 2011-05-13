/*
 * Copyright 2011 Peter Abeles
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package gecv.alg.detect.corner;

import gecv.abst.detect.corner.*;
import gecv.abst.detect.extract.CornerExtractor;
import gecv.abst.detect.extract.WrapperNonMax;
import gecv.alg.detect.extract.FastNonMaxCornerExtractor;
import gecv.alg.drawing.impl.ImageInitialization_I8;
import gecv.alg.filter.derivative.GradientSobel;
import gecv.alg.filter.derivative.GradientThree;
import gecv.alg.filter.derivative.HessianFromGradient;
import gecv.core.image.ConvertBufferedImage;
import gecv.gui.image.ShowImages;
import gecv.struct.QueueCorner;
import gecv.struct.image.ImageFloat32;
import gecv.struct.image.ImageSInt16;
import gecv.struct.image.ImageUInt8;
import pja.geometry.struct.point.Point2D_F64;
import pja.geometry.struct.point.Point2D_I16;
import pja.geometry.struct.point.UtilPoint2D;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Peter Abeles
 */
public class BenchmarkCornerAccuracy {

	int width = 250;
	int height = 300;
	int radius = 2;

	double distTol = 8;

	Random rand = new Random(234);

	ImageUInt8 image= new ImageUInt8(width,height);
	ImageSInt16 derivX = new ImageSInt16(width,height);
	ImageSInt16 derivY = new ImageSInt16(width,height);
	ImageSInt16 derivXX = new ImageSInt16(width,height);
	ImageSInt16 derivYY = new ImageSInt16(width,height);
	ImageSInt16 derivXY = new ImageSInt16(width,height);

	List<Point2D_F64> corners = new ArrayList<Point2D_F64>();

	ImageFloat32 imageIntensity;

	public QueueCorner detectMedianCorners( int imgWidth , int imgHeight , int medianRadius  ) {
		return detectCorners(WrapperMedianCornerIntensity.<ImageUInt8, ImageSInt16>create(ImageUInt8.class,imgWidth,imgHeight,medianRadius));
	}

	public QueueCorner detectCorners( FastCornerIntensity<ImageUInt8> intensity  ) {
		return detectCorners(new WrapperFastCornerIntensity<ImageUInt8, ImageSInt16>(intensity));
	}

	public QueueCorner detectCorners( GradientCornerIntensity<ImageSInt16> intensity  ) {
		return detectCorners(new WrapperGradientCornerIntensity<ImageUInt8, ImageSInt16>(intensity));
	}

	public QueueCorner detectCorners( KitRosCornerIntensity<ImageSInt16> intensity  ) {
		return detectCorners(new WrapperKitRosCornerIntensity<ImageUInt8, ImageSInt16>(intensity));
	}

	public QueueCorner detectCorners( GeneralCornerIntensity<ImageUInt8, ImageSInt16> intensity  ) {
		CornerExtractor extractor = new WrapperNonMax(new FastNonMaxCornerExtractor(radius + 10, radius + 10, 1f));
		GeneralCornerDetector<ImageUInt8, ImageSInt16> det =
				new GeneralCornerDetector<ImageUInt8, ImageSInt16>(intensity, extractor, corners.size()*2);

		if( det.getRequiresGradient() ) {
			GradientThree.process(image,derivX,derivY, true);
		}
		if( det.getRequiresHessian() ) {
			HessianFromGradient.hessianThree(derivX,derivY,derivXX,derivYY,derivXY,true);
//			HessianThree.process(image,derivXX,derivYY,derivXY,true);
		}

		det.process(image,derivX,derivY,derivXX,derivYY,derivXY);
		imageIntensity = det.getIntensity();

		return det.getCorners();
	}

	public void evaluateAll() {

		createTestImage();

		ShowImages.showWindow(image,"Evaluation Image");
		ShowImages.showWindow(derivX,"DerivX");
		ShowImages.showWindow(derivY,"DerivY");

		// todo try different noise levels

		evaluate(detectCorners(FactoryCornerIntensity.createFast12_I8(width, height, 10 , 11)),"FAST");
		evaluate(detectCorners(FactoryCornerIntensity.createHarris_I16(width, height, radius, 0.04f)),"Harris");
		evaluate(detectCorners(FactoryCornerIntensity.createKitRos_I16(width, height)),"KitRos");
		evaluate(detectCorners(FactoryCornerIntensity.createKlt_I16(width, height, radius )),"KLT");
		evaluate(detectMedianCorners(width, height, radius ),"Median");
	}

	private void createTestImage() {
		BufferedImage workImg = new BufferedImage(width,height,BufferedImage.TYPE_INT_BGR);
		Graphics2D g2 = workImg.createGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0,0,width,height);
		g2.setColor(Color.BLACK);
		addRectangle(g2,new AffineTransform(),40,50,60,50);

		AffineTransform tran = new AffineTransform();
		tran.setToRotation(0.5);
		addRectangle(g2,tran,120,140,60,50);

		tran.setToRotation(-1.2);
		addRectangle(g2,tran,-120,200,60,40);

		ConvertBufferedImage.convertFrom(workImg,image);
		ImageInitialization_I8.addNoise(image,rand,-2,2);
		GradientSobel.process(image,derivX,derivY, false);
	}

	private void addRectangle( Graphics2D g2 , AffineTransform tran , int x0 , int y0 , int w , int h )
	{
		g2.setTransform(tran);
		g2.fillRect(x0,y0,w,h);

		// -1 is added for w and h because it is drawn before that point
		corners.add( new Point2D_F64(x0,y0));
		corners.add( new Point2D_F64(x0+w-1,y0));
		corners.add( new Point2D_F64(x0+w-1,y0+h-1));
		corners.add( new Point2D_F64(x0,y0+h-1));
		for( int i = corners.size()-4; i < corners.size(); i++ ) {
			Point2D_F64 c = corners.get(i);
			Point2D src = new Point2D.Double(c.x,c.y);
			Point2D dst = new Point2D.Double();
			tran.transform(src,dst);
			c.x = dst.getX();
			c.y = dst.getY();
		}
	}

	public void evaluate( QueueCorner foundCorners , String name ) {

		
		ShowImages.showWindow(imageIntensity,"Intensity of "+name,true);

		int numMatched = 0;
		double error = 0;

		for( Point2D_F64 c : corners ) {
			double bestDistance = -1;
			Point2D_I16 bestPoint = null;

			for( int i = 0; i < foundCorners.size(); i++ ) {
				Point2D_I16 p = foundCorners.get(i);

				double dist = UtilPoint2D.distance(c.x,c.y,p.x,p.y);

				if( bestPoint == null || dist < bestDistance ) {
					bestDistance = dist;
					bestPoint = p;
				}
			}

			if( bestDistance <= distTol) {
				error += bestDistance;
				numMatched++;
			}
		}
		if( numMatched > 0 ) {
			error /= numMatched;

			System.out.println(name+" num matched corners: "+numMatched+"  average error "+error);
		} else {
			System.out.println(name+" no corner matches");
		}
	}

	public static void main( String args[] ) {
		BenchmarkCornerAccuracy benchmark = new BenchmarkCornerAccuracy();

		benchmark.evaluateAll();
	}
}

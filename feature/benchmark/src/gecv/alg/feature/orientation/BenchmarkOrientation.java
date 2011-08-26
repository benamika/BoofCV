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

package gecv.alg.feature.orientation;

import gecv.Performer;
import gecv.ProfileOperation;
import gecv.abst.filter.derivative.ImageGradient;
import gecv.alg.transform.ii.GIntegralImageOps;
import gecv.core.image.GeneralizedImageOps;
import gecv.factory.feature.orientation.FactoryOrientationAlgs;
import gecv.factory.filter.derivative.FactoryDerivative;
import gecv.struct.image.ImageBase;
import gecv.struct.image.ImageFloat32;
import gecv.struct.image.ImageSInt32;
import jgrl.struct.point.Point2D_I32;

import java.util.Random;


/**
 * @author Peter Abeles
 */
public class BenchmarkOrientation<I extends ImageBase, D extends ImageBase> {

	static final long TEST_TIME = 1000;
	static Random rand = new Random(234234);
	static int NUM_POINTS = 1000;
	static int RADIUS = 6;

	final static int width = 640;
	final static int height = 480;

	I image;
	D derivX;
	D derivY;
	ImageBase ii;

	Point2D_I32 pts[];

	Class<I> imageType;
	Class<D> derivType;

	public BenchmarkOrientation( Class<I> imageType , Class<D> derivType ) {

		this.imageType = imageType;
		this.derivType = derivType;

		Class integralType = ImageFloat32.class == imageType ? ImageFloat32.class : ImageSInt32.class;

		image = GeneralizedImageOps.createImage(imageType,width,height);
		ii = GeneralizedImageOps.createImage(integralType,width,height);
		derivX = GeneralizedImageOps.createImage(derivType,width,height);
		derivY = GeneralizedImageOps.createImage(derivType,width,height);

		GeneralizedImageOps.randomize(image,rand,0,100);
		GIntegralImageOps.transform(image,ii);

		ImageGradient<I,D> gradient = FactoryDerivative.sobel(imageType,derivType);
		gradient.process(image,derivX,derivY);

		pts = new Point2D_I32[NUM_POINTS];
		int border = 50;
		for( int i = 0; i < NUM_POINTS; i++ ) {
			int x = rand.nextInt(width-border)+border;
			int y = rand.nextInt(height-border)+border;
			pts[i] = new Point2D_I32(x,y);
		}

	}

	public class Gradient implements Performer {

		OrientationGradient<D> alg;
		String name;

		public Gradient(String name, OrientationGradient<D> alg) {
			this.alg = alg;
			this.name = name;
		}

		@Override
		public void process() {
			alg.setImage(derivX,derivY);
			for( Point2D_I32 p : pts ) {
				alg.compute(p.x,p.y);
			}
		}

		@Override
		public String getName() {
			return name;
		}
	}

	public class Integral implements Performer {

		OrientationIntegral alg;
		String name;

		public Integral(String name, OrientationIntegral alg) {
			this.alg = alg;
			this.name = name;
		}

		@Override
		public void process() {
			alg.setImage(ii);
			for( Point2D_I32 p : pts ) {
				alg.compute(p.x,p.y);
			}
		}

		@Override
		public String getName() {
			return name;
		}
	}

	public void perform() {
		System.out.println("=========  Profile Image Size " + width + " x " + height + " ========== "+imageType.getSimpleName());
		System.out.println();

//		ProfileOperation.printOpsPerSec(new Gradient("Average", average(RADIUS,false,derivType)), TEST_TIME);
//		ProfileOperation.printOpsPerSec(new Gradient("Average W", average(RADIUS,true,derivType)), TEST_TIME);
//		ProfileOperation.printOpsPerSec(new Gradient("Histogram", histogram(15,RADIUS,false,derivType)), TEST_TIME);
//		ProfileOperation.printOpsPerSec(new Gradient("Histogram W", histogram(15,RADIUS,true,derivType)), TEST_TIME);
//		ProfileOperation.printOpsPerSec(new Gradient("Sliding", sliding(15,Math.PI/3.0,RADIUS,false,derivType)), TEST_TIME);
//		ProfileOperation.printOpsPerSec(new Gradient("Sliding W", sliding(15,Math.PI/3.0,RADIUS,true,derivType)), TEST_TIME);
		ProfileOperation.printOpsPerSec(new Integral("Average II", FactoryOrientationAlgs.<I>average_ii(RADIUS,false)), TEST_TIME);
		ProfileOperation.printOpsPerSec(new Integral("Average II W", FactoryOrientationAlgs.<I>average_ii(RADIUS,true)), TEST_TIME);
	}

	public static void main( String argsp[ ] ) {
		BenchmarkOrientation<ImageFloat32,ImageFloat32> alg = new BenchmarkOrientation<ImageFloat32,ImageFloat32>(ImageFloat32.class,ImageFloat32.class);
//		BenchmarkOrientation<ImageUInt8,ImageSInt16> alg = new BenchmarkOrientation<ImageUInt8,ImageSInt16>(ImageUInt8.class, ImageSInt16.class);

		alg.perform();
	}
}

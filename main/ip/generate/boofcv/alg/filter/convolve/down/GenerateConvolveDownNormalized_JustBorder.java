/*
 * Copyright (c) 2011-2013, Peter Abeles. All Rights Reserved.
 *
 * This file is part of BoofCV (http://boofcv.org).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package boofcv.alg.filter.convolve.down;

import boofcv.misc.CodeGeneratorUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Creates code the will down convolve and image along the image borders while re-normalizing the kernel's weight.
 *
 * @author Peter Abeles
 */
public class GenerateConvolveDownNormalized_JustBorder {
		String className = "ConvolveDownNormalized_JustBorder";

	PrintStream out;

	public GenerateConvolveDownNormalized_JustBorder() throws FileNotFoundException {
		out = new PrintStream(new FileOutputStream(className + ".java"));
	}

	public void generate() {
		printPreamble();
		printAllOps("F32", "ImageFloat32","ImageFloat32","float","float","float","float","");
		printAllOps("I32", "ImageUInt8","ImageInt8","int","byte","byte","int"," & 0xFF");
		printAllOps("I32", "ImageSInt16","ImageInt16","int","short","short","int","");
		out.println("}");
	}

	private void printPreamble() {
		out.print(CodeGeneratorUtil.copyright);
		out.print("package boofcv.alg.filter.convolve.down;\n" +
				"\n" +
				"import boofcv.struct.convolve.Kernel2D_F32;\n" +
				"import boofcv.struct.convolve.Kernel2D_I32;\n" +
				"import boofcv.struct.convolve.Kernel1D_F32;\n" +
				"import boofcv.struct.convolve.Kernel1D_I32;\n" +
				"import boofcv.struct.image.*;\n"+
				"\n" +
				"/**\n" +
				" * <p>\n" +
				" * Covolves a 1D kernel in the horizontal or vertical direction while skipping pixels across an image's border.  The\n" +
				" * kernel is re-normalizing the depending upon the amount of overlap it has with the image.  These functions will\n" +
				" * NOT work on kernels which are large than the image.\n" +
				" * </p>\n" +
				" *\n" +
				" * <p>\n" +
				" * NOTE: Do not modify.  Automatically generated by {@link Generate"+className+"}.\n" +
				" * </p>\n" +
				" *\n" +
				" * @author Peter Abeles\n" +
				" */\n" +
				"@SuppressWarnings({\"ForLoopReplaceableByForEach\"})\n" +
				"public class "+className+" {\n\n");
	}

	private void printAllOps( String kernelType , String inputType , String outputType ,
								  String kernelData, String inputData, String outputData ,
								  String sumType, String  bitWiseOp )
	{
		printHorizontal(kernelType,inputType,outputType,kernelData,inputData,outputData,sumType,bitWiseOp);
		printVertical(kernelType,inputType,outputType,kernelData,inputData,outputData,sumType,bitWiseOp);
		printConvolve(kernelType,inputType,outputType,kernelData,inputData,outputData,sumType,bitWiseOp);
	}

	private void printHorizontal( String kernelType , String inputType , String outputType ,
								  String kernelData, String inputData, String outputData ,
								  String sumType, String  bitWiseOp) {

		String typeCast = outputData.compareTo(sumType) == 0 ? "" : "("+outputData+")";

		out.print("\tpublic static void horizontal(Kernel1D_"+kernelType+" kernel, "+inputType+" input, "+outputType+" output , int skip ) {\n" +
				"\t\tfinal "+inputData+"[] dataSrc = input.data;\n" +
				"\t\tfinal "+outputData+"[] dataDst = output.data;\n" +
				"\t\tfinal "+kernelData+"[] dataKer = kernel.data;\n" +
				"\n" +
				"\t\tfinal int radius = kernel.getRadius();\n" +
				"\t\tfinal int offset = UtilDownConvolve.computeOffset(skip,radius);\n" +
				"\t\tfinal int offsetEnd = UtilDownConvolve.computeMaxSide(input.width,skip,radius)+skip;\n"+
				"\n" +
				"\t\tfinal int width = input.width - input.width % skip;\n" +
				"\t\tfinal int height = input.getHeight();\n" +
				"\n" +
				"\t\tfor (int y = 0; y < height; y++) {\n" +
				"\n" +
				"\t\t\tint indexDest = output.startIndex + y*output.stride;\n" +
				"\n" +
				"\t\t\tfor( int x = 0; x < offset; x += skip ) {\n" +
				"\t\t\t\tint indexSrc = input.startIndex + y*input.stride+x;\n" +
				"\t\t\t\t"+sumType+" total = 0;\n" +
				"\t\t\t\t"+kernelData+" totalWeight = 0;\n" +
				"\n" +
				"\t\t\t\tfor( int k = -x; k <= radius; k++ ) {\n" +
				"\t\t\t\t\t"+kernelData+" w = dataKer[k+radius];\n" +
				"\t\t\t\t\ttotalWeight += w;\n" +
				"\t\t\t\t\ttotal += (dataSrc[indexSrc+k]"+bitWiseOp+") * w;\n" +
				"\t\t\t\t}\n" +
				"\t\t\t\tdataDst[indexDest++] = "+typeCast+"(total / totalWeight);\n" +
				"\t\t\t}\n" +
				"\n" +
				"\t\t\tindexDest = output.startIndex + y*output.stride + offsetEnd/skip;\n" +
				"\n" +
				"\t\t\tfor( int x = offsetEnd; x < width; x += skip ) {\n" +
				"\t\t\t\tint indexSrc = input.startIndex + y*input.stride+x;\n" +
				"\t\t\t\t"+sumType+" total = 0;\n" +
				"\t\t\t\t"+kernelData+" totalWeight = 0;\n" +
				"\n" +
				"\t\t\t\tint endKernel = input.width-x-1;\n" +
				"\t\t\t\tif( endKernel > radius ) endKernel = radius;\n" +
				"\n" +
				"\t\t\t\tfor( int k = -radius; k <= endKernel; k++ ) {\n" +
				"\t\t\t\t\t"+kernelData+" w = dataKer[k+radius];\n" +
				"\t\t\t\t\ttotalWeight += w;\n" +
				"\t\t\t\t\ttotal += (dataSrc[indexSrc+k]"+bitWiseOp+") * w;\n" +
				"\t\t\t\t}\n" +
				"\t\t\t\tdataDst[indexDest++] = "+typeCast+"(total / totalWeight);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	private void printVertical( String kernelType , String inputType , String outputType ,
								String kernelData, String inputData, String outputData ,
								String sumType, String  bitWiseOp) {

		String typeCast = outputData.compareTo(sumType) == 0 ? "" : "("+outputData+")";

		out.print("\tpublic static void vertical(Kernel1D_"+kernelType+" kernel, "+inputType+" input, "+outputType+" output , int skip ) {\n" +
				"\t\tfinal "+inputData+"[] dataSrc = input.data;\n" +
				"\t\tfinal "+outputData+"[] dataDst = output.data;\n" +
				"\t\tfinal "+kernelData+"[] dataKer = kernel.data;\n" +
				"\n" +
				"\t\tfinal int radius = kernel.getRadius();\n" +
				"\t\tfinal int offset = UtilDownConvolve.computeOffset(skip,radius);\n" +
				"\t\tfinal int offsetEnd = UtilDownConvolve.computeMaxSide(input.height,skip,radius)+skip;\n" +
				"\n" +
				"\t\tfinal int width = input.width;\n" +
				"\t\tfinal int height = input.height - input.height % skip;\n" +
				"\n" +
				"\t\tfor( int y = 0; y < offset; y += skip ) {\n" +
				"\t\t\tint indexDest = output.startIndex + (y/skip)*output.stride;\n" +
				"\n" +
				"\t\t\tfor( int x = 0; x < width; x++ ) {\n" +
				"\t\t\t\tint indexSrc = input.startIndex + y*input.stride+x;\n" +
				"\t\t\t\t"+sumType+" total = 0;\n" +
				"\t\t\t\t"+kernelData+" totalWeight = 0;\n" +
				"\n" +
				"\t\t\t\tfor( int k = -y; k <= radius; k++ ) {\n" +
				"\t\t\t\t\t"+kernelData+" w = dataKer[k+radius];\n" +
				"\t\t\t\t\ttotalWeight += w;\n" +
				"\t\t\t\t\ttotal += (dataSrc[indexSrc+k*input.stride]"+bitWiseOp+") * w;\n" +
				"\t\t\t\t}\n" +
				"\t\t\t\tdataDst[indexDest++] = "+typeCast+"(total / totalWeight);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\n" +
				"\t\tfor( int y = offsetEnd; y < height; y += skip ) {\n" +
				"\t\t\tint indexDest = output.startIndex + (y/skip)*output.stride;\n" +
				"\t\t\tint endKernel = input.height-y-1;\n" +
				"\t\t\tif( endKernel > radius ) endKernel = radius;\n" +
				"\n" +
				"\t\t\tfor( int x = 0; x < width; x++ ) {\n" +
				"\t\t\t\tint indexSrc = input.startIndex + y*input.stride+x;\n" +
				"\t\t\t\t"+sumType+" total = 0;\n" +
				"\t\t\t\t"+kernelData+" totalWeight = 0;\n" +
				"\n" +
				"\t\t\t\tfor( int k = -radius; k <= endKernel; k++ ) {\n" +
				"\t\t\t\t\t"+kernelData+" w = dataKer[k+radius];\n" +
				"\t\t\t\t\ttotalWeight += w;\n" +
				"\t\t\t\t\ttotal += (dataSrc[indexSrc+k*input.stride]"+bitWiseOp+") * w;\n" +
				"\t\t\t\t}\n" +
				"\t\t\t\tdataDst[indexDest++] = "+typeCast+"(total / totalWeight);\n" +
				"\t\t\t}\n" +
				"\t\t}\n"+
				"\t}\n\n");
	}

	public void printConvolve( String kernelType , String inputType , String outputType ,
							   String kernelData, String inputData, String outputData ,
							   String sumType, String  bitWiseOp) {

		String typeCast = outputData.compareTo(sumType) == 0 ? "" : "("+outputData+")";

		out.print("\tpublic static void convolve(Kernel2D_"+kernelType+" kernel, "+inputType+" input, "+outputType+" output , int skip ) {\n" +
				"\t\tfinal "+inputData+"[] dataSrc = input.data;\n" +
				"\t\tfinal "+outputData+"[] dataDst = output.data;\n" +
				"\t\tfinal "+kernelData+"[] dataKer = kernel.data;\n" +
				"\n" +
				"\t\tfinal int radius = kernel.getRadius();\n" +
				"\t\tfinal int kernelWidth = kernel.getWidth();\n" +
				"\n" +
				"\t\tfinal int width = input.width - input.width % skip;\n" +
				"\t\tfinal int height = input.height - input.height % skip;\n" +
				"\n" +
				"\t\tfinal int offset = UtilDownConvolve.computeOffset(skip,radius);\n" +
				"\t\tfinal int offsetEndX = UtilDownConvolve.computeMaxSide(input.width,skip,radius)+skip;\n" +
				"\t\tfinal int offsetEndY = UtilDownConvolve.computeMaxSide(input.height,skip,radius)+skip;\n" +
				"\n" +
				"\t\t// convolve across the left and right borders\n" +
				"\t\tfor (int y = 0; y < height; y += skip) {\n" +
				"\n" +
				"\t\t\tint minI = y >= radius ? -radius : -y;\n" +
				"\t\t\tint maxI = input.height-y-1;\n" +
				"\t\t\tif( maxI > radius ) maxI = radius;\n" +
				"\n" +
				"\t\t\tint indexDst = output.startIndex + (y/skip)* output.stride;\n" +
				"\n" +
				"\t\t\tfor( int x = 0; x < offset; x += skip ) {\n" +
				"\n" +
				"\t\t\t\t"+sumType+" total = 0;\n" +
				"\t\t\t\t"+kernelData+" totalWeight = 0;\n" +
				"\n" +
				"\t\t\t\tfor( int i = minI; i <= maxI; i++ ) {\n" +
				"\t\t\t\t\tint indexSrc = input.startIndex + (y+i)* input.stride+x;\n" +
				"\t\t\t\t\tint indexKer = (i+radius)*kernelWidth;\n" +
				"\n" +
				"\t\t\t\t\tfor( int j = -x; j <= radius; j++ ) {\n" +
				"\t\t\t\t\t\t"+kernelData+" w = dataKer[indexKer+j+radius];\n" +
				"\t\t\t\t\t\ttotalWeight += w;\n" +
				"\t\t\t\t\t\ttotal += (dataSrc[indexSrc+j]"+bitWiseOp+") * w;\n" +
				"\t\t\t\t\t}\n" +
				"\t\t\t\t}\n" +
				"\n" +
				"\t\t\t\tdataDst[indexDst++] = "+typeCast+"(total / totalWeight);\n" +
				"\t\t\t}\n" +
				"\n" +
				"\t\t\tindexDst = output.startIndex + (y/skip)* output.stride + offsetEndX/skip;\n" +
				"\t\t\tfor( int x = offsetEndX; x < width; x += skip ) {\n" +
				"\n" +
				"\t\t\t\tint maxJ = input.width-x-1;\n" +
				"\t\t\t\tif( maxJ > radius ) maxJ = radius;\n" +
				"\n" +
				"\t\t\t\t"+sumType+" total = 0;\n" +
				"\t\t\t\t"+kernelData+" totalWeight = 0;\n" +
				"\n" +
				"\t\t\t\tfor( int i = minI; i <= maxI; i++ ) {\n" +
				"\t\t\t\t\tint indexSrc = input.startIndex + (y+i)*input.stride + x;\n" +
				"\t\t\t\t\tint indexKer = (i+radius)*kernelWidth;\n" +
				"\n" +
				"\t\t\t\t\tfor( int j = -radius; j <= maxJ; j++ ) {\n" +
				"\t\t\t\t\t\t"+kernelData+" w = dataKer[indexKer+j+radius];\n" +
				"\t\t\t\t\t\ttotalWeight += w;\n" +
				"\t\t\t\t\t\ttotal += (dataSrc[indexSrc+j]"+bitWiseOp+")* w;\n" +
				"\t\t\t\t\t}\n" +
				"\t\t\t\t}\n" +
				"\n" +
				"\t\t\t\tdataDst[indexDst++] = "+typeCast+"(total / totalWeight);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\n" +
				"\t\t// convolve across the top border while avoiding convolving the corners again\n" +
				"\t\tfor (int y = 0; y < radius; y += skip) {\n" +
				"\n" +
				"\t\t\tint indexDst = output.startIndex + (y/skip)*output.stride + offset/skip;\n" +
				"\n" +
				"\t\t\tfor( int x = offset; x < offsetEndX; x += skip ) {\n" +
				"\n" +
				"\t\t\t\t"+sumType+" total = 0;\n" +
				"\t\t\t\t"+kernelData+" totalWeight = 0;\n" +
				"\n" +
				"\t\t\t\tfor( int i = -y; i <= radius; i++ ) {\n" +
				"\t\t\t\t\tint indexSrc = input.startIndex + (y+i)* input.stride+x;\n" +
				"\t\t\t\t\tint indexKer = (i+radius)*kernelWidth;\n" +
				"\n" +
				"\t\t\t\t\tfor( int j = -radius; j <= radius; j++ ) {\n" +
				"\t\t\t\t\t\t"+kernelData+" w = dataKer[indexKer+j+radius];\n" +
				"\t\t\t\t\t\ttotalWeight += w;\n" +
				"\t\t\t\t\t\ttotal += (dataSrc[indexSrc + j]"+bitWiseOp+")* w;\n" +
				"\t\t\t\t\t}\n" +
				"\t\t\t\t}\n" +
				"\n" +
				"\t\t\t\tdataDst[indexDst++] = "+typeCast+"(total / totalWeight);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\n" +
				"\t\t// convolve across the bottom border\n" +
				"\t\tfor (int y = offsetEndY; y < height; y += skip) {\n" +
				"\n" +
				"\t\t\tint maxI = input.height - y - 1;\n" +
				"\t\t\tif( maxI > radius ) maxI = radius;\n" +
				"\n" +
				"\t\t\tint indexDst = output.startIndex + (y/skip)*output.stride + offset/skip;\n" +
				"\n" +
				"\t\t\tfor( int x = offset; x < offsetEndX; x += skip ) {\n" +
				"\n" +
				"\t\t\t\t"+sumType+" total = 0;\n" +
				"\t\t\t\t"+kernelData+" totalWeight = 0;\n" +
				"\n" +
				"\t\t\t\tfor( int i = -radius; i <= maxI; i++ ) {\n" +
				"\t\t\t\t\tint indexSrc = input.startIndex + (y+i)* input.stride+x;\n" +
				"\t\t\t\t\tint indexKer = (i+radius)*kernelWidth;\n" +
				"\n" +
				"\t\t\t\t\tfor( int j = -radius; j <= radius; j++ ) {\n" +
				"\t\t\t\t\t\t"+kernelData+" w = dataKer[indexKer+j+radius];\n" +
				"\t\t\t\t\t\ttotalWeight += w;\n" +
				"\t\t\t\t\t\ttotal += (dataSrc[indexSrc + j]"+bitWiseOp+")* w;\n" +
				"\t\t\t\t\t}\n" +
				"\t\t\t\t}\n" +
				"\n" +
				"\t\t\t\tdataDst[indexDst++] = "+typeCast+"(total / totalWeight);\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t}\n\n");
	}

	public static void main(String args[]) throws FileNotFoundException {
		GenerateConvolveDownNormalized_JustBorder gen = new GenerateConvolveDownNormalized_JustBorder();
		gen.generate();
	}
}

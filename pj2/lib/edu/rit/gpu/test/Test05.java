//******************************************************************************
//
// File:    Test05.java
// Package: edu.rit.gpu.test
// Unit:    Class edu.rit.gpu.test.Test05
//
// This Java source file is copyright (C) 2014 by Alan Kaminsky. All rights
// reserved. For further information, contact the author, Alan Kaminsky, at
// ark@cs.rit.edu.
//
// This Java source file is part of the Parallel Java 2 Library ("PJ2"). PJ2 is
// free software; you can redistribute it and/or modify it under the terms of
// the GNU General Public License as published by the Free Software Foundation;
// either version 3 of the License, or (at your option) any later version.
//
// PJ2 is distributed in the hope that it will be useful, but WITHOUT ANY
// WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// A PARTICULAR PURPOSE. See the GNU General Public License for more details.
//
// A copy of the GNU General Public License is provided in the file gpl.txt. You
// may also obtain a copy of the GNU General Public License on the World Wide
// Web at http://www.gnu.org/licenses/gpl.html.
//
//******************************************************************************

package edu.rit.gpu.test;

import edu.rit.gpu.Kernel;
import edu.rit.gpu.Gpu;
import edu.rit.gpu.GpuIntArray;
import edu.rit.gpu.Module;
import edu.rit.pj2.Task;
import edu.rit.pj2.TerminateException;

/**
 * Class Test05 is a unit test main program that adds two integer vectors on the
 * GPU. It uses the same GPU kernel as class {@linkplain Test01}. However, the
 * result vector is rotated one position.
 * <P>
 * Usage: <TT>java pj2 edu.rit.gpu.test.Test01 <I>N</I></TT>
 * <BR><TT><I>N</I></TT> = Vector length
 *
 * @author  Alan Kaminsky
 * @version 30-May-2014
 */
public class Test05
	extends Task
	{

	/**
	 * GPU kernel interface.
	 */
	private static interface Test05Kernel
		extends Kernel
		{
		public void addIntVectors
			(GpuIntArray a,
			 GpuIntArray b,
			 GpuIntArray c,
			 int len);
		}

	/**
	 * Task main program.
	 */
	public void main
		(String[] args)
		throws Exception
		{
		// Verify command line arguments.
		if (args.length != 1) usage();
		int N = Integer.parseInt (args[0]);

		// Allocate vectors in GPU memory.
		Gpu gpu = Gpu.gpu();
		GpuIntArray a = gpu.getIntArray (N);
		GpuIntArray b = gpu.getIntArray (N);
		GpuIntArray c = gpu.getIntArray (N);

		// Initialize input vectors.
		for (int i = 0; i < N; ++ i)
			{
			a.item[i] = 2*(i + 1);
			b.item[i] = 3*(i + 1);
			}
		a.hostToDev();
		b.hostToDev();

		// Launch GPU kernel.
		Module module = gpu.getModule ("edu/rit/gpu/test/Test01.ptx");
		Test05Kernel kernel = module.getKernel (Test05Kernel.class);
		kernel.setBlockDim (256);
		kernel.setGridDim ((N + 255)/256);
		kernel.addIntVectors (a, b, c, N);

		// Print results.
		if (N == 1)
			c.devToHost();
		else
			{
			c.devToHost (0, 1, N-1);
			c.devToHost (N-1, 0, 1);
			}
		for (int i = 0; i < N; ++ i)
			System.out.printf ("a[%d]=%d\tb[%d]=%d\tc[%d]=%d%n",
				i, a.item[i], i, b.item[i], i, c.item[i]);
		}

	/**
	 * Print a usage message and exit.
	 */
	private static void usage()
		{
		System.err.println ("Usage: java pj2 edu.rit.gpu.test.Test05 <N>");
		System.err.println ("<N> = Vector length");
		terminate (1);
		}

	/**
	 * Specify that this task requires one core.
	 */
	protected static int coresRequired()
		{
		return 1;
		}

	/**
	 * Specify that this task requires one GPU accelerator.
	 */
	protected static int gpusRequired()
		{
		return 1;
		}

	}

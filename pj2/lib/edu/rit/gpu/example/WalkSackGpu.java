//******************************************************************************
//
// File:    WalkSackGpu.java
// Package: edu.rit.gpu.example
// Unit:    Class edu.rit.gpu.example.WalkSackGpu
//
// This Java source file is copyright (C) 2016 by Alan Kaminsky. All rights
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

package edu.rit.gpu.example;

import edu.rit.gpu.CacheConfig;
import edu.rit.gpu.Gpu;
import edu.rit.gpu.GpuIntMatrix;
import edu.rit.gpu.GpuIntVbl;
import edu.rit.gpu.GpuLongArray;
import edu.rit.gpu.GpuStructArray;
import edu.rit.gpu.Kernel;
import edu.rit.gpu.Module;
import edu.rit.pj2.Section;
import edu.rit.pj2.Task;
import edu.rit.pj2.Vbl;
import edu.rit.util.Instance;
import edu.rit.util.Sorting;
import java.util.Date;

/**
 * Class WalkSackGpu is a GPU parallel program that solves a knapsack problem
 * using a random walk.
 * <P>
 * Usage: <TT>java pj2 [gpus=<I>NA</I>] edu.rit.gpu.example.WalkSackGpu [-p]
 * "<I>constructor</I>" <I>seed</I> <I>reps</I> <I>steps</I> [<I>NB</I>
 * [<I>NT</I>]]</TT>
 * <BR><TT><I>NA</I></TT> = Number of GPU accelerators to use
 * <BR><TT>-p</TT> = Print knapsack problem parameters
 * <BR><TT><I>constructor</I></TT> = KnapsackProblem constructor expression
 * <BR><TT><I>seed</I></TT> = Random seed
 * <BR><TT><I>reps</I></TT> = Number of repetitions
 * <BR><TT><I>steps</I></TT> = Number of steps per repetition
 * <BR><TT><I>NB</I></TT> = Number of blocks
 * <BR><TT><I>NT</I></TT> = Number of threads per block
 * <P>
 * The <I>constructor</I> argument gives a constructor expression for a class
 * that implements interface {@linkplain KnapsackProblem KnapsackProblem}. The
 * program obtains the knapsack problem parameters&mdash;knapsack capacity,
 * number of items, weight and value of each item&mdash;from an instance of this
 * class. For further information about constructor expressions, see class
 * {@linkplain edu.rit.util.Instance edu.rit.util.Instance}.
 * <P>
 * The program performs <I>reps</I> repetitions. In each repetition, the program
 * executes a GPU kernel on each of <I>NA</I> GPU accelerators in parallel
 * (default: <I>NA</I> = all GPU accelerators on the node). The kernel consists
 * of a one-dimensional grid of one-dimensional blocks, with <I>NB</I> blocks
 * (default: <I>NB</I> = number of multiprocessors on the GPU) and <I>NT</I>
 * threads per block (default: <I>NT</I> = 256). Each GPU thread performs an
 * independent random walk through the space of subsets of items and keeps track
 * of the best solution found. After doing a certain number of <I>steps</I>, the
 * program prints the best of the per-thread solutions for the current
 * repetition. After doing all the repetitions, the program prints the
 * best-of-best solution.
 * <P>
 * The program prints the knapsack problem and its solution in the following
 * format. If the <TT>-p</TT> flag is omitted, the <TT>p</TT> and <TT>i</TT>
 * lines are omitted.
 * <UL>
 * <LI>
 * Lines beginning with <TT>#</TT> are comments.
 * <LI>
 * <TT>p zeroOne &lt;C&gt; &lt;N&gt;</TT> &mdash; Defines a zero-one
 * knapsack problem with knapsack capacity <I>C</I> and <I>N</I> items.
 * <LI>
 * <TT>i &lt;j&gt; &lt;W_j&gt; &lt;V_j&gt;</TT> &mdash; Defines the item at
 * index <I>j</I> with weight <I>W</I><SUB><I>j</I></SUB> and value
 * <I>V</I><SUB><I>j</I></SUB>.
 * <LI>
 * <TT>s &lt;W_total&gt; &lt;V_total&gt;</TT> &mdash; The solution has total
 * weight <I>W</I><SUB>total</SUB> and total value <I>V</I><SUB>total</SUB>.
 * <LI>
 * <TT>k &lt;j1&gt; &lt;j2&gt; ...</TT> &mdash; The solution consists of the
 * items at indexes <I>j1</I>, <I>j2</I>, and so on.
 * </UL>
 *
 * @author  Alan Kaminsky
 * @version 07-Sep-2016
 */
public class WalkSackGpu
	extends Task
	{

	/**
	 * Kernel interface for GPU walk() function.
	 */
	private static interface WalkKernel
		extends Kernel
		{
		public void walk
			(long C,
			 int N,
			 GpuStructArray<WV> item_wv,
			 GpuIntMatrix curr_soln,
			 GpuStructArray<WV> best_wv,
			 GpuIntMatrix best_soln,
			 GpuLongArray best_step,
			 long seed,
			 long steps);
		}

	/**
	 * Reduction variable class for knapsack problem solutions.
	 */
	private static class SolutionVbl
		implements Vbl
		{
		public long C;
		public WV wv;
		public int[] soln;

		public SolutionVbl (long C)
			{
			this.C = C;
			this.wv = new WV();
			this.soln = new int [0];
			}

		public void set (WV wv, int[] soln)
			{
			this.wv = new WV (wv);
			this.soln = (int[]) soln.clone();
			}

		public void set (SolutionVbl vbl)
			{
			this.C = vbl.C;
			this.wv = new WV (vbl.wv);
			this.soln = (int[]) vbl.soln.clone();
			}

		public Object clone()
			{
			try
				{
				SolutionVbl vbl = (SolutionVbl) super.clone();
				vbl.set (this);
				return vbl;
				}
			catch (CloneNotSupportedException exc)
				{
				throw new IllegalStateException (exc);
				}
			}

		public void set (Vbl vbl)
			{
			set ((SolutionVbl)vbl);
			}

		public void reduce (Vbl vbl)
			{
			if (((SolutionVbl)vbl).wv.isBetterThan (this.wv, this.C))
				set (vbl);
			}
		}

	// Command line arguments.
	boolean pFlag;
	String constructor;
	long seed;
	long reps;
	long steps;
	int NB;
	int NT;
	int NA;

	// Knapsack problem parameters.
	long C;
	int N;
	WV[] wv;

	// Global reduction variable for solution.
	SolutionVbl solution;

	/**
	 * Main program.
	 */
	public void main
		(String[] args)
		throws Exception
		{
		long t1 = System.currentTimeMillis();

		// Parse command line arguments.
		pFlag = false;
		int argi = 0;
		while (argi < args.length && args[argi].charAt(0) == '-')
			{
			if (args[argi].equals ("-p"))
				{
				pFlag = true;
				++ argi;
				}
			else
				usage();
			}
		if (argi >= args.length) usage();
		constructor = args[argi++];
		if (argi >= args.length) usage();
		seed = Long.parseLong (args[argi++]);
		if (argi >= args.length) usage();
		reps = Long.parseLong (args[argi++]);
		if (argi >= args.length) usage();
		steps = Long.parseLong (args[argi++]);
		NB = -1;
		NT = -1;
		if (argi < args.length)
			{
			NB = Integer.parseInt (args[argi++]);
			if (argi < args.length)
				{
				NT = Integer.parseInt (args[argi++]);
				if (argi < args.length) usage();
				}
			}

		// Determine number of GPU accelerators to use.
		NA = Gpu.allowedDeviceCount();

		// Print provenance.
		System.out.printf
			("# java pj2 gpus=%d edu.rit.gpu.example.WalkSackGpu", NA);
		if (pFlag) System.out.printf (" -p");
		System.out.printf (" \"%s\"", constructor);
		System.out.printf (" %d", seed);
		System.out.printf (" %d", reps);
		System.out.printf (" %d", steps);
		if (NB != -1) System.out.printf (" %d", NB);
		if (NT != -1) System.out.printf (" %d", NT);
		System.out.println();
		System.out.printf ("# Started %s%n", new Date (t1));
		System.out.flush();

		// Set up knapsack problem.
		KnapsackProblem problem = (KnapsackProblem)
			Instance.newInstance (constructor);
		C = problem.capacity();
		N = problem.itemCount();
		wv = new WV [N];
		for (int i = 0; i < N; ++ i)
			wv[i] = problem.next();
		if (pFlag)
			{
			System.out.printf ("p zeroOne %d %d%n", C, N);
			System.out.flush();
			for (int i = 0; i < N; ++ i)
				{
				System.out.printf ("i %d %d %d%n",
					i, wv[i].weight, wv[i].value);
				System.out.flush();
				}
			}

		// Set up global reduction variable for solution.
		solution = new SolutionVbl (C);

		// Run in parallel on multiple GPU accelerators.
		parallelDo (NA, new Section()
			{
			public void run() throws Exception
				{
				// Set up thread local reduction variable for solution.
				SolutionVbl mySolution = threadLocal (solution);

				// Set up GPU and kernel module.
				Gpu gpu = Gpu.gpu();
				Module module = gpu.getModule
					("edu/rit/gpu/example/WalkSackGpu.ptx");
				int myNB = NB == -1 ? gpu.getMultiprocessorCount() : NB;
				int myNT = NT == -1 ? 256 : NT;
				int myNG = myNB*myNT;
				System.out.printf
					("# GPU %d of %d: NB = %d, NT = %d, NG = %d%n",
					 rank() + 1, NA, myNB, myNT, myNG);
				System.out.flush();

				// Set up GPU variables.
				GpuIntVbl bobRank = module.getIntVbl ("bobRank");
				GpuStructArray<WV> item_wv =
					gpu.getStructArray (WV.class, N);
				for (int i = 0; i < N; ++ i)
					item_wv.item[i] = wv[i];
				item_wv.hostToDev();
				GpuIntMatrix curr_soln =
					gpu.getIntMatrix (myNG, N+1, 0, 0);
				GpuStructArray<WV> best_wv =
					gpu.getStructArray (WV.class, myNG, 1);
				best_wv.item[0] = new WV();
				GpuIntMatrix best_soln =
					gpu.getIntMatrix (myNG, N+1, 1, N+1);
				GpuLongArray best_step =
					gpu.getLongArray (myNG);

				// Set up GPU kernel.
				WalkKernel wkern = module.getKernel (WalkKernel.class);
				wkern.setGridDim (myNB);
				wkern.setBlockDim (myNT);
				wkern.setCacheConfig (CacheConfig.CU_FUNC_CACHE_PREFER_L1);

				// Set up best-of-best solution across all repetitions.
				WV bob_wv = new WV();
				int[] bob_soln = new int [N+1];

				// Perform repetitions.
				for (long R = 0; R < reps; ++ R)
					{
					// Perform random walks on the GPU.
					bobRank.item = -1;
					bobRank.hostToDev();
					wkern.walk
						(C, N, item_wv, curr_soln, best_wv, best_soln,
						 best_step, rank()*(1L<<32) + seed + R*myNG, steps);

					// Get best solution from GPU.
					bobRank.devToHost();
					best_wv.devToHost (0, bobRank.item, 1);
					best_soln.devToHost (0, 0, bobRank.item, 0, 1, N+1);
					best_step.devToHost();

					// Print best solution.
					System.out.printf ("# GPU %d of %d, rep %d of %d%n",
						rank() + 1, NA, R + 1, reps);
					System.out.printf ("#    Best rank: %d%n", bobRank.item);
					System.out.printf ("#    Best weight/value: %d %d%n",
						best_wv.item[0].weight, best_wv.item[0].value);
					long bestStepBobRank = best_step.item[bobRank.item];
					Sorting.sort (best_step.item);
					System.out.printf
						("#    Best step: 5%% = %d, 50%% = %d, 95%% = %d, best = %d%n",
						 best_step.item[(int)(0.05*myNG)],
						 best_step.item[(int)(0.50*myNG)],
						 best_step.item[(int)(0.95*myNG)],
						 bestStepBobRank);
					System.out.flush();

					// Update best-of-best solution.
					if (best_wv.item[0].isBetterThan (bob_wv, C))
						{
						bob_wv.copy (best_wv.item[0]);
						int k = best_soln.item[0][0];
						System.arraycopy (best_soln.item[0], 0, bob_soln, 0,
							k+1);
						}
					}

				// Record best-of-best solution for reduction.
				mySolution.set (bob_wv, bob_soln);
				}
			});

		// Print best-of-best solution.
		System.out.printf ("s %d %d%n", solution.wv.weight, solution.wv.value);
		System.out.printf ("k");
		int k = solution.soln[0];
		Sorting.sort (solution.soln, 1, k);
		for (int i = 1; i <= k; ++ i)
			System.out.printf (" %d", solution.soln[i]);
		System.out.println();

		// Print running time.
		long t2 = System.currentTimeMillis();
		System.out.printf ("# Finished %s%n", new Date (t2));
		System.out.printf ("# %d msec%n", t2 - t1);
		}

	/**
	 * Print a usage message and exit.
	 */
	private static void usage()
		{
		System.err.println ("Usage: java pj2 [gpus=<NA>] edu.rit.gpu.example.WalkSackGpu [-p] \"<constructor>\" <seed> <reps> <steps> [<NB> [<NT>]]");
		System.err.println ("<NA> = Number of GPU accelerators to use");
		System.err.println ("-p = Print knapsack problem parameters");
		System.err.println ("<constructor> = KnapsackProblem constructor expression");
		System.err.println ("<seed> = Random seed");
		System.err.println ("<reps> = Number of repetitions");
		System.err.println ("<steps> = Number of steps per repetition");
		System.err.println ("<NB> = Number of blocks");
		System.err.println ("<NT> = Number of threads per block");
		terminate (1);
 		}

	/**
	 * This program requires one CPU core.
	 */
	protected static int coresRequired()
		{
		return 1;
		}

	/**
	 * This program requires all GPU accelerators on the node.
	 */
	protected static int gpusRequired()
		{
		return ALL_GPUS;
		}

	}

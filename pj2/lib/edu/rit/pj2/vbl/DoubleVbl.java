//******************************************************************************
//
// File:    DoubleVbl.java
// Package: edu.rit.pj2.vbl
// Unit:    Class edu.rit.pj2.vbl.DoubleVbl
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

package edu.rit.pj2.vbl;

import edu.rit.io.InStream;
import edu.rit.io.OutStream;
import edu.rit.pj2.Tuple;
import edu.rit.pj2.Vbl;
import java.io.IOException;

/**
 * Class DoubleVbl provides a double precision floating point reduction variable
 * shared by multiple threads executing a {@linkplain
 * edu.rit.pj2.ParallelStatement ParallelStatement}. A DoubleVbl is also a
 * {@linkplain Tuple}.
 * <P>
 * Class DoubleVbl supports the <I>parallel reduction</I> pattern. Each thread
 * creates a thread-local copy of the shared variable by calling the {@link
 * edu.rit.pj2.Loop#threadLocal(Vbl) threadLocal()} method of class {@linkplain
 * edu.rit.pj2.Loop Loop} or the {@link edu.rit.pj2.Section#threadLocal(Vbl)
 * threadLocal()} method of class {@linkplain edu.rit.pj2.Section Section}. Each
 * thread performs operations on its own copy, without needing to synchronize
 * with the other threads. At the end of the parallel statement, the
 * thread-local copies are automatically <I>reduced</I> together, and the result
 * is stored in the original shared variable. The reduction is performed by the
 * shared variable's {@link #reduce(Vbl) reduce()} method.
 * <P>
 * The following subclasses provide various predefined reduction operations. You
 * can also define your own subclasses with customized reduction operations.
 * <UL>
 * <LI>Sum -- Class {@linkplain DoubleVbl.Sum}
 * <LI>Minimum -- Class {@linkplain DoubleVbl.Min}
 * <LI>Maximum -- Class {@linkplain DoubleVbl.Max}
 * <LI>Mean -- Class {@linkplain DoubleVbl.Mean}
 * </UL>
 *
 * @author  Alan Kaminsky
 * @version 11-Oct-2016
 */
public class DoubleVbl
	extends Tuple
	implements Vbl
	{

// Kludge to avert false sharing in multithreaded programs.

	// Padding fields.
	volatile long p0 = 1000L;
	volatile long p1 = 1001L;
	volatile long p2 = 1002L;
	volatile long p3 = 1003L;
	volatile long p4 = 1004L;
	volatile long p5 = 1005L;
	volatile long p6 = 1006L;
	volatile long p7 = 1007L;
	volatile long p8 = 1008L;
	volatile long p9 = 1009L;
	volatile long pa = 1010L;
	volatile long pb = 1011L;
	volatile long pc = 1012L;
	volatile long pd = 1013L;
	volatile long pe = 1014L;
	volatile long pf = 1015L;

	// Method to prevent the JDK from optimizing away the padding fields.
	long preventOptimization()
		{
		return p0 + p1 + p2 + p3 + p4 + p5 + p6 + p7 +
			p8 + p9 + pa + pb + pc + pd + pe + pf;
		}

// Exported data members.

	/**
	 * The shared double precision floating point item.
	 */
	public double item;

// Exported constructors.

	/**
	 * Construct a new shared double precision floating point variable. The
	 * item's initial value is 0.0.
	 */
	public DoubleVbl()
		{
		}

	/**
	 * Construct a new shared double precision floating point variable with the
	 * given initial value.
	 *
	 * @param  value  Initial value.
	 */
	public DoubleVbl
		(double value)
		{
		this.item = value;
		}

// Exported operations.

	/**
	 * Returns the double precision floating point value of this shared
	 * variable.
	 * <P>
	 * The base class <TT>doubleValue()</TT> method returns the {@link #item}
	 * field. A subclass may override the <TT>doubleValue()</TT> method to
	 * return something else.
	 *
	 * @return  Double value.
	 */
	public double doubleValue()
		{
		return item;
		}

	/**
	 * Set this shared variable to the given shared variable. This variable must
	 * be set to a deep copy of the given variable.
	 *
	 * @param  vbl  Shared variable.
	 *
	 * @exception  ClassCastException
	 *     (unchecked exception) Thrown if the class of <TT>vbl</TT> is not
	 *     compatible with the class of this shared variable.
	 */
	public void set
		(Vbl vbl)
		{
		this.item = ((DoubleVbl)vbl).doubleValue();
		}

	/**
	 * Reduce the given double into this shared variable. This shared variable's
	 * {@link #item item} field and the argument <TT>x</TT> are combined
	 * together using this shared variable's reduction operation, and the result
	 * is stored in the {@link #item item} field.
	 * <P>
	 * The base class <TT>reduce()</TT> method throws an exception. The
	 * reduction operation must be defined in a subclass's <TT>reduce()</TT>
	 * method.
	 *
	 * @param  x  Double.
	 */
	public void reduce
		(double x)
		{
		throw new UnsupportedOperationException
			("reduce() not defined in base class DoubleVbl; use a subclass");
		}

	/**
	 * Reduce the given shared variable into this shared variable. The two
	 * variables are combined together using this shared variable's reduction
	 * operation, and the result is stored in this shared variable.
	 *
	 * @param  vbl  Shared variable.
	 *
	 * @exception  ClassCastException
	 *     (unchecked exception) Thrown if the class of <TT>vbl</TT> is not
	 *     compatible with the class of this shared variable.
	 */
	public void reduce
		(Vbl vbl)
		{
		reduce (((DoubleVbl)vbl).doubleValue());
		}

	/**
	 * Returns a string version of this shared variable.
	 *
	 * @return  String version.
	 */
	public String toString()
		{
		return "" + doubleValue();
		}

	/**
	 * Write this object's fields to the given out stream.
	 *
	 * @param  out  Out stream.
	 *
	 * @exception  IOException
	 *     Thrown if an I/O error occurred.
	 */
	public void writeOut
		(OutStream out)
		throws IOException
		{
		out.writeDouble (item);
		}

	/**
	 * Read this object's fields from the given in stream.
	 *
	 * @param  in  In stream.
	 *
	 * @exception  IOException
	 *     Thrown if an I/O error occurred.
	 */
	public void readIn
		(InStream in)
		throws IOException
		{
		item = in.readDouble();
		}

// Exported subclasses.

	/**
	 * Class DoubleVbl.Sum provides a double precision floating point reduction
	 * variable, with addition as the reduction operation.
	 *
	 * @author  Alan Kaminsky
	 * @version 11-Oct-2016
	 */
	public static class Sum
		extends DoubleVbl
		{
		/**
		 * Construct a new shared double precision floating point variable. The
		 * item's initial value is 0.0.
		 */
		public Sum()
			{
			super();
			}

		/**
		 * Construct a new shared double precision floating point variable with
		 * the given initial value.
		 *
		 * @param  value  Initial value.
		 */
		public Sum
			(double value)
			{
			super (value);
			}

		/**
		 * Reduce the given double into this shared variable. This shared
		 * variable's {@link #item item} field and the argument <TT>x</TT> are
		 * combined together using the addition operation, and the result is
		 * stored in the {@link #item item} field.
		 *
		 * @param  x  Double.
		 */
		public void reduce
			(double x)
			{
			this.item += x;
			}
		}

	/**
	 * Class DoubleVbl.Min provides a double precision floating point reduction
	 * variable, with minimum as the reduction operation.
	 *
	 * @author  Alan Kaminsky
	 * @version 11-Oct-2016
	 */
	public static class Min
		extends DoubleVbl
		{
		/**
		 * Construct a new shared double precision floating point variable. The
		 * item's initial value is 0.0.
		 */
		public Min()
			{
			super();
			}

		/**
		 * Construct a new shared double precision floating point variable with
		 * the given initial value.
		 *
		 * @param  value  Initial value.
		 */
		public Min
			(double value)
			{
			super (value);
			}

		/**
		 * Reduce the given double into this shared variable. This shared
		 * variable's {@link #item item} field and the argument <TT>x</TT> are
		 * combined together using the minimum operation, and the result is
		 * stored in the {@link #item item} field.
		 *
		 * @param  x  Double.
		 */
		public void reduce
			(double x)
			{
			this.item = Math.min (this.item, x);
			}
		}

	/**
	 * Class DoubleVbl.Max provides a double precision floating point reduction
	 * variable, with maximum as the reduction operation.
	 *
	 * @author  Alan Kaminsky
	 * @version 11-Oct-2016
	 */
	public static class Max
		extends DoubleVbl
		{
		/**
		 * Construct a new shared double precision floating point variable. The
		 * item's initial value is 0.0.
		 */
		public Max()
			{
			super();
			}

		/**
		 * Construct a new shared double precision floating point variable with
		 * the given initial value.
		 *
		 * @param  value  Initial value.
		 */
		public Max
			(double value)
			{
			super (value);
			}

		/**
		 * Reduce the given double into this shared variable. This shared
		 * variable's {@link #item item} field and the argument <TT>x</TT> are
		 * combined together using the maximum operation, and the result is
		 * stored in the {@link #item item} field.
		 *
		 * @param  x  Double.
		 */
		public void reduce
			(double x)
			{
			this.item = Math.max (this.item, x);
			}
		}

	/**
	 * Class DoubleVbl.Mean provides a double precision floating point reduction
	 * variable, with mean as the reduction operation.
	 * <P>
	 * Call the {@link #accumulate(double) accumulate()} method or the {@link
	 * #reduce(Vbl) reduce()} method to accumulate a value into the running mean
	 * computation. Call the {@link #doubleValue()} method to get the mean of
	 * the values accumulated so far. The {@link #item item} field holds the sum
	 * of the accumulated values. The {@link #count count} field holds the
	 * number of accumulated values.
	 *
	 * @author  Alan Kaminsky
	 * @version 11-Oct-2016
	 */
	public static class Mean
		extends DoubleVbl
		{
		/**
		 * The number of accumulated values.
		 */
		public int count;

		/**
		 * Construct a new shared double precision floating point variable. The
		 * item's initial value is 0.0. The count's initial value is 0.
		 */
		public Mean()
			{
			super();
			count = 0;
			}

		/**
		 * Construct a new shared double precision floating point variable. The
		 * item's initial value is the given value. The count's initial value is
		 * 1.
		 *
		 * @param  value  Value.
		 */
		public Mean
			(double value)
			{
			super (value);
			count = 1;
			}

		/**
		 * Accumulate the given value into this shared variable. The mean of the
		 * values accumulated into this shared variable is updated to include
		 * <TT>value</TT>.
		 *
		 * @param  value  Value.
		 *
		 * @return  This shared variable.
		 */
		public Mean accumulate
			(double value)
			{
			item += value;
			++ count;
			return this;
			}

		/**
		 * Returns the double precision floating point value of this shared
		 * variable. The value returned is the mean of the values that have been
		 * accumulated. If no values have been accumulated, <TT>Double.NaN</TT>
		 * is returned.
		 *
		 * @return  Double value.
		 */
		public double doubleValue()
			{
			return count == 0 ? Double.NaN : item/count;
			}

		/**
		 * Set this shared variable to the given shared variable. This variable
		 * must be set to a deep copy of the given variable.
		 * <P>
		 * If <TT>vbl</TT> is an instance of class DoubleVbl.Mean, then this
		 * shared variable consists of the same values that had been accumulated
		 * into <TT>vbl</TT>. If <TT>vbl</TT> is an instance of class DoubleVbl
		 * or a subclass thereof, but not an instance of class DoubleVbl.Mean,
		 * then this shared variable consists of just the one value
		 * <TT>vbl.doubleValue()</TT>.
		 *
		 * @param  vbl  Shared variable.
		 *
		 * @exception  ClassCastException
		 *     (unchecked exception) Thrown if the class of <TT>vbl</TT> is not
		 *     compatible with the class of this shared variable.
		 */
		public void set
			(Vbl vbl)
			{
			if (vbl instanceof Mean)
				{
				Mean meanVbl = (Mean) vbl;
				this.item = meanVbl.item;
				this.count = meanVbl.count;
				}
			else if (vbl instanceof DoubleVbl)
				{
				this.item = ((DoubleVbl)vbl).doubleValue();
				this.count = 1;
				}
			else
				throw new ClassCastException();
			}

		/**
		 * Reduce the given double into this shared variable. The mean of the
		 * values accumulated into this shared variable is updated to include
		 * <TT>x</TT>.
		 *
		 * @param  x  Double.
		 */
		public void reduce
			(double x)
			{
			accumulate (x);
			}

		/**
		 * Reduce the given shared variable into this shared variable. The two
		 * items are combined together using the mean operation, and the
		 * result is stored in this shared variable.
		 * <P>
		 * If <TT>vbl</TT> is an instance of class DoubleVbl.Mean, then all the
		 * values that had been accumulated into <TT>vbl</TT> are accumulated
		 * into this shared variable. If <TT>vbl</TT> is an instance of class
		 * DoubleVbl or a subclass thereof, but not an instance of class
		 * DoubleVbl.Mean, then <TT>vbl.doubleValue()</TT> is accumulated into
		 * this shared variable.
		 *
		 * @param  vbl  Shared variable.
		 *
		 * @exception  ClassCastException
		 *     (unchecked exception) Thrown if the class of <TT>vbl</TT> is not
		 *     compatible with the class of this shared variable.
		 */
		public void reduce
			(Vbl vbl)
			{
			if (vbl instanceof Mean)
				{
				Mean meanVbl = (Mean) vbl;
				this.item += meanVbl.item;
				this.count += meanVbl.count;
				}
			else if (vbl instanceof DoubleVbl)
				accumulate (((DoubleVbl)vbl).doubleValue());
			else
				throw new ClassCastException();
			}

		/**
		 * Write this object's fields to the given out stream.
		 *
		 * @param  out  Out stream.
		 *
		 * @exception  IOException
		 *     Thrown if an I/O error occurred.
		 */
		public void writeOut
			(OutStream out)
			throws IOException
			{
			super.writeOut (out);
			out.writeInt (count);
			}

		/**
		 * Read this object's fields from the given in stream.
		 *
		 * @param  in  In stream.
		 *
		 * @exception  IOException
		 *     Thrown if an I/O error occurred.
		 */
		public void readIn
			(InStream in)
			throws IOException
			{
			super.readIn (in);
			count = in.readInt();
			}
		}

	}

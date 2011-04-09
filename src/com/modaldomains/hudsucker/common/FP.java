package com.modaldomains.hudsucker.common;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class FP
{
	/**
	 * A generic function.
	 *
	 * @param <E>
	 * @param <R>
	 */
	public static interface FN<E, R>
	{
		/**
		 * Apply the function to the input.
		 * @param input
		 * @return
		 */
		R a(E input);
	}
	
	/**
	 * An accumulation function.
	 *
	 * @param <E>
	 * @param <R>
	 */
	public static interface FN2<E, R>
	{
		/**
		 * Apply the function to an input, given an accumulation value.
		 * @param input
		 * @param accumulator
		 * @return
		 */
		R a(E input, R accumulator);
	}
	
	/**
	 * A Predicate.
	 *
	 * @param <E> The element type.
	 */
	public static interface P<E> extends FN<E, Boolean>
	{
	}
	
	/**
	 * Apply a function to every element in a collection.
	 * @param <E>
	 * @param <R>
	 * @param c
	 * @param fn
	 * @return
	 */
	public static <E, R> Collection<R> apply(final Collection<E> c, final FN<E, R> fn)
	{
		return new AbstractCollection<R>()
		{
			public Iterator<R> iterator()
			{
				return new Iterator<R>()
				{
					Iterator<E> core;
					
					{
						core = c.iterator();
					}

					@Override
					public boolean hasNext()
					{
						return core.hasNext();
					}

					@Override
					public R next()
					{
						return fn.a(core.next());
					}

					@Override
					public void remove()
					{
						throw new UnsupportedOperationException();
					}
				};
			}

			public int size()
			{
				// TODO Auto-generated method stub
				return c.size();
			}
		};
	}
	
	/**
	 * Reduce a collection, given a function.
	 * @param <E>
	 * @param <R>
	 * @param c
	 * @param fn
	 * @param initial
	 * @return
	 */
	public static <E, R> R fold(Collection<E> c, FN2<E, R> fn, R initial)
	{
		R ret = initial;
		for (E e : c)
			ret = fn.a(e, ret);
		return ret;
	}
	
	/**
	 * 
	 * @param <E>
	 * @param <R>
	 * @param c
	 * @param fn
	 * @return
	 */
	public static <E, R> R fold(Collection<E> c, FN2<E, R> fn)
	{
		return FP.fold(c, fn, null);
	}
	
	/**
	 * Filter out elements of a collection that don't match a predicate.
	 * @param <E>
	 * @param c The collection to filter.
	 * @param pred The filter predicate.
	 * @return A read-only collection that only contains elements that match the predicate.
	 */
	public static <E> Collection<E> filter(Collection<E> c, P<E> pred)
	{
		return new FilteredCollection<E>(c, pred);
	}
	
	/**
	 * Tell if any element in a collection matches a predicate.
	 * @param <E>
	 * @param c
	 * @param m
	 * @return
	 */
	public static <E> boolean any(Collection<E> c, P<E> m)
	{
		for (E e : c)
			if (m.a(e))
				return true;
		return false;
	}

	/**
	 * Return the first element of a collection, whatever ordering the collection maintains.
	 * @param <E>
	 * @param c
	 * @return
	 */
	public static <E> E first(Collection<E> c)
	{
		if (c.isEmpty())
			return null;
		return c.iterator().next();
	}
	
	/**
	 * Extract the minimum value of a collection.
	 * @param <E>
	 * @param c
	 * @param comp
	 * @return
	 */
	public static <E> E min(Collection<E> c, Comparator<E> comp)
	{
		E min = null;
		for (E e : c)
		{
			if (min == null || comp.compare(e, min) < 0)
				min = e;
		}
		return min;
	}
	
	/**
	 * Extract the maximum value of a collection.
	 * @param <E>
	 * @param c
	 * @param comp
	 * @return
	 */
	public static <E> E max(Collection<E> c, Comparator<E> comp)
	{
		E max = null;
		for (E e : c)
		{
			if (max == null || comp.compare(e, max) > 0)
				max = e;
		}
		return max;
	}
	
	/**
	 * A read-only collection that only "contains" objects from an underlying collection
	 * that match a predicate.
	 * @author csm
	 *
	 * @param <T>
	 */
	static class FilteredCollection<T> extends AbstractCollection<T>
	{
		private final Collection<T> collection;
		private final P<T> predicate;
		
		public FilteredCollection(Collection<T> collection, P<T> predicate)
		{
			collection.getClass();
			predicate.getClass();
			this.collection = collection;
			this.predicate = predicate;
		}

		@Override
		public Iterator<T> iterator()
		{
			final Iterator<T> core = collection.iterator();
			return new Iterator<T>()
			{
				T current;
				boolean hasNext;

				{
					current = null;
					this.nextMatching();
				}
				
				private void nextMatching()
				{
					boolean gotNext = false;
					while (core.hasNext())
					{
						T value = core.next();
						if (predicate.a(value))
						{
							gotNext = true;
							current = value;
							break;
						}
					}
					if (!gotNext)
						hasNext = false;
					else
						hasNext = true;
				}

				@Override
				public boolean hasNext()
				{
					return hasNext;
				}

				@Override
				public T next()
				{
					if (!hasNext)
						throw new NoSuchElementException();
					T ret = current;
					this.nextMatching();
					return ret;
				}

				@Override
				public void remove()
				{
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public int size()
		{
			return FP.fold(collection, new FN2<T, Integer>()
			{
				@Override
				public Integer a(T input, Integer accumulator)
				{
					if (accumulator == null)
						accumulator = 0;
					if (predicate.a(input))
						accumulator = accumulator + 1;
					return accumulator;
				}
			}, 0);
		}
	}
}

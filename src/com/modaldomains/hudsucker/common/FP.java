package com.modaldomains.hudsucker.common;

import java.util.ArrayList;
import java.util.Collection;

public class FP
{
	public static interface FN<E, R>
	{
		R transform(E input);
	}
	
	public static interface FN2<E, R>
	{
		R transform(E input, R accumulator);
	}
	
	public static interface Match<E>
	{
		boolean match(E first, E second);
	}
	
	public static <E, R> Collection<R> apply(Collection<E> c, FN<E, R> fn)
	{
		ArrayList<R> res = new ArrayList<R>();
		for (E e : c)
			res.add(fn.transform(e));
		return res;
	}
	
	public static <E, R> R fold(Collection<E> c, FN2<E, R> fn)
	{
		R ret = null;
		for (E e : c)
			ret = fn.transform(e, ret);
		return ret;
	}
	
	public static <E> Collection<E> filter(Collection<E> c, FN<E, Boolean> pred)
	{
		ArrayList<E> res = new ArrayList<E>();
		for (E e : c)
			if (pred.transform(e))
				res.add(e);
		return res;
	}
	
	public static <E> boolean any(Collection<E> c, FN<E, Boolean> m)
	{
		for (E e : c)
			if (m.transform(e))
				return true;
		return false;
	}
}

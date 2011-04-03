package com.modaldomains.hudsucker.common;

public class Pair<T>
{
	private final T first;
	private final T second;
	
	public Pair(T first, T second)
	{
		this.first = first;
		this.second = second;
	}
	
	public T first()
	{
		return first;
	}
	
	public T second()
	{
		return second;
	}
	
	public int hashCode()
	{
		return first.hashCode() ^ second.hashCode();
	}
	
	public boolean equals(Object o)
	{
		return this.equals((Pair<T>) o);
	}
	
	public boolean equals(Pair<? extends T> that)
	{
		if (that == null)
			return false;
		return first.equals(that.first) && second.equals(that.second);
	}
}

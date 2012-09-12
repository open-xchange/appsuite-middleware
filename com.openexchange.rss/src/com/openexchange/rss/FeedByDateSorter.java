package com.openexchange.rss;

import java.util.Comparator;

public class FeedByDateSorter<T> implements Comparator<RssResult>{
	private String order;

	public FeedByDateSorter(String order) {
		this.order = order;
	}

	@Override
	public int compare(RssResult r1, RssResult r2) {
		int res = r1.getDate().compareTo(r2.getDate());
		if(order.equalsIgnoreCase("DESC"))
			res *= -1;
		return res;
	}
	

}

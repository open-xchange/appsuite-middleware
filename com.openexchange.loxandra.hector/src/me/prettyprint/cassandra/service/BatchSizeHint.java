package me.prettyprint.cassandra.service;

public class BatchSizeHint
{
	private final int numOfRows;
	private final int numOfColumns;

	public BatchSizeHint(int numOfRows, int numOfColumns)
	{
		this.numOfRows = numOfRows;
		this.numOfColumns = numOfColumns;
	}

	public int getNumOfColumns()
	{
		return numOfColumns;
	}

	public int getNumOfRows()
	{
		return numOfRows;
	}
	
}

package org.tridas.io.util;

import java.util.ArrayList;

public class StatsUtil {

	
	public static int mode(ArrayList<Integer> arr) 
	{
		Integer[] a = arr.toArray(new Integer[arr.size()]);
		
		int maxValue = 0, maxCount = 0;

		for (int i = 0; i < a.length; ++i) 
		{
			int count = 0;
			for (int j = 0; j < a.length; ++j) 
			{
				if (a[j] == a[i]) ++count;
			}
			if (count > maxCount) 
			{
				maxCount = count;
				maxValue = a[i];
			}
		}

		return maxValue;
	}
	
}

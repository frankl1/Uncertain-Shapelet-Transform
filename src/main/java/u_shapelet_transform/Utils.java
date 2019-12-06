package u_shapelet_transform;

import java.util.List;

public class Utils {
	public static void sort(List<UOrderLineObj> orderline, int start, int end) {
		int p;
		System.out.println("Start:" + start + " end:" + end);
		if (start < end) {
			p = partition(orderline, start, end);
			sort(orderline, start, p-1);
			sort(orderline, p+1, end);
		}
	}
	
	public static int partition(List<UOrderLineObj> orderline, int lo, int hi) {
		UOrderLineObj pivot = orderline.get(lo);
		int i = lo - 1;
		int j = hi + 1;
		UOrderLineObj tmp;
		while (true) {
			do {
				i++;
			}while(i<=hi && orderline.get(i).compareTo(pivot) < 0);
			
			do {
				j--;
			}while(j>=0 && orderline.get(j).compareTo(pivot) > 0);
			
			if (i >= j) {
				return j;
			}
			
			tmp = orderline.get(i);
			orderline.set(i, orderline.get(j));
			orderline.set(j, tmp);
		}
		
	}
}

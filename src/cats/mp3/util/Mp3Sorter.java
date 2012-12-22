package cats.mp3.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cats.mp3.Mp3;

public class Mp3Sorter implements Comparator<Mp3>{
	
	public static final int NAME = 0;
	public static final int SIZE = 1;
	public static final int BITRATE = 2;
	public static final int DURATION = 3;
	
	private List<Mp3> list;
	private int field;
	
	public Mp3Sorter(final List<Mp3> list, final int field){
		this.list = list;
		this.field = field;
	}
	
	public int compare(final Mp3 m1, final Mp3 m2){
		if(field == NAME)
			return m2.getName().compareTo(m1.getName());
		else if(field == SIZE)
			return (int) (m2.getDoubleSize() - m1.getDoubleSize());
		else if(field == BITRATE)
			return m2.getBitrateAsInt() - m1.getBitrateAsInt();
		else if(field == DURATION)
			return m2.getDurationAsInt() - m1.getDurationAsInt();
		else
			return 0;
	}
	
	public void sort(){
		Collections.sort(list, this);
	}
	
	public static void sort(final List<Mp3> list, final int field){
		new Mp3Sorter(list, field).sort();
	}

}

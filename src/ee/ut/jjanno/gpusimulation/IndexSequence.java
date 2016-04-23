package ee.ut.jjanno.gpusimulation;

public class IndexSequence {

	private int index = 0;

	public int getIndex() {
		return index++;
	}
	
	public int getLastIndex() {
		return index;
	}

}
package ee.ut.jjanno.gpusimulation;

public class IndexSequence {

	private int index = 0;
	private int softIndex = 0;

	public IndexSequence(int index) {
		super();
		this.index = index;
	}

	public int getIndex() {
		softIndex++;
		return index++;
	}
	
	public int getLastIndex() {
		return index;
	}
	
	public void addRelativeIndices(IndexSequence seq) {
		this.index += seq.softIndex;
	}

}
package ee.ut.jjanno.gpusimulation;

public class IndexSequence {

	private int index = 0;
	private int parentIndex = 0;

	public IndexSequence(int index) {
		super();
		this.index = index;
		this.parentIndex = 0;
	}

	public int getIndex() {
		return index++;
	}
	
	public int getLastIndex() {
		return index;
	}

	public int getParentIndex() {
		return parentIndex;
	}

	public IndexSequence setParentIndex(int parentIndex) {
		this.parentIndex = parentIndex;
		return this;
	}

}
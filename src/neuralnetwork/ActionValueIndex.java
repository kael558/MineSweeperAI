package neuralnetwork;

public class ActionValueIndex {
	private double value;
	private int index;
	
	
	public ActionValueIndex(double value, int index){
		this.setValue(value);
		this.setIndex(index);
	}


	public int getIndex() {
		return index;
	}


	public void setIndex(int index) {
		this.index = index;
	}


	public double getValue() {
		return value;
	}


	public void setValue(double value) {
		this.value = value;
	}
	
	
	
	
}

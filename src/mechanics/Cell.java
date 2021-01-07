package mechanics;

import java.io.Serializable;

import interfaces.CellType;


public class Cell implements Serializable {

	private static final long serialVersionUID = 6027716377690031477L;
	private CellType secretCellType;
	
	
	public Cell(CellType secretCellType){
		this.secretCellType = secretCellType;
	}

	public void setSecretStatus(CellType secretCellType){
		this.secretCellType = secretCellType;
	}
	
	public CellType getSecretStatus(){
		return secretCellType;
	}
	
	

	
	
}

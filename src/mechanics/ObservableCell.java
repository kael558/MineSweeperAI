package mechanics;

import java.io.Serializable;

import interfaces.CellType;


public class ObservableCell implements Serializable{

	private static final long serialVersionUID = -8286311764252090592L;
	public CellType cellType;
	

	public ObservableCell(){
		this.cellType = CellType.HIDDEN;
	}
	
	public ObservableCell(CellType cellType){
		this.cellType = cellType;
	}
	
	public CellType getCellType(){
		return cellType;
	}
	
	public void setCellType(CellType cellType){
		this.cellType = cellType;
	}
}

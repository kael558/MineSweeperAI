package mechanics;

import java.io.Serializable;

import interfaces.StatusConstants;

public class ObservableCell  implements StatusConstants, Serializable{

	private static final long serialVersionUID = -8286311764252090592L;
	public int status;
	

	
	public ObservableCell(){
		this.status = STATUS_HIDDEN;
	}
	
	public ObservableCell(int status){
		this.status = status;
	}
	
	public int getStatus(){
		return status;
	}
	
	public void setStatus(int status){
		this.status = status;
	}
}

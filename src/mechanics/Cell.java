package mechanics;

import java.io.Serializable;

import interfaces.StatusConstants;

public class Cell  implements StatusConstants, Serializable {

	private static final long serialVersionUID = 6027716377690031477L;
	private int secretStatus; 
	
	
	public Cell(int secretStatus){
		this.secretStatus = secretStatus;
	}

	public void setSecretStatus(int secretStatus){
		this.secretStatus = secretStatus;
	}
	
	public int getSecretStatus(){
		return secretStatus;
	}
	
	

	
	
}

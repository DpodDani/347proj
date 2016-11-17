import java.io.Serializable;

public class Message implements Serializable{
	private long clientId;
	private String message;
	private String requestType;
	private long messageNo;

	
	public Message(){}
	
	public Message(String requestType){
		this.requestType = requestType;
	}
	
	public Message(String message, String requestType){
		this.message = message;
		this.requestType = requestType;
	}
	
	public Message(String message, String requestType, long messageNo){
		this.message = message;
		this.requestType = requestType;
		this.messageNo = messageNo;
		
	}
	
	public Message(long clientId, String message, String requestType,long messageNo){
		this.message = message;
		this.messageNo = messageNo;
		this.requestType = requestType;
		this.clientId = clientId;
	}
	
	public long getClientId(){
		return clientId;
	}
	
	public String getMessage(){
		return message;
	}
	
	public String getRequestType(){
		return requestType;
	}
	
	public long getMessageNo(){
		return messageNo;
	}
	
	public boolean setClientId(long clientId){
		this.clientId = clientId;
		return true;
	}
	
	public boolean setMessage(String message){
		this.message = message;
		return true;
	}
	
	public boolean setRequestType(String requestType){
		this.requestType = requestType;
		return true;
	}
	
	public boolean setMessageNo(long messageNo){
		this.messageNo = messageNo;
		return true;
	}
	
}
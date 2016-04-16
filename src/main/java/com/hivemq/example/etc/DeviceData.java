package com.hivemq.example.etc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceData {
	
	@JsonProperty(APIKeys.DEVICEDATA_ID)
	private String deviceID;
	
	@JsonProperty(APIKeys.DEVICEDATA_CONDITION)
	private int deviceCondition;
	
	@JsonProperty(APIKeys.DEVICEDATA_STATUS)
	private String deviceStatus;
	
	@JsonProperty(APIKeys.DEVICEDATA_ERROR_CODE)
	private int errorCode;
	
	public DeviceData(){}
	
	public DeviceData(String deviceId, int deviceCondition, String deviceStatus, int errorCode){
		this.errorCode = errorCode;
		this.deviceCondition = deviceCondition;
		this.deviceID = deviceId;
		this.deviceStatus = deviceStatus;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public int getDeviceCondition() {
		return deviceCondition;
	}

	public void setDeviceCondition(int deviceCondition) {
		this.deviceCondition = deviceCondition;
	}

	public String getDeviceStatus() {
		return deviceStatus;
	}

	public void setDeviceStatus(String deviceStatus) {
		this.deviceStatus = deviceStatus;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	
	

}

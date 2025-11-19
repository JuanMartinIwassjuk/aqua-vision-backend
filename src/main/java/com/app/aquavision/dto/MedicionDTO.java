package com.app.aquavision.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class MedicionDTO {

    @JsonProperty("deviceId")
    private String deviceId;

    @JsonProperty("id_sector")
    private Long sectorId;

    //private int flow;
    private Double flow;

    //@JsonFormat(pattern = "yyyy:MM:dd:HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    public MedicionDTO() {}

    // GETTERS Y SETTERS

    public String getDeviceId() { return deviceId; }
    
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Long getSectorId() { return sectorId; }

    public void setSectorId(Long sectorId) { this.sectorId = sectorId; }

    /*public int getFlow() { return flow; }*/
    
    public Double getFlow() { return flow; }

    /*public void setFlow(int flow) { this.flow = flow; }*/

    public void setFlow(Double flow) { this.flow = flow; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
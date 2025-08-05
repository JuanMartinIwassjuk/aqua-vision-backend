package com.app.aquavision.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class MedicionDTO {

    @JsonProperty("id_sector")
    private Long sectorId;

    private int flow;

    @JsonFormat(pattern = "yyyy:MM:dd:HH:mm")
    private LocalDateTime timestamp;

    public MedicionDTO() {}

    public Long getSectorId() {
        return sectorId;
    }

    public void setSectorId(Long sectorId) {
        this.sectorId = sectorId;
    }

    public int getFlow() {
        return flow;
    }

    public void setFlow(int flow) {
        this.flow = flow;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
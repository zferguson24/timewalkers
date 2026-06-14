package com.wow.timewalkers.exception;

import com.wow.timewalkers.dto.RejectedSlotDTO;

import java.util.List;

public class GearValidationException extends RuntimeException {

    private final List<RejectedSlotDTO> rejections;

    public GearValidationException(String message, List<RejectedSlotDTO> rejections) {
        super(message);
        this.rejections = rejections;
    }

    public List<RejectedSlotDTO> getRejections() {
        return rejections;
    }
}

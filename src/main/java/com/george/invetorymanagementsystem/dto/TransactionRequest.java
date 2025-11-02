package com.george.invetorymanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionRequest {

    @Positive(message = "ProductDTO id is requered")
    private Long productId;

    @Positive(message = "Quantity id is requered")
    private Integer quantity;

    private Long supplierId;

    private Long description;

}

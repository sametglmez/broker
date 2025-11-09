package com.example.broker.dto;

import lombok.*;


import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerDto {
    private Long id;
    private String name;
    private List<Long> assetIds;
    private List<Long> orderIds;
}
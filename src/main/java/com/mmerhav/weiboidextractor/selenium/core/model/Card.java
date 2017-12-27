package com.mmerhav.weiboidextractor.selenium.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(exclude = "name")
public class Card {

    private String id;
    private String name;
}

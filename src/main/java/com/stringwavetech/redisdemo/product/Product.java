package com.stringwavetech.redisdemo.product;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = BaseEntity.PREFIX + "product")
@Getter
@Setter
@NoArgsConstructor
public class Product extends BaseEntity {

    private String name;

    private double price;

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }
}

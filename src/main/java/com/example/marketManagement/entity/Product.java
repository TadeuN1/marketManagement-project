package com.example.marketManagement.entity;


import jakarta.persistence.*;


@Entity
@Table(name = "products")
public class Product {

    // attributes mapped to the table columns declared above
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String category;
    @Column(name = "price_cents", nullable = false)
    private Integer priceCents;
    @Column(nullable = false)
    private Boolean active;

    // manual getters/setters for value access and updates

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() { return name;}
    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {return category;}
    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getPriceCents() {
        return priceCents;
    }
    public void setPriceCents(Integer priceCents) {
        this.priceCents = priceCents;
    }

    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }
}

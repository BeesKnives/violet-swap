package com.launchcode.violetSwap.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "listing")
public class Listing extends AbstractEntity implements Comparable<Listing>{

    @ManyToOne
    private Variety variety;

    @ManyToOne
    private static User user;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    @Enumerated(EnumType.STRING)
    private Maturity maturity; //enum Maturity
    @Size(max = 300)
    private String description;

    @Column(length = 1000)
    private String imagePath;




    //constructors
    public Listing() {

    }

    public Listing(Variety variety, Maturity maturity, String description){ //Initialize id and fields.
        super(); //for id
        this.variety = variety;
        this.maturity = maturity;
        this.description = description;
    }


    //override the compareTo method of Comparable class, will be used in SearchService for sorting
    @Override public int compareTo(Listing otherListing){

        Double compareDistance = this.getUser().getDistance() - otherListing.getUser().getDistance(); //comparing listing's user's distances

        return (int)Math.round(compareDistance); //convert Double to int
    }




    //getters and setters


    public static User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Variety getVariety() {
        return variety;
    }

    public void setVariety(Variety variety) {
        this.variety = variety;
    }

    public Maturity getMaturity() {
        return maturity;
    }
    public void setMaturity(Maturity maturity) {
        this.maturity = maturity;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}

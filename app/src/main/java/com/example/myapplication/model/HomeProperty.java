package com.example.myapplication.model;

public class HomeProperty {
    private String propertyId;
    private final String type;
    private final String title;
    private final String location;
    private final String price;
    private final String imageUrl;
    private final String ownerPhone;
    private final double rawPrice;
    private final double rawArea;
    private final boolean hasGarage;
    private final boolean hasGarden;
    private final int bedrooms;
    private final int bathrooms;
    private final String city;
    private boolean liked;

    public HomeProperty(String propertyId, String type, String title, String location,
                        String price, String imageUrl, String ownerPhone, boolean liked) {
        this(propertyId, type, title, location, price, imageUrl, ownerPhone, liked, 0, 0, false, false, 0, 0, "");
    }

    public HomeProperty(String propertyId, String type, String title, String location,
                        String price, String imageUrl, String ownerPhone, boolean liked,
                        double rawPrice, double rawArea, boolean hasGarage, boolean hasGarden) {
        this(propertyId, type, title, location, price, imageUrl, ownerPhone, liked, rawPrice, rawArea, hasGarage, hasGarden, 0, 0, "");
    }

    public HomeProperty(String propertyId, String type, String title, String location,
                        String price, String imageUrl, String ownerPhone, boolean liked,
                        double rawPrice, double rawArea, boolean hasGarage, boolean hasGarden,
                        int bedrooms, int bathrooms) {
        this(propertyId, type, title, location, price, imageUrl, ownerPhone, liked, rawPrice, rawArea, hasGarage, hasGarden, bedrooms, bathrooms, "");
    }

    public HomeProperty(String propertyId, String type, String title, String location,
                        String price, String imageUrl, String ownerPhone, boolean liked,
                        double rawPrice, double rawArea, boolean hasGarage, boolean hasGarden,
                        int bedrooms, int bathrooms, String city) {
        this.propertyId = propertyId;
        this.type = type;
        this.title = title;
        this.location = location;
        this.price = price;
        this.imageUrl = imageUrl;
        this.ownerPhone = ownerPhone;
        this.liked = liked;
        this.rawPrice = rawPrice;
        this.rawArea = rawArea;
        this.hasGarage = hasGarage;
        this.hasGarden = hasGarden;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.city = city;
    }

    public String getPropertyId() { return propertyId; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getLocation() { return location; }
    public String getPrice() { return price; }
    public String getImageUrl() { return imageUrl; }
    public String getOwnerPhone() { return ownerPhone; }
    public double getRawPrice() { return rawPrice; }
    public double getRawArea() { return rawArea; }
    public boolean isHasGarage() { return hasGarage; }
    public boolean isHasGarden() { return hasGarden; }
    public int getBedrooms() { return bedrooms; }
    public int getBathrooms() { return bathrooms; }
    public String getCity() { return city; }
    public boolean isLiked() { return liked; }
    public void setLiked(boolean liked) { this.liked = liked; }
}

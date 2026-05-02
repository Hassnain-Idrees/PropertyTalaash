package com.example.myapplication.model;

import java.util.ArrayList;
import java.util.List;

public class Property {

    private String propertyId;
    private String ownerId;
    private String ownerName;
    private String ownerPhone;
    private String title;
    private String description;
    private String type;
    private String city;
    private String address;
    private double price;
    private double area;
    private String areaUnit;
    private int bedrooms;
    private int bathrooms;
    private boolean hasGarage;
    private boolean hasGarden;
    private double latitude;
    private double longitude;
    private String imageUrl;
    private List<String> imagePaths;
    private int yearBuilt;
    private long timestamp;

    public Property() {
    }

    public Property(String propertyId, String ownerId, String ownerName, String ownerPhone,
                    String title, String description, String type, String city, String address,
                    double price, double area, String areaUnit, int bedrooms,
                    int bathrooms, boolean hasGarage, boolean hasGarden,
                    double latitude, double longitude, String imageUrl, List<String> imagePaths, int yearBuilt, long timestamp) {
        this.propertyId = propertyId;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.ownerPhone = ownerPhone;
        this.title = title;
        this.description = description;
        this.type = type;
        this.city = city;
        this.address = address;
        this.price = price;
        this.area = area;
        this.areaUnit = areaUnit;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.hasGarage = hasGarage;
        this.hasGarden = hasGarden;
        this.latitude = latitude;
        this.longitude = longitude;
        this.imageUrl = imageUrl;
        this.imagePaths = imagePaths;
        this.yearBuilt = yearBuilt;
        this.timestamp = timestamp;
    }

    public String getPropertyId() { return propertyId; }
    public void setPropertyId(String propertyId) { this.propertyId = propertyId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public String getAreaUnit() { return areaUnit; }
    public void setAreaUnit(String areaUnit) { this.areaUnit = areaUnit; }

    public int getBedrooms() { return bedrooms; }
    public void setBedrooms(int bedrooms) { this.bedrooms = bedrooms; }

    public int getBathrooms() { return bathrooms; }
    public void setBathrooms(int bathrooms) { this.bathrooms = bathrooms; }

    public boolean isHasGarage() { return hasGarage; }
    public void setHasGarage(boolean hasGarage) { this.hasGarage = hasGarage; }

    public boolean isHasGarden() { return hasGarden; }
    public void setHasGarden(boolean hasGarden) { this.hasGarden = hasGarden; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getImagePaths() { return imagePaths; }
    public void setImagePaths(List<String> imagePaths) { this.imagePaths = imagePaths; }

    public int getYearBuilt() { return yearBuilt; }
    public void setYearBuilt(int yearBuilt) { this.yearBuilt = yearBuilt; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    
    public String getFirstImage() {
        if (imagePaths != null && !imagePaths.isEmpty()) return imagePaths.get(0);
        if (imageUrl != null && !imageUrl.isEmpty()) return imageUrl;
        return "";
    }
}

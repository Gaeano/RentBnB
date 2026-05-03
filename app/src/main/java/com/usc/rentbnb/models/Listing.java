package com.usc.rentbnb.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

public class Listing implements Parcelable {
    private String id;
    private String productName;
    private String description;
    private String category;
    private String island;
    private double price;
    private String priceUnit;
    private List<String> paymentMethods;
    private List<String> suggestedActivities;
    private List<String> imageUrls;
    private double rating;
    private int totalReviews;
    private int timesRented;
    private String createdAt;
    private String ownerId;
    private String ownerName;
    private String ownerFaq;

    public Listing(String id, String productName, String description, String category, String island, double price, String priceUnit, double rating, int totalReviews, List<String> paymentMethods, List<String> suggestedActivities, List<String> imageUrls, String createdAt, int timesRented, String ownerId, String ownerName, String ownerFaq) {
        this.id = id;
        this.productName = productName;
        this.description = description;
        this.category = category;
        this.island = island;
        this.price = price;
        this.priceUnit = priceUnit;
        this.rating = rating;
        this.totalReviews = totalReviews;
        this.paymentMethods = paymentMethods;
        this.suggestedActivities = suggestedActivities;
        this.imageUrls = imageUrls;
        this.createdAt = createdAt;
        this.timesRented = timesRented;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.ownerFaq = ownerFaq;
    }

    public Listing() {}

    protected Listing(Parcel in) {
        id = in.readString();
        productName = in.readString();
        description = in.readString();
        category = in.readString();
        island = in.readString();
        price = in.readDouble();
        priceUnit = in.readString();
        paymentMethods = in.createStringArrayList();
        suggestedActivities = in.createStringArrayList();
        imageUrls = in.createStringArrayList();
        rating = in.readDouble();
        totalReviews = in.readInt();
        timesRented = in.readInt();
        createdAt = in.readString();
        ownerId = in.readString();
        ownerName = in.readString();
        ownerFaq = in.readString();
    }

    public static final Creator<Listing> CREATOR = new Creator<Listing>() {
        @Override
        public Listing createFromParcel(Parcel in) {
            return new Listing(in);
        }

        @Override
        public Listing[] newArray(int size) {
            return new Listing[size];
        }
    };

    public String getId() {return id;}
    public String getProductName() {return productName;}
    public String getDescription() {return description;}
    public String getCategory() {return category;}
    public String getIsland() {return island;}
    public double getPrice() {return price;}
    public String getPriceUnit() {return priceUnit;}
    public double getRating() {return rating;}
    public int getTotalReviews() {return totalReviews;}
    public List<String> getImageUrls() {return imageUrls;}
    @Exclude
    public String getCreatedAt() {return createdAt;}
    public int getTimesRented() {return timesRented;}
    public List<String> getPaymentMethods() {return paymentMethods;}
    public List<String> getSuggestedActivities() {return suggestedActivities;}
    public String getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public String getOwnerFaq() { return ownerFaq; }

    public boolean isNew() {
        if (createdAt == null || createdAt.isEmpty()) {
            return false;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date dateCreated = sdf.parse(createdAt);

            if (dateCreated != null) {
                long diffInMillis = System.currentTimeMillis() - dateCreated.getTime();
                long hoursDiff = diffInMillis / (1000 * 60 * 60);
                return hoursDiff <= 48;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setIsland(String island) {
        this.island = island;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setPriceUnit(String priceUnit) {
        this.priceUnit = priceUnit;
    }

    public void setPaymentMethods(List<String> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }

    public void setSuggestedActivities(List<String> suggestedActivities) {
        this.suggestedActivities = suggestedActivities;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public void setTimesRented(int timesRented) {
        this.timesRented = timesRented;
    }

    @Exclude
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public void setOwnerFaq(String ownerFaq) { this.ownerFaq = ownerFaq; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(productName);
        dest.writeString(description);
        dest.writeString(category);
        dest.writeString(island);
        dest.writeDouble(price);
        dest.writeString(priceUnit);
        dest.writeStringList(paymentMethods);
        dest.writeStringList(suggestedActivities);
        dest.writeStringList(imageUrls);
        dest.writeDouble(rating);
        dest.writeInt(totalReviews);
        dest.writeInt(timesRented);
        dest.writeString(createdAt);
        dest.writeString(ownerId);
        dest.writeString(ownerName);
        dest.writeString(ownerFaq);
    }
}

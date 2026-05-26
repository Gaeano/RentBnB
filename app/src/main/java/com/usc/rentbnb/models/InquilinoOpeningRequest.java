package com.usc.rentbnb.models;

public class InquilinoOpeningRequest {
    private String chatRoomId;
    private String renterId;
    private String ownerId;
    private String listingId;
    private String listingTitle;
    private String listingCategory;
    private String listingLocation;
    private String listingPrice;
    private String listingDescription;
    private String ownerFaq;

    public InquilinoOpeningRequest(String chatRoomId, String renterId, String ownerId,
                                   String listingId, String listingTitle,
                                   String listingCategory, String listingLocation,
                                   String listingPrice, String listingDescription,
                                   String ownerFaq) {
        this.chatRoomId        = chatRoomId;
        this.renterId          = renterId;
        this.ownerId           = ownerId;
        this.listingId         = listingId;
        this.listingTitle      = listingTitle;
        this.listingCategory   = listingCategory;
        this.listingLocation   = listingLocation;
        this.listingPrice      = listingPrice;
        this.listingDescription = listingDescription;
        this.ownerFaq          = ownerFaq;
    }

    public String getChatRoomId()        { return chatRoomId; }
    public String getRenterId()          { return renterId; }
    public String getOwnerId()           { return ownerId; }
    public String getListingId()         { return listingId; }
    public String getListingTitle()      { return listingTitle; }
    public String getListingCategory()   { return listingCategory; }
    public String getListingLocation()   { return listingLocation; }
    public String getListingPrice()      { return listingPrice; }
    public String getListingDescription(){ return listingDescription; }
    public String getOwnerFaq()          { return ownerFaq; }
}
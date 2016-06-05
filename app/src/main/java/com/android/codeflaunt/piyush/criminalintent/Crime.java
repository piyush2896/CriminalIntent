package com.android.codeflaunt.piyush.criminalintent;

import android.text.format.DateFormat;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Piyush on 21-Feb-16.
 */
public class Crime {

    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private String mSuspect;
    private String mSuspectNo;

    public Crime(UUID id){
        mId = id;
        mDate = new Date();
    }

    public Crime(){
        this(UUID.randomUUID());
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public String getStringDate(){
        return new DateFormat().format("EEEE, MMM dd, yyyy", getDate()).toString();
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }

    public String getSuspectNo() {
        return mSuspectNo;
    }

    public void setSuspectNo(String suspectNo) {
        mSuspectNo = suspectNo;
    }

    public String getPhotoFileName(){
        return "IMG_" + getId().toString() + ".jpg";
    }
}

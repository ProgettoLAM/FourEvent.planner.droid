package lam.project.foureventplannerdroid.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * Created by spino on 28/07/16.
 */
public class Event implements Parcelable{

    public final String mTitle;

    public final String mDescription;

    public final String mStartDate;

    public final String mEndDate;

    public final String mTag;

    public final String mAddress;

    public final float mLatitude;

    public final float mLongitude;

    public final String mPrice;

    public int mParticipation;

    public int mCurrentTicket;

    public int mMaxTicket;

    public final String mImage;

    public boolean mIsPreferred;

    private Event(final String title, final String description, final String startDate,
                  final String endDate, final String tag, final String address, final float latitude,
                  final float longitude, final String price, final int participation, final String image,
                  final int currentTic, final int maxTic){

        this.mTitle = title;
        this.mDescription = description;
        this.mStartDate = startDate;
        this.mEndDate = endDate;
        this.mTag = tag;
        this.mAddress = address;
        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mPrice = price;
        this.mParticipation = participation;
        this.mImage = image;
        this.mCurrentTicket = currentTic;
        this.mMaxTicket = maxTic;
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public Event addParticipation(int mParticipation) {
        this.mParticipation = mParticipation;
        return this;
    }

    public Event addCurrentTicket(int mCurrentTicket) {
        this.mCurrentTicket = mCurrentTicket;
        return this;
    }

    public Event addMaxTicket(int mMaxTicket) {
        this.mMaxTicket = mMaxTicket;
        return this;
    }


    protected Event(Parcel in) {

        mTitle = in.readString();
        mDescription = in.readString();
        mStartDate = in.readString();
        mEndDate = in.readString();
        mTag = in.readString();
        mAddress = in.readString();
        mLatitude = in.readFloat();
        mLongitude = in.readFloat();
        mPrice = in.readString();
        mImage = in.readString();

        boolean present = in.readByte() == Keys.PRESENT;
        if(present) {

            mParticipation = in.readInt();
        }else{
            mParticipation = 0;
        }

        present = in.readByte() == Keys.PRESENT;
        if(present) {

            mCurrentTicket = in.readInt();
        }else{
            mCurrentTicket = 0;
        }

        present = in.readByte() == Keys.PRESENT;
        if(present) {

            mMaxTicket = in.readInt();
        }else{
            mMaxTicket = 0;
        }
    }

    public static Event fromJson(final JSONObject jsonObject) throws JSONException{

        final String title = jsonObject.getString(Keys.TITLE);
        final String description = jsonObject.getString(Keys.DESCRIPTION);
        final String startDate = jsonObject.getString(Keys.STARTDATE);
        final String endDate = jsonObject.getString(Keys.ENDDATE);
        final String tag = jsonObject.getString(Keys.TAG);
        final String address = jsonObject.getString(Keys.ADDRESS);
        final String price = jsonObject.getString(Keys.PRICE);
        final String image = jsonObject.getString(Keys.IMAGE);
        final float latitude = BigDecimal.valueOf(jsonObject.getDouble(Keys.LATITUDE)).floatValue();
        final float longitude = BigDecimal.valueOf(jsonObject.getDouble(Keys.LONGITUDE)).floatValue();

        Builder builder = Builder.create(title, description, startDate, endDate).withTag(tag).withAddress(address)
                .withLocation(latitude,longitude).withPrice(price).withImage(image);

        if(jsonObject.has(Keys.PARTICIPATION)) {
            builder.withParticipation(jsonObject.getInt(Keys.PARTICIPATION));
        }
        if(jsonObject.has(Keys.CURRENTTIC)) {
            builder.withCurrentTic(jsonObject.getInt(Keys.CURRENTTIC));
        }
        if(jsonObject.has(Keys.MAXTIC)) {
            builder.withMaxTic(jsonObject.getInt(Keys.MAXTIC));
        }

        return builder.build();
    }

    public JSONObject toJson() throws JSONException{

        final JSONObject jsonObject = new JSONObject();

        jsonObject.put(Keys.TITLE, mTitle);
        jsonObject.put(Keys.DESCRIPTION, mDescription);
        jsonObject.put(Keys.STARTDATE, mStartDate);
        jsonObject.put(Keys.ENDDATE, mEndDate);
        jsonObject.put(Keys.TAG, mTag);
        jsonObject.put(Keys.ADDRESS, mAddress);
        jsonObject.put(Keys.LATITUDE, mLatitude);
        jsonObject.put(Keys.LONGITUDE, mLongitude);
        jsonObject.put(Keys.PRICE, mPrice);
        jsonObject.put(Keys.IMAGE, mImage);

        if(mParticipation != 0) {
            jsonObject.put(Keys.PARTICIPATION, mParticipation);
        }
        if(mCurrentTicket != 0) {
            jsonObject.put(Keys.CURRENTTIC, mCurrentTicket);
        }
        if(mMaxTicket != 0) {
            jsonObject.put(Keys.MAXTIC, mMaxTicket);
        }

        return jsonObject;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(mTitle);
        dest.writeString(mDescription);
        dest.writeString(mStartDate);
        dest.writeString(mEndDate);
        dest.writeString(mTag);
        dest.writeString(mAddress);
        dest.writeFloat(mLatitude);
        dest.writeFloat(mLongitude);
        dest.writeString(mPrice);
        dest.writeString(mImage);

        if(mParticipation != 0) {
            dest.writeByte(Keys.PRESENT);
            dest.writeInt(mParticipation);
        }
        else
            dest.writeByte(Keys.NOT_PRESENT);

        if(mCurrentTicket != 0) {
            dest.writeByte(Keys.PRESENT);
            dest.writeInt(mCurrentTicket);
        }
        else
            dest.writeByte(Keys.NOT_PRESENT);

        if(mMaxTicket != 0) {
            dest.writeByte(Keys.PRESENT);
            dest.writeInt(mMaxTicket);
        }
        else
            dest.writeByte(Keys.NOT_PRESENT);
    }

    public static class Keys{

        public static final String TITLE = "title";
        public static final String DESCRIPTION = "description";
        public static final String STARTDATE = "startDate";
        public static final String ENDDATE = "endDate";
        public static final String TAG = "tag";
        public static final String ADDRESS = "address";
        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "longitude";
        public static final String PRICE = "price";
        public static final String PARTICIPATION = "participation";
        public static final String CURRENTTIC= "currentTicket";
        public static final String MAXTIC = "maxTicket";
        public static final String IMAGE = "image";

        public static final Byte PRESENT = 1;
        public static final Byte NOT_PRESENT = 0;

        public static final String EVENT = "event";

    }


    public static class Builder{

        private String mTitle;
        private String mDescription;
        private String mStartDate;
        private String mEndDate;
        private String mTag;
        private String mAddress;
        private float mLatitude;
        private float mLongitude;
        private String mPrice;
        private int mParticipation;
        private String mImage;
        private int mCurrentTicket;
        private int mMaxTicket;


        private Builder(final String title, final String description, final String startDate, final String endDate){

            this.mTitle = title;
            this.mDescription = description;
            this.mStartDate = startDate;
            this.mEndDate = endDate;
        }

        public static Builder create(final String title, final String description,
                                     final String startDate, final String endDate){

            return new Builder(title, description, startDate, endDate);
        }

        public Builder withTag(final String tag){

            this.mTag = tag;
            return this;
        }

        public Builder withAddress(final String address){

            this.mAddress = address;
            return this;
        }

        public Builder withLocation(final float latitude, final float longitude){

            this.mLatitude = latitude;
            this.mLongitude = longitude;
            return this;
        }

        public Builder withPrice(final String price) {
            this.mPrice = price;
            return this;
        }

        public Builder withParticipation(final int participation) {
            this.mParticipation = participation;
            return this;
        }

        public Builder withImage(final String image) {
            this.mImage = image;
            return this;
        }

        public Builder withCurrentTic(final int currentTicket) {
            this.mCurrentTicket = currentTicket;
            return this;
        }

        public Builder withMaxTic(final int maxTicket) {
            this.mMaxTicket = maxTicket;
            return this;
        }

        public Event build(){

            return new Event(mTitle, mDescription, mStartDate, mEndDate, mTag, mAddress, mLatitude,
                            mLongitude, mPrice, mParticipation, mImage, mCurrentTicket, mMaxTicket);
        }
    }
}

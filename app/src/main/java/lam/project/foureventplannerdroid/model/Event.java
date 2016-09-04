package lam.project.foureventplannerdroid.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.ParseException;

import lam.project.foureventplannerdroid.utils.DateConverter;

/**
 * Created by spino on 28/07/16.
 */
public class Event implements Parcelable{

    public String mId;

    public final String mTitle;

    private final String mDescription;

    public final String mStartDate;

    private String mEndDate;

    private final String mTag;

    private final String mAddress;

    private final float mLatitude;

    private final float mLongitude;

    private final String mPrice;

    private int mParticipation;

    private int mMaxTicket;

    public Double mDistance;

    public final String mImage;

    private final String mAuthor;

    private Event(final String id, final String title, final String description, final String startDate,
                  final String endDate, final String tag, final String address, final float latitude,
                  final float longitude, final String price, final int participation, final String image,
                  final int maxTic, final String author, final Double distance){

        this.mId = id;
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
        this.mMaxTicket = maxTic;
        this.mAuthor = author;
        this.mDistance = distance;

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

    public Event addEndDate(String mEndDate) {
        this.mEndDate = mEndDate;
        return this;
    }

    public Event addMaxTicket(int mMaxTicket) {
        this.mMaxTicket = mMaxTicket;
        return this;
    }

    public boolean hasDistance() {

        return mDistance != null;
    }

    private Event(Parcel in) {

        boolean present = in.readByte() == Keys.PRESENT;
        if(present) {

            mId = in.readString();
        }else{
            mId = null;
        }

        mTitle = in.readString();
        mDescription = in.readString();
        mStartDate = in.readString();
        mTag = in.readString();
        mAddress = in.readString();
        mLatitude = in.readFloat();
        mLongitude = in.readFloat();
        mPrice = in.readString();
        mImage = in.readString();

        present = in.readByte() == Keys.PRESENT;
        if(present) {

            mEndDate = in.readString();
        }else{
            mEndDate = null;
        }

        present = in.readByte() == Keys.PRESENT;
        if(present) {

            mParticipation = in.readInt();
        }else{
            mParticipation = 0;
        }

        present = in.readByte() == Keys.PRESENT;
        if(present) {

            mMaxTicket = in.readInt();
        }else{
            mMaxTicket = 0;
        }

        mAuthor = in.readString();
    }

    public static Event fromJson(final JSONObject jsonObject) throws JSONException{

        final String title = jsonObject.getString(Keys.TITLE);
        final String description = jsonObject.getString(Keys.DESCRIPTION);

        //TODO
        final String startDate = DateConverter.dateFromMillis(jsonObject.getLong(Keys.START_DATE));
        final String author = jsonObject.getString(Keys.AUTHOR);

        final String tag = jsonObject.getString(Keys.TAG);
        final String address = jsonObject.getString(Keys.ADDRESS);
        final String price = jsonObject.getString(Keys.PRICE);
        final String image = jsonObject.getString(Keys.IMAGE);

        //prendere latitudine e longitudine
        JSONArray coordinates = jsonObject.getJSONObject("loc").getJSONArray("coordinates");

        final float latitude = BigDecimal.valueOf(coordinates.getDouble(1)).floatValue();
        final float longitude = BigDecimal.valueOf(coordinates.getDouble(0)).floatValue();

        Builder builder = Builder.create(title, description, startDate,author).withTag(tag).withAddress(address)
                .withLocation(latitude,longitude).withPrice(price).withImage(image);

        if(jsonObject.has(Keys.END_DATE)) {

            //TODO
            builder.withEndDate(DateConverter.dateFromMillis(jsonObject.getLong(Keys.END_DATE)));
        }

        if(jsonObject.has(Keys.IMAGE)) {

            builder.withImage(jsonObject.getString(Keys.IMAGE));
        }

        if(jsonObject.has(Keys.ID)) {

            builder.withId(jsonObject.getString(Keys.ID));
        }


        if(jsonObject.has(Keys.PARTICIPATION)) {
            builder.withParticipation(jsonObject.getInt(Keys.PARTICIPATION));
        }

        if(jsonObject.has(Keys.MAX_TICKETS)) {
            builder.withMaxTic(jsonObject.getInt(Keys.MAX_TICKETS));
        }

        if(jsonObject.has(Keys.DISTANCE)) {

            builder.withDistance(jsonObject.getDouble(Keys.DISTANCE) / 1000);
        }


        return builder.build();
    }

    public JSONObject toJson() throws JSONException{

        final JSONObject jsonObject = new JSONObject();

        if(mId != null) {

            jsonObject.put(Keys.ID,mId);
        }

        jsonObject.put(Keys.TITLE, mTitle);
        jsonObject.put(Keys.DESCRIPTION, mDescription);
        jsonObject.put(Keys.AUTHOR,mAuthor);

        jsonObject.put(Keys.TAG, mTag);
        jsonObject.put(Keys.ADDRESS, mAddress);
        jsonObject.put(Keys.LATITUDE, mLatitude);
        jsonObject.put(Keys.LONGITUDE, mLongitude);
        jsonObject.put(Keys.PRICE, mPrice);
        jsonObject.put(Keys.IMAGE, mImage);

        jsonObject.put(Keys.START_DATE, mStartDate);

        if(mEndDate != null) {
            jsonObject.put(Keys.END_DATE, mEndDate);
        }

        if(mParticipation != 0) {
            jsonObject.put(Keys.PARTICIPATION, mParticipation);
        }
        if(mMaxTicket != 0) {
            jsonObject.put(Keys.MAX_TICKETS, mMaxTicket);
        }

        return jsonObject;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        if(mId != null) {
            dest.writeByte(Keys.PRESENT);
            dest.writeString(mId);

        } else {
            dest.writeByte(Keys.NOT_PRESENT);
        }
        dest.writeString(mTitle);
        dest.writeString(mDescription);
        dest.writeString(mStartDate);
        dest.writeString(mTag);
        dest.writeString(mAddress);
        dest.writeFloat(mLatitude);
        dest.writeFloat(mLongitude);
        dest.writeString(mPrice);
        dest.writeString(mImage);

        if(mEndDate != null) {
            dest.writeByte(Keys.PRESENT);
            dest.writeString(mEndDate);
        }
        else
            dest.writeByte(Keys.NOT_PRESENT);

        if(mParticipation != 0) {
            dest.writeByte(Keys.PRESENT);
            dest.writeInt(mParticipation);
        }
        else
            dest.writeByte(Keys.NOT_PRESENT);

        if(mMaxTicket != 0) {
            dest.writeByte(Keys.PRESENT);
            dest.writeInt(mMaxTicket);
        }
        else
            dest.writeByte(Keys.NOT_PRESENT);

        dest.writeString(mAuthor);
    }

    public static class Keys{

        static final String ID = "_id";
        static final String TITLE = "title";
        static final String DESCRIPTION = "description";
        static final String START_DATE = "start_date";
        static final String END_DATE = "end_date";
        static final String TAG = "tag";
        static final String ADDRESS = "address";
        static final String LATITUDE = "latitude";
        static final String LONGITUDE = "longitude";
        static final String PRICE = "price";
        static final String PARTICIPATION = "participations";
        static final String MAX_TICKETS = "tickets";
        static final String IMAGE = "image";
        static final String AUTHOR = "author";
        static final String DISTANCE = "distance";
        public static final String EVENT = "event";


        static final Byte PRESENT = 1;
        static final Byte NOT_PRESENT = 0;
    }

    public static class Builder{

        private String mId;
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
        private int mMaxTicket;
        private String mAuthor;
        private Double mDistance;


        private Builder(final String title, final String description, final String startDate, final String author){

            this.mTitle = title;
            this.mDescription = description;
            this.mStartDate = startDate;
            this.mAuthor = author;
        }

        public static Builder create(final String title, final String description,
                                     final String startDate, final String author){

            return new Builder(title, description, startDate, author);
        }

        public Builder withTag(final String tag){

            this.mTag = tag;
            return this;
        }

        public Builder withAddress(final String address){

            this.mAddress = address;
            return this;
        }

        Builder withLocation(final float latitude, final float longitude){

            this.mLatitude = latitude;
            this.mLongitude = longitude;
            return this;
        }

        public Builder withPrice(final String price) {
            this.mPrice = price;
            return this;
        }

        Builder withEndDate(final String endDate) {
            this.mEndDate = endDate;
            return this;
        }

        Builder withParticipation(final int participation) {
            this.mParticipation = participation;
            return this;
        }

        public Builder withImage(final String image) {
            this.mImage = image;
            return this;
        }

        Builder withMaxTic(final int maxTicket) {
            this.mMaxTicket = maxTicket;
            return this;
        }

        Builder withId(final String id) {
            this.mId = id;
            return this;
        }

        Builder withDistance(final Double distance) {
            this.mDistance = distance;
            return this;
        }

        public Event build(){

            return new Event(mId,mTitle, mDescription, mStartDate, mEndDate, mTag, mAddress, mLatitude,
                            mLongitude, mPrice, mParticipation, mImage, mMaxTicket,mAuthor, mDistance);
        }
    }
}

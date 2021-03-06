package lam.project.foureventplannerdroid.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.ParseException;

import lam.project.foureventplannerdroid.utils.DateConverter;

/**
 * Classe che rappresenta il modello del planner, con i relativi campi
 */
public class Planner implements Parcelable{

    public final String email;

    public String name;

    public String birthDate;

    public String location;

    public String role;

    public String gender;

    public float balance;

    public String image;

    private Planner(final String email, final String name,
                    final String birthDate, final String location, final String role, final String gender,
                    final float balance, final String image){

        this.email = email;
        this.name = name;
        this.birthDate = birthDate;
        this.location = location;
        this.role = role;
        this.gender = gender;
        this.balance = balance;
        this.image = image;
    }

    public void updateBalance(float amount) {

        this.balance += amount;
    }

    public Planner addName(String name) {
        this.name = name;
        return this;
    }

    public Planner addLocation(String location) {
        this.location = location;
        return this;
    }

    public Planner addRole(String role) {
        this.role = role;
        return this;
    }

    public Planner addGender(String gender) {
        this.gender = gender;
        return this;
    }

    public Planner addBirthDate(String birthDate) {
        this.birthDate = birthDate;
        return this;
    }

    public Planner updateImage(String image) {
        this.image = image;
        return this;
    }

    //region metodi parcelable

    public static final Creator<Planner> CREATOR = new Creator<Planner>() {
        @Override
        public Planner createFromParcel(Parcel in) {
            return new Planner(in);
        }

        @Override
        public Planner[] newArray(int size) {
            return new Planner[size];
        }
    };

    protected Planner(Parcel in) {

        email = in.readString();
        balance = in.readFloat();

        boolean present = in.readByte() == Keys.PRESENT;
        if(present) {
            name = in.readString();
        }
        else
            name = null;

        present = in.readByte() == Keys.PRESENT;
        if(present) {
            birthDate = in.readString();
        }
        else
            birthDate = null;

        present = in.readByte() == Keys.PRESENT;
        if(present) {
            location = in.readString();
        }
        else
            location = null;

        present = in.readByte() == Keys.PRESENT;
        if(present) {
            role = in.readString();
        }
        else
            role = null;

        present = in.readByte() == Keys.PRESENT;
        if(present) {
            gender = in.readString();
        }
        else
            gender = null;


    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeFloat(balance);

        if (name != null) {
            dest.writeByte(Keys.PRESENT);
            dest.writeString(name);
        }
        else
            dest.writeByte(Keys.NOT_PRESENT);

        if (birthDate != null) {
            dest.writeByte(Keys.PRESENT);
            dest.writeString(birthDate);
        }
        else
            dest.writeByte(Keys.NOT_PRESENT);

        if (location != null) {
            dest.writeByte(Keys.PRESENT);
            dest.writeString(location);
        }
        else
            dest.writeByte(Keys.NOT_PRESENT);

        if (role != null) {
            dest.writeByte(Keys.PRESENT);
            dest.writeString(role);
        }
        else
            dest.writeByte(Keys.NOT_PRESENT);

        if (gender != null) {
            dest.writeByte(Keys.PRESENT);
            dest.writeString(gender);
        }
        else
            dest.writeByte(Keys.NOT_PRESENT);

        if (image != null) {
            dest.writeByte(Keys.PRESENT);
            dest.writeString(image);
        }
        else
            dest.writeByte(Keys.NOT_PRESENT);
    }

    //endregion

    //region lettura/scrittura Json

    public static Planner fromJson(final JSONObject jsonObject) throws JSONException{

        final String email = jsonObject.getString(Keys.EMAIL);

        Builder builder = Builder.create(email);

        if (jsonObject.has(Keys.NAME)) {
            builder.withName(jsonObject.getString(Keys.NAME));
        }

        if(jsonObject.has(Keys.BIRTH_DATE)){

            builder.withBirthDate(DateConverter.dateFromMillis(jsonObject.getLong(Keys.BIRTH_DATE)));
        }

        if(jsonObject.has(Keys.LOCATION)){

            builder.withLocation(jsonObject.getString(Keys.LOCATION));
        }

        if(jsonObject.has(Keys.ROLE)){

            builder.withRole(jsonObject.getString(Keys.ROLE));
        }

        if(jsonObject.has(Keys.GENDER)){

            builder.withGender(jsonObject.getString(Keys.GENDER));
        }

        final float balance = BigDecimal.valueOf(jsonObject.getDouble(Keys.BALANCE)).floatValue();

        if(balance > 0) {

            builder.withBalance(balance);
        }

        Planner planner = builder.build();

        if(jsonObject.has(Keys.IMAGE)){

            builder.withImage(jsonObject.getString(Keys.IMAGE));
        }

        return planner;
    }

    public JSONObject toJson() throws JSONException {

        final JSONObject jsonObject = new JSONObject();

        jsonObject.put(Keys.EMAIL, email);
        jsonObject.put(Keys.BALANCE,balance);

        if (name != null) {

            jsonObject.put(Keys.NAME, name);
        }

        if (birthDate != null) {

            try{

                jsonObject.put(Keys.BIRTH_DATE, DateConverter.dateToMillis(birthDate));

            } catch (ParseException e) {

                e.printStackTrace();
            }
        }

        if (location != null) {

            jsonObject.put(Keys.LOCATION, location);
        }

        if (role != null) {

            jsonObject.put(Keys.ROLE, role);
        }

        if (gender != null) {

            jsonObject.put(Keys.GENDER, gender);
        }

        if (image != null) {

            jsonObject.put(Keys.IMAGE, image);
        }

        return jsonObject;
    }

    //endregion

    //region Keys

    public static class Keys{

        public static final String EMAIL = "email";

        public static final String NAME = "name";

        public static final String BIRTH_DATE = "birth_date";

        public static final String LOCATION = "location";

        public static final String ROLE = "role";

        public static final String GENDER = "gender";

        public static final String USER = "user";

        public static final String BALANCE = "balance";

        public static final String IMAGE = "image";

        public static final Byte PRESENT = 1;

        public static final Byte NOT_PRESENT = 0;
    }

    //endregion

    //region Builder

    public static class Builder{

        private String mEmail;

        private String mName;

        private String mBirthDate;

        private String mLocation;

        private String mRole;

        private String mGender;

        private float mBalance;

        private String mImage;

        private Builder(final String email){

            this.mEmail = email;
        }

        public static Builder create(final String email){

            return new Builder(email);
        }

        public Builder withName(final String name){

            this.mName = name;
            return this;
        }

        public Builder withBirthDate(final String birthDate){

            this.mBirthDate = birthDate;
            return this;
        }

        public Builder withLocation(final String location){

            this.mLocation = location;
            return this;
        }

        public Builder withRole(final String role){

            this.mRole = role;
            return this;
        }

        public Builder withGender(final String gender){

            this.mGender = gender;
            return this;
        }

        public Builder withBalance(final float balance) {

            this.mBalance = balance;
            return this;
        }

        public Builder withImage(final String image) {

            this.mImage = image;
            return this;
        }

        public Planner build(){
            return new Planner(mEmail,mName,mBirthDate,mLocation, mRole, mGender, mBalance, mImage);
        }
    }

    //endregion
}

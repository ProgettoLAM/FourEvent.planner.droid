package lam.project.foureventplannerdroid.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;

/**
 * Created by spino on 29/07/16.
 */
public class Planner implements Parcelable{

    public final String email;

    public String password;

    public String name;

    public String birthDate;

    public String location;

    public String role;

    public String gender;

    public float balance;

    private Planner(final String email, final String password, final String name,
                    final String birthDate, final String location, final String role, final String gender, final float balance){

        this.email = email;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.location = location;
        this.role = role;
        this.gender = gender;
        this.balance = balance;
    }

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

    public Planner updatePassword(String password) {
        this.password = password;
        return this;
    }

    protected Planner(Parcel in) {

        email = in.readString();
        password = in.readString();
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


    public static Planner fromJson(final JSONObject jsonObject) throws JSONException{

        final String email = jsonObject.getString(Keys.EMAIL);
        final String password = jsonObject.getString(Keys.PASSWORD);

        Builder builder = Builder.create(email,password);

        if (jsonObject.has(Keys.NAME)) {
            builder.withName(jsonObject.getString(Keys.NAME));
        }

        if(jsonObject.has(Keys.BIRTH_DATE)){

            builder.withBirthDate(jsonObject.getString(Keys.BIRTH_DATE));
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

        return planner;
    }

    public JSONObject toJson() throws JSONException {

        final JSONObject jsonObject = new JSONObject();

        jsonObject.put(Keys.EMAIL, email);
        jsonObject.put(Keys.PASSWORD, password);
        jsonObject.put(Keys.BALANCE,balance);

        if (name != null) {

            jsonObject.put(Keys.NAME, name);
        }

        if (birthDate != null) {

            jsonObject.put(Keys.BIRTH_DATE, birthDate);
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

        return jsonObject;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(password);
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
    }

    public static class Keys{

        public static final String EMAIL = "email";

        public static final String PASSWORD = "password";

        public static final String NAME = "name";

        public static final String BIRTH_DATE = "birth_date";

        public static final String LOCATION = "location";

        public static final String ROLE = "role";

        public static final String GENDER = "gender";

        public static final String USER = "user";

        public static final String BALANCE = "balance";

        public static final Byte PRESENT = 1;

        public static final Byte NOT_PRESENT = 0;
    }

    public static class Builder{

        private String mEmail;

        private String mPassword;

        private String mName;

        private String mBirthDate;

        private String mLocation;

        private String mRole;

        private String mGender;

        private float mBalance;

        //TODO completare la classe, aggiungendo i parametri, completare i metodi e usare la classe
        //TODO parcelable, utilizzare il metodo opzionale anche per trasformazione JSON

        private Builder(final String email,final String password){

            this.mEmail = email;
            this.mPassword = password;
        }

        public static Builder create(final String email, final String password){

            return new Builder(email,password);
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

        public Planner build(){
            return new Planner(mEmail,mPassword,mName,mBirthDate,mLocation, mRole, mGender,mBalance);
        }
    }
}

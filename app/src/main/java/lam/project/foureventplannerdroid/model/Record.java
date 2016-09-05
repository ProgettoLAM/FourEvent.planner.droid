package lam.project.foureventplannerdroid.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.ParseException;

import lam.project.foureventplannerdroid.utils.DateConverter;

/**
 * Classe che rappresenta il modello di un record, con i relativi campi
 */
public class Record {

    public final String mId;

    public final String mDate;

    public final float mAmount;

    public final String mType;

    public final String mUser;

    public final String mEvent;

    public Record(final String id, final String date, final float amount, final String type,
                  final String user, final String event) {

        this.mId = id;
        this.mDate = date;
        this.mAmount = amount;
        this.mType = type;
        this.mUser = user;
        this.mEvent = event;
    }

    //Region lettura/scrittura Json

    public static Record fromJson(JSONObject jsonObject) throws JSONException {

        float amount = BigDecimal.valueOf(jsonObject.getDouble(Keys.AMOUNT)).floatValue();
        String type = jsonObject.getString(Keys.TYPE);
        String user = jsonObject.getString(Keys.USER);

        Builder builder = Builder.create(amount,type,user);

        if(jsonObject.has(Keys.EVENT)) {

            builder.withEvent(jsonObject.getString(Keys.EVENT));
        }

        if(jsonObject.has(Keys.DATE)) {

            builder.withDate(jsonObject.getLong(Keys.DATE));
        }

        if (jsonObject.has(Keys.ID)) {

            builder.withId(jsonObject.getString(Keys.ID));
        }

        return builder.build();
    }

    public JSONObject toJson () throws JSONException, ParseException {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put(Keys.AMOUNT,mAmount);
        jsonObject.put(Keys.TYPE,mType);
        jsonObject.put(Keys.USER,mUser);

        if(mEvent != null) {

            jsonObject.put(Keys.EVENT,mEvent);
        }

        if(mDate != null) {

            jsonObject.put(Keys.DATE,DateConverter.toMillis(mDate));
        }

        if(mId != null) {

            jsonObject.put(Keys.ID,mId);
        }

        return jsonObject;
    }

    //Endregion

    //Region Builder

    public static class Builder {

        private String id;
        private String date;
        private float amount;
        private String type;
        private String user;
        private String event;

        private Builder(float amount, String type, String user) {

            this.amount = amount;
            this.type = type;
            this.user = user;
        }

        public static Builder create(float amount, String type, String user) {

            return new Builder(amount,type,user);
        }

        Builder withDate(long date) {

            this.date = DateConverter.fromMillis(date);
            return this;
        }

        public Builder withEvent(String event) {

            this.event = event;
            return this;
        }

        Builder withId(String id) {

            this.id = id;
            return this;
        }

        public Record build() {

            return new Record(id,date,amount,type,user,event);
        }
    }

    //Endregion

    //Region Keys

    public static class Keys {

        public static String ID = "_id";
        static String DATE = "date";
        public static String AMOUNT = "amount";
        public static String TYPE = "type";
        public static String USER = "user";
        public static String EVENT = "event";
        public static String RECORD =  "record";

        public static final String RECHARGE = "Ricarica conto";
        public static final String BUY_TICKETS = "Aumento biglietti";
        public static final String SPONSOR = "Popolari";

    }

    //Endregion
}

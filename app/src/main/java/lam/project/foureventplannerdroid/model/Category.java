package lam.project.foureventplannerdroid.model;

/**
 * Created by Vale on 24/08/2016.
 */

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by spino on 10/08/16.
 */
public class Category implements Parcelable{

    public final int id;
    public final String name;

    public Category(final int id, final String name){

        this.id = id;
        this.name = name;
    }

    protected Category(Parcel in) {
        id = in.readInt();
        name = in.readString();
    }

    public static final Creator<Category> CREATOR = new Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel in) {
            return new Category(in);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };

    public static Category fromJson(final JSONObject jsonObject) throws JSONException {

        final int id = jsonObject.getInt(Keys.ID);
        final String name = jsonObject.getString(Keys.NAME);

        return Builder.create(id,name).build();
    }

    public JSONObject toJson() throws JSONException{

        final JSONObject jsonObject = new JSONObject();

        jsonObject.put(Keys.ID,id);
        jsonObject.put(Keys.NAME, name);

        return jsonObject;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
    }

    public static class Keys{

        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String MUSICA = "MUSICA";
        public static final String TECNOLOGIA = "TECNOLOGIA";
        public static final String LETTURA = "LETTURA";
        public static final String SPORT = "SPORT";
        public static final String MODA = "MODA";
        public static final String FOTOGRAFIA = "FOTOGRAFIA";
        public static final String MARKETNG = "MARKETING";
        public static final String NATURA = "NATURA";
        public static final String ARTE = "ARTE";
        public static final String CIBO = "CIBO";
        public static final String VIAGGI = "VIAGGI";
        public static final String VIDEOGIOCHI = "VIDEOGIOCHI";

        public static final String[] categories = {MUSICA, TECNOLOGIA, LETTURA, SPORT, MODA,
                FOTOGRAFIA, MARKETNG, NATURA, ARTE, CIBO, VIAGGI, VIDEOGIOCHI};

    }

    public static class Builder{


        public final int id;
        public final String name;

        private Builder(final int id,final String name){

            this.id = id;
            this.name = name;
        }

        public static Builder create(final int id, final String name){

            return new Builder(id,name);
        }

        public Category build(){

            return new Category(this.id,this.name);
        }
    }
}
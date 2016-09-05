package lam.project.foureventplannerdroid.utils.connection;

import android.net.Uri;

/**
 * Classe per la definizione degli Uri passati nelle richieste al server
 */
public class FourEventUri {

    //Region Builder

    public static class Builder {

        private Uri.Builder mRequestedUriBuilder;

        private Builder(String key) {

            mRequestedUriBuilder = Uri.parse(Keys.BASE).buildUpon()
                    .appendPath(key);
        }

        public static Builder create(String key) {

            return new Builder(key);
        }

        public Builder appendPath(String newSegment) {

            mRequestedUriBuilder.appendPath(newSegment);

            return this;
        }

        //Percorso con simboli come la @
        public Builder appendEncodedPath(String newSegment) {

            mRequestedUriBuilder.appendEncodedPath(newSegment);

            return this;
        }

        public String getUri () {

            return mRequestedUriBuilder.build().toString();
        }
    }

    //Endregion

    //Region Keys

    public static class Keys {

        private static final String BASE = "http://annina.cs.unibo.it:8080/api/";
        public static final String EVENT = "event";
        public static final String RECORD = "record";
        public static final String PLANNER = "planner";
        public static final String TICKET = "ticket";

    }

    //Endregion
}

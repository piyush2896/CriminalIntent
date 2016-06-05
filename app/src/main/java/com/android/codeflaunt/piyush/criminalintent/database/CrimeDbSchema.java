package com.android.codeflaunt.piyush.criminalintent.database;

/**
 * Created by Piyush on 05-Mar-16.
 */
public class CrimeDbSchema {

    public static final class CrimeTable{
        public static final String NAME = "crimes";

        public static final class Cols{
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
            public static final String SUSPECT = "suspect";
            public static final String SUSPECT_NO = "phone_no";
        }
    }
}

package com.thecoffeecoders.chatex.db;

import android.provider.BaseColumns;

public class EquationContract {

    private EquationContract(){

    }

    public static final class EquationEntry implements BaseColumns{

        public static final String TABLE_NAME = "tblEquations";
        public static final String COLUMN_EQUATION = "equation";
    }
}

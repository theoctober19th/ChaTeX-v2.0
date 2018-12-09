package com.thecoffeecoders.chatex.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class EquationDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "chatexClientDatabase.db";
    public static final int DATABSE_VERSION = 1;

    private ArrayList<String> commonEquations;

    public EquationDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABSE_VERSION);
        commonEquations = new ArrayList<>();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_EQUATIONS_TABLE =
                "CREATE TABLE " + EquationContract.EquationEntry.TABLE_NAME + " ( "
                + EquationContract.EquationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + EquationContract.EquationEntry.COLUMN_EQUATION + " TEXT NOT NULL "
                + ");";
        db.execSQL(SQL_CREATE_EQUATIONS_TABLE);
        populateCommonEquations();
        addCommmonEquationsToDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void populateCommonEquations(){
        commonEquations.add("a^2 + b^2 = c^2");
        commonEquations.add("x^2 + y^2 = z^2");
        commonEquations.add("A = l \\times b");
        commonEquations.add("V = l \\times b \\times h");
        commonEquations.add("E = mc^2");
        commonEquations.add("\\log(xy) = \\log(x) + \\log(y)");
        commonEquations.add("\\frac{\\mathrm{df} }{\\mathrm{d}t} = \\lim_{h \\rightarrow 0} \\frac{f(t+h)-f(t)}{h}");
        commonEquations.add("F = G \\frac{m_1 m_2}{r^2}");
        commonEquations.add("i^2 = -1");
        commonEquations.add("F - E + V = 2");
        commonEquations.add("\\Phi (x) = \\frac{1}{\\sqrt{2\\pi\\sigma}} e^{\\frac{(x-\\mu)^2}{2\\sigma ^2}}");
        commonEquations.add("\\frac{\\partial^2 u}{\\partial t^2} = c^2 \\frac{\\partial^2 u}{\\partial x^2}");
        commonEquations.add("F(\\omega) = \\int_{-\\infty}^{\\infty}f(x)e^{-2\\pi i x \\omega}dx");
        commonEquations.add("\\rho (\\frac{\\partial v}{\\partial t}+\\nu \\cdot \\triangledown \\nu) = -\\triangledown p + \\triangledown \\cdot T + f"); //navier stokes theorem
        commonEquations.add("\\triangledown \\cdot E = 0");
        commonEquations.add("\\triangledown \\cdot H = 0");
        commonEquations.add("\\triangledown \\times E = -\\frac{1}{c}\\frac{\\partial H}{\\partial t}");
        commonEquations.add("\\triangledown \\times H = \\frac{1}{c}\\frac{\\partial E}{\\partial t}");
        commonEquations.add("dS \\geq 0");
        commonEquations.add("i \\hbar \\frac{\\partial }{\\partial t} - \\Psi = \\hat{H} \\Psi");
        commonEquations.add("H = - \\sum p(x) \\log p(x)");
        commonEquations.add("X_{t+1} = kx_t(1-x_t)");
        commonEquations.add("A = \\pi r^2");
        commonEquations.add("V = \\frac{4\\pi}{3}r^3");
        commonEquations.add("I = \\frac{PTR}{100}");
        commonEquations.add("F = ma");
        commonEquations.add("S = ut + \\frac12 at^2");
        commonEquations.add("v = u + at");
    }

    private void addCommmonEquationsToDatabase(SQLiteDatabase db){
        for (String string:commonEquations){
            ContentValues contentValues = new ContentValues();
            contentValues.put(EquationContract.EquationEntry.COLUMN_EQUATION, string);
            db.insert(EquationContract.EquationEntry.TABLE_NAME, null, contentValues);
        }
    }
}

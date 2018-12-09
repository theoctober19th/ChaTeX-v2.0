package com.thecoffeecoders.chatex.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {

    static int NOTIFICATION_ID = 0;

    static public String convertTimestampToDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);

        long secondDifference = (System.currentTimeMillis() - time )/ 1000;
        if( secondDifference < 24*60*60){
            String date = DateFormat.format("hh:mm a", cal).toString();
            return date;
        }else if(secondDifference < 2*24*60*60){
            return "Yesterday";
        }else if(secondDifference < 24*60*60*365){
            String date = DateFormat.format("MMM dd", cal).toString();
            return date;
        }else{
            String date = DateFormat.format("yyyy", cal).toString();
            return date;
        }
    }

    static public String getElapsedTime(long time){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);

        long millisDifference = System.currentTimeMillis()  - time;
        long secondDifference = millisDifference/ 1000;
        if( secondDifference < 60){
            return "Just now";
        }else if(secondDifference < 60*60){
            long elapsedTime = secondDifference/60;
            return String.valueOf(elapsedTime) + "m ago";
        }else if(secondDifference < 60*60*24){
            long elapsedTime = secondDifference/3600;
            return String.valueOf(elapsedTime) + "h ago";
        }else{
            return "";
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static TextDrawable getTextDrawable(String name, String key, String style){
        ColorGenerator generator = ColorGenerator.MATERIAL;
        char ch = name.charAt(0);
        String letter = Character.toString(ch);
        int color = generator.getColor(key);
        TextDrawable textDrawable;
        if(style.equals("rectangle")){
            textDrawable = TextDrawable.builder()
                    .buildRect(letter, color);
        }else if(style.equals("round_rectangle")){
            textDrawable = TextDrawable.builder()
                    .buildRoundRect(letter, color, 10);
        }else if(style.equals("round")){
            textDrawable = TextDrawable.builder()
                    .buildRound(letter, color);
        }else{
            //default is round
            textDrawable = TextDrawable.builder()
                    .buildRound(letter, color);
        }
        return textDrawable;

    }

    public static Bitmap getRoundedImage(Context context, String uriString) throws IOException {
//        Uri imageUri = Uri.parse(uriString);
//        Bitmap original = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
//        Bitmap imageRounded = Bitmap.createBitmap(original.getWidth(), original.getHeight(), original.getConfig());
//        Canvas canvas = new Canvas(imageRounded);
//        Paint mpaint = new Paint();
//        mpaint.setAntiAlias(true);
//        mpaint.setShader(new BitmapShader(original, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
//        canvas.drawRoundRect((new RectF(0, 0, original.getWidth(), original.getHeight())), 100, 100, mpaint);// Round Image Corner 100 100 100 100
//        return imageRounded;

        return null;
    }

    public static int createID(){
        return (int)System.currentTimeMillis()/1000;
    }

    public static int getUniqueInteger(){
        return NOTIFICATION_ID++;
    }
}

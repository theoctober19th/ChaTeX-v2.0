package com.thecoffeecoders.chatex.misc;

public class Constants {
    public static int CHATS_COUNT = 0;

    public static void increaseUnreadChatsCount(){
        CHATS_COUNT ++;
    }

    public static void decreaseUnreadChatsCount(){
        if(CHATS_COUNT > 0){
            CHATS_COUNT --;
        }
    }
}

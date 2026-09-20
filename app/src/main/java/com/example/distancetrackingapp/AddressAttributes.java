package com.example.distancetrackingapp;

import java.io.Serializable;

public class AddressAttributes implements Serializable{
    private String addy;
    private String time;

    public AddressAttributes(String addy, String time){
        this.addy = addy;
        this.time = time;
    }

   // public String getNumber(){
   //     return number;
   // }
    public String getAddy(){
        return addy;
    }
    public String getTime(){
        return time;
    }
    //public void setNumber(){
   //     this.number = number;
   // }
    public void setAddy(){
        this.addy = addy;
    }
    public void setTime(){
        this.time = time;
    }
}

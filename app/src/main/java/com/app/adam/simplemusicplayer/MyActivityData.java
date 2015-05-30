package com.app.adam.simplemusicplayer;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Adam on 5/24/2015.
 */
public class MyActivityData {

    private  int LIMIT = 18 ;
    private ArrayList<Integer> on_bicycle = new ArrayList<>();
    private ArrayList<Integer> in_vechicle = new ArrayList<>();
    private ArrayList<Integer> on_foot = new ArrayList<>();
    private ArrayList<Integer> still = new ArrayList<>();
    private ArrayList<Integer> unknown = new ArrayList<>();
    private ArrayList<Integer> tilting = new ArrayList<>();

    public MyActivityData() {

    }

    public void checkData(){
        if(in_vechicle.size()>LIMIT ||
                on_bicycle.size()>LIMIT ||
                on_foot.size()>LIMIT ||
                tilting.size()>LIMIT ||
                still.size()>LIMIT ||
                unknown.size()>LIMIT ){
            on_bicycle = new ArrayList<>();
            in_vechicle = new ArrayList<>();
            on_foot = new ArrayList<>();
            still = new ArrayList<>();
            unknown = new ArrayList<>();
            tilting = new ArrayList<>();
        }
    }

    public void addData(int type,int data){

        checkData();

        if(type==0) { in_vechicle.add(data);}
        if(type==1) { on_bicycle.add(data);}
        if(type==2) { on_foot.add(data);}
        if(type==3) { still.add(data);}
        if(type==4) { unknown.add(data);}
        if(type==5) { tilting.add(data);}
    }
    private float getSum(ArrayList<Integer> tmp){
        float a=0;
        for(int i=0;i<tmp.size();i++){
            a=a+tmp.get(i);
        }
        return a;
    }
    private float getAllSum(){
        return (getSum(in_vechicle)+getSum(on_bicycle)+getSum(on_foot)+getSum(still)+getSum(tilting)+getSum(unknown));
    }
    public float getInVechiclePercent(){
        if(in_vechicle.size()>0) {
            return (getSum(in_vechicle)*100) / getAllSum();
        }
        return 0;
    }
    public float getOnFootPercent(){
        if(on_foot.size()>0){
            return (getSum(on_foot)*100)/getAllSum();
        }
        return 0;
    }
    public float getOnBicyclePercent(){
        if(on_bicycle.size()>0){
            return (getSum(on_bicycle)*100)/getAllSum();
        }
        return 0;

    }
    public float getStillPercent(){
        if(still.size()>0){
            return (getSum(still)*100)/getAllSum();
        }
        return 0;
    }
    public float getTiltingPercent(){
        if(tilting.size()>0){
            return (getSum(tilting)*100)/getAllSum();
        }
        return 0;

    }
    public float getUnknownPercent(){
        if(unknown.size()>0){
            return (getSum(unknown)*100)/getAllSum();
        }
        return 0;
    }
    public void printAll(){
        Log.i("MainActivity", "inVech " + in_vechicle.size());
        Log.i("MainActivity", "on bike " + on_bicycle.size());
        Log.i("MainActivity", "on_foot " + on_foot.size());
        Log.i("MainActivity", "still " + still.size());
        Log.i("MainActivity", "unknow " + unknown.size());
        Log.i("MainActivity", "tilt " + tilting.size());
    }
    public void setLIMIT(int LIMIT) {
        this.LIMIT = LIMIT;
    }
    public MySortData getMostRelevant(){

        float tmpInVechicle=getInVechiclePercent();
        float tmpOnFoot=getOnFootPercent();
        float tmpStill=getStillPercent();
        float tmponBike=getOnBicyclePercent();

        ArrayList<MySortData> tmpSortData=new ArrayList<>();
        tmpSortData.add(new MySortData("vechicle",tmpInVechicle));
        tmpSortData.add(new MySortData("foot",tmpOnFoot));
        tmpSortData.add(new MySortData("still",tmpStill));
        tmpSortData.add(new MySortData("bike",tmponBike));

        for(int i=0;i<tmpSortData.size();i++){
            for(int j=0;j<tmpSortData.size();j++){
                if(tmpSortData.get(i).getVal()>tmpSortData.get(j).getVal()){
                    MySortData asd = new MySortData(tmpSortData.get(i).getAct(),tmpSortData.get(i).getVal());
                    tmpSortData.get(i).setAct(tmpSortData.get(j).getAct());
                    tmpSortData.get(i).setVal(tmpSortData.get(j).getVal());
                    tmpSortData.get(j).setAct(asd.getAct());
                    tmpSortData.get(j).setVal(asd.getVal());

                }
            }
        }


        return tmpSortData.get(0);
    }
}

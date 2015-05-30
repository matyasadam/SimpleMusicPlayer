package com.app.adam.simplemusicplayer;

/**
 * Created by Adam on 5/24/2015.
 */
public class MySortData {


    public float getVal() {
        return val;
    }

    public void setVal(float val) {
        this.val = val;
    }

    private float val;

    public String getAct() {
        return act;
    }

    public void setAct(String act) {
        this.act = act;
    }

    private String act;

    public MySortData(String _act,float _val) {
        act=_act;
        val=_val;

    }

}

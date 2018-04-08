package com.life.yiwanchen.barcodescanner;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yiwanchen on 2018/4/8.
 */

public class FormItem implements Parcelable{
    public String id;
    public String name;
    public Boolean enabled;

    public FormItem(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.getString("id");
        this.name = jsonObject.getString("name");
        this.enabled = jsonObject.getBoolean("enabled");
    }

    protected FormItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        enabled = in.readByte() != 0;
    }

    public static final Creator<FormItem> CREATOR = new Creator<FormItem>() {
        @Override
        public FormItem createFromParcel(Parcel in) {
            return new FormItem(in);
        }

        @Override
        public FormItem[] newArray(int size) {
            return new FormItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeByte((byte) (enabled ? 1 : 0));
    }
}

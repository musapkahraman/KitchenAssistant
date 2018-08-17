/*
 * Reference
 * https://android.jlelse.eu/android-architecture-components-room-relationships-bf473510c14a#da9f
 * http://www.vogella.com/tutorials/AndroidParcelable/article.html
 */

package com.example.kitchen.data.local.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@SuppressWarnings({"NullableProblems", "WeakerAccess", "CanBeFinal"})
@Entity(tableName = "storage")
public class Food implements Parcelable {
    public static final Creator<Food> CREATOR = new Creator<Food>() {
        public Food createFromParcel(Parcel in) {
            return new Food(in);
        }

        public Food[] newArray(int size) {
            return new Food[size];
        }
    };
    @PrimaryKey(autoGenerate = true) public int id;
    public String food;
    public int amount;
    public String amountType;
    public long bestBefore;

    @Ignore
    public Food(int id, String food, int amount, String amountType, long bestBefore) {
        this.id = id;
        this.food = food;
        this.amount = amount;
        this.amountType = amountType;
        this.bestBefore = bestBefore;
    }

    public Food(String food, int amount, String amountType, long bestBefore) {
        this.food = food;
        this.amount = amount;
        this.amountType = amountType;
        this.bestBefore = bestBefore;
    }

    private Food(Parcel in) {
        id = in.readInt();
        food = in.readString();
        amount = in.readInt();
        amountType = in.readString();
        bestBefore = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(food);
        dest.writeInt(amount);
        dest.writeString(amountType);
        dest.writeLong(bestBefore);
    }

    @Override
    public String toString() {
        return super.toString() +
                "\nid: " + id +
                "\nfood: " + food +
                "\namount: " + amount +
                "\namountType: " + amountType +
                "\nbestBefore: " + bestBefore;
    }
}
